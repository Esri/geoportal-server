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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.collection.StringSet;


/**
 * A factory for creating Search Data Access Objects.  Factory caches
 * the engines in session. 
 * 
 */
public abstract class SearchEngineFactory extends ASearchEngine {

// class variables =============================================================

/** class logger **/
private static final Logger LOG =
  Logger.getLogger(SearchEngineFactory.class.getCanonicalName());

// constructor =================================================================
/**
 * Instantiates a new search DAO factory. 
 */
protected SearchEngineFactory() {
  super();
} 

// methods =====================================================================

/**
 * Creates a new SearchDAO object.  This will return default searchEngine.
 * 
 * @param criteria the criteria  to be used by the search
 * @param result the result reference where the results will be stored after \
 * search.
 * @param context Used for connection information.  Can be null if you intend
 * to use CSW only.
 * @param messageBroker the message broker
 * 
 * @return the search Data Access
 * 
 * @throws SearchException when URI to search cannot be found
 */
public static ASearchEngine createSearchEngine(
                                         final SearchCriteria criteria, 
                                         final SearchResult result,
                                         final RequestContext context,
                                         final MessageBroker messageBroker) 
  throws SearchException {

  return createSearchEngine(
      criteria, 
      result,
      context,
      SearchEngineLocal.ID,
      messageBroker,
      null,
      null);
  
}

/**
 * Creates a new SearchEngine object.
 * 
 * @param criteria the criteria (should never be null)
 * @param result the result (should never be null)
 * @param context the context (should never be null)
 * @param key the key to find the corresponding search engine(should never be null)
 * @param messageBroker the message broker
 * 
 * @return search engine
 * 
 * @throws SearchException the search exception
 * 
 * @see #createSearchEngine(SearchCriteria, SearchResult, RequestContext, String, MessageBroker, String, String)
 */
public static ASearchEngine createSearchEngine(
    final SearchCriteria criteria, 
    final SearchResult result,
    final RequestContext context,
    String key,
    MessageBroker messageBroker
 ) throws SearchException {
  return createSearchEngine(
      criteria, 
      result,
      context,
      key,
      messageBroker,
      null,
      null);
}

/**
 * Creates a new Search 
 * 
 * @param criteria the criteria (should never be null)
 * @param result the result (should never be null)
 * @param context the context (should never be null)
 * @param key the key to find the corresponding search engine(should never be null) (Should be a string to be used with regex on the {@link SearchConfig#getSearchFactoryRepos()} key
 * @param username the username
 * @param password the password
 * 
 * @return the a search engine (never null, default search engine returned if no engine
 * is found corresponding to key
 * 
 *   
 * @throws SearchException Thrown if any parameters are null and on other faults
 */
public static ASearchEngine createSearchEngine(
    final SearchCriteria criteria, 
    final SearchResult result,
    final RequestContext context,
    final String key,
    final MessageBroker messageBroker,
    final String username,
    final String password) throws SearchException {
  String sKey = Val.chkStr(key);
  if(result == null) {
    throw new SearchException
      ("Result variable give in Search Engine Factory is null");
  }
  if(criteria == null) {
    throw new SearchException
      ("Criteria variable give in Search Engine Factory is null");
  }
  if(context == null) {
    throw new SearchException
      ("Context variable give in Search Engine Factory is null");
  }
  
  ASearchEngine sEngine = null;
  String value = null;

  try {

    
    sEngine = identifyEngine(sKey, context);

  } catch (Throwable e) {
    LOG.log(Level.WARNING, "Search Engine not found for key value {0}. Resorting to default search engine : {1}", new Object[]{sKey, e.getMessage()});
  }
  
  if(sEngine == null) {
    // For backward compatibility (fail safe) so that searches without 
    // searchFactory methods in gpt.xml work
    sEngine = new SearchEngineLocal(context);
  }
  
  intializeEngine(sEngine,criteria, result, context, sKey, messageBroker, 
      username, password);
  return sEngine;
}

private static void intializeEngine (
    final ASearchEngine sEngine,
    final SearchCriteria criteria, 
    final SearchResult result,
    final RequestContext context,
    final String sKey,
    final MessageBroker messageBroker,
    final String username,
    final String password
    ) throws SearchException {
  SearchRequestDefinition sDef = new SearchRequestDefinition(criteria, result);
  sEngine.setRequestDefinition(sDef);
  sEngine.setRequestContext(context);
  sEngine.setKey(sKey);
  Map<String, String> attribs = getAttributesOfKey(sKey, context);
  sEngine.setFactoryAttributes(attribs);
  
  HttpServletRequest hReq = null; 
  context.getServletRequest();
  if( context.getServletRequest() instanceof HttpServletRequest) {
    hReq = (HttpServletRequest) context.getServletRequest();
  }
  
  sEngine.setMessageBroker(messageBroker);
  sEngine.setResourceLinkBuilder(
       ResourceLinkBuilder.newBuilder(context, hReq, messageBroker));
  
  if(username != null && "".equals(username.trim())) {
    UsernamePasswordCredentials cred = new UsernamePasswordCredentials();
    cred.setUsername(username);
    cred.setPassword(password);
    sEngine.setCredentials(cred);
  }
  sEngine.init();
}

private static ASearchEngine identifyEngine(String sKey, RequestContext context) 
throws SearchException {
  ASearchEngine sEngine = null;
  try {

    Map<String, String> attribs = getAttributesOfKey(sKey, context);
    String value = attribs.get("CLASS");
    sEngine = getSearchEngine(value, sKey, context);
    

  } catch (Throwable e) {
    LOG.log(Level.WARNING, "Search Engine not found for key value {0}. Resorting to default search engine : {1}", new Object[]{sKey, e.getMessage()});
    throw new SearchException("Could not find engine");
    
  }
  return sEngine;
}

/**
 * Creates a new SearchEngine object.
 * 
 * @param endPointUrl the end point url
 * @param endPointData the end point data
 * @param options the options
 * @return the a search engine
 */
public ASearchEngine createSearchEngine(String endPointUrl, String endPointData,
    SearchPointOptions options) {
  Map<String, Map<String, String>> factMap = 
    SearchConfig.getConfiguredInstance().getSearchFactoryRepos();
  
  

  return null;
}

/**
 * Gets the search engine.  Sto/res it in the session incase
 * it is encountered again.
 * 
 * @param className the class name
 * @param key the key
 * @param context the context
 * 
 * @return the search engine (never null)
 * 
 * @throws SearchException if class cannot be constructed
 */
@SuppressWarnings("unchecked")
public static ASearchEngine getSearchEngine(String className,
    String key, RequestContext context) throws SearchException {
  Object obj = null;
  ASearchEngine sEngine = null;
  String value = className;
  
  try {
   
    Class cls = Class.forName(value);
    obj =  cls.newInstance();
    
  } catch (Throwable e) {
    
    try {
      Class cls = Class.forName(value);
      Class[] typesList = {RequestContext.class};
      Constructor ctr = cls.getConstructor(typesList);
      Object[] initargs = {context};
      obj = ctr.newInstance(initargs);
    
    } catch(Throwable f) {
      
      throw new SearchException("Could not locate SearchEngine Obj for key = " 
        + key +" : " + e.getMessage() + " : " + f.getMessage(),
        e);
    }
    
  }
  
  if(!(obj instanceof ASearchEngine)) {
    throw new SearchException("Object from searchfactory " 
        + obj.getClass().getCanonicalName() + " is not an instance of " + 
        ASearchEngine.class.getCanonicalName());
  }
  sEngine = (ASearchEngine) obj;
  
  return sEngine;
}

/**
 * Gets the value of key.
 * 
 * @param key the key to be used
 * @param context the context
 * 
 * @return attributes of the key (never null)
 * 
 * @throws SearchException if error or cannot find a value
 */
private static Map<String, String> getAttributesOfKey(String key, 
    RequestContext context) 
 throws SearchException {
  
  Map<String, String> attributes = null;
  GptRepository repository = new GptRepository();
  String error = "";
  
  // use the url from the db feed it to the configuration 
  
  
  // use the key itself to feed in to the configuration

  try {
    attributes = getAttributes(key, context);
  } catch (Exception e) {
    error += ": Did not get key =  " + key + " in config file "
        + e.getMessage();

  }

  if (attributes == null) {
    try {
      HrRecord record = repository.readHarvestRecord(key, context);
      String url = record.getHostUrl();
      attributes = getAttributes(url, context);
    } catch (Exception e) {
      error = "Did not get key = " + key + " in repository " + e.getMessage();
     
    }
  }
  
  
  if(attributes == null) {
    throw new SearchException(error);
  }

  return attributes;
  
}

/**
 * Gets the value of key from config (currently sourced from gpt.xml).
 * 
 * @param key the key
 * @param context the context
 * 
 * @return the value of key from config
 * 
 * @throws SearchException the search exception
 */
private static Map<String, String> getAttributes(String key, 
    RequestContext context) throws 
  SearchException {
  
  Map<String, Map<String, String>> factMap = 
    context.getApplicationConfiguration()
      .getCatalogConfiguration().getSearchConfig().getSearchFactoryRepos();
  Set<String> keys = factMap.keySet();
  Iterator<String> iter = keys.iterator();
  String rKey = null;
  Map<String, String> attribs = null;
  while (iter != null && iter.hasNext()) {
    try {
      rKey = iter.next();
      if (rKey == null) {
        continue;
      }
      String cKey = rKey;
      if (cKey.equals("*")) cKey = ".*";
      Pattern pattern = Pattern.compile(cKey, 
          Pattern.CASE_INSENSITIVE| Pattern.MULTILINE);
      Matcher matcher = pattern.matcher(key);
      if (!matcher.matches()) {
        continue;
      }
      attribs = factMap.get(rKey);
      break;
    } catch (Exception e) {
      LOG.log(Level.WARNING, "Error found while inspecting factory map", e);
    }
  }
  
  return attribs;
}

/**
 * Creates a new SearchEngine object.
 * 
 * @param criteria the criteria
 * @param result the result
 * @param context the context
 * @param rids the rids
 * @param messageBroker the message broker
 * @param username the username
 * @param password the password
 * @return the map
 * @throws SearchException the search exception
 */
public static Map<String, Object> createSearchEngines (
    final SearchCriteria criteria, 
    final SearchResult result,
    final RequestContext context,
    final StringSet rids,
    final MessageBroker messageBroker,
    final String username,
    final String password) throws SearchException {
  
  Map<String, Object> returnMap = new HashMap<String, Object>();
  Map<String,StringSet> mapEngineName2rids = 
    new HashMap<String,StringSet> ();
  Map<String, ASearchEngine> mapEngineName2Engine = 
    new HashMap<String, ASearchEngine>();
  
  for(String rid: rids) {
    
    ASearchEngine engine = identifyEngine(rid, context);
    if(engine == null) {
      returnMap.put(rid, messageBroker.getMessage(
          "catalog.search.distributedSearch.ridEngineNotFound", 
          new Object[]{rid}));
      continue;
    }
    
    // map rids to canonical engine
    String engineCanonicalName = engine.getClass().getCanonicalName();
    StringSet ridEngineSet = 
      mapEngineName2rids.get(engineCanonicalName);
    if(ridEngineSet == null) {
      ridEngineSet = new StringSet();
      mapEngineName2rids.put(engineCanonicalName, ridEngineSet);  
    }
    ridEngineSet.add(rid);
    
    // save engine
    if(!mapEngineName2Engine.containsKey(engineCanonicalName)) {
      mapEngineName2Engine.put(engineCanonicalName, engine);
    }
  }
  
  // create instances of search engines using asearchegine.createinstances
  Set<String> setEngineName = mapEngineName2rids.keySet();
  for(String sEngineName: setEngineName) {
    StringSet setEngineRids = mapEngineName2rids.get(sEngineName);
    ASearchEngine searchEngine = mapEngineName2Engine.get(sEngineName);
    try {
      searchEngine.setRequestContext(context);
      searchEngine.setMessageBroker(messageBroker);
      Map<String, Object> searchEngineInstances = 
        searchEngine.createInstances(setEngineRids); 
      returnMap.putAll(searchEngineInstances);
    } catch(SearchException e) {
      for(String rid: setEngineRids) {
        returnMap.put(rid, "Rid = " + rid + " " + e.getMessage());
        LOG.log(Level.WARNING, "Rid = " + rid + " error", e);
      }
    }
  }
  
  // Intialize engine
  Set<String> setRid = returnMap.keySet();
  for(String rid:setRid) {
    Object obj = returnMap.get(rid);
    if(!(obj instanceof ASearchEngine)) {
      continue;
    }
    ASearchEngine engine = (ASearchEngine) obj;
    try {
      // TODO: Make this threaded.  Get capabilities may be called
      intializeEngine(engine, criteria, result, context, rid, messageBroker, 
        username, password);
    } catch (Exception e) {
      returnMap.put(rid, e.getMessage());
      LOG.log(Level.FINE, "Rid += " + rid + " Error while initializing engine" ,
          e);
    }
    
  }
  
  return returnMap;
}


}
