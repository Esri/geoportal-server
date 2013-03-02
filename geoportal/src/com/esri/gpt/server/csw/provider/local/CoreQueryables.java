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
import com.esri.gpt.catalog.discovery.Discoverables;
import com.esri.gpt.catalog.discovery.PropertyMeanings;
import com.esri.gpt.catalog.schema.Schemas;
import com.esri.gpt.framework.context.RequestContext;

/**
 * Defines CSW queryables associated with the Dublin core.
 */
public class CoreQueryables {
      
  /** instance variables ====================================================== */  
  private PropertyMeanings propertyMeanings;
  
  /** constructors ============================================================ */

  /** Default constructor. */
  public CoreQueryables(RequestContext requestContext) {  
    Schemas schemas = requestContext.getCatalogConfiguration().getConfiguredSchemas();
    this.propertyMeanings = schemas.getPropertyMeanings();
  }
  
  /** properties ============================================================== */

  /**
   * Gets the map of all discoverables keyed on alias names.
   * @return the map of all aliased discoverables
   */
  public AliasedDiscoverables getAllAliased() {
    return this.propertyMeanings.getAllAliased();
  }
  
  /**
   * Gets the collection of discoverables associated with the brief element set.
   * @return the brief set
   */
  public Discoverables getBrief() {
    return this.propertyMeanings.getDcPropertySets().getBrief();
  }
  
  /**
   * Gets the collection of discoverables associated with the full element set.
   * @return the full set
   */
  public Discoverables getFull() {
    return this.propertyMeanings.getDcPropertySets().getFull();
  }
  
  /**
   * Gets the collection of discoverables associated with the summary element set.
   * @return the summary set
   */
  public Discoverables getSummary() {
    return this.propertyMeanings.getDcPropertySets().getSummary();
  }

}
