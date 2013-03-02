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

import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.esri.gpt.catalog.search.SearchGptXslProfile;
import com.esri.gpt.framework.search.SearchXslProfile.FORMAT_SEARCH_TO_XSL;
import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.CswProfile;
import com.esri.gpt.server.csw.client.CswProfiles;

/**
 * The collection of Search Profiles.
 * 
 * @param <P> the generic type
 */
public abstract class SearchXslProfiles<P extends SearchXslProfile<?, ?, ?, ?>>{

// class variables =============================================================
/** The LOG. */
private static Logger LOG = 
  Logger.getLogger(SearchXslProfiles.class.getCanonicalName());

// instance variables ==========================================================
/** The map. */
private final AbstractMap<String, P> map  = 
  new LinkedHashMap<String, P>();

/** The configuration folder path. */
public static final String CONFIG_FOLDER_PATH = "gpt/search/profiles/";

/** The configuration filename. */
private String configurationFileName = "GptXslSearchProfiles.xml";
// constructors ================================================================
/**
 * Instantiates a new search profiles.
 * 
 * 
 */
public SearchXslProfiles() {
}

// properties ==================================================================
/**
 * Gets the configuration file name.
 * 
 * @return the configuration file name (trimmed, never null)
 */
public String getConfigurationFileName() {
  return Val.chkStr(configurationFileName);
}

/**
 * Sets the configuration file name.
 * 
 * @param configurationFileName the new configuration file name
 */
public void setConfigurationFileName(String configurationFileName) {
  this.configurationFileName = configurationFileName;
}

/**
 * Gets the configuration folder path.
 * 
 * @return the configuration folder path (trimmed, never null)
 */
public String getConfigurationFolderPath() {
  return Val.chkStr(CONFIG_FOLDER_PATH);
}


// methods =====================================================================
/**
 * Add a key value pair to profile collection.
 * Add to profile collection
 * 
 * @param profile the profile
 *  
 */
public void addProfile(P profile) {
  map.put(profile.getId(), profile);
}

/**
 * Loads the profile details from configuration file.
 * 
 * The profiles details are loaded in the collection. Duplicate or invalid
 * profiles are ignored.
 *  
 * 
 * @throws ParserConfigurationException 
 * @throws SAXException 
 * @throws ParserConfigurationException 
 * @throws IOException 
 * @throws SAXException 
 * @throws XPathExpressionException 
 */
public abstract void loadProfilefromConfig()
    throws ParserConfigurationException, SAXException, IOException,
    XPathExpressionException;

/**
 * Loads the profile details from configuration file.
 * 
 * The profiles details are loaded in the collection. Duplicate or invalid
 * profiles are ignored.
 * 
 * @param filename
 * @param xslProfileClass Can never be null
 * @throws ParserConfigurationException 
 * @throws SAXException 
 * @throws ParserConfigurationException 
 * @throws IOException 
 * @throws SAXException 
 * @throws XPathExpressionException 

 */
public void loadProfilefromConfig(String filename, P xslProfileClass, 
    String profileTag)
    throws ParserConfigurationException, SAXException, IOException,
    XPathExpressionException {
  if(Val.chkStr(profileTag).equals("")) {
    profileTag = "Profile";
  }

  String configuration_folder_path = this.getConfigurationFolderPath();
  
  //Create absolute path to xslt files
  if (configuration_folder_path == null
      || configuration_folder_path.length() == 0) {
    // Create absolute path to file
    Properties properties = new Properties();
    final URL url = CswProfiles.class.getResource("CswCommon.properties");

    properties.load(url.openStream());

    configuration_folder_path = properties
        .getProperty("DEFAULT_CONFIGURATION_FOLDER_PATH");
  }
  
  
  // XML parser load doc specified by filename
  DocumentBuilder builder = DocumentBuilderFactory.newInstance()
      .newDocumentBuilder();
  ResourcePath rscPath = new ResourcePath();
  InputSource configFile = rscPath.makeInputSource(
      configuration_folder_path + filename);
  if (configFile == null) {
    configFile = rscPath.makeInputSource(
        "/" + configuration_folder_path + filename);
  }

  Document doc = builder.parse(configFile);
  
  // TODO: Move for loop to the super class so that SearchGptXlProfiles
  // Shares this source too. They are the same in the loop.
  // Get a list of nodes of which root is Profile tag
  NodeList profileNodes = doc.getElementsByTagName(profileTag);
  
  for (int i = 0; i < profileNodes.getLength(); i++) {
 // Get "profile" node
    Node currProfile = profileNodes.item(i);

    XPath xpath = XPathFactory.newInstance().newXPath();

    String id = Val.chkStr(xpath.evaluate("ID", currProfile));
    String name = Val.chkStr(xpath.evaluate("Name", currProfile));
    String description = Val.chkStr(xpath.evaluate("Description", currProfile));
    String requestXslt = Val.chkStr(xpath.evaluate(
        "GetRecords/XSLTransformations/Request", currProfile));
    String expectedGptXmlOutput = Val.chkStr(xpath.evaluate(
        "GetRecords/XSLTransformations/Request/@expectedGptXmlOutput", 
        currProfile));
    if(expectedGptXmlOutput.equals("")) {
      expectedGptXmlOutput = 
          FORMAT_SEARCH_TO_XSL.MINIMAL_LEGACY_CSWCLIENT.toString();
    }
    String responseXslt = Val.chkStr(xpath.evaluate(
        "GetRecords/XSLTransformations/Response", currProfile));
    String requestKVPs = Val.chkStr(xpath.evaluate("GetRecordByID/RequestKVPs",
        currProfile)); // GetRecordByID
    // Xslt to transform the response from GetRecordByID
    String metadataXslt = Val.chkStr(xpath.evaluate(
        "GetRecordByID/XSLTransformations/Response", currProfile));
    boolean extentSearch  = Boolean.parseBoolean(Val.chkStr(xpath.evaluate(
        "SupportSpatialQuery", currProfile)));
    boolean liveDataMaps = Boolean.parseBoolean(Val.chkStr(xpath.evaluate(
        "SupportContentTypeQuery", currProfile)));
    boolean extentDisplay = Boolean.parseBoolean(Val.chkStr(xpath.evaluate(
        "SupportSpatialBoundary", currProfile)));
    boolean harvestable = Boolean.parseBoolean(Val.chkStr(xpath.evaluate(
        "Harvestable", currProfile)));
    

    requestXslt = configuration_folder_path + requestXslt;
    responseXslt = configuration_folder_path + responseXslt;
    metadataXslt = configuration_folder_path + metadataXslt;
    
    SearchXslProfile profile = null;
    try {
      profile = xslProfileClass.getClass().newInstance();
      profile.setId(id);
      profile.setName(name);
      profile.setDescription(description);
      profile.setRequestxslt(requestXslt);
      profile.setResponsexslt(responseXslt);
      profile.setMetadataxslt(metadataXslt);
      profile.setSupportsContentTypeQuery(liveDataMaps);
      profile.setSupportsSpatialBoundary(extentDisplay);
      profile.setSupportsSpatialQuery(extentSearch);
      profile.setKvp(requestKVPs);
      profile.setHarvestable(harvestable);
      profile.setFormatRequestToXsl(
          SearchXslProfile.FORMAT_SEARCH_TO_XSL.valueOf(expectedGptXmlOutput));
      
      profile.setFilter_extentsearch(extentSearch);
      profile.setFilter_livedatamap(liveDataMaps);
      addProfile((P)profile);
    } catch (InstantiationException e) {
      throw new IOException("Could not instantiate profile class" + 
          e.getMessage());
    } catch (IllegalAccessException e) {
      throw new IOException("Could not instantiate profile class" + 
          e.getMessage());
    }
    //CswProfile profile = new CswProfile(id, name, description, requestKVPs,
       // requestXslt, responseXslt, metadataXslt, liveDataMaps, extentSearch);
   
  }
  
}

/**
 * Get csw profile with its id.
 * 
 * @param id the id
 * @return the profile by id
 */
public P getProfileById(String id) {
  return (P) map.get(id);
}

/**
 * Gets the profiles as collection.
 * 
 * @return the profiles as collection
 */
public Collection<P> getProfilesAsCollection() {
  return map.values();
}

/**
 * Gets the size.
 * 
 * @return the size
 */
public int getSize() {
  return map.values().size();
}

}