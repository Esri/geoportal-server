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
  private final Path path;
  private final boolean allowed;

  /**
   * Creates instance of the access.
   * @param path path
   * @param allowed access to the path
   */
  public Access(Path path, boolean allowed) {
    this.path = path;
    this.allowed = allowed;
  }

  public boolean isAllowed() {
    return allowed;
  }
  
  public int getLenth() {
    return path.getLength();
  }
  
  public boolean matches(String path) {
    return this.path.match(path);
  }
  
  @Override
  public String toString() {
    return String.format("%s: %s", !allowed? "Disallowed": "Allowed", path);
  }
  
}
