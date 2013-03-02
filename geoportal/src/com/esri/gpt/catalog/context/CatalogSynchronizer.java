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
package com.esri.gpt.catalog.context;
import com.esri.gpt.catalog.lucene.LuceneIndexSynchronizer;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.scheduler.IScheduledTask;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Background thread to ensure catalog synchronization.
 */
public class CatalogSynchronizer implements Runnable, IScheduledTask {

  /** class variables ========================================================= */
  private static final Logger LOGGER = Logger.getLogger(CatalogSynchronizer.class.getName());

  /** instance variables ====================================================== */
  private StringAttributeMap parameters;
  
  /** constructors  =========================================================== */

  /** Default constructor. */
  public CatalogSynchronizer() {}

  /** properties  ============================================================= */
  
  /**
   * Sets the configuration paramaters for the task.
   * @param parameters the configuration paramaters
   */
  public void setParameters(StringAttributeMap parameters) {
    this.parameters = parameters;
  }

  /** methods ================================================================= */
	
  /**
   * Run the synchronization process.
   */
  public void run() {
    try {
      
      LuceneIndexSynchronizer lis = new LuceneIndexSynchronizer(this.parameters);
      lis.syncronize();

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Unknown error.", e);
    }
  }

}

