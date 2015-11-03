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
package com.esri.gpt.framework.http.crawl;

/**
 * Crawl flag.
 */
public class CrawlFlag {
  private volatile boolean flag;
  
  /**
   * Hold until notified.
   * @param crawlDelay crawl delay in milliseconds
   * @throws InterruptedException if interrupted
   */
  public synchronized void hold(long crawlDelay) throws InterruptedException {
    while (!flag) {
      wait(crawlDelay);
    }
  }
  
  /**
   * Set flag and notify.
   */
  public synchronized void set() {
    flag = true;
    notifyAll();
  }
}
