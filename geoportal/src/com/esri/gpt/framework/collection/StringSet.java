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
package com.esri.gpt.framework.collection;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;

import com.esri.gpt.framework.util.Val;

/**
 * Maintains an ordered set of strings.
 * <p>
 * Null values are treated as zero-length strings (empty).
 * <br/>Empty values are only added if flagged as allowed.
 * <br/>Values will be trimmed if flagged.
 */
public class StringSet extends LinkedHashSet<String> {
  
// class variables =============================================================

// instance variables ==========================================================
private boolean                _allowEmptyValues = false;
private boolean                _isCaseSensitive = false;
private HashMap<String,String> _mapKeys = new HashMap<String,String>();
private boolean                _trimValues = true;
  
// constructors ================================================================

/**
 * Default constructor.
 * <br/>Empty values will not be allowed.
 * <br/>The list will not be case sensitive.
 * <br/>Values will be trimmed.
 */
public StringSet() {
  this(false,false,true);
}

/**
 * Constructs with an empty values allowed flag, a case sensitivity flag 
 * and a trimming preference.
 * @param allowEmptyValues true if empty values are allowed
 * @param isCaseSensitive true if the set is case sensitive
 * @param trimValues true if values will be trimmed
 */
public StringSet(boolean allowEmptyValues,
                 boolean isCaseSensitive,
                 boolean trimValues) {
  setAllowEmptyValues(allowEmptyValues);
  setIsCaseSensitive(isCaseSensitive);
  setTrimValues(trimValues);
}
  
// properties ==================================================================

/**
 * Gets the status indicating whether empty strings are allowed.
 * @return true if empty strings are allowed
 */
protected boolean getAllowEmptyValues() {
  return _allowEmptyValues;
}
/**
 * Sets the status indicating whether empty strings are allowed.
 * @param allowEmptyValues true if empty strings are allowed
 */
private void setAllowEmptyValues(boolean allowEmptyValues) {
  _allowEmptyValues = allowEmptyValues;
}

/**
 * Gets the case sensitive status for a unique list.
 * <br/>This value is only applicable to a unique list.
 * @return true if the unique list is case sensitive
 */
protected boolean getIsCaseSensitive() {
  return _isCaseSensitive;
}
/**
 * Sets the case sensitive status for a unique list.
 * <br/>This value is only applicable to a unique list.
 * @param isCaseSensitive true if the unique list is case sensitive
 */
private void setIsCaseSensitive(boolean isCaseSensitive) {
  _isCaseSensitive = isCaseSensitive;
}

/**
 * Gets the flag indicating whether values will be trimmed.
 * @return true if values will be trimmed
 */
protected boolean getTrimValues() {
  return _trimValues;
}
/**
 * Sets the flag indicating whether values will be trimmed.
 * @param trimValues true if values will be trimmed
 */
private void setTrimValues(boolean trimValues) {
  _trimValues = trimValues;
}

// methods =====================================================================

/**
 * Adds a value to the collection.
 * <br/>Null values are treated as zero-length strings (empty).
 * <br/>Empty values are only added if flagged as allowed.
 * <br/>Values will be trimmed if required.
 * @param value the value to add
 * @return true if the value was added
 */
@Override
public boolean add(String value) {
  
  // check the value
  boolean bAdded = false;
  value = checkValue(value);
  if (getAllowEmptyValues() || (value.length() > 0)) {
  
    // if the list is unique, ensure that the string is not already 
    // contained within the map, otherwise add the entry
    if (!getIsCaseSensitive()) {
      String sKey = checkKey(value);
      if (!_mapKeys.containsKey(sKey)) {
        _mapKeys.put(sKey,value);
        bAdded = super.add(value);
      }
    } else {
      bAdded = super.add(value);
    }
  }
  return bAdded;
}

/**
 * Adds a delimited set of tags to the collection.
 * <br/>The tag is tokenized with the following 3 delimiters:
 * <br/> semi-colon comma space
 * <br/>Tokens are trimmed.
 * <br/>Null or zero length tokens are ignored.
 * <br/>Example: addDelimited("a b,c;d e") results in 5 tags a b c d e
 * @param demilitedTags the tag to add
 */
public void addDelimited(String demilitedTags) {
  String[] aSemi = Val.tokenize(demilitedTags,";");
  for (int iSemi=0;iSemi<aSemi.length;iSemi++) {
    String[] aComma = Val.tokenize(aSemi[iSemi],",");
    for (int iComma=0;iComma<aComma.length;iComma++) {
      String[] aSpace = Val.tokenize(aComma[iComma]," ");
      for (int iSpace=0;iSpace<aSpace.length;iSpace++) {
        add(aSpace[iSpace]);
      }
    }
  }
}

/**
 * Checks the value of a key. 
 * <br/>If the collection is not case sensitive, keys are used to 
 * lower case entries.
 * <br/>The key is trimmed if required (nulls are returned as 
 * zero-length strings). If the map is not case sensitive, the 
 * key is returned in lower case.
 * @param key the key to check
 * @return the checked key
 */
private String checkKey(String key) {
  if (key == null) {
    key = "";
  } else if (getTrimValues()) {
    key = key.trim();
  }
  if (getIsCaseSensitive()) {
    return key;
  } else {
    return key.toLowerCase();
  }
}

/**
 * Checks a value. 
 * The value is trimmed if required (nulls are returned as 
 * zero-length strings).
 * @param value the value to check
 * @return the checked value
 */
private String checkValue(String value) {
  if (value == null) {
    return "";
  } else if (getTrimValues()) {
    return value.trim();
  } else {
    return value;
  }
}

/**
 * Clears the collection.
 */
@Override
public void clear() {
  _mapKeys.clear();
  super.clear();
}

/**
 * Determine if the collection contains a value.
 * @param value the value to check
 * @return true if the value is contained within the collection
 */
@Override
public boolean contains(Object value) {
  if (value == null) {
    return containsString("");
  } else if (value instanceof String) {
    return containsString((String)value);
  } else {
    return false;
  }
}

/**
 * Determine if the collection contains a value.
 * @param value the value to check
 * @return true if the value is contained within the collection
 */
public boolean containsString(String value) {
  value = checkValue(value);
  if (!getIsCaseSensitive()) {
    return _mapKeys.containsKey(checkKey(value));
  } else {
    return super.contains(value);
  }
}

/**
 * Removes a value from the collection.
 * @param value the value to remove
 * @return <code>true</code> if the value was removed
 */
@Override
public boolean remove(Object value) {
  if (value == null) {
    return remove("");
  } else if (value instanceof String) {
    return remove((String)value);
  } else {
    return false;
  }
}

/**
 * Removes a value from the collection.
 * @param value the value to remove
 * @return <code>true</code> if the value was removed
 */
public boolean remove(String value) {
  if (!getIsCaseSensitive()) {
    String sCheckedKey = checkKey(value);
    if (_mapKeys.containsKey(sCheckedKey)) {
      String sRemove = _mapKeys.remove(sCheckedKey);
      return super.remove(sRemove);
    } else {
      return false;
    }
  } else {
    return super.remove(checkValue(value));
  }
}

/**
 * Retains only those values that are included in the supplied collection.
 * @param c the collection defining the values to retain
 * @return <code>true</code> if the collection was modified
 */
@Override
public boolean retainAll(Collection<?> c) {
  int nSize = size();
  StringSet ss = new StringSet();
  for (Object o: c) {
    if (o instanceof String) {
      ss.add((String)o);
    }
  }
  clear();
  addAll(ss);
  return (nSize != size());
}

}
