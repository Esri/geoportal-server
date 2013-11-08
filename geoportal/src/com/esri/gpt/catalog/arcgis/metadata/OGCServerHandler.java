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

import com.esri.arcgisws.ServiceDescription;
import java.util.logging.Level;

import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Handles the collection of metadata for an ArcGIS server service implementing an 
 * OGC type (WMS, WFS, WCS).
 */
public class OGCServerHandler extends ServiceHandler {
  
  /** instance variables ====================================================== */
  private String ogcType ;
  
  /** constructors ============================================================ */

  /**
   * Constructs with a supplied OGC service type (e.g. WMS, WFS, WCS).
   * @param type the OGC service type
   */ 
  public OGCServerHandler(String type) {
    super();
    this.setOgcType(type);
  } 
  
  /** properties ============================================================== */
  
  /**
   * Gets the OGC service type (e.g. WMS, WFS, WCS).
   * @return the OGC service type
   */
  public String getOgcType() {
    return this.ogcType;
  }
  /**
   * Sets the OGC service type (e.g. WMS, WFS, WCS).
   * @param type the OGC service type
   */
  public void setOgcType(String type) {
    this.ogcType = type;
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
    String url = serviceInfo.getSoapUrl()+"?request=GetCapabilities&service="+this.getOgcType();
    serviceInfo.getKeywords().add(serviceInfo.getType());
    serviceInfo.getKeywords().add(this.getOgcType());
    serviceInfo.addKeywords("liveData,service",",");
    this.collectParentMetadata(handlerFactory,serviceInfo);
    serviceInfo.setResourceUrl(url);
    
    // generate the name, e.g. Parent Service Name (WMS)
    String resourceKey = "catalog.ags.namegeneration."+Val.chkStr(this.getOgcType()).toLowerCase();
    try {
      MessageBroker msgBroker = new MessageBroker();
      msgBroker.setBundleBaseName(MessageBroker.DEFAULT_BUNDLE_BASE_NAME);
      String name = msgBroker.retrieveMessage(resourceKey,new String[]{serviceInfo.getName()});
      if (!name.startsWith("???")) {
        serviceInfo.setName(name);
      }
    } catch (Exception e) {
      String msg = "Error auto-generating OGC service name for resource key: "+resourceKey;
      msg += ", service="+url;
      LogUtil.getLogger().log(Level.FINER,msg,e);
    }
  }

  @Override
  public ServiceInfo createServiceInfo(ServiceInfo parentInfo, ServiceDescription desc, String currentRestUrl, String currentSoapUrl) {
    ServiceInfo serviceInfo = super.createServiceInfo(parentInfo, desc, currentSoapUrl.replaceAll("\\?wsdl$", "") /*currentRestUrl*/, currentSoapUrl);
    if (parentInfo!=null) {
      serviceInfo.setLayersInfo(parentInfo.getLayersInfo());
      serviceInfo.setEnvelope(parentInfo.getEnvelope());
      serviceInfo.setCopyright(parentInfo.getCopyright());
    }
    return serviceInfo;
  }
    
}

