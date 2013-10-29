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
 * Represents an application role within the system.
 */
public class Role extends SecurityPrincipal {

// class variables =============================================================

// instance variables ==========================================================
private RoleSet _fullRoleSet = new RoleSet();
private String  _inherits = "";
private String  _resKey = "";
private boolean _manage = true;
private boolean _forbidden = false;
 
// constructors ================================================================

/** Default constructor. */
public Role() {
  this("");
}

/**
* Construct with a supplied key.
* @param key the key
*/
public Role(String key) {
  super(key);
}

// properties ==================================================================

/**
 * Gets full set of role keys applicable to this role.
 * <br/>The set includes this role key, plus all inherited role keys.
 * @return the full role set
 */
public RoleSet getFullRoleSet(){
  return _fullRoleSet;
}

/**
 * Sets full set of role keys applicable to this role.
 * <br/>The set includes this role key, plus all inherited role keys.
 * @param roleSet the full role set
 */
private void setFullRoleSet(RoleSet roleSet){
  _fullRoleSet = roleSet;
}

/**
 * Gets the configured inheritance specification string.
 * @return the inheritance string
 */
public String getInherits(){
  return _inherits;
}
/**
 * Sets the configured inheritance specification string.
 * @param inherits the inheritance string
 */
public void setInherits(String inherits){
  _inherits = Val.chkStr(inherits);
}

/**
 * Is role to be managed
 * @return if true the role is manage on user management page
 */
public boolean isManage() {
	return _manage;
}

/**
 * Set the manage role attribute
 * @param manage
 */
public void setManage(boolean manage) {
	this._manage = manage;
}

/**
 * Gets resource key configured for role
 * @return resource key for the role
 */
public String getResKey() {
	return _resKey;
}
/**
 * Sets resource key configuration
 * @param resKey
 */
public void setResKey(String resKey) {
	this._resKey = Val.chkStr(resKey);
}

/**
 * Check if role is configured to be forbidden from access
 * @return true if forbidden
 */
public boolean isForbidden() {
	return _forbidden;
}

/**
 * Sets forbidden configuration.
 * @param forbidden
 */
public void setForbidden(boolean forbidden) {
	this._forbidden = forbidden;
}

// methods =====================================================================

/**
 * Appends an inherited role to the full role set.
 * @param allRoles the collection of roles associated with the application configuration
 * @param roleToInherit the role that this role will inherit
 */
private void appendInheritedRole(Roles allRoles, Role roleToInherit) {
  if ((roleToInherit != null) && !getFullRoleSet().containsString(roleToInherit.getKey())) {
    getFullRoleSet().add(roleToInherit.getKey());
    RoleSet inheritedSet = new RoleSet();
    inheritedSet.addDelimited(roleToInherit.getInherits());
    for (String sKey: inheritedSet) {
      appendInheritedRole(allRoles,allRoles.get(sKey));
    }
  }
}

/**
 * Builds the full role set associated with this role.
 * <br/>The full role set will contains this role plus all inherited roles.
 * @param allRoles the collection of roles associated with the application configuration
 */
public void buildFullRoleSet(Roles allRoles) {
  getFullRoleSet().clear();
  appendInheritedRole(allRoles,this);
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" key=\"").append(getKey()).append("\"\n");
  sb.append(" ldapGroupDN=\"").append(getDistinguishedName()).append("\"\n");
  sb.append(" inherits=\"").append(getInherits()).append("\"\n");
  sb.append(" manage=\"").append(isManage()).append("\"\n");
  sb.append(" forbidden=\"").append(isForbidden()).append("\"\n");
  sb.append(" fullRoleSet=").append(getFullRoleSet()).append("\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}
