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
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A search request.
 */
public class AgpSearchRequest {
       
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpSearchRequest() {
    super();
  }
  
  /** methods ================================================================= */
  
  /**
   * Executes a search request.
   * @param connection the connection
   * @param criteria the criteria
   * @param listener the listener
   * @throws Exception if an exception occurs
   */
  public void search(AgpConnection connection, 
      AgpSearchCriteria criteria, AgpItemListener listener) throws Exception {
    
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
    
    // parse the result cursor
    AgpCursor cursor = new AgpCursor();
    cursor.parseResponse(jso);
    //System.err.println(cursor);
    
    // parse the results
    if (jso.has("results") && (!jso.isNull("results"))) {
      JSONArray jsoResults = jso.getJSONArray("results");
      int nResults = jsoResults.length();
      for (int iResult=0;iResult<nResults;iResult++) {
        if (!doContinue()) {
          return;
        }
        JSONObject jsoItem = jsoResults.getJSONObject(iResult);
        AgpItem agpItem = new AgpItem();
        agpItem.parseItem(jsoItem);
        //System.err.println("id="+agpItem.getProperties().get("id").getValue());
        if (listener != null) {
          listener.onItemLoaded(connection,agpItem);
        }
      }
      
      // page if required (shouldn't do this if there is no listener)
      if (listener != null) {
        long nNextStart = cursor.getNextStart();
        long nDeepTotal = criteria.getDeepTotal();
        if ((nNextStart > 0) && (nDeepTotal > 0)) {
          if (nNextStart <= nDeepTotal) {
            long nNumLeft = nDeepTotal - nNextStart + 1;
            if (nNumLeft < cursor.getNum()) {
              int nNum = (int)nNumLeft;
              criteria.setNum(nNum);
            }
            criteria.setStart(nNextStart);
            this.search(connection,criteria,listener);
          }
        }
      }
      
    }
  }
  
  protected boolean doContinue() {
    return true;
  }
}