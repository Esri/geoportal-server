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

package com.esri.gpt.server.usage.harvester;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.esri.gpt.control.rest.writer.ResponseWriter;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.sql.BaseDao;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.DateProxy;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.usage.api.IStatisticsWriter;

/**
 * Harvester statistics data access object.
 */
public class HarvesterStatisticsDao extends BaseDao {
	
	// class variables =============================================================

	// instance variables ==========================================================
	private ResponseWriter writer = null;
	private IStatisticsWriter statWriter;
	private boolean isDbCaseSensitive = false;

	// constructors ================================================================
    /**
     * Creates instance of the DAO.
     * @param writer response writer
     */
	public HarvesterStatisticsDao(ResponseWriter writer){
		this.writer = writer;
		this.statWriter = (IStatisticsWriter) writer;
		StringAttributeMap params  = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters();
	    String s = Val.chkStr(params.getValue("database.isCaseSensitive"));
	    isDbCaseSensitive = !s.equalsIgnoreCase("false");

	}

	// properties ==================================================================


	// methods ==================================================================			
	/**
	 * Adds clauses to sql query based on given constraints.
	 * @param uuids the uuids constraint
	 * @param startDate the start date constraint
	 * @param endDate the end date constraint
	 * @param dateField the date field to be used to apply date constraints
	 * @return the where clause
	 */	
	private String addClause(String uuids, String startDate, String endDate, String dateField){		
		StringBuilder sbSelectSql = new StringBuilder();
		boolean addWhere = true;
		String[] uuidArr = null;
		if(uuids.length() > 0 && uuids.indexOf(",") > -1){
			uuidArr = uuids.split(",");
			if(uuidArr.length > 10){
				uuidArr = null;
				uuids = "";
			}else{
				uuids = "(";
				for (int i =0; i< uuidArr.length ; i++){
					if(i==0){
						uuids += "?";
					}else{
						uuids +=  ",?";
					}
				}
				uuids += ")";
			}
		}else if(uuids.length() >0){
			uuids = "(?)";
		}
		
		  if(uuids.length() > 0){
			  sbSelectSql.append("WHERE UUID IN ").append(uuids);
			  addWhere = false;
		  }
		  if(startDate.length() > 0){
			  if(addWhere) sbSelectSql.append(" WHERE ");
			  else sbSelectSql.append(" AND ");
			  addWhere = false;
			  sbSelectSql.append(dateField).append(" >= ?");  
		  }
		  if(endDate.length() > 0){
			  if(addWhere) sbSelectSql.append(" WHERE ");
			  else sbSelectSql.append(" AND ");
			  sbSelectSql.append(dateField).append(" <= ?");    
		  }
		  sbSelectSql.append(" ORDER BY ").append(dateField);
		  return sbSelectSql.toString();
	}
	
	/**
	 * Creates Select SQL to count harvesting history table
	 * @return Select SQL
	 */
	protected String createHistoryCountsSQL(int timePeriod) {
	  StringBuilder sbSelectSql = new StringBuilder();
	  sbSelectSql.append("select sum(HARVESTED_COUNT) as hc,sum(VALIDATED_COUNT) as vc, sum(PUBLISHED_COUNT) as pc from ")
	  .append(getHarvestingHistoryTableName());
	  if(timePeriod != -1){
		  sbSelectSql.append(" where HARVEST_DATE > ? ");
	  }
	  return sbSelectSql.toString();
	}
	
	/**
	 * Creates Select SQL to fetch rows harvesting history table
	 * based on constraints.
	 * @return Select SQL
	 */
	protected String createSelectHistorySQL(String uuids, String startDate, String endDate) {
	  StringBuilder sbSelectSql = new StringBuilder();
	  sbSelectSql.append("select UUID,HARVEST_ID ,HARVEST_DATE,HARVESTED_COUNT,VALIDATED_COUNT,PUBLISHED_COUNT from ")
	  .append(getHarvestingHistoryTableName()).append(" ");
	  sbSelectSql.append(addClause(uuids, startDate, endDate, "HARVEST_DATE"));
	  return sbSelectSql.toString();
	}
	
	/**
	 * Creates Select SQL to count jobs grouped by 'Running' or 'submitted' statuses.
	 * @return Select SQL
	 */
	protected String createCountPendingSQLByStatus() {
	  StringBuilder sbSelectSql = new StringBuilder();
	  sbSelectSql.append("select count(*) as cnt, job_status from ")
	  .append(getHarvestingJobTableName());
	  if(isDbCaseSensitive){
		  sbSelectSql.append(" where UPPER(job_status) in ('RUNNING','SUBMITED') "); 
	  }else {
		  sbSelectSql.append(" where job_status in ('Running','submited') "); 
	  }
	  sbSelectSql.append(" group by job_status order by job_status desc");
	  return sbSelectSql.toString();
	}
	
	/**
	 * Creates Select SQL to count jobs with 'Running' or 'submitted' statuses.
	 * @return Select SQL
	 */
	protected String createCountPendingSQL(int timePeriod) {
	  StringBuilder sbSelectSql = new StringBuilder();
	  sbSelectSql.append("select count(*) as cnt from ")
	  .append(getHarvestingJobTableName());
	  if(isDbCaseSensitive){
		  sbSelectSql.append(" where UPPER(job_status) in ('RUNNING','SUBMITED') "); 
	  }else {
		  sbSelectSql.append(" where job_status in ('Running','submited') "); 
	  }
	  if(timePeriod != -1){
		  sbSelectSql.append(" and INPUT_DATE >= ? ");
	  }
	  return sbSelectSql.toString();
	}
	
	/**
	 * Creates Select SQL to count completed jobs.
	 * @return Select SQL
	 */
	protected String createCountCompletedSQL(int timePeriod) {
	  StringBuilder sbSelectSql = new StringBuilder();
	  sbSelectSql.append("select count(*) as cnt from ")
	  .append(getHarvestingJobsCompletedTableName());
	  if(timePeriod != -1){
		  sbSelectSql.append(" where harvest_date >= ? ");
	  }
	  return sbSelectSql.toString();
	}
	
	
	/**
	 * Creates Select SQL to select pending jobs based on criteria.
	 * @return Select SQL
	 */
	protected String createSelectPendingSQL(String uuids, String startDate, String endDate) {
	  StringBuilder sbSelectSql = new StringBuilder();
	  sbSelectSql.append("select UUID,HARVEST_ID ,HARVEST_DATE, INPUT_DATE,JOB_TYPE,JOB_STATUS,CRITERIA,SERVICE_ID from ")
	  .append(getHarvestingJobTableName()).append(" ");
	  sbSelectSql.append(addClause(uuids, startDate, endDate, "INPUT_DATE"));	  
	  return sbSelectSql.toString();
	}
			
	/**
	 * Creates Select SQL to select complete jobs based on criteria.
	 * @return Select SQL
	 */
	protected String createSelectCompletedSQL(String uuids, String startDate, String endDate) {
	  StringBuilder sbSelectSql = new StringBuilder();
	  sbSelectSql.append("select UUID,HARVEST_ID ,HARVEST_DATE, INPUT_DATE,JOB_TYPE,SERVICE_ID from ")
	  .append(getHarvestingJobsCompletedTableName()).append(" ");
	  sbSelectSql.append(addClause(uuids, startDate, endDate, "INPUT_DATE"));	  
	  return sbSelectSql.toString();
	}
			
	/**
	 * Create sql to fetch count of registered sites
	 * @return the sql string
	 */
	protected String createCountOfRegisteredSites(){
		StringBuilder sbSelectSql = new StringBuilder();
		sbSelectSql.append("SELECT count(*) as cnt FROM ").append(getResourceTableName());
		if(isDbCaseSensitive){
			  sbSelectSql.append(" where UPPER(pubmethod)='REGISTRATION' "); 
		}else {
			  sbSelectSql.append(" where pubmethod='registration' "); 
		}
		return sbSelectSql.toString();
	}
	
	/**
	 * Create sql to fetch count of approved sites
	 * @return the sql string
	 */
	protected String createCountOfApprovedSites(){
		StringBuilder sbSelectSql = new StringBuilder();
		sbSelectSql.append("SELECT count(*) as cnt FROM ").append(getResourceTableName());
		if(isDbCaseSensitive){
			sbSelectSql.append(" where UPPER(pubmethod)='REGISTRATION' and UPPER(approvalstatus)='APPROVED'");
		}else{
			sbSelectSql.append(" where pubmethod='registration' and approvalstatus='approved'");
		}
		return sbSelectSql.toString();
	}
	
	/**
	 * Create sql to fetch count of approved sites that are on a schedule
	 * @return the sql string
	 */
	protected String createCountOfApprovedSitesOnSchedule(){
		StringBuilder sbSelectSql = new StringBuilder();
		sbSelectSql.append("SELECT count(*) as cnt FROM ").append(getResourceTableName());
		if(isDbCaseSensitive){
			sbSelectSql.append(" where UPPER(pubmethod)='REGISTRATION' and UPPER(approvalstatus)='APPROVED' and UPPER(synchronizable) = 'TRUE'");
		}else{
			sbSelectSql.append(" where pubmethod='registration' and approvalstatus='approved' and synchronizable = 'true'");
		}
		return sbSelectSql.toString();
	}
	
	/**
	 * Create sql to fetch count of approved sites by protocol
	 * @return the sql string
	 */
	protected String createCountOfApprovedSitesByProtocol(){
		StringBuilder sbSelectSql = new StringBuilder();
		sbSelectSql.append("SELECT count(*) as cnt,protocol_type FROM ").append(getResourceTableName());
		if(isDbCaseSensitive){
			sbSelectSql.append(" where UPPER(pubmethod)='REGISTRATION' and UPPER(approvalstatus)='APPROVED' ");
		}else{
			sbSelectSql.append(" where pubmethod='registration' and approvalstatus='approved' ");
		} 
		sbSelectSql.append(" group by protocol_type");
		return sbSelectSql.toString();
	}
	
	/**
	 * Create sql to fetch count of registered sites by protocol
	 * @return the sql string
	 */
	protected String createCountOfRegisteredSitesByProtocol(){
		StringBuilder sbSelectSql = new StringBuilder();
		sbSelectSql.append("SELECT count(*) as cnt,protocol_type FROM ").append(getResourceTableName());
		if(isDbCaseSensitive){
			sbSelectSql.append(" where UPPER(pubmethod)='REGISTRATION' ");
		}else{
			sbSelectSql.append(" where pubmethod='registration' ");
		}
		sbSelectSql.append(" group by protocol_type");
		return sbSelectSql.toString();
	}
	
	/**
	 * Create sql to select distinct of registered sites  on schedule by protocol
	 * @return the sql string
	 */
	private String selectDistinctHarvestSitesOnScheduleOrderByProtocol(){
		StringBuilder sbSelectSql = new StringBuilder();
		sbSelectSql.append("SELECT distinct docuuid,protocol_type from ").append(getResourceTableName());
		if(isDbCaseSensitive){
			sbSelectSql.append(" where UPPER(pubmethod)='REGISTRATION' and UPPER(approvalstatus)='APPROVED' and UPPER(synchronizable) = 'TRUE'");
		}else{
			sbSelectSql.append(" where pubmethod='registration' and approvalstatus='approved' and synchronizable = 'true'");
		}
		sbSelectSql.append(" order by protocol_type");
		return sbSelectSql.toString();
	}
	
	/**
	 * Create sql to fetch count of approved sites on a schedule by protocol
	 * @return the sql string
	 */
	protected String createCountOfApprovedSitesOnScheduleByProtocol(){
		StringBuilder sbSelectSql = new StringBuilder();
		sbSelectSql.append("SELECT count(*) as cnt,protocol_type FROM ").append(getResourceTableName());
		if(isDbCaseSensitive){
			sbSelectSql.append(" where UPPER(pubmethod)='REGISTRATION' and UPPER(approvalstatus)='APPROVED' and UPPER(synchronizable) = 'TRUE'");
		}else{
			sbSelectSql.append(" where pubmethod='registration' and approvalstatus='approved' and synchronizable = 'true'");
		}
		sbSelectSql.append(" group by protocol_type");
		return sbSelectSql.toString();
	}
	
	/**
	 * Create sql to fetch published document count for a site.
	 * @return the sql string
	 */
	protected String createDocumentCount(){
		StringBuilder sbSelectSql = new StringBuilder();
		sbSelectSql.append("SELECT sum(published_count) as pc FROM ").append(getHarvestingHistoryTableName())
		.append(" where harvest_id = ? ");
		return sbSelectSql.toString();
	}
			
	/**
	 * Collect docuuid of sites on a schedule by protocol type
	 * @param protocolMap map of docuuid of sites on schedule and their corresponding protocol type
	 * @return docuuid map
	 * @throws SQLException if sql exception occurs
	 */
	protected HashMap<String,String> collectSitesByProtocolType(HashMap<String, Object> protocolMap) throws SQLException {
		 ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;
	      HashMap<String,String> docuuidMap = new HashMap<String,String>();
	    try {  
	      st = con.prepareStatement(selectDistinctHarvestSitesOnScheduleOrderByProtocol());	    
	      rs = st.executeQuery();	     
		  while(rs.next()){
			  String docuuid = Val.chkStr(rs.getString("docuuid"));
			  String protocolType = Val.chkStr(rs.getString("protocol_type"));
			  docuuidMap.put(docuuid, protocolType);	  
		  }
		  return docuuidMap;		  
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);	     
	    }
	}
	
	
	
	/**
	 * Fetch count of records using given sql and timeperiod
	 * @param sql the sql query to execute
	 * @param timePeriod the number of days from current date
	 * @return count value
	 * @throws SQLException if sql exception occurs
	 */
	protected int fetchCountByTime(String sql, int timePeriod) throws SQLException {
		int count = 0;
		 ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;	     
	    try {  
	      st = con.prepareStatement(sql);
	      if(timePeriod != -1){
	    	  st.setTimestamp(1, DateProxy.subtractDays(timePeriod));
	      }
	      rs = st.executeQuery();
		  while(rs.next()){
			  count = rs.getInt("cnt");
		  }
	      return count;
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);	     
	    }
	}
	
	
	
	/**
	 * Fetches information from harvesting complete table
	 * @param sql the sql query to execute
	 * @param uuids the uuids constraint
	 * @param startDate the start date constraint
	 * @param endDate the end date constraint
	 * @throws Exception if exception occurs
	 */
	protected void fetchCompleted(String sql,String uuids, String startDate, String endDate) throws Exception {
		// establish the connection
	      ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;
	      try {  
	   // initialize
	      st = con.prepareStatement(sql);
	      int parameterIdx =1;
	        String[] uuidArr = null;
			if(uuids.length() > 0 && uuids.indexOf(",") > -1){
				uuidArr = uuids.split(",");
				if(uuidArr.length <= 10){										
					for (int i =0; i< uuidArr.length ; i++){
						st.setString(parameterIdx, UuidUtil.addCurlies(uuidArr[i]));
						parameterIdx++;
					}
				}
			}else if(uuids.length() > 0){
				st.setString(parameterIdx,UuidUtil.addCurlies(uuids));
				parameterIdx++;
			}
	      
			 if(startDate.length() > 0){
				  st.setDate(parameterIdx, Date.valueOf(startDate)) ;
				  parameterIdx++;
			  }
			  if(endDate.length() > 0){
				  st.setDate(parameterIdx, Date.valueOf(endDate)) ;
				  parameterIdx++;
			  }
		  String[] columnTags = {
				  "UUID","HARVEST_ID" ,"HARVEST_DATE", "INPUT_DATE","JOB_TYPE","SERVICE_ID"
				  };
		  rs = st.executeQuery();
		  statWriter.writeResultSet(getHarvestingJobsCompletedTableName(), rs,columnTags);  
	      
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);
	     
	    }
	}
	
	/**
	 * Fetches published document count grouped by protocol type 
	 * @param protocolMap the protocol type object map
	 * @param docuuidMap the docuuid map of sites on schedule
	 * @throws SQLException if sql exception occurs
	 */
	protected void fetchDocumentCountByProtocol(HashMap<String, Object> protocolMap,HashMap<String,String> docuuidMap) throws SQLException {
		 ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;	     
	    try { 
	    	ArrayList<String> sortedKeys=new ArrayList<String>(docuuidMap.keySet());
			Collections.sort(sortedKeys);
			for(int i=0; i <sortedKeys.size(); i++){
			  String docuuid = sortedKeys.get(i);
		      String protocolType = docuuidMap.get(docuuid);
		      st = con.prepareStatement(createDocumentCount());	 
		      st.setString(1, docuuid);
		      rs = st.executeQuery();
			  while(rs.next()){
				  int count = Val.chkInt(rs.getString("pc"),0);
				  ProtocolInfo pi = (ProtocolInfo) protocolMap.get(protocolType);
				  pi.setDocumentCount(pi.getDocumentCount() + count); 			  
			  }
			  BaseDao.closeResultSet(rs);
		      BaseDao.closeStatement(st);
			}
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);	     
	    }
	}
	
	/**
	 * Fetches harvesting history information
	 * @param sql the sql query to execute
	 * @param uuids the uuids constraint
	 * @param startDate the start date constraint
	 * @param endDate the end date constraint
	 * @throws Exception if exception occurs
	 */
	protected void fetchHistory(String sql,String uuids, String startDate, String endDate) throws Exception{
		
		// establish the connection
	      ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;
	      try {  
	   // initialize
	      st = con.prepareStatement(sql);
	        int parameterIdx =1;
	        String[] uuidArr = null;
			if(uuids.length() > 0 && uuids.indexOf(",") > -1){
				uuidArr = uuids.split(",");
				if(uuidArr.length <= 10){										
					for (int i =0; i< uuidArr.length ; i++){
						st.setString(parameterIdx, UuidUtil.addCurlies(uuidArr[i]));
						parameterIdx++;
					}
				}
			}else if(uuids.length() > 0){
				st.setString(parameterIdx,UuidUtil.addCurlies(uuids));
				parameterIdx++;
			}
	      
			 if(startDate.length() > 0){
				  st.setDate(parameterIdx, Date.valueOf(startDate)) ;
				  parameterIdx++;
			  }
			  if(endDate.length() > 0){
				  st.setDate(parameterIdx, Date.valueOf(endDate)) ;   
				  parameterIdx++;
			  }
			
		  String[] columnTags = {
				  "UUID" ,
				  "HARVEST_ID" ,
				  "HARVEST_DATE" ,
				  "HARVESTED_COUNT",
				  "VALIDATED_COUNT" ,
				  "PUBLISHED_COUNT",
				  };
		  rs = st.executeQuery();
		  statWriter.writeResultSet(getHarvestingHistoryTableName(), rs,columnTags);  
	      
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);
	     
	    }
	}
	
	/**
	 * Fetches information from pending table.
	 * @param sql the sql query to execute
	 * @param uuids the uuids constraint
	 * @param startDate the start date constraint
	 * @param endDate the end date constraint
	 * @throws Exception if exception occurs
	 */
	protected void fetchPending(String sql,String uuids, String startDate, String endDate) throws Exception {
		// establish the connection
	      ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;
	      try {  
	   // initialize
	      st = con.prepareStatement(sql);	     
		  String[] columnTags = {
				  "UUID","HARVEST_ID" ,"HARVEST_DATE", "INPUT_DATE","JOB_TYPE","JOB_STATUS","CRITERIA","SERVICE_ID"
				  };
		  
		  int parameterIdx =1;
	        String[] uuidArr = null;
			if(uuids.length() > 0 && uuids.indexOf(",") > -1){
				uuidArr = uuids.split(",");
				if(uuidArr.length <= 10){										
					for (int i =0; i< uuidArr.length ; i++){
						st.setString(parameterIdx, UuidUtil.addCurlies(uuidArr[i]));
						parameterIdx++;
					}
				}
			}else if(uuids.length() > 0){
				st.setString(parameterIdx,UuidUtil.addCurlies(uuids));
				parameterIdx++;
			}
	      
			 if(startDate.length() > 0){
				  st.setDate(parameterIdx, Date.valueOf(startDate)) ;
				  parameterIdx++;
			  }
			  if(endDate.length() > 0){
				  st.setDate(parameterIdx, Date.valueOf(endDate)) ;
				  parameterIdx++;
			  }
		  
		  rs = st.executeQuery();
		  statWriter.writeResultSet(getHarvestingJobTableName(), rs,columnTags);  
	      
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);
	     
	    }
	}
			
	/**
	 * This method is used to fetch harvest counts for given number of days from current date
	 * @param timePeriod the number days to aggregate for
	 * @return the counts array (harvestedCount,publishedCount,validatedCount)
	 * @throws Exception if exception occurs
	 */
	protected int[] fetchHarvestCounts(int timePeriod) throws Exception {
		int harvestedCount = 0;
		int publishedCount = 0;
		int validatedCount = 0;
		 ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;	  
	      int[] counts = new int[3];	      
	    try {  
	      st = con.prepareStatement(createHistoryCountsSQL(timePeriod));
	      if(timePeriod != -1){
	    	  st.setTimestamp(1, DateProxy.subtractDays(timePeriod));
	      }
	      rs = st.executeQuery();
		  while(rs.next()){
			  harvestedCount = rs.getInt("hc");
			  publishedCount = rs.getInt("pc");
			  validatedCount = rs.getInt("vc");
		  }
		  
		  counts[0] = harvestedCount;
	      counts[1] = publishedCount;
	      counts[2] = validatedCount;
	     
		  
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);	     
	    }
	    
	    return counts;
	}
	
	/**
	 * Fetches Summary information for web harvester
	 * @return the active and submitted count for web harvest jobs from pending table
	 * @throws Exception if exception occurs
	 */
	protected int[] fetchSummary() throws Exception {
		 ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;	     
	      int activeCnt = 0;
	      int submittedCnt = 0;
	      int[] counts = new int[2];
	      counts[0] = activeCnt;
	      counts[1] = submittedCnt;
	    try {  
	   // initialize
	      st = con.prepareStatement(createCountPendingSQLByStatus());	
	      rs = st.executeQuery();	      
		  while(rs.next()){
			 String status = Val.chkStr(rs.getString("job_status"));
			 
			 if(status.equalsIgnoreCase("submited")){
				submittedCnt = rs.getInt("cnt");				
			 }else if(status.equalsIgnoreCase("Running")){
				 activeCnt = rs.getInt("cnt");
			 }
		  }
		  		  	      
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);	     
	    }
	    
	    return counts;
	}
	
	/**
	 * Fetches repository summary information by protocol
	 * @return map of protocol info objects
	 * @throws Exception if exception occurs
	 */
	protected HashMap<String, Object> fetchRepositoriesSummaryByProtocol() throws Exception {
		HashMap<String, Object> protocolMap = new HashMap<String,Object>();
		ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;	     
	    try {  
	      st = con.prepareStatement(selectDistinctProtocols());	    
	      rs = st.executeQuery();
		  while(rs.next()){
			  String protocolType = rs.getString("protocol_type");
			  protocolMap.put(protocolType, new ProtocolInfo());
		  }		  
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);	     
	    }
	    return protocolMap;
	}
	
	/**
	 * Gets the harvesting table name.
	 * @return the harvesting table name
	 */
	protected String getHarvestingTableName() {
	  return getRequestContext().getCatalogConfiguration().getResourceTableName();
	}

	/**
	 * Gets the harvesting table name.
	 * @return the harvesting table name
	 */
	protected String getHarvestingDataTableName() {
	  return getRequestContext().getCatalogConfiguration().getResourceDataTableName();
	}

	/**
	 * Gets harvesting history table name.
	 * @return the harvesting history table name
	 */
	protected String getHarvestingHistoryTableName() {
	  return getRequestContext().getCatalogConfiguration().
	           getHarvestingHistoryTableName();
	}

	/**
	 * Gets harvesting job table name.
	 * @return the harvesting job table name
	 */
	protected String getHarvestingJobTableName() {
	  return getRequestContext().getCatalogConfiguration().
	           getHarvestingJobsPendingTableName();
	}

	/**
	 * Gets completed harvesting jobs table name.
	 * @return completed harvesting jobs table name
	 */
	protected String getHarvestingJobsCompletedTableName() {
	  return getRequestContext().getCatalogConfiguration().
	    getHarvestingJobsCompletedTableName();
	}
	
	/**
	 * Gets completed harvesting jobs table name.
	 * @return completed harvesting jobs table name
	 */
	protected String getResourceTableName() {
	  return getRequestContext().getCatalogConfiguration().
			  getResourceTableName();
	}
	
	
	/**
	 * Populates protocol info objects map using sql.
	 * @param protocolMap the protocol map object
	 * @param sql the sql string
	 * @param propertyName the property name of protocol info object 
	 * to populate count using sql
	 * @throws SQLException if sql exception occurs
	 */
	protected void populateProtocolInfo(HashMap<String, Object> protocolMap,String sql, String propertyName) throws SQLException{
		 ManagedConnection mc = returnConnection();
	      Connection con = mc.getJdbcConnection();
	      ResultSet rs = null;
	      PreparedStatement st = null;	     
	    try {  
	      st = con.prepareStatement(sql);	    
	      rs = st.executeQuery();
		  while(rs.next()){
			 int count = rs.getInt("cnt");
			 String protocolType = Val.chkStr(rs.getString("protocol_type"));
			 ProtocolInfo pi = (ProtocolInfo) protocolMap.get(protocolType);
			 if(propertyName.equalsIgnoreCase("Approved")){
				 pi.setApprovedCount(count);
			 }else if(propertyName.equalsIgnoreCase("OnSchedule")){
				 pi.setOnScheduleCount(count);
			 }else if(propertyName.equalsIgnoreCase("Registered")){
				 pi.setRegisteredCount(count);
			 } 
		  }
	    } finally {
	      BaseDao.closeResultSet(rs);
	      BaseDao.closeStatement(st);
	      getRequestContext().getConnectionBroker().closeConnection(mc);	     
	    }
	}
			
	/**
	 * Create sql to select distinct registered site protocols
	 * @return the sql string
	 */
	protected String selectDistinctProtocols(){
		StringBuilder sbSelectSql = new StringBuilder();
		sbSelectSql.append(" select distinct protocol_type from ").append(getResourceTableName()).append(" where protocol_type is not null");
		return sbSelectSql.toString();
	}
}
