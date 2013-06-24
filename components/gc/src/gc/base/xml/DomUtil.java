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
package gc.base.xml;
import gc.base.util.ResourcePath;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * XML Document utilities.
 */
public class DomUtil {
	
  /**
   * Makes a document from a string.
   * @param xml the xml
   * @param namespaceAware true if the Document should be namespace aware
   * @return the document
   * @throws ParserConfigurationException if the exception occurs
   * @throws SAXException if the exception occurs
   * @throws IOException if the exception occurs
   */
	public static Document makeDom(String xml, boolean namespaceAware) 
			throws ParserConfigurationException, SAXException, IOException {
		if (xml != null) xml = XmlUtil.removeBOM(xml).trim();
		InputSource source = new InputSource(new StringReader(xml));
		return makeDom(source,namespaceAware);
	}
	
  /**
   * Makes a document from a source.
   * @param source the source
   * @param namespaceAware true if the Document should be namespace aware
   * @return the document
   * @throws ParserConfigurationException if the exception occurs
   * @throws SAXException if the exception occurs
   * @throws IOException if the exception occurs
   */
	public static Document makeDom(InputSource source, boolean namespaceAware) 
			throws ParserConfigurationException, SAXException, IOException {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(namespaceAware);
    DocumentBuilder builder = factory.newDocumentBuilder();
    Document dom = builder.parse(source);
    return dom;
	}
	
	/**
	 * Makes an XML document associated with a specified resource.
	 * @param path the path for the resource (relative to WEB-INF/classes)
	 * @param namespaceAware true if the Document should be namespace aware.
	 * @return the XML document
	 * @throws ParserConfigurationException if a configuration exception occurs
	 * @throws SAXException if an exception occurs during XML parsing
	 * @throws IOException if an i/o exception occurs
	 */
	public static Document makeDomFromResourcePath(String path, boolean namespaceAware)
	    throws ParserConfigurationException, SAXException, IOException {
	  ResourcePath rp = new ResourcePath();
	  return makeDom(rp.makeInputSource(path),namespaceAware);
	}

}
