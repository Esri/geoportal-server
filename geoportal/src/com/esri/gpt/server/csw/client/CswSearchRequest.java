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

import java.io.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.xml.sax.SAXException;

/**
 * CswSearchRequest class.
 * 
 * CswSearchRequest class is used to submit CSW search queries and to
 * return CSW search results. Before submiting a request, you need to specify a
 * catalog and provide search criteria.
 */
public class CswSearchRequest {

private static final Logger LOG = 
  Logger.getLogger(CswSearchRequest.class.getCanonicalName());

private CswCatalog        catalog;
private CswSearchCriteria criteria;
private CswClient         cswClient;
private CswSearchResponse response;


public CswSearchRequest() {
  this(null, null);
}

public CswSearchRequest(CswCatalog catalog, String searchText) {
  this.catalog = catalog;
  // Initialize the neccessary objects
  // create search criteria
  this.criteria = new CswSearchCriteria();
  this.criteria.setSearchText(searchText);
  // create csw client
  this.cswClient = new CswClient();
  this.response  = new CswSearchResponse();
}

/**
 * Accessor methods
 */
public CswCatalog getCatalog() {
  return this.catalog;
}

public void setCatalog(CswCatalog catalog) {
  this.catalog = catalog;
  this.cswClient.setBatchHttpClient(catalog.getBatchHttpClient());
}

public CswSearchCriteria getCriteria() {
  return this.criteria;
}

public void setCriteria(CswSearchCriteria criteria) {
  this.criteria = criteria;
}

public CswClient getCswClient() {
  return this.cswClient;
}

public void setCswClient(CswClient cswClient) {
  this.cswClient = cswClient;
}

public CswSearchResponse getCswSearchResponse() {
  return this.response;
}

public void setCswSearchResponse(CswSearchResponse response) {
  this.response = response;
}

/**
 * Retrieve metadata from CSW service by its ID
 * 
 * @param DocID    Metadata document ID
 */
public void getMetadataByID(String DocID) throws NullReferenceException,
    IOException, InvalidOperationException, TransformerException {
  if (DocID == null || DocID.length() == 0) {
    throw new NullReferenceException("No DocID specified");
  }
  if (catalog == null) {
    throw new NullReferenceException("Catalog not specified.");
  }
  if (catalog.getCapabilities() == null) {
    throw new NullReferenceException("Catalog capabilities not initialized.");
  }
  if (catalog.getProfile() == null) {
    throw new NullReferenceException("Catalog profile not specified.");
  }

  if (catalog.getCapabilities().get_getRecordByIDGetURL() == null
      || catalog.getCapabilities().get_getRecordByIDGetURL().length() == 0) {
    throw new NullReferenceException(
        "GetRecordByID URL not specified for the catalog capabilities.");
  }

  CswProfile profile = catalog.getProfile();

  // generate request url
  String getRecordByIDBaseUrl = catalog.getCapabilities()
      .get_getRecordByIDGetURL();

  CswRecord record = this.getRecordById(getRecordByIDBaseUrl, DocID, profile);
  
 
  // add record to the response
  CswRecords records = new CswRecords();
  if (record != null) {
    records.add(record);
  }
  response.setRecords(records);

}

/**
 * Gets the record by id.
 * 
 * @param requestURL the request URL (CSW service url)
 * @param DocID the doc ID (GUID or UUID)
 * @param profile the profile
 * 
 * @return the record by id
 * 
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws TransformerException the transformer exception
 * @throws NullReferenceException the null reference exception
 * @throws InvalidOperationException the invalid operation exception
 */
@SuppressWarnings("unchecked")
public CswRecord getRecordById(String requestURL, String DocID,
    CswProfile profile) throws IOException, TransformerException,
    NullReferenceException, InvalidOperationException {

  String requestUrl = profile.generateCSWGetMetadataByIDRequestURL(requestURL,
      DocID);

  if (cswClient == null) {
    cswClient = new CswClient();
    cswClient.setBatchHttpClient(catalog.getBatchHttpClient());
  }

  BufferedInputStream bStream = null;
  InputStream istIntermidiateDoc = null;
  InputStream istRealDoc = null;
  CswRecord record = null;

  try {
    istIntermidiateDoc = 
      cswClient.submitHttpRequest("GET", requestUrl,
      "");

    
    String responseStr = Utils.getInputString2(istIntermidiateDoc);
    
    LOG.log(Level.FINER, "Get Record By Id intermidiate XML = {0}", responseStr);
    response.setResponseXML(Utils.chkStr(responseStr));

    record = new CswRecord();
    CswRecords recordList = null;
    try {
      LOG.finer("GetRecordById: Making csw record object using g" +
      		"etRecordsResponse operation");
      // making cswObject by going through getrecord response xslt
      CswResult results = new CswResult();
      profile.readGetRecordsResponse(responseStr, results);
      recordList = results.getRecords();
      Iterator iter = recordList.iterator();
      if(iter.hasNext()) {
        Object obj = iter.next();
        if(obj instanceof CswRecord ) {
          record = (CswRecord) obj;
        }
      } else  {
        LOG.log(Level.WARNING, 
            "Could not get csw metadata of metadata document");
      }
    } catch (ParserConfigurationException e) {
      LOG.log(Level.INFO, 
        "Could not get csw metadata of metadata document (maybe this csw does " 
        + "not have csw metadata on getRecord by id. " + "{0}", e.getMessage());
    } catch (SAXException e) {
      LOG.log(Level.INFO, 
        "Could not get csw metadata of metadata document (maybe this csw does " 
        + "not have csw metadata on getRecord by id. " + "{0}", e.getMessage());
    } catch (XPathExpressionException e) {
      LOG.log(Level.INFO, 
        "Could not get csw metadata of metadata document (maybe this csw does " 
        + "not have csw metadata on getRecord by id. " + "{0}", e.getMessage());
    }
    record.setId(DocID);
    LOG.finer("GetRecordByID: Transforming intermidiate xml to populate xml " +
    		"into csw Record");
    profile.readCSWGetMetadataByIDResponse(getCswClient(),responseStr, record);
    if (record == null) {
      throw new NullReferenceException("Record not populated.");
    }
    

    // check if full metadata or resourceURL has been returned
    boolean hasFullMetadata = !(record.getFullMetadata() == null || record
        .getFullMetadata().equals(""));
    boolean hasResourceUrl = !(record.getMetadataResourceURL() == null || record
        .getMetadataResourceURL().equals(""));
    if(!hasResourceUrl && requestUrl != null ) {
      record.setMetadataResourceURL(requestUrl);
      hasResourceUrl = true;
    } 
    
    if (!hasFullMetadata && !hasResourceUrl) {
      throw new InvalidOperationException("Neither full metadata nor metadata"
          + " resource URL was found for the CSW record.");
    } 
  }
  finally {
    Utils.close(istIntermidiateDoc);
    Utils.close(bStream);
  }
  return record;

}

/**
 * Gets the record by id.
 * 
 * @param requestURL the request URL (CSW service url)
 * @param DocID the doc ID (GUID or UUID)
 * @param profile the profile
 * 
 * @return the record by id
 * 
 * @throws IOException Signals that an I/O exception has occurred.
 * @throws TransformerException the transformer exception
 * @throws NullReferenceException the null reference exception
 * @throws InvalidOperationException the invalid operation exception
 */
@SuppressWarnings("unchecked")
public CswRecord getRecordById(String requestURL, String DocID,
    CswProfile profile, String username, String password) 
    throws IOException, TransformerException,
    NullReferenceException, InvalidOperationException {

  String requestUrl = profile.generateCSWGetMetadataByIDRequestURL(requestURL,
      DocID);

  if (cswClient == null) {
    cswClient = new CswClient();
    cswClient.setBatchHttpClient(catalog.getBatchHttpClient());
  }

  BufferedInputStream bStream = null;
  InputStream istIntermidiateDoc = null;
  InputStream istRealDoc = null;
  CswRecord record = null;

  try {
    istIntermidiateDoc = 
      cswClient.submitHttpRequest("GET", requestUrl,
      "", username, password);

    
    String responseStr = Utils.getInputString2(istIntermidiateDoc);
    
    LOG.log(Level.FINER, "Get Record By Id intermidiate XML = {0}", responseStr);
    response.setResponseXML(Utils.chkStr(responseStr));

    record = new CswRecord();
    CswRecords recordList = new CswRecords();
    try {
      LOG.finer("GetRecordById: Making csw record object using g" +
          "etRecordsResponse operation");
      // making cswObject by going through getrecord response xslt
      profile.readCSWGetRecordsResponse(responseStr, recordList);
      Iterator iter = recordList.iterator();
      if(iter.hasNext()) {
        Object obj = iter.next();
        if(obj instanceof CswRecord ) {
          record = (CswRecord) obj;
        }
      } else  {
        LOG.log(Level.WARNING, 
            "Could not get csw metadata of metadata document");
      }
    } catch (ParserConfigurationException e) {
      LOG.log(Level.INFO, 
        "Could not get csw metadata of metadata document (maybe this csw does " 
        + "not have csw metadata on getRecord by id. " + "{0}", e.getMessage());
    } catch (SAXException e) {
      LOG.log(Level.INFO, 
        "Could not get csw metadata of metadata document (maybe this csw does " 
        + "not have csw metadata on getRecord by id. " + "{0}", e.getMessage());
    } catch (XPathExpressionException e) {
      LOG.log(Level.INFO, 
        "Could not get csw metadata of metadata document (maybe this csw does " 
        + "not have csw metadata on getRecord by id. " + "{0}", e.getMessage());
    }
    record.setId(DocID);
    LOG.finer("GetRecordByID: Transforming intermidiate xml to populate xml " +
        "into csw Record");
    profile.readCSWGetMetadataByIDResponse(getCswClient(), responseStr, record);
    if (record == null) {
      throw new NullReferenceException("Record not populated.");
    }
    

    // check if full metadata or resourceURL has been returned
    boolean hasFullMetadata = !(record.getFullMetadata() == null || record
        .getFullMetadata() == "");
    boolean hasResourceUrl = !(record.getMetadataResourceURL() == null || record
        .getMetadataResourceURL() == "");
    
    if(!hasResourceUrl && requestUrl != null ) {
      record.setMetadataResourceURL(requestUrl);
      hasResourceUrl = true;
    }

    if (!hasFullMetadata && !hasResourceUrl) {
      throw new InvalidOperationException("Neither full metadata nor metadata"
          + " resource URL was found for the CSW record.");
    } else if (hasResourceUrl) {
      // need to load metadata from resource URL
      istRealDoc = cswClient.submitHttpRequest("GET", Utils.chkStr(record
          .getMetadataResourceURL()), "", username, password);

      responseStr = Utils.getInputString2(istRealDoc);

      record.setFullMetadata(responseStr);
    }
  }
  finally {
    Utils.close(istIntermidiateDoc);
    Utils.close(bStream);
  }
  return record;

}



/**
 * Get the CSW search response of a CSW search request
 * 
 * Get the CSW search response of a CSW search request
 * @return a CswSearchResponse object.
 */
public CswSearchResponse getResponse() {
  return this.response;
}

/**
 * Search CSW catalog using the provided criteria. Search result can be accessed
 * by calling GetResponse().
 * @throws IOException 
 * @throws SAXException 
 * @throws ParserConfigurationException 
 * @throws XPathExpressionException 
 * @throws TransformerException 
 */
public void search() throws NullReferenceException, XPathExpressionException,
    ParserConfigurationException, SAXException, IOException,
    TransformerException {
  // Check the necessary info. to search csw
  if (catalog == null) {
    throw new NullReferenceException("No catalog specified");
  }
  if (criteria == null) {
    throw new NullReferenceException("No specified Criteria");
  }
  if (catalog.getUrl() == null || catalog.getUrl().length() == 0) {
    throw new NullReferenceException("No specified url");
  }
  if (catalog.getProfile() == null) {
    throw new NullReferenceException("No specified profile");
  }

  // Generate getRecords query
  CswProfile profile = catalog.getProfile();

  catalog.connect();
  CswCatalogCapabilities capabilities = catalog.getCapabilities();

  String requestUrl = capabilities.get_getRecordsPostURL();
  String requestQuery = profile.generateCSWGetRecordsRequest(criteria);

  
  // Submit search query and get response as an InputStream object
  InputStream responseStream = cswClient.submitHttpRequest("POST", requestUrl,
      requestQuery);
 
  BufferedInputStream bIStream = new BufferedInputStream(responseStream);
 
  try {
//    String responseStr = Utils.getInputString(bIStream);
    String responseStr = readCharacters(bIStream, "UTF-8");
    // Transform input xml to output xml with Profile
    CswRecords records = new CswRecords();

    profile.readCSWGetRecordsResponse(responseStr, records);

    response.set_requestStr(requestQuery);
    response.setRecords(records);
    response.setResponseXML(responseStr);
  } finally {
    Utils.close(bIStream);
    Utils.close(responseStream);
  }

}

/**
 * Fully reads the characters from an input stream.
 * @param stream the input stream
 * @param charset the encoding of the input stream
 * @return the characters read
 * @throws IOException if an exception occurs
 */
private String readCharacters(InputStream stream, String charset)
  throws IOException {
  StringBuilder sb = new StringBuilder();
  BufferedReader br = null;
  InputStreamReader ir = null;
  try {
    if ((charset == null) || (charset.trim().length() == 0)) charset = "UTF-8";
    char cbuf[] = new char[2048];
    int n = 0;
    int nLen = cbuf.length;
    ir = new InputStreamReader(stream,charset);
    br = new BufferedReader(ir);
    while ((n = br.read(cbuf,0,nLen)) > 0) sb.append(cbuf,0,n);
  } finally {
    try {if (br != null) br.close();} catch (Exception ef) {}
    try {if (ir != null) ir.close();} catch (Exception ef) {}
  }
  return sb.toString();
}

}