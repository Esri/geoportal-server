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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML SAX utility functions.
 */
public class SaxUtil {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
private SaxUtil() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Starts a SAX parser on an XML string.
 * @param handler the default handler
 * @param xml the XML string to parse
 * @param namespaceAware flag indication if the parser should be namespace aware
 * @throws ParserConfigurationException if the exception occurs
 * @throws SAXException if the exception occurs
 * @throws IOException if the exception occurs
 */
public static void parse(DefaultHandler handler, String xml, boolean namespaceAware)
  throws ParserConfigurationException, SAXException, IOException {
  SAXParserFactory factory = SAXParserFactory.newInstance();
  factory.setNamespaceAware(namespaceAware);
  SAXParser parser = factory.newSAXParser();
  InputSource src = new InputSource(new StringReader(xml));
  parser.parse(src,handler);
}

}
