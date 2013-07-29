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
import com.esri.gpt.framework.collection.CaseInsensitiveMap;

/**
 * Maintains and ordered map representing the attributes
 * associated with a user's profile.
 * <p/>
 * Map keys are trimmed and converted to lower case. Null or empty 
 * map keys are ignored.
 */
public class UserAttributeMap extends CaseInsensitiveMap<UserAttribute> {
  
// class variables =============================================================
  
/** The email address attribute tag = "email" */
public static final String TAG_EMAIL = "email";

/** The username attribute tag = "username" */
public static final String TAG_USER_NAME = "username";

/** The password attribute tag = "password" */
public static final String TAG_USER_PASSWORD = "password";

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public UserAttributeMap() {
  this(null);
}

/**
 * Constructs a clone of the supplied attribute map.
 * @param mapToClone the attribute map to clone
 */
public UserAttributeMap(UserAttributeMap mapToClone) {
  super(false);
  if (mapToClone != null) {
    for (UserAttribute attr: mapToClone.values()) {
      add(new UserAttribute(attr));
    }
  }
}
 
// properties ==================================================================

/**
* Gets the email address.
* @return the email address
*/
public String getEmailAddress() {
  return getValue(UserAttributeMap.TAG_EMAIL);
}
/**
* Sets the email address.
* @param emailAddress the email address
*/
public void setEmailAddress(String emailAddress) {
  set(UserAttributeMap.TAG_EMAIL,emailAddress);
}

/**
 * Gets the username.
 * @return the username
 */
public String getUsername() {
  return getValue(UserAttributeMap.TAG_USER_NAME);
}
/**
* Sets the email address.
* @param username the user name
*/
public void setUsername(String username) {
  set(UserAttributeMap.TAG_USER_NAME,username);
}

// methods =====================================================================

/**
 * Adds a member to the collection.
 * <br/>The member will not be added if it is null or
 * if it has an empty key.
 * @param member the member to add
 */
public void add(UserAttribute member) {
  if ((member != null) && (member.getKey().length() > 0)) {
    put(member.getKey(),member);
  }
}

/**
 * Gets the underlying value associated with an attribute.
 * @param key the key associated with the value
 * @return the value associated with the key
 */
private String getValue(String key) {
  UserAttribute attr = get(key);
  if (attr != null) {
    return attr.getValue();
  } else {
    return "";
  }
}


/**
 * Sets the value associated with a supplied key.
 * If an attribute associated with the key does not exist within the collection,
 * a new attribute will be created and added.
 * @param key the key associated with the value
 * @param value the value to set
 */
public void set(String key, String value) {
  UserAttribute attr = get(key);
  if (attr != null) {
    attr.setValue(value);
  } else {
    add(new UserAttribute(key,value));
  }
}

}
