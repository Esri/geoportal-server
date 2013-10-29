package com.esri.gpt.catalog.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.framework.util.Val;

/**
 * The Class MapViewerAgsPortal.
 */
public class MapViewerAgsPortal extends IAMapViewer {

// class variables =============================================================
/** The LOG. */
private static Logger LOG = Logger.getLogger(MapViewerAgsPortal.class
                              .getCanonicalName());

// methods =====================================================================
/**
 * Checks whether resource can be handled by this map viewer
 * 
 * @return true/false
 */
@Override
public boolean canHandleResource() {
  String hintServiceType = this.getHintServiceType();
  String resourceUri = this.getResourceUri();

  if (hintServiceType == null || resourceUri == null) {
    LOG.finer("Could NOT handle resrouceuri = " + resourceUri + ", hint = "
        + this.getHintServiceType());
    return false;
  }

  if (isKmlOrKmz(this.getResourceUri())) {
    LOG.finer("Can handle kml resrouceuri " + resourceUri);
    // this.setHintServiceType("kml");
    return true;

  }
  if (hintServiceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.WMS
      .name())) {
    LOG.finer("Can handle resrouceuri " + resourceUri);
    return true;

  }
  if (hintServiceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.AGS
      .name())) {
    String tmp = this.getResourceUri().toLowerCase();
    if (tmp.contains("mapserver") || tmp.contains("imageserver")
        || tmp.contains("featureserver")) {
      return true;
    }
    LOG.finer("Could handle resrouceuri " + this.getResourceUri());
  }
  LOG.finer("Class cannot handle resourceuri = " + this.getResourceUri()
      + " hint = " + this.getHintServiceType());
  return false;
}

/**
 * Reads the add to map URL
 * 
 * @return 'Add To Map' URL
 */
@Override
public String readAddToMapUrl() {
  String viewerUrl = null;
  try {
    viewerUrl = this.getMapViewerConfigs().getUrl();
    if (viewerUrl.contains("?") == false) {
      viewerUrl += "?";
    }
    if (isKmlOrKmz(this.getResourceUri())) {
      viewerUrl += "&kml=" + URLEncoder.encode(this.getResourceUri(), "UTF-8");
    } else if (this.getHintServiceType().equalsIgnoreCase(
        ResourceLinkBuilder.ServiceType.WMS.name())) {
      viewerUrl += "&wms=" + URLEncoder.encode(this.getResourceUri(), "UTF-8");
      if (!Val.chkStr(this.getMapViewerConfigs().getParameters().get(
          "wmsBaseMapUrl")).equals("")) {
        viewerUrl += "&basemapUrl="
            + URLEncoder.encode(
                Val.chkStr(this.getMapViewerConfigs().getParameters()
                    .get("wmsBaseMapUrl")), "UTF-8");
      }
    } else if (this.getHintServiceType().equalsIgnoreCase(
        ResourceLinkBuilder.ServiceType.AGS.name())) {
      viewerUrl += "&url=" + URLEncoder.encode(this.getResourceUri(), "UTF-8");
    }
  } catch (UnsupportedEncodingException uE) {
    LOG.log(Level.SEVERE, "", uE);
  }
  return viewerUrl;
}

/**
 * Reads the html link target value
 * 
 * @return html link "target" value
 */
@Override
public String readTarget() {
  return "_blank";
}

/**
 * Checks if is kml or kmz.
 * 
 * @param url
 *          the url
 * @return true, if is kml or kmz
 */
private boolean isKmlOrKmz(String url) {
  if (url.toLowerCase().endsWith(".kml") || url.toLowerCase().endsWith(".kmz")) {
    return true;
  }
  return false;
}

/**
 * Gets the default Map Viewer Url
 * 
 * @return default map viewer url
 */
@Override
public String readOpenDefaultMapViewerUrl() {
	String s =  this.getMapViewerConfigs().getUrl();
	//if(this.getUrlCanBeJscript() == true) {
		s = "javascript:window.open('" + s + "')";
	//}
  return s;
}

}
