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
 * Defines the model for an executable query.
 */
public class DiscoveryQuery extends DiscoveryComponent {
      
  /** instance variables ====================================================== */
  private DiscoveryFilter filter = new DiscoveryFilter();
  private DiscoveryResult result = new DiscoveryResult();
  private Discoverables returnables;
  private Sortables sortables;
      
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public DiscoveryQuery() {
    super();
  }
        
  /** properties ============================================================== */
  
  /**
   * Gets the filter that constrains the query.
   * @return the filter
   */
  public DiscoveryFilter getFilter() {
    return filter;
  }
  /**
   * Sets the filter that constrains the query.
   * @param filter the filter
   */
  public void setFilter(DiscoveryFilter filter) {
    this.filter = filter;
  }

  /**
   * Gets the result of the query.
   * @return the result
   */
  public DiscoveryResult getResult() {
    return result;
  }
  
  /**
   * Gets the list of discoverable properties to return.
   * @return the list of discoverables to return
   */
  public Discoverables getReturnables() {
    return returnables;
  }
  /**
   * Sets the list of discoverable properties to return.
   * @param returnables the list of discoverables to return
   */
  public void setReturnables(Discoverables returnables) {
    this.returnables = returnables;
  }
  
  /**
   * Gets the list of properties describing the sort option for results.
   * @return the list of sortable properties
   */
  public Sortables getSortables() {
    return sortables;
  }
  /**
   * Sets the list of properties describing the sort option for results.
   * @param sortables the list of sortable properties
   */
  public void setSortables(Sortables sortables) {
    this.sortables = sortables;
  }
  
  /** methods ================================================================= */
    
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  @Override
  public void echo(StringBuffer sb) {
    sb.append(getClass().getSimpleName()).append(":");
    if (getFilter() != null) {
      getFilter().echo(sb.append("\n"));
    }
    if (getSortables() != null) {
      getSortables().echo(sb.append("\n"));
    }
  }

}
