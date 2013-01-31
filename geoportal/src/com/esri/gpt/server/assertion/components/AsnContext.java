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
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.server.assertion.AsnFactory;

/**
 * Provides a context for assertion operations.
 */
public class AsnContext {
  
  /** instance variables ====================================================== */
  private AsnFactory        assertionFactory;
  private AsnAuthorizer     authorizer = new AsnAuthorizer();
  private MessageBroker     messageBroker;
  private AsnOperation      operation;
  private AsnResponse       operationResponse = new AsnResponse();
  private RequestContext    requestContext;
  private AsnRequestOptions requestOptions = new AsnRequestOptions();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnContext() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the assertion factory.
   * @return the assertion factory
   */
  public AsnFactory getAssertionFactory() {
    return this.assertionFactory;
  }
  /**
   * Sets the assertion factory.
   * @param factory the assertion factory
   */
  public void setAssertionFactory(AsnFactory factory) {
    this.assertionFactory = factory;
  }
  
  /**
   * Gets the authorizer.
   * @return the authorizer
   */
  public AsnAuthorizer getAuthorizer() {
    return this.authorizer;
  }
  /**
   * Sets the authorizer.
   * @param authorizer the authorizer
   */
  public void setAuthorizer(AsnAuthorizer authorizer) {
    this.authorizer = authorizer;
  }
  
  /**
   * Gets the resource bundle message broker.
   * @return the message broker (can be null)
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
   * Gets the active operation.
   * @return the active operation
   */
  public AsnOperation getOperation() {
    return this.operation;
  }
  /**
   * Sets the active operation.
   * @param operation the active operation
   */
  public void setOperation(AsnOperation operation) {
    this.operation = operation;
  }
    
  /**
   * Gets the operation response.
   * @return the operation response
   */
  public AsnResponse getOperationResponse() {
    return this.operationResponse;
  }
  /**
   * Sets the operation response.
   * @param response the operation response
   */
  public void setOperationResponse(AsnResponse response) {
    this.operationResponse = response;
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
   * Gets the request options.
   * @return the request options
   */
  public AsnRequestOptions getRequestOptions() {
    return this.requestOptions;
  }
  /**
   * Sets the request options.
   * @param requestOptions the request options
   */
  public void setRequestOptions(AsnRequestOptions requestOptions) {
    this.requestOptions = requestOptions;
  }
  
}
