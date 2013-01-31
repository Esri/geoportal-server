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
package com.esri.gpt.control.search;


/**
 * The Class SearchEvents.  Used to differentiate
 * events in the event handler method.
 * 
 */
public class SearchEvents {

// class variables ============================================================= 
/**
 * The Enum Event. 
 */
public static enum Event {

  /** The EVENT. The key for events */
  EVENT,

  /** The Plain search event. */
  EVENT_EXECUTE_SEARCH,

  /** The view metadata details event. */
  EVENT_VIEWMD_DETAILS,

  /** The view metadata in full event. */
  EVENT_VIEWMD_FULL,
  
  /** The request to view results in a certain page. */
  EVENT_GOTOPAGE,
  
  /** The indicator of a new search criteria to be input*/
  EVENT_NEWSEARCHCRITERIA,
  
  /** The indicator of a search criteriato be modified */
  EVENT_MODIFYSEARCHCRITERIA,
  
  /** For reinitiating the search */
  EVENT_REDOSEARCH,
  
  /** Show my searches */
  EVENT_MYSEARCHES,
  
  /** For saving a search */
  EVENT_SAVESEARCH,
  
  /** For loading a saved search */
  EVENT_LOADSAVEDSEARCH,
  
  /** For deleting a saved search */
  EVENT_DELTESAVEDSEARCH,
  
  /** Parameter of a UUID action event */
  PARAM_UUID,
  
  /** PARAM_EXECUTE_SEARCH */
  PARAM_EXECUTE_SEARCH,
  
  /** Catalog to be used */
  PARAM_CATALOG,
  
  /** For downloading a  search */
  EVENT_DOWNLOADSEARCH,
  
  /** For uploading a  search */
  EVENT_UPLOADSEARCH,

  /** For reseting the search */
  EVENT_RESET_SEARCH
}


// properties ==================================================================
/**
 * Gets the event search.
 * 
 * @return the event search
 */
public String getEventExecuteSearch() {
  return Event.EVENT_EXECUTE_SEARCH.name();
}

/**
 * JSF dummy method.
 * 
 * @param dummy the new event search
 */
public void setEventSearch(String dummy) {
}

/**
 * Gets the event key.
 * 
 * @return event key
 */
public String getEvent() {
  return Event.EVENT.toString();
}

/**
 * JSF dummy method.
 * 
 * @param dummy the new event
 */
public void setEvent(String dummy) {
}

/**
 * Gets the event view metadata in full form.
 * 
 * @return the event view md full
 */
public String getEventViewMdFull() {
  return Event.EVENT_VIEWMD_FULL.name();
}

/**
 * Gets the event view metadata in summary form.
 * 
 * @return the event view md summary
 */
public String getEventViewMdDetails() {
  return Event.EVENT_VIEWMD_DETAILS.name();
}

/**
 * Gets the param uuid.
 * 
 * @return the param uuid
 */
public String getParamUuid() {
  return Event.PARAM_UUID.name();
}

/**
 * Gets the event search modify criteria.
 * 
 * @return the search modify
 */
public String getEventModifySearchCriteria() {
  return Event.EVENT_MODIFYSEARCHCRITERIA.name();
}

/**
 * Gets the event new search criteria.
 * 
 * @return the new search
 */
public String getEventNewSearchCriteria() {
  return Event.EVENT_NEWSEARCHCRITERIA.name();
}

/**
 * Gets the event redo search.
 * 
 * @return the event redo search
 */
public String getEventRedoSearch() {
  return Event.EVENT_REDOSEARCH.name();
}

/**
 * Gets the event save search.
 * 
 * @return the event save search
 */
public String getEventSaveSearch(){
  return Event.EVENT_SAVESEARCH.name();
}

/**
 * Gets the event load saved search.
 * 
 * @return the event load saved search
 */
public String getEventLoadSavedSearch(){
  return Event.EVENT_LOADSAVEDSEARCH.name();
}

/**
 * Gets the event param execute search.
 * 
 * @return the event param execute search
 */
public String getParamExecuteSearch() {
  return Event.PARAM_EXECUTE_SEARCH.name();
}

/**
 * Gets the event delete saved search.
 * 
 * @return the event delete saved search
 */
public String getEventDeleteSavedSearch() {
  return Event.EVENT_DELTESAVEDSEARCH.name();
}

/**
 * Gets the event download  search. 
 * @return the event download saved search
 */
public String getEventDownloadSearch(){
  return Event.EVENT_DOWNLOADSEARCH.name();
}

/**
 * Gets the event to show my saved searches.
 * @return the event to show my saved searches
 */
public String getEventMySearches(){
  return Event.EVENT_MYSEARCHES.name();
}

/**
 * Gets the event upload  search.
 * @return the event upload saved search
 */
public String getEventUploadSearch(){
  return Event.EVENT_UPLOADSEARCH.name();
}

/**
 * Gets the param catalog.
 * 
 * @return the param catalog
 */
public String getParamCatalog(){
  return Event.PARAM_CATALOG.name();
}

/**
 * Gets the event reset search.
 * 
 * @return the event reset search
 */
public String getEventResetSearch() {
  return Event.EVENT_RESET_SEARCH.name();
}

}
