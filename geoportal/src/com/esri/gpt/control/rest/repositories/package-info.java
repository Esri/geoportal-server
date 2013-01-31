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
 * Provides a Rest API for repositories registered within the GPT harvesting tables.
 * <p/>
 * <i>http://host:port/context</i><b>/rest/repositories</b>
 * <p>Provides a listing of registered harvesting repositories.</p>
 * <b>Parameters:</b>
 *  <ul>
 *    <li>
 *      <b>id</b> - the integer ID associated with the repository (queryable)
 *    </li>
 *    <li>
 *      <b>uuid</b> - the UUID associated with the repository (queryable)
 *    </li>
 *    <li>
 *      <b>protocol</b> - the protocol for accessing the repository (queryable)
 *      <br/>Valid values: <code>ArcIMS, CSW, WAF, OAI, Z3950</code> 
 *    </li>
 *    <li>
 *      <b>name</b> - the name associated with the repository (queryable)
 *    </li>
  *    <li>
 *      <b>url</b> - the URL associated with the repository (queryable)
 *    </li>
 *    <li>
 *      <b>f</b> - the response format
 *      <br/>Valid values: <code>json, xml</code> - Default: <code>json</code>
 *    </li>
 *    <li>
 *      Example: <code>.../rest/repositories?protocol=csw&f=json</code>
 *    </li>
 *  </ul>
 */
package com.esri.gpt.control.rest.repositories;

