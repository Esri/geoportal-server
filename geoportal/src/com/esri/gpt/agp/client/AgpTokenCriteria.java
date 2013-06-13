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

/**
 * The criteria for a security token request.
 */
public class AgpTokenCriteria {
  
  /** instance variables ====================================================== */
  private AgpCredentials credentials;
  private int            expriation = 60;
  private String         referer;
     
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpTokenCriteria() {
    super();
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the credentials.
   * @return the credentials
   */
  public AgpCredentials getCredentials() {
    return this.credentials;
  }
  /**
   * Sets the credentials.
   * @param credentials the credentials
   */
  public void setCredentials(AgpCredentials credentials) {
    this.credentials = credentials;
  }
  
  /**
   * Gets the token expiration time in minutes. 
   * <br/>The default is 60 minutes.
   * @return the token expiration time
   */
  public int getExpriation() {
    return this.expriation;
  }
  /**
   * Sets the token expiration time in minutes.
   * <br/>The default is 60 minutes.
   * @param expriation the token expiration time
   */
  public void setExpriation(int expriation) {
    this.expriation = expriation;
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
  
  /** methods ================================================================= */
  
  /**
   * Appends parameters to a URL request buffer.
   * @param parameters the URL request buffer
   */
  public void appendURLParameters(StringBuilder parameters) {
    AgpUtil.appendURLParameter(parameters,
        "expiration",""+this.getExpriation(),true);
    AgpUtil.appendURLParameter(parameters,
        "referer",this.getReferer(),true);
    AgpUtil.appendURLParameter(parameters,
        "username",this.getCredentials().getUsername(),true);
    AgpUtil.appendURLParameter(parameters,
        "password",this.getCredentials().getPassword(),true);
  }
  
}