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
package com.esri.gpt.server.assertion.components;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.catalog.lucene.LuceneIndexAdapter;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.AuthenticationStatus;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.exception.AsnInsufficientPrivilegeException;
import com.esri.gpt.server.assertion.exception.AsnUnestablishedUserException;
import com.esri.gpt.server.assertion.index.AsnUserPart;
import com.esri.gpt.server.assertion.index.Assertion;

import java.sql.Timestamp;

/**
 * Handles authorization requests for an assertion operation.
 */
public class AsnAuthorizer {
  
  /** class variables ========================================================= */
  private static final String ACTION_CREATE = "create";
  private static final String ACTION_DELETE = "delete";
  private static final String ACTION_DISABLE = "disable";
  private static final String ACTION_ENABLE = "enable";
  private static final String ACTION_QUERY = "query";
  private static final String ACTION_UPDATE = "update";

  /** instance variables ====================================================== */
  private boolean wasUserEstablished = false;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AsnAuthorizer() {}

  /** properties ============================================================== */
  
  /**
   * Gets the flag indicating whether or not the user was established.
   * @return <code>true</code> if the user was established
   */
  public boolean getWasUserEstablished() {
    return this.wasUserEstablished;
  }
  /**
   * Sets the flag indicating whether or not the user was established.
   * @param wasUserEstablished <code>true</code> if the user was established
   */
  public void setWasUserEstablished(boolean wasUserEstablished) {
    this.wasUserEstablished = wasUserEstablished;
  }
 
  /** methods ================================================================= */
  
  /**
   * Authorizes a create, update, delete or query based operation.
   * @param context the assertion operation context
   * @param assertion the active assertion
   * @param action the action
   * @throws NotAuthorizedException if authentication was required
   * @throws AsnInsufficientPrivilegeException if the user has insufficient privilege
   */
  private void authorizeAction(AsnContext context, Assertion assertion, String action) 
    throws NotAuthorizedException, AsnInsufficientPrivilegeException {
  
    // ensure an authorization policy
    AsnOperation operation = context.getOperation();
    if (operation.getAuthPolicy() == null) {
      String msg = "An authorization policy was not configured.";
      throw new ConfigurationException(msg);
    }
    
    // check the user, ensure an authenticated user if required
    User user = context.getRequestContext().getUser();
    boolean userWasAuthenticated = false;
    if ((user != null) && user.getAuthenticationStatus().getWasAuthenticated()) {
      userWasAuthenticated = true;
    }
    if (operation.getAuthPolicy().getAuthenticationRequired() && !userWasAuthenticated) {
      throw new NotAuthorizedException("Not authorized.");
    }
    
    // determine the principals
    AsnPrincipals principals = null;
    boolean isWrite = false;
    if (action.equals(AsnAuthorizer.ACTION_CREATE)) {
      isWrite = true;
      principals = operation.getAuthPolicy().getCreatePrincipals();
      if (principals == null) {
        String msg = "Create principals were not configured.";
        throw new ConfigurationException(msg);
      }
      
    } else if (action.equals(AsnAuthorizer.ACTION_DELETE)) {
      isWrite = true;
      principals = operation.getAuthPolicy().getDeletePrincipals();
      if (principals == null) {
        String msg = "Delete principals were not configured.";
        throw new ConfigurationException(msg);
      }
      
    } else if (action.equals(AsnAuthorizer.ACTION_ENABLE) || 
               action.equals(AsnAuthorizer.ACTION_DISABLE)) {
      isWrite = true;
      principals = operation.getAuthPolicy().getEnableDisablePrincipals();
      if (principals == null) {
        String msg = "Enable/Disable principals were not configured.";
        throw new ConfigurationException(msg);
      }
      
    } else if (action.equals(AsnAuthorizer.ACTION_QUERY)) {
      principals = operation.getAuthPolicy().getQueryPrincipals();
      if (principals == null) {
        String msg = "Query principals were not configured.";
        throw new ConfigurationException(msg);
      }
      
    } else if (action.equals(AsnAuthorizer.ACTION_UPDATE)) {
      isWrite = true;
      principals = operation.getAuthPolicy().getQueryPrincipals();
      if (principals == null) {
        String msg = "Query principals were not configured.";
        throw new ConfigurationException(msg);
      }
    } 
    
    // hard check to ensure an authenticated user for any modifications 
    // (regardless of configuration)
    if (isWrite && !userWasAuthenticated) {
      throw new AsnInsufficientPrivilegeException();
    }
    
    // check "any user" user privilege
    if (principals.contains(AsnConstants.PRINCIPAL_ANY)) {
      return;
    }
    
    // check administrator privilege
    if (userWasAuthenticated) {
      if (principals.contains(AsnConstants.PRINCIPAL_ADMINISTRATOR)) {
        RoleSet roles = user.getAuthenticationStatus().getAuthenticatedRoles();
        if (roles.hasRole("gptAdministrator")) {
          return;
        }
      }
    }
    
    // check for ownership
    if (userWasAuthenticated && (assertion != null) && assertion.getWasReadFromIndex()) {
      if (principals.contains(AsnConstants.PRINCIPAL_OWNER)) {
        String asnUserKey = Val.chkStr(assertion.getUserPart().getKey());
        String userKey = Val.chkStr(user.getKey());
        if ((asnUserKey.length() > 0) && asnUserKey.equals(userKey)) {
          return;
        }
      }
    }
    
    throw new AsnInsufficientPrivilegeException();
  }
  
  /**
   * Authorizes a create operation.
   * @param context the assertion operation context
   * assertion the active assertion
   * @throws NotAuthorizedException if authentication was required
   * @throws CatalogIndexException indicates an I/O error with the resource index
   * @throws AsnInsufficientPrivilegeException if the user has insufficient privilege
   */
  public void authorizeCreate(AsnContext context, Assertion assertion) 
    throws NotAuthorizedException, CatalogIndexException, AsnInsufficientPrivilegeException {
    this.authorizeAction(context,assertion,AsnAuthorizer.ACTION_CREATE);
    this.authorizeResourceAccess(context);
  }
  
  /**
   * Authorizes a delete operation.
   * @param context the assertion operation context
   * @param assertion the active assertion
   * @throws NotAuthorizedException if authentication was required
   * @throws CatalogIndexException indicates an I/O error with the resource index
   * @throws AsnInsufficientPrivilegeException if the user has insufficient privilege
   */
  public void authorizeDelete(AsnContext context, Assertion assertion) 
    throws NotAuthorizedException, CatalogIndexException, AsnInsufficientPrivilegeException {
    this.authorizeAction(context,assertion,AsnAuthorizer.ACTION_DELETE);
    this.authorizeResourceAccess(context);
  }
  
  /**
   * Authorizes a disable operation.
   * @param context the assertion operation context
   * @param assertion the active assertion
   * @throws NotAuthorizedException if authentication was required
   * @throws CatalogIndexException indicates an I/O error with the resource index
   * @throws AsnInsufficientPrivilegeException if the user has insufficient privilege
   */
  public void authorizeDisable(AsnContext context, Assertion assertion) 
    throws NotAuthorizedException, CatalogIndexException, AsnInsufficientPrivilegeException {
    this.authorizeAction(context,assertion,AsnAuthorizer.ACTION_DISABLE);
    this.authorizeResourceAccess(context);
  }
  
  /**
   * Authorizes an enable operation.
   * @param context the assertion operation context
   * @param assertion the active assertion
   * @throws NotAuthorizedException if authentication was required
   * @throws CatalogIndexException indicates an I/O error with the resource index
   * @throws AsnInsufficientPrivilegeException if the user has insufficient privilege
   */
  public void authorizeEnable(AsnContext context, Assertion assertion) 
    throws NotAuthorizedException, CatalogIndexException, AsnInsufficientPrivilegeException {
    this.authorizeAction(context,assertion,AsnAuthorizer.ACTION_ENABLE);
    this.authorizeResourceAccess(context);
  }
  
  /**
   * Authorizes a query based operation.
   * @param context the assertion operation context
   * @throws NotAuthorizedException if authentication was required
   * @throws CatalogIndexException indicates an I/O error with the resource index
   * @throws AsnInsufficientPrivilegeException if the user has insufficient privilege
   */
  public void authorizeQuery(AsnContext context) 
    throws NotAuthorizedException, CatalogIndexException, AsnInsufficientPrivilegeException {
    this.authorizeAction(context,null,AsnAuthorizer.ACTION_QUERY);
    this.authorizeResourceAccess(context);
  }
  
  /**
   * Authorizes an update operation.
   * @param context the assertion operation context
   * @param assertion the active assertion
   * @throws NotAuthorizedException if authentication was required
   * @throws CatalogIndexException indicates an I/O error with the resource index
   * @throws AsnInsufficientPrivilegeException if the user has insufficient privilege
   */
  public void authorizeUpdate(AsnContext context, Assertion assertion) 
    throws NotAuthorizedException, CatalogIndexException, AsnInsufficientPrivilegeException {
    this.authorizeAction(context,assertion,AsnAuthorizer.ACTION_UPDATE);
    this.authorizeResourceAccess(context);
  }
  
  /**
   * Authorizes user access to a subject resource.
   * @param context the assertion operation context
   * @throws CatalogIndexException indicates an I/O error with the resource index
   * @throws AsnInsufficientPrivilegeException if the user has insufficient privilege
   */
  private void authorizeResourceAccess(AsnContext context) 
    throws CatalogIndexException, AsnInsufficientPrivilegeException  {
    
    String resourceId = context.getOperation().getSystemPart().getResourceId();
    if ((resourceId == null) || (resourceId.length() == 0)) {
      return;
    }
        
    // ensure acl access
    RequestContext rContext = context.getRequestContext();
    AuthenticationStatus auth = rContext.getUser().getAuthenticationStatus();
    boolean bAdmin = auth.getAuthenticatedRoles().hasRole("gptAdministrator");
    if (!bAdmin) {      
      MetadataAcl acl = new MetadataAcl(rContext);
      if (!acl.isPolicyUnrestricted()) {
        LuceneIndexAdapter adapter = new LuceneIndexAdapter(rContext);
        String[] resourceAcls = adapter.queryAcls(resourceId);
        if ((resourceAcls != null) && (resourceAcls.length > 0)) {
          
          String[] userAcls = acl.makeUserAcl();
          if ((userAcls != null) && (userAcls.length > 0)) {
            for (String resourcePrincipal: resourceAcls) {
              for (String userPrincipal: userAcls) {
                if (resourcePrincipal.equalsIgnoreCase(userPrincipal)) {
                  return;
                }
              }
            }
          }
          throw new AsnInsufficientPrivilegeException();
          
        }
      }
    }
    
    // ensure the existence of a local resource id
    if (!context.getOperation().getAuthPolicy().getAllowNonLocalResourceIds()) {
      LuceneIndexAdapter adapter = new LuceneIndexAdapter(rContext);
      Timestamp ts = adapter.queryModifiedDate(resourceId);
      if (ts == null) {
        throw new CatalogIndexException("Invalid resource id: "+resourceId);
      }
    }
      
  }
  
  /**
   * Determines if the user can perform an action.
   * @param context the assertion operation context
   * @param policy the authorization policy
   * @param assertion the active assertion
   * @param action the action
   * @return true if the user can perform the action
   */
  private boolean canAct(AsnContext context, AsnAuthPolicy policy, Assertion assertion, String action) {
  
    // ensure an authorization policy
    if (policy == null) {
      return false;
    }
    
    // check the user, ensure an authenticated user if required
    User user = context.getRequestContext().getUser();
    boolean userWasAuthenticated = false;
    if ((user != null) && user.getAuthenticationStatus().getWasAuthenticated()) {
      userWasAuthenticated = true;
    }
    if (policy.getAuthenticationRequired() && !userWasAuthenticated) {
      return false;
    }
    
    // determine the write principals (creation or modification)
    AsnPrincipals principals = null;
    boolean isWrite = false;
    if (action.equals(AsnAuthorizer.ACTION_CREATE)) {
      isWrite = true;
      principals = policy.getCreatePrincipals();
    } else if (action.equals(AsnAuthorizer.ACTION_DELETE)) {
      isWrite = true;
      principals = policy.getDeletePrincipals();
      if (assertion == null) return false;
    } else if (action.equals(AsnAuthorizer.ACTION_DISABLE)) {
      isWrite = true;
      principals = policy.getEnableDisablePrincipals();
      if (assertion == null) return false;
      if (!assertion.getSystemPart().getEnabled()) return false;
    } else if (action.equals(AsnAuthorizer.ACTION_ENABLE)){
      isWrite = true;
      principals = policy.getEnableDisablePrincipals();
      if (assertion.getSystemPart().getEnabled()) return false;
      if (assertion == null) return false;
    } else if (action.equals(AsnAuthorizer.ACTION_QUERY)) {
      principals = policy.getQueryPrincipals();
    } else if (action.equals(AsnAuthorizer.ACTION_UPDATE)) {
      isWrite = true;
      principals = policy.getQueryPrincipals();
      if (assertion == null) return false;
      if (!assertion.getSystemPart().getEnabled()) return false;
    } 
    if ((policy == null) || (principals == null)) {
      return false;
    }
    
    // hard check to ensure an authenticated user for any modifications 
    // (regardless of configuration)
    if (isWrite && !userWasAuthenticated) {
      return false;
    }
    
    // check "any user" user privilege
    if (principals.contains(AsnConstants.PRINCIPAL_ANY)) {
      return true;
    }
    
    // check administrator privilege
    if (userWasAuthenticated) {
      if (principals.contains(AsnConstants.PRINCIPAL_ADMINISTRATOR)) {
        RoleSet roles = user.getAuthenticationStatus().getAuthenticatedRoles();
        if (roles.hasRole("gptAdministrator")) {
          return true;
        }
      }
    }
    
    // check for ownership
    if (userWasAuthenticated && (assertion != null) && assertion.getWasReadFromIndex()) {
      if (principals.contains(AsnConstants.PRINCIPAL_OWNER)) {
        String asnUserKey = Val.chkStr(assertion.getUserPart().getKey());
        String userKey = Val.chkStr(user.getKey());
        if ((asnUserKey.length() > 0) && asnUserKey.equals(userKey)) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  /**
   * Determines if the user can create an assertion.
   * @param context the assertion operation context
   * @param policy the authorization policy
   * @return true if the user can perform the action
   */
  public boolean canCreate(AsnContext context, AsnAuthPolicy policy) {
    return this.canAct(context,policy,null,AsnAuthorizer.ACTION_CREATE);
  }

  /**
   * Determines if a the action user can delete an assertion.
   * @param context the assertion operation context
   * @param policy the authorization policy
   * @param assertion the active assertion
   * @return true if the user can perform the action
   */
  public boolean canDelete(AsnContext context, AsnAuthPolicy policy, Assertion assertion) {
    return this.canAct(context,policy,assertion,AsnAuthorizer.ACTION_DELETE);
  }
  
  /**
   * Determines if a the action user can disable an assertion.
   * @param context the assertion operation context
   * @param policy the authorization policy
   * @param assertion the active assertion
   * @return true if the user can perform the action
   */
  public boolean canDisable(AsnContext context, AsnAuthPolicy policy, Assertion assertion) {
    return this.canAct(context,policy,assertion,AsnAuthorizer.ACTION_DISABLE);
  }
  
  /**
   * Determines if a the action user can create an assertion.
   * @param context the assertion operation context
   * @param policy the authorization policy
   * @param assertion the active assertion
   * @return true if the user can perform the action
   */
  public boolean canEnable(AsnContext context, AsnAuthPolicy policy, Assertion assertion) {
    return this.canAct(context,policy,assertion,AsnAuthorizer.ACTION_ENABLE);
  }
  
  /**
   * Determines if the user can query assertions.
   * @param context the assertion operation context
   * @param policy the authorization policy
   * @return true if the user can perform the action
   */
  public boolean canQuery(AsnContext context, AsnAuthPolicy policy) {
    return this.canAct(context,policy,null,AsnAuthorizer.ACTION_QUERY);
  }
  
  /**
   * Determines if a the action user can update an assertion.
   * @param context the assertion operation context
   * @param policy the authorization policy
   * @param assertion the active assertion
   * @return true if the user can perform the action
   */
  public boolean canUpdate(AsnContext context, AsnAuthPolicy policy, Assertion assertion) {
    return this.canAct(context,policy,assertion,AsnAuthorizer.ACTION_UPDATE);
  }
  
  /**
   * Establishes the user associated with the operation.
   * @param context the assertion operation context
   * @throws NotAuthorizedException if authentication was required
   * @throws AsnInsufficientPrivilegeException if the user has insufficient privilege
   * @throws AsnUnestablishedUserException if the user could not be established
   */
  public void establishUser(AsnContext context) 
    throws NotAuthorizedException, AsnUnestablishedUserException {
    
    // initialize
    this.setWasUserEstablished(false);
    AsnOperation operation = context.getOperation();
    User user = context.getRequestContext().getUser();
    
    // establish the user part of the operation
    if (operation.getUserPart() == null) {
      operation.setUserPart(new AsnUserPart());
    }
    operation.getUserPart().setIPAddress(context.getRequestOptions().getIPAddress());
    AsnAuthPolicy authPolicy = operation.getAuthPolicy();
    if (authPolicy.getAuthenticationRequired()) {
      if ((user == null) || !user.getAuthenticationStatus().getWasAuthenticated()) {
        throw new NotAuthorizedException("Not authorized.");
      }
    }
    if ((user == null) || !user.getAuthenticationStatus().getWasAuthenticated()) {
      operation.getUserPart().setName(AsnConstants.ANONYMOUS_USERNAME);
      this.setWasUserEstablished(true);
    } else {
      String key = Val.chkStr(user.getKey());
      if (key.length() > 0) {
        operation.getUserPart().setKey(key);
        if (user.getLocalID() >= 0) {
          operation.getUserPart().setID(""+user.getLocalID());
          String name = Val.chkStr(user.getName());
          if (name.length() > 0) {
            operation.getUserPart().setName(name);
            this.setWasUserEstablished(true);
          }
        }
      }
    }
    if (!this.getWasUserEstablished()) {
      throw new AsnUnestablishedUserException();
    }
    
    // check the admin database for a disabled user:ipaddress or user:key
    
    // check the admin index for moderation privileges
    
  }
    
}
