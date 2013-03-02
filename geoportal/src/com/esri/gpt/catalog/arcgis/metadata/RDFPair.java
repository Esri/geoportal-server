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
package com.esri.gpt.catalog.arcgis.metadata;
import com.esri.gpt.framework.collection.StringSet;

/**
 * Represents a simple RDF pair consisting of a predicate URI and a set of 
 * associated string values.
 */
class RDFPair {
  
  /** instance variables ====================================================== */
  private String    predicate;
  private StringSet values = new StringSet();
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public RDFPair() {}

  /** properties ============================================================== */
  
  /**
   * Gets the predicate URI.
   * @return the predicate URI
   */
  public String getPredicate() {
    return this.predicate;
  }
  /**
   * Sets the predicate URI.
   * @param predicate the predicate URI
   */
  public void setPredicate(String predicate) {
    this.predicate = predicate;
  }
  
  /**
   * Gets the literal value.
   * @return the literal value
   */
  public StringSet getValues() {
    return this.values;
  }
  /**
   * Sets the literal value.
   * @param value the literal value
   */
  public void setValues(StringSet values) {
    this.values = values;
  }

}
