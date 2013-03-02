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

/**
 * Defines the functions supported by an IdentityAdapter.
 */
public class IdentitySupport {

// class variables =============================================================

// instance variables ==========================================================
private boolean _supportsGroupManagement       = false;
private boolean _supportsLogin                 = true;
private boolean _supportsLogout                = true;
private boolean _supportsPasswordChange        = false;
private boolean _supportsPasswordRecovery      = false;
private boolean _supportsUserManagement        = false;
private boolean _supportsUserProfileManagement = false;
private boolean _supportsUserRegistration      = false;

// constructors ================================================================

/** Default constructor. */
public IdentitySupport() {}

// properties ==================================================================

/**
 * Gets the group management support status (administrative).
 * <br/> The default value is false.
 * @return true if supported
 */
private boolean getSupportsGroupManagement() {
  return _supportsGroupManagement;
}
/**
 * Sets the group management support status (administrative).
 * @param hasSupport true if supported
 */
private void setSupportsGroupManagement(boolean hasSupport) {
  _supportsGroupManagement = hasSupport;
}

/**
 * Gets the the login support status.
 * <br/> The default value is true.
 * @return true if supported
 */
public boolean getSupportsLogin() {
  return _supportsLogin;
}
/**
 * Sets the the login support status.
 * @param hasSupport true if supported
 */
public void setSupportsLogin(boolean hasSupport) {
  _supportsLogin = hasSupport;
}

/**
 * Gets the logout support status.
 * <br/> The default value is true.
 * @return true if supported
 */
public boolean getSupportsLogout() {
  return _supportsLogout;
}
/**
 * Sets the logout support status.
 * @param hasSupport true if supported
 */
public void setSupportsLogout(boolean hasSupport) {
  _supportsLogout = hasSupport;
}

/**
 * Gets the password change support status.
 * <br/> The default value is false.
 * @return true if supported
 */
public boolean getSupportsPasswordChange() {
  return _supportsPasswordChange;
}
/**
 * Sets the password change support status.
 * @param hasSupport true if supported
 */
public void setSupportsPasswordChange(boolean hasSupport) {
  _supportsPasswordChange = hasSupport;
}

/**
 * Gets the password recovery support status.
 * <br/> The default value is false.
 * @return true if supported
 */
public boolean getSupportsPasswordRecovery() {
  return _supportsPasswordRecovery;
}
/**
 * Sets the password recovery support status.
 * @param hasSupport true if supported
 */
public void setSupportsPasswordRecovery(boolean hasSupport) {
  _supportsPasswordRecovery = hasSupport;
}

/**
 * Gets the user management support status (administrative).
 * <br/> The default value is false.
 * @return true if supported
 */
private boolean getSupportsUserManagement() {
  return _supportsUserManagement;
}
/**
 * Sets the user management support status (administrative).
 * @param hasSupport true if supported
 */
private void setSupportsUserManagement(boolean hasSupport) {
  _supportsUserManagement = hasSupport;
}

/**
 * Gets the user profile management support status.
 * <br/> The default value is false.
 * @return true if supported
 */
public boolean getSupportsUserProfileManagement() {
  return _supportsUserProfileManagement;
}
/**
 * Sets the user profile management support status.
 * @param hasSupport true if supported
 */
public void setSupportsUserProfileManagement(boolean hasSupport) {
  _supportsUserProfileManagement = hasSupport;
}

/**
 * Gets the user registration support status.
 * <br/> The default value is false.
 * @return true if supported
 */
public boolean getSupportsUserRegistration() {
  return _supportsUserRegistration;
}
/**
 * Sets the user registration support status.
 * @param hasSupport true if supported
 */
public void setSupportsUserRegistration(boolean hasSupport) {
  _supportsUserRegistration = hasSupport;
}

// methods =====================================================================

}
