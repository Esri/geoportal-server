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
package com.esri.gpt.framework.security.identity.ldap;
import com.esri.gpt.framework.util.Val;

/**
 * Super-class for a configured set of LDAP properties.
 */
public class LdapProperties {
  
// class variables =============================================================
private static final String[] SPECIAL_DN_CHARS = {"=","*",",","%"};

// instance variables ==========================================================
  
// constructors ================================================================

/** Default constructor. */
public LdapProperties() {}

// properties ==================================================================

// methods =====================================================================

/**
 * Determines if an objectDN contains a special character.
 * <br/>Special characters include: "=","*",",","%"
 * @param objectDN the DN to check
 * @return true if a special character is found
 */
public boolean hasSpecialDNCharacter(String objectDN) {
  if (objectDN.length() > 0) {
    for (int i=0;i<SPECIAL_DN_CHARS.length;i++) {
      if (objectDN.indexOf(SPECIAL_DN_CHARS[i]) != -1) {
        return true;
      }
    }
  }
  return false;
}

/**
 * Replaces all occurences of {0} within a pattern with the supplied value.
 * @param pattern the replacement pattern
 * @param value the replacement value
 */
public String replace(String pattern, String value) {
  pattern = Val.chkStr(pattern);
  value = Val.chkStr(value);
  if ((pattern.length() > 0) && (pattern.indexOf("{0}") != -1)) {
    value = pattern.replaceAll("\\{0\\}",value);
  }
  return value;
}

/**
 * Replaces all occurrences of param within a pattern with the supplied value.
 * @param pattern the replacement pattern
 * @param value the replacement value
 */
public String replaceParam(String pattern, String value, String param) {
  pattern = Val.chkStr(pattern);
  value = Val.chkStr(value);
  param = Val.chkStr(param);
  if ((pattern.length() > 0) && (pattern.indexOf("{0}") != -1)) {
	  param = pattern.replaceAll("\\{0\\}",param);
  }
  if ((param.length() > 0) && (param.indexOf("{1}") != -1)) {
	  value = param.replaceAll("\\{1\\}",value);
  }
  return pattern;
 // return value;
}

}
