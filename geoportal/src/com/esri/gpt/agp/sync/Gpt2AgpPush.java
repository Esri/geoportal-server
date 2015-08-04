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
import com.esri.gpt.agp.client.AgpItem;
import com.esri.gpt.agp.client.AgpProperties;
import com.esri.gpt.agp.client.AgpProperty;
import com.esri.gpt.agp.client.AgpUtil;
import com.esri.gpt.agp.multipart2.MultipartProvider;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.sql.IClobMutator;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.framework.xml.XsltTemplates;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A push synchronizer from a source Geoportal to a destination ArcGIS Portal.
 */
public class Gpt2AgpPush { 
	
/*
 * TODO
 * 
 * Dublin Core, ArcGIS Metadata
 * 
 * Summary: numFailed?
 * Log messages (especially for exceptions/ignored)
 * What about ACLs and collections?
 * What about metadata.xml?
 * What about data files?
 * FGDC/ISO to ArcGIS Metadata?
 * How to filter Geoportal documents to consider?
 * How to filter Geoportal documents to publish?
 * How to determine type?
 * How to generate item.json? XSLTs?
 * What about ownership?
 * Carry additional process metadata @ portal?
 * 
 * Only elements with values get updated? (causes issue, publish with use constraint, then update without, original remains)
 * No deletes? Only WMS?
 * 
 * 
 * FGDC -> limit tags to theme only?
 * ISO -> limit tags to theme only?, licenseInfo - multiple locations (concatenate?)
 * 
 */
	
/*
	
	thumbnail 				The URL to the thumbnail used for the item.
	typeKeywords			A set of keywords that further describes the type of this item.
	owner							The username of the user who owns this item.
	ownerFolder				The ID of the folder in which the owner has stored the item. Returned to the item owner or the org admin.
	access 						Indicates the level of access to this item: private, shared, org, or public.

	id 								The unique ID for this item
	modified					The date the item was last modified. Shown in UNIX time in milliseconds.
	title							The title of the item. Every item must have a title.
	url 							The URL for the resource represented by the item. 
	type							The GIS content type of this item.
	snippet						A short summary description of the item.
	description				Item description.
	extent						The bounding rectangle of the item. Should always be in WGS84.
	accessInformation	Information on the source of the item and its copyright status.
	licenseInfo				Any license information or restrictions.
	tags							User defined tags that describe the item.
	
	spatialReference	The coordinate system of the item.
	culture						The item locale information (language and country).
	
	name							The file name of the item for file types. Read-only.
	protected					Protects the item from deletion. false is the default.
	commentsEnabled		Indicates if comments are allowed on the item.
	
	info/metadata/metadata.xml
	info/metadata/gptsync.xml? should we carry this?
	info/metadata/srcmetadata.xml should we carry this?
	
	Defaults on creation:
		protected, commentsEnabled, ownerFolder, access, culture
		
 */
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static final Logger LOGGER = Logger.getLogger(Gpt2AgpPush.class.getName());
  
  /** XSL templates */
  private static XsltTemplates XSLTTEMPLATES = new XsltTemplates();
  
  /** instance variables ====================================================== */
  private AgpItemHelper itemHelper = new AgpItemHelper(); 
  
  private GptSource source;
  private AgpDestination destination;
  private boolean forceUpdates = false;
  private boolean readOnly = false;
  
  private long millisStart = 0;
  private long millisEnd = 0;
  private int numItemsConsidered = 0;
  private int numItemsInserted = 0;
  private int numItemsUpdated = 0;
  private int numMetadataPublished = 0;
  private int numOriginatedFromSynchronization = 0;
  private int numRelationshipsAdded = 0;
  private int numUnsyncedExistingAtDestination = 0;
  private int numWithNullId = 0;
  private int numWithNullTitle = 0;
  private int numWithNullType = 0;
  private int numWithXsltError = 0;
  
  private Connection con;
  private RequestContext context;
  private IClobMutator mutator;
  private String resourceDataTable;
  private String resourceTable;
  
  private String xsltLocation = "gpt/metadata/2PortalItem.xslt";
  
  /** Default constructor. */
  public Gpt2AgpPush() {}
  
  /**
   * Construct with a source and destination.
   * @param source the source
   * @param destination the destination
   */
  public Gpt2AgpPush(GptSource source, AgpDestination destination) {
    this.source = source;
    this.destination = destination;
    //this.forceUpdates = true; // TODO turn off
  }
  
  protected boolean doContinue() {
    return true;
  }
  
  /**
   * Publish an item to the destination.
   * @param sourceItem the source item
   * @param destItem the destination item
   * @throws Exception is an exception occurs
   */
  private void execPublishItem(AgpItem sourceItem, AgpItem destItem) throws Exception {
    AgpDestination dest = this.destination;
    String sSrcId = sourceItem.getProperties().getValue("id");
    String sDestId = destItem.getProperties().getValue("id");
    String sTitle = sourceItem.getProperties().getValue("title");
    LOGGER.finer("Publishing item: "+Val.stripControls(sSrcId)+" "+sTitle);
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
    	//System.err.println(destProp.getName()+":" +destProp.getValue());
      provider.add(destProp.getName(),destProp.getValue());
    }
    // TODO thumbnails and data
    //this.partHelper.addThumbnailPart(provider,src,sourceItem,dest,destItem);
    //this.partHelper.addDataPart(provider,src,sourceItem,dest,destItem);
        
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
        LOGGER.finer("Item inserted: "+Val.stripControls(sUrl));
      } else {
        this.numItemsUpdated++;
        LOGGER.finer("Item updated: "+Val.stripControls(sUrl));
      }
      
    } else {
      LOGGER.finer("Publish item FAILED for: "+Val.stripControls(sUrl));
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
      LOGGER.finer("Metadata updated for: "+Val.stripControls(sUrl));
    } else {
      LOGGER.finer("Metadata update FAILED for: "+Val.stripControls(sUrl));
      // TODO: throw exception here??
    }
  }
  
  /**
   * Gets a compiled XSLT template.
   * @param xsltPath the path to an XSLT
   * @return the compiled template
   * @throws IOException if an IO exception occurs
   * @throws TransformerException if a transformation exception occurs
   */
  private synchronized XsltTemplate getCompiledTemplate(String xsltPath) throws TransformerException {
    String sKey = xsltPath;
    XsltTemplate template = XSLTTEMPLATES.get(sKey);
    if (template == null) {
      template = XsltTemplate.makeTemplate(xsltPath);
      XSLTTEMPLATES.put(sKey, template);
    }
    return template;
  }
  
  private Double getDouble(JSONObject jsoItem, String key) {
  	if (jsoItem.has(key) && (!jsoItem.isNull(key))) {
  		try {
				Double d = jsoItem.getDouble(key);
				return d;
			} catch (JSONException e) {}
  	}
  	return null;
  }
  
  /**
   * Gets the process summary.
   * @param forHtml true if the summary should be formatted for HTML
   * @return the process summary
   */
  private String getSummary(boolean forHtml) {
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
    
    int n = this.numUnsyncedExistingAtDestination+this.numOriginatedFromSynchronization;
    n += this.numWithNullId+this.numWithNullTitle+this.numWithNullType+this.numWithXsltError;
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
      if (this.numWithNullTitle > 0) {
        msg.append(" numWithNullTitle:").append(this.numWithNullTitle);
      }
      if (this.numWithNullType > 0) {
        msg.append(" withNullType:").append(this.numWithNullType);
      }
      if (this.numWithXsltError > 0) {
        msg.append(" numWithXsltError:").append(this.numWithXsltError);
      }
    }
    return msg.toString();
  }
  
  private void prepareItem(JSONObject jsoItem, AgpItem sourceItem) throws Exception {
  	
    String type = null, url = null;
    if (jsoItem.has("_links") && (!jsoItem.isNull("_links"))) {
      JSONArray jsoValues = jsoItem.getJSONArray("_links");
      for (int i=0;i<jsoValues.length();i++) {
        String s = Val.chkStr(jsoValues.getString(i));
        if (s.length() == 0) continue;
        //System.err.println("link:"+s);
				String[] p = s.split("\\?");
				if (p.length > 1) {
					String[] p2 = p[1].split("&");
					for (String kvp: p2) {
					  String[] p3 = kvp.split("=");
					  if (p3.length != 2) continue;
					  if (p3[0].equalsIgnoreCase("service")) {
							if (p3[1].equalsIgnoreCase("WMS")) {
							  //System.err.println("link-wms:"+s);
							  type = "WMS";
							  url = s;
							  break;
							}
					  }
					}
				}
				if (type != null) break;
      }
    } 
    // TODO: force type to null
    sourceItem.getProperties().add(new AgpProperty("type",null));
    if (type != null) {
      sourceItem.getProperties().add(new AgpProperty("type",type));
    }
    if ((url != null) && (url.length() > 0)) {
    	sourceItem.getProperties().add(new AgpProperty("url",url));
    }
    //sourceItem.getProperties().add(new AgpProperty("type","WMS"));
    //sourceItem.getProperties().add(new AgpProperty("url","http://urbanm.esri.com/wms"));
    
    // TODO 
    if (!jsoItem.has("extent") || (jsoItem.isNull("extent"))) {
      Double minX = getDouble(jsoItem,"_minX"), minY = getDouble(jsoItem,"_minY");
      Double maxX = getDouble(jsoItem,"_maxX"), maxY = getDouble(jsoItem,"_maxY");
      if ((minX != null) && (minY != null) && (maxX != null) && (maxY != null)) {
        String ext = minX+","+minY+","+maxX+","+maxY;
        sourceItem.getProperties().add(new AgpProperty("extent",ext));
      }    	
    }
    
    if (jsoItem.has("_thumbnailurl") && (!jsoItem.isNull("_thumbnailurl"))) {
    	String s = Val.chkStr(jsoItem.getString("_thumbnailurl"));
    	if (s.startsWith("http://") || (s.startsWith("https://"))) {
    		System.err.println("*** "+s);
    		sourceItem.getProperties().add(new AgpProperty("thumbnailurl",s));
    	}
    }

  }
  
  /**
   * Reads the context of the metadata XML column.
   * <br/>This is not applicable when the ArcIMS metadata server is active.
   * @param uuid the UUID for the record to read
   * @throws SQLException if a database exception occurs
   */
  private String readXml(Connection con, String uuid) throws SQLException {
    PreparedStatement st = null;
    try {
      String sql = "SELECT XML FROM "+this.resourceDataTable+" WHERE DOCUUID=?";
      st = con.prepareStatement(sql.toString());
      st.setString(1,uuid);
      ResultSet rs = st.executeQuery();
      if (rs.next()) {
        return this.mutator.get(rs,1);
      }
    } finally {
      try {if (st != null) st.close();} catch (Exception ef) {}
    }
    return null;
  }
  
  /**
   * Synchronize an item.
   * @param sourceItem the source item
   * @return <code>true</code> if item has been updated
   * @throws Exception if an exception occurs
   */
  protected boolean syncItem(AgpItem sourceItem, String uuid, String xml) throws Exception {
    this.numItemsConsidered++;
    GptSource src = this.source;
    AgpDestination dest = this.destination; 
    
    String sId = uuid.replaceAll("\\{","").replaceAll("}","").replaceAll("-","");
    sourceItem.getProperties().add(new AgpProperty("id",sId));
    String sType = Val.chkStr(sourceItem.getProperties().getValue("type"));
    String sTitle = Val.chkStr(sourceItem.getProperties().getValue("title"));
    String sMsg = "Processing item ("+this.numItemsConsidered+")";
    sMsg += ", id:"+Val.stripControls(sId)+", type:"+Val.stripControls(sType)+", title:"+Val.stripControls(sTitle);
    LOGGER.finer(sMsg);

    // check the item
    if (sTitle.length() == 0) {
    	this.numWithNullTitle++;
    	LOGGER.log(Level.FINEST,"No title for uuid="+uuid+"\r\n"+xml);
    	return false;
    } else if (sType.length() == 0) {
    	this.numWithNullType++;
    	LOGGER.log(Level.FINEST,"No type for uuid="+uuid+"\r\n"+xml);
    	return false;
    }
    
    /*
    if (sId == null) {
      this.numWithNullId++;
      LOGGER.finer("Ignoring item with null id: "+Val.stripControls(sTitle));
      return false;
    } else if (sType == null) {
      this.numWithNullType++;
      LOGGER.finer("Ignoring item with null type: "+Val.stripControls(sId)+" "+Val.stripControls(sTitle));
      return false;
    }
    *
    
    // TODO re-tweet issues
    
    /*
    // don't re-publish items when the exact itemId exists at the destination
    boolean bUnsyncedItemExists = this.itemHelper.doesUnsyncedItemExist(sourceItem,dest);
    if (bUnsyncedItemExists) {
      this.numUnsyncedExistingAtDestination++;
      String s = "Ignoring unsynced item existing at destination: ";
      LOGGER.finer(s+Val.stripControls(sId)+" "+Val.stripControls(sTitle));
      return false;
    }
    
    // don't propagate synced items from portal to portal
    boolean bIsSyncedItem = this.itemHelper.isSyncedItem(sourceItem);
    if (bIsSyncedItem) {
      this.numOriginatedFromSynchronization++;
      String s = "Ignoring, an item that originated from synchronization will not be repropagated: ";
      LOGGER.finer(s+Val.stripControls(sId)+" "+Val.stripControls(sTitle));
      return false;
    }
    */
    
       
    // make the destination item, determine if the item requires an update, publish
    // TODO: there will be problems if the item is no longer visible to this user
    AgpItem destItem = this.itemHelper.makeDestinationItem(null,sourceItem);
    
    // TODO: temporary
    if (Val.chkStr(sourceItem.getProperties().getValue("thumbnailurl")).length() > 0) {
    	destItem.getProperties().add(new AgpProperty("thumbnailurl",sourceItem.getProperties().getValue("thumbnailurl")));
    }
    
    //System.err.println(sourceItem.getProperties());
    //System.err.println(destItem.getProperties());
    boolean bRequiresUpdate = this.itemHelper.requiresUpdate(sourceItem,dest,destItem);
    if (this.forceUpdates || bRequiresUpdate) {
    	 //System.err.println("update.......................");
       this.execPublishItem(sourceItem,destItem);
       // TODO publish metadata
       //this.execPublishMetadata(sDestId,sXml);
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
    LOGGER.info("Gpt2AgpPush: Starting synchronization...");
    PreparedStatement st = null;
    try {
    	
    	XsltTemplate xslt = this.getCompiledTemplate(this.xsltLocation);
    	this.destination.getConnection().generateToken();
    	this.context = RequestContext.extract(null);
      ManagedConnection mc = this.context.getConnectionBroker().returnConnection("");
      this.con = mc.getJdbcConnection();
      this.mutator = mc.getClobMutator();
      this.resourceTable =  this.context.getCatalogConfiguration().getResourceTableName();
      this.resourceDataTable =  this.context.getCatalogConfiguration().getResourceDataTableName();
      	
    	String sql = "SELECT DOCUUID,UPDATEDATE,APPROVALSTATUS,PROTOCOL_TYPE,FINDABLE FROM "+this.resourceTable;
      st = this.con.prepareStatement(sql);
      ResultSet rs = st.executeQuery();     
      while (rs.next()) {
      	if (Thread.interrupted()) {
      		throw new InterruptedException("Interrupted while iterating ResultSet.");
      	}
      	AgpItem sourceItem = null;
      	String xml = null;
        String uuid = rs.getString(1);
        Timestamp mod = rs.getTimestamp(2);
        String status = rs.getString(3);
        String protocolType = Val.chkStr(rs.getString(4));
        boolean findable = Val.chkBool(rs.getString(5),false);
        boolean sync = (mod != null) && (status != null) && (status.equalsIgnoreCase("approved") || status.equalsIgnoreCase("reviewed"));
        if (sync && (protocolType.length() > 0) && !findable) sync = false;
        
        if (sync) {
        	sourceItem = new AgpItem();
        	xml = Val.chkStr(this.readXml(this.con,uuid));
        	//System.err.println(xml);
        	if ((xml != null) && (xml.length() > 0)) {
        		String result = Val.chkStr(xslt.transform(xml));
        		System.err.println(result);
        		if ((result != null) && (result.length() > 0)) {
        			try {
        				JSONObject jsoItem = new JSONObject(result); 
        				sourceItem.parseItem(jsoItem);
                sourceItem.getProperties().add(new AgpProperty("modified",""+mod.getTime()));
                this.prepareItem(jsoItem,sourceItem);
                //System.err.println("type:"+sourceItem.getProperties().getValue("type"));
                System.err.println(sourceItem.getProperties());
                this.syncItem(sourceItem,uuid,xml);
        			} catch (Exception ejson) {
        				this.numWithXsltError++;
        				LOGGER.log(Level.WARNING,"JSON failed to load for "+uuid+", json="+result);
        				LOGGER.log(Level.WARNING,"JSON failed with exception: ",ejson);
        			}
        		}
        	}
        }
      }
    	
    } catch (Exception e) {
      e.printStackTrace(System.err);
      throw e;
    } finally {
      try {if (st != null) st.close();} catch (Exception ef) {}
      try {
        if (this.context != null) {
          this.context.getConnectionBroker().closeAll();
        }
      } catch (Exception ef) {
        LOGGER.log(Level.WARNING,"JDBC connection failed to close.",ef);
      }
      this.context = null;
      try {
        if (this.destination != null) {
        	this.destination.getConnection().close();
        }
      } catch (Exception ef) {
      	LOGGER.log(Level.WARNING,"HTTP connection failed to close.",ef);
      }
      this.millisEnd = System.currentTimeMillis();
      LOGGER.info(this.getSummary(false));
    }
  }

}