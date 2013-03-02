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
package com.esri.gpt.catalog.schema;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Defines a collection of metadata parameters.
 */
public class Parameters extends LinkedHashMap<String,Parameter> {

// class variables =============================================================
    
// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public Parameters() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Parameters(Parameters objectToDuplicate) {
  if (objectToDuplicate != null) {
    for (Parameter member: objectToDuplicate.values()) {
      add(member.duplicate());
    }
  }
}

// properties ==================================================================

// methods =====================================================================

/**
 * Adds a member to the collection.
 * <br/>The member will not be added if it is null or
 * if it has an empty key.
 * @param member the member to add
 */
public void add(Parameter member) {
  if ((member != null) && (member.getKey().length() > 0)) {
    put(member.getKey(),member);
  }
}

/**
 * Selects all parameters conforming to the condiditions defined by predicate.
 * @param predicate predicate
 * @return list of selected parameters
 */
public List<Parameter> selectParameters(Predicate predicate) {
  ArrayList<Parameter> selected = new ArrayList<Parameter>();
  for (Parameter p : values()) {
    if (predicate.eligible(p)) {
      selected.add(p);
    }
  }
  return selected;
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName());
  if (size() == 0) {
    sb.append(" ()");
  } else {
    sb.append(" (\n");
    for (Parameter member: values()) {
      sb.append(member).append("\n");
    }
    sb.append(") ===== end ").append(getClass().getName());
  }
  return sb.toString();
}

}

