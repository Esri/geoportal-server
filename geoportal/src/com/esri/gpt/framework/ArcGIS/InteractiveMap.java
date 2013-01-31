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
package com.esri.gpt.framework.ArcGIS;

import com.esri.gpt.framework.util.Val;

/**
 * Properties for interactive map controls.
 */
public class InteractiveMap {
// class variables =============================================================

// instance variables ==========================================================
private String _geometryServiceUrl = "";
private String _jsapiUrl = "";
private String _locatorSingleFieldParameter = "";
private String _locatorUrl = "";
private String _mapServiceType = "";
private String _mapServiceUrl = "";
private String _mapVisibleLayers = "[]";
private String _mapInitialExtent = "";

/** Default constructor. */
public InteractiveMap() {}

// properties ==================================================================

/**
 * Gets the URL for the geometry service.
 * @return the URL
 */
public String getGeometryServiceUrl() {
  return _geometryServiceUrl;
}

/**
 * Sets the URL for the geometry service.
 * @param url the URL
 */
public void setGeometryServiceUrl(String url) {
  _geometryServiceUrl = Val.chkStr(url);
}

/**
 * Gets the URL for the ArcGIS Server Javascript API.
 * @return the url
 */
public String getJsapiUrl() {
  return _jsapiUrl;
}

/**
 * Sets the URL for the ArcGIS Server Javascript API.
 * @param url the URL
 */
public void setJsapiUrl(String url) {
  _jsapiUrl = Val.chkStr(url);
}

/**
 * Gets the parameter name associated with a single field locator.
 * @return the parameter name associated with the locator
 */
public String getLocatorSingleFieldParameter() {
  return _locatorSingleFieldParameter;
}

/**
 * Sets the parameter name associated with a single field locator.
 * @param name parameter name associated with the locator
 */
public void setLocatorSingleFieldParameter(String name) {
  _locatorSingleFieldParameter = Val.chkStr(name);
}

/**
 * Gets the URL for the locator.
 * @return the URL
 */
public String getLocatorUrl() {
  return _locatorUrl;
}

/**
 * Sets the URL for the locator.
 * @param url the URL
 */
public void setLocatorUrl(String url) {
  _locatorUrl = Val.chkStr(url);
}

/**
 * Gets map service type.
 * @return the map service type
 */
public String getMapServiceType() {
  return _mapServiceType;
}

/**
 * Gets map visible layers (WMS only)
 * @return visible layers
 */
public String getMapVisibleLayers() {
  return _mapVisibleLayers;
}

/**
 * Sets map visible layers (WMS only)
 * @param mapVisibleLayers visible layers
 */
public void setMapVisibleLayers(String mapVisibleLayers) {
  this._mapVisibleLayers = Val.chkStr(mapVisibleLayers, "[]");
}

/**
 * Sets map service type.
 * @param type the map service type
 */
public void setMapServiceType(String type) {
  type = Val.chkStr(type);
  if (type.equalsIgnoreCase("dynamic")) {
    _mapServiceType = "dynamic";
  } else if (type.equalsIgnoreCase("tiled")) {
    _mapServiceType = "tiled";
  } else if (type.equalsIgnoreCase("openstreet")) {
    _mapServiceType = "openstreet";
  } else if (type.equalsIgnoreCase("wms")) {
    _mapServiceType = "wms";
  } else if (type.equalsIgnoreCase("wmts")) {
    _mapServiceType = "wmts";
  } else {
    _mapServiceType = "";
  }
}

/**
 * Gets the URL for the map service.
 * @return the URL
 */
public String getMapServiceUrl() {
  return _mapServiceUrl;
}

/**
 * Sets the URL for the map service.
 * @param url the URL
 */
public void setMapServiceUrl(String url) {
  _mapServiceUrl = Val.chkStr(url);
}

public String getMapInitialExtent() {
  return _mapInitialExtent;
}

public void setMapInitialExtent(String mapInitialExtent) {
  this._mapInitialExtent = Val.chkStr(mapInitialExtent);
}

// methods =====================================================================
/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" jsapiUrl=\"").append(getJsapiUrl()).append("\"\n");
  sb.append(" mapServiceUrl=\"").append(getMapServiceUrl()).append("\"\n");
  sb.append(" mapServiceType=\"").append(getMapServiceType()).append("\"\n");
  sb.append(" mapVisibleLayers=\"").append(getMapVisibleLayers()).append("\"\n");
  sb.append(" mapInitialExtent=\"").append(getMapInitialExtent()).append("\"\n");
  sb.append(" geometryServiceUrl=\"").append(getGeometryServiceUrl()).append("\"\n");
  sb.append(" locatorUrl=\"").append(getLocatorUrl()).append("\"\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}
}
