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
import com.esri.gpt.framework.collection.CaseInsensitiveMap;
import com.esri.gpt.framework.util.Val;

/**
 * Represents a map or simple RDF pairs, with a pair consisting of a predicate URI 
 * and a set of associated string values.
 */
class RDFPairs extends CaseInsensitiveMap<RDFPair> {
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public RDFPairs() {
    super(false);
  }
  
  /** methods ================================================================= */
  
  /**
   * Adds a predicate/value pair to the collection.
   * @param predicate the predicate URI
   * @param the literal value
   */
  public void addValue(String predicate, String value) {
    predicate = Val.chkStr(predicate);
    value = Val.chkStr(value);
    if ((predicate.length() > 0) && (value.length() > 0)) {
      RDFPair pair = this.get(predicate);
      if (pair == null) {
        pair = new RDFPair();
        pair.setPredicate(predicate);
        pair.getValues().add(value);
        this.put(pair.getPredicate(),pair);
        //System.err.println("******* "+predicate+"="+value);
      } else {
        pair.getValues().add(value);
      }
    }
  }

}
