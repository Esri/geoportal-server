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
package com.esri.gpt.framework.security.principal;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Defines a Group collection.
 */
public class Groups extends SecurityPrincipals<Group> 
          implements Comparator<Group> {

// class variables =============================================================

// instance variables ==========================================================
 
// constructors ================================================================

/** Default constructor. */
public Groups() {
  super(false);
}

// properties ==================================================================

// methods =====================================================================
/**
 * Compares 2 groups for sorting purposes.
 * <br/>The comparison is based upon the display name.
 * @param o1 first group to compare
 * @param o2 second group to compare
 */
public int compare(Group o1, Group o2) {
  if ((o1 != null) && (o2 != null)) {
    return o1.getName().toUpperCase().compareTo(
           o2.getName().toUpperCase());
  }
  return 0;
}
/**
 * Sorts a collection of groups by display name.
 */
public void sort() {
  if (size() > 1) {
    ArrayList<Group> al = new ArrayList<Group>();
    for (Group group: values()) al.add(group);
    java.util.Collections.sort(al,this);
    clear();
    for (Group group: al) add(group);
  }
}

}
