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
package com.esri.gpt.framework.security.principal;

/**
 * Represents a group within the system.
 */
public class Group extends SecurityPrincipal {

// class variables =============================================================

// instance variables ==========================================================
 
// constructors ================================================================

/** Default constructor. */
public Group() {
  this("");
}

/**
* Construct with a supplied key.
* @param key the key
*/
public Group(String key) {
  super(key);
}

/**
 * Construct with an id, a key and a name.
 * @param id the id.
 * @param key the key
 * @param name name of the group.
 */
public Group(int id, String key, String name) {
  //setId(id);
  //setKey(key);
  //setName(name);
}

// properties ==================================================================

// methods =====================================================================

}
