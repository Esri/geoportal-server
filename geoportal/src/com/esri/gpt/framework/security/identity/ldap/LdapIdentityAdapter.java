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
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.credentials.ChangePasswordCriteria;
import com.esri.gpt.framework.security.credentials.CredentialPolicy;
import com.esri.gpt.framework.security.credentials.CredentialPolicyException;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.RecoverPasswordCriteria;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.principal.Group;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.Users;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

/**
 * Identity adapter for an LDAP based identity store.
 */
public class LdapIdentityAdapter extends IdentityAdapter {
  
// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public LdapIdentityAdapter() {
  super();
}

// properties ==================================================================
  
/**
 * Gets the LDAP configuration.
 * @return the LDAP configuration
 */
protected LdapConfiguration getLdapConfiguration() {
  return getApplicationConfiguration().getIdentityConfiguration().getLdapConfiguration();
}

// methods =====================================================================
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
  LdapClient client = null;
  try {
    user.getAuthenticationStatus().reset();
    client = newServiceConnection();
    client.authenticate(getRequestContext(),user);
  } finally {
    if (client != null) client.close();
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
  LdapClient client = null;
  try {
    
    // initialize parameters
    String sUsername = user.getProfile().getUsername();
    UsernamePasswordCredentials origCred = criteria.getOriginalCredentials();
    UsernamePasswordCredentials newCred = criteria.getNewCredentials();
    origCred.setUsername(sUsername);
    newCred.setUsername(sUsername);
    
    // ensure that the old password was supplied correctly for the user
    UsernamePasswordCredentials testCred = new UsernamePasswordCredentials();
    testCred.setUsername(origCred.getUsername());
    testCred.setPassword(origCred.getPassword());
    User testUser = new User();
    testUser.setCredentials(testCred);
    authenticate(testUser);
    
    // ensure that the new credentials are valid
    CredentialPolicy policy = new CredentialPolicy();
    policy.validatePasswordPolicy(newCred);
    
    // update the password
    client = newServiceConnection();
    client.getEditFunctions().updateUserPassword(
           client.getConnectedContext(),user,newCred);
  } finally {
    if (client != null) client.close();
  }
}

/**
 * Makes a new LDAP client.
 * @return the new LDAP client
 */
protected LdapClient newLdapClient() {
  return new LdapClient(getLdapConfiguration());
}

/**
 * Makes a new connected LDAP client based upon the service account credentials.
 * @return the new LDAP client
 * @throws IdentityException if a service account connection cannot be established
 */
protected LdapClient newServiceConnection()
  throws IdentityException {
  try {
    LdapClient client = newLdapClient();
    client.connect();
    return client;
  } catch (Exception e) {
    throw new IdentityException("Unable to connect to LDAP.",e);
  }
}

/**
 * Populate user profile information from ldap.
 * @param context the RequestContext
 * @param user the user to be read
 * @throws IdentityException if a service account connection cannot be established
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
public void populateUser(RequestContext context,User user)throws IdentityException, NamingException, SQLException {
	  LdapClient client = null;
	  try {
	    client = newServiceConnection();
	    client.populateUser(context, user, "");
	  } finally {
	    if (client != null) client.close();
	  }
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
  LdapClient client = null;
  Users users = new Users();
  try {
    client = newServiceConnection();
    DirContext dirContext = client.getConnectedContext();
    LdapQueryFunctions queryF = client.getQueryFunctions();
    StringSet ssDNs = queryF.readGroupMembers(dirContext,groupDN);
    for (String sDN: ssDNs) {
      String sName = queryF.readUsername(dirContext,sDN);
      User user = new User();
      user.setDistinguishedName(sDN);
      user.setKey(user.getDistinguishedName());
      user.setName(sName);
      users.add(user);
    }
  } finally {
    if (client != null) client.close();
  }
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
  LdapClient client = null;
  try {
    client = newServiceConnection();
    client.getQueryFunctions().readUserGroups(
        client.getConnectedContext(),user);
  } finally {
    if (client != null) client.close();
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
  LdapClient client = null;
  try {
    client = newServiceConnection();
    client.getQueryFunctions().readUserProfile(
           client.getConnectedContext(),user);
  } finally {
    if (client != null) client.close();
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
  LdapClient client = null;
  try {
    client = newServiceConnection();
    return client.getEditFunctions().recoverUserPassword(
                  client.getConnectedContext(),
                  criteria.getUsername(),
                  criteria.getEmailAddress());
  } finally {
    if (client != null) client.close();
  }
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
  LdapClient client = null;
  try {
    
    // ensure that the new credentials are valid
    CredentialPolicy policy = new CredentialPolicy();
    UsernamePasswordCredentials cred;
    cred = user.getCredentials().getUsernamePasswordCredentials();
    policy.validateUsernamePolicy(cred);
    policy.validatePasswordPolicy(cred);
    policy.validateEmailPolicy(user.getProfile().getEmailAddress());
    
    // register the user
    client = newServiceConnection();
    client.getEditFunctions().registerUser(client.getConnectedContext(),user);
  } finally {
    if (client != null) client.close();
  }
}

/**
 * Adds  user to role.
 * @param user the subject user
 * @param role
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
@Override
public void addUserToRole(User user, String role)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
  LdapClient client = null;
  try {
           
    // register the user
    client = newServiceConnection();
    client.getEditFunctions().addUserToRole(client.getConnectedContext(),user, role);
  } finally {
    if (client != null) client.close();
  }
}

/**
 * Adds  user to group.
 * @param user the subject user
 * @param groupDn
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
@Override
public void addUserToGroup(User user, String groupDn)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
  LdapClient client = null;
  try {
           
    // register the user
    client = newServiceConnection();
    client.getEditFunctions().addUserToGroup(client.getConnectedContext(),user, groupDn);
  } finally {
    if (client != null) client.close();
  }
}

/**
 * Removes user from group.
 * @param user the subject user
 * @param groupDn
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
@Override
public void removeUserFromGroup(User user, String groupDn)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
  LdapClient client = null;
  try {           
    // register the user
    client = newServiceConnection();
    client.getEditFunctions().removeUserFromGroup(client.getConnectedContext(),user, groupDn);
  } finally {
    if (client != null) client.close();
  }
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
@Override
public void addAttribute(String objectDn, String attributeName, String attributeValue)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
	LdapClient client = null;
	  try {
	           
	    // register the user
	    client = newServiceConnection();
	    BasicAttributes ldapAttributes = new BasicAttributes();
	    BasicAttribute ldapAttribute = new BasicAttribute(attributeName,attributeValue);
	    ldapAttributes.put(ldapAttribute);
	    client.getEditFunctions().addAttribute(client.getConnectedContext(), objectDn, ldapAttributes);
	  } finally {
	    if (client != null) client.close();
	  }
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
@Override
public void removeAttribute(String objectDn,String attributeName, String attributeValue)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
	LdapClient client = null;
	  try {
	           
	    // register the user
	    client = newServiceConnection();
	    BasicAttributes ldapAttributes = new BasicAttributes();
	    BasicAttribute ldapAttribute = new BasicAttribute(attributeName,attributeValue);
	    ldapAttributes.put(ldapAttribute);
	    client.getEditFunctions().removeEntry(client.getConnectedContext(), objectDn, ldapAttributes);
	  } finally {
	    if (client != null) client.close();
	  }
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
  LdapClient client = null;
  try {
    
    // ensure that the email address is valid, update the profile
    CredentialPolicy policy = new CredentialPolicy();
    policy.validateEmailPolicy(user.getProfile().getEmailAddress());
    client = newServiceConnection();
    client.getEditFunctions().updateUserProfile(
           client.getConnectedContext(),user,false,false);
  } finally {
    if (client != null) client.close();
  }
}

/**
 * Builds list of ldap users matching filter.
 * @param filter the user search filter for ldap
 * @return the list of users matching filter
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 */
public Users readUsers(String filter, String attributeName) throws IdentityException, NamingException {
	LdapClient client = null;
	try {  
	  client = newServiceConnection();	 
	  return client.getQueryFunctions().readUsers(client.getConnectedContext(), filter,attributeName);
	} finally {
	  if (client != null) client.close();
	}
}

/**
 * Builds list of ldap groups matching filter.
 * @param filter the group search filter for ldap
 * @return the list of groups matching filter
 * @throws NamingException if an LDAP naming exception occurs
 * @throws IdentityException 
 */
public Groups readGroups(String filter) throws NamingException, IdentityException{
	LdapClient client = null;
	try {  
	  client = newServiceConnection();	 
	  return client.getQueryFunctions().readGroups(client.getConnectedContext(), filter);
	} finally {
	  if (client != null) client.close();
	}
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
	LdapClient client = null;
	try {  
	  // register the user
	  client = newServiceConnection();
	  removeUserFromGroups(user);
	  client.getConnectedContext().destroySubcontext(user.getDistinguishedName());
	} finally {
	  if (client != null) client.close();
	}
}

/**
 * Removes user from groups its member of.
 * @param user the subject user
 * @throws CredentialPolicyException if the credentials are invalid
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
private void removeUserFromGroups(User user)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
  try {
    Groups grps = user.getGroups();
    for(Group g : grps.values()){
    	removeUserFromGroup(user, g.getDistinguishedName());
    }
  
  } finally {
  }
}

}
