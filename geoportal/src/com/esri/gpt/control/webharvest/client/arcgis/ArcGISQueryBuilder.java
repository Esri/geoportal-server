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
package com.esri.gpt.control.webharvest.client.arcgis;

import com.esri.gpt.catalog.arcgis.metadata.AGSProcessor;
import com.esri.gpt.catalog.arcgis.metadata.AGSTarget;
import com.esri.gpt.catalog.publication.ProcessingContext;
import com.esri.gpt.catalog.publication.ResourceProcessor;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.common.CommonCapabilities;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.resource.api.Native;
import com.esri.gpt.framework.resource.query.Capabilities;
import com.esri.gpt.framework.resource.query.Criteria;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.security.principal.Publisher;

/**
 * ArcGIS server query builder.
 */
public class ArcGISQueryBuilder implements QueryBuilder {
/** capabilities */
private static final Capabilities capabilities = new ArcGisCommonCapabilities();
/** iteration context */
private IterationContext context;
/** info */
private ArcGISInfo info;

/**
 * Creates instance of the builder.
 * @param context iteration context
 * @param protocol harvest protocol
 * @param restUrl REST url
 * @param soapUrl SOAP url
 */
public ArcGISQueryBuilder(IterationContext context, ArcGISProtocol protocol, String restUrl, String soapUrl) {
  if (context == null)
    throw new IllegalArgumentException("No context provided.");
  this.context = context;
  this.info = new ArcGISInfo(restUrl, soapUrl, protocol.getUserName(), protocol.getUserPassword());
}

/**
 * Creates instance of the builder.
 * @param context iteration context
 * @param info info
 */
public ArcGISQueryBuilder(IterationContext context, ArcGISInfo info) {
  if (context == null)
    throw new IllegalArgumentException("No context provided.");
  this.context = context;
  this.info = info;
}

public Capabilities getCapabilities() {
  return capabilities;
}

public Query newQuery(Criteria crt) {
  RequestContext requestContext = RequestContext.extract(null);
  try {
    ResourceProcessor processor = newProcessor(requestContext);
    return processor.createQuery(context, crt);
  } finally {
    requestContext.onExecutionPhaseCompleted();
  }
}

public Native getNativeResource() {
  RequestContext requestContext = RequestContext.extract(null);
  try {
    ResourceProcessor processor = newProcessor(requestContext);
    return processor!=null? processor.getNativeResource(context): null;
  } finally {
    requestContext.onExecutionPhaseCompleted();
  }
}

/**
 * Creates new processor.
 * @param requestContext request context
 * @return processor
 */
public AGSProcessor newProcessor(RequestContext requestContext) {
  AGSProcessor processor = new AGSProcessor(newProcessingContext(requestContext));
  processor.setCredentials(info.newCredentials());
  AGSTarget target = processor.getTarget();
  target.setTargetUrl(info.getRestUrl());
  target.setTargetSoapUrl(info.getSoapUrl());

  int idx = -1;
  String rootRestUrl = info.getRestUrl();
  String rootSoapUrl = info.getSoapUrl();

  String restServicesString = "/rest/services";
  idx = rootRestUrl.toLowerCase().lastIndexOf(restServicesString);
  if (idx>=0) {
    rootRestUrl = rootRestUrl.substring(0, idx+restServicesString.length());
  }

  String soapServicesString = "/services";
  idx = rootSoapUrl.toLowerCase().lastIndexOf(soapServicesString);
  if (idx>=0) {
    rootSoapUrl = rootSoapUrl.substring(0, idx+soapServicesString.length());
  }

  target.setRestUrl(rootRestUrl);
  target.setSoapUrl(rootSoapUrl);

  target.setTargetType(rootRestUrl.toLowerCase().replaceAll("/*$", "").equals(restServicesString.toLowerCase())? AGSTarget.TargetType.ROOT: AGSTarget.TargetType.SERVICE);

  target.setWasRecognized(true);

  return processor;
}

/**
 * Creates new processing context.
 * @param requestContext request context
 * @return processing context
 */
private ProcessingContext newProcessingContext(RequestContext requestContext) {
  Publisher publisher = null;
  HttpClientRequest httpClient = HttpClientRequest.newRequest();
  httpClient.setCredentialProvider(info.newCredentialProvider());
  ProcessingContext processingContext = new ProcessingContext(requestContext, publisher, httpClient, null, false);

  FacesContextBroker contextBroker = new FacesContextBroker();
  MessageBroker msgBroker = contextBroker.extractMessageBroker();
  processingContext.setMessageBroker(msgBroker);

  return processingContext;
}

/**
 * ArcGIS capabilities.
 */
private static class ArcGisCommonCapabilities extends CommonCapabilities {

@Override
public boolean canQueryMaxRecords() {
  return true;
}
}

}
