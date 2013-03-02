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
package com.esri.ontology.service.catalog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Collection of terms.
 */
public class Terms extends ArrayList<Term> {

  /**
   * Sorts content by term 'count' factor.
   */
  public void sortByCount() {

    NeighborsMap neighbors = new NeighborsMap();
    neighbors.addAll(this);

    this.clear();
    this.addAll(neighbors.values());

    Collections.sort(this,new Comparator<Term>(){
      public int compare(Term o1, Term o2) {

        if (o1.getCount() < o2.getCount()) {
          return 1;
        }
        if (o1.getCount() > o2.getCount()) {
          return -1;
        }

        return o1.getSubject().compareTo(o2.getSubject());
      }
    });
  }

  /**
   * Removes all terms with count less than given threshold
   * @param threshold threshold
   */
  public void removeByThreshold(float threshold) {
    ArrayList<Term> underThreshold = new ArrayList<Term>();
    for (Term t : this) {
      if (t.getCount()<threshold) {
        underThreshold.add(t);
      }
    }
    this.removeAll(underThreshold);
  }
}
