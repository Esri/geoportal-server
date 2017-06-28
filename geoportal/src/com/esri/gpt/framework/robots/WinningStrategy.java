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

import java.util.List;

/**
 * Winning strategy.
 * <p>
 * Determines how a winner will be selected.
 * @see MatchingStrategy
 */
public interface WinningStrategy {
  /**
   * This strategy always chooses first winner from the list.
   */
  WinningStrategy FIRST_MATCH_STRATEGY = new FirstMatchWinningStrategy();
  /**
   * This strategy chooses a winner with the longest path.
   */
  WinningStrategy LONGEST_PATH_STRATEGY = new LongestPathWinningStrategy();
  
  /**
   * Selects a single winner amongst the candidates.
   * @param candidates list of candidates
   * @return a winner or {@code null} if no winner (for example: because empty list of candidates)
   */
  Access selectWinner(List<Access> candidates);
}
