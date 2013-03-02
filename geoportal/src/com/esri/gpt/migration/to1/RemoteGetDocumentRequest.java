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
package com.esri.gpt.migration.to1;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.sql.Timestamp;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.http.StringProvider;
import com.esri.gpt.framework.http.HttpClientRequest.MethodName;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XmlIoUtil;

/**
 * Retrieves a document executing a request against an ArcIMS metadata publish service.
 */
public class RemoteGetDocumentRequest extends DefaultHandler {

// class variables =============================================================

// instance variables ==========================================================
private String    _thumbnailUrl = "";
private Timestamp _updateDate = null;
private String    _uuid = "";
private String    _xml = "";
private String    _xmlUrl = "";
private String    _userName = "";
private String    _password = "";
private String    _serviceUrl = "";

private StringBuffer  _characters = new StringBuffer();

// constructors ================================================================
/**
 * Constructor
 * @param serviceUrl the arcims metadata service http endpoint
 * @param userName the arcims metadata service username
 * @param password the arcims metadata service password
 */
public RemoteGetDocumentRequest(String serviceUrl, String userName, String password) {
	_userName = userName;
	_password = password;
	_serviceUrl = serviceUrl;
}

// properties ==================================================================

/**
 * Gets the url to the thumbnail image.
 * @return the thumbnail url
 */
private String getThumbnailUrl() {
  return _thumbnailUrl;
}

/**
 * Sets the url to the thumbnail image.
 * @param url the thumbnail url
 */
private void setThumbnailUrl(String url) {
  _thumbnailUrl = Val.chkStr(url);
}

/**
 * Gets the update date.
 * @return the update date
 */
public Timestamp getUpdateDate() {
  return _updateDate;
}

/**
 * Sets the update date.
 * @param updateDate the update date
 */
private void setUpdateDate(String updateDate) {
  updateDate = Val.chkStr(updateDate);
  if (updateDate.length() == 0) {
    _updateDate = null;
  } else {
    try {
      _updateDate = Timestamp.valueOf(updateDate);
    } catch (Exception e) {
      _updateDate = null;
      System.err.println("Error setting update date: " + updateDate);
      e.printStackTrace(System.err);
    }
  }
}

/**
 * Sets the update date.
 * @param updateDate the update date
 */
protected void setUpdateTimestamp(Timestamp updateDate) {
  _updateDate = updateDate;
}

/**
 * Gets the document uuid.
 * @return the document uuid
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets the document uuid
 * @param uuid the document uuid
 */
private void setUuid(String uuid) {
  _uuid = UuidUtil.addCurlies(uuid);
}

/**
 * Gets the url to the xml document.
 * @return the url to the xml document
 */
private String getXmlUrl() {
  return _xmlUrl;
}

/**
 * Sets the url to the xml document.
 * @param url the xml document url
 */
private void setXmlUrl(String url) {
  _xmlUrl = Val.chkStr(url);
}

/**
 * Gets the document xml.
 * @return the document xml
 */
public String getXml() {
  return _xml;
}

/**
 * Sets the document xml.
 * @param xml the document xml
 */
protected void setXml(String xml) {
  _xml = Val.chkStr(xml);
}

// methods =====================================================================
/**
 * Executes a GET_METADATA_DOCUMENT request against an ArcIMS metadata publish service.
 * @param docUuid the metatata document uuid of the record to load
 * @throws Exception 
 * @throws PublishServiceException if an exception occurs
 */
public void executeGet(String docUuid)
  throws Exception {
  reset();
  setUuid(docUuid);
 
  // make the axl request
  StringBuffer sbAxl = new StringBuffer();
  sbAxl = new StringBuffer();
  sbAxl.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  sbAxl.append("\r\n<ARCXML version=\"1.1\">");
  sbAxl.append("\r\n<REQUEST>");
  sbAxl.append("\r\n<GET_METADATA>");
  sbAxl.append("\r\n<GET_METADATA_DOCUMENT docid=\"").append(getUuid()).append("\"/>");
  sbAxl.append("\r\n</GET_METADATA>");
  sbAxl.append("\r\n</REQUEST>");
  sbAxl.append("\r\n</ARCXML>");
 // setAxlRequest(sbAxl.toString());

  // execute the request, read the XML file
  // UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(_userName, _password);
  CredentialProvider provider = new CredentialProvider(_userName, _password);
  executeRequest(provider,_serviceUrl,sbAxl.toString());
 /* if (wasActionOK()) {
    if (getXmlUrl().length() > 0) {
      setXml(XmlIoUtil.readXml(getXmlUrl()));
    }
  }*/
}

/**
 * Executes an ArcIMS service request and parses the response.
 * @throws Exception 
 */
protected void executeRequest(CredentialProvider provider, String serviceUrl,String requestBody) throws Exception {
 // setActionStatus(ACTION_STATUS_NONE);
 // setAxlResponse("");
  String sMsg;
  String sErrPfx = "ArcIMS Service Communication Error: ";
  HttpClientRequest httpClient = new HttpClientRequest();
  // send the request
  try {	 
	
    httpClient.setCredentialProvider(provider);
    httpClient.setUrl(serviceUrl);
   // httpClient.setTimeoutMillisecs(service.getTimeoutMillisecs());
    httpClient.setMethodName(MethodName.POST);
 //   httpClient.setAxlRequest(getAxlRequest());
    StringProvider cprov = new StringProvider(requestBody,"text/xml");
    httpClient.setContentProvider(cprov);
    StringHandler sh = new StringHandler();
    httpClient.setContentHandler(sh);
  
    httpClient.execute();
    
 //   setAxlResponse(httpClient.getAxlResponse());
    int nHttpResponseCode = httpClient.getResponseInfo().getResponseCode();
    if ((nHttpResponseCode < 200) || (nHttpResponseCode > 299)) {
      throw new IOException("Request failed: HTTP "+nHttpResponseCode);
    }
  } catch (MalformedURLException em) {
  //  setActionStatus(ACTION_STATUS_ERROR);
    sMsg = sErrPfx+"The PublishServer was configured with a malformed URL";
    throw new ImsServiceException(sMsg,em);
  } catch (UnknownHostException eu) {
 //   setActionStatus(ACTION_STATUS_ERROR);
    sMsg = sErrPfx+"The PublishServer was configured with an unknown host";
    throw new ImsServiceException(sMsg,eu);
  } catch (Exception e) {
 //   setActionStatus(ACTION_STATUS_ERROR);
    int nHttpResponseCode = httpClient.getResponseInfo().getResponseCode();
    if (nHttpResponseCode == 0) {
      sMsg = Val.chkStr(e.getMessage());
      sMsg = sErrPfx+sMsg;
      throw new ImsServiceException(sMsg,e);
    } else {
      sMsg = sErrPfx+"[HTTP "+nHttpResponseCode+"] "+httpClient.getResponseInfo().getResponseMessage();
      throw new ImsServiceException(sMsg,e);
    }
  }
  
  String response = httpClient.readResponseAsCharacters();
  
  // parse the response
  if (response.length() == 0) {
    throw new Exception(sErrPfx+"Empty response.");
  } else {
	  /*int start = response.indexOf("<!--");
	  int end = response.indexOf("-->");
	  response = response.substring(start,end);	*/  
      parseAxlResponse(response);
      if (getXmlUrl().length() > 0) {
          setXml(XmlIoUtil.readXml(getXmlUrl()));
      }
  }
}


/**
 * Starts a SAX parser on an ArcIMS axl response.
 * @param axlResponse the ArcIMS axl response
 * @throws ParserConfigurationException if the exception occurs
 * @throws SAXException if the exception occurs
 * @throws IOException if the exception occurs
 */
protected void parseAxlResponse(String axlResponse)
  throws ParserConfigurationException, SAXException, IOException {
  SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
  InputSource src = new InputSource(new StringReader(axlResponse));
  parser.parse(src,this);
}

/**
 * Triggered when a SAX element is started during the parsing of an axl response.
 * @param lowerCaseTagName the lower-case tag name of the element
 * @param attributes the element attributes
 */

protected void onStartSaxElement(String lowerCaseTagName, Attributes attributes) {
  if (lowerCaseTagName.equals("metadata_dataset")) {
    setXmlUrl(attributes.getValue("url"));
    setThumbnailUrl(attributes.getValue("thumbnail"));
    setUpdateDate(attributes.getValue("updated"));
  }
}

/**
 * Resets the request.
 */
public void reset() {
  setUuid("");
  setXml("");
  setXmlUrl("");
  setThumbnailUrl("");
  setUpdateDate(null);
}

/**
 * Sets text node characters for a SAX element.
 * @param ch the array of characters
 * @param start the starting position within the array
 * @param length the number of characters to read from the array
 * @throws SAXException if the exception occurs
 */
@Override
public void characters(char ch[], int start, int length)
  throws SAXException {
  if ((ch != null) && (length > 0)) _characters.append(ch,start,length);
}

/**
 * Ends a SAX element.
 * <br/>The parser is not namespace aware.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @throws SAXException if a SAXException occurs
 */
@Override
public void endElement(String uri, String localName, String qName)
  throws SAXException {
  String sTag        = returnLowerCaseTag(uri,localName,qName);
  String sCharacters = _characters.toString().trim();
  
  // check for errors
  if (sTag.equals("error")) {
    throw new SAXException(sCharacters);
  } else if (sCharacters.startsWith("[ERR")) {    
    throw new SAXException(sCharacters);
  }

  // trigger end element, reset characters
   onEndSaxElement(sTag,sCharacters);
 
  _characters = new StringBuffer();
}

/**
 * Triggered when a SAX element is ended during the parsing of an axl response.
 * @param lowerCaseTagName the lower-case tag name of the element
 * @param characters the text node value of the element
 */
protected void onEndSaxElement(String lowerCaseTagName, String characters) {
  if (lowerCaseTagName.equals("uuid")) {
    setUuid(characters);
  }
}

/**
 * Returns the lower case tag name for an element.
 * <br/>The parser is not namespace aware.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @return the lower case tag name
 */
private String returnLowerCaseTag(String uri, String localName, String qName) {
  if (qName == null) {
    return "";
  } else {
    return qName.trim().toLowerCase();
  }
}

/**
 * Starts a SAX element.
 * <br/>The parser is not namespace aware.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @param attributes the attributes for the element
 * @throws SAXException if a SAXException occurs
 */
@Override
public void startElement(String uri,
                         String localName,
                         String qName,
                         Attributes attributes)
  throws SAXException {
  
  // reset characters, trigger start element
  _characters = new StringBuffer();
  String sTag = returnLowerCaseTag(uri,localName,qName);
  onStartSaxElement(sTag,attributes);
}
}

