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
package com.esri.gpt.agp.client;

import com.esri.gpt.framework.util.Val;

/**
 * An ArcGIS Portal connection.
 */
public class AgpConnection {
  
  /** instance variables ====================================================== */
  private AgpClient        client;
  private AgpContext       context;
  private String           host;
  private int              port = 0;
  private String           webContext;
  private AgpToken         token;
  private AgpTokenCriteria tokenCriteria;
     
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpConnection() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the client.
   * @return the client
   */
  public AgpClient getClient() {
    return this.client;
  }
  /**
   * Sets the client.
   * @param client the client
   */
  public void setClient(AgpClient client) {
    this.client = client;
  } 
  
  /**
   * Gets the context.
   * @return the context
   */
  public AgpContext getContext() {
    return this.context;
  }
  /**
   * Sets the context.
   * @param context the context
   */
  public void setContext(AgpContext context) {
    this.context = context;
  }
  
  /**
   * Gets the host.
   * @return the host
   */
  public String getHost() {
    return this.host;
  }
  /**
   * Sets the host.
   * @param host the host
   */
  public void setHost(String host) {
    this.host = host;
  }

  /**
   * Gets a web context.
   * </p>
   * Web context (typically: "/arcgis") provides compatibility with 10.2 version
   * of Portal of ArcGIS.
   * @return web context or <code>null</code> if no web context
   */
  public String getWebContext() {
    return webContext;
  }

  /**
   * Sets web context.
   * </p>
   * If web context is an empty string it will be replaced with <code>null</code>.</br>
   * If web context is a non empty string it will be assured that it has "/" in front of it.
   * @param webContext web context
   */
  public void setWebContext(String webContext) {
    webContext = Val.chkStr(webContext);
    if (webContext.isEmpty()) {
      webContext = null;
    } else {
      webContext = webContext.replaceAll("^[/]+", "/");
    }
    this.webContext = webContext;
  }
  
  /**
   * Gets the port.
   * @return the port
   */
  public int getPort() {
    return this.port;
  }
  /**
   * Sets the port.
   * @param port the port
   */
  public void setPort(int port) {
    this.port = port;
  }
  
  /**
   * Gets the token.
   * @return the token
   */
  public AgpToken getToken() {
    return this.token;
  }
  /**
   * Sets the token.
   * @param token the token
   */
  public void setToken(AgpToken token) {
    this.token = token;
  }
  
  /**
   * Gets the criteria to use when generating a security token.
   * @return the token criteria
   */
  public AgpTokenCriteria getTokenCriteria() {
    return this.tokenCriteria;
  }
  /**
   * Sets the criteria to use when generating a security token.
   * @param tokenCriteria the token criteria
   */
  public void setTokenCriteria(AgpTokenCriteria tokenCriteria) {
    this.tokenCriteria = tokenCriteria;
  }
  
  /** methods ================================================================= */
  
  /**
   * Appends the token parameter to a URL buffer.
   * @param params the buffer
   */
  public void appendToken(StringBuilder params) {
    if (this.getToken() != null) {
      String sToken = this.getToken().getTokenString();
      if (sToken != null) {
        AgpUtil.appendURLParameter(params,"token",sToken,true);
      }
    }
  }

  /**
   * Closes any open resources.
   */
  public void close() {
    if (this.getClient() != null) {
      this.getClient().close();
      this.setClient(null);
    }
  }
  
  /**
   * Ensures the existence of a client.
   */
  public AgpClient ensureClient() {
    if (this.getClient() == null) {
      this.setClient(new AgpClient());
    }
    return this.getClient();
  }
    
  /**
   * Generates a security token.
   */
  public void generateToken() throws Exception {
    AgpTokenCriteria criteria = this.getTokenCriteria();
    AgpTokenRequest request = new AgpTokenRequest();
    AgpToken token = request.generateToken(this,criteria);
    this.setToken(token);
  }
  
  /**
   * Makes the base url.
   * @return the base url
   */
  public String makeBaseUrl(String protocol) {
    String sUrl = protocol+"://"+this.getHost();
    if (this.getPort() > 0) {
      sUrl += ":"+this.getPort();
    }
    if (getWebContext()!=null) {
      sUrl += getWebContext();
    }
    return sUrl;
  }
  
  /**
   * Makes the generate token url.
   * @return the generate token url
   */
  public String makeGenerateTokenUrl() {
    return this.makeBaseUrl("https")+"/sharing/generateToken";
  }
  
  /**
   * Makes properties for the request header.
   * @return the request header properties
   */
  public AgpProperties makeRequestHeaderProperties() {
    AgpProperties hdr = new AgpProperties();
    if (this.getToken() != null) {
      if (this.getToken().getReferer() != null) {
        hdr.add(new AgpProperty("Referer",this.getToken().getReferer()));
      }
    }
    return hdr;
  }
  
  /**
   * Makes the sharing url.
   * @return the sharing url
   */
  public String makeSharingUrl() {
    return this.makeBaseUrl("https")+"/sharing";
  }
  
}