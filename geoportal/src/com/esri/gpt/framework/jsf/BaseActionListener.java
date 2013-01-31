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
package com.esri.gpt.framework.jsf;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClient401Exception;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ActionListener;
import javax.servlet.http.HttpServletResponse;

/**
 * Super-class for a JSF based action listener.
 */
public class BaseActionListener implements ActionListener {
  
//class variables =============================================================

// instance variables ==========================================================
private FacesContextBroker _contextBroker = new FacesContextBroker();
private String             _key = "";
private String             _navigationOutcome = "";
private String             _securityRoles = "";
private RoleSet            _securityRoleSet = new RoleSet();
  
// constructors ================================================================

/** Default constructor. */
public BaseActionListener() {}

// properties ==================================================================

/**
 * Gets the Faces context broker.
 * @return the Faces context broker
 */
protected FacesContextBroker getContextBroker() {
  return _contextBroker;
}

/**
 * Gets the key associated with the action listener.
 * @return the key
 */
public String getKey() {
  return _key;
}
/**
 * Sets the key associated with the action listener.
 * @param key the key
 */
public void setKey(String key) {
  _key = Val.chkStr(key);
}

/**
 * Gets the logger.
 * @return the logger
 */
public Logger getLogger() {
  return LogUtil.getLogger();
}

/**
 * Gets the navigation outcome.
 * @return the navigation outcome
 */
public String getNavigationOutcome() {
  return _navigationOutcome;
}
/**
 * Sets the navigation outcome.
 * @param outcome the navigation outcome
 */
protected void setNavigationOutcome(String outcome) {
  _navigationOutcome = Val.chkStr(outcome);
}
/**
 * Provides an interface to prepare the view for a page.
 * <p>
 * Some pages may require preparation during the render response phase
 * of the JSF cycle. In such cases, the page should include a hidden
 * variable to trigger view preparation. Example: 
 * &lth:inputHidden value="#{SomeController.prepareView}"/&gt;
 * <p>
 * Once triggered, the onPrepareView() method for the controller 
 * will be invoked. Override the onPrepareView() method to provide
 * controller specific behavior.
 * @return always an empty string
 */
public String getPrepareView() {
  try {
    RequestContext context = onPrepareViewStarted();
    authorizeAction(context);
    onPrepareView(context);
  } catch (NotAuthorizedException e) {
    try {
      ExternalContext ec = getContextBroker().getExternalContext();
      ec.redirect(Val.chkStr(ec.getRequestContextPath())+"/catalog/main/home.page");
    } catch (Throwable t) {
      getLogger().log(Level.SEVERE,"Exception raised.",t);
    }
  } catch (Throwable t) {
    getLogger().log(Level.SEVERE,"Exception raised.",t);
  } finally {
    onPrepareViewCompleted();
  }
  return "";
}
/**
 * Provides the setter for the prepareView property.
 * @param ignored always ignored
 */
public void setPrepareView(String ignored) {}

/**
 * Gets the security roles string associated with the action listener.
 * @return the security roles string
 */
public String getSecurityRoles() {
  return _securityRoles;
}
/**
 * Gets the security roles string associated with the action listener.
 * @param roles the security roles string
 */
public void setSecurityRoles(String roles) {
  _securityRoles = Val.chkStr(roles);
  _securityRoleSet.clear();
  _securityRoleSet.addDelimited(getSecurityRoles());
}

/**
 * Gets the security role set associated with the action listener.
 * @return the security role set
 */
protected RoleSet getSecurityRoleSet() {
  return _securityRoleSet;
}

// methods =====================================================================

/**
 * Asserts that the active user is logged in.
 * @param context the request context associated with this execution thread
 * @throws NotAuthorizedException if the user does not have a required role
 */
protected void assertLoggedIn(RequestContext context) 
  throws NotAuthorizedException {
  context.getUser().getAuthenticationStatus().assertLoggedIn();
}

/**
 * Authorizes an action.
 * <br/>Authorization is based upon the security roles for this 
 * action listener and the authenticated roles for the user associated
 * with the supplied request context.
 * <br/>If the security roles for this action are empty, the action is authorized.
 * @param context the request context associated with this execution thread
 * @throws NotAuthorizedException if the user does not have a required role
 */
protected void authorizeAction(RequestContext context) 
  throws NotAuthorizedException {
  context.getUser().getAuthenticationStatus().authorizeAction(getSecurityRoleSet());
}

/**
 * Extract the MessageBroker from the Faces context instance.
 * @return the MessageBroker
 */
protected MessageBroker extractMessageBroker() {
  return getContextBroker().extractMessageBroker();
}

/**
 * Extract the request context from the Faces context instance.
 * @return the request context
 */
public RequestContext extractRequestContext() {
  return getContextBroker().extractRequestContext();
}

/**
 * Handles an exception.
 * @param t the exception
 */
protected void handleException(Throwable t) {
  if (t instanceof NotAuthorizedException) {
    setNavigationOutcome("homeDirect");
    extractMessageBroker().addErrorMessage(t);
  } else {
    extractMessageBroker().addErrorMessage(t);
    getLogger().log(Level.SEVERE,"Exception raised.",t);
  }
}

/**
 * Fired when the execution phase has completed.
 */
protected void onExecutionPhaseCompleted() {
  RequestContext rc = extractRequestContext();
  rc.onExecutionPhaseCompleted();
}

/**
 * Fired when the execution phase has started.
 */
protected RequestContext onExecutionPhaseStarted() {
  return extractRequestContext();
}

/**
 * Fired when the getPrepareView() property is accessed.
 * <br/>This event is triggered from the page during the 
 * render response phase of the JSF cycle. 
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
protected void onPrepareView(RequestContext context) throws Exception {
  // no default implementation
}

/**
 * Fired when the onPrepareView() event has completed.
 */
protected void onPrepareViewCompleted() {
  RequestContext rc = extractRequestContext();
  rc.onPrepareViewCompleted();
}

/**
 * Fired when the onPrepareView()event has started.
 */
protected RequestContext onPrepareViewStarted() {
  return extractRequestContext();
}
  
/**
 * Processes the JSF based action.
 * <br/>This is the default entry point for a JSF ActionListener.
 * <br/>The default behavior is: flag the start of the execution phase,
 * authorize the action, invoke the processPreparedAction() method,
 * handle exceptions, flag the completion of the execution phase.
 * @param event the associated JSF action event
 * @throws AbortProcessingException if processing should be aborted
 */
public void processAction(ActionEvent event) 
   throws AbortProcessingException {
  boolean autoAuthenticate = true;
  try {
    RequestContext context = onExecutionPhaseStarted();
    StringAttributeMap params = context.getCatalogConfiguration().getParameters();
    autoAuthenticate = !Val.chkStr(params.getValue("BaseServlet.autoAuthenticate")).equalsIgnoreCase("false");
    authorizeAction(context);
    if (autoAuthenticate) {
      CredentialProvider.establishThreadLocalInstance(this.getContextBroker().extractHttpServletRequest());
    }
    processSubAction(event,context);
  } catch (AbortProcessingException e) {
    throw(e);
  } catch (HttpClient401Exception remoteAuthException) {
    if (autoAuthenticate) {
      String realm = Val.chkStr(remoteAuthException.getRealm());
      HttpServletResponse httpResponse = this.getContextBroker().extractHttpServletResponse();
      httpResponse.setHeader("WWW-Authenticate","Basic realm=\""+realm+"\"");
      try {
        httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED);
      } catch (IOException ioe) {
        throw new AbortProcessingException("Cannot send 401 to client.",ioe);
      } finally{
        this.getContextBroker().getFacesContext().responseComplete();
      }
    } else {
      handleException(remoteAuthException); 
    }
  } catch (Throwable t) {
    handleException(t);
  } finally {
    onExecutionPhaseCompleted();
  }
}

/**
 * This is the default entry point for a sub-class of this ActionListener.
 * <br/>This BaseActionListener handles the JSF processAction method and
 * invokes the processSubAction method of the sub-class.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws AbortProcessingException if processing should be aborted
 * @throws Exception if an exception occurs
 */
protected void processSubAction(ActionEvent event, RequestContext context) 
  throws AbortProcessingException, Exception {
  // no default behavior
}
    
}
