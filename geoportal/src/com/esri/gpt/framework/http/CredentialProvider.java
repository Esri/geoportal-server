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
package com.esri.gpt.framework.http;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;

/**
 * Provides credentials for the execution of an HTTP client request.
 */
public class CredentialProvider {

  /** thread local instance =================================================== */
  private static ThreadLocal<CredentialProvider> threadLocalInstance = new ThreadLocal<CredentialProvider>() {
    protected CredentialProvider initialValue() {return null;}
  };
    
  /** instance variables ====================================================== */
  private String username;
  private String password;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied username and password.
   * @param username the username
   * @param password the password
   */
  public CredentialProvider(String username, String password) {
    this.setUsername(username);
    this.setPassword(password);
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the thread local instance of the credential provider.
   * @return the thread local instance (can be null)
   */
  public static CredentialProvider getThreadLocalInstance() {
    return threadLocalInstance.get();
  }
  
  /**
   * Gets the password.
   * @return the password
   */
  public String getPassword() {
    return this.password;
  }
  /**
   * Sets the password.
   * @param password the password
   */
  public void setPassword(String password) {
    this.password = password;
  }
  
  /**
   * Gets the username.
   * @return the username
   */
  public String getUsername() {
    return this.username;
  }
  /**
   * Sets the username.
   * @param username the username
   */
  public void setUsername(String username) {
    this.username = username;
  }
  
  /** methods ================================================================= */
  
  /**
   * Establishes a thread local instance of credentials based upon authorization
   * credentials found within the HTTP request header.
   * <p/>
   * The general pattern is to challenge the UI client (browser) to provide credentials for
   * accessing a remote server.
   * @param request the executing HTTP servlet request
   * @return the extablished thread local instance (null if authorization credentials were not found)
   */
  public static CredentialProvider establishThreadLocalInstance(HttpServletRequest request) {
    String sAuthorization = request.getHeader("Authorization");
    if (sAuthorization != null) {
      if (sAuthorization.startsWith("Basic ")) {
        sAuthorization = sAuthorization.substring(6);
        if (sAuthorization.length() > 0) {
          String sDecoded = new String(Base64.decodeBase64(sAuthorization.getBytes()));
          int nIdx = sDecoded.indexOf(':');
          if (nIdx > 0) {
            String user = sDecoded.substring(0,nIdx);
            String pwd = sDecoded.substring(nIdx+1);
            CredentialProvider creds = new CredentialProvider(user,pwd);
            threadLocalInstance.set(creds);
          }
        }
      }
    }
    return threadLocalInstance.get();
  }
  
}
