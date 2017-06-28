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
 * Longest path winning strategy.
 */
/*package*/class LongestPathWinningStrategy implements WinningStrategy {

  @Override
  public Access selectWinner(List<Access> candidates) {
    Access winningDisallow = findLongestPath(candidates, false);
    Access winningAllow = findLongestPath(candidates, true);

    if (winningAllow!=null && winningAllow.getPath().length()>=(winningDisallow!=null? winningDisallow.getPath().length(): 0)) {
      return winningAllow;
    }

    if (winningDisallow!=null) {
      return winningDisallow;
    }
    
    return null;
  }

  private Access findLongestPath(List<Access> list, boolean hasAccess) {
    Access longest = null;
    for (Access acc: list) {
      if (acc.hasAccess()!=hasAccess) continue;
      if (longest==null || acc.getPath().length()>=longest.getPath().length()) {
        longest = acc;
      }
    }
    return longest;
  }
  
}
