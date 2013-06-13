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
package com.esri.gpt.agp.client;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A collection of items.
 */
public class AgpItems {
    
  /** instance variables ====================================================== */
  private ArrayList<AgpItem> items = new ArrayList<AgpItem>();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpItems() {}
  
  /** methods ================================================================= */
  
  /**
   * Adds an item.
   * @param item the item
   */
  public void add(AgpItem item) {
    this.items.add(item);
  }
    
  /**
   * The collection members. 
   * @return the collection members
   */
  public Collection<AgpItem> values() {
    return this.items;
  }
  
  /**
   * The collection size.
   * @return the collection size
   */
  public int size() {
    return this.items.size();
  }
  
}