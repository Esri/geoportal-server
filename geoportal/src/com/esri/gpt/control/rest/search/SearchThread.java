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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.search.ASearchEngine;
import com.esri.gpt.catalog.search.GetRecordsGenerator;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.catalog.search.SearchEngineCSW;
import com.esri.gpt.catalog.search.SearchEngineFactory;
import com.esri.gpt.catalog.search.SearchRequestDefinition;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;

/**
 * The Class SearchThread.
 * 
 * @author UM,TM
 */
public class SearchThread extends Thread {

// clas variables ==============================================================
/** class logger *. */
private Logger LOG = Logger.getLogger(SearchThread.class.getCanonicalName());

// instance variables ==========================================================
/** The is working. */
private boolean isWorking = false;

private boolean stop;

/** The rid. */
private String rid;

/** The search context. */
private SearchContext searchContext;

/** The search status. */
private SearchStatus searchStatus = new SearchStatus();

/** The listeners. */
private ArrayList<ISearchListener> listeners;

private ASearchEngine searchEngine;

private CountDownLatch countDownLatch;

private volatile boolean timeUp;

private volatile ReentrantReadWriteLock globalLock = new ReentrantReadWriteLock();

private volatile Lock lockListenerEvent = globalLock.readLock();

private volatile Lock lockNotifyTimeUp = globalLock.writeLock();




// constructors ================================================================
/**
 * Instantiates a new search thread.
 * 
 * @param context
 *          the context
 * @param rid
 *          the rid
 */
public SearchThread(SearchContext context, String rid, ASearchEngine engine,
  CountDownLatch countDownLatch) {
  this.setSearchContext(context);
  this.setRID(rid);
  this.setSearchEngine(engine);
  this.countDownLatch = countDownLatch;
  this.getSearchStatus().setRid(rid);
}

// properties ==================================================================

/**
 * Gets the flag indicating if the search is in a working state.
 * 
 * @return if the search is working
 */
public boolean getIsWorking() {
  return this.isWorking;
}

/**
 * Sets the flag indicating if the search is in a working state.
 * 
 * @param isWorking
 *          <code>true</code> if the search is working
 */
public void setIsWorking(boolean isWorking) {
  this.isWorking = isWorking;
  if(isWorking == true) {
    this.writeStatus(SearchStatus.STATUSTYPE_WORKING);
  }
}

/**
 * Gets the max search time.
 * 
 * @return the max search time
 */
private int getMaxSearchTime() {
  return this.getSearchContext().getMaxSearchTime();
}

/**
 * Gets the search engine.
 * 
 * @return the search engine
 */
public ASearchEngine getSearchEngine() {
  return searchEngine;
}

/**
 * Sets the search engine.
 * 
 * @param searchEngine
 *          the new search engine
 */
public void setSearchEngine(ASearchEngine searchEngine) {
  this.searchEngine = searchEngine;
}

/**
 * Gets the RID.
 * 
 * @return the RID
 */
public String getRID() {
  return this.rid;
}

/**
 * Sets the RID.
 * 
 * @param rid
 *          the RID
 */
public void setRID(String rid) {
  this.rid = rid;
}

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


public boolean isTimeUp() {
  
  return timeUp;
}

/**
 * Sets the time up.  Called to notify the thread that its time is up.
 * 
 * @param timeUp the new time up
 */
public void setTimeUp(boolean timeUp) {
  lockNotifyTimeUp.lock();
  try {
    
    LOG.finer("Setting time up when status is " + 
        this.getSearchStatus().getStatusType());
    
    if(timeUp == true && 
        !(this.getSearchStatus().getStatusType()
          .equals(SearchStatus.STATUSTYPE_COMPLETED) ||
        this.getSearchStatus().getStatusType()
          .equals(SearchStatus.STATUSTYPE_FAILED))
        ) {
      this.getSearchStatus().setStatusType(SearchStatus.STATUSTYPE_SEARCH_TIMEOUT);
      this.fireSearchEventWorker();
      this.timeUp = timeUp;
    }
  } finally {
    lockNotifyTimeUp.unlock();
  }
}

/**
 * Gets the search status.
 * 
 * @return the search status
 */
public SearchStatus getSearchStatus() {
  return this.searchStatus;
}

/**
 * Sets the search status.
 * 
 * @param searchStatus
 *          the search status
 */
public void setSearchStatus(SearchStatus searchStatus) {
  this.searchStatus = searchStatus;
}

// methods =====================================================================/**
/**
 * Write status.
 * 
 * @param status the status
 */
private void writeStatus(String status) {
  this.getSearchStatus().setStatusType(status);
  this.fireSearchEvent();
}

/**
 * Adds the action listener.
 * 
 * @param listener
 *          the listener
 */
public void addActionListener(ISearchListener listener) {
  if (this.listeners == null) {
    listeners = new ArrayList<ISearchListener>();
  }
  listeners.add(listener);
}

/**
 * Removes the action listener.
 * 
 * @param listener
 *          the listener
 */
public void removeActionListener(ISearchListener listener) {
  if (listeners == null || listeners.size() < 1) {
    return;
  }
  listeners.remove(listener);

}

/**
 * Fire search event.
 */
private void fireSearchEvent() {

  lockListenerEvent.lock();
  try {
    this.fireSearchEventWorker();
  } finally {
    lockListenerEvent.unlock();
  }
}

/**
 * Fire search event worker.  Use fire search event.  This method does not
 * have lockign and is called by fireSearchEvent;
 */
private void fireSearchEventWorker() {
  LOG.fine("Writing " + this.getSearchStatus().getStatusType() 
      + " to listners time up = " + this.isTimeUp());
  
  if (this.isTimeUp() || listeners == null || listeners.size() < 1) {
    return;
  }
  DistributedSearchEvent event = new DistributedSearchEvent(this, this
      .getSearchStatus());
  for (ISearchListener listener : listeners) {
    try {
      listener.searchEvent(event);
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error in listener", e);
    }

  }
}

/**
 * Runs the process.
 */
public void run() {
  try {
    setIsWorking(true);
    runWorker();
  } finally {
    if(countDownLatch != null) {
      countDownLatch.countDown();
    }
  }
}

/**
 * Runs the thread process
 * 
 */
public void runWorker() {

  
  RequestContext rc = null;
  try {
      // rc = RequestContext.extract(this.getSearchContext().getHttpRequest());
    rc = this.getSearchContext().getRequestContext();
    RestQuery query = this.getSearchContext().getRestQuery();

    SearchStatus status = this.getSearchStatus();
    status.setStartTimestamp(new Timestamp(System.currentTimeMillis()));
   
    if (this.isTimeUp()) {
      throw new Exception("This thread has been stopped");
    }
    SearchResult result = new SearchResult();
    SearchCriteria criteria = this.getSearchContext().getSearchCriteria();
    ASearchEngine engine = this.getSearchEngine();
    engine.setRequestDefinition(new SearchRequestDefinition(criteria, result));
    engine.setHitsOnly(true);
    engine.setConnectionTimeoutMs(this.getMaxSearchTime());
    engine.setResponseTimeout(this.getMaxSearchTime());

    if (LOG.isLoggable(Level.FINER)) {
      LOG.finer("Starting SEARCH IN thread id= " + this.getId() + ", rid="
          + this.getRID());
    }
    if (this.rid.equalsIgnoreCase("local")) {
      GetRecordsGenerator grg = new GetRecordsGenerator(rc);
      if (engine.getHitsOnly()) {
        grg.setResultType("HITS");
      }
      String cswRequest = grg.generateCswRequest(query);
      SearchEngineCSW csw = (SearchEngineCSW) engine;
      if (this.isTimeUp()) {
        throw new Exception("This threads time is up");
      }
      csw.doSearch(cswRequest);
    } else {
      if (this.isTimeUp()) {
        throw new Exception("This threads time is up");
      }
      engine.doSearch();
    }
    if (LOG.isLoggable(Level.FINER)) {
      LOG.finer("ENDING SEARCH IN thread id= " + this.getId() + ", rid="
          + this.getRID());
    }
    status.setHitCount(result.getMaxQueryHits());
    this.getSearchStatus().setEndTimestamp(
        new Timestamp(System.currentTimeMillis()));
    this.writeStatus(SearchStatus.STATUSTYPE_COMPLETED);
    
  } catch (Exception e) {
    this.getSearchStatus().setMessage(e.getMessage());
    this.writeStatus(SearchStatus.STATUSTYPE_FAILED);
    LOG.log(Level.WARNING, "Error during distributed search", e);
  } finally {

    this.getSearchStatus().setEndTimestamp(
        new Timestamp(System.currentTimeMillis()));
    this.setIsWorking(false);

  }
}

}
