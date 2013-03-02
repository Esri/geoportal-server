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
package com.esri.gpt.catalog.management;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.request.DaoRequest;
import com.esri.gpt.framework.request.RequestDefinition;
import com.esri.gpt.framework.security.principal.Publisher;

/**
 * Super-class for metadata management requests. 
 */
public class MmdRequest 
             extends DaoRequest<RequestDefinition<MmdCriteria,MmdResult>> {
  
// class variables =============================================================

// instance variables ==========================================================
private Publisher _publisher;

// constructors ================================================================

/**
 * Construct a metadata management request.
 * @param requestContext the request context
 * @param publisher the publisher
 * @param criteria the request criteria
 * @param result the request result
 */
protected MmdRequest(RequestContext requestContext,
                     Publisher publisher,
                     MmdCriteria criteria,
                     MmdResult result) {
  super(requestContext,
        new RequestDefinition<MmdCriteria,MmdResult>(criteria,result));
  setPublisher(publisher);
}
 
// properties ==================================================================

/**
 * Gets the action criteria.
 * @return the action criteria
 */
public MmdActionCriteria getActionCriteria() {
  return getRequestDefinition().getCriteria().getActionCriteria();
}

/**
 * Gets the action result.
 * @return the action result
 */
public MmdActionResult getActionResult() {
  return getRequestDefinition().getResult().getActionResult();
}

/**
 * Gets the ArcIMS service permissions table name.
 * @return the ArcIMS service permissions table name
 */
protected String getResourceTableName() {
  return getRequestContext().getCatalogConfiguration().getResourceTableName();
}

/**
 * Gets the Publisher associated with this request.
 * @return the Publisher
 */
protected Publisher getPublisher() {
  return _publisher;
}
/**
 * Sets the publisher associated with this request.
 * @param publisher the publisher
 */
private void setPublisher(Publisher publisher) {
  _publisher = publisher;
}

/**
 * Gets the query criteria.
 * @return the query criteria
 */
public MmdQueryCriteria getQueryCriteria() {
  return getRequestDefinition().getCriteria().getQueryCriteria();
}

/**
 * Gets the query result.
 * @return the query result
 */
public MmdQueryResult getQueryResult() {
  return getRequestDefinition().getResult().getQueryResult();
}

// methods =====================================================================

}
