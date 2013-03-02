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
package com.esri.gpt.framework.resource.common;

import com.esri.gpt.framework.resource.api.SourceUri;

/**
 * String-based source URI.
 */
public class StringUri implements SourceUri {

/** string */
private String str;

/**
 * Creates instance of the source URI.
 * @param str base string
 */
public StringUri(String str) {
  this.str = str;
}

@Override
public boolean equals(Object sourceUri) {
  if (sourceUri instanceof StringUri) {
    return str!=null && str.equalsIgnoreCase(((StringUri)sourceUri).str);
  }
  return false;
}

@Override
public int hashCode() {
  return str!=null? str.hashCode(): 0;
}

@Override
public String asString() {
  return str!=null? str: "";
}

@Override
public String toString() {
  return str!=null? str: "";
}
}
