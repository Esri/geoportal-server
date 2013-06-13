/*
 * Copyright 2013 Esri.
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
package com.esri.gpt.agp.client;

import org.json.JSONObject;

/**
 * Count request
 */
public class AgpCountRequest {
  public long count(AgpConnection connection, AgpSearchCriteria criteria) throws Exception {

    // prepare the request
    String sType = "application/x-www-form-urlencoded";
    String sUrl = connection.makeSharingUrl()+"/search";
    StringBuilder params = new StringBuilder("f=json");
    connection.appendToken(params);
    criteria.appendURLParameters(params);
    AgpProperties hdr = connection.makeRequestHeaderProperties();
    
    // execute the request
    AgpClient client = connection.ensureClient();
    JSONObject jso = client.executeJsonRequest(sUrl,hdr,params,sType);
    
    return jso.has("total")? jso.getLong("total"): 0;
  }
  
}
