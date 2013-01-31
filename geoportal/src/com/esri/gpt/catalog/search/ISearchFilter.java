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

import java.io.Serializable;

/**
 * The Interface ISearchFilter.defines filters that will be used
 * by the Search Criteria.
 */
public interface ISearchFilter extends Serializable {

//methods ======================================================================
/**
 * Checks if object has valid data.  Also performs any corrections to the
 * date needed.  Should be called at the beginning of the business process
 * stage.
 * 
 * @throws SearchException Exception thrown when none valid data found
 */
public void validate() throws SearchException; 

/**
 * Resets this object to default properties.
 */
public void reset();

/**
 * Gets the parameters representing <b>this</b> object to be saved for
 *  later restoration.
 * 
 * @return object <b>this</b> parameters with restoration values (possibly null)
 */
public SearchParameterMap getParams ();

/**
 * Restores <b>this</b> object to the state
 * held by the values in the Parameter map.
 * 
 * @param parameterMap the new parameters for <b>this</b>
 * 
 * @throws SearchException the search exception during error
 */
public void setParams(SearchParameterMap parameterMap) throws SearchException; 

/**
 * Checks object in argument is equals  to this.
 * 
 * @param obj the obj
 * 
 * @return true, if is equals
 */
public boolean isEquals(Object obj);

}

