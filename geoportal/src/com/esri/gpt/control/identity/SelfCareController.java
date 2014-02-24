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
package com.esri.gpt.control.identity;
import java.util.logging.Level;

import com.esri.gpt.control.ResourceKeys;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.mail.FeedbackMessage;
import com.esri.gpt.framework.mail.MailRequest;
import com.esri.gpt.framework.security.codec.PC1_Encryptor;
import com.esri.gpt.framework.security.credentials.ChangePasswordCriteria;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.EmailPolicyException;
import com.esri.gpt.framework.security.credentials.PasswordConfirmationException;
import com.esri.gpt.framework.security.credentials.PasswordPolicyException;
import com.esri.gpt.framework.security.credentials.RecoverPasswordCriteria;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.credentials.UsernamePolicyException;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;
import com.esri.gpt.framework.security.identity.IdentitySupport;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.UserAttributeMap;
import com.esri.gpt.framework.util.Val;

import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpServletRequest;

/**
 * Handles actions associated with user self care.
 * <p>
 * The action executed is based upon a supplied "command" attribute 
 * associated with the UIComponent that triggers the processAction 
 * event. Command values:<br/>
 * <li>changePassword - executes a password change</li>
 * <li>recoverPassword - recovers a forgotten password</li>
 * <li>registerUser - registers a user</li>
 * <li>sendFeedback - sends a feedback message </li>
 * <li>updateProfile - executes a user profile update</li>
 */
public class SelfCareController extends BaseActionListener {

// class variables =============================================================

// instance variables ==========================================================
private UserAttributeMap        _activeUserAttributes = null;
private ChangePasswordCriteria  _changePasswordCriteria;
private FeedbackMessage         _feedbackMessage;
private HasAttributeMap         _hasAttributeMap = null;
private User                    _newUser;
private RecoverPasswordCriteria _recoverPasswordCriteria;
  
// constructors ================================================================

/** Default constructor. */
public SelfCareController() {}

// properties ==================================================================

/**
 * Gets the active user profile attributes.
 * <p>
 * This method is intended for use from a JSP page only.
 * @return the active user profile attributes
 */
public UserAttributeMap getActiveUserAttributes() {
  if (_activeUserAttributes == null) {
    RequestContext rc = extractRequestContext();
    User user = rc.getUser();
    if (user.getAuthenticationStatus().getWasAuthenticated()) {
      _activeUserAttributes = new UserAttributeMap(user.getProfile());
      
      // attempt to reload the user's profile
      IdentityAdapter idAdapter = rc.newIdentityAdapter();
      User userUpdate = new User();
      userUpdate.setKey(user.getKey());
      userUpdate.setLocalID(user.getLocalID());
      userUpdate.setDistinguishedName(user.getDistinguishedName());
      userUpdate.setProfile(_activeUserAttributes);
      try {
        idAdapter.readUserProfile(userUpdate);
      } catch (Throwable t) {
        getLogger().log(Level.SEVERE,"Error reading user profile.",t);
      }
    } else {
      _activeUserAttributes = getNewUser().getProfile();
    }
  }
  return _activeUserAttributes;
}

/**
 * Gets the active username.
 * <br/>This is for display only.
 * @return the active username
 */
public String getActiveUsername() {
  return extractRequestContext().getUser().getProfile().getUsername();
}
/**
 * Setter for the active username.
 * <br/>This is for display only, the set is ignored
 * @param ignored
 */
public void setActiveUsername(String ignored) {}

/**
 * Gets the change password criteria.
 * @return the change password criteria
 */
public ChangePasswordCriteria getChangePasswordCriteria() {
  if (_changePasswordCriteria == null) {
    _changePasswordCriteria = new ChangePasswordCriteria();
  }
  return _changePasswordCriteria;
}

/**
 * Gets the message associated with user feedback.
 * @return the feedback message
 */
public FeedbackMessage getFeedbackMessage() {
  if (_feedbackMessage == null) {
    RequestContext context = extractRequestContext();
    _feedbackMessage = new FeedbackMessage();
    //_feedbackMessage.setFromName(context.getUser().getName());
    _feedbackMessage.setFromAddress(context.getUser().getProfile().getEmailAddress());
  }
  return _feedbackMessage;
}

/**
 * Returns a Map interface of configured user attributes to aid in
 * determining if a user attribute should be rendered.
 * <br/Example:<br/>
 * rendered="#{SelfCareController.hasUserAttribute['firstName']}"
 * @return the configured attribute map interface
 */
public HasAttributeMap getHasUserAttribute() {
  if (_hasAttributeMap == null) {
    _hasAttributeMap = new HasAttributeMap(getActiveUserAttributes());
  }
  return _hasAttributeMap;
}

/**
 * Gets the identity configuration associated with the application.
 * @return the identity configuration
 */
private IdentityConfiguration getIdentityConfiguration() {
  RequestContext rc = extractRequestContext();
  return rc.getApplicationConfiguration().getIdentityConfiguration();
}

/**
 * Gets the user associated with new registration requests.
 * @return the user associated with new registration requests
 */
public User getNewUser() {
  if (_newUser == null) {
    IdentityConfiguration idConfig = getIdentityConfiguration();
    _newUser = new User();
    _newUser.setCredentials(new UsernamePasswordCredentials());
    _newUser.setProfile(new UserAttributeMap(idConfig.getUserAttributeMap()));
  }
  return _newUser;
}

/**
 * Gets the credentials associated with the new registration requests.
 * @return the credentials associated with new registration requests
 */
public UsernamePasswordCredentials getNewUserCredentials() {
  return (UsernamePasswordCredentials)getNewUser().getCredentials();
}

/**
 * Encrypts a password for configuration file storage.
 * @return the enscypted password
 */
public String getEncryptedPwd() {
  String sPwd = getChangePasswordCriteria().getNewCredentials().getPassword();
  String sEncrypted = PC1_Encryptor.encrypt(sPwd);
  //String sDecrypted = PC1_Encryptor.decrypt(sEncrypted);
  //return sPwd+" "+sEncrypted+" "+sDecrypted;
  return sEncrypted;
}
/** This method does nothing, JSF chokes if it doesn't exist. */
public void setEncryptedPwd(String ignore) {}

/**
 * Gets the recover password criteria.
 * @return the recover password criteria
 */
public RecoverPasswordCriteria getRecoverPasswordCriteria() {
  if (_recoverPasswordCriteria == null) {
    _recoverPasswordCriteria = new RecoverPasswordCriteria();
  }
  return _recoverPasswordCriteria;
}

// methods =====================================================================

/**
 * Executes a change password action.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
private void executeChangePassword(ActionEvent event, RequestContext context) 
  throws Exception {
  MessageBroker msgBroker = extractMessageBroker();
  try {
    
    // execute the password change, 
    // set success massage and navigation outcome
    IdentityAdapter idAdapter = context.newIdentityAdapter();
    idAdapter.changePassword(context.getUser(),getChangePasswordCriteria());
    msgBroker.addSuccessMessage("identity.changePassword.success");
    setNavigationOutcome(ResourceKeys.NAVIGATIONOUTCOME_HOME_DIRECT);
    
  } catch (CredentialsDeniedException e) {
    msgBroker.addErrorMessage("identity.changePassword.err.old");
  } catch (PasswordPolicyException e) {
    msgBroker.addErrorMessage("identity.changePassword.err.new");
  } catch (PasswordConfirmationException e) {
    msgBroker.addErrorMessage("identity.changePassword.err.confirm");
  }
}

/**
 * Executes a recover password action.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
private void executeRecoverPassword(ActionEvent event, RequestContext context) 
  throws Exception {
  MessageBroker msgBroker = extractMessageBroker();
  try {
    
    // initialize parameters, recover the password
    String sUsername = getRecoverPasswordCriteria().getUsername();
    String sEmail = getRecoverPasswordCriteria().getEmailAddress();
    IdentityAdapter idAdapter = context.newIdentityAdapter();
    User user = idAdapter.recoverPassword(getRecoverPasswordCriteria());
    if (user != null) {
      
      // get the new password
      UsernamePasswordCredentials upCred;
      upCred = user.getCredentials().getUsernamePasswordCredentials();
      String sPassword = upCred.getPassword();
      
      // send mail with the new password
      String[] args = new String[2];
      args[0] = sUsername;
      args[1] = sPassword;
      String sSubject = msgBroker.retrieveMessage("identity.forgotPassword.email.subject");
      String sBody = msgBroker.retrieveMessage("identity.forgotPassword.email.body",args);
      ApplicationConfiguration appConfig = context.getApplicationConfiguration();
      MailRequest mailReq = appConfig.getMailConfiguration().newOutboundRequest();
      mailReq.setToAddress(sEmail);
      mailReq.setSubject(sSubject);
      mailReq.setBody(sBody);
      mailReq.send();
      
      // add the success message, set the navigation outcome
      msgBroker.addSuccessMessage("identity.forgotPassword.success");
      setNavigationOutcome(ResourceKeys.NAVIGATIONOUTCOME_HOME_DIRECT);
    } else {
      
      // add the error message
      msgBroker.addErrorMessage("identity.forgotPassword.err.denied");
    }
  } finally {
  }
}

/**
 * Executes a user registration action.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
private void executeRegisterUser(ActionEvent event, RequestContext context) 
  throws Exception {
  MessageBroker msgBroker = extractMessageBroker();
  try {
    
    // register the user,
    // add the success message, set navigation outcome to the home page 
    IdentityAdapter idAdapter = context.newIdentityAdapter();
    idAdapter.registerUser(getNewUser());
    msgBroker.addSuccessMessage("catalog.identity.userRegistration.success");
    setNavigationOutcome(ResourceKeys.NAVIGATIONOUTCOME_HOME_DIRECT);
   
    // attempt to login if not single sign-on mode
    boolean bSingleSignOn = false;
    if (!bSingleSignOn) {
      
      // authenticate the user, add the successful login message
      User user = extractRequestContext().getUser();
      user.reset();
      user.setCredentials(getNewUser().getCredentials());
      idAdapter.authenticate(user);
      String[] args = new String[1];
      args[0] = user.getName();
      extractMessageBroker().addSuccessMessage("identity.login.success",args);
    } else {

      // navigate to login page if single sign-on mode
      setNavigationOutcome("catalog.identity.login");
    }
    
  } catch (UsernamePolicyException e) {
    msgBroker.addErrorMessage("identity.profile.err.username");
  } catch (PasswordPolicyException e) {
    msgBroker.addErrorMessage("identity.profile.err.password");
  } catch (PasswordConfirmationException e) {
    msgBroker.addErrorMessage("identity.profile.err.confirm");
  } catch (EmailPolicyException e) {
    msgBroker.addErrorMessage("identity.profile.err.email");
  } catch (javax.naming.NameAlreadyBoundException e) {
    msgBroker.addErrorMessage("identity.profile.err.userExists");
  }
}

/**
 * Executes the sending of a user feedback message.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
private void executeSendFeedback(ActionEvent event, RequestContext context) 
  throws Exception {
  MessageBroker msgBroker = extractMessageBroker();
  ApplicationConfiguration appConfig = context.getApplicationConfiguration();
  FeedbackMessage msg = getFeedbackMessage();
  
  // validate parameters
  boolean bOk = true;
  String sName = msg.getFromName();
  String sEmail = msg.getFromAddress();
  String sBody = msg.getBody();
  String sSender = sEmail;
  if (!Val.chkEmail(sEmail)) {
    bOk = false;
    msgBroker.addErrorMessage("identity.feedback.err.email");
  } else if (sBody.length() == 0) {
    bOk = false;
    msgBroker.addErrorMessage("identity.feedback.err.body");
  } else if (sName.length() > 0) {
    sSender = sName;
  }
  
  // send mail if ok
  if (bOk) {
    
    // try to filter out mischievous content
    sSender = sSender.replaceAll("<", "&lt;");
    sBody = sBody.replaceAll("<", "&lt;");
    
    // build the message subject and body
    String[] args = new String[3];
    args[0] = sSender;
    args[1] = sBody;
    args[2] = RequestContext.resolveBaseContextPath((HttpServletRequest) context.getServletRequest());
    String sSubject = msgBroker.retrieveMessage("identity.feedback.email.subject");
    sBody = msgBroker.retrieveMessage("identity.feedback.email.body",args);
    
    // send the message to the site
    MailRequest mailReq = appConfig.getMailConfiguration().newInboundRequest();
    mailReq.setFromAddress(sEmail);
    mailReq.setSubject(sSubject);
    mailReq.setBody(sBody);
    mailReq.send();
    
    // send a copy of the message to the user
    MailRequest mailReqCopy = appConfig.getMailConfiguration().newOutboundRequest();
    mailReqCopy.setToAddress(sEmail);
    mailReqCopy.setSubject(sSubject);
    mailReqCopy.setBody(sBody);
    mailReqCopy.send();      
    
    // add the success message, set the navigation outcome
    msgBroker.addSuccessMessage("identity.feedback.success");
    setNavigationOutcome(ResourceKeys.NAVIGATIONOUTCOME_HOME_DIRECT);
  }
}

/**
 * Executes a user profile update action.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
private void executeUpdateProfile(ActionEvent event, RequestContext context) 
  throws Exception {
  MessageBroker msgBroker = extractMessageBroker();
  try {
    
    // make a temp user to process the update in case of failure
    User user = extractRequestContext().getUser();
    User userUpdate = new User();
    userUpdate.setKey(user.getKey());
    userUpdate.setLocalID(user.getLocalID());
    userUpdate.setDistinguishedName(user.getDistinguishedName());
    userUpdate.setProfile(getActiveUserAttributes());
    
    // execute the update, 
    // update the in memory profile for the active user
    // set the success message and navigation outcome
    IdentityAdapter idAdapter = context.newIdentityAdapter();
    idAdapter.updateUserProfile(userUpdate);
    user.setProfile(userUpdate.getProfile());
    msgBroker.addSuccessMessage("catalog.identity.myProfile.success");
    setNavigationOutcome(ResourceKeys.NAVIGATIONOUTCOME_HOME_DIRECT);
    
  } catch (EmailPolicyException e) {
    msgBroker.addErrorMessage("identity.profile.err.email");
  } catch (javax.naming.NameAlreadyBoundException e) {
    msgBroker.addErrorMessage("identity.profile.err.userExists");
  }
}

/**
 * Handles a user self care action.
 * <p>
 * The action executed is based upon a supplied "command" attribute 
 * associated with the UIComponent that triggered the processAction 
 * event. Command values:<br/>
 * changePassword, recoverPassword, registerUser, sendFeedback, updateProfile
 * <p>
 * <br/>This is the default entry point for a sub-class of BaseActionListener.
 * <br/>This BaseActionListener handles the JSF processAction method and
 * invokes the processSubAction method of the sub-class.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws AbortProcessingException if processing should be aborted
 * @throws Exception if an exception occurs
 */
@Override
protected void processSubAction(ActionEvent event, RequestContext context) 
  throws AbortProcessingException, Exception  {
  
  // determine the command
  IdentitySupport support = context.getIdentityConfiguration().getSupportedFunctions();
  UIComponent component = event.getComponent();
  String sCommand = Val.chkStr((String)component.getAttributes().get("command"));
  
  // execute the command
  if (sCommand.equals("changePassword")) {
    if (!support.getSupportsPasswordChange()) throw new NotAuthorizedException("Not authorized.");
    assertLoggedIn(context);
    executeChangePassword(event,context);
  } else if (sCommand.equals("recoverPassword")) {
    if (!support.getSupportsPasswordRecovery()) throw new NotAuthorizedException("Not authorized.");  
    executeRecoverPassword(event,context);
  } else if (sCommand.equals("registerUser")) {
    if (!support.getSupportsUserRegistration()) throw new NotAuthorizedException("Not authorized.");  
    executeRegisterUser(event,context);
  } else if (sCommand.equals("sendFeedback")) {
    executeSendFeedback(event,context);
  } else if (sCommand.equals("updateProfile")) {
    if (!support.getSupportsUserProfileManagement()) throw new NotAuthorizedException("Not authorized.");  
    assertLoggedIn(context);
    executeUpdateProfile(event,context);
  } 
}

}
