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
package com.esri.gpt.framework.security.credentials;
import com.esri.gpt.framework.util.Val;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import sun.misc.BASE64Encoder;

/**
 * Stores username/password credentials.
 */
public class UsernamePasswordCredentials extends Credentials {

// class variables =============================================================
private static Random RANDOM = new Random(System.currentTimeMillis());

// instance variables ==========================================================
private String _confirmationPassword = "";
private String _distinguishedName = "";
private String _password = "";
private String _targetedGroupDN = "";
private String _username = "";

// constructors ================================================================

/** Default constructor. */
public UsernamePasswordCredentials() {
  super();
}

/**
 * Constructs with a supplied username and password.
 * @param username the username
 * @param password the password
 */
public UsernamePasswordCredentials(String username, String password) {
  super();
  setUsername(username);
  setPassword(password);
}

// properties ==================================================================

/**
 * Gets the confirmation password.
 * <br/>A confirmation password is typically used when a someone is creating
 * a new, or modifying an existing password. They are asked to enter the
 * new password twice in an attempt to avoid a typographical error.
 * @return the confirmation password
 */
public String getConfirmationPassword() {
  return _confirmationPassword;
}
/**
 * Sets the confirmation password.
 * <br/>A confirmation password is typically used when a someone is creating
 * a new, or modifying an existing password. They are asked to enter the
 * new password twice in an attempt to avoid a typographical error.
 * @param password the  confirmation password
 */
public void setConfirmationPassword(String password) {
  if (password == null) password = "";
  _confirmationPassword = password;
}

/**
 * Gets the distinguished name for this user.
 * <br/>The distinguished name is typically used for an LDAP reference.
 * @return the distinguished name
 */
public String getDistinguishedName() {
  return _distinguishedName;
}
/**
 * Sets the distinguished name for this user.
 * <br/>The distinguished name is typically used for an LDAP reference.
 * <br/>The name is trimmed and stored in lower-case.
 * @param name the distinguished name
 */
public void setDistinguishedName(String name) {
  _distinguishedName = Val.chkStr(name).toLowerCase();
}

/**
 * Gets the password.
 * @return the password
 */
public String getPassword() {
  return _password;
}
/**
 * Sets the password.
 * @param password the password
 */
public void setPassword(String password) {
  if (password == null) password = "";
  _password = password;
}

/**
 * Gets the targeted group DN.
 * <br/>In some cases the user may wish to target a metadata management
 * group upon login. The convention is to supply the targeted group
 * name within the username as: myName@@someMetadataManagementGroupname
 * @return the targeted group DN
 */
public String getTargetedGroupDN() {
  return _targetedGroupDN;
}
/**
 * Sets the targeted group DN.
 * <br/>In some cases the user may wish to target a metadata management
 * group upon login. The convention is to supply the targeted group
 * name within the username as: myName@@someMetadataManagementGroupname
 * @param groupDN the targettd group DN
 */
public void setTargetedGroupDN(String groupDN) {
  _targetedGroupDN = Val.chkStr(groupDN).toLowerCase();
}

/**
 * Gets the username.
 * @return the username
 */
public String getUsername() {
  return _username;
}
/**
 * Sets the username.
 * @param username the username
 */
public void setUsername(String username) {
  _username = Val.chkStr(username);
}

// methods =====================================================================

/**
 * Encrypts a password according to the algorithm specified.
 * @param algorithm the algorithm (SHA or MD5)
 * @return the encrypted password
 */
public String encryptLdapPassword(String algorithm) {
  String sEncrypted = _password;
  if ((_password != null) && (_password.length() > 0)) {
    algorithm = Val.chkStr(algorithm);
    boolean bMD5 = algorithm.equalsIgnoreCase("MD5");
    boolean bSHA = algorithm.equalsIgnoreCase("SHA")  ||
                   algorithm.equalsIgnoreCase("SHA1") ||
                   algorithm.equalsIgnoreCase("SHA-1");
    if (bSHA || bMD5) {
      String sAlgorithm = "MD5";
      if (bSHA) {
        sAlgorithm = "SHA";
      }
      try {
        MessageDigest md = MessageDigest.getInstance(sAlgorithm);
        md.update(getPassword().getBytes("UTF-8"));
        sEncrypted = "{"+sAlgorithm+"}"+(new BASE64Encoder()).encode(md.digest());
      } catch (NoSuchAlgorithmException e) {
        sEncrypted = null;
        e.printStackTrace(System.err);
      } catch (UnsupportedEncodingException e) {
        sEncrypted = null;
        e.printStackTrace(System.err);
      }
    }
  }
  return sEncrypted;
}

/**
 * Generates and sets random password.
 */
public void generatePassword() {
  int nAlpha = 0;
  String aAlpha[] = {"a","b","c","d","e","f","g","h","i","j","k","l","m",
                     "n","o","p","q","r","s","t","u","v","w","x","y","z"};
  String aSpecial[] = {"#","$","_","@"};

  // generate a random digit string
  BigInteger iA = new BigInteger("3781927463263421");
  BigInteger iC = new BigInteger("2113248654051873");
  BigInteger iM = new BigInteger("10000000000000000");
  String sStart = ""+RANDOM.nextLong();
  BigInteger iS = new BigInteger(sStart);
  String sPwd = iS.multiply(iA).add(iC).mod(iM).toString();

  // prefix with alpha characters to ensure 16 characters
  while(sPwd.length() < 16) {
    sPwd = aAlpha[nAlpha] + sPwd;
    nAlpha++;
  }

  // ensure a special character within the first seven characters
  int nSpecIdx = RANDOM.nextInt(aSpecial.length);
  int nSpecPIdx = RANDOM.nextInt(7);
  sPwd = sPwd.substring(0,nSpecPIdx) + aSpecial[nSpecIdx] +
         sPwd.substring(nSpecPIdx+1);

  // ensure an upper case alpha character
  int nAlphaIdx = RANDOM.nextInt(aAlpha.length);
  int nAlphaPIdx = RANDOM.nextInt(16);
  if (nAlphaPIdx == nSpecPIdx) {
    nAlphaPIdx++;
  }
  sPwd = sPwd.substring(0,nAlphaPIdx) + aAlpha[nAlphaIdx].toUpperCase() +
         sPwd.substring(nAlphaPIdx+1);

  // ensure that 3 characters in a row are not the same
  String aThree[] = {"","",""};
  for (int i=0;i<sPwd.length();i++) {
    aThree[2] = aThree[1];
    aThree[1] = aThree[0];
    aThree[0] = ""+sPwd.charAt(i);
    if (aThree[0].equalsIgnoreCase(aThree[1]) && aThree[0].equalsIgnoreCase(aThree[2])) {
      aThree[0] = aAlpha[nAlpha];
      sPwd = sPwd.substring(0,i) + aThree[0] + sPwd.substring(i+1);
      nAlpha++;
    }
  }
  setPassword(sPwd);
}

}

