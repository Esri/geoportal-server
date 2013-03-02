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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.model.SelectItem;

import com.esri.gpt.framework.util.Val;

/**
 * The Class SearchFilterConnection.
 */
@SuppressWarnings("serial")
public class SearchFilterConnection implements ISearchFilter, ISearchFilterURI {

//class variables ==============================================================

/** The LOG. */
private static final Logger LOG =
  Logger.getLogger(SearchFilterConnection.class.getCanonicalName());

/**
 * The Enum MAP_PARAMS.
 */
private static enum MAP_PARAMS {

/** The selected uri. */
selectedURI,

/** The Selected connection type (default or other). */
selectedConnectionType

}

/**
 * The Enum OptionConnect.
 */
public static enum OptionConnect {

/** Use default connection. */
useDefaultConnection,

/** Use other connection. */
useOtherConnection

}

// instance variables ==========================================================
/** The selected uri. */
private String selectedUri;

/** The selected connection option. */
private String selectedConnectionOption;

/** The harvest sites. */
private List<SelectItem> harvestSites;

// constructor =================================================================
/**
 * Instantiates a new search filter connection.
 */
public SearchFilterConnection() {
  reset();
}

// properties ==================================================================

/**
 * Gets the selected uri.
 * 
 * @return the selected uri
 */
public String getSelectedUri() {

  if(this.getSelectedConnectionOption().
      equals(OptionConnect.useOtherConnection.name())){
    return Val.chkStr(this.selectedUri);

  } else {
    return getDefaultSearchUri();
  }

}

/**
 * Sets the selected uri.
 * 
 * @param uri the new selected uri
 */
public void setSelectedUri(String uri) {
  this.selectedUri = uri;
}

/**
 * Gets the default search uri.
 * 
 * @return the default search uri
 */
public String getDefaultSearchUri() {

  try {
    return SearchConfig.getConfiguredInstance().getSearchUri()
     .toString(); 
  } catch (SearchException e) {
    LOG.log(Level.SEVERE, "Could not get default search URI connection ", e);  
  }
  return "";
  
}

/**
 * Gets the selected connection option.
 * 
 * @return the selected connection option  (never null)
 */
public String getSelectedConnectionOption() {
  if(selectedConnectionOption == null || "".equals(selectedConnectionOption)) {
    selectedConnectionOption = OptionConnect.useDefaultConnection.name();
  }
  
  return selectedConnectionOption;
}

/**
 * Sets the selected connection option.
 * 
 * @param selectedConnectionOption the new selected connection option
 */
public void setSelectedConnectionOption(String selectedConnectionOption) {
  try {
    OptionConnect.valueOf(selectedConnectionOption);
  } catch(NullPointerException e) {
    LOG.log(
        Level.WARNING, "Selected connection option recieved is " +
        		selectedConnectionOption + "(invalid)" 
        ,e);
   
  } catch(IllegalArgumentException e) {
    LOG.log(
        Level.WARNING, "Selected connection option recieved is " +
            selectedConnectionOption + "(invalid)" 
        ,e);
   
  }
  this.selectedConnectionOption = selectedConnectionOption;
}


// methods =====================================================================



/**
 * Resets this Object.
 */
@Override
public void reset() {
  this.setSelectedUri(null);
  this.setSelectedConnectionOption(OptionConnect.useDefaultConnection.name());
}

/**
 * Object serialized in map
 * @return Map of parameters
 */
@Override
public SearchParameterMap getParams() {
  SearchParameterMap map = new SearchParameterMap();
  map.put(MAP_PARAMS.selectedURI.name(), map.new Value(
      this.getSelectedUri(),
      ""));
  map.put(MAP_PARAMS.selectedConnectionType.name(), map.new Value(
      this.getSelectedConnectionOption(),
      ""));
  return map;
}


/**
 * Hydrate object from map
 * @param parameterMap
 */
@Override
public void setParams(SearchParameterMap parameterMap) {
 
 if(parameterMap.get(MAP_PARAMS.selectedURI.name()) != null){
   this.setSelectedUri(parameterMap.get(MAP_PARAMS.selectedURI.name())
     .getParamValue());
 }
 if(parameterMap.get(MAP_PARAMS.selectedConnectionType.name()) != null) {
   this.setSelectedConnectionOption(
       (parameterMap.get(MAP_PARAMS.selectedConnectionType.name())
     .getParamValue()));
 }

}

@Override
public void validate() throws SearchException {
}

/**
 * Connection to search.
 * @return URI that will be used for search
 * @throws URISyntaxException
 */
  @Override
public URI getSearchURI() throws URISyntaxException {
 
   return new URI(this.getSelectedUri());
    
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


/**
 * @param obj Object to compare with this
 * @return true if obj values is same as this values
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
  if(!(obj instanceof SearchFilterConnection)) {
    return false;
  }
  return this.getParams().equalsSubset(
      ((SearchFilterConnection)obj).getParams());
}




}

