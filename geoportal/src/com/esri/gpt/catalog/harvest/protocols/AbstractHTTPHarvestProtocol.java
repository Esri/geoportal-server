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
package com.esri.gpt.catalog.harvest.protocols;

import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.util.Val;

/**
 * Abstract HTTP-based harvest protocol
 */
public abstract class AbstractHTTPHarvestProtocol extends HarvestProtocol {

// class variables =============================================================
// instance variables ==========================================================
  /** User name if required to login. */
  private String _userName = "";
  /** User password if required to login. */
  private String _userPassword = "";
// constructors ================================================================

// properties ==================================================================
  
  /**
   * Gets user name.
   * @return user name
   */
  public String getUserName() {
    return _userName;
  }

  /**
   * Sets user name.
   * @param userName user name
   */
  public void setUserName(String userName) {
    _userName = Val.chkStr(userName);
  }

  /**
   * Gets user password.
   * @return user password
   */
  public String getUserPassword() {
    return _userPassword;
  }

  /**
   * Sets user password.
   * @param userPassword user password
   */
  public void setUserPassword(String userPassword) {
    _userPassword = Val.chkStr(userPassword);
  }
// methods =====================================================================

  @Override
  public void ping(String url) throws Exception {
    HttpClientRequest httpRequest = new HttpClientRequest();
    httpRequest.setUrl(url);
    httpRequest.setCredentialProvider(getCredentialProvider());
    httpRequest.setContentHandler(new StringHandler());
    httpRequest.execute();
  }

  private CredentialProvider getCredentialProvider() {
    return (!getUserName().isEmpty() && !getUserPassword().isEmpty()? new CredentialProvider(getUserName(), getUserPassword()): null);
  }
}
