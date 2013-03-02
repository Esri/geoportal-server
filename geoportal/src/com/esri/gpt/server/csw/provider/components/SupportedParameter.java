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

/**
 * Defines a supported parameter and it's possible values.
 */
public class SupportedParameter {
  
  /** instance variables ====================================================== */
  private String           name;
  private ISupportedValues supportedValues;

  /** constructors ============================================================ */
  
  /** Default constructor */
  public SupportedParameter() {}
  
  /**
   * Constructs with a supplied parameter name and a collection of supported values.
   * @param name the parameter name
   * @param supportedValues the supported values
   */
  public SupportedParameter(String name, ISupportedValues supportedValues) {
    this.setName(name);
    this.setSupportedValues(supportedValues);
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the parameter name.
   * @return the parameter name
   */
  public String getName() {
    return this.name;
  }
  /**
   * Sets the parameter name.
   * @param name the parameter name
   */
  public void setName(String name) {
    this.name = name;
  }
  
  /**
   * Gets the supported values.
   * @return the supported values
   */
  public ISupportedValues getSupportedValues() {
    return this.supportedValues;
  }
  /**
   * Sets the supported values.
   * @param supportedValues the supported values
   */
  public void setSupportedValues(ISupportedValues supportedValues) {
    this.supportedValues = supportedValues;
  }

}
