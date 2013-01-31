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
package com.esri.gpt.catalog.search;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * The Interface ISearchFilterTemporal. Defines time parameters 
 * to be used to define time bounds of the search criteria.
 */
public interface ISearchFilterTemporal extends ISearchFilter {

// methods ====================================================================
/**
 * Gets the date modified from as date.
 * 
 * @return the date modified from as date
 */
public Date getDateModifiedFromAsDate();

/**
 * Gets the date modified to as date.
 * 
 * @return the date modified to as date
 */
public Date getDateModifiedToAsDate();


/**
 * Gets the params.
 * 
 * @param simpleDatFormat the simple dat format
 * 
 * @return the params
 */
public SearchParameterMap getParams(SimpleDateFormat simpleDatFormat);

/**
 * Gets the selected modified date option.
 * 
 * @return the selected modified date option (trimmed, never null or empty, 
 * default = "any")
 */
public String getSelectedModifiedDateOption();

/**
 * Sets the selected modified date option.
 * 
 * @param selectedModifiedDateOption the new selected modified date option
 */
public void setSelectedModifiedDateOption(String selectedModifiedDateOption);

}


