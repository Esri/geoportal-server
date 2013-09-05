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

import com.esri.gpt.control.georss.IFeedRecord;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import javax.mail.Quota.Resource;

import org.apache.commons.collections.SetUtils;
import org.apache.commons.io.IOUtils;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;

/**
 * The Class MapViewerFlex. Understands what the Flex viewer can view and calls
 * the Flex Viewer when appropriate.
 */
public class MapViewerFlex implements IMapViewer {
// class variables
// =============================================================
/** The class logger LOG. */
private final static Logger       LOG     = Logger.getLogger(MapViewerFlex.class
                                       .getCanonicalName());
private static String      JSCRIPT = null;

// instance variables
// ==========================================================
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
static {
  InputStream is = null;
  try {
    ResourcePath resourcePath = new ResourcePath();
    URL url = resourcePath
        .makeUrl("com/esri/gpt/catalog/search/MapViewerFlex.js");
    is = url.openStream();
    JSCRIPT = ("javascript:" + IOUtils.toString(is)).replace("\n", "");
    
  } catch (Throwable e) {
    LOG.log(Level.WARNING, "Could not configure map viewer", e);
  } finally {
    IOUtils.closeQuietly(is);
  }
}

public MapViewerFlex() {
  
}

// properties
// ==================================================================

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

/**
 * Gets the map viewer config.
 * 
 * @return the map viewer config (never null)
 */
private MapViewerConfigs getMapViewerConfig() {
  if (this._mapViewerConfigs == null) {
    return new MapViewerConfigs();
  }
  return this._mapViewerConfigs;
}

/**
 * Gets the window height.
 * 
 * @return the window height (trimmed, never null)
 */
private String getWindowHeight() {

  int height = Val.chkInt(
      this.getMapViewerConfig().getParameters().get("height"), 400);
  return String.valueOf(height);
}

/**
 * Gets the window width.
 * 
 * @return the window width (trimmed, never null)
 */
private String getWindowWidth() {
  int width = Val.chkInt(
      this.getMapViewerConfig().getParameters().get("width"), 500);
  return String.valueOf(width);
}

/**
 * Gets the window title.
 * 
 * @return the window title (trimmed, ever null)
 */
private String getWindowTitle() {
  String value = this.getMapViewerConfig().getParameters().get("title");
  if (value == null || "".equals(value)) {
    try {
      value = this.getRequestContext().getServletRequest()
          .getServerName();
    
    } catch (Throwable e) {
      LOG.log(Level.WARNING, "Could not get server name from request context. "
          + "using  window name = Map");
      value = "Map";
    }
  }
  value = value.replaceAll("[^\\w]*", "A");
  return Val.chkStr(value);
}

/**
 * Gets the regex can consume url.
 * 
 * @return the regex can consume url (trimmed, never null)
 */
private String getRegexCanConsumeUrl() {
  String value = this.getMapViewerConfig().getParameters()
      .get("regexCanConsumeUrl");
  return Val.chkStr(value);
}

// methods
// =====================================================================

/**
 * Reads the add to map url
 * 
 * @return the url (possibly none)
 */
public String readAddToMapUrl() {
  String url = this._mapViewerConfigs.getUrl();

  if (!url.contains("?")) {
    url = url + "?";
  }
  if (url.contains("=")) {
    url += "&";
  }
  try {
    String title = "";
    if (this.getSearchResultRecord() != null) {
      title = this.getSearchResultRecord().getTitle();
      title = title.replaceAll("'", "\\\\'");
    }
    String resourceValue = URLEncoder.encode(this.getHintServiceType()
        .toLowerCase() + ":" + this._resourceUri, "UTF-8");
    url += "title=" + URLEncoder.encode(title, "UTF-8");

    if (this.getUrlCanBeJscript() && JSCRIPT != null) {

      url = JSCRIPT.replace("{mapAndResourceUrl}", url + "&resource="
          + resourceValue);
      url = url.replace("{jsResourceUrl}",
          URLDecoder.decode(resourceValue, "UTF-8"));
      url = url.replace("{width}", getWindowWidth());
      url = url.replace("{height}", getWindowHeight());
      url = url.replace("{windowTitle}", getWindowTitle());

      url = url.replace("{title}", title);
      /*
       * url = "javascript:GptUtils.popUp('" + url + "'," +
       * "GptMapViewer.TITLE," + "GptMapViewer.dimensions.WIDTH," +
       * "GptMapViewer.dimensions.HEIGHT);";
       */
      //url = url.replace("%", "123456789");
    } else {
      if(this.getUrlCanBeJscript() && JSCRIPT == null) { 
         LOG.warning("Could not create javascript based add to map because " +
         		" of prior initialization errors"); 
      }
      url += "&resource=" + resourceValue;
    }
  } catch (UnsupportedEncodingException e) {
    LOG.log(Level.WARNING, "Error while forming add to map url", e);
  }
  LOG.finer("Url formed from readAddToMapUrl " + url);
  return url;
}

/**
 * If resource can be handled
 * 
 * @return true if resource can be handled or false if not
 */
public boolean canHandleResource() {
  String hintServiceType = this.getHintServiceType();
  String resourceUri = this.getResourceUri();

  if (hintServiceType == null || resourceUri == null) {
    LOG.finer("Could NOT handle resrouceuri = " + resourceUri + ", hint = "
        + _hintServiceType);
    return false;
  }
  if (hintServiceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.WMS
      .name())
      || hintServiceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.AIMS
          .name())) {
    LOG.finer("Could handle resrouceuri " + resourceUri);
    return true;

  }
  if (hintServiceType.equalsIgnoreCase(ResourceLinkBuilder.ServiceType.AGS
      .name())) {
    if (resourceUri.toLowerCase().contains("/rest")
        && resourceUri.toLowerCase().contains("/mapserver")) {
      this._hintServiceType = "agsrest";
      return true;
    }
    LOG.finer("Could handle resrouceuri " + _resourceUri);
  }
  if (!this.getRegexCanConsumeUrl().equals("")) {
    try {
      if (resourceUri.matches(this.getRegexCanConsumeUrl())) {
        return true;
      }
    } catch (PatternSyntaxException e) {
      LOG.log(Level.WARNING,
          "Regex not specified correctly " + this.getRegexCanConsumeUrl(), e);
    }
    LOG.finer("Could handle resrouceuri with configured regex " + _resourceUri);
  }
  LOG.finer("Could NOT handle resrouceuri = " + _resourceUri + ", hint = "
      + _hintServiceType);

  return false;
}

/**
 * Returns "" if javascript allowed or returns "_blank"
 * 
 * @return link target
 */
public String readTarget() {
  if (this.getUrlCanBeJscript() == true) {
    return "";
  }
  return "_blank";
}

/**
 * Reads url to open the default map viewer
 * 
 * @return The default map viewer url 
 */
public String readOpenDefaultMapViewerUrl() {
  String url = this._mapViewerConfigs.getUrl();
  url = JSCRIPT.replace("{mapAndResourceUrl}", url);
  url = url.replace("{jsResourceUrl}", "");
  url = url.replace("{width}", getWindowWidth());
  url = url.replace("{height}", getWindowHeight());
  url = url.replace("{windowTitle}", getWindowTitle());

  url = url.replaceAll("true == true", "true == false");
  return url;
}

}
