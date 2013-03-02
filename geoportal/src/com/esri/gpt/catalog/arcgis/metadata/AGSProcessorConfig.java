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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * AGSProcessor configuration.
 */
public class AGSProcessorConfig {
  
  /** class variables ========================================================= */
  
  /** Logger */
  private static final Logger LOGGER = Logger.getLogger(AGSProcessorConfig.class.getName());
  
  /** Availability status */
  private static Boolean ISAVAILABLE = null; 
  

  /** constructors ============================================================ */

  /** Default constructor. */
  public AGSProcessorConfig() {}
  
  /** methods ================================================================= */
  
  
  /**
   * Determine if the AGSProcessor processor is available.
   * <br/>Attempt to reference com.esri.arcgisws.ServiceCatalogBindingStub 
   * from arcgis_agsws_stubs.jar.
   * @return <code>true</code> if the processor is available
   */
  public static boolean isAvailable() {
    if (ISAVAILABLE == null) {
      try {
        Class<?> cls = Class.forName("com.esri.arcgisws.ServiceCatalogBindingStub");
        ISAVAILABLE = Boolean.TRUE;
        return true;
      } catch (Exception e) {
        ISAVAILABLE = Boolean.FALSE;
        String msg = "Unable to reference com.esri.arcgisws.ServiceCatalogBindingStub";
        msg += ", arcgis_agsws_stubs.jar is likely missing.";
        LOGGER.log(Level.WARNING,msg,e);
      }
      return false;
    } else {
      return ISAVAILABLE.booleanValue();
    }
  }

}
