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
import java.util.LinkedHashMap;

/**
 * Defines a collection of namespaces associated with a schema.
 */
public class Namespaces extends LinkedHashMap<String,Namespace> {

// class variables =============================================================
    
// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public Namespaces() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Namespaces(Namespaces objectToDuplicate) {
  if (objectToDuplicate != null) {
    for (Namespace member: objectToDuplicate.values()) {
      add(member.duplicate());
    }
  }
}

// properties ==================================================================

// methods =====================================================================

/**
 * Adds a member to the collection.
 * <br/>The member will not be added if it is null.
 * <br/>The collection is keyed on the namespace prefix
 * @param member the member to add
 */
public void add(Namespace member) {
  if (member != null) {
    put(member.getPrefix(),member);
  }
}

/**
 * Adds a member to the collection.
 * <br/>If either argument is empty, the namespace will not be added.
 * @param prefix the namespace prefix
 * @param uri the namespace URI
 */
public void add(String prefix, String uri) {
  Namespace member = new Namespace();
  member.setPrefix(prefix);
  member.setUri(uri);
  if ((member.getPrefix().length() > 0) && (member.getUri().length() > 0)) {
    put(member.getPrefix(),member);
  }
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
    for (Namespace member: values()) {
      sb.append(member).append("\n");
    }
    sb.append(") ===== end ").append(getClass().getName());
  }
  return sb.toString();
}

}

