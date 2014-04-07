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
import com.esri.gpt.catalog.harvest.clients.exceptions.HRTimeoutException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidResponseException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidUrlException;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

/**
 * Harvest repository HTTP client.
 * Provides implementation usefull when connecting to the remote repositories
 * utilizing HTTP protocol.
 */
public abstract class HRHttpClient extends HRAbstractClient {

// class variables =============================================================
  
/** http protocol prefix */
protected static final String HTTP_URL_PFX = "http://";
/** https protocol prefix */
protected static final String HTTPS_URL_PFX = "https://";
/** ftp protocol prefix */
protected static final String FTP_URL_PFX = "ftp://";

// instance variables ==========================================================

// constructors ================================================================

// properties ==================================================================
/**
 * Checks if protocol is a HTTP protocol.
 * @return <code>true</code> if protocol is a HTTP protocol
 */
protected boolean isHttp() {
  return getHostUrl().startsWith(HTTP_URL_PFX);
}
/**
 * Checks if protocol is a HTTPS protocol.
 * @return <code>true</code> if protocol is a HTTPS protocol
 */
protected boolean isHttps() {
  return getHostUrl().startsWith(HTTPS_URL_PFX);
}

/**
 * Checks if protocol is a FTP protocol.
 * @return <code>true</code> if protocol is a FTP protocol
 */
protected boolean isFtp() {
  return getHostUrl().startsWith(FTP_URL_PFX);
}

// methods =====================================================================

/**
 * Validates protocol definition.
 * @throws HRInvalidProtocolException if invalid protocol definition
 */
@Override
protected void validateProtocol() throws HRInvalidProtocolException {
  super.validateProtocol();
  if (!isHttp() && !isFtp() && !isHttps()) {
    throw new HRInvalidProtocolException(
      HRInvalidProtocolException.ProtocolElement.url,
      "Neither HTTP nor FTP protocol.");
  }
}

/**
 * Creates HTTP connection.
 * @param connectionString connection string
 * @return input stream to read response
 * @throws HRConnectionException if unable to open connection
 */
protected InputStream openConnection(String connectionString)
  throws HRConnectionException {
  try {

    URL connUrl = new URL(connectionString);
    URLConnection conn = connUrl.openConnection();

    conn.setConnectTimeout(getTimeout());
    conn.connect();

    return conn.getInputStream();

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
protected InputStream openConnection(String connectionString, String initString)
  throws HRConnectionException {
  
  initString = Val.chkStr(initString);
  
  try {

    URL connUrl = new URL(connectionString);
    URLConnection conn = connUrl.openConnection();

    conn.setConnectTimeout(getTimeout());
    conn.setDoOutput(true);
    conn.connect();

    OutputStream output = conn.getOutputStream();
    output.write(initString.getBytes());
    output.flush();
    
    return conn.getInputStream();

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
