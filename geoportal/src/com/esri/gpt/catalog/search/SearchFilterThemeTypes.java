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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * The Class SearchFilterTheme.
 * @author TM
 */
@SuppressWarnings("serial")
public class SearchFilterThemeTypes implements ISearchFilter, 
  ISearchFilterThemes {

// Class Variables =============================================================

/** class logger **/
@SuppressWarnings("unused")
private static final Logger LOG = 
  Logger.getLogger(SearchFilterThemeTypes.class.getCanonicalName());

/** Delimeter that will be used for getSearchParams value */
public static String DELIM_SEARCHTEME_PARAM = "|";

/**
 * The Enum Theme Params.
 */
private static enum ThemeSaveParams {
  
 /** The THEME. */
theme

}

// Instance Variables ==========================================================
/** The selected theme. */
@SuppressWarnings("unchecked")
private List selectedThemes;

/**
 * Gets the selected theme.
 * 
 * @return the selected theme
 */
@SuppressWarnings("unchecked")
public List getSelectedThemes() {
  if(selectedThemes == null) {
    selectedThemes = new LinkedList();
  }
  return selectedThemes;
}

/**
 * Sets the selected theme.
 * 
 * @param selectedThemes the new selected theme
 */
@SuppressWarnings("unchecked")
public void setSelectedThemes(List selectedThemes) {
  this.selectedThemes = selectedThemes;
}

// Methods =====================================================================
/**
 * Checks if Object given is equal to <b>this</b>.
 * @param obj
 * @return true if equal, false if not
 */
public boolean isEquals(Object obj) {
  if(!(obj instanceof SearchFilterThemeTypes)) {
    return false;
  }
  SearchFilterThemeTypes  foreign = (SearchFilterThemeTypes) obj;
  return this.getParams().equalsSubset(foreign.getParams());
}

/**
 * Checks if Object given is equal to <b>this</b>.
 * @param obj
 * @return true if equal, false if not
 */
@Override
public boolean equals(Object obj) {
  return isEquals(obj);
  
}

/**
 * Resets <b>this</b>.
 */
public void reset() {
  this.setSelectedThemes(null);

}

/**
 * Gets the map parameters representing saved state
 * @return SearchParameterMap (never null)
 */
@SuppressWarnings("unchecked")
public SearchParameterMap getParams() {
  SearchParameterMap searchParams = new SearchParameterMap();
  if(this.getSelectedThemes().size() < 1) {
    return searchParams;
  }
  
  Iterator iter = this.getSelectedThemes().listIterator();
  String themeString="";
  while(iter.hasNext()) {
    Object obj = iter.next();
    if(obj == null) {
      continue;
    }
    themeString += (obj instanceof String)? (String) obj : obj.toString();
    themeString += ((iter.hasNext())? DELIM_SEARCHTEME_PARAM : "");
  }
  
  searchParams.put(ThemeSaveParams.theme.name(), 
      searchParams.new Value(themeString));
  return searchParams;
}

/**
 * Restores <b>this</b> to state stored by parameter map
 * @param parameterMap (can be null) 
 * @throws SearchException 
 */
@SuppressWarnings("unchecked")
public void setParams(SearchParameterMap parameterMap) throws SearchException {
  if(parameterMap == null) {
    this.setSelectedThemes(null);
    return;
  }
  SearchParameterMap.Value value = parameterMap.get(
      ThemeSaveParams.theme.name());
  if(value == null || value.getParamValue() == null) {
    this.setSelectedThemes(null);
    return;
  }
  
  String themes[] = value.getParamValue().split("\\" + DELIM_SEARCHTEME_PARAM);
  this.setSelectedThemes(Arrays.asList(themes));
  
}

/**
 * Currently empty.
 * @throws SearchException
 */
public void validate() throws SearchException {
 

}

/**
 * String representation of class
 * @return string representation
 */
@Override
public String toString(){
  return "\n{=======================\n" + "Class representation " +
    this.getParams().toString()
    + "\n===========================}";
}

}

