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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * The Class DcList.  List that has key value pairs.  The keys
 * can be duplicate and will produce a list on get(key)
 */
public class DcList extends LinkedList<DcList.Value> {


// class variables =============================================================
/** The Constant serialVersionUID. */
private static final long serialVersionUID = 1L;

/** The Constant DELIMETER_LIST. */
public final static String DELIMETER_LIST = "\u2715";

/** The Constant DELIMETER_VALUES. */
public final static String DELIMETER_VALUES = "\u2714";

/** The Constant EMPTY_SCHEME. */
public final static String EMPTY_SCHEME = "************";


// constructors ================================================================
/**
 * Instantiates a new dc list.
 */
public DcList(){};

/**
 * Instantiates a new dc list.
 * 
 * @param dcList the dc list
 */
public DcList(String dcList) {
  
  this.add(dcList);
  
}

// methods =====================================================================
/**
 * Adds the.
 * 
 * @param dcString the dc string
 */
public void add(String dcString) {
  dcString = Utils.chkStr(dcString);
  if("".equals(dcString)) {
    return;
  }

  String schemeValues[] = dcString.split(DELIMETER_LIST);
 
  String arrKeyValue[];
  for(String schemeValue : schemeValues) {
      schemeValue = Utils.chkStr(schemeValue);
      if("".equals(schemeValue)) { 
        continue;
      }
      arrKeyValue = schemeValue.split(DELIMETER_VALUES);
      if(arrKeyValue.length == 1) {
        this.add(this.new Value(arrKeyValue[0], EMPTY_SCHEME));
        continue;
      }
      this.add(this.new Value(arrKeyValue[0], arrKeyValue[1]));
      
  }
}

/**
 * Gets the.
 * 
 * @param scheme the scheme (if null or empty, EMPTY_SCHEME will be used)
 * 
 * @return the list< string>
 */
public List<String> get(String scheme) {
  scheme = Utils.chkStr(scheme);
  if("".equals(scheme)) {
    scheme = EMPTY_SCHEME;
  }
  List<String> valueList = new LinkedList<String>();
  Iterator<DcList.Value> iter = this.iterator();
  
  while(iter.hasNext()) {
    Value value = iter.next();
    if(value == null) {
      continue;
    }
    if(value.getScheme().equalsIgnoreCase(scheme)) {
      valueList.add(value.getValue());
    }
  }
  return valueList;

}

/**
 * Removes the scheme.
 * 
 * @param urn the urn
 */
public void removeScheme(String urn) {
 
  Iterator<DcList.Value> iter = this.iterator();
  Value value = null;
  boolean found = false;
  while(iter.hasNext()) {
    value = iter.next();
    if(value == null) {
      continue;
    }
    if(value.getScheme().equalsIgnoreCase(urn)) {
      found = true;
      break;
    }
  }
  if(found) {
    super.remove(value);
  }

}

/**
 * Removes the value.
 * 
 * @param val the val
 */
public void removeValue(String val) {
  Iterator<DcList.Value> iter = this.iterator();
  Value value = null;
  boolean found = false;
  while(iter.hasNext()) {
    value = iter.next();
    if(value == null) {
      continue;
    }
    if(value.getValue().equalsIgnoreCase(val)) {
      found = true;
      break;
    }
  }
  if(found) {
    super.remove(value);
  }
}

/**
 * Removes the value.
 * @see #removeValue(String)
 * 
 * @param val the val
 */
public void remove(String val) {
  removeValue(val);
}

// inner class =================================================================
/**
 * The Class Value.
 */
public class Value {

// instance variables ==========================================================
/** The scheme. */
String scheme;

/** The value. */
String value;

// constructor =================================================================
/**
 * Instantiates a new value.
 * 
 * @param scheme the scheme
 * @param value the value
 */
public Value(String value, String scheme) {
   this.setScheme(scheme);
   this.setValue(value);
}

// properties ==================================================================
/**
 * Gets the scheme.
 * 
 * @return the scheme (trimmed, never null)
 */
public String getScheme() {
  return Utils.chkStr(scheme);
}

/**
 * Sets the scheme.
 * 
 * @param scheme the new scheme
 */
public void setScheme(String scheme) {
  this.scheme = scheme;
}

/**
 * Gets the value.
 * 
 * @return the value (trimmed, never null)
 */
public String getValue() {
  return Utils.chkStr(value);
  
}

/**
 * Sets the value.
 * 
 * @param value the new value
 */
public void setValue(String value) {
  this.value = value;
}

}
}
