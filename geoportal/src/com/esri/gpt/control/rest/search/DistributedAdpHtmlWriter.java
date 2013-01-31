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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esri.gpt.catalog.search.ASearchEngine;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

/**
 * Distributed ADP HTML writer.
 */
public class DistributedAdpHtmlWriter implements ISearchListener {

private static final Logger LOG = Logger
.getLogger(DistributedAdpHtmlWriter.class.getCanonicalName());

//instance variables ==========================================================
/** The servlet Request. **/
private volatile HttpServletRequest _servletRequest;

/** The servlet response. **/
private volatile HttpServletResponse _servletResponse;

private volatile boolean _stopFurtherResponseWrites;

private MessageBroker _messageBroker;

//properties ==================================================================

/**
* The messageBroker
* 
*@param messageBroker
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
* Gets the servlet request.
* 
* @return the servlet request
*/
public HttpServletRequest getServletRequest() {
return _servletRequest;
}

/**
* Sets the servlet request.
* 
* @param servletRequest
*          the new servlet request
*/
public void setServletRequest(HttpServletRequest servletRequest) {
this._servletRequest = servletRequest;
}

/**
* Gets the servlet response.
* 
* @return the servlet response
*/
public HttpServletResponse getServletResponse() {
return _servletResponse;
}

/**
* Sets the servlet response.
* 
* @param servletResponse
*          the new servlet response
*/
public void setServletResponse(HttpServletResponse servletResponse) {
this._servletResponse = servletResponse;
}

//methods =================================================================
/**
* Instantiates a new search page distributed adp.
* 
* 
*/
public void initBeforeSearch() throws IOException {
  HttpServletResponse response = this.getServletResponse();
  HttpServletRequest request = this.getServletRequest();

  this.setServletRequest(request);
  this.setServletResponse(response);
  if (LOG.isLoggable(Level.FINER)) {
    LOG.finer("Initializing class");
  } 
  this._stopFurtherResponseWrites = false;
  
  
  
  response.setContentType("text/html");
  response.setCharacterEncoding("UTF-8");
  String html = "<html><head></head><body>";
  PrintWriter writer = response.getWriter();
  writer.write(html);
  if (LOG.isLoggable(Level.FINER)) {
    String log = "// Time: " + System.currentTimeMillis() + " Thread # "
        + Thread.currentThread().getId() + "\n";
    log += "Writer = " + writer.hashCode() + "\n";
    log += "Response = " + response.hashCode();
    LOG.finer(html + log);
  }
}

/**
* On search thread complete.
* 
* @param event
*          the event
* @see com.esri.gpt.control.rest.search.ISearchListener#searchEvent(com.esri.gpt.control.rest.search.DistributedSearchEvent)
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
  String html = "";
 
  if(!"".equals(message)) {
    html += "<div class=\"snippet\">";
    html += "<div class=\"title\">";
    html += event.getSearchStatus().getRid();
    html += "</div>";
    html += "<div class=\"abstract\">";
    if(event.getSource() instanceof SearchThread) {
      SearchThread searchThread = (SearchThread) event.getSource();
      ASearchEngine engine = searchThread.getSearchEngine();
      try {
        html += engine.getKeyAbstract();
      } catch (SearchException e) {
        LOG.log(Level.FINE,"Could not get summary", e.getMessage());
        e.printStackTrace();
      }
    }
    html += "<div id=\"message\">" + message + "</div>";
    html += "</div>";
    html += "</div>";
    html += "</br>";

    try {
      PrintWriter writer = this.getServletResponse().getWriter();
      // There were some connection reset errors sporadically while flushing the
      // buffer. synchronization is is for good measure
      synchronized (this) {
        try {
          writer.write(html);
          this.getServletResponse().flushBuffer();
        } catch (Exception e) {
          LOG.log(Level.WARNING, "Error while flushing + " + e.getMessage());
        }
      }
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error while writting distributed search message: "
          + message, e);
    }
    
  }

}

/**
 * Signals all threads are done processing.
 * 
 * @see com.esri.gpt.control.rest.search.ISearchListener#searchDone()
 */
public void searchDone() {
  String html = "</body></html>";

  // HttpServletRequest request = this.getServletRequest();
  String log = "";
  if (LOG.isLoggable(Level.FINER)) {
    log += "// Time: " + System.currentTimeMillis() + " Thread # "
        + Thread.currentThread().getId() + "\n";
    LOG.finer("Sending " + html + " " + log);
  }

  try {
    this._stopFurtherResponseWrites = true;
    PrintWriter printWriter = this.getServletResponse().getWriter();
    printWriter.write(html);
  } catch (Throwable e) {
    LOG.log(Level.WARNING, "Error while closing distributed search", e);
  }
  // }
  // }
}

}
