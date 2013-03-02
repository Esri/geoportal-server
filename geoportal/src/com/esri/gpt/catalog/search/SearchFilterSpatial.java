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

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;



/**
 * The Class SearchFilterSpatial.
 * Filter specializing in spatial information.
 */
@SuppressWarnings("serial")
public class SearchFilterSpatial 
  implements ISearchFilter, ISearchFilterSpatialObj {


// class variables =============================================================

/** Class logger *. */
private final static Logger LOG = 
  Logger.getLogger(SearchFilterSpatial.class.getCanonicalName());

/**
 * The Enum SaveParams.
 */
public static enum SaveParams {
  
  /** The data inside envelope. */
  selectedBoundOption, 
  
  /** The max x bound. */
  maxX,
  
  /** The max y bound. */
  maxY,
  
  /** The min x bound. */
  minX,
  
  /** The min y bound. */
  minY
}

// instance variables ==========================================================
/** The envelope. */
transient private Envelope _selectedEnvelope = new Envelope();

/** The bound type selected. */
private String _selectedBounds = OptionsBounds.anywhere.name();

/** The envelope. */
transient private Envelope _visibleEnvelope = new Envelope();

// constructor =================================================================
/**
 * Instantiates a new search filter spatial.
 */
public SearchFilterSpatial() { 
  super();
  reset();
}

// properties ==================================================================

/**
 * Gets the user selected envelope.
 * @return the envelope (never null)
 */
public Envelope getSelectedEnvelope() {
  if(this._selectedEnvelope == null) {
    this.setSelectedEnvelope(new Envelope());
  }
  return _selectedEnvelope;
}

/**
 * Sets the user selected envelope.
 * @param envelope the new envelope
 */
public void setSelectedEnvelope(Envelope envelope) {
  _selectedEnvelope = envelope;
  if (_selectedEnvelope == null) _selectedEnvelope = new Envelope();
}

/**
 * Sets the selected bounds options i.e. whether data envelopes are contained, anywhere
 * or intersecting.
 * 
 * @param selectedBounds the new selected bounds
 */
public void setSelectedBoundsAsEnum(OptionsBounds selectedBounds) {
  setSelectedBounds(selectedBounds.name());
}

/**
 * Gets the selected bounds options.
 * 
 * @return the selected bounds
 */
public OptionsBounds getSelectedBoundsAsEnum() {
  return OptionsBounds.checkValueOf(getSelectedBounds());
}

/**
 * Gets the selected bounds.
 * @return the selected bounds (trimmed, never null, default = "anywhere")
 */
public String getSelectedBounds() {
  if(_selectedBounds == null || "".equals(_selectedBounds)) {
    _selectedBounds = OptionsBounds.anywhere.name();
  }
  return Val.chkStr(this._selectedBounds);
}

/**
 * Sets the selected bounds.
 * 
 * @param selectedBounds the new selected bounds
 * 
 * @throws IllegalArgumentException if selectedBounds not in OptionsBounds
 */
public void setSelectedBounds(String selectedBounds) {
  try {
    OptionsBounds.valueOf(selectedBounds);
    this._selectedBounds = selectedBounds;
  } catch(Exception e) {
    this._selectedBounds = OptionsBounds.anywhere.toString();
  }
}

/**
 * Gets the visible envelope (can be projected).
 * @return the visible envelope (never null)
 */
public Envelope getVisibleEnvelope() {
  if(_visibleEnvelope == null) {
    this.setVisibleEnvelope(new Envelope());
  }
  return _visibleEnvelope;
}

/**
 * Sets the visible envelope (can be projected).
 * @param envelope the visible envelope
 */
public void setVisibleEnvelope(Envelope envelope) {
  _visibleEnvelope = envelope;
  if (_visibleEnvelope == null) _visibleEnvelope = new Envelope();
}

// methods =====================================================================

/**
 * Reset <b>this</b> object to default.
 */
public void reset() {
  this.setSelectedEnvelope(new Envelope());
  this.setSelectedBounds(OptionsBounds.anywhere.name());
  this.setVisibleEnvelope(new Envelope());
}
 
/**
 * Validate.
 * 
 * @throws SearchException the search exception
 * 
 * @see com.esri.gpt.catalog.search.ISearchFilter#validate()
 */

public void validate() throws SearchException {
   
  String selectedBounds = this.getSelectedBounds();
  if (selectedBounds == null || "".equals(selectedBounds)) {
    this.setSelectedBounds(OptionsBounds.anywhere.toString());
  }

}

/**
 * Gets the params.
 * @return the params
 * @see com.esri.gpt.catalog.search.ISearchFilter#getParams()
 */
public SearchParameterMap getParams() {
  SearchParameterMap map = new SearchParameterMap();
  map.put(SaveParams.selectedBoundOption.name(), 
      map.new Value(this.getSelectedBounds()));
  
  Envelope envelope = this.getSelectedEnvelope();
  map.put(SaveParams.maxX.name(), map.new Value(String.valueOf(envelope.getMaxX())));
  map.put(SaveParams.maxY.name(), map.new Value(String.valueOf(envelope.getMaxY())));
  map.put(SaveParams.minX.name(), map.new Value(String.valueOf(envelope.getMinX())));
  map.put(SaveParams.minY.name(), map.new Value(String.valueOf(envelope.getMinY())));
  
  envelope = this.getVisibleEnvelope();
  map.put("visible.minx", map.new Value(String.valueOf(envelope.getMinX())));
  map.put("visible.miny", map.new Value(String.valueOf(envelope.getMinY())));
  map.put("visible.maxx", map.new Value(String.valueOf(envelope.getMaxX())));
  map.put("visible.maxy", map.new Value(String.valueOf(envelope.getMaxY())));
  map.put("visible.wkid", map.new Value(String.valueOf(envelope.getWkid())));
  return map;
}

/**
 * Sets the params.
 * 
 * @param parameterMap the parameter map
 * 
 * @see com.esri.gpt.catalog.search.ISearchFilter#setParams(com.esri.gpt.catalog.search.SearchParameterMap)
 */

public void setParams(SearchParameterMap parameterMap) {
  
  String tmp = parameterMap.get(SaveParams.selectedBoundOption.name()).getParamValue();
  this.setSelectedBounds(tmp);

  Envelope envelope = new Envelope();
  try {    
    tmp = parameterMap.get(SaveParams.maxX.name()).getParamValue();
    envelope.setMaxX(tmp);
    tmp = parameterMap.get(SaveParams.maxY.name()).getParamValue();
    envelope.setMaxY(tmp);
    tmp = parameterMap.get(SaveParams.minX.name()).getParamValue();
    envelope.setMinX(tmp);
    tmp = parameterMap.get(SaveParams.minY.name()).getParamValue();
    envelope.setMinY(tmp);
  } catch (NullPointerException e) {
    envelope = new Envelope();
    LOG.log(Level.WARNING, "Could not get envelope parameters " +
        "in Spatial Search Filter defaulting to" + envelope.toString(),e); 
  }
  if (envelope.isEmpty()) envelope = new Envelope();
  this.setSelectedEnvelope(envelope);
  
  envelope = new Envelope();
  try {
    envelope.setMinX(parameterMap.get("visible.minx").getParamValue());
    envelope.setMinY(parameterMap.get("visible.miny").getParamValue());
    envelope.setMaxX(parameterMap.get("visible.maxx").getParamValue());
    envelope.setMaxY(parameterMap.get("visible.maxy").getParamValue());
    envelope.setWkid(parameterMap.get("visible.wkid").getParamValue());
  } catch (NullPointerException e) {
    envelope = this.getSelectedEnvelope().clone();
  }
  if (envelope.isEmpty()) envelope = this.getSelectedEnvelope().clone();
  this.setVisibleEnvelope(envelope);
     
}

/**
 * Compares two objects.
 * @param obj
 * @return <code>true</code> if two objects are equal
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
  if(!(obj instanceof SearchFilterSpatial)) {
    return false;
  }
  SearchFilterSpatial tmp = (SearchFilterSpatial) obj;
  return this.getParams().equalsSubset(tmp.getParams());
}

/**
 * 
 * @see java.lang.Object#toString()
 */

@Override
public String toString() {
  
  return "\n{=======================\n" + this.getClass().getCanonicalName() +
  this.getParams().toString()
  + "\n===========================}";
}

/**
 * Gets the mvs url.  If empty, there was an error getting the MVS url.
 * 
 * @return the mvs url
 */
public String getMvsUrl() {
  URL url;
  try {
    url = SearchConfig.getConfiguredInstance().getMapviewerUrl2();
    if(url != null)
      return url.toString();    
  } catch (SearchException e) {
    LOG.log(Level.FINE, "" ,e);
  }
  return "";
  
  
}

/**
 * Gets the envelope associated with the class
 * @return the envelope
 */
public Envelope getEnvelope() {
  return this.getSelectedEnvelope();
}

/**
 * Sets the envelope associated with the class
 * @param envelope
 */
public void setEnvelope(Envelope envelope) {
  this.setSelectedEnvelope(envelope);
  
}

}
