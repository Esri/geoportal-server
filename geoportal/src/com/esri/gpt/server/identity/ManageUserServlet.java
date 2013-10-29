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
package com.esri.gpt.server.identity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.naming.NamingException;
import javax.naming.directory.AttributeInUseException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.credentials.CredentialPolicyException;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.DistinguishedNameCredential;
import com.esri.gpt.framework.security.credentials.UsernameCredential;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.identity.ldap.LdapConfiguration;
import com.esri.gpt.framework.security.identity.ldap.LdapIdentityAdapter;
import com.esri.gpt.framework.security.principal.Group;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.Role;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.security.principal.Roles;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.UserAttribute;
import com.esri.gpt.framework.security.principal.UserAttributeMap;
import com.esri.gpt.framework.security.principal.Users;
import com.esri.gpt.framework.util.Val;

/**
 * Manage User servlet.
 * Provides user role management functionality. 
 */
public class ManageUserServlet extends BaseServlet {

// class variables =============================================================
private MessageBroker msgBroker = null;
private String userDIT = "ou=users,ou=system";
private String groupDIT = "ou=groups,ou=system";
	
/** Serialization key */
private static final long serialVersionUID = 1L;

// constructors ================================================================

/**
 * Creates instance of the servlet.
 */
public ManageUserServlet() {}

// properties ==================================================================


// methods =====================================================================
/**
 * Process the HTTP request.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws ServletException if error invoking command.
 * @throws IOException if error writing to the buffer.
 */
@SuppressWarnings("unused")
protected void execute(HttpServletRequest request,
                     HttpServletResponse response,
                     RequestContext context)
  throws Exception {
   msgBroker = 
    new FacesContextBroker(request,response).extractMessageBroker();    
    
    String homePage = "/catalog/main/home.page";
	String contextPath = request.getContextPath();   
	try {
		
		if(!checkHasManageUsers(context)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\"Invalid request.\"}");
			return;
		}
		checkRole(context);
		
	} catch (NotAuthorizedException e) {
		response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "{ \"error\":\"Not Authorized.\"}");
		return;
	}
	String[] parts = request.getRequestURI().toString().split("/");
	IdentityConfiguration idConfig = context.getIdentityConfiguration();
	if(idConfig != null){
		LdapConfiguration ldapConfig = idConfig.getLdapConfiguration();
		if(ldapConfig != null){
		    userDIT = ldapConfig.getUserProperties().getUserSearchDIT();
			groupDIT = ldapConfig.getGroupProperties().getGroupSearchDIT();
		}
	}
	if(parts.length >= 5 && parts[4].equals("users") && parts[5].equals("search")){
		executeSearch(request,response,context); 	
	}else if(parts.length >= 5 && parts[4].equals("users") && parts[5].equals("searchMembers")){
		executeSearchMembers(request,response,context); 		
	}else if(parts.length >= 5 && (parts[4].equals("users")) && parts[5].equals("addAttribute")){
		// executeModifyUserAttribute(request,response,context,true); 	
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\"Invalid request.\"}");
		return;
	}else if(parts.length >= 5 && (parts[4].equals("users")) && parts[5].equals("removeAttribute")){
		// executeModifyUserAttribute(request,response,context,false);
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\"Invalid request.\"}");
		return;
	}else if(parts.length >= 5 && (parts[4].equals("groups")) && parts[5].equals("addAttribute")){
		// executeModifyGroupAttribute(request,response,context,true);
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\"Invalid request.\"}");
		return;
	}else if(parts.length >= 5 && (parts[4].equals("groups")) && parts[5].equals("removeAttribute")){
		executeModifyGroupAttribute(request,response,context,false);
	}else if(parts.length >= 7 && parts[4].equals("users") && parts[6].equals("profile")){
		executeReadUser(request,response,context); 	
	}else if(parts.length >= 7 && parts[4].equals("groups") && parts[6].equals("addMember")){
		executeAddMember(request,response,context); 
	}else if(parts.length >= 7 && parts[4].equals("groups") && parts[6].equals("removeMember")){
		executeRemoveMember(request,response,context); 
	}else if(parts.length >= 7 && parts[4].equals("users") && parts[6].equals("delete")){
		if(!checkHasDeleteUser(context)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\"Invalid request.\"}");
			return;
		}
		executeDeleteUser(request,response,context); 
	}else if(parts.length >= 5 && parts[4].equals("users")){
		executeReadUser(request,response,context); 	
	}else if(parts.length >= 5 && parts[4].equals("groups") && parts[5].equals("configured")){
		executeReadConfigureRoles(request,response,context); 	
	}else{		
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\"Invalid request.\"}");
		return;
	}	
 }

/**
 * Reads configured roles.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws IdentityException if a system error occurs
 */
private void executeReadConfigureRoles(HttpServletRequest request,
		HttpServletResponse response, RequestContext context) throws Exception {
	String mimeType = "application/json";
	String rolesJson = " { \"configuredRoles\" : [";
	Roles roles = buildSelectableRoles(context);
	ArrayList<String> sortedKeys=new ArrayList<String>(roles.keySet());
	Collections.sort(sortedKeys);
	boolean firstRole = true;
	for(int i=0; i <sortedKeys.size(); i++){
		Role role = roles.get(sortedKeys.get(i));
		String roleDn = Val.chkStr(role.getDistinguishedName());
		String roleKey = Val.chkStr(role.getKey());
		String roleName = msgBroker.retrieveMessage(Val.chkStr(role.getResKey()));
		if(!role.isManage()) continue;
		if(!firstRole) {
			rolesJson += ",";
		}else{
			firstRole = false;
		}
		rolesJson += " { \"roleName\" : \"" + Val.escapeStrForJson(roleName) + "\" , \"roleDn\" : \"" + Val.escapeStrForJson(roleDn) + "\" , \"roleKey\" : \"" + Val.escapeStrForJson(roleKey) + "\" }";  
	}
	rolesJson += " ] } ";
	
	writeCharacterResponse(response,
			rolesJson,"UTF-8",mimeType+";charset=UTF-8");
}

/**
 * Add attribute to ldap entry.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws IdentityException if a system error occurs preventing the action
 * @throws IOException if error writing to the buffer
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException 
 * @throws CredentialPolicyException 
 */
private void executeModifyUserAttribute(HttpServletRequest request,
        HttpServletResponse response,
        RequestContext context, boolean isAddAttributeRequest) throws IdentityException, IOException, NamingException, SQLException, CredentialPolicyException {
	String mimeType = "application/json";
	String filter = Val.chkStr(request.getParameter("q"));
	String attributeName = Val.chkStr(request.getParameter("an"));
	String attributeValue = Val.chkStr(request.getParameter("av"));
	if(filter.length() == 0) {
		response.getWriter().write("{ \"response\" : \"noResults\" }");
		return;
	} 
	IdentityAdapter idAdapter = context.newIdentityAdapter();
	Users users = idAdapter.readUsers(filter,null);
	for (User u : users.values()){		
		if(isAddAttributeRequest){
			try{
				idAdapter.addAttribute(u.getDistinguishedName(), attributeName, attributeValue);
			}catch(AttributeInUseException aiue){
				// TODO : do nothing if attribute exists ? or overwrite ?
			}
		}else {
			idAdapter.removeAttribute(u.getDistinguishedName(), attributeName, attributeValue);
		}			
	}
	writeCharacterResponse(response,"{ \"response\" : \"User attribute modification was successful.\" }","UTF-8",mimeType+";charset=UTF-8");
}

private void executeModifyGroupAttribute(HttpServletRequest request,
        HttpServletResponse response,
        RequestContext context, boolean isAddAttributeRequest) throws IdentityException, IOException, NamingException, SQLException, CredentialPolicyException {
	String mimeType = "application/json";
	String filter = Val.chkStr(request.getParameter("q"));
	String attributeName = Val.chkStr(request.getParameter("an"));
	String attributeValue = Val.chkStr(request.getParameter("av"));
	if(filter.length() == 0) {
		response.getWriter().write("{ \"response\" : \"noResults\" }");
		return;
	} 
	
	IdentityAdapter idAdapter = context.newIdentityAdapter();
	
	/*User selectableUser = new User();
    selectableUser.setDistinguishedName("*");
    idAdapter.readUserGroups(selectableUser);
    selectableGroups = selectableUser.getGroups();*/
    
	Groups groups = idAdapter.readGroups(filter);
	for (Group g : groups.values()){
		if(isAddAttributeRequest){
			try{
				idAdapter.addAttribute(g.getDistinguishedName(), attributeName, attributeValue);
			}catch(AttributeInUseException aiue){
				// TODO : do nothing if attribute exists ? or overwrite ?
			}
		}else {
			idAdapter.removeAttribute(g.getDistinguishedName(), attributeName, attributeValue);
		}
	}
	
	 writeCharacterResponse(response,"{ \"response\" : \"Group attribute modification was successful.\" }","UTF-8",mimeType+";charset=UTF-8");
}

/**
 * Searches users matching filter in ldap.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws IdentityException if a system error occurs preventing the action
 * @throws IOException if error writing to the buffer
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException 
 */
protected void executeSearchMembers(HttpServletRequest request,
        HttpServletResponse response,
        RequestContext context) throws IdentityException, IOException, NamingException, SQLException {
	String mimeType = "application/json";
	String filter = Val.chkStr(request.getParameter("q"));
	String attributeName = Val.chkStr(request.getParameter("a"));
	if(filter.length() == 0) {
		response.getWriter().write("{ \"response\" : \"noResults\" }");
		return;
	} 
	 writeCharacterResponse(response,serializeUsersAsJson(context,filter,attributeName,true),"UTF-8",mimeType+";charset=UTF-8");
}

/**
 * Searches users matching filter in ldap.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws IdentityException if a system error occurs preventing the action
 * @throws IOException if error writing to the buffer
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException 
 */
protected void executeSearch(HttpServletRequest request,
        HttpServletResponse response,
        RequestContext context) throws IdentityException, IOException, NamingException, SQLException {
	String mimeType = "application/json";
	String filter = Val.chkStr(request.getParameter("q"));
	if(filter.length() == 0) {
		response.getWriter().write("{ \"response\" : \"noResults\" }");
		return;
	} else {
		if(!filter.contains("*")){
		  filter += "*";
		}
	}
	 writeCharacterResponse(response,serializeUsersAsJson(context,filter,null,false),"UTF-8",mimeType+";charset=UTF-8");
}

/**
 * Reads user information from ldap.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws IOException if error writing to the buffer
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 */
protected void executeReadUser(HttpServletRequest request,
        HttpServletResponse response,
        RequestContext context) throws Exception {
	String mimeType = "application/json";
	String[] parts = request.getRequestURI().toString().split("/");	
	if(parts.length == 0) {
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\"Invalid request.\"}");
		return;		
	}	
	else {	
		User user = readUserProfile(context,request);
	    writeCharacterResponse(response,
			serializeUserAsJson(context,user),"UTF-8",mimeType+";charset=UTF-8");
	}

}



/**
 * Serializes user information from ldap to json string.
 * @param context request context
 * @param user the user to be serialized
 * @return the user profile information serialized as json string.
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 */
protected String serializeUserAsJson(RequestContext context,User user) throws IdentityException, NamingException{
	String usersJson = "{ \"attributes\": [";
	UserAttributeMap attributes = user.getProfile();
	boolean first = true;
	List<String> sortedKeys=new ArrayList<String>(attributes.keySet());
	// Collections.sort(sortedKeys); TODO to sort or not ?
	for(int i=0; i <sortedKeys.size(); i++){
		UserAttribute attr = attributes.get(sortedKeys.get(i));
		String key = Val.chkStr(msgBroker.retrieveMessage("catalog.identity.profile.label." + attr.getKey()));
		String value = "";		  
		value = Val.chkStr(attr.getValue());
		if(attr.getKey().equalsIgnoreCase("password")) continue;
		if(!first) {
			usersJson += ",";
		}else{
			first = false;
		}		
		usersJson += " { \"key\" : \"" + Val.escapeStrForJson(key) + "\" , \"value\" : \"" + Val.escapeStrForJson(value) + "\" }";  
	}
	usersJson += " ] , ";
	
	usersJson += " \"userDn\" : \"" + user.getDistinguishedName() + " \" , ";
	
	String groupsJson = " \"groups\" : [";
	Groups groups = user.getGroups();
	groups.sort();
	boolean firstGroup = true;
	for (Group group : groups.values()) {
		String gkey = Val.chkStr(group.getKey());
		String name = Val.chkStr(group.getName());
		String dn = Val.chkStr(group.getDistinguishedName());
		if(!firstGroup) {
			groupsJson += ",";
		}else{
			firstGroup = false;
		}
		groupsJson += " { \"key\" : \"" + Val.escapeStrForJson(gkey) + "\" , \"name\" : \"" + Val.escapeStrForJson(name) + "\" , \"dn\" : \"" + Val.escapeStrForJson(dn) + "\" }";
	}
	groupsJson += " ] , ";

	String rolesJson = " \"selectableRoles\" : [";
	Roles roles = buildSelectableRoles(context);
	sortedKeys=new ArrayList<String>(roles.keySet());
	Collections.sort(sortedKeys);
	boolean firstRole = true;
	for(int i=0; i <sortedKeys.size(); i++){
		Role role = roles.get(sortedKeys.get(i));
		String roleDn = Val.chkStr(role.getDistinguishedName());
		String roleKey = Val.chkStr(role.getKey());
		String roleName = msgBroker.retrieveMessage(Val.chkStr(role.getResKey()));
		if(!role.isManage()) continue;
		boolean hasRole = false;
		for (Group group : groups.values()){
			String groupDn = Val.chkStr(group.getDistinguishedName());
			if(roleDn.equals(groupDn)){
				hasRole = true;
				break;
			}
		}
		if(!firstRole) {
			rolesJson += ",";
		}else{
			firstRole = false;
		}
		rolesJson += " { \"roleName\" : \"" + Val.escapeStrForJson(roleName) + "\" , \"roleDn\" : \"" + Val.escapeStrForJson(roleDn) + "\" , \"roleKey\" : \"" + Val.escapeStrForJson(roleKey) + "\" , \"hasRole\" : \"" + hasRole + "\" }";  
	}
	rolesJson += " ] } ";
	String json = usersJson + groupsJson + rolesJson;
	return json;
 }

/**
 * Executes a add member action.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws Exception if an exception occurs
 */
protected void executeAddMember(HttpServletRequest request,
						        HttpServletResponse response,
						        RequestContext context) 
  throws Exception {
  try {
	String[] parts = request.getRequestURI().toString().split("/");
  	String member = Val.chkStr(request.getParameter("member")); 
  	String attempt = Val.chkStr(request.getParameter("attempt"));
  	IdentityAdapter idAdapter = context.newIdentityAdapter();
    User user = new User();
    user.setDistinguishedName(member);
    idAdapter.readUserProfile(user);
    boolean isSelf = checkSelf(context,member);
    if((isSelf && attempt.equals("2")) || !isSelf){    	
    	if(parts.length > 0) {
    		String groupIdentifier = URLDecoder.decode(parts[5].trim(),"UTF-8");
    		boolean checkGroupConfigured = true;
    		if(checkIfAllowConfigured(context)){
    			checkGroupConfigured = checkIfConfigured(context,groupIdentifier);
    		}
    		boolean isAllowedToManage = true;
    		isAllowedToManage = checkIfAllowedToManage(context, groupIdentifier);
    		if(checkGroupConfigured){
    			if(isAllowedToManage){
		    		if(groupIdentifier.endsWith(groupDIT)){
		    			idAdapter.addUserToGroup(user, groupIdentifier);   		    
		    		}else{	  		
		    			idAdapter.addUserToRole(user, groupIdentifier);	    
		    		}
    			}else{
        			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\""+ groupIdentifier +" is not allowed to be managed in geoportal. \"}");
        			return;
        		}
    		}else{
    			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\""+ groupIdentifier +" is not configured in geoportal. \"}");
    			return;
    		}
    	}
	  	response.getWriter().write(msgBroker.retrieveMessage("catalog.identity.addRole.success"));
    }else{
    	response.getWriter().write("prompt");
    }
  } finally{}
}

/**
 * Executes a remove member action.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws Exception if an exception occurs
 */
protected void executeRemoveMember(HttpServletRequest request,
						        HttpServletResponse response,
						        RequestContext context) 
  throws Exception {
  try {
	String[] parts = request.getRequestURI().toString().split("/");  
	String member = Val.chkStr(request.getParameter("member")); 
  	String attempt = Val.chkStr(request.getParameter("attempt"));
    IdentityAdapter idAdapter = context.newIdentityAdapter();
    User user = new User();
    user.setDistinguishedName(member);
    idAdapter.readUserProfile(user);
    if(parts.length > 0) {
		String groupIdentifier = URLDecoder.decode(parts[5].trim(),"UTF-8");
		if(!groupIdentifier.endsWith(groupDIT)){ 
		    IdentityConfiguration idConfig = context.getIdentityConfiguration();   
		    Roles configuredRoles = idConfig.getConfiguredRoles();     
			Role roleRegistered = configuredRoles.get(groupIdentifier);    
			groupIdentifier = roleRegistered.getDistinguishedName();
		}
	    boolean isSelf = checkSelf(context,member);
	    if((isSelf && attempt.equals("2")) || !isSelf){
	    	
	    	boolean checkGroupConfigured = true;
    		if(checkIfAllowConfigured(context)){
    			checkGroupConfigured = checkIfConfigured(context,groupIdentifier);
    		}
    		boolean isAllowedToManage = true;
    		isAllowedToManage = checkIfAllowedToManage(context, groupIdentifier);
    		if(checkGroupConfigured){
    			if(isAllowedToManage){
    				idAdapter.removeUserFromGroup(user, groupIdentifier);
    			    response.getWriter().write(msgBroker.retrieveMessage("catalog.identity.removeRole.success"));
    			}else{
        			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\""+ groupIdentifier +" is not allowed to be managed in geoportal. \"}");
        			return;
        		}
    		}else{
    			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\""+ groupIdentifier +" is not configured in geoportal. \"}");
    			return;
    		}
		    
	    }else{
	    	response.getWriter().write("prompt");
	    }
	}
  } finally{}
}

/**
 * Executes a delete user action.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws Exception if an exception occurs
 */
private void executeDeleteUser(HttpServletRequest request,
        HttpServletResponse response,
        RequestContext context) 
  throws Exception {
  try {
	String[] parts = request.getRequestURI().toString().split("/");
	if(parts.length > 0) {
		String userIdentifier = URLDecoder.decode(parts[5].trim(),"UTF-8");
		if(userIdentifier.endsWith(userDIT)){  
			String attempt = Val.chkStr(request.getParameter("attempt"));
		    IdentityAdapter idAdapter = context.newIdentityAdapter();
		    User user = new User();
		    user.setDistinguishedName(userIdentifier);
		    idAdapter.readUserProfile(user);    
		    idAdapter.readUserGroups(user);
		    
		    boolean isSelf = checkSelf(context,userIdentifier);
		    if((isSelf && attempt.equals("2")) || !isSelf){
		    	idAdapter.deleteUser(user);
		    	response.getWriter().write(msgBroker.retrieveMessage("catalog.identity.deleteUser.success"));
		    }else{
		    	response.getWriter().write("prompt");
		    }
		}
	}    
  } finally{}
}

/**
 * Checks if group is configured.
 * @param context
 * @param groupIdentifier
 * @return true if group is configured in geoportal
 */
protected boolean checkIfConfigured(RequestContext context,String groupIdentifier){
	boolean isConfigured = false;		
	Roles roles = buildSelectableRoles(context);
	for (Role role : roles.values()){
		if(groupIdentifier.endsWith(groupDIT)){
			if(role.getDistinguishedName().equalsIgnoreCase(groupIdentifier)) {
				isConfigured = true;
				break;
			}
		}else{
			if(role.getKey().equalsIgnoreCase(groupIdentifier)) {
				isConfigured = true;
				break;
			}
		}
	}
	return isConfigured;
}

/**
 * Checks if group is allowed to manage.
 * @param context
 * @param groupIdentifier
 * @return true if group is allowed to manage in geoportal
 */
protected boolean checkIfAllowedToManage(RequestContext context,String groupIdentifier){
	boolean isAllowedToManage = false;		
	Roles roles = buildSelectableRoles(context);
	for (Role role : roles.values()){
		if(groupIdentifier.endsWith(groupDIT)){
			if(role.getDistinguishedName().equalsIgnoreCase(groupIdentifier) && role.isManage()) {
				isAllowedToManage = true;
				break;
			}
		}else{
			if(role.getKey().equalsIgnoreCase(groupIdentifier) && role.isManage()) {
				isAllowedToManage = true;
				break;
			}
		}
	}
	return isAllowedToManage;
}

/**
 * Checks if managed user is active user.
 * @param context
 * @param managedUserDn
 * @return true if managed user is same as active user
 */
protected boolean checkSelf(RequestContext context,String managedUserDn){
	boolean isSelf = false;
	User user = context.getUser();
	if(user.getDistinguishedName().equals(managedUserDn)){
		isSelf = true;
	}
	return isSelf;
}

/**
 * Checks if user role matches provided groups distinguished name.
 * @param user user
 * @param groupDn group distingushed name
 * @return true if managed user role is same as groupDn
 */
protected boolean checkRole(User user,String groupDn){
	boolean isSelf = false;
	Groups groups = user.getGroups();
	for (Group group : groups.values()){
		String dn = Val.chkStr(group.getDistinguishedName());
		if(dn.equals(groupDn)){
			isSelf = true;
			break;
		}
	}
	return isSelf;
}

/**
 * Serializes list of ldap users matching filter.
 * @param context the current request context
 * @param filter the user search filter for ldap
 * @return the list of users as json 
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException 
 */
protected String serializeUsersAsJson(RequestContext context, String filter,String attributeName, boolean isMemberSearch) throws IdentityException, NamingException, SQLException {
	Users users = new Users();
	int totalMatches = 0;
	if(!isMemberSearch){
		HashMap<String,Object> resultsMap = buildUsersList(context, filter,null);
		users = (Users) resultsMap.get("topUserMatches");
		totalMatches = (Integer) resultsMap.get("totalMatches");
	}else if(isMemberSearch && attributeName != null){
		Roles configuredRoles = context.getIdentityConfiguration().getConfiguredRoles();
		Role role = configuredRoles.get(attributeName);
		String sDn = role.getDistinguishedName();
		IdentityAdapter idAdapter = context.newIdentityAdapter();
		users = idAdapter.readGroupMembers(sDn);
		totalMatches = users.size();
		users.sort();
	}else{
		IdentityAdapter idAdapter = context.newIdentityAdapter();
		Users members = idAdapter.readGroupMembers(filter);
	    for (User u: members.values()) {
	    	users.add(u);	    
	    }
	    users.sort();
	    totalMatches = users.size();
	}
	
	String usersJson = "{ \"totalUsers\" : \"" + totalMatches + "\" ,\"topUsers\" : \"" + users.size() + "\" , \"users\": [";
	boolean firstUser = true;
	for (User user : users.values()){
		String userName = user.getName();
		String dn = user.getKey();
		if(!firstUser) {
			usersJson += ",";
		}else{
			firstUser = false;
		}
		usersJson += " { \"dn\" : \"" + dn + "\" , \"userName\" : \"" + Val.escapeStrForJson(userName) + "\" }";  
	}
	usersJson += " ] }";
	return usersJson;
 }

/**
 * Checks if manage user role is enabled
 * @param context the current request context
 * @return true is the functionality is enabled
 */
protected boolean checkHasManageUsers(RequestContext context) {
	boolean umHasDeleteUserLink = false;
	UsernamePasswordCredentials upc = context.getIdentityConfiguration().getSimpleConfiguration().getServiceAccountCredentials();
	if(upc !=null) return umHasDeleteUserLink;
	
	StringAttributeMap umParameters = context.getCatalogConfiguration().getParameters();
	if(umParameters.containsKey("ldap.identity.manage.userRoleEnabled")){	
		String umHasDeleteUserLinkEnabled = com.esri.gpt.framework.util.Val.chkStr(umParameters.getValue("ldap.identity.manage.userRoleEnabled"));
		umHasDeleteUserLink = Boolean.valueOf(umHasDeleteUserLinkEnabled);
	}
	return umHasDeleteUserLink;
}

/**
 * Checks if delete user from ldap is enabled
 * @param context the current request context
 * @return true is the functionality is enabled
 */
private boolean checkHasDeleteUser(RequestContext context) {
	boolean umHasDeleteUserButton = false;
	StringAttributeMap umParameters = context.getCatalogConfiguration().getParameters();
	if(umParameters.containsKey("ldap.identity.manage.userRoleEnabled")){	
	   String umDeleteUserButtonEnabled = com.esri.gpt.framework.util.Val.chkStr(umParameters.getValue("ldap.identity.manage.userRoleEnabled"));
	   umHasDeleteUserButton = Boolean.valueOf(umDeleteUserButtonEnabled);
	}
	return umHasDeleteUserButton;
}

/**
 * Checks if manage user role is restricted to configured geoportal roles.
 * @param context the current request context
 * @return true is the functionality is enabled
 */
protected boolean checkIfAllowConfigured(RequestContext context) {
	boolean bCheckIfAllowed = false;
	StringAttributeMap umParameters = context.getCatalogConfiguration().getParameters();
	if(umParameters.containsKey("ldap.identity.restrictToConfiguredRoles")){	
	   String sCheckIfAllowed = com.esri.gpt.framework.util.Val.chkStr(umParameters.getValue("ldap.identity.restrictToConfiguredRoles"));
	   bCheckIfAllowed = Boolean.valueOf(sCheckIfAllowed);
	}
	return bCheckIfAllowed;
}

/** 
 * Constructs a administrator based upon the user associated with the 
 * current request context.
 * @param context the current request context (contains the active user)
 * @throws NotAuthorizedException if the user does not have publishing rights
 */
protected void checkRole(RequestContext context) 
  throws NotAuthorizedException {
  
  // initialize
  User user = context.getUser();
  user.setKey(user.getKey());
  user.setLocalID(user.getLocalID());
  user.setDistinguishedName(user.getDistinguishedName());
  user.setName(user.getName());
  
  // establish credentials
  UsernamePasswordCredentials creds = new UsernamePasswordCredentials();
  creds.setUsername(user.getName());
  user.setCredentials(creds);
  
  user.setAuthenticationStatus(user.getAuthenticationStatus());  
  assertAdministratorRole(user);
}

/**
 * Asserts the administrator role.
 * @throws NotAuthorizedException if the administrator role has not been granted
 */
private void assertAdministratorRole(User user) throws NotAuthorizedException {
  RoleSet roles = user.getAuthenticationStatus().getAuthenticatedRoles();
  roles.assertRole("gptAdministrator");
}

/**
 * Gets the status indicating whether this publisher is an administrator.
 * @return true if this publisher is an administrator
 */
private boolean getIsAdministrator(User user) {
  RoleSet roles = user.getAuthenticationStatus().getAuthenticatedRoles();
  return roles.hasRole("gptAdministrator");
}

/**
 * Builds a collection of configured roles in Geoportal (gpt.xml).
 * @param context
 *          the current request context (contains the active user)
 * @return the collection of roles
 */
protected Roles buildSelectableRoles(RequestContext context) {
  IdentityConfiguration idConfig = context.getIdentityConfiguration();
  Roles selectableRoles = idConfig.getConfiguredRoles();
  return selectableRoles;
}

/**
 * Builds list of ldap users matching filter.
 * @param context the current request context (contains the active user)
 * @param filter the user search filter for ldap
 * @return the list of users matching filter
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 */
protected HashMap<String,Object> buildUsersList(RequestContext context,String filter, String attributeName) 
		throws IdentityException, NamingException {
	HashMap<String,Object> resultsMap = new HashMap<String,Object>();
	IdentityAdapter idAdapter = context.newIdentityAdapter();
	String searchLimit = Val.chkStr(context.getCatalogConfiguration().getParameters().getValue("ldap.identity.search.maxResults"));
	int srchLimit = -1;
	if(searchLimit.length() > 0){
		srchLimit = Integer.parseInt(searchLimit);
	}
	Users users = idAdapter.readUsers(filter,attributeName);
	users.sort();
	int totalMatches = users.size();
	resultsMap.put("totalMatches", totalMatches);
	if(srchLimit == -1) { 
		resultsMap.put("topUserMatches", users);
		return resultsMap;
	}
	
	if(attributeName != null){
		resultsMap.put("topUserMatches", users);
		return resultsMap;
	}
	Users topUserMatches = new Users();
	int count = 0;
	for (User user : users.values()){
		count++;		
		if(count <= srchLimit){
			topUserMatches.add(user);
		}else{
			break;
		}
	}
	resultsMap.put("topUserMatches", topUserMatches);
	return resultsMap;
}


/**
 * Reads user profile from ldap.
 * @param context the current request context (contains the active user)
 * @param request HTTP request.
 * @return user the user whose profile was read
 * @throws IdentityException if a system error occurs preventing the action
 * @throws NamingException if an LDAP naming exception occurs
 * @throws SQLException if a database communication exception occurs
 * @throws CredentialsDeniedException 
 * @throws UnsupportedEncodingException 
 */
protected User readUserProfile(RequestContext context,HttpServletRequest request) 
		throws Exception {
	
	IdentityAdapter idAdapter = context.newIdentityAdapter();
	User user = new User();
	String[] parts = request.getRequestURI().toString().split("/");		
	String sEncoding = request.getCharacterEncoding();
    if ((sEncoding == null) || (sEncoding.trim().length() == 0)) {
       sEncoding = "UTF-8";
    }

	if(parts.length > 0) {
		String userIdentifier = Val.chkStr(URLDecoder.decode(parts[5].trim(),"UTF-8"));
		if(userIdentifier.endsWith(userDIT)){
			user.setDistinguishedName(userIdentifier);
			DistinguishedNameCredential dnCredential = new DistinguishedNameCredential();
			dnCredential.setDistinguishedName(userIdentifier);
			user.setCredentials(dnCredential);
		}else if(userIdentifier.length() > 0) {
			user.setCredentials(new UsernameCredential(userIdentifier));
		}
		((LdapIdentityAdapter)idAdapter).populateUser(context, user);
		return user;
	}else{		
		throw new Exception("error");	
	}
	
}

}
