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
package com.esri.gpt.server.csw.client;

import java.io.IOException;
import java.net.URL;
import java.util.Properties;

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

import com.esri.gpt.framework.search.SearchXslProfiles;
import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;


/**
 * The collection of CSw profiles.
 * 
 */
public class CswProfiles extends SearchXslProfiles<CswProfile> {

// instance variables ==========================================================

/** The configuration_folder_path. */
private String  configuration_folder_path = ""; 

/** The profiles file **/
private static final String CSW_PROFILES_FILE = "CSWProfiles.xml";

// constructors ================================================================
/**
 * Instantiates a new csw profiles.
 */
public CswProfiles() {
  super();
 
}

// properties ==================================================================


// methods =====================================================================
/**
 * Loads the profile details from configuration file.
 * 
 * The profiles details are loaded in the collection. Duplicate or invalid
 * profiles are ignored.
 * 
 * @param filename
 * @throws ParserConfigurationException 
 * @throws SAXException 
 * @throws ParserConfigurationException 
 * @throws IOException 
 * @throws SAXException 
 * @throws XPathExpressionException 
 */
public void loadProfilefromConfig(String filename)
    throws ParserConfigurationException, SAXException, IOException,
    XPathExpressionException {
  
  configuration_folder_path = this.getConfigurationFolderPath();
  if(Val.chkStr(filename).equals("")) {
    filename = CSW_PROFILES_FILE;
  }
  super.loadProfilefromConfig(filename, new CswProfile(), "Profile");
 
}

/**
 * Load profile from default profiles config file.
 * 
 * @throws XPathExpressionException the x path expression exception
 * @throws ParserConfigurationException the parser configuration exception
 * @throws SAXException the SAX exception
 * @throws IOException Signals that an I/O exception has occurred.
 */
public void loadProfilefromConfig() throws XPathExpressionException,
    ParserConfigurationException, SAXException, IOException {
  loadProfilefromConfig(null);
}

}