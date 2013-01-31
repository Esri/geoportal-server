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
 * menu navigation link based upon a supplied page id.
 * <p>Example:<br/>
 * styleClass="#{PageContext.menuStyleMap['catalog.main.home']}"
 */
public class MenuStyleMap extends FacesMap<String> {
  
// class variables =============================================================

// instance variables ==========================================================
private String _activePageId = "";
  
// constructors ================================================================
 
/**
 * Constructs with the id associated with the active page.
 * @param activePageId the id of the active page
 */
public MenuStyleMap(String activePageId) {
  _activePageId = Val.chkStr(activePageId);
}

// properties ==================================================================

// methods =====================================================================

/**
 * Implements the "get" method for a Map to determine the style class of a 
 * menu navigation link.
 * <p>
 * If the supplied page id matches the active page id "current" is returned,
 * otherwise an empty string is returned.
 * @param pageId array of pageId's separated by comma (,) associated with the menu navigation 
 *        link to check
 * @return the style class name
 */
@Override
public String get(Object pageId) {
  if ((pageId != null) && (pageId instanceof String)) {
    String [] sIds = ((String)pageId).split(",");
    for (String sId : sIds) {
      if (Val.chkStr(sId).equalsIgnoreCase(_activePageId)) {
        return "current";
      }
    }
  }
  return "";
}

}
