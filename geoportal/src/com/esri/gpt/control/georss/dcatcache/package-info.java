/*
 * Copyright 2013 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * DCAT cache related classes.
 * <p>
 * Please, include {@link com.esri.gpt.control.georss.dcatcache.DcatCacheTask} in
 * the &lt;scheduler&gt; node in gpt.xml configuration file, for example:
 * <code><pre>
 * 
 * &lt;scheduler active="true" corePoolSize="0"&gt;
 *   ...
 *   &lt;thread class="com.esri.gpt.control.georss.dcatcache.DcatCacheTask" period='1[DAY]' delay="15[SECOND]"/&gt;
 *   ...
 * &lt;/scheduler&gt;
 * </pre></code>
 * This will cause to periodically generate DCAT cache. Until the cache is generated,
 * empty response will be provided by the DCAT REST end-point.
 * </p>
 */
package com.esri.gpt.control.georss.dcatcache;
