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
package com.esri.gpt.catalog.arcims;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles the SAX parsing of an axl response from an ArcIMS metadata publish service.
 * <p>
 * Parsing starts by passing a publish service request and axl response to
 * the parseAxlResponse method.
 * <br/>When an element is started the PublishServiceRequest.onStartSaxElement
 * is triggered.
 * <br/>When an element is ended the PublishServiceRequest.onEndSaxElement
 * is triggered.
 */
class ImsSaxHandler extends DefaultHandler {

// class variables =============================================================

// instance variables ==========================================================
private StringBuffer  _characters = new StringBuffer();
private ImsRequest    _imsRequest = null;

// constructors ================================================================

/** Default constructor. */
protected ImsSaxHandler() {
  super();
}

// properties ==================================================================

// methods =====================================================================

/**
 * Sets text node characters for a SAX element.
 * @param ch the array of characters
 * @param start the starting position within the array
 * @param length the number of characters to read from the array
 * @throws SAXException if the exception occurs
 */
@Override
public void characters(char ch[], int start, int length)
  throws SAXException {
  if ((ch != null) && (length > 0)) _characters.append(ch,start,length);
}

/**
 * Ends a SAX element.
 * <br/>The parser is not namespace aware.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @throws SAXException if a SAXException occurs
 */
@Override
public void endElement(String uri, String localName, String qName)
  throws SAXException {
  String sTag        = returnLowerCaseTag(uri,localName,qName);
  String sCharacters = _characters.toString().trim();

  
  // check for errors
  if (sTag.equals("error")) {
    throw new ImsSaxException(sCharacters);
  } else if (sCharacters.startsWith("[ERR")) {
    if (_imsRequest != null) {
      _imsRequest.setActionStatus(ImsRequest.ACTION_STATUS_ERROR);
    }
    throw new ImsSaxException(sCharacters);
  }

  // check for metadata action status
  if (sTag.equals("metadata_action")) {
    if (_imsRequest != null) {
      if (sCharacters.equals("OK")) {
        _imsRequest.setActionStatus(ImsRequest.ACTION_STATUS_OK);
      } else if (sCharacters.equals("REPLACED")) {
        _imsRequest.setActionStatus(ImsRequest.ACTION_STATUS_REPLACED);
      }
    }
  }

  // trigger end element, reset characters
  if (_imsRequest != null) {
    _imsRequest.onEndSaxElement(sTag,sCharacters);
  }
  
  _characters = new StringBuffer();
}

/**
 * Starts a SAX parser on an ArcIMS axl response.
 * @param imsRequest the associated request
 * @param axlResponse the ArcIMS axl response
 * @throws ParserConfigurationException if the exception occurs
 * @throws SAXException if the exception occurs
 * @throws IOException if the exception occurs
 */
protected void parseAxlResponse(ImsRequest imsRequest,
                                String axlResponse)
  throws ParserConfigurationException, SAXException, IOException {
  _imsRequest = imsRequest;
  SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
  InputSource src = new InputSource(new StringReader(axlResponse));
  parser.parse(src,this);
}

/**
 * Returns the lower case tag name for an element.
 * <br/>The parser is not namespace aware.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @return the lower case tag name
 */
private String returnLowerCaseTag(String uri, String localName, String qName) {
  if (qName == null) {
    return "";
  } else {
    return qName.trim().toLowerCase();
  }
}

/**
 * Starts a SAX element.
 * <br/>The parser is not namespace aware.
 * @param uri the uri namespace for the element
 * @param localName the local name for the element
 * @param qName the qualified name for the element
 * @param attributes the attributes for the element
 * @throws SAXException if a SAXException occurs
 */
@Override
public void startElement(String uri,
                         String localName,
                         String qName,
                         Attributes attributes)
  throws SAXException {
  
  // reset characters, trigger start element
  _characters = new StringBuffer();
  String sTag = returnLowerCaseTag(uri,localName,qName);
  if (_imsRequest != null) {
    _imsRequest.onStartSaxElement(sTag,attributes);
  }
}

}

