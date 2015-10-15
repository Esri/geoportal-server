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
 * Robots TXT.
 */
public interface RobotsTxt {

  /**
   * Gets crawl delay.
   * @return crawl delay in minutes or <code>0</code> if no delay
   */
  int getCrawlDelay();

  /**
   * Gets host.
   * @return host or <code>null</code> if no any available
   */
  String getHost();

  /**
   * Gets sitemap.
   * @return sitemap or <code>null</code> if no any available
   */
  String getSitemap();

  /**
   * Checks if absolute path has access for this section.
   *
   * @param relativePath absolute path
   * @return <code>true</code> if has access
   */
  boolean hasAccess(String relativePath);
  
}
