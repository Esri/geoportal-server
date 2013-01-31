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
 * Thread scheduler.
 * Provides mechanizm to schedule threads defined in xml configuration file. 
 * Entire configuration can be loaded into the
 * {@link com.esri.gpt.framework.scheduler.ThreadSchedulerConfiguration 
 * Configuration Object}.
 * <h4>Configuration</h4>
 * Configuration of the scheduler can be read from <i>XML</i> configuration
 * file. Typically, node <b>&lt;scheduler&gt;</b> as a root node will hold 
 * entire scheduler configuration, although the name of the root node can be 
 * freely choosen.<br/>
 * Configuration of the scheduler consist of specific attributes assigned to the
 * scheduler root node, and zero or more <b>&lt;thread&gt;</b> nodes as children
 * of the root node.
 * <h5>Attributes:</h5>
 * Root node can have the following attibutes:
 * <ul>
 * <li><b>active</b> - false|true - turns of and on scheduler</li>
 * <li><b>corePoolSize</b> - <number> - size of thrads pool, 0 to stop creating 
 * pool</li>
 * </ul>
 * <h5>Threads:</h5>
 * Each thread is defined by node called: <b>&lt;thread&gt;</b>. Therad 
 * definition includes a classpath to the worker thread and timing definitions
 * allowing to schedule therad.
 * <ul>
 * <li><b>class</b> - java.lang.Runnable implementation to schedule [required]</li>
 * <li><b>delay</b> - delay to execution since registering thread in units [optional; default: 0]</li> 
 * <li><b>period</b> - frequency of execution in units [optional; default: 0]</li>
 * <li><b>at</b> - time of execution in thte format "HH:MM" [optional; default: 00:00]</li>
 * </ul>
 * <h5>Delay and period:</h5>
 * Both, delay and period attributes are, by default, described by the number
 * which is a time in milliseconds. This is also possible to declare time unit.<br/>
 * Time unit can be one of the following, self explanatory strings:<br/>
 * <br/>
 * <code>[MILLISECOND], [SECOND], [MINUTE], [HOUR], [DAY], [WEEK], [MONTH]</code><br/>
 * <br/>
 * <h5>Examples:</h5>
 * 1. Schedule thread to execute once only, five minutes after application start:
 * <br/>
 * <code><pre>
 *   &lt;thread class="my.runnable" delay='5[MINUTE]'/&gt;
 * </pre></code>
 * 2. Schedule thread to execute every day at five o'clock am.:
 * <br/>
 * <code><pre>
 *   &lt;thread class="my.runnable" at='05:00'/&gt;
 * </pre></code>
 * 3. Schedule thread to be executed periodically with period of 2 hours and 
 * initial delay of 5000 milliseconds:
 * <br/>
 * <code><pre>
 *   &lt;thread class="my.runnable" period='2[HOUR]' delay='5000'/&gt; </pre></code>
 * or
 * <code><pre>
 *   &lt;thread class="my.runnable" period='2[HOUR]' delay='5[SECOND]'/&gt;
 * </pre></code>
 * @see com.esri.gpt.framework.scheduler.ThreadSchedulerConfiguration
 * @see java.util.concurrent.ScheduledExecutorService
 * @see java.util.concurrent.ScheduledFuture
 */
package com.esri.gpt.framework.scheduler;

