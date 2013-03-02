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
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.security.principal.Publisher;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds context properties and summary information for a resource processing request.
 */
public class ProcessingContext {
  
  /** instance variables ====================================================== */
  private HttpClientRequest     httpClient;
  private Exception             lastException;
  private MessageBroker         messageBroker;
  private int                   numberCreated = 0;
  private int                   numberDeleted = 0;
  private int                   numberFailed = 0;
  private int                   numberReplaced = 0;
  private int                   numberUnchanged = 0;
  private int                   numberValidated = 0;
  private List<ProcessedRecord> processedRecords = new ArrayList<ProcessedRecord>();
  private Publisher             publisher;
  private RequestContext        requestContext;
  private PublicationRecord     template;
  private boolean               validateOnly = false;
  private boolean               wasSingleSource = false;
    
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public ProcessingContext() {}
  
  /**
   * Constrcuts a processing context.
   * @param requestContext the underlying request context
   * @param publisher the publisher associated with the processing request
   * @param httpClient an HTTP client suitable for outbound requests
   * @param template the template associated with new documents (can be null)
   * @param validateOnly true if this is a validate only request
   */
  public ProcessingContext(RequestContext requestContext, Publisher publisher, 
      HttpClientRequest httpClient, PublicationRecord template, boolean validateOnly) {
    this.setRequestContext(requestContext);
    this.setPublisher(publisher);
    this.setHttpClient(httpClient);
    this.setTemplate(template);
    this.setValidateOnly(validateOnly);
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets an HTTP client suitable for outbound requests.
   * @return the HTTP client
   */
  public HttpClientRequest getHttpClient() {
    return this.httpClient;
  }
  /**
   * Sets an HTTP client suitable for outbound requests.
   * @param client the HTTP client
   */
  public void setHttpClient(HttpClientRequest client) {
    this.httpClient = client;
  }
  
  /**
   * Gets the last exception encountered (if applicable).
   * @return the last exception
   */
  public Exception getLastException() {
    return this.lastException;
  }
  /**
   * Sets the last exception encountered (if applicable).
   * @param e the last exception
   */
  public void setLastException(Exception e) {
    this.lastException = e;
  }
  
  /**
   * Gets the resource bundle message broker.
   * @return the message broker
   */
  public MessageBroker getMessageBroker() {
    return this.messageBroker;
  }
  
  /**
   * Sets the resource bundle message broker.
   * @param messageBroker the message broker
   */
  public void setMessageBroker(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;
  }
  
  /**
   * Gets the number of documents created.
   * @return the number created
   */
  public int getNumberCreated() {
    return this.numberCreated;
  }
  /**
   * Sets the number of documents created.
   * @param number the number created
   */
  public void setNumberCreated(int number) {
    this.numberCreated = number;
  }
  
  /**
   * Gets the number of documents deleted.
   * @return the number deleted
   */
  public int getNumberDeleted() {
    return this.numberDeleted;
  }
  /**
   * Sets the number of documents deleted.
   * @param number the number deleted
   */
  public void setNumberDeteted(int number) {
    this.numberDeleted = number;
  }
  
  /**
   * Gets the number of documents that failed.
   * @return the number that failed
   */
  public int getNumberFailed() {
    return this.numberFailed;
  }
  /**
   * Sets the number of documents that failed.
   * @param number the number that failed
   */
  public void setNumberFailed(int number) {
    this.numberFailed = number;
  }
  
  /**
   * Gets the number of documents replaced.
   * @return the number replaced
   */
  public int getNumberReplaced() {
    return this.numberReplaced;
  }
  /**
   * Sets the number of documents replaced.
   * @param number the number replaced
   */
  public void setNumberReplaced(int number) {
    this.numberReplaced = number;
  }
  
  /**
   * Gets the number of documents unchanged.
   * @return the number unchanged
   */
  public int getNumberUnchanged() {
    return this.numberUnchanged;
  }
  /**
   * Sets the number of documents unchanged.
   * @param number the number unchanged
   */
  public void setNumberUnchanged(int number) {
    this.numberUnchanged = number;
  }
  
  /**
   * Gets the number of documents validated.
   * @return the number validated
   */
  public int getNumberValidated() {
    return this.numberValidated;
  }
  /**
   * Sets the number of documents validated.
   * @param number the number validated
   */
  public void setNumberValidated(int number) {
    this.numberValidated = number;
  }
  
  /**
   * Gets the list of records processed during this request.
   * @return the list of processed records 
   */
  public List<ProcessedRecord> getProcessedRecords() {
    return this.processedRecords;
  }
  
  /**
   * Gets publisher associated with the processing request.
   * @return the publisher
   */
  public Publisher getPublisher() {
    return this.publisher;
  }
  /**
   * Sets publisher associated with the processing request.
   * @param publisher the publisher
   */
  public void setPublisher(Publisher publisher) {
    this.publisher = publisher;
  }
  
  /**
   * Gets the underlying request context.
   * @return the request context
   */
  public RequestContext getRequestContext() {
    return this.requestContext;
  }
  /**
   * Sets the underlying request context.
   * @param requestContext the request context
   */
  public void setRequestContext(RequestContext requestContext) {
    this.requestContext = requestContext;
  }
  
  /**
   * Gets the template associated with new documents.
   * @return the new document template
   */
  public PublicationRecord getTemplate() {
    return this.template;
  }
  /**
   * Sets the template associated with new documents.
   * @param template the new document template
   */
  public void setTemplate(PublicationRecord template) {
    this.template = template;
  }
  
  /**
   * Gets the status indicating if this is a validate only request.
   * @return true if this is a validate only request
   */
  public boolean getValidateOnly() {
    return this.validateOnly;
  }
  /**
   * Sets ets the status indicating if this is a validate only request.
   * @param wasValidateOnly true if this is a validate only request
   */
  public void setValidateOnly(boolean wasValidateOnly) {
    this.validateOnly = wasValidateOnly;
  }
  
  /**
   * Gets the status indicating if processing occurred against a single source document.
   * @return true if processing occurred against a single source document
   */
  public boolean getWasSingleSource() {
    return this.wasSingleSource;
  }
  /**
   * Sets the status indicating if processing occurred against a single source document.
   * @param wasSingleSource true if processing occurred against a single source document
   */
  public void setWasSingleSource(boolean wasSingleSource) {
    this.wasSingleSource = wasSingleSource;
  }
  
  /** methods ================================================================= */
  
  /**
   * Increments the number of documents created.
   */
  public void incrementNumberCreated() {
    this.numberCreated++;
  }
  
  /**
   * Increments the number of documents deleted.
   */
  public void incrementNumberDeleted() {
    this.numberDeleted++;
  }
  
  /**
   * Increments the number of documents that failed.
   */
  public void incrementNumberFailed() {
    this.numberFailed++;
  }
  
  /**
   * Increments the number of documents replaced.
   */
  public void incrementNumberReplaced() {
    this.numberReplaced++;
  }
  
  /**
   * Increments the number of documents unchanged.
   */
  public void incrementNumberUnchanged() {
    this.numberUnchanged++;
  }
  
  /**
   * Increments the number of documents validated.
   */
  public void incrementNumberValidated() {
    this.numberValidated++;
  }
  
}
