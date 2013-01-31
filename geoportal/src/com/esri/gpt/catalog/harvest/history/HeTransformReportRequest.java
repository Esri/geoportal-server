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
import com.esri.gpt.framework.xml.XsltTemplate;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * Transforms report.
 */
public class HeTransformReportRequest extends HeRequest {

  private static final Logger LOGGER = Logger.getLogger(HeTransformReportRequest.class.getCanonicalName());

  private HeRecord record;

  /**
   * Creates instance of the request.
   * @param requestContext request context
   * @param record record
   */
  public HeTransformReportRequest(RequestContext requestContext, HeRecord record) {
    super(requestContext, new HeCriteria(), new HeResult());
    this.record = record;
  }

  /**
   * Executes request.
   * @param template template to use for transformation
   * @param writer writer
   * @param mapParams transformation parameters
   * @throws Exception exceptions is transformation fails
   */
  public void execute(XsltTemplate template, Writer writer, Map mapParams) throws Exception {
    // intitalize
    PreparedStatement st = null;
    try {

      // start the SQL expression
      StringBuilder sbSql   = new StringBuilder();
      sbSql.append("SELECT A.HARVEST_REPORT");
      sbSql.append(" FROM ").append(getHarvestingHistoryTableName()).append(" A ");
      sbSql.append(" WHERE UPPER(A.UUID)=?");

      // establish the connection
      ManagedConnection mc = returnConnection();
      Connection con = mc.getJdbcConnection();

      st = con.prepareStatement(sbSql.toString());
      st.setString(1, record.getUuid().toUpperCase());

      // execute the query
      logExpression(sbSql.toString());
      ResultSet rs = st.executeQuery();

      if (rs.next()) {
        IClobMutator cm = mc.getClobMutator();
        InputStream in = null;
        try {
          in = cm.getStream(rs, 1);
          template.transform(new StreamSource(in), new StreamResult(writer), mapParams);
        } finally {
          if (in!=null) {
            try {
              in.close();
            } catch (IOException ex) {}
          }
        }
      } else {
        LOGGER.log(Level.SEVERE, "No report found for the specifed harvest event: {0}", record.getUuid());
      }

    } finally {
      closeStatement(st);
    }
  }

}
