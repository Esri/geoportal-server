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

import java.util.WeakHashMap;
import java.util.logging.Logger;

/**
 * Crawl lock manager.
 */
class CrawlLockManager {
  private static final Logger LOG = Logger.getLogger(CrawlLockManager.class.getName());
  private final WeakHashMap<String,CrawlLock> locks = new  WeakHashMap<String, CrawlLock>();
  
  /**
   * Gets lock.
   * @param protocolHostPort protocol/host/port
   * @return crawl lock
   */
  public synchronized CrawlLock getLock(String protocolHostPort) {
    CrawlLock lock = locks.get(protocolHostPort);
    if (lock==null) {
      lock = new CrawlLock();
      locks.put(protocolHostPort, lock);
    }
    LOG.finer(String.format("Getting crawl lock: %s", lock));
    return lock;
  }
}
