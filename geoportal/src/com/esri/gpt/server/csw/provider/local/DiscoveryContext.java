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
package com.esri.gpt.server.csw.provider.local;
import com.esri.gpt.catalog.discovery.AliasedDiscoverables;
import com.esri.gpt.catalog.discovery.Discoverable;
import com.esri.gpt.catalog.discovery.Discoverables;
import com.esri.gpt.catalog.discovery.DiscoveryQuery;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.server.csw.provider.components.CswConstants;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.QueryOptions;

/**
 * Provides a context for executing CSW operation against the local catalog. 
 */
public class DiscoveryContext {
    
  /** instance variables ====================================================== */
  private DiscoveryQuery discoveryQuery = new DiscoveryQuery();
  private CoreQueryables queryables;
  
  /** constructors ============================================================ */
  
  /** 
   * Constructs with a supplied operation context
   * @param context the operation context
   */
  public DiscoveryContext(OperationContext context) {
    super();
    this.queryables = new CoreQueryables(context.getRequestContext());
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the active discovery query.
   * @return the active discovery query
   */
  public DiscoveryQuery getDiscoveryQuery() {
    return this.discoveryQuery;
  }
  
  /** methods ================================================================= */
  
  /**
   * Finds the discoverable associated with a property name.
   * @param clientName the property name (or alias)
   * @return the discoverable (null if none was found);
   */
  public Discoverable findDiscoverable(String clientName) {
    return this.queryables.getAllAliased().get(clientName);
  }
  
  /**
   * Sets the returnables for the active discovery query.
   * @param context the operation context
   */
  public void setReturnables(OperationContext context) {
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    CoreQueryables queryables = this.queryables;
    DiscoveryQuery query = this.getDiscoveryQuery();
    String elementSetType = qOptions.getElementSetType();
    if (elementSetType == null) {
      
      boolean isDublinCore = qOptions.isDublinCoreResponse();
      StringSet elementNames = qOptions.getElementNames();
      boolean hasElementNames = (elementNames != null) && (elementNames.size() > 0);
      if (isDublinCore && hasElementNames) {
        AliasedDiscoverables aliased = this.queryables.getAllAliased();
        query.setReturnables(new Discoverables());
        for (String elementName: elementNames) {
          Discoverable discoverable = aliased.get(elementName);
          if (discoverable != null) {
            query.getReturnables().add(discoverable);
          }
        }
      } else {
        qOptions.setElementSetType(CswConstants.ElementSetType_Summary);
        query.setReturnables(queryables.getSummary());
      }
      
    } else {
      if (elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Brief)) {
        query.setReturnables(queryables.getBrief());
      } else if (elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Summary)) {
        query.setReturnables(queryables.getSummary());
      } else if (elementSetType.equalsIgnoreCase(CswConstants.ElementSetType_Full)) {
        query.setReturnables(queryables.getFull());
      } else {
        query.setReturnables(queryables.getSummary());
      }
    }
  }

}
