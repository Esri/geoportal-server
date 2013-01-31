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
package com.esri.gpt.catalog.arcgis.metadata;

import com.esri.gpt.framework.util.Val;
import java.util.regex.Pattern;

/**
 * Holds properties associated with an ArcGIS Server and the type of resource targetted 
 * for processing.
 */
public class AGSTarget {  
  
  /** instance variables ====================================================== */ 
  private String     restUrl;
  private String     soapUrl;
  private String     targetUrl;
  private String     targetSoapUrl;
  private TargetType targetType;
  private boolean    wasRecognized = false;
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public AGSTarget() {}

  /** properties ============================================================== */
  
  /**
   * Gets the URL to the REST services endpoint of the ArcGIS server.
   * @return the REST URL
   */
  public String getRestUrl() {
    return this.restUrl;
  }
  /**
   * Sets the URL to the REST services endpoint of the ArcGIS server.
   * @param url the REST URL
   */
  public void setRestUrl(String url) {
    this.restUrl = url;
  }
  
  /**
   * Gets the URL to the SOAP services endpoint of the ArcGIS server.
   * @return the SOAP URL
   */
  public String getSoapUrl() {
    return this.soapUrl;
  }
  /**
   * Sets the SOAP URL for the service.
   * @param url the SOAP URL
   */
  public void setSoapUrl(String url) {
    this.soapUrl = url;
  }
  
  /**
   * Gets the URL to the targetted resource.
   * @return the targetted resource URL
   */
  public String getTargetUrl() {
    return this.targetUrl;
  }
  /**
   * Sets the URL to the targetted resource.
   * @param url the targetted resource URL
   */
  public void setTargetUrl(String url) {
    this.targetUrl = url;
  }

  /**
   * Gets target SOAP URL.
   * @return target SOAP URL
   */
  public String getTargetSoapUrl() {
    return targetSoapUrl;
  }

  /**
   * Sets target SOAP URL.
   * @param targetSoapUrl target SOAP URL
   */
  public void setTargetSoapUrl(String targetSoapUrl) {
    this.targetSoapUrl = targetSoapUrl;
  }
  
  /**
   * Update target SOAP URL.
   */
  public void updateTargetSoapUrl() {

    String targetBase = targetUrl;
    if (targetBase.indexOf("?") != -1) {
      targetBase = targetBase.substring(0,targetBase.indexOf("?"));
    }
    if (targetBase.endsWith("/")) {
      targetBase = targetBase.substring(0,targetBase.length() - 1);
    }
    targetSoapUrl = targetBase;
    if (Val.chkStr(restUrl).length()>0 && targetSoapUrl.startsWith(restUrl)) {
      targetSoapUrl = Pattern.compile("\\Q"+Val.chkStr(restUrl)+"\\E", Pattern.CASE_INSENSITIVE).matcher(targetSoapUrl).replaceFirst(soapUrl);
    }
    
  }

  /**
   * Gets the type of resource that is targeted for processing.
   * @return the target type
   */
  public TargetType getTargetType() {
    return this.targetType;
  }
  /**
   * Sets the type of resource that is targetted for processing.
   * @param type the target type
   */
  public void setTargetType(TargetType type) {
    this.targetType = type;
  }
  
  /**
   * Gets the status indicacating whether or not the target was recognized.
   * @return true if the target was recognized
   */
  public boolean getWasRecognized() {
    return this.wasRecognized;
  }
  /**
   * Sets the status indicacating whether or not the target was recognized.
   * @param wasRecognized true if the target was recognized
   */
  public void setWasRecognized(boolean wasRecognized) {
    this.wasRecognized = wasRecognized;
  }
  
  /** methods ================================================================= */
  
  /**
   * Returns a string representation of the object.
   * @return the string
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("AGSTarget");
    sb.append("\n  restUrl=").append(this.getRestUrl());
    sb.append("\n  soapUrl=").append(this.getSoapUrl());
    sb.append("\n  targetUrl=").append(this.getTargetUrl());
    sb.append("\n  targetType=").append(this.getTargetType());
    sb.append("\n  wasRecognized=").append(this.getWasRecognized());
    return sb.toString();
  }
    
  /** enumerations ============================================================ */
  
  /**
   * The type of resource that ws targettted on the ArcGIS server.
   */
  public enum TargetType {
    ROOT,
    FOLDER,
    SERVICE,
    UNKNOWN;
  }
  
}
