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
package com.esri.gpt.server.csw.client;

import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.esri.gpt.framework.util.Val;


/**
 * The Class CapabilitiesParse.
 */
public class CapabilitiesParse extends DefaultHandler {

// class variable ==============================================================
/** The LOG. */
Logger                 LOG            = Logger
                                          .getLogger(CapabilitiesParse.class
                                              .getCanonicalName());

// instance variables ==========================================================
/** The text. */
StringBuffer           text           = new StringBuffer();

/** The cap. */
CswCatalogCapabilities cap;

/** The ows parameter. */
boolean                owsParameter   = false;

/** The capabilities. */
boolean                capabilities   = false;

/** The get record by id. */
boolean                getRecordById  = false;

/** The get records. */
boolean                getRecords     = false;

/** The constraint. */
boolean                constraint     = false;

/** The value. */
boolean                value          = false;

/** The operation. */
boolean                operation      = false;

/** The get. */
boolean                get            = false;

/** The post. */
boolean                post           = false;

/** The get records url list. */
@SuppressWarnings("unchecked")
private final Stack          getRecordsList = new Stack();

// constructor ================================================================
/**
 * Instantiates a new capabilities parse.
 * 
 * @param capabilities the capabilities
 */
public CapabilitiesParse(CswCatalogCapabilities capabilities) {
  cap = capabilities;
  // TODO Auto-generated constructor stub
}

// methods =====================================================================
/**
 * Sax start element
 * @param uri
 * @param localName
 * @param qName
 * @param attrib
 * @throws SAXException
 */
@SuppressWarnings("unchecked")
@Override
public void startElement(String uri, String localName, String qName,
    Attributes attrib) throws SAXException {
  tracking(localName, true);
  text = new StringBuffer();

  if (localName.equals("")) {
    localName = qName;
  }
  LOG.finer("ELEMENT BEGIN " + localName);

  // Define handling rules 
  text = new StringBuffer();

  if (capabilities && localName.equalsIgnoreCase("operation")) {
    if (chkStr(attrib.getValue("name")).equalsIgnoreCase("GetRecordById")) {
      getRecordById = true;
    } else {
      getRecordById = false;
    }

    if (chkStr(attrib.getValue("name")).equalsIgnoreCase("GetRecords")) {
      getRecords = true;
    } else {
      getRecords = false;
    }
  }

  if (capabilities && getRecordById && localName.equalsIgnoreCase("get")) {
    cap.set_getRecordByIDGetURL(attrib.getValue("xlink:href"));
  }

  if (capabilities && getRecords && localName.equalsIgnoreCase("post")) {
    getRecordsList.push(chkStr(attrib.getValue("xlink:href")));
  }
  
  
}

/**
 * Sax end element
 * @param uri
 * @param localName
 * @param qName
 * @throws SAXException
 */
@Override
public void endElement(String uri, String localName, String qName)
    throws SAXException {
  if (localName.equals("")) {
    localName = qName;
  }

  LOG.finer("TEXT IN ELEMENT " + text);
  LOG.finer("ELEMENT END " + localName);
  try {
    text = new StringBuffer(chkStr(text.toString()));
    if (capabilities && getRecords && post && constraint
        && localName.equalsIgnoreCase("Value")
        && text.toString().equalsIgnoreCase("XML")
        && getRecordsList.peek() != null) {
      cap.set_getRecordsPostURL(getRecordsList.pop().toString());
    } else if (capabilities && getRecords && post && constraint
        && localName.equalsIgnoreCase("Value")
        && !text.toString().equalsIgnoreCase("XML")
        && getRecordsList.peek() != null) {
      getRecordsList.pop();
    }

    if (capabilities && getRecords && localName.equalsIgnoreCase("HTTP")
        && getRecordsList.peek() != null) {
      String tmp = chkStr(cap.get_getRecordsPostURL());
      if ("".equals(tmp)) {
        cap.set_getRecordsPostURL(getRecordsList.pop().toString());
      }
    }
    if(capabilities && localName.equalsIgnoreCase("Abstract")) {
      cap.setAbstractText(text.toString());
    }
    if(capabilities && localName.equalsIgnoreCase("Title")) {
      cap.setTitle(text.toString());
    }
  } catch (Throwable e) {
    LOG.log(Level.FINER, "Error while getting getrecords url", e);
  }
  tracking(localName, false);

}
 
/**
 * Tracks the xml elements
 * 
 * @param element the element
 * @param alive the alive
 */
private void tracking(String element, boolean alive) {

  if (element.equals("owsParameter")) {
    owsParameter = alive;
  } else if (element.equalsIgnoreCase("Capabilities")) {
    capabilities = alive;
  } else if (element.equalsIgnoreCase("Operation")) {
    operation = alive;
  } else if (element.equalsIgnoreCase("Constraint")) {
    constraint = alive;
  } else if (element.equalsIgnoreCase("Value")) {
    value = alive;
  } else if (element.equalsIgnoreCase("Post")) {
    post = alive;
  } else if (element.equalsIgnoreCase("Get")) {
    get = alive;
  }

}

/**
 * Appends element chracters.
 * 
 * @param chars Characters
 * @param start start position
 * @param length length to read
 */
@Override
public void characters(char chars[], int start, int length) {

  text.append(chars, start, length);
}

/**
 * If string is null, returns empty string, else returns trimmed
 * string.
 * 
 * @param string the string to be checked
 * 
 * @return the string (trimmed, never null)
 */
private static String chkStr(String string) {
  if (string == null) {
    return "";
  }
  return string.trim();
}

}
