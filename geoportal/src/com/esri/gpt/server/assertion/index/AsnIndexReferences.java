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
package com.esri.gpt.server.assertion.index;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of configured index references.
 */
public class AsnIndexReferences {

  /** instance variables ====================================================== */
  private AsnIndexReference       adminIndexRef;
  private List<AsnIndexReference> indexReferences = new ArrayList<AsnIndexReference>();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnIndexReferences() {}
  
  /**
   * Gets the configuration reference to the admin index.
   * @return the index configuration reference (null if disabled)
   */
  public AsnIndexReference getAdminIndexReference() {
    return this.adminIndexRef;
  }
  /**
   * Sets the configuration reference to the admin index.
   * @param indexReference the index configuration reference (null if disabled)
   */
  public void setAdminIndexReference(AsnIndexReference indexReference) {
    this.adminIndexRef = indexReference;
  }
  
  /** methods ================================================================= */
  
  /**
   * Adds an index configuration reference to the collection.
   * @param indexRef the index configuration reference to add
   */
  public void add(AsnIndexReference indexRef) {
    if (indexRef == null) {
      throw new IllegalArgumentException("The indexRef cannot be null.");
    } 
    this.indexReferences.add(indexRef);
  }
  
  /**
   * Returns the list of index references.
   * <br/>(should not contain the admin index reference)
   * @return the list of index references
   */
  public List<AsnIndexReference> values() {
    return this.indexReferences;
  }
    
}
