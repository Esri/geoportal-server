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
package com.esri.gpt.control.cart;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Simple implementation of an item cart based upon an 
 * in-memory collection of ids.
 */
public class Cart {
  
  /** instance variables ====================================================== */
  private Map<String,String> items = new ConcurrentHashMap<String,String>();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public Cart() {}
  
  /** methods ================================================================= */
  
  /**
   * Adds a key to the collection.
   * @param key the key
   */
  public void add(String key) {
    this.items.put(key,key);
  }
  
  /**
   * Clears the collection.
   */
  public void clear() {
    this.items.clear();
  }
  
  /**
   * Adds a key to the collection.
   * @param key the key
   */
  public boolean containsKey(String key) {
    return this.items.containsKey(key);
  }
  
  /**
   * Returns the collection key set.
   * @return the key set
   */
  public Set<String> keySet() {
    return this.items.keySet();
  }
  
  /**
   * Removes a key from the collection.
   * @param key the key
   */
  public void remove(String key) {
    this.items.remove(key);
  }
  
  /**
   * The collection size.
   * @return the size.
   */
  public int size() {
    return this.items.size();
  }

}
