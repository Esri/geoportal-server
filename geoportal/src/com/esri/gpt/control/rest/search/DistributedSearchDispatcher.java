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
package com.esri.gpt.control.rest.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.catalog.search.ASearchEngine;
import com.esri.gpt.catalog.search.SearchEngineFactory;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.framework.context.RequestContext;


/**
 * The Class DistributedSearch.
 */
public class DistributedSearchDispatcher {

// class variables =============================================================
/** Class Logger *. */
private Logger LOG = 
  Logger.getLogger(DistributedSearchDispatcher.class.getCanonicalName());

// instance variables ==========================================================
/** The search context. */
private SearchContext searchContext;

/** The threads. */
private List<SearchThread> threads = new ArrayList<SearchThread>();

/** The listeners. */
private ArrayList<ISearchListener> listeners = new ArrayList<ISearchListener>();

// constructors ================================================================

/**
 * Default constructor.
 */
public DistributedSearchDispatcher() {
}

/**
 * Main unit test method.
 * 
 * @param args
 *          startup arguments
 */
public static void main(String[] args) {
  try {

    DistributedSearchDispatcher self = new DistributedSearchDispatcher();
    self.search();

  } catch (Throwable t) {
    t.printStackTrace(System.err);
  }
}

// properties ==============================================================

/**
 * Gets the search context.
 * 
 * @return the search context
 */
public SearchContext getSearchContext() {
  return this.searchContext;
}

/**
 * Sets the search context.
 * 
 * @param searchContext
 *          the search context
 */
public void setSearchContext(SearchContext searchContext) {
  this.searchContext = searchContext;
}

/**
 * Gets the listeners.
 * 
 * @return the listeners (never null)
 */
private ArrayList<ISearchListener> getListeners() {
  if(this.listeners == null) {
    this.listeners = new ArrayList<ISearchListener>();
  }
  return this.listeners;
}

// methods =================================================================

/**
 * Adds the action listener.
 * 
 * @param listener the listener
 */
public void addActionListener(ISearchListener listener) {
  if(this.listeners == null) {
    listeners = new ArrayList<ISearchListener>();
  }
  listeners.add(listener);
}

/**
 * Removes the action listener.
 * 
 * @param listener the listener
 */
public void removeActionListener(ISearchListener listener) {
  if(listeners == null || listeners.size() < 1) {
    return;
  }
  listeners.remove(listener);
  
}



/**
 * Searches the endpoints.
 * @throws Exception the exception
 */
public void search() throws Exception {

  // initialize listeners before search
  if(listeners != null && listeners.size() > 0) {
    for(ISearchListener listener : listeners) {
      listener.initBeforeSearch();
    }
  }

  
  
  SearchContext context = this.getSearchContext();
  Map<String, Object> mapRid2Engine = 
    SearchEngineFactory.createSearchEngines(
      context.getSearchCriteria(), 
      new SearchResult(),
      this.searchContext.getRequestContext(),
       context.getRIDs(), 
      context.getMessageBroker(), 
      null, null);
  
  Set<String> setRids = mapRid2Engine.keySet();

 
  // Extract the engines
  Map<String, ASearchEngine> mapEngines = new HashMap<String, ASearchEngine>();
  Map<String, String> mapRidErrors = new HashMap<String, String>();
  for (String rid : setRids) {
    
    Object obj = mapRid2Engine.get(rid);
    if(!(obj instanceof ASearchEngine)) {
      mapRidErrors.put(rid, obj.toString());
      continue;
    }
    mapEngines.put(rid, (ASearchEngine) obj);
  }
  
  // Make the threads
  CountDownLatch countDownLatch = new CountDownLatch(mapEngines.size());
  for(String rid : mapEngines.keySet()) {
    ASearchEngine engine = mapEngines.get(rid);
    SearchThread searchThread = new SearchThread(this.getSearchContext(), rid,
         engine, countDownLatch);
    searchThread.setName(rid + System.currentTimeMillis());
    
    if(listeners != null && listeners.size() > 0) {
      for(ISearchListener listener : listeners) {
        searchThread.addActionListener(listener);
      }
    }
    this.threads.add(searchThread);
   
  }

  // start the threads
  for (SearchThread thread : this.threads) {
    if(LOG.isLoggable(Level.FINER)) {
      LOG.finer("Starting thread id= " + thread.getId() 
          + ", rid=" + thread.getRID());
    }
    thread.start();    
  }
  
  // wait for all threads to finish or for certain time to pass
  countDownLatch.await(this.getSearchContext().getMaxSearchTime(), 
      TimeUnit.MILLISECONDS);
  
  LOG.finer("Countdown latch released");
  // inform threads time is up
  for (SearchThread thread : this.threads) {
    thread.setTimeUp(true);
  }
  
  // tell listeners they are done
  if(listeners != null && listeners.size() > 0) {
    for(ISearchListener listener : listeners) {
      for(String rid: mapRidErrors.keySet()) {
        /*DistributedSearchEvent event = new DistributedSearchEvent(this, this
            .getSearchStatus());*/
        SearchStatus status = new SearchStatus();
        status.setStatusType(SearchStatus.STATUSTYPE_FAILED);
        status.setRid(rid);
        status.setMessage(mapRidErrors.get(rid));
        DistributedSearchEvent event = new DistributedSearchEvent(this, 
            status);
        listener.searchEvent(event);
      }
      listener.searchDone();
    }
  }
    
 /* for (SearchThread thread : this.threads) {
    if (thread.getSearchStatus().getStatusType()
        .equals(SearchStatus.STATUSTYPE_COMPLETED)) {
      // 
    }  else if(thread.isAlive()) {
      thread.getSearchStatus().setStatusType(
          SearchStatus.STATUSTYPE_SEARCH_TIMEOUT);
      thread.fireSearchEvent();
      thread.setStopThread(true);
      
      LOG.warning("Thread found alive at the end distributed search " +
      		"rid =" + thread.getId());
    } 
  }*/
 


}
}
