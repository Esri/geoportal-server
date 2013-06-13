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

import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.http.StringProvider;
import com.esri.gpt.framework.util.Val;

/**
 * Some common ArcGIS Portal requests.
 */
public class AgpCommonRequest {
       
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpCommonRequest() {
    super();
  }
  
  /** methods ================================================================= */
    
  /**
   * Searches for related items.
   * @param connection the connection
   * @param item the source item
   * @param relationshipType the relationship type
   * @param direction the relationship type
   * @return the related items
   * @throws Exception if an exception occurs
   */
  /*
  public AgpItems queryRelatedItems(AgpConnection connection, AgpItem item) 
    throws Exception {
    String sId = item.getProperties().getValue("id");
    String sType = item.getProperties().getValue("type");
    String sRelType = null;
    String sRelDir = "forward";
    if (sType != null) {
      if (sType.equalsIgnoreCase("Web Mapping Application")) {
        sRelType = "WMA2Code";
      } else if (sType.equalsIgnoreCase("Mobile Application")) {
        sRelType = "MobileApp2Code";
      } else if (sType.equalsIgnoreCase("Web Map")) {
        //sRelType = "Map2Service";           // related services
        //sRelType = "Map2FeatureCollection"; // related features
      } else if (sType.equalsIgnoreCase("Service Item")) {
        //sRelType = "Service2Data";
      }
    }
    if (sRelType != null) {
      return this.queryRelatedItems(connection,sId,sRelType,sRelDir);
    }
    return null;
  }
  */
  
  /**
   * Searches for related items.
   * @param connection the connection
   * @param itemID the item id
   * @param relationshipType the relationship type
   * @param direction the relationship type
   * @return the related items
   * @throws Exception if an exception occurs
   */
  public AgpItems queryRelatedItems(AgpConnection connection, 
      String itemID, String relationshipType, String direction) 
    throws Exception {
    AgpItems items = new AgpItems();
    
    // prepare the request
    String sType = "application/x-www-form-urlencoded";
    String sUrl = connection.makeSharingUrl()+"/content/items/";
    sUrl += AgpUtil.encodeURIComponent(itemID);
    sUrl += "/relatedItems";
    StringBuilder params = new StringBuilder("f=json");
    connection.appendToken(params);
    AgpUtil.appendURLParameter(params,"relationshipType",relationshipType,true);
    if ((direction != null) && (direction.length() > 0)) {
      AgpUtil.appendURLParameter(params,"relationshipType",relationshipType,true);
    }
    AgpProperties hdr = connection.makeRequestHeaderProperties();
    
    // execute the request
    AgpClient client = connection.ensureClient();
    JSONObject jso = client.executeJsonRequest(sUrl,hdr,params,sType);
        
    // parse the results
    if (jso.has("relatedItems") && (!jso.isNull("relatedItems"))) {
      JSONArray jsoResults = jso.getJSONArray("relatedItems");
      int nResults = jsoResults.length();
      for (int iResult=0;iResult<nResults;iResult++) {
        JSONObject jsoItem = jsoResults.getJSONObject(iResult);
        AgpItem agpItem = new AgpItem();
        agpItem.parseItem(jsoItem);
        items.add(agpItem);
      }
    }
    
    /*
    if ((items != null) && (items.size() > 0)) {
      for (AgpItem relItem: items.values()) {
        String sRelID = relItem.getProperties().getValue("id");
        System.err.println("relationship: "+itemID+" -> "+sRelID);
      }
    }
    */
    
    return items;
  }
  
  /**
   * Reads the formal metadata for an item.
   * @param connection the connection
   * @param itemId the item id
   * @return the formal metadata xml
   * @throws Exception if an exception occurs
   */
  public String readFormalMetadata(AgpConnection connection, String itemId) 
    throws Exception {
    String sUrl = connection.makeSharingUrl();
    sUrl += "/content/items";
    sUrl += "/"+AgpUtil.encodeURIComponent(itemId);
    sUrl += "/info/metadata/metadata.xml";
    
    String sType = "application/x-www-form-urlencoded";
    StringBuilder params = new StringBuilder();
    connection.appendToken(params);
    AgpProperties hdr = connection.makeRequestHeaderProperties();
    AgpClient client = connection.ensureClient();
    StringProvider provider = new StringProvider(params.toString(),sType);
    StringHandler handler = new StringHandler();
    
    client.executeRequest(sUrl,hdr,provider,handler);
    return Val.removeBOM(handler.getContent());
  }
  
}