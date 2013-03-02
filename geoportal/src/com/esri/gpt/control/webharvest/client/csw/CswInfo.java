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
package com.esri.gpt.control.webharvest.client.csw;

import com.esri.gpt.control.webharvest.common.CommonInfo;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.CswCatalog;
import com.esri.gpt.server.csw.client.CswProfiles;
import java.io.IOException;

/**
 * CSW service info.
 */
class CswInfo extends CommonInfo {

/** CSW profiles */
private CswProfiles cswProfiles;
/** url */
private String url;
/** CSW profile name */
private String profileName;
/** user name */
private String userName;
/** password */
private String password;

/**
 * Creates service info.
 * @param cswProfiles CSW profiles
 * @param url url
 * @param profileName CSW profile name
 * @param userName user name
 * @param password password
 */
public CswInfo(CswProfiles cswProfiles, String url, String profileName, String userName, String password) {
  this.cswProfiles = cswProfiles;
  this.url  = Val.chkStr(url);
  this.profileName = Val.chkStr(profileName);
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
 * @return new credential provider
 */
public CredentialProvider newCredentialProvider() {
  if (userName.length()>0 && password.length()>0) {
    return new CredentialProvider(userName,password);
  } else {
    return null;
  }
}

/**
 * Gets Csw Client catalog.
 * @return catalog
 * @throws IOException if getting profile failed
 */
public CswCatalog newCatalog() throws IOException {
  CswCatalog cswCatalog = new CswCatalog();

  cswCatalog.setProfile(getProfile());
  cswCatalog.setUrl(getUrl());
  cswCatalog.setBatchHttpClient(getBatchHttpClient());

  return cswCatalog;
}

@Override
public String toString() {
  return "{type: csw, url: \"" +getUrl()+ "\", profileName: \"" +profileName+ "\"}";
}

/**
 * Gets Csw profile by id.
 * @return profile
 */
private com.esri.gpt.server.csw.client.CswProfile getProfile() {
  return cswProfiles.getProfileById(profileName);
}

}
