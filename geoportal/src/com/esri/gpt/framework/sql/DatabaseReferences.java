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
package com.esri.gpt.framework.sql;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.util.Val;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * DatabaseReference collection.
 */
public final class DatabaseReferences implements Serializable {

// class variables =============================================================

/** Default tag. */
private static final String TAG_DEFAULT = "default";

// instance variables ==========================================================
private HashMap<String,DatabaseReference> _hmReferencesByName;
private HashMap<String,DatabaseReference> _hmReferencesByTagName;

// constructors ================================================================

/** Default constructor. */
public DatabaseReferences() {
  _hmReferencesByName = new HashMap<String,DatabaseReference>();
  _hmReferencesByTagName = new HashMap<String,DatabaseReference>();
}

// properties ==================================================================

// methods =====================================================================


/**
 * Adds a DatabaseReference to the collection.
 * @param reference the DatabaseReference to add
 * @param testConnection  if true the connection will be tested
 */
public void add(DatabaseReference reference,
                boolean testConnection) {

  // check the name
  reference.setIsJndiBased((reference.getJndiName().length() > 0));
  String sName = reference.getReferenceName();
  if ((sName.length() == 0) && reference.getIsJndiBased()) {
    reference.setReferenceName(reference.getJndiName());
    sName = reference.getReferenceName();
  }

  // add the reference
  if ((sName.length() > 0) && (findByTag(sName) == null)) {
    StringSet tags = reference.getTags();
    tags.add(sName);
    if (reference.getIsJndiBased()) {
      tags.add(reference.getJndiName());
    }

    // remove tags that have already been referenced
    Iterator<String> it = _hmReferencesByTagName.keySet().iterator();
    while (it.hasNext()) {
      tags.remove(it.next());
    }

    // store new references and tags
    int nTags = tags.size();
    if (nTags > 0) {
      _hmReferencesByName.put(sName.toLowerCase(),reference);
      for (String sTag: tags) {
        _hmReferencesByTagName.put(sTag.toLowerCase(),reference);
      }

      // this will try open and close a connection
      if (testConnection) {
        reference.testConnection();
      }
    }
  }
}

/**
 * Finds the DatabaseReference associated with a name.
 * @param referenceName the name associated with the reference to find
 * @return the associated reference (null if none)
 */
private DatabaseReference findByReferenceName(String referenceName) {
  return _hmReferencesByName.get(Val.chkStr(referenceName).toLowerCase());
}

/**
 * Finds the DatabaseReference associated with a tag.
 * @param tag the tag associated with the reference to find
 * @return the associated reference (null if none)
 */
protected DatabaseReference findByTag(String tag) {
  tag = Val.chkStr(tag).toLowerCase();
  if (tag.length() == 0) {
    tag = "default";
  }
  return _hmReferencesByTagName.get(tag);
}

/**
 * Returns a string representation of this object.
 * @return the string
 */
@Override
public String toString() {
  Iterator<DatabaseReference> it = _hmReferencesByName.values().iterator();
  StringBuffer sb = new StringBuffer();
  sb.append(getClass().getName()).append(" (\n");
  while (it.hasNext()) {
    DatabaseReference dbRef = it.next();
    sb.append(dbRef.toString()).append("\n");
  }
  sb.append(") ===== end ").append(getClass().getName());
  return sb.toString();
}

}
