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
package com.esri.gpt.catalog.search;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.esri.gpt.catalog.search.SearchParameterMap.Value;


import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.CswCatalog;
import com.esri.gpt.server.csw.client.CswCatalogCapabilities;


/**
 * 
 * 
 * The Class SearchFilterHarvestSites. Class started out as connecting the 
 * harvesting sites to the search UI.  Over time has evolved to facilitate
 * external search.
 * 
 */
@SuppressWarnings("serial")
public class SearchFilterHarvestSites implements ISearchFilterAuthURI { 

// class variables =============================================================
/** The Constant KEY_HARVEST_SITES_URL. */
private static final String KEY_HARVEST_SITE_URL = "KEY_HARVEST_SITES_URL";

/** The Constant KEY_HARVEST_SITE_NAME. */
private static final String KEY_HARVEST_SITE_NAME = "KEY_HARVEST_SITE_NAME";

/** The Constant KEY_HARVEST_SITE_ID. */
private static final String KEY_HARVEST_SITE_ID = "KEY_HARVEST_SITE_ID";

private static final String REST_KEY_HARVEST_SITE_ID_1 = "id";

private static final String REST_KEY_HARVEST_SITE_ID_2 = "rid";

private static final String KEY_PANEL_OPEN = "KEY_PANEL_OPEN";

private static final String KEY_DISTRIBUTED_RIDS = "KEY_DISTRIBUTED_RIDS";

private static final String KEY_URL_TOSEARCH = "KEY_URL_TOSEARCH";

/** The filter name **/
private static final String KEY_FILTERNAME = "KEY_FILTER_NAME";

// instance variables ==========================================================
/** The selected harvest site url. */
private String selectedHarvestSiteUrl;

/** The selected harvest site name. */
private String selectedHarvestSiteName;

/** The selected harvest site profile. */
private String selectedHarvestSiteProfile;

/** The selected harvest site post url. */
private String selectedHarvestSitePostUrl;

/** The selected harvest site get record url. */
private String selectedHarvestSiteGetRecordUrl;

/** The selected harvest site supoorts sptl qury. */
private String selectedHarvestSiteSupoortsSptlQury;

/** The selected harvest site supoorts ctp qury. */
private String selectedHarvestSiteSupoortsCtpQury;

/** The selected harvest site supoorts spt bnds. */
private String selectedHarvestSiteSupoortsSptBnds;

/** The selected harvest site password. */
private String selectedHarvestSitePassword;

/** The selected harvest site username. */
private String selectedHarvestSiteUsername;

/** The selected harvest site id. */
private String selectedHarvestSiteId;

/** The selected search site ids. */
private String selectedDistributedIds;

/** The selected Distribbuted names. */
private String selectedDistributedNames;

private boolean selectedHarvestSiteUsesAuth;

private boolean distributedPanelOpen;

/** The last search. */
private String searchUrl;

// properties ==================================================================



/**
 * Gets the search url.
 * 
 * @return the current searchurl (never null, trimmed)
 */
public String getSearchUrl() {
  return Val.chkStr(searchUrl);
}

/**
 * Sets the current search url.
 * 
 * @param searchUrl Search url
 */
public void setSearchUrl(String searchUrl ) {
  this.searchUrl = searchUrl;
}

/**
 * Gets the selected search site ids.
 * 
 * @return the selected search site ids (trimmed, never null)
 */
public String getSelectedDistributedIds() {
  
  return Val.chkStr(selectedDistributedIds);
}

/**
 * Sets the selected search site ids.
 * 
 * @param selectedSearchSiteIds the new selected search site ids
 */
public void setSelectedDistributedIds(String selectedSearchSiteIds) {
  this.selectedDistributedIds = selectedSearchSiteIds;
}

/**
 * Checks if is distributed panel open.
 * 
 * @return true, if is distributed panel open
 */
public boolean isDistributedPanelOpen() {
  return distributedPanelOpen;
}

public void setDistributedPanelOpen(boolean distributedPanelOpen) {
  this.distributedPanelOpen = distributedPanelOpen;
}

/**
 * Gets the selected distributed names.
 * 
 * @return the selected distributed names (trimmed, never null)
 */
public String getSelectedDistributedNames() {
  return Val.chkStr(selectedDistributedNames);
}

/**
 * Sets the selected distributed names.
 * 
 * @param selectedDistributedNames the new selected distributed names
 */
public void setSelectedDistributedNames(String selectedDistributedNames) {
  this.selectedDistributedNames = selectedDistributedNames;
}

/**
 * Gets the selected harvest site get record url.
 * 
 * @return the selected harvest site get record url (never null)
 */
public String getSelectedHarvestSiteGetRecordUrl() {
  return Val.chkStr(selectedHarvestSiteGetRecordUrl);

}

/**
 * Sets the selected harvest site get record url.
 * 
 * @param selectedHarvestSiteGetRecordUrl the new selected harvest site get record url
 */
public void setSelectedHarvestSiteGetRecordUrl(
    String selectedHarvestSiteGetRecordUrl) {
  this.selectedHarvestSiteGetRecordUrl = selectedHarvestSiteGetRecordUrl;
}

/**
 * Gets the selected harvest site post url.
 * 
 * @return the selected harvest site post url
 * 
 * @throws SearchException the search exception
 */
public String getSelectedHarvestSitePostUrl() throws SearchException {
  if(selectedHarvestSitePostUrl == null 
      || "".equals(selectedHarvestSitePostUrl.trim())){
    this.writePostGetUrl();
  }
  return Val.chkStr(selectedHarvestSitePostUrl);
}

/**
 * Sets the selected harvest site post url.
 * 
 * @param selectedHarvestSitePostUrl the new selected harvest site post url
 */
public void setSelectedHarvestSitePostUrl(String selectedHarvestSitePostUrl) {
  this.selectedHarvestSitePostUrl = selectedHarvestSitePostUrl;
}

/**
 * Gets the selected harvest site url.
 * 
 * @return the selected harvest site url (never null, trimmed)
 */
public String getSelectedHarvestSiteUrl() {
  return Val.chkStr(selectedHarvestSiteUrl);
}

/**
 * Sets the selected harvest site url.
 * 
 * @param selectedHarvestSiteUrl the new selected harvest site url
 */
public void setSelectedHarvestSiteUrl(String selectedHarvestSiteUrl) {
  if(this.selectedHarvestSiteUrl == null || 
      !this.selectedHarvestSiteUrl.equals(selectedHarvestSiteUrl)) {
    // resetting the selected harvest site post url since we
    // changed endpoints
    this.setSelectedHarvestSitePostUrl(null);
    this.setSelectedHarvestSiteGetRecordUrl(null);
  }
  this.selectedHarvestSiteUrl = selectedHarvestSiteUrl;
}


/**
 * Gets the selected harvest site name.
 * 
 * @return the selected harvest site name (trimmed, never null)
 */
public String getSelectedHarvestSiteName() {
  String siteName = Val.chkStr(selectedHarvestSiteName);
  if("".equals(siteName) && (
      this.getSelectedHarvestSiteId().equals(SearchEngineLocal.ID) ||
      this.getSelectedHarvestSiteId().equals(""))) {
    Map<String, String> attribs = 
      SearchConfig.getConfiguredInstance().getSearchFactoryRepos().get(
          SearchEngineLocal.ID);
    if(attribs == null) {
      return "";
    }
    siteName = attribs.get("labelResourceKey");
    com.esri.gpt.framework.jsf.MessageBroker bundle = 
      new com.esri.gpt.framework.jsf.MessageBroker();
    bundle.setBundleBaseName(
        com.esri.gpt.framework.jsf.MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
    siteName = bundle.retrieveMessage(siteName);
  }
  return Val.chkStr(siteName);
}

/**
 * Sets the selected harvest site name.
 * 
 * @param selectedHarvestSiteName the new selected harvest site name
 */
public void setSelectedHarvestSiteName(String selectedHarvestSiteName) {
  this.selectedHarvestSiteName = selectedHarvestSiteName;
}

/**
 * Gets the selected harvest site profile.
 * 
 * @return the selected harvest site profile
 */
public String getSelectedHarvestSiteProfile() {
  return Val.chkStr(selectedHarvestSiteProfile);
}

/**
 * Sets the selected harvest site profile.
 * 
 * @param selectedHarvestSiteProfile the new selected harvest site profile
 */
public void setSelectedHarvestSiteProfile(String selectedHarvestSiteProfile) {
  this.selectedHarvestSiteProfile = selectedHarvestSiteProfile;
}


/**
 * Gets the selected harvest site supoorts sptl qury.
 * 
 * @return the selected harvest site supoorts sptl qury
 */
public String getSelectedHarvestSiteSupoortsSptlQury() {
  return String.valueOf(Val.chkBool(selectedHarvestSiteSupoortsSptlQury, false));
}

/**
 * Sets the selected harvest site supoorts sptl qury.
 * 
 * @param selectedHarvestSiteSupoortsSptlQury the new selected harvest site supoorts sptl qury
 */
public void setSelectedHarvestSiteSupoortsSptlQury(
    String selectedHarvestSiteSupoortsSptlQury) {
  this.selectedHarvestSiteSupoortsSptlQury = selectedHarvestSiteSupoortsSptlQury;
}

/**
 * Gets the selected harvest site supoorts ctp qury.
 * 
 * @return the selected harvest site supoorts ctp qury
 */
public String getSelectedHarvestSiteSupoortsCtpQury() {
  return String.valueOf(Val.chkBool(selectedHarvestSiteSupoortsCtpQury, false));
}

/**
 * Sets the selected harvest site supoorts ctp qury.
 * 
 * @param selectedHarvestSiteSupoortsCtpQury the new selected harvest site supoorts ctp qury
 */
public void setSelectedHarvestSiteSupoortsCtpQury(
    String selectedHarvestSiteSupoortsCtpQury) {
  this.selectedHarvestSiteSupoortsCtpQury = selectedHarvestSiteSupoortsCtpQury;
}

/**
 * Gets the selected harvest site supoorts spt bnds.
 * 
 * @return the selected harvest site supoorts spt bnds
 */
public String getSelectedHarvestSiteSupoortsSptBnds() {
  return String.valueOf(Val.chkBool(selectedHarvestSiteSupoortsSptBnds, false));
}

/**
 * Sets the selected harvest site supoorts spt bnds.
 * 
 * @param selectedHarvestSiteSupoortsSptBnds the new selected harvest site supoorts spt bnds
 */
public void setSelectedHarvestSiteSupoortsSptBnds(
    String selectedHarvestSiteSupoortsSptBnds) {
  this.selectedHarvestSiteSupoortsSptBnds = selectedHarvestSiteSupoortsSptBnds;
}

/**
 * Gets the selected harvest site password.
 * 
 * @return the selected harvest site password
 */
public String getSelectedHarvestSitePassword() {
  return selectedHarvestSitePassword;
}

/**
 * Sets the selected harvest site password.
 * 
 * @param selectedHarvestSitePassword the new selected harvest site password
 */
public void setSelectedHarvestSitePassword(String selectedHarvestSitePassword) {
  this.selectedHarvestSitePassword = selectedHarvestSitePassword;
}

/**
 * Gets the selected harvest site username.
 * 
 * @return the selected harvest site username
 */
public String getSelectedHarvestSiteUsername() {
  return selectedHarvestSiteUsername;
}

/**
 * Sets the selected harvest site username.
 * 
 * @param selectedHarvestSiteUsername the new selected harvest site username
 */
public void setSelectedHarvestSiteUsername(String selectedHarvestSiteUsername) {
  this.selectedHarvestSiteUsername = selectedHarvestSiteUsername;
}

/**
 * Checks if is selected harvest site uses auth.
 * 
 * @return true, if is selected harvest site uses auth
 */
public boolean isSelectedHarvestSiteUsesAuth() {
  return selectedHarvestSiteUsesAuth;
}

/**
 * Sets the selected harvest site uses auth.
 * 
 * @param selectedHarvestSiteUsesAuth the new selected harvest site uses auth
 */
public void setSelectedHarvestSiteUsesAuth(boolean selectedHarvestSiteUsesAuth) {
  this.selectedHarvestSiteUsesAuth = selectedHarvestSiteUsesAuth;
}

/**
 * Gets the selected harvest site id.
 * 
 * @return the selected harvest site id (trimmed, never null, default = -1)
 */
public String getSelectedHarvestSiteId() {
 
  if(selectedHarvestSiteId == null || selectedHarvestSiteId.trim().equals("")) {
    this.setSelectedHarvestSiteId(SearchEngineLocal.ID);
  }
  return Val.chkStr(selectedHarvestSiteId);
}

/**
 * Sets the selected harvest site id.
 * 
 * @param selectedHarvestSiteId the new selected harvest site id
 */
public void setSelectedHarvestSiteId(String selectedHarvestSiteId) {
  if(this.selectedHarvestSiteId != selectedHarvestSiteId) {
    
    //this.setSelectedHarvestSiteName(null);
    //this.setSelectedHarvestSiteUrl(null);
    //this.setSelectedHarvestSitePostUrl(null);
    //this.setSelectedHarvestSiteId(null);
    //this.setSelectedHarvestSiteGetRecordUrl(null);
  }
  this.selectedHarvestSiteId = selectedHarvestSiteId;
}

/** 
 * Gets the parameters associated with the object
 * @return Returns the parameter map (never null)
 */
public SearchParameterMap getParams() {
  SearchParameterMap paramMap = new SearchParameterMap();
  paramMap.put(KEY_HARVEST_SITE_NAME, paramMap.new Value(
      this.getSelectedHarvestSiteName()));
  paramMap.put(KEY_HARVEST_SITE_URL, paramMap.new Value(
      this.getSelectedHarvestSiteUrl()));
  paramMap.put(KEY_HARVEST_SITE_ID, paramMap.new Value(
      this.getSelectedHarvestSiteId()));
  paramMap.put(KEY_DISTRIBUTED_RIDS, paramMap.new Value(
      this.getSelectedDistributedIds()));
  paramMap.put(KEY_PANEL_OPEN, paramMap.new Value(
      String.valueOf(this.isDistributedPanelOpen())));
  paramMap.put(KEY_URL_TOSEARCH, paramMap.new Value(
      this.getSearchUrl()));
 
  return paramMap;
}

/**
 * Checks for equality
 * @param obj Object to check
 * @return true if equals, false if not
 */
public boolean isEquals(Object obj) {
  if(!(obj instanceof SearchFilterHarvestSites)) {
    return false;
  }
  SearchFilterHarvestSites fObj = (SearchFilterHarvestSites) obj;
  
  return 
    fObj.getSelectedHarvestSiteName().equals(this.getSelectedHarvestSiteName())
    && fObj.getSelectedHarvestSiteId().equals(this.getSelectedHarvestSiteId());
}

/**
 * 
 */
public void reset() {
  
  this.setSelectedHarvestSiteName(null);
  this.setSelectedHarvestSiteUrl(null);
  this.setSelectedHarvestSitePostUrl(null);
  this.setSelectedHarvestSiteId(null);
  this.setSelectedHarvestSiteGetRecordUrl(null);
  this.setSelectedDistributedIds(null);
  this.setDistributedPanelOpen(false);
}

/**
 * @param parameterMap
 * @throws SearchException
 */
public void setParams(SearchParameterMap parameterMap) throws SearchException {
  
  Value val = parameterMap.get(KEY_HARVEST_SITE_NAME);
  if(val != null) {
    this.setSelectedHarvestSiteName(val.getParamValue());
  }
  val = parameterMap.get(KEY_HARVEST_SITE_URL);
  if(val != null) {
    this.setSelectedHarvestSiteUrl(val.getParamValue());
  }
  val = parameterMap.get(KEY_HARVEST_SITE_ID);
  if(val != null) {
    this.setSelectedHarvestSiteId(val.getParamValue());
  } else {
    val = parameterMap.get(REST_KEY_HARVEST_SITE_ID_1);
    if(val != null) {
      this.setSelectedHarvestSiteId(val.getParamValue());
    } else {
      val = parameterMap.get(REST_KEY_HARVEST_SITE_ID_2);
      if(val != null) {
        this.setSelectedHarvestSiteId(val.getParamValue());
      }
    }
  }
  val = parameterMap.get(KEY_DISTRIBUTED_RIDS);
  if(val != null) {
    this.setSelectedDistributedIds(val.getParamValue());
  }
  val = parameterMap.get(KEY_PANEL_OPEN);
  if(val != null) {
    this.setDistributedPanelOpen(Val.chkBool(val.getParamValue(), false));
  }
  val = parameterMap.get(KEY_URL_TOSEARCH);
  if(val != null) {
    this.setSearchUrl(Val.chkStr(val.getParamValue()));
  }
  
  
}

/**
 * @throws SearchException
 */
public void validate() throws SearchException {
    writePostGetUrl();
}

/**
 * Write post get url.
 * 
 * @throws SearchException the search exception
 */
protected void writePostGetUrl() throws SearchException {
  
  try {
    this.setSelectedHarvestSitePostUrl(null);
    CswCatalog cswCatalog = new CswCatalog(this.getSelectedHarvestSiteUrl(),
    this.getSelectedHarvestSiteName(), SearchConfig.getConfiguredInstance().getGptXslProfiles().getCswProfiles().getProfileById(this.getSelectedHarvestSiteProfile()));
    cswCatalog.connect();
    CswCatalogCapabilities cap = cswCatalog.getCapabilities();
    this.setSelectedHarvestSitePostUrl(cap.get_getRecordsPostURL());
    this.setSelectedHarvestSiteGetRecordUrl(cap.get_getRecordByIDGetURL());
    
  } catch (Exception e) {
    throw new SearchException("Could not get capabilties from url=: "
        + this.getSelectedHarvestSiteUrl() + " : "
        + e.getMessage(), e);
  }

} 

/**
 * Do get csw catalog.
 * 
 * @param url the url
 * @param name the name
 * @param profile the profile
 * 
 * @return the csw catalog
 */
protected CswCatalog doGetCswCatalog(String url, String name, String profile) throws SearchException {
  CswCatalog cswCatalog = new CswCatalog(this.getSelectedHarvestSiteUrl(),
      this.getSelectedHarvestSiteName(), SearchConfig.getConfiguredInstance().
             getGptXslProfiles().getCswProfiles().getProfileById(this.getSelectedHarvestSiteProfile()));
  return cswCatalog;
}

/**
 * Gets A url fo rthe search end point
 * @return The url associated with the search
 * @throws URISyntaxException
 */
public URI getSearchURI() throws URISyntaxException {
  String url = null;
  try {
    url = this.getSelectedHarvestSitePostUrl();
   return new URI(url);
  } catch (Exception e) {
    throw new URISyntaxException(url, "Could not make URI from postURL" +
    		" given: " + url + " : "
    		+ e.getMessage());
  }
}

/**
 * Reads users's password
 * @return the password
 */
public String readPassword() {
  return this.getSelectedHarvestSitePassword();
}

/**
 * Reads user's username
 * @return the username
 */
public String readUsername() {
  return this.getSelectedHarvestSiteUsername();
}

/**
 * Gets the jscript foreign sites.
 * 
 * @return the jscript foreign sites
 */
public String getJscriptForeignSites() {
  
  MessageBroker broker = new MessageBroker();
  broker.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
  StringBuffer json = new StringBuffer("[");
  com.esri.gpt.catalog.search.SearchConfig sConfig = 
    com.esri.gpt.catalog.search.SearchConfig.getConfiguredInstance();
  java.util.Map<String, java.util.Map<String, String>> 
  sfRepos = sConfig.getSearchFactoryRepos();
  java.util.Iterator<String> iter = sfRepos.keySet().iterator();
  boolean firstIter = false;
  while(iter != null && iter.hasNext()) {
    String key = iter.next();
    if(key == null) {
      continue;
    }
    java.util.Map<String, String> attribs = sfRepos.get(key);
    if(attribs == null) {
      continue;
    }
    String label = attribs.get("RESOURCEKEY");
    if(label == null || "".equals(label)) {
      label = attribs.get("LABELRESOURCEKEY");
      if(label == null || "".equals(label)) {
        continue;
      }
    }
    if(iter.hasNext() && firstIter) {
      json.append(",");
    }
    firstIter = true;
    json.append("{");
    label = broker.retrieveMessage(label);
    json.append("uuid: '" + 
        com.esri.gpt.framework.util.Val.escapeSingleQuotes(key) + "',");
    json.append("name: '" + 
        com.esri.gpt.framework.util.Val.escapeSingleQuotes(label) + "'");
    json.append("}");
    
  }  
  json.append("]");
  return json.toString();
}

}
