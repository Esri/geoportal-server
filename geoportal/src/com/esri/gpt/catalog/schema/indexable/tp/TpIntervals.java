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
package com.esri.gpt.catalog.schema.indexable.tp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A collection of timeperiod intervals.
 */
public class TpIntervals {
  
  /** instance variables ====================================================== */
  private List<TpInterval> items = new ArrayList<TpInterval>();
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public TpIntervals() {} 
  
  /** methods ================================================================= */
  
  /**
   * Adds an interval.
   * @param interval the interval
   */
  public void add(TpInterval interval) {
    this.items.add(interval);
  }
  
  /**
   * Gets the value at an index.
   * @param index the index
   * @return the value
   */
  public TpInterval get(int index) {
    return this.items.get(index);
  }
  
  /**
   * The number of items.
   * @return the number of items
   */
  public int size() {
    return this.items.size();
  }
  
  /**
   * Sorts the intervals.
   * <br/>This intervals are sorted by ascending lower boundary.
   * <br/>If the lower bounds are equal, sort on descending upper bound.
   */
  public void sort() {
    if (this.size() > 1) {
      Collections.sort(this.items, new Comparator<TpInterval>() {
        @Override 
        public int compare(TpInterval o1, TpInterval o2) {
          if ((o1.getLower() == null) && (o2.getLower() == null)) {
          } else if (o1.getLower() == null) {
            return 1;
          } else if (o2.getLower() == null) {
            return 1;
          } else {
            long nLower1 = o1.getLower().longValue();
            long nLower2 = o2.getLower().longValue();
            if (nLower1 < nLower2) {
              return -1;
            } else if (nLower1 > nLower2) {
              return 1;
            }
          }
          if ((o1.getUpper() != null) && (o2.getUpper() != null)) {
            long nUpper1 = o1.getUpper().longValue();
            long nUpper2 = o2.getUpper().longValue();
            if (nUpper2 > nUpper1) {
              return 1;
            }
          }
          return 0;
        }           
      });      
    }
  }
  
  /**
   * The values.
   * @return the values
   */
  public List<TpInterval> values() {
    return this.items;
  }

}
