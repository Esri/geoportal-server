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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.request.IRequest;
import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.CswRecord;
import java.util.Date;



/**
 * Defines operations to be carried out by the search
 * end point.
 */
public abstract class ASearchEngine 
   implements IRequest<SearchRequestDefinition> {
// class variables ==============================================================
/** Addtribute for connection timeout*. */
private static final String ATTRIBUTE_TIMEOUT_CONNECTION = "connectionTimeout";

/** Attribute for connection response timeout *. */
private static final String ATTRIBUTE_TIMEOUT_RESPONSE = "responseTimeout";

/** The Constant ATTRIBUTE_ABSTRACT_KEY. */
private static final String ATTRIBUTE_ABSTRACT_KEY    = "";

public static final String SEARCH_CREDENTIAL_MAP =  "SEARCH_CREDENTIAL_MAP";

/** class logger *. */
private static final Logger LOG = 
  Logger.getLogger(ASearchEngine.class.getCanonicalName());

// instance variables ==========================================================
/** The connection URI. */
private URI connectionUri;

/** The search time in milliseconds. */
private Long searchTimeInMilis = Long.MIN_VALUE;

/** The request definition. */
private SearchRequestDefinition requestDefinition;

/** The request context. */
private RequestContext requestContext;

/** The resource link builder. */
private ResourceLinkBuilder resourceLinkBuilder;

/** key that was used to find the searchengine. */
private String key;

/** Username Password  *. */
private UsernamePasswordCredentials credentials;

/** The factory attributes. */
private Map<String, String> factoryAttributes;

/** The message broker. */
private MessageBroker messageBroker;

/** The connection timeout. */
private int connectionTimeout;

/** The response timeout. */
private int responseTimeout;


/** The hits only. */
private boolean hitsOnly;



//Constructors ==================================================================
/** Default Constructor. SearchFactory can instantiate this constructor.*/
public ASearchEngine(){ }

/** 
 * Overloaded Constructor.  SearchFactory can instantiate this constructor.
 * @param context the request context
 */
public ASearchEngine(RequestContext context){
  this.requestContext = context;
}



// properties ==================================================================

/**
 * Gets the hits only.
 * 
 * @return the hits only
 */
public boolean getHitsOnly() {
  return hitsOnly;
}

/**
 * Sets the hits only.
 * 
 * @param hitsOnly the new hits only
 */
public void setHitsOnly(boolean hitsOnly) {
  this.hitsOnly = hitsOnly;
}

/**
 * Gets the resource link builder.
 *  
 * @return the resource link builder (never null)
 */
public ResourceLinkBuilder getResourceLinkBuilder() {
  return resourceLinkBuilder;
}

/**
 * Sets the resource link builder.
 * 
 * @param resourceLinkBuilder the new resource link builder
 */
public void setResourceLinkBuilder(ResourceLinkBuilder resourceLinkBuilder) {
  this.resourceLinkBuilder = resourceLinkBuilder;
}

/**
 * Gets the connection uri.  If connection uri is null, the URI in the
 * criteria will be used.
 * 
 * @return the connection uri (possibly null) 
 * @throws SearchException if uri from criteria is invalid
 */
public URI getConnectionUri() throws SearchException {
  if(connectionUri == null) {
    return this.getRequestDefinition().getCriteria().getSearchUri();
  }
  return connectionUri;
}

/**
 * Sets the connection uri. If connection uri is null, the URI in the
 * criteria will be used.
 * 
 * @param connectionUri the new connection uri
 */
public void setConnectionUri(URI connectionUri) {
  this.connectionUri = connectionUri;
}

/**
 * Gets the request context.
 * 
 * @return the request context (Could be null)
 */
public RequestContext getRequestContext() {
  return requestContext;
}

/**
 * Sets the request context.
 * 
 * @param context the new request context
 */
public void setRequestContext(RequestContext context) {
  this.requestContext = context;
}
 
/**
 * Gets the abstract text connected with the key.
 * 
 * @return the abstract hint
 * @throws SearchException the search exception
 */
public String getKeyAbstract() throws SearchException {
  return "";
}

/**
 * Gets the key associated with the engine.
 * 
 * @return the hint (trimmed, never null)
 */
public String getKey() {
  return Val.chkStr(key);
}

/**
 * Sets the key associated with the key.
 * 
 * @param key the key
 * 
 * @throws SearchException the search exception (If error)
 */
public void setKey(String key) throws SearchException{
  this.key = key;
  
}

/**
 * Gets the credentials.
 * 
 * @return the credentials
 */
public UsernamePasswordCredentials getCredentials() {
  if(credentials == null) {
    credentials = new UsernamePasswordCredentials();
	  Map<String, CredentialProvider> credMap = 
		  (Map<String, CredentialProvider>) 
		    this.getRequestContext().extractFromSession(
			  ASearchEngine.SEARCH_CREDENTIAL_MAP);
	  if(credMap != null) {
		  CredentialProvider credProvider = (CredentialProvider) 
		    credMap.get(this.getKey());
		  if(credProvider != null) {
			  credentials.setUsername(credProvider.getUsername());
			  credentials.setPassword(credProvider.getPassword());
		  }
	  }
    
  }
  return credentials;
}

/**
 * Sets the credentials.
 * 
 * @param credentials the new credentials
 */
public void setCredentials(UsernamePasswordCredentials credentials) {
  this.credentials = credentials;
}


/**
 * Gets the connection timeout in milliseconds.
 * 
 * @return the connection timeout
 */
public int getConnectionTimeoutMs() {
  return connectionTimeout;
}

/**
 * Sets the connection timeout in milliseconds.
 * 
 * @param connectionTimeout the new connection timeout
 */
public void setConnectionTimeoutMs(int connectionTimeout) {
  this.connectionTimeout = connectionTimeout;
}

/**
 * Gets the response timeout in milliseconds.
 * 
 * @return the response timeout
 */
public int getResponseTimeoutMs() {
  return responseTimeout;
}

/**
 * Sets the response timeout.
 * 
 * @param responseTimeout the new response timeout
 */
public void setResponseTimeout(int responseTimeout) {
  this.responseTimeout = responseTimeout;
}

// methods =====================================================================

/**
 * Do search.  Should update the requestDefinition.searchResult and 
 * requestDefinition.searchResult.pageCursor
 * 
 * @throws SearchException Exceptions from performing a search
 */
public abstract void doSearch()
throws SearchException;

/**
 * Gets record.
 * @param uuid UUID
 * @return record
 * @throws SearchException if searching fails
 */
public abstract ARecord getARecord(String uuid) throws SearchException;

/**
 * Gets the metadata as text.
 * 
 * @param uuid the uuid of the metadata
 * 
 * @return the metadata as text
 * 
 * @throws SearchException Exception from retrieving the document
 */
public abstract String getMetadataAsText(String uuid)
throws SearchException;

/**
 * Gets the metadata as object.
 * 
 * 
 * @param uuid the uuid of the metadata
 * 
 * @return the metadata as object
 * 
 * @throws SearchException the search exception
 */
public abstract SearchResultRecord getMetadataAsSearchResultRecord(String uuid)
throws SearchException;

/**
 * Gets the time in seconds.
 * 
 * @return the time in seconds
 */
public double getTimeInSeconds() {
  return this.getSearchTimeInMillis().doubleValue() / 1000.0;
  
}
/**
 * Gets the search time in millis.
 * 
 * @return the search time in millis
 */
public Long getSearchTimeInMillis() {
  return this.searchTimeInMilis;
}

/**
 * Sets the search time in millis.
 * 
 * @param time the new search time in millis
 */
protected void setSearchTimeInMillis(Long time) {
  this.searchTimeInMilis = time;
}

/**
 * Gets the search query as a string representation.  Should be the
 * native query translated from the criteria.
 * 
 * @return the search query
 * @throws SearchException the search exception
 */
public abstract String getSearchQuery() throws SearchException;


/**
 * Gets the request definition.
 * 
 * @return the request definition (possibly null)
 */
public SearchRequestDefinition getRequestDefinition() {
  return requestDefinition;
}

/**
 * Sets the request definition.
 * 
 * @param requestDefinition the new request definition
 */
public void setRequestDefinition(SearchRequestDefinition requestDefinition) {
  this.requestDefinition = requestDefinition;
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
public String getMetadataUrl(String uuid) throws SearchException  {
  return "";
}

/**
 * Inits the Object after you have set the properties.
 * 
 * Sets the attribute timeout connection and the attribute response timeout
 * 
 * @throws SearchExcpetion the search excpetion
 * @throws SearchException the search exception
 */
@SuppressWarnings("unchecked")
public void init() throws SearchException {
  
  Class c = this.getClass();
  Method methods[] = c.getDeclaredMethods();
  for(int i = 0; i < methods.length; i++) {
    Method method = methods[i];
    String setName = methods[i].getName();
    if(!setName.startsWith("set")) {
      continue;
    }
    if(setName.length() < 4) {
      continue;
    }
    String attributeName = setName.substring(3);
    Object obj = factoryAttributes.get(attributeName);
    if(obj == null) {
      continue;
    }
    Class paramClass[] = method.getParameterTypes();
    Exception ex = null;
    
    try {
      if(paramClass[0].isInstance(new String())) {
        method.invoke(this, new Object[]{(String) obj});
      }
      else if(paramClass[0].isInstance(new Integer(0))) {
        method.invoke(this, new Object[]{(Integer) obj});
      } else {
        method.invoke(this, new Object[]{obj});
      }
    } catch (IllegalArgumentException e) {
      ex = e;
    } catch (IllegalAccessException e) {
      ex = e;
    } catch (InvocationTargetException e) {
      ex = e;
    }
    if(ex != null) {
      throw new SearchException("Engine not intialized well with method " 
          + setName, ex);
    }
    
    
  }
  /*Map<String, String> factoryAttributes =  this.getFactoryAttributes();
  if(factoryAttributes == null) {
    return;  
  }
  Object obj = factoryAttributes.get(ATTRIBUTE_TIMEOUT_CONNECTION);
  if(obj != null) {
    this.setConnectionTimeout(Val.chkInt(obj.toString(), 0));
    LOG.finer("Connection timeout : " + obj.toString());
  }
  obj = factoryAttributes.get(ATTRIBUTE_TIMEOUT_RESPONSE);
  if(obj != null) {
    this.setResponseTimeout(Val.chkInt(obj.toString(), 0));
    LOG.finer("Connection timeout response : " + obj.toString());
  }*/
}

/**
 * Gets the factory attributes.
 * 
 * @return the factory attributes (possibly null if not set or if set to null) 
 */
public Map<String, String> getFactoryAttributes() {
   return factoryAttributes;
}

/**
 * Sets the factory attributes.
 * 
 * @param factoryAttributes the factory attributes
 */
public void setFactoryAttributes(Map<String, String> factoryAttributes) {
  this.factoryAttributes = factoryAttributes;
}

/**
 * Gets the message broker.
 * 
 * @return the message broker
 */
public MessageBroker getMessageBroker() {
  return messageBroker;
}

/**
 * Sets the message broker.
 * 
 * @param messageBroker the new message broker
 */
public void setMessageBroker(MessageBroker messageBroker) {
  this.messageBroker = messageBroker;
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
public abstract boolean canSearchEndPoint(String endPoint, String endPointData,
               SearchPointOptions options )
    throws SearchPointOptionsException;
*/

/**
 * Read is external search.
 * 
 * @return true, if successful
 */
protected abstract boolean readIsExternalSearch();

/**
 * Creates multiple instances of this search engine.  Created for use by
 * distributed search.  Some searchengines consult the DB on every
 * instance so if the searchengine knew beforehand that multiple searchengines
 * need information from the database in the same request,
 * then it is possible for it to generate 1 hit.
 * 
 * @param rids the rids
 * @return the map (value can be error string or ASearchEngine Object)
 * @throws SearchException the search exception
 */
public Map<String, Object> createInstances(StringSet rids) 
 throws SearchException{
  throw new SearchException("This engine does not create multiple instances");
}

/**
 * Check pagination.  Recalculates pagination in-case we come to a service
 * where max results are not returned.  Call this after search to straighten
 * out pagination
 * 
 * @throws SearchException the search exception
 */
protected void checkPagination() 
  throws SearchException {
  
  if(this.getHitsOnly() == true) {
    return;
  }
  SearchResult result = this.getRequestDefinition().getResult();
  SearchCriteria criteria = this.getRequestDefinition().getCriteria();
  ISearchFilterPagination criteriaCursor = criteria.getSearchFilterPageCursor();
  PageCursor resultsCursor = result.getPageCursor();
  
  resultsCursor.setRecordsPerPage(criteriaCursor.getRecordsPerPage());
  resultsCursor.setCurrentPage(criteriaCursor.getCurrentPage());

  if(result.getMaxQueryHits() >= result.getRecordSize()) {
    return;
  }
  if(result.getRecordSize() <= 0 && 
      criteria.getSearchFilterPageCursor().getCurrentPage() >= 1) {
    // TODO: When we are at a wall (records.getSize() == recordsPerPage) and
    //we send this request, maxQueryHits should be calculated without adding
    //1.  
    
    // Lets do a search for the prior page since we have reached the end
    criteria.getSearchFilterPageCursor().setCurrentPage(
        criteria.getSearchFilterPageCursor().getCurrentPage() - 1);
    this.doSearch();
  }
  
  int maxQueryHits = 0;
  if(result.getRecordSize() == 
    criteria.getSearchFilterPageCursor().getRecordsPerPage()) {
    maxQueryHits = 1 + 
      criteria.getSearchFilterPageCursor().getRecordsPerPage() *
      criteria.getSearchFilterPageCursor().getCurrentPage();
    result.setMaxQueryHits(maxQueryHits);
    criteria.getSearchFilterPageCursor().setTotalRecordCount(maxQueryHits);
  } else {
    maxQueryHits = (criteria.getSearchFilterPageCursor().getCurrentPage() - 1)
      * criteria.getSearchFilterPageCursor().getRecordsPerPage()
      + result.getRecordSize();
  }

  criteria.getSearchFilterPageCursor().setTotalRecordCount(maxQueryHits);
  result.getPageCursor().setTotalRecordCount(maxQueryHits);
  result.getPageCursor().setCurrentPage(
      criteria.getSearchFilterPageCursor().getCurrentPage());
 
  
  
}

/**
 * Fully reads the characters from the request input stream.
 * @param is input stream
 * @param sEncoding character encoding
 * @return the characters read
 * @throws IOException if an exception occurs
 */
protected String readInputCharacters(InputStream is, String sEncoding)
  throws IOException {
  StringBuffer sb = new StringBuffer();
  InputStreamReader ir = null;
  BufferedReader br = null;
  try {
    //if (request.getContentLength() > 0) {
      char cbuf[] = new char[2048];
      int n = 0;
      int nLen = cbuf.length;
      ir = new InputStreamReader(is,sEncoding);
      br = new BufferedReader(ir);
      while ((n = br.read(cbuf,0,nLen)) > 0) {
        sb.append(cbuf,0,n);
      }
    //}
  } finally {
    try {if (br != null) br.close();} catch (Exception ef) {}
    try {if (ir != null) ir.close();} catch (Exception ef) {}
    try {if (is != null) is.close();} catch (Exception ef) {}
  }
  return sb.toString();
}

/**
 * ASearchEngine record.
 */
public static interface ARecord {
  /**
   * Gets metadata as text.
   * @return metadata as text 
   */
  String getMetadataAsText();
  /**
   * Gets modified date.
   * @return modified date
   */
  Date getModifiedDate();
}
}



