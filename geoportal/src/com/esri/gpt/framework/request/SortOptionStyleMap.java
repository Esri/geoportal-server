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
import com.esri.gpt.framework.jsf.FacesMap;

/**
 * Provides a Map interface for setting the style class for
 * a column header. 
 * <p>Example:<br/>
 * styleClass="#{SomeController.queryCriteria.sortOption.styleMap['name']}"
 */
public class SortOptionStyleMap extends FacesMap<String> {
  
// class variables =============================================================

// instance variables ==========================================================
private SortOption _sortOption;
  
// constructors ================================================================
 
/**
 * Creates new instance of sort option style map.
 * @param sortOption sort option
 */
public SortOptionStyleMap(SortOption sortOption) {
  _sortOption = sortOption;
}

// properties ==================================================================

// methods =====================================================================

/**
 * Implements the "get" method for a Map to determine the style class for a 
 * column key.
 * <br/>"ascending" is returned if the sort direction is ascending and
 * the supplied column key matches the active column key
 * <br/>"descending" is returned if the sort direction is descending and
 * the supplied column key matches the active column key
 * <br/>"" is returned if the the supplied column key does not match the
 * active column key 
 * @param columnKey the subject column key
 * @return the style class for the column key 
 */
public String get(Object columnKey) {
  if ((_sortOption != null) && 
      (columnKey != null) && (columnKey instanceof String)) {
    return _sortOption.getStyleClass((String)columnKey);
  }
  return "";
}

}
