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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Crawl locker.
 */
public class CrawlLocker {
  private static final Logger LOG = Logger.getLogger(CrawlLocker.class.getName());
  private static final CrawlLockManager lockManager = new CrawlLockManager();
  private static CrawlLocker instance;
  
  /**
   * Gets singleton instance.
   * @return instance (never <code>null</code>)
   */
  public static CrawlLocker getInstance() {
    if (instance==null) {
      instance = new CrawlLocker();
    }
    return instance;
  }
  
  private CrawlLocker() {}
  
  /**
   * Enter server with crawl delay.
   * @param protocolHostPort server protocol/host/port
   * @param throttleDelay throttle delay in milliseconds(<code>null</code> for no delay)
   */
  public void enterServer(String protocolHostPort, Long throttleDelay) {
    try {
      LOG.fine(String.format("Entering server %s for %d milliseconds", protocolHostPort, throttleDelay));
      CrawlLock lock = lockManager.getLock(protocolHostPort);
      lock.enter(throttleDelay);
      LOG.fine(String.format("Exiting server %s", protocolHostPort));
    } catch (InterruptedException ex) {
      LOG.log(Level.SEVERE, "Interrupted.", ex);
    }
  }
  
}
