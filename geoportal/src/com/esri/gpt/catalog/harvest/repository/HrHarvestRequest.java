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

import com.esri.gpt.catalog.harvest.jobs.HjRecord;
import com.esri.gpt.control.webharvest.engine.Harvester;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Harvest request.
 */
public class HrHarvestRequest extends HrRequest {

// class variables =============================================================

// instance variables ==========================================================
/** Array of uids of harvest repositories to harvest. */
private String[] _uuids = new String[]{};
/** Harvest type (<code>true</code> - full, <code>false</code> - now). */
private HjRecord.JobType _jobType = HjRecord.JobType.Now;

// constructors ================================================================
/**
 * Create instance of the request.
 * @param requestContext request appCtx
 * @param uuids uuids of records to delete
 * @param jobType job type
 * @param criteria request criteria
 * @param result request result
 */
public HrHarvestRequest(RequestContext requestContext,
                         String[] uuids,
                         HjRecord.JobType jobType,
                         HrCriteria criteria,
                         HrResult result) {
  super(requestContext, criteria, result);
  setUuids(uuids);
  setJobType(jobType);
}
// properties ==================================================================
/**
 * Gets uuids of records to harvest.
 * @return uuids of records to harvest
 */
public String[] getUuids() {
  return _uuids;
}

/**
 * Sets uuids of records to harvest.
 * @param uuids uuids of records to harvest
 */
public void setUuids(String[] uuids) {
  ArrayList<String> validUuids = new ArrayList<String>();
  if (uuids != null) {
    for (String uuid : uuids) {
      uuid = Val.chkStr(uuid);
      if (UuidUtil.isUuid(uuid)) {
        validUuids.add(uuid);
      }
    }
  }
  _uuids = validUuids.toArray(new String[validUuids.size()]);
}

/**
 * Gets job type.
 * @return job type
 */
public HjRecord.JobType getJobType() {
  return _jobType;
}

/**
 * Sets job type.
 * @param jobType job type
 */
public void setJobType(HjRecord.JobType jobType) {
  _jobType = jobType;
}

// methods =====================================================================
/**
 * Executes request.
 * @throws java.sql.SQLException if request execution fails
 */
public void execute() throws SQLException {
  String[] qualifiedUuids = extractQualifiedUuids(getUuids());
  HrRecords records = readRecords(qualifiedUuids);

  int maxRecs = Val.chkInt(getActionCriteria().getMaxRecs(), 0);
  Date timestamp = getActionCriteria().getFromDateAsDate();
  
  int nRecordsUpdated = 0;
  for (HrRecord record : records) {
    Date fromDate = timestamp!=null? timestamp: getJobType()==HjRecord.JobType.Now? record.getLastSyncDate(): null;
    if (insertJob(record, maxRecs>0? maxRecs: null, fromDate)) {
      nRecordsUpdated++;
    }
  }

  getActionResult().setNumberOfRecordsModified(nRecordsUpdated);
}

/**
 * Inserts a new job.
 * @param record record to insert a new job for
 * @param jobType job type
 * @return <code>true</code> if job has been created
 */
private boolean insertJob(HrRecord record, Integer maxRecs, Date fromDate) {
  ApplicationContext appCtx = ApplicationContext.getInstance();
  Harvester harvester = appCtx.getHarvestingEngine();
  return harvester.submit(getRequestContext(), record, maxRecs, fromDate);
}

/**
 * Reads all the records from uuids.
 * @param uuids uuids of the selected records.
 * @return collection of records
 * @throws java.sql.SQLException if accessing database failed
 */
private HrRecords readRecords(String[] uuids) throws SQLException {
  HrRecords records = new HrRecords();

  for (String uuid : uuids) {
    HrSelectRequest request = new HrSelectRequest(getRequestContext(), uuid);
    request.execute();
    records.addAll(request.getQueryResult().getRecords());
  }

  return records;
}

/**
 * Gets uuids of qualified harvest repositories.
 * <p/>
 * Qualified harvest repository is such a repository which has no any 
 * <i>submitted</i> or <i>running</i> jobs.
 * @param uuids array of uuids to check if qualify
 * @return array of qualified uuids
 */
private String[] extractQualifiedUuids(String[] uuids) throws SQLException {
  ArrayList<String> qualifiedUuids = new ArrayList<String>();

  String uuidAsString = getUuidsAsString(uuids);

  if (uuidAsString.length() > 0) {
    // intitalize
    PreparedStatement stSelect = null;

    try {
      // start the SQL expression
      StringBuffer sbSql = new StringBuffer();

      sbSql.append("SELECT DOCUUID FROM ");
      sbSql.append(getHarvestingTableName());
      sbSql.append(" WHERE DOCUUID IN (");
      sbSql.append(uuidAsString);
      sbSql.append(") ");
      sbSql.append(" AND DOCUUID NOT IN (");
      sbSql.append("SELECT DISTINCT HARVEST_ID FROM (");
      sbSql.append("SELECT HARVEST_ID, JOB_STATUS FROM ");
      sbSql.append(getHarvestingJobTableName());
      sbSql.append(" WHERE HARVEST_ID IN (");
      sbSql.append(uuidAsString);
      sbSql.append(") AND JOB_STATUS IN ('");
      sbSql.append(HjRecord.JobStatus.Submited.name().toLowerCase());
      sbSql.append("','");
      sbSql.append(HjRecord.JobStatus.Running.name().toLowerCase());
      sbSql.append("','");
      sbSql.append(HjRecord.JobStatus.Canceled.name().toLowerCase());
      sbSql.append("')");
      sbSql.append(") A");
      sbSql.append(")");

      // establish the connection
      ManagedConnection mc = returnConnection();
      Connection con = mc.getJdbcConnection();

      // prepare statement
      stSelect = con.prepareStatement(sbSql.toString());

      // execute the query
      logExpression(sbSql.toString());
      ResultSet rs = stSelect.executeQuery();

      while (rs.next()) {
        String uuid = rs.getString(1);
        qualifiedUuids.add(uuid);
      }

    } finally {
      closeStatement(stSelect);
    }
  }

  return qualifiedUuids.toArray(new String[qualifiedUuids.size()]);
}

/**
 * Gets uuids as string with all uuids separated by coma (,).
 * @param uuids array of uuids to transform into string
 * @return uuids as string
 */
private String getUuidsAsString(String[] uuids) {
  StringBuilder sb = new StringBuilder();

  for (String uuid : uuids) {
    if (UuidUtil.isUuid(uuid)) {
      if (sb.length() > 0) {
        sb.append(",");
      }
      sb.append("'" + uuid + "'");
    }
  }

  return sb.toString();
}
}
