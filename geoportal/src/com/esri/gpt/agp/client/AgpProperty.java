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
package com.esri.gpt.agp.client;

/**
 * A name/value property.
 */
public class AgpProperty {
  
  /** instance variables ====================================================== */
  private String name;
  private String value;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpProperty() {}
  
  /**
   * Construct with a name/value pair.
   * @param name the name
   * @param value the value
   */
  public AgpProperty(String name, String value) {
    this.name = name;
    this.value = value;
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the name. 
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  /**
   * Sets the name. 
   * @param name the name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Gets the value. 
   * @return the value
   */
  public String getValue() {
    return this.value;
  }
  /**
   * Sets the value. 
   * @param value the value
   */
  public void setValue(String value) {
    this.value = value;
  }

}