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
package com.esri.gpt.catalog.lucene.stats;
import java.util.Comparator;

/**
 * Named frequency comparator, first by decending frequency, then by ascending name.
 */
class FrequencyComparator implements Comparator<NamedFrequency> {
  
  /** class variables ========================================================= */
  
  /** Sort by descending frequency, then by ascending name, value="frequency" */
  public static final String SORTBY_FREQUENCY = "frequency";
  
  /** Sort by ascending name then by descending frequenc, value="name" */
  public static final String SORTBY_NAME = "name";
  
  /** instance variables ====================================================== */
  private String sortBy = FrequencyComparator.SORTBY_FREQUENCY;

  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied sort option.
   * @param sortBy the sort option 
   *   (FrequencyComparator.SORTBY_NAME or FrequencyComparator.SORTBY_FREQUENCY)
   */
  public FrequencyComparator(String sortBy) {
    if (sortBy.equalsIgnoreCase(FrequencyComparator.SORTBY_NAME)) {
      this.sortBy = FrequencyComparator.SORTBY_NAME;
    } else {
      this.sortBy = FrequencyComparator.SORTBY_FREQUENCY;
    }
  }
  
  /** methods ================================================================= */
  
  /**
   * Compare two named frequencies.
   * @param arg0 the first comparable
   * @param arg1 the second comparable
   */
  public int compare(NamedFrequency arg0, NamedFrequency arg1) {
    boolean byName = this.sortBy.equalsIgnoreCase(FrequencyComparator.SORTBY_NAME);
    if (byName) {
      
      // sort by ascending name, then by descending frequency
      if (!arg0.getName().equalsIgnoreCase(arg1.getName())) {
        return arg0.getName().compareToIgnoreCase(arg1.getName());
      } else {
        if (arg1.getFrequency() > arg0.getFrequency()) {
          return 1;
        }
      }
    } else {
      
      // sort by descending frequency, then by ascending name
      if (arg1.getFrequency() > arg0.getFrequency()) {
        return 1;
      } else if (arg0.getFrequency() == arg1.getFrequency()) {
        return arg0.getName().compareToIgnoreCase(arg1.getName());
      }
    }
      
    return 0;
  }

}
