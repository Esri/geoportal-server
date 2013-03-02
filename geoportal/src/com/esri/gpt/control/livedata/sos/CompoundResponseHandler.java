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
 * SOS compound response handler.
 */
public class CompoundResponseHandler extends ResponseHandler {

private boolean bCompositeObservation = false;
private boolean bResult = false;
private int nCompositeLevel = 0;
private int nQuantityCompositeLevel = 0;
private String quantityName = "";
private String quantityValue = "";

private ValueComponents vc = null;

public CompoundResponseHandler(SOSContext sosContext, ValueComponentsArray vcArr) {
  super(sosContext, vcArr);
}

@Override
public void startDocument() throws SAXException {
  super.startDocument();
  bCompositeObservation = false;
  bResult = false;
  nCompositeLevel = 0;
  nQuantityCompositeLevel = 0;
  quantityName = "";
  quantityValue = "";
}

public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
  if (qName.endsWith("CompositeObservation")) {
    bCompositeObservation = true;
  }

  if (qName.endsWith("result") && bCompositeObservation) {
    bResult = true;
  }

  if (qName.endsWith("Composite") && bResult) {
    nCompositeLevel++;
  }

  if (qName.endsWith("Quantity") && nCompositeLevel>0) {
    if (nQuantityCompositeLevel==0) {
      nQuantityCompositeLevel = nCompositeLevel;
    }
    for (int i = 0; i < atts.getLength(); i++) {
      if (atts.getQName(i).endsWith("name")) {
        quantityName = atts.getValue(i);
        break;
      }
    }
  }
}

public void characters(char[] ch, int start, int length) throws SAXException {
  if (quantityName.length()>0) {
    quantityValue = new String(ch, start, length);
    insertData();
  }
}

public void endElement(String uri, String localName, String qName) throws SAXException {
  if (qName.endsWith("CompositeObservation")) {
    bCompositeObservation = false;
  }
  if (qName.endsWith("result")) {
    bResult = false;
  }

  if (qName.endsWith("Composite")) {
    acceptData();
    nCompositeLevel = Math.max(0, nCompositeLevel-1);
    if (nCompositeLevel<nQuantityCompositeLevel-1) {
      throw new ParseAbortException();
    }
  }

  if (qName.endsWith("Quantity")) {
    quantityName = "";
    quantityValue = "";
  }
}

private void insertData() {
  if (vc==null)
    vc = new ValueComponents();
  vc.put(quantityName, quantityValue);
}

private void acceptData() {
  if (vc!=null) {
    vcArr.add(vc);
    vc = null;
  }
}
}
