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
package com.esri.gpt.catalog.publication;

import java.util.ArrayList;
import java.util.List;

import com.esri.gpt.catalog.schema.ValidationException;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

/**
 * Maintains status information associated with a processed record.
 */
public class ProcessedRecord {

  /** instance variables ====================================================== */
  private List<String> exceptions;
  private String       sourceUri;
  private StatusType   statusType;
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public ProcessedRecord() {}
  
  /** properties ============================================================== */
    
  /**
   * Sets the exception encountered during processing (if applicable).
   * <br/>This method will build the message list retrievable with getExceptions().
   * @param exception the processing exception
   * @param msgBroker the message broker for message resource lookup
   */
  public void setException(Throwable exception, MessageBroker msgBroker) {
    if (exception == null) {
      this.exceptions = null;
    } else {
      this.exceptions = new ArrayList<String>();
      String msg = Val.chkStr(exception.getMessage());
      if (msg.length() == 0) {
        msg = Val.chkStr(exception.toString());
      } 
      this.exceptions.add(msg);
      if (msgBroker != null) {
        if (exception instanceof ValidationException) {
          ValidationException ve = (ValidationException)exception;
          ve.getValidationErrors().buildMessages(msgBroker,this.exceptions,true);
        }
      }
    }
  }
  
  /**
   * Gets the list of exception messages encountered during processing.
   * <br/>Can be null, more than one message will typically indicate validation issues.
   * @return the list of exception messages
   */
  public List<String> getExceptions() {
    return this.exceptions;
  }
  
  /**
   * Sets the list of exception messages encountered during processing.
   * <br/>Can be null, more than one message will typically indicate validation issues.
   * @param messages the list of exception messages
   */
  public void setExceptions(List<String> messages) {
    this.exceptions = messages;
  }
  
  /**
   * Gets the source URI associated with the record.
   * @return the source URI
   */
  public String getSourceUri() {
    return this.sourceUri;
  }
  /**
   * Sets the source URI associated with the record.
   * @param uri the source URI
   */
  public void setSourceUri(String uri) {
    this.sourceUri = uri;
  }
  
  /**
   * Gets the processing status associated with the record.
   * @return the status type
   */
  public StatusType getStatusType() {
    return this.statusType;
  }
  /**
   * Sets the processing status associated with the record.
   * @param type the status type
   */
  public void setStatusType(StatusType type) {
    this.statusType = type;
  }
  
  /** enumerations ============================================================ */
  
  /**
   * The processing status type.
   */
  public enum StatusType {
    CREATED,
    DELETED,
    FAILED,
    PENDING,
    REPLACED,
    UNCHNAGED,
    VALIDATED;
  }
  
}
