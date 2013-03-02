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
package com.esri.gpt.catalog.arcims;

import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Test connection harvest request.
 */
public class TestConnectionRequest extends HarvestRequest {

// class variables =============================================================
/** catalog service name */
private static final String CATALOG_SERVICE_NAME = "catalog";
// instance variables ==========================================================
/** service name */
private String _serviceName = "";
// constructors ================================================================
/**
 * Creates instance of the request.
 * @param url host url
 * @param timeout connection timeout
 */
public TestConnectionRequest(
  String url, int timeout, String serviceName) {
  super();
  setService(url, timeout);
  setServiceName(serviceName);
}

/**
 * Creates instance of the request.
 * @param credentials credentials
 * @param url host url
 * @param timeout connection timeout
 */
public TestConnectionRequest(
  UsernamePasswordCredentials credentials, String url, int timeout,
  String serviceName) {
  super(credentials);
  setService(url, timeout);
  setServiceName(serviceName);
}
// properties ==================================================================

// methods =====================================================================
/**
 * Tests connection.
 */
public boolean testConnection()
  throws ImsServiceException {
  StringBuilder sb = new StringBuilder();
  sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  sb.append("<GETCLIENTSERVICES/>");

  setAxlRequest(sb.toString());

  executeRequest();

  if (wasActionOK()) {
    try {

      Document document =
        DomUtil.makeDomFromString(getAxlResponse(), false);

      XPath xPath = XPathFactory.newInstance().newXPath();
      Node nService = (Node) xPath.evaluate(
        "/ARCXML/RESPONSE/SERVICES/SERVICE[@NAME=\"" + getServiceName() + "\"]",
        document, XPathConstants.NODE);
      return nService != null;
      
    } catch (XPathExpressionException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (ParserConfigurationException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (SAXException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (IOException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    }
  } else {
    throw new ImsResponseException("Invalid response.");
  }
}

/**
 * Creates service.
 * @param url host url
 * @param timeout connection timeout
 */
private void setService(String url, int timeout) {
  ImsService service = new ImsService();
  service.setServiceName(CATALOG_SERVICE_NAME);
  service.setServerUrl(Val.chkStr(url));
  service.setTimeoutMillisecs(Math.max(0, timeout));
  setService(service);
}

/**
 * Gets service name.
 * @return service name
 */
private String getServiceName() {
  return _serviceName;
}

/**
 * Sets service name to look for in the response.
 * @param serviceName service name
 */
private void setServiceName(String serviceName) {
  _serviceName = Val.chkStr(serviceName);
}
}
