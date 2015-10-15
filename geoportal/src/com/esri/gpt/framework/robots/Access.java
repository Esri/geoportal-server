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

/**
 * Access
 */
class Access {
  private final AccessPath accessPath;
  private final boolean accessAllowed;

  /**
   * Creates instance of the access.
   * @param accessPath access path
   * @param accessAllowed access to the path
   */
  public Access(AccessPath accessPath, boolean accessAllowed) {
    this.accessPath = accessPath;
    this.accessAllowed = accessAllowed;
  }

  /**
   * Check if this section gives an access
   * @return 
   */
  public boolean hasAccess() {
    return accessAllowed;
  }
  
  /**
   * Gets length of the path.
   * @return length of the path
   */
  public int getLenth() {
    return accessPath.getLength();
  }
  
  /**
   * Checks if path matches access path
   * @param path path to check
   * @return <code>true</code> if path matches access path
   */
  public boolean matches(String path) {
    return this.accessPath.match(path);
  }
  
  @Override
  public String toString() {
    return String.format("%s: %s", !accessAllowed? "Disallowed": "Allowed", accessPath);
  }
  
}
