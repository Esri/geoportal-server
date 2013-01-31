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

import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Repository delete request.
 */
public class HrDeleteRequest extends HrRequest {

// class variables =============================================================

// instance variables ==========================================================
/** Array of uids of harvest repositories to delete. */
private String[] _uuids = new String[]{};

// constructors ================================================================
/**
 * Create instance of the request.
 * @param requestContext request context
 * @param uuids uuids of records to delete
 */
public HrDeleteRequest(RequestContext requestContext, String[] uuids) {
  super(requestContext, new HrCriteria(), new HrResult());
  setUuids(uuids);
}
// properties ==================================================================
/**
 * Gets uuids of records to delete.
 * @return uuids of records to delete
 */
public String[] getUuids() {
  return _uuids;
}

/**
 * Sets uuids of records to delete.
 * @param uuids uuids of records to delete
 */
public void setUuids(String[] uuids) {
  ArrayList<String> validUuids = new ArrayList<String>();
  if (uuids != null) {
    for (String uuid: uuids) {
      uuid = Val.chkStr(uuid);
      if (UuidUtil.isUuid(uuid)) {
        validUuids.add(uuid);
      }
    }
  }
  _uuids = validUuids.toArray(new String[validUuids.size()]);
}

// methods =====================================================================
/**
 * Executes request.
 * @throws java.sql.SQLException if request execution fails
 */
public void execute() throws SQLException {
  Connection con = null;
  boolean autoCommit = true;

  if (getUuids().length > 0) {
    // intitalize
    PreparedStatement stJobsDelete = null;
    PreparedStatement stCompletedJobsDelete = null;
    PreparedStatement stHistoryDelete = null;

    ImsMetadataAdminDao adminDao = new ImsMetadataAdminDao(getRequestContext());

    try {

      StringBuffer sbJobsDeleteSql = new StringBuffer();
      StringBuffer sbCompletedJobsDeleteSql = new StringBuffer();
      StringBuffer sbHistoryDeleteSql = new StringBuffer();

      StringBuilder sbUuids = new StringBuilder();
      for (String uuid: getUuids()) {
        if (sbUuids.length() > 0) {
          sbUuids.append(",");
        }
        sbUuids.append("'" + uuid + "'");
      }

      sbJobsDeleteSql.append("delete from " +
        getHarvestingJobTableName() + " ");
      sbJobsDeleteSql.append("where HARVEST_ID = ?");

      sbCompletedJobsDeleteSql.append("delete from " +
        getHarvestingJobsCompletedTableName() + " ");
      sbCompletedJobsDeleteSql.append("where HARVEST_ID = ?");

      sbHistoryDeleteSql.append("delete from " +
        getHarvestingHistoryTableName() + " ");
      sbHistoryDeleteSql.append("where HARVEST_ID = ?");

      // establish the connection
      ManagedConnection mc = returnConnection();
      con = mc.getJdbcConnection();
      autoCommit = con.getAutoCommit();
      con.setAutoCommit(false);

      stJobsDelete = con.prepareStatement(sbJobsDeleteSql.toString());
      stCompletedJobsDelete = con.prepareStatement(sbCompletedJobsDeleteSql.
        toString());
      stHistoryDelete = con.prepareStatement(sbHistoryDeleteSql.toString());

      PreparedStatement[] stmts = new PreparedStatement[]{
        stJobsDelete,
        stCompletedJobsDelete,
        stHistoryDelete,
      };

      logExpression(stJobsDelete.toString());
      logExpression(stCompletedJobsDelete.toString());
      logExpression(stHistoryDelete.toString());

      int nRowCount = 0;

      for (String uuid: getUuids()) {
        nRowCount += executeForOne(adminDao, stmts, uuid);
      }
      
      getActionResult().setNumberOfRecordsModified(nRowCount);

      con.commit();

    } catch (SQLException ex) {
      if (con!=null) {
        con.rollback();
      }
      throw ex;
    } catch (Exception ex) {
      if (con!=null) {
        con.rollback();
      }
      throw new SQLException("Error deleting record.");
    } finally {
      closeStatement(stJobsDelete);
      closeStatement(stHistoryDelete);
      closeStatement(stCompletedJobsDelete);
      if (con!=null) {
        con.setAutoCommit(autoCommit);
      }
    }
  }
}

/**
 * Executes all prepared statements for one UUID.
 * @param adminDao admin dao
 * @param stmts array of statements
 * @param uuid UUID
 * @return number of records affected
 * @throws java.sql.SQLException if statement can not be executed
 */
private int executeForOne(ImsMetadataAdminDao adminDao, PreparedStatement[] stmts, String uuid) throws
  SQLException, CatalogIndexException {
  int nRowCount = 0;
  for (PreparedStatement st: stmts) {
    st.setString(1, uuid);
    nRowCount = st.executeUpdate();
  }
  adminDao.deleteRecord(uuid);
  return nRowCount;
}

/**
 * Reads all records designated to be deleted.
 * @return array of records to delete.
 * @throws SQLException if reading records fails
 */
private Map<String,HrRecord> readRecords() throws SQLException {
  TreeMap<String,HrRecord> records = new TreeMap<String,HrRecord>();
  for (String uuid: getUuids()) {
    HrSelectRequest request = new HrSelectRequest(getRequestContext(), uuid);
    request.execute();
    for (HrRecord hrRecord : request.getQueryResult().getRecords()) {
      records.put(hrRecord.getUuid(),hrRecord);
    }
  }
  return records;
}
}
