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
 * Represents an assertion predicate.
 */
public class AsnPredicate {

  /** instance variables ====================================================== */
  private String urn;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnPredicate() {}
  
  /**
   * Construct by duplicating an existing object.
   * @param objectToDuplicate the object to duplicate
   */
  public AsnPredicate(AsnPredicate objectToDuplicate) {
    if (objectToDuplicate != null) {
      this.setURN(objectToDuplicate.getURN());
    } 
  }
  
  /**
   * Constructs with supplied URN.
   * @param urn the URN
   */
  public AsnPredicate(String urn) {
    this.setURN(urn);
  }
  
  /** properties ============================================================== */
    
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
  
  /** methods ================================================================= */
  
  /**
   * Produces a deep clone of the object.
   * <br/>The duplication constructor is invoked.
   * <br/>return new AsnPredicate(this);
   * @return the duplicated object
   */
  public AsnPredicate duplicate() {
    return new AsnPredicate(this);
  }
    
}
