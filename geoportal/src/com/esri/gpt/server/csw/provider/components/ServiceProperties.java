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
import java.util.HashMap;
import java.util.Map;

import com.esri.gpt.framework.util.Val;

/**
 * Describes the properties associated with a service.
 */
public class ServiceProperties {
  
  /** instance variables ====================================================== */
  private boolean             allowTransactions = true;
  private Map<String,Object>  additionalProperties = new HashMap<String,Object>();
  private String              cswSubContextPath;
  private String              httpContextPath;
  private String              resourceFilePrefix;
  private String              serviceName;
  private String              serviceVersion;
  private SupportedParameters supportedParameters = new SupportedParameters();
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public ServiceProperties() {
    super();
  }
  
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
   * Gets the status indicating if transactions are allowed at this end-point.
   * @return true if transactions are allowed
   */
  public boolean getAllowTransactions() {
    return this.allowTransactions;
  }
  /**
   * Sets the status indicating if transactions are allowed at this end-point.
   * @param allowTransactions true transactions are allowed
   */
  public void setAllowTransactions(boolean allowTransactions) {
    this.allowTransactions = allowTransactions;
  }
  
  /**
   * Convenience method to get the base CSW URL used for call back functions.
   * <p>
   * A CSW base URL will have the form:
   * <br/>http://host:port/context<i>/sub-context</i>
   * <br/>e.g. http://host:port/geoportal<i>/csw<i>
   * @return the CSW base URL
   */
  public String getCswBaseURL() {
    return Val.chkStr(this.getHttpContextPath())+Val.chkStr(this.getCswSubContextPath());
  }
  
  /**
   * Gets the HTTP sub-context path associated with the CSW service.
   * <p>
   * A CSW request URL will have the form:
   * <br/>http://host:port/context<i>/sub-context</i>
   * <br/>e.g. http://host:port/geoportal<i>/csw<i>
   * <br/>where <i>/csw<i> is the CSW sub-context path
   * @return the CSW sub-context path
   */
  public String getCswSubContextPath() {
    return this.cswSubContextPath;
  }
  /**
   * Sets the HTTP sub-context path associated with the CSW service.
   * <p>
   * A CSW base URL will have the form:
   * <br/>http://host:port/context<i>/sub-context</i>
   * <br/>e.g. http://host:port/geoportal<i>/csw<i>
   * <br/>where <i>/csw<i> is the CSW sub-context path
   * @param path the CSW sub-context path
   */
  public void setCswSubContextPath(String path) {
    this.cswSubContextPath = path;
  }
  
  /**
   * Gets the base context path associated with an active HTTP request.
   * <p>
   * The base context path has the form:
   * <br/><i>http://host:port/context</i>
   * @return the base context path
   */
  public String getHttpContextPath() {
    return this.httpContextPath;
  }
  /**
   * Sets the base context path associated with an active HTTP request.
   * <p>
   * The base context path has the form:
   * <br/><i>http://host:port/context</i>
   * @param path the base context path
   */
  public void setHttpContextPath(String path) {
    this.httpContextPath = path;
  }
    
  /**
   * Gets the path prefix for resource files.
   * @return the resource file path prefix
   */
  public String getResourceFilePrefix() {
    return this.resourceFilePrefix;
  }
  /**
   * Sets the path prefix for XML/XSLT resource files.
   * @param resourceFilePrefix the resource file path prefix
   */
  public void setResourceFilePrefix(String resourceFilePrefix) {
    this.resourceFilePrefix = resourceFilePrefix;
  }
  
  /**
   * Gets the service name.
   * @return the service name
   */
  public String getServiceName() {
    return this.serviceName;
  }
  /**
   * Sets the service name.
   * @param serviceName the service name
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }
  
  /**
   * Gets the service version.
   * @return the service version
   */
  public String getServiceVersion() {
    return this.serviceVersion;
  }
  /**
   * Sets the service version.
   * @param serviceVersion the service version
   */
  public void setServiceVersion(String serviceVersion) {
    this.serviceVersion = serviceVersion;
  }
  
  /**
   * Gets the supported parameters.
   * @return the supported parameters
   */
  public SupportedParameters getSupportedParameters() {
    return this.supportedParameters;
  }
  /**
   * Sets the supported parameters.
   * @param supportedParameters the supported parameters
   */
  public void setSupportedParameters(SupportedParameters supportedParameters) {
    this.supportedParameters = supportedParameters;
  }
  
  /**
   * Gets the supported values for a parameter.
   * @param parameterName the parameter name
   * @return the supported values (can be null)
   */
  public ISupportedValues getSupportedValues(String parameterName) {
    SupportedParameter p = this.getSupportedParameters().get(parameterName);
    if (p != null) {
      return p.getSupportedValues();
    }
    return null;
  }
    
}
