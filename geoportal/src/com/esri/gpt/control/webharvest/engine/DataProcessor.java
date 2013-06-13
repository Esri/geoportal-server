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
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.framework.resource.api.Publishable;
import java.io.IOException;
import java.sql.SQLException;
import javax.xml.transform.TransformerConfigurationException;

/**
 * Data processor.
 */
public interface DataProcessor {
  /**
   * Gets name of the processor.
   * @return name
   */
  String getName();

  /**
   * Called upon the end of harvesting of the resource.
   * @param unit execution unit
   */
  void onEnd(final ExecutionUnit unit, boolean success);

  /**
   * Called upon iteration exception.
   * @param unit execution unit
   * @param ex exception
   */
  void onIterationException(ExecutionUnit unit, Exception ex);

  /**
   * Called uppon harvesting a single metadata.
   * @param unit execution unit
   * @param record record to publish
   * @throws IOException if reading metadata fails
   * @throws SQLException if  processing metadata fails
   * @throws CatalogIndexException if operation on index fails
   * @throws TransformerConfigurationException if processing metadata fails
   */
  void onMetadata(ExecutionUnit unit, Publishable record) throws IOException, SQLException, CatalogIndexException, TransformerConfigurationException;

  /**
   * Called upon the start of harvesting the resource.
   * @param unit execution unit
   */
  void onStart(ExecutionUnit unit);
  
}
