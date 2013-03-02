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
package com.esri.gpt.control.webharvest.client.oai;

import com.esri.gpt.control.webharvest.common.CommonInfo;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.util.Val;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * OAI service info.
 */
class OaiInfo extends CommonInfo {
/** Date format. */
private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
/** Verify string. */
private static final String VERIFYSTRING = "?verb=GetRecord&identifier=";
/** List identifiers string. */
private static final String LISTIDENTIFIERS = "?verb=ListIdentifiers";
/** Prefix string. */
private static final String PREFIXSTRING = "&metadataPrefix=";
/** Set string. */
private static final String SETSTRING = "&set=";
/** Resumption token string. */
private static final String RESUMPTIONTOKENSTRING = "&resumptionToken=";
/** From string. */
private static final String FROMSTRING = "&from=";
/** Until string. */
private static final String UNTILSTRING = "&until=";
/** url */
private String url;
/** prefix */
private String prefix;
/** set */
private String set;
/** user name */
private String userName;
/** password */
private String password;

/**
 * Creates instance of the service info.
 * @param url url
 * @param prefix prefix
 * @param set set
 * @param userName user name
 * @param password password
 */
public OaiInfo(String url, String prefix, String set, String userName, String password) {
  this.url = Val.chkStr(url);
  this.prefix = Val.chkStr(prefix);
  this.set = Val.chkStr(set);
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
 * Creates new URL to list ids.
 * @param resumptionToken resumption token
 * @param fromDate from date
 * @param toDate to date
 * @return URL to list ids
 */
public String newListIdsUrl(String resumptionToken, Date fromDate, Date toDate) {
  StringBuilder sb = new StringBuilder();
  sb.append(getUrl() + LISTIDENTIFIERS);
  if (resumptionToken==null || resumptionToken.length()==0) {
    sb.append(PREFIXSTRING + prefix);
    sb.append(set.length() > 0 ? SETSTRING + set : "");
    sb.append(fromDate != null ? FROMSTRING + DF.format(fromDate) : "");
    sb.append(toDate != null ? UNTILSTRING + DF.format(toDate) : "");
  } else {
    sb.append(RESUMPTIONTOKENSTRING + resumptionToken);
  }
  return sb.toString();
}

/**
 * Creates new URL to get metadata.
 * @param sourceUri source URI
 * @return URL to get metadata
 */
public String newReadMetadataUrl(String sourceUri) {
  return getUrl() + VERIFYSTRING + sourceUri + PREFIXSTRING + prefix;
}

@Override
public String toString() {
  return "{type: oai, url: \"" +getUrl()+ "\", prefix: \"" +prefix+ "\", set: \"" +set+"\"}";
}
}
