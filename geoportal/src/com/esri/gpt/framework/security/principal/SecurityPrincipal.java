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
package com.esri.gpt.framework.security.principal;
import com.esri.gpt.framework.util.Val;

/**
 * Super-class for a security principal.
 */
public class SecurityPrincipal {
  
// class variables =============================================================

// instance variables ==========================================================
private String _distinguishedName = "";
private String _key = "";
private int    _localID = -1;
private String _name = "";
  
// constructors ================================================================

/** Default constructor. */
public SecurityPrincipal() {
  this("");
}

/**
 * Construct with a supplied key.
 * @param key the key
 */
public SecurityPrincipal(String key) {
  setKey(key);
}

// properties ==================================================================

/**
 * Gets the distinguished name for this principal.
 * <br/>The distinguished name is typically used for an LDAP reference.
 * @return the distinguished name
 */
public String getDistinguishedName() {
  return _distinguishedName;
}
/**
 * Sets the distinguished name for this principal.
 * <br/>The distinguished name is typically used for an LDAP reference.
 * <br/>The name is trimmed and stored in lower-case.
 * @param name the distinguished name
 */
public void setDistinguishedName(String name) {
  _distinguishedName = Val.chkStr(name).toLowerCase();
}

/**
 * Gets the key.
 * @return the key
 */
public String getKey() {
  return _key;
}
/**
 * Sets the key.
 * <br/>The key will be trimmed. A null key is treated as an empty string.
 * @param key the key
 */
public void setKey(String key) {
  _key = Val.chkStr(key);
}

/**
 * Gets the local ID for this principal.
 * <br/>The local ID is the primary key for a principal
 * @return the local ID
 */
public int getLocalID() {
  return _localID;
}
/**
 * Sets the local ID for this principal.
 * <br/>The local ID is the primary key for a principal
 * @param localID the local id
 */
public void setLocalID(int localID) {
  _localID = localID;
}

/**
 * Gets the name for this principal.
 * @return the name
 */
public String getName() {
  return _name;
}
/**
 * Sets the name for this principal.
 * @param name the name
 */
public void setName(String name) {
  _name = Val.chkStr(name);
}

// methods =====================================================================

/**
 * Reset.
 */
public void reset() {
  setKey("");
  setName("");
  setLocalID(-1);
}
/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(":");
  sb.append(" key=\"").append(getKey()).append("\"");
  sb.append(" distinguishedName=\"").append(getDistinguishedName()).append("\"");
  sb.append(" localID=\"").append(getLocalID()).append("\"");
  sb.append(" name=\"").append(getName()).append("\"");
  return sb.toString();
}

}
