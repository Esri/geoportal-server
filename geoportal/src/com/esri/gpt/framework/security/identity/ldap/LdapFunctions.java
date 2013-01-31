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
import com.esri.gpt.framework.util.Val;
import javax.naming.NamingEnumeration;

/**
 * Super-class for LDAP query/edit functionality.
 */
public class LdapFunctions {

// class variables =============================================================

// instance variables ==========================================================
private LdapConfiguration _configuration = null;

// constructors ================================================================

/** Default constructor. */
protected LdapFunctions() {
  this(null);
}

/**
 * Construct with a supplied configuration.
 * @param configuration the configuration
 */
protected LdapFunctions(LdapConfiguration configuration) {
  this.setConfiguration(configuration);
}

// properties ==================================================================

/**
 * Gets the LDAP configuration.
 * @return the configuration
 */
protected LdapConfiguration getConfiguration() {
  return _configuration;
}
/**
 * Sets the LDAP configuration.
 * @param configuration the configuration
 */
protected void setConfiguration(LdapConfiguration configuration) {
  _configuration = configuration;
}

// methods =====================================================================

/**
 * Builds a full distinguished name based upon a relative DN and base DN.
 * @param objectDN the relative object DN
 * @param baseDN the base DN (for a search)
 * @return the full DN
 */
protected String buildFullDN(String objectDN, String baseDN) {
  objectDN = Val.chkStr(objectDN).toLowerCase();
  if (objectDN.length() > 0) {
    baseDN = Val.chkStr(baseDN).toLowerCase();
    if (baseDN.length() > 0) {
      boolean bForce = false;
      if (bForce || !objectDN.endsWith(","+baseDN)) {
        objectDN += ","+baseDN;
      }
    }
  }
  return objectDN;
}

/**
 * Closes a naming enumeration (if open).
 * @param en the enumeration to close
 */
protected void closeEnumeration(NamingEnumeration<? extends Object> en) {
  try {
    if (en != null) {
      en.close();
    } 
  } catch (Exception e) {}
}

}

