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
import com.esri.gpt.framework.collection.StringSet;

/**
 * Options associated with a CSW Transaction request.
 */
public class TransactionOptions {
  
  /** instance variables ====================================================== */
  private String             approvalStatus;
  private boolean            autoApprove = false;
  private StringSet          deletionIds = new StringSet();
  private String             publicationMethod;
  private String             requestId;
  private TransactionSummary summary = new TransactionSummary();
  private String             transactionType;
  private boolean            verbose = false;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public TransactionOptions() {
    super();
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the approval status (optional).
   * @return the approval status
   */
  public String getApprovalStatus() {
    return this.approvalStatus;
  }

  /**
   * Sets the approval status (optional).
   * @param status the approval status
   */
  public void setApprovalStatus(String status) {
    this.approvalStatus = status;
  }
  
  /**
   * Gets the status indicating if a new document should be automatically approved.
   * @return true if a new document should be automatically approved
   */
  public boolean getAutoApprove() {
    return this.autoApprove;
  }
  /**
   * Sets the status indicating if a new document should be automatically approved.
   * @param autoApprove true if a new document should be automatically approved
   */
  public void setAutoApprove(boolean autoApprove) {
    this.autoApprove = autoApprove;
  }
  
  /**
   * Gets the IDs to delete.
   * @return the IDs to delete
   */
  public StringSet getDeletionIDs() {
    return this.deletionIds;
  }
  /**
   * Sets the IDs to delete.
   * @param ids the IDs to delete
   */
  public void setDeletionIDs(StringSet ids) {
    this.deletionIds = ids;
  }
  
  /**
   * Gets the publication method (optional).
   * @return the publication method
   */
  public String getPublicationMethod() {
    return this.publicationMethod;
  }
  /**
   * Sets the publication method (optional).
   * @param method the publication method
   */
  public void setPublicationMethod(String method) {
    this.publicationMethod = method;
  }
  
  /**
   * Gets the request ID.
   * @return the request ID
   */
  public String getRequestId() {
    return this.requestId;
  }
  /**
   * Sets the request ID.
   * @param requestId the request ID
   */
  public void setRequestId(String requestId) {
    this.requestId = requestId;
  }
  
  /**
   * Gets the transaction summary.
   * @return the transaction summary
   */
  public TransactionSummary getSummary() {
    return this.summary;
  }
  /**
   * Sets the transaction summary.
   * @param summary the transaction summary
   */
  public void setSummary(TransactionSummary summary) {
    this.summary = summary;
  }
  
  /**
   * Gets the transaction type (Insert,Update,Delete).
   * @return the transaction type
   */
  public String getTransactionType() {
    return this.transactionType;
  }
  /**
   * Sets the transaction type (Insert,Update,Delete).
   * @param transactionType the transaction type
   */
  public void setTransactionType(String transactionType) {
    this.transactionType = transactionType;
  }
  
  /**
   * Gets the flag indicating a verbose response.
   * @return <code>true</code> for a verbose response
   */
  public boolean getVerboseResponse() {
    return this.verbose;
  }
  /**
   * Sets the flag indicating a verbose response.
   * @param verbose <code>true</code> for a verbose response
   */
  public void setVerboseResponse(boolean verbose) {
    this.verbose = verbose;
  }
  
}
