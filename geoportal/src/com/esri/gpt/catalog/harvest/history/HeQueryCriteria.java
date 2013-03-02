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

import com.esri.gpt.framework.request.QueryCriteria;
import com.esri.gpt.framework.util.DateRange;
import com.esri.gpt.framework.util.UuidUtil;

/**
 * Harvest events history query criteria.
 */
public class HeQueryCriteria extends QueryCriteria {

// class variables =============================================================

// instance variables ==========================================================
/** Repository id */
private String _uuid = "";
/** Event id */
private String _eventUuid = "";
/** Harvest date range. */
private DateRange _dateRange = new DateRange();

// constructors ================================================================

// properties ==================================================================

// methods =====================================================================

/**
 * Gets repository id.
 * @return repository id
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets repository id.
 * @param uuid repository id
 */
public void setUuid(String uuid) {
  _uuid = UuidUtil.isUuid(uuid) ? uuid : "";
}

/**
 * Gets event uuid.
 * @return the event uuid
 */
public String getEventUuid() {
  return _eventUuid;
}

/**
 * Sets event uuid.
 * @param eventUuid the event uuid
 */
public void setEventUuid(String eventUuid) {
  _eventUuid = UuidUtil.isUuid(eventUuid)? eventUuid: "";
}

/**
 * Gets harvest date range.
 * @return harvest date range
 */
public DateRange getDateRange() {
  return _dateRange;
}

/**
 * Sets harvest date range.
 * @param dateRange harvest date range
 */
public void setDateRange(DateRange dateRange) {
  _dateRange = dateRange!=null? dateRange: new DateRange();
}

}
