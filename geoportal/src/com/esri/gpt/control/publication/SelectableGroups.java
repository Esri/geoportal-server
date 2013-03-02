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
package com.esri.gpt.control.publication;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.Group;

import java.util.ArrayList;
import javax.faces.model.SelectItem;

/**
 * Defines a list of candidate access groups from the user interface.
 */
public class SelectableGroups {

// class variables =============================================================

// instance variables ==========================================================
  private ArrayList<SelectItem> _list = new ArrayList<SelectItem>();
  private String _selectedKey = "";

// constructors ================================================================
  /** Default constructor. */
  public SelectableGroups() {
  }

// properties ==================================================================
  /**
   * Determines if the items list contains multiple groups.
   * @return true if the items list is greater that 1
   */
  public boolean getHasMultiple() {
    return (_list.size() > 1);
  }

  /**
   * Gets the SelectItem list for UI display.
   * @return the SelectItem list
   */
  public ArrayList<SelectItem> getItems() {
    return _list;
  }

  /**
   * Gets the size of the list.
   * @return size of selactable groups
   */
  public int getSize() {
    return _list.size();
  }

  /**
   * Gets the currently selected key.
   * @return the currently selected key
   */
  public String getSelectedKey() {
    return _selectedKey;
  }

  /**
   * Sets the currently selected key.
   * @param key the currently selected key
   */
  public void setSelectedKey(String key) {
    _selectedKey = Val.chkStr(key);
  }

// methods =====================================================================
  /**
   * Builds the list of candidate access.
   * @param context the active request context
   */
  public void buildAllGroups(RequestContext context) {
    _list.clear();
    Groups groups = Publisher.buildSelectableGroups(context);
    for (Group g : groups.values()) {
      _list.add(new SelectItem(g.getKey(), g.getName()));
    }
  }

  /**
   * Builds the list of candidate access.
   * @param item SelectItem
   */
  public void buildConfiguredGroup(SelectItem item) {
    _list.clear();
    _list.add(item);

  }
}
