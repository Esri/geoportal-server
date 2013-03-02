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
package com.esri.gpt.control.webharvest.client.res;

import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;

/**
 * Resource info.
 */
class ResourceInfo {
/** url */
private String url;
/** user name */
private String userName;
/** password */
private String password;

/**
 * Creates instance of the service info.
 * @param url service URL
 * @param userName user name
 * @param password password
 */
public ResourceInfo(String url, String userName, String password) {
  this.url = Val.chkStr(url);
  this.userName = Val.chkStr(userName);
  this.password = Val.chkStr(password);
}

/**
 * Gets service URL.
 * @return service URL
 */
public String getUrl() {
  return url;
}

/**
 * Creates new credential provider.
 * @return credential provider
 */
public CredentialProvider newCredentialProvider() {
  if (userName.length() > 0 && password.length() > 0) {
    return new CredentialProvider(userName, password);
  } else {
    return null;
  }
}

/**
 * Creates new credentials.
 * @return credentials
 */
public UsernamePasswordCredentials newCredentials() {
  if (userName.length() > 0 && password.length() > 0) {
    return new UsernamePasswordCredentials(userName, password);
  } else {
    return null;
  }
}

@Override
public String toString() {
  return "{type: res, url: \"" +getUrl()+ "\"}";
}

}
