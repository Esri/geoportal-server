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
import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.el.VariableResolver;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;

/**
 * Broker for a JSF FacesContext.
 */
public class FacesContextBroker {
  
// class variables =============================================================
  
/** Managed bean name for the faces message broker. */
private static final String NAME_MESSAGEBROKER = "MessageBroker";

/** Managed bean name for the faces page context. */
private static final String NAME_PAGECONTEXT = "PageContext";

// instance variables ==========================================================
  
// constructors ================================================================
  
/** Default constructor. */
public FacesContextBroker() {}

/**
 * Constructs a Faces context broker from the current HTTP request.
 * @param request the HTTP request
 * @param response the HTTP response
 */
public FacesContextBroker(HttpServletRequest request, HttpServletResponse response) {
  FacesContextFactory contextFactory = (FacesContextFactory)FactoryFinder.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
  LifecycleFactory lifecycleFactory = (LifecycleFactory)FactoryFinder.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
  Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);
  ServletContext servletContext = ((HttpServletRequest)request).getSession().getServletContext();
  FacesContext fc = contextFactory.getFacesContext(servletContext, request, response, lifecycle);
  InnerFacesContext.setFacesContextAsCurrentInstance(fc);
  UIViewRoot view = fc.getApplication().getViewHandler().createView(fc,"/gptInnerFacesContext");
  fc.setViewRoot(view);
}

// properties ==================================================================

/**
 * Gets the external context associated with the current FacesContext instance.
 * @return the external context
 */
public ExternalContext getExternalContext() {
  FacesContext fc = getFacesContext();
  if (fc != null) {
    return fc.getExternalContext();
  } else {
    return null;
  }
}

/**
 * Gets the current FacesContext instance.
 * @return the FacesContext
 */
public FacesContext getFacesContext() {
  return FacesContext.getCurrentInstance();
}

// methods =====================================================================

/**
 * Extracts the HttpServletRequest from the Faces context instance.
 * @return the HttpServletRequest (null if none);
 */
public HttpServletRequest extractHttpServletRequest() {
  Object o = null;
  ExternalContext ec = getExternalContext();
  if (ec != null) o = ec.getRequest();
  if ((o != null) && (o instanceof HttpServletRequest)) {
    return (HttpServletRequest)o;
  } else {
    return null;
  }
}

/**
 * Extracts the HttpServletResponse from the Faces context instance.
 * @return the HttpServletResponse (null if none);
 */
public HttpServletResponse extractHttpServletResponse() {
  Object o = null;
  ExternalContext ec = getExternalContext();
  if (ec != null) o = ec.getResponse();
  if ((o != null) && (o instanceof HttpServletResponse)) {
    return (HttpServletResponse)o;
  } else {
    return null;
  }
}

/**
 * Extracts the HttpSession from the Faces context instance.
 * @return the HttpSession (null if none);
 */
public HttpSession extractHttpSession() {
  Object o = null;
  ExternalContext ec = getExternalContext();
  if (ec != null) o = ec.getSession(true);
  if ((o != null) && (o instanceof HttpSession)) {
    return (HttpSession)o;
  } else {
    return null;
  }
}

/**
 * Extract the MessageBroker from the Faces context instance.
 * @return the MessageBroker
 */
public MessageBroker extractMessageBroker() {
  MessageBroker broker = (MessageBroker)resolveManagedBean(NAME_MESSAGEBROKER);
  if (broker == null) {
    return new MessageBroker();
  }
  return broker;
}

/**
 * Extract the PageContext from the Faces context instance.
 * @return the PageContext
 */
public PageContext extractPageContext() {
  PageContext fpc = (PageContext)resolveManagedBean(NAME_PAGECONTEXT);
  if (fpc == null) {
    return new PageContext();
  }
  return fpc;
}

/**
 * Extract the request context from the Faces context instance.
 * @return the request context
 */
public RequestContext extractRequestContext() {
  return RequestContext.extract(extractHttpServletRequest());
}

/**
 * Extracts the ServletContext from the Faces context instance.
 * @return the ServletContext (null if none);
 */
public ServletContext extractServletContext() {
  Object o = null;
  ExternalContext ec = getExternalContext();
  if (ec != null) o = ec.getContext();
  if ((o != null) && (o instanceof ServletContext)) {
    return (ServletContext)o;
  } else {
    return null;
  }
}

/**
 * Extracts the ServletContext from the Faces context instance.
 * @return the ServletContext (null if none);
 */
public UIViewRoot extractViewRoot() {
  FacesContext fc = getFacesContext();
  if (fc != null) {
    return fc.getViewRoot();
  } else {
    return null;
  }
}
  
/**
 * Resolves a managed bean variable.
 * @param managedBeanName the configured name of the managed bean
 * @return the located object (can be null)
 */
public Object resolveManagedBean(String managedBeanName) {
  Object oBean = null;
  String sn = Val.chkStr(managedBeanName);
  FacesContext fc = getFacesContext();
  if ((sn.length() > 0) && (fc != null) && (fc.getApplication() != null)) {
    VariableResolver resolver = fc.getApplication().getVariableResolver();
    if (resolver != null) {
      oBean = resolver.resolveVariable(fc,sn);
    }
  }
  return oBean;
}

private abstract static class InnerFacesContext extends FacesContext {
  protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
    FacesContext.setCurrentInstance(facesContext);
  }
}
  
}
