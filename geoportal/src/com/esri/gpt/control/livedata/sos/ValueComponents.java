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
package com.esri.gpt.control.livedata.sos;

import java.util.Collection;
import java.util.HashMap;

/**
 * Collection of value readings. Represents a single reading of a multiple
 * features performed at a single point of interest at a single specific time.
 */
public class ValueComponents extends HashMap<String, Double> {

  /**
   * Normalizes value.
   * Assures that object has all the values specified in the argument, even if
   * hasempty values.
   * @param names collection of names
   */
  public void normalize(Collection<String> names) {
    for (String name : names) {
      if (!containsKey(name)) {
        put(name, Double.NaN);
      }
    }
  }

  /**
   * Stores named value given as string.
   * If value is not a valid double type value, 'Not a Number' is stored instead.
   * @param name value name
   * @param value value
   */
  public void put(String name, String value) {
    try {
      put(name, Double.parseDouble(value));
    } catch (NumberFormatException ex) {
      put(name, Double.NaN);
    }
  }
}
