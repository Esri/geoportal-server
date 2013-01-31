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
import com.esri.gpt.framework.util.Val;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Cancel request.
 */
public class HjCancelRequest extends HjRequest {

/**
 * Creates instance of the request.
 * @param requestContext request context
 * @param resourceUuid resource UUID
 */
public HjCancelRequest(RequestContext requestContext, String resourceUuid) {
  super(requestContext, new HjCriteria(), new HjResult());
  this.getActionCriteria().setResourceUuids(new String[]{Val.chkStr(resourceUuid)});
}

/**
 * Executes request.
 * @return <code>true</code> if records has been found and deleted
 * @throws SQLException if deleting jobs fails
 */
public boolean execute() throws SQLException {
  // intitalize
  PreparedStatement st = null;

  try {
    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    int updated = 0;

    // prepare statement
    st = con.prepareStatement(
        "DELETE FROM " + getHarvestingJobsPendingTableName()+ " " +
        "WHERE HARVEST_ID IN ("+this.getActionCriteria().getResourceUuidsForSql()+") " +
        "AND (SERVICE_ID='" +getActionCriteria().getHostAddress()+ "' OR SERVICE_ID IS NULL)");

    updated += st.executeUpdate();

    closeStatement(st);

    // prepare statement
    st = con.prepareStatement(
        "UPDATE " + getHarvestingJobsPendingTableName()+ " " +
        "SET JOB_STATUS='" +HjRecord.JobStatus.Canceled.name().toLowerCase()+ "' " +
        "WHERE HARVEST_ID IN ("+this.getActionCriteria().getResourceUuidsForSql()+")");

    // execute
    updated += st.executeUpdate();
    return updated>0;
  } finally {
    closeStatement(st);
  }
}

}
