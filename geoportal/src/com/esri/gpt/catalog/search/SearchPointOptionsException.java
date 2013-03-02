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
 * The Class SearchPointOptionsException.  Exception thrown when more 
 * information is needed by a search engine in order for it to be able to
 * consume the URL.
 */
@SuppressWarnings("serial")
public class SearchPointOptionsException extends SearchException {

// instance variables ==========================================================
/** The search point options. */
private SearchPointOptions searchPointOptions;

/** The search engine. */
private ASearchEngine searchEngine;

// constructors ================================================================
/**
 * Instantiates a new search point options exception.
 * 
 * @param errorMessage the error message
 * @param searchPointOptions the search point options (could be null)
 * @param engine the engine (could be null)
 */
public SearchPointOptionsException(String errorMessage, 
    SearchPointOptions searchPointOptions, ASearchEngine engine) {
  super(errorMessage);
  this.searchEngine = engine;
  this.searchPointOptions = searchPointOptions;
  
}

// properties ==================================================================
/**
 * Gets the search point options.
 * 
 * @return the search point options (could be null)
 */
public SearchPointOptions getSearchPointOptions() {
  return searchPointOptions;
}

/**
 * Gets the search engine.
 * 
 * @return the search engine (could be null)
 */
public ASearchEngine getSearchEngine() {
  return searchEngine;
}

}
