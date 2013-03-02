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
import com.esri.gpt.framework.util.Val;

/**
 * Stores information related to user feedback about the site.
 */
public class FeedbackMessage {
  
// class variables =============================================================

// instance variables ==========================================================
private String _body = "";
private String _fromAddress = "";
private String _fromName = "";
private int    _maxBodyLength = 10000;
  
/** Default constructor. */
public FeedbackMessage() {}

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
  if ((_maxBodyLength > 0) && (_body.length() > _maxBodyLength)) {
    _body = _body.substring(0,_maxBodyLength);
  }
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
 * Gets the name of the person sending feedback.
 * @return the name
 */
public String getFromName() {
  return _fromName;
}
/**
 * Sets the name of the person sending feedback.
 * @param name the name
 */
public void setFromName(String name) {
  _fromName = Val.chkStr(name);
}

/**
 * Gets the maximum length of the message body.
 * <br/>0 is unlimited, default is 10000.
 * @return the maximum length of the message body
 */
protected int getMaximumBodyLength() {
 return _maxBodyLength;
}
/**
 * Sets the maximum length of the message body.
 * <br/>0 is unlimited, default is 10000.
 * @param maximumLength the maximum length of the message body
 */
protected void setMaximumBodyLength(int maximumLength) {
  _maxBodyLength = maximumLength;
}

// methods =====================================================================

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  sb.append(" fromName=").append(getFromName()).append("\n");
  sb.append(" fromAddress=").append(getFromAddress()).append("\n");
  sb.append(" maximumBodyLength=").append(getMaximumBodyLength()).append("\n");
  sb.append(" body=").append(getBody()).append("\n");
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}
