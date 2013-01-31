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

import com.esri.gpt.framework.request.*;

/**
 * Sort direction style map concrete class example.
 */
public class SortDirectionStyleMap extends BaseSortDirectionStyleMap {

// class variables =============================================================

// instance variables ==========================================================
/** Sort option. */
private SortOption _sortOption = new SortOption();

// properties ==================================================================

/**
 * Sets sort option.
 * @param sortOption sort option
 */
public void setSortOption(SortOption sortOption) {
  _sortOption = sortOption!=null? sortOption: new SortOption();
}

/**
 * Gets sort option
 * @return sort option
 */
@Override
public SortOption getSortOption() {
  return _sortOption;
}

}
