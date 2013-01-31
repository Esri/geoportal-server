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
 * Contains implementations of harvest repository clients. Harvest repository 
 * client provides unified way to communicate with remote repository. It allows
 * to check <i>live</i> status of the remote repository, and verify document
 * presence on the remote repository.
 * <h5>Supported protocols</h5>
 * <ul>
 * <li><a href="http://edndoc.esri.com/arcims/9.2/">ArcIMS</a></li>
 * <li><a HREF="http://www.opengeospatial.org/standards/cat"><b>C</b>atalog <b>S</b>ervice for <b>W</b>eb</a></li>
 * <li><a HREF="http://webdav.org/"><b>W</b>eb <b>A</b>ccessible <b>F</b>olders</a></li>
 * <li><a HREF="http://www.openarchives.org/"><b>O</b>pen <b>A</b>rchives <b>I</b>nitiative</a></li>
 * <li><a href="http://www.loc.gov/z3950/agency/">Z39.50</a></li>
 * </ul>
 * @see com.esri.gpt.catalog.harvest.clients.HRClient
 */
package com.esri.gpt.catalog.harvest.clients;

