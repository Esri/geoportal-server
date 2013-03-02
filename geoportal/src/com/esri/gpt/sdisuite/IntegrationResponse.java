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
package com.esri.gpt.sdisuite;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents the response for a checkUrl request.
 */
public class IntegrationResponse {

  /** instance variables ====================================================== */
  private Map<String, BriefLicenseInfo> briefLicenseInfos = new LinkedHashMap<String, BriefLicenseInfo>();
  private boolean licensed;
  private String  licenseSelectionClientUrl;
  private boolean secured;
  private String  url;

  /** constructors ============================================================ */
  
  /** Default constructor */
  public IntegrationResponse() {}

  /** properties ============================================================== */
  
  /**
   * Gets the brief license info.
   * @return the brief license info
   */
  @Deprecated
  public Map<String, BriefLicenseInfo> getBriefLicenseInfos() {
    return this.briefLicenseInfos;
  }
  
  /**
   * Sets the brief license info.
   * @param briefLicenseInfos the brief license info
   */
  @Deprecated
  public void setBriefLicenseInfos(final Map<String, BriefLicenseInfo> briefLicenseInfos) {
    this.briefLicenseInfos = briefLicenseInfos;
  }
  
  /**
   * Gets the flag indicating whether of not the URL is licensed.
   * @return <code>true</code> if the URL is licensed
   */
  public final boolean isLicensed() {
    return this.licensed;
  }
  /**
   * Sets the flag indicating whether of not the URL is licensed.
   * @param licensed <code>true</code> if the URL is licensed
   */
  public final void setLicensed(boolean licensed) {
    this.licensed = licensed;
  }
  
  /**
   * Gets the license selection client URL.
   * @return the license selection client URL
   */
  public final String getLicenseSelectionClientUrl() {
    return licenseSelectionClientUrl;
  }
  /**
   * Sets the license selection client URL.
   * @param url the license selection client URL
   */
  public final void setLicenseSelectionClientUrl(String url) {
    this.licenseSelectionClientUrl = url;
  }
  
  /**
   * Gets the flag indicating whether of not the URL is secured.
   * @return <code>true</code> if the URL is secured
   */
  public final boolean isSecured() {
    return this.secured;
  }
  /**
   * Sets the flag indicating whether of not the URL is secured.
   * @param secured <code>true</code> if the URL is secured
   */
  public final void setSecured(boolean secured) {
    this.secured = secured;
  }
  
  /**
   * Gets the URL.
   * <br/>Can be either gatewayUrl or Url that has been forwarded to the checkUrl method.
   * @return the URL
   */
  public String getUrl() {
    return this.url;
  }
  /**
   * Sets the URL.
   * <br/>Can be either gatewayUrl or Url that has been forwarded to the checkUrl method.
   * @param url the URL
   */
  public void setUrl(final String url) {
    this.url = url;
  }
  
  /** methods ================================================================= */

  /**
   * Adds a brief license info parameter.
   * @param pKey the parameter key
   * @param pBriefLicenseInfo the parameter value
   */
  @Deprecated
  public void addBriefLicenseInfo(final String pKey, final BriefLicenseInfo pBriefLicenseInfo) {
    if (getBriefLicenseInfos().containsKey(pKey)) {
      getBriefLicenseInfos().remove(pKey);
    }
    getBriefLicenseInfos().put(pKey, pBriefLicenseInfo);
  }

}