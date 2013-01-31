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
import com.esri.gpt.framework.security.identity.AuthenticationStatus;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;

/**
 * Provides a Map interface for setting the rendered attribute of a
 * Faces component based upon a supplied role key.
 * <p>Example:<br/>
 * rendered="#{PageContext.roleMap['someRole']}"
 */
public class RoleMap extends FacesMap<Boolean> {
  
// class variables =============================================================

// instance variables ==========================================================
private User _activeUser;
  
// constructors ================================================================
 
/**
 * Constructs based upon a supplied user.
 * @param activeUser the user associated with the active request
 */
public RoleMap(User activeUser) {
  _activeUser = activeUser;
  if (_activeUser == null) {
    _activeUser = new User();
  }
}

// properties ==================================================================

// methods =====================================================================

/**
 * Implements the "get" method for a Map to determine if the active user has
 * a specified role.
 * <p>The supplied role String is tokenized with the following 3 delimiters:
 * <br/> semi-colon comma space
 * <br/>If the has any specified role, Boolean.TRUE is returned.
 * @param role the role(s) to check (must be a String)
 * @return Boolean.TRUE if the active user has the specified role
 */
@Override
public Boolean get(Object role) {
  AuthenticationStatus authStatus = _activeUser.getAuthenticationStatus();
  RoleSet authRoles = _activeUser.getAuthenticationStatus().getAuthenticatedRoles();
  if ((role != null) && (role instanceof String)) {
    String sRole = Val.chkStr((String)role);
    boolean bAnonymous = !authStatus.getWasAuthenticated();
    if (sRole.equalsIgnoreCase("anonymous")) {
      return new Boolean(bAnonymous);
    } else if (!bAnonymous) {
      if (sRole.equalsIgnoreCase("openid")) {
        return Val.chkStr(_activeUser.getKey()).startsWith("urn:openid:");
      } else {
        RoleSet rs = new RoleSet();
        rs.addDelimited(sRole);
        return new Boolean(authRoles.hasRole(rs));
      }
    }
  }
  return Boolean.FALSE;
}

}
