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
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.sql.IClobMutator;
import com.esri.gpt.framework.sql.ManagedConnection;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.framework.xml.XsltTemplates;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
 * WMS based item: format title extent description/abstract
 * WMS based item: service=WMS is removed from the item URL
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
  private int numWithSyncError = 0;
  
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
  
  /**
   * Gets a double value.
   * @param jsoItem the JSON item
   * @param key the property key
   * @return the value
   */
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
    
    if (this.numMetadataPublished > 0) {
    	msg.append(" metadataPublished:").append(this.numMetadataPublished);
    }
    if (this.numRelationshipsAdded > 0) {
    	msg.append(" relationshipsAdded:").append(this.numRelationshipsAdded);
    }
    
    int n = this.numUnsyncedExistingAtDestination+this.numOriginatedFromSynchronization;
    n += this.numWithNullId+this.numWithNullTitle+this.numWithNullType+this.numWithXsltError+this.numWithSyncError;
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
      if (this.numWithSyncError > 0) {
        msg.append(" numWithSyncError:").append(this.numWithSyncError);
      }
    }
    return msg.toString();
  }
  
  /**
   * Prepare an item for publication.
   * @param jsoItem the JSON item
   * @param sourceItem the source item
   * @throws Exception if an exception occurs
   */
  private void prepareItem(JSONObject jsoItem, AgpItem sourceItem) throws Exception {
    String type = null, url = null;
    if (jsoItem.has("_links") && (!jsoItem.isNull("_links"))) {
      JSONArray jsoValues = jsoItem.getJSONArray("_links");
      for (int i=0;i<jsoValues.length();i++) {
        String s = Val.chkStr(jsoValues.getString(i));
        if (s.length() == 0) continue;
        //System.err.println("link:"+s);
				String[] p = s.split("\\?");
				if (p[0].indexOf("/MapServer/WMSServer") > 0) {
				  type = "WMS";
				  url = s;
				} else if (p.length > 1) {
					String[] p2 = p[1].split("&");
					for (String kvp: p2) {
					  String[] p3 = kvp.split("=");
					  if (p3.length != 2) continue;
					  if (p3[0].equalsIgnoreCase("service")) {
							if (p3[1].equalsIgnoreCase("WMS")) {
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
    
    sourceItem.getProperties().add(new AgpProperty("type",null));
    if (type != null) {
      sourceItem.getProperties().add(new AgpProperty("type",type));
    }
    if ((url != null) && (url.length() > 0)) {
    	sourceItem.getProperties().add(new AgpProperty("url",url));
    }
    
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
    		sourceItem.getProperties().add(new AgpProperty("thumbnailurl",s));
    	}
    }
  }
  
  /**
   * Reads the content of the metadata XML column.
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
    AgpDestination dest = this.destination; 
    
    String id = uuid.replaceAll("\\{","").replaceAll("}","").replaceAll("-","");
    sourceItem.getProperties().add(new AgpProperty("id",id));
    String type = Val.chkStr(sourceItem.getProperties().getValue("type"));
    String title = Val.chkStr(sourceItem.getProperties().getValue("title"));
    String msg = "Processing item ("+this.numItemsConsidered+")";
    msg += ", id:"+Val.stripControls(id)+", type:"+Val.stripControls(type)+", title:"+Val.stripControls(title);
    LOGGER.finer(msg);

    // check the item
    if (title.length() == 0) {
    	this.numWithNullTitle++;
    	LOGGER.log(Level.FINEST,"No title for uuid="+uuid+"\r\n"+xml);
    	return false;
    } else if (type.length() == 0) {
    	this.numWithNullType++;
    	LOGGER.log(Level.FINEST,"No type for uuid="+uuid+"\r\n"+xml);
    	return false;
    }
    
    // make the destination item, determine if the item requires an update, publish
    // TODO: there will be problems if the item is no longer visible to this user
    AgpItem destItem = this.itemHelper.makeDestinationItem(null,sourceItem);
    if (Val.chkStr(sourceItem.getProperties().getValue("thumbnailurl")).length() > 0) {
    	destItem.getProperties().add(new AgpProperty("thumbnailurl",sourceItem.getProperties().getValue("thumbnailurl")));
    }
    
    //System.err.println(sourceItem.getProperties());
    //System.err.println(destItem.getProperties());
    boolean bRequiresUpdate = this.itemHelper.requiresUpdate(sourceItem,dest,destItem);
    if (this.forceUpdates || bRequiresUpdate) {
    	boolean bPublish = false;
    	
      if (type.equals("WMS")) {
   	    WMS wms = new WMS();
   	    wms.readCapabilities(destItem);
   	    bPublish = wms.ok;
      }
      
      if (bPublish) {
        this.execPublishItem(sourceItem,destItem);
    	 
        // TODO publish metadata
        //this.execPublishMetadata(sDestId,sXml);
      }
      return bPublish;
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
    	//sql += " WHERE DOCUUID ='{E51DA72A-2600-4C2C-B50D-C859CF9BDFE9}'";
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
        boolean indexable = (status != null) && (status.equalsIgnoreCase("approved") || status.equalsIgnoreCase("reviewed"));
        boolean sync = (mod != null) && indexable;
        if (sync && (protocolType.length() > 0) && !findable) sync = false;
        
        if (sync) {
        	sourceItem = new AgpItem();
        	xml = Val.chkStr(this.readXml(this.con,uuid));
        	//System.err.println(xml);
        	if ((xml != null) && (xml.length() > 0)) {
        		String result = Val.chkStr(xslt.transform(xml));
        		//System.err.println(result);
        		if ((result != null) && (result.length() > 0)) {
        			JSONObject jsoItem = null;
        			try {
        				jsoItem = new JSONObject(result); 
        				sourceItem.parseItem(jsoItem);
        			} catch (Exception ejson) {
        				jsoItem = null;
        				this.numWithXsltError++;
        				LOGGER.log(Level.FINER,"JSON failed to load for "+uuid+", json="+result);
        				LOGGER.log(Level.FINER,"JSON failed with exception: ",ejson);
        			}
        			if (jsoItem != null) {
        				try {
	                sourceItem.getProperties().add(new AgpProperty("modified",""+mod.getTime()));
	                this.prepareItem(jsoItem,sourceItem);
	                //System.err.println("type:"+sourceItem.getProperties().getValue("type"));
	                //System.err.println(sourceItem.getProperties());
	                this.syncItem(sourceItem,uuid,xml);
	        			} catch (Exception esync) {
	        				this.numWithSyncError++;
	        				LOGGER.log(Level.WARNING,"Sync failed with exception for: "+uuid,esync);
	        			}
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
  
  /* ........................................................................ */
  
  /**
   * A WMS item.
   */
  private class WMS {
  	
  	private boolean ok = true;
  	
  	/**
  	 * Finds the GetMap node within a capabilities document.
  	 * @param root the root node
  	 * @return the GetMap node
  	 * @throws Exception if an exception occurs
  	 */
  	private Node findGetMapNode(Node root) throws Exception {
  		Node ndCapability = DomUtil.findFirst(root,"Capability");
  		if (ndCapability != null) {
  			Node ndRequest = DomUtil.findFirst(ndCapability,"Request");
  			if (ndRequest != null)
  			  return  DomUtil.findFirst(ndRequest,"GetMap");
  		}
  		return null;
  	}
  	
  	/**
  	 * Finds the GetMap url.
  	 * @param getMapNode the GetMap mode
  	 * @return the url
  	 * @throws Exception  if an exception occurs
  	 */
  	private String findGetMapUrl(Node getMapNode) throws Exception {
  		if (getMapNode != null) {
				Node[] dcps = DomUtil.findChildren(getMapNode,"DCPType");
				for (Node dcp: dcps) {
					Node[] https = DomUtil.findChildren(dcp,"HTTP");
  				for (Node ndHttp: https) {
  					Node ndGet = DomUtil.findFirst(ndHttp,"Get");
  					if (ndGet != null) {
  						Node ndRes = DomUtil.findFirst(ndGet,"OnlineResource");
  						if (ndRes != null) {
  							Node ndx = ndRes.getAttributes().getNamedItemNS("http://www.w3.org/1999/xlink","href");
  							if (ndx != null) {
  								return ndx.getNodeValue();
  							}
  						}
  					}
  				}
				}
  		}
  		return null;
  	}
  	
  	/**
  	 * Makes a GetCapabilities url.
  	 * @param url the item url
  	 * @return the capabilities url
  	 */
  	private String makeCapabilitiesUrl(String url) {
      url = rmParams(url);
      String base = url, q = "service=WMS&request=GetCapabilities";
      int idx = url.indexOf("?");
      if (idx != -1) {
      	base = url.substring(0,idx);
        String q2 = url.substring(idx+1);
        if (q2.length() > 0) {
        	q = q+"&"+q2;
        }
      }
      return base+"?"+q;
  	}
    
  	/**
  	 * Reads the capabilities.
  	 * @param destItem the destination item
  	 * @throws Exception if an exception occurs
  	 */
    private void readCapabilities(AgpItem destItem) throws Exception {
      	
    	String url = Val.chkStr(destItem.getProperties().getValue("url"));
    	//url = "http://54.176.223.239/server/services/SampleWorldCities/MapServer/WMSServer?request=GetCapabilities&service=WMS";
	
    	String capabilitiesUrl = this.makeCapabilitiesUrl(url);
    	System.err.println("capabilitiesUrl="+capabilitiesUrl);
    	HttpClientRequest http = new HttpClientRequest();
    	http.setUrl(capabilitiesUrl);
    	String xml = http.readResponseAsCharacters();
    	//System.err.println("xml:\r\n"+xml);
    	
    	JSONObject data = new JSONObject();
    	data.put("version","1.3.0");
    	data.put("title","");
    	data.put("url","");
    	data.put("mapUrl","");
    	data.put("copyright","");
    	data.put("maxWidth",5000);
    	data.put("maxHeight",5000);
    	data.put("format",JSONObject.NULL);
    	data.put("layers",new JSONArray());
    	data.put("spatialReferences",new JSONArray());
    	List<Integer> allSpatialReferences = new ArrayList<Integer>();
    	
    	Document dom = DomUtil.makeDomFromString(xml,true);
    	Node root = dom.getDocumentElement();
    	if (root != null) {
    		String ln = Val.chkStr(root.getNodeName());
    		if (ln.equals("WMT_MS_Capabilities")) {
    		} else if (ln.equals("WMS_Capabilities")) {
    		} else {
    			root = null;
    			throw new Exception("WMS has no known root node "+capabilitiesUrl);
    		}
    	}
    	if (root == null) return;
    	
    	String s; int n;
    	
    	String version = null;
    	Node ndVersion = root.getAttributes().getNamedItem("version");
    	if (ndVersion != null) version = ndVersion.getNodeValue();
    	if (version == null || version.length() == 0) version = "1.3.0";
    	data.put("version",version);    
    	
    	// TODO removing service=WMS from url?
    	String sUrl2 = rmParams(url);
    	data.put("url",sUrl2);
    	destItem.getProperties().get("url").setValue(sUrl2);
    	
    	Node ndGetMap = this.findGetMapNode(root);
    	String sGetMapUrl = Val.chkStr(this.findGetMapUrl(ndGetMap));
    	if (sGetMapUrl != null) {
    		sGetMapUrl = rmParams(sGetMapUrl);
    		data.put("mapUrl",sGetMapUrl);
    	} else {
    		throw new Exception("WMS has no GetMap endpoint "+capabilitiesUrl);
    	}
    	
    	Node ndService = DomUtil.findFirst(root,"Service");
    	if (ndService != null) {
    		String name = null, title = null;
        NodeList nl = ndService.getChildNodes(); 
        for (int i=0; i<nl.getLength(); i++) {
        	Node nd = nl.item(i);
        	if (nd.getNodeType() == Node.ELEMENT_NODE) { 
        		String ln2 = Val.chkStr(nd.getNodeName());
        		if (ln2.equalsIgnoreCase("Name")) {
        			s = Val.chkStr(nd.getTextContent());
        			if (s.length() > 0) name = s;
        		} else if (ln2.equalsIgnoreCase("Title")) {
        			s = Val.chkStr(nd.getTextContent());
        			if (s.length() > 0) {
        				title = s;
        				data.put("title",s);
        			}
        		} else if (ln2.equalsIgnoreCase("Abstract")) {
        			//s = Val.chkStr(nd.getTextContent());
        		} else if (ln2.equalsIgnoreCase("AccessConstraints")) {
        			s = Val.chkStr(nd.getTextContent());
        			if (s.length() > 0) data.put("copyright",s);
        		} else if (ln2.equalsIgnoreCase("MaxWidth")) {
        			s = Val.chkStr(nd.getTextContent());
        			if (s.length() > 0) {
        				n = Val.chkInt(s,-1);
        				if (n > 0) data.put("maxWidth",n);
        			}
        		} else if (ln2.equalsIgnoreCase("MaxHeight")) {
        			s = Val.chkStr(nd.getTextContent());
        			if (s.length() > 0) {
        				n = Val.chkInt(s,-1);
        				if (n > 0) data.put("maxHeight",n);
        			}
        		}
        	}
        }
        if ((title == null) && (name != null)) {
        	data.put("title",name);
        } 
    	}
    	
			/*
    	if (ndGetMap != null) {
  			Node[] fmts = DomUtil.findChildren(ndGetMap,"Format");
  			for (Node fmt: fmts) {
  				String s = Val.chkStr(fmt.getTextContent());
  				if (s.length() > 0) {
  					// check the formats ???
  					//System.err.println("**************format="+s);
  		      // make sure the format we want is supported; otherwise switch
  		      //if (!array.some(this.getMapFormats, function(el){
  		        // also support: <Format>image/png; mode=24bit</Format>
  		      //  return el.indexOf(this.imageFormat) > -1;
  		      //}, this)) {
  		      //  this.imageFormat = this.getMapFormats[0];
  		      //}
  				}
  			}    		
    	}
    	*/
    	
    	Node ndCapability = DomUtil.findFirst(root,"Capability");
    	if (ndCapability != null) {
    		JSONArray jsaLayers = data.getJSONArray("layers");
    		JSONArray jsaRefs = data.getJSONArray("spatialReferences");
				Node[] layerNodes = DomUtil.findChildren(ndCapability,"Layer");
				for (Node ndLayer: layerNodes) {
    			// TODO can there be more than one main layer?
        	WMSLayerInfo layer = new WMSLayerInfo();
        	layer.parse(ndLayer,allSpatialReferences);
        	if (layer.subLayers.size() == 0) {
        		layer.append(jsaLayers);
        	} else {
        		for (WMSLayerInfo subLayer: layer.subLayers) {
        			subLayer.append(jsaLayers);
        		}
        	}
				}
				for (int ref: allSpatialReferences) {
					jsaRefs.put(ref);
				}
    	}
         	
    	String text = data.toString(1);
    	destItem.getProperties().add(new AgpProperty("text",text));
    	//System.err.println("----text----\r\n"+text);
    }
    
    /**
     * Removes several WMS related parameters from a url.
     * @param url the url
     * @return the modified url
     */
  	private String rmParams(String url) {
  		String[] rm = {"version", "service", "request", "bbox", "format", "height", "width", "layers", 
  				"srs", "crs", "styles", "transparent", "bgcolor", "exceptions", "time", "elevation", "sld", "wfs"};
  		boolean wasModified = false;
      int idx = url.indexOf("?");
      if (idx != -1) {
      	String base = url.substring(0,idx);
      	StringBuilder sbq = new StringBuilder();
        String queryString = url.substring(idx+1);
        String[] pairs = queryString.split("&");
        for (String pair: pairs) {
        	boolean bAdd = true;
          idx = pair.indexOf("=");
          if (idx > 0) {
            String key = pair.substring(0,idx);
            //String value = pair.substring(idx+1);
            for (String s: rm) {
            	if (key.equalsIgnoreCase(s)) {
            		bAdd = false;
            		break;
            	}
            }
          } 
          if (bAdd) {
          	if (sbq.length() > 0) sbq.append("&");
          	sbq.append(pair);
          } else {
          	wasModified = true;
          }
        }
        if (wasModified) {
        	if (sbq.length() > 0) {
        		return base+"?"+sbq.toString();
        	} else {
        		return base;
        	}
        }
      }
      return url;
  	}
  	 
  }
  
  /**
   * A WMS layer.
   */
  private class WMSLayerInfo {
  	
  	private String name;
  	private String title;
  	@SuppressWarnings("unused")
  	private String description;
  	private String legendURL;
  	//Extent extent;
    //List<Extent> allExtents = new ArrayList<Extent>();
  	private List<WMSLayerInfo> subLayers = new ArrayList<WMSLayerInfo>();
  	private List<Integer> spatialReferences = new ArrayList<Integer>();
  	
  	/**
  	 * Appends info to an array of layers.
  	 * @param layers the array of layers
  	 * @throws Exception if an exception occurs
  	 */
  	private void append(JSONArray layers) throws Exception {
  		JSONObject layer = new JSONObject();
  		if ((this.name != null) && (this.name.length() > 0)) {
  			layer.put("name",this.name);
  		} else {
  			return;
  		}
  		if ((this.title != null) && (this.title.length() > 0)) {
  			layer.put("title",this.title);
  		}
  		if ((this.legendURL != null) && (this.legendURL.length() > 0)) {
  			layer.put("legendURL",this.legendURL);
  		}
  		layers.put(layer);
  	}
  	
  	/**
  	 * Parses a WMS layer node.
  	 * @param layerNode the layer node
  	 * @param allSpatialReferences the list of spatial references for the service
  	 * @throws Exception if an exception occurs
  	 */
  	private void parse(Node layerNode, List<Integer> allSpatialReferences) throws Exception {

      /*  		
  		// all services have LatLonBoundingBox or EX_GeographicBoundingBox (might not be on the first layer ...)
			Node ndLLBB = DomUtil.findFirst(layerNode,"LatLonBoundingBox");
			Node ndGeoBB = DomUtil.findFirst(layerNode,"EX_GeographicBoundingBox");
			if (ndLLBB != null) {
        //var minx = parseFloat(boundsXML.getAttribute("minx"));
        //var miny = parseFloat(boundsXML.getAttribute("miny"));
        //var maxx = parseFloat(boundsXML.getAttribute("maxx"));
        //var maxy = parseFloat(boundsXML.getAttribute("maxy"));
			}
			if (ndGeoBB != null) {
        //extent.xmin = parseFloat(this._getTagValue("westBoundLongitude", geographicBoundingBox, 0));
        //extent.ymin = parseFloat(this._getTagValue("southBoundLatitude", geographicBoundingBox, 0));
        //extent.xmax = parseFloat(this._getTagValue("eastBoundLongitude", geographicBoundingBox, 0));
        //extent.ymax = parseFloat(this._getTagValue("northBoundLatitude", geographicBoundingBox, 0));        	
			}
      //if (!latLonBoundingBox && !geographicBoundingBox) {
        // not according to spec
        //extent = new Extent(-180, -90, 180, 90, new SpatialReference({
          //wkid: 4326
        //}));
        //result.allExtents[0] = extent;
      //}
      */
			
    	String s;
      NodeList nl = layerNode.getChildNodes(); 
      for (int i=0; i<nl.getLength(); i++) {
        Node nd = nl.item(i);
        if (nd.getNodeType() != Node.ELEMENT_NODE) continue;
        String ln = Val.chkStr(nd.getNodeName());
        
        if (ln.equalsIgnoreCase("Name")) {
    			s = Val.chkStr(nd.getTextContent());
    			if (s.length() > 0) this.name = s;
        } else if (ln.equalsIgnoreCase("Title")) {
    			s = Val.chkStr(nd.getTextContent());
    			if (s.length() > 0) this.title = s;
        } else if (ln.equalsIgnoreCase("Abstract")) {
    			s = Val.chkStr(nd.getTextContent());
    			if (s.length() > 0) this.description = s;	

        } else if (ln.equalsIgnoreCase("BoundingBox")) {
        	
        } else if (ln.equalsIgnoreCase("SRS") || ln.equalsIgnoreCase("CRS")) {
          // supported spatial references
          // <SRS>EPSG:4326</SRS> or <SRS>EPSG:4326 EPSG:32624 EPSG:32661</SRS>
        	s = Val.chkStr(nd.getTextContent());
        	String[] a = s.split(" ");
        	for (String s2: a) {
        		boolean isCRS = ln.equalsIgnoreCase("CRS");
        		String s3 = null;
        		if (s2.indexOf(":") != -1) {
        			String[] a2 = s2.split(":");
        			if (a2.length == 2) {
        				s3 = Val.chkStr(a2[1]);
        				isCRS = a2[0].equalsIgnoreCase("CRS");
        			}
        		} else {
        			s3 = Val.chkStr(s2);
        		}
        		int v = 0;
        		if ((s3 != null) && (s3.length() > 0)) {
        			v = Val.chkInt(s3,0);
        		}
        		if (v > 0) {
        			if (isCRS) {
          			if (v == 84) v = 4326;
          			else if (v == 83) v = 4269;
          			else if (v == 27) v = 4267;        				
        			}
        			if (!this.spatialReferences.contains(v)) {
        				this.spatialReferences.add(v);
        			}
        			if (!allSpatialReferences.contains(v)) {
        				allSpatialReferences.add(v);
        			}
        		}
        	}
        	
        } else if (ln.equalsIgnoreCase("Style")) {
    			Node ndUrl = DomUtil.findFirst(nd,"LegendURL");
    			if (ndUrl != null) {
						Node ndRes = DomUtil.findFirst(ndUrl,"OnlineResource");
						if (ndRes != null) {
							Node ndx = ndRes.getAttributes().getNamedItemNS("http://www.w3.org/1999/xlink","href");
							if (ndx != null) {
								s = Val.chkStr(ndx.getNodeValue());
								if (s.length() > 0) this.legendURL = s;
							}
						}
    			}
        	
        } else if (ln.equalsIgnoreCase("Layer")) {
        	WMSLayerInfo subLayer = new WMSLayerInfo();
        	subLayer.parse(nd,allSpatialReferences);
        	this.subLayers.add(subLayer);
        }
      }
      
      if ((this.title == null) || (this.title.length() == 0)) {
      	this.title = this.name;
      }
    }
  	
  }

}