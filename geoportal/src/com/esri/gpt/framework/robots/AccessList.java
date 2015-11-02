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

import com.esri.gpt.framework.util.StringBuilderWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Access list.
 */
/*package*/class AccessList {
  private final List<AccessImpl> accessList = new ArrayList<AccessImpl>();

  /**
   * Adds access to the list.
   * @param access access
   */
  public void addAccess(AccessImpl access) {
    accessList.add(access);
  }
  
  /**
   * Imports entire access list from another instance.
   * @param ref another instance
   */
  public void importAccess(AccessList ref) {
    accessList.addAll(ref.accessList);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    PrintWriter writer = new PrintWriter(new StringBuilderWriter(sb));
    
    for (AccessImpl access: accessList) {
      writer.println(access.toString());
    }
    
    // no need to close writer or catch any exception
    
    return sb.toString();
  }
  
  /**
   * Select any access matching input path.
   * @param relativePath path to test
   * @param matchingStrategy matcher
   * @return list of matching elements
   */
  public List<Access> select(String relativePath, MatchingStrategy matchingStrategy) {
    ArrayList<Access> allMatching = new ArrayList<Access>();
    
    if (relativePath!=null) {
      for (AccessImpl acc: accessList) {
        if (acc.matches(relativePath, matchingStrategy)) {
          allMatching.add(acc);
        }
      }
    }
    
    return allMatching;
  }
}
