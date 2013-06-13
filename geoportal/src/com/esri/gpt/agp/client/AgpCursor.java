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
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Cursor information associated with a search result.
 */
public class AgpCursor {
  
  /** instance variables ====================================================== */  
  private long   nextStart = -1;
  private int    num = 0;
  private String query;
  private long   start = 0;
  private long   total = 0;
    
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpCursor() {}
  
  /** properties ============================================================== */

  /**
   * Gets the start index for the next result page.
   * @return the start index for the next result page
   */
  public long getNextStart() {
    return this.nextStart;
  }
  /**
   * Sets the start index for the next result page.
   * @param nextStart the start index for the next result page
   */
  public void setNextStart(long nextStart) {
    this.nextStart = nextStart;
  }
  
  /**
   * Gets the maximum number of matches to return.
   * @return the maximum number of matches to return
   */
  public int getNum() {
    return this.num;
  }
  /**
   * Sets the maximum number of matches to return.
   * @param num the maximum number of matches to return
   */
  public void setNum(int num) {
    this.num = num;
  }

  /**
   * Gets the query that was executed.
   * @return the query that was executed
   */
  public String getQuery() {
    return this.query;
  }
  /**
   * Sets the query that was executed.
   * @param query the query that was executed
   */
  public void setQuery(String query) {
    this.query = query;
  }

  /**
   * Gets the start index for the result page.
   * @return the start index for the result page
   */
  public long getStart() {
    return this.start;
  }
  /**
   * Sets the start index for the result page.
   * @param start the start index for the result page
   */
  public void setStart(long start) {
    this.start = start;
  }

  /**
   * Gets the total number of hits.
   * @return the total number of hits
   */
  public long getTotal() {
    return this.total;
  }
  /**
   * Sets the total number of hits.
   * @param total the total number of hits
   */
  public void setTotal(long total) {
    this.total = total;
  }
  
  /** methods ================================================================= */
  
  /**
   * Parse the JSON response.
   * @param jsoResponse the JSON response
   * @throws Exception if an exception occurs
   */
  public void parseResponse(JSONObject jsoResponse) throws JSONException {
    String sProp;
    
    sProp = "query";
    if (jsoResponse.has(sProp) && (!jsoResponse.isNull(sProp))) {
      this.setQuery(jsoResponse.getString(sProp));        
    }
    sProp = "total";
    if (jsoResponse.has(sProp) && (!jsoResponse.isNull(sProp))) {
      this.setTotal(jsoResponse.getLong(sProp));
    }
    sProp = "start";
    if (jsoResponse.has(sProp) && (!jsoResponse.isNull(sProp))) {
      this.setStart(jsoResponse.getLong(sProp));
    }
    sProp = "num";
    if (jsoResponse.has(sProp) && (!jsoResponse.isNull(sProp))) {
      this.setNum(jsoResponse.getInt(sProp));
    }
    sProp = "nextStart";
    if (jsoResponse.has(sProp) && (!jsoResponse.isNull(sProp))) {
      this.setNextStart(jsoResponse.getLong(sProp));
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(this.getClass().getCanonicalName());
    sb.append(" {");
    sb.append("\n query=").append(this.getQuery());
    sb.append("\n total=").append(this.getTotal());
    sb.append(" start=").append(this.getStart());
    sb.append(" num=").append(this.getNum());
    sb.append(" nextStart=").append(this.getNextStart());
    sb.append("\n}");
    return sb.toString();
  }
  
  
}