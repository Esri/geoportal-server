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
package com.esri.gpt.catalog.search;

import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;

/**
 * The Class SearchSaveBean.  Bean representing a saved search criteria.
 */
public class SavedSearchCriteria {

// instance variables ==========================================================
/** The repository. */
private ISearchSaveRepository repository;

/** The search criteria. */
private SearchCriteria searchCriteria;

/** The id. */
private String id;

/** The name. */
private String name;

/** The user associated with criteria **/
private User user;

/** The xml or rest criteria **/
private String criteria;


// constructor =================================================================
/**
 * Initiated by the class saving the criteria into the db.
 * 
 * @param name the name
 * @param criteria the criteria
 */
public SavedSearchCriteria(String name, SearchCriteria criteria, User user){
  this.setName(name);
  this.setSearchCriteria(criteria);
  this.setUser(user);
  
}

/**
 * Initiated by a a method that know the repository object so that
 * the searchCriteria can be fetched later on from the repository if
 *  (Lazy loading of search criteria from repository).
 * 
 * @param id The id that will be used to get the document from the repository
 * @param name The name associated with the record
 * @param repository to use for fetching the the criteria when getSearchCriteria is called
 */
public SavedSearchCriteria(String id, String name, 
                                               ISearchSaveRepository repository) 
{
   this.setId(id);
   this.setName(name);
   this.setRepository(repository);
}


// properties ==================================================================
/**
 * Gets the criteria. May be xml criteria or rest url criteria
 * 
 * @return the criteria (trimmed, never null)
 */
public String getCriteria() {
  return Val.chkStr(criteria);
}

/**
 * Sets the criteria.
 * 
 * @param criteria the new criteria
 */
public void setCriteria(String criteria) {
  this.criteria = criteria;
}

/**
 * Gets the user.
 * 
 * @return the user
 */
public User getUser() {
  return user;
}

/**
 * Sets the user.
 * 
 * @param user the new user
 */
public void setUser(User user) {
  this.user = user;
}

/**
 * Gets the id.
 * 
 * @return the id (never null)
 */
public String getId() {
  return Val.chkStr(id);
}

/**
 * Sets the id.
 * 
 * @param id the new id
 */
private void setId(String id) {
  this.id = id;
}

/**
 * Gets the name.
 * 
 * @return the name (never null)
 */
public String getName() {
  return Val.chkStr(name);
}

/**
 * Sets the name.
 * 
 * @param name the new name
 */
private void setName(String name) {
  this.name = name;
}

/**
 * Gets the repository.
 * 
 * @return the repository
 */
private ISearchSaveRepository getRepository() {
  return this.repository;
}

/**
 * Sets the repository.
 * 
 * @param repository the new repository
 */
private void setRepository(ISearchSaveRepository repository) {
  this.repository = repository;
}

/**
 * Sets the search criteria.
 * 
 * @param criteria the new search criteria
 */
private void setSearchCriteria(SearchCriteria criteria) {
  this.searchCriteria = criteria;
}

/**
 * Gets the search criteria.
 * 
 * @return the search criteria
 * 
 * @throws SearchException the search exception
 */
public SearchCriteria getSearchCriteria() throws SearchException {
  if(this.searchCriteria != null){
    return this.searchCriteria;
  }
  if(this.getRepository() == null) {
    throw new SearchException(ISearchSaveRepository.class.getCanonicalName()
        + " missing from " + this.getClass().getCanonicalName());
    
  }
  // TODO: Retrieve
  
  return null;
}

}

