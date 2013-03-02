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
package com.esri.gpt.framework.xml;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.esri.gpt.framework.util.ResourcePath;
import com.esri.gpt.framework.util.Val;

/**
 * XML Document utility functions.
 */
public class DomUtil {

// class variables =============================================================
private static final String DEFAULT_HEADER =
  "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

// instance variables ==========================================================

// constructors ================================================================
/** Default constructor. */
private DomUtil() {
}

// properties ==================================================================

// methods =====================================================================
/**
 * Returns an attribute value from a NamedNodeMap.
 * @param nnm the NamedNodeMap of attributes
 * @param name the name of the attribute to retrieve
 * @return the attribute value (trimmed, never null)
 */
public static String getAttributeValue(NamedNodeMap nnm, String name) {
  String s = "";
  if ((nnm != null) && (nnm.getLength() > 0)) {
    Node nd = nnm.getNamedItem(name);
    if (nd != null) {
      s = Val.chkStr(nd.getNodeValue());
    }
  }
  //System.err.println("extractAttributeValue "+name+"="+s);
  return s;
}

/**
 * Returns the text node content associated with a parent node.
 * <br/>The text will be trimmed, a null will be returned as an empty string.
 * @param parentNode the parent node for which text will be extracted
 * @return the text
 */
public static String getTextContent(Node parentNode) {
  if (parentNode != null) {
    return Val.chkStr(parentNode.getTextContent());
  } else {
    return "";
  }
}

/**
 * Finds the children of a parent node matching the supplied child
 * tag name.
 * <br/>This is not recursive, only the immediate children are considered.
 * @param parent the parent containing the children to find
 * @param childTagName the tag name of the child to find
 * @return an array of matching nodes
 */
public static Node[] findChildren(Node parent, String childTagName) {
  ArrayList<Node> list = new ArrayList<Node>();
  if (parent != null) {
    NodeList nlChildren = parent.getChildNodes();
    if (nlChildren != null) {
      int nChildren = nlChildren.getLength();
      for (int i = 0; i < nChildren; i++) {
        Node ndChild = nlChildren.item(i);
        if (ndChild.getNodeName().equalsIgnoreCase(childTagName)) {
          list.add(ndChild);
        }
      }
    }
  }
  return list.toArray(new Node[0]);
}

/**
 * Finds the first child of a parent node based upon the supplied tag
 * name for the child.
 * <br/>This is not recursive, only the immediate children are considered.
 * @param parent the parent of the child node to find
 * @param childTagName the tag name for the node to find
 * @return the located node
 */
public static Node findFirst(Node parent, String childTagName) {
  Node[] nodes = findChildren(parent, childTagName);
  if (nodes.length > 0) {
    return nodes[0];
  } else {
    return null;
  }
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
public static Document makeDomFromResourcePath(String path,
                                                 boolean namespaceAware)
  throws ParserConfigurationException, SAXException, IOException {
  ResourcePath rp = new ResourcePath();
  return makeDomFromSource(rp.makeInputSource(path), namespaceAware);
}

/**
 * Makes an XML document based upon an input source.
 * @param src the InputSource
 * @param namespaceAware true if the Document should be namespace aware
 * @return the XML document
 * @throws ParserConfigurationException if a configuration exception occurs
 * @throws SAXException if an exception occurs during XML parsing
 * @throws IOException if an i/o exception occurs
 */
public static Document makeDomFromSource(InputSource src,
                                           boolean namespaceAware)
  throws ParserConfigurationException, SAXException, IOException {
  Document dom = null;
  if (src == null) {
    throw new IOException("The InputSource is null.");
  } else {
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(namespaceAware);
    DocumentBuilder builder = factory.newDocumentBuilder();
    dom = builder.parse(src);
  }
  return dom;
}

/**
 * Makes an XML document from an XML string.
 * @param xml the xml string
 * @param namespaceAware true if the Document should be namespace aware.
 * @return the XML document
 * @throws ParserConfigurationException if a configuration exception occurs
 * @throws SAXException if an exception occurs during XML parsing
 * @throws IOException if an i/o exception occurs
 */
public static Document makeDomFromString(String xml, boolean namespaceAware)
  throws ParserConfigurationException, SAXException, IOException {
  InputSource src = new InputSource(new StringReader(Val.chkStr(xml)));
  return makeDomFromSource(src, namespaceAware);
}

/**
 * Creates an empty XML document.
 * @return the new document
 * @throws ParserConfigurationException if a configuration exception occurs
 * @throws SAXException if an exception occurs during XML parsing
 * @throws IOException if an i/o exception occurs
 */
public static Document newDocument()
  throws ParserConfigurationException, SAXException, IOException {
  String sXml = DEFAULT_HEADER + "<a>empty</a>";
  Document dom = makeDomFromString(sXml, true);
  Element root = dom.getDocumentElement();
  if (root != null) {
    dom.removeChild(root);
  }
  return dom;
}
}
