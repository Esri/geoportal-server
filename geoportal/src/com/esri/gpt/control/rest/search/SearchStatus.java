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
package com.esri.gpt.control.rest.search;
import java.sql.Timestamp;

import com.esri.gpt.framework.util.Val;

/**
 * Search status.
 */
public class SearchStatus {
  
  // class variables =========================================================
  
  /** The search completed successfully (= "completed") */
  public static String STATUSTYPE_COMPLETED = "completed";
  
  /** The search failed (= "failed") */
  public static String STATUSTYPE_FAILED = "failed";
  
  /** The search was stopped (= "stopped") */
  public static String STATUSTYPE_STOPPED = "stopped";
  
  /** The search is in progress (= "working") */
  public static String STATUSTYPE_WORKING = "working";
  
  /** Set when the search times out **/
  public static String STATUSTYPE_SEARCH_TIMEOUT = "searchTimeout";
  
  // instance variables ======================================================
  private Timestamp endTimestamp;
  private boolean   forceStop = false;
  private boolean   hadMatch = false;
  private long      hitCount = -1;
  private Timestamp startTimestamp;
  private String    statusType="";
  private String    rid = "";
  private String    message = "";
  
  // constructors ============================================================
  
  /**
   * Default constructor.
   */
  public SearchStatus() {}
  
  // properties ==============================================================
  
  /**
   * Gets the ending time stamp. 
   * @return the ending time stamp 
   */
  public Timestamp getEndTimestamp() {
    return this.endTimestamp;
  }
  /**
   * Sets the ending time stamp.
   * @param endTimestamp the ending time stamp 
   */
  public void setEndTimestamp(Timestamp endTimestamp) {
    this.endTimestamp = endTimestamp;
  }
  
  /**
   * Gets the flag indicating whether or not the process should be forceably stopped. 
    * @return <code>true</code> if the process should stopped
   */
  public boolean getForceStop() {
    return this.forceStop;
  }
  /**
   * Sets the flag indicating whether or not the process should be forceably stopped.
   * @param forceStop <code>true</code> if the process should stopped
   */
  public void setForceStop(boolean forceStop) {
    this.forceStop = forceStop;
  }
  
  /**
   * Gets the flag indicating whether or not the searchable endpoint had a match. 
    * @return <code>true</code> if there was a match 
   */
  public boolean getHadMatch() {
    return this.hadMatch;
  }
  /**
   * Sets the flag indicating whether or not the searchable endpoint had a match.
   * @param hadMatch <code>true</code> if there was a match
   */
  public void setHadMatch(boolean hadMatch) {
    this.hadMatch = hadMatch;
  }
  
  /**
   * Gets the number of hits. 
   * A count of <code>-1<code> indicates that a hit count is unavailable.
   * @return the number of hits 
   */
  public long getHitCount() {
    return this.hitCount;
  }
  /**
   * Sets the number of hits.
   * A count of <code>-1<code> indicates that a hit count is unavailable.
   * @param hitCount the number of hits
   */
  public void setHitCount(long hitCount) {
    this.hitCount = hitCount;
    if (this.hitCount > 0) {
      this.setHadMatch(true);
    } else {
      this.setHadMatch(false);
    }
  }
  
  /**
   * Gets the message.
   * 
   * @return the message (trimmed, never null)
   */
  public String getMessage() {
    return Val.chkStr(message);
  }

  /**
   * Sets the message.
   * 
   * @param message the new message
   */
  public void setMessage(String message) {
    this.message = message;
  }
  
  /**
   * Gets the rid.
   * 
   * @return the rid (trimmed, never null)
   */
  public String getRid() {
    return Val.chkStr(rid);
  }

  /**
   * Sets the rid.
   * 
   * @param rid the new rid
   */
  public void setRid(String rid) {
    this.rid = rid;
  }
  
  /**
   * Gets the starting time stamp. 
   * @return the starting time stamp 
   */
  public Timestamp getStartTimestamp() {
    return this.startTimestamp;
  }
  /**
   * Sets the starting time stamp.
   * @param startTimestamp the starting time stamp 
   */
  public void setStartTimestamp(Timestamp startTimestamp) {
    this.startTimestamp = startTimestamp;
  }
  
  /**
   * Gets the status type. 
   * @return the status type
   */
  public String getStatusType() {
    return this.statusType;
  }
  /**
   * Sets the status type.
   * @param statusType the status type 
   */
  public void setStatusType(String statusType) {
    this.statusType = statusType;
  }
  
  /**
   * Determines if the search is in a working state.
   * @return <code>true</code> if the search is working
   */
  public boolean isWorking() {
    return STATUSTYPE_WORKING.equalsIgnoreCase(getStatusType());
  }
  
}
