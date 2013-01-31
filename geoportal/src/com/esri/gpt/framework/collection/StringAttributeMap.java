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

/**
 * Maintains an ordered map of StringAttribute objects with case insensitive
 * map keys.
 */
public class StringAttributeMap extends CaseInsensitiveMap<StringAttribute> {
  
// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public StringAttributeMap() {
  super(false);
}

/**
 * Construct with a flag indicating if an empty map key is allowed.
 * @param allowEmptyKey true if an empty key is allowed
 */
public StringAttributeMap(boolean allowEmptyKey) {
  super(allowEmptyKey);
}
 
// properties ==================================================================

// methods =====================================================================

/**
 * Adds a member to the collection.
 * <br/>The member will not be added if it is null.
 * @param member the member to add
 */
public void add(StringAttribute member) {
  if (member != null) {
    put(member.getKey(),member);
  }
}

/**
 * Gets the value associated with a key.
 * @param key the subject key
 * @return the associated value (null if not found)
 */
public String getValue(String key) {
  StringAttribute member = this.get(key);
  if (member != null) {
    return member.getValue();
  } else {
    return null;
  }
}

/**
 * Instantiates a new StringAttribute with the supplied key and value
 * and puts it into the collection.
 * @param key the key 
 * @param value the value
 * @return previous value associated with the key, 
 *         or null if there was no existing mapping for the key
 */
public StringAttribute set(String key, String value) {
  StringAttribute attr = new StringAttribute(key,value);
  return put(attr.getKey(),attr);
}

}
