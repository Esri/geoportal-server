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
package com.esri.gpt.catalog.harvest.protocols;

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.waf.WafQueryBuilder;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.resource.query.QueryBuilder;

/**
 * WAF protocol.
 */
public class HarvestProtocolWaf extends AbstractHTTPHarvestProtocol {

// class variables =============================================================
// instance variables ==========================================================
// constructors ================================================================
// properties ==================================================================
  /**
   * Gets protocol type.
   * @return protocol type
   * @deprecated 
   */
  @Override
  @Deprecated
  public final ProtocolType getType() {
    return ProtocolType.WAF;
  }

  @Override
  public String getKind() {
    return "WAF";
  }

// methods =====================================================================
  @Override
  public QueryBuilder newQueryBuilder(IterationContext context, String url) {
    return new WafQueryBuilder(context, this, url);
  }

  @Override
  public StringAttributeMap getAttributeMap() {
    StringAttributeMap properties = new StringAttributeMap();
    properties.set("waf.username", getUserName());
    properties.set("waf.password", getUserPassword());
    return properties;
  }

  @Override
  public void setAttributeMap(StringAttributeMap attributeMap) {
    setUserName(chckAttr(attributeMap.get("waf.username")));
    setUserPassword(chckAttr(attributeMap.get("waf.password")));
  }

  @Override
  public void applyAttributeMap(StringAttributeMap attributeMap) {
    setUserName(decryptString(chckAttr(attributeMap.get("username"))));
    setUserPassword(decryptString(chckAttr(attributeMap.get("password"))));
  }

  @Override
  public StringAttributeMap extractAttributeMap() {
    StringAttributeMap properties = new StringAttributeMap();
    properties.set("username", encryptString(getUserName()));
    properties.set("password", encryptString(getUserPassword()));
    return properties;
  }
}
