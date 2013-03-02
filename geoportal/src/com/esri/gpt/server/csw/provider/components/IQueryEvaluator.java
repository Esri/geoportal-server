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
package com.esri.gpt.server.csw.provider.components;

/**
 * Interface for evaluating a CSW query.
 */
public interface IQueryEvaluator {
  
  /**
   * Builds and evaluates an ID based query.
   * @param context the operation context
   * @param ids the IDs to query
   * @throws Exception if a processing exception occurs
   */
  public void evaluateIdQuery(OperationContext context, String[] ids) throws Exception;
  
  /**
   * Evaluates the query.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void evaluateQuery(OperationContext context) throws Exception;

}
