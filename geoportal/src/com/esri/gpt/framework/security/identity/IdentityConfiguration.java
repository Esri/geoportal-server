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

import com.esri.gpt.framework.context.Configuration;
import com.esri.gpt.framework.security.identity.ldap.LdapConfiguration;
import com.esri.gpt.framework.security.identity.local.SimpleIdentityConfiguration;
import com.esri.gpt.framework.security.identity.open.OpenProvider;
import com.esri.gpt.framework.security.identity.open.OpenProviders;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.Roles;
import com.esri.gpt.framework.security.principal.UserAttributeMap;
import com.esri.gpt.framework.util.Val;

/**
 * Identity management configuration information.
 */
public class IdentityConfiguration extends Configuration {

// instance variables ==========================================================
private String                      _adapterClassName = "";
private String                      _catalogAdminDN = "";
private Roles                       _configuredRoles;
private String                      _encKey = "";
private LdapConfiguration           _ldapConfiguration;
private Groups                      _metadataManagementGroups;
private String                      _name;
private OpenProviders               _openProviders = new OpenProviders();
private String                      _realm;
private SimpleIdentityConfiguration _simpleConfiguration;
private SingleSignOnMechanism       _ssoMechanism;
private IdentitySupport             _supportedFunctions;
private UserAttributeMap            _userAttributeMap;

/** Default constructor. */
public IdentityConfiguration() {
  setUserAttributeMap(new UserAttributeMap());
  setConfiguredRoles(new Roles());
  setSupportedFunctions(new IdentitySupport());
  setLdapConfiguration(new LdapConfiguration(this));
  setMetadataManagementGroups(new Groups());
  setSimpleConfiguration(new SimpleIdentityConfiguration(this));
  setSingleSignOnMechanism(new SingleSignOnMechanism());
}

// properties ==================================================================
/**
 * Gets the adapter class name.
 * @return the adapter class name
 */
public String getAdapterClassName() {
  return _adapterClassName;
}

/**
 * Sets the adapter class name.
 * @param adapterClassName the adapter class name
 */
public void setAdapterClassName(String adapterClassName) {
  _adapterClassName = Val.chkStr(adapterClassName);
}

/**
 * Gets the distinguished name for the catalog administration account.
 * @return the distinguished name
 */
public String getCatalogAdminDN() {
  return _catalogAdminDN;
}

/**
 * Sets the distinguished name for the catalog administration account.
 * @param dn the distinguished name
 */
public void setCatalogAdminDN(String dn) {
  _catalogAdminDN = Val.chkStr(dn);
}

/**
 * Gets the configured roles for the application.
 * @return the configured roles
 */
public Roles getConfiguredRoles() {
  return _configuredRoles;
}

/**
 * Sets the LDAP configuration.
 * @param roles the configured roles
 */
protected void setConfiguredRoles(Roles roles) {
  _configuredRoles = roles;
}

/**
 * Gets encryption key.
 * @return encryption key
 */
public String getEncKey() {
  return _encKey;
}
/**
 * Sets encryption key.
 * @param encKey encryption key
 */
public void setEncKey(String encKey) {
  _encKey = Val.chkStr(encKey);
}

/**
 * Gets the LDAP configuration.
 * @return the configuration
 */
public LdapConfiguration getLdapConfiguration() {
  return _ldapConfiguration;
}

/**
 * Sets the LDAP configuration.
 * @param configuration the configuration
 */
private void setLdapConfiguration(LdapConfiguration configuration) {
  _ldapConfiguration = configuration;
}

/**
 * Gets the configured metadata management groups for the application.
 * @return the configured metadata management groups
 */
public Groups getMetadataManagementGroups() {
  return _metadataManagementGroups;
}
/**
 * Sets the configured metadata management groups for the application.
 * @param groups the configured metadata management groups
 */
protected void setMetadataManagementGroups(Groups groups) {
  _metadataManagementGroups = groups;
}

/**
 * Gets the name associated with this configuration.
 * @return the name
 */
public String getName() {
  return _name;
}
/**
 * Sets the name associated with this configuration.
 * @param name the name
 */
public void setName(String name) {
  _name = Val.chkStr(name);
}

/**
 * Gets the configured Openid or oAuth providers.
 * @return the open providers
 */
public OpenProviders getOpenProviders() {
  return _openProviders;
}
/**
 * Sets the configured Openid or oAuth providers.
 * @param providers the open providers
 */
public void setOpenProviders(OpenProviders providers) {
  _openProviders = providers;
}

/**
 * Gets the realm (used as an identifier during HTTP 401 credential challenge/response).
 * @return the realm
 */
public String getRealm() {
  return _realm;
}
/**
 * Sets the realm (used as an identifier during HTTP 401 credential challenge/response).
 * @param realm the realm
 */
public void setRealm(String realm) {
  _realm = Val.chkStr(realm);
}

/**
 * Gets the simple identity configuration.
 * <br/>For a simple installation of one known user (the administrator).
 * @return the configuration
 */
public SimpleIdentityConfiguration getSimpleConfiguration() {
  return _simpleConfiguration;
}

/**
 * Sets the simple configuration.
 * <br/>For a simple installation of one known user (the administrator).
 * @param configuration the configuration
 */
private void setSimpleConfiguration(SimpleIdentityConfiguration configuration) {
  _simpleConfiguration = configuration;
}

/**
 * Gets the single sign-on mechanism.
 * @return the single sign-on mechanism
 */
public SingleSignOnMechanism getSingleSignOnMechanism() {
  return _ssoMechanism;
}

/**
 * Sets the single sign-on mechanism
 * @param mechanism the single sign-on mechanism
 */
private void setSingleSignOnMechanism(SingleSignOnMechanism mechanism) {
  _ssoMechanism = mechanism;
}

/**
 * Gets the supported functions.
 * @return the supported functions
 */
public IdentitySupport getSupportedFunctions() {
  return _supportedFunctions;
}

/**
 * Sets the supported functions.
 * @param support the supported functions
 */
private void setSupportedFunctions(IdentitySupport support) {
  _supportedFunctions = support;
}

/**
 * Gets the configured user attribute map.
 * @return the configured user attribute map
 */
public UserAttributeMap getUserAttributeMap() {
  return _userAttributeMap;
}

/**
 * Sets the configured user attribute map.
 * @param map the configured user attribute map
 */
private void setUserAttributeMap(UserAttributeMap map) {
  _userAttributeMap = map;
}

// methods =====================================================================

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" name=\"").append(getName()).append("\"\n");
  sb.append(" adapterClassName=\"").append(getAdapterClassName()).append("\"\n");
  sb.append(" catalogAminDN=").append(getCatalogAdminDN()).append("\n");
  sb.append(getConfiguredRoles()).append("\n");
  sb.append(getSingleSignOnMechanism()).append("\n");
  sb.append(getSupportedFunctions()).append("\n");  
  if (getAdapterClassName().endsWith("SimpleIdentityAdapter")) {
    sb.append(getSimpleConfiguration()).append("\n");
  } else {
    sb.append(getLdapConfiguration()).append("\n");
    sb.append("metadataManagementGroups ").append(getMetadataManagementGroups()).append("\n");
  }
  if ((this.getOpenProviders() != null) && (this.getOpenProviders().size() > 0)) {
    for (OpenProvider op: this.getOpenProviders().values()) {
      sb.append(op).append("\n");
    }
  }
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}
}
