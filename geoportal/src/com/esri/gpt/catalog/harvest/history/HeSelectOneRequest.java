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
package com.esri.gpt.catalog.harvest.history;

import com.esri.gpt.catalog.harvest.repository.HrRecords;
import com.esri.gpt.catalog.harvest.repository.HrSelectRequest;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Selects a single event.
 */
public class HeSelectOneRequest extends HeRequest {

/**
 * Create instance of the request.
 * @param requestContext request context
 * @param eventUuid event uuid
 */
public HeSelectOneRequest(RequestContext requestContext, String eventUuid) {
  super(requestContext, new HeCriteria(), new HeResult());
  getQueryCriteria().setEventUuid(eventUuid);
}

/**
 * Executes request.
 * @throws java.sql.SQLException if accessing database fails
 */
public void execute() throws SQLException {
  PreparedStatement st = null;
  HeQueryCriteria criteria = getQueryCriteria();
  HeRecords records = getQueryResult().getRecords();
  ResultSet rs = null;

  try {
    
    StringBuffer sbSql   = new StringBuffer();
    StringBuffer sbWhere = new StringBuffer();

    sbSql.append("SELECT A.HARVEST_ID,A.HARVEST_DATE,");
    sbSql.append("A.HARVESTED_COUNT,A.VALIDATED_COUNT,A.PUBLISHED_COUNT");
    sbSql.append(" FROM ");
    sbSql.append(getHarvestingHistoryTableName());
    sbSql.append(" A");

    // harvest event UUID
    String sEventUuid =
      UuidUtil.addCurlies(
      UuidUtil.removeCurlies(criteria.getEventUuid().toUpperCase()));
    if (sEventUuid.length() > 0) {
      sEventUuid = appendValueFilter(sbWhere,"UPPER(A.UUID)",sEventUuid,false);
    }

    // append the where clause expressions
    if (sbWhere.length() > 0) {
      sbSql.append(" WHERE ").append(sbWhere.toString());
    }

    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    // prepare the statements
    int n = 0;
    st = con.prepareStatement(sbSql.toString());

    // harvest UUID
    if (sEventUuid.length() > 0) {
      n++;
      st.setString(n,sEventUuid);
    }

    // execute the query
    logExpression(sbSql.toString());
    rs = st.executeQuery();

    while (rs.next()) {
      n = 1;
      String ownerUuid = rs.getString(n++);
      HrSelectRequest hrSelect = new HrSelectRequest(getRequestContext(),ownerUuid);
      hrSelect.execute();
      HrRecords hrRecords = hrSelect.getQueryResult().getRecords();
      if (!hrRecords.isEmpty()) {
        HeRecord record = new HeRecord(hrRecords.get(0));
        records.add(record);
        record.setUuid(sEventUuid);
        record.setHarvestDate(rs.getTimestamp(n++));
        record.setHarvestedCount(rs.getInt(n++));
        record.setValidatedCount(rs.getInt(n++));
        record.setPublishedCount(rs.getInt(n++));
      }
    }

  } finally {
    closeResultSet(rs);
    closeStatement(st);
  }

}
}
