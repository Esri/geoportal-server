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

import com.esri.gpt.control.webharvest.engine.Harvester;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Harvest history events delete request.
 */
public class HeDeleteRequest extends HeRequest {

// class variables =============================================================

// instance variables ==========================================================
/** Array of uids of harvest history events to delete. */
private String[] _uuids = new String[]{};

// constructors ================================================================
/**
 * Creates instance of the request.
 * @param requestContext request context
 * @param uuids uuid's of harvest history events to delete.
 */
public HeDeleteRequest(RequestContext requestContext,
                       String [] uuids) {
  super(requestContext, new HeCriteria(), new HeResult());
  setUuids(uuids);
}

// properties ==================================================================
/**
 * Gets uuid's of harvest history events to delete.
 * @return uuid's of harvest history events to delete
 */
public String[] getUuids() {
  return _uuids;
}

/**
 * Sets uuid's of harvest history events to delete.
 * @param uuids uuid's of harvest history events to delete
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

// methods =====================================================================
/**
 * Executes request.
 * @throws SQLException if error accessing database.
 */
public void execute() throws SQLException {
  if (getUuids().length>0) {

    PreparedStatement stHistoryDelete = null;

    try {
      StringBuffer sbHistoryDeleteSql = new StringBuffer();

      StringBuilder sbUuids = new StringBuilder();
      for (String uuid : getUuids()) {
        if (sbUuids.length() > 0) {
          sbUuids.append(",");
        }
        sbUuids.append("'" + uuid + "'");
      }

      sbHistoryDeleteSql.append("delete from " + 
        getHarvestingHistoryTableName() + " ");
      sbHistoryDeleteSql.append("where UUID in (" + 
        sbUuids.toString() + ")");

      // establish the connection
      ManagedConnection mc = returnConnection();
      Connection con = mc.getJdbcConnection();

      stHistoryDelete = con.prepareStatement(sbHistoryDeleteSql.toString());

      logExpression(stHistoryDelete.toString());

      int nRowCount = stHistoryDelete.executeUpdate();
      getActionResult().setNumberOfRecordsModified(nRowCount);

      // NEW in 10.0;  notify update
      Harvester harvestEngine = getRequestContext().getApplicationContext().getHarvestingEngine();
      harvestEngine.reselect();

    } finally {
      closeStatement(stHistoryDelete);
    }
  }
}

}
