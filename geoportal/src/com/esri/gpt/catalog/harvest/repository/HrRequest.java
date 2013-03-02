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
import com.esri.gpt.framework.request.DaoRequest;
import com.esri.gpt.framework.request.RequestDefinition;

/**
 * Harvest repository query request.
 */
public class HrRequest 
       extends DaoRequest<RequestDefinition<HrCriteria,HrResult>> {

// class variables =============================================================

// instance variables ==========================================================
  
// constructors ================================================================

/**
 * Creates instance of the request.
 * @param requestContext request context
 * @param criteria search criteria
 * @param result query result
 */
public HrRequest(RequestContext requestContext,
                 HrCriteria criteria, 
                 HrResult result) {
  super(requestContext, 
        new RequestDefinition<HrCriteria,HrResult>(criteria,result));
}
// properties ==================================================================

/**
 * Gets the query criteria.
 * @return the query criteria
 */
public HrQueryCriteria getQueryCriteria() {
  return getRequestDefinition().getCriteria().getQueryCriteria();
}

/**
 * Gets the query result.
 * @return the query result
 */
public HrQueryResult getQueryResult() {
  return getRequestDefinition().getResult().getQueryResult();
}

/**
 * Gets the action criteria.
 * @return the action criteria
 */
public HrActionCriteria getActionCriteria() {
  return getRequestDefinition().getCriteria().getActionCriteria();
}

/**
 * Gets the action result.
 * @return the action result
 */
public HrActionResult getActionResult() {
  return getRequestDefinition().getResult().getActionResult();
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
// methods =====================================================================

}
