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
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;

import com.esri.gpt.framework.util.Val;

/**
 * Basic XML IO utitilies.
 */
public class XmlIoUtil {

// class variables =============================================================
private static final String DEFAULT_ENCODING = "UTF-8";
private static final String DEFAULT_HEADER   = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
private XmlIoUtil() {}

// properties ==================================================================

/**
 * Gets the default character encoding for XML.
 * @return the default character encoding for XML
 */
private static String getDefaultEncoding() {
  return DEFAULT_ENCODING;
}

/**
 * Gets the default XML header line.
 * @return the default XML header line
 */
private static String getDefaultHeader() {
  return DEFAULT_HEADER;
}

// methods =====================================================================

/**
 * Check a string value.
 * @param s the string to check
 * @return the checked string (trimmed, zero length if the supplied String was null)
 */
private static String chkStr(String s) {
  if (s == null) return "";
  else return s.trim();
}

/**
 * Returns the xml string for a document.
 * <br/>The output encoding is set to UTF-8
 * @param dom the xml Document
 * @return the xml string
 * @throws TransformerException if an exception occurs
 */
public static String domToString(Document dom) throws TransformerException {
  StringWriter result = new StringWriter();
  transform(new DOMSource(dom),new StreamResult(result),true) ;
  return chkStr(result.toString());
}

/**
 * Reads an xml string from a file or URL.
 * <br/>The output encoding is set to UTF-8
 * <br/>systemId examples:
 * <br/>c:/somefolder/somefile.xml
 * <br/>file:///c:/somefolder/somefile.xml
 * <br/>http://www.somesitereturningxml.com
 * @param systemId the system id of the file or URL to read
 * @return the xml string
 * @throws TransformerException if an exception occurs
 */
public static String readXml(String systemId) throws TransformerException {
  StringWriter result = new StringWriter();
  transform(new StreamSource(systemId),new StreamResult(result),false) ;
  return chkStr(result.toString());
}

/**
 * Reads an xml string from a file.
 * <br/>The output encoding is set to UTF-8
 * @param file the file to read
 * @return the xml string
 * @throws TransformerException if an exception occurs
 */
private static String readXmlFile(File file) throws TransformerException {
  StringWriter result = new StringWriter();
  transform(new StreamSource(file),new StreamResult(result),false) ;
  return chkStr(result.toString());
}

/**
 * Executes an identity transformation against an XML string.
 * <br/>The output encoding is set to UTF-8
 * <br/>The indent is set to "yes"
 * @param xml the XML to transform
 * @return the transformed xml
 * @throws TransformerException if an exception occurs
 */
public static String transform(String xml) throws TransformerException {
  xml = Val.removeBOM(Val.chkStr(xml));
  StringReader reader = new StringReader(xml);
  StringWriter writer = new StringWriter();
  XmlIoUtil.transform(new StreamSource(reader),new StreamResult(writer),true);
  xml = Val.chkStr(writer.toString());
  return xml;
}

/**
 * Executes a transformation.
 * <br/>The output encoding is set to UTF-8
 * <br/>The indent is set to "yes"
 * @param source the transformation source
 * @param result the transformation result
 * @param indent if true, the output indent key is set to "yes"
 * @throws TransformerException if an exception occurs
 */
public static void transform(javax.xml.transform.Source source,
                              javax.xml.transform.Result result,
                              boolean indent)
  throws TransformerException {
  Transformer transformer = TransformerFactory.newInstance().newTransformer() ;
  transformer.setOutputProperty(OutputKeys.ENCODING,getDefaultEncoding());
  if (indent) transformer.setOutputProperty(OutputKeys.INDENT,"yes");
  transformer.transform(source,result);
}

/**
 * Writes an xml string to a file.
 * <br/>The output encoding is set to UTF-8
 * @param file the file to create
 * @param xml them xml string
 * @throws TransformerException if an exceition occurs
 */
public static void writeXmlFile(String xml, File file) throws TransformerException {
  StringReader source = new StringReader(xml);
  // This is in order to get function working on Java 1.5
  StreamResult stream = new StreamResult(file);
  stream.setSystemId(file.getAbsolutePath());
  transform(new StreamSource(source),stream,true);
}

/**
 * JSON To xml.
 * 
 * @param xmlString the xml string
 * @param rootElement the root element that the xml should have.  
 * By default = gptJsonXml
 * @return the xml. 
 * @throws Exception thrown if error while converting xmlString
 */
public static String jsonToXml(String xmlString, String rootElement) 
  throws Exception {
  try {
    JSONObject jso = new JSONObject(xmlString);
    rootElement = Val.chkStr(rootElement);
    if("".equals(rootElement)) {
      rootElement = "gptJsonXml";
    }
    String xml = XML.toString(jso, "gptJsonXml");
    StreamSource source = new StreamSource(new StringReader(xml));
    StringWriter writer = new StringWriter();
    StreamResult result = new StreamResult(writer);
    Transformer transformer = TransformerFactory.newInstance().newTransformer();
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform(source, result);
    return Val.chkStr(writer.toString());
  } catch (Exception e) {
    throw e;
  }
}

}
