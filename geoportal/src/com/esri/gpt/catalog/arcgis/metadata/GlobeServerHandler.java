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
import com.esri.arcgisws.GlobeLayerInfo;
import com.esri.arcgisws.GlobeServerBindingStub ;

/**
 * Handles the collection of metadata for an ArcGIS globe service (GlobeServer).
 */
public class GlobeServerHandler extends ServiceHandler {
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public GlobeServerHandler() {
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
    GlobeServerBindingStub stub = 
      getCredentials()==null || getCredentials().getUsername().length()==0 || getCredentials().getPassword().length()==0?
      new GlobeServerBindingStub(serviceInfo.getSoapUrl()):
      new GlobeServerBindingStub(serviceInfo.getSoapUrl(), getCredentials().getUsername(), getCredentials().getPassword());
    serviceInfo.getKeywords().add(serviceInfo.getType());
    serviceInfo.addKeywords("liveData,service,globe",",");
    
    // TODO: layer names and envelope
    GlobeLayerInfo[] layers = stub.getLayerInfos(0);
    if (layers != null) {
      for (GlobeLayerInfo layer: layers) {
        serviceInfo.addRDFPair("service.layername",layer.getName());
        //System.err.println("layerName="+layer.getName());
        //System.err.println("  layerType="+layer.getLayerType());
        //System.err.println("  layerDescription="+layer.getDescription());
      }
    } 
  }
  
}

