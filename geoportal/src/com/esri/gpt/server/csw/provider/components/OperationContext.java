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
import com.esri.gpt.framework.context.RequestContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds context information associated with an operation.
 */
public class OperationContext {
  
  /** instance variables ====================================================== */
  private Map<String,Object> additionalProperties = new HashMap<String,Object>();
  private String             operationName;
  private OperationResponse  operationResponse = new OperationResponse();
  private IProviderFactory   providerFactory;
  private RequestContext     requestContext;
  private RequestOptions     requestOptions = new RequestOptions();
  private ServiceProperties  serviceProperties;
  
  // TODO: sub operation name?
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public OperationContext() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the free form map of additional properties.
   * @return the additional properties
   */
  public Map<String,Object> getAdditionalProperties() {
    return this.additionalProperties;
  }
  /**
   * Sets the free form map of additional properties.
   * @param additionalProperties the additional properties
   */
  public void setAdditionalProperties(Map<String,Object> additionalProperties) {
    this.additionalProperties = additionalProperties;
  }
  
  /**
   * Gets the operation name.
   * @return the operation name
   */
  public String getOperationName() {
    return this.operationName;
  }
  /**
   * Sets the operation name.
   * @param operationName the operation name
   */
  public void setOperationName(String operationName) {
    this.operationName = operationName;
  }
    
  /**
   * Gets the operation response.
   * @return the operation response
   */
  public OperationResponse getOperationResponse() {
    return this.operationResponse;
  }
  /**
   * Sets the operation response.
   * @param operationResponse the operation response
   */
  public void setOperationResponse(OperationResponse operationResponse) {
    this.operationResponse = operationResponse;
  }
  
  /**
   * Gets the operation provider factory.
   * @return the operation provider factory
   */
  public IProviderFactory getProviderFactory() {
    return this.providerFactory;
  }
  /**
   * Sets the operation provider factory.
   * @param factory the operation provider factory
   */
  public void setProviderFactory(IProviderFactory factory) {
    this.providerFactory = factory;
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
  public RequestOptions getRequestOptions() {
    return this.requestOptions;
  }
  /**
   * Sets the request options.
   * @param requestOptions the request options
   */
  public void setRequestOptions(RequestOptions requestOptions) {
    this.requestOptions = requestOptions;
  }
        
  /**
   * Gets the service options.
   * @return the service options
   */
  public ServiceProperties getServiceProperties() {
    return this.serviceProperties;
  }
  /**
   * Sets the service properties.
   * @param serviceProperties the service properties
   */
  public void setServiceProperties(ServiceProperties serviceProperties) {
    this.serviceProperties = serviceProperties;
  }
  
}
