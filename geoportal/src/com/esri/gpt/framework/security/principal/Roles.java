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
import com.esri.gpt.framework.util.Val;

/**
 * Defines a Role collection.
 */
public class Roles extends SecurityPrincipals<Role> {

// class variables =============================================================

// instance variables ==========================================================
private boolean _authenticatedUserRequiresRole = true;
private String  _registeredUserRoleKey = "";
 
// constructors ================================================================

/** Default constructor. */
public Roles() {
  super(false);
}

// properties ==================================================================

/**
 * Gets the policy regarding authenticated users and role membership.
 * <br/>If true, a user must authenticate and belong to a role based
 * group to gain access.
 * <br/>If false, any user that passes authentication will be allowed access.
 * @return the policy
 */
public boolean getAuthenticatedUserRequiresRole() {
  return _authenticatedUserRequiresRole;
}
/**
 * Sets the policy regarding authenticated users and role membership.
 * <br/>If true, a user must authenticate and belong to a role based
 * group to gain access.
 * <br/>If false, any user that passes authentication will be allowed access.
 * @param roleRequired the policy
 */
public void setAuthenticatedUserRequiresRole(boolean roleRequired) {
  _authenticatedUserRequiresRole = roleRequired;
}

/**
 * Gets the key associated with the registered user role.
 * @return the distinguished name
 */
public String getRegisteredUserRoleKey() {
  return _registeredUserRoleKey;
}
/**
 * Sets the key associated with the registered user role.
 * @param key the associated key
 */
public void setRegisteredUserRoleKey(String key) {
  _registeredUserRoleKey = Val.chkStr(key);
}

// methods =====================================================================

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" authenticatedUserRequiresRole=\"").append(
      getAuthenticatedUserRequiresRole()).append("\"\n");
  sb.append(" registeredUserRoleKey=\"").append(
      getRegisteredUserRoleKey()).append("\"\n");
  for (Role role: values()) {
    sb.append(role).append("\n");
  }
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}


}
