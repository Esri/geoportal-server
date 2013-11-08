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
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Super-class associated with the collection and publication of metadata 
 * for an ArcGIS Server service.
 */
public abstract class ServiceHandler {
  
  private final static Logger LOGGER = Logger.getLogger(ServiceHandler.class.getCanonicalName());
  protected final static HttpClientRequest http = new HttpClientRequest();
  private UsernamePasswordCredentials credentials;
  /** constructors ============================================================ */

  /** Default constructor. */
  public ServiceHandler() {}

  /**
   * Gets credentials.
   * @return credentials
   */
  public UsernamePasswordCredentials getCredentials() {
    return credentials;
  }

  /**
   * Sets credentials.
   * @param credentials credentials
   */
  public void setCredentials(UsernamePasswordCredentials credentials) {
    this.credentials = credentials;
  }
  /** attributes =============================================================== */


  /** methods ================================================================= */

  /**
   * Collect metadata associated with the parent of a service.
   * @param processor the ArcGIS Server service processor
   * @param serviceInfo the active service information
   * @throws Exception if an exception occurs
   */
  public void collectParentMetadata(AGSProcessor processor, ServiceInfo serviceInfo)
      throws Exception {
    this.collectParentMetadata(processor.getHandlerFactory(), serviceInfo);
  }
  
  /**
   * Collect metadata associated with the parent of a service.
   * @param handlerFacory handler factory
   * @param serviceInfo the active service information
   * @throws Exception if an exception occurs
   */
  public void collectParentMetadata(ServiceHandlerFactory handlerFacory, ServiceInfo serviceInfo)
    throws Exception { 
    String parentType = serviceInfo.getParentType();
    if ((parentType != null) && (parentType.length() > 0)) {
      ServiceHandler parentHandler = handlerFacory.makeHandler(parentType);
      parentHandler.setCredentials(getCredentials());
      if (parentHandler != null) {
        String soapUrl = serviceInfo.getSoapUrl();
        try {
          int idx = soapUrl.lastIndexOf("/");
          serviceInfo.setSoapUrl(soapUrl.substring(0,idx));
          parentHandler.collectMetadata(handlerFacory, serviceInfo);
        } finally {
          serviceInfo.setSoapUrl(soapUrl);
        }
        serviceInfo.getKeywords().remove(parentType);
      }
    }
  }

  /**
   * Collect metadata associated with a service.
   * @param processor the ArcGIS Server service processor
   * @param serviceInfo the active service information
   * @throws Exception if an exception occurs
   */
  public void collectMetadata(AGSProcessor processor, ServiceInfo serviceInfo)
      throws Exception {
    this.collectMetadata(processor.getHandlerFactory(), serviceInfo);
  }

  /**
   * Collect metadata associated with a service.
   * @param handlerFactory handler factory
   * @param serviceInfo the active service information
   * @throws Exception if an exception occurs
   */
  public abstract void collectMetadata(ServiceHandlerFactory handlerFactory, ServiceInfo serviceInfo)
    throws Exception;
  
  /**
   * Creates new record instance (or more record instances) for given ServiceInfo 
   * and appends to the collection.
   * @param records collection of records
   * @param factory handler factory
   * @param serviceInfo service info
   * @param isNative <code>true</code> to append native record
   * @throws Exception if an exception occurs
   */
  public void appendRecord(Collection<Resource> records, ServiceHandlerFactory factory, ServiceInfo serviceInfo, boolean isNative) throws Exception {
    records.add(isNative? new NativeServiceRecord(factory, serviceInfo): new ServiceRecord(factory, serviceInfo));
  }

  /**
   * Publishes metadata associated with a service.
   * @param processor the ArcGIS Server service processor
   * @param serviceInfo the active service information
   * @throws Exception if an exception occurs
   */
  public void publishMetadata(AGSProcessor processor, ServiceInfo serviceInfo) 
    throws Exception {   
    String resourceUrl = serviceInfo.getResourceUrl();
    String resourceXml = serviceInfo.asDublinCore(processor);
    processor.publishMetadata(resourceUrl,resourceXml);
  }

  /**
   * Creates service info.
   * @param parentInfo parent info
   * @param desc service description
   * @param currentRestUrl current REST URL
   * @param currentSoapUrl current SOAP URL
   * @return service info
   */
  public ServiceInfo createServiceInfo(ServiceInfo parentInfo, ServiceDescription desc, String currentRestUrl, String currentSoapUrl) {
    ServiceInfo info = new ServiceInfo();
    info.setCapabilities(desc.getCapabilities());
    info.setDescription(desc.getDescription());
    info.setName(desc.getName());
    info.setParentType(desc.getParentType());
    info.setResourceUrl(currentRestUrl);
    info.setRestUrl(currentRestUrl);
    info.setSoapUrl(currentSoapUrl);
    info.setType(desc.getType());
    info.setParentInfo(parentInfo);
    return info;
  }

  /**
   * Service specific Record implementation.
   */
  public class ServiceRecord extends ServiceInfoProvider implements Publishable {

    private ServiceHandlerFactory factory;

    public ServiceRecord(ServiceHandlerFactory factory, ServiceInfo info) {
      super(info);
      this.factory = factory;
    }

    @Override
    public SourceUri getSourceUri() {
      return new UrlUri(getServiceInfo().getResourceUrl());
    }

    @Override
    public String getContent() throws IOException {
      ApplicationContext appCtx = ApplicationContext.getInstance();
      ApplicationConfiguration cfg = appCtx.getConfiguration();
      LOGGER.finer("Collecting metadata for: " + getServiceInfo().getSoapUrl());
      try {
        ServiceHandler.this.collectMetadata(factory, getServiceInfo());
        return getServiceInfo().asDublinCore(cfg, http);
      } catch (Exception ex) {
        throw new IOException("Error collecting metadata. Cause: "+ex.getMessage());
      }
    }

    @Override
    public Iterable<Resource> getNodes() {
      return new ArrayList<Resource>();
    }

    @Override
    public Date getUpdateDate() {
      return null;
    }

  }

  /**
   * Native service specific Record implementation.
   */
  private class NativeServiceRecord extends ServiceRecord implements Native {
    public NativeServiceRecord(ServiceHandlerFactory factory, ServiceInfo info) {
      super(factory, info);
    }

    @Override
    public Date getUpdateDate() {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }
}
