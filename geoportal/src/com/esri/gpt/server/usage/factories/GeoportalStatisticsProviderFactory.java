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
package com.esri.gpt.server.usage.factories;

import com.esri.gpt.server.usage.api.GeoportalUsageInformation;
import com.esri.gpt.server.usage.api.StatisticsMetrics;
import com.esri.gpt.server.usage.api.StatisticsRequestContext;
import com.esri.gpt.server.usage.api.StatisticsTypes;
import com.esri.gpt.server.usage.harvester.HarvesterStatisticsBuilder;

/**
 * This class is used to instantiate and provide requested 
 * statistics builder.
 *
 */
public class GeoportalStatisticsProviderFactory {
	/**
	 * Builds usage report based on request.
	 * @param statRequestCtx the statistics request context
	 * @return geoportal statistics information
	 * @throws Exception if exception occurs
	 */
   public GeoportalUsageInformation buildUsageReport(StatisticsRequestContext statRequestCtx) throws Exception{
	   GeoportalUsageInformation geoportalStats = new GeoportalUsageInformation();
	   String[] restUriParts = statRequestCtx.getRestUriParts();
	   if(statRequestCtx.getRequestType().equalsIgnoreCase(StatisticsTypes.HARVESTER.toString())){
		   HarvesterStatisticsBuilder harvesterStats = new HarvesterStatisticsBuilder(statRequestCtx);
		   if(restUriParts.length >= 6 && restUriParts[5].equalsIgnoreCase(StatisticsMetrics.CONFIG.toString())){
		     harvesterStats.buildEnvironmentStatistics();
		   }else{
		     harvesterStats.buildStatistics();
		   }
		   geoportalStats.setHarvesterStats(harvesterStats);
	   }else{
		   
	   }
	   return geoportalStats;
	}
}
