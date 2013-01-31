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
 * Represents an assertion subject.
 */
public class AsnSubject {

  /** instance variables ====================================================== */
  private boolean requiresValuePart = true;
  private String  urn;
  private String  urnPrefix;
  private String  valuePart;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnSubject() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnSubject(AsnSubject objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setRequiresValuePart(objectToDuplicate.getRequiresValuePart());
      this.setURN(objectToDuplicate.getURN());
      this.setURNPrefix(objectToDuplicate.getURNPrefix());
      this.setValuePart(objectToDuplicate.getValuePart());
    } 
  }
  
  /**
   * Constructs with a URN prefix and type part.
   * @param urnPrefix the URN prefix
   */
  public AsnSubject(String urnPrefix) {
    this.setURNPrefix(urnPrefix);
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the flag indicating whether or not a value part is required.
   * @return <code>true</code> if a value part is required
   */
  public boolean getRequiresValuePart() {
    return this.requiresValuePart;
  }
  /**
   * Sets the flag indicating whether or not a value part is required.
   * @param requiresValuePart <code>true</code> if a value part is required
   */
  public void setRequiresValuePart(boolean requiresValuePart) {
    this.requiresValuePart = requiresValuePart;
  }
    
  /**
   * Gets the URN.
   * @return the URN
   */
  public String getURN() {
    return this.urn;
  }
  /**
   * Sets the URN.
   * @param urn the URN
   */
  public void setURN(String urn) {
    this.urn = urn;
  }
  
  /**
   * Gets the URN prefix.
   * @return the URN prefix
   */
  public String getURNPrefix() {
    return this.urnPrefix;
  }
  /**
   * Sets the URN prefix.
   * @param urnPrefix the URN prefix
   */
  public void setURNPrefix(String urnPrefix) {
    this.urnPrefix = urnPrefix;
  }
  
  /**
   * Gets the value part associated with the subject URN.
   * @return the value part
   */
  public String getValuePart() {
    return this.valuePart;
  }
  /**
   * Sets the value part associated with the subject URN.
   * @param valuePart the value part
   */
  public void setValuePart(String valuePart) {
    this.valuePart = valuePart;
  }
  
  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnSubject(this);
   * @return the duplicated object
   */
  public AsnSubject duplicate() {
    return new AsnSubject(this);
  }
  
  public boolean isType(String urnPrefix) {
    String pfx = this.getURNPrefix();
    return (pfx != null) && pfx.equals(urnPrefix);
  }
    
}
