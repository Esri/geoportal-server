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
package com.esri.gpt.framework.context;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.mail.MailConfiguration;
import com.esri.gpt.framework.security.credentials.UsernameCredential;
import com.esri.gpt.framework.security.identity.IdentityAdapter;
import com.esri.gpt.framework.security.identity.IdentityConfiguration;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.sql.ConnectionBroker;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Context for a request.
 * <p>
 * This class is intended for use by a single execution thread.
 */
public class RequestContext {

// class variables =============================================================

/** The key used to store the instance within the ServletRequest. */
public static final String REFERENCEKEY = "com.esri.gpt.framework.context.RequestContext";
/** user key */
private static final String USERKEY = "com.esri.gpt.user";

// instance variables ==========================================================
private ApplicationContext _applicationContext;
private ConnectionBroker   _connectionBroker;
private Map<String,Object> _objectMap = new HashMap<String,Object>();
private ServletRequest     _servletRequest;
private Timestamp          _timeCompleted;
private Timestamp          _timeStarted;
private User               _user;
private boolean            _viewerExecutedJavascript;

// constructors ================================================================


/** Default constructor. */
protected RequestContext() {
  this(null);
}

/**
 * Constructs with an associated servlet request.
 * @param request the current servlet request
 */
protected RequestContext(ServletRequest request) {
  _servletRequest = request;
  _timeStarted = new Timestamp(System.currentTimeMillis());
  _timeCompleted = null;
  setApplicationContext(ApplicationContext.getInstance());
  ApplicationConfiguration config = getApplicationContext().getConfiguration();
  setConnectionBroker(new ConnectionBroker(config.getDatabaseReferences()));
  setUser(new User());
  boolean bWasUserInSession = false;
  
  if (request instanceof HttpServletRequest) {
    HttpServletRequest httpReq = (HttpServletRequest)request;
    HttpSession sn = httpReq.getSession(true);
    User user = (User)sn.getAttribute(USERKEY);
    if (user == null) {
      sn.setAttribute(USERKEY,getUser());
    } else {
      bWasUserInSession = true;
      setUser(user);
    }
    user = getUser();
   
    // check for single sign-on
    if (getIdentityConfiguration().getSingleSignOnMechanism().getActive()) {
      if (Val.chkStr(user.getKey()).startsWith("urn:openid:")) return;
      
      String sPrevious = user.getName();
      String sUsername = getIdentityConfiguration().getSingleSignOnMechanism().determineUsername(httpReq);
      if (!sPrevious.equals(sUsername)) {
        if (bWasUserInSession) {
          setUser(new User());
          sn.setAttribute(USERKEY,getUser());
          user = getUser();
        }
        if (sUsername.length() > 0) {
          user.setCredentials(new UsernameCredential(sUsername));
          try {
            newIdentityAdapter().authenticate(user);
          } catch (Exception e) {
            String sMsg = "An error occured while evaluating single sign-on credentials for: "+sUsername;
            setUser(new User());
            sn.setAttribute(USERKEY,getUser());
            getLogger().log(Level.SEVERE,sMsg,e);
          } finally {
            this.getConnectionBroker().closeAll();
          }
        }
      }
    }
  }
  
 }

// properties ==================================================================

/**
 * Gets the application configuration.
 * @return the application configuration
 */
public ApplicationConfiguration getApplicationConfiguration() {
  return getApplicationContext().getConfiguration();
}

/**
 * Gets the context for this application.
 * @return the ApplicationContext
 */
public ApplicationContext getApplicationContext() {
  return _applicationContext;
}
/**
 * Sets the context for this application.
 * @param context the ApplicationContext
 */
private void setApplicationContext(ApplicationContext context) {
  _applicationContext = context;
  if (_applicationContext == null) {
    _applicationContext = ApplicationContext.getInstance();
  }
}

/**
 * Gets the metadata catalog configuration associated with this application.
 * @return the catalog configuration
 */
public CatalogConfiguration getCatalogConfiguration() {
  return getApplicationConfiguration().getCatalogConfiguration();
}

/**
 * Gets the broker for database access.
 * @return the database connection broker
 */
public ConnectionBroker getConnectionBroker() {
  return _connectionBroker;
}
/**
 * Sets the broker for database access.
 * @param broker the database connection broker
 */
private void setConnectionBroker(ConnectionBroker broker) {
  _connectionBroker = broker;
}

/**
 * Gets the identity configuration associated with this application.
 * @return the identity configuration
 */
public IdentityConfiguration getIdentityConfiguration() {
  return getApplicationConfiguration().getIdentityConfiguration();
}

/**
 * Gets the logger.
 * @return the logger
 */
public Logger getLogger() {
  return LogUtil.getLogger();
}

/**
 * Gets the mail configuration associated with this application.
 * @return the mail configuration
 */
public MailConfiguration getMailConfiguration() {
  return getApplicationConfiguration().getMailConfiguration();
}

/**
 * Gets underlying servlet request.
 * <br/>This is only available if it was passed on the coonstructor.
 * @return the underlying servlet request (can be null)
 */
public ServletRequest getServletRequest() {
  return _servletRequest;
}

/**
 * Gets the free form object map associated with this request.
 * <br/>This map can be used in a manner similar to the attributes
 * of a ServletRequest.
 * @return the free form object map
 */
public Map<String,Object> getObjectMap() {
  return _objectMap;
}

/**
 * Gets the user associated with this request.
 * @return the user
 */
public User getUser() {
  return _user;
}
/**
 * Sets the user associated with this request.
 * @param user the user
 */
protected void setUser(User user) {
  _user = user;
}


/**
 * Gets the viewer executes javascript.
 * 
 * @return the viewer executed javascript
 */
public boolean getViewerExecutesJavascript() {
  return _viewerExecutedJavascript;
}

/**
 * Sets the viewer executes javascript.
 * 
 * @param viewerExecutedJavascript the new viewer executed javascript
 */
public void setViewerExecutesJavascript(boolean viewerExecutedJavascript) {
  this._viewerExecutedJavascript = viewerExecutedJavascript;
}

// methods =====================================================================

/**
 * Extracts the request context from the current servlet request.
 * @param request the current servlet request
 * @return the request context
 */
public static RequestContext extract(ServletRequest request) {
  String sKey = REFERENCEKEY;
  RequestContext requestContext = null;
  if (request == null) {
    requestContext = new RequestContext();
  } else {
    requestContext = (RequestContext)request.getAttribute(sKey);
    if (requestContext == null) {
      requestContext = new RequestContext(request);
      request.setAttribute(sKey,requestContext);
    }
  }
  return requestContext;
}

/**
 * Resolves the base context path associated with an HTTP request.
 * <br/>(<i>http://&lt;host:port&gt;/&lt;Context&gt;</i>)
 * <p/>
 * If a reverse proxy base URL hase been configured, it will be returned:
 * <br/>
 * /gptConfig/catalog/parameter@key="reverseProxy.baseContextPath"
 * @return the base context path
 */
public static String resolveBaseContextPath(HttpServletRequest request) {
  if (request == null) return "";
  StringAttributeMap params = null;
  RequestContext ctx = (RequestContext)request.getAttribute(REFERENCEKEY);
  if (ctx != null) {
    params = ctx.getCatalogConfiguration().getParameters();  
  } else {
    params = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration().getParameters();
  }
  String basePath = Val.chkStr(params.getValue("reverseProxy.baseContextPath"));
  if (basePath.length() == 0) {
    StringBuffer requestURL = request.getRequestURL();
    String ctxPath = request.getContextPath();
    basePath = requestURL.substring(0,requestURL.indexOf(ctxPath)+ctxPath.length());
  }
  return basePath;
}

/**
 * Instantiates a new Identity adapter.
 * @return the new identity adapter
 */
public IdentityAdapter newIdentityAdapter() {
  IdentityAdapter idAdapter = getApplicationConfiguration().newIdentityAdapter();
  idAdapter.setRequestContext(this);
  return idAdapter;
}

/**
 * Flags the end of the execution phase of a request.
 * <br/>Database connections accessed during the request are freed by this method.
 * <br/>This method must be triggered by the controller managing this request context.
 */
public void onExecutionPhaseCompleted() {
  getConnectionBroker().closeAll();
  if (_timeCompleted == null) {
    _timeCompleted = new Timestamp(System.currentTimeMillis());
  }
}

/**
 * Flags the end of the execution phase of a request.
 * <br/>Database connections accessed during the request are freed by this method.
 * <br/>This method must be triggered by the controller if the previewView
 * event has been invoked.
 */
public void onPrepareViewCompleted() {
  getConnectionBroker().closeAll();
}

/**
 * Adds the to session.  Placed in central class so that inner
 * classes do not have to work with the http session object.
 * 
 * @param key the key
 * @param obj the obj
 */
public void addToSession(String key, Object obj) {
  
  ServletRequest request = this.getServletRequest();
  if(!(request instanceof HttpServletRequest)) {
    return;
  }
  HttpServletRequest hRequest = (HttpServletRequest) request;
  HttpSession session = hRequest.getSession(true);
  session.setAttribute(key, obj);
  
}

/**
 * Extract an object from session.
 * 
 * @param key the key
 * 
 * @return the object (possibly null)
 */
public Object extractFromSession(String key) {
  ServletRequest request = this.getServletRequest();
  if(!(request instanceof HttpServletRequest)) {
    return null;
  }
  HttpServletRequest hRequest = (HttpServletRequest) request;
  HttpSession session = hRequest.getSession(true);
  return session.getAttribute(key);
  
}

}
