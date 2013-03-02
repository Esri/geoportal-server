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
import com.esri.gpt.framework.util.Val;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A simple client for sending SMTP based E-Mail messages.
 */
public class MailRequest {
  
// class variables =============================================================
  
/** Mime type - HTML, "text/html; charset=UTF-8" */
public static final String MIMETYPE_HTML = "text/html; charset=UTF-8";

/** Mime type - plain, "text/plain; charset=UTF-8"  */
public static final String MIMETYPE_PLAIN = "text/plain; charset=UTF-8";
  
// instance variables ==========================================================
private Authenticator _authenticator;
private String        _body = "";
private String        _fromAddress = "";
private String        _host = "";
private String        _mimeType = MIMETYPE_PLAIN;
private int           _port = -1;
private StringSet     _recipients;
private String        _subject = "";
private String        _toAddress = "";
  
//constructors ================================================================

/** Default constructor. */
public MailRequest() {
  setRecipients(new StringSet());
}

/**
 * Constructs with an authenticator if crendentials are required by the mail server.
 * @param authenticator the authenticator
 */
public MailRequest(Authenticator authenticator) {
  _authenticator = authenticator;
  setRecipients(new StringSet());
}

// properties ==================================================================

/**
 * Gets the body.
 * @return the body
 */
public String getBody() {
  return _body;
}
/**
 * Sets the body.
 * @param body the body
 */
public void setBody(String body) {
  _body = Val.chkStr(body);
}

/**
 * Gets the from E-Mail address.
 * @return the from address
 */
public String getFromAddress() {
  return _fromAddress;
}
/**
 * Sets the from E-Mail address.
 * @param address the from address
 */
public void setFromAddress(String address) {
  _fromAddress = Val.chkStr(address);
}

/**
 * Gets the host.
 * @return the host
 */
public String getHost() {
  return _host;
}
/**
 * Sets the host.
 * @param host the host
 */
public void setHost(String host) {
  _host = Val.chkStr(host);
}

/**
 * Gets the Mime type.
 * @return the Mime type
 */
public String getMimeType() {
  return _mimeType;
}
/**
 * Sets the Mime type to text/html.
 */
public void setMimeTypeHtml() {
  _mimeType = MIMETYPE_HTML;
}
/**
 * Sets the Mime type to text/plain.
 */
public void setMimeTypePlain() {
  _mimeType = MIMETYPE_PLAIN;
}

/**
 * Gets the port.
 * @return the port
 */
public int getPort() {
  return _port;
}
/**
 * Sets the port.
 * @param port the port
 */
public void setPort(int port) {
  _port = port;
}

/**
 * Gets the recipients.
 * @return the recipients
 */
public StringSet getRecipients() {
  return _recipients;
}
/**
 * Sets the recipients.
 * @param recipients the recipients
 */
private void setRecipients(StringSet recipients) {
  _recipients = recipients;
}

/**
 * Gets the subject.
 * @return the subject
 */
public String getSubject() {
  return _subject;
}
/**
 * Sets the subject.
 * @param subject the subject
 */
public void setSubject(String subject) {
  _subject = Val.chkStr(subject);
}

/**
 * Gets the to E-Mail address.
 * @return the to address
 */
public String getToAddress() {
  return _toAddress;
}
/**
 * Sets the to E-Mail address.
 * <br/>The address is used to immediately set the recipients. The address 
 * string is tokenized with delimiters: 
 * <br/> semi-colon comma space
 * @param address the to address
 */
public void setToAddress(String address) {
  _toAddress = Val.chkStr(address);
  getRecipients().clear();
  getRecipients().addDelimited(getToAddress());
}
  
// methods =====================================================================

/**
 * Makes an Internet E-Mail address.
 * @param address the E-Mail address string
 * @return the Internet address
 * @throws AddressException if the E-Mail address is invalid
 */
private InternetAddress makeAddress(String address) 
  throws AddressException {
  return new InternetAddress(address);
}

/**
 * Sends the E-Mail message.
 * @throws AddressException if an E-Mail address is invalid
 * @throws MessagingException if an exception occurs
 */
public void send() throws AddressException, MessagingException { 
  
  // setup the mail server properties
  Properties props = new Properties();
  props.put("mail.smtp.host",getHost());
  if (getPort() > 0) {
    props.put("mail.smtp.port",""+getPort());
  }
  
  // set up the message
  Session session = Session.getDefaultInstance(props,_authenticator);
  Message message = new MimeMessage(session);
  message.setSubject(getSubject());
  message.setContent(getBody(),getMimeType());
  message.setFrom(makeAddress(getFromAddress()));
  for (String sTo: getRecipients()) {
    message.addRecipient(Message.RecipientType.TO,makeAddress(sTo));
  }

  // send the message
  Transport.send(message);
}

}
