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
import com.esri.gpt.framework.security.credentials.Credentials;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.DistinguishedNameCredential;
import com.esri.gpt.framework.security.credentials.UsernameCredential;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.identity.local.LocalDao;
import com.esri.gpt.framework.security.principal.Group;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.Role;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.security.principal.Roles;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.NamingException;

/**
 * A client for connection to an LDAP identity store.
 */
public class LdapClient {

// class variables =============================================================

// instance variables ==========================================================
private LdapConfiguration  _configuration  = null;
private Credentials        _credentials    = null;
private DirContext         _dirContext     = null;
private LdapEditFunctions  _editFunctions  = new LdapEditFunctions();
private LdapQueryFunctions _queryFunctions = new LdapQueryFunctions();

// constructors ================================================================

/** Default constructor. */
protected LdapClient() {
  this(null,null);
}

/**
 * Construct with a supplied configuration.
 * @param configuration the configuration
 */
protected LdapClient(LdapConfiguration configuration) {
  this(configuration,null);
}

/**
 * Construct with a supplied configuration and credentials.
 * @param configuration the configuration
 * @param credentials the connection credentials
 */
protected LdapClient(LdapConfiguration configuration, 
                     Credentials credentials) {
  if (configuration == null) {
    setConfiguration(new LdapConfiguration());
  } else {
    setConfiguration(configuration);
  }
  if (credentials == null) {
    setCredentials(configuration.getConnectionProperties().getServiceAccountCredentials());
  } else {
    setCredentials(credentials);
  }
}

// properties ==================================================================

/**
 * Gets the LDAP configuration.
 * @return the configuration
 */
public LdapConfiguration getConfiguration() {
  return _configuration;
}
/**
 * Sets the LDAP configuration.
 * @param configuration the configuration
 */
public void setConfiguration(LdapConfiguration configuration) {
  _configuration = configuration;
  getEditFunctions().setConfiguration(configuration);
  getQueryFunctions().setConfiguration(configuration);
}

/**
 * Gets the connected directory context.
 * @return the connected directory context
 * @throws NamingException if a connection has not been established
 */
protected final DirContext getConnectedContext()
  throws NamingException {
  if (_dirContext == null) {
    throw new NamingException("An LDAP connection has not been established.");
  }
  return _dirContext;
}
/**
 * Sets the connected directory context.
 * @param connectedContext the connected directory context
 */
protected final void setConnectedContext(DirContext connectedContext) {
  // ensure that the current connection context is closed before resetting
  try {
    if (_dirContext != null) {
      _dirContext.close();
      _dirContext = null;
    }
  } catch (Exception e) {
    LogUtil.getLogger().log(Level.WARNING,"Error closing LDAP directory context.",e);
  }
  _dirContext = connectedContext;
}

/**
 * Gets the credentials for the connection.
 * @return the credentials
 */
public Credentials getCredentials() {
  return _credentials;
}
/**
 * Sets the credentials for the connection.
 * @param credentials the credentials
 */
public void setCredentials(Credentials credentials) {
  _credentials = credentials;
}

/**
 * Gets the edit functions.
 * @return the edit functions
 */
protected LdapEditFunctions getEditFunctions() {
  return _editFunctions;
}

/**
 * Gets the query functions.
 * @return the query functions
 */
protected LdapQueryFunctions getQueryFunctions() {
  return _queryFunctions;
}

// methods =====================================================================

/**
 * Authenticates a user.
 * @param requestContext the context associated with the request
 * @param user the subject user
 * @throws CredentialsDeniedException if credentials are denied
 * @throws IdentityException if a system error occurs preventing authentication
 * @throws SQLException if a database communication exception occurs
 */
protected void authenticate(RequestContext requestContext, User user)
  throws CredentialsDeniedException, IdentityException, SQLException {
  LdapClient connectionClient = null;
  try {
    user.getAuthenticationStatus().reset();
    String sUsername = "";
    String sAuthenticatedDN  = "";
    String sTargetedGroupDN = "";
    LdapUserProperties userProps = getConfiguration().getUserProperties();

    // determine the authentication method
    Credentials credentials = user.getCredentials();
    UsernamePasswordCredentials upCredentials = null;
    boolean bUseDirectConnect = false;
    boolean bUseLoginPattern  = false;
    if (credentials != null) {
      if (credentials instanceof UsernamePasswordCredentials) {
        upCredentials = (UsernamePasswordCredentials)credentials;
        upCredentials.setTargetedGroupDN("");
        sUsername = upCredentials.getUsername();
        String sPattern  = userProps.getUsernameSearchPattern();
        if (sUsername.length() > 0) {
          if (userProps.hasSpecialDNCharacter(sUsername)) {
            bUseDirectConnect = true;
          } else {
            bUseLoginPattern = (sPattern.length() > 0);
          }
        }
        
      } else if (credentials instanceof DistinguishedNameCredential) {
        DistinguishedNameCredential dnCredential;
        dnCredential = (DistinguishedNameCredential)credentials;
        sAuthenticatedDN = dnCredential.getDistinguishedName();
        
      } else if (credentials instanceof UsernameCredential) {
        UsernameCredential unCredential = (UsernameCredential)credentials;
        String sBaseDN = userProps.getUserSearchDIT();
        String sFilter = userProps.returnUserLoginSearchFilter(unCredential.getUsername());
        StringSet ssDNs = getQueryFunctions().searchDNs(
                          getConnectedContext(),sBaseDN,sFilter);
        if (ssDNs.size() > 1) {
          throw new IdentityException("Multiple LDAP usernames matched for:"+ unCredential.getUsername());
        } else if (ssDNs.size() == 1) {
          sAuthenticatedDN = ssDNs.iterator().next();
        }
      }
    }

    // Attempt to connect with the supplied credentials.
    // An AuthenticationException will be thrown if the credentials are invalid
    if (bUseDirectConnect) {
      connectionClient = new LdapClient(getConfiguration(),upCredentials);
      sAuthenticatedDN = connectionClient.connect();
      bUseLoginPattern = false;
      connectionClient.close();
      connectionClient = null;
    }

    // Attempt to authenticate by first executing a search for all users 
    // matching the input username, then checking the supplied password against 
    // each matching DN.
    // An AuthenticationException will be thrown if the credentials are invalid.
    if (bUseLoginPattern) {
      sAuthenticatedDN = searchForUser(upCredentials);
      sTargetedGroupDN = upCredentials.getTargetedGroupDN();
    }

    // ensure an authenticated DN
    if (sAuthenticatedDN.length() == 0) {
      throw new AuthenticationException("Invalid credentials.");
    } 
    
    // populate the authentication status and profile information
    user.setDistinguishedName(sAuthenticatedDN);
    populateUser(requestContext,user,sTargetedGroupDN);
    
    RoleSet roles = user.getAuthenticationStatus().getAuthenticatedRoles();
    if (roles.hasRole("gptForbiddenAccess")) {
      User activeUser = requestContext.getUser();
      if(activeUser.getAuthenticationStatus().getWasAuthenticated()){
    	  String activeUserDn = requestContext.getUser().getDistinguishedName();
    	  String managedUserDn = user.getDistinguishedName();
    	  if(activeUserDn.equals(managedUserDn)){
    		throw new AuthenticationException("Forbidden"); 
    	  }
      }else{
        throw new AuthenticationException("Forbidden");
      }
    }
    
  } catch (AuthenticationException e) {
    user.getAuthenticationStatus().reset();
    throw new CredentialsDeniedException("Invalid credentials.");
  } catch (com.esri.gpt.framework.context.ConfigurationException e) {
    user.getAuthenticationStatus().reset();
    throw new IdentityException(e.getMessage(),e);
  } catch (NamingException e) {
    user.getAuthenticationStatus().reset();
    throw new IdentityException(e.getMessage(),e);
  } catch (SQLException e) {
    user.getAuthenticationStatus().reset();
    throw e;
  } catch (IdentityException e) {
    user.getAuthenticationStatus().reset();
    throw e;
  } finally {
    if (connectionClient != null) connectionClient.close();
  }
}
    
/**
 * Checks the distinguished name within a set of username/password credentials.
 * <br/>If the distinguished name has not been set, the configured
 * username pattern is applied to determine the distinguished name.
 * @param credentials the credentials to check
 */
private void checkDistinguishedName(UsernamePasswordCredentials credentials) {
  String sDN = credentials.getDistinguishedName();
  if (sDN.length() == 0) {
    credentials.setDistinguishedName(credentials.getUsername());
  }
}

/**
 * Closes the connected directory context (if open).
 */
public final void close() {
  setConnectedContext(null);
}

/**
 * Establishes an LDAP connection.
 * @return the SECURITY_PRINCIPAL associated with the connection
 * @throws AuthenticationException if an authentication exception occurs
 * @throws NamingException if a naming exception occurs
 */
protected String connect() throws AuthenticationException, NamingException {
  close();
  LdapConfiguration configuration = getConfiguration();
  LdapConnectionProperties conProps = configuration.getConnectionProperties();

  boolean bForceCredentials = true;
  String sAuthenticationLevel = conProps.getSecurityAuthenticationLevel();
  String sSecurityProtocol = conProps.getSecurityProtocol();
  String sPrincipal = "";
  String sPassword = "";

  // check the credentials
  Credentials credentials = getCredentials();
  if (credentials != null) {
    if (credentials instanceof UsernamePasswordCredentials) {
      UsernamePasswordCredentials upCredentials = (UsernamePasswordCredentials)credentials;
      checkDistinguishedName(upCredentials);
      sPrincipal = upCredentials.getDistinguishedName();
      sPassword = upCredentials.getPassword();
    }
  }

  // make the environment map
  Hashtable<String,String> env = new Hashtable<String,String>(11);
  env.put(Context.INITIAL_CONTEXT_FACTORY,conProps.getInitialContextFactoryName());
  env.put(Context.PROVIDER_URL,conProps.getProviderUrl());
  if (sAuthenticationLevel.length() > 0) {
    env.put(Context.SECURITY_AUTHENTICATION,sAuthenticationLevel);
  }
  if (sSecurityProtocol.length() > 0) {
    env.put(Context.SECURITY_PROTOCOL,sSecurityProtocol);
  }
  if (sPrincipal.length() > 0) {
    env.put(Context.SECURITY_PRINCIPAL,sPrincipal);
  } else if (bForceCredentials) {
    throw new AuthenticationException("Invalid credentials.");
  }
  if (sPassword.length() > 0) {
    env.put(Context.SECURITY_CREDENTIALS,sPassword);
  } else if (bForceCredentials) {
    throw new AuthenticationException("Invalid credentials.");
  }
  
  // env.put(Context.REFERRAL,"follow");

  // make the initial directory context
  boolean useInitialLdapContext = false;
  LdapGroupProperties groupProps = configuration.getGroupProperties();
  String sDyn1 = Val.chkStr(groupProps.getGroupDynamicMemberAttribute());
  String sDyn2 = Val.chkStr(groupProps.getGroupDynamicMembersAttribute());
  if (sDyn1.startsWith("controlid=") || sDyn2.startsWith("controlid=")) {
    useInitialLdapContext = true;
  }
  if (useInitialLdapContext) {
    setConnectedContext(new InitialLdapContext(env,null));
  } else {
    setConnectedContext(new InitialDirContext(env));
  }
  
  return sPrincipal;
}

/**
 * Finalize on garbage collection.
 * @throws Throwable if an exception occurs
 */
@Override
protected void finalize() throws Throwable {
  super.finalize();
  close();
}

/**
 * Populates the authentication status and profile information for
 * a user based upon the user's DN.
 * @param requestContext the context associated with the request
 * @param user the subject user
 * @throws IdentityException if a system error occurs preventing authentication
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
protected void populateUser(RequestContext requestContext, 
                          User user,
                          String targetedGroupDN)
  throws IdentityException, NamingException, SQLException {
  
  // initialize
  String sAuthenticatedDN = user.getDistinguishedName();
  user.getAuthenticationStatus().reset();
  DirContext dirContext = getConnectedContext();
  
  // ensure an authenticated DN
  if (sAuthenticatedDN.length() == 0) {
    throw new AuthenticationException("Invalid credentials.");
  } 
  
  // populate profile information
  user.setDistinguishedName(sAuthenticatedDN);
  user.setKey(user.getDistinguishedName());
  getQueryFunctions().readUserProfile(dirContext,user);
  user.setName(user.getProfile().getUsername());
  
  // read groups, set authenticated roles
  getQueryFunctions().readUserGroups(dirContext,user);
  Groups userGroups = user.getGroups();
  Roles configuredRoles = getConfiguration().getIdentityConfiguration().getConfiguredRoles();
  RoleSet authenticatedRoles = user.getAuthenticationStatus().getAuthenticatedRoles();
  for (Role role: configuredRoles.values()) {
    if (userGroups.containsKey(role.getDistinguishedName())) {
      authenticatedRoles.addAll(role.getFullRoleSet());
    }
  }
  user.getAuthenticationStatus().setWasAuthenticated(true);
  
  // ensure membership if a targeted metadata management group was specified
  if (targetedGroupDN.length() > 0) {
    if (!userGroups.containsKey(targetedGroupDN)) {
      user.getAuthenticationStatus().reset();
      throw new AuthenticationException("Invalid credentials, not a member of the supplied group.");
    }
  }
  
  // ensure a local reference for the user
  LocalDao localDao = new LocalDao(requestContext);
  localDao.ensureReferenceToRemoteUser(user);
}

/**
 * Searches for a user by first executing a search for all users matching the
 * supplied username credential, then checking the supplied password credential
 * against each matching DN.
 * @param credentials the credentials to authenticate
 * @return the distinguised name associated with a located user
 * @throws AuthenticationException if the authentication of credentials failed
 * @throws NamingException if an LDAP naming exception occurs
 */
protected String searchForUser(UsernamePasswordCredentials credentials)
  throws AuthenticationException, NamingException {
  LdapClient client = null;
  String sAuthenticatedDN = "";
  boolean bMultipleAuthenticated = false;
  try {
    String sUsername = credentials.getUsername();
    
    // check for a metadata management login: username@@group
    int nIdx = sUsername.indexOf("@@");
    if (nIdx != -1) {
      Groups mmGroups = getConfiguration().getIdentityConfiguration().getMetadataManagementGroups();
      if ((mmGroups != null) && (mmGroups.size() > 0)) {
        String sMmUser = Val.chkStr(sUsername.substring(0,nIdx));
        String sMmGroup = Val.chkStr(sUsername.substring(nIdx+2));
        if ((sMmUser.length() > 0) && (sMmGroup.length() > 0)) {
          for (Group group: mmGroups.values()) {
            if (sMmGroup.equalsIgnoreCase(group.getName())) {
              sUsername = sMmUser;
              credentials.setTargetedGroupDN(group.getDistinguishedName());
            }
          }
        }
      }
    }
    
    // search for the user
    LdapUserProperties userProps = getConfiguration().getUserProperties();
    String sBaseDN = userProps.getUserSearchDIT();
    String sFilter = userProps.returnUserLoginSearchFilter(sUsername);
    StringSet ssDNs = getQueryFunctions().searchDNs(
                      getConnectedContext(),sBaseDN,sFilter);
    
    // loop through each DN found,
    // attempt to connect with the supplied password
    for (String sDN: ssDNs) {
      credentials.setDistinguishedName(sDN);
      client = new LdapClient(getConfiguration(),credentials);
      try {
        String sTestDN = client.connect();
        client.close();
        if (sAuthenticatedDN.length() == 0) {
          sAuthenticatedDN = sTestDN;
        } else {
          sAuthenticatedDN = "";
          bMultipleAuthenticated = true;
          break;
        }
      } catch (AuthenticationException e) {
        client.close();
      }      
    }
    
    // throw an exception if authentication failed
    if (bMultipleAuthenticated) {
      // more than one username/password match was found
      String sMsg = "Multiple LDAP credential matches were found for login: "+sUsername;
      LogUtil.getLogger().warning(sMsg);
      throw new AuthenticationException(sMsg);
    } else if (sAuthenticatedDN.length() == 0) {
      // no username/password match was found
      throw new AuthenticationException("Invalid credentials.");
    }
    
  } finally {
    credentials.setDistinguishedName(sAuthenticatedDN);
    if (client != null) client.close();
  }
  return sAuthenticatedDN;
}

}


