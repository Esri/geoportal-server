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
package com.esri.gpt.server.assertion.components;

/**
 * Represents an assertion value.
 */
public class AsnValue {

  /** instance variables ====================================================== */
  private String       value;
  private AsnValueType valueType;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnValue() {}
    
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnValue(AsnValue objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setTextValue(objectToDuplicate.getTextValue());
      if (objectToDuplicate.getValueType() != null) {
        this.setValueType(objectToDuplicate.getValueType().duplicate());
      }
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the text value.
   * @return the value
   */
  public String getTextValue() {
    return this.value;
  }
  /**
   * Sets the text value.
   * @param value the value
   */
  public void setTextValue(String value) {
    this.value = value;
  }
  
  /**
   * Gets the value type.
   * @return the value type
   */
  public AsnValueType getValueType() {
    return this.valueType;
  }
  /**
   * Sets the value type.
   * @param valueType the value type
   */
  public void setValueType(AsnValueType valueType) {
    this.valueType = valueType;
  }
  
  /** methods ================================================================= */
    
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnValue(this);
   * @return the duplicated object
   */
  public AsnValue duplicate() {
    return new AsnValue(this);
  }
    
}
