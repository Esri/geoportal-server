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
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.credentials.ChangePasswordCriteria;
import com.esri.gpt.framework.security.credentials.CredentialPolicyException;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.RecoverPasswordCriteria;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.Users;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

/**
 * Super class for an identity adapter.
 * <p>
 * An identity adapter provides the basic interface to an underlying
 * identity store.
 * <p>Currently, an adapter based upon local identity tables 
 * (LocalIdentityAdapter) and an adapter based upon remote 
 * LDAP identities (LdapIdentityAdapter) are supported.
 */
public class IdentityAdapter {
  
// class variables =============================================================

// instance variables ==========================================================
private ApplicationConfiguration _appConfig = null;
private RequestContext _requestContext;

// constructors ================================================================

/** Default constructor. */
public IdentityAdapter() {}

// properties ==================================================================

/**
 * Gets the application configuration.
 * @return the application configuration
 */
public ApplicationConfiguration getApplicationConfiguration() {
  return _appConfig;
}
/**
 * Sets the application configuration.
 * @param appConfig the application configuration
 */
public void setApplicationConfiguration(ApplicationConfiguration appConfig) {
  _appConfig = appConfig;
}

/**
 * Gets the associated request context.
 * @return the request context
 */
public RequestContext getRequestContext() {
  return _requestContext;
}
/**
 * Sets the associated request context.
 * @param requestContext the request context
 */
public void setRequestContext(RequestContext requestContext) {
  _requestContext = requestContext;
}
  
// methods =====================================================================
/**
 * Authenticates credentials.
 * @param user the subject user
 * @throws CredentialsDeniedException if credentials are denied
 * @throws IdentityException if a system error occurs preventing authentication
 * @throws SQLException if a database communication exception occurs
 */
public void authenticate(User user)
  throws CredentialsDeniedException, IdentityException, SQLException {
}

/**
 * Changes the password for a user.
 * @param user the subject user
 * @param criteria the criteria associated with the password change
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void changePassword(User user, ChangePasswordCriteria criteria)
  throws CredentialsDeniedException, CredentialPolicyException, 
         IdentityException, NamingException, SQLException {
  throwNotSupportedException("changePassword");
}

/**
 * Delete user from ldap
 * @param user the user to be deleted from ldap.
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void deleteUser(User user)
	throws CredentialPolicyException, IdentityException, NamingException, SQLException {
}

/**
 * Reads the members of a group.
 * @param groupKey the key for the group
 * @return the collection of users belonging to the group
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public Users readGroupMembers(String groupKey) 
  throws IdentityException, NamingException, SQLException {
  return null; 
}

/**
 * Reads the groups to which a user belongs.
 * @param user the subject user
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void readUserGroups(User user)
  throws IdentityException, NamingException, SQLException {
}

/**
 * Reads the profile attributes for a user.
 * @param user the subject user
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void readUserProfile(User user)
  throws IdentityException, NamingException, SQLException {
}

/**
 * Recovers a user password.
 * @param criteria the criteria associated with the password recovery
 * @return the user associated with the recovered credentials (null if no match)
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public User recoverPassword(RecoverPasswordCriteria criteria)
  throws IdentityException, NamingException, SQLException {
  throwNotSupportedException("recoverPassword");
  return null;  
}

/**
 * Registers a new user.
 * @param user the subject user
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void registerUser(User user)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
}

/**
 * Adds user attribute.
 * @param  objectDn the subject dn
 * @param  attributeName the user attribute will be added.
 * @param  attributeValue the user attribute value will be added.
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void addAttribute(String objectDn, String attributeName, String attributeValue)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
}

/**
 * Adds user attribute.
 * @param  objectDn the subject dn
 * @param  attributeName the user attribute will be removed.
 * @param  attributeValue the user attribute value will be removed
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void removeAttribute(String objectDn, String attributeName, String attributeValue)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
}

/**
 * Adds user to role.
 * @param user the subject user
 * @param  role the role user will be added.
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void addUserToRole(User user, String role)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
}

/**
 * Adds user to group.
 * @param user the subject user
 * @param  groupDn the dn of group user will be added.
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void addUserToGroup(User user, String groupDn)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
}


/**
 * Removes user from group.
 * @param user the subject user
 * @param groupDn the distinguishedName for the ldap group
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void removeUserFromGroup(User user, String groupDn)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {	
}


/**
 * Throws an exception.
 * <br/>This is intended to be used if a unsupported method call is sent
 * to an adapter
 * @param method the name of the method that is not supported
 * @throws IdentityException the thrown exception
 */
public void throwNotSupportedException(String method)
  throws IdentityException {
  throw new IdentityException("Method is not supported by this adapter: "+method);
}

/**
 * Updates the profile attributes for a user.
 * @param user the subject user
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void updateUserProfile(User user)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
}

/**
 * Builds list of ldap users matching filter.
 * @param filter the user search filter for ldap
 * @return the list of users matching filter
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 */
public Users readUsers(String filter, String attributeName) 
	throws IdentityException, NamingException {
	return null;
}

/**
 * Builds list of ldap groups matching filter.
 * @param filter the group search filter for ldap
 * @return the list of groups matching filter
 * @throws NamingException if an LDAP naming exception occurs
 * @throws IdentityException 
 */
public Groups readGroups(String filter) 
		throws NamingException, IdentityException{
	return null;
}

}
