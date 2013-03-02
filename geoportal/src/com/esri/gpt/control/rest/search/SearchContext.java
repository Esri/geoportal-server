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
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import javax.servlet.http.HttpServletRequest;

/**
 * Search context.
 */
public class SearchContext {
  
  // instance variables ======================================================  
  private HttpServletRequest httpRequest;
  private int                maxSearchTime;
  private MessageBroker      messageBroker;
  private RestQuery          restQuery;
  private StringSet          rids;
  private SearchCriteria     searchCriteria;
  private RequestContext     requestContext;

 
  // properties ==============================================================
  
  
  
  /**
   * Gets the HTTP request. 
   * @return the HTTP request
   */
  public HttpServletRequest getHttpRequest() {
    return this.httpRequest;
  }
  /**
   * Sets the HTTP request.
   * @param httpRequest the HTTP request
   */
  public void setHttpRequest(HttpServletRequest httpRequest) {
    this.httpRequest = httpRequest;
  }
  
  /**
   * Gets the max search time.
   * 
   * @return the max time
   */
  public int getMaxSearchTime() {
    return maxSearchTime;
  }
  
  /**
   * Sets the max search time.
   * 
   * @param maxTime the new max time
   */
  public void setMaxSearchTime(int maxTime) {
    this.maxSearchTime = maxTime;
  }

  /**
   * Gets the message broker. 
   * @return the message broker
   */
  public MessageBroker getMessageBroker() {
    return this.messageBroker;
  }
  /**
   * Sets the message broker.
   * @param messageBroker the message broker
   */
  public void setMessageBroker(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;
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
  
  /**
   * Gets the rest query. 
   * @return the rest query
   */
  public RestQuery getRestQuery() {
    return this.restQuery;
  }
  /**
   * Sets the rest query.
   * @param restQuery the rest query
   */
  public void setRestQuery(RestQuery restQuery) {
    this.restQuery = restQuery;
  }
  
  /**
   * Gets the RIDs. 
   * @return the RIDs
   */
  public StringSet getRIDs() {
    return this.rids;
  }
  /**
   * Sets the RIDs.
   * @param rids the RIDs
   */
  public void setRIDs(StringSet rids) {
    this.rids = rids;
  }
  
  /**
   * Gets the search criteria. 
   * @return the search criteria
   */
  public SearchCriteria getSearchCriteria() {
    return this.searchCriteria;
  }
  /**
   * Sets the search criteria.
   * @param searchCriteria the search criteria
   */
  public void setSearchCriteria(SearchCriteria searchCriteria) {
    this.searchCriteria = searchCriteria;
  }
  
}
