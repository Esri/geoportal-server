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
import java.util.logging.Logger;

/**
 * Request to find next job to run.
 * Next job if present, will be placed in query result. Status will be 
 * automatically changed to {@link HjRecord.JobStatus#Running}. If no more jobs
 * exist, no any record will be placed in {@link HjQueryResult}.
 */
public class HjGetNextRequest extends HjRequest {

// class variables =============================================================
private static final Logger LOGGER = Logger.getLogger(HjGetNextRequest.class.getCanonicalName());
// instance variables ==========================================================
// constructors ================================================================

/**
 * Creates instance of the request.
 * @param requestContext request context
 */
public HjGetNextRequest(RequestContext requestContext) {
  super(requestContext, new HjCriteria(), new HjResult());
}

// properties ==================================================================
// methods =====================================================================

/**
 * Executes request.
 * @throws SQLException if accessing database failed
 */
public void execute() throws SQLException {

  // establish the connection
  ManagedConnection mc = returnConnection();
  Connection con = mc.getJdbcConnection();
  
  // intitalize
  HjRecords records = getQueryResult().getRecords();
  
  HjRecord record = null;
  while ( (record=readNext(con))!=null) {
    if (markAsRunning(con, record)) {
      records.add(record);
      break;
    }
  }
}

/**
 * Reads next pending job record.
 * @param con database connection
 * @return next pending job record or <code>null</code> if no more pending jobs
 * @throws java.sql.SQLException if accessing database failed
 */
private HjRecord readNext(Connection con) throws SQLException {
  
  // intitalize
  PreparedStatement st = null;
  
  try {

    // start the SQL expression
    StringBuilder sbSql   = new StringBuilder();
    StringBuilder sbFrom  = new StringBuilder();
    StringBuilder sbWhere = new StringBuilder();
    sbSql.append("SELECT A.UUID,A.HARVEST_ID,A.INPUT_DATE,A.HARVEST_DATE");
    sbSql.append(",A.JOB_STATUS,A.JOB_TYPE,A.CRITERIA,A.SERVICE_ID");
    
    // append from clause
    sbFrom.append(" FROM ");
    sbFrom.append(getHarvestingJobsPendingTableName()).append(" A");
    sbSql.append(sbFrom);
    
    // create where clause
    if (getIsDbCaseSensitive(this.getRequestContext())) {
      sbWhere.append("UPPER(A.JOB_STATUS)='").append(HjRecord.JobStatus.Submited.name().toUpperCase()).append("'");
    } else {
      sbWhere.append("A.JOB_STATUS='").append(HjRecord.JobStatus.Submited.name().toUpperCase()).append("'");
    }
    
    // append the where clause expressions
    if (sbWhere.length() > 0) {
      sbSql.append(" WHERE ").append(sbWhere.toString());
      sbSql.append(" ORDER BY A.HARVEST_DATE ASC ");
    }
    
    // prepare the statements
    int n = 0;
    st = con.prepareStatement(sbSql.toString());

    // execute the query
    logExpression(sbSql.toString());
    ResultSet rs = st.executeQuery();
    
    while (rs.next()) {
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
 * Marsk given job as running.
 * @param con database connection
 * @param record record to mark as running
 * @return <code>true</code> if record has been marked
 * @throws java.sql.SQLException if accessing database failed
 */
private boolean markAsRunning(Connection con, HjRecord record) 
  throws SQLException {
  
  Date currentDate = new Date();
  
  // intitalize
  PreparedStatement st = null;
  try {

    // establish the connection
    ManagedConnection mc = returnConnection();
    con = mc.getJdbcConnection();

    StringBuilder sbStmt = new StringBuilder();

    String serviceId = getActionCriteria().getHostAddress();

    // update sql
    sbStmt.append("UPDATE ").append(getHarvestingJobsPendingTableName());
    sbStmt.append(" SET JOB_STATUS=?,HARVEST_DATE=?");
    if (serviceId.length()>0) {
      sbStmt.append(",SERVICE_ID=?");
    }
    if (getIsDbCaseSensitive(this.getRequestContext())) {
      sbStmt.append(" WHERE HARVEST_ID=? AND UPPER(JOB_STATUS)=?");
    } else {
      sbStmt.append(" WHERE HARVEST_ID=? AND JOB_STATUS=?");
    }

    st = con.prepareStatement(sbStmt.toString());

    int n = 1;
    st.setString(n++, HjRecord.JobStatus.Running.name());
    st.setTimestamp(n++, new java.sql.Timestamp(currentDate.getTime()));
    if (serviceId.length()>0) {
      st.setString(n++, serviceId);
    }
    st.setString(n++, record.getHarvestSite().getUuid());
    st.setString(n++, HjRecord.JobStatus.Submited.name().toUpperCase());

    logExpression(sbStmt.toString());

    int nRowCount = st.executeUpdate();
    getActionResult().setNumberOfRecordsModified(nRowCount);

    if (nRowCount>0) {
      record.setStatus(HjRecord.JobStatus.Running);
      record.setJobDate(currentDate);
    }
    
    return nRowCount>0;

  } finally {
    closeStatement(st);
  }
  
}

@Override
protected void logExpression(String expression) {
  if (expression!=null) {
    LOGGER.finer(expression);
  }
}
}
