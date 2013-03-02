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

import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * Sensor Observation Service response handler.
 */
public abstract class ResponseHandler implements ContentHandler {

/** SOS context */
protected SOSContext sosContext;
/** collected data */
protected ValueComponentsArray vcArr;

/**
 * Creates instance of the handler.
 * @param sosContext SOS context
 * @param vcArr container, where data will be collected
 */
public ResponseHandler(SOSContext sosContext, ValueComponentsArray vcArr) {
  super();
  this.sosContext = sosContext;
  this.vcArr = vcArr;
}

public void startDocument() throws SAXException {
  vcArr.clear();
}

public void endDocument() throws SAXException {
}

public void endPrefixMapping(String prefix) throws SAXException {
}

public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
}

public void processingInstruction(String target, String data) throws SAXException {
}

public void setDocumentLocator(Locator locator) {
}

public void skippedEntity(String name) throws SAXException {
}

public void startPrefixMapping(String prefix, String uri) throws SAXException {
}
}
