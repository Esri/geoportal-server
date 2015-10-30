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

import static com.esri.gpt.framework.robots.BotsUtils.decode;

/**
 * Plain string matching strategy.
 * <p>
 * This strategy only checks if path starts with pattern (case-sensitive).
 */
/*package*/ class PlainStringMatchingStrategy implements MatchingStrategy {

  @Override
  public boolean matches(String pattern, String pathToTest) {
    try {
      String relativePath = decode(pathToTest);
      if (pattern.endsWith("/") && !relativePath.endsWith("/")) {
        relativePath += "/";
      }
      return relativePath.startsWith(pattern);
    } catch (Exception ex) {
      return false;
    }
  }
  
}
