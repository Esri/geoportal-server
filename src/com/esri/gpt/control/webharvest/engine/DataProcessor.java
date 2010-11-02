/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.harvest.history.HeRecord;
import com.esri.gpt.catalog.harvest.history.HeUpdateReportRequest;
import com.esri.gpt.catalog.harvest.history.HeUpdateRequest;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.catalog.harvest.repository.HrUpdateLastSyncDate;
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.catalog.publication.CollectSourceUrisRequest;
import com.esri.gpt.catalog.publication.DeleteSourceUrisRequest;
import com.esri.gpt.catalog.publication.HarvesterRequest;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.catalog.schema.ValidationException;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.mail.MailRequest;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;
import com.esri.gpt.framework.security.identity.local.LocalDao;
import com.esri.gpt.framework.security.principal.Group;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.Users;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XsltTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Data processor.
 */
class DataProcessor {

  /** notification subject transformation path */
  private static final String NOTIF_SUBJECT_PATH = "gpt/harvest/notifSubject.xslt";
  /** notification message transformation path */
  private static final String NOTIF_MESSAGE_PATH = "gpt/harvest/notifMessage.xslt";
  /** logger */
  private static final Logger LOGGER = Logger.getLogger(DataProcessor.class.getCanonicalName());
  /** message broker */
  private MessageBroker messageBroker;
  /** listeners */
  private Harvester.Listener listener;
  /** base context path */
  private String baseContextPath = "";

  /**
   * Creates instance of the processor.
   * @param messageBroker message broker
   * @param baseContextPath base context path
   * @param listener listener array
   */
  public DataProcessor(MessageBroker messageBroker, String baseContextPath, Harvester.Listener listener) {
    if (messageBroker == null) {
      throw new IllegalArgumentException("No message broker provided.");
    }
    this.baseContextPath = Val.chkStr(baseContextPath);
    if (listener == null) {
      throw new IllegalArgumentException("No listener provided.");
    }
    this.messageBroker = messageBroker;
    this.listener = listener;
  }

  /**
   * Called upon the start of harvesting the resource.
   * @param unit execution unit
   */
  public void onStart(ExecutionUnit unit) {
    LOGGER.info("[SYNCHRONIZER] Starting processing metadata records through: " + unit);
    Date startTime = new Date();
    // create request context
    RequestContext context = RequestContext.extract(null);
    Long maxRepRecords = context.getApplicationConfiguration().getHarvesterConfiguration().getMaxRepRecords();
    if (maxRepRecords < 0)
      maxRepRecords = null;
    Long maxRepErrors = context.getApplicationConfiguration().getHarvesterConfiguration().getMaxRepErrors();
    if (maxRepErrors < 0)
      maxRepErrors = null;

    try {

      // initialize report builder
      Criteria criteria = unit.getCriteria();
      ReportBuilder rp = new ReportBuilder(
        criteria != null ? criteria.getMaxRecords() : null,
        maxRepRecords,
        maxRepErrors);
      rp.setStartTime(startTime);
      unit.setReportBuilder(rp);

      // prepare the publisher
      LocalDao localDao = new LocalDao(context);
      String uDN = localDao.readDN(unit.getRepository().getOwnerId());
      Publisher publisher = new Publisher(context, uDN);
      unit.setPublisher(publisher);

      // get all existing URI's for the specific harvesting site
      if (unit.getCleanupFlag()) {
        SourceUriArray sourceUris = new SourceUriArray(new String[]{"uri", "uuid"});
        unit.setSourceUris(sourceUris);
        collectExistingSourceURIs(context, unit.getRepository(), sourceUris);
      }

      // notify listeners
      listener.onHarvestStart(unit.getRepository());

    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Error starting metadata processing.", ex);
    } finally {
      context.onExecutionPhaseCompleted();
    }
  }

  /**
   * Called when harvesting successfully completed.
   * @param unit execution unit
   */
  public void onCompleted(final ExecutionUnit unit) {
    RequestContext context = RequestContext.extract(null);

    try {
      ReportBuilder rp = unit.getReportBuilder();

      // save last sync date
      unit.getRepository().setLastSyncDate(rp.getStartTime());
      HrUpdateLastSyncDate updLastSyncDate = new HrUpdateLastSyncDate(context, unit.getRepository());
      updLastSyncDate.execute();

    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Error completing metadata records through: " + unit, ex);
    }
  }

  /**
   * Called upon the end of harvesting of the resource.
   * @param unit execution unit
   */
  public void onEnd(final ExecutionUnit unit) {
    RequestContext context = RequestContext.extract(null);
    Date endTime = new Date();
    try {

      // get report builder
      ReportBuilder rp = unit.getReportBuilder();
      rp.setEndTime(endTime);

      // prepare event record
      HeRecord event = new HeRecord(unit.getRepository());

      event.setHarvestedCount((int) rp.getHarvestedCount());
      event.setValidatedCount((int) rp.getValidatedCount());
      event.setPublishedCount((int) rp.getPublishedCount());

      // save report
      HeUpdateRequest updateReq = new HeUpdateRequest(context, event);
      updateReq.execute();

      HeUpdateReportRequest updateReportReq =
        new HeUpdateReportRequest(context, event);
      ReportStream reportStream = rp.createReportStream();

      InputStream in = null;
      try {
        in = reportStream.getStream();
        updateReportReq.execute(in, reportStream.getLength());
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException ex) {
          }
        }
      }

      sendNotification(context, event);

      // perform cleanup for the specific harvest repository
      if (unit.getCleanupFlag()) {
        // create Iterable based on MapEntryIterator
        Iterable<Map.Entry<String, String>> iterable = new Iterable<Map.Entry<String, String>>() {

          public Iterator<Map.Entry<String, String>> iterator() {
            return new MapEntryIterator(unit.getSourceUris());
          }
        };

        if (unit.getPublisher() != null) {
          // delete all records identified by each sourceUri available in Iterable
          DeleteSourceUrisRequest deleteSourceUrisRequest = new DeleteSourceUrisRequest(context, unit.getPublisher(), iterable);
          deleteSourceUrisRequest.execute();
        }
      }

      // notify listeners
      listener.onHarvestEnd(unit.getRepository());

      LOGGER.info("[SYNCHRONIZER] Completed processing metadata records through: " + unit + ". Harvested: " + rp.getHarvestedCount() + ", validated: " + rp.getValidatedCount() + ", published: " + rp.getPublishedCount());
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Error completing metadata records through: " + unit, ex);
    } finally {
      try {
        if (unit.getSourceUris() != null) {
          unit.getSourceUris().close();
        }
      } catch (IOException ex) {
      }
      context.onExecutionPhaseCompleted();
    }
  }

  /**
   * Called uppon harvesting a single metadata.
   * @param unit execution unit
   * @param record record to publish
   * @throws IOException if reading metadata fails
   * @throws SAXException if processing metadata fails
   * @throws TransformerException if processing metadata fails
   */
  public void onMetadata(ExecutionUnit unit, Publishable record) throws IOException, TransformerException, SAXException {
    // extract record information
    SourceUri sourceUri = record.getSourceUri();

    RequestContext context = RequestContext.extract(null);

    // get report builder
    ReportBuilder rp = unit.getReportBuilder();

    try {
      // immediately remove from the source URI's collection
      if (unit.getCleanupFlag()) {
        unit.getSourceUris().remove("uri", sourceUri.asString());
      }

      boolean proceed =
        (ProtocolInvoker.getUpdateDefinition(unit.getRepository().getProtocol()) && record instanceof Native)
        || (ProtocolInvoker.getUpdateContent(unit.getRepository().getProtocol()) && !(record instanceof Native));

      if (proceed) {
        // if there is 'from-date' criteria, check for all non-native records having
        // non-null last update date if that date is after 'from-date'. Only such a records
        // should be published
        if (!(record instanceof Native) && unit.getCriteria().getFromDate() != null) {
          Date lastUpdateDate = record.getUpdateDate();
          if (lastUpdateDate != null && lastUpdateDate.before(unit.getCriteria().getFromDate())) {
            // stop harvesting it
            proceed = false;
          }
        }
      }

      if (proceed) {
        String metadata = "";
        try {
          // notify listeners
          metadata = record.getContent();
          listener.onHarvestMetadata(unit.getRepository(), sourceUri, metadata);

          // publication request
          HarvesterRequest publicationRequest =
            new HarvesterRequest(context, unit.getPublisher(), unit.getRepository().getUuid(), sourceUri.asString(), metadata);
          publicationRequest.getPublicationRecord().setAutoApprove(ProtocolInvoker.getAutoApprove(unit.getRepository().getProtocol()));

          // if this is a repository descriptor, update repository record
          if (record instanceof Native) {
            String sMethod = MmdEnums.PublicationMethod.registration.toString();
            publicationRequest.getPublicationRecord().setUuid(unit.getRepository().getUuid());
            publicationRequest.getPublicationRecord().setPublicationMethod(sMethod);
            publicationRequest.getPublicationRecord().setAlternativeTitle(unit.getRepository().getName());
            publicationRequest.getPublicationRecord().setLockTitle(ProtocolInvoker.getLockTitle(unit.getRepository().getProtocol()));
          }

          publicationRequest.publish();
          boolean bReplaced =
            publicationRequest.getPublicationRecord().getWasDocumentReplaced();

          LOGGER.finer("[SYNCHRONIZER] SUCCEEDED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri);

          // notify listeners
          listener.onPublishMetadata(unit.getRepository(), sourceUri, publicationRequest.getPublicationRecord().getUuid(), metadata);

          // create harvest report entry for the current record
          rp.createEntry(sourceUri.asString(), !bReplaced);
        } catch (ValidationException ex) {
          if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "[SYNCHRONIZER] FAILED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri, ex);
          } else {
            LOGGER.finer("[SYNCHRONIZER] FAILED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri + ", details: " + ex.getMessage());
          }
          ArrayList<String> messages = new ArrayList<String>();
          ex.getValidationErrors().buildMessages(messageBroker, messages, true);
          rp.createInvalidEntry(sourceUri.asString(), messages);
          listener.onPublishException(unit.getRepository(), sourceUri, metadata, ex);
        } catch (IllegalArgumentException ex) {
          if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "[SYNCHRONIZER] FAILED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri, ex);
          } else {
            LOGGER.finer("[SYNCHRONIZER] FAILED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri + ", details: " + ex.getMessage());
          }
          rp.createUnpublishedEntry(sourceUri.asString(), Arrays.asList(new String[]{ex.getMessage()}));
          listener.onPublishException(unit.getRepository(), sourceUri, metadata, ex);
        } catch (SchemaException ex) {
          if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "[SYNCHRONIZER] FAILED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri, ex);
          } else {
            LOGGER.finer("[SYNCHRONIZER] FAILED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri + ", details: " + ex.getMessage());
          }
          rp.createInvalidEntry(sourceUri.asString(), Arrays.asList(new String[]{ex.getMessage()}));
          listener.onPublishException(unit.getRepository(), sourceUri, metadata, ex);
        } catch (ImsServiceException ex) {
          if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.log(Level.FINEST, "[SYNCHRONIZER] FAILED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri, ex);
          } else {
            LOGGER.finer("[SYNCHRONIZER] FAILED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri + ", details: " + ex.getMessage());
          }
          rp.createUnpublishedEntry(sourceUri.asString(), Arrays.asList(new String[]{ex.getMessage()}));
          listener.onPublishException(unit.getRepository(), sourceUri, metadata, ex);
        }
      }
    } catch (Exception ex) {
      LOGGER.finer("[SYNCHRONIZER] FAILED processing metadata #" + (rp.getHarvestedCount() + 1) + " through: " + unit + ", source URI: " + sourceUri + ", details: " + ex.getMessage());
      listener.onHarvestException(unit.getRepository(), sourceUri, ex);
    } finally {
      context.onExecutionPhaseCompleted();
    }
  }

  /**
   * Called upon iteration exception.
   * @param task task
   * @param ex exception
   */
  public void onIterationException(Task task, Exception ex) {
    LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Iteration exception through: " + task, ex);
    listener.onIterationException(task.getResource(), ex);
  }

  /**
   * Called upon iteration exception.
   * @param unit execution unit
   * @param ex exception
   */
  public void onIterationException(ExecutionUnit unit, Exception ex) {
    LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Iteration exception through: " + unit, ex);
    listener.onIterationException(unit.getRepository(), ex);
  }

  /**
   * Called upon iteration exception.
   * @param unit execution unit
   * @param sourceUri source URI of the metadata
   * @param ex exception
   */
  public void onIterationException(ExecutionUnit unit, SourceUri sourceUri, Exception ex) {
    LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Iteration exception through: " + unit, ex);
    listener.onIterationException(unit.getRepository(), ex);
    // immediately remove from the source URI's collection
    if (unit.getCleanupFlag()) {
      unit.getSourceUris().remove("uri", sourceUri.asString());
    }
  }

  /**
   * Collects existing source URI's for the given repository.
   * @param context request context
   * @param repository repository
   * @param sourceUris source URI's collector
   * @throws SQLException if accessing database fails
   */
  private void collectExistingSourceURIs(RequestContext context, HrRecord repository, final SourceUriArray sourceUris) throws SQLException {
    CollectSourceUrisRequest request = new CollectSourceUrisRequest(context, repository) {

      @Override
      protected void onSourceUri(String sourceUri, String uuid) {
        sourceUris.add(new String[]{sourceUri, uuid});
      }
    };
    request.execute();
  }

  /**
   * Sends email notification about completed harvest.
   * @param context request context
   * @param event harvest event
   * @throws SQLException if accessing database fails
   */
  private void sendNotification(RequestContext context, HeRecord event) {
    try {
      // check repository record to see if notification has to be sent
      HrRecord record = event.getRepository();
      if (record.getSendNotification()) {
        // create email addresses storage
        ArrayList<String> emailAddresses = new ArrayList<String>();

        // get distingushed name for the record owner
        LocalDao localDao = new LocalDao(context);
        String uDN = localDao.readDN(record.getOwnerId());

        // obtain LDAP adapter
        IdentityAdapter ldapAdapter = context.newIdentityAdapter();

        // declare users
        Users users = new Users();
        // check if the owner is a group
        Group group = null;
        IdentityConfiguration idConfig = context.getIdentityConfiguration();
        Groups mgmtGroups = idConfig.getMetadataManagementGroups();
        if (mgmtGroups != null) {
          group = mgmtGroups.get(uDN);
        }
        if (group != null) {
          // read all members of the group
          users = ldapAdapter.readGroupMembers(uDN);
          for (User user : users.values()) {
            ldapAdapter.readUserProfile(user);
            String emailAddress = user.getProfile().getEmailAddress();
            if (emailAddress.length() > 0) {
              emailAddresses.add(emailAddress);
            }
          }
        } else {
          User user = new User();
          user.setDistinguishedName(uDN);
          ldapAdapter.readUserProfile(user);
          String emailAddress = user.getProfile().getEmailAddress();
          if (emailAddress.length() > 0) {
            emailAddresses.add(emailAddress);
          }
        }

        // if is there any address
        if (!emailAddresses.isEmpty()) {

          String link = baseContextPath.length() > 0
            ? baseContextPath + "/catalog/harvest/report.page?uuid=" + Val.escapeXml(event.getUuid())
            : "";

          String notification =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<notification>"
            + "<eventId>" + Val.escapeXml(event.getUuid()) + "</eventId>"
            + "<eventDate>" + Val.escapeXml(event.getHarvestDate().toString()) + "</eventDate>"
            + "<repositoryId>" + Val.escapeXml(record.getUuid()) + "</repositoryId>"
            + "<repositoryName>" + Val.escapeXml(record.getName()) + "</repositoryName>"
            + "<reportLink>" + link + "</reportLink>"
            + "</notification>";

          // create notification subject and message using transformations
          XsltTemplate notifSubjectTemplate = XsltTemplate.makeFromResourcePath(NOTIF_SUBJECT_PATH);
          XsltTemplate notifMessageTemplate = XsltTemplate.makeFromResourcePath(NOTIF_MESSAGE_PATH);
          String notifSubject = notifSubjectTemplate.transform(notification);
          String notfiMessage = notifMessageTemplate.transform(notification);

          // send email to each recipient
          for (String emailAddress : emailAddresses) {

            // create and init mail request
            MailRequest mailRequest = context.getMailConfiguration().newOutboundRequest();
            mailRequest.setMimeTypeHtml();
            mailRequest.setToAddress(emailAddress);
            mailRequest.setSubject(notifSubject);
            mailRequest.setBody(notfiMessage);

            // send email notification
            mailRequest.send();
          }
        }
      }
    } catch (Exception ex) {
      LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Error sending email notification", ex);
    }
  }

  /**
   * Specialized adapter converting SourceUriArray iterator into
   * Map.Entry&lt;String, String&gt; iterator
   */
  private static class MapEntryIterator implements Iterator<Map.Entry<String, String>> {

    /** source URI's iterator */
    private Iterator<String[]> sourceUrisIterator;

    /**
     * Creates instance of the iterator.
     * @param collector collector
     */
    public MapEntryIterator(SourceUriArray collector) {
      this.sourceUrisIterator = collector.iterator();
    }

    public boolean hasNext() {
      return sourceUrisIterator.hasNext();
    }

    public Map.Entry<String, String> next() {
      // get nest element for the collector
      final String[] n = sourceUrisIterator.next();

      // create new Map Entry
      return new Map.Entry<String, String>() {

        public String getKey() {
          return n[0];
        }

        public String getValue() {
          return n[1];
        }

        public String setValue(String value) {
          String oldValue = n[1];
          n[1] = value;
          return oldValue;
        }
      };
    }

    public void remove() {
      sourceUrisIterator.remove();
    }
  }
}
