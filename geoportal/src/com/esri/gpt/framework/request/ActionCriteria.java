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
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.util.Val;

/**
 * Represents the criteria associated with an action.
 * <p>
 * An action is typically associated with a database modification
 * (create/update/delete).
 */
public class ActionCriteria extends Criteria {

// class variables =============================================================
  
// instance variables ==========================================================
private String    _actionKey = "";
private String    _selectedRecordIds = "";
private StringSet _selectedRecordIdSet = new StringSet(false,true,true);
  
// constructors ================================================================

/** Default constructor. */
public ActionCriteria() {}

/**
 * Construct by duplicating a supplied action criteria.
 * @param criteriaToDuplicate the criteria to duplicate
 */
public ActionCriteria(ActionCriteria criteriaToDuplicate) {
  if (criteriaToDuplicate != null) {
    setActionKey(criteriaToDuplicate.getActionKey());
    setSelectedRecordIds(criteriaToDuplicate.getSelectedRecordIds());
  }
}
  
// properties ==================================================================

/**
 * Gets the key representing the action to be performed.
 * @return the action key
 */
public String getActionKey() {
  return _actionKey;
}
/**
 * Sets the key representing the action to be performed.
 * @param actionKey the action key
 */
public void setActionKey(String actionKey) {
  _actionKey = Val.chkStr(actionKey);
}

/**
 * Gets a delimited string (comma space ;) of record ids selected for
 * an action.
 * <br/>This property is just a store supporting the transfer of UI 
 * selection criteria to the back-end action.
 * @return the delimited string of ids
 */
public String getSelectedRecordIds() {
  return _selectedRecordIds;
}
/**
 * Sets a delimited string (comma space ;) of record ids selected for
 * an action.
 * <br/>This property is just a store supporting the transfer of UI 
 * selection criteria to the back-end action.
 * @param ids the delimited string of ids
 */
public void setSelectedRecordIds(String ids) {
  _selectedRecordIds = Val.chkStr(ids);
  _selectedRecordIdSet.clear();
  _selectedRecordIdSet.addDelimited(getSelectedRecordIds());
}

/**
 * Gets the set of selected record ids.
 * @return the set of selected record ids
 */
public StringSet getSelectedRecordIdSet() {
  return _selectedRecordIdSet;
}

// methods =====================================================================

/**
 * Resets the criteria.
 */
public void reset() {
  
}

}
