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
import com.esri.arcgisws.GPServerBindingStub ;
import com.esri.arcgisws.GPToolInfo;

/**
 * Handles the collection of metadata for an ArcGIS geoprocessing service (GPServer).
 */
public class GPServerHandler extends ServiceHandler {
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public GPServerHandler() {
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
    GPServerBindingStub stub = 
      getCredentials()==null || getCredentials().getUsername().length()==0 || getCredentials().getPassword().length()==0?
      new GPServerBindingStub(serviceInfo.getSoapUrl()):
      new GPServerBindingStub(serviceInfo.getSoapUrl(), getCredentials().getUsername(), getCredentials().getPassword());

    serviceInfo.getKeywords().add(serviceInfo.getType());
    serviceInfo.addKeywords("geographicService,service,geoprocessing",",");
    
    //stub.getExecutionType();
    
    GPToolInfo[] tasks = stub.getTaskInfos();
    if (tasks != null) {
      for (GPToolInfo tool: tasks) {
        //System.err.println("task getCategory="+tool.getCategory());
        //System.err.println("task getDisplayName="+tool.getDisplayName());
        //System.err.println("task getName="+tool.getName());
        //System.err.println("task getHelp="+tool.getHelp());
        //System.err.println("task getExtent="+tool.getExtent());
        serviceInfo.getKeywords().add(tool.getName());
        if ((tool.getExtent() != null) && (serviceInfo.getEnvelope() == null)) {
          serviceInfo.setEnvelope(tool.getExtent());
        }
      }
    }

    /*
    GPToolInfo[] tools = stub.getToolInfos();
    if (tools != null) {
      for (GPToolInfo tool: tools) {
        System.err.println("tool getCategory="+tool.getCategory());
        System.err.println("tool getDisplayName="+tool.getDisplayName());
        System.err.println("tool getName="+tool.getName());
        System.err.println("tool getExtent="+tool.getExtent());
        System.err.println("tool getHelp="+tool.getHelp());
        if ((tool.getExtent() != null) && (serviceInfo.getEnvelope() == null)) {
          serviceInfo.setEnvelope(tool.getExtent());
        }
      }
    }
    */
    
  }
  
}

