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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.search.SearchXslRecord;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XmlIoUtil;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.server.csw.client.CswCatalog;
import com.esri.gpt.server.csw.client.CswCatalogCapabilities;
import com.esri.gpt.server.csw.client.CswClient;
import com.esri.gpt.server.csw.client.CswProfile;
import com.esri.gpt.server.csw.client.CswProfiles;
import com.esri.gpt.server.csw.client.CswRecord;
import com.esri.gpt.server.csw.client.CswRecords;
import com.esri.gpt.server.csw.client.CswResult;
import com.esri.gpt.server.csw.client.CswSearchCriteria;
import com.esri.gpt.server.csw.client.CswSearchRequest;
import com.esri.gpt.server.csw.client.InvalidOperationException;
import com.esri.gpt.server.csw.client.NullReferenceException;
import java.text.ParseException;
import java.util.Date;



/**
 * The class SearchEngineCSW. Accesses a CSW repository to send searTch 
 * and parse back results.  works closely with the csw client jar.  
 * Parameters that can be used are "url" and "profileId".
 */
public class SearchEngineCSW extends ASearchEngine {

// class variables =============================================================
/** Date format used by csw **/
public static final String DATE_FORMAT = "yyyy-MM-dd";

/** Date format object **/
private static SimpleDateFormat DATE_FORMAT_OBJ = 
  new SimpleDateFormat(DATE_FORMAT); 

public static final String KEY_LINK_INFO = "linkInfo";

public static final String KEY_REFERENCES = "XSL_RESULT_REFERENCES";

public static final String KEY_TYPES = "XSL_RESULT_TYPES";

/** constructors ============================================================ */
public SearchEngineCSW(RequestContext context) {
  super(context);
}

/**
 * The Enum Scheme.  Used to get values from  csw class DcList.
 * 
 */
public static enum Scheme {
  
  /** The FGDC content type scheme */
  CONTENTTYPE_FGDC
    ("urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ContentType") ,
  
  /** The ISO content type scheme. */
  CONTENTTYPE_ISO
    ("http://www.isotc211.org/2005/gmd/MD_Metadata/hierarchyLevelName"),
  
  /** The server scheme. */
  SERVER
    ("urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Server"),
  
  /** The service scheme. */
  SERVICE
    ("urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Service"),
  
  /** The service type scheme. */
  SERVICE_TYPE
    ("urn:x-esri:specification:ServiceType:ArcIMS:Metadata:ServiceType"),
  
  /** The fgdc onlink scheme. */
  ONLINK_FGDC
    ("urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Onlink"),
  
  /** The ISO onlink scheme. */
  ONLINK_ISO
    ("http://www.isotc211.org/2005/gmd/MD_BrowseGraphic/filename"), 
  
   /** The FGDC thumbnail scheme **/
  THUMBNAIL_FGDC
    ("urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Thumbnail"),
  
  /** The iso thumbnail scheme **/
  THUMBNAIL_ISO
    ("http://www.isotc211.org/2005/gmd/MD_BrowseGraphic/filename"),
  
  /** The Metadata document scheme. */
  METADATA_DOCUMENT
    ("urn:x-esri:specification:ServiceType:ArcIMS:Metadata:Document");
  
  
  /** The string representation. */
  private String urn;
  

  
  /**
   * Instantiates a new scheme.
   * 
   * @param r the r
   */
  private Scheme(String urn) {
    this.urn = urn;
  }
  
  /**
   * Gets the urn.
   * 
   * @return the urn (trimmed, never null)
   */
  public String getUrn(){
    return Val.chkStr(this.urn);
  }
  
}

/** ArcIMS content types */
public static enum AimsContentTypes {
  unknown,
  
  liveData,
  
  downloadableData,
  
  offlineData,
  
  staticMapImage,
  
  document,
  
  application,
  
  geographicService,
  
  clearinghouse,
  
  mapFiles,
  
  geographicActivities;
  
  /**
   * Checks value.
   * @param value value 
   * @return enum object from AimsContentType
   */
  public static AimsContentTypes checkValueOf(String value) {
    value = Val.chkStr(value);
    for (AimsContentTypes act : values()) {
      if (act.name().equalsIgnoreCase(value)) {
        return act;
      }
    }
    return unknown;
  }
}

/**
 * The Enum SEARCH_OPERATION.  Types of search operation used in SearchCriteria
 * DOM
 */
public static enum SEARCH_OPERATION {

/** The do pre-fetch. Appended to search criteria DOM during prefetch ops. */
doPrefetch,

/** The do search. Appended to search criteria DOM during search ops. */
doSearch

};

/** The class logger. */
private final static Logger LOG 
  = Logger.getLogger(SearchEngineCSW.class.getCanonicalName());

// instance variables ==========================================================
/** The csw client. */
private CswClient cswClient;

/** The profile id. */
private String profileId;

/** The get metadata record url. */
private String getMetadataRecordUrl;

/** The post request url. */
private String getRecordsUrl; 

/** The get capabilties url. */
private String getCapabiltiesUrl;

/** The capabilities. */
private CswCatalogCapabilities capabilities;

// constructor =================================================================
/**
 * Instantiates a new search DAOCSW.
 */
protected SearchEngineCSW() {
  super();
}

// properties ==================================================================
/**
 * Gets the capabilities.
 * 
 * @return the capabilities
 */
public CswCatalogCapabilities getCapabilities() {
  return capabilities;
}

/**
 * Sets the capabilities.
 * 
 * @param capabilities the new capabilities
 */
public void setCapabilities(CswCatalogCapabilities capabilities) {
  this.capabilities = capabilities;
}


/**
 * Gets the gets the metadata record url.
 * 
 * @return the gets the metadata record url
 */
public String getGetMetadataRecordUrl() {
  return getMetadataRecordUrl;
}

/**
 * Sets the gets the metadata record url. (will be used as prefix for getMetadata
 * 
 * @param getMetadataRecordUrl the new gets the metadata record url
 */
public void setGetMetadataRecordUrl(String getMetadataRecordUrl) {
  this.getMetadataRecordUrl = getMetadataRecordUrl;
}

/**
 * Gets the gets the capabilties url.
 * 
 * @return the gets the capabilties url (trimmed, never null)
 */
public String getGetCapabiltiesUrl() {
  return Val.chkStr(getCapabiltiesUrl);
}

/**
 * Sets the gets the capabilties url.
 * 
 * @param getCapabiltiesUrl the new gets the capabilties url
 */
public void setGetCapabiltiesUrl(String getCapabiltiesUrl) {
  this.getCapabiltiesUrl = getCapabiltiesUrl;
  
}

/**
 * Gets the get records request url.
 * 
 * @return the post request url (trimmed, never null)
 */
public String getGetRecordsUrl() {
  return Val.chkStr(getRecordsUrl);
}

/**
 * Sets the get records request url.
 * 
 * @param postRequestUrl the new post request url
 * 
 * @throws SearchException the search exception
 */
public void setGetRecordsUrl(String postRequestUrl) throws SearchException {
  this.getRecordsUrl = postRequestUrl;
  try {
    this.setConnectionUri(new URL(postRequestUrl).toURI());
  } catch (Exception e) {
    throw new SearchException("Could not make URL object from post requet url = " 
        + postRequestUrl + " : " 
        + e.getMessage(), e);
  }
}

/**
 * Gets the csw client.
 * 
 * @return the csw client (never null)
 */
public CswClient getCswClient() {
  if(cswClient == null){
    CswClient client = new CswClient();
    client.setConnectTimeout(SearchConfig.getConfiguredInstance().getTimeOut());
    client.setReadTimeout(client.getConnectTimeout() * 10);
    this.setCswClient(client);
  }
  return cswClient;
}

/**
 * Sets the csw client.
 * 
 * @param cswClient the new csw client
 */
public void setCswClient(CswClient cswClient) {
  this.cswClient = cswClient;
}

/**
 * Gets the profile id.
 * 
 * @return the profile id (trimmed, never null)
 */
public String getProfileId() {
  if(profileId == null || "".equals(profileId.trim())) {
    this.setProfileId(SearchConfig
        .getConfiguredInstance().getCswProfile());
  }
  return Val.chkStr(profileId);
}

/**
 * Sets the profile id.
 * 
 * @param profileId the new profile id
 */
public void setProfileId(String profileId) {
  this.profileId = profileId;
}


// methods =====================================================================


@Override
public void init() throws SearchException {
  
  // Cache so that we are not always connecting for a get capabilties on
  // each page request since this engine can be generated on each page request
  // and might not be stored in a session
  
  //Update - Urban prefers the cache to be session based.  commenting out
  //application caching.  Currently Search Factor caches
  
  try {
    initAttributes();
    //String key = this.getCapabiltiesUrl + ":" + this.getProfileId();
    CswCatalogCapabilities cap = null;
    //cap = CAPABILITIES_CACHE.get(key);
    //if(cap == null) {
       CswCatalog cswCatalog = new CswCatalog(this.getGetCapabiltiesUrl(), "",
           readCswProfiles().getProfileById(this.getProfileId()));
       cswCatalog.setConnectionTimeoutMs(this.getConnectionTimeoutMs());
       cswCatalog.setResponseTimeoutMs(this.getResponseTimeoutMs());
       cswCatalog.connect();
       cap = cswCatalog.getCapabilities();
       this.setCapabilities(cap);
       //CAPABILITIES_CACHE.put(key, cap);
    //}
    this.setGetMetadataRecordUrl(cap.get_getRecordByIDGetURL());
    this.setGetRecordsUrl(cap.get_getRecordsPostURL());
  } catch (Exception e) {
    throw new SearchException("Could not get capabilities from  "
        + this.getGetCapabiltiesUrl() + " : " + e.getMessage(), e);
  }
}

/**
 * Tells us if the search is external to GPT9 or Internal
 * @return false
 */
@Override
protected boolean readIsExternalSearch() {
  return false;
}

/**
 * Performs search.  It will use the SearchCritiria and SearchResults
 * supplied by the factory class.
 * 
 *
 * @throws SearchException thrown when error with search.
 * 
 */
@Override
public void doSearch() throws SearchException {

  // Basic checks
  SearchRequestDefinition reqDef = getRequestDefinition();
  if (reqDef == null) {
    throw new SearchException("null searchRequestDefinition before search.");
  }
  SearchCriteria criteria = reqDef.getCriteria();
  if (criteria == null) {
    throw new SearchException("null searchCriteria before search.");
  }
  SearchResult result = reqDef.getResult();
  if (result == null) {
    throw new SearchException("null searchResult before search.");
  }
  
  // ensure that the envelope is valid
  if (!criteria.getSearchFilterSpatial().getSelectedBounds().equalsIgnoreCase(
      ISearchFilterSpatialObj.OptionsBounds.anywhere.name())) {
    if (criteria.getSearchFilterSpatial().getEnvelope().isEmpty()) {
      criteria.getSearchFilterSpatial().getEnvelope().put(-180,-90,180,90);
    }
  }
  
  // Transform SearchCriteria DOM to CSW search String, execute
  String cswRequest = getSearchQuery();
  doSearch(cswRequest);
}

/**
 * Performs a search using a supplied CSW request string.
 * @param cswRequest the CSW request string.
 * @throws SearchException if an exception occurs
 */
public void doSearch(String cswRequest) throws SearchException {  
  LOG.fine("Performing search operation");
  SearchResult result = getRequestDefinition().getResult();
  SearchCriteria criteria = getRequestDefinition().getCriteria();
  Long timeBeforeSend = System.currentTimeMillis();
  CswRecords cswClientRecords = sendRequest(cswRequest);
  Long timeAfterSend = System.currentTimeMillis();
  this.setSearchTimeInMillis(timeAfterSend - timeBeforeSend);
  marshallRecords(cswClientRecords);
  
  //int tmp = criteria.getSearchFilterPageCursor().getTotalPageCount();
  
  //criteria.getSearchFilterPageCursor()
  //  .setTotalRecordCount(Integer.MAX_VALUE);
  //int maxQueryHits =  cswClientRecords.getMaximumQueryHits();
  /*if(cswClientRecords.getSize() > 0 && cswClientRecords.getSize() 
      + criteria.getSearchFilterPageCursor().getStartRecord() > 
     maxQueryHits) {
    maxQueryHits = cswClientRecords.getSize()  + 
      criteria.getSearchFilterPageCursor().getStartRecord();
  }*/
  result.setMaxQueryHits(cswClientRecords.getMaximumQueryHits());
  criteria.getSearchFilterPageCursor().setTotalRecordCount(
      result.getMaxQueryHits());
  checkPagination();
}



/**
 * Gets the metadata url for get record by Id.
 * 
 * @param uuid the uuid
 * 
 * @return the metadata url
 * 
 * @throws SearchException the search exception
 */
@Override
public String getMetadataUrl(String uuid) throws SearchException {
  CswRecord record = this.getMetadata(uuid);
  return Val.chkStr(record.getMetadataResourceURL());
}

/**
 * Parses the CSW response.
 * @param cswResponse the input source associated with the CSW response XML
 * @return the resultant records
 * @throws SearchException if an exception occurs
 */
public CswRecords parseResponse(String cswResponse) 
   throws SearchException {
  CswResult results = new CswResult();
  try {
    getCswProfile().readGetRecordsResponse(cswResponse,
        results);
    LOG.log(Level.FINE, "Number of records returned {0}", results.getRecords().getSize());
  } catch (Exception e) {
    throw new SearchException(e);
  } 
  return results.getRecords();
}



/**
 * Sends a CSW GetRecords request to CSW service.
 * @param cswRequest the CSW XML request
 * @return the resultant records
 * @throws SearchException the search exception
 */
protected CswRecords sendRequest(String cswRequest) throws SearchException {
  InputStream cswInput = null;
  try {
    
    // submit the request
    String url = this.getConnectionUri().toString();
    try {
      cswInput = this.getCswClient().submitHttpRequest("POST", url,cswRequest);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Exception when Posting CSW query to "+url,e);
      if (e instanceof java.net.SocketTimeoutException) {
        throw new SearchException("catalog.search.error.searchTimeOut",e, 
          new String[] {String.valueOf(SearchConfig.getConfiguredInstance().getTimeOut()/1000)});
      }
      throw new SearchException("Exception when Posting CSW query to "+url
          + ": " + e.getMessage(),  e);
    } 
    
    // parse the response
    try {
      return this.parseResponse(this.readInputCharacters(
          cswInput, "UTF-8"));
    } catch (IOException e) {
      throw new SearchException("Could not get response for send request", e);
    }
    
  } finally {    
    try {if (cswInput != null) cswInput.close();} catch (Exception ef) {}
  }
}

/**
 * Returns the search query for the search operation
 * @return CSW query for the search criteria of the object
 * @throws SearchException
 */
@Override
public String getSearchQuery() throws SearchException {

  String searchQuery = null;
  try {
    SearchRequestDefinition reqDef = getRequestDefinition();
    if (reqDef == null) {
      throw new SearchException("null searchRequestDefinition before search.");
    }
    SearchCriteria criteria = reqDef.getCriteria();
    /*if (this.getProfileId().equals(
        SearchConfig.getConfiguredInstance().getCswProfile())) {
      if (criteria == null) {
        throw new SearchException("null searchCriteria before search.");
      }
      return transformGptToCswRequest(criteria, SEARCH_OPERATION.doSearch);
    } else {*/
 
      searchQuery = this.getCswProfile().generateGetRecordsRequest(criteria, 
          null, 
          this.getHitsOnly());
      searchQuery = this.getCswProfile().generateCSWGetRecordsRequest(
          marshallGptToCswClientCriteria(criteria));
    //}
    if (searchQuery == null) {
       throw new Exception("Search Query is null");
    }
  } catch (Exception e) {
    throw new SearchException(
        "Could not translate gpt criteria as string query : " + e.getMessage(), 
        e);
  }
  return searchQuery;
}

/**
 * Transform GPT criteria to CSW request.
 * 
 * @param criteria the criteria
 * @param operation Type of operation to be appended into DOM.
 * 
 * @return the string (trimmed, never null)
 * 
 * @throws SearchException the search exception
 */
public static String transformGptToCswRequest(final SearchCriteria criteria,
    final SEARCH_OPERATION operation) 
  throws SearchException {
  
  DOMSource dSource = new DOMSource(criteria.toDom(operation.name(), 
      DATE_FORMAT_OBJ));
  StringWriter writer = new StringWriter();

  XsltTemplate template = SearchConfig.getConfiguredInstance()
      .getGptToCswXsltTemplate();

  try {
    template.transform(dSource, new StreamResult(writer), null);
    if (LOG.isLoggable(Level.FINER)) {
      LOG.log(Level.FINER, "SearchCriteria DOM := \n{0}", XmlIoUtil.domToString(criteria.toDom(operation.name(), DATE_FORMAT_OBJ)));
      LOG.log(Level.FINER, "CSW Request formed := \n{0}", writer.toString());
    }
  } catch (Exception e) {
    LOG.log(Level.SEVERE,
        "Could not translate (XSLT) search filter to CSW query", e);
    throw new SearchException(e);
  }
  return Val.chkStr(writer.toString());
  
}

/**
 * Marshall CSW client records and gpt search records.
 * @param cswRecords the CSW records
 */
@SuppressWarnings("unchecked")
protected void marshallRecords(final CswRecords cswRecords) 
  throws SearchException {
  
  Iterator iter = cswRecords.iterator();
  SearchResultRecord searchResultRecord = null;
  SearchResultRecords searchResultRecords = 
    this.getRequestDefinition().getResult().getRecords();
  searchResultRecords.clear();
  
  while(iter.hasNext()) {
    Object cswRecordObj = iter.next();
    
    if(cswRecordObj == null) {
      LOG.warning("Found csw record object from csw client is null");
      continue;
    }
    
    if(cswRecordObj instanceof CswRecord) {
       
      searchResultRecord = this.marshallRecord((CswRecord) cswRecordObj);
            
      if(LOG.isLoggable(Level.FINER)) {
        LOG.log(Level.FINER, "Result Metadata Record Id = {0}{1}", new Object[]{((CswRecord) cswRecordObj).getId(), searchResultRecord.getEnvelope().toString()});
      }
      searchResultRecords.add(searchResultRecord);
    } else if(cswRecordObj instanceof SearchXslRecord) {
      searchResultRecord =  ((SearchXslRecord) cswRecordObj)
        .readAsSearchResult(this.getResourceLinkBuilder(), 
            this.readIsExternalSearch(), this.getKey());
      searchResultRecord.setExternal(this.readIsExternalSearch());  
      searchResultRecord.setExternalId(this.getKey());
      searchResultRecords.add(searchResultRecord);
      }else {
      
      LOG.log(Level.WARNING,"CswRecord expected but not found" +
          " Found object = " + "{0}", cswRecordObj.getClass().getCanonicalName());
      
    }
    
  } 
  
  
 /* RequestContext context = this.getRequestContext();
  searchResultRecords.buildResourceLinks(context);*/
}

/**
 * Marshall record.  Converts csw client record to search result record.
 * 
 * 
 * @param cswRecord the csw record
 * 
 * @return the search result record
 */
protected SearchResultRecord marshallRecord(
    final CswRecord cswRecord) throws SearchException {
  
  SearchResultRecord searchResultRecord = new SearchResultRecord();
  searchResultRecord.setExternal(this.readIsExternalSearch());  
  searchResultRecord.setAbstract(cswRecord.getAbstractData());
  searchResultRecord.setTitle(cswRecord.getTitle());
  searchResultRecord.setExternalId(this.getKey());
  searchResultRecord.setDefaultGeometry(cswRecord.isDefaultGeometry());
  
  
  // marshall envelopes
  searchResultRecord.setEnvelope(cswRecord.getEnvelope());
    
  // marshall id
  searchResultRecord.setUuid(cswRecord.getId());
    try {
    searchResultRecord.setModifiedDate(
          cswRecord.getModifiedDateAsDateObject("yyyy-MM-dd'T'HH:mm:ssZ")
        );
  } catch (Exception e) {
    LOG.log(Level.INFO, "Could not set modfied time on record {0}", searchResultRecord.getUuid());
  }
  ResourceLinkBuilder resourceLinkBuilder = 
    this.getResourceLinkBuilder();
  if(resourceLinkBuilder == null) {
    throw new SearchException(new NullPointerException("ResourceLinkBuilder "
      + "object in search engine is null"));
  }
  resourceLinkBuilder.build(cswRecord, searchResultRecord);
  searchResultRecord.getObjectMap().put(KEY_LINK_INFO, cswRecord.getLinks());
  searchResultRecord.getObjectMap().put(KEY_TYPES, cswRecord.getTypes());
  searchResultRecord.getObjectMap().put(KEY_REFERENCES, cswRecord.getReferences());
  return searchResultRecord;

}
   


/**
 * Gets the csw profile.
 * 
 * @return the csw profile
 * 
 * @throws SearchException the search exception
 */
protected CswProfile getCswProfile() throws SearchException {
  
  try {
    
    CswProfile profile = readCswProfiles().getProfileById(this.getProfileId());
    /*
     * if (CSW_PROFILE == null) { synchronized (SearchEngineCSW.class) { if
     * (CSW_PROFILE == null) { CSW_PROFILE =
     * CSW_PROFILES.getProfileById(SearchConfig
     * .getConfiguredInstance().getCswProfile()); } } }
     */
    if (profile == null) {
      throw new Exception();
    }
    return profile;
  } catch (Exception e) {
    throw new SearchException("Could not configure search with CSW profile = "
        + this.getProfileId(), e);
  }
}

/**
 * Csw profiles.
 * 
 * @return the csw profiles
 * @throws SearchException the search exception
 */
private CswProfiles readCswProfiles() throws SearchException {
  return SearchConfig.getConfiguredInstance()
    .getGptXslProfiles().getCswProfiles();
}

@Override
public ARecord getARecord(String uuid) throws SearchException {
  final CswRecord record = getMetadata(uuid);
  ARecord aRecord = new ARecord() {
   
    public String getMetadataAsText() {
      return record.getFullMetadata();
    }

 
    public Date getModifiedDate() {
      String modifedDateAsString = record.getModifiedDate();
      try {
        return new IsoDateFormat().parseObject(modifedDateAsString);
      } catch (ParseException ex) {
        return null;
      }
    }
  };
  return aRecord;
}

/**
 * Gets the metadata as text.
 * 
 * @param uuid (Guid or uuid, method will not verify parameter)
 * 
 * @return  Metadata as Text (possibly null)
 * 
 * @throws SearchException the search exception
 */
@Override
public String getMetadataAsText(String uuid) throws SearchException {
  
  ARecord record = getARecord(uuid);
  
  return record.getMetadataAsText();
 
}

/**
 * Gets the metadata as a csw client api record.
 * 
 * @param uuid the uuid
 * 
 * @return the metadata
 * 
 * @throws SearchException the search exception
 */
protected CswRecord getMetadata(String uuid) throws SearchException {
  CswRecord record = new CswRecord();
  Exception exc = null;
  try {
    CswProfile profile = getCswProfile();
    CswSearchRequest request = new CswSearchRequest();
    CswClient client = this.getCswClient();
    request.setCswClient(client);
    record = request.getRecordById(this.getConnectionUri().toString(), 
        uuid, profile);
    
  } catch (SearchException e) {
    exc  = e;
  } catch (IOException e) {
    exc  = e;
  } catch (TransformerException e) {
    exc  = e;
  } catch (NullReferenceException e) {
    exc  = e;
  } catch (InvalidOperationException e) {
    exc  = e;
  }
  if(exc != null)  {
    throw new 
    SearchException("Could not get metadata record object uuid=" + uuid + " : " 
        + exc.getMessage(), exc);
  }
  return record;
}

/**
 * Marshall gpt to csw client criteria.
 * 
 * @param gptSearchCriteria the gpt search criteria
 * 
 * @return the csw search criteria
 */
public static CswSearchCriteria marshallGptToCswClientCriteria2(
    SearchCriteria gptSearchCriteria) {
  
  CswSearchCriteria cswClientCriteria = new CswSearchCriteria();
  cswClientCriteria.setSearchText(
      gptSearchCriteria.getSearchFilterKeyword().getSearchText());
  Envelope gptEnvelope = 
    gptSearchCriteria.getSearchFilterSpatial().getEnvelope();
  double maxX = 180;
  double maxY = 90;
  double minX = -180;
  double minY = -90;
  if(gptEnvelope != null && !gptEnvelope.isEmpty() && 
      !gptSearchCriteria.getSearchFilterSpatial().getSelectedBounds().equals(
          ISearchFilterSpatialObj.OptionsBounds.anywhere.toString())) {
    if(!gptEnvelope.isValid()) {
      gptEnvelope.setMaxX(maxX);
      gptEnvelope.setMinX(minX);
      gptEnvelope.setMaxY(maxY);
      gptEnvelope.setMinY(minY);
    }
    if(gptEnvelope.getMinX() < minX || gptEnvelope.getMinX() > maxX) {
      gptEnvelope.setMinX(minX);
    }
    if(gptEnvelope.getMaxX() < minX || gptEnvelope.getMaxX() > maxX) {
      gptEnvelope.setMaxX(maxX);
    }
    if(gptEnvelope.getMaxY() < minY || gptEnvelope.getMaxY() > maxY) {
      gptEnvelope.setMaxY(maxY);
    }
    if(gptEnvelope.getMinY() < minY || gptEnvelope.getMinY() > maxY) {
      gptEnvelope.setMinY(minY);
    }
    cswClientCriteria.setEnvelope(new com.esri.gpt.server.csw.client.Envelope(
      gptEnvelope.getMinX(), gptEnvelope.getMinY(), 
      gptEnvelope.getMaxX(), gptEnvelope.getMaxY()));
    
  }
  //Work around reset of pagecursor if there are no totalRecordCounts
  int tmp = gptSearchCriteria.getSearchFilterPageCursor().getTotalPageCount();
  
  gptSearchCriteria.getSearchFilterPageCursor()
    .setTotalRecordCount(Integer.MAX_VALUE);
  cswClientCriteria.setStartPosition(
    gptSearchCriteria.getSearchFilterPageCursor().getStartRecord());
  gptSearchCriteria.getSearchFilterPageCursor()
    .setTotalRecordCount(tmp);
  
  cswClientCriteria.setMaxRecords(
      gptSearchCriteria.getSearchFilterPageCursor().getRecordsPerPage()
      );
  
  boolean bDataWithinExtent = gptSearchCriteria.getSearchFilterSpatial().getSelectedBounds().equals(
		  ISearchFilterSpatialObj.OptionsBounds.dataWithinExtent.toString());
  boolean bDataIntersectsWithExtent = gptSearchCriteria.getSearchFilterSpatial().getSelectedBounds().equals(
		  ISearchFilterSpatialObj.OptionsBounds.useGeogExtent.toString());
   
  cswClientCriteria.setUseGeographicExtent(bDataIntersectsWithExtent || bDataWithinExtent);
  cswClientCriteria.setEnvelopeIntersects(bDataIntersectsWithExtent);
  cswClientCriteria.setEnvelopeContains(bDataWithinExtent);
  return cswClientCriteria;
}

/**
 * Marshall gpt to csw client criteria.
 * 
 * @param gptSearchCriteria the gpt search criteria
 * 
 * @return the csw search criteria
 */
public CswSearchCriteria marshallGptToCswClientCriteria(
    SearchCriteria gptSearchCriteria) {
  
  CswSearchCriteria cswClientCriteria = 
    marshallGptToCswClientCriteria2(gptSearchCriteria);
  
  
  try {
    if (getCswProfile().isSupportsContentTypeQuery()) {

      cswClientCriteria.setLiveDataAndMapsOnly(gptSearchCriteria
          .getSearchFilterContentTypes().getSelectedContentType()
          .equalsIgnoreCase(AimsContentTypes.liveData.toString()));
    }
  } catch (Exception e) {
    LOG.log(Level.WARNING, "error while setting livedata  value to csw client", 
        e);
  }
  
  return cswClientCriteria;
  
}


/**
 * Gets the url and the profile id.  
 * Url is set to the getCapabilities url. 
 * 
 * 
 * @throws SearchException
 */
private void initAttributes() throws SearchException {
  
  Map<String, String> attrib = this.getFactoryAttributes();
  if(attrib == null) {
    return;
  }
  String url = Val.chkStr(attrib.get("url"));
  if(!"".equals(url)) {
    this.setGetCapabiltiesUrl(url);
  }
  String profileIdLocal = Val.chkStr(attrib.get("profileId"));
  if(profileIdLocal != null && !"".equals(profileIdLocal)) {
    this.setProfileId(profileIdLocal);
  }
  else {
    profileIdLocal = Val.chkStr(attrib.get("cswProfileId"));
    if (profileIdLocal != null && !"".equals(profileIdLocal)) {
      this.setProfileId(profileIdLocal);
    }
  }
  
}


/**
 * Can search end point.  Interrogates the URL and maybe connects to the enpoint
 * to identify if this endpoint is searchable.
 * 
 * @param endPoint the end point
 * @param endPointData the end point data.  If available then endpoint may not
 * need to be consulted.
 * @param options the options
 * @return true, if the endpoint can be searched, false if the endpoint cannot
 * be searched.  Exception thrown when more info needed to make url searchable
 * @throws SearchPointOptionsException thrown when url can be searchable but
 * more options are needed to make it searchable
 *
@Override
public boolean canSearchEndPoint(String endPoint, String endPointData,
               SearchPointOptions options )
    throws SearchPointOptionsException {

  return false;
}*/

/** 
 * Gets metadata as search result record
 * 
 *@param uuid Uniquie id of the record
 *@return Search result record
 *@throws SearchException
 */
@Override
public SearchResultRecord getMetadataAsSearchResultRecord(String uuid)
    throws SearchException {
  CswRecord record = this.getMetadata(uuid);
  return this.marshallRecord(record);
}

/** 
 * 
 * Creates instances
 *@param rids
 *@return Map with engine
 * @throws SearchException (Currently does not throw an excpetion)
 */
@Override
public Map<String, Object> createInstances(StringSet rids) throws 
  SearchException {
  
  Map<String, Object> mapRid2Engine = new HashMap<String, Object>();
  for(String rid:rids) {
    ASearchEngine engine = new SearchEngineCSW();
    try {
      engine.setKey(rid);
      mapRid2Engine.put(rid, engine);
    } catch (SearchException e) {
      mapRid2Engine.put(rid,"Error while intializing id " + rid + " " 
          + e.getMessage());
      LOG.log(Level.WARNING,"Error while intializing id " + rid,e);
    }
    
  }
  return mapRid2Engine;
}


}
