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

import com.esri.gpt.catalog.harvest.jobs.HjRecord.JobStatus;
import com.esri.gpt.catalog.harvest.jobs.HjRecord.JobType;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.control.webharvest.common.CommonCriteria;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * Create job request.
 * This request will insert new harvesting job. A job will not be created if any
 * other job already exist for the specific repository or repository doesn't 
 * exist.
 */
public class HjCreateRequest extends HjRequest {

// class variables =============================================================
// instance variables ==========================================================
/** parent harvest site */
private HrRecord _parent;
/** job type. Default: {@link HjRecord.JobType#Now} */
private JobType _jobType = JobType.Now;
/** job status. Default: {@link HjRecord.JobStatus#Submited} */
private JobStatus _jobStatus = JobStatus.Submited;
/** criteria */
private CommonCriteria criteria = new CommonCriteria();

// constructors ================================================================
/**
 * Creates instance of the request.
 * @param requestContext request context
 * @param parent parent harvest site
 * @param running <code>true</code> to make job marked as running
 */
public HjCreateRequest(RequestContext requestContext, HrRecord parent, CommonCriteria criteria, boolean running) {
  super(requestContext, new HjCriteria(), new HjResult());
  setParent(parent);
  setJobType(criteria == null || criteria.getFromDate() == null ? JobType.Full : JobType.Now);
  setJobStatus(running? JobStatus.Running: JobStatus.Submited);
  this.criteria = criteria;
}

// properties ==================================================================
/**
 * Gets parent site.
 * @return parent site
 */
private HrRecord getParent() {
  return _parent;
}

/**
 * Sets parent site.
 * @param parent parent site
 */
private void setParent(HrRecord parent) {
  _parent = parent != null ? parent : new HrRecord();
}

/**
 * Gets job type.
 * @return job type
 */
private HjRecord.JobType getJobType() {
  return _jobType;
}

/**
 * Sets job type.
 * @param jobType job type
 */
private void setJobType(HjRecord.JobType jobType) {
  _jobType = jobType;
}

/**
 * Gets job status.
 * @return job status.
 */
public JobStatus getJobStatus() {
  return _jobStatus;
}

/**
 * Sets job status.
 * @param jobStatus job status
 */
public final void setJobStatus(JobStatus jobStatus) {
  this._jobStatus = jobStatus;
}

// methods =====================================================================
/**
 * Inserts a new jobs into the database.
 * If there is any job for the particular repository, no record will be 
 * inserted.
 * @return <code>true</code> if any record has been inserted
 */
public boolean execute() {
  if (recover())
    return true;
  // nothing has been recovered; create new record
  return insert();
}

/**
 * Recovers possible canceled task.
 * @return <code>true</code> if any record has been recovered
 */
private boolean recover() {
  // intitalize
  PreparedStatement st = null;

  Date currentDate = new Date();

  try {

    StringBuilder sbStmt = new StringBuilder();

    // insert sql
    sbStmt.append("UPDATE ").append(getHarvestingJobsPendingTableName());
    sbStmt.append(" SET INPUT_DATE=?,HARVEST_DATE=?,JOB_STATUS=?,");
    sbStmt.append(" JOB_TYPE=?,CRITERIA=?");
    if (getIsDbCaseSensitive(this.getRequestContext())) {
      sbStmt.append(" WHERE HARVEST_ID=? AND UPPER(JOB_STATUS)='").append(JobStatus.Canceled.name().toUpperCase()).append("'");
    } else {
      sbStmt.append(" WHERE HARVEST_ID=? AND JOB_STATUS='").append(JobStatus.Canceled.name().toUpperCase()).append("'");
    }


    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    // prepare statement
    st = con.prepareStatement(sbStmt.toString());

    int n = 1;
    st.setTimestamp(n++, new java.sql.Timestamp(currentDate.getTime()));
    st.setTimestamp(n++, new java.sql.Timestamp(currentDate.getTime()));
    st.setString(n++, HjRecord.JobStatus.Submited.name().toLowerCase());
    st.setString(n++, getJobType().name().toLowerCase());
    st.setString(n++, criteria.toXmlString());
    st.setString(n++, getParent().getUuid());

    logExpression(sbStmt.toString());

    // execute
    int nRowCount = st.executeUpdate();
    getActionResult().setNumberOfRecordsModified(nRowCount);

    return nRowCount > 0;

  } catch (SQLException ex) {

    return false;

  } finally {
    closeStatement(st);
  }
}

/**
 * Inserts a new record.
 * @return <code>true</code> if new record has been inserted
 */
private boolean insert() {
  // intitalize
  PreparedStatement st = null;

  Date currentDate = new Date();

  try {

    StringBuilder sbStmt = new StringBuilder();

    // insert sql
    sbStmt.append("INSERT INTO ").append(getHarvestingJobsPendingTableName());
    sbStmt.append(" (UUID,INPUT_DATE,HARVEST_DATE,JOB_STATUS,");
    sbStmt.append(" JOB_TYPE,CRITERIA,HARVEST_ID,SERVICE_ID) ");
    sbStmt.append(" VALUES (?,?,?,?,?,?,?,?)");


    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    // prepare statement
    st = con.prepareStatement(sbStmt.toString());

    // init statement
    String sUuid = UuidUtil.makeUuid(true);

    int n = 1;
    st.setString(n++, sUuid);
    st.setTimestamp(n++, new java.sql.Timestamp(currentDate.getTime()));
    st.setTimestamp(n++, new java.sql.Timestamp(currentDate.getTime()));
    st.setString(n++, getJobStatus().name().toLowerCase());
    st.setString(n++, getJobType().name().toLowerCase());
    st.setString(n++, criteria.toXmlString());
    st.setString(n++, getParent().getUuid());
    st.setString(n++, getActionCriteria().getHostAddress());

    logExpression(sbStmt.toString());

    // execute
    int nRowCount = st.executeUpdate();
    getActionResult().setNumberOfRecordsModified(nRowCount);

    return nRowCount > 0;

  } catch (SQLException ex) {

    return false;

  } finally {
    closeStatement(st);
  }
}
}
