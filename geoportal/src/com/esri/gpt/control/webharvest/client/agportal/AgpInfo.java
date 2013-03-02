/*
 * See the NOTICE file distributed with
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
package com.esri.gpt.control.webharvest.client.agportal;

import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.util.Val;

/**
 * ArcGIS Portal info.
 * NOTE! This is EXPERIMENTAL feature. It might be removed at any time in the future.
 */
public class AgpInfo {

  /** url */
  private String url;
  /** user name */
  private String userName;
  /** password */
  private String password;
  
  /**
   * Creates instance of the info.
   * @param url URL to the ArcGIS Portal.
   * @param userName user name
   * @param password password
   */
  public AgpInfo(String url, String userName, String password) {
    this.url = Val.chkStr(url);
    this.userName = Val.chkStr(userName);
    this.password = Val.chkStr(password);
  }
  
  /**
   * Gets URL.
   * @return URL
   */
  public String getUrl() {
    return url;
  }
  
  /**
   * Gets user name.
   * @return user name
   */
  public String getUserName() {
    return userName;
  }
  
  /**
   * Gets password.
   * @return password;
   */
  public String getPassword() {
    return password;
  }

/**
 * Creates new credential provider.
 * @return new credential provider
 */
public CredentialProvider newCredentialProvider() {
  if (userName.length()>0 && password.length()>0) {
    return new CredentialProvider(userName,password);
  } else {
    return null;
  }
}

@Override
public String toString() {
  return "{type: agportal, url: \"" +getUrl()+ "\"}";
}
}
