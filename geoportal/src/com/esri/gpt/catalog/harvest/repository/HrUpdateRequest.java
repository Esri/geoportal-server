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
package com.esri.gpt.catalog.harvest.repository;

import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.catalog.management.MmdEnums.PublicationMethod;
import com.esri.gpt.catalog.publication.PublicationRequest;
import com.esri.gpt.control.webharvest.engine.Harvester;
import com.esri.gpt.control.webharvest.protocol.ProtocolSerializer;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.StringUri;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.identity.local.LocalDao;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Harvest repository update request.
 * <p/>
 * Allows to store harvest repository data in the database. If the harvest
 * repository has no uuid defined, it will be a new hr created with new
 * uuid. If has, current hr will be overwriten.
 */
public class HrUpdateRequest extends HrRequest {

// class variables =============================================================
private static final Logger LOGGER = Logger.getLogger(HrUpdateRequest.class.getCanonicalName());
// instance variables ==========================================================
/** User owning harvest repositories. */
private User _owner = new User();
/** Repository to update. */
private HrRecord _repository = new HrRecord();

// constructors ================================================================
/**
 * Create instance of the request.
 * @param requestContext request context
 * @param owner owner of the records
 * @param record record to update
 */
public HrUpdateRequest(RequestContext requestContext, 
                       User owner, 
                       HrRecord record) {
  super(requestContext, new HrCriteria(), new HrResult());
  _owner = owner != null ? owner : new User();
  _repository = record != null ? record : new HrRecord();
}
// properties ==================================================================

/**
 * Gets owner.
 * @return owner of the repository
 */
public User getOwner() {
  return _owner;
}

/**
 * Sets owner.
 * 
 * <p/>
 * If the <code>owner</code> argument is <code>null</code> than the owner of
 * repositories is set to non existing user.
 * 
 * @param owner owner of the repository
 */
public void setOwner(User owner) {
  _owner = owner != null ? owner : new User();
}

/**
 * Gets repository to update.
 * @return repository to update
 */
public HrRecord getRepository() {
  return _repository;
}

/**
 * Sets repository to update.
 * @param repository repository to update
 */
public void setRepository(HrRecord repository) {
  _repository = repository != null ? repository : new HrRecord();
}

// methods =====================================================================

/**
 * Executes request.
 * If native resource not provided, no metadata will be created for the repository.
 * @param nativeResource native resource or <code>null</code>
 * @throws Exception if request execution fails
 */
public void executeUpdate(Native nativeResource) throws Exception {
  Connection con = null;
  boolean autoCommit = true;

  // intitalize
  PreparedStatement st = null;

  try {
    
    HrRecord hr = getRepository();

    // establish the connection
    ManagedConnection mc = returnConnection();
    con = mc.getJdbcConnection();
    autoCommit = con.getAutoCommit();
    con.setAutoCommit(false);

    String sql = "";
    boolean isUpdate = false;
    String sUuid = "";
    boolean finableBeforeUpdate = false;
    
    if (UuidUtil.isUuid(hr.getUuid())) {
      sUuid = hr.getUuid();
      finableBeforeUpdate = queryFindable(con);
      sql = createUpdateSQL();
      st = con.prepareStatement(sql);
      isUpdate = true;
    } else {
      sUuid = UuidUtil.makeUuid(true);
      finableBeforeUpdate = hr.getFindable();
      sql = createInsertSQL();
      st = con.prepareStatement(sql);
    }
    if (hr.getOwnerId()<0) {
      hr.setOwnerId(getOwner().getLocalID());
    }
    
    int n = 1;

    st.setInt(n++, hr.getOwnerId());
    st.setTimestamp(n++, makeTimestamp(hr.getInputDate()));
    st.setTimestamp(n++, makeTimestamp(hr.getUpdateDate()));
    st.setString(n++, hr.getName());
    st.setString(n++, hr.getHostUrl());
    st.setString(n++, hr.getHarvestFrequency().toString());
    st.setString(n++, Boolean.toString(hr.getSendNotification()));
    st.setString(n++, hr.getProtocol().getKind().toLowerCase());
    st.setString(n++, ProtocolSerializer.toXmlString(hr.getProtocol()));
    st.setString(n++, PublicationMethod.registration.name());
    if (!isUpdate) {
      if (getRequestContext().getApplicationConfiguration().getHarvesterConfiguration().getResourceAutoApprove()) {
        st.setString(n++, ApprovalStatus.approved.name());
      } else {
        st.setString(n++, ApprovalStatus.posted.name());
      }
    }
    // NOTE! Don't update 'findable' here. It has to be updated by ImsMetadataAdminDao.updateRecord.
    st.setString(n++, Boolean.toString(hr.getSearchable()));
    st.setString(n++, Boolean.toString(hr.getSynchronizable()));
    st.setString(n++, sUuid);

   logExpression(sql);

    int nRowCount = st.executeUpdate();
    getActionResult().setNumberOfRecordsModified(nRowCount);

    if (!isUpdate && nRowCount==1) {
      closeStatement(st);
      st = con.prepareStatement("SELECT ID FROM "+getHarvestingTableName()+" WHERE UPPER(DOCUUID)=?");
      st.setString(1, sUuid.toUpperCase());
      ResultSet genKeys = st.executeQuery();
      genKeys.next();
      int nLocalId = genKeys.getInt(1);
      hr.setLocalId(nLocalId);
      hr.setUuid(sUuid);
      closeResultSet(genKeys);
    }

    con.commit();

    // Use PublicationRequest if native resource present or 'findable' flag has changed.
    if (nativeResource!=null || (isUpdate && finableBeforeUpdate!=hr.getFindable())) {
      try {
        // If no native resource provided but this is update, fetch native resource for that
        // repository from database
        if (nativeResource==null && isUpdate) {
          nativeResource = queryNative(con);
        }
        // skip if still no native resource
        if (nativeResource!=null) {
          String content = nativeResource.getContent();
          String sourceUri = nativeResource.getSourceUri().asString();

          Publisher publisher = createPublisherOfRepository();

          PublicationRequest publicationRequest = createPublicationRequest(publisher, content, sourceUri);

          publicationRequest.publish();
        }
      } catch (Exception ex) {
        LOGGER.log(Level.INFO, "Unable to create resource definition.", ex);
      }
    }

    // NEW in 10.0;  notify update
    Harvester harvestEngine = getRequestContext().getApplicationContext().getHarvestingEngine();
    if (_repository.getIsHarvestDue()) {
      harvestEngine.submit(getRequestContext(), _repository, null, _repository.getLastSyncDate());
    }
    harvestEngine.reselect();

  } catch (Exception ex) {
    if (con!=null) {
      con.rollback();
    }
    throw ex;
  } finally {
    closeStatement(st);
    if (con!=null) {
      con.setAutoCommit(autoCommit);
    }
  }

}

/**
 * Queries native resource.
 * @param con database connection
 * @return native connection
 * @throws SQLException if accessing database fails
 */
private Native queryNative(Connection con) throws SQLException {
  final String content = queryContent(con);
  final String sourceUri = querySourceUri(con);
  final Date updateDate = queryUpdateDate(con);
  return new Native() {

      @Override
      public SourceUri getSourceUri() {
        return new StringUri(sourceUri);
      }

      @Override
      public String getContent() throws IOException {
        return content;
      }

      @Override
      public Iterable<Resource> getNodes() {
        return new ArrayList<Resource>();
      }

      @Override
      public Date getUpdateDate() {
        return updateDate;
      }

  };
}

/**
 * Queries content.
 * @param con database content
 * @return content
 * @throws SQLException if accessing database fails
 */
private String queryContent(Connection con) throws SQLException {
  String xml = "";

  StringBuilder sbSql = new StringBuilder();
  sbSql.append("SELECT XML FROM ").append(getHarvestingDataTableName()).append(" ");
  sbSql.append("WHERE DOCUUID=?");

  PreparedStatement st = null;
  ResultSet rs = null;

  try {

    st = con.prepareStatement(sbSql.toString());
    st.setString(1, getRepository().getUuid());

    logExpression(sbSql.toString());

    rs = st.executeQuery();

    if (rs.next()) {
      xml = Val.chkStr(rs.getString(1));
    }
  } finally {
    closeResultSet(rs);
    closeStatement(st);
  }

  return xml;
}

/**
 * Queries source URI.
 * @param con database connection
 * @return source URI
 * @throws SQLException if accessing database fails
 */
private String querySourceUri(Connection con) throws SQLException {
  String sourceUri = "";

  StringBuilder sbSql = new StringBuilder();
  sbSql.append("SELECT SOURCEURI FROM ").append(getHarvestingTableName()).append(" ");
  sbSql.append("WHERE DOCUUID=?");

  PreparedStatement st = null;
  ResultSet rs = null;

  try {

    st = con.prepareStatement(sbSql.toString());
    st.setString(1, getRepository().getUuid());

    logExpression(sbSql.toString());

    rs = st.executeQuery();

    if (rs.next()) {
      sourceUri = Val.chkStr(rs.getString(1));
    }
  } finally {
    closeResultSet(rs);
    closeStatement(st);
  }

  return sourceUri;
}


/**
 * Queries update date.
 * @param con database connection
 * @return source URI
 * @throws SQLException if accessing database fails
 */
private Date queryUpdateDate(Connection con) throws SQLException {
  Date updateDate = null;

  StringBuilder sbSql = new StringBuilder();
  sbSql.append("SELECT UPDATEDATE FROM ").append(getHarvestingTableName()).append(" ");
  sbSql.append("WHERE DOCUUID=?");

  PreparedStatement st = null;
  ResultSet rs = null;

  try {

    st = con.prepareStatement(sbSql.toString());
    st.setString(1, getRepository().getUuid());

    logExpression(sbSql.toString());

    rs = st.executeQuery();

    if (rs.next()) {
      updateDate = rs.getTimestamp(1);
    }
  } finally {
    closeResultSet(rs);
    closeStatement(st);
  }

  return updateDate;
}

/**
 * Creates publication request.
 * @param publisher publisher
 * @param content content
 * @param xml source URI
 * @return request
 */
private PublicationRequest createPublicationRequest(Publisher publisher, String content, String sourceUri) {
  
  PublicationRequest publicationRequest = new PublicationRequest(getRequestContext(), publisher, content);
  publicationRequest.getPublicationRecord().setUuid(getRepository().getUuid());
  publicationRequest.getPublicationRecord().setPublicationMethod(MmdEnums.PublicationMethod.registration.toString());
  publicationRequest.getPublicationRecord().setSourceUri(sourceUri);
  publicationRequest.getPublicationRecord().setSourceFileName(sourceUri);
  publicationRequest.getPublicationRecord().setAutoApprove(getRequestContext().getApplicationConfiguration().getHarvesterConfiguration().getResourceAutoApprove());
  publicationRequest.getPublicationRecord().setAlternativeTitle(getRepository().getName());
  publicationRequest.getPublicationRecord().setUpdateOnlyIfXmlHasChanged(false);
  publicationRequest.getPublicationRecord().setIndexEnabled(getRepository().getFindable());

  return publicationRequest;
}

/**
 * Creates publisher of the repository.
 * @return publisher
 * @throws SQLException if accessing database fails
 * @throws CredentialsDeniedException if invalid credentials
 * @throws NotAuthorizedException if not authorized
 * @throws IdentityException if no identity found
 * @throws ImsServiceException if accessing ArcIMS service fails
 */
private Publisher createPublisherOfRepository()
  throws SQLException, CredentialsDeniedException, NotAuthorizedException, IdentityException, ImsServiceException {

  LocalDao localDao = new LocalDao(getRequestContext());
  String uDN = localDao.readDN(getRepository().getOwnerId());
  Publisher publisher = new Publisher(getRequestContext(), uDN);

  return publisher;
}

/**
 * Queries findable.
 * @param con database connection
 * @return findable
 * @throws SQLException if accessing database fails
 */
private boolean queryFindable(Connection con) throws SQLException {
  boolean findable = false;

  StringBuilder sbSql = new StringBuilder();
  sbSql.append("SELECT FINDABLE FROM ").append(getHarvestingTableName()).append(" ");
  sbSql.append("WHERE DOCUUID=?");

  PreparedStatement st = null;
  ResultSet rs = null;

  try {

    st = con.prepareStatement(sbSql.toString());
    st.setString(1, getRepository().getUuid());

    logExpression(sbSql.toString());

    rs = st.executeQuery();

    if (rs.next()) {
      findable = Val.chkBool(rs.getString(1), false);
    }
  } finally {
    closeResultSet(rs);
    closeStatement(st);
  }

  return findable;
}

/**
 * Creates INSERT SQL.
 * @return INSERT SQL
 */
private String createInsertSQL() {
  StringBuilder sbInsertSql = new StringBuilder();

  // NOTE! Don't update 'findable' here. It has to be updated by ImsMetadataAdminDao.updateRecord.
  sbInsertSql.append("insert into ").append(getHarvestingTableName()).append(" ");
  sbInsertSql.append("(OWNER,INPUTDATE,UPDATEDATE,TITLE,");
  sbInsertSql.append("HOST_URL,FREQUENCY,");
  sbInsertSql.append("SEND_NOTIFICATION,PROTOCOL_TYPE,PROTOCOL,PUBMETHOD,APPROVALSTATUS,");
  sbInsertSql.append("SEARCHABLE,SYNCHRONIZABLE,");
  sbInsertSql.append("DOCUUID)");
  sbInsertSql.append("values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

  return sbInsertSql.toString();
}

/**
 * Creates UPDATE SQL.
 * @return UPDATE SQL
 */
private String createUpdateSQL() {
  StringBuilder sbUpdateSql = new StringBuilder();

  // NOTE! Don't update 'findable' here. It has to be updated by ImsMetadataAdminDao.updateRecord.
  sbUpdateSql.append("update ").append(getHarvestingTableName()).append(" ");
  sbUpdateSql.append("set OWNER=?,INPUTDATE=?,UPDATEDATE=?,");
  sbUpdateSql.append("TITLE=?,HOST_URL=?,FREQUENCY=?,");
  sbUpdateSql.append("SEND_NOTIFICATION=?,PROTOCOL_TYPE=?,PROTOCOL=?,PUBMETHOD=?, ");
  sbUpdateSql.append("SEARCHABLE=?,SYNCHRONIZABLE=? ");
  sbUpdateSql.append("where DOCUUID=?");

  return sbUpdateSql.toString();
}

/**
 * Creates timestamp from date.
 * If date is <code>null</code>, timestamp will be <code>null</code> as well.
 * @param date date to make timestamp
 * @return timestamp
 */
private java.sql.Timestamp makeTimestamp(Date date) {
  return date != null ? new java.sql.Timestamp(date.getTime()) : null;
}
}
