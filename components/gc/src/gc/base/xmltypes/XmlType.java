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
package gc.base.xmltypes;
import gc.base.util.ValueUtil;
import gc.base.xml.XsltReference;
import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An XML type (e.g. metadata standard).
 */
public class XmlType {
	
  //private Properties properties;
  
  /** Instance variables. */
  private XmlInterrogationInfo interrogationInfo = new XmlInterrogationInfo();
  private String               identifier;
  private String               key;
  private List<XsltReference>  xsltReferences = new ArrayList<XsltReference>();
  
	/** Default constructor. */
  public XmlType() {}

  /**
   * Gets the interrogation information.
   * @return the interrogation information
   */
	public XmlInterrogationInfo getInterrogationInfo() {
		return this.interrogationInfo;
	}
	/**
	 * Sets the interrogation information.
	 * @param interrogationInfo the interrogation information
	 */
	public void setInterrogationInfo(XmlInterrogationInfo interrogationInfo) {
		this.interrogationInfo = interrogationInfo;
	}
	
  /**
   * Gets the identifier.
   * @return the identifier
   */
	public String getIdentifier() {
		return this.identifier;
	}
	/**
	 * Sets the identifier.
	 * @param identifier the identifier
	 */
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
  /**
   * Gets the key.
   * @return the key
   */
  public String getKey() {
		return this.key;
	}
	/**
	 * Sets the key.
	 * @param key the key
	 */
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Gets the XSLT reference used to create Solr indexable fields.
	 * @return the XSLT reference
	 */
	public XsltReference getToSolrXslt() {
		for (XsltReference ref: xsltReferences) {
			String p = ref.getPurpose();
			if ((p != null) && p.equalsIgnoreCase("metadataToSolr")) {
				return ref;
			}
		}
		return null;
	}
	
	/**
	 * Gets the XSLT references.
	 * @return the XSLT references
	 */
	public List<XsltReference> getXsltReferences() {
		return this.xsltReferences;
	}
	
	public static XmlType createFromConfigNode(Node xmltypeNode) 
			throws XPathExpressionException {
  	XmlType type = new XmlType();
  	XPath xpath = XPathFactory.newInstance().newXPath();
  	Node ndT = xmltypeNode;
  	
    type.setKey(ValueUtil.trim(
    		xpath.evaluate("property[@name='key']/@value",ndT)));
    type.setIdentifier(ValueUtil.trim(
    		xpath.evaluate("property[@name='identifier']/@value",ndT)));
    //System.err.println(type.getKey());
    //System.err.println(type.getIdentifier());
    
    Node ndI = (Node)xpath.evaluate("property[@name='interrogation']",ndT,XPathConstants.NODE);
    if (ndI != null) {
    	XmlInterrogationInfo interrogation = type.getInterrogationInfo();
      NodeList nlN = (NodeList)xpath.evaluate("property[@name='namespace']",ndI,XPathConstants.NODESET);
      for (int iN=0; iN<nlN.getLength();iN++) {
  	    Node ndN = nlN.item(iN);
  	    String pfx = xpath.evaluate("property[@name='prefix']/@value",ndN);
  	    String uri = xpath.evaluate("property[@name='uri']/@value",ndN);
  	    interrogation.getNamespaces().add(ValueUtil.trim(pfx),ValueUtil.trim(uri));
      }
      //for (XmlNamespace ns: interrogation.getNamespaces().values()) {
      //	System.err.println(ns.getPrefix()+":"+ns.getUri());
      //}
      String xp = xpath.evaluate("property[@name='xpath.count']/@value",ndI);
      interrogation.setCountExpression(ValueUtil.trim(xp));
      //System.err.println(interrogation.getCountExpression());
    }
    
    NodeList nlX = (NodeList)xpath.evaluate("property[@name='xslt.reference']",ndT,XPathConstants.NODESET);
    for (int iX=0; iX<nlX.getLength();iX++) {
	    Node ndX = nlX.item(iX);
	    String purpose = xpath.evaluate("property[@name='purpose']/@value",ndX);
	    String src = xpath.evaluate("property[@name='src']/@value",ndX);
	    String version = xpath.evaluate("property[@name='version']/@value",ndX);
	    XsltReference ref = new XsltReference(
	    		ValueUtil.trim(purpose),ValueUtil.trim(src),ValueUtil.trim(version));
	    type.getXsltReferences().add(ref);
    }
    /*
    for (XsltReference ref: type.getXsltReferences()) {
    	System.err.println(ref.getPurpose());
    	System.err.println(ref.getSrc());
    	System.err.println(ref.getVersion());
    }
    */
    return type;
	}
  
}
