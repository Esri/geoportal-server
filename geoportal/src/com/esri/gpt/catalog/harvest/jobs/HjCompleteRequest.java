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
package com.esri.gpt.catalog.harvest.jobs;

import com.esri.gpt.catalog.harvest.repository.HrRecords;
import com.esri.gpt.catalog.harvest.repository.HrSelectRequest;
import com.esri.gpt.control.webharvest.common.CommonCriteria;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * Makes job completed.
 */
public class HjCompleteRequest extends HjRequest {

/** resource resourceUuid */
private String resourceUuid;

/**
 * Creates instance of the request.
 * @param requestContext request context
 * @param resourceUuid resource UUID
 */
public HjCompleteRequest(RequestContext requestContext, String resourceUuid) {
  super(requestContext, new HjCriteria(), new HjResult());
  this.resourceUuid = Val.chkStr(resourceUuid);
}

/**
 * Executes request.
 * @return <code>true</code> if the record has been updated
 * @throws java.sql.SQLException if error accesing database occured
 */
public boolean execute() throws SQLException {

    // establish the connection
  ManagedConnection mc = returnConnection();
  Connection con = mc.getJdbcConnection();

  HjRecord record = read(con);
  if (record!=null) {
    insertCompleted(con, record);
  }
  return delete(con);
}

/**
 * Inserts completed job.
 * @param con database connection
 * @param record job record to duplicate
 * @return <code>true</code> if the record has been updated
 * @throws java.sql.SQLException if error accesing database occured
 */
private boolean insertCompleted(Connection con, HjRecord record)
  throws SQLException {
  // intitalize
  PreparedStatement st = null;

  Date currentDate = new Date();

  try {

    // establish the connection
    ManagedConnection mc = returnConnection();
    con = mc.getJdbcConnection();

    StringBuffer sbStmt = new StringBuffer();

    // insert sql
    sbStmt.append("INSERT INTO " + getHarvestingJobsCompletedTableName());
    sbStmt.append(" (UUID,INPUT_DATE,HARVEST_DATE,");
    sbStmt.append(" JOB_TYPE,HARVEST_ID) ");
    sbStmt.append(" VALUES (?,?,?,?,?)");

    st = con.prepareStatement(sbStmt.toString());

    int n = 1;
    st.setString(n++, record.getUuid());
    st.setTimestamp(n++, new java.sql.Timestamp(record.getInputDate().getTime()));
    st.setTimestamp(n++, new java.sql.Timestamp(currentDate.getTime()));
    st.setString(n++, record.getType().name().toLowerCase());
    st.setString(n++, record.getHarvestSite().getUuid());

    logExpression(sbStmt.toString());

    int nRowCount = st.executeUpdate();
    getActionResult().setNumberOfRecordsModified(nRowCount);

    return nRowCount>0;

  } finally {
    closeStatement(st);
  }
}

/**
 * Reads job.
 * @param con database connection
 * @return job record or <code>null</code> if no job found
 * @throws java.sql.SQLException if error accesing database occured
 */
private HjRecord read(Connection con) throws SQLException {

  // intitalize
  PreparedStatement st = null;

  try {

    // start the SQL expression
    StringBuffer sbSql   = new StringBuffer();
    StringBuffer sbFrom  = new StringBuffer();
    StringBuffer sbWhere = new StringBuffer();
    sbSql.append("SELECT A.UUID,A.HARVEST_ID,A.INPUT_DATE,A.HARVEST_DATE");
    sbSql.append(",A.JOB_STATUS,A.JOB_TYPE,A.CRITERIA,A.SERVICE_ID");

    // append from clause
    sbFrom.append(" FROM ");
    sbFrom.append(getHarvestingJobsPendingTableName()).append(" A");
    sbSql.append(sbFrom);

    // create where clause
    if (getIsDbCaseSensitive(this.getRequestContext())) {
      sbWhere.append("UPPER(A.HARVEST_ID)=?");
    } else {
      sbWhere.append("A.HARVEST_ID=?");
    }

    // append the where clause expressions
    sbSql.append(" WHERE ").append(sbWhere.toString());

    // prepare the statements
    int n = 1;
    st = con.prepareStatement(sbSql.toString());
    st.setString(n++, resourceUuid.toUpperCase());

    // execute the query
    logExpression(sbSql.toString());
    ResultSet rs = st.executeQuery();

    if (rs.next()) {
      String harvestUuid = Val.chkStr(rs.getString(2));
      if (UuidUtil.isUuid(harvestUuid)) {
        HrSelectRequest harvestRequest =
          new HrSelectRequest(getRequestContext(), harvestUuid);
        harvestRequest.execute();
        HrRecords harvestRecords = harvestRequest.getQueryResult().getRecords();
        if (harvestRecords.size()>=1) {
          HjRecord record = new HjRecord(harvestRecords.get(0));
          n = 1;
          record.setUuid(rs.getString(n++));
          rs.getString(n++);
          record.setInputDate(rs.getTimestamp(n++));
          record.setJobDate(rs.getTimestamp(n++));
          record.setStatus(HjRecord.JobStatus.checkValueOf(rs.getString(n++)));
          record.setType(HjRecord.JobType.checkValueOf(rs.getString(n++)));
          record.setCriteria(CommonCriteria.parseXmlString(rs.getString(n++)));
          record.setServiceId(rs.getString(n++));

          return record;
        }
      }
    }

    return null;

  } finally {
    closeStatement(st);
  }
}

/**
 * Deletes job.
 * @param con database connection
 * @return <code>true</code> if the record has been deleted
 * @throws java.sql.SQLException if error accesing database occured
 */
private boolean delete(Connection con) throws SQLException {
  // intitalize
  PreparedStatement st = null;

  try {

    // establish the connection
    ManagedConnection mc = returnConnection();
    con = mc.getJdbcConnection();

    StringBuffer sbStmt = new StringBuffer();

    // update sql
    sbStmt.append("DELETE FROM " + getHarvestingJobsPendingTableName());
    sbStmt.append(" WHERE HARVEST_ID=?");

    st = con.prepareStatement(sbStmt.toString());

    int n = 1;
    st.setString(n++, resourceUuid);

    logExpression(sbStmt.toString());

    int nRowCount = st.executeUpdate();
    getActionResult().setNumberOfRecordsModified(nRowCount);

    return nRowCount>0;

  } finally {
    closeStatement(st);
  }
}

}
