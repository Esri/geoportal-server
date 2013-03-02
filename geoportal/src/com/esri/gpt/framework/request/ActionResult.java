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
 * Represents the result associated with an action.
 * <p>
 * An action is typically associated with a database modification
 * (create/update/delete).
 */
public class ActionResult extends Result {

// class variables =============================================================
  
// instance variables ==========================================================
private int _numRecordsModified = 0;
  
// constructors ================================================================

/** Default constructor. */
public ActionResult() {}
  
// properties ==================================================================

/**
 * Gets the number of records that were modified.
 * @return the number of records that were modified
 */
public int getNumberOfRecordsModified() {
  return _numRecordsModified;
}
/**
 * Sets the number of records that were modified..
 * @param numModified the number of records that were modified
 */
public void setNumberOfRecordsModified(int numModified) {
  _numRecordsModified = numModified;
}

// methods =====================================================================

/**
 * Resets the result.
 */
public void reset() {
  setNumberOfRecordsModified(0);
}

}
