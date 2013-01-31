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

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Resets running jobs.
 */
public class HjResetRunningRequest extends HjRequest {

/**
 * Creates instance of the request.
 * @param requestContext request context
 */
public HjResetRunningRequest(RequestContext requestContext) {
  super(requestContext, new HjCriteria(), new HjResult());
}

/**
 * Executes request.
 * @throws SQLException if accessing database failed
 */
public void execute() throws SQLException {
  // intitalize
  PreparedStatement stRecover = null;

  try {
    StringBuffer sbRecoverSql = new StringBuffer();
    StringBuffer sbWhere = new StringBuffer();

    sbRecoverSql.append("UPDATE " + getHarvestingJobsPendingTableName() + " ");
    sbRecoverSql.append("SET JOB_STATUS='"
        + HjRecord.JobStatus.Submited.name().toLowerCase() + "' ");

    sbWhere.append(" UPPER(JOB_STATUS)='"
        + HjRecord.JobStatus.Running.name().toUpperCase() + "' ");

    String sServiceName =
        appendValueFilter(sbWhere, "UPPER(SERVICE_ID)", getActionCriteria().getHostAddress(), false);

    // append the where clause expressions
    if (sbWhere.length() > 0) {
      sbRecoverSql.append(" WHERE ").append(sbWhere.toString());
    }

    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    stRecover = con.prepareStatement(sbRecoverSql.toString());

    int n = 1;
    // repository uuid
    if (sServiceName.length() > 0) {
      stRecover.setString(n++, sServiceName);
    }

    // execute the query
    logExpression(stRecover.toString());

    int nRowCount = stRecover.executeUpdate();
    getActionResult().setNumberOfRecordsModified(nRowCount);

  } finally {
    closeStatement(stRecover);
  }
}
}
