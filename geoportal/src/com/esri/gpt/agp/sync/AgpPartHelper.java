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
package com.esri.gpt.agp.sync;
import com.esri.gpt.agp.client.AgpClient;
import com.esri.gpt.agp.client.AgpConnection;
import com.esri.gpt.agp.client.AgpDPart;
import com.esri.gpt.agp.client.AgpItem;
import com.esri.gpt.agp.client.AgpProperties;
import com.esri.gpt.agp.client.AgpUtil;
import com.esri.gpt.agp.multipart2.MultipartProvider;
import com.esri.gpt.framework.http.StringHandler;
import com.esri.gpt.framework.http.StringProvider;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Multi-part helper functions for the AgpPush synchronizer.
 */
public class AgpPartHelper { 
  
  /** instance variables ====================================================== */
  private AgpItemHelper itemHelper = new AgpItemHelper(); 

  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpPartHelper() {}
    
  /** methods ================================================================= */
  
  /**
   * Adds the data part to a multiipart request for item
   * addition or update. 
   * @param provider the multi-part request provider
   * @param source the synchronization source 
   * @param sourceItem the synchronization source item
   * @param destination the synchronization destination 
   * @param destItem the synchronization destination item
   * @throws Exception if an exception occurs
   */
  protected void addDataPart(MultipartProvider provider, 
      AgpSource source, AgpItem sourceItem, 
      AgpDestination destination, AgpItem destItem) 
    throws Exception {
    
    AgpConnection con = source.getConnection();
    String sId = sourceItem.getProperties().getValue("id");
    String sUrl = con.makeSharingUrl()+"/content/items";
    sUrl += "/"+AgpUtil.encodeURIComponent(sId);
    sUrl += "/data";
    
    // this is probably not necessary, -1 seems to work, 
    // search?q= results return size=-1, 
    // you need an items/[id] request to get the actual size
    String sDataSize = sourceItem.getProperties().getValue("size");
    long nDataSize = -1;
    try {
      nDataSize = Long.valueOf(sDataSize);
    } catch (NumberFormatException nfe) {
      nDataSize = -1;
      nfe.printStackTrace(System.err);
    }
    //System.err.println("nnnnDataSize="+nDataSize);
 
    String sFileName = null;
    String sPartName = "text"; // TODO is this correct?
    String sItem = sourceItem.getProperties().get("item").getValue();
    String sItemType = sourceItem.getProperties().getValue("itemType");
    String sType = sourceItem.getProperties().getValue("type");
    
    if ((sItemType != null) && sItemType.equals("file")) {
      sPartName = "file";
      sFileName = sItem; // TODO is this correct?
      provider.add("item",sItem); // TODO is this correct?
      provider.add(new AgpDPart(con,sourceItem,sUrl,sPartName,sFileName,nDataSize));
      
    } else if ((sItemType != null) && sItemType.equals("url")) {
      // Content-Type: text/plain; charset=utf-8
      sPartName = "text";
      sFileName = null;  
      provider.add("item",sItem); // TODO is this correct?
      provider.add(new AgpDPart(con,sourceItem,sUrl,sPartName,sFileName,nDataSize));
      
    } else if ((sItemType != null) && sItemType.equals("text")) {
      sPartName = "text";
      sFileName = null; 
      if (!sType.equals("Web Map")) {
        provider.add("item",sItem); // TODO is this correct?
        provider.add(new AgpDPart(con,sourceItem,sUrl,sPartName,sFileName,nDataSize));
      } else {
        
        provider.add("item",sItem); // TODO is this correct?
        
        String sCType = "application/x-www-form-urlencoded";
        StringBuilder params = new StringBuilder();
        con.appendToken(params);
        AgpProperties hdr = con.makeRequestHeaderProperties();
        AgpClient client = con.ensureClient();
        StringProvider prov = new StringProvider(params.toString(),sCType);
        StringHandler handler = new StringHandler();;
        client.executeRequest(sUrl,hdr,prov,handler);
        String sWebMapJson = handler.getContent();
        
        
        /*
        web map
        "operationalLayers":[
           {
              "url":"http://irwinags/arcgis/rest/services/IRWIN/MapServer",
              "id":"IRWIN_8147",
              "visibility":true,
              "opacity":1,
              "title":"IRWIN",
              "itemId":"0d635c1158a844d4a19c048c854345df"
           },
        */
        AgpConnection con2 = destination.getConnection();
        boolean bMod = false;
        JSONObject jso = new JSONObject(sWebMapJson); 
        String sProp = "operationalLayers";
        if (jso.has(sProp) && (!jso.isNull(sProp))) {
          JSONArray jsoLayers = jso.getJSONArray(sProp);
          int n = jsoLayers.length();
          for (int i=0;i<n;i++) {
            JSONObject jsoLayer = jsoLayers.getJSONObject(i);
            if (jsoLayer.has("itemId") && (!jsoLayer.isNull("itemId"))) {
              String sItemId = jsoLayer.getString("itemId");
              //System.err.println("itemId="+sItemId);
              String sSyncKey = this.itemHelper.makeSyncKey(sItemId);
              String sDestQuery = "typekeywords:"+sSyncKey;
              AgpItem qItem = this.itemHelper.querySingleItem(con2,sDestQuery);
              if (qItem == null) {
                // TODO: is this query ok?
                sDestQuery = "id:"+sId;
                qItem = this.itemHelper.querySingleItem(con2,sDestQuery);
              }
              if (qItem == null) {
                // TODO: is this query ok?
                //System.err.println("------- removing webmap rel id "+sId);
                jsoLayer.remove("itemId");
                bMod = true;
              } else {
                String sDestId = qItem.getProperties().getValue("id");
                //System.err.println("------- putting webmap rel id "+sDestId);
                //jsoLayer.remove("itemId");
                jsoLayer.put("itemId",sDestId);
                bMod = true;
              }
            }
          }
        }
        if (bMod) {
          sWebMapJson = jso.toString();
        }
        provider.add("text",sWebMapJson);
        
      }
    }
  } 
  
  /**
   * Adds the thumbnail part to a multiipart request for item
   * addition or update. 
   * @param provider the multi-part request provider
   * @param source the synchronization source 
   * @param sourceItem the synchronization source item
   * @param destination the synchronization destination 
   * @param destItem the synchronization destination item
   * @throws Exception if an exception occurs
   */
  protected void addThumbnailPart(MultipartProvider provider, 
      AgpSource source, AgpItem sourceItem,
      AgpDestination destination, AgpItem destItem) 
    throws Exception {
    AgpConnection con = source.getConnection();
    String sThumbnail = sourceItem.getProperties().getValue("thumbnail");
    if (sThumbnail != null) {
      int n = sThumbnail.indexOf("thumbnail/");
      if (n == 0) {
        String sFileName = sThumbnail.substring(10);
        if ((sFileName.length() > 0) && (sFileName.indexOf("/") == -1)) {
          String sId = sourceItem.getProperties().getValue("id");
          String sUrl = con.makeSharingUrl()+"/content/items";
          sUrl += "/"+AgpUtil.encodeURIComponent(sId);
          sUrl += "/info/thumbnail/"+sFileName;
          provider.add(new AgpDPart(
              con,sourceItem,sUrl,"thumbnail",sFileName,-1));
        }
      }
    }    
  }
  
}