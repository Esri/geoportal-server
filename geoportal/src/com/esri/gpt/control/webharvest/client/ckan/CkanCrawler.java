/*
 * Copyright 2015 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.control.webharvest.client.ckan;

import com.esri.gpt.framework.ckan.CkanPackage;
import com.esri.gpt.framework.robots.BotsMode;
import java.net.URL;

/**
 * CKAN crawler.
 */
public class CkanCrawler {
  private final CkanConfig config;

  public CkanCrawler(CkanConfig config) {
    this.config = config;
  }
  
  /**
   * Crawl content of the CKAN
   * @param baseUrl base CKAN URL
   * @param query query or {@code null}
   * @param callback callback
   */
  public void crawl(URL baseUrl, String query, Callback callback) {
    for (CkanPackage pkg: new CkanIterator(config, baseUrl, query)) {
      if (!callback.onPackage(pkg)) {
        break;
      }
    }
  }
  
  
  /**
   * Crawl content of the CKAN
   * @param baseUrl base CKAN URL
   * @param callback callback
   */
  public void crawl(URL baseUrl, Callback callback) {
    for (CkanPackage pkg: new CkanIterator(config, baseUrl)) {
      if (!callback.onPackage(pkg)) {
        break;
      }
    }
  }
  
  public static interface Callback {
    /**
     * Called upon new package
     * @param pkg package
     * @return {@code true} if crawling should continue;
     */
    boolean onPackage(CkanPackage pkg);
  }
}
