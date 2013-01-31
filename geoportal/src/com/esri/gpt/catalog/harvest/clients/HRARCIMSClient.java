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

import com.esri.gpt.catalog.arcims.ImsResponseException;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.arcims.TestConnectionRequest;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRConnectionException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidResponseException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidUrlException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRTimeoutException;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * ArcIMS repository client.
 * Provides complete functionality required to establish connection with ArcIMS
 * server.
 * @see <a href="http://edndoc.esri.com/arcims/9.2/">
 * ArcIMS 9.2 documentation</a>
 */
public class HRARCIMSClient extends HRHttpClient {

// class variables =============================================================
/** Default port number. */
public static final int DEFAULT_PORT_NO = 80;

// instance variables ==========================================================
/** host URL */
private String _hostUrl = "";
/** port number */
private int _portNo = 0;
/** user name */
private String _userName = "";
/** user password */
private String _userPassword = "";
/** service name */
private String _serviceName = "";
/** root folder */
private String _rootFolder = "";
// constructors ================================================================
/**
 * Creates instance of the client.
 * @param hostUrl host url
 * @param portNo port number
 * @param serviceName service name
 * @param userName user name (optional)
 * @param userPassword user password (optional)
 * @param rootFolder root folder  (optional)
 */
public HRARCIMSClient(
  String hostUrl, int portNo, String serviceName, String userName, 
  String userPassword, String rootFolder) {
  setHostUrl(hostUrl);
  setPortNo(portNo);
  setServiceName(serviceName);
  setUserName(userName);
  setUserPassword(userPassword);
  setRootFolder(rootFolder);
}

// properties ==================================================================
/**
 * Gets connection port number.
 * @return port number
 */
public int getPortNo() {
  return _portNo;
}

/**
 * Sets port number.
 * @param portNo port number
 */
public void setPortNo(int portNo) {
  _portNo = portNo >= 0 && portNo < 65536 ? portNo : DEFAULT_PORT_NO;
}

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

/**
 * Gets user name required to make connection.
 * @return user name required to make connection
 */
public String getUserName() {
  return _userName;
}

/**
 * Sets user name required to make connection.
 * @param userName user name required to make connection
 */
public void setUserName(String userName) {
  _userName = Val.chkStr(userName);
}

/**
 * Gets user password required to make connection.
 * @return user password required to make connection
 */
public String getUserPassword() {
  return _userPassword;
}

/**
 * Sets user password required to make connection.
 * @param userPassword user password required to make connection
 */
public void setUserPassword(String userPassword) {
  _userPassword = Val.chkStr(userPassword);
}

/**
 * Gets service name.
 * @return service name
 */
public String getServiceName() {
  return _serviceName;
}

/**
 * Sets service name.
 * @param serviceName service name
 */
public void setServiceName(String serviceName) {
  _serviceName = Val.chkStr(serviceName);
}

/**
 * Gets root folder.
 * @return root folder
 */
public String getRootFolder() {
  return _rootFolder;
}

/**
 * Sets root folder.
 * @param rootFolder root folder
 */
public void setRootFolder(String rootFolder) {
  _rootFolder = Val.chkStr(rootFolder);
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

  try {
    TestConnectionRequest tchRequest = new TestConnectionRequest(
      getCredentials(), getFullUrl(), getTimeout(), getServiceName());
    if (!tchRequest.testConnection()) {
      throw new HRInvalidProtocolException(
        HRInvalidProtocolException.ProtocolElement.serviceName,
        "Requested service unaccessible."
        );
    }
  } catch (ImsResponseException ex) {
    throw new HRInvalidResponseException(
      "Invalid response received from the host", ex);
  } catch (ImsServiceException ex) {
    handleImsServiceException(ex);
  }
}

/**
 * Returns a string representation of the object.
 * @return string representation of the object
 */
@Override
public String toString() {
  return "ARCIMS " + super.toString() +
    " PORT:" + getPortNo() +
    " USERNAME:" + getUserName() +
    " PASSWORD:" + getUserPassword() +
    " SERVICE:" + getServiceName() +
    " FOLDER:" + getRootFolder();
}

/**
 * Validates protocol definition.
 * @throws HRInvalidProtocolException if invalid protocol definition
 */
@Override
protected void validateProtocol()
  throws HRInvalidProtocolException {
  if (!isHttp() && !isFtp() && !isHttps()) {
    throw new HRInvalidProtocolException(
      HRInvalidProtocolException.ProtocolElement.url,
      "Neither HTTP, nor HTTPS, nor FTP protocol.");
  }
  if (getPortNo() < 0 || getPortNo() >= 65536) {
    throw new HRInvalidProtocolException(
      HRInvalidProtocolException.ProtocolElement.portNo,
      "Invalid port number: " + getPortNo());
  }
  if (getServiceName().length() == 0) {
    throw new HRInvalidProtocolException(
      HRInvalidProtocolException.ProtocolElement.serviceName,
      "Empty service name.");
  }
}

/**
 * Gets user credentials.
 * @return user credentials
 */
private UsernamePasswordCredentials getCredentials() {
  UsernamePasswordCredentials credentials =
    new UsernamePasswordCredentials();

  if (getUserName().length() > 0 && getUserPassword().length() > 0) {
    credentials.setUsername(getUserName());
    credentials.setPassword(getUserPassword());
  }

  return credentials;
}

/**
 * Handles ImsServiceException
 * @param ex {@link com.esri.gpt.catalog.arcims.ImsServiceException} to 
 * translate
 * @throws HRConnectionException exception into which ImsServiceException has 
 * been translated
 */
private void handleImsServiceException(ImsServiceException ex)
  throws HRConnectionException {

  if (ex.getCause() != null) {
    if (ex.getCause() instanceof MalformedURLException) {
      throw new HRInvalidUrlException(
        "Invalid URL: " + getFullUrl(), ex.getCause());
    }
    if (ex.getCause() instanceof UnknownHostException) {
      throw new HRInvalidUrlException(
        "Invalid URL: " + getFullUrl(), ex.getCause());
    }
    if (ex.getCause() instanceof SocketTimeoutException) {
      throw new HRTimeoutException(
        "Timeout of " + getTimeout() + " milliseconds exceeded.", ex);
    }
  }
  throw new HRConnectionException(
    "Error connection to the harvest repository.", ex);
}

/**
 * Gets full url.
 * @return full url
 */
private String getFullUrl() {
  // remove trailing / or \
  String fullUrl =
    Val.chkStr(getHostUrl()).replaceAll("[/\\\\]\\p{Blank}*$", "");

  if (getPortNo() != DEFAULT_PORT_NO) {
    fullUrl = fullUrl.replaceAll(":\\p{Digit}*$", "");
    fullUrl = fullUrl + ":" + getPortNo();
  }

  return fullUrl;
}
}
