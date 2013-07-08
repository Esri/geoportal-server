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
package com.esri.gpt.control.georss;

import com.esri.gpt.catalog.search.ISearchFilter;
import com.esri.gpt.catalog.search.SearchConfig;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchFilterHarvestSites;
import com.esri.gpt.catalog.search.SearchFiltersList;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.catalog.search.SearchResultRecords;
import com.esri.gpt.control.search.SearchController;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.util.Val;
import java.util.ConcurrentModificationException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.FactoryFinder;
import javax.faces.application.Application;
import javax.faces.application.NavigationHandler;
import javax.faces.application.ViewHandler;
import javax.faces.context.FacesContext;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Class HtmlAdvancedWriter.  Generates the JSF page from rest.
 */
public class HtmlAdvancedWriter implements FeedWriter {

// class variables =============================================================
/** The Constant JSFBEAN_SEARCH_CONTROLLER. */
private static final String JSFBEAN_SEARCH_CONTROLLER = "SearchController";

/** The Constant JSFBEAN_SEARCH_CONTROLLER. */
private static final String JSFBEAN_SEARCH_CRITERIA = "SearchCriteria";

/** The Search results page. */
private static final String SEARCH_RESULTS_PAGE = 
  "/catalog/search/resultsBody.jsp";

/** The search page. */
private static final String SEARCH_PAGE = "/catalog/search/search.jsp";

/** Class logger *. */
private static final Logger LOG = 
  Logger.getLogger(HtmlAdvancedWriter.class.getCanonicalName());

// instance variables ==========================================================
/** The results only. */
private boolean resultsOnly;
 
/** The criteria. */
private SearchCriteria criteria;

private FacesContextBroker broker;

private RequestContext requestContext;

// properties ==================================================================
/**
 * Checks if is results only.
 * 
 * @return true, if is results only
 */
public boolean isResultsOnly() {
  return resultsOnly;
}

/**
 * Sets the results only.
 * 
 * @param resultsOnly the new results only
 */
public void setResultsOnly(boolean resultsOnly) {
  this.resultsOnly = resultsOnly;
}

/**
 * Gets the criteria.
 * 
 * @return the criteria (possibly null)
 */
public SearchCriteria getCriteria() {
  return criteria;
}

/**
 * Sets the criteria.
 * 
 * @param criteria the new criteria
 */
public void setCriteria(SearchCriteria criteria) {
  this.criteria = criteria;
  
  
}

/**
 * Gets the request context.
 * 
 * @return the request context
 */
public RequestContext getRequestContext() {
  return requestContext;
}

/**
 * Sets the request context.
 * 
 * @param requestContext the new request context
 */
public void setRequestContext(RequestContext requestContext) {
  this.requestContext = requestContext;
}

// methods =====================================================================
/**
 * Write.
 * 
 * @param records the records
 */
  @Override
  public void write(IFeedRecords records) {
  SearchController controller = this.readController();
  controller.getSearchResult().setRecords(new SearchResultRecords(records));
  controller.setWasSearched(true);
  try {
    showResults();
  } catch (Exception e) {
    LOG.log(Level.WARNING, "Error while showing results", e);
  }
}

/**
 * Write.
 * 
 * @param result the result
 */
public void write(SearchResult result) {
  
  SearchController controller = this.readController();
  controller.setSearchCriteria(this.getCriteria());
  this.getCriteria().getSearchFilterPageCursor().setTotalRecordCount(
      result.getMaxQueryHits());
  
  controller.setSearchResult(result);
  controller.setWasSearched(true);
  try {
    showResults();
  } catch (Exception e) {
    // TODO Auto-generated catch block
    e.printStackTrace();
  }
}

/**
 * Read controller from the request.
 * 
 * @return the search controller
 */
private SearchController readController() {
  FacesContextBroker broker = new FacesContextBroker();
  @SuppressWarnings("unused")
  FacesContext fc = broker.getFacesContext();
  // intentionally not used
  SearchController controller = 
    (SearchController) broker.resolveManagedBean(JSFBEAN_SEARCH_CONTROLLER);
  controller.setSearchCriteria(this.getCriteria());
  return controller;
}

/**
 * Read SearchCriteria from the session.
 * 
 * @return the search controller
 */
private SearchCriteria readSessionCriteria() {
  FacesContextBroker broker = new FacesContextBroker();
  @SuppressWarnings("unused")
  FacesContext fc = broker.getFacesContext();
  fc.getApplication().getViewHandler().createView(fc, SEARCH_PAGE);
  
  // intentionally not used
  SearchCriteria criteria = 
    (SearchCriteria) broker.resolveManagedBean(JSFBEAN_SEARCH_CRITERIA);

  return criteria;
  
}

/**
 * Show results.
 * 
 * @throws Exception
 *           the exception
 */
protected void showResults() throws Exception {
  FacesContextBroker broker = new FacesContextBroker();
  String dispatchTo = "";
  String tagName = "com.esri.gpt.control.filter.EncodingFilterTag";
  // will prevent going to front page
  this.getRequestContext().addToSession(tagName, "tag");
  HttpServletRequest httpReq = broker.extractHttpServletRequest();
  HttpServletResponse httpResp = broker.extractHttpServletResponse();
  FacesContext fctx = broker.getFacesContext();
  Application application = fctx.getApplication();
  
  if (this.isResultsOnly()) {
    dispatchTo = SEARCH_RESULTS_PAGE;

  } else {
  
    // TODO: Glassfish does not support this section (f=searchpage)
    NavigationHandler navHandler = application.getNavigationHandler();
    navHandler.handleNavigation(fctx, null, "catalog.search.results");
    dispatchTo = fctx.getViewRoot().getViewId();

    String jsfSuffix = SearchConfig.getConfiguredInstance().getJsfSuffix();
    if ("".equals(jsfSuffix)) {
      jsfSuffix = ".page";
    }
    if (jsfSuffix.indexOf('.') < 0) {
      jsfSuffix = "." + jsfSuffix;
    }
    //javax.faces.DEFAULT_SUFFIX 
    dispatchTo = dispatchTo.replaceAll(".jsp", jsfSuffix);
    //ViewHandler.DEFAULT_SUFFIX_PARAM_NAME = jsfSuffix; 
    //fctx.getExternalContext().dispatch(dispatchTo);
    //httpReq.getRequestDispatcher( dispatchTo).forward(httpReq, httpResp);
      
 
  }

  
  // Synching criteria with session criteria in thread so that search result
  // does
  // not have to wait for synch to complete.
  setExtraCriteriaProperties(httpReq);
  SynchSessionCriteria synchSc = new SynchSessionCriteria(criteria, this
      .readSessionCriteria());
  synchSc.synch();
  //Thread thread = new Thread(synchSc);
  //thread.start();
  jsfDispatchPage(fctx, dispatchTo);
 
}

/**
 * Jsf dispatch page.
 * 
 * @param fctx the faces context
 * @param page the pge
 */
private void jsfDispatchPage(FacesContext fctx, String page) {
  LifecycleFactory lf = (LifecycleFactory) FactoryFinder
      .getFactory(FactoryFinder.LIFECYCLE_FACTORY);
  Lifecycle lifecycle = lf.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
  ViewHandler vh = fctx.getApplication().getViewHandler();
  fctx.getViewRoot().setRenderKitId(vh.calculateRenderKitId(fctx));
  fctx.setViewRoot(vh.createView(fctx, page));
  
  // view rendering
  try {
    lifecycle.render(fctx);
  } catch (Exception e) {
    LOG.log(Level.INFO, "Error while rendering page. Attempting again" + page,
        e);
    lifecycle.render(fctx);
  } finally {
    fctx.release();
  }
}

/**
 * Sets the extra criteria properties.  Properties unique to using
 * this writer.  Currently gets the rids and the ridName attribute.
 * 
 * @pa  ram httpReq the new extra criteria properties
 */
private void setExtraCriteriaProperties(HttpServletRequest httpReq) {
  String rids = httpReq.getParameter("rids");
  String ridName = httpReq.getParameter("ridName");
 
  SearchCriteria criteria = this.getCriteria();
  SearchFiltersList filters = criteria.getMiscelleniousFilters();
  for(ISearchFilter filter:filters) {
    if(filter instanceof SearchFilterHarvestSites) {
      SearchFilterHarvestSites sfHs = (SearchFilterHarvestSites) filter;
      sfHs.setSelectedDistributedIds(rids);
      sfHs.setSelectedHarvestSiteName(ridName);
    }
  }
  
  boolean expandResults = 
    Val.chkBool(httpReq.getParameter("expandResults"), false);
  criteria.setExpandResultContent(expandResults);
}

/**
 * Class to update the session criteria
 * 
 * @author TM
 *
 */
private class SynchSessionCriteria implements Runnable {
  private SearchCriteria __suppliedCriteria;
  private SearchCriteria __sessionCriteria;
  
  /**
   * Instantiates a new synch session criteria.
   * 
   * @param suppliedCriteria the supplied criteria
   * @param sessionCriteria the session criteria
   */
  private SynchSessionCriteria(SearchCriteria suppliedCriteria, 
      SearchCriteria sessionCriteria){
    __suppliedCriteria = criteria;
    __sessionCriteria = sessionCriteria;
  }

  /** 
   * Thread runner.  Calls the synch method.
   */
  public void run() {
    try {
      synch();
    } catch(Throwable e) {
      LOG.log(Level.WARNING, "Error while synchronizing rest criteria with " +
      		"session criteria", e );
    }
    
  }
  
  /**
   * Synch.
   * 
   * @throws SearchException the search exception
   */
  public void synch() throws SearchException {
    
    if(__suppliedCriteria == __sessionCriteria) {
      return;
    }
    try {
      __sessionCriteria.loadSearchCriteria(__suppliedCriteria.toDom());
    } catch(ConcurrentModificationException e) {
      // This sometimes occurs especially when the user presesses the search
      // button multiple times at a very fast pace
      LOG.fine("Saving session failed" + e.getMessage());
    }
  }
   
  
}

}
