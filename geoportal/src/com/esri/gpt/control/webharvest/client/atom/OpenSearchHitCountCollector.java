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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.esri.gpt.framework.util.ResourceXml;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

/**
 * This class is used retrieve hit count for an open search based pagination.
 *
 */
public class OpenSearchHitCountCollector implements IHitCountCollector{

	/**
	 * Retrieves hit count from feed xml.
	 * @param source source
	 * @return hit count.
	 */
	@Override
	public int collectHitCount(Object source) throws Exception {
		String url = Val.chkStr((String) source);
		if(url.length() > 0){
			ResourceXml resourceXml = new ResourceXml();
			String feedXml = "";
			Document fDom = null;
			try {
				feedXml = resourceXml.makeResourceXmlFromResponse(url);
				fDom = DomUtil.makeDomFromString(feedXml, true);
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			XPath xPath = AtomNamespaceUtil.makeXPath(true);
			Node feedNd = null;
			feedNd = (Node) xPath.evaluate("/atom:feed", fDom, XPathConstants.NODE);
 
			int totalResults = -1;
			if(feedNd != null){
				try{
					totalResults =Val.chkInt((String) xPath.evaluate(
				            "/atom:feed/opensearch:totalResults/text()",
				            feedNd, XPathConstants.STRING),-1);
				}catch (XPathExpressionException xpe){
					return totalResults;
				}
			
			}
			return totalResults;
	}
		return -1;
 }
}
