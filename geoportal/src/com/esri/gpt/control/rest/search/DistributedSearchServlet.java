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

import com.esri.gpt.catalog.discovery.rest.RestQuery;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.catalog.search.SearchResultRecord;
import com.esri.gpt.catalog.search.SearchResultRecords;
import com.esri.gpt.control.georss.FeedWriter;
import com.esri.gpt.control.georss.RestQueryServlet;
import com.esri.gpt.control.search.SearchController;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.IOUtility;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

/**
 * End-point for distributed search requests.
 */
@SuppressWarnings("serial")
public class DistributedSearchServlet extends RestQueryServlet {

// class variables =============================================================
/** class logger **/
private static Logger LOG = Logger.getLogger(DistributedSearchServlet.class
    .getCanonicalName());

/** Default max search time  TODO: Get it from SearchConfig **/
private static int DEFAULT_MAX_SEARCH_TIME = 5000;

// instance variables ==========================================================

/** constructors ============================================================ */

/** Default constructor. */
public DistributedSearchServlet() {
}

/** methods ================================================================= */

/**
 * Processes the HTTP request.
 * 
 * @param request
 *          the HTTP request.
 * @param response
 *          HTTP response.
 * @param context
 *          request context
 * @throws Exception
 *           if an exception occurs
 */
@Override
public void execute(HttpServletRequest request, HttpServletResponse response,
    RequestContext context) throws Exception {
  
  LOG.finer("Handling rest query string=" + request.getQueryString());

  MessageBroker msgBroker = new FacesContextBroker(request, response)
      .extractMessageBroker();

  // parse the query
  RestQuery query = null;
  PrintWriter printWriter = null;

  
  try {
   
    query = parseRequest(request, context);
    if (query == null)
      query = new RestQuery();
    this.executeQuery(request, response, context, msgBroker, query);
      
  } catch (Exception e) {
    LOG.log(Level.SEVERE, "Error executing query.", e);
    String msg = Val.chkStr(e.getMessage());
    if (msg.length() == 0)
      msg = e.toString();
    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
  } finally {
    printWriter = response.getWriter();
    if (printWriter != null) {
      try {
        try {
          printWriter.flush();
        } catch (Throwable e) {
          LOG.log(Level.FINE, "Error while flushing printwriter", e);
        }
      } catch (Throwable t) {
        LOG.log(Level.FINE, "Error while closing printwriter", t);
      }
    }
   
  }

}

/**
 * 
 * @see com.esri.gpt.control.georss.RestQueryServlet#executeQuery(javax.servlet.http.HttpServletRequest, com.esri.gpt.framework.context.RequestContext, com.esri.gpt.framework.jsf.MessageBroker, com.esri.gpt.catalog.discovery.rest.RestQuery)
 */
protected SearchResultRecords executeQuery(HttpServletRequest request,
    HttpServletResponse response,
    RequestContext context, MessageBroker msgBroker, RestQuery query)
    throws SearchException {

  // determine the rids
  StringSet rids = this.getRids(request);

  // Get the criteria
  SearchCriteria criteria = this.toSearchCriteria(request, context, query);

  SearchResultRecords searchResultRecords = new SearchResultRecords();
  try {

    // make the search context
    SearchContext searchContext = new SearchContext();
    searchContext.setHttpRequest(request);
    searchContext.setMessageBroker(msgBroker);
    searchContext.setRestQuery(query);
    searchContext.setRIDs(rids);
    searchContext.setSearchCriteria(criteria);
    searchContext.setRequestContext(context);
    
    int maxSearchTime = 
      Val.chkInt(getRequestParameter(request,"maxSearchTimeMilliSec"), 
          DEFAULT_MAX_SEARCH_TIME);
    if(maxSearchTime <= 0) {
      maxSearchTime = DEFAULT_MAX_SEARCH_TIME;
    }
    searchContext.setMaxSearchTime(maxSearchTime);

    // execute the search
    ISearchListener sp = null;
    String sFormat = getRequestParameter(request,"f");
    if (sFormat.equalsIgnoreCase("searchpage")) {
      sp = new DistributedAdpSearchPageWriter();
    } else if (sFormat.equalsIgnoreCase("atom")) {
      sp = new DistributedAdpAtomSearchPageWriter();
    } else if (sFormat.equalsIgnoreCase("html")) {
      sp = new DistributedAdpHtmlWriter();
    } else {
      throw new Exception("Format f= not understood"); 
    }
    sp.setServletRequest(request);
    sp.setServletResponse(response);
    sp.setMessageBroker(msgBroker);
    
    DistributedSearchDispatcher distributedSearch = new DistributedSearchDispatcher();
    distributedSearch.setSearchContext(searchContext);
    distributedSearch.addActionListener(sp);
    distributedSearch.search();
       
  } catch (Exception e) {
    throw new SearchException(e.getMessage(), e);
  }
  return searchResultRecords;

}

/**
 * Gets the RIDs specified within the request.
 * 
 * @param request
 *          the HTTP request
 * @return the RIDs
 */
@SuppressWarnings("unchecked")
private StringSet getRids(HttpServletRequest request) {
  StringSet rids = new StringSet();
  Map<String, String[]> requestParameterMap = request.getParameterMap();
  for (Map.Entry<String, String[]> e : requestParameterMap.entrySet()) {
    if (e.getKey().equalsIgnoreCase("rids")) {
      String[] values = e.getValue();
      if (values != null) {
        for (String tokens : values) {
          StringTokenizer st = new StringTokenizer(tokens, ",");
          while (st.hasMoreElements()) {
            String value = Val.chkStr((String) st.nextElement());
            if (value.length() > 0) {
              try {
                rids.add(URLDecoder.decode(value, "UTF-8"));
              } catch (UnsupportedEncodingException e1) {
                // Should never happen
                LOG.log(Level.WARNING, "Could not decde uuid", e1);
              }
            }
          }
        }
      }
    }
  }
  
  String arrRids[] = request.getParameterValues("rid");
  for(int i = 0; arrRids != null && i < arrRids.length; i++) {
    rids.add(arrRids[i]);
  }
  return rids;
}

}
