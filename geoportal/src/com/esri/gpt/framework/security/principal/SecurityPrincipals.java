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
import com.esri.gpt.framework.collection.CaseInsensitiveMap;

/**
 * Super-class for a SecurityPrincipal collection.
 */
public class SecurityPrincipals<T extends SecurityPrincipal> 
       extends CaseInsensitiveMap<T> {
  
// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public SecurityPrincipals() {
  this(false);
}

/**
 * Construct with a flag indicating if an empty map key is allowed.
 * @param allowEmptyKey true if an empty key is allowed
 */
public SecurityPrincipals(boolean allowEmptyKey) {
  super(allowEmptyKey);
}

// properties ==================================================================

// methods =====================================================================

/**
 * Adds a member to the collection.
 * <br/>The member will not be added if it is null.
 * @param member the member to add
 */
public void add(T member) {
  if (member != null) {
    put(member.getKey(),member);
  }
}

}
