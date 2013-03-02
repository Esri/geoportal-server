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

/**
 * A key/value pair of strings.
 * <p/>
 * <br/>Keys are trimmed, null keys are treated as empty strings.
 * <br/>Values are trimmed, null values are treated as empty strings.
 */
public class StringAttribute {
  
// class variables =============================================================

// instance variables ==========================================================
private String _key = "";
private String _value = "";
  
// constructors ================================================================

/** Default constructor. */
public StringAttribute() {
  this("","");
}

/**
 * Constructs with a supplied key and value.
 * @param key the key
 * @param value the value
 */
public StringAttribute(String key, String value) {
  setKey(key);
  setValue(value);
}

// properties ==================================================================

/**
 * Gets the key.
 * @return the key
 */
public String getKey() {
  return _key;
}
/**
 * Sets the key.
 * <br/>The key will be trimmed. A null key is treated as an empty string.
 * @param key the key
 */
public void setKey(String key) {
  _key = Val.chkStr(key);
}

/**
 * Gets the value.
 * @return the key
 */
public String getValue() {
  return _value;
}

/**
 * Sets the value.
 * <br/>The value will be trimmed. A null value is treated as an empty string.
 * @param value the value
 */
public void setValue(String value) {
  _value = Val.chkStr(value);
}

// methods =====================================================================

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(":");
  sb.append(" key=\"").append(getKey()).append("\"");
  sb.append(" value=\"").append(getValue()).append("\"");
  return sb.toString();
}

}
