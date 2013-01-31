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
import com.esri.gpt.control.ResourceKeys;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.sdisuite.IntegrationContext;
import com.esri.gpt.sdisuite.IntegrationContextFactory;

import javax.faces.context.ExternalContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.servlet.http.HttpSession;

/**
 * Handles login and logout actions.
 */
public class LoginController extends BaseActionListener {

// class variables =============================================================

// instance variables ==========================================================
private UsernamePasswordCredentials _credentials;
  
// constructors ================================================================

/** Default constructor. */
public LoginController() {
  setCredentials(new UsernamePasswordCredentials());
}

// properties ==================================================================

/**
 * Gets the credentials.
 * @return the credentials
 */
public UsernamePasswordCredentials getCredentials() {
  return _credentials;
}
/**
 * Sets the credentials.
 * @param credentials the credentials
 */
private void setCredentials(UsernamePasswordCredentials credentials) {
  _credentials = credentials;
}

// methods =====================================================================

/**
 * Invalidates the active user and session.
 * @param context the context associated with the active request
 */
private void invalidateSession(RequestContext context) {
  context.getUser().reset();
  HttpSession session = getContextBroker().extractHttpSession(); 
  if (session != null) {
    session.invalidate();
  }
}

/**
 * Fired when the getPrepareView() property is accessed.
 * <br/>This event is triggered from the page during the 
 * render response phase of the JSF cycle. 
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
@Override
protected void onPrepareView(RequestContext context) throws Exception {
  if (context.getUser().getAuthenticationStatus().getWasAuthenticated()) {
    ExternalContext ec = getContextBroker().getExternalContext();
    if (ec != null) {
      ec.redirect(Val.chkStr(ec.getRequestContextPath()+"/catalog/main/home.page"));
    }
  }
}

/**
 * Handles a logout action.
 * @param event the associated JSF action event
 * @throws AbortProcessingException if processing should be aborted
 */
public void processLogout(ActionEvent event) 
  throws AbortProcessingException {
  try {
    RequestContext context = onExecutionPhaseStarted();
    invalidateSession(context);
  } catch (AbortProcessingException e) {
    throw(e);
  } catch (Throwable t) {
    handleException(t);
  } finally {
    onExecutionPhaseCompleted();
  }
}
  

/**
 * Handles a login action.
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
  try {
    
    // set the user credentials
    User user = context.getUser();
    user.reset();
    UsernamePasswordCredentials creds = new UsernamePasswordCredentials();
    creds.setUsername(getCredentials().getUsername());
    creds.setPassword(getCredentials().getPassword());
    user.setCredentials(creds);
    
    // authenticate the user
    IdentityAdapter idAdapter = context.newIdentityAdapter();
    idAdapter.authenticate(user);
    
    // inform if sdi.suite integration is enabled
    IntegrationContextFactory icf = new IntegrationContextFactory();
    if (icf.isIntegrationEnabled()) {
      IntegrationContext ic = icf.newIntegrationContext();
      if (ic != null) {
        ic.ensureToken(user);
        ic.initializeUser(user);
      }
    }
    
    // set the outcome
    setNavigationOutcome(ResourceKeys.NAVIGATIONOUTCOME_HOME_DIRECT);
    String[] args = new String[1];
    args[0] = user.getName();
    extractMessageBroker().addSuccessMessage("identity.login.success",args);
  } catch (CredentialsDeniedException e) {
    extractMessageBroker().addErrorMessage("identity.login.err.denied");
  } 
}

}
