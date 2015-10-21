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
 * Crawl lock.
 */
class CrawlLock {

  private static final Logger LOG = Logger.getLogger(CrawlLock.class.getName());
  private final CrawlFlagManager flagManager = new CrawlFlagManager();

  private Integer crawlDelay;
  private volatile boolean status;

  /**
   * Enters lock with crawl delay.
   * @param delay crawl delay (<code>null</code> for no delay)
   * @throws InterruptedException 
   */
  public synchronized void enter(Integer delay) throws InterruptedException {
    CrawlFlag flag = flagManager.newFlag();

    if (status) {
      flag.hold();
    }

    crawlDelay = delay;
    if (crawlDelay != null) {
        Semaphore semaphore = new Semaphore();
        semaphore.start();
        
        while (!status) {
          synchronized (semaphore) {
            semaphore.wait();
          }
        }
    } else {
      flagManager.notifyLast();
    }
  }

  /**
   * Semaphore for crawl lock.
   */
  private class Semaphore extends Thread {

    public Semaphore() {
      setDaemon(true);
    }

    @Override
    public void run() {
      status = true;
      synchronized (this) {
        notify();
      }
      try {
        Thread.sleep(crawlDelay * 1000);
      } catch (InterruptedException ex) {
        LOG.log(Level.SEVERE, null, ex);
      } finally {
        flagManager.notifyLast();
      }
    }
  }
}
