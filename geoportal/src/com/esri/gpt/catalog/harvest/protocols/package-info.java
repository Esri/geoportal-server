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
 * Protocols definitions.
 * <p/>
 * Protocol implements all methods required to store and restore protocol 
 * attributes from the database. It also provides a method to create suitable
 * communication client.
 * <p/>
 * Generic <code>HarvestProtocol</code> class provides two abstract 
 * <code>applyAttributeMap</code> and <code>extractAttributeMap</code> helping 
 * with protocol serialization. Each concrete protocol has to provide 
 * implementation of both methods, to be serialized into <i>xml</i> data.
 * 
 * @see com.esri.gpt.catalog.harvest.protocols.HarvestProtocol
 */
package com.esri.gpt.catalog.harvest.protocols;

