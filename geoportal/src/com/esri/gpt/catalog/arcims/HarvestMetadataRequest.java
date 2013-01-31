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
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Harvest metadata request.
 */
public class HarvestMetadataRequest extends HarvestRequest {

// class variables =============================================================

// instance variables ==========================================================
/** stored metadata */  
private String _metadata;
// constructors ================================================================
/**
 * Creates instance of the request.
 */  
public HarvestMetadataRequest() {
  super();
}

/**
 * Creates instance of the request.
 * @param credentials credentials
 */
public HarvestMetadataRequest(UsernamePasswordCredentials credentials) {
  super(credentials);
}
// properties ==================================================================
/**
 * Gets metadata.
 * @return metadata text or <code>null</code> if metadata not available
 */
public String getMetadata() {
  return _metadata;
}
// methods =====================================================================

/**
 * Harvests single metadata.
 * @param sourceUri metadata URI
 * @throws ImsServiceException if harvest failed
 */
public void executeHarvest(String sourceUri) 
  throws ImsServiceException {

  _metadata = null;
  
  StringBuilder sb = new StringBuilder();
  sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  sb.append("\r\n<ARCXML version=\"1.1\">");
  sb.append("\r\n<REQUEST>");
  sb.append("\r\n<GET_METADATA>");
  sb.append("\r\n<GET_METADATA_DOCUMENT docid=\"");
  sb.append(sourceUri);
  sb.append("\"/>");
  sb.append("\r\n</GET_METADATA>");
  sb.append("\r\n</REQUEST>");
  sb.append("\r\n</ARCXML>");

  setAxlRequest(sb.toString());

  executeRequest();

  if (wasActionOK()) {
    try {
      Document document =
        DomUtil.makeDomFromString(getAxlResponse(), false);

      XPath xPath = XPathFactory.newInstance().newXPath();
      Node node = (Node) xPath.evaluate(
        "/ARCXML/RESPONSE/METADATA", document, XPathConstants.NODE);
      
      // search for metadata nodes
      if (node!=null) {
        // extract metadata
        Document mdDoc = DomUtil.newDocument();
        mdDoc.appendChild(mdDoc.importNode(node, true));
        String mdText = XmlIoUtil.domToString(mdDoc);
        
        _metadata = mdText;
      } else {
        throw new ImsResponseException("Metadata not available.");
      }

    } catch (TransformerException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (XPathExpressionException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (ParserConfigurationException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (SAXException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    } catch (IOException ex) {
      throw new ImsResponseException("Response parse error.", ex);
    }
  }
}
}
