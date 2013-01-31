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
import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.logging.Level;
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


import com.esri.gpt.framework.request.QueryCriteria;
import com.esri.gpt.framework.request.QueryResult;
import com.esri.gpt.framework.request.Record;
import com.esri.gpt.framework.request.Records;
import com.esri.gpt.framework.search.SearchXslProfile;
import com.esri.gpt.framework.search.SearchXslProfiles;
import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.client.CswProfile;
import com.esri.gpt.server.csw.client.CswProfiles;


/**
 * The Class SearchGptProfiles.  Encapsulates CSW profiles and Gpt profiles.
 */
public class SearchGptXslProfiles 
  extends SearchXslProfiles<SearchXslProfile<QueryCriteria, Record, 
  Records<Record>, QueryResult<Records<Record>>>> {

// class variables =============================================================
/** Class logger *. */
private static Logger LOG = Logger.getLogger(
    SearchGptXslProfiles.class.getCanonicalName());

// instance variables ==========================================================
/** The _csw profiles. */
private CswProfiles _cswProfiles = new CswProfiles();

// constructors ================================================================

/**
 * Instantiates a new search gpt xsl profiles.
 */
public SearchGptXslProfiles() {
  super();
  super.setConfigurationFileName("GptXslSearchProfiles.xml");
  _cswProfiles.setConfigurationFileName(this.getConfigurationFileName());
  
}
// properties ==================================================================
/**
 * Gets the csw profiles.
 * 
 * @return the csw profiles
 */
public CswProfiles getCswProfiles() {
  if(_cswProfiles == null) {
    _cswProfiles = new CswProfiles();
  }
  return _cswProfiles;
}

// methods =====================================================================
/**
 * Gets the profile associated with the id.  First looks at the cswProfile
 * then the gpt profiles
 * 
 * @param id Gets the profile
 *
 * @see com.esri.gpt.framework.search.SearchXslProfiles#getProfileById(java.lang.String)
 */
@SuppressWarnings("unchecked")
@Override
public SearchXslProfile getProfileById(
    String id) {
  CswProfile cswProfile = this.getCswProfiles().getProfileById(id);
  if(cswProfile != null) {
    return cswProfile;
  }
  return super.getProfileById(id);
}

/**
 * Gets the profiles as collection.
 * 
 * @return the profiles as collection
 */
@Override
public Collection<SearchXslProfile<QueryCriteria, Record, Records<Record>, 
  QueryResult<Records<Record>>>> 
  getProfilesAsCollection() {
 
  return super.getProfilesAsCollection();
}

/**
 * Gets the size.
 * 
 * @return the size
 */
@Override
public int getSize() {
 
  return super.getSize();
}



/**
 * Load profilefrom config.
 * 
 * @throws ParserConfigurationException the parser configuration exception
 * @throws SAXException the sAX exception
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws XPathExpressionException the x path expression exception
 */
@SuppressWarnings("unchecked")
@Override
public void loadProfilefromConfig()
    throws ParserConfigurationException, SAXException, IOException,
    XPathExpressionException {
 

 this.getCswProfiles().loadProfilefromConfig();
 
 SearchXslProfile profile = new SearchGptXslProfile();
 super.loadProfilefromConfig(this.getConfigurationFileName(), 
     profile, "GptProfile");

}


}
