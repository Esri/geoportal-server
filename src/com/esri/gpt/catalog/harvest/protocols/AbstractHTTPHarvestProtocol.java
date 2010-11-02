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
package com.esri.gpt.catalog.harvest.protocols;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.security.codec.PC1_Encryptor;
import com.esri.gpt.framework.util.Val;

/**
 * Abstract HTTP-based harvest protocol
 */
public abstract class AbstractHTTPHarvestProtocol extends HarvestProtocol {

// class variables =============================================================

// instance variables ==========================================================
/** User name if required to login. */
private String _userName = "";
/** User password if required to login. */
private String _userPassword = "";
// constructors ================================================================

// properties ==================================================================
/**
 * Gets user name.
 * @return user name
 */
public String getUserName() {
  return _userName;
}

/**
 * Sets user name.
 * @param userName user name
 */
public void setUserName(String userName) {
  _userName = Val.chkStr(userName);
}

/**
 * Gets user password.
 * @return user password
 */
public String getUserPassword() {
  return _userPassword;
}

/**
 * Sets user password.
 * @param userPassword user password
 */
public void setUserPassword(String userPassword) {
  _userPassword = Val.chkStr(userPassword);
}
// methods =====================================================================
/**
 * Gets all the attributes.
 * @return attributes as attribute map
 */
protected StringAttributeMap extractAttributeMap() {
  StringAttributeMap properties = new StringAttributeMap();

  properties.set("username", encryptString(_userName));
  properties.set("password", encryptString(_userPassword));

  return properties;
}

/**
 * Gets all the attributes.
 * @return attributes as attribute map
 */
public StringAttributeMap getAttributeMap() {
  StringAttributeMap properties = new StringAttributeMap();

  properties.set("username", _userName);
  properties.set("password", _userPassword);

  return properties;
}

/**
 * Sets all the attributes.
 * @param attributeMap attributes as attribute map
 */
protected void applyAttributeMap(StringAttributeMap attributeMap) {
  setUserName(decryptString(chckAttr(attributeMap.get("username"))));
  setUserPassword(decryptString(chckAttr(attributeMap.get("password"))));
}

/**
 * Sets all the attributes.
 * @param attributeMap attributes as attribute map
 */
public void setAttributeMap(StringAttributeMap attributeMap) {
  setUserName(chckAttr(attributeMap.get("username")));
  setUserPassword(chckAttr(attributeMap.get("password")));
}

/**
 * Decrypts string.
 * @param s string to decrypt
 * @return decrypted string
 */
private String decryptString(String s) {
  s = Val.chkStr(s);
  String sEncKey = getEncKey();
  if (sEncKey.length()>0 && s.length()>0) {
    try {
      s = PC1_Encryptor.decrypt(sEncKey, s);
    } catch (IllegalArgumentException ex) {
      
    }
  }
  return s;
}
/**
 * Encrypts string.
 * @param s string to encrypt
 * @return encrypted string
 */
private String encryptString(String s) {
  s = Val.chkStr(s);
  String sEncKey = getEncKey();
  if (sEncKey.length()>0) {
    s = PC1_Encryptor.encrypt(sEncKey, s);
  }
  return s;
}
/**
 * Gets encryption key.
 * @return encryption key
 */
private String getEncKey() {
  ApplicationContext appCtx = ApplicationContext.getInstance();
  ApplicationConfiguration appCfg = appCtx.getConfiguration();
  return appCfg.getIdentityConfiguration().getEncKey();
}
}
