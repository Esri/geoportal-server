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
package com.esri.gpt.catalog.search;

/**
 * The Class SearchFilterHarvestSpatial.
 */
@SuppressWarnings("serial")
public class SearchFilterHarvestSpatial extends SearchFilterSpatial {

/**
 * Sets the selected bounds.
 * 
 * @param selectedBounds the new selected bounds
 * 
 * @throws IllegalArgumentException if selectedBounds not in OptionsBounds
 */
@Override
public void setSelectedBounds(String selectedBounds) {
  try {
    OptionsBounds.valueOf(selectedBounds);
    super.setSelectedBounds(selectedBounds);
  } catch(Exception e) {
    super.setSelectedBounds(OptionsBounds.anywhere.toString());
  }
}

/**
 * @throws SearchException
 */
@Override
public void validate() throws SearchException {
  String selectedBounds = this.getSelectedBounds();
  if(selectedBounds == null || "".equals(selectedBounds)) {
    this.setSelectedBounds(OptionsBounds.anywhere.toString());
  }
}




}
