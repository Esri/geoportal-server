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



/**
 *  The Class SearchSaveRepository. Creates a repository
 * object to be used for search storage.
 */
public class SearchSaveRpstryFactory {

// constructor =================================================================
/**
 * Utility class. Cannot be instantiated.
 */
private SearchSaveRpstryFactory(){
  
}

// class methods ===============================================================
/**
 * Gets the search repository instance object to be used by application.
 * 
 * @return the search personalization instance
 */
public static ISearchSaveRepository getSearchSaveRepository(){
  ISearchSaveRepository repository = new GptRepository();
    return new GptRepository();
}

}
