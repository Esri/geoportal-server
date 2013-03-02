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
package com.esri.gpt.catalog.harvest.clients;

import com.esri.gpt.catalog.harvest.clients.exceptions.HRConnectionException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidResponseException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * CSW client.
 * Provides complete implementation of the client accessing CSW harvest 
 * repositories.
 * @see <a HREF="http://www.opengeospatial.org/standards/cat">
 * <b>C</b>atalog <b>S</b>ervice for <b>W</b>eb specification</a>
 */
public class HRCSWClient extends HRHttpClient {

// class variables =============================================================

// instance variables ==========================================================
/** host url */
private String _hostUrl = "";
/** profile id */
private String _profile = "";
// constructors ================================================================
/**
 * Creates instance of the client.
 * @param hostUrl host URL
 * @param profile profile
 */
public HRCSWClient(String hostUrl, String profile) {
  setHostUrl(hostUrl);
  setProfile(profile);
}
// properties ==================================================================
/**
 * Gets CSW profile.
 * @return CSW profile
 */
public String getProfile() {
  return _profile;
}

/**
 * Sets CSW profile.
 * @param profile profile
 */
public void setProfile(String profile) {
  _profile = Val.chkStr(profile);
}

/**
 * Gets host URL.
 * @return host URL
 */
@Override
public String getHostUrl() {
  return _hostUrl;
}

/**
 * Sets host URL.
 * @param hostUrl host URL
 */
public void setHostUrl(String hostUrl) {
  _hostUrl = Val.chkStr(hostUrl);
}
// methods =====================================================================
/**
 * Checks and verifies connection to the remote repository.
 * @throws HRInvalidProtocolException if provided connection
 * definition is incomplete
 * @throws HRConnectionException if connection to the remote repository can not
 * be established at this moment
 */
  @Override
public void ping()
  throws HRInvalidProtocolException, HRConnectionException {

  validateProtocol();

  InputStream input = null;

  try {
    input = openConnection(getPingString());
    analyzePingResponse(input);
  } finally {
    if (input != null) {
      try {
        input.close();
      } catch (IOException ignore) {
      }
    }
  }
}

/**
 * Returns a string representation of the object.
 * @return string representation of the object
 */
@Override
public String toString() {
  return "CSW " + super.toString() +
    " PROFILE:" + getProfile().toString();
}

/**
 * Analyzes response from the ping request.
 * @param response response from the server
 * @throws HRInvalidResponseException if response is invalid
 */
protected void analyzePingResponse(InputStream response)
  throws HRInvalidResponseException {
  try {
    
    InputSource source = new InputSource(response);
    Document document = DomUtil.makeDomFromSource(source, false);
    XPath xPath = XPathFactory.newInstance().newXPath();
    
    if ( ((Node) xPath.evaluate(
      "/Capabilities", document, XPathConstants.NODE))==null) {
      throw new HRInvalidResponseException(
        "Received response has no 'Capabilities' node.");
    }
    
    Node recordsNode = (Node) xPath.evaluate(
        "/Capabilities/OperationsMetadata/Operation[@name=\"GetRecords\"]", 
        document, XPathConstants.NODE);
    Node recordByIdNode = (Node) xPath.evaluate(
        "/Capabilities/OperationsMetadata/Operation[@name=\"GetRecordById\"]", 
        document, XPathConstants.NODE);

    if (recordsNode==null || recordByIdNode==null) {
      throw new HRInvalidResponseException(
        "Received response has either no 'GetRecords' node," +
        " or no 'GetRecordById' node.");
    }
  } catch (XPathExpressionException ex) {
    throw new HRInvalidResponseException("Error parsing response.", ex);
  } catch (SAXException ex) {
    throw new HRInvalidResponseException("Error parsing response.", ex);
  } catch (ParserConfigurationException ex) {
    throw new HRInvalidResponseException("Error parsing response.", ex);
  } catch (IOException ex) {
    throw new HRInvalidResponseException("Error parsing response.", ex);
  } finally {
    try {
      response.close();
    } catch (IOException ignore) {
    }
  }
}

/**
 * Gets connection string.
 * @return connection string
 */
private String getPingString() {
  String url = Val.chkStr(getHostUrl());

  // remove trailing / or \
  url = url.replaceAll("[/\\\\]\\p{Blank}*$", "");

  return url;
}
}
