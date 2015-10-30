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
 * Path.
 */
/*package*/class AccessPath {
  private final String path;

  /**
   * Creates instance of the path.
   * @param relativePath path relative to the host
   */
  public AccessPath(String relativePath) {
    this.path = relativePath;
  }

  /**
   * Gets path.
   * @return path
   */
  public String getPath() {
    return path;
  }
  
  /**
   * Checks if given path matches.
   * @param relativePath path to check
   * @return <code>true</code> if path matches
   */
  public boolean match(String relativePath, MatchingStrategy matchingStrategy) {
    return matchingStrategy.matches(getPath(), relativePath);
  }
  
  @Override
  public String toString() {
    return path;
  }
}
