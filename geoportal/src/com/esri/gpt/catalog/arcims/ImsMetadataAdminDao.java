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
package com.esri.gpt.catalog.arcims;
import com.esri.gpt.catalog.context.CatalogIndexAdapter;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.lucene.RemoteIndexer;
import com.esri.gpt.catalog.management.CollectionDao;
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.catalog.management.MmdQueryCriteria;
import com.esri.gpt.catalog.publication.PublicationRecord;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.sql.IClobMutator;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

import java.sql.*;
import java.util.Map;
import java.util.logging.Level;

/**
 * Database access object associated with the ArcIMS metadata administration table.
 */
public class ImsMetadataAdminDao extends BaseDao {
  
// class variables =============================================================
  
/** Status code for synchronized records = 1 */
public static final int SYNCSTATUS_SYNCHRONIZED = 1;

/** Status code for unsynchronized records = 0 */
public static final int SYNCSTATUS_UNSYNCHRONIZED = 0;
  
// instance variables ==========================================================
private CswRemoteRepository cswRemoteRepository;
private boolean hadUnalteredDraftDocuments = false;
private boolean updateIndex = true;

// constructors ================================================================

/** Default constructor. */
protected ImsMetadataAdminDao() {
  super();
}

/**
 * Constructs with an associated request context.
 * @param requestContext the request context
 */
public ImsMetadataAdminDao(RequestContext requestContext) {
  super(requestContext);
  cswRemoteRepository = new CswRemoteRepository(requestContext);
}

public boolean getUpdateIndex() {
  return updateIndex;
}

public void setUpdateIndex(boolean updateIndex) {
  this.updateIndex = updateIndex;
}

// properties ==================================================================

/**
 * Gets the catalog index adapter.
 * @return the the catalog index adapter (null if none)
 */
private CatalogIndexAdapter getCatalogIndexAdapter() {
  return getUpdateIndex()? getRequestContext().getCatalogConfiguration().makeCatalogIndexAdapter(getRequestContext()): null;
}


/**
 * Gets resource table name.
 * @return resource table name
 */
private String getResourceTableName() {
  return getRequestContext().getCatalogConfiguration().getResourceTableName();
}

/**
 * Gets resource data table name.
 * @return resource data table name
 */
private String getResourceDataTableName() {
  return getRequestContext().getCatalogConfiguration().getResourceDataTableName();
}

/**
 * Gets the status indicating whether or not documents in draft mode were unaltered by an update.
 * <br/>The approval status of a document in draft mode can only be altered by publishing
 * the document from the online editor.
 * @return true if draft documents were unaltered
 */
public boolean hadUnalteredDraftDocuments() {
  return this.hadUnalteredDraftDocuments;
}

// methods =====================================================================

/**
 * Gets the count of records (non-folder) from the ADMIN table that are referenced within the 
 * ArcIMS metadata table.
 * @return count of referenced records
 * @throws SQLException if a database exception occurs
 */
public int countReferencedRecords() throws SQLException {
  PreparedStatement st = null;
  ResultSet rs = null;
  int recdCount = 0;
  try {
    Connection con = returnConnection().getJdbcConnection();
    StringBuilder sbSql = new StringBuilder();
    sbSql.append("SELECT count(*) FROM ");
    sbSql.append(getResourceTableName()).append(" A,");
    sbSql.append(getResourceDataTableName()).append(" B");
    sbSql.append(" WHERE (A.DOCUUID = B.DOCUUID)");
    logExpression(sbSql.toString());
    st = con.prepareStatement(sbSql.toString());
    rs = st.executeQuery();    
    if (rs.next()){
      recdCount = rs.getInt(1);
    }
  } finally {
    closeResultSet(rs);
    closeStatement(st);
  }
  return recdCount;
}

/**
 * Gets the count of records from the ADMIN table that are not referenced within the 
 * ArcIMS metadata table.
 * @return the count of unreferenced records in ADMIN table
 * @throws SQLException if a database exception occurs
 */
public int countUnreferencedRecords() 
  throws SQLException {
  PreparedStatement st = null;
  ResultSet rs = null;
  try {
    StringBuilder sbSql = new StringBuilder();
    sbSql.append("SELECT count(*) FROM ").append(getResourceDataTableName());
    sbSql.append(" WHERE DOCUUID NOT IN (SELECT DOCUUID FROM ");
    sbSql.append(getResourceTableName()).append(")");
    logExpression(sbSql.toString());
    Connection con = returnConnection().getJdbcConnection();
    st = con.prepareStatement(sbSql.toString());
    rs = st.executeQuery();
    int nCount = 0;
    if (rs.next()) {
      nCount = rs.getInt(1); 
    }
    return nCount;
  } finally {
    closeResultSet(rs);
    closeStatement(st);
  }
}

/**
 * Deletes a metadata administration record.
 * <br/>Records are only deleted from the ADMIN table if they are
 * not references within ArcIMS metadata table.
 * @param uuid the UUID for the record to delete
 * @return the number of rows affected
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public int deleteRecord(String uuid) 
  throws SQLException, CatalogIndexException {
  Connection con = null;
  boolean autoCommit = true;
  PreparedStatement st = null;
  ResultSet rs = null;
  int nRows = 0;
  boolean cancelTask = false;
  
  // read the file identifer before deletion
  StringSet fids = new StringSet();
  if (cswRemoteRepository.isActive()) {
    StringSet uuids = new StringSet();
    uuids.add(uuid);
    fids = queryFileIdentifiers(uuids);
  }
  
  // delete the database record
  try {
    con = returnConnection().getJdbcConnection();
    autoCommit = con.getAutoCommit();
    con.setAutoCommit(false);

    String sSql = "SELECT COUNT(*) FROM "+getResourceTableName()+" WHERE DOCUUID=? AND PROTOCOL_TYPE IS NOT NULL AND PROTOCOL_TYPE<>''";
    logExpression(sSql);
    st = con.prepareStatement(sSql);
    st.setString(1,uuid);
    rs = st.executeQuery();
    if (rs.next()) {
      cancelTask = rs.getInt(1)>0;
    }
    
    closeStatement(st);
    sSql = "DELETE FROM "+getResourceTableName()+" WHERE DOCUUID=?";
    logExpression(sSql);
    st = con.prepareStatement(sSql);
    st.setString(1,uuid);
    nRows = st.executeUpdate();

    closeStatement(st);
    sSql = "DELETE FROM "+getResourceDataTableName()+" WHERE DOCUUID=?";
    logExpression(sSql);
    st = con.prepareStatement(sSql);
    st.setString(1,uuid);
    st.executeUpdate();
    
    CollectionDao colDao = new CollectionDao(this.getRequestContext());
    if (colDao.getUseCollections()) {
      closeStatement(st);
      sSql = "DELETE FROM "+colDao.getCollectionMemberTableName()+" WHERE DOCUUID=?";
      logExpression(sSql);
      st = con.prepareStatement(sSql);
      st.setString(1,uuid);
      st.executeUpdate();
    }

    con.commit();
  } catch (SQLException ex) {
    if (con!=null) {
      con.rollback();
    }
    throw ex;
  } finally {
    closeResultSet(rs);
    closeStatement(st);
    if (con!=null) {
      con.setAutoCommit(autoCommit);
    }
  }
  
  // delete the indexed record
  CatalogIndexAdapter indexAdapter = getCatalogIndexAdapter();
  if (indexAdapter != null) {
    indexAdapter.deleteDocument(uuid);
    
    if (cswRemoteRepository.isActive()) {
      if (fids.size() > 0) cswRemoteRepository.onRecordsDeleted(fids);
    }
  }

  // stop synchronization
  if (cancelTask && getRequestContext()!=null) {
    getRequestContext().getApplicationContext().getHarvestingEngine().cancel(getRequestContext(), uuid);
  }

  return nRows;
}

/**
 * Deletes records matching criteria.
 * @param criteria filter criteria
 * @param publisher publisher
 * @return the number of rows affected
 * @throws Exception if performing operation fails
 */
public int deleteRecord(Publisher publisher, MmdQueryCriteria criteria) throws Exception {
  int nRows = 0;

  if (!publisher.getIsAdministrator()) {
    throw new ImsServiceException("DeleteRecordsRequest: not authorized.");
  }

  PreparedStatement st = null;
  // establish the connection
  ManagedConnection mc = returnConnection();
  Connection con = mc.getJdbcConnection();

  DatabaseMetaData dmt = con.getMetaData();
  String database = dmt.getDatabaseProductName().toLowerCase();
  
  boolean autoCommit = con.getAutoCommit();
  con.setAutoCommit(false);

  try {

    // create WHERE phrase
    StringBuilder sbWhere = new StringBuilder();
    Map<String,Object> args = criteria.appendWherePhrase(null, sbWhere, publisher);
    
    // delete data
    StringBuilder sbData = new StringBuilder();
    if (database.contains("mysql")) {
      sbData.append(" DELETE ").append(getResourceDataTableName()).append(" FROM ").append(getResourceDataTableName());
      sbData.append(" LEFT JOIN ").append(getResourceTableName());
      sbData.append(" ON ").append(getResourceDataTableName()).append(".ID=").append(getResourceTableName()).append(".ID WHERE ").append(getResourceTableName()).append(".ID in (");
      sbData.append(" SELECT ID FROM ").append(getResourceTableName()).append(" ");
      if (sbWhere.length() > 0) {
        sbData.append(" WHERE ").append(sbWhere.toString());
      }
      sbData.append(")");
    } else {
      sbData.append(" DELETE FROM ").append(getResourceDataTableName());
      sbData.append(" WHERE ").append(getResourceDataTableName()).append(".ID in (");
      sbData.append(" SELECT ID FROM ").append(getResourceTableName()).append(" ");
      if (sbWhere.length() > 0) {
        sbData.append(" WHERE ").append(sbWhere.toString());
      }
      sbData.append(")");
    }
    
    st = con.prepareStatement(sbData.toString());
    criteria.applyArgs(st, 1, args);
    logExpression(sbData.toString());
    st.executeUpdate();

    // delete records
    StringBuilder sbSql = new StringBuilder();
    sbSql.append("DELETE FROM ").append(getResourceTableName()).append(" ");
    if (sbWhere.length() > 0) {
      sbSql.append(" WHERE ").append(sbWhere.toString());
    }
    closeStatement(st);
    st = con.prepareStatement(sbSql.toString());
    criteria.applyArgs(st, 1, args);
    logExpression(sbSql.toString());
    nRows = st.executeUpdate();

    con.commit();
  } catch (Exception ex) {
    con.rollback();
    throw ex;
  } finally {
    closeStatement(st);
    con.setAutoCommit(autoCommit);
  }

  return nRows;
}

/**
 * Transfers ownership for records matching criteria.
 * @param localId new owner local id
 * @param criteria filter criteria
 * @param publisher publisher
 * @return the number of rows affected
 * @throws Exception if transferring ownership fails
 */
public int transferOwnership(Publisher publisher, MmdQueryCriteria criteria, int localId)
  throws Exception {
  int nRows = 0;


  if (!publisher.getIsAdministrator()) {
    throw new ImsServiceException("TransferOwnershipRequest: not authorized.");
  }

  PreparedStatement st = null;

  try {
    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    StringBuilder sbSql = new StringBuilder();
    sbSql.append("UPDATE ").append(getResourceTableName()).append(" ");
    sbSql.append(" SET OWNER=? ");

    StringBuilder sbWhere = new StringBuilder();

    Map<String,Object> args = criteria.appendWherePhrase(null, sbWhere, publisher);

    // append the where clause expressions
    if (sbWhere.length() > 0) {
      sbSql.append(" WHERE ").append(sbWhere.toString());
    }

    // prepare the statements
    st = con.prepareStatement(sbSql.toString());

    int n = 1;
    st.setInt(n++, localId);
    criteria.applyArgs(st, n, args);

    // query the count
    logExpression(sbSql.toString());

    nRows = st.executeUpdate();

  } finally {
    closeStatement(st);
  }

  return nRows;
}

/**
 * Unindexes record.
 * @param uuid the UUID for the record to delete
 * @return the number of rows affected
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public int unindexRecord(String uuid)
  throws SQLException, CatalogIndexException {
  Connection con = null;
  boolean autoCommit = true;
  PreparedStatement st = null;
  int nRows = 0;

  // read the file identifer before deletion
  StringSet fids = new StringSet();
  if (cswRemoteRepository.isActive()) {
    StringSet uuids = new StringSet();
    uuids.add(uuid);
    fids = queryFileIdentifiers(uuids);
  }

  // delete the database record
  try {
    con = returnConnection().getJdbcConnection();
    autoCommit = con.getAutoCommit();
    con.setAutoCommit(false);

    String sSql = "DELETE FROM "+getResourceDataTableName()+" WHERE DOCUUID=?";
    logExpression(sSql);
    st = con.prepareStatement(sSql);
    st.setString(1,uuid);
    nRows = st.executeUpdate();

    con.commit();
  } catch (SQLException ex) {
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

  // delete the indexed record
  CatalogIndexAdapter indexAdapter = getCatalogIndexAdapter();
  if (indexAdapter != null) {
    indexAdapter.deleteDocument(uuid);

    if (cswRemoteRepository.isActive()) {
      if (fids.size() > 0) cswRemoteRepository.onRecordsDeleted(fids);
    }
  }
  return nRows;
}

/**
 * Deletes records from the ADMIN table that are not referenced within the 
 * ArcIMS metadata table.
 * @param maxValuesForIndex the maximum number to collect for catalog index deletion
 * @return the number of rows affected
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public int deleteUnreferencedRecords(int maxValuesForIndex) 
  throws SQLException, CatalogIndexException {
  StringSet uuids = new StringSet();
  PreparedStatement st = null;
  int nRows = 0;
  
  // find unreferenced records
  try {
    StringBuilder sbSql = new StringBuilder();
    sbSql.append("SELECT DOCUUID FROM ").append(getResourceDataTableName());
    sbSql.append(" WHERE DOCUUID NOT IN (SELECT DOCUUID FROM ");
    sbSql.append(getResourceTableName()).append(")");
    logExpression(sbSql.toString());
    Connection con = returnConnection().getJdbcConnection();
    st = con.prepareStatement(sbSql.toString());
    st.setMaxRows(maxValuesForIndex);
    ResultSet rs = st.executeQuery();
    while (rs.next()) {
      uuids.add(rs.getString(1));
    }
  } finally {
    closeStatement(st);
    st = null;
  }
  StringSet fids = new StringSet();
  if (cswRemoteRepository.isActive()) {
    fids = queryFileIdentifiers(uuids);
  }
  
  // delete unreferenced records from the admin table
  try {
    if (uuids.size() > 0) {
      String sMsg = "Deleting "+uuids.size()+" unreferenced documents from table: "+getResourceDataTableName();
      LogUtil.getLogger().info(sMsg);
      StringBuilder sbSql = new StringBuilder();
      sbSql.append("DELETE FROM ").append(getResourceDataTableName());
      sbSql.append(" WHERE DOCUUID IN (").append(uuidsToInClause(uuids)).append(")");
      logExpression(sbSql.toString());
      Connection con = returnConnection().getJdbcConnection();
      st = con.prepareStatement(sbSql.toString());
      nRows = st.executeUpdate();
    }
  } finally {
    closeStatement(st);
  }
  
  // delete unreferenced records from the index
  if (uuids.size() > 0) {
    CatalogIndexAdapter indexAdapter = getCatalogIndexAdapter();
    if (indexAdapter != null) {
      String sMsg = "Deleting "+uuids.size()+" unreferenced documents from the catalog index.";
      LogUtil.getLogger().info(sMsg);
      indexAdapter.deleteDocuments(uuids.toArray(new String[0]));
      
      if (cswRemoteRepository.isActive()) {
        if (fids.size() > 0) cswRemoteRepository.onRecordsDeleted(fids);
      }
    }
  }
  
  return nRows;
}

/**
 * Determines if a document UUID exists within the ArcIMS metadata table.
 * @param con the JDBC connection
 * @param uuid the document UUID to check
 * @return true if the document UUID exists
 * @throws SQLException if a database exception occurs
 */
private boolean doesImsUuidExist(Connection con, String uuid)
  throws SQLException {
  boolean bExists = false;
  PreparedStatement st = null;
  try {
    String sSql = "SELECT DOCUUID FROM "+getResourceTableName()+" WHERE DOCUUID=?";
    logExpression(sSql);
    st = con.prepareStatement(sSql);
    st.setString(1,uuid);
    ResultSet rs = st.executeQuery();
    if (rs.next()) {
      bExists = true;
    }
  } finally {
    closeStatement(st);
  }
  return bExists;
}

/**
 * Checks for an existing metadata document UUID for a document that
 * is about to be published.
 * @param fileIdentifier the file identifier to check
 * @param sourceUri the source uri to check
 * @return the existing document UUID (empty string if none)
 * @throws SQLException if a database exception occurs
 */
public String findExistingUuid(String fileIdentifier, String sourceUri)
  throws SQLException {
  String sUuid = "";
  sUuid = findExistingUuidFromField("FILEIDENTIFIER",fileIdentifier);
  if (sUuid.length() == 0) {
    sUuid = findExistingUuidFromField("SOURCEURI",sourceUri);
  }
  return sUuid;
}

/**
 * Finds source URI from metadata UUID.
 * @param uuid metadata UUID
 * @return source URI or empty string if no source URI available
 * @throws SQLException if a database exception occurs
 */
public String findExistingSourceUri(String uuid) throws SQLException {
  String sSourceUri = "";
  PreparedStatement st = null;
  try {
    uuid = Val.chkStr(uuid);
    if (uuid.length() > 0) {
      
      // query for a matching UUID
      Connection con = returnConnection().getJdbcConnection();
      StringBuilder sbSql = new StringBuilder();
      sbSql.append("SELECT SOURCEURI FROM ").append(getResourceTableName());
      if (getIsDbCaseSensitive(this.getRequestContext())) {
        sbSql.append(" WHERE UPPER(DOCUUID)=?");
      } else {
        sbSql.append(" WHERE DOCUUID=?");
      }
      logExpression(sbSql.toString());
      st = con.prepareStatement(sbSql.toString());
      st.setString(1,uuid.toUpperCase());
      ResultSet rs = st.executeQuery();
      int n = 0;
      if (rs.next()) {
        n++;
        sSourceUri = Val.chkStr(rs.getString(1)); 
      }
    }
  } finally {
    closeStatement(st);
  }
  return sSourceUri;
}

/**
 * Checks for an existing metadata document UUID for a document that
 * is about to be published.
 * @param field the field to query
 * @param value the value to query
 * @return the existing document UUID (empty string if none)
 * @throws SQLException if a database exception occurs
 */
private String findExistingUuidFromField(String field, String value)
  throws SQLException {
  String sUuid = "";
  PreparedStatement st = null;
  try {
    value = Val.chkStr(value);
    if (value.length() > 0) {
      
      // query for a matching UUID
      Connection con = returnConnection().getJdbcConnection();
      StringBuilder sbSql = new StringBuilder();
      sbSql.append("SELECT DOCUUID FROM ").append(getResourceTableName());
      if (getIsDbCaseSensitive(this.getRequestContext())) {
        sbSql.append(" WHERE UPPER(").append(field).append(")=?");
      } else {
        sbSql.append(" WHERE ").append(field).append("=?");
      }
      logExpression(sbSql.toString());
      st = con.prepareStatement(sbSql.toString());
      st.setString(1,value.toUpperCase());
      ResultSet rs = st.executeQuery();
      int n = 0;
      while (rs.next()) {
        n++;
        sUuid = Val.chkStr(rs.getString(1)); 
        if (n > 1) {
          // multiple matches, ignore
          sUuid = "";
          break;
        }
      }
    }
  } finally {
    closeStatement(st);
  }
  return sUuid;
}

/**
 * Checks for an existing metadata document UUID based upon a supplied UUID or FileIdentifier.
 * @param id the UUID of FileIdentifier to check
 * @return the existing document UUID (empty string if none)
 * @throws SQLException if a database exception occurs
 */
public String findUuid(String id) throws SQLException {
  String sUuid = findExistingUuidFromField("DOCUUID",id);
  if (sUuid.length() == 0) {
    sUuid = findExistingUuidFromField("FILEIDENTIFIER",id);
  }
  return sUuid;
}

/**
 * Updates the synchronization status code (SYNCSTATUS_SYNCHRONIZED) for the supplied UUIDs.
 * @param uuids the collection of UUIDs to update
 * @throws SQLException if a database exception occurs
 */
public void onRecordsSynchronized(StringSet uuids)
  throws SQLException {
  updateSynchronizationStatus(SYNCSTATUS_SYNCHRONIZED,"DOCUUID IN ("+uuidsToInClause(uuids)+")");
}

/**
 * Queries the acl associated with a document.
 * @param principal query string
 * @return the acl xml string (empty string if not found)
 * @throws SQLException if a database exception occurs
 */
private String queryAclByPrincipal(String principal) throws SQLException {
  String sAcl = "";
  PreparedStatement st = null;
  String sQueryString = Val.chkStr(principal);
  try {  
    String sAdminTable = getResourceTableName();
    String sSql = "SELECT ACL FROM "+sAdminTable+" WHERE ACL LIKE %'>'?'</principal>'";
    logExpression(sSql);
    Connection con = returnConnection().getJdbcConnection();
    st = con.prepareStatement(sSql);
    st.setString(1,sQueryString);
     ResultSet rs = st.executeQuery();
      if (rs.next()) {
        sAcl = Val.chkStr(rs.getString(1));
      }
  } finally {
    closeStatement(st);
  }
  return sAcl;
}

/**
 * Queries the acl associated with a document.
 * @param uuid the document UUID
 * @return the acl xml string (empty string if not found)
 * @throws SQLException if a database exception occurs
 */
  public String queryAclByUUID(String uuid) throws SQLException {
    String sAcl = "";
    PreparedStatement st = null;
    String sUuid = Val.chkStr(uuid);
    try {
      String sAdminTable = getResourceTableName();
      String sSql = "SELECT ACL FROM "+sAdminTable+
                    " WHERE DOCUUID=?";
      logExpression(sSql);
      Connection con = returnConnection().getJdbcConnection();
      st = con.prepareStatement(sSql);
      st.setString(1,sUuid);
       ResultSet rs = st.executeQuery();
        if (rs.next()) {
          sAcl = Val.chkStr(rs.getString(1));
        }
    } finally {
      closeStatement(st);
    }
  return sAcl;
}

/**
 * Queries the approval status associated with a document.
 * @param uuid the document UUID
 * @return the approval status (empty string if not found)
 * @throws SQLException if a database exception occurs
 */
public String queryApprovalStatus(String uuid) throws SQLException {
  String sStatus = "";
  PreparedStatement st = null;
  try {
    uuid = Val.chkStr(uuid);
    if (uuid.length() > 0) {
      Connection con = returnConnection().getJdbcConnection();
      String sSql = "SELECT APPROVALSTATUS FROM "+getResourceTableName()+
                    " WHERE DOCUUID=?";
      logExpression(sSql);
      st = con.prepareStatement(sSql);
      st.setString(1,uuid);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        sStatus = Val.chkStr(rs.getString(1));
      }
    }
  } finally {
    closeStatement(st);
  }
  return sStatus;
}

/**
 * Queries the file idenitfier assoicated with a uuid.
 * @param uuid the document UUID
 * @return the approval status (empty string if not found)
 * @throws SQLException if a database exception occurs
 */
private String queryFileIdentifier(String uuid) throws SQLException {
  PreparedStatement st = null;
  try {
    uuid = Val.chkStr(uuid);
    if (uuid.length() > 0) {
      Connection con = returnConnection().getJdbcConnection();
      String sSql = "SELECT FILEIDENTIFIER FROM "+getResourceTableName()+" WHERE DOCUUID=?";
      logExpression(sSql);
      st = con.prepareStatement(sSql);
      st.setString(1,uuid);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        return Val.chkStr(rs.getString(1));
      }
    }
  } finally {
    closeStatement(st);
  }
  return "";
}

/**
 * Queries the file idenitfier assoicated with a uuid.
 * @param uuid the document UUID
 * @return the approval status (empty string if not found)
 * @throws SQLException if a database exception occurs
 */
private StringSet queryFileIdentifiers(StringSet uuids) throws SQLException {
  PreparedStatement st = null;
  StringSet fids = new StringSet();
  try {
    if ((uuids != null) && (uuids.size() > 0)) {
      Connection con = returnConnection().getJdbcConnection();
      StringBuilder sbSql = new StringBuilder();
      sbSql.append("SELECT FILEIDENTIFIER FROM ").append(getResourceTableName());
      sbSql.append(" WHERE DOCUUID IN (").append(uuidsToInClause(uuids)).append(")");
      logExpression(sbSql.toString());
      st = con.prepareStatement(sbSql.toString());
      ResultSet rs = st.executeQuery();
      while (rs.next()) {
        String fid = Val.chkStr(rs.getString(1));
        if (fid.length() > 0) fids.add(rs.getString(1));
      }
    }
  } finally {
    closeStatement(st);
  }
  return fids;
}

/**
 * Determines the distinguished name associated with the owner of a document.
 * @param uuid the document UUID
 * @return the owner's distinguished name(empty string if not found)
 * @throws SQLException if a database exception occurs
 */
public String queryOwnerDN(String uuid) throws SQLException {
  String sDN = "";
  PreparedStatement st = null;
  try {
    String sOwnerName = queryOwnerName(uuid);
    if (sOwnerName.length() > 0) {
      Connection con = returnConnection().getJdbcConnection();
      String sUserTable = getRequestContext().getCatalogConfiguration().getUserTableName();
      String sSql = null;
      if (getIsDbCaseSensitive(this.getRequestContext())) {
        sSql = "SELECT DN FROM "+sUserTable+" WHERE UPPER(USERNAME) = ?";
      } else {
        sSql = "SELECT DN FROM "+sUserTable+" WHERE USERNAME = ?";
      }
      logExpression(sSql);
      st = con.prepareStatement(sSql);
      st.setString(1,sOwnerName.toUpperCase());
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        sDN = Val.chkStr(rs.getString(1));
      }
    }
  } finally {
    closeStatement(st);
  }
  return sDN;
}

/**
 * Queries the ArcIMS owner name associated with a document.
 * @param uuid the document UUID
 * @return the owner name (empty string if not found)
 * @throws SQLException if a database exception occurs
 */
public String queryOwnerName(String uuid) throws SQLException {
  String sOwnerName = "";
  PreparedStatement st = null;
  try {
    uuid = Val.chkStr(uuid);
    if (uuid.length() > 0) {
      Connection con = returnConnection().getJdbcConnection();
      StringBuilder sbSql = new StringBuilder();
      String userTable = this.getRequestContext().getCatalogConfiguration().getUserTableName();
      sbSql.append("SELECT B.USERNAME FROM ");
      sbSql.append(getResourceTableName()).append(" A");
      sbSql.append(",").append(userTable).append(" B");
      sbSql.append(" WHERE (A.OWNER = B.USERID) AND A.DOCUUID=?");
      logExpression(sbSql.toString());
      st = con.prepareStatement(sbSql.toString());
      st.setString(1,uuid);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        sOwnerName = Val.chkStr(rs.getString(1));
      }
    }
  } finally {
    closeStatement(st);
  }
  return sOwnerName;
}

/**
 * Queries the system update date associated with a document.
 * @param uuid the document UUID
 * @return the update date (null if none was found)
 * @throws SQLException if a database exception occurs
 */
public Timestamp queryUpdateDate(String uuid) throws SQLException {
  Timestamp tsUpdate = null;
  PreparedStatement st = null;
  try {
    uuid = Val.chkStr(uuid);
    if (uuid.length() > 0) {
      Connection con = returnConnection().getJdbcConnection();
      String sSql = "SELECT UPDATEDATE FROM "+getResourceTableName()+" WHERE DOCUUID=?";
      logExpression(sSql);
      st = con.prepareStatement(sSql);
      st.setString(1,uuid);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        return rs.getTimestamp(1);
      }
    }
  } finally {
    closeStatement(st);
  }
  return tsUpdate;
}

/**
 * Finds documents harvested from the specific site.
 * @param siteUuid harvest site UUID
 * @return collection of documents UUID
 * @throws SQLException if a database exception occurs
 */
public StringSet querySiteUuid(String siteUuid) throws SQLException {
  siteUuid = Val.chkStr(siteUuid);
  PreparedStatement st = null;
  StringSet uuids = new StringSet(false,true,true);
  try {
    Connection con = returnConnection().getJdbcConnection();
    String sSql = null;
    if (getIsDbCaseSensitive(this.getRequestContext())) {
      sSql = "SELECT DOCUUID FROM "+getResourceTableName()+
             " WHERE SITEUUID=? AND UPPER(PUBMETHOD)=?";
    } else {
      sSql = "SELECT DOCUUID FROM "+getResourceTableName()+
             " WHERE SITEUUID=? AND PUBMETHOD=?";
    }
    logExpression(sSql);
    st = con.prepareStatement(sSql);
    st.setString(1,siteUuid);
    st.setString(2,
      MmdEnums.PublicationMethod.harvester.toString().toUpperCase());
    ResultSet rs = st.executeQuery();
    while (rs.next()) {
      uuids.add(rs.getString(1));
    }
  } finally {
    closeStatement(st);
  }
  return uuids;
}

/**
 * Reads the UUIDs for the currently unsynchronized records.
 * @param maxUuids the maximum number to read
 * @return the set of UUIDs
 * @throws SQLException if a database exception occurs
 */
public StringSet readUuidsForSynchronization(int maxUuids) throws SQLException {
  StringSet uuids = new StringSet();
  PreparedStatement st = null;
  try {
    Connection con = returnConnection().getJdbcConnection();
    StringBuilder sbSql = new StringBuilder();
    sbSql.append("SELECT DOCUUID FROM ");
    sbSql.append(getResourceTableName());
    sbSql.append(" WHERE ((APPROVALSTATUS = 'approved') OR (APPROVALSTATUS = 'reviewed'))");
    logExpression(sbSql.toString());
    st = con.prepareStatement(sbSql.toString());
    st.setMaxRows(maxUuids);
    ResultSet rs = st.executeQuery();
    while (rs.next()) {
      uuids.add(rs.getString(1));
    }  
  } finally {
    closeStatement(st);
  }
  if (uuids.size() > 0) onRecordsSynchronized(uuids);
  return uuids;
}

/**
 * Reads the XML associated with a document uuid.
 * @param docUuid the document uuid
 * @return the XML string (empty string if not found)
 * @throws SQLException if a database exception occurs
 */
public String readXml(String docUuid) throws SQLException {
  String sXml = "";
  PreparedStatement st = null;
  try {  
    docUuid = Val.chkStr(docUuid);
    if (docUuid.length() > 0) {
      String sSql = "SELECT XML FROM "+getResourceDataTableName()+" WHERE DOCUUID=?";
      logExpression(sSql);
      ManagedConnection mc = returnConnection();
      Connection con = mc.getJdbcConnection();
      IClobMutator cm = mc.getClobMutator();
      st = con.prepareStatement(sSql);
      st.setString(1,docUuid);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        return Val.chkStr(cm.get(rs,1));
      }
    }
  } finally {
    closeStatement(st);
  }
  return sXml;
}

/**
 * Resets the synchronization status code (SYNCSTATUS_UNSYNCHRONIZED) for all records.
 * @throws SQLException if a database exception occurs
 */
public void resetSynchronizationStatus() throws SQLException {
  updateSynchronizationStatus(SYNCSTATUS_UNSYNCHRONIZED,"1=1");
}

/**
 * Updates the acl for records matching criteria.
 * @param publisher the publisher executing this request
 * @param criteria filter criteria
 * @param acl the new acl
 * @return the number of rows affected
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public int updateAcl(Publisher publisher, MmdQueryCriteria criteria, String acl)
  throws Exception {
  int nRows = 0;


  if (!publisher.getIsAdministrator()) {
    throw new ImsServiceException("UpdateAclRequest: not authorized.");
  }

  PreparedStatement st = null;

  try {
    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    StringBuilder sbSql = new StringBuilder();
    sbSql.append("UPDATE ").append(getResourceTableName()).append(" ");
    sbSql.append(" SET ACL=? ");

    StringBuilder sbWhere = new StringBuilder();

    Map<String,Object> args = criteria.appendWherePhrase(null, sbWhere, publisher);

    // append the where clause expressions
    if (sbWhere.length() > 0) {
      sbSql.append(" WHERE ").append(sbWhere.toString());
    }

    // prepare the statements
    st = con.prepareStatement(sbSql.toString());

    int n = 1;
    if(acl != null){
    	st.setString(n++,acl);
    }else{
    	st.setNull(n++,java.sql.Types.VARCHAR);
    }
    criteria.applyArgs(st, n, args);

    // query the count
    logExpression(sbSql.toString());

    nRows = st.executeUpdate();

  } finally {
    closeStatement(st);
  }

  return nRows;
}

/**
 * Updates the acl for a set of UUIDs.
 * @param publisher the publisher executing this request
 * @param uuids the set of uuids to update
 * @param acl the new acl
 * @return the number of rows affected
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public int updateAcl(Publisher publisher, StringSet uuids, String acl) 
  throws SQLException, CatalogIndexException {
  
  // insert acl for document
  PreparedStatement st = null;
  String sUuids = uuidsToInClause(uuids);
  int nRows = 0;
  try {
    StringBuilder sbSql = new StringBuilder();
      sbSql.append("UPDATE ").append(getResourceTableName());
      sbSql.append(" SET ACL=?");
      sbSql.append(" WHERE DOCUUID IN (").append(sUuids).append(")");
      logExpression(sbSql.toString());
   
    Connection con = returnConnection().getJdbcConnection();
    st = con.prepareStatement(sbSql.toString());
    if(acl != null){
    	st.setString(1,acl);
    }else{  
    	st.setNull(1,java.sql.Types.VARCHAR);
    }
    nRows = st.executeUpdate();
  } finally {
    closeStatement(st);
  }
  
  // publish the record to the index if approved
  CatalogIndexAdapter indexAdapter = getCatalogIndexAdapter();
  if (indexAdapter != null) {
    StringAttributeMap params = this.getRequestContext().getCatalogConfiguration().getParameters();
    String param = Val.chkStr(params.getValue("lucene.useRemoteWriter"));
    boolean bUseRemoteWriter = param.equalsIgnoreCase("true");
    param = Val.chkStr(params.getValue("lucene.useLocalWriter"));
    boolean bUseLocalWriter = !param.equalsIgnoreCase("false");
    if (bUseRemoteWriter) {
      RemoteIndexer remoteIndexer = new RemoteIndexer();
      remoteIndexer.send(this.getRequestContext(),"publish",
          uuids.toArray(new String[0]));
    } 
    if (bUseLocalWriter) {
      for (String uuid: uuids) {
        String sStatus = queryApprovalStatus(uuid);
        if (MmdEnums.ApprovalStatus.isPubliclyVisible(sStatus)) {
          String xml = indexAdapter.publishDocument(uuid,publisher);
          //if (cswRemoteRepository.isActive()) {
          //  cswRemoteRepository.onRecordUpdated(xml);
          //}
        }
      }
    }
  }  
  
  return nRows;
}

/**
 * Updates the approval status for a set of UUIDs.
 * <br/>Documents if "draft" status will not be updated by the method.
 * @param uuids the set of uuids to update
 * @param approvalStatus the new approval status
 * @return the number of rows affected
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public int updateApprovalStatus(Publisher publisher, 
                                StringSet uuids, 
                                MmdEnums.ApprovalStatus approvalStatus)
  throws SQLException, CatalogIndexException {
  
  // update the database approval status
  PreparedStatement st = null;
  int nRows = 0;
  this.hadUnalteredDraftDocuments = false;
  try {
    String sUuids = uuidsToInClause(uuids);
    if (sUuids.length() > 0) {   
      
      // determine if the set contains documents in 'draft' mode
      Connection con = returnConnection().getJdbcConnection();
      StringBuffer sbSql = new StringBuffer();
      sbSql.append("SELECT DOCUUID FROM ").append(getResourceTableName());
      sbSql.append(" WHERE DOCUUID IN (").append(sUuids).append(")");
      sbSql.append(" AND APPROVALSTATUS = ?"); 
      logExpression(sbSql.toString());
      st = con.prepareStatement(sbSql.toString());
      st.setString(1,MmdEnums.ApprovalStatus.draft.toString());
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        this.hadUnalteredDraftDocuments = true;
      }
      closeStatement(st);
      
      // execute the update, don't update documents in 'draft' mode
      sbSql = new StringBuffer();     
      sbSql.append("UPDATE ").append(getResourceTableName());
      sbSql.append(" SET APPROVALSTATUS=?");
      sbSql.append(" WHERE DOCUUID IN (").append(sUuids).append(")");
      sbSql.append(" AND (APPROVALSTATUS IS NULL OR APPROVALSTATUS <> ?)");
      logExpression(sbSql.toString());
      st = con.prepareStatement(sbSql.toString());
      st.setString(1,approvalStatus.toString());
      st.setString(2,MmdEnums.ApprovalStatus.draft.toString());
      nRows = st.executeUpdate();
      
      // re-build the index uuid set if 'draft' documents were not updated
      if (this.hadUnalteredDraftDocuments || (nRows != uuids.size())) {
        closeStatement(st);
        uuids.clear();
        sbSql = new StringBuffer();
        sbSql.append("SELECT DOCUUID FROM ").append(getResourceTableName());
        sbSql.append(" WHERE DOCUUID IN (").append(sUuids).append(")");
        sbSql.append(" AND (APPROVALSTATUS IS NULL OR APPROVALSTATUS <> ?)"); 
        logExpression(sbSql.toString());
        st = con.prepareStatement(sbSql.toString());
        st.setString(1,MmdEnums.ApprovalStatus.draft.toString());
        ResultSet rs2 = st.executeQuery();
        while (rs2.next()) {
          uuids.add(rs2.getString(1));
        }
      }
    }
  } finally {
    closeStatement(st);
  }
  
  // publish to or remove from the index
  CatalogIndexAdapter indexAdapter = getCatalogIndexAdapter();
  if ((indexAdapter != null) && (uuids.size() > 0)) {
    if (MmdEnums.ApprovalStatus.isPubliclyVisible(approvalStatus.toString())) {
      
      StringAttributeMap params = this.getRequestContext().getCatalogConfiguration().getParameters();
      String param = Val.chkStr(params.getValue("lucene.useRemoteWriter"));
      boolean bUseRemoteWriter = param.equalsIgnoreCase("true");
      param = Val.chkStr(params.getValue("lucene.useLocalWriter"));
      boolean bUseLocalWriter = !param.equalsIgnoreCase("false");
      if (bUseRemoteWriter) {
        RemoteIndexer remoteIndexer = new RemoteIndexer();
        remoteIndexer.send(this.getRequestContext(),"publish",
            uuids.toArray(new String[0]));
      } 
      if (bUseLocalWriter) {
        boolean bHadException = false;
        for (String uuid: uuids) {
          try {
            boolean indexAllowed = queryIndexEnabled(uuid);
            if (indexAllowed) {
              String xml = indexAdapter.publishDocument(uuid,publisher);
  
              if (cswRemoteRepository.isActive()) {
                cswRemoteRepository.onRecordUpdated(xml);
              }
            }
          } catch (CatalogIndexException e) {
            bHadException = true;
            LogUtil.getLogger().log(Level.SEVERE,"Error publishing document to index.",e);
          }
        }
        if (bHadException) {
          throw new CatalogIndexException("Error publishing document to index.");
        }
      }
      
    } else {
      indexAdapter.deleteDocuments(uuids.toArray(new String[0]));
      
      if (cswRemoteRepository.isActive()) {
        StringSet fids = queryFileIdentifiers(uuids);
        if (fids.size() > 0) cswRemoteRepository.onRecordsDeleted(fids);
      }
    }
  }
  return nRows;
}

/**
 * Checks if current record is eligible to be found.
 * If the record doesn't exist it will return <code>false</code>.
 * If the record is not repository it will return <code>true</code>.
 * If the record is a repository it will return value of the FINDABLE.
 * @param uuid record UUID
 * @return <code>true</code> if record is eligible to be found
 * @throws SQLException if accessing database fails
 */
public boolean queryIndexEnabled(String uuid) throws SQLException {
  boolean findable = false;

  PreparedStatement st = null;
  try {
    uuid = Val.chkStr(uuid);
    if (uuid.length() > 0) {
      Connection con = returnConnection().getJdbcConnection();
      String sSql = "SELECT FINDABLE, PROTOCOL_TYPE FROM "+getResourceTableName()+
                    " WHERE DOCUUID=?";
      logExpression(sSql);
      st = con.prepareStatement(sSql);
      st.setString(1,uuid);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        String sFindable = Val.chkStr(rs.getString(1));
        String sProtocolType = Val.chkStr(rs.getString(2));
        if (sProtocolType.length()==0 || Val.chkBool(sFindable, false)) {
          findable = true;
        }
      }
    }
  } finally {
    closeStatement(st);
  }

  return findable;
}

/**
 * Updates the approval status for records matching criteria.
 * <br/>Documents if "draft" status will not be updated by the method.
 * @param criteria filter criteria
 * @param approvalStatus the new approval status
 * @return the number of rows affected
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public int updateApprovalStatus(Publisher publisher,
                                MmdQueryCriteria criteria,
                                MmdEnums.ApprovalStatus approvalStatus)
  throws Exception {
  int nRows = 0;


  if (!publisher.getIsAdministrator()) {
    throw new ImsServiceException("UpdateApprovalStatusRequest: not authorized.");
  }
  
  PreparedStatement st = null;

  try {
    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    StringBuilder sbSql = new StringBuilder();
    sbSql.append("UPDATE ").append(getResourceTableName()).append(" ");
    sbSql.append(" SET APPROVALSTATUS=? ");

    StringBuilder sbWhere = new StringBuilder();

    Map<String,Object> args = criteria.appendWherePhrase(null, sbWhere, publisher);

    // append the where clause expressions
    if (sbWhere.length() > 0) {
      sbSql.append(" WHERE ").append(sbWhere.toString());
    }

    // prepare the statements
    st = con.prepareStatement(sbSql.toString());

    int n = 1;
    st.setString(n++, approvalStatus.name());
    criteria.applyArgs(st, n, args);

    // query the count
    logExpression(sbSql.toString());

    nRows = st.executeUpdate();

  } finally {
    closeStatement(st);
  }

  return nRows;
}

/**
 * Updates metadata administration record following publication.
 * @param schema the associated schema
 * @param record the associated metadata document
 * @return the number of rows affected
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
public int updateRecord(Schema schema, PublicationRecord record)
  throws SQLException, CatalogIndexException {

  PreparedStatement st = null;
  String statusBeforeUpdate = null;
  boolean indexEnabledBeforeUpdate = true;
  int nRows = 0;
  
  // execute the update
  
  try {
    statusBeforeUpdate = queryApprovalStatus(record.getUuid());
    indexEnabledBeforeUpdate = queryIndexEnabled(record.getUuid());

    String newStatus = Val.chkStr(record.getApprovalStatus());
    boolean wasDraft = statusBeforeUpdate.equalsIgnoreCase(MmdEnums.ApprovalStatus.draft.toString());
    if (wasDraft && (newStatus.length() == 0)) {
      newStatus = MmdEnums.ApprovalStatus.posted.toString();
    }
    
    Connection con = returnConnection().getJdbcConnection();
    StringBuilder sbSql = new StringBuilder();
    sbSql.append("UPDATE ").append(getResourceTableName());
    // Note! Since 'findable' is being read before and after update, it has to be done here.
    // See: HrUpdateRequest
    sbSql.append(" SET PUBMETHOD=?, SITEUUID=?, SOURCEURI=?, FILEIDENTIFIER=?, FINDABLE=?");
    if (newStatus.length() > 0) sbSql.append(", APPROVALSTATUS=?");
    sbSql.append(" WHERE DOCUUID=?");
    logExpression(sbSql.toString());
    st = con.prepareStatement(sbSql.toString());
    int nIdx = 1;
    st.setString(nIdx++,record.getPublicationMethod().toString());
    st.setString(nIdx++,record.getSiteUuid());
    st.setString(nIdx++,record.getSourceUri());
    st.setString(nIdx++,record.getFileIdentifier());
    st.setString(nIdx++,Boolean.toString(record.getIndexEnabled()));
    if (newStatus.length() > 0) st.setString(nIdx++,newStatus);
    st.setString(nIdx++,record.getUuid());
    nRows = st.executeUpdate();
    
  } finally {
    closeStatement(st);
  }
  
  // publish the record to the index if approved
  String statusAfterUpdate = queryApprovalStatus(record.getUuid());
  boolean indexEnabled = queryIndexEnabled(record.getUuid());
  if (MmdEnums.ApprovalStatus.isPubliclyVisible(statusAfterUpdate) && indexEnabled) {
    createIndex(schema, record);
  } else if (MmdEnums.ApprovalStatus.isPubliclyVisible(statusBeforeUpdate) && indexEnabledBeforeUpdate) {
    deleteIndex(record);
  }
  
  return nRows;
}

/**
 * Creates index.
 * @param schema schema
 * @param record publication record
 * @throws SQLException if accessing database fails
 * @throws CatalogIndexException if accessing index file fails
 */
public void createIndex(Schema schema, PublicationRecord record)
  throws SQLException, CatalogIndexException {

  CatalogIndexAdapter indexAdapter = getCatalogIndexAdapter();
  if (indexAdapter!=null) {
    Timestamp tsUpdate = queryUpdateDate(record.getUuid());
    String acl = queryAclByUUID(record.getUuid());
    indexAdapter.publishDocument(record.getUuid(),tsUpdate,schema,acl);

    if (cswRemoteRepository.isActive()) {
      cswRemoteRepository.onRecordUpdated(schema,record);
    }
  }

}

/**
 * Deletes index.
 * @param record publication record
 * @throws SQLException if accessing database fails
 * @throws CatalogIndexException if accessing index file fails
 */
public void deleteIndex(PublicationRecord record)
  throws CatalogIndexException, SQLException {

  CatalogIndexAdapter indexAdapter = getCatalogIndexAdapter();
  if (indexAdapter!=null) {
    StringSet uuids = new StringSet();
    uuids.add(record.getUuid());
    indexAdapter.deleteDocuments(uuids.toArray(new String[0]));
    if (cswRemoteRepository.isActive()) {
      StringSet fids = queryFileIdentifiers(uuids);
      if (fids.size() > 0) cswRemoteRepository.onRecordsDeleted(fids);
    }
  }

}

/**
 * Updates the synchronization status code for a collection of records.
 * @param status the synchronization status code
 * @param where the where clause indicating the recouds to update
 * @throws SQLException if a database exception occurs
 */
private void updateSynchronizationStatus(int status, String where) throws SQLException {
  PreparedStatement st = null;
  try {
    Connection con = returnConnection().getJdbcConnection();
    StringBuilder sbSql = new StringBuilder();
    sbSql.append("UPDATE ").append(getResourceTableName());
    sbSql.append(" SET CATSYNC=? WHERE ").append(where);
    logExpression(sbSql.toString());
    st = con.prepareStatement(sbSql.toString());
    st.setInt(1,status);
    st.executeUpdate();
  } finally {
    closeStatement(st);
  }
}

/**
 * Returns a comma delimited, single quoted string of UUIDs
 * suitable for an SQL IN clause.
 * @param uuids the set of UUIDs
 * @return the delimited string
 */
private String uuidsToInClause(StringSet uuids) {
  StringBuilder sb = new StringBuilder();
  for (String sUuid: uuids) {
    if (sb.length() > 0) sb.append(",");
    sb.append("'").append(sUuid).append("'");
  }
  return sb.toString();
}

}
