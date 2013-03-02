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
package com.esri.gpt.server.csw.provider.components;
import com.esri.gpt.framework.collection.StringSet;

/**
 * Options associated with a CSW GetCapabilities request.
 */
public class CapabilityOptions {
  
  /** instance variables ====================================================== */
  private String    capabilitiesLocation;
  private String    languageCode;
  private String    responseLanguageCode;
  private StringSet sections = new StringSet();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public CapabilityOptions() {
    super();
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the location of the CSW Capabilities resource XML.
   * @return the Capabilities XML location
   */
  public String getCapabilitiesLocation() {
    return this.capabilitiesLocation;
  }
  /**
   * Sets the location of the CSW GetCapabilities resource XML.
   * @param location the Capabilities XML location
   */
  public void setCapabilitiesLocation(String location) {
    this.capabilitiesLocation = location;
  }
  
  /**
   * Gets the language code.
   * @return the language code
   */
  public String getLanguageCode() {
    return this.languageCode;
  }
  /**
   * Sets the language code.
   * @param languageCode the language code
   */
  public void setLanguageCode(String languageCode) {
    this.languageCode = languageCode;
  }
  
  /**
   * Gets the response language code.
   * @return the language code 
   */
  public String getResponseLanguageCode() {
    return this.responseLanguageCode;
  }
  /**
   * Sets the response language code.
   * @param languageCode the language code
   */
  public void setResponseLanguageCode(String languageCode) {
    this.responseLanguageCode = languageCode;
  }
  
  /**
   * Gets the requested sections.
   * @return the requested sections
   */
  public StringSet getSections() {
    return this.sections;
  }
  /**
   * Sets the requested sections.
   * @param sections the requested sections
   */
  public void setSections(StringSet sections) {
    this.sections = sections;
  }
  
}
