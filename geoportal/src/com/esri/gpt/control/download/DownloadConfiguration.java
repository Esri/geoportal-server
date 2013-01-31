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
package com.esri.gpt.control.download;

import com.esri.gpt.framework.util.Val;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * DownloadConfiguration
 */
public class DownloadConfiguration implements Serializable {

/** task url */
private String _taskUrl = "";
/** map service url */
private String _mapServiceUrl = "";
/** map service type */
private String _mapServiceType = "";
/** map initial extent */
private String _mapInitialExtent = "";
/** projection info */
private ArrayList<ItemInfo> _projectionInfo = new ArrayList<ItemInfo>();
/** output format info */
private ArrayList<ItemInfo> _outputFormatInfo = new ArrayList<ItemInfo>();
/** raster format info */
private ArrayList<ItemInfo> _rasterFormatInfo = new ArrayList<ItemInfo>();
/** feature format info */
private ArrayList<ItemInfo> _featureFormatInfo = new ArrayList<ItemInfo>();

/**
 * Gets geoprocessing task url.
 * @return geoprocessing task url
 */
public String getTaskUrl() {
  return _taskUrl;
}

/**
 * Sets geoprocessing task url.
 * @param taskUrl geoprocessing task url
 */
public void setTaskUrl(String taskUrl) {
  _taskUrl = Val.chkStr(taskUrl);
}

/**
 * Gets map service url.
 * @return map service url.
 */
public String getMapServiceUrl() {
  return _mapServiceUrl;
}

/**
 * Sets map service url.
 * @param mapServiceUrl map service url
 */
public void setMapServiceUrl(String mapServiceUrl) {
  _mapServiceUrl = Val.chkStr(mapServiceUrl);
}

/**
 * Gets map service type.
 * @return map service type.
 */
public String getMapServiceType() {
  return _mapServiceType;
}

/**
 * Sets map service type.
 * @param mapServiceType map service type
 */
public void setMapServiceType(String mapServiceType) {
  _mapServiceType = Val.chkStr(mapServiceType);
}

/**
 * Gets project info.
 * @return array of project info
 */
public ArrayList<ItemInfo> getProjectionInfo() {
  return _projectionInfo;
}

/**
 * Gets output format info.
 * @return array of output format info
 */
public ArrayList<ItemInfo> getOutputFormatInfo() {
  return _outputFormatInfo;
}

/**
 * Gets feature format info.
 * @return feature format info
 */
public ArrayList<ItemInfo> getFeatureFormatInfo() {
  return _featureFormatInfo;
}

/**
 * Sets feature format info.
 * @param featureFormatInfo feature format info
 */
public void setFeatureFormatInfo(ArrayList<ItemInfo> featureFormatInfo) {
  this._featureFormatInfo = featureFormatInfo;
}

/**
 * Gets raster format info.
 * @return raster format info
 */
public ArrayList<ItemInfo> getRasterFormatInfo() {
  return _rasterFormatInfo;
}

/**
 * Sets raster format info.
 * @param rasterFormatInfo raster format info
 */
public void setRasterFormatInfo(ArrayList<ItemInfo> rasterFormatInfo) {
  this._rasterFormatInfo = rasterFormatInfo;
}

public String getMapInitialExtent() {
  return _mapInitialExtent;
}

public void setMapInitialExtent(String mapInitialExtent) {
  this._mapInitialExtent = Val.chkStr(mapInitialExtent);
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" taskUrl=\"").append(getTaskUrl()).append("\"\n");
  sb.append(" mapServiceUrl=\"").append(getMapServiceUrl()).append("\"\n");
  sb.append(" mapServiceType=\"").append(getMapServiceType()).append("\"\n");
  sb.append(" mapInitialExtent=\"").append(getMapInitialExtent()).append("\"\n");
  sb.append(" projections=(\r\n");
  for (ItemInfo ii : getProjectionInfo()) {
    sb.append("  "+ii.toString()+"\r\n");
  }
  sb.append(" )\r\n");
  sb.append(" output formats=(\r\n");
  for (ItemInfo ii : getOutputFormatInfo()) {
    sb.append("  "+ii.toString()+"\r\n");
  }
  sb.append(" )\r\n");
  sb.append(" featureFormats=(\r\n");
  for (ItemInfo ii : getFeatureFormatInfo()) {
    sb.append("  "+ii.toString()+"\r\n");
  }
  sb.append(" )\r\n");
  sb.append(" rasterFormats=(\r\n");
  for (ItemInfo ii : getRasterFormatInfo()) {
    sb.append("  "+ii.toString()+"\r\n");
  }
  sb.append(" )\r\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}
}
