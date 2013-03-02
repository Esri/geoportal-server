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
import com.esri.gpt.framework.util.Val;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Asserts unique URL.
 */
public class HrAssertUrlRequest extends HrRequest {

  private String url;

  /**
   * Create instance of the request.
   * @param requestContext request context
   * @param url URL to assert
   */
  public HrAssertUrlRequest(RequestContext requestContext, String url) {
    super(requestContext, new HrCriteria(), new HrResult());
    this.url = Val.chkStr(url);
  }

  /**
   * Executes assertion.
   * @throws SQLException if accessing database fails
   * @throws HrAssertUrlException if record with the same URL already exist in database
   */
  public void executeAssert() throws SQLException, HrAssertUrlException {
    if (execute()) {
      throw new HrAssertUrlException();
    }
  }

  /**
   * Executes request.
   * @return <code>true</code> if record with identical host URL exists
   * @throws SQLException if accessing database fails
   */
  public boolean execute() throws SQLException {
    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();

    PreparedStatement st = null;
    ResultSet rs = null;

    try {
      StringBuilder sbSql = new StringBuilder();

      sbSql.append("SELECT DOCUUID FROM ").append(getHarvestingTableName()).append(" ");
      sbSql.append("WHERE UPPER(HOST_URL)=?");

      st = con.prepareStatement(sbSql.toString());
      st.setString(1, url.toUpperCase());

      rs = st.executeQuery();

      return rs.next();

    } finally {
      closeResultSet(rs);
      closeStatement(st);
    }
  }
}
