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
package com.esri.gpt.framework.robots;

import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.List;

/**
 * Path.
 */
class Path {
  private final List<String> elements;

  /**
   * Creates instance of the path.
   * @param relativePath path relative to the host
   */
  public Path(String relativePath) {
    this.elements = splitNames(relativePath);
  }
  
  /**
   * Gets length (number of names) of the path.
   * @return length (number of names) of the path
   */
  public int getLength() {
    return elements.size();
  }
  
  /**
   * Checks if given path matches.
   * @param relativePath path to check
   * @return <code>true</code> if path matches
   */
  public boolean match(String relativePath) {
    List<String> current = splitNames(relativePath);

    if (elements.size()>current.size()) return false;
    
    for (int i=0; i<elements.size(); i++) {
      if (!elements.get(i).equalsIgnoreCase(current.get(i))) {
        return false;
      }
    }
    
    return true;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for (String el: elements) {
      sb.append(el);
    }
    return sb.toString();
  }
  
  private List<String> splitNames(String path) {
    ArrayList<String> names = new ArrayList<String>();
    for (String element: Val.chkStr(path).replaceAll("[*]+$", "").split("/")) {
      element = Val.chkStr(element);
      if (element.isEmpty()) continue;
      names.add("/"+element);
    }
    return names;
  }
}
