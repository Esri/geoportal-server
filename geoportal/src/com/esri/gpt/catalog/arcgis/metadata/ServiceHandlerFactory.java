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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A factory for instantiating handlers capabable of processing specific 
 * ArcGIS Server service types.
 */
public class ServiceHandlerFactory {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static Logger LOGGER = Logger.getLogger(ServiceHandlerFactory.class.getName());
  
  private Map<String,String> handlers = new HashMap<String,String>();
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public ServiceHandlerFactory() {
    
    /*
     ServiceCatalogBindingStub
     
     GeocodeServerBindingStub
     GeoDataServerBindingStub
     GeometryServerBindingStub
     GlobeServerBindingStub
     GPServerBindingStub
     ImageServerBindingStub
     MapServerBindingStub
     MobileServerBindingStub
     NAServerBindingStub
    
     WMSServer
     WFSServer
     WCSServer
      
     KMLServer
      
    */
    
    handlers.put("mapserver","com.esri.gpt.catalog.arcgis.metadata.MapServerHandler");
    handlers.put("globeserver","com.esri.gpt.catalog.arcgis.metadata.GlobeServerHandler");
    handlers.put("imageserver","com.esri.gpt.catalog.arcgis.metadata.ImageServerHandler");
    handlers.put("geodataserver","com.esri.gpt.catalog.arcgis.metadata.GeoDataServerHandler");
    handlers.put("mobileserver","com.esri.gpt.catalog.arcgis.metadata.MobileServerHandler");    
    
    
    handlers.put("gpserver","com.esri.gpt.catalog.arcgis.metadata.GPServerHandler");
    handlers.put("geocodeserver","com.esri.gpt.catalog.arcgis.metadata.GeocodeServerHandler");
    handlers.put("geometryserver","com.esri.gpt.catalog.arcgis.metadata.GeometryServerHandler");
    handlers.put("naserver","com.esri.gpt.catalog.arcgis.metadata.NAServerHandler");
    
    
    handlers.put("wmsserver","com.esri.gpt.catalog.arcgis.metadata.WMSServerHandler");
    handlers.put("wfsserver","com.esri.gpt.catalog.arcgis.metadata.WFSServerHandler");
    handlers.put("wcsserver","com.esri.gpt.catalog.arcgis.metadata.WCSServerHandler");
    
    //handlers.put("searchserver","com.esri.gpt.catalog.arcgis.metadata.SearchServerHandler");
    handlers.put("featureserver","com.esri.gpt.catalog.arcgis.metadata.FeatureServerHandler");
    
  }
  
  /**
   * Instantiates a handler capabable of processing a specific ArcGIS Server service type.
   * @param serviceType the ArcGIS Server service type
   * @return the service handler (can be null)
   */
  public ServiceHandler makeHandler(String serviceType) {
    if ((serviceType != null) && (serviceType.length() > 0)) {
      String className = this.handlers.get(serviceType.toLowerCase());
      if ((className != null) && (className.length() > 0)) {
        String errMsg = "Error instantiating AGS ServiceHandler: type="+serviceType+", class="+className;
        try {
          Class<?> cls = Class.forName(className);
          Object obj = cls.newInstance();
          if (obj instanceof ServiceHandler) {
            return (ServiceHandler)obj;
          } else {
            String msg = className+" is not an instance of "+ServiceHandler.class.getName();
            throw new InstantiationException(msg);
          }
        } catch (ClassNotFoundException e) {
          LOGGER.log(Level.WARNING,errMsg,e);
        } catch (InstantiationException e) {
          LOGGER.log(Level.WARNING,errMsg,e);
        } catch (IllegalAccessException e) {
          LOGGER.log(Level.WARNING,errMsg,e);
        }
        
      }
    }
    return null;
  }

}
