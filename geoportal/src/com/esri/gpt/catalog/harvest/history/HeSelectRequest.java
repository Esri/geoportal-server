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
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.UuidUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Select history events request.
 */
public class HeSelectRequest extends HeRequest {

// class variables =============================================================

// instance variables ==========================================================

/** Harvest repository as owner */  
private HrRecord _owner = new HrRecord();

// constructors ================================================================

/**
 * Create instance of the request.
 * @param requestContext request context
 * @param owner owner of the records
 * @param criteria request criteria
 * @param result request result
 */
public HeSelectRequest(RequestContext requestContext,
                       HrRecord owner,
                       HeCriteria criteria, 
                       HeResult result) {
  super(requestContext, criteria, result);
  setOwner(owner);
}

/**
 * Create instance of the request.
 * @param requestContext request context
 * @param owner owner of the records
 * @param eventUuid event uuid
 */
public HeSelectRequest(RequestContext requestContext,
                       HrRecord owner,
                       String eventUuid) {
  super(requestContext, new HeCriteria(), new HeResult());
  setOwner(owner);
  getQueryCriteria().setEventUuid(eventUuid);
}

// properties ==================================================================

/**
 * Gets owner.
 * @return harvest repository as owner
 */
public HrRecord getOwner() {
  return _owner;
}

/**
 * Sets owner.
 * @param owner harvest repository as owner
 */
public void setOwner(HrRecord owner) {
  _owner = owner!=null? owner: new HrRecord();
}

// methods =====================================================================

/**
 * Executes request.
 * @throws java.sql.SQLException if request execution fails
 */
public void execute() throws SQLException {
  // intitalize
  PreparedStatement st = null;
  PreparedStatement stCount = null;
  HeQueryCriteria criteria = getQueryCriteria();
  HeRecords records = getQueryResult().getRecords();
  PageCursor pageCursor = getQueryResult().getPageCursor();
  
  try {
    
    // start the SQL expression
    StringBuffer sbSql   = new StringBuffer();
    StringBuffer sbCount = new StringBuffer();
    StringBuffer sbFrom  = new StringBuffer();
    StringBuffer sbWhere = new StringBuffer();

    sbSql.append("SELECT A.UUID, A.HARVEST_DATE,");
    sbSql.append("A.HARVESTED_COUNT,A.VALIDATED_COUNT,A.PUBLISHED_COUNT");
    sbCount.append("SELECT COUNT(*)");
    
    // append from clause
    sbFrom.append(" FROM ");
    sbFrom.append(getHarvestingHistoryTableName());
    sbFrom.append(" A");
    sbSql.append(sbFrom);
    sbCount.append(sbFrom);

    appendValueFilter(sbWhere,"UPPER(A.HARVEST_ID)",
      getOwner().getUuid(),false);
    
    // harvest event UUID
    String sEventUuid = 
      UuidUtil.addCurlies(
      UuidUtil.removeCurlies(criteria.getEventUuid().toUpperCase()));
    if (sEventUuid.length() > 0) {
      sEventUuid = appendValueFilter(sbWhere,"UPPER(A.UUID)",sEventUuid,false);
    }
    
    // harvest date range
    Timestamp tsFrom = criteria.getDateRange().getFromTimestamp();
    Timestamp tsTo = criteria.getDateRange().getToTimestamp();
    if (tsFrom != null) {
      appendExpression(sbWhere,"A.HARVEST_DATE >= ?");
    }
    if (tsTo != null) {
      appendExpression(sbWhere,"A.HARVEST_DATE <= ?");
    }
    
    // append the where clause expressions
    if (sbWhere.length() > 0) {
      sbSql.append(" WHERE ").append(sbWhere.toString());
      sbCount.append(" WHERE ").append(sbWhere.toString());
    }
    
    // append the order by clause
    String sOrderByColumn = criteria.getSortOption().getColumnKey();
    String sOrderByDir = 
      criteria.getSortOption().getDirection().name().toUpperCase();
    if (sOrderByColumn.equalsIgnoreCase("report_id")) {
      sOrderByColumn = "A.UUID";
    } else if (sOrderByColumn.equalsIgnoreCase("harvest_date")) {
      sOrderByColumn = "A.HARVEST_DATE";
    } else if (sOrderByColumn.equalsIgnoreCase("harvested_count")) {
      sOrderByColumn = "A.HARVESTED_COUNT";
    } else if (sOrderByColumn.equalsIgnoreCase("validated_count")) {
      sOrderByColumn = "A.VALIDATED_COUNT";
    } else if (sOrderByColumn.equalsIgnoreCase("published_count")) {
      sOrderByColumn = "A.PUBLISHED_COUNT";
    } else {
      sOrderByColumn = "A.HARVEST_DATE";
      sOrderByDir = "DESC";
    }
    if (sOrderByDir.length() == 0) {
      sOrderByDir = "ASC";
    }
    sbSql.append(" ORDER BY ");
    sbSql.append(sOrderByColumn).append(" ").append(sOrderByDir);

    // establish the connection
    ManagedConnection mc = returnConnection();
    Connection con = mc.getJdbcConnection();
    
    // prepare the statements
    int n = 0;
    st = con.prepareStatement(sbSql.toString());
    stCount = con.prepareStatement(sbCount.toString());
    
    // owner id
    n++;
    st.setString(n,getOwner().getUuid().toUpperCase());
    stCount.setString(n,getOwner().getUuid().toUpperCase());
    
    // harvest UUID
    if (sEventUuid.length() > 0) {
      n++;
      st.setString(n,sEventUuid);
      stCount.setString(n,sEventUuid);
    }
    
    // harvest update date range
    if (tsFrom != null) {
      n++;
      st.setTimestamp(n,tsFrom);
      stCount.setTimestamp(n,tsFrom);     
    }
    if (tsTo != null) {
      n++;
      st.setTimestamp(n,tsTo);
      stCount.setTimestamp(n,tsTo);     
    }
    
    // query the count
    logExpression(sbCount.toString());
    ResultSet rsCount = stCount.executeQuery();
    if (rsCount.next()) {
      pageCursor.setTotalRecordCount(rsCount.getInt(1));
    }
    closeStatement(stCount);
    stCount = null;
    
    // query records if a count was found
    if (pageCursor.getTotalRecordCount() > 0) {

      // set the start record and the number of records to retrieve
      int nCurPage = pageCursor.getCurrentPage();
      int nRecsPerPage = getQueryResult().getPageCursor().getRecordsPerPage();
      int nStartRecord = ((nCurPage - 1) * nRecsPerPage) + 1;
      int nMaxRecsToRetrieve = nCurPage * nRecsPerPage;
      st.setMaxRows(nMaxRecsToRetrieve);

      // execute the query
      logExpression(sbSql.toString());
      ResultSet rs = st.executeQuery();

      // build the record set
      int nCounter = 0;
      while (rs.next()) {
        n = 1;
        nCounter++;
        if (nCounter >= nStartRecord) {
          HeRecord record = new HeRecord(getOwner());
          records.add(record);
          record.setUuid(rs.getString(n++));
          record.setHarvestDate(rs.getTimestamp(n++));
          record.setHarvestedCount(rs.getInt(n++));
          record.setValidatedCount(rs.getInt(n++));
          record.setPublishedCount(rs.getInt(n++));
          // break if we hit the max value for the cursor
          if (records.size() >= nRecsPerPage) {
            break;
          }
        }
      }
    }
    
  } finally {
    closeStatement(st);
    closeStatement(stCount);
  }
}

}
