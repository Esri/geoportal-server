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
package com.esri.gpt.control.livedata.sos;

import com.esri.gpt.framework.util.Val;

/**
 * Sensor Observation Service context.
 * Holds connection information.
 */
public class SOSContext {

/** image width */
private int width;
/** image height */
private int height;
/** service url */
private String url = "";
/** service access method */
private SimpleSOSBridge.Method method = SimpleSOSBridge.Method.parse(null);
/** offering id */
private String offeringName = "";
/** response format */
private String responseFormat = "";
/** observed property */
private String observedProperty = "";
/** feature of interest */
private String featureOfInterest = "";
/** begind position (time) */
private String beginPosition = "";
/** end position (time) */
private String endPosition = "";

/**
 * Gets image width.
 * @return the width
 */
public int getWidth() {
  return width;
}

/**
 * Sets image width.
 * @param width the width to set
 */
public void setWidth(int width) {
  this.width = width;
}

/**
 * Gets image height.
 * @return the height
 */
public int getHeight() {
  return height;
}

/**
 * Sets image height.
 * @param height the height to set
 */
public void setHeight(int height) {
  this.height = height;
}

/**
 * Gets service URL.
 * @return the url
 */
public String getUrl() {
  return url;
}

/**
 * Sets service URL.
 * @param url the url to set
 */
public void setUrl(String url) {
  this.url = Val.chkStr(url);
}

/**
 * Gets service access method.
 * @return the method
 */
public SimpleSOSBridge.Method getMethod() {
  return method;
}

/**
 * Sets service method.
 * @param method the method to set
 */
public void setMethod(SimpleSOSBridge.Method method) {
  this.method = method != null ? method : SimpleSOSBridge.Method.parse(null);
}

/**
 * Gets offering id.
 * @return the offeringName
 */
public String getOfferingName() {
  return offeringName;
}

/**
 * Sets offering id.
 * @param offeringName the offeringName to set
 */
public void setOfferingName(String offeringName) {
  this.offeringName = Val.chkStr(offeringName);
}

/**
 * Gets observed property.
 * @return the observedProperty
 */
public String getObservedProperty() {
  return observedProperty;
}

/**
 * Sets response format.
 * @param responseFormat the response format to set
 */
public void setResponseFormat(String responseFormat) {
  this.responseFormat = Val.chkStr(responseFormat);
}

/**
 * Gets response format.
 * @return the response format
 */
public String getResponseFormat() {
  return responseFormat;
}

/**
 * Sets observed property.
 * @param observedProperty the observedProperty to set
 */
public void setObservedProperty(String observedProperty) {
  this.observedProperty = Val.chkStr(observedProperty);
}

/**
 * Gets feature of interest.
 * @return the featureOfInterest
 */
public String getFeatureOfInterest() {
  return featureOfInterest;
}

/**
 * Sets feature of interest.
 * @param featureOfInterest the featureOfInterest to set
 */
public void setFeatureOfInterest(String featureOfInterest) {
  this.featureOfInterest = Val.chkStr(featureOfInterest);
}

/**
 * Gets begin position.
 * @return the beginPosition
 */
public String getBeginPosition() {
  return beginPosition;
}

/**
 * Sets begin position.
 * @param beginPosition the beginPosition to set
 */
public void setBeginPosition(String beginPosition) {
  this.beginPosition = Val.chkStr(beginPosition);
}

/**
 * Gets end position.
 * @return the endPosition
 */
public String getEndPosition() {
  return endPosition;
}

/**
 * Sets end position.
 * @param endPosition the endPosition to set
 */
public void setEndPosition(String endPosition) {
  this.endPosition = Val.chkStr(endPosition);
}
}
