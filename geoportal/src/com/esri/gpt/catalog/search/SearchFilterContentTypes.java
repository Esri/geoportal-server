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
 * The Class SearchFilterContentTypes.
 */
@SuppressWarnings("serial")
public class SearchFilterContentTypes implements ISearchFilterContentTypes {

// class variables =============================================================
/**
 * The Enum ParamKey.
 */ 
private static enum ParamKey {

/** The selected content type. */
selectedContentType

}

/** Class logger */
private static Logger LOG = 
    Logger.getLogger(SearchFilterContentTypes.class.getCanonicalName());

// instance variables ==========================================================
/** The selected content type. */
private String selectedContentType;

// constructor =================================================================
/**
 * Instantiates a new search filter content types.
 */
public SearchFilterContentTypes() {
  reset();
}

// properties ==================================================================

/**
 * Gets the selected content type.
 * 
 * @return the selected content type
 */
public String getSelectedContentType() {
  return Val.chkStr(selectedContentType);
}

/**
 * Sets the selected content type.
 * 
 * @param selectedContentType the new selected content type
 */
public void setSelectedContentType(String selectedContentType) {
  selectedContentType = Val.chkStr(selectedContentType);
  try{
    SearchEngineCSW.AimsContentTypes.valueOf(selectedContentType);
  } catch(IllegalArgumentException e){
    LOG.log(Level.FINER, "selectedContentType = "
        +selectedContentType +"is not an an arcIMS content type");
  }
  this.selectedContentType = selectedContentType;
}

// methods =====================================================================
/**
 * Gets parameters serializing the class
 * @return SearchParameterMap with class values
 */
public SearchParameterMap getParams() {
  SearchParameterMap map = new SearchParameterMap();
  map.put(ParamKey.selectedContentType.name(), 
      map.new Value(this.getSelectedContentType()));
  return map;
}

/**
 * Tells us if this is equal to obj
 * @param obj Object parameter to test for equality with this
 * @return <code>true</code> if equals
 */
public boolean isEquals(Object obj) {
  if(!(obj instanceof SearchFilterContentTypes)){
    return false;
  }
  return this.getParams().equalsSubset
    (((SearchFilterContentTypes)obj).getParams());
}

/**
 * Resets this instance
 */
public void reset() {
  this.setSelectedContentType(null);

}

/**
 * Inflates this with the parameters in the parameter map
 * @param parameterMap
 * @throws SearchException
 */
public void setParams(SearchParameterMap parameterMap) throws SearchException {
  SearchParameterMap.Value value = 
    parameterMap.get(ParamKey.selectedContentType.name());
  if(value != null) {
    this.setSelectedContentType(value.getParamValue());
  }
 
}

/**
 * @return String representation of class
 */
@Override
public String toString() {
  return "\n{=======================\n" + this.getClass().getCanonicalName() +
  this.getParams().toString()
  + "\n===========================}";
}

/**
 * Validates if input values for this object are fine
 * @throws SearchException on invalid state
 */
public void validate() throws SearchException {
  // TODO Auto-generated method stub

}



}
