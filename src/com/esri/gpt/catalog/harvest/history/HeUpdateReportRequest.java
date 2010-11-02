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

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.IClobMutator;
import com.esri.gpt.framework.sql.ManagedConnection;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Updates report request.
 */
public class HeUpdateReportRequest extends HeRequest {

  private HeRecord record;

  /**
   * Creates instance of the request.
   * @param context request context
   * @param record event record
   */
  public HeUpdateReportRequest(RequestContext context, HeRecord record) {
    super(context, new HeCriteria(), new HeResult());
    this.record = record;
  }

  /**
   * Executes request.
   * @param report input stream with the report
   * @throws SQLException if writing report fails
   */
  public void execute(InputStream report, long length) throws SQLException {
    // intitalize
    PreparedStatement st = null;

    try {

      // start the SQL expression
      StringBuilder sbSql   = new StringBuilder();
      sbSql.append("UPDATE ").append(getHarvestingHistoryTableName()).append(" ");
      sbSql.append("SET HARVEST_REPORT=? ");
      sbSql.append("WHERE UPPER(UUID)=?");

      // establish the connection
      ManagedConnection mc = returnConnection();
      Connection con = mc.getJdbcConnection();
      IClobMutator cm = mc.getClobMutator();

      st = con.prepareStatement(sbSql.toString());
      int n = 1;
      cm.setStream(st, n++, report, length);
      st.setString(n++, record.getUuid());

      st.executeUpdate();
      
    } finally {
      closeStatement(st);
    }

  }
}
