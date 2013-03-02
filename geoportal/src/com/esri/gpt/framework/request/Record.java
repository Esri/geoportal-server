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
 * Represents a record within a collection of records.
 * <p>
 * A record typically represents a row within a database table, or an item
 * associated with a remote service.
 */
public class Record {

// class variables =============================================================
  
// instance variables ==========================================================
private boolean _isSelected = false;
  
// constructors ================================================================

/** Default constructor. */
public Record() {}
  
// properties ==================================================================

/**
 * Gets the selected status for the record.
 * @return <code>true</code> if selected
 */
public boolean getIsSelected() {
  return _isSelected;
}
/**
 * Sets the selected status for the record.
 * @param isSelected <code>true</code> if selected
 */
public void setIsSelected(boolean isSelected) {
  _isSelected = isSelected;
}

// methods =====================================================================

}
