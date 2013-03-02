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
package com.esri.gpt.control.search;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.catalog.search.ASearchEngine;
import com.esri.gpt.catalog.search.RestUrlBuilder;
import com.esri.gpt.catalog.search.SearchConfig;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.catalog.search.SearchEngineFactory;
import com.esri.gpt.catalog.search.SearchEngineLocal;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchFilterHarvestSites;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
/**
 * 
 * The Class SearchHarvestSitesController.  9.3.1 new search controller.  
 * extends the 9.3 controller. Should be used under a faces Context.
 */
public class SearchHarvestSitesController extends SearchController {

// static variables ============================================================
/** The LOG. */
private static Logger LOG = Logger.getLogger(
    SearchHarvestSitesController.class.getName());

// instance variables ==========================================================

/** The full metadata url. */
private String fullMetadataUrl;

/** The search harvest sites. */
private SearchFilterHarvestSites searchHarvestSites;

/** The default site id. */
private String defaultSiteId;

/** The default site name. */
private String defaultSiteName;

// properties ==================================================================
/**
 * Gets the search filter harvest sites.
 * 
 * @return the search filter harvest sites
 * @throws SearchException 
 */
public SearchFilterHarvestSites getSearchFilterHarvestSites() 
  throws SearchException {
  if(searchHarvestSites == null) {
    this.setSearchFilterHarvestSites(new SearchFilterHarvestSites());
  }
  return searchHarvestSites;
}

/**
 * Sets the search filter harvest sites.
 * 
 * @param searchHarvestSites the new search filter harvest sites
 */
public  void setSearchFilterHarvestSites(
    SearchFilterHarvestSites searchHarvestSites) {
  this.searchHarvestSites = searchHarvestSites;
}


/**
 * Gets the full metadata url.
 * 
 * @return the full metadata url
 */
public String getFullMetadataUrl() {
  return Val.chkStr(this.fullMetadataUrl);
}

/**
 * Sets the full metadata url.
 * 
 * @param fullMetadataUrl the new full metadata url
 */
public void setFullMetadataUrl(String fullMetadataUrl) {
  this.fullMetadataUrl = fullMetadataUrl;
}

/**
 * Gets the default site id.
 * 
 * @return the default site id (trimmed, never null)
 */
public String getDefaultSiteId() {
  return Val.chkStr(defaultSiteId);
}

/**
 * Sets the default site id.
 * 
 * @param defaultSiteId the new default site id
 */
public void setDefaultSiteId(String defaultSiteId) {
  this.defaultSiteId = defaultSiteId;
}

/**
 * Gets the default site name.
 * 
 * @return the default site name (trimmed, never null)
 */
public String getDefaultSiteName() {
  return Val.chkStr(defaultSiteName);
}

/**
 * Sets the default site name.
 * 
 * @param defaultSiteName the new default site name
 */
public void setDefaultSiteName(String defaultSiteName) {
  this.defaultSiteName = defaultSiteName;
}

// methods =====================================================================

/**
 * Gets the search config.
 * 
 * @return the search config
 */
public SearchConfig getSearchConfig() {
  SearchConfig cfg = 
    extractRequestContext().getCatalogConfiguration().getSearchConfig();
  return cfg;
}

/**
 * Gets the checks for rest urls.
 * 
 * @return the checks for rest urls
 */
public boolean getHasRestUrls() {
  return this.getRestSearchRequestUrlGeorss().length() > 1;
}


/**
 * Process full metadata url.
 */
public void processFullMetadataUrl() {
  
  this.onExecutionPhaseStarted();
  String response = "";
  try {
    HttpServletRequest request = this.getContextBroker()
        .extractHttpServletRequest();
    String uuid = Val.chkStr(request.getParameter("uuid"));
    String harvestID = Val.chkStr(request.getParameter("id"));
    if("".equals(harvestID)) {
      harvestID = Val.chkStr(request.getParameter("rid"));
    }
    
    ASearchEngine engine = null;
    if("".equals(harvestID)) {
      engine = super.getSearchDao();
    } else {
    
      engine = SearchEngineFactory.createSearchEngine(
          new SearchCriteria(), new SearchResult(), 
          this.extractRequestContext(), harvestID, 
          this.getContextBroker().extractMessageBroker());
    }
    response = Val.chkStr(engine.getMetadataUrl(uuid));
    
  } catch (Exception e) {
    MessageBroker broker = this.extractMessageBroker();
    FacesMessage message = new FacesMessage();
    message.setSeverity(FacesMessage.SEVERITY_ERROR);
    message.setSummary(e.getMessage());
    message.setDetail(e.getMessage());
    broker.addMessage(message);
    broker.addErrorMessage(new SearchException(""));
    LOG.log(Level.WARNING, "Could not get Full Metadata Url", 
        e);
  } finally {
    this.onExecutionPhaseCompleted();
  }
  this.setFullMetadataUrl(response);
 
}

/**
 * overiding the de fault to add 9.3.1 actions
 * @param event Event to be processed
 * @param context 
 * @throws AbortProcessingException
 * @throws Exception
 */
@Override
protected void processSearchActions(ActionEvent event, RequestContext context) 
throws AbortProcessingException, Exception {
  context.setViewerExecutesJavascript(true);
  if(this.getEventType(event).equals(
      SearchEvents.Event.EVENT_EXECUTE_SEARCH.toString())) {
    // Search from another page thats not search ends up reset but on search text
    // carried on
    Object obj = event.getComponent().getAttributes().get("onSearchPage");
    if(obj == null || 
        (obj != null && (Val.chkBool(obj.toString(), false) == false))) {
      String text = 
        this.getSearchCriteria().getSearchFilterKeyword().getSearchText();
      this.getSearchCriteria().reset();
      this.getSearchCriteria().getSearchFilterKeyword().setSearchText(text);
      this.doSetDefaultSite(this.getSearchFilterHarvestSites());
    }
   // TM: Need to see how this affects the search but was making the ajax
   // search not work
   // this.getSearchCriteria().getSearchFilterPageCursor().setCurrentPage(1);
  }
  
  if(this.getEventType(event).equals(
      SearchEvents.Event.EVENT_RESET_SEARCH.name())) {
    this.getSearchCriteria().reset();
    this.getSearchResult().reset();
    this.doSetDefaultSite(this.getSearchFilterHarvestSites());
    return;
  } 

  // resource identifier uses "rid" to build preview link
  String rid = this.getSearchFilterHarvestSites().getSelectedHarvestSiteId();
  if(!SearchEngineLocal.ID.equals(rid)) {
    getContextBroker().extractHttpServletRequest().setAttribute("rid", rid);
  }

  super.processSearchActions(event, context);
  //this.setNavigationOutcome(null);
}

/**
 * Gets the search dao.
 * 
 * @return the search dao
 * 
 * @throws SearchException the search exception
 */
@Override
protected ASearchEngine getSearchDao() throws SearchException {
  try {
    String id =  this.getSearchFilterHarvestSites().getSelectedHarvestSiteId();
    if("".equals(id)) {
      id = SearchEngineLocal.ID;
    }
    return SearchEngineFactory.createSearchEngine(
        this.getSearchCriteria(), 
        this.getSearchResult(), 
        this.getContextBroker().extractRequestContext(),
        id,
        this.getContextBroker().extractMessageBroker());
    
  } catch (Exception e) {
    throw new SearchException("Could not get a csw search engine: " 
        + e.getMessage(), e);
  }
}

/**
 * Gets the rest search request url.
 * @param format the format
 * @return the rest search request url (can be null)
 */
@Override
protected String getRestSearchRequestUrl(String format) {
  String rid = "";
  try {
    rid = this.getSearchFilterHarvestSites().getSelectedHarvestSiteId();
    if(SearchEngineLocal.ID.equals(rid)) rid = "";
  } catch (Exception e) {
    LOG.log(Level.WARNING, "Could not get rid during creating rest urls", e);
  } 
  
 
  SearchCriteria criteria = this.getSearchCriteria();
  RequestContext context =  this.getContextBroker().extractRequestContext();
  HttpServletRequest request = this.getContextBroker().extractHttpServletRequest();
  
  // If request is coming from search page then we can parse the url and
  // come up with the url
  String queryString = Val.chkStr(request.getQueryString());
  if(!"".equals(queryString) && 
      queryString.toLowerCase().contains("f=searchpage")) {
    queryString = queryString.replaceAll("(?i)F=[^&]*", "f=" + format + "&");
    queryString = queryString.replaceAll("(?i)maxSearchTimeMilliSec=[^&]*", "");
    queryString = queryString.replaceAll("(?i)rids=[^&]*", "");
    queryString = queryString.replaceAll("(?i)ridname=[^&]*", "");
    queryString = queryString.replaceAll("(?i)rid=local[&]*", "");
    queryString = queryString.replaceAll("(?i)orderby=relevance[^&]*", "");
    queryString = queryString.replaceAll("(?i)[a-zA-Z0-9\\-]*=&", "");
    queryString = queryString.replaceAll("(?i)[a-zA-Z0-9\\-]*=$", "");
    queryString = queryString.replaceAll("&&&*", "&");
    queryString = queryString.replaceAll("&$", "");
    return request.getContextPath()+"/rest/find/document" + "?" + queryString;
  }
  
  MessageBroker messageBroker = this.getContextBroker().extractMessageBroker();
  RestUrlBuilder builder = RestUrlBuilder.newBuilder(context,request,messageBroker);
  String params = builder.buildParameters(criteria,format,rid);
  String url = request.getContextPath()+"/rest/find/document";
  if ((params != null) && (params.length() > 0)) {
    url += "?"+params;
  }
  return url;
}


/**
 * Prepares the view for a jsf page
 */
@Override
public void prepareView() {
  // TODO Auto-generated method stub
  super.prepareView();
}

/**
 * Do set default site if one has been specified.
 * 
 * @param sFilter the s filter
 */
private void doSetDefaultSite(SearchFilterHarvestSites sFilter) {
  if("".equals(this.getDefaultSiteId())) {
    return;
  }
  sFilter.setSelectedHarvestSiteId(this.getDefaultSiteId());
  sFilter.setSelectedHarvestSiteName(this.getDefaultSiteName());
   
}
/**
 * Gets the navigation outcome.
 * @return the navigation outcome
 */
public String getNavigationOutcome() {
  String nav = Val.chkStr(super.getNavigationOutcome());
  if("".equals(nav) || nav.equals(NAV_CRITERIA2RESULTS)) {
    FacesContextBroker broker = new FacesContextBroker();
    Object objIsAjax =            
        broker.getExternalContext().getRequestParameterMap().get("ajax");
    if(objIsAjax != null && Val.chkBool(objIsAjax.toString(), false)) {
      return "catalog.search.results.ajax";
    }
  }
  return nav;
}

public String getDoSearch() {
	return null;
}
}
