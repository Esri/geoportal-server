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
package com.esri.gpt.control.webharvest.client.res;

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolResource;
import com.esri.gpt.catalog.publication.ProcessingContext;
import com.esri.gpt.catalog.publication.ProcessorFactory;
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
import java.io.IOException;
import java.util.logging.Logger;

/**
 * ArcGIS query builder.
 */
public class ResourceQueryBuilder implements QueryBuilder {
/** logger */
private static final Logger LOGGER = Logger.getLogger(ResourceQueryBuilder.class.getCanonicalName());
/** capabilities */
private static final Capabilities capabilities = new ArcGisCommonCapabilities();
/** iteration context */
private IterationContext context;
/** info */
private ResourceInfo info;


/**
 * Creates instance of the builder.
 * @param context iteration context
 * @param protocol harvest protocol
 * @param url url
 */
public ResourceQueryBuilder(IterationContext context, HarvestProtocolResource protocol, String url) {
  if (context == null)
    throw new IllegalArgumentException("No context provided.");
  this.context = context;
  this.info = new ResourceInfo(url, protocol.getUserName(), protocol.getUserPassword());
}

public Capabilities getCapabilities() {
  return capabilities;
}

public Query newQuery(Criteria crt) {
  RequestContext requestContext = RequestContext.extract(null);
  try {
    ResourceProcessor processor = new ProcessorFactory().interrogate(newProcessingContext(requestContext), info.getUrl(), info.newCredentials());
    return processor.createQuery(context, crt);
  } catch (IOException ex) {
    context.onIterationException(ex);
    return null;
  } finally {
    requestContext.onExecutionPhaseCompleted();
  }
}

public Native getNativeResource() {
  RequestContext requestContext = RequestContext.extract(null);
  try {
    ResourceProcessor processor = new ProcessorFactory().interrogate(newProcessingContext(requestContext), info.getUrl(), info.newCredentials());
    return processor!=null? processor.getNativeResource(context): null;
  } catch (IOException ex) {
    context.onIterationException(ex);
    return null;
  } finally {
    requestContext.onExecutionPhaseCompleted();
  }
}

/**
 * Creates new processing context.
 * @param requestContext request context
 * @return processing context
 */
private ProcessingContext newProcessingContext(RequestContext requestContext) {
  Publisher publisher = null;
  HttpClientRequest httpClient = HttpClientRequest.newRequest();
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
