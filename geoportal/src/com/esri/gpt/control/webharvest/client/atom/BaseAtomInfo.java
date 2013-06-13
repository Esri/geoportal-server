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
package com.esri.gpt.control.webharvest.client.atom;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;

import com.esri.gpt.control.webharvest.common.CommonInfo;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.util.Val;

/**
 * Atom feed info.
 */
class BaseAtomInfo extends CommonInfo {
/** Date format. */
private static final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd");
/** url */
private String url;
/** user name */
private String userName;
/** password */
private String password;
/** totalResults */
private int totalResults=-1;
/** hitCountCollectorClassName */
private String hitCountCollectorClassName = "";
/** hitCountCollector */
private IHitCountCollector hitCountCollector;
/** entryProcessorClassName */
private String entryProcessorClassName = "";

/**
 * Creates instance of the service info.
 * @param url url
 * @param prefix prefix
 * @param set set
 * @param userName user name
 * @param password password
 * @throws Exception 
 */
public void initialize(String url, String userName, String password) {
  try {
		this.url = Val.chkStr(url);
		if(this.url.contains("?")){
			int idx = this.url.indexOf("?");
			if(idx > -1){				
				String queryString = Val.chkStr(this.url.substring(idx+1));
				if(queryString.length() > 0){
					String sParams = this.url.substring(idx+1);
					String[] params = sParams.split("&");
					String encodedParams = "";
					for (String param : params){
						String[] parts = param.split("=");
						if(parts.length ==2 && parts[1] != null){
							if(encodedParams.length() ==0){
								encodedParams = parts[0] + "=" + URLEncoder.encode(parts[1],"UTF-8");
							}else {
								encodedParams += "&" + parts[0] + "=" + URLEncoder.encode(parts[1],"UTF-8");
							}
						}
					}
					this.url = this.url.replace(queryString,encodedParams);
				}
			}
		}
	} catch (UnsupportedEncodingException e) {
		throw new IllegalArgumentException("Url encoding failed.");
	}
  this.userName = Val.chkStr(userName);
  this.password = Val.chkStr(password);
}

/**
 * Sets hitCountCollector.
 */
public void setHitCountCollector(IHitCountCollector hitCountCollector) {
	this.hitCountCollector = hitCountCollector;
}
/**
 * Gets hitCountCollector.
 * @return hitCountCollector
 */
public IHitCountCollector getHitCountCollector() {
	return hitCountCollector;
}
/**
 * Gets totalResults.
 * @return totalResults
 */
public int getTotalResults() {
	return totalResults;
}
/**
 * Gets service URL.
 * @return service URL
 */
public String getUrl() {
  return url;
}

/**
 * Gets entry processor class names
 * @return entryProcessorClassName
 */
public String getEntryProcessorClassName() {
	return entryProcessorClassName;
}

/**
 * Sets entry processor class name
 * @param entryProcessorClassName
 */
public void setEntryProcessorClassName(String entryProcessorClassName) {
	this.entryProcessorClassName = entryProcessorClassName;
}

/**
 * Gets hitCountCollectorClassName
 * @return hitCountCollectorClassName
 */
public String getHitCountCollectorClassName() {
	return hitCountCollectorClassName;
}

/**
 * Sets hitCountCollectorClassName
 * @param hitCountCollectorClassName
 */
public void setHitCountCollectorClassName(String hitCountCollectorClassName) {
	this.hitCountCollectorClassName = hitCountCollectorClassName;
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
 * Creates new URL to get metadata.
 * @param sourceUri source URI
 * @return URL to get metadata
 */
public String newReadMetadataUrl(String sourceUri) {
  return getUrl() +  sourceUri;
}

/**
 * Creates new URL to list ids.
 * @param resumptionToken resumption token
 * @param fromDate from date
 * @param toDate to date
 * @return URL to list ids
 */
public String newUrl(int start,int max) {
  StringBuilder sb = new StringBuilder();
  String url = getUrl();
  try {
		url = url.replaceFirst(URLEncoder.encode("{startIndex?}","UTF-8"), String.valueOf(start));	
    url = url.replaceFirst(URLEncoder.encode("{count?}","UTF-8"),  String.valueOf(max));
  } catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  sb.append(url);  
  return sb.toString();
}

/**
 * Sets totalResults
 * @param totalResults
 */
public void setTotalResults(int totalResults) {
	this.totalResults = totalResults;
}

@Override
public String toString() {
  return "{type: atom, url: \"" +getUrl()+ "\"}";
}
}
