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
package com.esri.gpt.control.webharvest.client.arcims;

import com.esri.gpt.catalog.arcims.ImsService;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;

/**
 * Service info.
 */
class ArcImsInfo {

/** url */
private final String url;
/** service name */
private final String serviceName;
/** root folder */
private final String rootFolder;
/** user name */
private final String userName;
/** password */
private final String password;

/**
 * Creates instance of the info.
 * @param url url
 * @param serviceName service name
 * @param userName user name
 * @param password password
 */
public ArcImsInfo(String url, String serviceName, String rootFolder, String userName, String password) {
  this.url = Val.chkStr(url);
  this.serviceName = Val.chkStr(serviceName);
  this.rootFolder = Val.chkStr(rootFolder);
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
 * Creates new credentials.
 * @return credentials
 */
public UsernamePasswordCredentials newCredentials() {
  if (userName.length()>0 && password.length()>0) {
    return new UsernamePasswordCredentials(userName,password);
  } else {
    return null;
  }
}

/**
 * Creates new service.
 * @return service
 */
public ImsService newService() {
  ImsService service = new ImsService();
  service.setServerUrl(getUrl());
  service.setServiceName(serviceName);
  service.setTimeoutMillisecs(30000);
  return service;
}

/**
 * Gets root folder.
 * @return root folder
 */
public String getRootFolder() {
  return rootFolder;
}

@Override
public String toString() {
  return "{type: arcims, url: \"" +getUrl()+ "\", serviceName: \"" +serviceName+ "\"}";
}
}
