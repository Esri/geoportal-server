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
package com.esri.gpt.framework.security.identity;
import com.esri.gpt.framework.security.principal.RoleSet;

/**
 * Authentication status associated with a user.
 */
public class AuthenticationStatus {

// class variables =============================================================

// instance variables ==========================================================
private RoleSet _authenticatedRoles = new RoleSet();
private boolean _wasAuthenticated = false;
  
// constructors ================================================================

/** Default constructor. */
public AuthenticationStatus() {}
  
// properties ==================================================================

/**
 * Gets the set of authenticated roles.
 * @return the set of authenticated roles
 */
public RoleSet getAuthenticatedRoles() {
  return _authenticatedRoles;
}

/**
 * Gets the status indicating whether of not authentication was successful.
 * @return true if authentication was successful
 */
public boolean getWasAuthenticated() {
  return _wasAuthenticated;
}
/**
 * Sets the status indicating whether of not authentication was successful.
 * @param wasAuthenticated true if authentication was successful
 */
public void setWasAuthenticated(boolean wasAuthenticated) {
  _wasAuthenticated = wasAuthenticated;
}

// methods =====================================================================

/**
 * Asserts that the active user is logged in.
 * @throws NotAuthorizedException if the user is not logged in
 */
public void assertLoggedIn() throws NotAuthorizedException {
  if (!getWasAuthenticated()) {
    throw new NotAuthorizedException("Not authorized.");
  }
}

/**
 * Authorizes an action based upon the authenticated roles for the
 * current user and a supplied role set for the action.
 * <br/>The current user must have at least one of the roles associated
 * with the action.
 * <br/>If the supplied rolseForAction is null or empty, the action is authorized.
 * @param rolesForAction the set of roles associated with the action
 * @throws NotAuthorizedException if the user does not have a required role
 */
public void authorizeAction(RoleSet rolesForAction)
  throws NotAuthorizedException {
  if ((rolesForAction != null) && (rolesForAction.size() > 0)) {
    if (!getWasAuthenticated()) {
      throw new NotAuthorizedException("Not authorized.");
    }
    RoleSet rolesForUser = getAuthenticatedRoles();
    rolesForUser.assertRole(rolesForAction);
  }
}

/**
 * Resets the authentication status.
 */
public void reset() {
  setWasAuthenticated(false);
  getAuthenticatedRoles().clear();
}
  
}
