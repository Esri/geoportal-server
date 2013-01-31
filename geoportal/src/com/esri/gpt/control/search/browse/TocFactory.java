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
package com.esri.gpt.control.search.browse;

import java.io.IOException;
import java.util.logging.Level;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

/**
 * Provides a factory for instantiating toc component objects.
 */
public class TocFactory {

  // class variables =============================================================
    
  // instance variables ==========================================================
    
  // constructors ================================================================
  
  /** Default constructor. */
  public TocFactory() {}
  
  // properties ==================================================================
  
  // methods =====================================================================
  	  
  /**
   * Loads the tocs contained within a file.
   * @param cgfContext the configuration connects
   * @param TocCollection the categories collection to populate
   * @param relativePath the relative path to the toc file
   * @throws ParserConfigurationException if configuration exception occurs
   * @throws SAXException if an exception during xml parsing
   * @throws IOException if an i/o exception occurs
   */
  private void loadTocsFile(TocCollection tocs, String relativePath)
    throws ParserConfigurationException, SAXException, IOException {
	 
    Document dom = DomUtil.makeDomFromResourcePath(relativePath,false);
    Node[] tocNodes = null;
    Node ndContainer = DomUtil.findFirst(dom,"treeCollection");
	if (ndContainer != null) {
		tocNodes = DomUtil.findChildren(ndContainer,"tree");
	} else {
		tocNodes = DomUtil.findChildren(dom,"tree");
	}
	
	if (tocNodes != null) {
	  for (Node ndToc: tocNodes) {
	    NamedNodeMap nnmToc = ndToc.getAttributes();
	    String sFileName = Val.chkStr(DomUtil.getAttributeValue(nnmToc,"fileName"));
	    String sKey = Val.chkStr(DomUtil.getAttributeValue(nnmToc,"key"));
	    if (sFileName.length() > 0) {	  
	      try {
	        tocs.put(sKey, sFileName);
	      } catch (Exception e) {
	        String sMsg = "Error loading browse tocs: "+relativePath+" , @key="+sKey;
	            LogUtil.getLogger().log(Level.SEVERE,sMsg,e);
	          }	          
	        }
	      }
	    }
  }
    
  /**
   * Loads all tocs from the gpt/browse/browse.xml configuration file.
   * @throws ParserConfigurationException if configuration exception occurs
   * @throws SAXException if an exception during xml parsing
   * @throws IOException if an i/o exception occurs
   */
  public TocCollection loadTocConfig() 
    throws ParserConfigurationException, SAXException, IOException {
    TocCollection tocs = new TocCollection();
    loadTocsFile(tocs,"gpt/search/browse/browse.xml");    
    return tocs;
  }

}
