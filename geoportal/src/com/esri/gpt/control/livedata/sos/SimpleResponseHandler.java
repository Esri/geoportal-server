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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * SOS simple response handler.
 */
public class SimpleResponseHandler extends ResponseHandler {

// processing flags
private boolean bObservationCollection = false;
private boolean bObservation = false;
private boolean bEncoding = false;
private boolean bValues = false;
private String samplingPoint = "";
private String tokenSeparator = "";
private String blockSeparator = "";
private String values = "";

/**
 * Creates instance of the handler.
 * @param sosContext SOS context
 * @param vcArr container, where data will be collected
 */
public SimpleResponseHandler(SOSContext sosContext, ValueComponentsArray vcArr) {
  super(sosContext, vcArr);
}

@Override
public void startDocument() throws SAXException {
  super.startDocument();
  bObservationCollection = false;
  bObservation = false;
  bEncoding = false;
  bValues = false;
  samplingPoint = "";
  tokenSeparator = "";
  blockSeparator = "";
  values = "";
}

public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
  if (qName.endsWith("ObservationCollection")) {
    bObservationCollection = true;
  }
  if (qName.endsWith("Observation") && bObservationCollection) {
    bObservation = true;
  }

  if (qName.endsWith("SamplingPoint") && bObservation) {
    for (int i = 0; i < atts.getLength(); i++) {
      if (atts.getQName(i).endsWith("id")) {
        samplingPoint = atts.getValue(i);
      }
    }
  }

  if (qName.endsWith("encoding") && bObservation) {
    bEncoding = true;
  }

  if (qName.endsWith("TextBlock") && bEncoding) {
    for (int i = 0; i < atts.getLength(); i++) {
      if (atts.getQName(i).endsWith("tokenSeparator")) {
        tokenSeparator = atts.getValue(i);
      }
      if (atts.getQName(i).endsWith("blockSeparator")) {
        blockSeparator = atts.getValue(i);
      }
    }
  }

  if (qName.endsWith("values") && bObservation) {
    bValues = true;
  }
}

public void characters(char[] ch, int start, int length) throws SAXException {
  if (bValues) {
    values = new String(ch, start, length);
  }
}

public void endElement(String uri, String localName, String qName) throws SAXException {
  if (qName.endsWith("ObservationCollection")) {
    bObservationCollection = false;
  }
  if (qName.endsWith("Observation")) {
    if (sosContext.getFeatureOfInterest().endsWith(samplingPoint)) {
      insertData();
      throw new ParseAbortException();
    }
    bObservation = false;
    bValues = false;
    samplingPoint = "";
    tokenSeparator = "";
    blockSeparator = "";
    values = "";
  }
  if (qName.endsWith("encoding")) {
    bEncoding = false;
  }
  if (qName.endsWith("values")) {
    bValues = false;
  }
}

private void insertData() throws SAXException {
  String[] readings = values.split(blockSeparator);
  for (int i = Math.max(readings.length - ((sosContext.getWidth() - 2 * SimpleSOSBridge.marginSize) / SimpleSOSBridge.maxReadingsDensity), 0); i < readings.length; i++) {
    String[] tokens = readings[i].split(tokenSeparator);
    if (tokens.length == 3) {
      ValueComponents vc = new ValueComponents();
      vc.put(sosContext.getObservedProperty(), tokens[2]);
      vcArr.add(vc);
    }
  }
}
}
