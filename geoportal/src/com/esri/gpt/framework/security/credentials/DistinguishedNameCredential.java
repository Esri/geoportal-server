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
 * Stores a distinguished name credential.
 * <p>
 * The distinguished name is typically used for an LDAP reference.
 * <p>
 * This credential should not be used in a challenge/response situation.
 * It should only be used when a trusted mechanism has supplied a 
 * distinguished name.
 */
public class DistinguishedNameCredential extends Credentials {

// class variables =============================================================

// instance variables ==========================================================
private String _distinguishedName = "";

// constructors ================================================================

/** Default constructor. */
public DistinguishedNameCredential() {
  super();
}

/**
 * Constructs with a supplied distinguished name.
 * @param distinguishedName the distinguishedName
 */
public DistinguishedNameCredential(String distinguishedName) {
  super();
  setDistinguishedName(distinguishedName);
}

// properties ==================================================================

/**
 * Gets the distinguished name for this user.
 * <br/>The distinguished name is typically used for an LDAP reference.
 * @return the distinguished name
 */
public String getDistinguishedName() {
  return _distinguishedName;
}
/**
 * Sets the distinguished name for this user.
 * <br/>The distinguished name is typically used for an LDAP reference.
 * <br/>The name is trimmed and stored in lower-case.
 * @param name the distinguished name
 */
public void setDistinguishedName(String name) {
  _distinguishedName = Val.chkStr(name).toLowerCase();
}

// methods =====================================================================

}

