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
 * Provides information about an XML node (element or attribute).
 */
public class XmlNodeInfo {
  
  /** instance variables ====================================================== */
  private String localName;
  private String namespacePrefix;
  private String namespaceURI;
  private String nodeValue;

  /** constructors ============================================================ */
  
  /** Default constructor */
  public XmlNodeInfo() {
    super();
  }
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public XmlNodeInfo(XmlNodeInfo objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setLocalName(objectToDuplicate.getLocalName());
      this.setNamespacePrefix(objectToDuplicate.getNamespacePrefix());
      this.setNamespaceURI(objectToDuplicate.getNamespaceURI());
      this.setNodeValue(objectToDuplicate.getNodeValue());
    } 
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the local node name.
   * @return the local node name
   */
  public String getLocalName() {
    return this.localName;
  }
  /**
   * Sets the local node name.
   * @param localName local node name
   */
  public void setLocalName(String localName) {
    this.localName = localName;
  }
  
  /**
   * Gets the namespace prefix.
   * @return the namespace prefix
   */
  public String getNamespacePrefix() {
    return this.namespacePrefix;
  }
  /**
   * Sets the namespace prefix.
   * @param prefix the namespace prefix
   */
  public void setNamespacePrefix(String prefix) {
    this.namespacePrefix = prefix;
  }
  
  /**
   * Gets the namespace URI.
   * @return the namespace URI
   */
  public String getNamespaceURI() {
    return this.namespaceURI;
  }
  /**
   * Sets the namespace URI.
   * @param uri the namespace URI
   */
  public void setNamespaceURI(String uri) {
    this.namespaceURI = uri;
  }
  
  /**
   * Gets the node value.
   * @return the node value
   */
  public String getNodeValue() {
    return this.nodeValue;
  }
  /**
   * Sets the node value.
   * @param nodeValue the node value
   */
  public void setNodeValue(String nodeValue) {
    this.nodeValue = nodeValue;
  }
  
  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new XmlNodeInfo(this);
   * @return the duplicated object
   */
  public XmlNodeInfo duplicate() {
    return new XmlNodeInfo(this);
  }
  
}
