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

/**
 * Provides a Rest API to search multiple repositories registerd in GPT
 * <p/>
 * <i>http://host:port/context</i><b>/rest/distributed?</b>
 * <p>Provides a listing of registered harvesting repositories.</p>
 * <b>Parameters:</b>
 *  <ul>
 *    <li>
 *      <b>rid</b> - the id associated with the repository.  You can have 
 *      multiple parameters with this name so as to compare results
 *      between different repositories (queryable)
 *    </li>
 *    <li>
 *      <b>rids</b> - Comma Delimited rid.  Can be used instead of the multiple 
 *      rid parameters(see above)(queryable)
 *    </li>
 *    <li>
 *      <b>maxSearchTimeMilliSec</b> - Maximum amount of time the results should
 *      take.  By default it's 5000 milliseconds
 *    </li>
 *    <li>
 *      <b>f</b> - the response format
 *      <br/>Valid values: <code>atom, html</code> 
 *    </li>
 *    <li>
 *      Other parameters are the same as the search criteria rest parameters 
 *      .  See this package summary of com.esri.gpt.control.georss for more 
 *      description of rest criteria.
 *    <li>
 *      Example: <code>.../rest/distributed?rid=local&rid=ArcGIS.COM&start=1&max=10&orderBy=relevance&maxSearchTimeMilliSec=5000&f=atom</code>
 *    </li>
 *  </ul>
 */
package com.esri.gpt.control.rest.search;
