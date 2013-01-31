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
package com.esri.gpt.framework.http;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Represents an exception throws when a a remote client requests authorization credentials. 
 */
public class HttpClient401Exception extends HttpClientException {
  
  /** instance variables ====================================================== */
  private String scheme;
  private String realm;
  private String url;
  
  /** constructors ============================================================ */
  
  /**
   * Construct with a supplied exception  message.
   * @param msg the exception message
   */
  public HttpClient401Exception(String msg) {
    super(401,msg);
  }
  
  /** properties  ============================================================= */
  
  /**
   * Gets the authentication realm associated with the request that failed.
   * @return the authentication realm
   */
  public String getRealm() {
    return this.realm;
  }
  /**
   * Sets the authentication realm associated with the request that failed.
   * @param realm the authentication realm
   */
  public void setRealm(String realm) {
    this.realm = realm;
  }
  
  /**
   * Gets the authentication scheme associated with the request that failed.
   * @return the authentication scheme
   */
  public String getScheme() {
    return this.scheme;
  }
  /**
   * Sets the authentication scheme associated with the request that failed.
   * @param scheme the authentication scheme
   */
  public void setScheme(String scheme) {
    this.scheme = scheme;
  }
  
  /**
   * Gets the URL associated with the request that failed.
   * @return the request URL
   */
  public String getUrl() {
    return this.url;
  }
  /**
   * Sets the URL associated with the request that failed.
   * @param url the request URL
   */
  public void setUrl(String url) {
    this.url = url;
  }
  
  /** methods ================================================================= */
  
  /**
   * Creates a realm based upon the host:port for the underlying URL.
   * @return the generated realm
   */
  public String generateHostBasedRealm() {
    String rlm = null;
    if (this.url != null) {
      try {
        URL tmp = new URL(this.url);
        //rlm = tmp.getProtocol()+"://"+tmp.getHost();
        rlm = tmp.getHost();
        if (tmp.getPort() >= 0) rlm += ":"+tmp.getPort();
      } catch (MalformedURLException e) {
        // ignore
      }
    }
    return rlm;
  }

}
