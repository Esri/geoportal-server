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

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.control.webharvest.engine.Harvester;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * Updates harvest history event.
 */
public class HeUpdateRequest extends HeRequest {

// class variables =============================================================

// instance variables ==========================================================
/** Event to update. */
private HeRecord _event = new HeRecord(new HrRecord());  

// constructors ================================================================
/**
 * Create instance of the request.
 * @param requestContext request context
 * @param event event to update
 */
public HeUpdateRequest(RequestContext requestContext,
                       HeRecord event) {
  super(requestContext, new HeCriteria(), new HeResult());
  setEvent(event);
}

// properties ==================================================================

/**
 * Gets event to update.
 * @return event
 */
public HeRecord getEvent() {
  return _event;
}

/**
 * Sets event to update.
 * @param event event
 */
public void setEvent(HeRecord event) {
  _event = event!=null? event: new HeRecord(new HrRecord());
}

// methods =====================================================================

/**
 * Executes event.
 * @throws java.sql.SQLException if accessing database failed
 */
public void execute() throws SQLException {
  // intitalize
  PreparedStatement stInsert = null;
  PreparedStatement stUpdate = null;

  try {

    StringBuilder sbInsertSql = new StringBuilder();
    StringBuilder sbUpdateSql = new StringBuilder();

    sbInsertSql.append("INSERT INTO ").append(getHarvestingHistoryTableName()).append(" ");
    sbInsertSql.append("(HARVEST_ID,HARVEST_DATE,HARVESTED_COUNT,");
    sbInsertSql.append("VALIDATED_COUNT,PUBLISHED_COUNT,UUID) ");
    sbInsertSql.append("VALUES (?,?,?,?,?,?)");

    sbUpdateSql.append("UPDATE ").append(getHarvestingHistoryTableName()).append(" ");
    sbUpdateSql.append("SET HARVEST_ID=?,HARVEST_DATE=?,HARVESTED_COUNT=?,");
    sbUpdateSql.append("VALIDATED_COUNT=?,PUBLISHED_COUNT=? ");
    sbUpdateSql.append("WHERE UPPER(UUID)=?");


    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();
    
    stInsert = con.prepareStatement(sbInsertSql.toString());
    stUpdate = con.prepareStatement(sbUpdateSql.toString());
    
    PreparedStatement st = null;
    boolean isUpdate = false;
    String sUuid = "";
    Date harvestDate = new Date();
    
    if (UuidUtil.isUuid(getEvent().getUuid())) {
      sUuid = getEvent().getUuid().toUpperCase();
      harvestDate = getEvent().getHarvestDate();
      st = stUpdate;
      isUpdate = true;
    } else {
      sUuid = UuidUtil.makeUuid(true);
      st = stInsert;
    }
    
    int n = 1;
    st.setString(n++, getEvent().getRepository().getUuid());
    st.setTimestamp(n++,
      new java.sql.Timestamp(harvestDate.getTime()));
    st.setInt(n++, getEvent().getHarvestedCount());
    st.setInt(n++, getEvent().getValidatedCount());
    st.setInt(n++, getEvent().getPublishedCount());
    st.setString(n++, sUuid);
    
    if (isUpdate) {
      logExpression(sbUpdateSql.toString());
    } else {
      logExpression(sbInsertSql.toString());
    }
    
    int nRowCount = st.executeUpdate();
    getActionResult().setNumberOfRecordsModified(nRowCount);

    if (!isUpdate && nRowCount==1) {
      getEvent().setUuid(sUuid);
      getEvent().setHarvestDate(harvestDate);
      Harvester harvestEngine = getRequestContext().getApplicationContext().getHarvestingEngine();
      harvestEngine.reselect();
    }
    
  } finally {
    closeStatement(stInsert);
    closeStatement(stUpdate);
  }
}

}
