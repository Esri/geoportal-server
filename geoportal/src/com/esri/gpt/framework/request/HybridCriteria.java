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
 * Holds both action and query criteria.
 * <p>
 * <br/>generic: ACT represents the ActionCriteria type
 * <br/>generic: QCT represents the QueryCriteria type
 */
public class HybridCriteria<ACT extends ActionCriteria, 
                            QCT extends QueryCriteria> 
       extends Criteria {

// class variables =============================================================
  
// instance variables ==========================================================
private ACT _actionCriteria;
private QCT _queryCriteria;
  
// constructors ================================================================

/** Default constructor. */
public HybridCriteria() {
  this(null,null);
}

/**
 * Constructs with supplied criteria.
 * @param actionCriteria the action criteria
 * @param queryCriteria the query criteria
 */
public HybridCriteria(ACT actionCriteria, QCT queryCriteria) {
  setActionCriteria(actionCriteria);
  setQueryCriteria(queryCriteria);
}
  
// properties ==================================================================

/**
 * Gets the action criteria.
 * @return the action criteria
 */
public ACT getActionCriteria() {
  return _actionCriteria;
}
/**
 * Sets the action criteria.
 * @param criteria the action criteria
 */
public void setActionCriteria(ACT criteria) {
  _actionCriteria = criteria;
}

/**
 * Gets the query criteria.
 * @return the query criteria
 */
public QCT getQueryCriteria() {
  return _queryCriteria;
}
/**
 * Sets the query criteria
 * @param criteria the query criteria
 */
public void setQueryCriteria(QCT criteria) {
  _queryCriteria = criteria;
}

// methods =====================================================================

/**
 * Resets the criteria.
 */
public void reset() {
  getActionCriteria().reset();
  getQueryCriteria().reset();
}

}
