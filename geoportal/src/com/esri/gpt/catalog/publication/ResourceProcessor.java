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
package com.esri.gpt.catalog.publication;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Super-class for a processor that interacts with a resource for the express 
 * purpose of publishing metadata to the catalog.
 */
public abstract class ResourceProcessor {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(ResourceProcessor.class.getName());

  /** instance variables ====================================================== */
  private ProcessingContext  context;
  private String             publicationMethod = "upload";
  private Map<String,String> sourceURIs;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied processing context.
   * @param context the resource processing context
   */
  public ResourceProcessor(ProcessingContext context) {
    this.context = context;
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the processing context.
   * @return the processing context
   */
  public ProcessingContext getContext() {
    return this.context ;
  }
  
  /**
   * Gets the publication method.
   * @return the publication method
   */
  public String getPublicationMethod() {
    return this.publicationMethod;
  }
  /**
   * Sets the publication method.
   * @param method the publication method
   */
  public void setPublicationMethod(String method) {
    this.publicationMethod = Val.chkStr(method);
  }
    
  /** methods ================================================================= */
  
  /**
   * Collects document source URIs associated with a parent resource (SQL LIKE).
   * @param pattern the source URI pattern of the parent resource
   * @param pattern2 optional secondary source URI pattern of the parent resource
   * @throws SQLException if an exception occurs while communicating with the database
   */
  protected void collectExistingSourceURIs(String pattern, String pattern2) 
    throws SQLException {
    CatalogDao dao = new CatalogDao(this.getContext().getRequestContext());
    this.sourceURIs = dao.querySourceURIs(pattern,pattern2);
  }
  
  /**
   * Deletes catalog documents that are no longer referenced by the parent resource.
   * <br/>Deletion only occurs if at least one URI was processed during this request.
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws ImsServiceException if an exception occurs during delete
   * @throws CatalogIndexException if an exception occurs during delete
   * @throws IOException if accessing index fails
   */
  protected void deleteUnreferencedSourceURIs() 
    throws SQLException, ImsServiceException, CatalogIndexException, IOException {
    List<ProcessedRecord> records = this.getContext().getProcessedRecords();
    if ((this.sourceURIs != null) && (this.sourceURIs.size() > 0) && (records.size() > 0)) {
      for (ProcessedRecord record: records) {
        this.sourceURIs.remove(record.getSourceUri());
      }
      if (this.sourceURIs.size() > 0) {
        CatalogDao dao = new CatalogDao(this.getContext().getRequestContext());
        dao.deleteSourceURIs(this.getContext(),this.sourceURIs);
      }
    }
  }
  
  /**
   * Invokes processing against the resource.
   * @throws Exception if an exception occurs
   */
  public abstract void process() throws Exception;

  /**
   * Creates iteration query.
   * Query is being used during synchronization.
   * @param context iteration context
   * @param criteria query criteria or <code>null</code> if no criteria
   * @return query
   */
  public abstract Query createQuery(IterationContext context, Criteria  criteria);

  /**
   * Gets native resource.
   * Native resource is a publishable resource created just for repository definition.
   * Each native resource is {@link Publishable} and each repository has to be able to
   * provide one.
   * @param context iteration context
   * @return native resource.
   */
  public abstract Native getNativeResource(IterationContext context);
  
  /**
   * Publishes metadata associated with a resource.
   * @param resourceUrl the URL for the resource being published
   * @param resourceXml the resource XML
   * @throws Exception if an exception occurs
   */
  public void publishMetadata(String resourceUrl, String resourceXml) 
    throws Exception {
    this.publishMetadata(resourceUrl,resourceXml,null);
  }
  
  /**
   * Publishes metadata associated with a resource.
   * @param resourceUrl the URL for the resource being published
   * @param resourceXml the resource XML
   * @param sourceUri a URI identifying the source
   * @throws Exception if an exception occurs
   */
  public void publishMetadata(String resourceUrl, String resourceXml, String sourceUri) 
    throws Exception {
    ProcessingContext context  = getContext();
    ProcessedRecord processedRcord = new ProcessedRecord();
    if ((sourceUri != null) && (sourceUri.length() > 0)) {
      processedRcord.setSourceUri(sourceUri);
    } else if ((resourceUrl != null) && (resourceUrl.length() > 0)) {
      processedRcord.setSourceUri(resourceUrl);
    }
    context.getProcessedRecords().add(processedRcord);
    
    // handle validation only requests
    if (context.getValidateOnly()) {
      try {
        ValidationRequest request = new ValidationRequest(
            context.getRequestContext(),resourceUrl,resourceXml);
        request.verify();
        context.incrementNumberValidated();
        processedRcord.setStatusType(ProcessedRecord.StatusType.VALIDATED);
      } catch (Exception e) {
        context.incrementNumberFailed();
        context.setLastException(e);
        processedRcord.setStatusType(ProcessedRecord.StatusType.FAILED);
        processedRcord.setException(e,this.getContext().getMessageBroker());
        if (context.getWasSingleSource()) {
          throw e;
        } else {
          // TODO: log this?
          LOGGER.log(Level.FINER,"Error\n"+processedRcord.getSourceUri()+"\n"+resourceXml,e);
        }
      }
      
    // handle publication requests
    } else {
      try {
        PublicationRecord template = context.getTemplate();
        UploadRequest request = new UploadRequest(
            context.getRequestContext(),context.getPublisher(),resourceUrl,resourceXml);
        PublicationRecord publicationRecord = request.getPublicationRecord();
        if ((sourceUri != null) && (sourceUri.length() > 0)) {
          publicationRecord.setSourceUri(sourceUri);
        }
        if (template != null) {
          publicationRecord.setAutoApprove(template.getAutoApprove());
          publicationRecord.setUpdateOnlyIfXmlHasChanged(template.getUpdateOnlyIfXmlHasChanged());
        }
        if ((this.getPublicationMethod() != null) && (this.getPublicationMethod().length() > 0)) {
          publicationRecord.setPublicationMethod(this.getPublicationMethod());
        }
        request.publish();
        context.incrementNumberValidated();
        if (request.getPublicationRecord().getWasDocumentUnchanged()) {
          context.incrementNumberUnchanged();
          processedRcord.setStatusType(ProcessedRecord.StatusType.UNCHNAGED);
        } else if (request.getPublicationRecord().getWasDocumentReplaced()) {
          context.incrementNumberReplaced();
          processedRcord.setStatusType(ProcessedRecord.StatusType.REPLACED);
        } else {
          context.incrementNumberCreated();
          processedRcord.setStatusType(ProcessedRecord.StatusType.CREATED);
        }
      } catch (Exception e) {
        context.incrementNumberFailed();
        context.setLastException(e);
        processedRcord.setStatusType(ProcessedRecord.StatusType.FAILED);
        processedRcord.setException(e,this.getContext().getMessageBroker());
        if (context.getWasSingleSource()) {
          throw e;
        } else {
          // TODO: log this?
          LOGGER.log(Level.FINER,"Error\n"+processedRcord.getSourceUri()+"\n"+resourceXml,e);
        }
      }
    }
    
  }
  
}
