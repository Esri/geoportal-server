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
import com.esri.arcgisws.ImageServerBindingStub ;
import com.esri.arcgisws.ImageServiceInfo;
import com.esri.arcgisws.runtime.exception.ArcGISWebServiceException;
import com.esri.gpt.framework.resource.api.Resource;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the collection of metadata for an ArcGIS image service (ImageServer).
 */
public class ImageServerHandler extends ServiceHandler {
  private static final Logger LOGGER = Logger.getLogger(ImageServerHandler.class.getCanonicalName());
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public ImageServerHandler() {
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
    ImageServerBindingStub stub = 
      getCredentials()==null || getCredentials().getUsername().length()==0 || getCredentials().getPassword().length()==0?
      new ImageServerBindingStub(serviceInfo.getSoapUrl()):
      new ImageServerBindingStub(serviceInfo.getSoapUrl(), getCredentials().getUsername(), getCredentials().getPassword());
    serviceInfo.getKeywords().add(serviceInfo.getType());
    serviceInfo.addKeywords("liveData,service,image",",");
    
    ImageServiceInfo imageServiceInfo = stub.getServiceInfo();
    //serviceInfo.setName(imageServiceInfo.getName());
    serviceInfo.setDescription(imageServiceInfo.getDescription());
    serviceInfo.setEnvelope(imageServiceInfo.getExtent());
    
    // TODO: thumbnail url?

    //System.err.println(" getCopyrightText="+imageServiceInfo.getCopyrightText());
    //System.err.println(" getServiceDataType="+imageServiceInfo.getServiceDataType());
    //System.err.println(" getServiceSourceType="+imageServiceInfo.getServiceSourceType());
    //System.err.println(" getServiceProperties="+imageServiceInfo.getServiceProperties());
  }

  @Override
  public void appendRecord(Collection<Resource> records, ServiceHandlerFactory factory, ServiceInfo serviceInfo, boolean isNative) throws Exception {
    try {
    ImageServerBindingStub stub = 
      getCredentials()==null || getCredentials().getUsername().length()==0 || getCredentials().getPassword().length()==0?
      new ImageServerBindingStub(serviceInfo.getSoapUrl()):
      new ImageServerBindingStub(serviceInfo.getSoapUrl(), getCredentials().getUsername(), getCredentials().getPassword());
    ImageServiceInfo imageServiceInfo = stub.getServiceInfo();
    serviceInfo.setEnvelope(imageServiceInfo.getExtent());
    } catch (ArcGISWebServiceException ex) {
      LOGGER.log(Level.FINE, "Error getting ImageServiceInfo.", ex);
    }
    super.appendRecord(records, factory, serviceInfo, isNative);
  }
  
}

