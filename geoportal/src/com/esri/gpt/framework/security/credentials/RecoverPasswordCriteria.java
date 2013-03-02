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
 * Criteria associated with a password recovery request.
 */
public class RecoverPasswordCriteria {

// class variables =============================================================

// instance variables ==========================================================
private String _emailAddress = "";
private String _username     = "";
  
// constructors ================================================================

/** Default constructor. */
public RecoverPasswordCriteria() {}

// properties ==================================================================

/**
* Gets the email address.
* @return the email address
*/
public String getEmailAddress() {
  return _emailAddress;
}

/**
* Sets the email address.
* @param emailAddress the email address
*/
public void setEmailAddress(String emailAddress) {
  _emailAddress = Val.chkStr(emailAddress);
}

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
