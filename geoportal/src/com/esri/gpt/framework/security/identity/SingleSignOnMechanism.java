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
package com.esri.gpt.framework.security.identity;
import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.framework.util.Val;

/**
 * Provides support for a single sign on process.
 * <p/>
 * There are 2 patterns for specifying the credential location:
 * <li>credentialLocation="userPrincipal"</li>
 * <li>credentialLocation="header.variable"</li>
 * <p/>
 * "userPrincipal" is the typical case and indicates that the credential 
 * can be found within HttpServletRequest.getUserPrincipal().getName()
 * <p/>
 * header.variable indicates that the credential can be found within 
 * the HTTP header. 
 * <br/>Example: credentialLocation="header.html-wg-useruid" indicates that the 
 * credential can be found within an HTTP header variable named 
 * "html-wg-useruid" (ie. HttpServletRequest.getHeader("html-wg-useruid"))
 * 
 */
public class SingleSignOnMechanism {

// class variables =============================================================
  

// instance variables ==========================================================
private boolean _active = false;
private boolean _canCheck = false;
private String  _anonymousValue = "";
private boolean _checkUserPrincipal = false;
private String  _credentialLocation = "";
private String  _headerVariableName = "";

// constructors ================================================================

/** Default constructor. */
public SingleSignOnMechanism() {}

// properties ==================================================================

/**
 * Gets the flag indication whether or not single sign-on is active.
 * @return true if single sign-on is active.
 */
public boolean getActive() {
  return _active && _canCheck; 
}

/**
 * Sets the flag indication whether or not single sign-on is active.
 * @param active true if single sign-on is active.
 */
public void setActive(boolean active) {
  _active = active; 
}

/**
 * Gets the credential value that indicates an anonymous user.
 * <br/>For most mechanisms, an anonymous user is represented by a null value 
 * or empty string. For others, the specification is explicit,
 * eg. "OblixAnonymous"
 * @return the credential value indicating an anonymous user
 */
public String getAnonymousValue() {
  return _anonymousValue;
}
/**
 * Sets the credential value that indicates an anonymous user.
 * <br/>For most mechanisms, an anonymous user is represented by a null value 
 * or empty string. For others, the specification is explicit,
 * eg. "OblixAnonymous"
 * @param value the credential value indicating an anonymous user
 */
public void setAnonymousValue(String value) {
  _anonymousValue = Val.chkStr(value);
}

/**
 * Gets the location of the credential.
 * @return the credential location
 */
public String getCredentialLocation() {
  return _credentialLocation;
}
/**
 * Sets the location of the credential.
 * @param location the credential location
 */
public void setCredentialLocation(String location) {
  _credentialLocation = Val.chkStr(location);
  _canCheck = false;
  _checkUserPrincipal = false;
  _headerVariableName = "";
  if (_credentialLocation.equalsIgnoreCase("userPrincipal")) {
    _checkUserPrincipal = true;
    _canCheck = true;
  } else if (_credentialLocation.toLowerCase().startsWith("header.")) {
    _headerVariableName = Val.chkStr(_credentialLocation.substring(7));
    _canCheck = (_headerVariableName.length() > 0);
  }
}

// methods =====================================================================

/**
 * Determines the user name associated with a single sign-on request.
 * @return the username (empty string if anonymous)
 */
public String determineUsername(HttpServletRequest request) {
  String sUsername = "";
  if (_canCheck && _checkUserPrincipal) {
    if (request.getUserPrincipal() != null) {
      sUsername = Val.chkStr(request.getUserPrincipal().getName());
    }
  } else if (_canCheck && (_headerVariableName.length() > 0)) {
    sUsername = Val.chkStr(request.getHeader(_headerVariableName));
    if ((getAnonymousValue().length() > 0) && getAnonymousValue().equals(sUsername)) {
      sUsername = "";
    }
  }
  return sUsername;
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" active=\"").append(getActive()).append("\"\n");
  sb.append(" credentialLocation=\"").append(getCredentialLocation()).append("\"\n");
  sb.append(" anonymousValue=\"").append(getAnonymousValue()).append("\"\n");
  if (getActive() && (_headerVariableName.length() > 0)) {
    sb.append(" headerVariableName=\"").append(_headerVariableName).append("\"\n");
  }
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}
