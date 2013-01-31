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
import com.esri.gpt.framework.util.Val;
import java.util.LinkedHashMap;

/**
 * Maintains an ordered map where are all map keys are stored as
 * lower case strings.
 * <p/>
 * The primary intent is to provide a map having case insensitive keys.
 * <p/>
 * <br/>Map keys are trimmed and converted to lower case.
 * <br/>Null map keys are converted to empty strings.
 * <br/>Empty map keys are only set if specified as allowed on the constructor.
 * <p/>
 * Null values are not stored.
 */
public class CaseInsensitiveMap<T> extends LinkedHashMap<String,T> {
  
// class variables =============================================================

// instance variables ==========================================================
private boolean _allowEmptyKey = false;

// constructors ================================================================

/** Default constructor. */
private CaseInsensitiveMap() {}

/**
 * Construct with a flag indicating if an empty map key is allowed.
 * @param allowEmptyKey true if an empty key is allowed
 */
public CaseInsensitiveMap(boolean allowEmptyKey) {
  setAllowEmptyKey(allowEmptyKey);
}

// properties ==================================================================

/**
 * Gets the status indicating whether an empty key is allowed.
 * @return true if an empty key is allowed
 */
protected boolean getAllowEmptyKey() {
  return _allowEmptyKey;
}
/**
 * Sets the status indicating whether an empty key is allowed.
 * @param allowEmptyKey true if an empty key is allowed
 */
private void setAllowEmptyKey(boolean allowEmptyKey) {
  _allowEmptyKey = allowEmptyKey;
}

// methods =====================================================================

/**
 * Checks a key.
 * <br/>Keys are trimmed and converted to lower case.
 * <br/>Null keys are converted to empty strings.
 * @param key the key to check
 * @return the checked key
 */
private String checkKey(Object key) {
  if ((key != null) && (key instanceof String)) {
    return Val.chkStr((String)key).toLowerCase();
  }
  return "";
}

/**
 * Determines if a key is contained within the map.
 * @param key the key to check
 * @return true if the key is contained
 */
@Override
public boolean containsKey(Object key) {
  return super.containsKey(checkKey(key));
}

/**
 * Gets the value associated with a key.
 * @param key the key associated with the value to find
 * @return the corresponding value (null if none was found)
 */
@Override
public T get(Object key) {
  return super.get(checkKey(key));
}

/**
 * Sets a value within the map.
 * <p/>
 * <br/>Keys are trimmed and converted to lower case.
 * <br/>Null keys are converted to empty strings.
 * <br/>Empty map keys are only set if specified as allowed on the constructor.
 * <p/>
 * Null values are not stored. If a null value is supplied, the value
 * associated with the supplied key is removed (if it exists);
 * @param key the key to set
 * @param member the value to set
 * @return the previously mapped value associated with the key, 
 *         or null if there was no existing mapping for the key
 */
@Override
public T put(String key, T member) {
  key = checkKey(key);
  if (getAllowEmptyKey() || (key.length() > 0)) {
    if (member == null) {
      return this.remove(key);
    } else {
      return super.put(key,member);
    }    
  }
  return null;
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName());
  if (size() == 0) {
    sb.append(" ()");
  } else {
    sb.append(" (\n");
    for (T value: values()) {
      sb.append(value).append("\n");
    }
    sb.append(") ===== end ").append(getClass().getName());
  }
  return sb.toString();
}

}
