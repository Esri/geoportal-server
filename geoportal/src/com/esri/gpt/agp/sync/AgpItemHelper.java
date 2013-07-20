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
import com.esri.gpt.agp.client.AgpConnection;
import com.esri.gpt.agp.client.AgpItem;
import com.esri.gpt.agp.client.AgpItemListener;
import com.esri.gpt.agp.client.AgpProperties;
import com.esri.gpt.agp.client.AgpProperty;
import com.esri.gpt.agp.client.AgpSearchCriteria;
import com.esri.gpt.agp.client.AgpSearchRequest;

import java.util.ArrayList;

/**
 * Item related helper functions for the AgpPush synchronizer.
 */
public class AgpItemHelper { 

  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpItemHelper() {}
  
    
  /** methods ================================================================= */
  
  /**
   * Determines if an item with an exact itemId exists at the destination.
   * <br/>Avoid publishing these (these tend to be the start up items 
   * common to all arcGIS Portal instances.
   * @param sourceItem the source item
   * @param destination the destination
   * @return true if an item with the exact itemId exists at the destination
   */
  public boolean doesUnsyncedItemExist(AgpItem sourceItem, AgpDestination destination) 
    throws Exception {
    
    final AgpItem destItem = new AgpItem();
    destItem.setProperties(null);
    
    String sSrcId = sourceItem.getProperties().getValue("id");
    String sQuery = "id:"+sSrcId;
    AgpSearchRequest request = new AgpSearchRequest();
    AgpSearchCriteria criteria = new AgpSearchCriteria();
    criteria.setQ(sQuery);
    criteria.setNum(1);
    request.search(destination.getConnection(),criteria, 
      new AgpItemListener() {
        @Override
        public void onItemLoaded(AgpConnection connection, AgpItem item)
          throws Exception {
          destItem.setProperties(item.getProperties());
        }
      }
    );
    if (destItem.getProperties() != null) {
      return true;
    }
    return false;
  }
  
  /**
   * Determines if a source item originated from synchronization.
   * <br/>Avoid propagating synced items from portal to portal. 
   * @param item the source item
   * @return true if the item originated from synchronization 
   */
  public boolean isSyncedItem(AgpItem item) {
    String sTypeKeywords = item.getProperties().getValue("typeKeywords");
    if (sTypeKeywords != null) {
      String[] aTokens = sTypeKeywords.split(",");
      for (String sToken: aTokens) {
        sToken = sToken.trim();
        if (sToken.length() > 0) {
          if (sToken.equalsIgnoreCase("gptsync")) {
            return true;
          }
        }
      }
    }
    return false;
  }
  
  /**
   * Make the destination item.
   * @param source the source
   * @param sourceItem the source item
   * @return the destination item
   */
  protected AgpItem makeDestinationItem(AgpSource source, AgpItem sourceItem) {
    AgpItem destItem = new AgpItem();
    
    String[] aIgnoreProps = {
      "id",
      "item",
      "uploaded",
      "modified",
      "thumbnail",
      "thumbnailurl",
      "metadata",
      "owner",
      "access",
      "typeKeywords",
    };
    
    // TODO: item plus typeKeywords ??
    
    String[] aProps = {
      "itemType",
      "type",
      "url",
      "text",
      "title",
      "description",
      "snippet",
      "accessInformation",
      "licenseInfo",
      "culture",
      "tags",
      "extent",
      "spatialReference"
    };
    
    AgpProperties sourceProps = sourceItem.getProperties();
    AgpProperties destProps = destItem.getProperties();
    for (String sProp: aProps) {
      AgpProperty sourceProp = sourceProps.get(sProp);
      if (sourceProp != null) {
        String sValue = sourceProp.getValue();
        if (sValue == null) sValue = "";
        else sValue = sValue.trim();
        if (sValue.length() > 0) {
          //System.err.println(sProp+"=push="+sValue);
          destProps.add(new AgpProperty(sourceProp.getName(),sValue));
        }
      }
    }
    
    String sTypeKeywords = this.makeDestinationTypeKeywords(source,sourceItem);
    destProps.add(new AgpProperty("typeKeywords",sTypeKeywords));
    
    // TODO: item property?
    /*
    String sSrcItemType = sourceProps.getValue("itemType");
    if ((sSrcItemType != null) && (sSrcItemType.equals("url"))) {
      String sValue = sourceProps.getValue("item");
      if (sValue == null) sValue = "";
      else sValue = sValue.trim();
      if (sValue.length() > 0) {
        //System.err.println(sProp+"=push="+sValue);
        destProps.add(new AgpProperty("item",sValue));
      }
    }
    */
    
    return destItem;
  }
  
  /**
   * Make the ArcGIS Portal typeKeywords for the destination item.
   * @param source the source
   * @param sourceItem the source item
   * @return the ArcGIS Portal typeKeywords (delimited).
   */
  private String makeDestinationTypeKeywords(AgpSource source, AgpItem sourceItem) {
  
    // add the type keywords
    // TODO: what about the original typekeywords
    StringBuilder sbTypeKeywords = new StringBuilder();
    String sModified = sourceItem.getProperties().getValue("modified");
    String sSyncKey = this.makeSyncKey(sourceItem);
    ArrayList<String> aTypeKeywords = new ArrayList<String>();
    String sTypeKeywords = sourceItem.getProperties().getValue("typeKeywords");
    if (sTypeKeywords != null) {
      String[] aTokens = sTypeKeywords.split(",");
      for (String sToken: aTokens) {
        sToken = sToken.trim();
        if (sToken.length() > 0) {
          if (!sToken.startsWith("gptsync")) {
            aTypeKeywords.add(sToken);
          }
        }
      }
    }
    
    // TODO: need to specify the host?
    String sAlphaNumeric = "[^a-zA-Z0-9]+";
    String sSrcHost = source.getConnection().getHost().toLowerCase();
    sSrcHost = sSrcHost.replace(".","DOT");
    sSrcHost = sSrcHost.replaceAll(sAlphaNumeric,"R");
    String sFullTag = sSyncKey;
    sFullTag += "srcmodified"+sModified;
    sFullTag += "srchost"+sSrcHost;
    aTypeKeywords.add("gptsync");
    aTypeKeywords.add(sSyncKey);
    aTypeKeywords.add(sFullTag);
  
    
    for (String sTypeKeyword: aTypeKeywords) {
      if (sbTypeKeywords.length() > 0) sbTypeKeywords.append(",");
      sbTypeKeywords.append(sTypeKeyword);
    }
    return sbTypeKeywords.toString();
  }
  
  /**
   * Make the synchronization key.
   * @param sourceItem the source item
   * @return the key
   */
  protected String makeSyncKey(AgpItem sourceItem) {
    String sSourceID = sourceItem.getProperties().get("id").getValue();
    String sSyncKey = "gptsyncsrcid"+sSourceID;
    return sSyncKey;
  }
  
  /**
   * Make the synchronization key.
   * @param sourceId the ID of the source item
   * @return the key
   */
  protected String makeSyncKey(String sourceId) {
    return "gptsyncsrcid"+sourceId;
  }
  
  /**
   * Query for a destination item.
   * @param destination the destination
   * @param sourceItem the source item
   * @return the found item
   * @throws Exception if an exception occurs
   */
  private AgpItem queryDestinationItem(AgpDestination destination, 
      AgpItem sourceItem) throws Exception {
    
    final AgpItem destinationItem = new AgpItem();
    destinationItem.setProperties(null);
    
    String sSyncKey = this.makeSyncKey(sourceItem);
    String sQuery = "typekeywords:"+sSyncKey;
    
    AgpSearchRequest request = new AgpSearchRequest();
    AgpSearchCriteria criteria = new AgpSearchCriteria();
    criteria.setQ(sQuery);
    criteria.setNum(1);
    request.search(destination.getConnection(),criteria, 
      new AgpItemListener() {
        @Override
        public void onItemLoaded(AgpConnection connection, AgpItem item)
          throws Exception {
          destinationItem.setProperties(item.getProperties());
          //System.err.println("idgggggggggggg="+item.getProperties().get("id").getValue());
        }
      }
    );
    if (destinationItem.getProperties() != null) {
      //System.err.println("idzzzzzzzzzzz="+destinationItem.getProperties().get("id").getValue());
      return destinationItem;
    }
    return null;
  }
  
  /**
   * Query for a single item.
   * @param connection the connection
   * @param query the query string
   * @return the found item
   * @throws Exception if an exception occurs
   */
  protected AgpItem querySingleItem(AgpConnection connection, String query) 
    throws Exception {
    final AgpItem qItem = new AgpItem();
    qItem.setProperties(null);
    AgpSearchRequest request = new AgpSearchRequest();
    AgpSearchCriteria criteria = new AgpSearchCriteria();
    criteria.setQ(query);
    criteria.setNum(1);
    request.search(connection,criteria, 
      new AgpItemListener() {
        @Override
        public void onItemLoaded(AgpConnection connection, AgpItem item) throws Exception {
          qItem.setProperties(item.getProperties());
        }
      }
    );
    if (qItem.getProperties() != null) {
      return qItem;
    }
    return null;
  }
  
  /**
   * Determine if the destination item requires an update.
   * @param sourceItem the synchronization source item
   * @param destination the synchronization destination 
   * @param destItem the synchronization destination item
   * @return true if the item should be updated
   * @throws Exception if an exception occurs
   */
  protected boolean requiresUpdate(AgpItem sourceItem, 
      AgpDestination destination, AgpItem destItem) throws Exception {
    boolean bRequiresUpdate = true;
    AgpItem existingDestItem = this.queryDestinationItem(destination,sourceItem);
    if (existingDestItem != null) {
      String sDestID = existingDestItem.getProperties().getValue("id");
      destItem.getProperties().add(new AgpProperty("id",sDestID));
      String sTMod = existingDestItem.getProperties().get("modified").getValue();
      String sSMod = sourceItem.getProperties().get("modified").getValue();
      long nTMod = Long.valueOf(sTMod);
      long nSMod = Long.valueOf(sSMod);
      long nDiff = nSMod - nTMod;
      long nThresh = (1 * 1000);
      if (nDiff < nThresh) {
        bRequiresUpdate = false;
      }
    }
    return bRequiresUpdate;
  }
  
}