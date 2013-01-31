package com.esri.gpt.catalog.search;

import java.util.logging.Logger;

import com.esri.gpt.framework.util.Val;



/**
 * Works with the 2.4 geoportal widget which works with 2.4 viewer version.
 * 
 * Adds kml/kmz ImageServer and FeatureServer support
 * 
 * @author TM
 *
 */
public class MapViewerFlex_2_4 extends MapViewerFlex {

// class variables =============================================================
private Logger LOG = Logger.getLogger(MapViewerFlex_2_4.class.toString());

// instance variables ==========================================================
String resourceUri = null;

String hintServiceType = null;

// properties ==================================================================
/**
 * Gets the Resource uri.
 *
 * @return the hint Resource uri (trimmed, never null)
 */
public String getResourceUri() {
	return Val.chkStr(resourceUri);
}

/**
 * Sets the resource uri.
 *
 * @param resourceUri the new resource uri
 */
public void setResourceUri(String resourceUri) {
	this.resourceUri = resourceUri;
}

/**
 * Gets the hint service type.
 *
 * @return the hint service type (trimmed, never null)
 */
public String getHintServiceType() {
	return Val.chkStr(hintServiceType);
}

/**
 * Sets the hint service type.
 *
 * @param hintServiceType the new hint service type
 */
public void setHintServiceType(String hintServiceType) {
	this.hintServiceType = hintServiceType;
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
	super.setResourceUri(resourceUri, hintServiceType);
  this.resourceUri = resourceUri;
  this.hintServiceType = hintServiceType;

}

// methods =====================================================================
/**
 * Adding handling of kml/kmz, image and feature service
 */
@Override
public boolean canHandleResource() {
	
	boolean superCanHandleResource = super.canHandleResource();
	if(superCanHandleResource == true) {
		return superCanHandleResource;
	}
	String resourceUri = this.getResourceUri();
	if(resourceUri.toLowerCase().endsWith(".kmz") || 
			resourceUri.toLowerCase().endsWith(".kml")) {
		LOG.finer("Could handle resrouceuri " + getResourceUri());
		return true;
	}
	
	String hintServiceType = this.getHintServiceType();
	if (hintServiceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.AGS
      .name())) {
    if (resourceUri.toLowerCase().contains("/rest")
        && (resourceUri.toLowerCase().contains("/mapserver") ||
         resourceUri.toLowerCase().contains("/imageserver") ||
         resourceUri.toLowerCase().contains("/featureserver"))) {
      this.setHintServiceType("agsrest");
      LOG.finer("Could handle resrouceuri " + getResourceUri());
      return true;
    }
    
  }
	return superCanHandleResource;
}

}
