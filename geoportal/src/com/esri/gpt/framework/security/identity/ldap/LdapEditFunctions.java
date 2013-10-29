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
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;

import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.credentials.CredentialPolicyException;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.principal.Role;
import com.esri.gpt.framework.security.principal.Roles;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.UserAttribute;
import com.esri.gpt.framework.security.principal.UserAttributeMap;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Handles functionality related to editing an LDAP identity store.
 */
public class LdapEditFunctions extends LdapFunctions {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
protected LdapEditFunctions() {
  super();
}

/**
 * Construct with a supplied configuration.
 * @param configuration the configuration
 */
protected LdapEditFunctions(LdapConfiguration configuration) {
  super(configuration);
}

// properties ==================================================================

// methods =====================================================================

/**
 * Adds and attribute(s) to an LDAP object.
 * @param dirContext the directory context
 * @param objectDN the distinguished name for the object to modify
 * @param attributes the attribute collection to add
 * @throws NamingException if an exception occurs
 */
protected void addAttribute(DirContext dirContext,
                            String objectDN, 
                            Attributes attributes)
  throws NamingException {
  modifyEntry(dirContext,objectDN,DirContext.ADD_ATTRIBUTE,attributes);
}

/**
 * Adds an entry to LDAP.
 * @param dirContext the directory context
 * @param objectDN the distinguished name for the new entry
 * @param attributes the attributes for the new entry
 * @throws NamingException if an exception occurs
 */
protected void addEntry(DirContext dirContext,
                        String objectDN, 
                        Attributes attributes)
  throws NamingException {
  dirContext.createSubcontext(objectDN,attributes);
}

/**
 * Modifies the attribute(s) for an LDAP object.
 * @param dirContext the directory context
 * @param objectDN the distinguished name for the object to modify
 * @param operation the operation to perform
 *        DirContext.[ADD_ATTRIBUTE|REPLACE_ATTRIBUTE|REMOVE_ATTRIBUTE]
 * @param attributes the attribute collection to modify
 * @throws NamingException if an exception occurs
 */
private void modifyEntry(DirContext dirContext,
                         String objectDN,
                         int operation,
                         Attributes attributes)
  throws NamingException {
	try{
		dirContext.modifyAttributes(objectDN,operation,attributes);
	}catch(javax.naming.directory.InvalidAttributeValueException iave){
		LogUtil.getLogger().severe(iave.getMessage());
		FacesContextBroker contextBroker = new FacesContextBroker();
		MessageBroker msgBroker = contextBroker.extractMessageBroker();
		String errMsg = "javax.naming.directory.InvalidAttributeValueException";
		if(msgBroker != null){
			errMsg = msgBroker.getMessage("javax.naming.directory.InvalidAttributeValueException").getSummary();
		}
		throw new LdapException(errMsg);
	}
}

/**
 * Prepares attributes for a user that is about to be registered.
 * @param credentials the user credentials
 * @param localMap the user profile attribute map
 */
private Attributes prepareRegistrationAttributes(UsernamePasswordCredentials credentials,
                                                 UserAttributeMap localMap) {
  BasicAttributes ldapAttributes = new BasicAttributes();
  LdapUserProperties userProps = getConfiguration().getUserProperties();
  LdapNameMapping nameMap = userProps.getUserProfileMapping();

  // append required object classes
  Attribute objectClasses = userProps.getUserObjectClasses();
  if ((objectClasses != null) && (objectClasses.size() > 0)) {
    ldapAttributes.put(objectClasses);
  }

  // add the username and password from the credentials
  // ignore those profile attributes that do not have a corresponding LDAP key
  String sUsername = credentials.getUsername();
  String sPassword = credentials.encryptLdapPassword(
                     userProps.getPasswordEncryptionAlgorithm());
  localMap.set(UserAttributeMap.TAG_USER_NAME,sUsername);
  localMap.set(UserAttributeMap.TAG_USER_PASSWORD,sPassword);

  // append all attributes of the supplied user profile,
  // ignore those profile attributes that do not have a corresponding LDAP key
  BasicAttribute basicAttr;
  boolean bHasCN = false;
  for (UserAttribute localAttr: localMap.values()) {
    String sLdapKey = nameMap.findLdapName(localAttr.getKey());
    String sLocalValue = localAttr.getValue();
    if ((sLdapKey.length() > 0) && (sLocalValue != null) && (sLocalValue.length() > 0)) {
      basicAttr = new BasicAttribute(sLdapKey,localAttr.getValue());
      ldapAttributes.put(basicAttr);
      if (sLdapKey.equalsIgnoreCase("cn")) {
        bHasCN = false;
      }
    }
  }

  // ensure that a CN was added
  if (!bHasCN) {
    basicAttr = new BasicAttribute("cn",sUsername);
    ldapAttributes.put(basicAttr);
  }
  return ldapAttributes;
}

/**
 * Recovers a password.
 * <br/>The password is not actually recovered from LDAP,
 * a new password is generated and written to LDAP, the new password
 * is returned within the credentials.
 * @param dirContext the directory context
 * @param username the username
 * @param emailAddress the email address
 * @return the user associated with the recovered credentials (null if no match)
 * @throws NamingException if an LDAP naming exception occurs
 */
protected User recoverUserPassword(DirContext dirContext,
                                   String username,
                                   String emailAddress)
  throws NamingException {
  User userFound = null;
  UsernamePasswordCredentials credentials = null;
  username = Val.chkStr(username);
  emailAddress = Val.chkStr(emailAddress);
  if ((username.length() > 0) && (emailAddress.length() > 0)) {
    LdapQueryFunctions queryFunctions = new LdapQueryFunctions(getConfiguration());
    LdapUserProperties userProps = getConfiguration().getUserProperties();
    boolean bMultipleFound = false;
    String sBaseDN = userProps.getUserSearchDIT();
    String sFilter = userProps.returnUserLoginSearchFilter(username);
    StringSet ssDNs = queryFunctions.searchDNs(dirContext,sBaseDN,sFilter);

    // loop through each DN found, check for an email address match
    for (String sDN: ssDNs) {
      User userTmp = new User();
      userTmp.setDistinguishedName(sDN);
      queryFunctions.readUserProfile(dirContext,userTmp);
      if (userTmp.getProfile().getEmailAddress().equals(emailAddress)) {
        if (userFound == null) {
          credentials = new UsernamePasswordCredentials();
          credentials.setUsername(username);
          credentials.generatePassword();
          userFound = userTmp;
          userFound.setCredentials(credentials);
        } else {
          bMultipleFound = true;
          userFound = null;
          break;
        }
      }
    }
    
    if (userFound != null) {
      updateUserPassword(dirContext,userFound,credentials);
    } else if (bMultipleFound) {
      String sMsg = "Multiple LDAP usernames with same email address were located: "+
                    "username:"+username+ "  emailAddress="+emailAddress;
      LogUtil.getLogger().warning(sMsg);
    }
  }
  return userFound;
}

/**
 * Extract the request context.
 * @return the request context
 */
public RequestContext extractRequestContext() {
  return RequestContext.extract(null);
}

/**
 * Register a new user.
 * @param dirContext the directory context
 * @param user the subject user
 * @throws CredentialPolicyException if the username or password is empty
 * @throws NamingException if an LDAP naming exception occurs
 * @throws NameAlreadyBoundException if the new user DN already exists
 */
protected void registerUser(DirContext dirContext, User user) 
  throws CredentialPolicyException, NamingException, NameAlreadyBoundException {

  // initialize
  user.setDistinguishedName("");
  LdapUserProperties userProps = getConfiguration().getUserProperties();
  LdapGroupProperties groupProps = getConfiguration().getGroupProperties();
  UsernamePasswordCredentials upCreds;
  upCreds = user.getCredentials().getUsernamePasswordCredentials();
  if (upCreds != null) {
    user.setDistinguishedName(userProps.returnNewUserDN(upCreds.getUsername()));
  }
  
  if (upCreds == null) {
    throw new CredentialPolicyException("The credentials were not supplied.");
  } else if (user.getDistinguishedName().length() == 0) {
    throw new CredentialPolicyException("The supplied username is invalid.");
  } else if ((upCreds.getPassword() == null) || (upCreds.getPassword().length() == 0)) {
    throw new CredentialPolicyException("The supplied password is invalid.");
  }

  // prepare attributes and add the new user to LDAP
  Attributes attributes = prepareRegistrationAttributes(upCreds,user.getProfile());  
  addEntry(dirContext,user.getDistinguishedName(),attributes);

  // add user to general user group
  Roles configuredRoles = getConfiguration().getIdentityConfiguration().getConfiguredRoles();
  if (configuredRoles.getAuthenticatedUserRequiresRole()) {
    String sRoleRegistered = configuredRoles.getRegisteredUserRoleKey();
    Role roleRegistered = configuredRoles.get(sRoleRegistered);
    String sGeneralDN = roleRegistered.getDistinguishedName();
    String sGroupAttribute = groupProps.getGroupMemberAttribute();
    BasicAttribute groupAttribute = new BasicAttribute(sGroupAttribute);
    BasicAttributes groupAttributes = new BasicAttributes();
    groupAttribute.add(user.getDistinguishedName());
    groupAttributes.put(groupAttribute);
    addAttribute(dirContext,sGeneralDN,groupAttributes);
  }
}

/**
 * Adds user to role.
 * @param dirContext the directory context
 * @param user the subject user
 * @param role the role key for the role
 * @throws CredentialPolicyException if the username or password is empty
 * @throws NamingException if an LDAP naming exception occurs
 * @throws NameAlreadyBoundException if the new user DN already exists
 */
protected void addUserToRole(DirContext dirContext, User user, String role) 
  throws CredentialPolicyException, NamingException {
    
 //TODO: need to check if the user is already in role.

  // add user to general user group
  Roles configuredRoles = getConfiguration().getIdentityConfiguration().getConfiguredRoles();     
  Role roleRegistered = configuredRoles.get(role);    
  String sGeneralDN = roleRegistered.getDistinguishedName();
  addUserToGroup(dirContext, user, sGeneralDN);
}

/**
 * Adds user to group.
 * @param dirContext the directory context
 * @param user the subject user
 * @param groupDn the dn for the group
 * @throws CredentialPolicyException if the username or password is empty
 * @throws NamingException if an LDAP naming exception occurs
 * @throws NameAlreadyBoundException if the new user DN already exists
 */
protected void addUserToGroup(DirContext dirContext, User user, String groupDn) 
  throws CredentialPolicyException, NamingException {
    
  // initialize
  LdapGroupProperties groupProps = getConfiguration().getGroupProperties();   
  
  // add user to general user group
  String sGroupAttribute = groupProps.getGroupMemberAttribute();
  BasicAttribute groupAttribute = new BasicAttribute(sGroupAttribute);
  BasicAttributes groupAttributes = new BasicAttributes();
  groupAttribute.add(user.getDistinguishedName());
  groupAttributes.put(groupAttribute);
  addAttribute(dirContext,groupDn,groupAttributes);  
  /*
  Roles configuredRoles = getConfiguration().getIdentityConfiguration().getConfiguredRoles();     
  for (Role role : configuredRoles.values()){
	  if(role.getDistinguishedName().equalsIgnoreCase(groupDn)){
		  String accessKeyAttribute = Val.chkStr(role.getAccessKey());
		  if(accessKeyAttribute.length() > 0){
			  BasicAttribute accessAttr = new BasicAttribute(accessKeyAttribute);
			  BasicAttributes attributes = new BasicAttributes();
			  attributes.put(accessAttr);
			  try {
				  removeEntry(dirContext,user.getDistinguishedName(), attributes);
			  }catch(javax.naming.directory.AttributeInUseException aue){}
			  catch(javax.naming.directory.NoSuchAttributeException nse){}
			  break;
		  }
	  }
  }*/
}


/**
 * Removes an attribute(s) from an LDAP object.
 * @param dirContext the directory context
 * @param objectDN the distinguished name for the object to modify
 * @param attributes the attribute collection to remove
 * @throws NamingException if an exception occurs
 */
protected void removeEntry(DirContext dirContext,
                           String objectDN, 
                           Attributes attributes)
  throws NamingException {
  modifyEntry(dirContext,objectDN,DirContext.REMOVE_ATTRIBUTE,attributes);
}

/**
 * Updates the profile attributes for a user.
 * @param dirContext the directory context
 * @param user the subject user
 * @param considerUsername true if the username should be considered for update
 * @param considerPassword true if the password should be considered for update
 * @throws NamingException if an LDAP naming exception occurs
 */
protected void updateUserProfile(DirContext dirContext, 
                                 User user,
                                 boolean considerUsername,
                                 boolean considerPassword)  
  throws NamingException {

  // initialize
  ArrayList<ModificationItem>  alModItems = new ArrayList<ModificationItem>();
  Attributes ldapAttributes = null;
  String sUserDN = user.getDistinguishedName();
  if (sUserDN.length() > 0) {
    ldapAttributes = dirContext.getAttributes(sUserDN);
  }
  if (ldapAttributes != null) {

    // iterate through the attributes of the supplied user profile
    LdapNameMapping  nameMap  = getConfiguration().getUserProperties().getUserProfileMapping();
    UserAttributeMap localMap = user.getProfile();
    ModificationItem modItem;
    for (UserAttribute localAttr: localMap.values()) {
      
      // determine the local and LDAP keys
      String sLocalKey = localAttr.getKey();
      String sLocalValue = localAttr.getValue();
      String sLdapKey = nameMap.findLdapName(localAttr.getKey());
      if ((sLocalValue == null) || (sLocalValue.length() == 0)) {
        sLocalValue = null;
      }
      
      //System.err.println("sLocalKey="+sLocalKey+" sLdapKey="+sLdapKey);
      if (sLocalKey.equalsIgnoreCase(UserAttributeMap.TAG_USER_NAME)) {
        if (!considerUsername) sLdapKey = "";
      } else if (sLocalKey.equalsIgnoreCase(UserAttributeMap.TAG_USER_PASSWORD)) {
        if (!considerPassword) sLdapKey = "";
      }

      if (sLdapKey.length() > 0) {

        // if the attribute exists in LDAP then replace the value,
        // otherwise create a new attribute
        Attribute ldapAttribute = ldapAttributes.get(sLdapKey);
        if (ldapAttribute != null) {
          if (!ldapAttribute.isOrdered()) {
            ldapAttribute.clear();
            ldapAttribute.add(sLocalValue);
          } else {
            ldapAttribute.set(0,sLocalValue);
          }
          modItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,ldapAttribute);
          alModItems.add(modItem);
        } else {
          if ((sLocalValue != null) && (sLocalValue.length() > 0)) {
            BasicAttribute basicAttr = new BasicAttribute(sLdapKey,sLocalValue);
            modItem = new ModificationItem(DirContext.ADD_ATTRIBUTE,basicAttr);
            alModItems.add(modItem);
          }
        }

       } else {
        // no associated LDAP key, we won't throw an exception in this case
      }
    }
  }

  // execute the LDAP modification if modification items exist
  if (alModItems.size() > 0) {
    ModificationItem[] modItems = (ModificationItem[])alModItems.toArray(
                                   new ModificationItem[0]);
    dirContext.modifyAttributes(sUserDN,modItems);

  }
}

/**
 * Updates the password for a user.
 * @param dirContext the directory context
 * @param user the subject user
 * @param newCredentials the credentials containing the new password
 * @throws NamingException if an LDAP naming exception occurs
 */
protected void updateUserPassword(DirContext dirContext, 
                                  User user,
                                  UsernamePasswordCredentials newCredentials)
  throws NamingException {
  User userUpd = new User();
  userUpd.setDistinguishedName(user.getDistinguishedName());
  String sPassword = newCredentials.encryptLdapPassword(
         getConfiguration().getUserProperties().getPasswordEncryptionAlgorithm());
  userUpd.getProfile().set(UserAttributeMap.TAG_USER_PASSWORD,sPassword);
  updateUserProfile(dirContext,userUpd,false,true);
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
protected void removeUserFromGroup(DirContext dirContext,User user, String groupDn)
  throws CredentialPolicyException, IdentityException, NamingException, SQLException {
  LdapClient client = null;
  ArrayList<ModificationItem>  alModItems = new ArrayList<ModificationItem>();
  ModificationItem[] modItems = null;
  try {
           	  	  
    Attributes attributes = dirContext.getAttributes(groupDn);
 // initialize
    
    ModificationItem modItem;
    
    try {
      if (attributes != null) {
      	Attribute attr = attributes.get("uniqueMember");
      		NamingEnumeration<?> vals = attr.getAll();;
      		while (vals.hasMore()) {
      			String val = (String) vals.next();
      			if(val.equalsIgnoreCase(user.getDistinguishedName())){
      				attr.remove(val);
      			}
      			
      		}
      		
      		modItem = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,attr);
          alModItems.add(modItem);

      }
    } finally {
    }
    
 // execute the LDAP modification if modification items exist
    if (alModItems.size() > 0) {
      modItems = (ModificationItem[])alModItems.toArray(
                                     new ModificationItem[0]);
      dirContext.modifyAttributes(groupDn,modItems);
    }

    
  } finally {
    if (client != null) client.close();
  }
}

}

