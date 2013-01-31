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

import com.esri.gpt.framework.jsf.MessageBroker;

/**
 * Defines a code collection.
 */
public class Codes extends LinkedHashMap<String,Code> {

// class variables =============================================================
    
// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public Codes() {}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Codes(Codes objectToDuplicate) {
  if (objectToDuplicate != null) {
    for (Code member: objectToDuplicate.values()) {
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
 */
public void add(Code member) {
  if (member != null) {
    put(member.getKey(),member);
  }
}

/**
 * Looks up the display value for a code.
 * @param messageBroker the message broker
 * @param key the code key
 * @return the display value for the code
 */
public String lookupDisplayValue(MessageBroker messageBroker, String key) {
  if ((size() > 0) && (messageBroker != null)) {
    Code code = get(key);
    if (code != null) {
      String sResKey = code.getResourceKey();
      if (sResKey.length() > 0) {
        String sLabel = messageBroker.retrieveMessage(sResKey);
        if ((sLabel.length() > 0) && !sLabel.startsWith("??")) {
          return sLabel;
        }
      }      
    }
  }
  return key;
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
    for (Code member: values()) {
      sb.append(member).append("\n");
    }
    sb.append(") ===== end ").append(getClass().getName());
  }
  return sb.toString();
}

}

