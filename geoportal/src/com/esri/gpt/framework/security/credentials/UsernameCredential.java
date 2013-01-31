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
package com.esri.gpt.framework.security.credentials;
import com.esri.gpt.framework.util.Val;

/**
 * Stores a username credential.
 * <p>
 * This credential should not be used in a challenge/response situation.
 * It should only be used when a trusted mechanism (typically single sign-on)
 * has supplied a username.
 */
public class UsernameCredential extends Credentials {

// class variables =============================================================

// instance variables ==========================================================
private String _username = "";

// constructors ================================================================

/** Default constructor. */
public UsernameCredential() {
  super();
}

/**
 * Constructs with a supplied username.
 * @param username the username
 */
public UsernameCredential(String username) {
  super();
  setUsername(username);
}

// properties ==================================================================

/**
 * Gets the username.
 * @return the username
 */
public String getUsername() {
  return _username;
}
/**
 * Sets the username.
 * @param username the username
 */
public void setUsername(String username) {
  _username = Val.chkStr(username);
}

// methods =====================================================================

}

