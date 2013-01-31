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
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.framework.xml.XsltTemplates;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Identifies resource and content type
 */
public class XsltResourceIdentifier extends ResourceIdentifier {

// class variables =============================================================
  
/** Cached templates */
private static XsltTemplates XSLTTEMPLATES = new XsltTemplates();

/** XSLT for hinting resource identification : "gpt/metadata/HintResourceIdentification.xslt" */
public static final String XSLT_HINT_RESOURCE_IDENTIFICATION 
                            = "gpt/metadata/HintResourceIdentification.xslt";

// instance variables ==========================================================

/** Default constructor. */
public XsltResourceIdentifier() {
}

// properties ==================================================================

// methods =====================================================================

/**
 * Guesses the ArcGIS Server SOAP endpoint for a MapServer server.
 * </br>Only MapServer services are considered.
 * @param server the server URL
 * @return the ArcGIS MapServer SOAP endpoint (without "?wsdl", null if unknown) 
 */
@Override
public String guessAgsMapServerSoapUrl(String server) {
  String strServer = Val.chkStr(server).toLowerCase();
  return Val.chkStr(guessFromXslt("getAgsMapServerSoapUrl", strServer));
}

/**
 * Guesses the ArcGIS Server rest endpoint for a service.
 * @param serviceUrl the service URL
 * @return the service's ArcGIS Server rest endpoint (null if unknown)
 */
@Override
public String guessAgsServiceRestUrl(String serviceUrl) {
  String strServer = Val.chkStr(serviceUrl).toLowerCase();
  return Val.chkStr(guessFromXslt("getAgsRestServerRootUrl",strServer));
}

/**
 * Guesses the ArcIMS content type based upon a supplied resource type.
 * @param resourceType the resource type
 * @return the ArcIMS content type (empty String if none)
 */
@Override
public String guessArcIMSContentTypeFromResourceType(String resourceType) {
  resourceType = Val.chkStr(resourceType).toLowerCase();
  return Val.chkStr(guessFromXslt("guessArcIMSContentTypeFromResourceType",
      resourceType));
}

/**
 * Guesses the ArcIMS content type based upon a given url.
 * @param url the resource url
 * @return the ArcIMS content type (empty String if none) 
 */
@Override
public String guessArcIMSContentTypeFromUrl(String url) {
  url = Val.chkStr(url).toLowerCase();
  return Val.chkStr(guessFromXslt("guessArcIMSContentTypeFromUrl", url));
}

/**
 * Guesses a service type from a URL.
 * 
 * @param url
 *          the url
 * @return the service type
 */
@Override
public String guessServiceTypeFromUrl(String url) {
  url = Val.chkStr(url).toLowerCase();
  return Val.chkStr(guessFromXslt("guessServiceTypeFromUrl", url));
}

/**
 * Gets a compiled XSLT template.
 * @param xsltPath the path to an XSLT
 * @return the compiled template
 * @throws IOException if an IO exception occurs
 * @throws TransformerException if a transformation exception occurs
 * @throws SAXException if a SAX parsing exception occurs
 */
private synchronized XsltTemplate getCompiledTemplate(String xsltPath)
  throws TransformerException {
  String sKey = xsltPath;
  XsltTemplate template = XSLTTEMPLATES.get(sKey);
  if (template == null) {
    template = XsltTemplate.makeTemplate(xsltPath);
    XSLTTEMPLATES.put(sKey,template);
  }
  return template;
}

/**
 * 
 * @param operation
 * @param input
 * @return Guess 
 */
private String guessFromXslt(String operation, String input) {

  try {
    XsltTemplate template = getCompiledTemplate(XSLT_HINT_RESOURCE_IDENTIFICATION);

    Document dom = makeDom();
    if (dom != null) {
      DOMSource source = new DOMSource();

      StringWriter writer = new StringWriter();
      StreamResult result = new StreamResult(writer);
      HashMap<String, String> params = new HashMap<String, String>();
      params.put("operation", operation);
      params.put("input", input);
      template.transform(source, result, params);

      Document outputDom = null;
      try {
        outputDom = DomUtil.makeDomFromString(Val.chkStr(writer.toString()),
            true);
      } catch (ParserConfigurationException e) {
        // TODO Auto-generated catch block
        // e.printStackTrace();
      } catch (SAXException e) {
        // TODO Auto-generated catch block
        // e.printStackTrace();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        // e.printStackTrace();
      }
      Node node = outputDom.getFirstChild();
      String s = node.getTextContent();
      return s;
    }

  } catch (TransformerConfigurationException e) {
    // TODO Auto-generated catch block
    // e.printStackTrace();
  } catch (TransformerException e) {
    // TODO Auto-generated catch block
    // e.printStackTrace();
  }
  return "";
}

/**
 * Builds document from xml string
 * 
 * @return dom
 */
private Document makeDom() {
  try {
    String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><dummyXml/>";
    return DomUtil.makeDomFromString(xml, false);
  } catch (ParserConfigurationException e) {
  } catch (SAXException e) {
  } catch (IOException e) {
  }
  return null;
}
}
