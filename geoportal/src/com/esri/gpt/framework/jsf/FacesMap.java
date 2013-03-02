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
package com.esri.gpt.framework.jsf;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Supplies an implemented Map interface intended for use within a JSF expression.
 * <p>
 * All Map interface methods are implemented, but basically have no behavior. 
 * <p>
 * Sub-classes should override the "public Object get(Object key)" method to
 * provide JSF with expression based results.
 * <p>Example:<br/>
 * rendered="#{PageContext.roleMap['someRole']}"
 * <p>
 * In this case, RoleMap extends FacesMap and overrides the get() 
 * method returning a Boolean indicating whether or not the active user has 
 * the supplied role 'someRole'.
 */
public class FacesMap<MemberType extends Serializable> 
  implements Map<String,MemberType>, Serializable {
  
// class variables =============================================================

// instance variables ==========================================================
  
// constructors ================================================================
 
/** Default constructor. */
public FacesMap() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Clears the map.
 * <br/>The method is ignored.
 */
public void clear() {}

/**
 * Determines if a key is contained within the map.
 * @param key the key to check
 * @return true if the key is contained (always false)
 */
public boolean containsKey(Object key) {
  return false;
}

/**
 * Determines if a value is contained within the map.
 * @param value the value to check
 * @return true if the value is contained (always false)
 */
public boolean containsValue(Object value) {
  return false;
}

/**
 * Returns the set of entries contained within the map.
 * @return the set of entries (always null)
 */
public Set<java.util.Map.Entry<String, MemberType>> entrySet() {
  return null;
}

/**
 * Gets the value associated with a key.
 * <br/>This method is overridden by the sub-class to provide a Map
 * interface for a JSF tag.
 * @return the associated value (always null for this super-class)
 */
public MemberType get(Object key) {
  return null;
}

/**
 * Determines if the map is empty.
 * @return always false
 */
public boolean isEmpty() {
  return false;
}

/**
 * Returns the set of keys contained within the map.
 * @return the set of keys (always null)
 */
public Set<String> keySet() {
  return null;
}

/**
 * Puts a value within the map.
 * <br/>The method is ignored.
 * @param key the map entry key
 * @param value the map entry value (always null)
 */
public MemberType put(String key, MemberType value) {
  return null;
}

/**
 * Copies the entries from the supplied Map to this Map.
 * <br/>The method is ignored.
 * @param map the map to copy
 */
public void putAll(Map<? extends String, ? extends MemberType> map) {}

/**
 * Removes the value associated with a key.
 * <br/>The method is ignored.
 * @param key the key associated with the value to remove
 * @return the value that was removed (always null))
 */
public MemberType remove(Object key) {
  return null;
}

/**
 * Returns the number of entries contained within the map.
 * @return the number of entries (always 0)
 */
public int size() {
  return 0;
}

/**
 * Returns the collection of values contained within the map.
 * @return the collection of values (always null)
 */
public Collection<MemberType> values() {
  return null;
}

}
