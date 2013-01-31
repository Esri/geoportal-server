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
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidResponseException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidUrlException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRTimeoutException;
import com.esri.gpt.framework.http.ByteArrayHandler;
import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.http.StringProvider;
import com.esri.gpt.framework.util.Val;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Protected HTTP client.
 */
public abstract class HRProtectedHttpClient extends HRHttpClient {

// instance variables ==========================================================

/** user name */
private String _userName = "";
/** user password */
private String _userPassword = "";

// properties ==================================================================

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

// methods ==================================================================

/**
 * Creates HTTP connection.
 * @param connectionString connection string
 * @return input stream to read response
 * @throws HRConnectionException if unable to open connection
 */
  @Override
protected InputStream openConnection(String connectionString)
  throws HRConnectionException {
  try {

    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(connectionString);
    ByteArrayHandler sh = new ByteArrayHandler();
    cr.setContentHandler(sh);
    CredentialProvider cp = getUserName().length()>0 && getUserPassword().length()>0?
      new CredentialProvider(getUserName(),getUserPassword()):
      CredentialProvider.getThreadLocalInstance();
    cr.setCredentialProvider(cp);
    cr.execute();

    return new ByteArrayInputStream(sh.getContent());

  } catch (MalformedURLException ex) {
    throw new HRInvalidUrlException("Invalid URL: " + getHostUrl(), ex);
  } catch (UnknownHostException ex) {
    throw new HRInvalidUrlException("Invalid URL: " + getHostUrl(), ex);
  } catch (SocketTimeoutException ex) {
    throw new HRTimeoutException(
      "Timeout of " + getTimeout() + " milliseconds exceeded.", ex);
  } catch (IOException ex) {
    throw new HRInvalidResponseException("Error reading response.", ex);
  }
}


/**
 * Creates http connection.
 * @param connectionString connection string
 * @param initString text of request to be sent uppon connection
 * @return input stream to read response
 * @throws HRConnectionException if unable to open connection
 */
  @Override
protected InputStream openConnection(String connectionString, String initString)
  throws HRConnectionException {

  initString = Val.chkStr(initString);

  try {

    HttpClientRequest cr = new HttpClientRequest();
    cr.setUrl(connectionString);
    ByteArrayHandler sh = new ByteArrayHandler();
    StringProvider cprov = new StringProvider(initString,"text/plain");
    cr.setContentHandler(sh);
    cr.setContentProvider(cprov);
    CredentialProvider cp = getUserName().length()>0 && getUserPassword().length()>0?
      new CredentialProvider(getUserName(),getUserPassword()):
      CredentialProvider.getThreadLocalInstance();
    cr.setCredentialProvider(cp);
    cr.execute();

    return new ByteArrayInputStream(sh.getContent());

  } catch (MalformedURLException ex) {
    throw new HRInvalidUrlException("Invalid URL: " + getHostUrl(), ex);
  } catch (SocketTimeoutException ex) {
    throw new HRTimeoutException(
      "Timeout of " + getTimeout() + " milliseconds exceeded.", ex);
  } catch (ConnectException ex) {
    throw new HRInvalidUrlException("Invalid URL: " + getHostUrl(), ex);
  } catch (IOException ex) {
    throw new HRInvalidResponseException("Error reading response.", ex);
  }
}


}
