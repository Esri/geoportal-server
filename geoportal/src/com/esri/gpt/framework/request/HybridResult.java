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
 * Holds both an action and query result.
 * <p>
 * <br/>generic: ART represents the ActionResult type
 * <br/>generic: QRT represents the QueryResult type
 */
public class HybridResult<ART extends ActionResult,
                          QRT extends QueryResult<? extends Records<? extends Record>>>
       extends Result {

// class variables =============================================================
  
// instance variables ==========================================================
private ART _actionResult;
private QRT _queryResult;
  
// constructors ================================================================

/** Default constructor. */
public HybridResult() {
  this(null,null);
}

/**
 * Constructs with supplied results.
 * @param actionResult the action result
 * @param queryResult the query result
 */
public HybridResult(ART actionResult, QRT queryResult) {
  setActionResult(actionResult);
  setQueryResult(queryResult);
}
  
// properties ==================================================================

/**
 * Gets the action result.
 * @return the action result
 */
public ART getActionResult() {
  return _actionResult;
}
/**
 * Sets the action result.
 * @param result the action result
 */
public void setActionResult(ART result) {
  _actionResult = result;
}

/**
 * Gets the query result.
 * @return the query result
 */
public QRT getQueryResult() {
  return _queryResult;
}
/**
 * Sets the query result.
 * @param result the query result
 */
public void setQueryResult(QRT result) {
  _queryResult = result;
}

// methods =====================================================================

/**
 * Resets the result.
 */
public void reset() {
  getActionResult().reset();
  getQueryResult().reset();
}

}
