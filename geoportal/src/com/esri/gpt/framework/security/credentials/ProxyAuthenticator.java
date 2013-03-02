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
package com.esri.gpt.framework.security.credentials;
import java.net.Authenticator;  
import java.net.InetAddress;
import java.net.PasswordAuthentication;

/**
 * Provides credentials for outbound HTTP requests requiring proxy authentication.
 */
public class ProxyAuthenticator extends Authenticator {
  
  /*
  1. Authenticator.setDefault(new ProxyAuthenticator("user", "password"));  
  2. System.setProperty("http.proxyHost", "proxy host");  
  3. System.setProperty("http.proxyPort", "port");  
  */

  /** instance variables ====================================================== */
  private String username;
  private String password;  

  /** constructors ============================================================ */
  
  /**
   * Constructs the authenticator.
   * @param username the username
   * @param password the password
   */
  public ProxyAuthenticator(String username, String password) {  
    this.username = username;  
    this.password = password;  
  }  
  
  /** methods ================================================================= */
  
  /**
   * Gets an instance of the password authentication credentials.
   * @return the password authentication credentials
   */
  protected PasswordAuthentication getPasswordAuthentication() {    
    boolean isProxy = Authenticator.RequestorType.PROXY.equals(this.getRequestorType());
    if (isProxy) {
      return new PasswordAuthentication(this.username,this.password.toCharArray());
    } else {
      return null;
    }
  }
  
  /**
   * Sets up the system default authenticator of outbound HTTP requests requiring
   * proxy authentication.
   * <br/>The default authenticator will only be set if:
   * <ul>
   *   <li>the trimmed length of the username is geater than zero</li>
   *   <li>the ength of the password is geater than zero</li>
   * </ul>
   * @param username the username
   * @param password the password
   */
  public static void setDefault(String username, String password) {
    if ((username != null) && (username.trim().length() > 0) && 
        (password != null) && (password.length() > 0)) {
      Authenticator.setDefault(new ProxyAuthenticator(username.trim(),password));
    }    
  }
  
}
