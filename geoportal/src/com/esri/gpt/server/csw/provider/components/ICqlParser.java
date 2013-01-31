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
 * Interface for parsing the csw:CqlText portion of a CSW request.
 */
public interface ICqlParser {
  
  /**
   * Parses the csw:CqlText expression.
   * @param context the operation context
   * @param cql the CQL expression
   * @throws OwsException if validation fails
   */
  public void parseCql(OperationContext context, String cql) 
    throws OwsException;

}
