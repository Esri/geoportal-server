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
import com.esri.gpt.catalog.publication.ProcessedRecord;
import com.esri.gpt.catalog.publication.ProcessingContext;
import com.esri.gpt.catalog.publication.PublicationRecord;
import com.esri.gpt.catalog.publication.ResourceProcessor;

import com.esri.arcgisws.ServiceCatalogBindingStub;
import com.esri.arcgisws.ServiceDescription;
import com.esri.arcgisws.runtime.exception.ArcGISWebServiceException;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonResult;
import com.esri.gpt.framework.resource.adapters.FlatResourcesAdapter;
import com.esri.gpt.framework.resource.adapters.LimitedLengthResourcesAdapter;
import com.esri.gpt.framework.resource.adapters.PublishablesAdapter;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.util.ReadOnlyIterator;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Processes resources associated with an ArcGIS server.
 */
public class AGSProcessor extends ResourceProcessor {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(AGSProcessor.class.getName());
  
  /** instance variables ====================================================== */
  private ServiceHandlerFactory factory = new ServiceHandlerFactory();
  private AGSTarget             target = new AGSTarget();
  private UsernamePasswordCredentials credentials;

  /** constructors ============================================================ */

  /**
   * Constructs with an associated processing context.
   * @param context the procesing context
   */
  public AGSProcessor(ProcessingContext context) {
    super(context);
    if (context.getTemplate() == null) {
      PublicationRecord template = new PublicationRecord();
      template.setUpdateOnlyIfXmlHasChanged(true);
      context.setTemplate(template);
    }
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the ArcGIS server service handler factory.
   * @return the factory
   */
  public ServiceHandlerFactory getHandlerFactory() {
    return this.factory;
  }
  
  /**
   * Gets the targeted ArcGIS server and resource.
   * @return the target
   */
  public AGSTarget getTarget() {
    return this.target;
  }

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
  
  /** methods ================================================================= */
  
  /**
   * Interrogates the character response from a target resource URL attempting to
   * determine the REST and SOAP endpoints for an ArcGIS server services catalog.
   * @param url the target URL associated with the resource being interrogated
   * @param response the character based response previously returned from the target URL
   * @return <code>true</code> if the target was recognized as an ArcGIS server endpoint
   */
  public boolean interrogate(URL url, String response) throws IOException {
    AGSInterrogator interragator = new AGSInterrogator(this.getContext(),this.getTarget());
    interragator.interrogate(url,response);
    getTarget().updateTargetSoapUrl();
    return getTarget().getWasRecognized();
  }
  
  /**
   * Invokes processing against the resource.
   * @throws Exception if an exception occurs
   */
  @Override
  public void process() throws Exception {
    
   /*
    * ServiceCatalogBindingStub
    *   name:          dc:title
    *   description:   dc:description
    *   resource.url:  dct:references 
    *                  scheme=urn:x-esri:specification:ServiceType:ArcGIS:type
    *                  value is typically the rest endpoint for the service
    *   type:          typically dc:subject -> keyword Lucene field is dataTheme
    *   parentType:    not indexed
    *   capabilities:  not indexed
    *   sourceUri:     service rest url (by default)
    *   
    * GeocodeServerBindingStub
    *   keywords: GeocodeServer,geographicService,service,locator,geocode,geocoder
    *   
    * GeoDataServerBindingStub
    *   keywords: GeoDataServer,geographicService,service
    *   per dataElement: publish element.getMetadata
    *                    sourceUri=serviceRestUrl/element.getName
    *   
    * GeometryServerBindingStub
    *   keywords: GeometryServer,geographicService,service,geometry,projection
    *   
    * GlobeServerBindingStub
    *   keywords: GlobeServer,liveData,service,globe
    *   per layer: dct:abstract/rdf:value@rdf:resource=service.layername
    *   
    * GPServerBindingStub
    *   keywords: GPServer,geographicService,service,geoprocessing
    *   per task: add task name as keyword, set service envelope is applicable
    *   
    * ImageServerBindingStub
    *   keywords: ImageServer,liveData,service,image
    *   imageServiceInfo.description: dc:description
    *   imageServiceInfo.extent: ows:WGS84BoundingBox
    * 
    * MapServerBindingStub
    *   keywords: ImageServer,liveData,service,image
    *   mapServerInfo.description: dc:description
    *   mapServerInfo.fullExtent: ows:WGS84BoundingBox
    *   thumbnail.url: serviceRestUrl/export?size=256,256&f=image
    *   documentInfo['Title']: dc:title
    *   documentInfo['Author']: dc:creator
    *   documentInfo['Comments']: dct:abstract/rdf:value@rdf:resource=mxd.comments
    *   documentInfo['Subject']: dct:abstract/rdf:value@rdf:resource=mxd.subject
    *   documentInfo['Category']: dct:abstract/rdf:value@rdf:resource=mxd.category
    *   documentInfo['Keywords']: dc:subject
    *   per layer: dct:abstract/rdf:value@rdf:resource=service.layername
    * 
    * MobileServerBindingStub
    *   keywords: MobileServer,liveData,service
    *   
    * NAServerBindingStub
    *   keywords: NAServer,geographicService,service,network,route
    *   
    * WCSServer
    *   keywords: WCSServer,liveData,service
    *   resource.url: soapEndpoint?request=GetCapabilities&service=WCSServer
    *   plus parent service metadata
    *   
    * WFSServer
    *   keywords: WFSServer,liveData,service
    *   resource.url: soapEndpoint?request=GetCapabilities&service=WFSServer
    *   plus parent service metadata
    *   
    * WMSServer
    *   keywords: WMSServer,liveData,service
    *   resource.url: soapEndpoint?request=GetCapabilities&service=WMSServer
    *   plus parent service metadata
    *   
    */
    
    String restUrl = this.getTarget().getRestUrl();
    String soapUrl = this.getTarget().getSoapUrl();
    AGSTarget.TargetType targetType = this.getTarget().getTargetType();
    
    if ((targetType != null) && targetType.equals(AGSTarget.TargetType.ROOT)) {
      this.collectExistingSourceURIs(restUrl,soapUrl);
    }
    
    // TODO: check the TargetType
    // determine the target (entire server, a folder or a service)
    getTarget().updateTargetSoapUrl();
    String targetSoapUrl = getTarget().getTargetSoapUrl();
    boolean matchAll = targetSoapUrl.equals(soapUrl);
    boolean checkFolder = !targetSoapUrl.endsWith("Server");
    
    // loop through the service descriptions
    ServiceCatalogBindingStub stub = new ServiceCatalogBindingStub(soapUrl);    
    for (ServiceDescription desc: stub.getServiceDescriptions()) {
      if (Thread.currentThread().isInterrupted()) return;
      String currentSoapUrl = desc.getUrl();
      String currentRestUrl = currentSoapUrl.replace(soapUrl,restUrl);
 
      // determine if there is a metch
      boolean matchesTarget = false;
      if (!matchAll) {
        matchesTarget = targetSoapUrl.equals(currentSoapUrl);
        if (!matchesTarget && checkFolder) {
          matchesTarget = currentSoapUrl.startsWith(targetSoapUrl+"/");
        }
      }
      if (matchAll || matchesTarget) {
        
        // make a handler for this service type
        ServiceHandler handler = this.factory.makeHandler(desc.getType());
        handler.setCredentials(getCredentials());
        if (handler != null) {
      
          // initialize service information
          ServiceInfo info = handler.createServiceInfo(null, desc, currentRestUrl, currentSoapUrl);
          
          // collect
          try {
            LOGGER.log(Level.FINER, "Collecting metadata for: {0}", info.getSoapUrl());
            handler.collectMetadata(this,info);
          } catch (Exception e) {
            ProcessedRecord processedRcord = new ProcessedRecord();
            processedRcord.setSourceUri(info.getResourceUrl());
            processedRcord.setStatusType(ProcessedRecord.StatusType.FAILED);
            processedRcord.setException(e,this.getContext().getMessageBroker());
            this.getContext().incrementNumberFailed();
            this.getContext().setLastException(e);
            this.getContext().getProcessedRecords().add(processedRcord);
            LOGGER.log(Level.FINER,"Error\n"+processedRcord.getSourceUri(),e);
          }
          
          // publish
          try {
            LOGGER.log(Level.FINER, "Publishing metadata for: {0}", info.getResourceUrl());
            handler.publishMetadata(this,info);
          } catch (Exception e) {
            LOGGER.log(Level.FINER,"Error during publication.",e);
          }
          
          // break if we have found a single targetted service
          if (!matchAll && !checkFolder) {
            break;
          }
          
        }
      }
    }
    
    // cleanup unreferenced source URIs
    if ((targetType != null) && targetType.equals(AGSTarget.TargetType.ROOT)) {
      this.deleteUnreferencedSourceURIs();
    }
    
  }

  @Override
  public Query createQuery(final IterationContext context, final Criteria criteria) {
    return new Query() {
      @Override
      public Result execute() {
        ResourceFolders folders = createResourceFolders(context);
        return new CommonResult(new LimitedLengthResourcesAdapter(folders, criteria.getMaxRecords()));
      }
    };
  }

  @Override
  public Native getNativeResource(IterationContext context) {
    ResourceFolders folders = createResourceFolders(context);
    for (Publishable publishable : new PublishablesAdapter(new FlatResourcesAdapter(folders))) {
      if (publishable instanceof Native) {
        return (Native)publishable;
      }
      break;
    }
    return null;
  }

  /**
   * Normalizes URL by removing 'wsdl'.
   * @param url URL
   * @return normalized URL
   */
  private String normalizeUrl(String url) {
    Pattern wsdlPattern = Pattern.compile("\\?wsdl$", Pattern.CASE_INSENSITIVE);
    Matcher wsdlMatcher = wsdlPattern.matcher(Val.chkStr(url));
    String wsdlResult = wsdlMatcher.replaceFirst("");
    
    Pattern servicesPattern = Pattern.compile("services\\?wsdl/", Pattern.CASE_INSENSITIVE);
    Matcher servicesMatcher = servicesPattern.matcher(wsdlResult);
    String servicesResult = servicesMatcher.replaceAll("");
    
    return servicesResult.replaceAll("/+$", "");
    
    /*
    return Pattern.compile("services\\?wsdl/", Pattern.CASE_INSENSITIVE).matcher(
      Pattern.compile("\\?wsdl$", Pattern.CASE_INSENSITIVE).matcher(
        Val.chkStr(url)
      ).replaceFirst("")
    ).replaceFirst("");
    */
  }

  /**
   * Extracts root URL.
   * @param url URL
   * @return root URL
   */
  private String extractRootUrl(String url) {
    url = Val.chkStr(url);
    try {
      URI uri = new URI(url);
      Matcher matcher = Pattern.compile("^/[^/]*(/services)?",Pattern.CASE_INSENSITIVE).matcher(uri.getPath());
      if (matcher.find()) {
        return uri.getScheme() + "://" + uri.getAuthority() + matcher.group();
      } else {
        return url;
      }
    } catch (Exception ex) {
      return url;
    }
  }

  /**
   * Reads service descriptions.
   * @return array of service descriptions
   * @throws ArcGISWebServiceException if accessing service descriptions 
   */
  private ServiceDescription[] readServiceDescriptions() throws ArcGISWebServiceException {
    String soapUrl = extractRootUrl(getTarget().getSoapUrl());
    ServiceCatalogBindingStub stub = new ServiceCatalogBindingStub(soapUrl);
    ServiceDescription[] descriptors = stub.getServiceDescriptions();
    return descriptors;
  }
  
  /**
   * Creates resource folders.
   * @param context iteration context
   * @return resource folders
   */
  private ResourceFolders createResourceFolders(IterationContext context) {
    try {
      ServiceDescription[] descriptors = readServiceDescriptions();
      return new ResourceFolders(context, factory, descriptors);
    } catch (ArcGISWebServiceException ex) {
      context.onIterationException(ex);
      return new ResourceFolders(context, factory, new ServiceDescription[]{});
    }
  }

  /**
   * ArcGIS folders.
   */
  private class ResourceFolders implements Iterable<IServiceInfoProvider> {
  /** iteration context */
  private IterationContext context;
  /** service handler factory */
  private ServiceHandlerFactory factory;
  /** service descriptors */
  private ServiceDescription[] descriptors;
  /** normalized target SOAP URL */
  private String normalizedTargetSoapUrl;
  /** indicator to match everything or only selected service */
  private boolean matchAll;
  /** indicator to check folder */
  private boolean checkFolder;
  
  private HashMap<ServiceDescription,ServiceDescription> childToParent = new HashMap<ServiceDescription, ServiceDescription>();
  private HashMap<ServiceDescription,ServiceInfo> sdToSi = new HashMap<ServiceDescription, ServiceInfo>();

  /**
   * Creates instance of the folders.
   * @param context iteration context
   * @param factory service handler factory
   * @param descriptors service descriptors
   */
  public ResourceFolders(IterationContext context, ServiceHandlerFactory factory, ServiceDescription[] descriptors) {
    if (context==null) throw new IllegalArgumentException("No context provided.");
    if (factory==null) throw new IllegalArgumentException("No factory provided.");
    if (descriptors==null) throw new IllegalArgumentException("No descriptors provided.");
    this.context = context;
    this.factory = factory;
    this.descriptors = descriptors;
    this.normalizedTargetSoapUrl = normalizeUrl(getTarget().getTargetSoapUrl());
    this.matchAll = normalizedTargetSoapUrl.equalsIgnoreCase(extractRootUrl(getTarget().getSoapUrl()));
    this.checkFolder = !normalizedTargetSoapUrl.endsWith("Server");
    
    HashMap<String,ServiceDescription> urlToSD = new HashMap<String, ServiceDescription>();
    for (ServiceDescription sd: descriptors) {
      String url = sd.getUrl();
      urlToSD.put(url, sd);
    }
    
    for (ServiceDescription sd: descriptors) {
      if (sd.getParentType().isEmpty()) continue;
      int index = sd.getUrl().indexOf(sd.getParentType()) + sd.getParentType().length();
      String url = sd.getUrl().substring(0, index);
      
      ServiceDescription parentSD = urlToSD.get(url);
      childToParent.put(sd, parentSD);
    }
  }

  public Iterator<IServiceInfoProvider> iterator() {
    return new AGSRecordsIterator();
  }

  /**
   * ArcGIS folders iterator.
   */
  private class AGSRecordsIterator extends ReadOnlyIterator<IServiceInfoProvider> {
  /** index of the current folder */
  private int index = -1;
  /** service handler */
  private ServiceHandler handler = null;
  /** service info */
  private ServiceInfo info = null;

  /**
   * Resets current service information.
   */
  private void reset() {
    handler = null;
    info = null;
  }

  public boolean hasNext() {
    if (handler!=null && info!=null) return true;
    if (index+1>=descriptors.length) return false;
    ServiceDescription desc = descriptors[++index];
    String currentSoapUrl = desc.getUrl();
    String currentRestUrl = Pattern.compile("\\Q"+getTarget().getSoapUrl()+"\\E", Pattern.CASE_INSENSITIVE).matcher(currentSoapUrl).replaceFirst(getTarget().getRestUrl());
    boolean matchesTarget = false;
    if (!matchAll) {
      matchesTarget = normalizedTargetSoapUrl.equalsIgnoreCase(currentSoapUrl);
      if (!matchesTarget && checkFolder) {
        matchesTarget = currentSoapUrl.toLowerCase().startsWith(normalizedTargetSoapUrl.toLowerCase()+"/");
      }
    }
    if (!(matchAll || matchesTarget)) return hasNext();
    handler = factory.makeHandler(desc.getType());
    if (handler==null) return hasNext();
    handler.setCredentials(getCredentials());
    
    // get parent description if available for the current description
    ServiceDescription parentDesc = childToParent.get(desc);
    // get service info for the parent
    ServiceInfo parentInfo = sdToSi.get(parentDesc);
    
    // create servcice info for the current service description
    info = handler.createServiceInfo(parentInfo, desc, currentRestUrl, currentSoapUrl);
    
    // store mapping between service descritpion and service info
    sdToSi.put(desc, info);

    return true;
  }

  @Override
  public IServiceInfoProvider next() {
    final ResourceRecordsFamily family = new ResourceRecordsFamily(context, factory, handler, info, !matchAll);
    reset();
    return new ServiceInfoProvider(info) {
      @Override
      public Iterable<Resource> getNodes() {
        return family;
      }
    };
  }
  }
  }

  /**
   * Family of the records. This is a collection of records derived from the same
   * service URL.
   */
  private class ResourceRecordsFamily implements Iterable<Resource> {
  /** iteration context */
  private IterationContext context;
  /** service handler factory */
  private ServiceHandlerFactory factory;
  /** service handler */
  private ServiceHandler handler;
  /** service info */
  private ServiceInfo info;
  /** is native */
  private boolean isNative;

  /**
   * Creates instance of the records family.
   * @param context iteration context
   * @param factory service handler factory
   * @param handler service handler
   * @param info service info
   * @param isNative <code>true</code> to append native record
   */
  public ResourceRecordsFamily(IterationContext context, ServiceHandlerFactory factory, ServiceHandler handler, ServiceInfo info, boolean isNative) {
    if (context==null) throw new IllegalArgumentException("No context provided.");
    if (factory==null) throw new IllegalArgumentException("No factory provided.");
    if (handler==null) throw new IllegalArgumentException("No handler provided.");
    if (info==null) throw new IllegalArgumentException("No info provided.");
    this.context = context;
    this.factory = factory;
    this.handler = handler;
    this.info = info;
    this.isNative = isNative;
  }

  public Iterator<Resource> iterator() {
    ArrayList<Resource> recs = new ArrayList<Resource>();
    try {
      handler.appendRecord(recs, factory, info, isNative);
    } catch (Exception ex) {
      context.onIterationException(ex);
    }
    return recs.iterator();
  }
  }

}
