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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.esri.gpt.catalog.harvest.jobs.HjRecord;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocol;
import com.esri.gpt.catalog.harvest.repository.HrRecord.RecentJobStatus;
import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.control.webharvest.protocol.ProtocolParseException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.identity.local.LocalDao;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.Users;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Harvest repository search request.
 */
public class HrSelectRequest extends HrRequest {

// class variables =============================================================
  private static final Logger LOGGER = Logger.getLogger(HrSelectRequest.class.getCanonicalName());
// instance variables ==========================================================
  /**
   * forces to ignore user logged in
   */
  private boolean _ignoreUser;
  /**
   * forces to ignore pagination
   */
  private boolean _ignorePagination;

// constructors ================================================================
  /**
   * Create instance of the request. Uses search criteria to select
   * repositories.
   *
   * @param requestContext request context
   * @param criteria request criteria
   * @param ignoreUser <code>true</code> to ignore logged in user and search
   * within repositories registered by any user
   * @param result request result
   */
  public HrSelectRequest(RequestContext requestContext,
          HrCriteria criteria,
          HrResult result,
          boolean ignoreUser) {
    super(requestContext, criteria, result);
    _ignoreUser = ignoreUser;
  }

  /**
   * Create instance of the request. Uses repository uui to pick exactly one
   * repository
   *
   * @param requestContext request context
   * @param uuid uuid of record to read
   */
  public HrSelectRequest(RequestContext requestContext, String uuid) {
    super(requestContext, new HrCriteria(), new HrResult());
    getQueryCriteria().setUuid(uuid);
    _ignoreUser = true;
    _ignorePagination = true;
  }

  /**
   * Create instance of the request. Uses localId to pick exactly one repository
   *
   * @param requestContext request context
   * @param localId local id of record to read
   */
  public HrSelectRequest(RequestContext requestContext, int localId) {
    super(requestContext, new HrCriteria(), new HrResult());
    getQueryCriteria().setLocalId(Integer.toString(localId));
    _ignoreUser = true;
    _ignorePagination = true;
  }

  /**
   * Create instance of the request. Creates list of all repositories
   *
   * @param requestContext request context
   */
  public HrSelectRequest(RequestContext requestContext) {
    super(requestContext, new HrCriteria(), new HrResult());
    _ignoreUser = true;
    _ignorePagination = true;
  }

  /**
   * Create instance of the request. Creates list of all repositories
   *
   * @param requestContext request context
   * @param ignoreUser <code>true</code> to ignore logged in user and search
   * within repositories registered by any user
   */
  public HrSelectRequest(RequestContext requestContext, boolean ignoreUser) {
    super(requestContext, new HrCriteria(), new HrResult());
    _ignoreUser = ignoreUser;
    _ignorePagination = true;
  }
// properties ==================================================================

  /**
   * Sets the ignore pagination.
   *
   * @param value the new ignore pagination
   */
  public void setIgnorePagination(boolean value) {
    this._ignorePagination = value;
  }

// methods =====================================================================
  /**
   * Executes request.
   *
   * @throws java.sql.SQLException if request execution fails
   */
  public void execute() throws SQLException {

    // intitalize
    PreparedStatement st = null;
    PreparedStatement stCount = null;
    HrQueryCriteria criteria = getQueryCriteria();
    HrRecords records = getQueryResult().getRecords();
    PageCursor pageCursor = getQueryResult().getPageCursor();

    try {

      // start the SQL expression
      StringBuffer sbSql = new StringBuffer();
      StringBuffer sbCount = new StringBuffer();
      StringBuffer sbFrom = new StringBuffer();
      StringBuffer sbWhere = new StringBuffer();
      StringBuffer sbJoin = new StringBuffer();

      sbSql.append("SELECT A.ID,A.DOCUUID,A.OWNER,A.INPUTDATE,A.UPDATEDATE");
      sbSql.append(",A.TITLE,A.HOST_URL,A.FREQUENCY");
      sbSql.append(",A.SEND_NOTIFICATION,A.PROTOCOL,H.LAST_HARVEST_DATE");
      sbSql.append(",A.FINDABLE,A.SEARCHABLE,A.SYNCHRONIZABLE,A.APPROVALSTATUS,A.LASTSYNCDATE");

      sbSql.append(",(SELECT COUNT(*) FROM " + getHarvestingJobTableName() + " HJ");
      sbSql.append(" WHERE HJ.HARVEST_ID=A.DOCUUID");
      if (getIsDbCaseSensitive(this.getRequestContext())) {
        sbSql.append(" AND UPPER(HJ.JOB_STATUS)='" + HjRecord.JobStatus.Submited.name().toUpperCase() + "') ");
      } else {
        sbSql.append(" AND HJ.JOB_STATUS='" + HjRecord.JobStatus.Submited.name().toUpperCase() + "') ");
      }

      sbSql.append(",(SELECT COUNT(*) FROM " + getHarvestingJobTableName() + " HJ");
      sbSql.append(" WHERE HJ.HARVEST_ID=A.DOCUUID");
      if (getIsDbCaseSensitive(this.getRequestContext())) {
        sbSql.append(" AND UPPER(HJ.JOB_STATUS)='" + HjRecord.JobStatus.Running.name().toUpperCase() + "') ");
      } else {
        sbSql.append(" AND HJ.JOB_STATUS='" + HjRecord.JobStatus.Running.name().toUpperCase() + "') ");
      }

      sbSql.append(",(SELECT COUNT(*) FROM " + getHarvestingJobTableName() + " HJ");
      sbSql.append(" WHERE HJ.HARVEST_ID=A.DOCUUID");
      if (getIsDbCaseSensitive(this.getRequestContext())) {
        sbSql.append(" AND UPPER(HJ.JOB_STATUS)='" + HjRecord.JobStatus.Completed.name().toUpperCase() + "') ");
      } else {
        sbSql.append(" AND HJ.JOB_STATUS='" + HjRecord.JobStatus.Completed.name().toUpperCase() + "') ");
      }

      sbSql.append(",(SELECT COUNT(*) FROM " + getHarvestingJobTableName() + " HJ");
      sbSql.append(" WHERE HJ.HARVEST_ID=A.DOCUUID");
      if (getIsDbCaseSensitive(this.getRequestContext())) {
        sbSql.append(" AND UPPER(HJ.JOB_STATUS)='" + HjRecord.JobStatus.Canceled.name().toUpperCase() + "') ");
      } else {
        sbSql.append(" AND HJ.JOB_STATUS='" + HjRecord.JobStatus.Canceled.name().toUpperCase() + "') ");
      }

      sbCount.append("SELECT COUNT(A.DOCUUID)");

      // append from clause
      sbFrom.append(" FROM ").append(getHarvestingTableName()).append(" A");

      sbSql.append(sbFrom);
      sbCount.append(sbFrom);

      // append join clause
      sbJoin.append(" LEFT JOIN (SELECT MAX(HH.HARVEST_DATE) AS LAST_HARVEST_DATE");
      sbJoin.append(",HH.HARVEST_ID AS UUID FROM GPT_HARVESTING_HISTORY HH ");
      sbJoin.append("GROUP BY HH.HARVEST_ID) H ON A.DOCUUID=H.UUID");

      sbSql.append(sbJoin);
      sbCount.append(sbJoin);

      // build the where clause
      // TODO remove for the final version after merging
      sbWhere.append(" (A.PROTOCOL IS NOT NULL) ");
      if (!_ignoreUser) {
        Users users = buildSelectablePublishers(getRequestContext());
        if (users.size() > 0) {
          StringBuilder sb = new StringBuilder();
          for (User u : users.values()) {
            if (sb.length() > 0) {
              sb.append(",");
            }
            sb.append(Integer.toString(u.getLocalID()));
          }
          if (sb.length() > 0) {
            if (sbWhere.length() > 0) {
              sbWhere.append(" and ");
            }
            sbWhere.append(" A.OWNER in (");
            sbWhere.append(sb.toString());
            sbWhere.append(") ");
          }
        }
      }

      // local harvest id
      String sLocalId = getQueryCriteria().getLocalId();
      if (sLocalId.length() > 0 && Val.chkInt(sLocalId, 0) > 0) {
        sLocalId = appendValueFilter(sbWhere, "A.ID", sLocalId, false);
      } else {
        sLocalId = "";
      }

      // harvest UUID
      String sHarvestUuid =
              UuidUtil.addCurlies(
              UuidUtil.removeCurlies(getQueryCriteria().getUuid().toUpperCase()));
      if (sHarvestUuid.length() > 0) {
        if (getIsDbCaseSensitive(this.getRequestContext())) {
          sHarvestUuid = appendValueFilter(sbWhere, "UPPER(A.DOCUUID)", sHarvestUuid, false);
        } else {
          sHarvestUuid = appendValueFilter(sbWhere, "A.DOCUUID", sHarvestUuid, false);
        }
      }

      // repository name
      String sName = criteria.getName().toUpperCase();
      if (sName.length() > 0) {
        if (getIsDbCaseSensitive(this.getRequestContext())) {
          sName = appendValueFilter(sbWhere, "UPPER(A.TITLE)", sName, true);
        } else {
          sName = appendValueFilter(sbWhere, "A.TITLE", sName, true);
        }
      }

      // host name
      String sHostUrl = criteria.getHost().toUpperCase();
      if (sHostUrl.length() > 0) {
        if (getIsDbCaseSensitive(this.getRequestContext())) {
          sHostUrl = appendValueFilter(sbWhere, "UPPER(A.HOST_URL)", sHostUrl, true);
        } else {
          sHostUrl = appendValueFilter(sbWhere, "A.HOST_URL", sHostUrl, true);
        }
      }

      // protocol type
      String sProtocolType = criteria.getProtocolTypeAsString().toUpperCase();
      if (criteria.getProtocolType() != HarvestProtocol.ProtocolType.None) {
        if (getIsDbCaseSensitive(this.getRequestContext())) {
          sProtocolType = appendValueFilter(sbWhere, "UPPER(A.PROTOCOL_TYPE)", sProtocolType, false);
        } else {
          sProtocolType = appendValueFilter(sbWhere, "A.PROTOCOL_TYPE", sProtocolType, false);
        }
      }

      // update date range
      Timestamp tsFrom = criteria.getDateRange().getFromTimestamp();
      Timestamp tsTo = criteria.getDateRange().getToTimestamp();
      if (tsFrom != null) {
        appendExpression(sbWhere, "A.UPDATEDATE >= ?");
      }
      if (tsTo != null) {
        appendExpression(sbWhere, "A.UPDATEDATE <= ?");
      }

      // harvest date range
      Timestamp tsHarvestFrom =
              criteria.getLastHarvestDateRange().getFromTimestamp();
      Timestamp tsHarvestTo = criteria.getLastHarvestDateRange().getToTimestamp();
      if (tsHarvestFrom != null) {
        appendExpression(sbWhere, "H.LAST_HARVEST_DATE >= ?");
      }
      if (tsHarvestTo != null) {
        appendExpression(sbWhere, "H.LAST_HARVEST_DATE <= ?");
      }

      // append the where clause expressions
      if (sbWhere.length() > 0) {
        sbSql.append(" WHERE ").append(sbWhere.toString());
        sbCount.append(" WHERE ").append(sbWhere.toString());
      }

      // append the order by clause
      String sOrderByColumn = criteria.getSortOption().getColumnKey();
      String sOrderByDir = criteria.getSortOption().getDirection().name();
      if (sOrderByColumn.equalsIgnoreCase("local_id")) {
        sOrderByColumn = "A.ID";
      } else if (sOrderByColumn.equalsIgnoreCase("harvest_id")) {
        sOrderByColumn = "A.DOCUUID";
      } else if (sOrderByColumn.equalsIgnoreCase("input_date")) {
        sOrderByColumn = "A.INPUTDATE";
      } else if (sOrderByColumn.equalsIgnoreCase("update_date")) {
        sOrderByColumn = "A.UPDATEDATE";
      } else if (sOrderByColumn.equalsIgnoreCase("last_harvest_date")) {
        sOrderByColumn = "H.LAST_HARVEST_DATE";
      } else if (sOrderByColumn.equalsIgnoreCase("name")) {
        sOrderByColumn = "A.TITLE";
      } else if (sOrderByColumn.equalsIgnoreCase("host_url")) {
        sOrderByColumn = "A.HOST_URL";
      } else if (sOrderByColumn.equalsIgnoreCase("protocol_type")) {
        sOrderByColumn = "A.PROTOCOL_TYPE";
      } else {
        sOrderByColumn = "A.INPUTDATE";
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

      // local harvest id 
      if (sLocalId.length() > 0) {
        n++;
        st.setInt(n, Val.chkInt(sLocalId, 0));
        stCount.setInt(n, Val.chkInt(sLocalId, 0));
      }

      // harvest UUID
      if (sHarvestUuid.length() > 0) {
        n++;
        st.setString(n, sHarvestUuid);
        stCount.setString(n, sHarvestUuid);
      }

      // repository name
      if (sName.length() > 0) {
        n++;
        st.setString(n, sName);
        stCount.setString(n, sName);
      }

      // host name
      if (sHostUrl.length() > 0) {
        n++;
        st.setString(n, sHostUrl);
        stCount.setString(n, sHostUrl);
      }

      // protocol type
      if (criteria.getProtocolType() != HarvestProtocol.ProtocolType.None) {
        n++;
        st.setString(n, sProtocolType);
        stCount.setString(n, sProtocolType);
      }

      // update date range
      if (tsFrom != null) {
        n++;
        st.setTimestamp(n, tsFrom);
        stCount.setTimestamp(n, tsFrom);
      }
      if (tsTo != null) {
        n++;
        st.setTimestamp(n, tsTo);
        stCount.setTimestamp(n, tsTo);
      }


      // harvest date range
      if (tsHarvestFrom != null) {
        n++;
        st.setTimestamp(n, tsHarvestFrom);
        stCount.setTimestamp(n, tsHarvestFrom);
      }
      if (tsHarvestTo != null) {
        n++;
        st.setTimestamp(n, tsHarvestTo);
        stCount.setTimestamp(n, tsHarvestTo);
      }

      // query the count
      logExpression(sbCount.toString());
      ResultSet rsCount = stCount.executeQuery();

      int nTotalRecordCount = 0;
      if (rsCount.next()) {
        nTotalRecordCount = rsCount.getInt(1);
        pageCursor.setTotalRecordCount(nTotalRecordCount);
      }
      closeStatement(stCount);
      stCount = null;

      // query records if a count was found
      if (nTotalRecordCount > 0) {

        // set the start record and the number of records to retrieve
        int nCurPage = pageCursor.getCurrentPage();
        int nRecsPerPage = getQueryResult().getPageCursor().getRecordsPerPage();
        int nStartRecord = ((nCurPage - 1) * nRecsPerPage) + 1;
        int nMaxRecsToRetrieve = nCurPage * nRecsPerPage;
        if (_ignorePagination || criteria.getDueOnly()) {
          st.setMaxRows(nTotalRecordCount);
        } else {
          st.setMaxRows(nMaxRecsToRetrieve);
        }

        // execute the query
        logExpression(sbSql.toString());
        ResultSet rs = st.executeQuery();

        // build the record set
        int nCounter = 0;
        while (rs.next()) {
          try {
            HrRecord record = null;
            if (criteria.getDueOnly()) {
              record = readRecord(con, rs);
              if (record.getIsHarvestDue()) {
                nCounter++;
              } else {
                nTotalRecordCount = nTotalRecordCount > 0 ? nTotalRecordCount - 1 : 0;
              }
            } else {
              nCounter++;
            }
            if (nCounter >= nStartRecord) {
              if (record == null) {
                record = readRecord(con, rs);
              }
              if (criteria.getDueOnly()) {
                if (record.getIsHarvestDue()) {
                  records.add(record);
                }
              } else {
                records.add(record);
              }
              // break if we hit the max value for the cursor
              if (!_ignorePagination && records.size() >= nRecsPerPage) {
                break;
              }
            }
          } catch (ProtocolParseException ex) {
            LOGGER.log(Level.WARNING, "Error reading record.", ex);
          }
        }

      }

      pageCursor.setTotalRecordCount(nTotalRecordCount);

    } finally {
      closeStatement(st);
      closeStatement(stCount);
    }
  }

  /**
   * Reads single record.
   *
   * @param con database connection
   * @param cm CLOB mutator
   * @param rs result set
   * @return record
   * @throws SQLException if accessing database fails
   * @throws ProtocolParseException if parsing harvest protocol fails
   */
  private HrRecord readRecord(Connection con, ResultSet rs) throws SQLException, ProtocolParseException {

    HrRecord record = new HrRecord();

    int n = 1;
    record.setLocalId(rs.getInt(n++));
    record.setUuid(rs.getString(n++));
    record.setOwnerId(rs.getInt(n++));
    record.setInputDate(rs.getTimestamp(n++));
    record.setUpdateDate(rs.getTimestamp(n++));
    record.setName(rs.getString(n++));
    record.setHostUrl(rs.getString(n++));
    record.setHarvestFrequency(
            HrRecord.HarvestFrequency.checkValueOf(rs.getString(n++)));
    record.setSendNotification(Val.chkBool(rs.getString(n++), false));
    record.setProtocol(getApplicationConfiguration().getProtocolFactories().parseProtocol(rs.getString(n++)));
    record.setLastHarvestDate(rs.getTimestamp(n++));
    record.setFindable(Val.chkBool(rs.getString(n++), false));
    record.setSearchable(Val.chkBool(rs.getString(n++), false));
    record.setSynchronizable(Val.chkBool(rs.getString(n++), false));
    record.setApprovalStatus(ApprovalStatus.checkValue(rs.getString(n++)));
    record.setLastSyncDate(rs.getTimestamp(n++));

    int submited = rs.getInt(n++);
    int running = rs.getInt(n++);
    int completed = rs.getInt(n++);
    int canceled = rs.getInt(n++);

    if (running > 0) {
      record.setRecentJobStatus(RecentJobStatus.Running);
    } else if (submited > 0) {
      record.setRecentJobStatus(RecentJobStatus.Submited);
    } else if (completed > 0) {
      record.setRecentJobStatus(RecentJobStatus.Completed);
    } else if (canceled > 0) {
      record.setRecentJobStatus(RecentJobStatus.Canceled);
    } else {
      record.setRecentJobStatus(RecentJobStatus.Unavailable);
    }

    return record;
  }

  /**
   * Builds a list of selectable publlishers.
   *
   * @param context request context
   * @return list of selectable publishers
   * @throws SQLException
   */
  private Users buildSelectablePublishers(RequestContext context)
          throws SQLException {
    Users allUsers = Publisher.buildSelectablePublishers(context, false);
    Users validUsers = new Users();
    if (allUsers.size() > 0) {
      LocalDao localDao = new LocalDao(getRequestContext());
      for (User u : allUsers.values()) {
        try {
          localDao.ensureReferenceToRemoteUser(u);
          validUsers.add(u);
        } catch (IdentityException ex) {
          LogUtil.getLogger().severe(
                  "Error ensuring reference to the remote user: " + ex.getMessage());
        }
      }

    }
    return validUsers;
  }
}
