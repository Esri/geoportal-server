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

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * Updates last successful synchronization date/time.
 */
public class HrUpdateLastSyncDate extends HrRequest {

  private HrRecord record;

  /**
   * Create instance of the request.
   * @param requestContext request context
   * @param record record to update
   */
  public HrUpdateLastSyncDate(RequestContext requestContext, HrRecord record) {
    super(requestContext, new HrCriteria(), new HrResult());
    this.record = record;
  }

  /**
   * Executes request.
   * @throws SQLException if accessing database fails
   */
  public void execute() throws SQLException {
    PreparedStatement st = null;

    if (record != null && UuidUtil.isUuid(record.getUuid())) {
      try {
        // establish the connection
        ManagedConnection mc = returnConnection();
        Connection con = mc.getJdbcConnection();

        StringBuilder sbSql = new StringBuilder();
        sbSql.append("UPDATE ").append(getHarvestingTableName()).append(" SET ");
        sbSql.append("LASTSYNCDATE=? ");
        sbSql.append("where DOCUUID=?");

        st = con.prepareStatement(sbSql.toString());

        int n = 1;
        st.setTimestamp(n++, makeTimestamp(record.getLastSyncDate()));
        st.setString(n++, record.getUuid());

        logExpression(sbSql.toString());

        int nRowCount = st.executeUpdate();
        getActionResult().setNumberOfRecordsModified(nRowCount);

      } finally {
        closeStatement(st);
      }
    }
  }

  /**
   * Creates timestamp from date.
   * If date is <code>null</code>, timestamp will be <code>null</code> as well.
   * @param date date to make timestamp
   * @return timestamp
   */
  private java.sql.Timestamp makeTimestamp(Date date) {
    return date != null ? new java.sql.Timestamp(date.getTime()) : null;
  }
}
