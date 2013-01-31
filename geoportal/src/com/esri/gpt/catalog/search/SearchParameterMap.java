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
package com.esri.gpt.catalog.search;

import java.util.Iterator;
import java.util.LinkedHashMap;

import com.esri.gpt.framework.util.Val;

/**
 * The Class SearchParameterMap.  Used by filters
 * to store save state parameter.
 */
@SuppressWarnings("serial")
public class SearchParameterMap extends
    LinkedHashMap<String, SearchParameterMap.Value> {

// methods =====================================================================
/**
 * Since this class can be used to serialize classes, this equals is to
 * be used when comparing 2 class maps, as opposed to just comparing the 2 
 * maps since a subclass may have a superset of key value pairs
 * 
 * @param obj the object to be tested against
 * 
 * @return true, if successful
 */
public boolean equalsSubset(SearchParameterMap obj) {
  return equals(obj, true);
}

/**
 * Compares this parameter maps either as a straight map to map comparison
 * or as maps representing objects.
 * 
 * @param obj the object to be tested against
 * @param objectEquals if true, object comparison mode used, if false then
 * map comparison mode used
 * 
 * @return true, if successful
 */
private boolean equals(Object obj, boolean objectEquals) {
  if(!(obj instanceof SearchParameterMap)) {
    return false;
  }
  SearchParameterMap map = (SearchParameterMap) obj;
  if(!objectEquals && this.size() != map.size()) {
    return false;
  }
  Iterator<String> itrKey = this.keySet().iterator();
  while(itrKey.hasNext()) {
    String strKey = itrKey.next();
    Value mapValue = map.get(strKey);
    Value thisValue = this.get(strKey);
    if(mapValue == null && thisValue == null) {
      continue;
    }
    
    if(mapValue!= null && mapValue.equals(thisValue)) {
      continue;
    } else {
      return false;
    }
    
    
  }
  return true;
}
 /**
 * Checks equality of argument object.  This is a straight map equals so 
 * the size of the maps will be checked for equality apart from the key 
 * value pairs.
 * 
 * @param obj Object to be evaluated
 * @return true of false on evaluation
 */
@Override
public boolean equals(Object obj) {
  
  return equals(obj, false);
}

@Override
@SuppressWarnings("unchecked")
/**
 * 
 * String representation
 */
public String toString() {
  Iterator iter = this.keySet().iterator();
  String strRepresentation = "\n{";
  while(iter.hasNext()) {
    Object key = iter.next();
    Object obj = this.get(key);
    if(obj == null || !(obj instanceof Value)) {
      continue;
    }
    strRepresentation += "\n Param = " + key.toString();
    strRepresentation += obj.toString();
  }
  strRepresentation += "\n}";
  return strRepresentation;
}



// inner class =================================================================
/**
 * The Class Value.  Used as the Map value parameter
 */
public class Value {

// instance variables ==========================================================
/** The parameter value. */
private String paramValue;

/** The additional parameter info. */
private String paramInfo;

// constructor =================================================================
/**
 * Instantiates a new value.
 * 
 * @param paramValue the param value
 * @param paramInfo the param info
 */
public Value(String paramValue, String paramInfo) {
  this.paramValue = paramValue;
  this.paramInfo = paramInfo;
}

/**
 * Instantiates a new value.
 * 
 * @param paramValue the param value
 */
public Value(String paramValue) {
  this.paramValue = paramValue;
}

// properties ==================================================================
/***
 * Gets the value.
 * 
 * @return the value (trimmed, never null)
 */
public String getParamValue() {
  return Val.chkStr(paramValue);
}

/**
 * Sets the value.
 * 
 * @param value the new value
 */
public void setParamValue(String value) {
  this.paramValue = value;
}

/**
 * Gets the additional parameter info.
 * 
 * @return the info (trimmed, never null)
 */
public String getInfo() {
  return Val.chkStr(paramInfo);
}

/**
 * Sets the additional parameter info.
 * 
 * @param info the new info
 */
public void setParamInfo(String info) {
  this.paramInfo = info;
}

/**
 * Checks equality of argument object
 * @param obj Object to be evaluated
 * @return true of false on evaluation
 */
@Override
public boolean equals(Object obj) {
  if(!(obj instanceof SearchParameterMap.Value)) {
    return false;
  }
  SearchParameterMap.Value val = (SearchParameterMap.Value) obj;
  return val.getInfo().equals(this.getInfo()) && 
    val.getParamValue().equals(this.getParamValue());
}

/**
 * @return <b>this</b> in string form
 */
@Override
public String toString() {
  return  
    "{ Value = \"" + this.getParamValue() + "\", Info = \"" + this.getInfo() 
    + "\"}";
}

}

}
