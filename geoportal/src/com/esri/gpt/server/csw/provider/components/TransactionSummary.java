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
package com.esri.gpt.server.csw.provider.components;

/**
 * Summary information associated with a CSW Transaction operation.
 */
public class TransactionSummary  {
    
  /** instance variables ====================================================== */
  private int totalDeleted = 0;
  private int totalInserted = 0;
  private int totalUpdated = 0;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public TransactionSummary() {}
    
  /** properties ============================================================== */
  
  /**
   * Gets the number of documents deleted.
   * @return the number deleted
   */
  public int getTotalDeleted() {
    return this.totalDeleted;
  }
  /**
   * Sets the number of documents deleted.
   * @param number the number deleted
   */
  public void setTotalDeleted(int number) {
    this.totalDeleted = number;
  }
  
  /**
   * Gets the number of documents inserted (created).
   * @return the number inserted
   */
  public int getTotalInserted() {
    return this.totalInserted;
  }
  /**
   * Sets the number of documents deleted.
   * @param number the number deleted
   */
  public void setTotalInserted(int number) {
    this.totalInserted = number;
  }
  
  /**
   * Gets the number of documents updated (replaced).
   * @return the number updated
   */
  public int getTotalUpdated() {
    return this.totalUpdated;
  }
  /**
   * Sets the number of documents deleted.
   * @param number the number deleted
   */
  public void setTotalUpdated(int number) {
    this.totalUpdated = number;
  }
  
}
