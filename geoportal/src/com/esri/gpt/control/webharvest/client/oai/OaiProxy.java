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
package com.esri.gpt.control.webharvest.client.oai;

import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.XmlHandler;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.NodeListAdapter;
import com.esri.gpt.framework.xml.XmlIoUtil;
import com.esri.gpt.server.csw.client.NullReferenceException;
import java.io.IOException;
import java.util.logging.Logger;
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
 * OAI service proxy.
 */
class OaiProxy {
/** logger */
private static final Logger LOGGER = Logger.getLogger(OaiProxy.class.getCanonicalName());
/** service info */
private OaiInfo info;

/**
 * Creates instance of the proxy.
 * @param info service info
 */
public OaiProxy(OaiInfo info) {
  if (info==null) throw new IllegalArgumentException("No info provided.");
  this.info = info;
}

public String read(String sourceUri) throws IOException {
  LOGGER.finer("Reading metadata of source URI: \"" +sourceUri+ "\" through proxy: "+this);
  try {
    sourceUri = Val.chkStr(sourceUri).replaceAll("\\{", "%7B").replaceAll("\\}", "%7D");
    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(info.newReadMetadataUrl(sourceUri));
    XmlHandler sh = new XmlHandler(false);
    cr.setContentHandler(sh);
    cr.setCredentialProvider(info.newCredentialProvider());
    cr.setBatchHttpClient(info.getBatchHttpClient());
    cr.execute();
    Document doc = sh.getDocument();
    XPath xPath = XPathFactory.newInstance().newXPath();
    Node node = (Node) xPath.evaluate(
        "/OAI-PMH/GetRecord/record/metadata",
        doc, XPathConstants.NODE);
    Node elementNode = null;
    if (node!=null) {
      for (Node nd : new NodeListAdapter(node.getChildNodes())) {
        if (nd.getNodeType()==Node.ELEMENT_NODE) {
          elementNode = nd;
          break;
        }
      }
    }
    if (elementNode==null) {
      LOGGER.finer("Received empty metadata of source URI: \"" +sourceUri+ "\" through proxy: "+this);
      return "";
    }
    Document mdDoc = DomUtil.newDocument();
    mdDoc.appendChild(mdDoc.importNode(elementNode, true));
    String mdText = XmlIoUtil.domToString(mdDoc);
    LOGGER.finer("Received metadata of source URI: \"" +sourceUri+ "\" through proxy: "+this);
    LOGGER.finest(mdText);
    return mdText;
  } catch (TransformerException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (SAXException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (ParserConfigurationException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  } catch (XPathExpressionException ex) {
    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
  }
}

@Override
public String toString() {
  return info.toString();
}
}
