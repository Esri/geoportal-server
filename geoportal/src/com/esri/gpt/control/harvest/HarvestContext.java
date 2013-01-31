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
package com.esri.gpt.control.harvest;

import com.esri.gpt.catalog.harvest.history.HeCriteria;
import com.esri.gpt.catalog.harvest.repository.HrCriteria;
import com.esri.gpt.framework.request.PageCursor;

/**
 * Managed bean to store harvest data.
 */
public class HarvestContext implements PageCursor.IRecordsPerPageProvider {


// class variables =============================================================

// instance variables ==========================================================
/** Harvest repository criteria. */
private HrCriteria _harvestCriteria = new HrCriteria();
/** Harvest history criteria. */
private HeCriteria _historyCriteria = new HeCriteria();
/** Records per page. */
private int _recordsPerPage = 
  PageCursor.IRecordsPerPageProvider.DEFAULT_RECORDS_PER_PAGE;

// constructors ================================================================

// properties ==================================================================

/**
 * Gets harvest repository criteria. 
 * @return  harvest repository criteria
 */
public HrCriteria getHarvestCriteria() {
  return _harvestCriteria;
}

/**
 * Sets harvest repository criteria.
 * @param harvestCriteria harvest repository criteria
 */
public void setHarvestCriteria(HrCriteria harvestCriteria) {
  _harvestCriteria = harvestCriteria!=null? harvestCriteria: new HrCriteria();
}

/**
 * Gets harvest history criteria.
 * @return harvest history criteria
 */
public HeCriteria getHistoryCriteria() {
  return _historyCriteria;
}

/**
 * Sets harvest history criteria.
 * @param historyCriteria harvest history criteria
 */
public void setHistoryCriteria(HeCriteria historyCriteria) {
  _historyCriteria = historyCriteria!=null? historyCriteria: new HeCriteria();
}

/**
 * Gets number of records per page.
 * @return number of records per page
 */
@Override
public int getRecordsPerPage() {
  return _recordsPerPage;
}

/**
 * Sets number of records per page.
 * @param recordsPerPage number of records per page
 */
@Override
public void setRecordsPerPage(int recordsPerPage) {
  _recordsPerPage = Math.max(recordsPerPage, 
    PageCursor.IRecordsPerPageProvider.MIN_RECORDS_PER_PAGE);
}

// methods =====================================================================
}
