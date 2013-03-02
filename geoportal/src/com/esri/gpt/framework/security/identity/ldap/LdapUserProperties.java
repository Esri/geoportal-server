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
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

/**
 * Defines the configured properties for LDAP user access.
 */
public class LdapUserProperties extends LdapProperties {
  
// class variables =============================================================

// instance variables ==========================================================
private String          _passwordEncryptionAlgorithm  = "";
private String          _userDisplayNameAttribute = "";
private String          _userDNPattern = "";
private String          _usernameSearchPattern = "";
private Attribute       _userObjectClasses;
private LdapNameMapping _userProfileMapping;
private String          _userSearchDIT = "";
private String          _userRequestsSearchPattern = "";
// constructors ================================================================

/** Default constructor. */
public LdapUserProperties() {
  super();
  setUserObjectClasses(new BasicAttribute("objectclass"));
  setUserProfileMapping(new LdapNameMapping());
}

// properties ==================================================================
public String getUserRequestsSearchPattern() {
	return _userRequestsSearchPattern;
}

public void setUserRequestsSearchPattern(String userRequestsSearchPattern) {
	this._userRequestsSearchPattern = userRequestsSearchPattern;
}

/**
 * Gets the password encryption algorithm.
 * @return the password encryption algorithm
 */
public String getPasswordEncryptionAlgorithm() {
  return _passwordEncryptionAlgorithm;
}

/**
 * Sets the password encryption algorithm.
 * @param algorithm the password encryption algorithm
 */
public void setPasswordEncryptionAlgorithm(String algorithm) {
  _passwordEncryptionAlgorithm = Val.chkStr(algorithm);
}

/**
 * Gets the name of the attribute associated with the user's display name.
 * @return the attribute name
 */
public String getUserDisplayNameAttribute() {
  return _userDisplayNameAttribute;
}
/**
 * Sets the name of the attribute associated with the user's display name.
 * @param attributeName the attribute name
 */
public void setUserDisplayNameAttribute(String attributeName) {
  _userDisplayNameAttribute = Val.chkStr(attributeName);
}

/**
 * Gets the distinguished name pattern for a new user.
 * <br/>eg. cn={0},cn=users,o=esri,c=us
 * <br/>The intent is to support user registration.
 * <br/>If a user registers "myname", the {0} section of the pattern will be replaced
 * to produce:
 * <br/>cn=myname,cn=users,o=esri,c=us
 * @return the distinguished name pattern for a new user
 */
public String getUserDNPattern() {
  return _userDNPattern;
}
/**
 * Sets the distinguished name pattern for a new user.
 * <br/>eg. cn={0},cn=users,o=esri,c=us
 * <br/>The intent is to support user registration.
 * <br/>If a user registers "myname", the {0} section of the pattern will be replaced
 * to produce:
 * <br/>cn=myname,cn=users,o=esri,c=us
 * @param pattern the distinguished name pattern for a new user
 */
public void setUserDNPattern(String pattern) {
  _userDNPattern = Val.chkStr(pattern).toLowerCase();
}

/**
 * Gets the username search pattern.
 * <br/>eg. (&(objectclass=person)(uid={0}))
 * <br/>If a user supplies "myname" on the login page, the {0}
 * section of the pattern will be replaced to produce:
 * <br/>(&(objectclass=person)(uid={myname}))
 * <br/>The search for the user will begin at the base DN defined by:
 * getUserSearchDIT()
 * @return the user login search pattern
 */
public String getUsernameSearchPattern() {
  return _usernameSearchPattern;
}
/**
 * Sets the username search pattern.
 * <br/>eg. (&(objectclass=person)(uid={0}))
 * <br/>If a user supplies "myname" on the login page, the {0}
 * section of the pattern will be replaced to produce:
 * <br/>(&(objectclass=person)(uid={myname}))
 * @param pattern the user login search pattern
 */
public void setUsernameSearchPattern(String pattern) {
  _usernameSearchPattern = Val.chkStr(pattern);
}

/**
 * Gets the required user object classes.
 * @return the required user object classes
 */
public Attribute getUserObjectClasses() {
  return _userObjectClasses;
}
/**
 * Sets the required user object classes.
 * @param userObjectClasses Object classes for a new user
 */
private void setUserObjectClasses(Attribute userObjectClasses) {
  _userObjectClasses = userObjectClasses;
}

/**
 * Gets the user profile attribute name mapping.
 * @return the name mapping for user profile attributes
 */
public LdapNameMapping getUserProfileMapping() {
  return _userProfileMapping;
}
/**
 * Sets the user profile attribute name mapping.
 * @param mapping the name mapping for user profile attributes
 */
private void setUserProfileMapping(LdapNameMapping mapping) {
  _userProfileMapping = mapping;
}

/**
 * Gets the root directory where searching of users will take place.
 * @return directory root under which all users reside
 */
public String getUserSearchDIT() {
  return _userSearchDIT;
}
/** Sets the root directory where searching of users will take place.
 * @param dit directory root under which all users reside
 */
public void setUserSearchDIT(String dit){
  _userSearchDIT = Val.chkStr(dit).toLowerCase();
}

// methods =====================================================================

/**
 * Adds user objects used to create users
 * @param className Object class to add
 */
public void addUserObjectClass(String className) {
  className = Val.chkStr(className);
  if ((className.length() > 0) && !_userObjectClasses.contains(className)) {
    _userObjectClasses.add(className);
  }
}

/**
 * Returns the DN for a user about to be registered.
 * <br/>The new DN will be based upon the configured getUserDNPattern().
 * @param username the login username
 * @return the new DN
 */
public String returnNewUserDN(String username) {
  username = Val.chkStr(username).toLowerCase();
  if (username.length() == 0) {
    return "";
  } else if (hasSpecialDNCharacter(username)) {
    return "";
  } else {
    return replace(getUserDNPattern(),username).toLowerCase();
  }
}

/**
 * Returns the search filter for finding a user based upon a login username.
 * <br/>The filter will be based upon the configured getUserLoginSearchPattern().
 * @param username the login username
 * @return the filter
 */
public String returnUserLoginSearchFilter(String username) {
  username = Val.chkStr(username);
  if (username.length() > 0) {
    return replace(getUsernameSearchPattern(),username);
  } else {
    return username;
  }
}

/**
 * Returns the search filter for finding a user based upon a login username.
 * <br/>The filter will be based upon the configured getUserLoginSearchPattern().
 * @param username the login username
 * @return the filter
 */
public String returnUserNewRequestSearchFilter(String username,String param) {
  username = Val.chkStr(username);
  if (username.length() > 0) {
    return replaceParam(getUserRequestsSearchPattern(),username,param);
  } else {
    return username;
  }
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" displayNameAttribute=\"").append(
      getUserDisplayNameAttribute()).append("\"\n");
  sb.append(" passwordEncryptionAlgorithm=\"").append(
      getPasswordEncryptionAlgorithm()).append("\"\n");
  sb.append(" newUserDNPattern=\"").append(
      getUserDNPattern()).append("\"\n");
  sb.append(" usernameSearchPattern=\"").append(
      getUsernameSearchPattern()).append("\"\n");
  sb.append(" searchDIT=\"").append(
      getUserSearchDIT()).append("\"\n");
  if (getUserObjectClasses() != null) {
    sb.append(" ").append(getUserObjectClasses()).append("\n");
  }
  
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}
