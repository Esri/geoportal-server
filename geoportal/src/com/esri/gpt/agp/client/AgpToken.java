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
import org.json.JSONObject;

/**
 * A security token.
 */
public class AgpToken {
  
  /** instance variables ====================================================== */
  private long    expires = -1;
  private String  referer;
  private boolean requiresSSL = false;
  private String  tokenString;
     
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpToken() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the expiration time of the token in milliseconds since Jan 1st, 1970. 
   * @return the expiration time
   */
  public long getExpires() {
    return this.expires;
  }
  /**
   * Sets the expiration time of the token in milliseconds since Jan 1st, 1970. 
   * @param expires expiration time
   */
  public void setExpires(long expires) {
    this.expires = expires;
  }
  
  /**
   * Gets the referer.
   * @return the referer
   */
  public String getReferer() {
    return this.referer;
  }
  /**
   * Sets the referer.
   * @param referer the referer
   */
  public void setReferer(String referer) {
    this.referer = referer;
  }
  
  /**
   * Gets the status indicating if the token must be always passed over SSL.
   * @return true if SSL is required
   */
  public boolean getRequiresSSL() {
    return this.requiresSSL;
  }
  /**
   * Sets the status indicating if the token must be always passed over SSL.
   * @param requiresSSL true if SSL is required
   */
  public void setRequiresSSL(boolean requiresSSL) {
    this.requiresSSL = requiresSSL;
  }
  
  /**
   * Gets the token string.
   * @return the token string
   */
  public String getTokenString() {
    return this.tokenString;
  }
  /**
   * Sets the token string.
   * @param tokenString the token string
   */
  public void setTokenString(String tokenString) {
    this.tokenString = tokenString;
  }
  
  /** methods ================================================================= */
  
  /**
   * Parse the JSON response.
   * @param jsoResponse the JSON response
   * @throws Exception if an exception occurs
   */
  public void parseResponse(JSONObject jsoResponse) throws Exception {
    String sProp;
    
    sProp = "token";
    if (jsoResponse.has(sProp) && (!jsoResponse.isNull(sProp))) {
      this.setTokenString(jsoResponse.getString(sProp));        
    } else {
      AgpError error = new AgpError();
      error.setMessage("A token was not located within the response.");
      throw new AgpException(error);
    }
    sProp = "expires";
    if (jsoResponse.has(sProp) && (!jsoResponse.isNull(sProp))) {
      this.setExpires(jsoResponse.getLong(sProp));
    }
    sProp = "ssl";
    if (jsoResponse.has(sProp) && (!jsoResponse.isNull(sProp))) {
      this.setRequiresSSL(jsoResponse.getBoolean(sProp));
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(this.getClass().getCanonicalName());
    sb.append(" {");
    sb.append("\n token=").append(this.getTokenString());
    sb.append("\n expires=").append(this.getExpires());
    sb.append(" referer=").append(this.getReferer());
    sb.append(" requiresSSL=").append(this.getRequiresSSL());
    sb.append("\n}");
    return sb.toString();
  }
  
}