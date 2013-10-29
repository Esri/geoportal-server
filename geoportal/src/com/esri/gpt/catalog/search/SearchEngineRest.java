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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.isodate.IsoDateFormat;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.search.SearchXslProfile;
import com.esri.gpt.framework.search.SearchXslRecord;
import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;
import java.text.ParseException;
import java.util.Date;

/**
 * The class SearchEngineCSW. Accesses a CSW repository to send searTch 
 * and parse back results.  works closely with the csw client jar.
 * 
 */
public class SearchEngineRest extends ASearchEngine {

// class variables =============================================================
/** The Class Logger. */
private static final Logger LOG = Logger.getLogger(
		SearchEngineRest.class.getCanonicalName());

/** The Constant DELIMETER key value pair */
public final static String DELIMETER_KVP = "\u2715";

/** The Constant DELIMETER_VALUES between key value*/
public final static String DELIMETER_KV = "\u2714";

/** Xsl Param opensearch URL **/
private static final String XSL_PARAM_OPENSEARCH_URL = "searchQueryUrl";

/** The Constant JSFBEAN_SEARCH_CONTROLLER. */
private static final String JSFBEAN_SEARCH_CONTROLLER = "SearchController";

// instance variables ==========================================================
/** The search xsl req res. */

/** The get record by id kvpurl. */
private String recordByIdKvpurl = null;

/** The profile id *. */
private String profileId = "";

/** The end point url. */
private String endPointSearchUrl;

/** The end point metadata url. */
private String endPointMetadataUrl;

/** The xsl profile. */
private SearchGptXslProfiles xslProfiles;

/** The default search text. */
private String defaultParamValues;

/** The replace url values. */
private String replaceUrlValues;



// constructor =================================================================

private SearchEngineRest() {
  
}
/**
 * Instantiates a new search engine rest.
 * 
 * @param context the context
 */
public SearchEngineRest(RequestContext context) {
    super(context);
}
// properties ==================================================================

/**
 * Sets the default param values.
 * 
 * @param defaultParamValues the new default param values
 */
public void setDefaultParamValues(String defaultParamValues) {
  this.defaultParamValues = defaultParamValues;
}

/**
 * Gets the end point url.
 * 
 * @return the end point url (trimmed, never null)
 */
public String getEndPointSearchUrl() {
  return Val.chkStr(endPointSearchUrl);
}

/**
 * Sets the end point url.
 * 
 * @param endPointUrl the new end point url
 */
public void setEndPointSearchUrl(String endPointUrl) {
  this.endPointSearchUrl = endPointUrl;
}


/**
 * Gets the profile id.
 * 
 * @return the profile id (trimmed, never null)
 */
public String getProfileId() {
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

/**
 * Gets the record by id kvpurl.
 * 
 * @return the record by id kvpurl (trimmed, never null)
 * @throws SearchException 
 */
public String getRecordByIdKvpurl() throws SearchException {
  if("".equals(Val.chkStr(recordByIdKvpurl))) {
    return Val.chkStr(this.readXslProfile().getKvp());
  }
  return Val.chkStr(recordByIdKvpurl);
}

/**
 * Sets the record by id kvpurl.
 * 
 * @param recordByIdKvpurl the new record by id kvpurl
 */
public void setRecordByIdKvpurl(String recordByIdKvpurl) {
  this.recordByIdKvpurl = recordByIdKvpurl;
}

/**
 * Sets the replace url values.
 * 
 * @param replaceUrlValues the new replace url values
 */
public void setReplaceUrlValues(String replaceUrlValues) {
  this.replaceUrlValues = replaceUrlValues;
}

/**
 * Gets the xsl profiles.
 * 
 * @return the xsl profiles
 * @throws SearchException the search exception
 */
public SearchGptXslProfiles getXslProfiles() throws SearchException {
  if(xslProfiles == null) {
    xslProfiles = SearchConfig.getConfiguredInstance().getGptXslProfiles();
  }
  return xslProfiles;
}


/**
 * Sets the xsl profiles.
 * 
 * @param xslProfiles the new xsl profiles
 */
public void setXslProfiles(SearchGptXslProfiles xslProfiles) {
  this.xslProfiles = xslProfiles;
}

/**
 * Gets the end point metadata url.
 * 
 * @return the end point metadata url (trimmed, never null)
 */
public String getEndPointMetadataUrl() {
  return Val.chkStr(endPointMetadataUrl);
}


/**
 * Sets the end point metadata url.
 * 
 * @param endPointMetadataUrl the new end point metadata url
 */
public void setEndPointMetadataUrl(String endPointMetadataUrl) {
  this.endPointMetadataUrl = endPointMetadataUrl;
}

/**
 * Gets the metadata url for record.
 * 
 * @param uuid the uuid
 * 
 * @return the metadata url
 * 
 * @throws SearchException the search exception
 */
@Override
public String getMetadataUrl(String uuid) throws SearchException  {
  return this.getRecordByIdKvpurl().replaceFirst("\\{id\\}", uuid);
}

/**
 * Get the connection uri
 * 
 * @return uri representing the search
 */
@Override
public URI getConnectionUri() throws SearchException {
  Exception ex = null;
  try {
    return new URL(this.getSearchQuery()).toURI();
  } catch (MalformedURLException e) {
    ex = e;
  } catch (SearchException e) {
    ex = e;
  } catch (URISyntaxException e) {
    ex = e;
  }
  throw new SearchException("Could not get the connection uri", ex);
  
}

// methods =====================================================================
/* 
 * Sets items that may have been handed from the factory (from gpt.xml)
 * <br/>
 * Assigns value of endPointUrl to setEndPointUrl
 * <br/>
 * Assigns profileId to setProfileId
 * 
 * Uses attributes connectionUrl, profileId, metadataUrl attributes for 
 * searchengine in gpt.xml
 * 
 *
 * @see com.esri.gpt.catalog.search.ASearchEngine#init()
 */
/*@Override
public void init() throws SearchException {
  super.init();
  this.class.
  Map<String, String> factoryAttributes =  this.getFactoryAttributes();
  if(factoryAttributes == null) {
    return;  
  }
  Object obj = factoryAttributes.get(ATTRIBUTE_CONNECTION_URL);
  if(obj != null) {
    this.setEndPointSearchUrl(obj.toString());
    LOG.finer("Init Connection url: " + obj.toString());
  }
  obj = factoryAttributes.get(ATTRIBUTE_PROFILE_ID);
  if(obj != null) {
    this.setProfileId(obj.toString());
    LOG.finer("Init Profile id: " + obj.toString());
  }
  obj = factoryAttributes.get(ATTRIBUTE_METADATA_URL);
  if(obj != null) {
    this.setEndPointMetadataUrl(obj.toString());
    LOG.finer("Init Metadata Profile id: " + obj.toString());
  }
  obj = factoryAttributes.get(ATTRIBUTE_DEFAULT_PARAMVALS);
  if(obj != null) {
    this.setDefaultParamValues(obj.toString());
    LOG.finer("Init Default Param values: " + obj.toString());
  }
}*/




/* 
 * Does a search
 * 
 * @param hitsOnly Only the hits
 * @see com.esri.gpt.catalog.search.ASearchEngine#doSearch()
 */
@Override
public void doSearch() throws SearchException {
  Exception ex = null;
  
  
  try {
    URI uri = this.getConnectionUri();
    URL url = uri.toURL();
    HttpClientRequest clientRequest = 
      HttpClientRequest.newRequest(HttpClientRequest.MethodName.GET, 
          url.toExternalForm());
    clientRequest.setConnectionTimeMs(getConnectionTimeoutMs());
    clientRequest.setResponseTimeOutMs(getResponseTimeoutMs());
    
    Map map = (Map) 
      this.getRequestContext().extractFromSession(SEARCH_CREDENTIAL_MAP);
    if(map != null) {
      CredentialProvider credProvider = (CredentialProvider) 
    	  map.get(this.getKey());
      if(credProvider  != null) {
        clientRequest.setCredentialProvider(credProvider);
      }
    }        
    
    clientRequest.execute();
    String response = clientRequest.readResponseAsCharacters();
    InputStream is = null;
    try {
      SearchXslProfile profile = this.readXslProfile();
      String js = Val.chkStr(profile.getResponsexslt());
      //String js = Val.chkStr(this.getFactoryAttributes().get("searchResponseJsT"));
      String xml = null;
      if(js.toLowerCase().endsWith(".js")) {
    	  try {
	    	  ResourcePath rPath = new ResourcePath();
	    	  URL fileUrl = rPath.makeUrl(js);
	    	  is = fileUrl.openStream();
	    	  String jsTransFile = IOUtils.toString(is, "UTF-8");
	    	  jsTransFile = "var jsGptInput =" + response + ";" + jsTransFile;
	    	  HttpServletRequest servletRequest = (HttpServletRequest)
	            this.getRequestContext().getServletRequest();
	    	  if(servletRequest != null) {
	    	    jsTransFile = "var jsGptQueryString = '" 
	    	    	+ servletRequest.getQueryString() + "';" + jsTransFile;	    		  
	    	  }
	    	  jsTransFile = "var jsGptEndpointSearchQuery = '" + 
	    	    url.toExternalForm() + "';" + jsTransFile;
	    	  ScriptEngineManager manager = new ScriptEngineManager();
	    	  ScriptEngine engine = manager.getEngineByName("JavaScript");
	    	  //manager.put("jsGptInput", response);
	          Object obj = engine.eval(jsTransFile);
	          xml = obj.toString();
	    	  parseResponse(xml);// has to work before the finally. dont move
    	  } catch (Exception e) {
    	    throw new SearchException(e.getMessage() + ":" + 
    	    		"Error when doing transformation from javascript", e);
    	  } 
      } else {
        xml = XmlIoUtil.jsonToXml(response, "gptJsonXml");
        parseResponse(xml);
      }
      
      checkPagination();
    } catch(SearchException e) { 
      throw e;
    } catch (Exception e) {
      parseResponse(response);
      checkPagination();
    } finally {
    	if(is != null) {
    		  IOUtils.closeQuietly(is);
    		}
    }
       
  } catch (MalformedURLException e) {
    ex = e;
  } catch (IOException e) {
    ex = e;
  } finally {
	
  }
  if(ex != null) {
    throw new SearchException(ex.getMessage() + ": Could not perform search",
    		ex);
  }
}

/**
 * Parses the response.
 * 
 * @param xml the xml
 * @throws SearchException 
 */
@SuppressWarnings("unchecked")
public void parseResponse(String xml) throws SearchException {
  
  SearchResult searchResult = new SearchResult();
  SearchResultRecords searchResultRecords = 
    this.getRequestDefinition().getResult().getRecords();
  try {
    this.readXslProfile().readGetRecordsResponse(xml, searchResult);
    this.getRequestDefinition().getResult().setMaxQueryHits(
        searchResult.getMaxQueryHits());
    SearchResultRecords records = searchResult.getRecords();
    
    HttpServletRequest servletRequest = (HttpServletRequest)
      this.getRequestContext().getServletRequest();
	if (servletRequest != null) {
		String queryString = Val
				.chkStr(servletRequest.getQueryString());
		if (records.size() > searchResult.getPageCursor()
						.getRecordsPerPage()) {
			// must be one of those endpoints that do not have a number
			
		}
	}
    Iterator iter = records.iterator();
    while(iter.hasNext()) {
      
      Object obj = iter.next();
      
      if(obj instanceof SearchResultRecord) {
        SearchResultRecord searchResultRecord =(SearchResultRecord) obj;
        searchResultRecord.setExternal(this.readIsExternalSearch());  
        searchResultRecord.setExternalId(this.getKey());
        searchResultRecords.add(searchResultRecord);
      } else if (obj instanceof SearchXslRecord) {
        SearchResultRecord searchResultRecord = 
          ((SearchXslRecord)obj).readAsSearchResult(
              this.getResourceLinkBuilder(), 
              this.readIsExternalSearch(),
              this.getKey()
            );
        searchResultRecords.add(searchResultRecord);
      } else {
        throw new SearchException("Did not understand the parsed object " +
            obj.getClass().getCanonicalName());
      }
    }
  } catch (Exception e) {
    throw new SearchException(e.getMessage() + ":" + 
        "Could not parse getRecordsResponse", e);
  }
  
}

private SearchXslRecord getMetadataAsObjectX(String uuid) throws SearchException {
  SearchResultRecord record = new SearchResultRecord();
  String metaUrl = this.getMetadataUrl(uuid);
  SearchXslRecord recordX = null;

  URL url = null;
  Exception ex = null;
  try {
    url = new URL(metaUrl);

    HttpClientRequest clientRequest = HttpClientRequest.newRequest(
        HttpClientRequest.MethodName.GET, url.toExternalForm());
    clientRequest.setConnectionTimeMs(getConnectionTimeoutMs());
    clientRequest.setResponseTimeOutMs(getResponseTimeoutMs());
    clientRequest.execute();
    String response = clientRequest.readResponseAsCharacters();
    LOG.log(Level.FINER, "Response from get Metadata url = {0}\n response = \n{1}", new Object[]{url.toExternalForm(), response});
    recordX = new SearchXslRecord();
    this.readXslProfile().readGetMetadataByIDResponse(response, recordX);
  } catch (MalformedURLException e) {
    ex = e;
  } catch (IOException e) {
    ex = e;
  } catch(TransformerException e) {
    ex = e;
  }
  if(ex != null) {
    throw new SearchException("Could not get metadata id url = " + url, ex);
  }
  return recordX;
}

/**
 * Gets the metadata as object.
 * 
 * @param uuid the uuid
 * @return the metadata as object
 * @throws SearchException the search exception
 * @see com.esri.gpt.catalog.search.ASearchEngine#getMetadataAsSearchResultRecord(java.lang.String)
 */
@Override
public SearchResultRecord getMetadataAsSearchResultRecord(String uuid) throws 
 SearchException {
  return this.getMetadataAsObjectX(uuid)
    .readAsSearchResult(this.getResourceLinkBuilder(),  
        this.readIsExternalSearch(),
        this.getKey());
}

@Override
public ARecord getARecord(String uuid) throws SearchException {
  final SearchXslRecord record = getMetadataAsObjectX(uuid);
  ARecord aRecord = new ARecord() {
    @Override
    public String getMetadataAsText() {
      return record.getFullMetadata();
    }

    @Override
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
 * @param uuid the uuid
 * @return the metadata as text
 * @throws SearchException the search exception
 * @see com.esri.gpt.catalog.search.ASearchEngine#getMetadataAsText(java.lang.String)
 */
@Override
public String getMetadataAsText(String uuid) throws SearchException {
  this.getMetadataAsObjectX(uuid).getFullMetadata();
  return null;
}

/**
 * Gets the default param values as map.
 * 
 * @return the default param values (never null)
 */
public Map<String, String> readDefaultParamValues() {
  return readParamValues(Val.chkStr(this.defaultParamValues));
}

/**
 * Read replace param values as map.
 * 
 * @return the map
 */
public Map<String, String> readReplaceParamValues() {
  return readParamValues(Val.chkStr(this.replaceUrlValues));
}

/**
 * Read param values as maps.
 * 
 * @param param the param
 * @return the map
 */
private Map<String, String>readParamValues(String param) {
  Map<String, String> defVals = new HashMap<String, String>();
  String vals = Val.chkStr(defaultParamValues);
  String kvp[] = vals.split(DELIMETER_KVP);
  for (int i = 0; kvp != null && i < kvp.length; i++) {
    String kv[] = (kvp[i]).split(DELIMETER_KV);
    if(kv == null) {
      continue;
    }
    String key = "";
    String value = "";
    if(kv.length < 1) {
      continue;
    }
    key = Val.chkStr(kv[0]);
    if("".equals(kv)) {
      continue;
    }
    if(kv.length > 1) {
      value = Val.chkStr(kv[1]);
    }
    defVals.put(key, value);    
  }
  return defVals;
}
/**
 * Read xsl profile.
 * 
 * @return the search xsl profile
 * @throws SearchException the search exception
 */
@SuppressWarnings("unchecked")
public SearchXslProfile readXslProfile() throws SearchException {
  return  this.getXslProfiles().getProfileById(this.getProfileId());
  
}

/**
 * Gets the search query.
 * 
 * @return the search query
 * @throws SearchException the search exception
 */
@SuppressWarnings("unchecked")
@Override
public String getSearchQuery() throws SearchException {
  String searchQuery = null;
  Exception ex = null;
  Map<String, String> map = this.getFactoryAttributes();
  map.put(XSL_PARAM_OPENSEARCH_URL,this.getEndPointSearchUrl());
 
    
  try {
    searchQuery = this.readXslProfile().generateGetRecordsRequest(this
        .getRequestDefinition().getCriteria(), map, this.getHitsOnly());
    String searchEndPoint = this.getEndPointSearchUrl();
    // assign values from the xslt
    Document doc = DomUtil.makeDomFromString(searchQuery, false);
    NodeList paramList = doc.getElementsByTagName("parameter");
    for(int i = 0; i < paramList.getLength(); i++ ) {
      Node paramNode = paramList.item(i);
      String key = paramNode.getAttributes().getNamedItem("key")
        .getNodeValue();
      String value = paramNode.getAttributes().getNamedItem("value")
        .getNodeValue();
      
      String paramValue = this.getFactoryAttributes().get(key);
      if (paramValue != null) {
        String paramValues[] = paramValue.split(DELIMETER_KVP);
        for (int j = 0; j < paramValues.length; j++) {
          String paramValueValues[] = Val.chkStr(paramValues[j]).split(
              DELIMETER_KV);
          if (paramValueValues.length < 2) {
            continue;
          }
          if (Val.chkStr(paramValueValues[0]).equalsIgnoreCase(value)) {
            value = paramValueValues[1];
          }
        }
      }
      key = key.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1");
      searchEndPoint = searchEndPoint.replaceAll("(?i)" +  key, 
          URLEncoder.encode(value, "UTF-8"));
    }
    
    // assign default values input
    Map<String, String> paramVals = this.readDefaultParamValues();
    Iterator<String> keyIter =  paramVals.keySet().iterator();
    while(keyIter.hasNext()) {
      String key = keyIter.next();
      if(searchEndPoint.contains(key + "=&") || searchEndPoint.endsWith(key + "=")) {
        searchEndPoint = searchEndPoint.replaceAll(key + "=", 
            key + "=" + URLEncoder.encode(paramVals.get(key), "UTF-8"));
      }
    }
    
    // replace sections of the url
    paramVals = this.readReplaceParamValues();
    keyIter =  paramVals.keySet().iterator();
    while(keyIter.hasNext()) {
      String key = Val.chkStr(keyIter.next());
      String value = Val.chkStr(paramVals.get(key));
      searchEndPoint.replaceAll(key, value);
    }
    
    LOG.log(Level.FINER, "Search Query: {0}", searchEndPoint);
    return searchEndPoint;
  } catch (XPathExpressionException e) {
    ex = e;
  } catch (TransformerException e) {
    ex = e;
  } catch (ParserConfigurationException e) {
    ex = e;
  } catch (SAXException e) {
    ex = e;
  } catch (IOException e) {
    ex = e;
  } 
  if (ex == null) {
    throw new SearchException("Error when generating search query", ex);
  }

  return searchQuery;

}

/**
 * Read is external search.
 * 
 * @return true, if successful
 * @see com.esri.gpt.catalog.search.ASearchEngine#readIsExternalSearch()
 */
@Override
protected boolean readIsExternalSearch() {
  return true;
}

/** 
 * 
 * Creates instances of this search engines from the rids
 *@param rids
 *@return mapping between rids and search engines instances
 *@throws SearchException
 */
@Override
public Map<String, Object> createInstances(StringSet rids) 
throws SearchException{
 
  Map<String, Object> mapRid2Engine = new HashMap<String, Object>();
  for(String rid: rids) {
    SearchEngineRest searchEngineRest = new SearchEngineRest();
    searchEngineRest.setKey(rid);
    mapRid2Engine.put(rid, searchEngineRest);
  }
  return mapRid2Engine;
}

/**
 * Gets the abstract associated with the key
 * 
 * @return the abstract
 * @throws SearchException
 */
@Override
public String getKeyAbstract() throws SearchException {
  
  Map<String, String> map = this.getFactoryAttributes();
  String absKey = null;
  if(map != null) {
     absKey = map.get("abstractResourceKey");
  } 
 
  MessageBroker bundle = new MessageBroker();
  bundle.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
  return bundle.retrieveMessage(absKey);
}

}
