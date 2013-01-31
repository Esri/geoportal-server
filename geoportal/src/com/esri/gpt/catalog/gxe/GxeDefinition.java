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
package com.esri.gpt.catalog.gxe;

/**
 * A Geoportal XML editor definition.
 */
public class GxeDefinition {
  
  /** instance variables ====================================================== */
  private String     fileLocation;
  private String     key;
  private XmlElement rootElement;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public GxeDefinition() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public GxeDefinition(GxeDefinition objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setFileLocation(objectToDuplicate.getFileLocation());
      this.setKey(objectToDuplicate.getKey());
      if (objectToDuplicate.getRootElement() != null) {
        this.setRootElement(objectToDuplicate.getRootElement().duplicate(null));
      }
    }
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the location of the root definition file.
   * @return the location
   */
  public String getFileLocation() {
    return this.fileLocation;
  }
  
  /**
   * Sets the location of the root definition file.
   * @param fileLocation the location
   */
  public void setFileLocation(String fileLocation) {
    this.fileLocation = fileLocation;
  }
  
  /**
   * Gets the key for this definition.
   * @return the key
   */
  public String getKey() {
    return this.key;
  }
  /**
   * Sets the key for this definition.
   * @param key the key
   */
  public void setKey(String key) {
    this.key = key;
  }
  
  /**
   * Gets the root element.
   * @return the root element
   */
  public XmlElement getRootElement() {
    return this.rootElement;
  }
  /**
   * Sets the root element.
   * @param rootElement the root element
   */
  public void setRootElement(XmlElement rootElement) {
    this.rootElement = rootElement;
  }
    
  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new GxdDefinition(this);
   * @return the duplicated object
   */
  public GxeDefinition duplicate() {
    return new GxeDefinition(this);
  }
   
}
