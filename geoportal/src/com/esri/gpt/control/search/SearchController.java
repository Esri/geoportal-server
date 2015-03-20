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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.catalog.schema.UiContext;
import com.esri.gpt.catalog.search.ASearchEngine;
import com.esri.gpt.catalog.search.ISearchFilter;
import com.esri.gpt.catalog.search.ISearchSaveRepository;
import com.esri.gpt.catalog.search.RestUrlBuilder;
import com.esri.gpt.catalog.search.SavedSearchCriteria;
import com.esri.gpt.catalog.search.SavedSearchCriterias;
import com.esri.gpt.catalog.search.SearchConfig;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.catalog.search.SearchEngineFactory;
import com.esri.gpt.catalog.search.SearchEngineLocal;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchFilterHarvestSites;
import com.esri.gpt.catalog.search.SearchFilterPagination;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.catalog.search.SearchSaveRpstryFactory;
import com.esri.gpt.control.georss.RestQueryServlet;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.jsf.components.UIPagination;
import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.util.Val;
/**
 * The Class SearchController.  Controller for search operations.  To be
 * used in JSF scope.
 */
public class SearchController extends BaseActionListener {

//class variables =============================================================
/** The Navigation rule name to results page *. */
static final String NAV_CRITERIA2RESULTS =  "catalog.search.results"; 

/** The Navigation rule name to view details page *. */
static final String NAV_RESULTS2VIEWDETAILS = "viewDetails";

/** The Constant NAV_2SEARCHCRITERIA. */
static final String NAV_2SEARCHCRITERIA = "catalog.search.home";

/** The class logger *. */
private static final Logger LOG = 
  Logger.getLogger(SearchController.class.getCanonicalName());

//instance variables ==========================================================

/** The binding panel for the view details page. */
private HtmlPanelGroup detailsPanelGroup;

/** resource URL */
private String resourceUrl = "";

/** Flag indicating whether or not result records should be expanded by default. */
private final boolean expandResultContent = false;

/** User's saved searches */
private List<SelectItem> savedSearches = new ArrayList<SelectItem>();

/** Style to toggle the display of saved searches. */
private String savedSearchesPanelStyle = "display: none";

/** The search criteria. */
private SearchCriteria searchCriteria;

/** The search event. */
private SearchEvents searchEvent;

/** The search result. */
private SearchResult searchResult;

/** Flag indicating whether or not a search was performed. */
private boolean wasSearched = false;

// constructors ================================================================

/** Default constructor. */
public SearchController() {
  detailsPanelGroup = new HtmlPanelGroup();
}

// properties  =================================================================
/**
 * Gets the bound HtmlPanelGroup for the details page panel.
 * <br/>This object is used during the Faces component binding process.
 * @return the bound HtmlPanelGroup
 */
public HtmlPanelGroup getDetailsPanelGroup() {
  return detailsPanelGroup;
}
/**
 * Sets the bound HtmlPanelGroup for the details page panel.
 * <br/>This object is used during the Faces component binding process.
 * @param htmlPanelGroup the bound HtmlPanelGroup
 */
public void setDetailsPanelGroup(HtmlPanelGroup htmlPanelGroup) {
  detailsPanelGroup = htmlPanelGroup; 
}

/**
 * Gets resource URL.
 * @return resource URL
 */
public String getResourceUrl() {
  return resourceUrl;
}

/**
 * Sets resource URL.
 * @param resourceUrl resource URL
 */
public void setResourceUrl(String resourceUrl) {
  this.resourceUrl = Val.chkStr(resourceUrl);
}

/**
 * Gets the style attribute for the display results page.
 * @return the style
 */
public String getDisplayResultsStyle() {
  if (!this.getWasSearched()) {
    return "display: none;";
  } else {
    return "";
  }
}

/**
 * Gets the style attribute for the expand result check-box.
 * @return the style (display none or empty)
 */
public String getExpandResultCheckboxStyle() {
  if (this.getSearchResult().getRecordSize() == 0) {
    return "display: none;";
  } else {
    return "";
  }
}

/**
 * Gets the style attribute for the content panel of a result record.
 * @return the style (display none or block)
 */
public String getExpandResultContentStyle() {
  if (getSearchCriteria().getExpandResultContent()) {
    return "display: block;";
  } else {
    return "display: none;";
  }
}

/**
 * Gets the collection of saved searches for this user.
 * @return the user's saved searches
 */
public List<SelectItem> getSavedSearches() {
  return savedSearches;
}

/**
 * Gets the style attribute for the saved searches panel.
 * @return the style
 * @deprecated since version 1
 */
public String getSavedSearchesPanelStyle() {
  return savedSearchesPanelStyle;
}
/**
 * Sets the style attribute for the saved searches panel.
 * @param style the style
 * @deprecated since version 1
 */
public void setSavedSearchesPanelStyle(String style) {
  this.savedSearchesPanelStyle = Val.chkStr(style);
}

/**
 * Gets the style attribute for the save search control.
 * @return the style
 */
public String getSaveSearchStyle() {
  SearchConfig cfg = extractRequestContext().getCatalogConfiguration().getSearchConfig();
  int nMax = cfg.getMaxSavedSearches();
  if (this.savedSearches.size() >= nMax) {
    return "display: none;";
  } else {
    return "";
  }
}

/**
 * Gets the result records as list  data model.
 * @return the result records as list model (never null)
 *
public ListDataModel getResultRecordsAsListModel() {
  ListDataModel model =  new ListDataModel();
  model.setWrappedData(this.getSearchResult().getRecords());
  return model;
}
*/

/**
 * Gets the search criteria.
 * @return the search criteria (never null)
 */
public SearchCriteria getSearchCriteria() {
  if (this.searchCriteria == null) {
    this.setSearchCriteria(new SearchCriteria());
  }
  return this.searchCriteria;
}
/**
 * Sets the search criteria.
 * @param searchCriteria the new search criteria
 */
public void setSearchCriteria(SearchCriteria searchCriteria) {
  this.searchCriteria = searchCriteria;
  
}

/**
 * Gets the search event.
 * @return the search event (never null)
 */
public SearchEvents getSearchEvent() {
  return (searchEvent == null)? new SearchEvents(): searchEvent;
}

/**
 * Gets the search result.
 * @return the search result (never null)
 */
public SearchResult getSearchResult() {
  if(searchResult == null) {
    this.setSearchResult(new SearchResult());
  }
  return this.searchResult;
}
/**
 * Sets the search result.
 * @param searchResult the new search result
 */
public void setSearchResult(SearchResult searchResult) {
  this.searchResult = searchResult;
}

/**
 * Gets the flag indicating whether or not a search was performed.
 * @return true if a search was performed.
 */
public boolean getWasSearched() {
  return wasSearched;
}
/**
 * Sets the flag indicating whether or not a search was performed.
 * @param wasSearched true if a search was performed
 */
public void setWasSearched(boolean wasSearched) {
  this.wasSearched = wasSearched;
}

// methods =====================================================================

/**
 * Loads a user's saved searches.
 * @param requestContext the active request context
 * @return the saved searches
 * @throws SearchException if an exception occurs
 */
private List<SelectItem> loadSavedSearches(RequestContext requestContext) 
  throws SearchException {
  List<SelectItem> list = new ArrayList<SelectItem>();
  ISearchSaveRepository saveRpstry = SearchSaveRpstryFactory.getSearchSaveRepository();
  SavedSearchCriterias savedCriterias = saveRpstry.getSavedList(requestContext.getUser());
  for(SavedSearchCriteria savedCriteria: savedCriterias){
    SelectItem selectItem = new SelectItem();
    selectItem.setValue(savedCriteria.getId());
    selectItem.setLabel(savedCriteria.getName());
    list.add(selectItem);
  }
  return list;
}

/**
 * Fired at the start of the view phase for the search page.
 */
public void prepareView() {
  //if (!this.getWasSearched()) {
  if (false) {
    try {
      onPrepareViewStarted();
      HttpServletRequest request = getContextBroker().extractHttpServletRequest();
      this.getSearchResult().reset();
      doSearch(getSearchCriteria().getSearchFilterPageCursor().getCurrentPage(),true);
    } catch (Throwable t) {
      getLogger().log(Level.SEVERE,"Exception raised.",t);
    } finally {
      onPrepareViewCompleted();
    }  
  }
}

/**
 * Main actionListener method for this class.  Called by superClass
 * 
 * @param event The event information
 * @param context Request Context Information
 * 
 * @throws AbortProcessingException Exception on error
 * @throws Exception Exception on error
 */
@Override
public void processSubAction(ActionEvent event, RequestContext context) 
throws AbortProcessingException, Exception {
  try {
    processSearchActions(event, context);
  } catch (Exception e) {

    boolean rethrowExcep = false;
    String strMessage = e.getMessage();
    if( e instanceof SearchException) {
      SearchException searchException = (SearchException) e;
      if(searchException.getHasUserMessage()) {
        LOG.log(Level.FINER, strMessage, e);
        rethrowExcep = false;
      }         
      rethrowExcep = true;
    }
    else {
      strMessage = e.getMessage();
      rethrowExcep = true;
    }

    MessageBroker broker = this.extractMessageBroker();
    if(broker != null) {
      FacesMessage message = new FacesMessage();
      message.setSummary(strMessage);
      message.setSeverity(FacesMessage.SEVERITY_ERROR);
      broker.addMessage(message);


    }
    if(rethrowExcep) {
      throw e;
    }
  }
}

/**
 * Does process request parameters.
 * It is used to process 'uuid' parameter to fetch metadata details.
 * @return empty string
 */
@SuppressWarnings("unchecked")
public String processRequestParams() {

  try {

    // start view preparation phase
    RequestContext context = onPrepareViewStarted();
    HttpServletRequest request = getContextBroker().extractHttpServletRequest();
    Map parameterMap = request.getParameterMap();
    Object url =  parameterMap.get("catalog");

    String catalogUrl = null;
    if(url instanceof String[] && ((String[])url).length > 0 ) {
      //catalogUrl = url.toString().trim();
      catalogUrl = ((String[])url)[0].trim();
    }

    if (parameterMap.containsKey("uuid")) {
      Object oUuid = parameterMap.get("uuid");
      if (oUuid instanceof String[] && ((String[])oUuid).length>0) {
        doViewMetadataDetails(context,((String[])oUuid)[0], catalogUrl);
      }
    }

  } catch (Throwable t) {
    handleException(t);
  } finally {
    onPrepareViewCompleted();
  }

  return "";
}

/**
 * ActionListener method
 * 
 * @param event The event information
 * @param context Request Context Information
 * 
 * @throws AbortProcessingException Exception on error
 * @throws Exception Exception on error
 */
@SuppressWarnings("unchecked")
protected void processSearchActions(ActionEvent event, RequestContext context) 
throws AbortProcessingException, Exception {
  
  // Actions will have to set the next navigation
  this.setNavigationOutcome(null);
  this.setSavedSearchesPanelStyle("display: none;");
  
  String eventType = getEventType(event);

  // create search URL builder
  HttpServletRequest request = getContextBroker().extractHttpServletRequest();

  LOG.log(Level.FINE, "Search Event type = {0}", eventType);
  if(eventType == null) {

    throw new  
    SearchException("Controller could not determine type of event passed");

  } else if(eventType.equals(
      SearchEvents.Event.EVENT_GOTOPAGE.name())) { 

    Integer goToPage = Integer.MIN_VALUE;
    goToPage = (Integer)event.getComponent().getAttributes()
    .get(UIPagination.PageEvents.goToPage.name());
    
    LOG.log(Level.FINE, "Going to page {0}", goToPage);
    this.doSearch(goToPage, false);

  } else if(eventType.equals(
      SearchEvents.Event.EVENT_MODIFYSEARCHCRITERIA.name())) {

    LOG.fine("Performing Modifying of Search");
    this.setNavigationOutcome(NAV_2SEARCHCRITERIA);

  } else if (eventType.equals(
      SearchEvents.Event.EVENT_NEWSEARCHCRITERIA.name())) {

    LOG.fine("going to search criteria and resetting");
    this.getSearchCriteria().reset();
    this.setNavigationOutcome(NAV_2SEARCHCRITERIA);

  } /*else if(eventType.equals(SearchEvents.Event.EVENT_VIEWMD_DETAILS.name())) {

    FacesContextBroker facesBroker = new FacesContextBroker();
    Map requestMap = facesBroker.getExternalContext().getRequestParameterMap();
    String uuid = (String) requestMap.get(SearchEvents.Event.PARAM_UUID);
    LOG.fine("Viewing Summary of "+ uuid);
    doViewMetadataDetails(context,uuid);

  }*/else if (eventType.equals(SearchEvents.Event.EVENT_EXECUTE_SEARCH.name())) {
    LOG.fine("Initiating new search");
    
    // extra params
    Map<String,String> extraMap = new HashMap<String,String>();
    context.getObjectMap().put(RestQueryServlet.EXTRA_REST_ARGS_MAP, extraMap);
    if (extraMap.get(RestQueryServlet.PARAM_KEY_SHOW_RELATIVE_URLS) == null) {
      extraMap.put(RestQueryServlet.PARAM_KEY_SHOW_RELATIVE_URLS, "true");
    }
    extraMap.put(RestQueryServlet.PARAM_KEY_IS_JSFREQUEST, "true");
    if (request.getScheme().toLowerCase().equals("https")
        && extraMap.get(RestQueryServlet.PARAM_KEY_SHOW_THUMBNAIL) == null) {
      String agent = request.getHeader("user-agent");
      if (agent != null && agent.toLowerCase().indexOf("msie") > -1) {
        extraMap.put(RestQueryServlet.PARAM_KEY_SHOW_THUMBNAIL, "false");
      }
    }
    
    this.getSearchResult().reset();
    doSearch(1, true);
  } else if(eventType.equals(SearchEvents.Event.EVENT_REDOSEARCH.name())) {
    LOG.fine("Redoing search in session");
    doSearch(-1, false);

  } else if (eventType.equals(SearchEvents.Event.EVENT_MYSEARCHES.name())){    
    LOG.info("Loading my searches");
    this.setSavedSearchesPanelStyle("");
    savedSearches = loadSavedSearches(context);
    
  } else if (eventType.equals(SearchEvents.Event.EVENT_SAVESEARCH.name())) { 
    LOG.info("Saving Search Criteria");
    try {
      doSave();   
    } finally {
      this.setSavedSearchesPanelStyle("");
      savedSearches = loadSavedSearches(context);
      //redoSearch(urlBuilder, event, false);
    }
    
    
  } else if (eventType.equals(SearchEvents.Event.EVENT_LOADSAVEDSEARCH.name())){    
    LOG.info("Loading Search Criteria");
    boolean success = false;
    try {
      FacesContextBroker facesBroker = new FacesContextBroker();
      Map requestMap = facesBroker.getExternalContext().getRequestParameterMap();
      String searchId = (String) 
      requestMap.get(SearchEvents.Event.PARAM_UUID);
      doLoad(searchId);  
      success = true;
      SearchCriteria searchCriteria = this.getSearchCriteria();
      searchCriteria.getSearchFilterPageCursor().setCurrentPage(1);
      this.getSearchResult().reset();
      for(ISearchFilter iSearchFilter 
          : searchCriteria.getMiscelleniousFilters()) {
        if(iSearchFilter instanceof SearchFilterHarvestSites) {
          SearchFilterHarvestSites sfHvSites = 
            (SearchFilterHarvestSites) iSearchFilter;
          try {
            String url = sfHvSites.getSearchUrl();
            if (!url.equals("")) {
              url = url.replaceAll("(?i)F=[^&]*", "f=searchpage"  );
              facesBroker.getExternalContext().redirect(url);
              FacesContext facesContext = facesBroker.getFacesContext();
              context.onExecutionPhaseCompleted();
              facesContext.responseComplete();
              return;
            }
          } catch (MalformedURLException ex) {
            // Not a url, use the other workflow
          }
        }
      }
      
    } finally {
      // either redo current search if unsuccessful, or do new loaded search
      // if successful
      redoSearch(event, success);
    }
    
  } else if(eventType.equals(SearchEvents.Event.EVENT_DELTESAVEDSEARCH.name())){
    LOG.info("Deleting Search Criteria");
    FacesContextBroker facesBroker = new FacesContextBroker();
    Map requestMap = facesBroker.getExternalContext().getRequestParameterMap();
    try {
      String searchId = (String) 
      requestMap.get(SearchEvents.Event.PARAM_UUID);
      ISearchSaveRepository searchSaveRepository = 
        SearchSaveRpstryFactory.getSearchSaveRepository();
      searchSaveRepository.delete(searchId, 
          this.extractRequestContext().getUser());
    } finally {
      this.setSavedSearchesPanelStyle("");
      savedSearches = loadSavedSearches(context);
      
      // Re-Execute search since results not in session
      //redoSearch(urlBuilder, event, false);
    }
  }
}


/**
 * Gets the event type.
 * 
 * @param event the event
 * 
 * @return the event type
 */
@SuppressWarnings("unchecked")
protected String getEventType(ActionEvent event) {
  UIComponent comp = event.getComponent();
  Map attributeMap = comp.getAttributes();
  Map requestMap = null;

  String eventType = Val.chkStr((String) attributeMap.get(SearchEvents.Event.EVENT.name()));
  if (eventType.length() == 0) {
    FacesContextBroker facesBroker = new FacesContextBroker();
    requestMap = facesBroker.getExternalContext().getRequestParameterMap();
    Object obj = (requestMap != null) ?
    requestMap.get(SearchEvents.Event.EVENT) : null;
    if (obj != null) {
      eventType = Val.chkStr(obj.toString());
    }
  }
  if (eventType.length() == 0) {
    Integer goToPage = Integer.MIN_VALUE;
    goToPage = 
      (Integer)attributeMap.get(UIPagination.PageEvents.goToPage.name());
    if(goToPage != null) {
      eventType = SearchEvents.Event.EVENT_GOTOPAGE.name();

    }
  }
  return eventType;
}

/**
 * Do save the search.
 * 
 * @throws SearchException the search exception
 */
private void doSave() throws SearchException {
  LOG.info("Event: Performing Save");
  SearchCriteria criteria = this.getSearchCriteria();
  //if(criteria.getSavedSearchList().size() 
  //    >= SearchConfig.getConfiguredInstance().getMaxSavedSearches()) {
  //  throw new SearchException("catalog.search.error.maxSavedSearchesReached");
  //}
  if (LOG.isLoggable(Level.FINE)) {
    LOG.log(Level.FINE, "Search Criteria Object being saved = \n{0}", criteria.toString());
  }
  criteria = new SearchCriteria(criteria.toDom());
  criteria.getSearchFilterPageCursor().setCurrentPage(1);
  SavedSearchCriteria savedSearchCriteria = 
    new SavedSearchCriteria(this.getSearchCriteria().getSavedSearchName(),
        criteria, this.extractRequestContext().getUser());

  ISearchSaveRepository saveRpstry = 
    SearchSaveRpstryFactory.getSearchSaveRepository();
  saveRpstry.save(savedSearchCriteria);
  criteria.setSavedSearchName(null);

}

/**
 * Do load the search.
 * 
 * @throws SearchException the search exception
 */
private void doLoad(String id) throws SearchException {
  LOG.info("Event: Performing Load");
  
  if(LOG.isLoggable(Level.FINE)) {
    LOG.log(Level.FINE, "Current Search Criteria Object = \n{0}", this.getSearchCriteria().toString());
  }
  ISearchSaveRepository saveRepository = SearchSaveRpstryFactory
  .getSearchSaveRepository();
  SearchCriteria criteria = 
    saveRepository.getSearchCriteria(id, 
        this.extractRequestContext().getUser());
  
  this.getSearchCriteria().loadSearchCriteria(criteria.toDom());
  if(LOG.isLoggable(Level.FINE)) {
    LOG.log(Level.FINE, "Loaded Criteria Object = \n{0}", criteria.toString());
  }
}


/**
 * Redoes the search by using the pagecursor in the session.  Looks
 * for parameter SearchEvents.Event.PARAM_UUID
 *
 * @param event the event
 * @param doPrefetch the Should the prefetch take place
 *
 * @throws SearchException the search exception
 */
@SuppressWarnings("unchecked")
private void redoSearch(ActionEvent event,
    boolean doPrefetch)
throws SearchException {

  FacesContextBroker facesBroker = new FacesContextBroker();
  Map requestMap = facesBroker.getExternalContext().getRequestParameterMap();

  String dosearch = (String) requestMap.get
  (SearchEvents.Event.PARAM_EXECUTE_SEARCH);
  if("".equals(Val.chkStr(dosearch)) && event != null
      && event.getComponent() != null) {
    UIComponent component = event.getComponent();
    dosearch = (String)
    component.getAttributes().get(
        SearchEvents.Event.PARAM_EXECUTE_SEARCH.name());
  }
  if(Val.chkBool(dosearch, false)){
    doSearch(-1, false);
  }
}


/**
 * Do search.
 *
 * @param page the page number (if page <= 0, last page in session will be used)
 * @param doPrefetch the do prefetch
 *
 * @throws SearchException the search exception
 */
protected void doSearch(int page, boolean doPrefetch)
throws SearchException {
  setWasSearched(true);
  LOG.fine("Event: Performing Search");
  this.getSearchResult().reset();
  SearchCriteria criteria = this.getSearchCriteria();

  if(LOG.isLoggable(Level.FINER)) {
    LOG.log(Level.FINER, "Search Criteria Object = \n{0}", criteria.toString());
  }
  PageCursor pageCursor = this.getSearchResult().getPageCursor();

  //record this for resetting the page cursor
  int recordsPerPage = pageCursor.getRecordsPerPage();
  try {

    // if page < 0, then the page in the session (pageCursor) will be used
    if(page > 0){
      pageCursor.setCurrentPage(page);
    }


    // Changed below so that pre-fetch does not do hits.  Pagination
    // will be achieved by getting 1 more result the the records per page
    // to see if there will be more results (Peeking)



    // Controlling startposition since will be adjusting the
    // records per page
    if(pageCursor instanceof SearchFilterPagination && page >= 1) {
      ((SearchFilterPagination)pageCursor).setStartPostion(
          ((page - 1) * pageCursor.getRecordsPerPage()) + 1  );

    }
    // if page == pageCursor total than we are on an edge, we should check
    // if next page exists.
    // if the total number of pages == 0 then we are also on a starting edge
    // we should check if next page exists (
    //
    /*if(page == pageCursor.getTotalPageCount() || pageCursor.getTotalPageCount()
        == 0) {
      pageCursor.setRecordsPerPage(recordsPerPage + 1);
    }*/

    ASearchEngine dao = this.getSearchDao();

    double time = 0;
    // checks if we are at the beginning border (totalPages == 0) or we
    // are at a late border (totalPages > 0
    int border = 0;


    border = 1;

    dao.doSearch();

    // If search criteria changes while you are in a page more than 1
    // then reset to page 1 if no results exists for this search
    /*
    if(page > 1 && this.getSearchResult().getRecords().size() < 1) {
      this.doSearch(urlBuilder, 1, doPrefetch);
    }
    */
   // RequestContext context = this.extractRequestContext();
   // this.getSearchResult().getRecords().buildResourceLinks(context);
    this.getSearchResult().setSearchTimeInSeconds(time += dao.getTimeInSeconds());

    // Insert maximum query hits if provided
    int maxQueryHits = this.getSearchResult().getMaxQueryHits();
    if (getSearchResult().getRecords().size() == 0) {
      maxQueryHits = 0;
    }
    if(maxQueryHits > pageCursor.getTotalPageCount()) {
      pageCursor.setTotalRecordCount(maxQueryHits);
    }
    //pageCursor.checkCurrentPage(); Resolves Ontime # 39321

    // If peek succeeded in getting extra records then adjust the total record
    // count accordingly
    /*if(maxQueryHits < 0 && page >= pageCursor.getTotalPageCount() || pageCursor.getTotalPageCount()
        == 0) {
      pageCursor.setTotalRecordCount(pageCursor.getTotalRecordCount()
          +  this.getSearchResult().getRecordSize() - border);
    }*/

    // Reduce the size of the results if we did a peek
    if(this.getSearchResult().getRecordSize() > recordsPerPage) {

      this.getSearchResult().getRecords().remove(recordsPerPage);

    }
    this.setNavigationOutcome(NAV_CRITERIA2RESULTS);
  } finally {
    // Readjust records per page incase it was affected by peeking
    //pageCursor.setRecordsPerPage(recordsPerPage);
    pageCursor.checkCurrentPage();
  }


}

/**
 * Do view metadata details.
 * 
 * @param context the active request context
 * @param uuid the uuid
 * @param catalogUri the catalog uri (can be null to use default)
 * 
 * @throws SearchException the search exception
 * @throws SchemaException if an exception occurs while evaluating the schema
 * @throws SAXException if a SAX exception occurs while styling details page
 * @throws ParserConfigurationException if parser cofiguration exception occurs while loading details style page
 * @throws IOException if I/O exception occurs which reading details style page
 * @throws XPathExpressionException 
 */
private void doViewMetadataDetails(RequestContext context, String uuid, String catalogUri) 
  throws SearchException, SchemaException, XPathExpressionException, IOException, ParserConfigurationException, SAXException {
  if (detailsPanelGroup != null) {
    detailsPanelGroup.getChildren().clear();
  }
  setResourceUrl("");
  if (uuid == null || "".equals(uuid)) {
    throw new SearchException("UUID given for document requested is either null or empty");
  }
  String metadataXml = this.getMetadataText(uuid, catalogUri);
  this.getSearchResult().setCurrentMetadataXmlInView(metadataXml);
  this.setNavigationOutcome(NAV_RESULTS2VIEWDETAILS); 
  MetadataDocument document = new MetadataDocument();
  Schema schema = document.prepareForView(context,metadataXml); 
  if ((detailsPanelGroup != null) && (schema != null)) {
    setResourceUrl(schema.getMeaning().getResourceUrl());
    
    // check for a configured XSLT to generate the details page,
    // otherwise, generate the details page from the defined schema
    String htmlFragment = "";
    if (schema.getDetailsXslt().length() > 0) {
       try {
    	   	MessageBroker broker = this.extractMessageBroker();
            htmlFragment = Val.chkStr(document.transformDetails(metadataXml,schema.getDetailsXslt(),broker));
      } catch (TransformerException e) {
        htmlFragment = "";
        LOG.log(Level.SEVERE,"Cannot transform metadata details: "+schema.getDetailsXslt(),e);
      }
    }
    if ((htmlFragment != null) && (htmlFragment.length() > 0)) {
      HtmlOutputText component = new HtmlOutputText();
      component.setId("xsltBasedDetails"); 
      component.setValue(htmlFragment);
      component.setEscape(false);
      detailsPanelGroup.getChildren().add(component);
    } else {
      UiContext uiContext = new UiContext();
      schema.appendDetailSections(uiContext,detailsPanelGroup);
    }
  }
}

/**
 * Gets the metadata text.
 * 
 * @param uuid the uuid
 * 
 * @return the metadata text
 * 
 * @throws SearchException rethrows getMetadataText(uuid, null)
 */
public String getMetadataText(String uuid) throws SearchException {
  return getMetadataText(uuid, null);
}

/**
 * Gets the metadata text.
 * 
 * @param uuid the uuid
 * @param catalogUri the catalog uri
 * 
 * @return the metadata text
 * 
 * @throws SearchException the search exception
 */
public String getMetadataText(String uuid, String catalogUri) 
throws SearchException {
  if(uuid == null || "".equals(uuid)) {
    throw new SearchException("UUID given for document requested is either null" 
        + " or empty");
  }
  ASearchEngine dao = this.getSearchDao();

  try {
    if(catalogUri != null){
      dao.setConnectionUri(new URI(catalogUri));
    }
    return dao.getMetadataAsText(uuid);
  } catch (URISyntaxException e) {
    throw new SearchException
    ("Invalid Search URL given for catalog "+ catalogUri + " " 
        + e.getMessage() , e);
  }

}

/**
 * Gets the search dao.
 * @return the search dao
 * @throws SearchException the search exception
 */
protected ASearchEngine getSearchDao() throws SearchException {
  ASearchEngine dao = SearchEngineFactory.createSearchEngine(
      this.getSearchCriteria(), 
      this.getSearchResult(), this.extractRequestContext(), 
      SearchEngineLocal.ID,
      (new FacesContextBroker()).extractMessageBroker());
  return dao;
}
/**
 * Gets the rest search request url atom.
 * @return the rest search request url atom
 */
public String getRestSearchRequestUrlAtom() {
  return getRestSearchRequestUrl("atom");
}
/**
 * Gets the rest search request url georss.
 * @return the rest search request url georss
 */
public String getRestSearchRequestUrlGeorss() {
  return getRestSearchRequestUrl("georss");
}
/**
 * Gets the rest search request url json.
 * @return the rest search request url json
 */
public String getRestSearchRequestUrlJson() {
  return getRestSearchRequestUrl("pjson");
}
/**
 * Gets the rest search request url DCAT.
 * @return the rest search request url DCAT
 */
public String getRestSearchRequestUrlDcat() {
  return getRestSearchRequestUrl("dcat");
}

/**
 * Gets the rest search request url html.
 * @return the rest search request url html
 */
public String getRestSearchRequestUrlHtml() {
  return getRestSearchRequestUrl("html");
}

/**
 * Gets the rest search request url html fragment.
 * @return the rest search request url html fragment
 */
public String getRestSearchRequestUrlHtmlFragment() {
  return getRestSearchRequestUrl("htmlfragment");
}

/**
 * Gets the rest search request url html results jsf.
 * 
 * @return the rest search request url html results jsf
 */
public String getRestSearchRequestUrlHtmlResultsJsf() {
  return getRestSearchRequestUrl("htmlresultsjsf");
}
/**
 * Gets the rest search request url kml.
 * @return the rest search request url kml
 */
public String getRestSearchRequestUrlKml() {
  return getRestSearchRequestUrl("kml");
}

/**
 * Gets the rest search request url.
 * @param format the format
 * @return the rest search request url
 */
protected String getRestSearchRequestUrl(String format) {
  SearchCriteria criteria = this.getSearchCriteria();
  RequestContext context =  this.getContextBroker().extractRequestContext();
  HttpServletRequest request = this.getContextBroker().extractHttpServletRequest();
  
  MessageBroker messageBroker = this.getContextBroker().extractMessageBroker();
  RestUrlBuilder builder = RestUrlBuilder.newBuilder(context,request,messageBroker);
  String params = builder.buildParameters(criteria,format,null);
  String url = request.getContextPath()+"/rest/find/document";
  if ((params != null) && (params.length() > 0)) {
    url += "?"+params;
  }
  return url;
}

public boolean getServiceCheckerEnabled() {
  return Val.chkBool(getParameter("servicechecker.enabled",""), false);
}

public String getServiceCheckerCheckUrl() {
  return getParameter("servicechecker.checkUrl","http://registry.fgdc.gov/statuschecker/api/v2/results");
}

public String getServiceCheckerInfoUrl() {
  return getParameter("servicechecker.infoUrl","http://registry.fgdc.gov/statuschecker/ServiceDetail.php");
}


public String getServiceCheckerToken() {
  return getParameter("servicechecker.token","1059304b3e56ebbeddc23686f3ff8ef0");
}

private String getParameter(String parameterName, String defaultParameterValue) {
  return Val.chkStr(ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters().getValue(parameterName), defaultParameterValue);
}
}
