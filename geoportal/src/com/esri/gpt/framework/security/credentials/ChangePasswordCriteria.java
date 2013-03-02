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
import com.esri.gpt.framework.security.principal.User;

/**
 * Criteria associated with a change password request.
 */
public class ChangePasswordCriteria {

// class variables =============================================================

// instance variables ==========================================================
private UsernamePasswordCredentials _newCredentials;
private UsernamePasswordCredentials _origCredentials;
private User                        _subjectUser = null;
  
// constructors ================================================================

/** Default constructor. */
public ChangePasswordCriteria() {
  setNewCredentials(new UsernamePasswordCredentials());
  setOriginalCredentials(new UsernamePasswordCredentials());
}

// properties ==================================================================

/**
 * Gets the new credentials.
 * @return the new credentials
 */
public UsernamePasswordCredentials getNewCredentials() {
  return _newCredentials;
}
/**
 * Sets the new credentials.
 * @param credentials the new credentials
 */
private void setNewCredentials(UsernamePasswordCredentials credentials) {
  _newCredentials = credentials;
}

/**
 * Gets the original credentials.
 * @return the original credentials
 */
public UsernamePasswordCredentials getOriginalCredentials() {
  return _origCredentials;
}
/**
 * Sets the original credentials.
 * @param credentials the original credentials
 */
private void setOriginalCredentials(UsernamePasswordCredentials credentials) {
  _origCredentials = credentials;
}

/**
 * Gets the user for which the password will be changed.
 * @return the subject user
 */
public User getSubjectUser() {
  return _subjectUser;
}
/**
 * Sets the user for which the password will be changed.
 * param user the subject user
 */
public void setSubjectUser(User user) {
  _subjectUser = user;
}

// methods =====================================================================

}
