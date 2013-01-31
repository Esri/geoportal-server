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
package com.esri.gpt.framework.security.identity.local;
import com.esri.gpt.framework.security.credentials.ChangePasswordCriteria;
import com.esri.gpt.framework.security.credentials.CredentialPolicyException;
import com.esri.gpt.framework.security.credentials.Credentials;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.DistinguishedNameCredential;
import com.esri.gpt.framework.security.credentials.RecoverPasswordCriteria;
import com.esri.gpt.framework.security.credentials.UsernameCredential;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.principal.Group;
import com.esri.gpt.framework.security.principal.Role;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.security.principal.Roles;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.Users;

import java.sql.SQLException;
import javax.naming.NamingException;

/**
 * Identity adapter for a simple installation of one known user (the administrator).
 */
public class SimpleIdentityAdapter extends IdentityAdapter {
  
// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public SimpleIdentityAdapter() {
  super();
}

// properties ==================================================================

/**
 * Gets the password for the one known user.
 * @return the password
 */
private UsernamePasswordCredentials getCredentials() {
  return getApplicationConfiguration().getIdentityConfiguration().getSimpleConfiguration().getServiceAccountCredentials();
}

/**
 * Gets the distinguished name for the one known user.
 * @return the distinguished name
 */
private String getDN() {
  return getCredentials().getDistinguishedName();
}

/**
 * Gets the password for the one known user.
 * @return the password
 */
private String getPassword() {
  return getCredentials().getPassword();
}

/**
 * Gets the username for the one known user.
 * @return the username
 */
private String getUsername() {
  return getCredentials().getUsername();
}
  
// methods =====================================================================

/**
 * Adds  user to role.
 * @param user the subject user
 * @param role the subject role
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
@Override
public void addUserToRole(User user, String role)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
}

/**
 * Authenticates a user.
 * @param user the subject user
 * @throws CredentialsDeniedException if credentials are denied
 * @throws IdentityException if a system error occurs preventing authentication
 * @throws SQLException if a database communication exception occurs
 */
@Override
public void authenticate(User user)
  throws CredentialsDeniedException, IdentityException, SQLException {
  boolean bAuthenticated = false;
  user.getAuthenticationStatus().reset();
  
  // authenticate
  Credentials credentials = user.getCredentials();
  if (credentials != null) {
    if (credentials instanceof UsernamePasswordCredentials) {
      UsernamePasswordCredentials upCreds = (UsernamePasswordCredentials)credentials;
      bAuthenticated = (getUsername().length() > 0) &&
                       (getPassword().length() > 0) &&
                       upCreds.getUsername().equalsIgnoreCase(getUsername()) && 
                       upCreds.getPassword().equals(getPassword());
    } else if (credentials instanceof DistinguishedNameCredential) {
      DistinguishedNameCredential dnCred = (DistinguishedNameCredential)credentials;
      bAuthenticated = (getDN().length() > 0) &&
                       dnCred.getDistinguishedName().equalsIgnoreCase(getDN());
    } else if (credentials instanceof UsernameCredential) {
      UsernameCredential unCred = (UsernameCredential)credentials;
      bAuthenticated = (getUsername().length() > 0) &&
                       unCred.getUsername().equalsIgnoreCase(getUsername());
    }
  }
  
  // setup the authenticated user
  if (bAuthenticated) {
    user.setDistinguishedName(getDN());
    user.setKey(user.getDistinguishedName());
    user.setName(getUsername());
    user.getProfile().setUsername(user.getName());
    user.getAuthenticationStatus().setWasAuthenticated(true);
    
    // set role/group properties
    try {
      readUserGroups(user);
      RoleSet authRoles = user.getAuthenticationStatus().getAuthenticatedRoles();
      Roles cfgRoles = getApplicationConfiguration().getIdentityConfiguration().getConfiguredRoles();
      for (Role role: cfgRoles.values()) {
        authRoles.addAll(role.getFullRoleSet());
      }
    } catch (NamingException e) {
      // will never be thrown
    }
    
    // ensure a local reference for the user
    LocalDao localDao = new LocalDao(getRequestContext());
    localDao.ensureReferenceToRemoteUser(user);
  }
  if (!bAuthenticated) {
    throw new CredentialsDeniedException("Invalid credentials.");
  } 
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
@Override
public void changePassword(User user, ChangePasswordCriteria criteria)
  throws CredentialsDeniedException, CredentialPolicyException, 
         IdentityException, NamingException, SQLException {
  throwNotSupportedException("changePassword");
}

/**
 * Reads the members of a group.
 * @param groupDN the distinguished name for the group
 * @return the collection of users belonging to the group
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
@Override
public Users readGroupMembers(String groupDN) 
  throws IdentityException, NamingException, SQLException {
  Users users = new Users();
  User user = new User();
  user.setDistinguishedName(getDN());
  user.setKey(user.getDistinguishedName());
  user.setName(getUsername());
  users.add(user);
  return users;
}

/**
 * Reads the groups to which a user belongs.
 * @param user the subject user
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
@Override
public void readUserGroups(User user)
  throws IdentityException, NamingException, SQLException {
  if ((user != null) && (user.getDistinguishedName().equalsIgnoreCase(getDN()))) {
    Roles cfgRoles = getApplicationConfiguration().getIdentityConfiguration().getConfiguredRoles();
    for (Role role: cfgRoles.values()) {
      Group group = new Group();
      group.setDistinguishedName(role.getKey());
      group.setKey(role.getKey());
      group.setName(role.getKey());
      user.getGroups().add(group);
    }
  }
}

/**
 * Reads the profile attributes for a user.
 * @param user the subject user
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
@Override
public void readUserProfile(User user)
  throws IdentityException, NamingException, SQLException {
  if (user != null) {
    user.getProfile().setUsername(user.getName());
  }
}

/**
 * Recovers a user password.
 * @param criteria the criteria associated with the password recovery
 * @return the user associated with the recovered credentials (null if no match)
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
@Override
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
@Override
public void registerUser(User user)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
  throwNotSupportedException("registerUser");
}

/**
 * Updates the profile attributes for a user.
 * @param user the subject user
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
@Override
public void updateUserProfile(User user)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
  throwNotSupportedException("updateUserProfile");
}

}
