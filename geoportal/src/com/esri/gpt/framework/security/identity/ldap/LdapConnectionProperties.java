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
package com.esri.gpt.framework.security.identity.ldap;
import com.esri.gpt.framework.security.credentials.Credentials;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;

/**
 * Defines the configured properties for an LDAP connection.
 */
public class LdapConnectionProperties {
  
// class variables =============================================================
private static final String ICTX_FACTORY_NAME = "com.sun.jndi.ldap.LdapCtxFactory";

// instance variables ==========================================================
private String      _ictxFactoryName = "";
private String      _providerUrl = "";
private Credentials _serviceAccountCredentials = null;
private String      _securityAuthenticationLevel = "";
private String      _securityProtocol = "";
  
// constructors ================================================================

/** Default constructor. */
public LdapConnectionProperties() {
  setInitialContextFactoryName(ICTX_FACTORY_NAME);
  setSecurityAuthenticationLevel("simple");
  setServiceAccountCredentials(new UsernamePasswordCredentials());
}

// properties ==================================================================

/**
 * Gets the initial context factory name.
 * @return the initial context factory name
 */
public String getInitialContextFactoryName() {
  return _ictxFactoryName;
}
/**
 * Sets the initial context factory name.
 * @param name the initial context factory name
 */
public void setInitialContextFactoryName(String name) {
  name = Val.chkStr(name);
  if (name.length() > 0) {
    _ictxFactoryName = name;
  } else {
    _ictxFactoryName = ICTX_FACTORY_NAME;
  }
}

/**
 * Gets the LDAP provider url.
 * @return the LDAP provider url
 */
public String getProviderUrl() {
  return _providerUrl;
}
/**
 * Sets the LDAP provider url.
 * @param providerUrl the LDAP provider url
 */
public void setProviderUrl(String providerUrl) {
  _providerUrl = Val.chkStr(providerUrl);
}

/**
 * Gets the credentials for the service account.
 * @return the credentials
 */
public Credentials getServiceAccountCredentials() {
  return _serviceAccountCredentials;
}
/**
 * Sets the credentials for the service account.
 * @param credentials the credentials
 */
public void setServiceAccountCredentials(Credentials credentials) {
  _serviceAccountCredentials = credentials;
  if (_serviceAccountCredentials == null) {
    _serviceAccountCredentials = new UsernamePasswordCredentials();
  }
}

/**
 * Gets the LDAP security authentication level.
 * <br/>Corresponds to the Context.SECURITY_AUTHENTICATION setting
 * <br/>eg. "none", "simple"
 * @return the LDAP security authentication level
 */
public String getSecurityAuthenticationLevel() {
  return _securityAuthenticationLevel;
}
/**
 * Sets the LDAP security authentication level.
 * <br/>Corresponds to the Context.SECURITY_AUTHENTICATION setting
 * <br/>eg. "none", "simple"
 * @param level the LDAP security authentication level
 */
public void setSecurityAuthenticationLevel(String level) {
  _securityAuthenticationLevel = Val.chkStr(level);
}

/**
 * Gets the LDAP security protocol.
 * <br/>Corresponds to the Context.SECURITY_PROTOCOL setting
 * <br/>eg. null, "ssl"
 * @return the LDAP security protocol
 */
public String getSecurityProtocol() {
  return _securityProtocol;
}
/**
 * Sets the LDAP security protocol.
 * <br/>Corresponds to the Context.SECURITY_PROTOCOL setting
 * <br/>eg. null, "ssl"
 * @param protocol the LDAP security protocol
 */
public void setSecurityProtocol(String protocol) {
  _securityProtocol = Val.chkStr(protocol);
}

// methods =====================================================================

/**
 * Replaces all occurences of {0} within a pattern with the supplied value.
 * @param pattern the replacement pattern
 * @param value the replacement value
 */
public String replace(String pattern, String value) {
  if ((pattern.length() > 0) && (pattern.indexOf("{0}") != -1)) {
    value = pattern.replaceAll("\\{0\\}",value);
  }
  return value;
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" providerURL=\"").append(
      getProviderUrl()).append("\"\n");
  sb.append(" initialContextFactoryName=\"").append(
      getInitialContextFactoryName()).append("\"\n");
  sb.append(" securityAuthentication=\"").append(
      getSecurityAuthenticationLevel()).append("\"\n");
  sb.append(" securityProtocol=\"").append(
      getSecurityProtocol()).append("\"\n");
  
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
