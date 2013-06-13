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

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.XmlHandler;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.NodeListAdapter;
import com.esri.gpt.framework.xml.XmlIoUtil;

/**
 * This class is a simple implementation of entry processor.
 * It fetch entry node xml.
 * @author prat5814
 *
 */
public class SimpleEntryProcessor implements IEntryProcessor{

	/**
	 * Extracts xml of entry node and returns xml string.
	 * @param info the BaseAtomInfo object
	 * @param entry the entry node
	 * @return the metadata xml string
	 */
	@Override
	public String extractMetadata(BaseAtomInfo info,Node entry) {
		String mdText = "";
	 	 try { 
	        Document mdDoc = DomUtil.newDocument();
			    mdDoc.appendChild(mdDoc.importNode(entry, true));     	
			    mdText =  XmlIoUtil.domToString(mdDoc);
	    } catch (Exception e) {
	  			// TODO Auto-generated catch block
	  			e.printStackTrace();
	    } 
	 	return mdText;
	}

	/**
	 * Makes http request to feed url and returns xml string.
	 * @param info the base atom info
	 * @param url the metadata xml url
	 * @return the metadata xml string.
	 * @throws IOException if the http request fails due to i/o exception
	 */
	private String read(BaseAtomInfo info, String sourceUri) throws IOException {
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
}
