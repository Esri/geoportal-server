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
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import org.apache.commons.httpclient.HttpClient;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Maintains information of CSW Catalog
 * 
 * The catalogs contain all the information like url, profile information
 * credentials and capabilities.
 */
public class CswCatalog implements Comparable {

/** The base url. */
private String baseUrl;

/** The capabilities. */
private CswCatalogCapabilities capabilities = null;

/** The credentials. */
private UsernamePasswordCredentials credentials;

/** The id. */
private String id;

/** The ID counter. */
private static int IDCounter = 1;

/** The is connected. */
private boolean isConnected = false;

/** The locking. */
private boolean locking = false;

/** The name. */
private String name;

/** The profile. */
private CswProfile profile;

/** The url. */
private String url;

/** The request timeout ms. */
private int responseTimeoutMs;

/** The connection timeout ms. */
private int connectionTimeoutMs;

private HttpClient batchHttpClient;



/**
 * Instantiates a new csw catalog.
 */
public CswCatalog() {
}

/**
 * Creates instance of the catalog.
 * 
 * @param url the url
 * @param name the name
 * @param profile the profile
 */
public CswCatalog(String url, String name, CswProfile profile) {
  this.url = url;
  this.id = this.url;
  this.name = name;
  this.profile = profile;
}

/**
  * Gets the underlying Apache HttpClient to be used for batch requests to the
  * same server.
  *
  * @return the batch client
  */
public HttpClient getBatchHttpClient() {
  return this.batchHttpClient;
}

/**
  * Sets the underlying Apache HttpClient to be used for batch requests to the
  * same server.
  *
  * @param batchHttpClient the batch client
  */
public void setBatchHttpClient(HttpClient batchHttpClient) {
  this.batchHttpClient = batchHttpClient;
}

/**
 * Gets the base url.
 * 
 * @return the base url
 */
public String getBaseUrl() {
  return baseUrl;
}

/**
 * Sets the base url.
 * 
 * @param baseUrl the new base url
 */
public void setBaseUrl(String baseUrl) {
  this.baseUrl = baseUrl;
}

/**
 * Gets the connection timeout ms.
 * 
 * @return the connection timeout ms
 */
public int getConnectionTimeoutMs() {
  return connectionTimeoutMs;
}

/**
 * Sets the connection timeout ms.
 * 
 * @param connectionTimeoutMs the new connection timeout ms
 */
public void setConnectionTimeoutMs(int connectionTimeoutMs) {
  this.connectionTimeoutMs = connectionTimeoutMs;
}

/**
 * Gets the request timeout ms.
 * 
 * @return the request timeout ms
 */
public int getResponseTimeoutMs() {
  return responseTimeoutMs;
}

/**
 * Sets the request timeout ms.
 * 
 * @param requestTimeoutMs the new request timeout ms
 */
public void setResponseTimeoutMs(int requestTimeoutMs) {
  this.responseTimeoutMs = requestTimeoutMs;
}

/**
 * Execute GetCapabilities using SAX objects. Send GetCapabilities request,
 * receive the response from a service, and parse the response to get URLs for
 * "GetRecords" and "GetRecordsById".
 * 
 * @return the csw catalog capabilities
 * @throws SAXException the sAX exception
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws ParserConfigurationException the parser configuration exception
 * @return Csw Capabilities object
 */
private CswCatalogCapabilities executeGetCapabilitiesWithSAX()
    throws SAXException, IOException, ParserConfigurationException {
  CswCatalogCapabilities capabilities = new CswCatalogCapabilities();

  CswClient client = new CswClient();
  client.setConnectTimeout(this.getConnectionTimeoutMs());
  client.setReadTimeout(this.getResponseTimeoutMs());
  client.setBatchHttpClient(getBatchHttpClient());
  // Execute submission and parsing into response element
  InputStream responseStream = client.submitHttpRequest("GET", url, "");
 

  SAXParserFactory factory = SAXParserFactory.newInstance();
  factory.setNamespaceAware(true);
  CapabilitiesParse cParse = new CapabilitiesParse(capabilities);
  factory.newSAXParser().parse(new InputSource(responseStream), cParse);

  this.capabilities = capabilities;
  Utils.close(responseStream);
  return capabilities;
}

/**
 * Gets the capabilities.
 * 
 * @return the capabilities
 */
public CswCatalogCapabilities getCapabilities() {
  return this.capabilities;
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
 * Gets the credentials.
 * 
 * @return the credentials
 */
public UsernamePasswordCredentials getCredentials() {
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
 * Gets the id.
 * 
 * @return the id
 */
public String getId() {
  return id;
}

/**
 * Sets the id.
 * 
 * @param id the new id
 */
public void setId(String id) {
  this.id = id;
}

/**
 * Gets the name.
 * 
 * @return the name
 */
public String getName() {
  return name;
}

/**
 * Sets the name.
 * 
 * @param name the new name
 */
public void setName(String name) {
  // if there is no input for name, use url as a name
  if (name == null || name.length() == 0) {
    name = url;
  }

  this.name = name;
}

/**
 * Gets the profile.
 * 
 * @return the profile
 */
public CswProfile getProfile() {
  return profile;
}

/**
 * Sets the profile.
 * 
 * @param profile the new profile
 */
public void setProfile(CswProfile profile) {
  this.profile = profile;
}

/**
 * Gets the url.
 * 
 * @return the url
 */
public String getUrl() {
  return url;
}

/**
 * Sets the url.
 * 
 * @param url the new url
 */
public void setUrl(String url) {
  this.url = url;
}

/**
 * To connect to a catalog service. The capabilties details are populated based
 * on the service.
 * 
 * @return true if connection can be made to the csw service
 * @throws ParserConfigurationException the parser configuration exception
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws SAXException the sAX exception
 */
public boolean connect() throws SAXException, IOException,
    ParserConfigurationException {
  // Execute getCapabilites and setup URLs for "GetRecords" and "GetRecordById"
  CswCatalogCapabilities capabilities = executeGetCapabilitiesWithSAX();

  this.isConnected = capabilities.isReady();

  return this.isConnected;
}

/**
 * To test if already connected to a catalog service.
 * 
 * @return true if connection has already been made to the csw service else
 *         false
 */
public boolean IsConnected() {
  return this.isConnected;
}

/**
 * Locking.
 * 
 * @return true, if successful
 */
public boolean Locking() {
  return false;
}


public int compareTo(Object arg0) {
  // TODO: IMPLEMENT THIS LATER
  return 0;
}

}