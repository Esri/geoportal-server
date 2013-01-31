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

import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.csw.CswQueryBuilder;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.Val;

/**
 * CSW protocol.
 */
public class HarvestProtocolCsw extends AbstractHTTPHarvestProtocol {

// class variables =============================================================
  /** Default profile */
  private static String DEFAULT_PROFILE = "";

  static {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    DEFAULT_PROFILE = appCfg.getCatalogConfiguration().getSearchConfig().getCswProfile();
  }
// instance variables ==========================================================
  /** CSW profile. */
  private String _profile = DEFAULT_PROFILE;

// constructors ================================================================
// properties ==================================================================
  /**
   * Gets profile.
   * @return profile
   */
  public String getProfile() {
    return _profile;
  }

  /**
   * Sets profile.
   * @param profile profile
   */
  public void setProfile(String profile) {
    _profile = Val.chkStr(profile);
  }

  /**
   * Gets protocol type.
   * @return protocol type
   * @deprecated 
   */
  @Override
  @Deprecated
  public final ProtocolType getType() {
    return ProtocolType.CSW;
  }

  @Override
  public String getKind() {
    return "CSW";
  }

// methods =====================================================================

  /**
   * Gets all the attributes.
   * @return attributes as attribute map
   */
  @Override
  public StringAttributeMap getAttributeMap() {
    StringAttributeMap properties = new StringAttributeMap();
    properties.set("profile", getProfile());
    return properties;
  }

  /**
   * Sets all the attributes.
   * @param attributeMap attributes as attribute map
   */
  @Override
  public void setAttributeMap(StringAttributeMap attributeMap) {
    setProfile(chckAttr(attributeMap.get("profile")));
  }

  @Override
  public QueryBuilder newQueryBuilder(IterationContext context, String url) {
    return new CswQueryBuilder(context, this, url);
  }
}
