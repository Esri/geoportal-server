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
package com.esri.gpt.server.assertion.handler;
import com.esri.gpt.server.assertion.components.AsnContext;
import com.esri.gpt.server.assertion.index.AsnIndexAdapter;

/**
 * Super-class for an assertion operation handler.
 */
public abstract class AsnOperationHandler {
  
  /** instance variables ====================================================== */
  private AsnIndexAdapter indexAdapter;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnOperationHandler() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the index adapter associated with the operation.
   * @return the assertion index adapter
   */
  public AsnIndexAdapter getIndexAdapter() {
    return this.indexAdapter;
  }
  /**
   * Sets the index adapter associated with the operation.
   * @param adapter the assertion index adapter
   */
  public void setIndexAdapter(AsnIndexAdapter adapter) {
    this.indexAdapter = adapter;
  }
  
  /** methods ================================================================= */
  
  /**
   * Handles an assertion operation.
   * @param context the assertion operation context
   * @throws Exception if a processing exception occurs
   */
  public abstract void handle(AsnContext context) throws Exception;
  
}
