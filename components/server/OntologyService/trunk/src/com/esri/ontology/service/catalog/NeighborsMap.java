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

import java.util.Collection;
import java.util.TreeMap;

/**
 * Map of neighbors.
 */
/* package */ class NeighborsMap extends TreeMap<String,Term> {

  @Override
  public boolean containsKey(Object key) {
    return super.containsKey(key.toString().toLowerCase());
  }

  @Override
  public Term get(Object key) {
    return super.get(key.toString().toLowerCase());
  }

  @Override
  public Term put(String key, Term value) {
    return super.put(key.toLowerCase(), value);
  }

  /**
   * Adds a neighbor indexed by subject.
   * If duplicated neighbors found, only with highest count will be retained.
   * @param neighbor neighbor to add
   */
  public void add(Term neighbor) {
    String key = neighbor.getSubject();
    if (containsKey(key)) {
      Term n = get(key);
      if (neighbor.getCount()>n.getCount()) {
        put(key,neighbor);
      }
    } else {
      put(key,neighbor);
    }
  }

  /**
   * Adds all neighbors from the collection.
   * @param neighbors collection of neighbors
   */
  public void addAll(Collection<? extends Term> neighbors) {
    for (Term n : neighbors) {
      add(n);
    }
  }
}
