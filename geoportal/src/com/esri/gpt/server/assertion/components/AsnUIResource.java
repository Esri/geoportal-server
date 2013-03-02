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
import com.esri.gpt.framework.util.Val;

/**
 * Represents a UI resource.
 */
public class AsnUIResource extends AsnProperty {
  
  /** instance variables ====================================================== */
  private String defaultValue;
  private String resourceKey;
  private String resourceValue;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnUIResource() {
    super();
  }
  
  /**
   * Constructs with a resource key and default value.
   * @param predicate the predicate
   * @param resourceKey the resource key
   * @param defaultValue the default value
   */
  public AsnUIResource(String predicate, String resourceKey, String defaultValue) {
    this.setPredicate(predicate);
    this.setResourceKey(resourceKey);
    this.setDefaultValue(defaultValue);
  }
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnUIResource(AsnUIResource objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setPredicate(objectToDuplicate.getPredicate());
      this.setResourceKey(objectToDuplicate.getResourceKey());
      this.setResourceValue(objectToDuplicate.getResourceValue());
      this.setDefaultValue(objectToDuplicate.getDefaultValue());
    }
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the default value.
   * @return the default value
   */
  public String getDefaultValue() {
    return this.defaultValue;
  }
  /**
   * Sets the default value.
   * @param defaultValue the default value
   */
  public void setDefaultValue(String defaultValue) {
    this.defaultValue = defaultValue;
  }
    
  /**
   * Gets the UI property bundle resource key.
   * @return the resource key
   */
  public String getResourceKey() {
    return this.resourceKey;
  }
  
  /**
   * Sets the UI property bundle resource key.
   * @param resourceKey the resource key
   */
  public void setResourceKey(String resourceKey) {
    this.resourceKey = resourceKey;
  }
  
  /**
   * Gets the resource value.
   * @return the resource value
   */
  public String getResourceValue() {
    return this.resourceValue;
  }
  /**
   * Sets the resource value.
   * @param resourceValue the resource value
   */
  public void setResourceValue(String resourceValue) {
    this.resourceValue = resourceValue;
  }
  
  /**
   * Gets the value.
   * @return the value
   */
  @Override
  public String getValue() {
    String v = Val.chkStr(this.getResourceValue());
    if (v.length() > 0) {
      return v;
    } else {
      return this.getDefaultValue();
    }
  }
  /**
   * Sets the value.
   * @param value the value
   */
  @Override
  public void setValue(String value) {
    this.setResourceValue(value);
  }
  
  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnUIResource(this);
   * @return the duplicated object
   */
  public AsnUIResource duplicate() {
    return new AsnUIResource(this);
  }
  
}
