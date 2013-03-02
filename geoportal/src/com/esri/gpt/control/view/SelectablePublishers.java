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
package com.esri.gpt.control.view;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.Users;
import com.esri.gpt.framework.util.Val;

import java.sql.SQLException;
import java.util.ArrayList;
import javax.faces.model.SelectItem;

/**
 * Defines a list of publishers selectable from the user interface.
 */
public class SelectablePublishers {

// class variables =============================================================

// instance variables ==========================================================
private ArrayList<SelectItem> _list = new ArrayList<SelectItem>();
private String                _selectedKey = "";
  
// constructors ================================================================

/** Default constructor. */
public SelectablePublishers() {}

// properties ==================================================================

/**
 * Determines if the items list contains multiple publishers.
 * @return true if the items list is greater that 1
 */
public boolean getHasMultiple() {
  return (_list.size() > 1);
}

/**
 * Gets the SelectItem list for UI display.
 * @return the SelectItem list
 */
public ArrayList<SelectItem> getItems() {
  return _list;
}

/**
 * Gets the currently selected key.
 * @return the currently selected key
 */
public String getSelectedKey() {
  return _selectedKey;
}
/**
 * Sets the currently selected key.
 * @param key the currently selected key
 */
public void setSelectedKey(String key) {
  _selectedKey = Val.chkStr(key);
}

// methods =====================================================================


/**
 * Builds the list of selectable publishers.
 * @param context the active request context
 * @param forManagement <code>true</code> for management
 */
public void build(RequestContext context, boolean forManagement) {
  _list.clear();
  Users users = Publisher.buildSelectablePublishers(context,forManagement);
  for (User u: users.values()) {
    _list.add(new SelectItem(u.getKey(),u.getName()));
  }
}

/** 
 * Creates a published from the key (DN) selected within the user interface.
 * @param context the current request context (contains the active user)
 * @param forManagement true if the request is in support of the metadata management page
 * @return a publisher associated with the selected key (DN)
 * @throws CredentialsDeniedException if the selected DN is invalid
 * @throws NotAuthorizedException if the new publisher not have publishing rights
 * @throws IdentityException if an integrity violation occurs
 * @throws ImsServiceException if an exception occurs when creating the default folder
 * @throws SQLException if a database exception occurs
 */
public Publisher selectedAsPublisher(RequestContext context, 
                                     boolean forManagement) 
  throws NotAuthorizedException,
         IdentityException, 
         ImsServiceException, 
         SQLException, 
         CredentialsDeniedException {
  String sSelectedKey = getSelectedKey();
  if (sSelectedKey.length() == 0) {
    return new Publisher(context);
  } else {
    User user = context.getUser();
    if (sSelectedKey.equals(user.getKey())) {
      return new Publisher(context);
    } else {
      Users users = Publisher.buildSelectablePublishers(context,forManagement);
      if (users.containsKey(sSelectedKey)) {
        return new Publisher(context,sSelectedKey);
      } else {
        throw new NotAuthorizedException("Not authorized.");
      }
    }
  } 
}
}
