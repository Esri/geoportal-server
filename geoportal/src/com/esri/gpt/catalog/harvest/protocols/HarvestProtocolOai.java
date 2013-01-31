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
import com.esri.gpt.control.webharvest.client.oai.OaiQueryBuilder;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.Val;

/**
 * OAI protocol.
 */
public class HarvestProtocolOai extends AbstractHTTPHarvestProtocol {

// class variables =============================================================
// instance variables ==========================================================
  /** OAI set name. */
  private String _set = "";
  /** OAI prefix. */
  private String _prefix = "";

// constructors ================================================================
// properties ==================================================================
  /**
   * Gets set.
   * @return set
   */
  public String getSet() {
    return _set;
  }

  /**
   * Sets set.
   * @param set set
   */
  public void setSet(String set) {
    _set = Val.chkStr(set);
  }

  /**
   * Gets prefix.
   * @return prefix
   */
  public String getPrefix() {
    return _prefix;
  }

  /**
   * Sets prefix.
   * @param prefix prefix
   */
  public void setPrefix(String prefix) {
    _prefix = Val.chkStr(prefix);
  }

  /**
   * Gets protocol type.
   * @return protocol type
   * @deprecated
   */
  @Override
  @Deprecated
  public final ProtocolType getType() {
    return ProtocolType.OAI;
  }

  @Override
  public String getKind() {
    return "OAI";
  }

// methods =====================================================================

  /**
   * Gets all the attributes.
   * @return attributes as attribute map
   */
  @Override
  public StringAttributeMap getAttributeMap() {
    StringAttributeMap properties = new StringAttributeMap();

    properties.set("set", getSet());
    properties.set("prefix", getPrefix());

    return properties;
  }

  /**
   * Sets all the attributes.
   * @param attributeMap attributes as attribute map
   */
  @Override
  public void setAttributeMap(StringAttributeMap attributeMap) {
    setSet(chckAttr(attributeMap.get("set")));
    setPrefix(chckAttr(attributeMap.get("prefix")));
  }

  @Override
  public QueryBuilder newQueryBuilder(IterationContext context, String url) {
    return new OaiQueryBuilder(context, this, url);
  }
}
