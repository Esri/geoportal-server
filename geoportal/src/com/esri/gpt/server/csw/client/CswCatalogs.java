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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import java.io.IOException;

import java.net.URL;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;


/**
 * Collections class for catalogs.
 *
 * The collection support both sequential and random access objects based
 * on key.
 */
public class CswCatalogs {
    private AbstractMap map = new HashMap();

    public CswCatalogs() {
    }

    /**
     * Add a key value pair to catalog collection.
     *
     * Add to catalog collection.
     * @param key The key which is the url hashcode for the catalog
     * @param catalog the catalog object
     */
    public void addCatalog(String key, CswCatalog catalog) {
        map.put(key, catalog);
    }

    /**
     * Add a catalog using its id as a key.
     * @param catalog
     */
    public void addCatalog(CswCatalog catalog) {
        map.put(catalog.getId(), catalog);
    }

    /**
     * Add a new Catalog to the collection. The catalog details are also added to the
     * configuration to the file.
     *
     * The catalog details are added in the collection. The catalog details
     * are also appended in the configuration file.
     * @param catalog CswCatalog
     * @param filename the catalog configuration file
     */
    public void addCatalogtoConfig(CswCatalog catalog, String filename) {
        // NO IMPLMENTATION NEEDED
    }

    /**
     * Delete an existing Catalog from the configuration file.
     *
     * The catalog details are deleted from the collection. The catalog
     * details are also deleted from the configuration file.
     * @param catalog CswCatalog
     * @param filename the catalog configuration file
     */
    public void deleteCatalogfromConfig(CswCatalog catalog, String filename) {
        // NO IMPLMENTATION NEEDED
    }

    /**
     * Loads the catalog details from configuration file.
     *
     * The catalog details are loaded in the collection. Duplicate or invalid
     * catalog are ignored. Invalid catalog includes catalogs with profiles
     * information not present in profiles collection.
     * @param filename the catalog configuration file
     * @param profileList the profiles collection
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws XPathExpressionException
     */
    public void loadCatalogfromConfig(String filename, CswProfiles profileList)
        throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        if ((filename == null) || (filename.length() == 0)) {
            // Create absolute path to file
            Properties properties = new Properties();
            final URL url = CswCatalogs.class.getResource("CswCommon.properties");

            properties.load(url.openStream());

            filename = properties.getProperty("DEFAULT_CONFIGURATION_FOLDER_PATH");
            filename += properties.getProperty("DEFAULT_CATALOG_FILE");
        }

        String CATALOG_TAG = "CSWCatalog";

        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                                                        .newDocumentBuilder();
        Document doc = builder.parse(filename);

        NodeList nodes = doc.getElementsByTagName(CATALOG_TAG);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node currNode = nodes.item(i);

            // Parse xml and get each value
            XPath xpath = XPathFactory.newInstance().newXPath();

            String url = xpath.evaluate("URL", currNode);
            String name = xpath.evaluate("Name", currNode);

            // get profile id from the current node
            String profileId = xpath.evaluate("CSWProfile", currNode);
            String userName = xpath.evaluate("Credentials/Username", currNode);
            String password = xpath.evaluate("Credentials/Password", currNode);

            // get the corresponding profile from CswProfiles
            CswProfile profile = profileList.getProfileById(profileId);

            CswCatalog catalog = new CswCatalog(url, name, profile);

            addCatalog(catalog);
        }
    }
    
    
    
    

    /**
     *
     * @param id
     */
    public CswCatalog getCatalog(String id) {
        return (CswCatalog) map.get(id);
    }

    public CswCatalog getCatalog(int index) {
        String key = null;

        key = (String) map.keySet().toArray()[index];

        return (CswCatalog) map.get(key);
    }

    public int getSize() {
        return map.values().size();
    }

    /**
     * Update the name of an existing Catalog. The name is also updated in the
     * configuration file.
     *
     * The catalog name is updated from the collection. The catalog name is
     * also updated from the configuration file.
     * @param catalog CswCatalog
     * @param displayname The string name
     * @param surl The string updated url
     * @param profile The string name
     * @param filename the catalog configuration file
     */
    public void updateCatalogNameinConfig(CswCatalog catalog,
        String displayname, String surl, CswProfile profile, String filename) {
        // NO IMPLMENTATION NEEDED
    }

   

 
}
