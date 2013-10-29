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
package com.esri.gpt.catalog.arcgis.metadata;
import com.esri.gpt.framework.util.Val;

import com.esri.arcgisws.MapLayerInfo;
import com.esri.arcgisws.MapServerBindingStub;
import com.esri.arcgisws.MapServerInfo;
import com.esri.arcgisws.PropertySet;
import com.esri.arcgisws.PropertySetProperty;
import com.esri.arcgisws.ServiceDescription;
import com.esri.arcgisws.runtime.exception.ArcGISWebServiceException;
import com.esri.gpt.framework.resource.api.Resource;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Handles the collection of metadata for an ArcGIS map service (MapServer).
 */
public class MapServerHandler extends ServiceHandler {
  private static final Logger LOGGER = Logger.getLogger(MapServerHandler.class.getCanonicalName());
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public MapServerHandler() {
    super();
  } 
  
  /** methods ================================================================= */
  
  /**
   * Collect metadata associated with a service.
   * @param handlerFactory handler factory
   * @param serviceInfo the active service information
   * @throws Exception if an exception occurs
   */
  @Override
  public void collectMetadata(ServiceHandlerFactory handlerFactory, ServiceInfo serviceInfo)
    throws Exception {
    MapServerBindingStub stub =
      getCredentials()==null || getCredentials().getUsername().length()==0 || getCredentials().getPassword().length()==0?
      new MapServerBindingStub(serviceInfo.getSoapUrl()):
      new MapServerBindingStub(serviceInfo.getSoapUrl(), getCredentials().getUsername(), getCredentials().getPassword());
    serviceInfo.getKeywords().add(serviceInfo.getType());
    serviceInfo.addKeywords("liveData,service",",");
    
    MapServerInfo mapInfo = stub.getServerInfo(stub.getDefaultMapName());
    
    //serviceInfo.setName(mapInfo.getName());
    serviceInfo.setDescription(mapInfo.getDescription());
    serviceInfo.setEnvelope(mapInfo.getFullExtent());
    //serviceInfo.setEnvelope(mapInfo.getExtent());
    
    /*
    System.err.println("nm="+mapInfo.getDefaultMapDescription().getName());
    System.err.println("ld="+mapInfo.getDefaultMapDescription().getLayerDescriptions());
    System.err.println("     getDefaultMapName()="+service.getDefaultMapName());
    System.err.println("     getCopyrightText()="+mapinfo.getCopyrightText());
    System.err.println("     getMapLayerInfos()="+mapinfo.getMapLayerInfos());
    System.err.println("     getSpatialReference()="+mapinfo.getSpatialReference());
    System.err.println("     getDefaultMapDescription()="+mapinfo.getDefaultMapDescription());
     */
    
    // thumbnail
    String thumbnailUrl = Val.chkStr(serviceInfo.getRestUrl());
    if (thumbnailUrl.length() > 0) {
      if (!thumbnailUrl.endsWith("/MapServer")) {
        int idx = thumbnailUrl.lastIndexOf("/MapServer/");
        if (idx != -1) {
          thumbnailUrl = thumbnailUrl.substring(0,idx+10);
        }
      }
      if (thumbnailUrl.endsWith("/MapServer")) {
        serviceInfo.setThumbnailUrl(thumbnailUrl+"/export?size=256,256&f=image");
      }
    }
    
    
    
    // MXD properties
    PropertySet docInfo = stub.getDocumentInfo();
    if (docInfo != null) {
      PropertySetProperty[] psps = docInfo.getPropertyArray();
      if (psps != null) {
        for (PropertySetProperty psp: psps) {
          String key = Val.chkStr(psp.getKey());
          String value = "";
          Object oVal = psp.getValue();
          if ((oVal != null) && (oVal instanceof String)) {
            value = Val.chkStr((String)oVal);
          }
          if ((key.length() > 0) && (value.length() > 0)) {
            //System.err.println(key+"=="+value);
            
            if (key.equalsIgnoreCase("Title")) {
              serviceInfo.setName(value);
            } else if (key.equalsIgnoreCase("Author")) {
              serviceInfo.setCreator(value);
            } else if (key.equalsIgnoreCase("Comments")) {
              serviceInfo.addRDFPair("mxd.comments",value);
            } else if (key.equalsIgnoreCase("Subject")) {
              serviceInfo.addRDFPair("mxd.subject",value);
            } else if (key.equalsIgnoreCase("Category")) {
              serviceInfo.addRDFPair("mxd.category",value);
            } else if (key.equalsIgnoreCase("Keywords")) {
              serviceInfo.addKeywords(value,",");
            } else {
            }
             
          }
        }
      }
    }
    
    // layers
    MapLayerInfo[] layers = mapInfo.getMapLayerInfos();
    if (layers != null) {
      for (MapLayerInfo layer: layers) {
        serviceInfo.addRDFPair("service.layername",layer.getName());
        /*
        System.err.println("layerName="+layer.getName());
        System.err.println("  layerType="+layer.getLayerType());
        System.err.println("  layerDescription="+layer.getDescription());
        System.err.println("  layerSourceDescription="+layer.getSourceDescription());
        System.err.println("  layerIsComposite="+layer.isIsComposite());
        System.err.println("  layerIsFeatureLayer="+layer.isIsFeatureLayer());
        */
      }
      //for (MapLayerInfo layer: layers) {
      //  serviceInfo.addRDFPair("service.layertype",layer.getLayerType());
      //}
    }  
    
  }

  /*
  @Override
  public void appendRecord(Collection<Resource> records, ServiceHandlerFactory factory, ServiceInfo serviceInfo, boolean isNative) throws Exception {
    try {
      MapServerBindingStub stub =
        getCredentials()==null || getCredentials().getUsername().length()==0 || getCredentials().getPassword().length()==0?
        new MapServerBindingStub(serviceInfo.getSoapUrl()):
        new MapServerBindingStub(serviceInfo.getSoapUrl(), getCredentials().getUsername(), getCredentials().getPassword());
      MapServerInfo mapInfo = stub.getServerInfo(stub.getDefaultMapName());
      serviceInfo.setEnvelope(mapInfo.getFullExtent());
      
      MapLayerInfo[] mapLayerInfos = mapInfo.getMapLayerInfos();
      for (MapLayerInfo li: mapLayerInfos) {
        if (!li.isIsFeatureLayer()) continue;
        String name = Integer.toString(li.getLayerID());
        String title = li.getName();
        serviceInfo.getLayersInfo().add(new ServiceInfo.LayerInfo(name, title));
      }
      
      String copyrightText = mapInfo.getCopyrightText();
      serviceInfo.setCopyright(copyrightText);
      
    } catch (ArcGISWebServiceException ex) {
      LOGGER.log(Level.FINE, "Error getting MapServerInfo.", ex);
    }
    super.appendRecord(records, factory, serviceInfo, isNative);
  }
  */

  @Override
  public ServiceInfo createServiceInfo(ServiceInfo parentInfo, ServiceDescription desc, String currentRestUrl, String currentSoapUrl) {
    ServiceInfo serviceInfo = super.createServiceInfo(parentInfo, desc, currentRestUrl, currentSoapUrl);
    try {
      MapServerBindingStub stub =
        getCredentials()==null || getCredentials().getUsername().length()==0 || getCredentials().getPassword().length()==0?
        new MapServerBindingStub(serviceInfo.getSoapUrl()):
        new MapServerBindingStub(serviceInfo.getSoapUrl(), getCredentials().getUsername(), getCredentials().getPassword());
      MapServerInfo mapInfo = stub.getServerInfo(stub.getDefaultMapName());
      serviceInfo.setEnvelope(mapInfo.getFullExtent());
      
      MapLayerInfo[] mapLayerInfos = mapInfo.getMapLayerInfos();
      for (MapLayerInfo li: mapLayerInfos) {
        if (!li.isIsFeatureLayer()) continue;
        String name = Integer.toString(li.getLayerID());
        String title = li.getName();
        serviceInfo.getLayersInfo().add(new ServiceInfo.LayerInfo(name, title));
      }
      
      String copyrightText = mapInfo.getCopyrightText();
      serviceInfo.setCopyright(copyrightText);
      
    } catch (ArcGISWebServiceException ex) {
      LOGGER.log(Level.FINE, "Error getting MapServerInfo.", ex);
    }
    return serviceInfo;
  }
  
}

