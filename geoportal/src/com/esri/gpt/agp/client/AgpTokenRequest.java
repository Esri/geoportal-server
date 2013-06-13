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
 * A request for a security token.
 */
public class AgpTokenRequest {
     
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpTokenRequest() {
    super();
  }
  
  /** methods ================================================================= */
  
  /**
   * Executes a generateToken request.
   * @param connection the connection
   * @param criteria the criteria
   * @return the token
   * @throws Exception if an exception occurs
   */
  public AgpToken generateToken(AgpConnection connection, AgpTokenCriteria criteria) 
    throws Exception {
    
    // prepare the request
    String sUrl = connection.makeGenerateTokenUrl();
    String sType = "application/x-www-form-urlencoded";
    StringBuilder params = new StringBuilder("f=json");
    criteria.appendURLParameters(params);
    AgpProperties hdr = new AgpProperties();
    hdr.add(new AgpProperty("Referer",criteria.getReferer()));
    
    // execute the request
    AgpClient client = connection.ensureClient();
    JSONObject jso = client.executeJsonRequest(sUrl,hdr,params,sType);
    
    // parse the response
    AgpToken token = new AgpToken();
    token.parseResponse(jso);
    token.setReferer(criteria.getReferer());
    return token;
  }
  
}