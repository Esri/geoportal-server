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
package com.esri.gpt.framework.security.identity.local;
import com.esri.gpt.framework.context.Configuration;
import com.esri.gpt.framework.security.credentials.Credentials;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;

/**
 * Configuration information adapter for a simple installation of one known user (the administrator).
 */
public class SimpleIdentityConfiguration extends Configuration {
  
// class variables =============================================================

// instance variables ==========================================================
private IdentityConfiguration _identityConfiguration;
private UsernamePasswordCredentials _serviceAccountCredentials = null;
  
// constructors ================================================================

/** Construct with a parent identity configuration
 * @param idConfig the parent identity configuration
 */
public SimpleIdentityConfiguration(IdentityConfiguration idConfig) {
  setIdentityConfiguration(idConfig);
}

// properties ==================================================================

/**
 * Gets the identity configuration associated with this application.
 * @return the identity configuration
 */
public IdentityConfiguration getIdentityConfiguration() {
  return _identityConfiguration;
}
/**
 * Sets the identity configuration associated with this application.
 * @param configuration the identity configuration
 */
private void setIdentityConfiguration(IdentityConfiguration configuration) {
  _identityConfiguration = configuration;
  if (_identityConfiguration == null) _identityConfiguration = new IdentityConfiguration();
}

/**
 * Gets the credentials for the service account.
 * @return the credentials
 */
public UsernamePasswordCredentials getServiceAccountCredentials() {
  return _serviceAccountCredentials;
}
/**
 * Sets the credentials for the service account.
 * @param credentials the credentials
 */
public void setServiceAccountCredentials(UsernamePasswordCredentials credentials) {
  _serviceAccountCredentials = credentials;
  if (_serviceAccountCredentials == null) {
    _serviceAccountCredentials = new UsernamePasswordCredentials();
  }
}

// methods =====================================================================

/**
 * Returns the string representation of the object.
 * @return the string
 */
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  Credentials cred = getServiceAccountCredentials();
  if ((cred != null) && (cred instanceof UsernamePasswordCredentials)) {
    UsernamePasswordCredentials upCred = (UsernamePasswordCredentials)cred;
    int nPwdLen = 0;
    if (upCred.getPassword() != null) {
      nPwdLen = upCred.getPassword().length();
    }
    sb.append(" serviceAccount(");
    sb.append("securityPrincipal=\"").append(upCred.getUsername()).append("\"");
    sb.append(" securityCredentials=\"");
    for (int i=0;i<nPwdLen;i++) sb.append("*");
    sb.append("\")\n");
  }
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}
