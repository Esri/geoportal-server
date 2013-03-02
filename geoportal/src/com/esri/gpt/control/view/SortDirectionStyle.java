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

import com.esri.gpt.framework.util.Val;

/**
 * Sort direction style.
 * <p/>
 * Keeps CSS style name for each sort state of column header. Column can be
 * sorted in <i>ascending</i> order, <i>descending</i> order, or might be
 * <i>unsorted</i>. Anyof this style will be returned by 
 * <code>BaseSortDirectionStyleMap</code> during sort order evaluation.
 * @see BaseSortDirectionStyleMap
 */
public class SortDirectionStyle {

// class variables =============================================================

// instance variables ==========================================================
/** CSS style for column sorted in ascending order. */
private String _styleAsc = "";
/** CSS style for column sorted in descending order. */
private String _styleDesc = "";
/** CSS style for unsorted column. */
private String _styleNone = "";

// constructors ================================================================

// properties ==================================================================

/**
 * Gets CSS style for header of column sorted in ascending order.
 * @return CSS style name
 */
public String getStyleAsc() {
  return _styleAsc;
}

/**
 * Sets CSS style for header of column sorted in ascending order.
 * @param styleAsc CSS style name
 */
public void setStyleAsc(String styleAsc) {
  _styleAsc = Val.chkStr(styleAsc);
}

/**
 * Gets CSS style for header of column sorted in descending order.
 * @return CSS style name
 */
public String getStyleDesc() {
  return _styleDesc;
}

/**
 * Sets CSS style for header of column sorted in descending order.
 * @param styleDesc CSS style name
 */
public void setStyleDesc(String styleDesc) {
  _styleDesc = Val.chkStr(styleDesc);
}

/**
 * Gets CSS style for header of unsorded column.
 * @return CSS style name
 */
public String getStyleNone() {
  return _styleNone;
}

/**
 * Sets CSS style for header of unsorted column.
 * @param styleNone CSS style name
 */
public void setStyleNone(String styleNone) {
  _styleNone = Val.chkStr(styleNone);
}

// methods =====================================================================

/**
 * Gets string representatio of the style object.
 * @return string representation
 */
@Override
public String toString() {
  return getStyleAsc() + " " + getStyleDesc() + " " + getStyleNone();
}

}
