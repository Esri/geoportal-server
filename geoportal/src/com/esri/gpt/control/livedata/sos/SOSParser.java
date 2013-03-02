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
package com.esri.gpt.control.livedata.sos;

import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Sensor Observation Service response parser.
 */
public class SOSParser extends DefaultHandler {

private ResponseHandler[] sosHandlers = {};

public SOSParser(ValueComponentsArray vcArr, SOSContext sosContext) throws SAXException {
  super();
  sosHandlers = new ResponseHandler[]{
    new SimpleResponseHandler(sosContext, vcArr),
    new CompoundResponseHandler(sosContext, vcArr)
  };
}

public void parseDocument(InputSource is) throws ParserConfigurationException, SAXException, IOException {
  try {
    SAXParserFactory spf = SAXParserFactory.newInstance();
    SAXParser sp = spf.newSAXParser();
    sp.parse(is, this);
  } catch (ParseAbortException ex) {}
}

@Override
public void startDocument() throws SAXException {
  for (ResponseHandler h : sosHandlers) {
    h.startDocument();
  }
}

@Override
public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
  super.startElement(uri, localName, qName, atts);
  for (ResponseHandler h : sosHandlers) {
    h.startElement(uri, localName, qName, atts);
  }
}

@Override
public void characters(char[] ch, int start, int length) throws SAXException {
  super.characters(ch, start, length);
  for (ResponseHandler h : sosHandlers) {
    h.characters(ch, start, length);
  }
}

@Override
public void endElement(String uri, String localName, String qName) throws SAXException {
  super.endElement(uri, localName, qName);
  for (ResponseHandler h : sosHandlers) {
    h.endElement(uri, localName, qName);
  }
}
}
