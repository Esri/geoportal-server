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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Crawl locker.
 */
public class CrawlLocker {
  private static final Logger LOG = Logger.getLogger(CrawlLocker.class.getName());
  private static final WeakHashMap<String,CrawlLock> locks = new  WeakHashMap<String, CrawlLock>();
  private static CrawlLocker crawlLocker;
  
  public static CrawlLocker getInstance() {
    if (crawlLocker==null) {
      crawlLocker = new CrawlLocker();
    }
    return crawlLocker;
  }
  
  public CrawlLocker() {}
  
  public void enterServer(String protocolHostPort, Integer crawlDelay) {
    try {
      LOG.info(String.format("Entering server %s for %d seconds", protocolHostPort, crawlDelay));
      CrawlLock lock = makeLock(protocolHostPort, crawlDelay);
      lock.enter(crawlDelay);
      LOG.info(String.format("Exiting server %s", protocolHostPort));
    } catch (InterruptedException ex) {
      LOG.log(Level.SEVERE, "Interrupted.", ex);
    }
  }
  
  private synchronized CrawlLock makeLock(String protocolHostPort, Integer crawlDelay) {
    CrawlLock crawlLock = locks.get(protocolHostPort);
    if (crawlLock==null) {
      crawlLock = new CrawlLock(crawlDelay);
      locks.put(protocolHostPort, crawlLock);
    }
    return crawlLock;
  }
  
}
