/*
 * Copyright 2011 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.client.waf;

import com.esri.gpt.framework.http.CredentialProvider;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;

/**
 * FTP request.
 */
class FtpClientRequest {

  private static final Logger LOG = Logger.getLogger(FtpClientRequest.class.getCanonicalName());
  private static final int DEFAULT_NO_OF_ATTEMPTS = 5;
  private FTPClient client = new FTPClient();
  private String protocol;
  private String host;
  private int port;
  private String root;
  private CredentialProvider cp;
  private int noOfAttempts = DEFAULT_NO_OF_ATTEMPTS;
  private boolean aborted;

  /**
   * Creates instance of the request.
   * @param url url
   * @param cp credential provider
   */
  public FtpClientRequest(URL url, CredentialProvider cp) {
    this.protocol = Val.chkStr(url.getProtocol());
    this.host = Val.chkStr(url.getHost());
    this.port = url.getPort() >= 0 ? url.getPort() : 21;
    this.root = Val.chkStr(url.getPath()).replaceAll("/$", "");
    this.cp = cp;
  }

  /**
   * Gets number of attempts.
   * @return number of attempts
   */
  public int getNoOfAttempts() {
    return noOfAttempts;
  }

  /**
   * Sets number of attempts.
   * @param noOfAttempts number of attempts
   */
  public void setNoOfAttempts(int noOfAttempts) {
    this.noOfAttempts = noOfAttempts;
  }

  /**
   * Gets server.
   * @return server
   */
  public String getServer() {
    return protocol + "://" + host + (port != 21 ? ":" + port : "");
  }

  /**
   * Gets folder.
   * @return folder
   */
  public String getRootFolder() {
    return root;
  }

  /**
   * Checks if connected.
   * @return <code>true</code> if connected
   */
  public boolean isConnected() {
    return client.isConnected();
  }

  /**
   * Checks if aborted.
   * @return <code>true</code> if aborted
   */
  public boolean isAborted() {
    return aborted;
  }

  /**
   * Connects to the server.
   * @throws IOException if connecting fails
   */
  public void connect() throws IOException {
    aborted = false;
    try {
      LOG.log(Level.INFO, "Connecting to: {0}", host);
      client.connect(host, port);
      if (cp == null) {
        client.login("anonymous", "anonymous");
      } else {
        client.login(cp.getUsername(), cp.getPassword());
      }
      client.setKeepAlive(true);
      client.enterLocalPassiveMode();
    } catch (IOException ex) {
      aborted = true;
      throw ex;
    }
  }

  /**
   * Disconnects from the server.
   */
  public void disconnect() {
    aborted = false;
    LOG.log(Level.INFO, "Disconnecting from: {0}", host);
    try {
      client.logout();
    } catch (Exception ex) {
      LOG.log(Level.FINE, "Error disconnecting from the host: " + host, ex);
    }
    try {
      client.disconnect();
    } catch (Exception ex) {
      LOG.log(Level.FINE, "Error disconnecting from the host: " + host, ex);
    }
  }

  public FTPFile[] listFiles(String pathName) throws IOException {
    try {
      LOG.log(Level.FINE, "Listing files on: {0} from the path: {1}", new Object[]{host, pathName});
      return listFiles(1, pathName);
    } catch (IOException ex) {
      aborted = true;
      throw ex;
    }
  }

  protected FTPFile[] listFiles(int attempt, String pathName) throws IOException {
    try {
      stayOpen();
      return client.listFiles(pathName);
    } catch (SocketException ex) {
      if (attempt < getNoOfAttempts()) {
        LOG.log(Level.INFO, "Listing files [attempt: {2}] on: {0} from the path: {1}", new Object[]{host, pathName, attempt + 1});
        reConnect();
        return listFiles(attempt + 1, pathName);
      } else {
        throw ex;
      }
    } catch (FTPConnectionClosedException ex) {
      if (attempt < getNoOfAttempts()) {
        LOG.log(Level.INFO, "Listing files [attempt: {2}] on: {0} from the path: {1}", new Object[]{host, pathName, attempt + 1});
        reConnect();
        return listFiles(attempt + 1, pathName);
      } else {
        throw ex;
      }
    }
  }

  public String readTextFile(String pathName) throws IOException {
    try {
      LOG.log(Level.FINE, "Reading text file on: {0} from the path: {1}", new Object[]{host, pathName});
      return readTextFile(1, pathName);
    } catch (IOException ex) {
      aborted = true;
      throw ex;
    }
  }

  protected String readTextFile(int attempt, String pathName) throws IOException {
    InputStream input = null;
    Reader reader = null;
    try {
      stayOpen();
      StringBuilder sb = new StringBuilder();
      input = client.retrieveFileStream(pathName);
      if (input != null) {
        reader = new InputStreamReader(input, "UTF-8");
        char[] buff = new char[1000];
        int length = 0;
        while ((length = reader.read(buff)) >= 0) {
          sb.append(buff, 0, length);
        }
      }
      return sb.toString();
    } catch (SocketException ex) {
      if (attempt < getNoOfAttempts()) {
        LOG.log(Level.INFO, "Reading text file [attempt: {2}] on: {0} from the path: {1}", new Object[]{host, pathName, attempt + 1});
        reConnect();
        return readTextFile(attempt + 1, pathName);
      } else {
        throw ex;
      }
    } catch (FTPConnectionClosedException ex) {
      if (attempt < getNoOfAttempts()) {
        LOG.log(Level.INFO, "Reading text file [attempt: {2}] on: {0} from the path: {1}", new Object[]{host, pathName, attempt + 1});
        reConnect();
        return readTextFile(attempt + 1, pathName);
      } else {
        throw ex;
      }
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
          LOG.log(Level.FINE, "Error closing reader on: " + host, ex);
        }
      }
      if (input != null) {
        try {
          input.close();
        } catch (IOException ex) {
          LOG.log(Level.FINE, "Error closing input on: " + host, ex);
        }
      }
      try {
        client.completePendingCommand();
      } catch (IOException ex) {
        LOG.log(Level.FINE, "Error completing pending command: " + host, ex);
      }
    }
  }

  protected void reConnect() throws IOException {
    disconnect();
    connect();
  }

  protected void stayOpen() throws IOException {
    if (!isConnected()) {
      connect();
    }
  }
}
