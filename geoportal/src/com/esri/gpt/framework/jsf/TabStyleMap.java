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
package com.esri.gpt.framework.jsf;
import com.esri.gpt.framework.util.Val;

/**
 * Provides a Map interface for setting the style class of a 
 * menu navigation tab based upon a supplied tab id.
 * <p>Example:<br/>
 * styleClass="#{PageContext.tabStyleMap['catalog.search']}"
 */
public class TabStyleMap extends FacesMap<String> {
  
// class variables =============================================================

// instance variables ==========================================================
private String _activePageId = "";
private String _activeTabId = "";
  
// constructors ================================================================
 
/**
 * Constructs with the id associated with the active page.
 * @param activeTabId the id of the active page
 * @param activePageId the tab id of the active page
 */
public TabStyleMap(String activeTabId, String activePageId) {
  _activeTabId = Val.chkStr(activeTabId);
  _activePageId = Val.chkStr(activePageId);
}

// properties ==================================================================

// methods =====================================================================

/**
 * Implements the "get" method for a Map to determine the style class of a 
 * tab navigation link.
 * <p>
 * If the supplied tab id matches the active page's tab id "current" is returned,
 * otherwise an empty string is returned.
 * @param tabId the active tab id
 * @return the style class name
 */
@Override
public String get(Object tabId) {
  if ((tabId != null) && (tabId instanceof String)) {
    String sTabId = Val.chkStr((String)tabId);
    if (sTabId.length() > 0) {
      if (_activeTabId.length() > 0) {
        if (sTabId.equalsIgnoreCase(_activeTabId)) return "current";
      } else if (_activePageId.length() > 0) {
        if (_activePageId.startsWith(sTabId)) return "current";
      }
    }
  }
  return "";
}

}
