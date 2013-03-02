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

import com.esri.gpt.catalog.arcims.ImsPermissionDao;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.DistinguishedNameCredential;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.util.UuidUtil;

import java.sql.SQLException;
import java.util.logging.Level;

/**
 * Represents a user with a metadata publishing role.
 */
public class Publisher extends User {

// class variables =============================================================

// instance variables ==========================================================
private String  _folderUuid = "";
 
// constructors ================================================================

/** Default constructor. */
protected Publisher() {
  super();
}

/** 
 * Constructs a publisher based upon the user associated with the 
 * current request context.
 * @param context the current request context (contains the active user)
 * @throws NotAuthorizedException if the user does not have publishing rights
 * @throws IdentityException if an integrity violation occurs
 * @throws ImsServiceException if an exception occurs when 
 *         creating the default folder
 * @throws SQLException if a database exception occurs
 */
public Publisher(RequestContext context) 
  throws NotAuthorizedException,
         IdentityException, 
         ImsServiceException, 
         SQLException {
  
  // initialize
  User user = context.getUser();
  setKey(user.getKey());
  setLocalID(user.getLocalID());
  setDistinguishedName(user.getDistinguishedName());
  setName(user.getName());
  
  // establish credentials
  UsernamePasswordCredentials creds = new UsernamePasswordCredentials();
  creds.setUsername(getName());
  setCredentials(creds);
  
  this.setAuthenticationStatus(user.getAuthenticationStatus());
  
  // assert a publishing role, ensure proper ArcIMS permissions
  assertPublisherRole();
  ImsPermissionDao dao = new ImsPermissionDao(context);
  dao.preparePublisher(this,true);
}

/** 
 * Constructs a publisher based upon the user associated with the 
 * current request context and a distinguished name on behalf of which a
 * document will be published.
 * @param context the current request context (contains the active user)
 * @param userDN the DN associated with on behalf of which a document will be published.
 * @throws CredentialsDeniedException if the supplied DN is invalid
 * @throws NotAuthorizedException if the user does not have publishing rights
 * @throws IdentityException if an integrity violation occurs
 * @throws ImsServiceException if an exception occurs when 
 *         creating the default folder
 * @throws SQLException if a database exception occurs
 */
public Publisher(RequestContext context, String userDN) 
  throws CredentialsDeniedException,
         NotAuthorizedException,
         IdentityException, 
         ImsServiceException, 
         SQLException  {
    
  // check to see if this request is on behalf of a
  // metadata management group
  Group mgmtGroup = checkManagementGroup(context,userDN);
  if (mgmtGroup != null) {
    setKey(mgmtGroup.getKey());
    setDistinguishedName(mgmtGroup.getDistinguishedName());
    setName(mgmtGroup.getName());
    IdentityConfiguration idConfig = context.getIdentityConfiguration();
    Role pubRole = idConfig.getConfiguredRoles().get("gptPublisher");
    if (pubRole != null) {
      RoleSet roles = getAuthenticationStatus().getAuthenticatedRoles();
      roles.addAll(pubRole.getFullRoleSet());
    }
    
  } else {
    
    // authenticate the publisher based upon the supplied distinguished name
    DistinguishedNameCredential dnCred = new DistinguishedNameCredential(userDN);
    setCredentials(dnCred);
    context.newIdentityAdapter().authenticate(this);
  }

  // establish credentials
  UsernamePasswordCredentials creds = new UsernamePasswordCredentials();
  creds.setUsername(getName());
  setCredentials(creds);
  
  // assert a publishing role, ensure proper ArcIMS permissions
  assertPublisherRole();
  ImsPermissionDao dao = new ImsPermissionDao(context);
  dao.preparePublisher(this,true);
}

// properties ==================================================================

/**
 * Asserts the administrator role.
 * @throws NotAuthorizedException if the administrator role has not been granted
 */
private void assertAdministratorRole() throws NotAuthorizedException {
  RoleSet roles = getAuthenticationStatus().getAuthenticatedRoles();
  roles.assertRole("gptAdministrator");
}

/**
 * Asserts the publisher role.
 * @throws NotAuthorizedException if the publisher role has not been granted
 */
private void assertPublisherRole() throws NotAuthorizedException {
  RoleSet roles = getAuthenticationStatus().getAuthenticatedRoles();
  roles.assertRole("gptPublisher");
}

/**
 * Gets the default folder name for this publisher.
 * @return the default folder name
 */
public String getDefaultFolderName() {
  return getName();
}

/**
 * Gets the folder uuid for this publisher.
 * @return the folder uuid
 */
public String getFolderUuid() {
  return _folderUuid;
}
/**
 * Sets the folder uuid for this publisher.
 * @param folderUuid the folder uuid
 */
public void setFolderUuid(String folderUuid) {
  _folderUuid = UuidUtil.addCurlies(folderUuid);
}

/**
 * Gets the status indicating whether this publisher is an administrator.
 * @return true if this publisher is an administrator
 */
public boolean getIsAdministrator() {
  RoleSet roles = getAuthenticationStatus().getAuthenticatedRoles();
  return roles.hasRole("gptAdministrator");
}

/**
 * Gets the status indicating whether this publisher exists within a remote identity store.
 * @return true if this publisher is a remote reference
 */
public boolean getIsRemote() {
  return true;
}

// methods =====================================================================

/**
 * Builds a collection of groups that can be selected by the current user to set
 * access policy
 * 
 * @param context
 *          the current request context (contains the active user)
 * @return the collection of groups that can be selected
 */
public static Groups buildSelectableGroups(RequestContext context) {

  IdentityAdapter idAdapter = context.newIdentityAdapter();
  IdentityConfiguration idConfig = context.getIdentityConfiguration();
  Groups selectableGroups = null;

  User user = context.getUser();
  RoleSet roles = user.getAuthenticationStatus().getAuthenticatedRoles();
  boolean bIsAdministrator = roles.hasRole("gptAdministrator");

  try {
    if (bIsAdministrator) {
      User selectableUser = new User();
      selectableUser.setDistinguishedName("*");
      idAdapter.readUserGroups(selectableUser);
      selectableGroups = selectableUser.getGroups();
    } else {
      selectableGroups = user.getGroups();
    }
    Groups mgmtGroups = idConfig.getMetadataManagementGroups();
    if ((mgmtGroups != null) && (mgmtGroups.size() > 0)) {
      for (Group mgmtGroup : mgmtGroups.values()) {
        boolean bAdd = bIsAdministrator;
        if ((selectableGroups != null)
            && selectableGroups.containsKey(mgmtGroup.getKey())) {
          bAdd = true;
        }
        if (bAdd) {
          selectableGroups.add(mgmtGroup);
        }
      }

    }
  } catch (Throwable t) {
    context.getLogger().log(Level.SEVERE, "Exception raised.", t);
  }
  
  selectableGroups.sort();
  return selectableGroups;
}


/**
 * Builds a collection of publishers (users) that can be selected by the current user.
 * @param context the current request context (contains the active user)
 * @param forManagement true if the list to build is in support of the metadata management page
 * @return the collection of publishers that can be selected
 */
public static Users buildSelectablePublishers(RequestContext context,
                                              boolean forManagement) {
  IdentityAdapter idAdapter = context.newIdentityAdapter();
  IdentityConfiguration idConfig = context.getIdentityConfiguration();
  
  // add the current user to the list
  Users users = new Users();
  User user = context.getUser();
  users.add(user);
  RoleSet roles = user.getAuthenticationStatus().getAuthenticatedRoles();
  boolean bIsAdministrator = roles.hasRole("gptAdministrator");
      
  try {
    if (bIsAdministrator && forManagement) {
      
      // add the administrators
      Role adminRole = idConfig.getConfiguredRoles().get("gptAdministrator");
      if (adminRole != null) {
        Users admins = idAdapter.readGroupMembers(adminRole.getDistinguishedName());
        for (User u: admins.values()) users.add(u);
      }
      
      // add the publishers
      Role pubRole = idConfig.getConfiguredRoles().get("gptPublisher");
      if (pubRole != null) {
        Users publishers = idAdapter.readGroupMembers(pubRole.getDistinguishedName());
        for (User u: publishers.values()) users.add(u);
      }
      users.sort();
    }
    
    // add the metadata management groups
    Groups userGroups = user.getGroups();
    Groups mgmtGroups = idConfig.getMetadataManagementGroups();
    Users mgmtUsers = new Users();
    if ((mgmtGroups != null) && (mgmtGroups.size() > 0)){
      for (Group mgmtGroup : mgmtGroups.values()) {
        boolean bAdd = bIsAdministrator;
        if ((userGroups != null) && userGroups.containsKey(mgmtGroup.getKey())) {
          bAdd = true;
        }
        if (bAdd) {
          User u = new User();
          u.setKey(mgmtGroup.getKey());
          u.setDistinguishedName(mgmtGroup.getDistinguishedName());
          u.setName(mgmtGroup.getName());
          mgmtUsers.add(u);
        }
      }
      mgmtUsers.sort();
      for (User u: mgmtUsers.values()) users.add(u);
    }
    
  } catch (Throwable t) {
    context.getLogger().log(Level.SEVERE,"Exception raised.",t);
  }
  
  return users;
}

/**
 * Checks to see if a supplied DN corresponds to a metadata management group.
 * 
 * @param context
 *          the current request context (contains the active user)
 * @param userDN
 *          the distinguished name to check
 * @return the corresponding metadata management group or null
 */
private Group checkManagementGroup(RequestContext context, String userDN) {
  IdentityConfiguration idConfig = context.getIdentityConfiguration();
  Groups mgmtGroups = idConfig.getMetadataManagementGroups();
  if (mgmtGroups != null) {
    return mgmtGroups.get(userDN);
  }
  return null;
}

/**
 * Creates a catalog administrator based upon the distinguished name credential supplied within the
 * GPT configuration file (@catalogAdminDN).
 * <p/>
 * The administrative publisher is used during background processes such as synchronization.
 * @param context the current request context
 * @return a publisher with catalog metadata administration rights
 * @throws CredentialsDeniedException if the configured administrative credentials were invalid
 * @throws NotAuthorizedException if the associated user does not have administrative rights
 * @throws IdentityException if an integrity violation occurs
 * @throws ImsServiceException if an exception occurs when creating the default folder
 * @throws SQLException if a database exception occurs
 */
public static Publisher makeSystemAdministrator(RequestContext context) 
  throws CredentialsDeniedException, IdentityException, SQLException, 
         NotAuthorizedException, ImsServiceException {
  
  // create the publisher
  String sAdminDN = context.getIdentityConfiguration().getCatalogAdminDN();
  Publisher admin = new Publisher();
  DistinguishedNameCredential dnCred = new DistinguishedNameCredential(sAdminDN);
  admin.setCredentials(dnCred);
  context.newIdentityAdapter().authenticate(admin);
  
  // establish credentials
  UsernamePasswordCredentials creds = new UsernamePasswordCredentials();
  creds.setUsername(admin.getName());
  admin.setCredentials(creds);
  
  // assert a publishing role, ensure proper ArcIMS permissions
  admin.assertAdministratorRole();
  ImsPermissionDao dao = new ImsPermissionDao(context);
  dao.preparePublisher(admin,true);

  return admin;
}

}
