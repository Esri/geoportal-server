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
package com.esri.gpt.catalog.harvest.clients;

import com.esri.gpt.catalog.harvest.clients.exceptions.HRConnectionException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidResponseException;
import com.esri.gpt.framework.util.Val;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Harvest repository WAF client.
 * Provides complete implementation of the client cappable to access to
 * <b>W</b>eb <b>A</b>ccessible <b>F</b>olders.
 */
public class HRWAFClient extends HRProtectedHttpClient {

// class variables =============================================================

// instance variables ==========================================================
/** host URL */
private String _hostUrl = "";
// constructors ================================================================
/**
 * Creates instance of the client.
 * @param hostUrl host sUrl
 * @param userName user name
 * @param userPassword user password
 */
public HRWAFClient(String hostUrl, String userName, String userPassword) {
  setHostUrl(hostUrl);
  setUserName(userName);
  setUserPassword(userPassword);
}
// properties ==================================================================

/**
 * Gets host URL.
 * @return host URL
 */
@Override
public String getHostUrl() {
  return _hostUrl;
}

/**
 * Sets host URL.
 * @param hostUrl host URL
 */
public void setHostUrl(String hostUrl) {
  _hostUrl = Val.chkStr(hostUrl);
}
// methods =====================================================================
/**
 * Checks and verifies connection to the remote repository.
 * @throws HRInvalidProtocolException if provided connection 
 * definition is incomplete
 * @throws HRConnectionException if connection to the remote repository can not
 * be established at this moment
 */
@Override
public void ping()
  throws HRInvalidProtocolException, HRConnectionException {

  validateProtocol();
  ping(createPingString());
}

/**
 * Returns a string representation of the object.
 * @return string representation of the object
 */
@Override
public String toString() {
  return "WAF " + super.toString() +
    " USERNAME:" + getUserName() +
    " PASSWORD:" + getUserPassword();
}

/**
 * Validates protocol definition.
 * @throws HRInvalidProtocolException if invalid protocol definition
 */
@Override
protected void validateProtocol()
  throws HRInvalidProtocolException {
  super.validateProtocol();

  if (isFtp()) {
    if (getUserName().length() == 0 && getUserPassword().length() > 0) {
      throw new HRInvalidProtocolException(
        HRInvalidProtocolException.ProtocolElement.userName,
        "Empty user name.");
    }
    if (getUserName().length() > 0 && getUserPassword().length() == 0) {
      throw new HRInvalidProtocolException(
        HRInvalidProtocolException.ProtocolElement.userPassword,
        "Empty user password.");
    }
  }
}

/**
 * Checks and verifies connection to the remote repository.
 * @param pingString ping string
 * @throws HRInvalidProtocolException if provided connection 
 * definition is incomplete
 * @throws HRConnectionException if connection to the remote repository can not
 * be established at this moment
 */
private void ping(String pingString)
  throws HRInvalidProtocolException, HRConnectionException {

  InputStream input = null;

  try {

    input = openConnection(pingString);
    analyzePingResponse(input);

  } finally {
    if (input != null) {
      try {
        input.close();
      } catch (IOException ignore) {
      }
    }
  }
}

/**
 * Analyzes response from the ping request.
 * @param response response from the server
 * @throws HRInvalidResponseException if response is invalid
 */
private void analyzePingResponse(InputStream response)
  throws HRInvalidResponseException {
  try {
    BufferedReader reader = new BufferedReader(new InputStreamReader(response));
    reader.readLine();
    reader.close();
  } catch (IOException ex) {
    throw new HRInvalidResponseException("Invalid response.", ex);
  }
}

/**
 * Gets connection string
 * @return connection string
 */
private String createPingString() {

  // remove trailing / or \
  String url = getHostUrl();

  if (isFtp()) {
    if (getUserName().length() > 0 && getUserPassword().length() > 0) {
      url = FTP_URL_PFX +
        getUserName() +
        ":" +
        getUserPassword() +
        "@" +
        url.substring(FTP_URL_PFX.length());
    }
    url += "/";
  }

  return url;
}
}
