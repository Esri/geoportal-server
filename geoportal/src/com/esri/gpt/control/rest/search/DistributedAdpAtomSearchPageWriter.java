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

import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.utils.XMLUtils;

import com.esri.gpt.catalog.search.ASearchEngine;
import com.esri.gpt.catalog.search.ResourceLink;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.control.georss.AtomFeedWriter;
import com.esri.gpt.control.georss.AtomFeedWriter.AtomEntry;
import com.esri.gpt.control.georss.AtomFeedWriter.AtomFeed;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;


/**
 * The Class DistributedAdpAtomSearchPageWriter.
 * 
 * @author TM
 */
public class DistributedAdpAtomSearchPageWriter implements ISearchListener {

// class variables =============================================================
/** The LOG. */
private static Logger LOG = Logger.getLogger(
    DistributedAdpAtomSearchPageWriter.class.getCanonicalName());

/** The ATTRIBUT e_ ini. */
private static String ATTRIBUTE_INI = "init" + 
  DistributedAdpAtomSearchPageWriter.class.getCanonicalName();

private static final String NAMESPACE_GPT = "http://www.esri.com/geoportal";

// instance variables ==========================================================
/** The _stop further response writes. */
private boolean _stopFurtherResponseWrites;

/** The http servlet response. */
private HttpServletResponse _httpServletResponse;

/** The http servlet request. */
private HttpServletRequest _httpServletRequest;

/** The print writer. */
private PrintWriter _printWriter;

/** The atom feed. */
private AtomFeed _atomFeed;

/** The message broker. */
private MessageBroker _messageBroker;

/** The rest url. */
private String _restUrl;

// properties ==================================================================
/**
 * Gets the rest url.
 * 
 * @return the rest url
 */
public String getRestUrl() {
  return _restUrl;
}

/**
 * Sets the rest url.
 * 
 * @param url the new rest url
 */
public void setRestUrl(String url) {
  this._restUrl = url;
}

/**
 * Gets the prints the writer.
 * 
 * @return the prints the writer
 */
private PrintWriter getPrintWriter() {
  return _printWriter;
}

/**
 * Sets the prints the writer.
 * 
 * @param printWriter the new prints the writer
 */
private void setPrintWriter(PrintWriter printWriter) {
  this._printWriter = printWriter;
}

/**
 * Sets the servlet request.
 * 
 * @param request the new servlet request
 */
public void setServletRequest(HttpServletRequest request) {
  this._httpServletRequest = request;
  
}

/**
 * Gets the servlet request.
 * 
 * @return the servlet request
 */
private HttpServletRequest getServletRequest() {
  return this._httpServletRequest;
}

/**
 * Sets the servlet response.
 * 
 * @param response the new servlet response
 */
public void setServletResponse(HttpServletResponse response) {
  this._httpServletResponse = response;
  
  
}

/**
 * Sets the message broker.
 * 
 * @param messageBroker the new message broker
 */
public void setMessageBroker(MessageBroker messageBroker) {
  this._messageBroker = messageBroker;
}

/**
 * Gets the message broker.
 * 
 * @return the message broker
 */
private MessageBroker getMessageBroker() {
  return this._messageBroker;
}

/**
 * Gets the servlet response.
 * 
 * @return the servlet response
 */
private HttpServletResponse getServletResponse()  {
  return this._httpServletResponse;
}

// methods =====================================================================
/**
 * Inits the before search.
 * 
 * @throws Exception the exception
 */
public void initBeforeSearch() throws Exception {
  HttpServletResponse response = this.getServletResponse();
  HttpServletRequest request = this.getServletRequest();
  
  String queryString = request.getQueryString();
  queryString = queryString.replaceAll("((?i)rid=([^&])*)", "");
  queryString = queryString.replaceAll("&&", "");
  queryString = request.getContextPath() + "/rest/find/document?" + queryString;
  
  this.setRestUrl(queryString);
  
  this.setPrintWriter(response.getWriter());
  response.setContentType("application/atom+xml;charset=UTF-8");
  
  this.setServletRequest(request);
  this.setServletResponse(response);
  if(LOG.isLoggable(Level.FINER)) {
    LOG.finer("Initializing class");
  }
  this._stopFurtherResponseWrites = false;
  //synchronized (request) {
 
  AtomFeed atom = this.getAtomFeed();
  atom.addStringToXmlHeader(" xmlns:gpt=\"" + NAMESPACE_GPT + "\"");
  atom.setTitle(this.getMessageBroker().retrieveMessage(
  "catalog.search.distributedSearch.atomTitle"));
  atom.writePreamble(this.getPrintWriter());

     
 
  
}

/**
 * Search event.
 * 
 * @param event the event
 */
public void searchEvent(DistributedSearchEvent event) {
  
  if(this._stopFurtherResponseWrites == true) {
    return;
  }
    
  long results = 0;
  String message = "";
  if(event.getSearchStatus().getStatusType().equals(
      SearchStatus.STATUSTYPE_FAILED)) {
    message = this.getMessageBroker().retrieveMessage(
        "catalog.search.distributedSearch.searchFailed") + " : " + 
        event.getSearchStatus().getMessage();
   
  } else if(event.getSearchStatus().getStatusType().equals(
      SearchStatus.STATUSTYPE_COMPLETED)) {
   
    results = event.getSearchStatus().getHitCount();
    if(results < 0) {
      message = this.getMessageBroker().retrieveMessage(
        "catalog.search.distributedSearch.hitCountUnknown");
    } else  {
      message = this.getMessageBroker().retrieveMessage(
        "catalog.search.distributedSearch.results");
      message = message.replaceAll("\\{0\\}", String.valueOf(
        results));
    }
  } else if(event.getSearchStatus().getStatusType().equals(
      SearchStatus.STATUSTYPE_SEARCH_TIMEOUT)) {
    message = this.getMessageBroker().retrieveMessage(
      "catalog.search.distributedSearch.searchTimeout");
    
  }
  message = Val.chkStr(message);
  if(!"".equals(message)) {
    AtomEntry ae = new AtomFeedWriter(this.getPrintWriter()).new AtomEntry();
    ae.setId(event.getSearchStatus().getRid());
    ae.setTitle(ae.getId() + " " + message);
    
    
    ResourceLink link = new ResourceLink();
    link.setLabel(this.getMessageBroker().retrieveMessage(
      "catalog.search.distributedSearch.linkLabel"));
    link.setUrl(this.getRestUrl() + "&rid=" + event.getSearchStatus().getRid());
    ae.addResourceLink(link);
    if(event.getSource() instanceof SearchThread) {
      SearchThread searchThread = (SearchThread) event.getSource();
      ASearchEngine engine = searchThread.getSearchEngine();
      try {
        ae.setSummary(engine.getKeyAbstract());
      } catch (SearchException e) {
        LOG.log(Level.FINE,"Could not get summary", e.getMessage());
        e.printStackTrace();
      }
    }
    StringBuffer bCustomXml = new StringBuffer();
    
    bCustomXml
      .append("<gpt:distributedSearch>")
      .append("<gpt:hits>").append(results).append("</gpt:hits>");
    if (event.getSearchStatus().getStatusType().equals(
        SearchStatus.STATUSTYPE_COMPLETED)) {
      long time = System.currentTimeMillis()- event.getSearchStatus()
      .getStartTimestamp().getTime();
      bCustomXml.append("<gpt:msTime>").append(time).append("</gpt:msTime>");
    } else if (event.getSearchStatus().getStatusType().equals(
        SearchStatus.STATUSTYPE_FAILED) || 
        event.getSearchStatus().getStatusType().equals(
            SearchStatus.STATUSTYPE_SEARCH_TIMEOUT)) {
      bCustomXml.append("<gpt:error>")
        .append(Val.escapeXml(message))
        .append("</gpt:error>");
    }
       
    bCustomXml.append("</gpt:distributedSearch>");
    ae.setCustomElements(bCustomXml.toString());
    
    PrintWriter writer = this.getPrintWriter();
    synchronized(writer) {
      try {
        ae.WriteTo(writer);
        writer.flush();
      } catch(Exception e) {
        LOG.log(Level.INFO, "Error while flushing writer", e);
      }
    }
  }

}



/**
 * Search done.
 * 
 * @throws Exception the exception
 */
public void searchDone() throws Exception {
  _stopFurtherResponseWrites = true; 
  AtomFeed atom = this.getAtomFeed();
  
  atom.writeEnd(this.getPrintWriter());
}

/**
 * Gets the atom feed.
 * 
 * @return the atom feed
 */
private AtomFeed getAtomFeed() {
  if(_atomFeed == null) {
    AtomFeed af = new AtomFeedWriter(this.getPrintWriter()).new AtomFeed();
    af.setTitle(getMessageBroker().retrieveMessage("catalog.rest.title"));
    af.setDescription(getMessageBroker().retrieveMessage(
        "catalog.rest.description"));
    af.setAuthor(getMessageBroker().retrieveMessage(
        "catalog.rest.generator"));
    af.setCopyright(getMessageBroker().retrieveMessage(
        "catalog.rest.copyright"));
    //af.setLink(getEntryBaseUrl());
    //af.setId(getEntryBaseUrl());
    af.setUpdated(new Date());
    _atomFeed = af;
  }
  return _atomFeed;
}


}
