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
import com.esri.gpt.framework.security.credentials.Credentials;
import com.esri.gpt.framework.security.identity.AuthenticationStatus;

/**
 * Represents a user within the system.
 */
public class User extends SecurityPrincipal {

// class variables =============================================================

// instance variables ==========================================================
private UserAttributeMap     _attributeMap = new UserAttributeMap();
private AuthenticationStatus _authStatus = new AuthenticationStatus();
private Credentials          _credentials = null;
private Groups               _groups = new Groups();
 
// constructors ================================================================

/** Default constructor. */
public User() {
  this("");
}

/**
* Construct with a supplied key.
* @param key the key
*/
public User(String key) {
  super(key);
}

// properties ==================================================================

/**
* Gets the authentication status.
* @return the authentication status
*/
public AuthenticationStatus getAuthenticationStatus() {
  return _authStatus;
}

/**
* Sets the authentication status.
* @param authStatus authentication status
*/
public void setAuthenticationStatus(AuthenticationStatus authStatus) {
  _authStatus = authStatus;
}

/**
 * Gets the credentials for the user.
 * @return the credentials
 */
public Credentials getCredentials() {
  return _credentials;
}
/**
 * Sets the credentials for the user.
 * @param credentials the credentials
 */
public void setCredentials(Credentials credentials) {
  _credentials = credentials;
}

/**
* Gets the groups to which the user belongs.
* @return the groups
*/
public Groups getGroups() {
  return _groups;
}
/**
* Sets the groups to which the user belongs.
* @param groups the groups
*/
public void setGroups(Groups groups) {
  _groups = groups;
}

/**
 * Gets the name for this user.
 * @return the name
 */
@Override
public String getName() {
  String sName = super.getName();
  if (sName.length() > 0) {
    return sName;
  } else {
    return getProfile().getUsername();
  }
}

/**
* Gets the attribute map describing the user's profile.
* @return the attribute map
*/
public UserAttributeMap getProfile() {
  return _attributeMap;
}

/**
* Sets the attribute map describing the user's profile.
* @param map the attribute map
*/
public void setProfile(UserAttributeMap map) {
  _attributeMap = map;
}

// methods =====================================================================

/**
 * Reset.
 */
@Override
public void reset() {
  super.reset();
  setCredentials(null);
  setProfile(new UserAttributeMap());
  setGroups(new Groups());
  getAuthenticationStatus().reset();
}

}
