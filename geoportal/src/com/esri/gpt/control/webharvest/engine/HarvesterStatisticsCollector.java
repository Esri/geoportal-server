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
package com.esri.gpt.control.webharvest.engine;

import java.util.Iterator;
import java.util.Set;

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.catalog.search.ISearchFilterSpatialObj.OptionsBounds;
import com.esri.gpt.catalog.search.SearchEngineCSW.AimsContentTypes;
import com.esri.gpt.control.rest.writer.ResponseWriter;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.TimePeriod;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.usage.api.IStatisticsWriter;

/**
 * Harvest statistic collector.
 */
public class HarvesterStatisticsCollector {
	
	// class variables =============================================================

	// instance variables ==========================================================
	/** watch-dog */
	  private WatchDog watchDog;
	/** message broker */
	  private MessageBroker messageBroker;
	 /** task queue */
	  private TaskQueue taskQueue;
	  /** pool of threads */
	  private Pool pool;
	  /** Response writer */
	  private ResponseWriter writer = null;
	  /** Statistics writer */
	  private IStatisticsWriter statWriter = null;
	  
	// constructors ================================================================
	  /**
	   * Parameterized constructor to collect web harvester engine statistics
	   * @param pool the web harvester pool
	   * @param watchDog the web harvester watch dog
	   * @param taskQueue the task queue of web harvester
	   * @param messageBroker the message broker
	   */
	  public HarvesterStatisticsCollector(Pool pool,WatchDog watchDog,
			  TaskQueue taskQueue,MessageBroker messageBroker){
		  this.messageBroker = messageBroker;
		  this.pool = pool;
		  this.watchDog = watchDog;
		  this.taskQueue = taskQueue;
	  }
	  
	// methods ================================================================
	 /**
	   * Adding spaces for pretty print.
	   * @param writer the response writer
	   * @param sb the string builder
	   * @param depth the tab space depth
	   */
	  private void addSpaces(StringBuilder sb,int depth){
			sb.append(writer.getNewline()).append(writer.makeTabs(depth));
		}
	  
	  /**
	   * Collects execution unit statistics
	   * @param eu the execution unit object
	   * @param sb the string builder
	   * @param isLastElement true if is last element of array
	   * @throws Exception if exception occurs
	   */
	  private void collectionStatsOnExecutionUnit(ExecutionUnit eu,StringBuilder sb,boolean isLastElement) throws Exception{		
			addSpaces(sb,4);
			sb.append("{");
			addSpaces(sb,5);
			ExecutionUnitHelper helper = new ExecutionUnitHelper(eu);
		    ReportBuilder rb = helper.getReportBuilder();
		    sb.append("\"stats\": {");
		    addSpaces(sb,6);
		    statWriter.writeElement("performance",String.valueOf(rb.getPerformance()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("publishedCount",String.valueOf(rb.getPublishedCount()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("harvestedCount",String.valueOf(rb.getHarvestedCount()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("modifiedCount",String.valueOf(rb.getModifiedCount()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("validatedCount",String.valueOf(rb.getValidatedCount()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("addedCount",String.valueOf(rb.getAddedCount()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("startTime",String.valueOf(rb.getStartTime()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("endTime",String.valueOf(rb.getEndTime()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("duration",new TimePeriod(rb.getDuration()).toLocalizedString(messageBroker),false,false);
		    addSpaces(sb,5);
		    sb.append("},");
		    addSpaces(sb,5);
		    Publisher publisher = eu.getPublisher();
		    sb.append("\"publisher\":{");
		    addSpaces(sb,6);
		    statWriter.writeElement("name",String.valueOf(publisher.getName()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("hasAdministratorRole",String.valueOf(publisher.getIsAdministrator()),false,false);
		    addSpaces(sb,5);
		    sb.append("},");
		    addSpaces(sb,5);
		    HrRecord hrecord = eu.getRepository();
		    sb.append("\"hrRecord\": {");
			addSpaces(sb,6);
			statWriter.writeElement("findable",String.valueOf(hrecord.getFindable()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("isHarvestDue",String.valueOf(hrecord.getIsHarvestDue()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("isSelected",String.valueOf(hrecord.getIsSelected()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("localId",String.valueOf(hrecord.getLocalId()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("ownerId",String.valueOf(hrecord.getOwnerId()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("searchable",String.valueOf(hrecord.getSearchable()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("searchRequiresLogin",String.valueOf(hrecord.getSearchRequiresLogin()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("sendNotification",String.valueOf(hrecord.getSendNotification()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("synchronizable",String.valueOf(hrecord.getSynchronizable()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("approvalStatus",String.valueOf(hrecord.getApprovalStatus()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("hostUrl",String.valueOf(hrecord.getHostUrl()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("inputDate",String.valueOf(hrecord.getInputDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("lastHarvestDate",String.valueOf(hrecord.getLastHarvestDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("lastSyncDate",String.valueOf(hrecord.getLastSyncDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("nextHarvestDate",String.valueOf(hrecord.getNextHarvestDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("updateDate",String.valueOf(hrecord.getUpdateDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("uuid",String.valueOf(hrecord.getUuid()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("recentJobStatus",String.valueOf(hrecord.getRecentJobStatus()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("name",String.valueOf(hrecord.getName()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("harvestFrequency",String.valueOf(hrecord.getHarvestFrequency()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("protocolType",String.valueOf(hrecord.getProtocol().getKind()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("protocol",String.valueOf(hrecord.getProtocol()),false,false);
			addSpaces(sb,5);
			sb.append("},");
		    addSpaces(sb,5);
		    sb.append("\"criteria\": {");
		    addSpaces(sb,6);	    
		    Criteria criteria = eu.getCriteria();
		    Envelope envelope = criteria.getBBox();
		    OptionsBounds options = criteria.getBBoxOption();
		    AimsContentTypes ct = criteria.getContentType();
		    statWriter.writeElement("fromDate",String.valueOf(criteria.getFromDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("toDate",String.valueOf(criteria.getToDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("searchText",String.valueOf(criteria.getSearchText()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("maxRecords",String.valueOf(criteria.getMaxRecords()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("sortOption",String.valueOf(criteria.getSortOption()),false,false);
		    addSpaces(sb,5);
		    String[] dc = criteria.getDataCategory();
		    sb.append("},");
		    addSpaces(sb,5);	    
		    Query qry = eu.getQuery();
		//    JSONObject qjso = new JSONObject();
		//    qjso.put("criteria", qry.toString());
		   // sb.append("\"query\":" +  Val.escapeStrForJson(qry.toString()) + ",");
		   // addSpaces(sb,4);
		    Set<String> restrictions = eu.getRestrictions();
		    Iterator<String> iter = restrictions.iterator();
		    sb.append("\"restrictions\":[");
		    boolean first = true;
		    while (iter.hasNext()){
		    	if(!first) sb.append(",");
		    	String value = Val.chkStr(iter.next());
		    	if(value.length() > 0){
			    	addSpaces(sb,6);
			    	statWriter.writeElement("restriction",Val.escapeStrForJson(value),false,false);
			    	first = false;
		    	}
		    }
		    addSpaces(sb,5);
		    sb.append("]");
		    addSpaces(sb,4);
		    if(isLastElement){
		    	sb.append("}");
		    }else{
		    	sb.append("},");
		    } 	
	  }
	  
	  /**
	   * Collects task queue statistics
	   * @param task the task object
	   * @param sb the string builder
	   * @param isLastElement true if is last element of array
	   * @throws Exception if exception occurs
	   */
	  private void collectTaskQueueStats(Task task,StringBuilder sb,boolean isLastElement) throws Exception{
		  sb.append("{");
			addSpaces(sb,5);
			Criteria criteria = task.getCriteria();	
			statWriter.writeElement("criteria",String.valueOf(criteria.toString()),true,false);
			addSpaces(sb,5);
			sb.append("\"hrRecord\": {");
			HrRecord record = task.getResource();
			addSpaces(sb,6);
			statWriter.writeElement("findable",String.valueOf(record.getFindable()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("isHarvestDue",String.valueOf(record.getIsHarvestDue()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("isSelected",String.valueOf(record.getIsSelected()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("localId",String.valueOf(record.getLocalId()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("ownerId",String.valueOf(record.getOwnerId()),true,true);
		    addSpaces(sb,6);
		    statWriter.writeElement("searchable",String.valueOf(record.getSearchable()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("searchRequiresLogin",String.valueOf(record.getSearchRequiresLogin()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("sendNotification",String.valueOf(record.getSendNotification()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("synchronizable",String.valueOf(record.getSynchronizable()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("approvalStatus",String.valueOf(record.getApprovalStatus()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("hostUrl",String.valueOf(record.getHostUrl()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("inputDate",String.valueOf(record.getInputDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("lastHarvestDate",String.valueOf(record.getLastHarvestDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("lastSyncDate",String.valueOf(record.getLastSyncDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("nextHarvestDate",String.valueOf(record.getNextHarvestDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("updateDate",String.valueOf(record.getUpdateDate()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("uuid",String.valueOf(record.getUuid()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("recentJobStatus",String.valueOf(record.getRecentJobStatus()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("name",String.valueOf(record.getName()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("harvestFrequency",String.valueOf(record.getHarvestFrequency()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("protocolType",String.valueOf(record.getProtocol().getKind()),true,false);
		    addSpaces(sb,6);
		    statWriter.writeElement("protocol",String.valueOf(record.getProtocol()),false,false);
			addSpaces(sb,5);
			sb.append("}");		
			addSpaces(sb,4);
			if(isLastElement){
		    	sb.append("}");
		    }else{
		    	sb.append("},");
		    }			
	  }

	  /**
	   * Writes harvester engine statistics
	   * @param writer the response writer
	   * @param sb the response string builder
	   * @throws Exception if exception occurs
	   */
	  public void writeStatistics(ResponseWriter writer,StringBuilder sb) throws Exception {
		this.writer = writer;
		statWriter = (IStatisticsWriter) writer;
		int cnt = 0;
		int poolSize = this.pool.size();
		statWriter.writeElement("poolSize",String.valueOf(poolSize),true,true);
		addSpaces(sb,3);
		ExecutionUnit[] executedUnits = this.pool.getAllExecutedUnits();
		statWriter.writeElement("numberOfExecutionUnits",String.valueOf(executedUnits.length),true,true);
		addSpaces(sb,3);
		sb.append("\"executionUnits\": [");
		boolean isLast = false;
		for (ExecutionUnit eu : executedUnits){
			if(cnt == executedUnits.length-1){
				isLast = true;
		    }
		    cnt++;	    	
			collectionStatsOnExecutionUnit(eu,sb,isLast);
		}
		addSpaces(sb,3);
		sb.append("],");
		addSpaces(sb,3);

		RequestContext context = RequestContext.extract(null);
		Task[] tasksInQueue = this.taskQueue.all(context);
		statWriter.writeElement("numberOfTasksInQueue",String.valueOf(tasksInQueue.length),true,true);
		addSpaces(sb,3);
		sb.append("\"tasksInQueue\": [");
		addSpaces(sb,4);
		cnt=0;
		isLast = false;
		for(Task task : tasksInQueue){	
			if(cnt == tasksInQueue.length-1){
				isLast = true;
		    }
			cnt++;
			collectTaskQueueStats(task,sb,isLast);
		}
		addSpaces(sb,3);
		sb.append("],");
		addSpaces(sb,3);
		
		String[] currentlyHarvestedResourceUuids = this.watchDog.getCurrentlyHarvesterResourceUuids();
		sb.append("\"currentlyHarvestedResourceUuids\": [");	
		cnt=0;
		for (String currentlyHarvestedResourceUuid : currentlyHarvestedResourceUuids){
			addSpaces(sb,4);
			if(cnt == tasksInQueue.length-1){
				sb.append("{");
				statWriter.writeElement("currentlyHarvestedResourceUuid",Val.escapeStrForJson(UuidUtil.removeCurlies(currentlyHarvestedResourceUuid)),false,false);
				sb.append("}");
		    }else{
		    	sb.append("{");
		    	statWriter.writeElement("currentlyHarvestedResourceUuid",Val.escapeStrForJson(UuidUtil.removeCurlies(currentlyHarvestedResourceUuid)),false,false);
		    	sb.append("},");
		    }
			cnt++;
		}
		addSpaces(sb,3);
		sb.append("]");
		addSpaces(sb,2);	
	  }

}
