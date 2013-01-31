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

import com.esri.gpt.framework.util.Val;

/**
 * Maintains property information supporting OpenSearch response elements.
 */
public class OpenSearchProperties {
  
  /** instance variables ====================================================== */
  private String descriptionURL = "";
  private int    numberOfHits = 0;
  private int    recordsPerPage = 10;
  private String shortName = "";
  private int    startRecord = 1;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public OpenSearchProperties() {}

  /** properties ============================================================== */
  
  /**
   * Gets the URL for the open search service description.
   * @return the open search description URL
   */
  public String getDescriptionURL() {
    return this.descriptionURL;
  }
  /**
   * Sets the URL for the open search service description.
   * @param url the open search description URL
   */
  public void setDescriptionURL(String url) {
    this.descriptionURL = Val.chkStr(url);
  }
  
  /**
   * Gets the number of records hit by the query.
   * @return the number of hits
   */
  public int getNumberOfHits() {
    return this.numberOfHits;
  }
  /**
   * Sets the number of records hit by the query.
   * @param hits the number of hits
   */
  public void setNumberOfHits(int hits) {
    this.numberOfHits = hits;
  }
  
  /** 
   * Gets number of requested records per page.
   * @return number of records per page
   */
  public int getRecordsPerPage() {
    return this.recordsPerPage;
  }
  /**
   * Sets number of requested records per page.
   * @param recordsPerPage number of records per page
   */
  public void setRecordsPerPage(int recordsPerPage) {
    this.recordsPerPage = recordsPerPage;
  }
  
  /**
   * Gets the short name (i.e. title) associated with  the open search service.
   * @return the short name
   */
  public String getShortName() {
    return this.shortName;
  }
  /**
   * Sets the short name (i.e. title) associated with  the open search service.
   * @param name the short name
   */
  public void setShortName(String name) {
    this.shortName = Val.chkStr(name);
  }
  
  /**
   * Gets the starting record.
   * <br/>The record set starts at 1 not 0. 
   * @return the starting record
   */
  public int getStartRecord() {
    return this.startRecord;
  }
  /**
   * Sets the starting record.
   * <br/>The record set starts at 1 not 0. 
   * <br/>If the supplied value is less that 1, the start record will be set to 1.
   * @param startRecord the starting record
   */
  public void setStartRecord(int startRecord) {
    this.startRecord = startRecord;
    if (this.startRecord < 1) this.startRecord = 1;
  }
    
  /** methods ================================================================= */
    
}
