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
import com.esri.gpt.agp.client.AgpCommonRequest;
import com.esri.gpt.agp.client.AgpConnection;
import com.esri.gpt.agp.client.AgpItem;
import com.esri.gpt.agp.client.AgpItemListener;
import com.esri.gpt.agp.client.AgpItems;
import com.esri.gpt.agp.client.AgpProperties;
import com.esri.gpt.agp.client.AgpProperty;
import com.esri.gpt.agp.client.AgpSearchCriteria;
import com.esri.gpt.agp.client.AgpSearchRequest;
import com.esri.gpt.agp.client.AgpUtil;
import com.esri.gpt.agp.multipart2.MultipartProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * A push synchronizer from a source ArcGIS Portal to a 
 * destination ArcGIS Portal.
 */
public class AgpPush { 
  
/*


AgpPush
 - ** don't use if source or destination < 1.6.0.2
 - ** prevent host to same host synchronization?
 - ** prevent synchronization of items with same itemId? same title?
 - ** prevent propagation of synchronized items?
 - show group ids, owner ids from push.jsp?
 - auto share ?
 - makeDestinationTypeKeywords
 - what about deletes?
 - should there be retries?
 - determine the version?
 - /sharing vs /sharing/rest
 - issues with referenced/related items for web maps?
 - code attachments aren't updating correctly
 - special characters in description, utf issue?
 - metadata starting with <xhtml>
 - specify port
 - specify non-https
 - too many items in a folder


ok AgpClient
?  AgpCommonRequest (queryRelatedItems?)
ok AgpConnection
ok AgpContext
ok AgpCredentials
ok AgpCursor
ok AgpDPart
ok AgpError
ok AgpException
ok AgpItem
ok AgpItemListener
ok AgpItems
ok AgpProperties
ok AgpProperty
ok AgpSearchCriteria
ok AgpSearchRequest
ok AgpToken
ok AgpTokenCriteria
ok AgpTokenRequest
ok AgpUtil

ok AgpDestination
ok AgpSource
?  AgpItemHelper
?  AgpPartHelper

ok MPart
ok MultipartProvider

push.jsp
push-exec.jsp
push-working.jsp

*/

/*
  // TODO: 
  
  issues with referenced/related items for web maps?
  makeDestinationTypeKeywords
  
  should there be retries?
  determine the version
  
  try to supply an id
  Supported types?
  web maps, item property plus typeKeywords ??
  addDataPart
  
  item property?
  sort by date on the primary search?
  metadata thumbnail file attachments relationships
  auto share
  Comments Ratings Views
  what about deletes?
  typekeywords
  code attachments aren't updating correctly
  don't move if the id exists on the other side?
  registered services within a web map?
  special characters in description, utf issue?
  
*/  

  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static final Logger LOGGER = Logger.getLogger(AgpPush.class.getName());
  
  /** instance variables ====================================================== */
  private AgpItemHelper  itemHelper = new AgpItemHelper(); 
  private AgpPartHelper  partHelper = new AgpPartHelper(); 
  
  private AgpSource      source;
  private AgpDestination destination;
  private boolean        forceUpdates = false;
  private boolean        readOnly = false;
  private List<String>   webMaps = new ArrayList<String>();
  
  private long millisStart = 0;
  private long millisEnd = 0;
  private int  numItemsConsidered = 0;
  private int  numItemsInserted = 0;
  private int  numItemsUpdated = 0;
  private int  numMetadataPublished = 0;
  private int  numOriginatedFromSynchronization = 0;
  private int  numRelationshipsAdded = 0;
  private int  numUnsyncedExistingAtDestination = 0;
  private int  numWithNullId = 0;
  private int  numWithNullType = 0;
     
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public AgpPush() {}
  
  /**
   * Construct with a source and destination.
   * @param source the source
   * @param destination the destination
   */
  public AgpPush(AgpSource source, AgpDestination destination) {
    this.source = source;
    this.destination = destination;
    //this.forceUpdates = true; // TODO turn off
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the process summary.
   * @param forHtml true if the summary should be formatted for HTML
   * @return the process summary
   */
  public String getSummary(boolean forHtml) {
    StringBuilder msg = new StringBuilder();
    String s;
    String nl = "\n";
    if (forHtml) nl = "<br/>";
    
    msg.append("Sync:");
    
    double dSec = (this.millisEnd - this.millisStart) / 1000.0;
    String sMin = (Math.round(dSec / 60.0 * 100.0) / 100.0)+" minutes";
    String sSec = (Math.round(dSec * 100.0) / 100.0)+" seconds";
    if (dSec > 300) {
      s = sMin;
    } else if (dSec <= 60) {
      s = sSec;
    } else {
      //s = sMin +", "+sSec;
      s = sSec;
    } 
    msg.append(" ").append(s);
    
    msg.append(", considered:").append(this.numItemsConsidered);
    msg.append(" inserted:").append(this.numItemsInserted);
    msg.append(" updated:").append(this.numItemsUpdated);
    msg.append(" metadataPublished:").append(this.numMetadataPublished);
    msg.append(" relationshipsAdded:").append(this.numRelationshipsAdded);
    
    int n = this.numUnsyncedExistingAtDestination+this.numOriginatedFromSynchronization+this.numWithNullId+this.numWithNullType;
    if (n > 0) {
      msg.append(nl).append(" ignored:").append(n);
      if (this.numUnsyncedExistingAtDestination > 0) {
        msg.append(" unsyncedExistingAtDestination:").append(this.numUnsyncedExistingAtDestination);
      }
      if (this.numOriginatedFromSynchronization > 0) {
        msg.append(" originatedFromSynchronization:").append(this.numOriginatedFromSynchronization);
      }
      if (this.numWithNullId > 0) {
        msg.append(" withNullId:").append(this.numWithNullId);
      }
      if (this.numWithNullType > 0) {
        msg.append(" withNullType:").append(this.numWithNullType);
      }
    }
    return msg.toString();
  }
    
  /** methods ================================================================= */
  
  /**
   * Add a relationship between items.
   * @param connection the connection
   * @param owner the owner of the relationship
   * @param originItemId the from item for the relationship
   * @param destinationItemId the to item for the relationship
   * @param relationshipType the relationship type
   * @throws Exception if an exception occurs
   */
  private void execAddRelationship(AgpConnection connection, String owner,
      String originItemId, String destinationItemId, String relationshipType) 
    throws Exception {
    String sMsg = originItemId+" to "+destinationItemId+" ,type="+relationshipType;
    LOGGER.finer("Adding relationship "+sMsg);
        
    String sUrl = connection.makeSharingUrl();
    sUrl += "/content/users";
    sUrl += "/"+AgpUtil.encodeURIComponent(owner);
    sUrl += "/addRelationship";
    StringBuilder params = new StringBuilder("f=json");
    connection.appendToken(params);
    AgpUtil.appendURLParameter(params,"originItemId",originItemId,true);
    AgpUtil.appendURLParameter(params,"destinationItemId",destinationItemId,true);
    AgpUtil.appendURLParameter(params,"relationshipType",relationshipType,true);
    AgpProperties hdr = connection.makeRequestHeaderProperties();
    String sMimeType = "application/x-www-form-urlencoded";
    
    // TODO: sometimes this takes a retry, why?
    if (this.readOnly) return;
    AgpClient client = connection.ensureClient();
    JSONObject jso = client.executeJsonRequest(sUrl,hdr,params,sMimeType);
    if (jso.has("success") && jso.getString("success").equals("true")) { 
      this.numRelationshipsAdded++;
      LOGGER.finer("Relationship added: "+sUrl+" "+sMsg);
    } else {
      LOGGER.finer("Add relationship failed: "+sUrl+" "+sMsg);
      // TODO: throw exception here??
    }
  }
  
  /**
   * Publish an item to the destination.
   * @param sourceItem the source item
   * @param destItem the destination item
   * @throws Exception is an exception occurs
   */
  private void execPublishItem(AgpItem sourceItem, AgpItem destItem) 
    throws Exception {
    AgpSource src = this.source;
    AgpDestination dest = this.destination;
    String sSrcId = sourceItem.getProperties().getValue("id");
    String sDestId = destItem.getProperties().getValue("id");
    String sTitle = sourceItem.getProperties().getValue("title");
    LOGGER.finer("Publishing item: "+sSrcId+" "+sTitle);
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest(sourceItem.getProperties().toString());
      LOGGER.finest(destItem.getProperties().toString());
    }
    
    // make the URL
    boolean bInsert = true;
    String sUrl = dest.getConnection().makeSharingUrl();
    sUrl += "/content/users";
    sUrl += "/"+AgpUtil.encodeURIComponent(dest.getDestinationOwner());
    sUrl += "/"+AgpUtil.encodeURIComponent(dest.getDestinationFolderID()); 
    if (sDestId == null) {
      sUrl += "/addItem";
    } else {
      bInsert = false;
      sUrl += "/items";
      sUrl += "/"+AgpUtil.encodeURIComponent(sDestId);
      sUrl += "/update";
    }
    
    // make the content provider, add thumb-nail and data parts
    MultipartProvider provider = new MultipartProvider();
    provider.add("f","json");
    provider.add("token",destination.getConnection().getToken().getTokenString());
    provider.add("overwrite","true");
    for (AgpProperty destProp: destItem.getProperties().values()) {
      provider.add(destProp.getName(),destProp.getValue());
    }
    this.partHelper.addThumbnailPart(provider,src,sourceItem,dest,destItem);
    this.partHelper.addDataPart(provider,src,sourceItem,dest,destItem);
        
    // execute
    if (this.readOnly) return;
    AgpProperties hdr = dest.getConnection().makeRequestHeaderProperties();
    AgpClient client = dest.getConnection().ensureClient();
    JSONObject jso = client.executeJsonRequest(sUrl,hdr,provider);
    if (jso.has("id") && jso.has("success") && jso.getString("success").equals("true")) {
      if (sDestId == null) {
        sDestId = jso.getString("id");
        destItem.getProperties().add(new AgpProperty("id",sDestId));
      }  
      if (bInsert) {
        this.numItemsInserted++; 
        LOGGER.finer("Item inserted: "+sUrl);
      } else {
        this.numItemsUpdated++;
        LOGGER.finer("Item updated: "+sUrl);
      }
      
    } else {
      LOGGER.finer("Publish item FAILED for: "+sUrl);
      // TODO: throw exception here??
    }
  }
  
  /**
   * Publish formal metadata to the destination.
   * @param itemId the destination item ID
   * @param xml the metadata XML
   * @throws Exception if an exception occurs
   */
  private void execPublishMetadata(String itemId, String xml) throws Exception {
    LOGGER.finer("Publishing metadata for:"+itemId);
    
    AgpDestination dest = this.destination;    
    String sUrl = dest.getConnection().makeSharingUrl();
    sUrl += "/content/users";
    sUrl += "/"+AgpUtil.encodeURIComponent(dest.getDestinationOwner());
    sUrl += "/"+AgpUtil.encodeURIComponent(dest.getDestinationFolderID());
    sUrl += "/items";
    sUrl += "/"+AgpUtil.encodeURIComponent(itemId);
    sUrl += "/update";
    
    AgpProperties hdr = dest.getConnection().makeRequestHeaderProperties();
    MultipartProvider provider = new MultipartProvider();
    provider.add("f","json");
    provider.add("token",destination.getConnection().getToken().getTokenString());
    provider.add("overwrite","true");
    provider.add("metadata",xml.getBytes("UTF-8"),"metadata.xml","text/xml","UTF-8");
   
    if (this.readOnly) return;
    AgpClient client = dest.getConnection().ensureClient();
    JSONObject jso = client.executeJsonRequest(sUrl,hdr,provider);
    if (jso.has("success") && jso.getString("success").equals("true")) {  
      this.numMetadataPublished++;
      LOGGER.finer("Metadata updated for: "+sUrl);
    } else {
      LOGGER.finer("Metadata update FAILED for: "+sUrl);
      // TODO: throw exception here??
    }
  }
  
  /**
   * Process the formal metadata for an item.
   * @param sourceItem the source item
   * @param destItem the destination item
   * @throws Exception is an exception occurs
   */
  private void processMetadata(AgpItem sourceItem, AgpItem destItem) 
    throws Exception {
    String sSrcId = sourceItem.getProperties().getValue("id");
    String sDestId = destItem.getProperties().getValue("id");
    String sTitle = sourceItem.getProperties().getValue("title");
    LOGGER.finer("Processing metadata for: "+sSrcId+" "+sTitle);
    AgpCommonRequest request = new AgpCommonRequest();
    AgpConnection connection = this.source.getConnection();
    try {
      String sXml = request.readFormalMetadata(connection,sSrcId);
      if ((sXml != null) && (sXml.length() > 0)) {
        if (sXml.indexOf("<xhtml") == -1) {
          this.execPublishMetadata(sDestId,sXml);
        }
      }
    } catch (IOException ioe) {
      String s = ioe.toString();
      
      // this is what happens when there is no metadata
      // 1.6.02         HTTP Request failed: HTTP/1.1 500 Internal Server Error
      // AGOL 4/13/2012 HTTP Request failed: HTTP/1.1 400 Bad Request
      if (s.contains("HTTP Request failed")) {
        LOGGER.finest("No metadata found for item:"+sSrcId+" "+s);
      } else {
        //TODO: throw exception here?
        ioe.printStackTrace(System.err);
      }
    }
  }
  
  /**
   * Process the related items for a source item
   * @param sourceItem the source item
   * @param destItem the destination item
   * @throws Exception is an exception occurs
   */
  private void processRelatedItems(AgpItem sourceItem, AgpItem destItem) 
    throws Exception {
    AgpSource src = this.source;
    AgpDestination dest = this.destination;
    String sSrcId = sourceItem.getProperties().getValue("id");
    String sDestId = destItem.getProperties().getValue("id");
    String sTitle = sourceItem.getProperties().getValue("title");
    String sType = sourceItem.getProperties().getValue("type");
    LOGGER.finer("Processing related items for: "+sSrcId+" "+sTitle);

    // TODO: there are more relationships to consider
    // TODO: currently only considering rel types WMA2Code and MobileApp2Code
    
    String sRelType = null;
    String sRelDir = "forward";
    if (sType.equalsIgnoreCase("Web Mapping Application")) {
      sRelType = "WMA2Code";
    } else if (sType.equalsIgnoreCase("Mobile Application")) {
      sRelType = "MobileApp2Code";
    } else if (sType.equalsIgnoreCase("Web Map")) {
      //sRelType = "Map2Service";           // related services
      //sRelType = "Map2FeatureCollection"; // related features
    } else if (sType.equalsIgnoreCase("Service Item")) {
      //sRelType = "Service2Data";
    } else if (sType.equalsIgnoreCase("Map Service")) {
      //sRelType = "Service2Data";
    } else if (sType.equalsIgnoreCase("Feature Service")) {
      //sRelType = "Service2Data";
    }
    
    AgpItems destRelItems = null;
    AgpItems srcRelItems = null;
    AgpCommonRequest request = new AgpCommonRequest();
    if (sRelType != null) {
      
      srcRelItems = request.queryRelatedItems(src.getConnection(),
          sSrcId,sRelType,sRelDir);
      destRelItems = request.queryRelatedItems(dest.getConnection(),
          sDestId,sRelType,sRelDir);
      
      // TODO: consider when to delete destination related items
      
      if ((srcRelItems != null) && (srcRelItems.size() > 0)) {
        for (AgpItem srcRelItem: srcRelItems.values()) {
          
          AgpItem destRelItem = this.itemHelper.makeDestinationItem(src,srcRelItem);
          boolean bRequiresUpdate = this.itemHelper.requiresUpdate(srcRelItem,dest,destRelItem);
          if (this.forceUpdates || bRequiresUpdate) {
            this.execPublishItem(srcRelItem,destRelItem);
          }
          
          boolean bAddRel = true;
          if ((destRelItems != null) && (destRelItems.size() > 0)) {
            // TODO: this isn't enough
            bAddRel = false; 
          }
          
          if (bAddRel) {
            String sRelOwner = dest.getDestinationOwner();
            String sRelDestId = destRelItem.getProperties().getValue("id");
            this.execAddRelationship(dest.getConnection(),
                sRelOwner,sDestId,sRelDestId,sRelType);
          }
        }
      }
    }
  }
  
  /**
   * Process a web map.
   * @param sourceItem the source item
   * @throws Exception if an exception occurs
   */
  private void processWebMap(AgpItem sourceItem) throws Exception {
    AgpDestination dest = this.destination;
    String sId = sourceItem.getProperties().getValue("id");
    LOGGER.finer("Publishing web map: "+sId);
    AgpItem destItem = this.itemHelper.makeDestinationItem(this.source,sourceItem);
    boolean bRequiresUpdate = this.itemHelper.requiresUpdate(sourceItem,dest,destItem);
    if (this.forceUpdates || bRequiresUpdate) {
      this.execPublishItem(sourceItem,destItem);
      this.processMetadata(sourceItem,destItem);
      this.processRelatedItems(sourceItem,destItem);
    }
  }
  
  /**
   * Publish the web maps to the destination.
   * <br/>Web maps are collected then published last, this is
   * to ensure that items referenced by the web map have 
   * been previously pushed.
   * @throws Exception if an exception occurs
   */
  private void processWebMaps() throws Exception {
    LOGGER.finer("Publishing "+this.webMaps.size()+" web map(s).");
    for (String sId: this.webMaps) {
      AgpSearchRequest request = new AgpSearchRequest();
      AgpSearchCriteria criteria = new AgpSearchCriteria();
      criteria.setQ("id:"+sId);
      criteria.setNum(1);
      request.search(this.source.getConnection(),criteria, 
        new AgpItemListener() {
          @Override
          public void onItemLoaded(AgpConnection connection, AgpItem item)
            throws Exception {
            processWebMap(item);
          }
        }
      );
    }
  }
  
  
  /**
   * Synchronized an item.
   * @param sourceItem the source item
   * @return <code>true</code> if item has been updated
   * @throws Exception if an exception occurs
   */
  protected boolean syncItem(AgpItem sourceItem) throws Exception {
    this.numItemsConsidered++;
    AgpSource src = this.source;
    AgpDestination dest = this.destination; 
    String sId = sourceItem.getProperties().getValue("id");
    String sType = sourceItem.getProperties().getValue("type");
    String sTitle = sourceItem.getProperties().getValue("title");
    String sMsg = "Processing item ("+this.numItemsConsidered+")";
    sMsg += ", id:"+sId+", type:"+sType+", title:"+sTitle;
    LOGGER.info(sMsg);

    // check the id and type
    if (sId == null) {
      this.numWithNullId++;
      LOGGER.finer("Ignoring item with null id: "+sTitle);
      return false;
    } else if (sType == null) {
      this.numWithNullType++;
      LOGGER.finer("Ignoring item with null type: "+sId+" "+sTitle);
      return false;
    } else if (sType.equalsIgnoreCase("Code Attachment")) {
      // don't publish Code Attachments now, publish within processRelatedItems
      return false;
    }
    
    // TODO sync items within the same portal
    // TODO don't propagate synced items from portal to portal
    
    // don't re-publish items when the exact itemId exists at the destination
    boolean bUnsyncedItemExists = this.itemHelper.doesUnsyncedItemExist(sourceItem,dest);
    if (bUnsyncedItemExists) {
      this.numUnsyncedExistingAtDestination++;
      String s = "Ignoring unsynced item existing at destination: ";
      LOGGER.finer(s+sId+" "+sTitle);
      return false;
    }
    
    // don't propagate synced items from portal to portal
    boolean bIsSyncedItem = this.itemHelper.isSyncedItem(sourceItem);
    if (bIsSyncedItem) {
      this.numOriginatedFromSynchronization++;
      String s = "Ignoring, an item that originated from synchronization will not be repropagated: ";
      LOGGER.finer(s+sId+" "+sTitle);
      return false;
    }
    
       
    // determine if the item requires an update
    // process web maps at the end of the job
    // TODO: there will be problems if the item is no longer visible to this user
    AgpItem destItem = this.itemHelper.makeDestinationItem(src,sourceItem);
    boolean bRequiresUpdate = this.itemHelper.requiresUpdate(sourceItem,dest,destItem);
    if (this.forceUpdates || bRequiresUpdate) {
      if (sType.equalsIgnoreCase("Web Map")) {
        this.webMaps.add(sId);
      } else {
        this.execPublishItem(sourceItem,destItem);
        this.processMetadata(sourceItem,destItem);
        this.processRelatedItems(sourceItem,destItem);
      }
      return true;
    }
    
    return false;
  }
  
  /**
   * Execute the synchronization process.
   * @throws Exception if an exception occurs
   */
  public void synchronize() throws Exception {
    this.millisStart = System.currentTimeMillis();
    LOGGER.info("AgpPush: Starting synchronization...");
    try {
      this.source.getConnection().generateToken();
      this.destination.getConnection().generateToken();
      AgpSearchRequest sourceRequest = new AgpSearchRequest() {
        @Override
        protected boolean doContinue() {
          return AgpPush.this.doContinue();
        }
      };
      sourceRequest.search(this.source.getConnection(),this.source.getSearchCriteria(), 
        new AgpItemListener() {
          @Override
          public void onItemLoaded(AgpConnection connection, AgpItem item) throws Exception {
            AgpPush.this.syncItem(item);
          }
        }
      );
      this.processWebMaps();
      
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw e;
      
    } finally {
      try {
        this.source.getConnection().close();
      } catch (Throwable t) {
        t.printStackTrace(System.err);
      }
      try {
        this.destination.getConnection().close();
      } catch (Throwable t) {
        t.printStackTrace(System.err);
      }
      this.millisEnd = System.currentTimeMillis();
      LOGGER.info(this.getSummary(false));
    }
  }
  
  protected boolean doContinue() {
    return true;
  }
  
}