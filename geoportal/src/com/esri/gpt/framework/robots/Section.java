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

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Robots section.
 */
class Section {
  private final List<String> userAgents = new ArrayList<String>();
  private final AccessList accessList = new AccessList();
  private boolean anyAgent;

  public boolean isAnyAgent() {
    return anyAgent;
  }

  public void setAnyAgent(boolean anyAgent) {
    this.anyAgent = anyAgent;
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
   * Adds access.
   * @param access access
   */
  public void addAccess(Access access) {
    this.accessList.addAccess(access);
  }
  
  /**
   * Checks if absolute path has access for this section.
   * @param relativaPath absolute path
   * @return <code>true</code> if has access
   */
  public Access findAccess(String userAgant, String relativaPath) {
    if (!matchUserAgant(userAgant)) {
      return null;
    }
    return accessList.findAccess(relativaPath);
  }
  
  private boolean matchUserAgant(String userAgent) {
    return anyAgent || userAgents.contains(userAgent);
  }
  
  @Override
  public String toString() {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out, "UTF-8")));
      
      if (anyAgent) {
        writer.printf("User-agent: %s", "*");
        writer.println();
      }
      
      for (String userAgent: userAgents) {
        writer.printf("User-agent: %s", userAgent);
        writer.println();
      }
      
      writer.println(accessList.toString());
      
      writer.close();
      
      return out.toString("UTF-8");
    } catch (IOException ex) {
      return "";
    }
  }
}
