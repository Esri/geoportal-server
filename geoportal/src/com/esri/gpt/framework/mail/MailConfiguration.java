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
package com.esri.gpt.framework.mail;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.Configuration;
import com.esri.gpt.framework.util.Val;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * Mail configuration information.
 */
public class MailConfiguration extends Configuration {
  
// class variables =============================================================

// instance variables ==========================================================
private String                 _emailAddressRegexp = "";
private String                 _incomingToAddress = "";
private String                 _outgoingFromAddress = "";
private PasswordAuthentication _passwordAuthentication;
private String                 _smtpHost = "";
private int                    _smtpPort = -1;

/** Default constructor. */
public MailConfiguration() {}

// properties ==================================================================

/**
 * Gets the regular expression used to validate an E-Mail address.
 * @return the address validation regular expression
 */
public String getEmailAddressRegexp() {
  return _emailAddressRegexp;
}
/**
 * Sets the regular expression used to validate an E-Mail address.
 * @param regexp the address validation regular expression
 */
public void setEmailAddressRegexp(String regexp) {
  _emailAddressRegexp = Val.chkStr(regexp);
}

/**
 * Gets the to E-Mail address for incoming mail.
 * @return the to address
 */
public String getIncomingToAddress() {
  return _incomingToAddress;
}
/**
 * Sets the to E-Mail address for incoming mail.
 * @param incomingToAddress the to address for incoming mail
 */
public void setIncomingToAddress(String incomingToAddress) {
  _incomingToAddress = Val.chkStr(incomingToAddress);
}

/**
 * Gets the from E-Mail address for outgoing mail.
 * @return the from address
 */
public String getOutgoingFromAddress() {
  return _outgoingFromAddress;
}
/**
 * Sets the from E-Mail address for outgoing mail.
 * @param outgoingFromAddress the from address for outgoing mail
 */
public void setOutgoingFromAddress(String outgoingFromAddress) {
  _outgoingFromAddress = Val.chkStr(outgoingFromAddress);
}

/**
 * Gets the credentials used for password authentication (SMPT_AUTH).
 * @return the credentials user for password authentication
 */
public PasswordAuthentication getPasswordAuthentication() {
  return _passwordAuthentication;
}
/**
 * Sets the credentials used for password authentication (SMPT_AUTH).
 * @param credentials the credentials user for password authentication
 */
public void setPasswordAuthentication(PasswordAuthentication credentials) {
  _passwordAuthentication = credentials;
}

/**
 * Gets the SMTP host.
 * @return the SMTP host
 */
public String getSmtpHost() {
  return _smtpHost;
}
/**
 * Sets the SMPT host.
 * @param smtpHost the SMPT host
 */
public void setSmtpHost(String smtpHost) {
  _smtpHost = Val.chkStr(smtpHost);
}

/**
 * Gets the SMTP port.
 * @return the SMTP port
 */
public int getSmtpPort() {
  return _smtpPort;
}
/**
 * Sets the SMTP port.
 * @param smtpPort the SMTP port
 */
public void setSmtpPort(int smtpPort) {
  _smtpPort = smtpPort;
}

// methods =====================================================================

/**
 * Makes a password authenticator if required (if amptAuth was configured).
 * @return the password authenticator (can be null)
 */
private Authenticator makeAuthenticator() {
  if (getPasswordAuthentication() == null) {
    return null;
  } else {
    return new MailAuth(this.getPasswordAuthentication());
  }
}

/**
 * Instantiates a new in-bound mail request
 * <br/>The SMTP host and port are set, the to E-mail address is set.
 * @return the mail request
 */
public MailRequest newInboundRequest() {
  MailRequest request = new MailRequest(makeAuthenticator());
  request.setHost(getSmtpHost());
  request.setPort(getSmtpPort());
  request.setToAddress(getIncomingToAddress());
  return request;
}

/**
 * Instantiates a new out-bound mail request
 * <br/>The SMTP host and port are set, the to E-mail address is set.
 * @return the mail request
 */
public MailRequest newOutboundRequest() {
  MailRequest request = new MailRequest(makeAuthenticator());
  request.setHost(getSmtpHost());
  request.setPort(getSmtpPort());
  request.setFromAddress(getOutgoingFromAddress());
  return request;
}

/**
 * Puts configuration values.
 * @param smtpPort the SMPT port
 * @param smtpHost the SMPT host
 * @param outgoingFromAddress the from address for outgoing mail
 * @param incomingToAddress the to address for incoming mail
 */
public void put(String smtpHost, 
                String smtpPort, 
                String outgoingFromAddress,
                String incomingToAddress) {
  setSmtpHost(smtpHost);
  setSmtpPort(Val.chkInt(smtpPort,-1));
  
  // tokenize the addresses, try to ensure that valid values have been set
  StringSet ssFrom = new StringSet();
  StringSet ssTo = new StringSet();
  ssFrom.addDelimited(outgoingFromAddress);
  ssTo.addDelimited(incomingToAddress);
  if ((ssFrom.size() > 0) && (ssTo.size() > 0)) {
    setOutgoingFromAddress(ssFrom.iterator().next());
    setIncomingToAddress(incomingToAddress);
  } else if (ssFrom.size() > 0) {
    setOutgoingFromAddress(ssFrom.iterator().next());
    setIncomingToAddress(outgoingFromAddress);
  } else if (ssTo.size() > 0) {
    setOutgoingFromAddress(ssTo.iterator().next());
    setIncomingToAddress(incomingToAddress);
  }
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" smtpHost=").append(getSmtpHost()).append("\n");
  sb.append(" smtpPort=").append(getSmtpPort()).append("\n");
  sb.append(" outgoingFromAddress=").append(getOutgoingFromAddress()).append("\n");
  sb.append(" incomingToAddress=").append(getIncomingToAddress()).append("\n");
  sb.append(" emailAddressRegexp=").append(getEmailAddressRegexp()).append("\n");
  if (getPasswordAuthentication() != null) {
    sb.append(" smptAuth.username=").append(getPasswordAuthentication().getUserName()).append("\n");
    String sPwd = getPasswordAuthentication().getPassword();
    if (sPwd == null) sPwd = "";
    int nPwdLen = sPwd.length();
    sb.append(" smptAuth.password=");
    for (int i=0;i<nPwdLen;i++) sb.append("*");
    sb.append("\n");
  }
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}
