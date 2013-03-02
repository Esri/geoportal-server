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
package com.esri.gpt.catalog.arcims;
import com.esri.gpt.framework.security.codec.Base64;
import com.esri.gpt.framework.security.codec.Digest;
import com.esri.gpt.framework.security.credentials.Credentials;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.Val;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a basic client for out-bound ArcIMS HTTP communication.
 *
 */
public class ImsHttpClient extends ImsClient {

// class variables =============================================================
private static Logger LOGGER = Logger.getLogger(ImsHttpClient.class.getName());

// instance variables ==========================================================
private Credentials _credentials = null;
private String      _responseContentEncoding = "";
private long        _responseContentLength = 0;
private String      _responseContentType = "";
private String      _url = "";

// constructors ================================================================

/** Default constructor. */
public ImsHttpClient() {
  super();
}

// properties ==================================================================
/**
 * Gets the credentials for the user.
 * @return the credentials
 */
public Credentials getCredentials() {
  return _credentials;
}

/**
 * Sets the credentials for the user.
 * @param credentials the credentials
 */
public void setCredentials(Credentials credentials) {
  _credentials = credentials;
}

/**
 * Gets the response content encoding.
 * @return the response content encoding
 */
public String getResponseContentEncoding() {
  return _responseContentEncoding;
}

/**
 * Sets the response content encoding.
 * @param encoding the response content encoding
 */
protected void setResponseContentEncoding(String encoding) {
  _responseContentEncoding = Val.chkStr(encoding);
}

/**
 * Gets the response content length.
 * @return the response content length
 */
public long getResponseContentLength() {
  return _responseContentLength;
}

/**
 * Sets the response content length.
 * @param length the response content length
 */
protected void setResponseContentLength(long length) {
  _responseContentLength = length;
}

/**
 * Gets the response content type.
 * @return the response content type
 */
public String getResponseContentType() {
  return _responseContentType;
}

/**
 * Sets the response content type.
 * @param type the response content type
 */
protected void setResponseContentType(String type) {
  _responseContentType = Val.chkStr(type);
}

/**
 * Gets the server url.
 * @return the server url
 */
public String getUrl() {
  return _url;
}

/**
 * Sets the server url.
 * @param url the server url
 */
public void setUrl(String url) {
  _url = Val.chkStr(url);
}

// methods =====================================================================
/**
 * Executes the sending of the HTTP request.
 * @throws MalformedURLException if the URL was not properly specified
 * @throws IOException if a communication exception occurs
 */
private void executeSend()
  throws MalformedURLException, IOException {
  resetResponse();
  HttpURLConnection httpCon = null;
  InputStream responseStream = null;
  
  if (LOGGER.isLoggable(Level.FINER)) {
    StringBuffer sb = new StringBuffer();
    sb.append("Sending ArcIMS HTTP request\n");
    sb.append(" url=").append(getUrl());
    sb.append("\n").append(getAxlRequest());
    LOGGER.finer(sb.toString());
  }

  try {
    
    // set-up the connection
    String sMethod = "GET";
    boolean bSendData = false;
    if (getAxlRequest().length() > 0) {
      sMethod = "POST";
      bSendData = true;
    }

//    if (getUrl().startsWith("https://")) {
//      boolean allowSetDefault = true;
//      // check security manager first for net permission to perform 'setDefault'
//      try {
//        SecurityManager security = System.getSecurityManager();
//        if (security!=null) {
//          security.checkPermission(
//            new java.net.NetPermission("setDefaultAuthenticator"));
//        }
//      } catch (SecurityException ex) {
//        allowSetDefault = false;
//      }
//      // perform 'setDefault' if allowed
//      if (allowSetDefault) {
//        if (getCredentials() instanceof UsernamePasswordCredentials) {
//          final UsernamePasswordCredentials credentials =
//            (UsernamePasswordCredentials)getCredentials();
//          Authenticator.setDefault(
//              new Authenticator() {
//              @Override
//                protected PasswordAuthentication getPasswordAuthentication() {
//                  return new PasswordAuthentication(
//                    credentials.getUsername(), credentials.getPassword().toCharArray());
//                }
//          });
//        }
//      }
//    }
    
    // open connection
    httpCon = openConnection(sMethod, bSendData);

    // setup basic credentials
    setupBasicCredentials(getCredentials(), httpCon);

    // send data if required
    if (bSendData) {
      sendData(httpCon);
    }

    // read the response
    setResponseCode(httpCon.getResponseCode());
    setResponseCodeText(httpCon.getResponseMessage());

    // not authenticated; setup digest authentication and resend request
    if (getResponseCode() == 401) {
      Digest digResp = Digest.extractFrom(httpCon);
      if (digResp.isValid()) {

        // reconnect
        httpCon.disconnect();
        httpCon = openConnection(sMethod, bSendData);
        // setup credentials
        digResp.injectTo(httpCon,(UsernamePasswordCredentials)getCredentials());
        // send data if required
        if (bSendData) {
          sendData(httpCon);
        }

        // read the response
        setResponseCode(httpCon.getResponseCode());
        setResponseCodeText(httpCon.getResponseMessage());
      }
    }

    if ((getResponseCode() < 200) || (getResponseCode() > 299)) {
      throw new IOException("Request failed: HTTP " + getResponseCode());
    }
    
    setResponseContentEncoding(httpCon.getContentEncoding());
    setResponseContentType(httpCon.getContentType());
    setResponseContentLength(httpCon.getContentLength());
    responseStream = httpCon.getInputStream();
    setAxlResponse(readCharacters(responseStream).toString());
    
    if (LOGGER.isLoggable(Level.FINER)) {
      StringBuffer sb = new StringBuffer();
      sb.append("Read ArcIMS HTTP response\n");
      sb.append(" url=").append(getUrl());
      sb.append(" responseCode=").append(getResponseCode());
      sb.append(" responseContentType=").append(getResponseContentType());
      sb.append(" responseContentEncoding=").append(getResponseContentEncoding());
      sb.append("\n").append(getAxlResponse());
      LOGGER.finer(sb.toString());
    }

  } finally {
    try {
      if (responseStream != null) responseStream.close();
    } catch (Exception ef) {}
  }
}

/**
 * Creates an open HTTP connection.
 * @param sMethod method ("GET" or "POST")
 * @param bSendData <code>true</code> if any data to send
 * @return connection
 * @throws java.net.MalformedURLException if invalid URL
 * @throws java.io.IOException if error opening connection
 */
private HttpURLConnection openConnection(String sMethod, boolean bSendData)
  throws MalformedURLException, IOException {
  URL url = new URL(getUrl());
  HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
  httpCon.setRequestMethod(sMethod);
  httpCon.setConnectTimeout(getTimeoutMillisecs());
  httpCon.setDoInput(true);
  httpCon.setDoOutput(bSendData);
  httpCon.setUseCaches(false); // turn off document caching
  httpCon.setRequestProperty("Connection", "Close"); // Disable keep-alive
  return httpCon;
}

/**
 * Sends request to the opened HTTP connection.
 * @param httpCon HTTP connection
 * @throws java.io.IOException if sending data failed
 */
private void sendData(HttpURLConnection httpCon)
  throws IOException {
  OutputStream sendStream = null;
  try {
    httpCon.setRequestProperty("Content-Type","text/xml; charset=UTF-8");
    httpCon.setRequestProperty("Content-Length",""+getAxlRequest().length());
    sendStream = httpCon.getOutputStream();
    sendStream.write(getAxlRequest().getBytes("UTF-8"));
    sendStream.flush();
  } finally {
    try {
      if (sendStream != null) {
        sendStream.close();
      }
    } catch (Exception ef) {
    }
  }
}

/**
 * Fully reads the characters from an InputStream.
 * @param strm the InputStream
 * @return the characters read
 * @throws IOException if an exception occurs
 */
protected StringBuffer readCharacters(InputStream strm)
  throws IOException {
  StringBuffer sb = new StringBuffer();
  BufferedReader br = null;
  InputStreamReader ir = null;
  try {
    char cbuf[] = new char[2048];
    int n = 0;
    int nLen = cbuf.length;
    ir = new InputStreamReader(strm, "UTF-8");
    br = new BufferedReader(ir);
    while ((n = br.read(cbuf, 0, nLen)) > 0) {
      sb.append(cbuf, 0, n);
    }
  } finally {
    try {if (br != null) br.close();} catch (Exception ef) {}
    try {if (ir != null) ir.close();} catch (Exception ef) {}
  }
  return sb;
}

/**
 * Resets the response.
 */
protected void resetResponse() {
  setAxlResponse("");
  setResponseCode(0);
  setResponseCodeText("");
  setResponseContentEncoding("");
  setResponseContentLength(0);
  setResponseContentType("");
}

/**
 * Sends the HTTP request.
 * @throws MalformedURLException if the URL was not properly specified
 * @throws IOException if a communication exception occurs
 */
public void sendRequest()
  throws MalformedURLException, IOException {
  executeSend();
}

/**
 * Sets up HTTP header basic authorization.
 * @param credentials the associated credentials
 * @param urlCon the URLConnection
 */
private void setupBasicCredentials(Credentials credentials,
                                    URLConnection urlCon) {

  // Currently we are only handling Basic scheme username/password encoding
  if ((credentials != null) && (credentials instanceof UsernamePasswordCredentials)) {
    UsernamePasswordCredentials upCredentials =
      (UsernamePasswordCredentials) credentials;
    String sUsername = upCredentials.getUsername();
    String sPassword = upCredentials.getPassword();
    if ((sUsername.length() > 0) || (sPassword.length() > 0)) {
      String sUserPwd = sUsername + ":" + sPassword;
      String sEncodedUserPwd = "";
      try {
        sEncodedUserPwd = Base64.encode(sUserPwd, null);
      } catch (UnsupportedEncodingException eun) {
      }
      urlCon.setRequestProperty("Authorization", "Basic " + sEncodedUserPwd);
    }
  }
}
}
