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
package com.esri.gpt.catalog.lucene;
import com.esri.gpt.framework.collection.StringSet;

/**
 * Represents the a virtual property associated with the execution of
 * a multi-field query parser.
 */
public class AnyTextProperty extends Storeable {
  
  /** instance variables ====================================================== */
  private StringSet namesToConsider = new StringSet();
    
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied name and a set of field names to consider.
   * @param name the property name
   * @param namesToConsider the set of field names to consider
   */
  public AnyTextProperty(String name, StringSet namesToConsider) {
    super(name);
    this.namesToConsider = namesToConsider;
  }
    
  /** class variables ========================================================= */
  
  /**
   * Gets the array of field names to in include within the multi-field query.
   * the field names (can be null or empty if not properly configured)
   */
  public String[] getFieldNames() {
    if (namesToConsider != null) {
      return namesToConsider.toArray(new String[0]);
    } else {
      return null;
    }
  }
      
}
