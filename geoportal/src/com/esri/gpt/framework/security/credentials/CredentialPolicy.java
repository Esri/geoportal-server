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
import java.util.regex.Pattern;

import com.esri.gpt.framework.util.Val;

/**
 * Super-class for a credential policy.
 * <p>
 * A credential policy can provide one or more restrictions associated with
 * the creation or modification of credential. For instance:
 * <ul>
 *   <li>a username may not contain certain characters</li>
 *   <li>a password must be at least [n] characters long</li>
 *   <li>...</li>
 * </ul>
 */
public class CredentialPolicy {
  
// class variables =============================================================

// instance variables ==========================================================
private String[] _restrictedUsernameCharacters = {"=","*",",","%"};

// constructors ================================================================

/** Default constructor. */
public CredentialPolicy() {}

// properties ==================================================================

/**
 * Gets the array of characters that are restricted for a new username.
 * @return the restricted characters.
 */
protected String[] getRestrictedUsernameCharacters() {
 return _restrictedUsernameCharacters;
}
/**
 * Sets the array of characters that are restricted for a new username.
 * @param characters the restricted characters.
 */
protected void setRestrictedUsernameCharacters(String[] characters) {
 _restrictedUsernameCharacters = characters;
}

// methods =====================================================================

/**
 * Validates an email address according to policy.
 * @param email the email address to check validate
 * @throws EmailPolicyException if the email policy is violated
 */
public void validateEmailPolicy(String email)
  throws EmailPolicyException  {
  email = Val.chkStr(email);
  if (!Val.chkEmail(email)) {
    throw new EmailPolicyException("The email address is invalid.");
  }
}

/**
 * Validates password credentials according to policy.
 * @param credentials the credentials containing the password to validate
 * @throws PasswordPolicyException if the password policy is violated
 * @throws PasswordConfirmationException if the confirmation password does
 *         not match the new password
 */
public void validatePasswordPolicy(UsernamePasswordCredentials credentials)
  throws CredentialPolicyException {
  validatePasswordPolicyWeak(credentials);
}

/**
 * Validates password credentials according to a strong policy.
 * @param credentials the credentials containing the password to validate
 * @throws PasswordPolicyException if the password policy is violated
 * @throws PasswordConfirmationException if the confirmation password does
 *         not match the new password
 */
private void validatePasswordPolicyStrong(UsernamePasswordCredentials credentials)
  throws CredentialPolicyException {
  String sMsg;
  int nLength = credentials.getPassword().length();
  int nMinLength = 8;
  if (credentials.getPassword().equals(credentials.getUsername())) {
    sMsg = "The password cannot equal the username.";
    throw new PasswordPolicyException(sMsg);
  } else if ((nLength == 0) || (nLength < nMinLength)) {
    sMsg = "The password is less than the minimum length.";
    throw new PasswordPolicyException(sMsg);
  } else if (!credentials.getPassword().equals(credentials.getConfirmationPassword())) {
    sMsg = "The password and confirmation password do not match.";
    throw new PasswordConfirmationException(sMsg);
  }

  // check specific password rules
  String sPwd = credentials.getPassword();
  boolean bHas3Consecutive = Pattern.compile("(.)\\1{2}").matcher(sPwd).find();
  boolean bHasAlpha        = Pattern.compile("[a-zA-Z]").matcher(sPwd).find();
  boolean bHasDigit        = Pattern.compile("[0-9]").matcher(sPwd).find();
  boolean bHasSpecial      = Pattern.compile("[_@#$]").matcher(sPwd).find();
  boolean bHasRestricted   = Pattern.compile("[ ]").matcher(sPwd).find();

  if (bHasRestricted || bHas3Consecutive || !bHasAlpha || !bHasDigit) {
    throw new PasswordPolicyException("The password is invalid.");
  }
  //System.err.println(sPwd);
  //System.err.println("bHas3Consecutive "+bHas3Consecutive);
  //System.err.println("bHasAlpha "+bHasAlpha);
  //System.err.println("bHasDigit "+bHasDigit);
  //System.err.println("bHasSpecial "+bHasSpecial);
  //System.err.println("bHasRestricted "+bHasRestricted);
}

/**
 * Validates password credentials according to a weak policy.
 * @param credentials the credentials containing the password to validate
 * @throws PasswordPolicyException if the password policy is violated
 * @throws PasswordConfirmationException if the confirmation password does
 *         not match the new password
 */
private void validatePasswordPolicyWeak(UsernamePasswordCredentials credentials)
  throws CredentialPolicyException {
  String sMsg;
  int nLength = credentials.getPassword().length();
  int nMinLength = 3;
  if ((nLength == 0) || (nLength < nMinLength)) {
    sMsg = "The password is less than the minimum length.";
    throw new PasswordPolicyException(sMsg);
  } else if (!credentials.getPassword().equals(credentials.getConfirmationPassword())) {
    sMsg = "The password and confirmation password do not match.";
    throw new PasswordConfirmationException(sMsg);
  }

}

/**
 * Validates username credentials according to policy.
 * @param credentials the credentials containing the username to validate
 * @throws UsernamePolicyException if the username policy is violated
 */
public void validateUsernamePolicy(UsernamePasswordCredentials credentials)
  throws UsernamePolicyException  {
  String sMsg;
  String sUsername = credentials.getUsername();
  String[] aRestricted = getRestrictedUsernameCharacters();
  int nLength = sUsername.length();
  int nMinLength = 3;
  if ((nLength == 0) || (nLength < nMinLength)) {
    sMsg = "The username is less than the minimum length.";
    throw new UsernamePolicyException(sMsg);
  }
  if (aRestricted != null) {
    for (int i=0;i<aRestricted.length;i++) {
      if (sUsername.indexOf(aRestricted[i]) != -1) {
        sMsg = "The username contains a restricted character.";
        throw new UsernamePolicyException(sMsg);
      }
    }
  }
}

}
