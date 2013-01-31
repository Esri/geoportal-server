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

import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.request.DaoRequest;
import com.esri.gpt.framework.request.RequestDefinition;

/**
 * Harvest repository history request.
 */
public class HeRequest 
        extends DaoRequest<RequestDefinition<HeCriteria,HeResult>> {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/**
 * Create instance of the request.
 * @param requestContext request context
 * @param criteria request criteria
 * @param result request result
 */
public HeRequest(RequestContext requestContext,
                 HeCriteria criteria, 
                 HeResult result) {
  super(requestContext, 
        new RequestDefinition<HeCriteria,HeResult>(criteria,result));
}

// properties ==================================================================

/**
 * Gets the query criteria.
 * @return the query criteria
 */
public HeQueryCriteria getQueryCriteria() {
  return getRequestDefinition().getCriteria().getQueryCriteria();
}

/**
 * Gets the query result.
 * @return the query result
 */
public HeQueryResult getQueryResult() {
  return getRequestDefinition().getResult().getQueryResult();
}

/**
 * Gets the action criteria.
 * @return the action criteria
 */
public HeActionCriteria getActionCriteria() {
  return getRequestDefinition().getCriteria().getActionCriteria();
}

/**
 * Gets the action result.
 * @return the action result
 */
public HeActionResult getActionResult() {
  return getRequestDefinition().getResult().getActionResult();
}

/**
 * Gets the harvesting table name.
 * @return the harvesting table name
 */
protected String getHarvestingHistoryTableName() {
  CatalogConfiguration cfg = getRequestContext().getCatalogConfiguration();
  return cfg.getHarvestingHistoryTableName();
}

// methods =====================================================================

}
