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
package com.esri.gpt.framework.security.principal;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.util.Val;

/**
 * Represents an attribute associated with a user's profile.
 */
public class UserAttribute extends StringAttribute {
  
// class variables =============================================================

// instance variables ==========================================================
private String _ldapName = "";
  
// constructors ================================================================

/** Default constructor. */
public UserAttribute() {
  this("","");
}

/**
 * Constructs with a supplied key and value.
 * @param key the key
 * @param value the value
 */
public UserAttribute(String key, String value) {
  super(key,value);
}

/**
 * Constructs a clone of the supplied attribute.
 * @param attributeToClone the attribute to clone
 */
public UserAttribute(UserAttribute attributeToClone) {
  super("","");
  if (attributeToClone != null) {
    setKey(attributeToClone.getKey());
    setLdapName(attributeToClone.getLdapName());
    setValue(attributeToClone.getValue());
  }
}

// properties ==================================================================

/**
 * Gets the corresponding LDAP name for this attribute.
 * @return the LDAP name
 */
public String getLdapName() {
  return _ldapName;
}
/**
 * Sets the corresponding LDAP name for this attribute.
 * @param name the LDAP name
 */
public void setLdapName(String name) {
  _ldapName = Val.chkStr(name);
}

// methods =====================================================================

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(":");
  sb.append(" key=\"").append(getKey()).append("\"");
  sb.append(" ldapName=\"").append(getLdapName()).append("\"");
  sb.append(" value=\"").append(getValue()).append("\"");
  return sb.toString();
}

}
