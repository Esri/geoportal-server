package com.esri.gpt.catalog.search;

import com.esri.gpt.control.georss.IFeedRecord;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;

/**
 * Convinience class that implements IMapViewer properties
 * 
 * @author TM
 *
 */

public abstract class IAMapViewer implements IMapViewer {

// instance variables ==========================================================
/** The map viewer configs. */
private MapViewerConfigs   _mapViewerConfigs;

/** The resource uri. */
private String             _resourceUri;

/** The hint service type. */
private String             _hintServiceType;

/** The search result record. */
private IFeedRecord _searchResultRecord;

/** The request context. */
private RequestContext     _requestContext;


// constructors ===============================================================

// properties
// =============================================================================

/**
 * Sets the map viewer configs.
 * 
 * @param mapViewerConfigs
 *          the map viewer configs
 */
public void setMapViewerConfigs(MapViewerConfigs mapViewerConfigs) {
  this._mapViewerConfigs = mapViewerConfigs;

}

/**
 * Sets the resource uri.
 * 
 * @param resourceUri
 *          the resource uri
 * @param hintServiceType
 *          the hint service type
 */
public void setResourceUri(String resourceUri, String hintServiceType) {
  this._resourceUri = resourceUri;
  this._hintServiceType = hintServiceType;

}

/**
 * Gets the hint service type.
 * 
 * @return the hint service type (trimmed, never null)
 */
protected String getHintServiceType() {
  return Val.chkStr(this._hintServiceType);
}

/**
 * Gets the map viewer configs.
 *
 * @return the map viewer configs
 */
protected MapViewerConfigs getMapViewerConfigs() {
  return this._mapViewerConfigs;
}

/**
 * Sets the search result record.
 * 
 * @param searchResultRecord
 *          the search result record
 */
public void setSearchResultRecord(IFeedRecord searchResultRecord) {
  this._searchResultRecord = searchResultRecord;

}

/**
 * Gets the search result record.
 * 
 * @return the search result record
 * @return The search result record. Could be null.
 */
private IFeedRecord getSearchResultRecord() {
  return this._searchResultRecord;
}

/**
 * Sets the request context.
 * 
 * @param context
 *          the context
 */
public void setRequestContext(RequestContext context) {
  this._requestContext = context;
}

/**
 * Gets the request context.
 * 
 * @return the request context
 */
public RequestContext getRequestContext() {
  return this._requestContext;
}

/**
 * Gets the resource uri.
 * 
 * @return the resource uri (trimmed, never null)
 */
public String getResourceUri() {
  return Val.chkStr(this._resourceUri);
}

/**
 * Gets the url can be jscript.
 * 
 * @return the url can be jscript
 */
public boolean getUrlCanBeJscript() {
  if (this.getRequestContext() == null) {
    return false;
  }
  return this.getRequestContext().getViewerExecutesJavascript();
}


}
