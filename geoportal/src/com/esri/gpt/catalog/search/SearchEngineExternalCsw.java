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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolCsw;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.control.rest.repositories.CswRepository;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.CswCatalogCapabilities;
import com.esri.gpt.server.csw.client.CswClient;
import com.esri.gpt.server.csw.client.CswProfile;
import com.esri.gpt.server.csw.client.CswRecord;
import com.esri.gpt.server.csw.client.CswRecords;
import com.esri.gpt.server.csw.client.CswSearchRequest;
import com.esri.gpt.server.csw.client.InvalidOperationException;
import com.esri.gpt.server.csw.client.NullReferenceException;

/**
 *  
 * The Class SearchEngineExternalCsw.  Request based.  For searching
 * Harvest Site CSWs in our repositories.  Parameters that can be used
 * are is "uuid" of harvest site
 */

public class SearchEngineExternalCsw 
  extends SearchEngineCSW  {

// class variables =============================================================
/** The class LOG. */
private static final Logger LOG = Logger.getLogger(
    SearchEngineExternalCsw.class.getCanonicalName());



// instance variables ==========================================================

/** Tracks The former id we inited on. */
private String formerId = "";

/** The harvest record **/
private HrRecord hrRecord = null;

private boolean hasBeenInited = false;

// constructors ================================================================
/**
 * 
 * Instantiates a new search engine external csw.
 */
public SearchEngineExternalCsw(){}

// properties ==================================================================

/**
 * Gets the former hint.
 * 
 * @return the former hint
 */
public String getFormerId() {
  return formerId;
}

/**
 * Sets the former hint.
 * 
 * @param formerHint the new former hint
 */
public void setFormerId(String formerHint) {
  this.formerId = formerHint;
}

/**
 * Gets the hr record.
 * 
 * @return the hr record (possibly null)
 */
public HrRecord getHrRecord() {
   return hrRecord;
}

/**
 * Sets the hr record.
 * 
 * @param hrRecord the new hr record
 */
public void setHrRecord(HrRecord hrRecord) {
  this.hrRecord = hrRecord;
}
// methods =====================================================================
/**
 * Tells us if the search is external to GPT9 or Internal
 * @return true
 */
@Override
protected boolean readIsExternalSearch() {
  return true;
}



/**
 * Sends a CSW GetRecords request to CSW service.
 * @param cswRequest the CSW XML request
 * @return the resultant records
 * @throws SearchException the search exception
 */
@Override
protected CswRecords sendRequest(String cswRequest) throws SearchException {
  
  if(hasBeenInited == false) {
    hasBeenInited = true;
    init(false);
  }
  InputStream cswInput = null;
  try {
    
    String username = Val.chkStr(this.getCredentials().getUsername());
    String password = Val.chkStr(this.getCredentials().getPassword());
    if("".equals(username)) {
      return super.sendRequest(cswRequest);
    }
   
    // submit the request
    String url = this.getGetRecordsUrl();
    try {
      cswInput = this.getCswClient().submitHttpRequest(
           "POST", url,cswRequest,username,password);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Exception when Posting CSW query to "+url,e);
      if (e instanceof java.net.SocketTimeoutException) {
        throw new SearchException("catalog.search.error.searchTimeOut",e, 
          new String[] {String.valueOf(SearchConfig.getConfiguredInstance()
              .getTimeOut()/1000)});
      }
      throw new SearchException("ERROR while searching authenticating end point " +
        url + " : "  +e.getMessage()
          ,e);
    } 
    
    // parse the response
    try {
      return this.parseResponse(this.readInputCharacters(cswInput, "UTF-8"));
    } catch (IOException e) {
      throw new SearchException("Could not get response for send request", e);
    }
    
  } finally {    
    try {if (cswInput != null) cswInput.close();} catch (Exception ef) {}
  }
}

/**
 * Marshalls csw records
 * @param cswRecords CSW records
 * @throws SearchException
 */
@Override
protected void marshallRecords(final CswRecords cswRecords)
    throws SearchException {

  CswProfile profile = this.getCswProfile();
  SearchResult result = this.getRequestDefinition().getResult();
  //result.setSupportsContentTypeQuery(profile.isSupportsContentTypeQuery());
  //result.setSupportsSpatialDisplay(profile.isSupportsSpatialBoundary());

  super.marshallRecords(cswRecords);
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
@Override
protected CswRecord getMetadata(String uuid) throws SearchException {
  CswRecord record = new CswRecord();
  Exception exc = null;
  try {
    CswProfile profile = getCswProfile();
    CswSearchRequest request = new CswSearchRequest();
    CswClient client = this.getCswClient();
    //client.setReadTimeout(SearchConfig.getConfiguredInstance().getTimeOut());
    request.setCswClient(client);
    String username = Val.chkStr(this.getCredentials().getUsername());
    String password = Val.chkStr(this.getCredentials().getPassword());
    if("".equals(username)) {
      record = request.getRecordById(this.getGetMetadataRecordUrl(), 
        uuid, profile);
    } else  {
      record = request.getRecordById(this.getGetMetadataRecordUrl(), 
          uuid, profile, username, password);
    }
    
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
 * Initialization 
 * 
 *@throws SearchException
 */
@Override
public void init() throws SearchException {
  init(true);
}
/**
 * Looks for the id in the repository then fills in 
 * other information e.g. profile, capabilities url, post url
 * @throws SearchException
 */

private void init(boolean calledByFactory) throws SearchException {
  
  HrRecord record = this.getHrRecord();
  if(record != null && calledByFactory == true) {
    if(record.getProtocol() instanceof HarvestProtocolCsw) {
      HarvestProtocolCsw hpCsw = (HarvestProtocolCsw) record.getProtocol();
      this.setProfileId(hpCsw.getProfile());
    }
    
    return;
  }
  
  boolean doCapabilities = false;
  
  // get uuid for this search
  String uuid = "";
  Map<String, String> map = super.getFactoryAttributes();
  if(map != null) {
    uuid = Val.chkStr(super.getFactoryAttributes().get("uuid"));
  }
  if("".equals(uuid)) {
    uuid = this.getKey();
  }
  
  try {

    // Extracting getRecordByID and GetRecordsUrl associated with uuid in
    // session
    RequestContext reqContext = this.getRequestContext();
    if(reqContext == null) {
      throw new NullPointerException("RequestContext in search engine is null");  
    }
    String sessionKeyPrfx = this.getClass().getCanonicalName() + ":uuid:"
        + uuid;
    String sessionGetMetadataKey = sessionKeyPrfx + ":GetMetadataRecord:url";
    String sessionGetRecordsKey = sessionKeyPrfx + ":GetRecords:url";
    String sessionProfileId = sessionKeyPrfx + ":profileId";
    String sessionCapabUrl = sessionKeyPrfx + ":capabilities:url";
    String sessionCapObject = sessionKeyPrfx + ":capabilities:object";
    
    Object objCapObject = reqContext.extractFromSession(sessionCapObject);
    if(objCapObject instanceof CswCatalogCapabilities) {
      this.setCapabilities((CswCatalogCapabilities)objCapObject);
    }
    Object objGetMetadataUrl = reqContext.extractFromSession(
        sessionGetMetadataKey);
    Object objGetRecordsUrl = reqContext.extractFromSession(
        sessionGetRecordsKey);
    Object objProfileId = reqContext.extractFromSession(
        sessionProfileId);
    Object objGetCapabUrl = reqContext.extractFromSession(
        sessionCapabUrl); 
    
    if (objGetMetadataUrl == null 
        || "".equals(objGetMetadataUrl.toString().trim())
        || objGetRecordsUrl == null
        || "".equals(objGetRecordsUrl.toString().trim())
        || objProfileId == null
        || "".equals(objProfileId.toString().trim())
        || objGetCapabUrl == null
        || "".equals(objGetCapabUrl.toString().trim())) {
      doCapabilities = true;
    } else {
      this.setGetRecordsUrl(objGetRecordsUrl.toString());
      this.setGetMetadataRecordUrl(objGetMetadataUrl.toString());
      this.setProfileId(objProfileId.toString());
      this.setGetCapabiltiesUrl(objGetCapabUrl.toString());
    }
    
    // Checking the db to check if profile and url are still the same
    GptRepository repos = new GptRepository();
    
    try {
      if (record == null) {

        record = repos.readHarvestRecord(uuid, this.getRequestContext());

      }
      Protocol harvestProtocol = record.getProtocol();
      if (harvestProtocol instanceof HarvestProtocolCsw) {
        HarvestProtocolCsw harvestProtocolCsw = (HarvestProtocolCsw) harvestProtocol;
        if (!this.getProfileId().equals(harvestProtocolCsw.getProfile())
            || !this.getGetCapabiltiesUrl().equals(record.getHostUrl())) {
          doCapabilities = true;
          this.setProfileId(harvestProtocolCsw.getProfile());
          this.setGetCapabiltiesUrl(record.getHostUrl());
        }

      } else {
        throw new SearchException("repository id " + uuid + " is not expected"
            + " CSW protocol");
      }
    } catch (SearchException e) {
      // T.M. accomodates gpt.xml values
      String url = Val.chkStr(this.getFactoryAttributes().get("url"));
      String profileId = Val.chkStr(this.getFactoryAttributes()
          .get("profileid"));
      if(!"".equals(url) && !"".equals(profileId)) {
        doCapabilities = true;
        this.setProfileId(profileId);
        this.setGetCapabiltiesUrl(url);
      } else {
        throw e;
      }
    }
    // Do the capabilities if requested
    if (doCapabilities) {
      super.init();
      // Add info to session
      reqContext.addToSession(sessionGetMetadataKey, 
          this.getGetMetadataRecordUrl());
      reqContext.addToSession(sessionGetRecordsKey, this.getGetRecordsUrl());
      reqContext.addToSession(sessionCapabUrl, this.getGetCapabiltiesUrl());
      reqContext.addToSession(sessionProfileId, this.getProfileId());
      reqContext.addToSession(sessionCapObject, this.getCapabilities());
    }
  } catch (Throwable e) {
    throw new SearchException("Error while getting items from repository "
        + " with id of " + uuid + e.getMessage(), e);
  }
}

/**
 * Gets the abstract associated with the key
 * 
 * @return the abstract
 * @throws SearchException
 */
@Override
public String getKeyAbstract() throws SearchException {

  CswCatalogCapabilities cap = this.getCapabilities();
  if(cap != null) {
    return Val.chkStr(cap.getTitle()) + " : " 
      + Val.chkStr(cap.getAbstractText());
  }
  String abs = null;
  String uuid = this.getKey();
  try {
    GptRepository repos = new GptRepository();
    HrRecord record = this.getHrRecord();
    if(record == null) {
      record = repos.readHarvestRecord(uuid, this.getRequestContext());
    }
    String url = record.getHostUrl();
    CswRepository csw = new CswRepository();

    if (url.length() > 0) {
      abs = csw.transformToHtml(url);
    }
  } catch (Exception e) {
    throw new SearchException("Could not get abstract for uuid =" + uuid, e);
  }
  return Val.chkStr(abs);

}

/**
 * Creates the instances.
 * 
 * @param rids the rids
 * @return the array list
 * @throws SearchException 
 * @throws SearchException the search exception
 */
@Override
public Map<String, Object> createInstances(StringSet rids) throws SearchException {
  Map<String, Object> mRidEngine = 
    new HashMap<String, Object>();
  GptRepository repos = new GptRepository();
  Map<String, HrRecord> repositoryRecord = 
    repos.readHarvestRecords(rids, this.getRequestContext()); 
  
  // Check if any rids are missing
  String msgErr = null;
  for(String rid:rids) {
    if(repositoryRecord.containsKey(rid)) {
      continue;
    }
    ASearchEngine engine = SearchEngineFactory.createSearchEngine(
       new SearchCriteria(), 
       new SearchResult(),
       this.getRequestContext(), rid, this.getMessageBroker());
      
    if(engine != null) {
      mRidEngine.put(rid, engine);
      continue;
    }
    if(msgErr == null) {
      msgErr = this.getMessageBroker().retrieveMessage(
          "catalog.search.distributedSearch.ridDbEntryNotFound");
    
    }
    msgErr = msgErr.replaceAll("\\{0\\}", rid);
    mRidEngine.put(rid, msgErr);
  
  }
  
  // Create search engines for existing rids
  Iterator<Entry<String, HrRecord>> iter = repositoryRecord.entrySet().iterator();
  while(iter.hasNext()) {
    Entry<String, HrRecord> entry = iter.next();
    String rid = entry.getKey();
    HrRecord hrRecord = entry.getValue();
    SearchEngineExternalCsw sEngine = new SearchEngineExternalCsw();
    sEngine.setKey(rid);
    sEngine.setHrRecord(hrRecord);
    mRidEngine.put(rid, sEngine);
  }

  return mRidEngine;
 
}




}
