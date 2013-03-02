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
import com.esri.gpt.control.search.browse.TocsByKey;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.IdentitySupport;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;
import java.util.Locale;

/**
 * Represents the context for an active JSF based page.
 * <p>
 * The PageContext is a JSF managed bean in request scope.
 */
public class PageContext {
  
// class variables =============================================================

// instance variables ==========================================================
private String             _caption = "";
private String             _captionResourceKey = "";
private FacesContextBroker _contextBroker = new FacesContextBroker();
private String             _pageId = "";
private RoleMap            _roleMap = null;
private String             _prepareView = "";
private String             _tabId = "";
private TocsByKey		   _tocsByKey = null;
private boolean 		   _manageUser = false;
 
// constructors ================================================================
 
/** Default constructor. */
public PageContext() {}

// properties ==================================================================

/**
 * Gets the page caption.
 * @return the caption
 */
public String getCaption() {
  if (_caption.length() > 0) {
    return _caption;
  } else if (getCaptionResourceKey().length() > 0) {
    MessageBroker broker = getContextBroker().extractMessageBroker();
    return broker.retrieveMessage(getCaptionResourceKey());    
  } else {
    return "???Caption";
  }
}
/**
 * Sets the page caption.
 * @param caption the caption
 */
public void setCaption(String caption) {
  _caption = Val.chkStr(caption);
}

/**
 * Gets the resource key associated with the page caption.
 * @return the resource key
 */
public String getCaptionResourceKey() {
  return _captionResourceKey;
}
/**
 * Sets the resource key associated with the page caption.
 * @param resourceKey the resource key
 */
public void setCaptionResourceKey(String resourceKey) {
  _captionResourceKey = Val.chkStr(resourceKey);
}

/**
 * Gets tab id.
 * @return tab id
 */
public String getTabId() {
  return _tabId;
}

/**
 * Sets tab id.
 * @param tabId tab id
 */
public void setTabId(String tabId) {
  _tabId = Val.chkStr(tabId);
}

/**
 * Gets the Faces context broker.
 * @return the Faces context broker
 */
private FacesContextBroker getContextBroker() {
  return _contextBroker;
}

/**
 * Gets the context path.
 * @return the context path
 */
public String getContextPath() {
  return this.getContextBroker().getExternalContext().getRequestContextPath();
}

/**
 * Gets application configuration.
 * @return application configuration
 */
public ApplicationConfiguration getApplicationConfiguration() {
  return getContextBroker().extractRequestContext().getApplicationConfiguration();
}

/**
 * Gets the supported identity functions.
 * @return the supported identity functions
 */
public IdentitySupport getIdentitySupport() {
  return getApplicationConfiguration().getIdentityConfiguration().getSupportedFunctions();
}

/**
 * Gets language code (lowercase).
 * @return language 2-letter code
 */
public String getLanguage() {
  Locale locale = getContextBroker().extractHttpServletRequest().getLocale();
  if (locale==null) {
    locale = Locale.ENGLISH;
  }
  return locale.getLanguage();
}

/**
 * Gets the menu link style map.
 * @return the menu link style map
 */
public MenuStyleMap getMenuStyleMap() {
  return new MenuStyleMap(getPageId());
}

/**
 * Gets the page id.
 * @return the page id
 */
public String getPageId() {
  return _pageId;
}
/**
 * Sets the page id.
 * @param id the page id
 */
public void setPageId(String id) {
  _pageId = Val.chkStr(id);
}

/**
 * Gets resource message associated with a key.
 * @return the resource message
 */
public String getResourceMessage(String resourceKey) {
  MessageBroker broker = getContextBroker().extractMessageBroker();
  return broker.retrieveMessage(resourceKey);
}

/**
 * Gets the role map associated with the active user.
 * @return the role map
 */
public RoleMap getRoleMap() {
  if (_roleMap == null) {
    _roleMap = new RoleMap(getUser());
  }
  return _roleMap;
}



/**
 * Gets the site title.
 * @return the site title
 */
public String getSiteTitle() {
  return this.getResourceMessage("catalog.site.title");
}

/**
 * Gets the menu link style map.
 * @return the menu link style map
 */
public TabStyleMap getTabStyleMap() {
  return new TabStyleMap(getTabId(),getPageId());
}

/**
 * Gets the toc collection map.
 * @return the toc collection map
 */
public TocsByKey getTocsByKey() {
  if (this._tocsByKey == null) {
    this._tocsByKey = new TocsByKey(getApplicationConfiguration().getCatalogConfiguration().getConfiguredTocs());
  }
  return _tocsByKey;
}
/**
 * Gets the user associated with the active request.
 * @return the user
 */
public User getUser() {
  return getContextBroker().extractRequestContext().getUser();
}

/**
 * Gets the application version.
 * @return the application version
 */
public String getVersion() {
  return getContextBroker().extractRequestContext().getApplicationConfiguration().getVersion();
}

/**
 * Gets the welcome message.
 * @return the site title
 */
public String getWelcomeMessage() {
  String sName = getUser().getName();
  if (sName.length() == 0) {
    return "";
  } else {
    String[] args = new String[1];
    args[0] = sName;
    MessageBroker broker = getContextBroker().extractMessageBroker();
    return broker.retrieveMessage("site.welcome",args);
  }
}

/**
 * Gets expression used to prepare view.
 * @return expression used to prepare view
 */
public String getPrepareView() {
  return _prepareView;
}

/**
 * Sets expression used to prepare view.
 * @param prepareView expression used to prepare view
 */
public void setPrepareView(String prepareView) {
  _prepareView = Val.chkStr(prepareView);
}

/**
 * Checks if manage user role is enabled
 * @return manageUser enabled if true
 */
public boolean isManageUser() {
	RequestContext rc = getContextBroker().extractRequestContext();
	UsernamePasswordCredentials upc = rc.getIdentityConfiguration().getSimpleConfiguration().getServiceAccountCredentials();
	if(upc !=null) return _manageUser;
	
	StringAttributeMap sNavParameters = rc.getCatalogConfiguration().getParameters();
	if(sNavParameters.containsKey("ldap.identity.manage.userRoleEnabled")){	
		String hasManageUser = Val.chkStr(sNavParameters.getValue("ldap.identity.manage.userRoleEnabled"));
		_manageUser = Boolean.valueOf(hasManageUser);
	}
	return _manageUser;
}

/**
 * Sets value for manage users link
 * @param _manageUser the value for manageUser
 */
public void setManageUser(boolean _manageUser) {
	this._manageUser = _manageUser;
}

// methods =====================================================================
/**
 * Extract the PageContext from the Faces context instance.
 * @return the PageContext
 */
public static PageContext extract() {
  return (new FacesContextBroker()).extractPageContext();
}

/**
 * Extract the MessageBroker from the Faces context instance.
 * @return the PageContext
 */
public static MessageBroker extractMessageBroker() {
  return (new FacesContextBroker()).extractMessageBroker();
}


}
