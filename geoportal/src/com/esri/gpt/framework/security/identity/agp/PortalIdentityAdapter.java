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
package com.esri.gpt.framework.security.identity.agp;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.http.StringProvider;
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
import com.esri.gpt.framework.security.identity.local.LocalDao;
import com.esri.gpt.framework.security.principal.Role;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.security.principal.Roles;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.Users;

import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Identity adapter when using ArcGIS Online or Portal as the identity store.
 * <p>gpt.xml configuration:</p>
 * <p>element: /gptConfig/identity/arcgisPortalAdapter</p>
 * <p>attribute appId: application id (Geoportal app registered at ArcGIS Online or Portal)</p> 
 * <p>attribute authorizeUrl: OAuth2 authorize url (e.g. https://www.arcgis.com/sharing/oauth2/authorize)</p> 
 * <p>attribute expirationMinutes: token expiration minutes</p> 
 * <p>attribute gptAdministratorsGroupId: group id for Geoportal administrators (optional)</p>
 * <p>attribute gptPublishersGroupId: group id for Geoportal publishers (optional)</p>
 * <p>attribute allUsersCanPublish: true or false, if true all authenticated users can publish items to Geoportal</p>
 * <p></p>
 * <p>Self-care: registration, password change, etc will not be provided by Geoportal</p>
 * <p>The metadataAccessPolicy should be: &lt;metadataAccessPolicy type="unrestricted"/&gt;</p>
 * <p>Harvesting related e-mails will not be sent.</p>
 */
public class PortalIdentityAdapter extends IdentityAdapter {
  
	// class variables =============================================================
	public static String AppId;
	public static String AuthorizeUrl;	
	public static int ExpirationMinutes = 120;
	public static String GptPublishersGroupId;	
	public static String GptAdministratorsGroupId;
	public static boolean AllUsersCanPublish = false;
	
	 private static Logger LOGGER = Logger.getLogger(PortalIdentityAdapter.class.getName());
	
	/** Default constructor. */
	public PortalIdentityAdapter() {
	  super();
	}
	
	/**
	 * Gets the application id (the app id registered at ArcGIS Online or Portal for ArcGIS)
	 * @return the app id
	 */
	public String getAppId() {
		return AppId;
	}
	
	/**
	 * Gets the OAuth2 authorize url.
	 * @return the authorize url
	 */
	public String getAuthorizeUrl() {
		return AuthorizeUrl;
	}
	
	/**
	 * Gets the token expiration minutes.
	 * @return the expiration minutes
	 */
	public int getExpirationMinutes() {
		return ExpirationMinutes;
	}
	
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
		throwNotSupportedException("addUserToRole");
	}
	
	/**
	 * Authenticates a user.
	 * @param user the subject user
	 * @throws CredentialsDeniedException if credentials are denied
	 * @throws IdentityException if a system error occurs preventing authentication
	 * @throws SQLException if a database communication exception occurs
	 */
	@Override
	public void authenticate(User user) throws CredentialsDeniedException, IdentityException, SQLException {
	  user.getAuthenticationStatus().reset();
	  Credentials credentials = user.getCredentials();
	  if (credentials != null) {
	  	
	  	if (credentials instanceof UsernamePasswordCredentials) {
	  		UsernamePasswordCredentials upCreds = (UsernamePasswordCredentials)credentials;
	  		String username = upCreds.getUsername();
	  		String password = upCreds.getPassword();
	  		try {
	  			String referer = getThisReferer();
	  			String token = executeGetToken(username,password,referer);
	  			executeGetUser(user,token,username,referer);
				} catch (Exception e) {
			    user.getAuthenticationStatus().reset();
			    e.printStackTrace();
			    LOGGER.log(Level.SEVERE,"Error authenticating user",e);
			    throw new IdentityException(e.getMessage(),e);
				}
	  	
	    } else if (credentials instanceof DistinguishedNameCredential) {
	    	DistinguishedNameCredential dnCred = (DistinguishedNameCredential)credentials;
	    	String dn = dnCred.getDistinguishedName();
	    	LocalDao localDao = new LocalDao(getRequestContext());
	    	int userId = localDao.readUserIdByDN(dn);
	    	if (userId != -1) {
	    		String username = localDao.readUsername(userId);
	        user.setDistinguishedName(dn);
	        user.setKey(user.getDistinguishedName());
	        user.setName(username);
	        user.getProfile().setUsername(username);
	    		user.getAuthenticationStatus().setWasAuthenticated(true);
	        RoleSet authRoles = user.getAuthenticationStatus().getAuthenticatedRoles();
	        Roles cfgRoles = getApplicationConfiguration().getIdentityConfiguration().getConfiguredRoles();
	        for (Role role: cfgRoles.values()) {
	        	if (role.getKey().equals("gptAdministrator")) {
	        	} else if (role.getKey().equals("gptPublisher")) {
	        		authRoles.addAll(role.getFullRoleSet());
	        	} else if (role.getKey().equals("gptRegisteredUser")) {	
	        		authRoles.addAll(role.getFullRoleSet());
	        	}
	        }
	    	}
	    	
	    } else if (credentials instanceof UsernameCredential) {
		  }
	  }
	  
	  if (!user.getAuthenticationStatus().getWasAuthenticated()) {
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
		if ((groupDN != null) && groupDN.toLowerCase().equals("gptpublisher")) {
	    LocalDao localDao = new LocalDao(getRequestContext());
	    return localDao.readAllUsers();
		} else {
			return new Users();
		}
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
	  // Not implemented.
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
		// Not implemented.
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
	
	/**
	 * Validates the OAuth2 response token and establishes a server side session.
	 * @param token the token
	 * @param username the username
	 */
	public void validateOAuthResponseToken(String token, String username) {
    User user = this.getRequestContext().getUser();
    user.reset();
		try {
			String referer = null;
			//Decode username from response, not-required change was made to oauthResponse.jsp
		  //username = URLDecoder.decode(username,"UTF-8");
			executeGetUser(user,token,username,referer);
		} catch (Throwable t) {
			t.printStackTrace();
			LOGGER.log(Level.SEVERE,"Error validating OAuth response",t);
		}
	}
	
	private String executeGetToken(String username, String password, String referer) throws Exception {
		String token = null;
    String restBaseUrl = this.getRestBaseUrl();
    String url = restBaseUrl+"generateToken";
    //System.err.println("generateTokenUrl="+url);
    StringBuilder content = new StringBuilder();
    content.append("f=json");
    content.append("&username=").append(URLEncoder.encode(username,"UTF-8"));
    content.append("&password=").append(URLEncoder.encode(password,"UTF-8"));
    content.append("&expiration=").append(URLEncoder.encode(""+getExpirationMinutes(),"UTF-8"));
    content.append("&referer=").append(URLEncoder.encode(referer,"UTF-8"));
    content.append("&client=").append(URLEncoder.encode("referer","UTF-8"));
    StringProvider provider = new StringProvider(content.toString(),"application/x-www-form-urlencoded");
		StringHandler handler = new StringHandler();
		HttpClientRequest http = new HttpClientRequest();
		http.setRetries(-1);
		http.setUrl(url);
		http.setContentProvider(provider);
		http.setContentHandler(handler);
		http.execute();
		
    String sResponse = handler.getContent();
    //System.err.println(sResponse);
    JSONObject jsoResponse = new JSONObject(sResponse);
    if (jsoResponse.has("error") && (!jsoResponse.isNull("error"))) {
    	throw new Exception(sResponse);
    }
    if (jsoResponse.has("token") && (!jsoResponse.isNull("token"))) {
    	token = jsoResponse.getString("token");
    } else {
    	throw new Exception(sResponse);
    }
    return token;
	}
	
	private void executeGetUser(User user, String token, String username, String referer) throws Exception {
		String adminGroupId = GptAdministratorsGroupId;
		String pubGroupId = GptPublishersGroupId;
		boolean allUsersCanPublish = AllUsersCanPublish;
		boolean isInAdminGroup = false;
		boolean isInPubGroup = false;
		boolean hasOrgAdminRole = false;
		boolean hasOrgPubRole = false;
		boolean hasOrgUserRole = false;
		
    String restBaseUrl = this.getRestBaseUrl();
    //String url = restBaseUrl+"community/users/"+URLEncoder.encode(username,"UTF-8");
    String url = restBaseUrl+"community/self/";
    url += "?f=json&token="+URLEncoder.encode(token,"UTF-8");
    
		//System.err.println("userUrl="+url);
		StringHandler handler = new StringHandler();
		HttpClientRequest http = new HttpClientRequest();
		http.setRetries(-1);
		http.setUrl(url);
		http.setContentHandler(handler);
		if (referer != null) {
			//System.err.println("Referer="+referer);
			http.setRequestHeader("Referer",referer);
		};
		http.execute();
		//System.err.println(handler.getContent());
    
    String sResponse = handler.getContent();
    //System.err.println("username="+username);
    //System.err.println("response="+sResponse);
    JSONObject jsoResponse = new JSONObject(sResponse);
    if (jsoResponse.has("error") && (!jsoResponse.isNull("error"))) {
    	LOGGER.warning(sResponse);
    	return;
    }
    if (!jsoResponse.has("username") || (jsoResponse.isNull("username"))) {
    	LOGGER.warning("No username. "+sResponse);
    	return;
    } else {
    	username = jsoResponse.getString("username");
    }
    
    user.setDistinguishedName(username);
    user.setKey(user.getDistinguishedName());
    user.setName(username);
    user.getProfile().setUsername(username);
    user.getAuthenticationStatus().setWasAuthenticated(true);

  	if (jsoResponse.has("role") && (!jsoResponse.isNull("role"))) {
  	  // "role": "org_admin"  "org_publisher" or "org_user"
  		String role = jsoResponse.getString("role");
      if (role.equals("org_admin") || role.equals("account_admin")) hasOrgAdminRole = true;
      if (role.equals("org_publisher") || role.equals("account_publisher")) hasOrgPubRole = true;
      if (role.equals("org_user") || role.equals("account_user")) hasOrgUserRole = true;
  	}
  	if (jsoResponse.has("email") && (!jsoResponse.isNull("email"))) {
  		user.getProfile().setEmailAddress(jsoResponse.getString("email"));
  	}
  	if (jsoResponse.has("groups") && (!jsoResponse.isNull("groups"))) {
  		JSONArray jsoGroups = jsoResponse.getJSONArray("groups");
      for (int i=0;i<jsoGroups.length();i++) {
      	JSONObject jsoGroup = jsoGroups.getJSONObject(i);
        String groupId = jsoGroup.getString("id");
        if ((adminGroupId != null) && (adminGroupId.length() > 0) && adminGroupId.equals(groupId)) {
        	isInAdminGroup = true;
        }
        if ((pubGroupId != null) && (pubGroupId.length() > 0) && pubGroupId.equals(groupId)) {
        	isInPubGroup = true;
        }
        //System.err.println("groupId="+groupId); 
      }
  	}
    	
    boolean isAdmin = false;
    boolean isPublisher = false;
    if ((adminGroupId != null) && (adminGroupId.length() > 0)) {
    	if (isInAdminGroup) isAdmin = true;
    	if (hasOrgAdminRole) isAdmin = true;
    } else {
    	if (hasOrgAdminRole) isAdmin = true;
    }
  	if (allUsersCanPublish) {
  	  if (hasOrgAdminRole || hasOrgPubRole || hasOrgUserRole) isPublisher = true;
  	}
    if ((pubGroupId != null) && (pubGroupId.length() > 0)) {
    	if (isInPubGroup) isPublisher = true;
    } else {
    	if (hasOrgPubRole) isPublisher = true;
    }
    
    RoleSet authRoles = user.getAuthenticationStatus().getAuthenticatedRoles();
    Roles cfgRoles = getApplicationConfiguration().getIdentityConfiguration().getConfiguredRoles();
    for (Role role: cfgRoles.values()) {
    	if (role.getKey().equals("gptAdministrator")) {
    		if (isAdmin) authRoles.addAll(role.getFullRoleSet());
    	} else if (role.getKey().equals("gptPublisher")) {
    		if (isPublisher) authRoles.addAll(role.getFullRoleSet());
    	} else if (role.getKey().equals("gptRegisteredUser")) {	
    		authRoles.addAll(role.getFullRoleSet());
    	}
    }
    
    LocalDao localDao = new LocalDao(getRequestContext());
    localDao.ensureReferenceToRemoteUser(user);
	}
	
	private String getRestBaseUrl() {
    String authorizeUrl = this.getAuthorizeUrl();
    //String restBaseUrl = authorizeUrl.substring(0,authorizeUrl.indexOf("/oauth2/"))+"/rest/";
    if (authorizeUrl.indexOf("/sharing/oauth2/") > 0) {
      return authorizeUrl.substring(0,authorizeUrl.indexOf("/sharing/oauth2/"))+"/sharing/rest/";
    }
    return authorizeUrl.substring(0,authorizeUrl.indexOf("/sharing/rest/oauth2/"))+"/sharing/rest/";
	}
	
	private String getThisReferer() {
    try {
      return InetAddress.getLocalHost().getCanonicalHostName();
    } catch (UnknownHostException ex) {
      return "";
    }
  }

}
