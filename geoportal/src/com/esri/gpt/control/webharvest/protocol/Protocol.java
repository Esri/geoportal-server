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
  public abstract QueryBuilder newQueryBuilder(IterationContext context, String url);
  /**
   * Gets ad-hoc harvesting info.
   * @return ad-hoc
   */
  String getAdHoc();
  /**
   * Sets ad-hoc info.
   * @param adHoc ad-hoc iinfo
   */
  void setAdHoc(String adHoc);
}
