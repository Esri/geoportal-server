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
package com.esri.gpt.control.webharvest.client.atom;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.esri.gpt.catalog.schema.NamespaceContextImpl;
import com.esri.gpt.catalog.schema.Namespaces;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.XmlHandler;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.CommonPublishable;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.util.ResourceXml;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.NodeListAdapter;
import com.esri.gpt.framework.xml.XmlIoUtil;

/**
 * Atom feed proxy.
 */
class AtomProxy {
/** logger */
private static final Logger LOGGER = Logger.getLogger(AtomProxy.class.getCanonicalName());
/** service info */
private BaseAtomInfo info;

/**
 * Creates instance of the proxy.
 * @param info service info
 */
public AtomProxy(BaseAtomInfo info) {
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
        "/feed/entry",
        doc, XPathConstants.NODE);
    Node elementNode = null;
    for (Node nd : new NodeListAdapter(node.getChildNodes())) {
      if (nd.getNodeType()==Node.ELEMENT_NODE) {
        elementNode = nd;
        break;
      }
    }
    if (elementNode==null) {
      throw new IOException("Error extracting metadata from <metadata> node.");
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

/**
 * Gets native resource.
 * @return native resource
 */
public Native getNativeResource() {
  return new NativeImpl();
}
/**
 * Native implementation.
 */
private class NativeImpl extends CommonPublishable implements Native {
	
    @Override
  public SourceUri getSourceUri() {
    return new UrlUri(info.getUrl());
  }

    @Override
  public String getContent() throws IOException, SAXException {
    ResourceXml resourceXml = new ResourceXml();
    String feedXml = resourceXml.makeResourceXmlFromResponse(info.getUrl().replaceAll("\\{", "'").replaceAll("\\}","'"));
    Document fDom = null;
	try {
		fDom = DomUtil.makeDomFromString(feedXml, true);
	} catch (ParserConfigurationException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	XPath xPath = AtomNamespaceUtil.makeXPath(true);
    NodeList entries = null;
    Node feedNd = null;
	try {
		feedNd = (Node) xPath.evaluate("/atom:feed", fDom, XPathConstants.NODE);
		entries = (NodeList) xPath.evaluate("/atom:feed/atom:entry", fDom, XPathConstants.NODESET);		
	} catch (XPathExpressionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    for (Node entry : new NodeListAdapter(entries)) {
    	feedNd.removeChild(entry);
    }
    try {
    	String xml = Val.chkStr(XmlIoUtil.domToString(fDom));
    	if(xml.length() > 0){
    		int endTagStartIdx = xml.lastIndexOf("</");
    		int endTagEndIdx = xml.lastIndexOf(">");
    		String lastTag = xml.substring(endTagStartIdx,endTagEndIdx+1);
    		String[] parts = xml.split(lastTag);
    		if(parts != null && parts.length > 0){
    			parts[0] = parts[0].trim();
    		}
    		xml = parts[0] + "\n" + lastTag;
    	}
    	
		return xml;
	} catch (TransformerException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	return feedXml;
  }
}
}
