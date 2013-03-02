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
package com.esri.gpt.control.identity;
import com.esri.gpt.framework.jsf.FacesMap;
import com.esri.gpt.framework.security.principal.UserAttributeMap;

/**
 * Provides a Map interface to determine if a user attribute should be
 * rendered. 
 * <p>Only configured attributes will be rendered.
 * <br/Example:<br/>
 * rendered="#{SelfCareController.hasUserAttribute['firstName']}"
 */
public class HasAttributeMap extends FacesMap<Boolean> {
  
// class variables =============================================================

// instance variables ==========================================================
private UserAttributeMap _activeAttributes = null;
  
// constructors ================================================================
 
/**
 * Constructs based upon a supplied user attribute map.
 * @param map the user attribute map
 */
public HasAttributeMap(UserAttributeMap map) {
  _activeAttributes = map;
}

// properties ==================================================================

// methods =====================================================================

/**
 * Implements the "get" method for a Map to determine if a user attribute
 * is available.
 * <br/>The supplied key should be a string.
 * @param key the key for the user attribute to check
 * @return Boolean.TRUE if the attribute has been configured
 */
@Override
public Boolean get(Object key) {
  if ((_activeAttributes != null) && (key != null) && (key instanceof String)) {
    return new Boolean(_activeAttributes.containsKey(key));
  }
  return Boolean.FALSE;
}

}
