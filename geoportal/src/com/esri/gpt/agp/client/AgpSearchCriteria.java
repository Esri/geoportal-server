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

/**
 * The criteria for a search request.
 */
public class AgpSearchCriteria {
  
  /** instance variables ====================================================== */
  private String bbox;
  private long   deepTotal = -1;
  private int    num = 10;
  private String q;
  private String sortField;
  private String sortOrder;
  private long   start = 1;
     
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpSearchCriteria() {
    super();
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the bounding box for a spatial search.
   * <br/>Defined as minx, miny, maxx, maxy. 
   * <br/>Search requires q, bbox or both. 
   * <br/>Document extent is assumed to be in the WGS84 geographic 
   * coordinate system.
   * @return the bounding box
   */
  public String getBBox() {
    return this.bbox;
  }
  /**
   * Sets the bounding box for a spatial search.
   * <br/>Defined as minx, miny, maxx, maxy. 
   * <br/>Search requires q, bbox or both. 
   * <br/>Document extent is assumed to be in the WGS84 geographic 
   * coordinate system.
   * @param bbox the bounding box
   */
  public void setBBox(String bbox) {
    this.bbox = bbox;
  }
  
  /**
   * Gets the maximum number of results to process for a deep search.
   * <br/>A deep search is a paged search.
   * @return the deep total
   */
  public long getDeepTotal() {
    return this.deepTotal;
  }
  /**
   * Sets the maximum number of results to process for a deep search.
   * <br/>A deep search is a paged search.
   * @param deepTotal the deep total
   */
  public void setDeepTotal(long deepTotal) {
    this.deepTotal = deepTotal;
  }
  
  /**
   * Gets the maximum number of matches to return.
   * <br/>The default value is 10 and the maximum allowed value is 100.
   * @return the maximum number of matches to return
   */
  public int getNum() {
    return this.num;
  }
  /**
   * Sets the maximum number of matches to return.
   * <br/>The default value is 10 and the maximum allowed value is 100.
   * @param num the maximum number of matches to return
   */
  public void setNum(int num) {
    this.num = num;
  }
  
  /**
   * Gets the query to execute.
   * @return the query to execute
   */
  public String getQ() {
    return this.q;
  }
  /**
   * Sets the query to execute.
   * @param q the query to execute
   */
  public void setQ(String q) {
    this.q = q;
  }
  
  /**
   * Gets the sort field.
   * <br/>You can also sort by multiple fields for an item, 
   * comma separated. The allowed sort field names are 
   * title, created, type, owner, avgRating, numRatings, numComments and numViews.
   * @return the query to execute
   */
  public String getSortField() {
    return this.sortField;
  }
  /**
   * Sets the sort field.
   * <br/>You can also sort by multiple fields for an item, 
   * comma separated. The allowed sort field names are 
   * title, created, type, owner, avgRating, numRatings, numComments and numViews.
   * @param sortField the sort field
   */
  public void setSortField(String sortField) {
    this.sortField = sortField;
  }
  
  /**
   * Gets the sort order.
   * <br/>Values: asc | desc (default is asc). 
   * @return the sort order
   */
  public String getSortOrder() {
    return this.sortOrder;
  }
  /**
   * Sets the sort order.
   * <br/>Values: asc | desc (default is asc). 
   * @param sortOrder the sort order
   */
  public void setSortOrder(String sortOrder) {
    this.sortOrder = sortOrder;
  }
  
  /**
   * Gets the number of the first entry in the result set response. 
   * <br/>The index number is 1-based, the default is 1. 
   * @return the starting index
   */
  public long getStart() {
    return this.start;
  }
  /**
   * Sets the number of the first entry in the result set response. 
   * <br/>The index number is 1-based, the default is 1. 
   * @param start the starting index
   */
  public void setStart(long start) {
    this.start = start;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends parameters to a URL request buffer.
   * @param parameters the URL request buffer
   */
  public void appendURLParameters(StringBuilder parameters) {
    AgpUtil.appendURLParameter(parameters,"q",this.getQ(),true);
    AgpUtil.appendURLParameter(parameters,"bbox",this.getBBox(),true);
    AgpUtil.appendURLParameter(parameters,"sortField",this.getSortField(),true);
    AgpUtil.appendURLParameter(parameters,"sortOrder",this.getSortOrder(),true);
    if (this.getStart() > 1) {
      AgpUtil.appendURLParameter(parameters,"start",""+this.getStart(),true);
    }
    if (this.getNum() != 10) {
      AgpUtil.appendURLParameter(parameters,"num",""+this.getNum(),true);
    }
  }
  
}