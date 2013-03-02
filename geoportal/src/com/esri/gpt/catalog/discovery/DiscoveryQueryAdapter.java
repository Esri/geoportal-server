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
package com.esri.gpt.catalog.discovery;
import com.esri.gpt.framework.context.RequestContext;

/**
 * An abstract class defining the interface for the execution of
 * a discovery query.
 */
public abstract class DiscoveryQueryAdapter {
  
  /** methods ================================================================= */

  /**
   * Executes a query for metadata documents.
   * @param context the active request context
   * @param discoveryQuery the query to execute
   * @throws DiscoveryException if an exception occurs
   */
  public abstract void execute(RequestContext context, 
                               DiscoveryQuery discoveryQuery) 
    throws DiscoveryException;
  
}
