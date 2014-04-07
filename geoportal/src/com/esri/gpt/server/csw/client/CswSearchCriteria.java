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
package com.esri.gpt.server.csw.client;

import com.esri.gpt.framework.request.Criteria;
import com.esri.gpt.framework.util.Val;

/**
 * The Class CswSearchCriteria.
 * 
 * Minimal used for a for a csw search
 * 
 */
@SuppressWarnings("serial")
public class CswSearchCriteria extends Criteria {

// instance variables ==========================================================
/** The _envelope. */
private Envelope _envelope;

/** The _live data and maps only. */
private boolean _liveDataAndMapsOnly;

/** The _max records. */
private int _maxRecords;

/** The _search text. */
private String _searchText;

/** The _start position. */
private int _startPosition;


private boolean envelopeIntersects;

private boolean envelopeContains;

private boolean useGeographicExtent;



// constructors ================================================================
/**
 * Instantiates a new csw search criteria.
 */
public CswSearchCriteria() {
  // set DEFAULT start position and max records.
  _startPosition = 1;
  _maxRecords = 10;
}

// properties ==================================================================
/**
 * Accessor methods.
 * 
 * @return the envelope
 */
public Envelope getEnvelope() {
  return _envelope;
}

/**
 * Sets the envelope.
 * 
 * @param envelope the new envelope
 */
public void setEnvelope(Envelope envelope) {
  _envelope = envelope;
}

/**
 * Checks if is live data and maps only.
 * 
 * @return true, if is live data and maps only
 */
public boolean isLiveDataAndMapsOnly() {
  return _liveDataAndMapsOnly;
}

/**
 * Sets the live data and maps only.
 * 
 * @param liveDataAndMapsOnly the new live data and maps only
 */
public void setLiveDataAndMapsOnly(boolean liveDataAndMapsOnly) {
  _liveDataAndMapsOnly = liveDataAndMapsOnly;
}



/**
 * Gets the max records.
 * 
 * @return the max records
 */
public int getMaxRecords() {
  return _maxRecords;
}

/**
 * Sets the max records.
 * 
 * @param maxRecords the new max records
 */
public void setMaxRecords(int maxRecords) {
  _maxRecords = maxRecords;
}

/**
 * Gets the search text.
 * 
 * @return the search text
 */
public String getSearchText() {
  return _searchText;
}

/**
 * Sets the search text.
 * 
 * @param text the new search text
 */
public void setSearchText(String text) {
  _searchText = text;
}

/**
 * Gets the start position.
 * 
 * @return the start position
 */
public int getStartPosition() {
  return _startPosition;
}

/**
 * Sets the start position.
 * 
 * @param position the new start position
 */
public void setStartPosition(int position) {
  _startPosition = position;
}

/**
 * @return
 */


public boolean isEnvelopeIntersects() {
	return envelopeIntersects;
}

public void setEnvelopeIntersects(boolean envelopeIntersects) {
	this.envelopeIntersects = envelopeIntersects;
}

public boolean isEnvelopeContains() {
	return envelopeContains;
}

public void setEnvelopeContains(boolean envelopeContains) {
	this.envelopeContains = envelopeContains;
}

public boolean isUseGeographicExtent() {
	return useGeographicExtent;
}

public void setUseGeographicExtent(boolean useGeographicExtent) {
	this.useGeographicExtent = useGeographicExtent;
}

// methods ================================================================================================================

/**
 * To xml.
 * 
 * @return the string
 */
public String toXml() {
  CswSearchCriteria search = this;
  String request = "<?xml version='1.0' encoding='UTF-8' ?>";
  request += "<GetRecords>" + "<StartPosition>" + search.getStartPosition()
      + "</StartPosition>";
  request += "<MaxRecords>" + search.getMaxRecords() + "</MaxRecords>";
  request += "<KeyWord>" + Val.escapeXml(search.getSearchText()) + "</KeyWord>";
  request += ("<LiveDataMap>" + search.isLiveDataAndMapsOnly() + "</LiveDataMap>");
  if (search.getEnvelope() != null) {
    request += ("<Envelope>");
    request += "<MinX>" + search.getEnvelope().getMinX() + "</MinX>";
    request += "<MinY>" + search.getEnvelope().getMinY() + "</MinY>";
    request += "<MaxX>" + search.getEnvelope().getMaxX() + "</MaxX>";
    request += "<MaxY>" + search.getEnvelope().getMaxY() + "</MaxY>";
    request += "</Envelope>";
    request += "<RecordsFullyWithinEnvelope>"+ this.isEnvelopeContains() +"</RecordsFullyWithinEnvelope>";
    request += "<RecordsIntersectWithEnvelope>"+ this.isEnvelopeIntersects() +"</RecordsIntersectWithEnvelope>";
   
  }
  request += "</GetRecords>";

  return request;

}



/* 
 * Reset of criteria
 * 
 * @see com.esri.gpt.framework.request.Criteria#reset()
 */
@Override
public void reset() {
  this.setSearchText("");
  this.setEnvelope(null);
  this.setLiveDataAndMapsOnly(false);
  this.setMaxRecords(-1);
  
}

}