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
package com.esri.gpt.framework.resource.query;

import com.esri.gpt.catalog.search.ISearchFilterSpatialObj.OptionsBounds;
import com.esri.gpt.catalog.search.SearchEngineCSW.AimsContentTypes;
import com.esri.gpt.catalog.search.SearchFilterSort.OptionsSort;
import com.esri.gpt.framework.geometry.Envelope;
import java.util.Date;

/**
 * Query criteria.
 * @see QueryBuilder
 */
public interface Criteria {

/**
 * Gets maximum number of records to fetch.
 * @return maximum number of records to fetch or <code>null</code> if no limit
 */
Integer getMaxRecords();

/**
 * Gets search text.
 * @return search text or <code>null</code> or <i>empty string</i> if no search text
 */
String getSearchText();

/**
 * Gets update date to search from.
 * @return update date to search from or <code>null</code> if no date
 */
Date getFromDate();

/**
 * Gets update date to search to.
 * @return update date to search to or <code>null</code> if no date
 */
Date getToDate();

/**
 * Gets bounding box. Bounding box is an array of numbers arranged in the
 * following pattern: [<i>xmin</i>,<i>ymin</i>,<i>xmax</i>,<i>ymax</i>].
 * @return bounding box or <code>null</code> if no bounding box
 */
Envelope getBBox();

/**
 * Content type.
 * @return content type
 */
AimsContentTypes getContentType();

/**
 * Gets data categories.
 * @return array of data categories or <code>null</code> if no any data category
 */
String[] getDataCategory();

/**
 * Gets bounding box option.
 * @return bounding box option
 */
OptionsBounds getBBoxOption();

/**
 * Gets sort option.
 * @return sort option
 */
OptionsSort getSortOption();
}
