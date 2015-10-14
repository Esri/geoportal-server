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

import java.util.ArrayList;
import java.util.List;

/**
 * Access list.
 */
class AccessList {
  private final List<Access> accessList = new ArrayList<Access>();
  
  /**
   * Adds access to the list.
   * @param access access
   */
  public void addAccess(Access access) {
    accessList.add(access);
  }
  
  @Override
  public String toString() {
    return accessList.toString();
  }
  
  /**
   * Checks if relative path has access.
   * @param relativePath relative path
   * @return <code>true</code> if path has access
   */
  public boolean hasAccess(String relativePath) {
    List<Access> allMatching = selectMatching(relativePath);
    int maxLength = findMaxLength(allMatching);
    Access firstMatching = findFirstByLength(allMatching, maxLength);
    
    return firstMatching!=null? firstMatching.isAllowed(): true;
  }
  
  private Access findFirstByLength(List<Access> allMatching, int length) {
    for (Access acc: allMatching) {
      if (acc.getLenth()==length) {
        return acc;
      }
    }
    return null;
  }
  
  private int findMaxLength(List<Access> allMatching) {
    int length = 0;
    for (Access acc: allMatching) {
      if (acc.getLenth()>length) {
        length = acc.getLenth();
      }
    }
    return length;
  }
  
  private List<Access> selectMatching(String relativePath) {
    List<Access> allMatching = new ArrayList<Access>();
    for (Access acc: accessList) {
      if (acc.matches(relativePath)) {
        allMatching.add(acc);
      }
    }
    return allMatching;
  }
}
