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
package com.esri.gpt.framework.request;

/**
 * Interface for a query or an action request.
 * <p>
 * <br/>generic: RDT represents the request definition type
 */
public interface IRequest
       <RDT extends RequestDefinition<? extends Criteria, ? extends Result>> {

// class variables =============================================================
  
// instance variables ==========================================================
  
// constructors ================================================================
  
// properties ==================================================================

/**
 * Gets the definition for the request.
 * @return the request definition
 */
public RDT getRequestDefinition();

/**
 * Sets the definition for the request.
 * @param requestDefinition the request definition
 */
public void setRequestDefinition(RDT requestDefinition);

// methods =====================================================================

}
