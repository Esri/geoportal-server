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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import com.esri.gpt.control.rest.writer.ResponseWriter;
import com.esri.gpt.control.webharvest.engine.HarvesterConfiguration;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.usage.api.IStatisticsBuilder;
import com.esri.gpt.server.usage.api.IStatisticsWriter;
import com.esri.gpt.server.usage.api.StatisticsDimensions;
import com.esri.gpt.server.usage.api.StatisticsMetrics;
import com.esri.gpt.server.usage.api.StatisticsRequestContext;

/**
 * This class is used to build harvester statistics.
 * 
 * @author prat5814
 * 
 */
public class HarvesterStatisticsBuilder implements IStatisticsBuilder {

	// class variables =============================================================

	// instance variables ==========================================================
	private StatisticsRequestContext statRequestCtx;
	private ResponseWriter writer;
	private IStatisticsWriter statWriter;
	private HarvesterStatisticsDao harvesterStatDao;
	private StringBuilder sb;

	// constructors ================================================================
	public HarvesterStatisticsBuilder(StatisticsRequestContext statRequestCtx) {
		this.statRequestCtx = statRequestCtx;
		this.writer = statRequestCtx.getWriter();
		this.statWriter = (IStatisticsWriter) writer;
		sb = statRequestCtx.getResponseString();
		harvesterStatDao = new HarvesterStatisticsDao(writer);
		harvesterStatDao.setRequestContext(getRequestContext());
	}

	// properties ==================================================================
	/**
	 * Gets the associated request context.
	 * 
	 * @return the request context
	 */
	private RequestContext getRequestContext() {
		return statRequestCtx.getRequestContext();
	}

	// methods ==================================================================
	/**
	 * Adds spaces to format response.
	 * 
	 * @param depth
	 *            number of tab spaces
	 */
	private void addSpaces(int depth) {
		if (depth == 0) {
			sb.append(writer.getNewline());
		} else {
			sb.append(writer.getNewline()).append(writer.makeTabs(depth));
		}
	}

	/**
	 * Build the configuration statistics for web harvester.
	 */
	@Override
	public void buildEnvironmentStatistics() throws Exception {
		try {
			HttpServletResponse response = statRequestCtx.getResponse();
			writer.begin(response);
			collectEnvironmentInfo();
		} finally {
			writer.close();
		}
	}

	/**
	 * This method is used to build statistics for web harvester
	 */
	@Override
	public void buildStatistics() throws Exception {
		try {
			String[] restUriParts = statRequestCtx.getRestUriParts();
			HttpServletResponse response = statRequestCtx.getResponse();
			StringAttributeMap params = statRequestCtx.getStatQueryParams();
			writer.begin(response);
			sb.append("{");
			addSpaces(1);
			sb.append("\"harvester\": {");
			addSpaces(2);
			String metrics = Val.chkStr(params.getValue("metrics"));
			String dimensions = Val.chkStr(params.getValue("dimensions"));
			String uuids = Val.chkStr(params.getValue("uuids"));
			String startDate = Val.chkStr(params.getValue("start-date"));
			String endDate = Val.chkStr(params.getValue("end-date"));
			boolean hasDatabaseInfo = false;
			boolean hasConfigInfo = false;
			if (metrics.contains(StatisticsMetrics.CONFIG.toString()
					.toLowerCase())) {
				collectEnvironmentInfo();
				hasConfigInfo = true;
			}
			if (restUriParts.length >= 6
					&& restUriParts[5]
							.equalsIgnoreCase(StatisticsDimensions.SUMMARY
									.toString())) {
				sb.append("\"summary\": {");
				addSpaces(3);
				buildSummary();
				addSpaces(2);
				sb.append(" }, ");
				addSpaces(2);
				sb.append("\"last24Hours\": {");
				buildTimeBoundSummary(1);
				addSpaces(2);
				sb.append(" }, ");
				addSpaces(2);
				sb.append("\"last7Days\": {");
				buildTimeBoundSummary(7);
				addSpaces(2);
				sb.append(" },");
				addSpaces(2);
				sb.append("\"allTime\": {");
				buildTimeBoundSummary(-1);
				addSpaces(2);
				sb.append(" },");
				addSpaces(2);
				sb.append("\"numberOfHarvestingSites\": {");
				buildRepositoriesSummary();
				addSpaces(2);
				sb.append("},");
				addSpaces(2);
				sb.append("\"numberOfHarvestingSitesByProtocol\": {");
				buildRepositoriesSummaryByProtocol();
				addSpaces(2);
				sb.append("}");
				addSpaces(1);
			} else if (restUriParts.length >= 6
					&& restUriParts[5]
							.equalsIgnoreCase(StatisticsDimensions.PENDING
									.toString())) {
				if(hasConfigInfo){
					sb.append(",");
					addSpaces(2);
				}
				sb.append("\"databaseInfo\": {");
				addSpaces(3);
				String sql = harvesterStatDao.createSelectPendingSQL(uuids,
						startDate, endDate);
				harvesterStatDao.fetchPending(sql, uuids, startDate, endDate);
				addSpaces(2);
				sb.append("}");
				addSpaces(1);
			} else if (restUriParts.length >= 6
					&& restUriParts[5]
							.equalsIgnoreCase(StatisticsDimensions.COMPLETED
									.toString())) {
				if(hasConfigInfo){
					sb.append(",");
					addSpaces(2);
				}
				sb.append("\"databaseInfo\": {");
				addSpaces(3);
				String sql = harvesterStatDao.createSelectCompletedSQL(uuids,
						startDate, endDate);
				harvesterStatDao.fetchCompleted(sql, uuids, startDate, endDate);
				addSpaces(2);
				sb.append("}");
				addSpaces(1);
			} else if (restUriParts.length >= 6
					&& restUriParts[5]
							.equalsIgnoreCase(StatisticsDimensions.HISTORY
									.toString())) {
				if(hasConfigInfo){
					sb.append(",");
					addSpaces(2);
				}
				sb.append("\"databaseInfo\": {");
				addSpaces(3);
				String sql = harvesterStatDao.createSelectHistorySQL(uuids,
						startDate, endDate);
				harvesterStatDao.fetchHistory(sql, uuids, startDate, endDate);
				addSpaces(2);
				sb.append("}");
				addSpaces(1);
			} else {				
				if (metrics.contains(StatisticsMetrics.DATABASE.toString()
						.toLowerCase())) {
					if(hasConfigInfo){
						sb.append(",");
						addSpaces(2);
					}
					sb.append("\"databaseInfo\": {");
					addSpaces(3);
					if (dimensions.contains(StatisticsDimensions.PENDING
							.toString().toLowerCase())) {
						String sql = harvesterStatDao.createSelectPendingSQL(
								uuids, startDate, endDate);
						harvesterStatDao.fetchPending(sql, uuids, startDate,
								endDate);
						sb.append(",");
						addSpaces(3);
					}
					if (dimensions.contains(StatisticsDimensions.COMPLETED
							.toString().toLowerCase())) {
						String sql = harvesterStatDao.createSelectCompletedSQL(
								uuids, startDate, endDate);
						harvesterStatDao.fetchCompleted(sql, uuids, startDate,
								endDate);
						sb.append(",");
						addSpaces(3);
					}
					if (dimensions.contains(StatisticsDimensions.HISTORY
							.toString().toLowerCase())) {
						String sql = harvesterStatDao.createSelectHistorySQL(
								uuids, startDate, endDate);
						harvesterStatDao.fetchHistory(sql, uuids, startDate,
								endDate);
						addSpaces(2);
					}
					sb.append("}");
					hasDatabaseInfo = true;
					
				}
			}
			if (metrics.contains(StatisticsMetrics.ENGINE.toString()
					.toLowerCase())) {
				ApplicationContext appCtx = ApplicationContext
						.getInstance();
				if(hasDatabaseInfo || hasConfigInfo){
					sb.append(",");
					addSpaces(1);
				}
				sb.append("\"engineInfo\": {");
				addSpaces(3);
				appCtx.getHarvestingEngine().writeStatistics(writer, sb);
				sb.append("}");
				addSpaces(2);
			}
			sb.append("}").append(writer.getNewline());
			sb.append("}").append(writer.getNewline());
			writer.write(sb.toString());
		} finally {
			writer.close();
		}
	}

	/**
	 * This is method is used to build repository summary for web harvester.
	 * 
	 * @throws Exception
	 *             if exception occurs.
	 */
	private void buildRepositoriesSummary() throws Exception {
		addSpaces(3);
		makeElement("registered", String.valueOf(harvesterStatDao
				.fetchCountByTime(
						harvesterStatDao.createCountOfRegisteredSites(), -1)),
				true, true);
		addSpaces(3);
		makeElement("approved", String.valueOf(harvesterStatDao
				.fetchCountByTime(
						harvesterStatDao.createCountOfApprovedSites(), -1)),
				true, true);
		addSpaces(3);
		makeElement(
				"onSchedule",
				String.valueOf(harvesterStatDao.fetchCountByTime(
						harvesterStatDao.createCountOfApprovedSitesOnSchedule(),
						-1)), false, true);
	}

	/**
	 * This method is used build repository summary by protocol types.
	 * 
	 * @throws Exception
	 *             if exception occurs
	 */
	private void buildRepositoriesSummaryByProtocol() throws Exception {
		HashMap<String, Object> protocolMap = harvesterStatDao
				.fetchRepositoriesSummaryByProtocol();
		harvesterStatDao.populateProtocolInfo(protocolMap,
				harvesterStatDao.createCountOfRegisteredSitesByProtocol(),
				"Registered");
		harvesterStatDao.populateProtocolInfo(protocolMap,
				harvesterStatDao.createCountOfApprovedSitesByProtocol(),
				"Approved");
		harvesterStatDao
				.populateProtocolInfo(protocolMap, harvesterStatDao
						.createCountOfApprovedSitesOnScheduleByProtocol(),
						"OnSchedule");
		HashMap<String, String> docuuidMap = harvesterStatDao
				.collectSitesByProtocolType(protocolMap);
		if (docuuidMap.size() > 0) {
			harvesterStatDao.fetchDocumentCountByProtocol(protocolMap,
					docuuidMap);
		}
		serializeProtocolInfoMapToJson(protocolMap);
	}

	/**
	 * Build time bound summary of web harvester jobs
	 * 
	 * @param timePeriod
	 *            the time period as number of days from current date
	 * @throws Exception
	 *             if exception occurs
	 */
	private void buildTimeBoundSummary(int timePeriod) throws Exception {
		int completedCount = harvesterStatDao.fetchCountByTime(
				harvesterStatDao.createCountCompletedSQL(timePeriod),
				timePeriod);
		int queuedCount = harvesterStatDao.fetchCountByTime(
				harvesterStatDao.createCountPendingSQL(timePeriod), timePeriod);
		addSpaces(3);
		makeElement("numberOfJobsCompleted", String.valueOf(completedCount),
				true, true);
		addSpaces(3);
		makeElement("numberOfJobsQueued",
				String.valueOf(completedCount + queuedCount), true, true);
		addSpaces(3);
		sb.append("\"numberOfDocumentsHarvested\": {");
		addSpaces(4);
		populateHarvestCounts(timePeriod);
		addSpaces(3);
		sb.append("}");
	}

	/**
	 * Builds web harvester usage summary
	 * 
	 * @throws Exception
	 *             if exception occurs
	 */
	private void buildSummary() throws Exception {
		int[] counts = harvesterStatDao.fetchSummary();
		int activeCnt = counts[0];
		int submittedCnt = counts[1];

		makeElement("numberOfActiveJobs", String.valueOf(activeCnt), true, true);
		addSpaces(3);
		makeElement("numberOfRecordsInPendingTable",
				String.valueOf(submittedCnt + activeCnt), false, true);
	}

	/**
	 * Collects web harvester configuration information.
	 * 
	 * @throws Exception
	 *             if exception occurs.
	 */
	private void collectEnvironmentInfo() throws Exception {
		HarvesterConfiguration cfg = getRequestContext()
				.getApplicationConfiguration().getHarvesterConfiguration();
		sb.append("\"configuration\":{");
		addSpaces(3);
		makeElement("active", String.valueOf(cfg.getActive()), true, false);
		addSpaces(3);
		makeElement("queueEnabled", String.valueOf(cfg.getQueueEnabled()),
				true, false);
		addSpaces(3);
		makeElement("poolSize", String.valueOf(cfg.getPoolSize()), true, true);
		addSpaces(3);
		makeElement("autoSelectFrequency",
				String.valueOf(cfg.getAutoSelectFrequency()), true, true);
		addSpaces(3);
		makeElement("watchDogFrequency",
				String.valueOf(cfg.getWatchDogFrequency()), true, true);
		addSpaces(3);
		makeElement("baseContextPath",
				String.valueOf(cfg.getBaseContextPath()), true, false);
		addSpaces(3);
		makeElement("maxRepRecords", String.valueOf(cfg.getMaxRepRecords()),
				true, true);
		addSpaces(3);
		makeElement("maxRepErrors", String.valueOf(cfg.getMaxRepErrors()),
				true, true);
		addSpaces(3);
		makeElement("resourceAutoApprove",
				String.valueOf(cfg.getResourceAutoApprove()), false, false);
		addSpaces(2);
		sb.append("}");
		
	}

	/**
	 * Makes element to write in response.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value
	 * @param hasMore
	 *            true if has more elements in an JSON array
	 * @param isNumber
	 *            true if value is number.
	 * @throws Exception
	 *             if exception occurs
	 */
	private void makeElement(String key, String value, boolean hasMore,
			boolean isNumber) throws Exception {
		statWriter.writeElement(key, String.valueOf(value), hasMore, isNumber);
	}

	/**
	 * Aggregates harvest counts by query database tables
	 * 
	 * @param timePeriod
	 *            the time period as number of days from current date
	 * @throws Exception
	 *             if exception occurs
	 */
	private void populateHarvestCounts(int timePeriod) throws Exception {
		int[] counts = harvesterStatDao.fetchHarvestCounts(timePeriod);
		int harvestedCount = counts[0];
		int publishedCount = counts[1];
		int validatedCount = counts[2];

		makeElement("harvestedCount", String.valueOf(harvestedCount), true,
				true);
		addSpaces(4);
		makeElement("validatedCount", String.valueOf(validatedCount), true,
				true);
		addSpaces(4);
		makeElement("publishedCount", String.valueOf(publishedCount), false,
				true);
	}

	/**
	 * This method is used to serialize protocol info
	 * 
	 * @param protocolMap
	 *            the map containing protocol info objects
	 * @throws Exception
	 *             if exception occurs
	 */
	private void serializeProtocolInfoMapToJson(
			HashMap<String, Object> protocolMap) throws Exception {
		ArrayList<String> sortedKeys = new ArrayList<String>(
				protocolMap.keySet());
		Collections.sort(sortedKeys);
		boolean firstElement = true;
		addSpaces(3);
		for (int i = 0; i < sortedKeys.size(); i++) {
			String protocolType = sortedKeys.get(i);
			ProtocolInfo pi = (ProtocolInfo) protocolMap.get(protocolType);
			if (!firstElement) {
				sb.append(",");
				addSpaces(3);
			}
			sb.append("\"" + protocolType + "\": {");
			addSpaces(4);
			makeElement("registered", String.valueOf(pi.getRegisteredCount()),
					true, true);
			addSpaces(4);
			makeElement("approved", String.valueOf(pi.getApprovedCount()),
					true, true);
			addSpaces(4);
			makeElement("onSchedule", String.valueOf(pi.getOnScheduleCount()),
					true, true);
			addSpaces(4);
			makeElement("documents", String.valueOf(pi.getDocumentCount()),
					false, true);
			addSpaces(3);
			sb.append("}");
			firstElement = false;
		}
	}

}
