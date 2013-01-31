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
 * Represents the result of a query.
 */
public class DiscoveryResult extends DiscoveryComponent {
      
  /** instance variables ====================================================== */
  private int numberOfHits = 0;
  private DiscoveredRecords records = new DiscoveredRecords();
      
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public DiscoveryResult() {
    super();
  }
        
  /** properties ============================================================== */
  
  /**
   * Gets the number of records hit.
   * @return the number of hits
   */
  public int getNumberOfHits() {
    return numberOfHits;
  }
  /**
   * Sets the number of records hit.
   * @param hits the number of hits
   */
  public void setNumberOfHits(int hits) {
    this.numberOfHits = hits;
  }
  
  /**
   * Gets the records that matched the query.
   * @return the list of records
   */
  public DiscoveredRecords getRecords() {
    return records;
  }
  
  /** methods ================================================================= */
    
  /**
   * Appends property information for the component to a buffer.
   * @param sb the buffer to use when appending information
   */
  @Override
  public void echo(StringBuffer sb) {
    sb.append(getClass().getSimpleName()).append(":");
    sb.append(" hits=").append(getNumberOfHits()).append("");
    sb.append(" records=").append(getRecords().size()).append("");
    if (this.getRecords().size() <= 10) getRecords().echo(sb);
  }

}
