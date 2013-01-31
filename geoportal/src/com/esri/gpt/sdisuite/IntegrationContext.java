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
package com.esri.gpt.sdisuite;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.User;

/**
 * Provides an integration context for sde.suite components.
 */
public abstract class IntegrationContext {
  
  /** instance variables ====================================================== */
  private StringAttributeMap config = new StringAttributeMap();
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public IntegrationContext() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the configuration parameters.
   * @return the configuration parameters
   */
  public StringAttributeMap getConfig() {
    return this.config;
  }
  /**
   * Sets the configuration parameters.
   * @param config the configuration parameters
   */
  public void setConfig(StringAttributeMap config) {
    this.config = config;
  }
  
  /** methods ================================================================= */
  
  /**
   * Authorizes access to a URL.
   * @param url the URL
   * @param user the active user
   * @param username the username 
   * @param password the password
   * @param licenseReturnUrl the return URL (called following license negotiation)
   * @return the integration response
   * @throws NotAuthorizedException if authorization for the supplied URL was denied
   * @throws Exception if a processing exception occurs
   */
  public abstract IntegrationResponse checkUrl(String url, 
                                               User user, 
                                               String username, 
                                               String password, 
                                               String licenseReturnUrl) 
  throws NotAuthorizedException, Exception;

  /**
   * Ensure that the active user has a valid SAML token.
   * <br/>This would typically be called following login.
   * @param user the active user
   * @throws Exception if an exception occurs
   */
  public abstract void ensureToken(User user) throws Exception;
  
  /**
   * Base64 encodes the SAML token associated with the active user.
   * <br/>This would typically be called prior to launching an sdi.suite component.
   * @param user the active user
   * @throws Exception if an exception occurs
   */
  public abstract String getBase64EncodedToken(User user) throws Exception;
  
  /**
   * Gets the license id associated with a license reference.
   * @param licenseReference the license reference
   * @return the license id
   * @throws Exception if an exception occurs
   */
  public String getLicenseId(String licenseReference) throws Exception {
    return null;
  }
  
  /**
   * Gets the username encoded within a SAML token.
   * <br/>This would typically be called when an external client executes a Geoportal
   * request (e.g. when the smart editor saves, a SAML token is passed as part of the 
   * SOAP request) 
   * @param samlToken the SAML token
   * @throws Exception if an exception occurs
   */
  public abstract String getUsernameFromSAMLToken(String samlToken) throws Exception;
  
  /**
   * Provides an opportunity to initialize additional user properties.
   * <br/>Called following login (?? following authentication ?? prior to ??).
   * @param user the active user
   * @throws Exception if an exception occurs
   */
  public abstract void initializeUser(User user) throws Exception;
  
  /**
   * Executed on web-app shutdown.
   * @throws Exception if an exception occurs
   */
  public abstract void shutdown() throws Exception;
  
}
