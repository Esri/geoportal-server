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
 * Harvest job requests.
 * Harvest job is an order to harvest harvesting repository as soon as possible
 * regardles regular schedule. <i>Harvesting Service</i> will pickup a job and
 * launch <i>Harvesting Tool</i> with command to harvest specified site.
 * <p/>
 * Harvesting job can be <i>submitted</i>, <i>running</i>, or <i>completed</i>.
 * There is only one <i>submited</i>, or <i>running</i> job for a single harvest
 * repository (<i>submitted</i> and <i>running</i> are mutual exclusive).
 * <p/>
 * It is possible to change status from <i>submitted</i> to <i>running</i>, and 
 * from <i>running</i> back to <i>submitted</i>. It is also possible to change
 * status either from <i>submitted</i>, or <i>running</i> to <i>completed</i>.
 * However, once the status has been changed to <i>completed</i> it is 
 * impossible to change this status back to <i>submitted</i> or <i>running</i>.
 */
package com.esri.gpt.catalog.harvest.jobs;

