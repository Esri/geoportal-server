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
 * Class representing keyword filter in search.
 */
@SuppressWarnings("serial")
public class SearchFilterKeyword implements ISearchFilter, ISearchFilterKeyword 
{

// class variables =============================================================

/**
 * The parameter keys for saving *.
 */
private static enum KeySaveParams {

/** The Search text. */
SearchText, // Parameter for searchText
/** The Search text option. */
SearchTextOption
// Parameter for searchTextOption
}

/** Class Logger *. */
private final static Logger LOG      = Logger
                                         .getLogger(SearchFilterKeyword.class
                                                 .getCanonicalName());

// instance variables ==========================================================
/** The search text. */
private String              _searchText           = "";

/** Key of selected search option. * */
private String              _selectedSearchOption = "";

// constructors ================================================================
/**
 * Instantiates a new search filter keyword.
 */
public SearchFilterKeyword() {
  super();
  reset();
}

// properties ==================================================================
/**
 * Gets the search text.
 * 
 * @return the search text (Trimmed, Never Null)
 */
public String getSearchText() {
  return Val.chkStr(_searchText);
}

/**
 * Sets the search text.
 * 
 * @param text the new search text
 */
public void setSearchText(String text) {

  if (LOG.isLoggable(Level.INFO)) {
    LOG.fine("search text value posted " + text);
  }

  _searchText = text;

}


/**
 * Gets search option as enum.
 * @return search option as enum
 */
public KeySearchTextOptions getSearchOptionAsEnum() {
  return KeySearchTextOptions.checkValueOf(getSelectedSearchOption());
}

/**
 * Sets search option as enum.
 * @param option search option as enum
 */
public void setSearchOptionAsEnum(KeySearchTextOptions option) {
  setSelectedSearchOption(option.name());
}

/**
 * Gets the selected search option.
 * 
 * @return the selected search option (Trimmed, Never Null, set to "exact" as
 * default)
 */
public String getSelectedSearchOption() {

  
  if (_selectedSearchOption == null || "".equals(_selectedSearchOption)) {
    _selectedSearchOption = KeySearchTextOptions.exact.name();
  }
  if(_selectedSearchOption.equals(KeySearchTextOptions.any.name())) {
    return KeySearchTextOptions.exact.name();
  }

  return Val.chkStr(_selectedSearchOption);
}

/**
 * Sets the selected search option.
 * 
 * @param selectedSearchOption the new selected search option.
 * 
 * @throws IllegalArgumentException if searchOption is not in KeySearchTextOptions
 */
public void setSelectedSearchOption(String selectedSearchOption) {
  if (LOG.isLoggable(Level.INFO)) {
    LOG.finer("selected search option " + selectedSearchOption);
  }
  
  this._selectedSearchOption = Val.chkStr(selectedSearchOption);
  

}

// methods =====================================================================
/**
 * Reset.
 */
public void reset() {
  this.setSearchText("");
  this.setSelectedSearchOption(KeySearchTextOptions.any.name());

}

/**
 * Tests values and if invalid value is found throws exception.
 * 
 * @throws SearchException the search exception
 * 
 * @see com.esri.gpt.catalog.search.ISearchFilter#validate()
 */
public void validate() throws SearchException {

  try {
    KeySearchTextOptions.valueOf(this.getSelectedSearchOption());

  } catch (Exception e) {
    throw new IllegalArgumentException(
        "search text value passed not valid : option = "
            + this.getSelectedSearchOption());
  }

}

/**
 * Gets parameters.
 * 
 * @return the map of search parameters
 * 
 * @see com.esri.gpt.catalog.search.ISearchFilter#getParams()
 */
public SearchParameterMap getParams() {
  SearchParameterMap map = new SearchParameterMap();
  map.put(KeySaveParams.SearchText.name(), map.new Value(this.getSearchText(),
      ""));
  map.put(KeySaveParams.SearchTextOption.name(), map.new Value(this
      .getSelectedSearchOption(), ""));
  return map;
}

/**
 * Sets parameters.
 * 
 * @param parameterMap the map of search parameters
 */
public void setParams(SearchParameterMap parameterMap) {
  if(parameterMap.get(KeySaveParams.SearchText.name()) != null) {
    this.setSearchText(parameterMap.get(KeySaveParams.SearchText.name())
        .getParamValue());
  }
  if(parameterMap.get(KeySaveParams.SearchTextOption.name()) != null) {
    this.setSelectedSearchOption(parameterMap.get(
        KeySaveParams.SearchTextOption.name()).getParamValue());
  }

}

/**
 * Checks if two objects are equal.
 * 
 * @param obj object to compare
 * 
 * @return <code>true</code> if both object are equal
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
  if (!(obj instanceof SearchFilterKeyword)) {
    return false;
  }
  SearchFilterKeyword kWrd = (SearchFilterKeyword) obj;
  return this.getParams().equalsSubset(kWrd.getParams());
}
/**
 * Creates hash code from the content of the object.
 * 
 * @return hash code
 */
@Override
public int hashCode() {
  int hash = 3;
  hash = 23 * hash
      + (this._searchText != null ? this._searchText.hashCode() : 0);
  hash = 23
      * hash
      + (this._selectedSearchOption != null ? this._selectedSearchOption
          .hashCode() : 0);
  return hash;
}

/**
 * Creates string representation of the object.
 * 
 * @return string representation
 */
@Override
public String toString() {
  return "\n{=======================\n" + this.getClass().getCanonicalName() +
  this.getParams().toString()
  + "\n===========================}";
}


/**
 * String representation.
 * 
 * @return the string
 *
private String stringRepresentation() {
  StringBuffer string = new StringBuffer();
  string.append("\n{ searchText = ").append(this.getSearchText()).append(" , ")
      .append(" selectedSearchOption = ")
      .append(this.getSelectedSearchOption()).append(" } \n");
  return string.toString();
  
}*/


}
