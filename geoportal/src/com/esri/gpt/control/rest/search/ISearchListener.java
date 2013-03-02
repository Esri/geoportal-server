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

import java.util.EventListener;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esri.gpt.framework.jsf.MessageBroker;

/**
 * Interface for a search thread listener.
 */
public interface ISearchListener extends EventListener {

// properties ==================================================================

/**
 * Sets the servlet request.
 * 
 * @param request the new servlet request
 */
public void setServletRequest(HttpServletRequest request);

/**
 * Sets the servlet respone.
 * 
 * @param response the new servlet respone
 */
public void setServletResponse(HttpServletResponse response);

// methods =====================================================================

/**
 * Fired when a search thread has an event to fire.
 * 
 * @param event the event
 * @throws Exception the exception
 */
public void searchEvent(DistributedSearchEvent event) throws Exception;

/**
 * All threads done.
 * 
 * @throws Exception the exception
 */
public void searchDone() throws Exception;


/**
 * Sets the message broker.
 * 
 * @param messageBroker the new message broker
 */
public void setMessageBroker(MessageBroker messageBroker);

/**
 * Called before a search to do some initializations.
 * 
 * @throws Exception the exception
 */
public void initBeforeSearch() throws Exception;

}
