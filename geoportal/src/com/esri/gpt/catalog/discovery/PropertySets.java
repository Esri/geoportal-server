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
package com.esri.gpt.catalog.discovery;

/** 
 * Stores collections of brief, summary and full property sets. 
 */
public class PropertySets extends DiscoveryComponent {
  
  /** instance variables ====================================================== */
  private AliasedDiscoverables allAliased = new AliasedDiscoverables();
  private Discoverables brief = new Discoverables();
  private Discoverables full = new Discoverables();
  private Discoverables summary = new Discoverables();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public PropertySets() {
    super();
  }
    
  /** properties ============================================================== */
  
  /** 
   * Gets the entire map of aliased discoverables. 
   * @return the map of aliased discoverables
   */
  public AliasedDiscoverables getAllAliased() {
    return allAliased;
  }
  
  /** 
   * Gets the brief discoverable set. 
   * @return the brief discoverable set
   */
  public Discoverables getBrief() {
    return brief;
  }
  
  /** 
   * Gets the full discoverable set. 
   * @return the full discoverable set
   */
  public Discoverables getFull() {
    return full;
  }
  
  /** 
   * Gets the summary discoverable set. 
   * @return the summary discoverable set
   */
  public Discoverables getSummary() {
    return summary;
  }
     
  /** methods ================================================================= */
    
}