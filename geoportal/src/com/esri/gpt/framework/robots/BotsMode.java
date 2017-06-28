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
 * RobotsTxt mode.
 * <p>
 * Controls how robots.txt should be respected during harvesting.
 * <p>
 * {@link BotsMode#inherit} uses "bot.robotstxt.enabled" parameter from gpt.xml configuration file.
 */
public enum BotsMode {
  /** inherit from gpt.xml => bot.robotstxt.enabled */
  inherit,
  /** force to use robots.txt if available robots.txt available, regardless default configuration */
  always,
  /** disable to use robots.txt if available robots.txt available, regardless default configuration */
  never;
  
  /**
   * Gets default value.
   * @return {@link RobotsTxtMode#inherit}
   */
  public static BotsMode getDefault() {
    return inherit;
  }
  
  /**
   * Parse string into mode. Default: {@link RobotsTxtMode#inherit}.
   * @param mode mode as text
   * @return mode
   */
  public static BotsMode parseMode(String mode) {
    for (BotsMode value: BotsMode.values()) {
      if (value.name().equalsIgnoreCase(mode)) {
        return value;
      }
    }
    return getDefault();
  }
}
