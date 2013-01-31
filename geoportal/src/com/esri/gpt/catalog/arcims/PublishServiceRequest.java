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
package com.esri.gpt.catalog.arcims;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;

/**
 * Super-class for an ArcIMS publish service request.
 */
public class PublishServiceRequest extends ImsRequest {

// class variables =============================================================

// instance variables ==========================================================
private Publisher      _publisher;
private RequestContext _requestContext;

// constructors ================================================================

/** Default constructor. */
public PublishServiceRequest() {}

/**
 * Constructs with an associated request context and publisher.
 * @param requestContext the request context
 * @param publisher the publisher
 */
public PublishServiceRequest(RequestContext requestContext,
                             Publisher publisher) {
  setRequestContext(requestContext);
  setPublisher(publisher);
  CatalogConfiguration catConfig = requestContext.getCatalogConfiguration();
  setService(catConfig.getArcImsCatalog().getPublishService());
  setCredentials(getPublisher().getCredentials());
}

// properties ==================================================================

/**
 * Gets the publisher associated with the request.
 * @return the publisher
 */
protected Publisher getPublisher() {
  return _publisher;
}
/**
 * Sets the publisher associated with the request.
 * @param publisher the publisher
 */
protected void setPublisher(Publisher publisher) {
  _publisher = publisher;
}

/**
 * Gets the associated request context.
 * @return the request context
 */
protected RequestContext getRequestContext() {
  return _requestContext;
}
/**
 * Sets the associated request context.
 * @param requestContext the request context
 */
protected void setRequestContext(RequestContext requestContext) {
  _requestContext = requestContext;
}

// methods =====================================================================

/**
 * Executes an ArcIMS service request and parses the response.
 * @throws ImsServiceException if an exception occurs
 */
@Override
protected void executeRequest() throws ImsServiceException {
  super.executeRequest(makeClient(getRequestContext(),getPublisher()));
}


}
