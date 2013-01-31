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
package com.esri.gpt.framework.search;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.esri.gpt.catalog.search.ResourceLinkBuilder;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.catalog.search.SearchResult;
import com.esri.gpt.catalog.search.SearchResultRecord;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.request.Record;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.CswRecord;

/**
 * The Class CswRecord.
 * 
 * 
 */
public class SearchXslRecord extends Record {

// class variables =============================================================
/** Class logger  */
private static Logger LOG = 
  Logger.getLogger(SearchXslRecord.class.getCanonicalName());
/* The Default Date Format */
public static final String DEFAULT_DATE_FORMAT =  "yyyy-dd-MM HH:MM:SS";

/** key goes into the objectMap in searchResultRecord 
 * 
 */
public static final String KEY_LINK_INFO = "linkInfo";

public static final String KEY_REFERENCES = "XSL_RESULT_REFERENCES";

public static final String KEY_TYPES = "XSL_RESULT_TYPES";

// instance variables ==========================================================
/** The abstract data. */
private String   abstractData;

/** The brief metadata. */
private String   briefMetadata;

/** The full metadata. */
private String   fullMetadata;

/** The id. */
private String   id;

/** The is live data or map. */
private boolean  isLiveDataOrMap;

/** The metadata resource URL. */
private String   metadataResourceURL;

/** The summary metadata. */
private String   summaryMetadata;

/** The title. */
private String   title;

/** The bounding box. */
private Envelope envelope;

/** The types. */
private DcList types;

/** The references. */
private DcList references;

/** The modified date. */
private String modifiedDate;

/** The link **/
private SearchXslRecordLinks links;

private boolean isDefaultGeometry = false;

// constructors ================================================================
/**
 * Instantiates a new csw record.
 */
public SearchXslRecord() {
}

/**
 * Instantiates a new csw record.
 * 
 * @param id the id
 */
public SearchXslRecord(String id) {
  this.id = id;
}


/**
 * The Constructor.
 * 
 * @param sabstract the sabstract
 * @param id the id
 * @param title the title
 */
public SearchXslRecord(String id, String title, String sabstract) {
  this.id = id;
  this.title = title;
  this.abstractData = sabstract;
}

// properties ==================================================================

/**
 * Gets the modified date.
 * 
 * @return the modified date (possibly null)
 */
public String getModifiedDate() {
  return modifiedDate;
}

/**
 * Sets the modified date.
 * 
 * @param modifiedDate the new modified date
 */
public void setModifiedDate(String modifiedDate) {
  this.modifiedDate = modifiedDate;
}

/**
 * Gets the references.
 * 
 * @return the references (never null)
 */
public DcList getReferences() {
  if(references == null) {
    references = new DcList();
  }
  return references;
}

/**
 * Sets the references.
 * 
 * @param references the new references
 */
public void setReferences(DcList references) {
  this.references = references;
}

/**
 * Sets the reference.
 * 
 * @param list the new reference
 */
public void setReference(String list) {
  this.setReferences(new DcList(list));
}

/**
 * Gets the types.
 * 
 * @return the types (never null)
 */
public DcList getTypes() {
  if(types == null){
    types = new DcList();
  }
  return types;
}

/**
 * Sets the types.
 * 
 * @param types the new types
 */
public void setTypes(DcList types) {
  this.types = types;
}

/**
 * Sets the types.
 * 
 * @param list the new types
 */
public void setTypes(String list) {
  this.setTypes(new DcList(list));
}

/**
 * Gets the bounding box.
 * 
 * @return the bounding box (possibly null)
 */
public Envelope getEnvelope() {
  return envelope;
}

/**
 * Sets the bounding box.
 * 
 * @param boundingBox the new bounding box
 */
public void setEnvelope(Envelope boundingBox) {
  this.envelope = boundingBox;
}

/**
 * Gets the abstract data.
 * 
 * @return the abstract data (possibly null)
 */
public String getAbstractData() {
  return abstractData;
}

/**
 * Sets the abstract data.
 * 
 * @param abstractData the new abstract data
 */
public void setAbstractData(String abstractData) {
  this.abstractData = abstractData;
}

/**
 * Gets the brief metadata.
 * 
 * @return the brief metadata (possibly null)
 */
public String getBriefMetadata() {
  return briefMetadata;
}

/**
 * Sets the brief metadata.
 * 
 * @param briefMetadata the new brief metadata
 */
public void setBriefMetadata(String briefMetadata) {
  this.briefMetadata = briefMetadata;
}

/**
 * Gets the full metadata.
 * 
 * @return the full metadata (possibly null)
 */
public String getFullMetadata() {
  return fullMetadata;
}

/**
 * Sets the full metadata.
 * 
 * @param fullMetadata the new full metadata
 */
public void setFullMetadata(String fullMetadata) {
  this.fullMetadata = fullMetadata;
}

/**
 * Gets the id.
 * 
 * @return the id (possibly null)
 */
public String getId() {
  return id;
}

/**
 * Sets the id.
 * 
 * @param id the new id
 */
public void setId(String id) {
  this.id = id;
}

/**
 * Checks if is live data or map.
 * 
 * @return true, if is live data or map
 */
public boolean isLiveDataOrMap() {
  return isLiveDataOrMap;
}

/**
 * Sets the live data or map.
 * 
 * @param isLiveDataOrMap the new live data or map
 */
public void setLiveDataOrMap(boolean isLiveDataOrMap) {
  this.isLiveDataOrMap = isLiveDataOrMap;
}

/**
 * Gets the metadata resource URL.
 * 
 * @return the metadata resource URL (possibly null)
 */
public String getMetadataResourceURL() {
  return metadataResourceURL;
}

/**
 * Sets the metadata resource URL.
 * 
 * @param metadataResourceURL the new metadata resource URL
 */
public void setMetadataResourceURL(String metadataResourceURL) {
  this.metadataResourceURL = metadataResourceURL;
}

/**
 * Gets the summary metadata.
 * 
 * @return the summary metadata (possibly null)
 */
public String getSummaryMetadata() {
  return summaryMetadata;
}

/**
 * Sets the summary metadata.
 * 
 * @param summaryMetadata the new summary metadata
 */
public void setSummaryMetadata(String summaryMetadata) {
  this.summaryMetadata = summaryMetadata;
}

/**
 * Gets the title.
 * 
 * @return the title (possibly null)
 */
public String getTitle() {
  return title;
}

/**
 * Sets the title.
 * 
 * @param title the new title
 */
public void setTitle(String title) {
  this.title = title;
}

/**
 * Gets the modified date as date object.
 * Uses the simple date format returned by arcIMS CSW.  If error, exception 
 * thrown.  Use this.getModifiedDate to get the String representation for self
 * parsing i.e. yyyy-dd-MM HH:MM:SS e.g. 2008-02-12 13:06:04
 * @return the modified date as date object
 * @throws ParseException if modified date format could not be understood 
 *  
 *  */
public Date getModifiedDateAsDateObject() {
  return getModifiedDateAsDateObject(DEFAULT_DATE_FORMAT);
}

/**
 * Gets the modified date as date object.
 * 
 * @param format the date format.  If null, default format used.
 * 
 * @return the modified date as date object
 */
public Date getModifiedDateAsDateObject(String format) {
  if(format == null || format.trim().equals("")) {
    format = DEFAULT_DATE_FORMAT;
  }
  String date = Val.chkStr(this.getModifiedDate());
  DateFormat fmt = new SimpleDateFormat(format);
  try {
    return fmt.parse(date);
  } catch (ParseException e) {
      try {
        int tmp = date.length() - date.lastIndexOf(":");
        if(tmp > 0 && tmp <= 3) {
          date = date.substring(0, date.lastIndexOf(":")) + 
            date.substring(date.lastIndexOf(":") + 1);
          return fmt.parse(date);
        }
        
      } catch (Throwable f) {
        LOG.log(Level.WARNING, "ignoring record date = "+ date + ":" 
            + e.getMessage() + " : " + f.getMessage()); 
      }
  }
  return null;
}

/**
 * Gets the custom links.
 * 
 * @return the custom links (never null)
 */
public SearchXslRecordLinks getLinks() {
  
  if(links == null) {
    links = new SearchXslRecordLinks();
  }
  return links;
}

/**
 * Checks if is default geometry. Tells us whether the record has a true envelope
 * or whether just the default geometry is being used.
 * 
 * @return true, if is default envelope
 */
public boolean isDefaultGeometry() {
  return isDefaultGeometry;
}


/**
 * Sets the default envelope.
 * 
 * @param isDefaultGeometry the new default envelope
 */
public void setDefaultEnvelope(boolean isDefaultGeometry) {
  this.isDefaultGeometry = isDefaultGeometry;
}

// methods =====================================================================
/**
 * Read as search result.
 * 
 * @param resourceLinkBuilder the resource link builder
 * @param isExternal the is external (if the resource is not the native search, 
 * set to true)
 * @param rid the remote id of the site
 * @return the search result record
 * @throws SearchException the search exception
 */
public SearchResultRecord readAsSearchResult(
    ResourceLinkBuilder resourceLinkBuilder, boolean isExternal, String rid) 
throws SearchException {
  
  SearchResultRecord searchResultRecord = new SearchResultRecord();
  searchResultRecord.setAbstract(this.getAbstractData());
  searchResultRecord.setTitle(this.getTitle());
  searchResultRecord.setExternal(isExternal);  
  searchResultRecord.setExternalId(rid);

  // marshall envelopes
  searchResultRecord.setEnvelope(this.getEnvelope());
    
  // marshall id
  searchResultRecord.setUuid(this.getId());
    try {
    searchResultRecord.setModifiedDate(
          this.getModifiedDateAsDateObject("yyyy-MM-dd'T'HH:mm:ssZ")
        );
  } catch (Exception e) {
    LOG.log(Level.INFO, "Could not set modfied time on record " 
        + searchResultRecord.getUuid());
  }
  
  if(resourceLinkBuilder == null) {
    throw new SearchException(new NullPointerException("ResourceLinkBuilder "
      + "object in search engine is null"));
  }
  searchResultRecord.getObjectMap().put(KEY_LINK_INFO, this.getLinks());
  searchResultRecord.getObjectMap().put(KEY_TYPES, this.getTypes());
  searchResultRecord.getObjectMap().put(KEY_REFERENCES, this.getReferences());
  
  searchResultRecord.setDefaultGeometry(this.isDefaultGeometry());
  
  resourceLinkBuilder.build(this, searchResultRecord);
  
  return searchResultRecord;

}

}