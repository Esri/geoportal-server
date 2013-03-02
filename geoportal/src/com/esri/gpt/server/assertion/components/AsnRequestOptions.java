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
package com.esri.gpt.server.assertion.components;

/**
 * Options associated with an assertion request.
 */
public class AsnRequestOptions {
  
  /** instance variables ====================================================== */
  private String ipAddress;
  private int    maxRecords = 10;
  private int    maxRecordsThreshold = 500;
  private String predicate;
  private int    startRecord = 1;
  private String subject;
  private String value;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnRequestOptions() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the IP address associated with the request.
   * @return the IP address
   */
  public String getIPAddress() {
    return this.ipAddress;
  }
  /**
   * Sets the IP address associated with the request.
   * @param ipAddress the IP address
   */
  public void setIPAddress(String ipAddress) {
    this.ipAddress = ipAddress;
  }
  
  /**
   * Gets the maximum number of records to return.
   * <br/>Default = 10.
   * @return the maximum number of records to return
   */
  public int getMaxRecords() {
    return this.maxRecords;
  }
  /**
   * Sets the maximum number of records to return.
   * <br/>A value of zero or less will return no records (hit count only).
   * <br/>If the supplied value exceeds the threshold, the max records
   * will be set to the threshold.
   * @param maxRecords maximum number of records to return
   */
  public void setMaxRecords(int maxRecords) {
    this.maxRecords = maxRecords;
    if (this.maxRecords > this.getMaxRecordsThreshold()) {
      this.maxRecords = this.getMaxRecordsThreshold();
    }
  }
  
  /**
   * Gets the threshold for the maximum number of record to return.
   * <br/>Default = 500.
   * @return the maximum number of records threshhold
   */
  public int getMaxRecordsThreshold() {
    return this.maxRecordsThreshold;
  }
  /**
   * Sets the threshold for the maximum number of record to return.
   * @param maxRecordsThreshold the maximum number of records threshhold
   */
  public void setMaxRecordsThreshold(int maxRecordsThreshold) {
    this.maxRecordsThreshold = maxRecordsThreshold;
  }
  
  /**
   * Gets the predicate associated with the request.
   * @return the request predicate
   */
  public String getPredicate() {
    return this.predicate;
  }
  /**
   * Sets the predicate associated with the request.
   * @param predicate the request predicate
   */
  public void setPredicate(String predicate) {
    this.predicate = predicate;
  }
  
  /**
   * Gets the starting record.
   * @return the starting record
   */
  public int getStartRecord() {
    return startRecord;
  }
  /**
   * Sets the starting record.
   * <br/>If the supplied value is less that 1, the start record will be set to 1.
   * @param startRecord the starting record
   */
  public void setStartRecord(int startRecord) {
    this.startRecord = startRecord;
    if (this.startRecord < 1) this.startRecord = 1;
  }
  
  /**
   * Gets the subject associated with the request.
   * @return the request subject
   */
  public String getSubject() {
    return this.subject;
  }
  /**
   * Sets the subject associated with the request.
   * @param subject the request subject
   */
  public void setSubject(String subject) {
    this.subject = subject;
  }
  
  /**
   * Gets the value associated with the request predicate.
   * @return the request predicate value
   */
  public String getValue() {
    return this.value;
  }
  /**
   * Sets the value associated with the request predicate.
   * @param value the request predicate value
   */
  public void setValue(String value) {
    this.value = value;
  }
  
}
