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
package  com.esri.gpt.framework.security.identity.ldap;
import com.esri.gpt.framework.security.principal.UserAttribute;
import com.esri.gpt.framework.security.principal.UserAttributeMap;
import com.esri.gpt.framework.util.Val;
import java.util.HashMap;

/**
 * Maps application names to LDAP names.
 */
public class LdapNameMapping {


// class variables =============================================================

// instance variables ==========================================================
private HashMap<String,String> _hmApplicationNames = new HashMap<String,String>();
private HashMap<String,String> _hmLdapNames = new HashMap<String,String>();
private boolean                _useLowercaseKeys = true;

// constructors ================================================================

/** Default constructor. */
public LdapNameMapping() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Adds a naming reference.
 * @param applicationName the application reference name
 * @param ldapName the corresponding LDAP reference name
 */
public void add(String applicationName, String ldapName) {
  applicationName = Val.chkStr(applicationName);
  ldapName = Val.chkStr(ldapName);
  if ((applicationName.length() > 0) && (ldapName.length() > 0)) {
    if (_useLowercaseKeys) {
      _hmApplicationNames.put(applicationName.toLowerCase(),ldapName);
      _hmLdapNames.put(ldapName.toLowerCase(),applicationName);
    } else {
      _hmApplicationNames.put(applicationName,ldapName);
      _hmLdapNames.put(ldapName,applicationName);      
    }
  }
}

/**
 * Clears the mapping.
 */
private void clear() {
  _hmApplicationNames.clear();
  _hmLdapNames.clear();
}

/**
 * Configures the mapping from a supplied user attribute map.
 * @param configuredAttributes the configured user attributes
 */
public void configureFromUserAttributes(UserAttributeMap configuredAttributes) {
  clear();
  if (configuredAttributes != null) {
    for (UserAttribute attr: configuredAttributes.values()) {
      add(attr.getKey(),attr.getLdapName());
    }
  }
}

/**
 * Finds the application name associated with and LDAP name.
 * @param ldapName the known LDAP name
 * @return the corresponding application name (zero-length string if none)
 */
public String findApplicationName(String ldapName) {
  if (_useLowercaseKeys) {
    ldapName = Val.chkStr(ldapName).toLowerCase();
  } 
  return Val.chkStr((String)_hmLdapNames.get(ldapName));
}
  
/**
 * Finds the LDAP name associated with an application name.
 * @param applicationName the known application name
 * @return the corresponding LDAP name (zero-length string if none)
 */
public String findLdapName(String applicationName) {
  if (_useLowercaseKeys) {
    applicationName = Val.chkStr(applicationName).toLowerCase();
  }
  return Val.chkStr((String)_hmApplicationNames.get(applicationName));
}

}

