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

/**
 * The Interface ISearchHibernation.  Represents a repository
 * where the search criteria will be kept for each user who 
 * wants to save a search for a later time.
 */
public interface ISearchSaveRepository {

// methods =====================================================================
/**
 * Saves the criteria onto the repository.
 * 
 * @param savedCriteria the criteria
 *  
 * @throws SearchException the search exception
 */
public void save(SavedSearchCriteria savedCriteria)throws SearchException;



/**
 * Returns the original search criteria from the repository.
 * @param id id of the search
 * @param user user
 * @return search criteria
 * @throws SearchException the search exception 
 */
public SearchCriteria getSearchCriteria(Object id, User user)
throws SearchException;


/**
 * Delete.
 * 
 * @param id the id of the search to be deleted
 * @param user the user user associated with action
 * 
 * @throws SearchException the search exception
 */
public void delete(Object id, User user) 
throws SearchException;


/**
 * Gets the saved list.
 * 
 * @param user the user
 * 
 * @return the saved list
 * 
 * @throws SearchException the search exception
 */
public SavedSearchCriterias getSavedList(User user)throws SearchException;

/**
 * Save.
 * 
 * @param name the name
 * @param restCriteria the rest criteria
 * @param user the user
 * @throws SearchException the search exception
 */
public void save(String name, String restCriteria, User user) 
  throws SearchException; 
}
