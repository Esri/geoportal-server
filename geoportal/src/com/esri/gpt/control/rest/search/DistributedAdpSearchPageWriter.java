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

import com.esri.gpt.framework.jsf.MessageBroker;

/**
 * The Class SearchPageDistributedAdp.
 */
public class DistributedAdpSearchPageWriter implements ISearchListener {

// class variable ==============================================================

/** Class logger **/
private static final Logger LOG = Logger
    .getLogger(DistributedAdpSearchPageWriter.class.getCanonicalName());

// instance variables ==========================================================
/** The servlet Request. **/
private volatile HttpServletRequest _servletRequest;

/** The servlet response. **/
private volatile HttpServletResponse _servletResponse;

private volatile boolean _stopFurtherResponseWrites;

private MessageBroker _messageBroker;

// properties ==================================================================

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

// methods =================================================================
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
  // synchronized (request) {
 //f (request.getAttribute(ATTRIBUTE_HTML_INI) == null) {
    //response.setBufferSize(10);
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
   // request.setAttribute(ATTRIBUTE_HTML_INI, new Object());
    String script = "";
    PrintWriter writer = response.getWriter();
    writer.write("<html><head>" + script);
    if (LOG.isLoggable(Level.FINER)) {
      script = "// Time: " + System.currentTimeMillis() + " Thread # "
          + Thread.currentThread().getId() + "\n";
      script += "Writer = " + writer.hashCode() + "\n";
      script += "Response = " + response.hashCode();
      LOG.finer("Writting html header <html></head> " + script);
    }

   //}
  // }
}

/**
 * On search thread complete.
 * 
 * @param event
 *          the event
 * @see com.esri.gpt.control.rest.search.ISearchListener#searchEvent(com.esri.gpt.control.rest.search.DistributedSearchEvent)
 */
public void searchEvent(DistributedSearchEvent event) {

  if (LOG.isLoggable(Level.FINER)) {
    LOG.finer("In Search Thread Complete" + event);
  }

  String message = "";
  SearchStatus searchStatus = event.getSearchStatus();
  if (searchStatus.getStatusType().equals(SearchStatus.STATUSTYPE_COMPLETED)) {
    long results = searchStatus.getHitCount();
    if(results < 0) {
      message = this.getMessageBroker().retrieveMessage(
        "catalog.search.distributedSearch.hitCountUnknown");
    } else  {
      message = this.getMessageBroker().retrieveMessage(
        "catalog.search.distributedSearch.results");
      message = message.replaceAll("\\{0\\}", String.valueOf(
        results));
    }
  } else if (searchStatus.getStatusType()
      .equals(SearchStatus.STATUSTYPE_FAILED)) {
    message = this.getMessageBroker().retrieveMessage(
        "catalog.search.distributedSearch.searchFailed")
        + " : " + event.getSearchStatus().getMessage();
  } else if (searchStatus.getStatusType().equals(
      SearchStatus.STATUSTYPE_WORKING)) {
    message = searchStatus.getMessage() + " : Working";
  } else if (searchStatus.getStatusType().equals(
      SearchStatus.STATUSTYPE_SEARCH_TIMEOUT)) {
    message = this.getMessageBroker().retrieveMessage(
        "catalog.search.distributedSearch.searchTimeout");
  } else {
    message = searchStatus.getMessage();
  }
  String script = "<script type=\"text/javascript\">";
      script += "window.parent.updateDistributedSearch( '"
          + searchStatus.getRid().replaceAll("'", "\'") + "', '"
          + message.replaceAll("'", "\'") + "','"
          + searchStatus.getStatusType().replaceAll("'", "\'") + "');";
  script += "</script>";
  
  try {

    if (this._stopFurtherResponseWrites == false) {

      

      PrintWriter writer = this.getServletResponse().getWriter();
      // There were some connection reset errors sporadically while flushing the
      // buffer. synchronization is is for good measure
      synchronized (this) {
        try {
          writer.write(script);
          this.getServletResponse().flushBuffer();
        } catch (Exception e) {
          LOG.log(Level.WARNING, "Error while flushing + " + e.getMessage());
        }
      }

      if (LOG.isLoggable(Level.FINER)) {
        script += "// Time: " + System.currentTimeMillis() + " Thread # "
            + Thread.currentThread().getId() + "\n";
        script += "Writer = " + writer.hashCode() + "\n";
        script += "Response = " + this.getServletResponse().hashCode();
        LOG.finer("Sending " + script);
      }
    } else {
      LOG.finer("Response Already commited."
          + " Stopped from writting to the response message = "
          + script
          + " Search Time so far : "
          + (System.currentTimeMillis() - searchStatus.getStartTimestamp()
              .getTime()));
    }

  } catch (Throwable e) {
    LOG.log(Level.WARNING, "Error while writting distributed search message: "
        + message, e);
  }

}

/**
 * Signals all threads are done processing.
 * 
 * @see com.esri.gpt.control.rest.search.ISearchListener#searchDone()
 */
public void searchDone() {
  String script = "<script type=\"text/javascript\">";
  script += "window.parent.distributedSearchDone();";
  script += "</script>";

  //HttpServletRequest request = this.getServletRequest();
  if (LOG.isLoggable(Level.FINER)) {
    script += "// Time: " + System.currentTimeMillis() + " Thread # "
        + Thread.currentThread().getId() + "\n";
    LOG.finer("Sending " + script);
  }
  // synchronized(request) {
  //if (request.getAttribute(ATTRIBUTE_HTML_END) == null) {
    //request.setAttribute(ATTRIBUTE_HTML_END, new Object());
    script += "</head><body></body></html>";
    try {
      this._stopFurtherResponseWrites = true;
      PrintWriter printWriter = this.getServletResponse().getWriter();
      printWriter.write(script);
      // printWriter.flush();
    } catch (Throwable e) {
      LOG.log(Level.WARNING, "Error while closing distributed search", e);
    }
  //}
  // }
}

}
