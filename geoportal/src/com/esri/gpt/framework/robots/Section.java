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
import java.util.Collections;
import java.util.List;

/**
 * Robots section.
 */
/*package*/class Section {
  private final List<String> userAgents = new ArrayList<String>();
  private final AccessList accessList = new AccessList();
  private boolean anyAgent;

  /**
   * Checks if is any agent.
   * @return <code>true</code> if any agent
   */
  public boolean isAnyAgent() {
    return anyAgent;
  }
  
  /**
   * Checks if section is exact in terms of user agents.
   * @param section section to compare
   * @return {@code true} if sections are exact.
   */
  public boolean isExact(Section section) {
    if (isAnyAgent() && section.isAnyAgent()) return true;
    if ((isAnyAgent() && !section.isAnyAgent() || (!isAnyAgent() && section.isAnyAgent()))) return false;

    for (String sectionUserAgent: section.userAgents) {
      boolean hasAgent = false;
      for (String thisUserAgent: userAgents) {
        if (thisUserAgent.equalsIgnoreCase(sectionUserAgent)) {
          hasAgent = true;
          break;
        }
      }
      if (!hasAgent) {
        return false;
      }
    }
    
    return true;
  }
  
  /**
   * Adds user agent.
   * @param userAgent host name
   */
  public void addUserAgent(String userAgent) {
    if (userAgent.equals("*")) {
      anyAgent = true;
    } else {
      this.userAgents.add(userAgent);
    }
  }

  /**
   * Gets access list.
   * @return access list
   */
  public AccessList getAccessList() {
    return accessList;
  }
  
  /**
   * Adds access.
   * @param access access
   */
  public void addAccess(AccessImpl access) {
    this.accessList.addAccess(access);
  }
  
  /**
   * Select any access matching input path.
   * @param userAgen user agent
   * @param relativePath path to test
   * @param matchingStrategy matcher
   * @return list of matching elements
   */
  public List<Access> select(String userAgent, String relativaPath, MatchingStrategy matchingStrategy) {
    if (userAgent==null || relativaPath==null || !matchUserAgent(userAgent)) {
      return Collections.EMPTY_LIST;
    }
    return accessList.select(relativaPath, matchingStrategy);
  }
  
  /**
   * Checks if the section is applicable for a given user agent.
   * @param userAgent requested user agent
   * @return <code>true</code> if the section is applicable for the requested user agent
   */
  public boolean matchUserAgent(String userAgent) {
    if (anyAgent) return true;
    for (String agent: userAgents) {
      if (agent.equalsIgnoreCase(userAgent)) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    PrintWriter writer = new PrintWriter(new StringBuilderWriter(sb));
    
    if (anyAgent) {
      writer.printf("User-agent: %s", "*");
      writer.println();
    }

    for (String userAgent: userAgents) {
      writer.printf("User-agent: %s", userAgent);
      writer.println();
    }

    writer.println(accessList.toString());
    
    // no need to close writer or catch any exception
    
    return sb.toString();
  }
}
