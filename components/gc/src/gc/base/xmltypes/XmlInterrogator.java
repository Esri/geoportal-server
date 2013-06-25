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
import gc.base.xml.XmlNamespaceContext;
import gc.base.xml.XmlNamespaces;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

/**
 * Interrogates an XML document to determine the XML type 
 * (i.e. metadata standard)
 */
public class XmlInterrogator {

  /** Default constructor. */
  public XmlInterrogator() {}
  
  /**
   * Evaluates the count expression for an XML type.
   * @param type the XML type
   * @param dom the XML document
   * @return the count
   * @throws XPathExpressionException if the exception occurs
   */
  public int evaluateCount(XmlType type, Document dom) 
  		throws XPathExpressionException {
  	XmlInterrogationInfo info = type.getInterrogationInfo();
  	if (info != null) {
	  	String countExpression = info.getCountExpression();
	    if ((countExpression != null)  && (countExpression.length() > 0)) {
	    	XPath xpath = XPathFactory.newInstance().newXPath();
	    	XmlNamespaces ns = info.getNamespaces();
	      XmlNamespaceContext nc = new XmlNamespaceContext(ns);
	      xpath.setNamespaceContext(nc);
	      String sCount = xpath.evaluate(countExpression,dom);
			  try {
			  	Integer nCount = Integer.valueOf(sCount);
			  	return nCount;
			  } catch (NumberFormatException nfe) {}
	    }
  	}
    return 0;
  }

  /**
   * Interrogates an XML document to determine the XML type.
   * @param types the configured XML types
   * @param dom the XML document
   * @return the XML type
   * @throws UnrecognizedXmlTypeException if the type could not 
   *         be determined
   */
	public XmlType interrogate(XmlTypes types, Document dom) 
			throws UnrecognizedXmlTypeException {
		XmlType located = null;
		for (XmlType type: types.values()) {
      try {
      	int count = evaluateCount(type,dom);
        if (count > 0) {
        	located = type;
        	break;
        }
      } catch (XPathExpressionException e) {
      	e.printStackTrace(System.err);
      }
		}
		if (located == null) {
			throw new UnrecognizedXmlTypeException("Unrecognized XML type.");
		}
    return located;
  }
	
}
