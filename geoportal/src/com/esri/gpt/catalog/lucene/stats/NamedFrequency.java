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

/**
 * Associated a frequency with a name.
 */
class NamedFrequency {
  
  /** instance variables ====================================================== */
  private long   frequency = 0;
  private String name;
  
  /** constructors ============================================================ */
  
  /**
   * Constructs with a supplied name and frequency.
   * @param name the name
   * @param frequency the frequency
   */
  public NamedFrequency(String name, long frequency) {
    this.name = name;
    this.frequency = frequency;
  }

  /** properties  ============================================================= */
  
  /**
   * Gets the frequency.
   * @return the frequency
   */
  public long getFrequency() {
    return this.frequency;
  }
  
  /**
   * Gets the name.
   * @return the name
   */
  public String getName() {
    return this.name;
  }
  
  /** methods ================================================================= */
  
  /**
   * Increments the frequency by the supplied amount.
   * @param frequency the increment amount
   */
  public void incrementFrequency(long frequency) {
    this.frequency += frequency;
  }
  
}
