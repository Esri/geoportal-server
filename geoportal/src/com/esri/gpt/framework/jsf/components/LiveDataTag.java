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
package com.esri.gpt.framework.jsf.components;

import com.esri.gpt.framework.util.Val;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

/**
 * Live Data tag.
 */
public class LiveDataTag extends UIComponentTag {

// class variables ==============================================================
/** The LOG. */
private static Logger LOG =
  Logger.getLogger(JscriptTag.class.getCanonicalName());
// instance variables ==========================================================
/** basemap URL */
private String mapService = "";
/** geometry service URL */
private String geometryService = "";
/** resource url. */
private String url = "";
/** mapStyle */
private String mapStyle = "";
/** verbose */
private boolean verbose;
/** error message */
private String errorMessage = "";
/** basemap label */
private String basemapLabel = "";
/** tooltips */
private String tooltips = "";
/** onCreatePlaceholder */
private String onCreatePlaceholder = "";


/**
 * Gets map service URL.
 * @return mapService URL
 */
public String getMapService() {
  return mapService;
}

/**
 * Sets map service URL.
 * @param mapService map service URL
 */
public void setMapService(String mapService) {
  this.mapService = Val.chkStr(mapService);
}

/**
 * Gets geometry service URL.
 * @return geometry service URL
 */
public String getGeometryService() {
  return geometryService;
}

/**
 * Sets geometry service URL.
 * @param geometryService geometry srevice URL
 */
public void setGeometryService(String geometryService) {
  this.geometryService = Val.chkStr(geometryService);
}

/**
 * Gets resource url.
 * @return the url
 */
public String getUrl() {
  return url;
}

/**
 * Sets resource url.
 * @param url the url to set
 */
public void setUrl(String url) {
  this.url = Val.chkStr(url);
}

/**
 * Gets map style.
 * @return map style
 */
public String getMapStyle() {
  return this.mapStyle;
}

/**
 * Sets map style.
 * @param mapStyle map style
 */
public void setMapStyle(String mapStyle) {
  this.mapStyle = Val.chkStr(mapStyle);
}

/**
 * Gets verbose flag.
 * @return verbose flag
 */
public boolean getVerbose() {
  return verbose;
}

/**
 * Sets verbose flag.
 * @param verbose verbose flag
 */
public void setVerbose(boolean verbose) {
  this.verbose = verbose;
}

/**
 * Gets error message.
 * @return error message
 */
public String getErrorMessage() {
  return errorMessage;
}

/**
 * Sets error message.
 * @param errorMessage error message
 */
public void setErrorMessage(String errorMessage) {
  this.errorMessage = Val.chkStr(errorMessage);
}

/**
 * Gets basemap label.
 * @return basemap label
 */
public String getBasemapLabel() {
  return basemapLabel;
}

/**
 * Sets basemap label.
 * @param basemapLabel basemap label
 */
public void setBasemapLabel(String basemapLabel) {
  this.basemapLabel = Val.chkStr(basemapLabel);
}

/**
 * Gets tooltips.
 * @return tooltips
 */
public String getTooltips() {
  return tooltips;
}

/**
 * Sets tooltips.
 * @param tooltips tooltips
 */
public void setTooltips(String tooltips) {
  this.tooltips = Val.chkStr(tooltips);
}

/**
 * Gets onCreatePlaceholder handler name.
 * @return onCreatePlaceholder handler name
 */
public String getOnCreatePlaceholder() {
  return onCreatePlaceholder;
}

/**
 * Sets onCreatePlaceholder handler name.
 * @param onCreatePlaceholder onCreatePlaceholder handler name
 */
public void setOnCreatePlaceholder(String onCreatePlaceholder) {
  this.onCreatePlaceholder = onCreatePlaceholder;
}

/** The tag support. */
private final TagSupport tagSupport = new TagSupport();

// methods ==================================================================
@Override
public void setProperties(UIComponent component) {

  super.setProperties(component);
  tagSupport.setPropValueBind(component, "mapService", getMapService());
  tagSupport.setPropValueBind(component, "geometryService", getGeometryService());
  tagSupport.setPropValueBind(component, "url", getUrl());
  tagSupport.setPropValueBind(component, "mapStyle", getMapStyle());
  tagSupport.setPropValueBind(component, "verbose", Boolean.toString(getVerbose()));
  tagSupport.setPropValueBind(component, "errorMessage", getErrorMessage());
  tagSupport.setPropValueBind(component, "basemapLabel", getBasemapLabel());
  tagSupport.setPropValueBind(component, "tooltips", getTooltips());
  tagSupport.setPropValueBind(component, "onCreatePlaceholder", getOnCreatePlaceholder());
}

/**
 *
 * Clears all instance variables.
 */
@Override
public void release() {
  mapService = "";
  geometryService = "";
  url = "";
  mapStyle = "";
  verbose = false;
  errorMessage = "";
  basemapLabel = "";
  tooltips = "";
  onCreatePlaceholder = "";
}

/**
 * Returns component type string representation
 * @return Component type
 *
 */
@Override
public String getComponentType() {
  return UILiveData.COMPONENT_TYPE;
}

/**
 * Gets the JSF render Type
 * @return JSF render Type
 *
 */
@Override
public String getRendererType() {

  return null;
}
}
