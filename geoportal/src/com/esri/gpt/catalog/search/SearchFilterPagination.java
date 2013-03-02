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

import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.util.Val;

/**
 * The Class SearchFilterPagination.  Filter for controlling page navigation.
 */
@SuppressWarnings("serial")
public class SearchFilterPagination 
  extends PageCursor implements ISearchFilterPagination {

// class variables =============================================================

/**
 * The Parameters used by the getParams method.
 */
private static enum Params {

/** The current page. */
currentPage,

/** The records per page. */
recordsPerPage,

/** The start record. */
startPosition

}
// instance variables ==========================================================
/** The records per page. */
private int recordsPerPage;
/**
 * Start position.
 */
private int startPosition = Integer.MIN_VALUE;
// constructor =================================================================
/**
 * Instantiates a new search filter pagination.
 */
public SearchFilterPagination() {
  reset();
}

// properties ==================================================================
/**
 * Gets the records per page.
 * 
 * @return the results per page
 */
@Override
public int getRecordsPerPage() {
  if(recordsPerPage <= 0) {
    recordsPerPage = 
      SearchConfig.getConfiguredInstance().getResultsPerPage();
  }
  return recordsPerPage;
}

/**
 * Sets the records per page.
 * 
 * @param resultsPerPage the new results per page
 */
@Override
public void setRecordsPerPage(int resultsPerPage) {
  super.setRecordsPerPage(resultsPerPage);
  recordsPerPage = resultsPerPage;
}



// methods =====================================================================
/**
 * Sets the start position.
 */
public void setStartPostion(int startPosition) {
  this.startPosition = startPosition;
  
}

/**
 * Gets the start position.
 * 
 * @return the start position
 */
public int getStartPosition() {
  if(this.startPosition >= 0) {
   
    return this.startPosition;
  }
  return (this.getCurrentPage() - 1) * this.getRecordsPerPage() + 1;
}

/**
 * Gets the params.
 * 
 * @return Map with parameters (never null)
 * 
 * @see com.esri.gpt.catalog.search.ISearchFilter#getParams()
 */

public SearchParameterMap getParams() {
  
  SearchParameterMap map = new SearchParameterMap();
  map.put(Params.currentPage.name(), map.new Value(String.valueOf(this
      .getCurrentPage()), ""));
  map.put(Params.recordsPerPage.name(), map.new Value(String.valueOf(this
      .getRecordsPerPage()), ""));
  map.put(Params.startPosition.name(), map.new Value(String.valueOf(this
      .getStartPosition()), ""));
    
  return map;
}

/**
 * Sets the params.
 * 
 * @param parameterMap the parameter map
 * 
 * @see com.esri.gpt.catalog.search.ISearchFilter#setParams
 */

public void setParams(SearchParameterMap parameterMap) {
  
  reset();
  int temp = 0;
  if(parameterMap.get(Params.currentPage.name()) != null) {
    temp = Val.chkInt(parameterMap.get(Params.currentPage.name())
        .getParamValue(), 1);
    
  } else {
    temp = 1;
  }
  this.setCurrentPage(temp);
  
  if(parameterMap.get(Params.recordsPerPage.name()) != null) {
    temp = Val.chkInt(parameterMap.get(Params.recordsPerPage.name())
        .getParamValue(), 
        SearchConfig.getConfiguredInstance().getResultsPerPage());
  } else {
    temp = SearchConfig.getConfiguredInstance().getResultsPerPage();
  }
  this.setRecordsPerPage(temp);
  if(parameterMap.get(Params.startPosition.name()) != null) {
    temp = Val.chkInt(parameterMap.get(Params.startPosition.name())
        .getParamValue(), Integer.MIN_VALUE);
  }
  this.setStartPostion(temp);
  

}

/**
 * Reset.
 * 
 * @see com.esri.gpt.catalog.search.ISearchFilter#reset()
 */

public void reset() {
  this.setCurrentPage(1);
  this.setRecordsPerPage(SearchConfig.getConfiguredInstance()
      .getResultsPerPage());
  startPosition = Integer.MIN_VALUE;

}

/**
 * Validate.
 * 
 * @throws SearchException the search exception
 * 
 * @see com.esri.gpt.catalog.search.ISearchFilter#validate()
 */
public void validate() throws SearchException {

}

/**
 * @param obj Object for comparison
 * @return true if object is equal to this
 */
@Override
public boolean equals(Object obj) {
  return isEquals(obj);
}

/**
 * Checks if object in argument is equal to <b>this</b>
 * @param obj
 * @return true if equal, false otherwise
 */
public boolean isEquals(Object obj) {
  if(!(obj instanceof SearchFilterPagination)) {
    return false;
  }
  SearchFilterPagination tmpObj = (SearchFilterPagination) obj;
  return this.getParams().equalsSubset(tmpObj.getParams());
}





/**
 * @return String representation
 */
@Override
public String toString() {
  return "\n{=======================\n" + this.getClass().getCanonicalName() +
  this.getParams().toString()
  + "\n===========================}";
}

/*
 * Checks to ensure that the current page is not greater than 
 * the total page count.
 */
/**
 * Check current page.
 */
public void checkCurrentPage() {
  //if (getCurrentPage() > getTotalPageCount()) {
    //setCurrentPage(1);
  //}
}


}
