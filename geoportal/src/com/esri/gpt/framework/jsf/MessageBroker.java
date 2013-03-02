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
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;

import com.esri.gpt.framework.util.Val;

/**
 * Retrieves messages based upon the resource bundle associated with the locale
 * of the Faces view root.
 */
public class MessageBroker {
  
// class variables =============================================================
  
/** Navigation outcome - home page with no redirection, "homeDirect" */
private static final String RESOURCEKEY_PREFIX= "catalog.";

/** Default resource bundle that should be looked at **/
public static final String DEFAULT_BUNDLE_BASE_NAME = "gpt.resources.gpt";

// instance variables ==========================================================
private ResourceBundle     _bundle = null;
private String             _bundleBaseName = "";
private final FacesContextBroker _contextBroker = new FacesContextBroker();

  
// constructors ================================================================
  
/** Default constructor. */
public MessageBroker() {}
  
// properties ==================================================================

/**
 * Gets the associated ResourceBundle
 * @return the ResourceBundle
 */
private ResourceBundle getBundle() {
  if (_bundle == null) {
    _bundle = makeResourceBundle(getBundleBaseName());
  }
  return _bundle;
}

/**
 * Gets the base name for the resource bundle.
 * @return the ResourceBundle base name
 */
public String getBundleBaseName() {
  return _bundleBaseName;
}
/**
 * Sets the base name for the resource bundle.
 * @param baseName the resource bundle base name
 */
public void setBundleBaseName(String baseName) {
  baseName = Val.chkStr(baseName);
  if (!baseName.equalsIgnoreCase(_bundleBaseName)) {
    _bundle = null;
  }
  _bundleBaseName = baseName;
}

/**
 * Gets the Faces context broker.
 * @return the Faces context broker
 */
private FacesContextBroker getContextBroker() {
  return _contextBroker;
}

/**
 * Gets the resource key prefix.
 * @return the resource key prefix
 */
public String getResourceKeyPrefix() {
  return RESOURCEKEY_PREFIX;
}
  
// methods =====================================================================

/**
 * Adds a message to the Faces context.
 * @param message the message to add
 */
public void addMessage(FacesMessage message) {
  FacesContext fc = getContextBroker().getFacesContext();
  if ((fc != null) && (message != null)) {
    fc.addMessage(null,message);
  }
}

/**
 * Adds an error message to the Faces context.
 * @param resourceKey the resource key for the message
 */
public void addErrorMessage(String resourceKey) { 
  addErrorMessage(resourceKey,null);
}

/**
 * Adds an error message message to the Faces context.
 * @param resourceKey the resource key for the message
 * @param parameters optional formating parameters
 */
public void addErrorMessage(String resourceKey, Object[] parameters) { 
  String sMsg = retrieveMessage(resourceKey,parameters);
  addMessage(new FacesMessage(FacesMessage.SEVERITY_ERROR,sMsg,null));
}

/**
 * Adds an error message to the Faces context based upon a thrown exception.
 * <p>
 * The full class name (including path) of the exception is used a key 
 * to look up an associated message within the resource bundle. If no
 * associated resource is found, the exception's toString method is
 * used to generate the message.
 * @param t the thrown exception
 */
public void addErrorMessage(Throwable t) {
  String sMsg = retrieveString(t.getClass().getName(),false);
  if (sMsg.length() == 0) sMsg = Val.chkStr(t.getMessage());
  if (sMsg.length() == 0) sMsg = Val.chkStr(t.toString());
  FacesMessage msg = new FacesMessage(sMsg);
  msg.setSeverity(FacesMessage.SEVERITY_ERROR);
  addMessage(msg);
}

/**
 * Adds a success message to the Faces context.
 * @param resourceKey the resource key for the message
 */
public void addSuccessMessage(String resourceKey) { 
  addSuccessMessage(resourceKey,null);
}

/**
 * Adds a success message to the Faces context.
 * @param resourceKey the resource key for the message
 * @param parameters optional formating parameters
 */
public void addSuccessMessage(String resourceKey, Object[] parameters) { 
  String sMsg = retrieveMessage(resourceKey,parameters);
  addMessage(new FacesMessage(FacesMessage.SEVERITY_INFO,sMsg,null));
}

/**
 * Gets a message from a resource bundle.
 * @param resourceKey the resource key for the message
 * @return the message
 */
public FacesMessage getMessage(String resourceKey) { 
  return getMessage(resourceKey,null);
}

/**
 * Gets a message from a resource bundle.
 * @param resourceKey the resource key for the message
 * @param parameters optional formating parameters
 * @return the message
 */
public FacesMessage getMessage(String resourceKey, Object[] parameters) { 
  String sMsg = retrieveMessage(resourceKey,parameters);
  return new FacesMessage(sMsg);
}

/**
 * Gets locale.  If locale is null, it will attempt to get it from the
 * jsf viewRoot
 *  
 * @return locale
 */
public Locale getLocale() {
 
  Locale locale = null;
  UIViewRoot viewRoot = getContextBroker().extractViewRoot();
  if (viewRoot != null) {
    locale = viewRoot.getLocale();
  }
  if (locale == null) {
    locale = Locale.getDefault();
  }
  return locale;
}

/**
 * Makes a resource bundle.  Uses {@link #getLocale()}
 * @param bundleBasename the resource bundle base name
 * @return the resource bundle
 */
private ResourceBundle makeResourceBundle(String bundleBasename) {
  
  // determine the class loader
  ClassLoader loader = Thread.currentThread().getContextClassLoader();
  if (loader == null) {
    loader = ClassLoader.getSystemClassLoader();
  }
  
  // determine the locale
  Locale locale = getLocale();
  
  ResourceBundle bundle = ResourceBundle.getBundle(bundleBasename,locale,loader);
  return bundle;
}

/**
 * Makes an unfound UI resource string.
 * @param resourceKey the key for the resource
 * @return a string indicating an unfound UI resource ("???"+resourceKey)
 */
private String makeUnfoundResource(String resourceKey) {
  return "???"+Val.chkStr(resourceKey);
}

/**
 * Retrieves a message from a resource bundle.
 * @param resourceKey the resource key for the message
 * @return the message
 */
public String retrieveMessage(String resourceKey) {
  return retrieveMessage(resourceKey,null);
}

/**
 * Retrieves a message from a resource bundle.
 * @param resourceKey the resource key for the message
 * @param parameters optional formating parameters
 * @return the message
 */
public String retrieveMessage(String resourceKey, Object[] parameters) {
  String sResource = retrieveString(resourceKey,true);
  if (sResource.length() == 0) {
    sResource = makeUnfoundResource(resourceKey);
  } else if ((parameters != null) && (parameters.length > 0)) {
    //MessageFormat formatter = new MessageFormat(resource,locale);
    sResource = MessageFormat.format(sResource,parameters);
  }
  return sResource;
}

/**
 * Retrieves a resource string from a resource bundle.
 * @param resourceKey the resource key 
 * @param checkPrefix if true, check to ensure that the key is properly prefixed
 * @return the resource string (zero-length if no match)
 */
private String retrieveString(String resourceKey, boolean checkPrefix) {
  String sResource = "";
  try {
    resourceKey = Val.chkStr(resourceKey);
    if (checkPrefix && (resourceKey.length() > 0)) {
      if (!resourceKey.startsWith(getResourceKeyPrefix())) {
        if (!resourceKey.startsWith("fgdc.")) {
          resourceKey = getResourceKeyPrefix()+resourceKey;
        }
      }
    }
    ResourceBundle bundle = getBundle();
    sResource = Val.chkStr(bundle.getString(resourceKey));
  } catch (MissingResourceException mre) {
    sResource = "";
  } catch (Exception e) {
    sResource = "";
  }
  return sResource;
}

}
