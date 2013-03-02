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

import java.util.EventObject;

/**
 * The Class DistributedSearchEvent.
 */
@SuppressWarnings("serial")
public class DistributedSearchEvent extends EventObject {

// instance variables ==========================================================
/** The search status. */
private SearchStatus searchStatus;

// constructors ================================================================
/**
 * Instantiates a new distributed search event.
 * 
 * @param source the source
 * @param status the status
 */
public DistributedSearchEvent(Object source, SearchStatus status) {
    super(source);
    this.searchStatus = status;
}

// properties ==================================================================
/**
 * Gets the search status.
 * 
 * @return the search status
 */
public SearchStatus getSearchStatus() {
  return searchStatus;
}

}
