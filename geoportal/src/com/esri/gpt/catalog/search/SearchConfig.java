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

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerConfigurationException;

import org.w3c.dom.Node;

import com.esri.gpt.framework.context.Configuration;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XsltTemplate;



/**
 * The Class SearchConfig.  Holds the searchConfiguration.
 * Do not instantiate this class to get the configuration.  The configuration
 * should be accessed through the ApplicationContext or through the
 * convinience method getConfiguredInstance.
 * 
 * @see com.esri.gpt.framework.context.ApplicationContext
 */
public class SearchConfig extends Configuration {

// class variables =============================================================

/** The default number of results per page *. */
public final static int DEFAULT_RESULTS_PER_PAGE = 10; 

/** The Constant DEFAULT_TIMEOUT. */
public final static int DEFAULT_TIMEOUT = -1;

/** The Constant DEFAULT_DISTR_TIMEOUT. */
public final static int DEFAULT_DISTR_TIMEOUT = 5000;

/** The Constant DEFAULT_MAXIMUM_SEARCHSITES. */
public final static int DEFAULT_MAXIMUM_SEARCHSITES = 5;

/** The Constant DEFAULT CSW PROFILE. */
public final static String DEFAULT_CSW_PROFILE = "CSW.OGCCORE.ESRI";

/** The dConstant DEFAULT MAXIMUM SAVED SEARCHES *. */
public final static int DEFAULT_MAX_SAVED_SEARCHES = 10;

/** Class logger *. */
private final static Logger LOG = 
  Logger.getLogger(SearchConfig.class.getCanonicalName());

// instance variables ==========================================================
/** The default envelope. */
private Envelope defaultEnvelope;

/** The CSW profile. */
private String cswProfile;  

/** The GPT TO CSW XSLT template *. */
private XsltTemplate gptToCswXsltTemplate;

/** The GPT to CSW XSLT relative file path *. */
private String gptToCswXsltPath;

/** The results per page. */
private String resultsPerPage;

/** The results reviews shown. */
private String resultsReviewsShown;

/** The search URI. */
private String searchUri;

/** The search factory. */
@SuppressWarnings("unchecked")
private LinkedHashMap searchFactoryRepos;

/** The map viewer instances. */
private ArrayList<MapViewerConfigs> mapViewerInstances;

/** The time out. */
private String timeOut;

/** The distributed search timeout millisecs. */
private String distributedSearchTimeoutMillisecs;

/** The distributed search max selected sites. */
private String distributedSearchMaxSelectedSites;

/** The map viewer url. (Was meant for an MVS Map viewer*/
private String mapViewerUrl;

/** The map viewer url meant for a generic map viewer). */
private String mapViewerUrl2;

/** The maximum saved searches. */
private String maxSavedSearches;

/** The mvs username. */
private String mvsUsername;

/** The mvs password. */
private String mvsPassword;

/** The allow external search. */
private boolean allowExternalSearch;

/** The allow temporal search. */
private boolean allowTemporalSearch = false;

/** XSL workflow object *. */
private SearchGptXslProfiles gptXslProfiles = new SearchGptXslProfiles();

/** The jsf suffix. */
private String jsfSuffix;

private Node searchConfigNode;



// constructor =================================================================
/**
 * Instantiates a new search configuration.
 */
public SearchConfig () {
  super();
}

// properties ==================================================================

/**
 * Checks if is allow external search.
 * 
 * @return true, if is allow external search
 */
public boolean getAllowExternalSearch() {
  return allowExternalSearch;
}

/**
 * Sets the allow external search.
 * 
 * @param allowExternalSearch the new allow external search
 */
public void setAllowExternalSearch(boolean allowExternalSearch) {
  this.allowExternalSearch = allowExternalSearch;
}

/**
 * Checks if is allow temporal search.
 * @return true, if is allow temporal search
 */
public boolean getAllowTemporalSearch() {
  return allowTemporalSearch;
}
/**
 * Sets the allow temporal search.
 * @param allowTemporalSearch allow temporal search
 */
public void setAllowTemporalSearch(boolean allowTemporalSearch) {
  this.allowTemporalSearch = allowTemporalSearch;
}

/**
 * Gets the search config node.
 *
 * @return the search config node
 */
public Node getSearchConfigNode() {
  return searchConfigNode;
}

/**
 * Sets the search config node.
 *
 * @param searchConfigNode the new search config node
 */
public void setSearchConfigNode(Node searchConfigNode) {
  this.searchConfigNode = searchConfigNode;
}
/**
 * Gets the time out.
 * 
 * @return the time out
 */
public int getTimeOut() {
  
  try {
    int tmp =  Integer.valueOf(this.timeOut);
    if(tmp < 1) {
      throw new NumberFormatException("timeout must be positive integer");
    }
    return tmp;
  }catch(NumberFormatException e) {
    LOG.log(Level.WARNING, "Timeout read from search " +
        "configuration = "+ this.timeOut 
        + " not a valid integer.  Will use default. " +
        DEFAULT_TIMEOUT, new ConfigurationException("", e));
   
  }
  return DEFAULT_TIMEOUT;
}

/**
 * Sets the time out.
 * 
 * @param timeOut the new time out
 */
public void setTimeOut(String timeOut) {
  this.timeOut = Val.chkStr(timeOut);
}

/**
 * Gets the default envelope.
 * 
 * @return the default envelope (never null)
 */
public Envelope getDefaultEnvelope() {
  
  if(defaultEnvelope == null) {
    /* Envelope tmp = new Envelope();
    tmp.setMaxX(DEFAULT_MAXX);
    tmp.setMaxY(DEFAULT_MAXY);
    tmp.setMinY(DEFAULT_MINY);
    tmp.setMinX(DEFAULT_MINX);
    LOG.warning("Search Default envelope missing.  Setting envelope " +
    		"to " + tmp.toString());
    return tmp;*/
    return new Envelope();
  }

  return defaultEnvelope;
}

/**
 * Sets the default envelope.
 * 
 * @param defaultEnvelope the new default envelope
 */
public void setDefaultEnvelope(Envelope defaultEnvelope) {
  this.defaultEnvelope = defaultEnvelope;
  
}


/**
 * Gets the jsf suffix.
 * 
 * @return the jsf suffix (trimmed, never null, default = ".page")
 */
public String getJsfSuffix() {
  return Val.chkStr(jsfSuffix);
}

/**
 * Sets the jsf suffix.
 * 
 * @param jsfSuffix the new jsf suffix
 */
public void setJsfSuffix(String jsfSuffix) {
  this.jsfSuffix = jsfSuffix;
}

/**
 * Gets the map viewer url.
 * 
 * @return the map viewer url (trimmed never null)
 */
public String getMapViewerUrl() {
  return Val.chkStr(mapViewerUrl);
}

/**
 * Sets the map viewer url.
 * 
 * @param mapViewerUrl the new map viewer url
 */
public void setMapViewerUrl(String mapViewerUrl) {
  this.mapViewerUrl = mapViewerUrl;
}

/**
 * Gets the gpt to csw xslt template.
 * 
 * @return the gpt to csw xslt template
 * 
 * @throws SearchException the search exception
 * @throws Searchception xlst template not initialized by configuration
 */
public XsltTemplate getGptToCswXsltTemplate() throws SearchException {
  if(this.gptToCswXsltTemplate != null) {
    return this.gptToCswXsltTemplate;
  }
  String path = this.getGptToCswXsltPath();
  Exception tmpltException = null;
  synchronized(this) {
    
    if(this.gptToCswXsltTemplate != null) {
      return this.gptToCswXsltTemplate;
    }
    try {
      this.setGptToCswXsltTemplate(XsltTemplate.makeTemplate(path));
    } catch (TransformerConfigurationException e) {
      tmpltException = e;
    }
   
  }
  if(tmpltException != null) {
    throw new SearchException("Could not make xslt template from path " +
    		path , tmpltException );
  }
    
  return this.gptToCswXsltTemplate;
}

/**
 * Sets the gpt to csw xslt template.
 * 
 * @param gptToCswXsltTemplate the new gpt to csw xslt template
 */
private void setGptToCswXsltTemplate(XsltTemplate gptToCswXsltTemplate) {
  this.gptToCswXsltTemplate = gptToCswXsltTemplate;
}



/**
 * Gets the GPT to CSW XSLT relative file path in classes directory.
 * 
 * @return the GPT to CSW XSLT (never null, trimmed)
 */
private String getGptToCswXsltPath() {

  return gptToCswXsltPath;
}

/**
 * Gets the results per page.
 * 
 * @return the results per page
 */
public int getResultsPerPage() {

 try {
   int tmp =  Integer.valueOf(this.resultsPerPage);
   if(tmp < 1) {
     throw new NumberFormatException("Pages must be positive integer");
   }
   return tmp;
 }catch(NumberFormatException e) {
   LOG.log(Level.WARNING, "Results per page read from search " +
       "configuration = "+ this.resultsPerPage 
       + " not a valid integer.  Will use default. " +
       DEFAULT_RESULTS_PER_PAGE, new ConfigurationException("", e));
  
 }
 
  return DEFAULT_RESULTS_PER_PAGE;
}

/**
 * Sets the results per page.
 * 
 * @param resultsPerPage the results per page
 */
public void setResultsPerPage(String resultsPerPage) {
  this.resultsPerPage = Val.chkStr(resultsPerPage);
   
}

/**
 * Gets the gpt xsl profiles.
 * 
 * @return the gpt xsl profiles
 * @throws SearchException 
 * TODO: Make this threadsafe TM
 */
public SearchGptXslProfiles getGptXslProfiles() throws SearchException {
  if(this.gptXslProfiles == null) {
    this.gptXslProfiles = new SearchGptXslProfiles();
  }
  if(this.gptXslProfiles.getSize() < 1) {
    try {
    this.gptXslProfiles.loadProfilefromConfig();
    } catch(Exception e) {
      LOG.log(Level.WARNING, "Could not gpt xsl profiles load profiles", e);
      throw new SearchException(e);
    }
  }
  return this.gptXslProfiles;
}

/**
 * Gets the Search URI.
 * 
 * @return the Search uri (never null)
 * 
 * @throws SearchException if uri configured has a problem
 */
public URI getSearchUri() throws SearchException {
  
 
  try {
    return new URI(searchUri);
  } catch (URISyntaxException e) {
    throw new SearchException("Search URI configured = " +
        this.searchUri +" invalid", e);
  }
}

/**
 * Sets the search URI.
 * 
 * @param searchUri the new search URI (nulls allowed)
 */
public void setSearchUri(String searchUri) {
  this.searchUri = Val.chkStr(searchUri);
}

/**
 * Gets the CSW profile.
 * If not configured, the default profile returned is CSW.OGCCORE.ESRI
 * 
 * @return the CSW profile (trimmed, never null )
 */
public String getCswProfile() {
  cswProfile = Val.chkStr(cswProfile);
  if("".equals(cswProfile)) {
    LOG.warning("Search Configuration: CSW Profile not configured. Defaulting" +
    		" to " + DEFAULT_CSW_PROFILE);
    return DEFAULT_CSW_PROFILE;
  }
  return cswProfile;
}

/**
 * Sets the CSW profile.
 * 
 * @param cswProfile the new CSW profile
 */
public void setCswProfile(String cswProfile) {
  this.cswProfile = cswProfile;
}

/**
 * Sets the gpt to csw xslt.
 * 
 * @param path the new gpt to csw xslt file path
 * 
 * @exception ConfigurationException If resource cannot be initialized
 */
public void setGptToCswXsltPath(String path){
 
  this.setGptToCswXsltTemplate(null);
  if(path == null || "".equals(path.trim())) {
    path = "gpt/search/gpt2csw_OGCCORE.xslt";
  }
  this.gptToCswXsltPath = Val.chkStr(path);
   
}

/**
 * Gets the distributed search timeout millisecs.
 * 
 * @return the distributed search timeout millisecs
 */
public int getDistributedSearchTimeoutMillisecs() {
  Exception exc;
  try {
    return Integer.parseInt(distributedSearchTimeoutMillisecs);
  } catch (NullPointerException e) {
    exc = e;

  } catch (NumberFormatException e) {
    exc = e;
  }
  if( exc != null) {
    LOG.log(Level.WARNING, "Maximum Distributed Search Timeout = " +  
        distributedSearchTimeoutMillisecs 
        + " in gpt configuratin invalid. Using default = " 
        + DEFAULT_DISTR_TIMEOUT, exc);
  }
  
  return DEFAULT_DISTR_TIMEOUT;

}

/**
 * Sets the distributed search timeout millisecs.
 * 
 * @param distributedSearchTimeoutMillisecs the new distributed search timeout millisecs
 */
public void setDistributedSearchTimeoutMillisecs(
    String distributedSearchTimeoutMillisecs) {
  this.distributedSearchTimeoutMillisecs = distributedSearchTimeoutMillisecs;
}

/**
 * Gets the distributed search max selected sites.
 * 
 * @return the distributed search max selected sites
 */
public int getDistributedSearchMaxSelectedSites() {
  Exception exc;
  try {
    return Integer.parseInt(distributedSearchMaxSelectedSites);
  } catch (NullPointerException e) {
    exc = e;

  } catch (NumberFormatException e) {
    exc = e;
  }
  if( exc != null) {
    LOG.log(Level.WARNING, "Maximum Distributed Search Sites = " +  
        distributedSearchMaxSelectedSites 
        + " in gpt configuratin invalid. Using default = " 
        + DEFAULT_MAXIMUM_SEARCHSITES, exc);
  }
  
  return DEFAULT_MAXIMUM_SEARCHSITES;
}

/**
 * Sets the distributed search max selected sites.
 * 
 * @param distributedSearchMaxSelectedSites the new distributed search max selected sites
 */
public void setDistributedSearchMaxSelectedSites(
    String distributedSearchMaxSelectedSites) {
  this.distributedSearchMaxSelectedSites = distributedSearchMaxSelectedSites;
}

/**
 * Sets the map viewer instances.
 * 
 * @param mapViewerInstances the new map viewer instances
 */
public void setMapViewerInstances(
    ArrayList<MapViewerConfigs> mapViewerInstances) {
  this.mapViewerInstances = mapViewerInstances;
}

/**
 * Gets the max saved searches.
 * 
 * @return the max saved searches
 */
public int getMaxSavedSearches() {
  Exception exc = null;
  try {
    return Integer.parseInt(maxSavedSearches);
  } catch (NullPointerException e) {
    exc = e;

  } catch (NumberFormatException e) {
    exc = e;
  }
  if( exc != null) {
    LOG.log(Level.WARNING, "Maximum Saved Searches = " +  maxSavedSearches 
        + " in gpt configuratin invalid. Using default = " 
        + DEFAULT_MAX_SAVED_SEARCHES, exc);
  }
  return DEFAULT_MAX_SAVED_SEARCHES;
}


/**
 * Sets the max saved searches.
 * 
 * @param maxSavedSearches the new max saved searches
 */
public void setMaxSavedSearches(String maxSavedSearches) {
  this.maxSavedSearches = maxSavedSearches;
}

/**
 * Gets the mvs username.
 * 
 * @return the mvs username (trimmed, never null)
 */
public String getMvsUsername() {
  //return Val.chkStr(mvsUsername);
  //TODO: change to variable
  return "admin";
}

/**
 * Sets the mvs username.
 * 
 * @param mvsUsername the new mvs username
 */
public void setMvsUsername(String mvsUsername) {
  this.mvsUsername = mvsUsername;
}

/**
 * Gets the mvs password.
 * 
 * @return the mvs password (trimmed, never null)
 */
public String getMvsPassword() {
  //return Val.chkStr(mvsPassword);
  //TODO: Change to variable
  return "admin";
}

/**
 * Sets the mvs password.
 * 
 * @param mvsPassword the new mvs password
 */
public void setMvsPassword(String mvsPassword) {
  this.mvsPassword = mvsPassword;
}

/**
 * Gets the search factory.
 * 
 * @return the search factory (never null)
 */
@SuppressWarnings("unchecked")
public LinkedHashMap<String, Map<String, String>> getSearchFactoryRepos() {
  if(searchFactoryRepos == null) {
    searchFactoryRepos = new LinkedHashMap<String, Map<String, String>>();
  }
  return searchFactoryRepos;
}

/**
 * Sets the search factory.
 * 
 * @param searchFactory the new search factory
 */
public void setSearchFactoryRepos(LinkedHashMap<String, Map<String, String>> 
  searchFactory) {
  this.searchFactoryRepos = searchFactory;
}

/**
 * Gets the map viewer instances.
 * 
 * @return the map viewer instances (possibly null)
 */
public ArrayList<MapViewerConfigs> getMapViewerInstances() {
  return mapViewerInstances;
}

/**
 * Gets the results reviews shown.
 * 
 * @return the results reviews shown (always either "none", "only-reviewed" or "all")
 */
public String getResultsReviewsShown() {
  String tmp = Val.chkStr(resultsReviewsShown);
  String sDefault = "none";
  if("".equals(tmp)) {
    return sDefault;
  } else if(tmp.toLowerCase().equals("only-reviewed")) {
    return "only-reviewed";
  } else if(tmp.toLowerCase().equals("all")) {
    return "all";
  } else {
    sDefault = "none";
  }
  return sDefault;
}

/**
 * Sets the results reviews shown.
 * 
 * @param resultsReviewsShown the new results reviews shown
 */
public void setResultsReviewsShown(String resultsReviewsShown) {
  this.resultsReviewsShown = resultsReviewsShown;
}
 
// methods =====================================================================
/**
 * Convinience method. Gets the configured instance.
 * 
 * @return the configured instance  (never null)
 * 
 * @throws ConfigurationException if SearchConfig is null in
 * ApplicationContext
 */
public static SearchConfig getConfiguredInstance() {
  
  RequestContext requestContext = RequestContext.extract(null);
  try {
    SearchConfig config = 
      requestContext
      .getApplicationConfiguration()
      .getCatalogConfiguration()
      .getSearchConfig();

    if(config == null ) {
      throw new ConfigurationException("Got null Search Configuration from " +
      "ApplicationContext.CatalogConfiguration.searchConfig");
    } 
    return config;
  } finally {
    if(requestContext != null) {
      requestContext.onExecutionPhaseCompleted();
    }
  }

}


/**
 * To string.
 * 
 * @return String representing object
 * 
 * @see com.esri.gpt.framework.context.Configuration#toString()
 */
@Override
public String toString () {
  StringBuilder sb = new StringBuilder();
 

  sb.append(" GPT TO CSW XSLT Resource = ").append(this.getGptToCswXsltPath())
  .append("\n")
  .append("\n")
  .append(" Results Displayed Per Page = ").append(this.getResultsPerPage())
  .append("\n")
  .append(" Timeout in milliseconds ").append(this.getTimeOut())
  .append("\n")
  .append(" Search URI").append(this.searchUri)
  .append("\n")
  .append(" Map Viewer URL").append(this.getMapViewerUrl())
  .append("\n")
  .append(" MVS Sign on username").append(this.getMvsUsername())
  .append("\n")
  .append(" Maximum Saved Searches = ").append(this.getMaxSavedSearches())
  .append("\n")
  .append(" Allow external search  = ").append(this.getAllowExternalSearch())
  .append("\n")
  .append("Maximum selected distributed search sites allowed  = ").append(
      this.getDistributedSearchMaxSelectedSites())
  .append("Maximum selected distributed search time  = ").append(
      this.getDistributedSearchTimeoutMillisecs())    
  .append("\n")
  .append("Default map viewer url  = ").append(
      this.getDefaultViewerUrl()).append("\n")
  .append("Jsf Suffix  = ").append(
      this.getJsfSuffix()).append("\n")
  .append("Review Results = ").append(
      this.getResultsReviewsShown()).append("\n")    
  .append("\n")
  
  ;
  
  return sb.toString();
}

/**
 * Validate.  Triggers gets which print errors
 * 
 * @throws ConfigurationException if configuration parameters are wrong
 */
public void validate() {
  
  this.getCswProfile();
  this.getGptToCswXsltPath();
  this.getResultsPerPage();
  this.getMaxSavedSearches();
 
  
  try {
    this.getGptToCswXsltTemplate();
  } catch (SearchException e) {
    LOG.log(Level.SEVERE, "error while getting XSLT csw to gpt xslt " , e);
  }
 
  try {
    this.getMapviewerUrl2();
  } catch (SearchException e) {
    LOG.log(Level.SEVERE, "error while getting the url of the map viewer " +
    		" value = " + this.getMapViewerUrl()
        , e);
  }
  
  //TODO: Take this out once map service stuff is straight
  /*
  try {
    this.getMapviewerServiceUrl2();
  } catch (SearchException e) {
    LOG.log(Level.SEVERE, "error while getting the url of the map viewer service" 
        + " value = " + this.getMapViewerUrl()
        , e);
  }*/

}

/**
 * Gets the mapviewer as java.net.URL object.
 * 
 * @return the mapviewer url (never null)
 * 
 * @throws SearchException If mapvewer url was found to be errant
 */
public URL getMapviewerUrl2() throws SearchException {
  try {
    if(this.getMapViewerUrl() != null && this.getMapViewerUrl().length() > 0)
      return new URL(this.getMapViewerUrl());
    else
      return null;
  } catch(Exception e) {
    throw new SearchException("Url = " + this.getMapViewerUrl(), e);
  }
}

/**
 * Gets the default viewer url.
 * 
 * @return the default viewer url (never null, trimmed)
 */
public String getDefaultViewerUrl() {
  if(mapViewerUrl2 == null) {
    IMapViewer iMapViewer = MapViewerFactory.createDefaultMapViewer();
    if (iMapViewer != null) {
      mapViewerUrl2 = iMapViewer.readOpenDefaultMapViewerUrl();
    }
  }
  return Val.chkStr(mapViewerUrl2);
}





}

