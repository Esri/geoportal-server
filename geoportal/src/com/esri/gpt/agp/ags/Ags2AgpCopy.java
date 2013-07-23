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
package com.esri.gpt.agp.ags;

import com.esri.arcgisws.EnvelopeN;
import com.esri.gpt.agp.client.AgpClient;
import com.esri.gpt.agp.client.AgpConnection;
import com.esri.gpt.agp.client.AgpItem;
import com.esri.gpt.agp.client.AgpItemListener;
import com.esri.gpt.agp.client.AgpProperties;
import com.esri.gpt.agp.client.AgpProperty;
import com.esri.gpt.agp.client.AgpSearchCriteria;
import com.esri.gpt.agp.client.AgpSearchRequest;
import com.esri.gpt.agp.client.AgpUtil;
import com.esri.gpt.agp.multipart2.MultipartProvider;
import com.esri.gpt.agp.sync.AgpDestination;
import com.esri.gpt.agp.sync.AgpItemHelper;
import com.esri.gpt.agp.sync.AgpPartHelper;
import com.esri.gpt.catalog.arcgis.metadata.IServiceInfoProvider;
import com.esri.gpt.catalog.arcgis.metadata.ServiceInfo;
import com.esri.gpt.catalog.arcgis.metadata.ServiceInfoProviderAdapter;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.client.arcgis.ArcGISInfo;
import com.esri.gpt.control.webharvest.client.arcgis.ArcGISQueryBuilder;
import com.esri.gpt.control.webharvest.common.CommonCriteria;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.resource.adapters.FlatResourcesAdapter;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import com.esri.gpt.framework.util.Val;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * Copies from ArcGIS Server to Portal for ArcGIS.
 */
public class Ags2AgpCopy {
  private AgpItemHelper  itemHelper = new AgpItemHelper(); 
  private AgpPartHelper  partHelper = new AgpPartHelper(); 
  private static final Logger LOGGER = Logger.getLogger(Ags2AgpCopy.class.getCanonicalName());
  
  private ArcGISInfo source;
  private AgpDestination destination;
  private int numItemsConsidered;
  private int numWithNullType;
  
  private static final HashMap<String,String> agsToAgpType = new HashMap<String, String>();
  
  static {
    agsToAgpType.put("MapServer", "Map Service");
    agsToAgpType.put("ImageServer", "Image Service");
    agsToAgpType.put("FeatureServer", "Feature Service");
    agsToAgpType.put("WFSServer", "Feature Service");
    agsToAgpType.put("WMSServer", "WMS");
    agsToAgpType.put("GPServer", "Geoprocessing Service");
    agsToAgpType.put("NASServer", "Network Analysis Service");
    agsToAgpType.put("GeometryServer", "Geometry Service");
  }
  
  /**
   * Creates instance of the class.
   * @param source source
   * @param destination destination
   */
  public Ags2AgpCopy(ArcGISInfo source, AgpDestination destination) {
    this.source = source;
    this.destination = destination;
  }
  
  /**
   * Executes copy action.
   * @throws Exception if anything fails
   */
  public void copy() throws Exception {
    RequestContext requestContext = RequestContext.extract(null);
    try {

      ArcGISQueryBuilder qb = new ArcGISQueryBuilder(new IterationContext() {
        @Override
        public void onIterationException(Exception ex) {
          LOGGER.log(Level.SEVERE, "Error iterating through AGS resources.", ex);
        }
      }, source);
      
      Query newQuery = qb.newQuery(new CommonCriteria());
      Result result = newQuery.execute();

      this.destination.getConnection().generateToken();
      
      Iterable<IServiceInfoProvider> records = new ServiceInfoProviderAdapter(new FlatResourcesAdapter(result.getResources()));
      for (IServiceInfoProvider r: records) {
        ServiceInfo serviceInfo = r.getServiceInfo();
        AgpItem agpItem = createAgpItem(serviceInfo);
        if (agpItem!=null) {
          syncItem(agpItem);
        }
      }

    } finally {
      requestContext.onExecutionPhaseCompleted();
    }
  }
  
  private AgpItem createAgpItem(ServiceInfo serviceInfo) {
    if (serviceInfo!=null) {
      AgpItem agpItem = new AgpItem();
      AgpProperties props = agpItem.getProperties();
      String type = serviceInfo.getType();
      
      props.add(new AgpProperty("title", Val.chkStr(serviceInfo.getName())));
      props.add(new AgpProperty("name", Val.chkStr(serviceInfo.getName())));
      props.add(new AgpProperty("url", Val.chkStr(serviceInfo.getRestUrl())));
      props.add(new AgpProperty("description", Val.chkStr(serviceInfo.getDescription())));
      props.add(new AgpProperty("thumbnailurl", Val.chkStr(serviceInfo.getThumbnailUrl())));
      if (serviceInfo.getEnvelope() instanceof EnvelopeN) {
        EnvelopeN e = (EnvelopeN) serviceInfo.getEnvelope();
        String envelope = ""+e.getXMin()+","+e.getYMin()+","+e.getXMax()+","+e.getYMax();
        Integer wkid = e.getSpatialReference().getWKID();
        props.add(new AgpProperty("extent", envelope));
        props.add(new AgpProperty("spatialreference", "{wkid: " +wkid+ "}"));
      }
      
      String agpType = agsToAgpType.get(type);
      if (agpType!=null) {
        props.add(new AgpProperty("type", agpType));
        return agpItem;
      }
    }
    return null;
  }
  
  private boolean syncItem(AgpItem sourceItem) throws Exception  {
    this.numItemsConsidered++;
    
    String sType = sourceItem.getProperties().getValue("type");
    String sTitle = sourceItem.getProperties().getValue("title");
    String sMsg = "Processing item ("+this.numItemsConsidered+")";
    sMsg += ", type:"+sType+", title:"+sTitle;
    LOGGER.info(sMsg);

    if (sType == null) {
      this.numWithNullType++;
      LOGGER.finer("Ignoring item with null type: "+sTitle);
      return false;
    }

    AgpItem destinationItem = queryDestinationItem(sourceItem);
    execPublishItem(sourceItem,destinationItem);
    
    return true;
  }
  
  private void execPublishItem(AgpItem sourceItem,AgpItem destItem) throws Exception {
    AgpDestination dest = this.destination;
    String sDestId = destItem!=null? destItem.getProperties().getValue("id"): null;
    String sTitle = sourceItem.getProperties().getValue("title");
    LOGGER.finer("Publishing item: "+sTitle);
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.finest(sourceItem.getProperties().toString());
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
    for (AgpProperty destProp: sourceItem.getProperties().values()) {
      provider.add(destProp.getName(),destProp.getValue());
    }
    //this.partHelper.addThumbnailPart(provider,src,sourceItem,dest,destItem);
    //this.partHelper.addDataPart(provider,src,sourceItem,dest,destItem);
        
    // execute
    AgpProperties hdr = dest.getConnection().makeRequestHeaderProperties();
    AgpClient client = dest.getConnection().ensureClient();
    JSONObject jso = client.executeJsonRequest(sUrl,hdr,provider);
    
    if (jso.has("id") && jso.has("success") && jso.getString("success").equals("true")) {
      if (sDestId == null) {
        sDestId = jso.getString("id");
        sourceItem.getProperties().add(new AgpProperty("id",sDestId));
      }  
      /*
      if (bInsert) {
        this.numItemsInserted++; 
        LOGGER.finer("Item inserted: "+sUrl);
      } else {
        this.numItemsUpdated++;
        LOGGER.finer("Item updated: "+sUrl);
      }
      */
    } else {
      LOGGER.finer("Publish item FAILED for: "+sUrl);
      // TODO: throw exception here??
    }
  }
  
  /**
   * Checks if the item already exists.
   * @param sourceItem source item
   * @return destination item or <code>null</code> if destination item doesn't exist.
   * @throws Exception if accessing destination fails
   */
  private AgpItem queryDestinationItem(AgpItem sourceItem) throws Exception {
    
    final AgpItem destinationItem = new AgpItem();
    destinationItem.setProperties(null);
    
    String sQuery = "url:\""+sourceItem.getProperties().getValue("url")+"\"";
    
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
        }
      }
    );
    if (destinationItem.getProperties() != null) {
      return destinationItem;
    }
    return null;
  }
}
