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
 * Defines a User collection.
 */
public class Users extends SecurityPrincipals<User> 
       implements Comparator<User> {

// class variables =============================================================

// instance variables ==========================================================
  
// constructors ================================================================

/** Default constructor. */
public Users() {
  super(false);
}

// properties ==================================================================

// methods =====================================================================

/**
 * Compares 2 users for sorting purposes.
 * <br/>The comparison is based upon the display name.
 * @param o1 first user to compare
 * @param o2 second user to compare
 */
public int compare(User o1, User o2) {
  if ((o1 != null) && (o2 != null)) {
    return o1.getName().toUpperCase().compareTo(
           o2.getName().toUpperCase());
  }
  return 0;
}

/**
 * Sorts a collection of users by display name.
 */
public void sort() {
  if (size() > 1) {
    ArrayList<User> al = new ArrayList<User>();
    for (User user: values()) al.add(user);
    java.util.Collections.sort(al,this);
    clear();
    for (User user: al) add(user);
  }
}
}
