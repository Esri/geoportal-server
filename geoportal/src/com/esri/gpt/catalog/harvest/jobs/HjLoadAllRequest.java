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

/**
 * Loads all pending jobs.
 */
public class HjLoadAllRequest extends HjRequest {

/**
 * Creates request.
 * @param requestContext request context
 */
public HjLoadAllRequest(RequestContext requestContext) {
  super(requestContext, new HjCriteria(), new HjResult());
}


/**
 * Creates request.
 * @param requestContext request context
 * @param resourceUuids resources UUID's
 */
public HjLoadAllRequest(RequestContext requestContext, String[] resourceUuids) {
  super(requestContext, new HjCriteria(), new HjResult());
  getQueryCriteria().setResourceUuids(resourceUuids);
}

/**
 * Executes request.
 * @throws SQLException if accessing database fails
 */
public void execute() throws SQLException {

  // establish the connection
  ManagedConnection mc = returnConnection();
  Connection con = mc.getJdbcConnection();

  // intitalize
  HjRecords records = getQueryResult().getRecords();

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

    if (getQueryCriteria().getResourceUuids().length>0) {
       StringBuilder sb = new StringBuilder();
       for (String harvestId : getQueryCriteria().getResourceUuids()) {
        if (sb.length()>0)
          sb.append(",");
        sb.append("'"+Val.chkStr(harvestId)+"'");
       }
       sbWhere.append(" WHERE A.HARVEST_ID IN (");
       sbWhere.append(sb);
       sbWhere.append(")");
    }

    sbSql.append(sbFrom);
    sbSql.append(sbWhere);

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

          records.add(record);
        }
      }
    }

  } finally {
    closeStatement(st);
  }

}
}
