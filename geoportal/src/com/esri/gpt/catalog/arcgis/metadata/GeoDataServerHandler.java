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
import com.esri.arcgisws.DEBrowseOptions;
import com.esri.arcgisws.DataElement;
import com.esri.arcgisws.GeoDataServerBindingStub ;
import com.esri.arcgisws.XmlPropertySet;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.api.SourceUri;
import com.esri.gpt.framework.resource.common.UrlUri;
import com.esri.gpt.framework.util.Val;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the collection of metadata for an ArcGIS geodatabase service (GeoDataServer).
 */
public class GeoDataServerHandler extends ServiceHandler {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(GeoDataServerHandler.class.getName());
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public GeoDataServerHandler() {
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
    serviceInfo.getKeywords().add(serviceInfo.getType());
    serviceInfo.addKeywords("geographicService,service",",");   
  }
  
  /**
   * Publishes metadata associated with a service.
   * <br/>A record is published for the GeoDataServer itself.
   * <br/>Any data element containing an XML will also be published, 
   * the SORCEURI for the element will be: [geodataserver-rest-endpoint]/element.name
   * @param processor the ArcGIS Server service processor
   * @param serviceInfo the active service information
   * @throws Exception if an exception occurs
   */
  @Override
  public void publishMetadata(AGSProcessor processor, ServiceInfo serviceInfo) 
    throws Exception {   
    
    // publish the service
    String resourceUrl = serviceInfo.getResourceUrl();
    String resourceXml = serviceInfo.asDublinCore(processor);
    processor.publishMetadata(resourceUrl,resourceXml);
    
    // configuration parameters for data element recursion
    RequestContext rContext = processor.getContext().getRequestContext();
    StringAttributeMap params = rContext.getCatalogConfiguration().getParameters();
    boolean recurse = true;
    if (Val.chkStr(params.getValue("AGSProcessor.GeoDataServer.recurse")).equalsIgnoreCase("false")) {
      recurse = false;
    }
    boolean expandDescendants = false;
    if (Val.chkStr(params.getValue("AGSProcessor.GeoDataServer.expandDescendants")).equalsIgnoreCase("true")) {
      expandDescendants= true;
    }
    int maxDataElements = Val.chkInt(params.getValue("AGSProcessor.GeoDataServer.maxDataElements"),200);
    if (maxDataElements == 0) {
      recurse = false;
    }
    
    if (recurse) {
      
      // publish each data element within the geodatabase that has metadata
      GeoDataServerBindingStub stub =
        getCredentials()==null || getCredentials().getUsername().length()==0 || getCredentials().getPassword().length()==0?
        new GeoDataServerBindingStub(serviceInfo.getSoapUrl()):
        new GeoDataServerBindingStub(serviceInfo.getSoapUrl(), getCredentials().getUsername(), getCredentials().getPassword());

      DEBrowseOptions options = new DEBrowseOptions();
      if (expandDescendants) {
        options.setExpandType(com.esri.arcgisws.EsriDEExpandType.esriDEExpandDescendants);
      } else {
        options.setExpandType(com.esri.arcgisws.EsriDEExpandType.esriDEExpandChildren);
      }
      options.setRetrieveFullProperties(false);
      options.setRetrieveMetadata(false);
      
      // retrieve data elements 
      DataElement[] elements = null;
      try {
        if (maxDataElements < 0) {
          options.setRetrieveMetadata(true);
          elements = stub.getDataElements(options);  
        } else {
          
          // test first without retrieving actual metadata to avoid out of memory issues,
          // if under the threshould element count then go back and get the metadata
          DataElement[] test = stub.getDataElements(options);
          if ((test != null) && (test.length > 0) && (test.length <= maxDataElements)) {
            options.setRetrieveMetadata(true);
            elements = stub.getDataElements(options);         
          }
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING,"Error while processing: "+serviceInfo.getSoapUrl(),e);
        return;
      } 

      // publish metadata per data element
      if (elements != null) {
        for (DataElement element: elements) {
          if (Thread.currentThread().isInterrupted()) return;
          XmlPropertySet xmlProps = element.getMetadata();
          if (xmlProps != null) {
            String xml = Val.chkStr(xmlProps.getXmlDoc());
            if ((xml != null) && (xml.length() > 0)) {
              String sourceUri = serviceInfo.getRestUrl()+"/"+element.getName();
              LOGGER.finer("Publishing metadata for: "+sourceUri);
              processor.publishMetadata(null,xml,sourceUri);
            }
          }
        }
      }
      
    }
    
  }

  @Override
  public void appendRecord(Collection<Resource> records, ServiceHandlerFactory factory, ServiceInfo serviceInfo, boolean isNative) throws Exception {
    super.appendRecord(records, factory, serviceInfo, isNative);

    // configuration parameters for data element recursion
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration cfg = appCtx.getConfiguration();
    StringAttributeMap params = cfg.getCatalogConfiguration().getParameters();
    boolean recurse = true;
    if (Val.chkStr(params.getValue("AGSProcessor.GeoDataServer.recurse")).equalsIgnoreCase("false")) {
      recurse = false;
    }
    boolean expandDescendants = false;
    if (Val.chkStr(params.getValue("AGSProcessor.GeoDataServer.expandDescendants")).equalsIgnoreCase("true")) {
      expandDescendants= true;
    }
    int maxDataElements = Val.chkInt(params.getValue("AGSProcessor.GeoDataServer.maxDataElements"),200);
    if (maxDataElements == 0) {
      recurse = false;
    }
    if (recurse) {

      // publish each data element within the geodatabase that has metadata
      GeoDataServerBindingStub stub =
        getCredentials()==null || getCredentials().getUsername().length()==0 || getCredentials().getPassword().length()==0?
        new GeoDataServerBindingStub(serviceInfo.getSoapUrl()):
        new GeoDataServerBindingStub(serviceInfo.getSoapUrl(), getCredentials().getUsername(), getCredentials().getPassword());
      DEBrowseOptions options = new DEBrowseOptions();
      if (expandDescendants) {
        options.setExpandType(com.esri.arcgisws.EsriDEExpandType.esriDEExpandDescendants);
      } else {
        options.setExpandType(com.esri.arcgisws.EsriDEExpandType.esriDEExpandChildren);
      }
      options.setRetrieveFullProperties(false);
      options.setRetrieveMetadata(false);

      // retrieve data elements
      DataElement[] elements = null;
      try {
        if (maxDataElements < 0) {
          options.setRetrieveMetadata(true);
          elements = stub.getDataElements(options);
        } else {

          // test first without retrieving actual metadata to avoid out of memory issues,
          // if under the threshould element count then go back and get the metadata
          DataElement[] test = stub.getDataElements(options);
          if ((test != null) && (test.length > 0) && (test.length <= maxDataElements)) {
            options.setRetrieveMetadata(true);
            elements = stub.getDataElements(options);
          }
        }
      } catch (Exception e) {
        LOGGER.log(Level.WARNING,"Error while processing: "+serviceInfo.getSoapUrl(),e);
        return;
      }

      // publish metadata per data element
      if (elements != null) {
        for (DataElement element: elements) {
          addElement(records, serviceInfo, element);
        }
      }

    }
  }

  /**
   * Adds a single element.
   * @param records records to add element
   * @param serviceInfo service info
   * @param element element to add
   */
  private void addElement(Collection<Resource> records, ServiceInfo serviceInfo, DataElement element) {
    records.add(new DataElementRecord(serviceInfo, element));
    DataElement [] children = element.getChildren();
    if (children!=null) {
      for (DataElement child: children) {
        addElement(records, serviceInfo, child);
      }
    }
  }

  /**
   * Data element specific Record implementation.
   */
  private class DataElementRecord extends ServiceInfoProvider implements Publishable {

    private DataElement element;

    public DataElementRecord(ServiceInfo info, DataElement element) {
      super(info);
      this.element = element;
    }

    @Override
    public SourceUri getSourceUri() {
      return new UrlUri(getServiceInfo().getRestUrl()+"/"+element.getName());
    }

    @Override
    public String getContent() throws IOException {
      XmlPropertySet xmlProps = element.getMetadata();
      return xmlProps!=null? Val.chkStr(xmlProps.getXmlDoc()): "";
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
}

