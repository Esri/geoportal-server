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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
public class AGPEntryProcessor implements IEntryProcessor{
	
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
			    String id = "";
			    try {
					id = parseId(mdDoc);
				} catch (XPathExpressionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			    String georssBox = readBbox(info,id);
			    mdText = mdText.replace("</entry>", georssBox + "</entry>");
	    } catch (Exception e) {
	  			// TODO Auto-generated catch block
	  			e.printStackTrace();
	    } 
	 	return mdText;
	}
		
	/**
	 * Parses Id from atom entry
	 * @param doc the feed
	 * @return the id
	 * @throws XPathExpressionException
	 */
	protected String parseId(Document doc) throws XPathExpressionException{
		XPath xPath = AtomNamespaceUtil.makeXPath(true);
	  	String id = (String) xPath.evaluate("/atom:entry/atom:id/text()", doc, XPathConstants.STRING);
	    if(id != null) return id;
		return null;
	}
	
	/**
	 * Makes http request to json endpoint and returns georss box string.
	 * @param info the base atom info
	 * @param id the item id
	 * @return the georss box string.
	 * @throws IOException if the http request fails due to i/o exception
	 * @throws JSONException if json parsing fails
	 */
	protected String readBbox(BaseAtomInfo info,String id) throws IOException, JSONException {
		String url = info.getUrl();
		String georssBox = "<georss:box>";
	  if(url.length() > 0){
		  url = url.substring(0,url.indexOf("?"));
		  String params = "&id="+id+"f=json";	
		  url = url + params;
	    HttpClientRequest cr = new HttpClientRequest();
	    cr.setUrl(url);
	    String response = Val.chkStr(cr.readResponseAsCharacters());
	    if(response.length() > 0){
	    	JSONObject jso = new JSONObject(response);
	        if(jso.has("results")){
	        	JSONArray results = jso.getJSONArray("results");
	        	for(int i=0; i < results.length(); i++){
	        		JSONObject result = results.getJSONObject(i);
	        		String idJso = result.getString("id");
	        		if(idJso != id) continue;
	        		JSONArray extentArray = result.getJSONArray("extent");
	        	    if (extentArray!=null && extentArray.length()==2) {
	        	      String [] lowerCorner = Val.chkStr(extentArray.getJSONArray(0).toString()).replaceAll("^\\[|\\]$", "").split(",");
	        	      String [] upperCorner = Val.chkStr(extentArray.getJSONArray(1).toString()).replaceAll("^\\[|\\]$", "").split(",");
	        	      double minx = -180, miny = -90, maxx = 180, maxy = 90;
	        	      if (lowerCorner!=null && lowerCorner.length==2) {
	        	        minx = Val.chkDbl(lowerCorner[0], minx);
	        	        miny = Val.chkDbl(lowerCorner[1], miny);
	        	      }
	        	      if (upperCorner!=null && upperCorner.length==2) {
	        	        maxx = Val.chkDbl(upperCorner[0], maxx);
	        	        maxy = Val.chkDbl(upperCorner[1], maxy);
	        	      }
	        	      georssBox += miny + " " + minx + " " + maxy + " " + maxx + "</georss:box>";
	        	    }
	        		
	        	 }
	        }
	    }
	  }
	  return georssBox;
	}
}
