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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Accumulates named frequencies into a sortable list.
 */
class FrequencyAccumulator {

  /** instance variables ====================================================== */
  private List<NamedFrequency>           list = new ArrayList<NamedFrequency>();
  private HashMap<String,NamedFrequency> map = new HashMap<String,NamedFrequency>();
  private long                           totalFrequency = 0;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public FrequencyAccumulator() {}
 
  /** properties  ============================================================= */
  
  /**
   * Gets the list of named frequencies.
   * @return the frequencies
   */
  public List<NamedFrequency> getFrequencies() {
    return this.list;
  }
  
  /**
   * Gets the total frequency across the collection.
   * @return the total frequency
   */
  public long getTotalFrequency() {
    return this.totalFrequency;
  }
  
  /** methods ================================================================= */
  
  /**
   * Adds a named frequency to the collection.
   * @param name the name
   * @param frequency the frequency
   */
  public void add(String name, long frequency) {
    String key = name.toLowerCase();
    NamedFrequency namedFrequency = this.map.get(key);
    if (namedFrequency == null) {
      namedFrequency = new NamedFrequency(name,frequency);
      this.map.put(key,namedFrequency);
      this.list.add(namedFrequency);
    } else {
      namedFrequency.incrementFrequency(frequency);
    }
    this.totalFrequency += frequency;
  }
  
  /**
   * Sorts the list of named frequencies using the FrequencyComparator.
   */
  public void sortByFrequency() {
    Collections.sort(this.list,new FrequencyComparator(FrequencyComparator.SORTBY_FREQUENCY));
  }
  
  /**
   * Sorts the list of named frequencies using the FrequencyComparator.
   */
  public void sortByName() {
    Collections.sort(this.list,new FrequencyComparator(FrequencyComparator.SORTBY_NAME));
  }

}
