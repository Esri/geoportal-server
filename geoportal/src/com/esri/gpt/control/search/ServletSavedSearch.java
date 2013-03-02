/**
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.esri.gpt.control.search;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.axis.utils.XMLUtils;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;


import com.esri.gpt.catalog.search.GptRepository;
import com.esri.gpt.catalog.search.ISearchFilter;
import com.esri.gpt.catalog.search.ISearchSaveRepository;
import com.esri.gpt.catalog.search.RestUrlBuilder;
import com.esri.gpt.catalog.search.SavedSearchCriteria;
import com.esri.gpt.catalog.search.SavedSearchCriterias;
import com.esri.gpt.catalog.search.SearchConfig;
import com.esri.gpt.catalog.search.SearchCriteria;
import com.esri.gpt.catalog.search.SearchEngineLocal;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchFilterHarvestSites;
import com.esri.gpt.catalog.search.SearchSaveRpstryFactory;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;


/**
 * Servlet implementation class ServletSavedSearch.
 * 
 * @author TM
 */
public class ServletSavedSearch extends BaseServlet {

// class variables =============================================================

/** The LOG. */
private static Logger LOG = 
  Logger.getLogger(ServletSavedSearch.class.getCanonicalName());

// constructors ================================================================

/**
 * Instantiates a new servlet saved search.
 * 
 * @see HttpServlet#HttpServlet()
 */
public ServletSavedSearch() {
  super();
  // TODO Auto-generated constructor stub
}


// methods =====================================================================

/**
 * Execute.
 * 
 * @param request the request
 * @param response the response
 * @param context the context
 * @throws Exception the exception
 */
@Override
protected void execute(HttpServletRequest request,
    HttpServletResponse response, RequestContext context) throws Exception {
  
  try {
    if(context.getUser().getName().equals("")) {
      throw new AuthenticationException("Login needed");
    }
    if(request.getMethod().toLowerCase().equals("get")) {
      writeSavedSearches(request, response, context);
    } else if(request.getMethod().toLowerCase().equals("put") ||
        request.getMethod().toLowerCase().equals("post")) {
      putSavedSearches(request, response, context);
    } else if(request.getMethod().toLowerCase().equals("delete")) {
      deleteSavedSearch(request, response, context);
    }
    
  } catch(AuthenticationException e) { 
    response.sendError(404, "User principle not found.  Try authenticating.");
    LOG.log(Level.INFO,"Tried to get saved search but principle not found for "
        + request.getRemoteAddr(), 
        e);
  } catch(Throwable e) {
    response.sendError(500, e.getMessage());
    LOG.log(Level.WARNING,"Error while interacting with saved searches", 
        e);
  }
  

}

/**
 * Put saved searches.
 * 
 * @param request the request
 * @param response the response
 * @param context the context
 * @throws SearchException the search exception
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws JSONException Throws json exception
 */
private void putSavedSearches(HttpServletRequest request,
    HttpServletResponse response, RequestContext context) 
  throws SearchException, IOException, JSONException {
  
  ISearchSaveRepository saveRpstry = 
    SearchSaveRpstryFactory.getSearchSaveRepository();
  if(saveRpstry instanceof GptRepository) {
	  ((GptRepository) saveRpstry).setRequestContext(context);
  }
    
  SavedSearchCriterias savedSearchCriterias = 
    saveRpstry.getSavedList(context.getUser());
  if(savedSearchCriterias.size() >= 
    SearchConfig.getConfiguredInstance().getMaxSavedSearches()) {
    MessageBroker messageBroker = 
      new FacesContextBroker(request,response).extractMessageBroker();
    String message = 
      messageBroker.getMessage("catalog.search.error.maxSavedSearchesReached")
        .getSummary();
    writeSavedSearches(request, response, context, message);
    return;
  }
  
  String body = IOUtils.toString(request.getInputStream(), "UTF-8");
  Map<String, String> paramMap = urlToParamMap(body);
  
  String name = "";
  String criteria = "";
  name = paramMap.get("name");
  if("".equals(Val.chkStr(name))) {
    MessageBroker messageBroker = 
      new FacesContextBroker(request,response).extractMessageBroker();
    String message = 
      messageBroker.getMessage("catalog.search.savedSearches.noSaveName")
        .getSummary();
    writeSavedSearches(request, response, context, message);
    return;
  }
  criteria = Val.chkStr(paramMap.get("criteria"));
  if(criteria.contains("?")) {
    criteria = criteria.substring(criteria.indexOf("?"));
    criteria = criteria.replace("?", "");
  }
  
  saveRpstry.save(name, criteria, context.getUser());
  writeSavedSearches(request, response, context);
  
}

/**
 * Delete saved search.
 * 
 * @param request the request
 * @param response the response
 * @param context the context
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws SearchException the search exception
 * @throws JSONException the jSON exception
 */
private void deleteSavedSearch(HttpServletRequest request,
    HttpServletResponse response, RequestContext context) 
throws IOException, SearchException, JSONException {
  String url = IOUtils.toString(request.getInputStream(), "UTF-8");
  Map<String, String> urlParams = urlToParamMap(url);
  String id = Val.chkStr(urlParams.get("id"));
  if(id.equals("")) {
    id = Val.chkStr(request.getParameter("id"));
  }
  ISearchSaveRepository saveRpstry = 
    SearchSaveRpstryFactory.getSearchSaveRepository();
  if(saveRpstry instanceof GptRepository) {
	  ((GptRepository) saveRpstry).setRequestContext(context);
  }
  saveRpstry.delete(id, context.getUser());
  writeSavedSearches(request, response, context);
  
}

/**
 * Write saved searches.
 * 
 * @param request the request
 * @param response the response
 * @param context the context
 * @throws SearchException the search exception
 * @throws JSONException the jSON exception
 * @throws IOException Signals that an I/O exception has occurred.
 */
private void writeSavedSearches(HttpServletRequest request,
    HttpServletResponse response, RequestContext context) 
  throws SearchException, JSONException, IOException {
  writeSavedSearches(request, response, context, null);
}

/**
 * Write saved searches.
 * 
 * @param request the request
 * @param response the response
 * @param context the context
 * @param errorMessage the error message
 * @throws SearchException the search exception
 * @throws JSONException the jSON exception
 * @throws IOException Signals that an I/O exception has occurred.
 */
private void writeSavedSearches(HttpServletRequest request,
    HttpServletResponse response, RequestContext context, String errorMessage) 
  throws SearchException, JSONException, IOException {

  ISearchSaveRepository saveRpstry = 
    SearchSaveRpstryFactory.getSearchSaveRepository();
  if(saveRpstry instanceof GptRepository) {
	  ((GptRepository) saveRpstry).setRequestContext(context);
  }
  SavedSearchCriterias savedCriterias = 
    saveRpstry.getSavedList(context.getUser());
  JSONArray resultsArray = new JSONArray();
  for(SavedSearchCriteria savedCriteria: savedCriterias){
    JSONObject jObj = new JSONObject();
    jObj.put("id", savedCriteria.getId());
    jObj.put("name", savedCriteria.getName());
        
    String criteria = savedCriteria.getCriteria();
    try {
      InputStream inputStream = new ByteArrayInputStream(criteria.getBytes());
      Document doc = XMLUtils.newDocument(new InputSource(inputStream));
      SearchCriteria searchCriteria = new SearchCriteria(doc);
      MessageBroker messageBroker = 
        new FacesContextBroker(request,response).extractMessageBroker();
      RestUrlBuilder builder = RestUrlBuilder.newBuilder(context,request,
          messageBroker);
      String id = SearchEngineLocal.ID;
      for(ISearchFilter filter : searchCriteria.getMiscelleniousFilters()) {
        if(filter instanceof SearchFilterHarvestSites) {
          SearchFilterHarvestSites hFilter = (SearchFilterHarvestSites) filter;
          id = hFilter.getSelectedHarvestSiteId();
        }
      }
      String params = builder.buildParameters(searchCriteria,
          "searchPage",
          id);
      criteria = params;
    } catch(Exception e) {
      LOG.log(Level.FINER, "" , e);
    }
    jObj.put("criteria", criteria);
    resultsArray.put(jObj);
    
  }
  String contentType = "application/json";
  int indent = 0;
  if(Val.chkStr(request.getParameter("f")).equals("pjson")) {
    indent = 2;
    contentType = "text/plain";
  } 
  JSONObject resultObj = new JSONObject();
  errorMessage = Val.chkStr(errorMessage);
  if(!errorMessage.equals("")) {
      resultObj.put("error", errorMessage);
  }
  
  resultObj.put("resultRecords", resultsArray);
  String content = resultObj.toString(indent);
  String callBack = Val.chkStr(request.getParameter("callBack"));
  if(callBack.equals("") == false) {
    content += callBack + "("+ content +")";
  }
  writeCharacterResponse(
      response, 
      content, 
      "UTF-8", 
      contentType);
  
}

/**
 * Url to param map.
 * 
 * @param url the url
 * @return the map
 * @throws UnsupportedEncodingException the unsupported encoding exception
 */
private Map<String, String> urlToParamMap(String url) 
  throws UnsupportedEncodingException {
  
  if(url.indexOf("?") >= 0) {
    url = url.replace("?", "");
  }
  String kvparams[] = url.split("&");
  Map<String, String> paramMap = 
    new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
  for(String kvpParam: kvparams) {
    String kvp[] = kvpParam.split("=");
    if(kvp.length < 2) {
      continue;
    }
    String key = Val.chkStr(kvp[0]);
    String value = URLDecoder.decode(Val.chkStr(kvp[1]), "UTF-8");
    if(key.equals("")) {
      continue;
    }
    paramMap.put(key, value);
  }
  return paramMap;
}

/**
 * Do delete.
 * 
 * @param req the req
 * @param resp the resp
 * @throws ServletException the servlet exception
 * @throws IOException Signals that an I/O exception has occurred.
 */
@Override
protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
  
  super.doGet(req, resp);
}

/**
 * Do put.
 * 
 * @param req the req
 * @param resp the resp
 * @throws ServletException the servlet exception
 * @throws IOException Signals that an I/O exception has occurred.
 */
@Override
protected void doPut(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
  // TODO Auto-generated method stub
  super.doGet(req, resp);
}


}
