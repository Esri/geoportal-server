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
package com.esri.gpt.catalog.management;
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.catalog.harvest.repository.HrRecord.HarvestFrequency;
import com.esri.gpt.catalog.harvest.repository.HrRecord.RecentJobStatus;
import com.esri.gpt.catalog.management.MmdEnums.PublicationMethod;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.RoleMap;
import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.Users;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.DateProxy;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Queries metadata documents based upon associated query criteria.
 */
public class MmdQueryRequest extends MmdRequest {

// class variables =============================================================

// instance variables ==========================================================
private ImsMetadataAdminDao adminDao;
private String tblImsUser;
private HashMap<String, String> hmEditablePublishers = new HashMap<String, String>();
private Groups allGroups = null;
private boolean isGptAdministrator;
// constructors ================================================================
/**
 * Construct a metadata management query request.
 * @param requestContext the request context
 * @param publisher the publisher
 * @param criteria the request criteria
 * @param result the request result
 */
public MmdQueryRequest(RequestContext requestContext, Publisher publisher,
    MmdCriteria criteria, MmdResult result) {
  super(requestContext, publisher, criteria, result);
}

// properties ==================================================================

// methods =====================================================================
/**
 * Executes the query request.
 * @throws SQLException if a database exception occurs
 * @throws IOException 
 * @throws SAXException 
 * @throws ParserConfigurationException 
 */
public void execute() throws SQLException, IdentityException, NamingException,
    ParserConfigurationException, SAXException, IOException {

  // intitalize
  PreparedStatement st = null;
  PreparedStatement stCount = null;
  MmdQueryCriteria criteria = getQueryCriteria();
  MmdRecords records = getQueryResult().getRecords();
  PageCursor pageCursor = getQueryResult().getPageCursor();
  criteria.getDateRange().check();
  pageCursor.setTotalRecordCount(0);

  adminDao = new ImsMetadataAdminDao(getRequestContext());
  tblImsUser = getRequestContext().getCatalogConfiguration().getUserTableName();
  Users editablePublishers = Publisher.buildSelectablePublishers(getRequestContext(), false);
  for (User u : editablePublishers.values()) {
    if (u.getName().length() > 0) {
      hmEditablePublishers.put(u.getName().toLowerCase(), u.getKey());
    }
  }
  User tmpUser = new User();
  tmpUser.setDistinguishedName("*");
  getRequestContext().newIdentityAdapter().readUserGroups(tmpUser);
  allGroups = tmpUser.getGroups();

  isGptAdministrator = new RoleMap(getRequestContext().getUser()).get("gptAdministrator");
  
  // determine if we are in ArcIMS metadata server proxy mode

  try {

    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    // start the SQL expression
    StringBuilder sbSql = new StringBuilder();
    StringBuilder sbCount = new StringBuilder();
    StringBuilder sbFrom = new StringBuilder();
    StringBuilder sbWhere = new StringBuilder();
    sbSql.append("SELECT A.TITLE,A.DOCUUID,A.SITEUUID,C.USERNAME");
    sbSql.append(",A.APPROVALSTATUS,A.PUBMETHOD,A.UPDATEDATE,A.ACL");
    sbSql.append(",A.ID,A.HOST_URL,A.FREQUENCY,A.SEND_NOTIFICATION,A.PROTOCOL");
    sbSql.append(",A.FINDABLE,A.SEARCHABLE,A.SYNCHRONIZABLE");
    sbCount.append("SELECT COUNT(*)");

    // append from clause
    sbFrom.append(" FROM ").append(tblImsUser).append(" C");
    sbFrom.append(",").append(getResourceTableName()).append(" A");
    sbSql.append(sbFrom);
    sbCount.append(sbFrom);

    // build the where clause
    if (sbWhere.length()>0) {
      sbWhere.append(" AND");
    }
    sbWhere.append(" (A.OWNER = C.USERID)");

    Map<String,Object> args = criteria.appendWherePhrase("A", sbWhere, getPublisher());

    // append the where clause expressions
    if (sbWhere.length() > 0) {
      sbSql.append(" WHERE ").append(sbWhere.toString());
      sbCount.append(" WHERE ").append(sbWhere.toString());
    }

    // append the order by clause
    String sSortColumn = criteria.getSortOption().getColumnKey();
    String sSortDir = criteria.getSortOption().getDirection().toString();
    if (sSortColumn.equalsIgnoreCase("title")) {
      sSortColumn = "UPPER(A.TITLE)";
    } else if (sSortColumn.equalsIgnoreCase("uuid")) {
      sSortColumn = "A.DOCUUID";
    } else if (sSortColumn.equalsIgnoreCase("owner")) {
      sSortColumn = "UPPER(C.USERNAME)";
    } else if (sSortColumn.equalsIgnoreCase("status")) {
      sSortColumn = "A.APPROVALSTATUS";
    } else if (sSortColumn.equalsIgnoreCase("method")) {
      sSortColumn = "A.PUBMETHOD";
    } else if (sSortColumn.equalsIgnoreCase("acl")) {
      sSortColumn = "A.ACL";
    } else if (sSortColumn.equalsIgnoreCase("updatedate")) {
      sSortColumn = "A.UPDATEDATE";
    } else {
      sSortColumn = "A.UPDATEDATE";
      sSortDir = "DESC";
      criteria.getSortOption().setColumnKey("updatedate");
      criteria.getSortOption().setDirection("desc");
    }
    sbSql.append(" ORDER BY ");
    sbSql.append(sSortColumn).append(" ").append(sSortDir.toUpperCase());
    if (!sSortColumn.equalsIgnoreCase("A.UPDATEDATE")) {
      sbSql.append(", A.UPDATEDATE DESC");
    }

    // prepare the statements
    st = con.prepareStatement(sbSql.toString());
    stCount = con.prepareStatement(sbCount.toString());

    int n = 1;
    criteria.applyArgs(st, n, args);
    criteria.applyArgs(stCount, n, args);

    // query the count
    logExpression(sbCount.toString());
    ResultSet rsCount = stCount.executeQuery();
    if (rsCount.next()) {
      pageCursor.setTotalRecordCount(rsCount.getInt(1));
    }
    stCount.close();
    stCount = null;

    // query records if a count was found
    pageCursor.checkCurrentPage();
    if (pageCursor.getTotalRecordCount() > 0) {

      // set the start record and the number of records to retrieve
      int nCurPage = pageCursor.getCurrentPage();
      int nRecsPerPage = getQueryResult().getPageCursor().getRecordsPerPage();
      int nStartRecord = ((nCurPage - 1) * nRecsPerPage) + 1;
      int nMaxRecsToRetrieve = nCurPage * nRecsPerPage;
      st.setMaxRows(nMaxRecsToRetrieve);

      // determine publisher names associated with editable records

      // execute the query
      logExpression(sbSql.toString());
      ResultSet rs = st.executeQuery();

      // build the record set
      int nCounter = 0;

      while (rs.next()) {
        n = 1;
        nCounter++;
        if (nCounter >= nStartRecord) {
          MmdRecord record = new MmdRecord();
          records.add(record);

          readRecord(rs, record);

          // break if we hit the max value for the cursor
          if (records.size() >= nRecsPerPage) {
            break;
          }

        }
      }

      TreeMap<String, MmdRecord> recordsMap = new TreeMap<String, MmdRecord>(String.CASE_INSENSITIVE_ORDER);
      StringBuilder keys = new StringBuilder();

      for (MmdRecord r : records) {
        if (r.getProtocol()==null) continue;
        recordsMap.put(r.getUuid(), r);
        if (keys.length()>0) {
          keys.append(",");
        }
        keys.append("'").append(r.getUuid().toUpperCase()).append("'");
      }

      readJobStatus(con, recordsMap, keys.toString());
      readLastHarvestDate(con, recordsMap, keys.toString());
    }

  } finally {
    closeStatement(st);
    closeStatement(stCount);
  }
}

/**
 * Reads owner id.
 * @param con database connection
 * @return owner id or -1 if no owner id
 * @throws SQLException if accessing database fails
 */
private int readImsOwnerId(Connection con) throws SQLException {
  int nUserId = -1;
  PreparedStatement st = null;
  try {
    boolean bQuery = true;
    CatalogConfiguration config = getRequestContext().getCatalogConfiguration();
    String sUserTable = config.getUserTableName();
    String sDN = getQueryCriteria().getOwner();

    // only an administrator can query all records
    if (sDN.length() == 0) {
      if (getPublisher().getIsAdministrator()) {
        bQuery = false;
      } else {
        sDN = getPublisher().getDistinguishedName();
        getQueryCriteria().setOwner(sDN);
      }
    }

    // execute the query
    if (bQuery) {
        StringBuilder sbSql = new StringBuilder();
        sbSql.append("SELECT USERID FROM ").append(sUserTable);
        sbSql.append(" WHERE UPPER(DN)=?");
        logExpression(sbSql.toString());
        st = con.prepareStatement(sbSql.toString());
        st.setString(1,sDN.toUpperCase());
        ResultSet rs = st.executeQuery();
        if (rs.next()) {
          nUserId = rs.getInt(1);
        }
    }
  } finally {
    closeStatement(st);
  }
  return nUserId;
}

/**
 * Reads the ArcIMS owner id to be queried.
 * @param con the JDBC connection
 * @return the ArcIMS owner name to be queried (empty string for any owner)
 * @throws SQLException if a database exception occurs
 */
private String readImsOwnerName(Connection con) throws SQLException {
  String sImsOwnerName = "-1";
  PreparedStatement st = null;
  try {
    boolean bQuery = true;
    CatalogConfiguration config = getRequestContext().getCatalogConfiguration();
    String sUserTable = config.getUserTableName();
    String sDN = getQueryCriteria().getOwner();

    // only an administrator can query all records
    if (sDN.length() == 0) {
      if (getPublisher().getIsAdministrator()) {
        bQuery = false;
        sImsOwnerName = "";
      } else {
        sDN = getPublisher().getDistinguishedName();
        getQueryCriteria().setOwner(sDN);
      }
    }

    // execute the query
    if (bQuery) {
      StringBuilder sbSql = new StringBuilder();
      sbSql.append("SELECT USERNAME FROM ").append(sUserTable);
      sbSql.append(" WHERE UPPER(DN)=?");
      logExpression(sbSql.toString());
      st = con.prepareStatement(sbSql.toString());
      st.setString(1,sDN.toUpperCase());
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        sImsOwnerName = rs.getString(1);
      }
    }
  } finally {
    closeStatement(st);
  }
  return sImsOwnerName;
}

/**
 * Reads record data.
 * @param rs result set to read from
 * @param record record to write to
 * @throws SQLException if accessing database fails
 * @throws ParserConfigurationException if unable to reach parser configuration
 * @throws IOException if unable to perform IO operation
 * @throws SAXException if unable to parse XML data
 */
private void readRecord(ResultSet rs, MmdRecord record) throws SQLException, ParserConfigurationException, IOException, SAXException {
  int n = 1;

  // set the title and uuid
  record.setTitle(rs.getString(n++));
  record.setUuid(rs.getString(n++));
  record.setSiteUuid(rs.getString(n++));
  if (getActionCriteria().getSelectedRecordIdSet().contains(record.getUuid())) {
    record.setIsSelected(true);
  }

  // set the owner, approval status and publication method
  record.setOwnerName(rs.getString(n++));
  record.setApprovalStatus(rs.getString(n++));
  record.setPublicationMethod(rs.getString(n++));

  // set the update date,
  Timestamp ts = rs.getTimestamp(n++);
  if (ts != null) {
    record.setSystemUpdateDate(ts);
    record.setFormattedUpdateDate(DateProxy.formatDate(ts));
  }

  // set the ACL
  String aclXml = rs.getString(n++);
  if (aclXml != null && aclXml.trim().length() > 0) {
    record.setMetadataAccessPolicyType("Restricted");
    MetadataAcl acl = new MetadataAcl(getRequestContext());
    record.setCurrentMetadataAccessPolicy(acl.makeGroupsfromXml(allGroups,aclXml));
    record.setCurrentMetadataAccessPolicyKeys(acl.makeGroupsKeysfromXml(allGroups,aclXml));
  } else {
    record.setMetadataAccessPolicyType("Unrestricted");
    record.setCurrentMetadataAccessPolicy("Unrestricted");
    record.setCurrentMetadataAccessPolicyKeys("Unrestricted");
  }

  // set harvesting specific data
  record.setLocalId(rs.getInt(n++));
  record.setHostUrl(rs.getString(n++));
  String frequency = Val.chkStr(rs.getString(n++));
  if (frequency.length()>0)
    record.setHarvestFrequency(HarvestFrequency.checkValueOf(frequency));
  record.setSendNotification(Val.chkBool(rs.getString(n++), false));
  String protocol = Val.chkStr(rs.getString(n++));
  if (protocol.length()>0)
    record.setProtocol(getApplicationConfiguration().getProtocolFactories().parseProtocol(protocol));

  // set the editable status
  boolean isEditor = record.getPublicationMethod().equalsIgnoreCase(PublicationMethod.editor.name());
  boolean isSEditor = record.getPublicationMethod().equalsIgnoreCase(PublicationMethod.seditor.name());
  boolean isProtocol = record.getProtocol()!=null;
  boolean isOwner = hmEditablePublishers.containsKey(record.getOwnerName().toLowerCase());
  record.setCanEdit(
    (isEditor || isSEditor || isProtocol) &&
    ( isOwner || (isProtocol && isGptAdministrator))
  );

  // TODO remove as this is a temporary fix
  boolean isOther = record.getPublicationMethod().equalsIgnoreCase(PublicationMethod.other.name());
  if (isOther && isProtocol) {
    record.setPublicationMethod(PublicationMethod.registration.name());
  }

  record.setFindable(Val.chkBool(rs.getString(n++), false));
  record.setSearchable(Val.chkBool(rs.getString(n++), false));
  record.setSynchronizable(Val.chkBool(rs.getString(n++), false));
}

/**
 * Reads jobs statuses for each record in the collection
 * @param con connection
 * @param recordsMap records map
 * @param keys keys
 * @throws SQLException if accessing database fails
 */
private void readJobStatus(Connection con, TreeMap<String, MmdRecord> recordsMap, String keys) throws SQLException {
  if (keys.length()>0) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = con.prepareStatement("SELECT HARVEST_ID, JOB_STATUS FROM GPT_HARVESTING_JOBS_PENDING WHERE UPPER(HARVEST_ID) IN (" +keys.toString()+ ")");
      rs = st.executeQuery();

      while (rs.next()) {
        String harvestId = rs.getString(1);
        String jobStatus = rs.getString(2);
        MmdRecord r = recordsMap.get(harvestId);
        if (r!=null) {
          r.setRecentJobStatus(RecentJobStatus.checkValueOf(jobStatus));
        }
      }
    } finally {
      closeResultSet(rs);
      closeStatement(st);
    }
  }
}

/**
 * Reads last harvest date for each record in the collection.
 * @param con connection
 * @param recordsMap records map
 * @param keys keys
 * @throws SQLException if accessing database fails
 */
private void readLastHarvestDate(Connection con, TreeMap<String, MmdRecord> recordsMap, String keys) throws SQLException {
  if (keys.length()>0) {
    PreparedStatement st = null;
    ResultSet rs = null;
    try {
      st = con.prepareStatement("SELECT HARVEST_ID, MAX(HARVEST_DATE) FROM GPT_HARVESTING_HISTORY WHERE UPPER(HARVEST_ID) IN (" +keys.toString()+ ") GROUP BY HARVEST_ID");
      rs = st.executeQuery();

      while (rs.next()) {
        String harvestId = rs.getString(1);
        Timestamp lastHarvestDate = rs.getTimestamp(2);
        MmdRecord r = recordsMap.get(harvestId);
        if (r!=null) {
          r.setLastHarvestDate(lastHarvestDate);
        }
      }
    } finally {
      closeResultSet(rs);
      closeStatement(st);
    }
  }
}
}
