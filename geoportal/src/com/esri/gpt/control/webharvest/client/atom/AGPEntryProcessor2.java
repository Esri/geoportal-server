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

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.esri.gpt.framework.geometry.Envelope;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.XmlHandler;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.NodeListAdapter;
import com.esri.gpt.framework.xml.XmlIoUtil;

/**
 * Implements IEntryProcessor.extractMetadata() to process entry node
 * and extract metadata xml.
 */
public class AGPEntryProcessor2 extends AGPEntryProcessor{

	/**
	 * Extract xml of entry node and returns xml string.
	 * @param info the BaseAtomInfo object
	 * @param entry the entry node
	 * @return the metadata xml string
	 */
	@Override
	public String extractMetadata(BaseAtomInfo info,Node entry) {
		String mdText = "";
	 	 try { 	       			   
			    XPath xPath = AtomNamespaceUtil.makeXPath(true);			
					String href = Val.chkStr((String) xPath.evaluate(
				            "atom:link/@href",
				            entry, XPathConstants.STRING),-1);
					
					if(href.length() > 0 && href.contains("/home/item.html?id=")){
						href = href.replace("/home/item.html?id=", "/sharing/content/items/");
						href += "/info/metadata/metadata.xml";
						mdText = read(info,href);
					}else if(href.length() > 0 && href.contains("/item.html?id=")){
						String[] parts = href.split("/item.html?id=");
						if(parts != null && parts.length == 2){
							int idx = parts[0].lastIndexOf("//");
							parts[0] = parts[0].substring(0,idx);
							href = parts[0] + "/sharing/content/items/" + parts[1] + "/info/metadata/metadata.xml";
							mdText = read(info,href);
						}
					}
					if(mdText.length() ==0){
					 Document mdDoc = DomUtil.newDocument();
					 mdDoc.appendChild(mdDoc.importNode(entry, true));     	
					 mdText =  XmlIoUtil.domToString(mdDoc);
					 String id = "";
					    try {
							id = parseId(mdDoc);
						} catch (XPathExpressionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					    
					    String georssBox = readBbox(info,id);
					    mdText = mdText.replace("</entry>", georssBox + "</entry>");
				  }					
	    } catch (Exception e) {
	  			// TODO Auto-generated catch block
	  			e.printStackTrace();
	    } 
	 	return mdText;
	}

	/**
	 * Makes http request to url and returns xml string.
	 * @param info the base atom info
	 * @param url the metadata xml url
	 * @return the metadata xml string.
	 * @throws IOException if the http request fails due to i/o exception
	 */
	private String read(BaseAtomInfo info,String url) throws IOException {
	  try {
	    HttpClientRequest cr = new HttpClientRequest();
	    cr.setUrl(url);
	    XmlHandler sh = new XmlHandler(false);
	    cr.setContentHandler(sh);
	    cr.setCredentialProvider(info.newCredentialProvider());
	    cr.setBatchHttpClient(info.getBatchHttpClient());
	    try {
				cr.execute();
			} catch (IOException e) {
				return "";
			}	    
	    Document doc = sh.getDocument();	    
	    String mdText = XmlIoUtil.domToString(doc);
	    return mdText;
	  } catch (TransformerException ex) {
	    throw new IOException("Error accessing metadata. Cause: "+ex.getMessage());
	  }
	}	
}
