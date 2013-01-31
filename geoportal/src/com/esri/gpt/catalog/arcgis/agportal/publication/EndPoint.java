/*
 * See the NOTICE file distributed with
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
package com.esri.gpt.catalog.arcgis.agportal.publication;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;

/**
 * Publication endpoint.
 */
public class EndPoint {
  private String baseArcGISUrl;
  private String generateTokenUrl;
  private String referer = "";
  
  /**
   * Creates instance of the end-point.
   * @param baseArcGISUrl base ArcGIS portal URL
   * @param generateTokenUrl URL to generate token
   * @param referer referer
   */
  public EndPoint(String baseArcGISUrl, String generateTokenUrl, String referer) {
    this.baseArcGISUrl = Val.chkStr(baseArcGISUrl);
    this.generateTokenUrl = Val.chkStr(generateTokenUrl);
    this.referer = Val.chkStr(referer);
  }

  /**
   * Extracts end-point from the configuration
   * @param ctx request context
   * @return end-point
   */
  public static EndPoint extract(RequestContext ctx) {
    StringAttributeMap params = ctx.getCatalogConfiguration().getParameters();
    return new EndPoint(
        params.get("share.with.arcgis.base.url").getValue(),
        params.get("share.with.arcgis.token.url").getValue(),
        params.get("share.with.arcgis.referer").getValue());
  }
  
  /**
   * Gets base ArcGIS portal URL.
   * @return  base ArcGIS portal URL
   */
  public String getBaseArcGISUrl() {
    return baseArcGISUrl;
  }
  
  /**
   * Gets URL to generate token.
   * @return URL to generate token
   */
  public String getGenerateTokenUrl() {
    return generateTokenUrl;
  }

  /**
   * Gets referer.
   * @return referer
   */
  public String getReferer() {
    return referer;
  }
}
