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
 * Matching strategy.
 * <p>
 * It determines how and if path is matching a pattern.
 * @see WinningStrategy
 */
public interface MatchingStrategy {
  /**
   * This strategy only checks if path starts with pattern (case-sensitive).
   */
  MatchingStrategy PLAIN_STRING_STRATEGY = new PlainStringMatchingStrategy();
  /**
   * This strategy recognizes (*) and ($) as wildcards.
   */
  MatchingStrategy SIMPLE_PATTERN_STRATEGY = new SimplePatternMatchingStrategy();
  /**
   * Matches given path with a pattern.
   * @param pattern pattern
   * @param pathToTest path to test
   * @return <code>true</code> if match
   */
  boolean matches(String pattern, String pathToTest);
}
