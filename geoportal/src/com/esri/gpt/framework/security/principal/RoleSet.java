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
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;

/**
 * Maintains a set of role keys.
 * <p>
 * The intent is to use this set for quick evaluation of
 * authorization constraints.
 * <p>
 * Each value in the collection is a String. Null values are ignored. 
 * Values are trimmed, zero-length Strings are ignored. The set is 
 * unique and is not case sensitive.
 */
public class RoleSet extends StringSet {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public RoleSet() {
  super(false,false,true);
}

// properties ==================================================================

// methods =====================================================================

/**
 * Asserts that the current user has a required role.
 * <br/>If the user does not have the required role, a
 * NotAuthorizedException exception is thrown.
 * @param roleKey the key associated with the required role
 * @throws NotAuthorizedException if the user does not have a required role
 */
public void assertRole(String roleKey) throws NotAuthorizedException {
  if (!hasRole(roleKey)) {
    throw new NotAuthorizedException("Not authorized.");
  }
}

/**
 * Asserts that the current user has at least one of
 * the roles specified in the supplied role set.
 * <br/>If the user does not have the required role, a
 * NotAuthorizedException exception is thrown.
 * @param roleSet the set of roles to check
 * @throws NotAuthorizedException if the user does not have a required role
 */
public void assertRole(RoleSet roleSet) throws NotAuthorizedException {
  if (!hasRole(roleSet)) {
    throw new NotAuthorizedException("Not authorized.");
  }
}

/**
 * Determines if this set has a specified role.
 * @param roleKey the key associated with the role to check
 */
public boolean hasRole(String roleKey) {
  return contains(roleKey);
}

/**
 * Determines if the current set has at least one of
 * the roles specified in the supplied role set.
 * @param roleSet the set of roles to check
 */
public boolean hasRole(RoleSet roleSet) {
  boolean bHasRole = false;
  for (String sKey: roleSet) {
    if (hasRole(sKey)) {
      bHasRole = true;
      break;
    }
  }
  return bHasRole;
}

}

