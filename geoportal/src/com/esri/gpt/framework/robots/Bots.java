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
 * Represents access policy from a single "robots.txt" file.
 * <p>
 * @see Access
 * @see BotsUtils
 */
public interface Bots {

// ===== properties ============================================================
  /**
   * Gets user agent.
   * @return user agent
   */
  String getUserAgent();
  
  /**
   * Gets crawl delay.
   * @return crawl delay in seconds or <code>0</code> if no delay
   */
  Integer getCrawlDelay();

  /**
   * Gets host.
   * @return host or <code>null</code> if no any available
   */
  String getHost();

  /**
   * Gets sitemaps.
   * @return list of sitemap urls.
   */
  List<String> getSitemaps();
  
  /**
   * Gets matching strategy.
   * <p>
   * Matching strategy defines how it is determined if a requested URL is 
   * matching a pattern.
   * @return matching strategy (never {@code null})
   */
  MatchingStrategy getMatchingStrategy();
  
  /**
   * Gets winning strategy.
   * <p>
   * Winning strategy defines how a single matching path is selected amongst
   * a list of matching paths.
   * @return winning strategy (never {@code null})
   */
  WinningStrategy getWinningStrategy();

// ===== methods ===============================================================
  
  /**
   * Selects matching accesses.
   * @param path path
   * @param matchingStrategy matcher
   * @return list of matching accesses (newer <code>null</code>)
   */
  List<Access> select(String path, MatchingStrategy matchingStrategy);
}
