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
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.ArrayList;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles core SAX events during the parsing of an XML document.
 */
public class BaseSaxHandler extends DefaultHandler {

// class variables =============================================================

// instance variables ==========================================================
private StringBuffer      _characters = new StringBuffer();
private ArrayList<String> _localNameStack = new ArrayList<String>();
private String            _localPath = "";

// constructors ================================================================

/** Default constructor. */
public BaseSaxHandler() {
  super();
}

// properties ==================================================================

// methods =====================================================================

/**
 * Sets text node characters for a SAX element.
 * <br/>This is a core method for a SAX DefaultHandler.
 * @param ch the array of characters
 * @param start the starting position within the array
 * @param length the number of characters to read from the array
 * @throws SAXException if the exception occurs
 */
@Override
public final void characters(char ch[], int start, int length)
  throws SAXException {
  if ((ch != null) && (length > 0)) {
    _characters.append(ch,start,length);
  }
}

/**
 * Ends the SAX document.
 * <br/>This is a core method for a SAX DefaultHandler.
 * @throws SAXException if a SAXException occurs
 */
@Override
public void endDocument() throws SAXException {
  resetCharacters();
  resetLocalNameStack();
}

/**
 * Ends a SAX element.
 * <br/>This is a core method for a SAX DefaultHandler.
 * <br/>An onEndSaxElement event is triggered.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @throws SAXException if a SAXException occurs
 */
@Override
public final void endElement(String uri, String localName, String qName)
  throws SAXException {
  try {
    onEndSaxElement(uri,localName,qName,_localPath,_characters.toString());
  } finally {
    resetCharacters();
    popLocalName();
  }
}

/**
 * Triggered when an element is ended during the
 * parsing of an XML document.
 * <br/>There is no default implementation.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @param localPath the local path for the element
 * @param textNodeValue the text node value for the element
 * @throws SAXException if a SAXException occurs
 */
protected void onEndSaxElement(String uri,
                               String localName,
                               String qName,
                               String localPath,
                               String textNodeValue)
  throws SAXException {
  // no default implementation
  //System.err.println("onEnd: "+localPath+" = "+textNodeValue);
}

/**
 * Triggered when an element is started during the
 * parsing of an XML document.
 * <br/>There is no default implementation.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @param localPath the local path for the element
 * @param attributes the attributes for the element
 * @throws SAXException if a SAXException occurs
 */
protected void onStartSaxElement(String uri,
                                 String localName,
                                 String qName,
                                 String localPath,
                                 Attributes attributes)
  throws SAXException {
  // no default implementation
  //System.err.println("onStart: "+localPath);
}

/**
 * Starts a SAX parser on an XML string.
 * @param xml the XML string to parse
 * @param namespaceAware flag indication if the parser should be namespace aware
 * @throws ParserConfigurationException if the exception occurs
 * @throws SAXException if the exception occurs
 * @throws IOException if the exception occurs
 */
public void parse(String xml, boolean namespaceAware)
  throws ParserConfigurationException, SAXException, IOException {
  SaxUtil.parse(this,xml,namespaceAware);
}

/**
 * Pops a local element name from the stack and resets the local path
 * <br/>The name is removed from the stack.
 */
private void popLocalName() {
  int nSize = _localNameStack.size();
  if (nSize > 0) {
    _localNameStack.remove(nSize - 1);
  }
  resetLocalPath();
}

/**
 * Pushes a local element name onto the stack and resets the local path.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 */
private void pushLocalName(String uri, String localName, String qName) {
  String sName = Val.chkStr(qName);
  if (sName.length() == 0) {
    sName = Val.chkStr(localName);
    if (sName.length() == 0) {
      sName = "unknownElementName";
    }
  }
  _localNameStack.add(sName);
  resetLocalPath();
}

/**
 * Resets the text node characters for the current SAX element.
 */
private void resetCharacters() {
  _characters = new StringBuffer();
}

/**
 * Resets the local element name stack.
 */
private void resetLocalNameStack() {
  _localNameStack = new ArrayList<String>();
  resetLocalPath();
}

/**
 * Resets the local path for the active element.
 * <br/>The local path is derived from the local name stack.
 */
private void resetLocalPath() {
  StringBuffer sbPath = new StringBuffer();
  int n = _localNameStack.size();
  for (int i=0;i<n;i++) {
    if (i > 0) {
      sbPath.append("/");
    }
    String sName = (String)_localNameStack.get(i);
    sbPath.append(sName);
  }
  _localPath = sbPath.toString();
}

/**
 * Starts the SAX document.
 * <br/>This is a core method for a SAX DefaultHandler.
 * @throws SAXException if a SAXException occurs
 */
@Override
public void startDocument() throws SAXException {
  resetCharacters();
  resetLocalNameStack();
}

/**
 * Starts a SAX element.
 * <br/>This is a core method for a SAX DefaultHandler.
 * <br/>An onStartSaxElement event is triggered.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @param attributes the attributes for the element
 * @throws SAXException if a SAXException occurs
 */
@Override
public final void startElement(String uri,
                               String localName,
                               String qName,
                               Attributes attributes)
  throws SAXException {
  resetCharacters();
  pushLocalName(uri,localName,qName);
  onStartSaxElement(uri,localName,qName,_localPath,attributes);
}

}
