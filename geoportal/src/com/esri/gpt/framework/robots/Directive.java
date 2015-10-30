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

import com.esri.gpt.framework.util.Val;

/**
 * Robots.txt file directive.
 */
public enum Directive {
  /** User-Agent directive */
  UserAgent("User-Agent"),
  /** Disallow directive */
  Disallow("Disallow"),
  /** Allow directive */
  Allow("Allow"),
  /** Crawl-Delay directive */
  CrawlDelay("Crawl-Delay"),
  /** Host directive */
  Host("Host"),
  /** Sitemap directive */
  Sitemap("Sitemap")
  ;
  private final String symbol;
  
  Directive(String symbol) {
    this.symbol = symbol;
  }
  
  /**
   * Gets symbol.
   * <p>
   * Symbol is a string which could be recognized within robots.txt file
   * @return symbol
   */
  public String symbol() {
    return this.symbol;
  }

  @Override
  public String toString() {
    return symbol;
  }
  
  public static Directive parseDirective(String nameOrSymbol) {
    nameOrSymbol = Val.chkStr(nameOrSymbol);
    for (Directive d: values()) {
      if (d.name().equalsIgnoreCase(nameOrSymbol) || d.symbol().equalsIgnoreCase(nameOrSymbol)) {
        return d;
      }
    }
    return null;
  }
}
