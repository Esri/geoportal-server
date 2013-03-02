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
import com.esri.gpt.framework.context.Configuration;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;

/**
 * Stores the primary configuration properties for accessing an
 * external LDAP identity store.
 */
public class LdapConfiguration extends Configuration {
  
// class variables =============================================================

// instance variables ==========================================================
private LdapConnectionProperties _connectionProperties;
private LdapGroupProperties      _groupProperties;
private IdentityConfiguration    _identityConfiguration;
private LdapUserProperties       _userProperties;
  
// constructors ================================================================

/** Default constructor. */
public LdapConfiguration() {
  this(null);
}

/** Construct with a parent identity configuration
 * @param idConfig the parent identity configuration
 */
public LdapConfiguration(IdentityConfiguration idConfig) {
  setIdentityConfiguration(idConfig);
  setConnectionProperties(new LdapConnectionProperties());
  setGroupProperties(new LdapGroupProperties());
  setUserProperties(new LdapUserProperties());
}

// properties ==================================================================

/**
 * Gets the configured LDAP connection properties.
 * @return the connection properties
 */
public LdapConnectionProperties getConnectionProperties(){
  return _connectionProperties;
}
/**
 * Sets the configured LDAP connection properties.
 * @param properties the connection properties
 */
public void setConnectionProperties(LdapConnectionProperties properties){
  _connectionProperties = properties;
}

/**
 * Gets the configured LDAP group properties.
 * @return the group properties
 */
public LdapGroupProperties getGroupProperties(){
  return _groupProperties;
}
/**
 * Sets the configured LDAP group properties.
 * @param properties the group properties
 */
public void setGroupProperties(LdapGroupProperties properties){
  _groupProperties = properties;
}

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
  if (_identityConfiguration == null) {
    _identityConfiguration = new IdentityConfiguration();
  }
}

/**
 * Gets the configured LDAP user properties.
 * @return the user properties
 */
public LdapUserProperties getUserProperties(){
  return _userProperties;
}
/**
 * Sets the configured LDAP user properties.
 * @param properties the user properties
 */
public void setUserProperties(LdapUserProperties properties){
  _userProperties = properties;
}

// methods =====================================================================

/**
 * Returns the string representation of the object.
 * @return the string
 */
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(getConnectionProperties()).append("\n");
  sb.append(getUserProperties()).append("\n");
  sb.append(getGroupProperties()).append("\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}
