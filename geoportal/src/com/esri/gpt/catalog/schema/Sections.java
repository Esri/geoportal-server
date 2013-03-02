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
 * Defines a collection of metadata sections.
 */
public class Sections extends LinkedHashMap<String,Section> {

// class variables =============================================================
    
// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public Sections() {
  this(null,null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 * @param parent the parent section
 */
public Sections(Sections objectToDuplicate, Section parent) {
  if (objectToDuplicate != null) {
    for (Section member: objectToDuplicate.values()) {
      Section section = member.duplicate();
      section.setParent(parent);
      add(section);
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
public void add(Section member) {
  if ((member != null) && (member.getKey().length() > 0)) {
    put(member.getKey(),member);
  }
}

/**
 * Checks to ensure that no more than one mutually exclusive section is open.
 */
public void checkExclusiveOpenStatus() {
  boolean bFoundOpen = false;
  for (Section member: values()) {
    if (member.getObligation().equalsIgnoreCase(Section.OBLIGATION_EXCLUSIVE)) {
      if (member.getOpen()) {
        if (bFoundOpen) {
          member.setOpen(false);
        } else {
          bFoundOpen = true;
        }
      }
    }
  }
}

/**
 * Selects all parameters conforming to the condiditions defined by predicate.
 * @param predicate predicate
 * @return list of selected parameters
 */
public List<Parameter> selectParameters(Predicate predicate) {
  ArrayList<Parameter> selected = new ArrayList<Parameter>();
  for (Section s : this.values()) {
    selected.addAll(s.selectParameters(predicate));
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
    for (Section member: values()) {
      sb.append(member).append("\n");
    }
    sb.append(") ===== end ").append(getClass().getName());
  }
  return sb.toString();
}

}

