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

/* Imports added to handle Bookmarks list */
import java.util.ArrayList;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.NodeListAdapter;
import javax.faces.model.SelectItem;
import com.esri.gpt.framework.util.LogUtil;

import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;
import org.apache.commons.lang3.StringEscapeUtils;



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
transient private Envelope _visibleEnvelope = new EnvelopeWrapper(new Envelope());

/** Configuration for Bookmarks (predefined extents) **/
private String _extent=new String();
private ArrayList _predefinedExtents=new ArrayList();
private static final String PREDEFINED_EXTENTS_FILE="gpt/config/extents.xml"; // NAME OF FILE IS HARDCODED

// constructor =================================================================
/**
 * Instantiates a new search filter spatial.
 */
public SearchFilterSpatial() { 
  super();
  reset();
  
// Load a fioel with predefined extents (bookmarks)
  Document domFe;
  XPath xpathFe;
  try{
	  String sPredefinedExtents = PREDEFINED_EXTENTS_FILE;
	  LogUtil.getLogger().log(Level.FINE, "Loading Predefined extents file: {0}", sPredefinedExtents);
	  domFe = DomUtil.makeDomFromResourcePath(sPredefinedExtents, false);
	  xpathFe = XPathFactory.newInstance().newXPath();
  }
  catch(Exception e)
  {
	  LogUtil.getLogger().log(Level.FINE, "No predefined extents file");
	  domFe=null;
	  xpathFe=null;
  }
  // predefined extent file root
  Node rootFe=null;
  try{
  if(domFe!=null)
  	rootFe= (Node) xpathFe.evaluate("/extents", domFe, XPathConstants.NODE);
  }
  catch(Exception e)
	{
	  rootFe=null;
	  LogUtil.getLogger().log(Level.FINE, "Xpath problem for predefined extents file");
	}
  /**
   * Reads Bookmarks and load an array
   */
  NodeList extents;
  ArrayList extentsList= new ArrayList();
  if(rootFe!=null)
  { try{
	  extents=(NodeList)xpathFe.evaluate("extent", rootFe, XPathConstants.NODESET);
	  for (Node extent : new NodeListAdapter(extents)) {
		  String extPlace=(String) xpathFe.evaluate("@place", extent, XPathConstants.STRING);
		  String extValue=(String) xpathFe.evaluate("@ext", extent, XPathConstants.STRING);
		  LogUtil.getLogger().log(Level.FINE,"Element added:"+extPlace+","+extValue);
		  SelectItem extElement=new SelectItem(extValue,extPlace);
		  extentsList.add(extElement);
	  }
  	}
	catch(Exception e)
	{
		extentsList=null;
		LogUtil.getLogger().log(Level.FINE, "Xpath problem for predefined extents file");
	}
  }
  else
	  extentsList=null;
  
  this._predefinedExtents=extentsList;
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
    this.setVisibleEnvelope(new EnvelopeWrapper(new Envelope()));
  }
  return _visibleEnvelope;
}

/**
 * Sets the visible envelope (can be projected).
 * @param envelope the visible envelope
 */
public void setVisibleEnvelope(Envelope envelope) {
  _visibleEnvelope = envelope;
  if (_visibleEnvelope == null) _visibleEnvelope = new EnvelopeWrapper(new Envelope());
}

// methods =====================================================================

/**
 * Reset <b>this</b> object to default.
 */
public void reset() {
  this.setSelectedEnvelope(new Envelope());
  this.setSelectedBounds(OptionsBounds.anywhere.name());
  this.setVisibleEnvelope(new EnvelopeWrapper(new Envelope()));
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
  
  envelope = new EnvelopeWrapper(new Envelope());
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

private static class EnvelopeWrapper extends Envelope {
    private final Envelope src;

    public EnvelopeWrapper(Envelope src) {
      this.src = src;
    }

    @Override
    public double getHeight() {
      return src.getHeight();
    }

    @Override
    public double getMaxX() {
      return src.getMaxX();
    }

    @Override
    public void setMaxX(double d) {
      src.setMaxX(d);
    }

    @Override
    public void setMaxX(String s) {
      src.setMaxX(s);
    }

    @Override
    public double getMaxY() {
      return src.getMaxY();
    }

    @Override
    public void setMaxY(double d) {
      src.setMaxY(d);
    }

    @Override
    public void setMaxY(String s) {
      src.setMaxY(s);
    }

    @Override
    public double getMinX() {
      return src.getMinX();
    }

    @Override
    public void setMinX(double d) {
      src.setMinX(d);
    }

    @Override
    public void setMinX(String s) {
      src.setMinX(s);
    }

    @Override
    public double getMinY() {
      return src.getMinY();
    }

    @Override
    public void setMinY(double d) {
      src.setMinY(d);
    }

    @Override
    public void setMinY(String s) {
      src.setMinY(s);
    }

    @Override
    public double getWidth() {
      return src.getWidth();
    }

    @Override
    public double getCenterX() {
      return src.getCenterX();
    }

    @Override
    public double getCenterY() {
      return src.getCenterY();
    }

    @Override
    public void echo(StringBuffer sb) {
      src.echo(sb);
    }

    @Override
    public String toString() {
      return src.toString();
    }

    @Override
    public boolean hasSize() {
      return src.hasSize();
    }

    @Override
    public boolean isEmpty() {
      return src.isEmpty();
    }

    @Override
    public boolean isValid() {
      return src.isValid();
    }

    @Override
    public boolean isValidWGS84() {
      return src.isValidWGS84();
    }

    @Override
    public void put(double minx, double miny, double maxx, double maxy) {
      src.put(minx, miny, maxx, maxy);
    }

    @Override
    public void put(String minx, String miny, String maxx, String maxy) {
      src.put(minx, miny, maxx, maxy);
    }

    @Override
    public Envelope clone() {
      return src.clone();
    }

    @Override
    public boolean equals(Object obj) {
      return src.equals(obj);
    }

    @Override
    public void merge(Envelope envelope) {
      src.merge(envelope);
    }

    @Override
    public String getWkid() {
      return src.getWkid();
    }

    @Override
    public void setWkid(String wkid) {
      src.setWkid(StringEscapeUtils.escapeHtml4(wkid));
    }
  
    
}

/* Added functions to handle predefined extents
/**
 * @param _extent the extent to set
 */
public void setExtent(String extent) {
	this._extent = extent;
}

/**
 * @return the _extent
 */
public String getExtent() {
	return _extent;
}

/**
 * @param _predefinedExtents the _predefinedExtents to set
*/ 
public void setPredefinedExtents(ArrayList predefinedExtents) {
	this._predefinedExtents = _predefinedExtents;
}

/**
 * @return the _predefinedExtents
 */
public ArrayList getPredefinedExtents() {
	return _predefinedExtents;
}
}
