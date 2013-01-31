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
package com.esri.gpt.catalog.harvest.jobs;

import com.esri.gpt.framework.request.QueryCriteria;

/**
 * Harvest job query criteria.
 * @see HjCriteria
 */
public class HjQueryCriteria extends QueryCriteria {

// class variables =============================================================
// instance variables ==========================================================
private String[] resourceUuids = new String[]{};
// constructors ================================================================
// properties ==================================================================

/**
 * Gets resource UUIDS.
 * @return the resourceUuids
 */
public String[] getResourceUuids() {
  return resourceUuids;
}

/**
 * Sets resource UUIDS.
 * @param resourceUuids the resourceUuids to set
 */
public void setResourceUuids(String[] resourceUuids) {
  this.resourceUuids = resourceUuids!=null? resourceUuids: new String[]{};
}
// methods =====================================================================
}
