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

/**
 * Defines a list of content values.
 */
public class ContentValues extends ArrayList<ContentValue> {

// class variables =============================================================
    
// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public ContentValues() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public ContentValues(ContentValues objectToDuplicate) {
  if (objectToDuplicate != null) {
    for (ContentValue member: objectToDuplicate) {
      add(member.duplicate());
    }
  }
}

// properties ==================================================================

// methods =====================================================================

/**
 * Adds a member to the collection.
 * <br/>The member will not be added if it is null.
 * @param member the member to add
 * @return <code>true</code> if the collection was changed
 */
@Override
public boolean add(ContentValue member) {
  if (member != null) {
    return super.add(member);
  } else {
    return false;
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
    for (ContentValue member: this) {
      sb.append(member).append("\n");
    }
    sb.append(") ===== end ").append(getClass().getName());
  }
  return sb.toString();
}

}

