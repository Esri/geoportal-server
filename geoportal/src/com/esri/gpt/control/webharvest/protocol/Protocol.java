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
package com.esri.gpt.control.webharvest.protocol;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.engine.DataProcessor;
import com.esri.gpt.control.webharvest.engine.ExecutionUnit;
import com.esri.gpt.control.webharvest.engine.Executor;
import com.esri.gpt.control.webharvest.engine.IWorker;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.resource.query.QueryBuilder;

/**
 * Protocol.
 */
public interface Protocol {
  /**
   * Gets protocol kind.
   * @return protocol kind
   */
  String getKind();
  /**
   * Gets protocol attributes map. Used to exchange attributes with the form.
   * @return attributes map
   */
  StringAttributeMap getAttributeMap();
  /**
   * Sets protocol attributes map. Used to exchange attributes with the form.
   * @param attributeMap attributes map
   */
  void setAttributeMap(StringAttributeMap attributeMap);
  /**
   * Extracts protocol attributes map. Used saving (typically encrypts user name/password).
   * @return attributes map
   */
  StringAttributeMap extractAttributeMap();
  /**
   * Applies protocol attributes map. Used loading (typically decrypts user name/password).
   * @param attributeMap attributes map
   */
  void applyAttributeMap(StringAttributeMap attributeMap);
  /**
   * Gets flag set.
   * @return flags set
   */
  long getFlags();
  /**
   * Sets flag set.
   * @param flags flag set
   */
  void setFlags(long flags);
  /**
   * Creates new query builder.
   * @param context iteration context
   * @param url url
   * @return query builder
   */
  QueryBuilder newQueryBuilder(IterationContext context, String url);
  /**
   * Creates new executor.
   * @param dataProcessor data processor
   * @param unit execution unit
   * @param worker worker
   * @return executor
   */
  Executor newExecutor(DataProcessor dataProcessor, ExecutionUnit unit, IWorker worker);
  /**
   * Gets ad-hoc harvesting info.
   * <p>
   * Ad-hoc information allows to store precise definition or pattern of the time
   * when harvesting should occur. Ad-hoc is a pipe (|) separated set of time definitions.
   * Currently, the following syntax is supported:
   * <ul>
   * <li>
   * <b>hh:mm</b> - daily at a certain time, for example: 10:05 (every day at 10:05)
   * </li>
   * <li>
   * <b>&lt;day of the week&gt;,hh:mm</b> - every specified day of the week at a certain time, for example: SUNDAY,10:05 (every Sunday at 10:05)
   * </li>
   * <li>
   * <b>&lt;day of the month&gt;,hh:mm</b> - every specified day of the month at a certain time, for example: 7,10:05 (every seventh day of the month at 10:05
   * </li>
   * <li>
   * <b>&lt;n-th day of the week&gt;,&lt;day of the week&gt;,hh:mm</b> - every specified day of the week at a certain time, for example: 1,MONDAY,10:05 (every first Monday of the month at 10:05)
   * </li>
   * <li>
   * <b>yyyy-MM-ddThh:mm</b> - at the specific date and time, for example: 2013-03-17T10:05 (precisely on 17-th of March, 2013 at 10:05)
   * </li>
   * </ul>
   * </p>
   * @return ad-hoc
   */
  String getAdHoc();
  /**
   * Sets ad-hoc info.
   * @param adHoc ad-hoc info
   * @see #setAdHoc
   */
  void setAdHoc(String adHoc);
}
