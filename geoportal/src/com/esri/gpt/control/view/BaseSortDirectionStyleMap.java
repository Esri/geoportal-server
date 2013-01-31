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
package com.esri.gpt.control.view;

import com.esri.gpt.framework.jsf.FacesMap;
import com.esri.gpt.framework.request.SortOption;
import com.esri.gpt.framework.util.Val;

/**
 * Base (abstract) sort direction style map.
 * <p/>
 * Helps with CSS style name evaluation for the table header title. CSS style
 * name evaluation is based upon two factors: <i>sort direction style</i> 
 * stored within the object, and <code>SortOption</code> provided runtime by the
 * concrete implementation of the class.
 * @see com.esri.gpt.framework.request.SortOption
 */
public abstract class BaseSortDirectionStyleMap extends FacesMap<String> {

// class variables =============================================================

// instance variables ==========================================================
/** Sort direction style. */
private SortDirectionStyle _style = new SortDirectionStyle();

// constructors ================================================================

// properties ==================================================================
/**
 * Gets header style for the given column. 
 * @param key column name
 * @return style name
 */
@Override
public String get(Object key) {
  String name = Val.chkStr(key instanceof String ? (String) key : "");
  if (getSortOption().getColumnKey().equalsIgnoreCase(name)) {
    switch (getSortOption().getDirection()) {
      case asc:
        return getStyle().getStyleAsc();
      case desc:
        return getStyle().getStyleDesc();
      }
  }
  return getStyle().getStyleNone();
}

/**
 * Gets sort direcion style.
 * @return sort direction style
 */
public SortDirectionStyle getStyle() {
  return _style;
}

/**
 * Sets sort direction style.
 * @param style sort direction style
 */
public void setStyle(SortDirectionStyle style) {
  _style = style!=null? style: new SortDirectionStyle();
}

// methods =====================================================================

/**
 * Gets sort option.
 * @return sort option
 */
public abstract SortOption getSortOption();

}
