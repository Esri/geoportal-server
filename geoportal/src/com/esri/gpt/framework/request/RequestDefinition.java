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
 * Defines the criteria for and the result of a request.
 * <p>
 * <br/>generic: CT represents the criteria type
 * <br/>generic: RT represents the result type
 */
public class RequestDefinition<CT extends Criteria, RT extends Result> {

// class variables =============================================================
  
// instance variables ==========================================================
private CT _criteria;
private RT _result;
  
// constructors ================================================================

/** Default constructor. */
public RequestDefinition() {
  this(null,null);
}

/**
 * Constructs with a supplied criteria and result.
 * @param criteria the request criteria
 * @param result the request result
 */
public RequestDefinition(CT criteria, RT result) {
  setCriteria(criteria);
  setResult(result);
}
  
// properties ==================================================================

/**
 * Gets the criteria for the request.
 * @return the request criteria (possibly null)
 */
public CT getCriteria() {
  return _criteria;
}
/**
 * Sets the criteria for the request.
 * @param criteria the request criteria 
 */
public void setCriteria(CT criteria) {
  _criteria = criteria;
}

/**
 * Gets the result of the request.
 * @return the request result (possibly null)
 */
public RT getResult() {
  return _result;
}
/**
 * Sets the result of the request.
 * @param result the request result
 */
public void setResult(RT result) {
  _result = result;
}

// methods =====================================================================

}
