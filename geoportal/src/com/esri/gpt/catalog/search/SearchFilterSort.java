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

import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.framework.util.Val;

/**
 * The Class SearchFilterSort.
 */
@SuppressWarnings("serial")
public class SearchFilterSort implements ISearchFilterSort {

//class variables =============================================================
/**
 * 
 * The Enum OptionsSort. Options for sorting.
 */ 
public static enum OptionsSort {

/** The date ascending. */
dateAscending,

/** The date descending. */
dateDescending,

/** The relevance. */
relevance,

/** The name. */
title,

/** The content type. */
format,

/** The area ascending. */
areaAscending,

/** The area descending. */
areaDescending


}
/**
 * The Enum SaveParamKeys.  Parameters for serializiing <b>this</b> to map
 */
private static enum SaveParamKeys {
  
  /** The selected sort. */
  selectedSort
}

private static Logger LOG 
  = Logger.getLogger(SearchFilterSort.class.getCanonicalName());

// instance variables ========================================================== 
/** The user selected sort. */
private String selectedSort;

// constructor =================================================================
/**
 * Instantiates a new search filter sort.
 */
public SearchFilterSort() {
  reset();
}

// properties ==================================================================

/**
 * Gets the user selected sort method.
 * @return the selected sort (trimmed, never null, default = "relevance")
 */
public String getSelectedSort() {
  if(selectedSort == null || "".equals(selectedSort)) {
    this.setSelectedSort(OptionsSort.relevance.name());
  }
  return Val.chkStr(selectedSort);
}

/**
 * Sets the selected sort method.
 * @param selectedSort the new selected sort (if not one of OptionsSort,
 * warning generated and parameter ignored (can be null).
 */
public void setSelectedSort(String selectedSort) {
  Exception exception = null;
  try {
    OptionsSort.valueOf(selectedSort);
    
  } catch(IllegalArgumentException e) {
    exception = e;
  } catch(NullPointerException e) {
    exception = e;
  }
  if(exception != null ) {
    LOG.log(Level.WARNING, "Unknown Sort option has been posted "
        + ": Recieved Sort Option = " + selectedSort );
  }
  this.selectedSort = selectedSort;
}

/**
 * Dehydrates <b>this</b> object into a map
 * @return map with <b>this</b> field parameters to be stored
 * 
 */
public SearchParameterMap getParams() {
  SearchParameterMap map = new SearchParameterMap();
  map.put(SaveParamKeys.selectedSort.name(), 
      map.new Value(this.getSelectedSort()));
  return map;
}

/**
 * Hydrates the object from the parameterMap
 * @param parameterMap
 * 
 */
public void setParams(SearchParameterMap parameterMap) {
 SearchParameterMap.Value value = 
   parameterMap.get(SaveParamKeys.selectedSort.name());
 if(value != null) {
   this.setSelectedSort(value.getParamValue());
 }
}

/**
 *Resets the class to its default values 
 * 
 */
public void reset() {
  this.setSelectedSort(OptionsSort.relevance.name());

}

/**
 * @throws SearchException
 * 
 */
public void validate() throws SearchException {


}

/**
 * @param obj
 * @return true if equal, false otherwise
 * 
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
  if(!(obj instanceof SearchFilterSort)) {
    return false;
  }
  SearchFilterSort tmpObj = (SearchFilterSort) obj;
  return tmpObj.getSelectedSort().equals(this.getSelectedSort());
}

/**
 * @return String representation of object
 * 
 */
@Override
public String toString () {
  return "\n{=======================\n" + this.getClass().getCanonicalName() +
  this.getParams().toString()
  + "\n===========================}";
}


}
