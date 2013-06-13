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
package com.esri.gpt.catalog.search;
import com.esri.gpt.control.georss.IFeedRecord;
import com.esri.gpt.control.georss.IFeedRecords;
import com.esri.gpt.framework.request.Records;

/**
 * The Class SearchResultRecords. Collection of search result records.
 */
@SuppressWarnings("serial")
public class SearchResultRecords extends Records<SearchResultRecord> {
  
  private OpenSearchProperties openSearchProperties;
    
  /** Default constructor. */
  public SearchResultRecords() {
    super();
  }

  public SearchResultRecords(IFeedRecords records) {
    super();
    for (IFeedRecord record: records) {
      add(new SearchResultRecord(record));
    }
  }
  /** properties ============================================================== */
  
  /**
   * Gets the open search properties associated with this record set.
   * @return the open search properties (can be null)
   */
  public OpenSearchProperties getOpenSearchProperties() {
    return this.openSearchProperties;
  }
  /**
   * Sets the open search properties associated with this record set.
   * @param properties the open search properties (can be null)
   */
  public void setOpenSearchProperties(OpenSearchProperties properties) {
    this.openSearchProperties = properties;
  }
      
  /** methods ================================================================= */

}
