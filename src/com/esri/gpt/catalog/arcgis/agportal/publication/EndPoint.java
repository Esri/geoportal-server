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

import com.esri.gpt.framework.util.Val;

/**
 * Publication endpoint.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class EndPoint {
  private String baseArcGISUrl;
  private String generateTokenUrl;
  private boolean isPublic;
  private boolean overwrite;
  
  /**
   * Creates instance of the end-point.
   * @param baseArcGISUrl base ArcGIS portal URL
   * @param generateTokenUrl URL to generate token
   * @param isPublic <code>true</code> if is public
   * @param overwrite  <code>true</code> if overwrite
   */
  public EndPoint(String baseArcGISUrl, String generateTokenUrl, boolean isPublic, boolean overwrite) {
    this.baseArcGISUrl = Val.chkStr(baseArcGISUrl);
    this.generateTokenUrl = Val.chkStr(generateTokenUrl);
    this.isPublic = isPublic;
    this.overwrite = overwrite;
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
   * Gets <code>true</code> if is public.
   * @return <code>true</code> if is public
   */
  public boolean getIsPublic() {
    return isPublic;
  }
  
  /**
   * Gets <code>true</code> if overwrite.
   * @return <code>true</code> if overwrite
   */
  public boolean getOverwrite() {
    return overwrite;
  }
}
