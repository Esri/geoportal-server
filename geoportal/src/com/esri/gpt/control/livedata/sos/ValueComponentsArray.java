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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Collection of value components.
 */
public class ValueComponentsArray extends ArrayList<ValueComponents> {

/**
 * Gets all the names.
 * @return all the names
 */
public Collection<String> getNames() {
  HashSet<String> names = new HashSet<String>();
  for (ValueComponents vc : this) {
    names.addAll(vc.keySet());
  }
  return names;
}

/**
 * Normalizes all the value components.
 * Assures that object has all the values specified in the argument, even if
 * hasempty values.
 */
public void normalize() {
  Collection<String> names = getNames();
  for (ValueComponents vc : this) {
    vc.normalize(names);
  }
}

/**
 * Selects all the values by name.
 * @param name value name
 * @return collection of values
 */
public ArrayList<Double> select(String name) {
  ArrayList<Double> values = new ArrayList<Double>();
  for (ValueComponents vc : this) {
    Double value = vc.get(name);
    values.add(value);
  }
  return values;
}
}
