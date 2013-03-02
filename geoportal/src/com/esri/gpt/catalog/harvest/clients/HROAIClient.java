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
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidResponseException;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * OAI client.
 * Provides somplete implementation of the client capable to access OAI harvest 
 * respositories.
 * @see <a HREF="http://www.openarchives.org/">
 * <b>O</b>pen <b>A</b>rchives <b>I</b>nitiative</a>
 */
public class HROAIClient extends HRHttpClient {

// class variables =============================================================
/** Service string. */
private static final String SERVICESTRING = "?verb=Identify";

// instance variables ==========================================================
/** host URL */
private String _hostUrl = "";
/** prefix */
private String _prefix = "";
/** set */
private String _set = "";
// constructors ================================================================
/**
 * Creates instance of the client.
 * @param hostUrl host url
 * @param prefix prefix
 * @param set set
 */
public HROAIClient(String hostUrl, String prefix, String set) {
  setHostUrl(hostUrl);
  setPrefix(prefix);
  setSet(set);
}
// properties ==================================================================

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

/**
 * Gets prefix.
 * @return prefix
 */
public String getPrefix() {
  return _prefix;
}

/**
 * Sets prefix.
 * @param prefix prefix
 */
public void setPrefix(String prefix) {
  _prefix = Val.chkStr(prefix);
}

/**
 * Gets set.
 * @return set
 */
public String getSet() {
  return _set;
}

/**
 * Sets set.
 * @param set set
 */
public void setSet(String set) {
  _set = Val.chkStr(set);
}
// methods =====================================================================
/**
 * Pings the server.
 * @throws HRInvalidProtocolException if repository reference is invalid
 * @throws HRConnectionException if unable to connect to the repository
 */
@Override
public void ping()
  throws HRInvalidProtocolException, HRConnectionException {

  validateProtocol();
  ping(createPingString());
}

/**
 * Returns a string representation of the object.
 * @return string representation of the object
 */
@Override
public String toString() {
  return "OAI " + super.toString() +
    " PREFIX:" + getPrefix() +
    " SET:" + getSet();
}

/**
 * Validates protocol definition.
 * @throws HRInvalidProtocolException if invalid protocol definition
 */
@Override
protected void validateProtocol()
  throws HRInvalidProtocolException {
  super.validateProtocol();
  if (getPrefix().length() == 0) {
    throw new HRInvalidProtocolException(
      HRInvalidProtocolException.ProtocolElement.prefix,
      "Empty metadata prefix.");
  }
  if (getSet().length() == 0) {
    throw new HRInvalidProtocolException(
      HRInvalidProtocolException.ProtocolElement.set,
      "Empty set.");
  }
}

/**
 * Pings the server.
 * @param pingString ping string
 * @throws HRInvalidProtocolException if repository reference is invalid
 * @throws HRConnectionException if unable to connect to the repository
 */
private void ping(String pingString)
  throws HRInvalidProtocolException, HRConnectionException {

  InputStream input = null;

  try {

    input = openConnection(pingString);
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
 * Analyzes response from the ping request.
 * @param response response from the server
 * @throws HRInvalidResponseException if response is invalid
 */
private void analyzePingResponse(InputStream response)
  throws HRInvalidResponseException {

  try {

    InputSource source = new InputSource(response);

    Document document = DomUtil.makeDomFromSource(source, false);

    XPath xPath = XPathFactory.newInstance().newXPath();
    if (((Node) xPath.evaluate(
      "/OAI-PMH/Identify", document, XPathConstants.NODE)) == null) {
      throw new HRInvalidResponseException("Not OAI response.");
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
 * Creates ping string.
 * @return string used to perfotm ping connection
 */
private String createPingString() {
  return getHostUrl() + SERVICESTRING;
}
}
