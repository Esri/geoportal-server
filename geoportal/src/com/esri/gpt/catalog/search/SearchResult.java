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

import java.util.logging.Logger;

import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.request.QueryResult;
import com.esri.gpt.framework.util.Val;

/**
 * The Class SearchResult. Represents a a result from a search.
 * methods also summarise the records contained within.
 */
public class SearchResult extends QueryResult<SearchResultRecords> {

// class variables =============================================================
/** Class logger. */
private static Logger LOG = 
  Logger.getLogger(SearchResult.class.getCanonicalName());

// instance variables ==========================================================
/** The current envelope in view. */
private Envelope currentEnvelopeInView;

/** The current metadata in view. */
private String currentMetadataXmlInView;

/** The search time in seconds. */
private double searchTimeInSeconds = Integer.MIN_VALUE;

/** The supports content type query. */
private boolean supportsContentTypeQuery;

/** The supports spatial display. */
private boolean supportsSpatialDisplay;

//properties ===================================================================

/**
 * Gets the current metadata xml requested for  view.
 * 
 * @return the current metadata xml in view (trimmed, never null)
 */
public String getCurrentMetadataXmlInView() {
  return Val.chkStr(currentMetadataXmlInView);
}

/**
 * Sets the current metadata xml requested view.
 * 
 * @param currentMetadataInView the new current metadata xml in view
 */
public void setCurrentMetadataXmlInView(String currentMetadataInView) {
  this.currentMetadataXmlInView = currentMetadataInView;
}


/**
 * Gets the current envelope in view.
 * 
 * @return the current envelope in view (never null)
 */
public Envelope getCurrentEnvelopeInView() {
  if(currentEnvelopeInView == null) {
    this.setCurrentEnvelopeInView(this.getEnclosingEnvelope());
  }
  return currentEnvelopeInView;
}

/**
 * Sets the current envelope in view.
 * 
 * @param currentEnvelopeInView the new current envelope in view
 */
public void setCurrentEnvelopeInView(Envelope currentEnvelopeInView) {
  this.currentEnvelopeInView = currentEnvelopeInView;
}


/**
 * Gets the enclosing envelope.
 * 
 * @return the enclosing envelope (never null)
 */
public Envelope getEnclosingEnvelope() {
  
  //TODO: Check with urban about merging envelopes
  Envelope encEnv = null;
  Envelope recordEnvelope = null;
  
  for(SearchResultRecord record : this.getRecords()) {
    recordEnvelope = record.getEnvelope();
    if(encEnv == null) {
      encEnv = new Envelope();
      encEnv.setMaxX(recordEnvelope.getMaxX());
      encEnv.setMaxY(recordEnvelope.getMaxY());
      encEnv.setMinX(recordEnvelope.getMinX());
      encEnv.setMinY(recordEnvelope.getMinY());
      continue;
    }
   encEnv.merge(recordEnvelope);
   
  }
  
  if(encEnv == null) {
    encEnv = SearchConfig.getConfiguredInstance().getDefaultEnvelope();
    
  }

  return encEnv;
  
}

/**
 * Gets the search time in seconds.
 * 
 * @return the search time in seconds
 */
public double getSearchTimeInSeconds() {
  return Math.round(searchTimeInSeconds * 10000)/10000.0;
}

/**
 * Sets the search time in seconds.
 * 
 * @param searchTimeInSeconds the new search time in seconds
 */
public void setSearchTimeInSeconds(double searchTimeInSeconds) {
  this.searchTimeInSeconds = searchTimeInSeconds;
}

/**
 * Checks if is supports content type query.
 * 
 * @return true, if is supports content type query
 */
public boolean isSupportsContentTypeQuery() {
  return supportsContentTypeQuery;
}

/**
 * Sets the supports content type query.
 * 
 * @param supportsContentTypeQuery the new supports content type query
 */
public void setSupportsContentTypeQuery(boolean supportsContentTypeQuery) {
  this.supportsContentTypeQuery = supportsContentTypeQuery;
}

/**
 * Checks if is supports spatial display.
 * 
 * @return true, if is supports spatial display
 */
public boolean isSupportsSpatialDisplay() {
  return supportsSpatialDisplay;
}

/**
 * Sets the supports spatial display.
 * 
 * @param supportsSpatialDisplay the new supports spatial display
 */
public void setSupportsSpatialDisplay(boolean supportsSpatialDisplay) {
  this.supportsSpatialDisplay = supportsSpatialDisplay;
}

// methods =====================================================================
/**
 * Returns records.
 * 
 * @return SearchResultRecords (never null)
 * 
 * @see com.esri.gpt.framework.request.QueryResult#getRecords()
 */
@Override
public SearchResultRecords getRecords() {

  if(super.getRecords() == null) {
    
    this.setRecords(new SearchResultRecords());
  }
  return super.getRecords();
 
      
}

/**
 * Gets the record size.
 * 
 * @return the record size
 */
public int getRecordSize() {
  
  return this.getRecords().size();
  
}

/**
 * Sets the page cursor.
 * 
 * @param cursor the cursor
 * 
 * @see com.esri.gpt.framework.request.QueryResult#setPageCursor(com.esri.gpt.framework.request.PageCursor)
 */
@Override
public void setPageCursor(PageCursor cursor) {
  super.setPageCursor(cursor);
  
}

/**
 * Gets the record as JSON. Will give envelopes & and contenttype
 * 
 * @return the record envelopes as JSON (never null)
 */
public String getRecordsAsJSON() {
  StringBuffer jsRecords = new StringBuffer();
  jsRecords.append("{ \"records\" : [");
  SearchResultRecords records = this.getRecords();
  int index = 0;
  for(SearchResultRecord record : records ) {
    Envelope recEnvelope = record.getEnvelope().clone();
    //Object obj = record.getObjectMap().get(key)
    if(recEnvelope.getMaxY() > 90){
      recEnvelope.setMaxY(90);
    }
    if(recEnvelope.getMinY() < -90) {
      recEnvelope.setMinY(-90);
    }
    jsRecords.append(" {\"bboxes\" : [");
    Envelope[] envs = this.internationalDatelineSplit(recEnvelope);
    int i = 0;
    Envelope enclosingEnvelope = null;
    do {
       
      if(i > 0){
        jsRecords.append(",");
      }
      jsRecords
      .append("{")
      .append( " \"isDefaultGeometry\"  : ").append(record.isDefaultGeometry()).append(",")
      .append( " \"maxX\"  : ").append(envs[i].getMaxX()).append(",")
      .append( " \"maxY\"  : ").append(envs[i].getMaxY()).append(",")
      .append( " \"minX\"  : ").append(envs[i].getMinX()).append(",")
      .append( " \"minY\"  : ").append(envs[i].getMinY())
      .append("}");
      
      if(enclosingEnvelope == null) {
        enclosingEnvelope = envs[i];
      } else {
        enclosingEnvelope.merge(envs[i]);
      }
      i++;
      
    } while(i < envs.length);
    
    jsRecords.append("], \"enclosingEnvelope\" : ")
      .append("{")
      .append( " \"maxX\"  : ").append(enclosingEnvelope.getMaxX()).append(",")
      .append( " \"maxY\"  : ").append(enclosingEnvelope.getMaxY()).append(",")
      .append( " \"minX\"  : ").append(enclosingEnvelope.getMinX()).append(",")
      .append( " \"minY\"  : ").append(enclosingEnvelope.getMinY())
      .append("}");
    
    jsRecords.append(", \"uuid\" : ").append("\"")
      .append(Val.escapeStrForJson(record.getUuid()))
      .append("\"");
    
    jsRecords.append("}");
    enclosingEnvelope = null;
   
    if(index < records.size()) {
      jsRecords.append(",");
    }
  }
  jsRecords.append("]}");
  
  return jsRecords.toString();
}

/**
 * International dateline split. Splits envelope
 * into 2 if envelope goes over international dateline
 * 
 * @param envelope the envelope
 * 
 * @return the envelope[]
 */
private Envelope[] internationalDatelineSplit(Envelope envelope) {
  if(envelope.getMaxX() >= envelope.getMinX()) {
    return new Envelope[]{envelope};
  }
  Envelope leftEnvelope = new Envelope();
  Envelope rightEnvelope = new Envelope();
  leftEnvelope.setMaxY(envelope.getMaxY());
  leftEnvelope.setMinY(envelope.getMinY());
  leftEnvelope.setMinX(-180);
  leftEnvelope.setMaxX(envelope.getMaxX());
  
  rightEnvelope.setMaxY(envelope.getMaxY());
  rightEnvelope.setMinY(envelope.getMinY());
  rightEnvelope.setMaxX(180);
  rightEnvelope.setMinX(envelope.getMinX());
    
  return new Envelope[]{leftEnvelope, rightEnvelope};
}

/**
 * Clear/Reset all instance variables.
 */
public void clear() {
  super.reset();
  currentEnvelopeInView = null;
  this.currentMetadataXmlInView = null;
  super.setMaxQueryHits(Integer.MIN_VALUE);
}








}
