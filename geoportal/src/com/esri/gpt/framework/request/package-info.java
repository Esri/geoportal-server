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
 * Single request support classes.
 * <p/>
 * Classes found in this package help to implement <i>Request Pattern</i>
 * commonly used in the application. <i>Request Pattern</i> is a standard way
 * to obtain required information from the underlying database, perform action
 * on the selected group of records, or generally, define any kind of request
 * which can be invoked regardles the data source (it doesn't have to be 
 * database records to be modified). Request can be a <i>query request</i>
 * or <i>action request</i> depending whether it doesn't or does modify data.
 * <p/>
 * There are several important elements of the pattern:
 * <ul>
 * <li><i>Record</i> - a record of data</li>
 * <li><i>Records</i> - collection of records</li>
 * <li><i>QueryCriteria</i> - selection criteria for the unmodyfying query</li>
 * <li><i>QuryResul</i>t - result of invoked query (usually: records)</li>
 * <li><i>ActionCriteria</i> - selection criteria for the modyfying request</li>
 * <li><i>ActionResult</i> - result of the invoked action (usually: number of 
 * the affected records)</li>
 * <li><i>DaoRequest</i> - data access object abstract implementation</li>
 * </ul>
 * There are also several minor classes defined, facilitiating sorting feature
 * implementation, filters, hybrid criteria and request.
 * <p/>
 * Suggested way of implementing <i>Request Pattern</i> is to create class of 
 * the request inherited from <i>DaoRequest</i>, which have arbitrary 
 * <i>action</i> method for example: <code>execute()</code>. This method will
 * read <i>query criteria</i> or <i>action criteria</i> depending of the 
 * category of the request, than performs request logic and places result in 
 * either <i>query result</i> or <i>action result</i>.
 * <p/>
 * It is advised to keep each result in the servlet <i>request</i> scope, where
 * criteria may be placed in <i>session</i> scope.
 */
package com.esri.gpt.framework.request;

