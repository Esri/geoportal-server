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
import gc.base.xml.DomUtil;

import java.util.Collection;
import java.util.LinkedHashMap;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * A collection of XML types.
 */
public class XmlTypes {
	
	/** Instance variables. */
	private LinkedHashMap<String,XmlType> members = new LinkedHashMap<String,XmlType>();
	
  /** Default constructor. */
  public XmlTypes() {}
  
  /**
   * Adds a type.
   * @param xmlType the type
   */
  public void add(XmlType xmlType) {
  	members.put(xmlType.getKey(),xmlType);
  }
 
  /**
   * Gets the types.
   * @return the types
   */
  public Collection<XmlType> values() {
  	return members.values();
  }
  
  public static XmlTypes createFromConfig() throws Exception {
		String configPath = "gc-config/xmltypes.xml";
		Document dom = DomUtil.makeDomFromResourcePath(configPath,true);
		return XmlTypes.createFromConfigDom(dom);
	}
	
  public static XmlTypes createFromConfigDom(Document dom) 
  		throws XPathExpressionException {
  	XmlTypes types = new XmlTypes();
  	XPath xpath = XPathFactory.newInstance().newXPath();
	  NodeList nlT = (NodeList)xpath.evaluate("//property[@name='xmltype']",dom,XPathConstants.NODESET);
	  for (int i=0; i<nlT.getLength();i++) {
	  	XmlType type = XmlType.createFromConfigNode(nlT.item(i));
	  	if (type != null) types.add(type);
	  }
	  return types;
  }
  
}
