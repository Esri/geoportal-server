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

  private Long throttleDelay;
  private volatile boolean status;

  /**
   * Enters lock with crawl delay.
   * @param delay crawl delay (<code>null</code> for no delay)
   * @throws InterruptedException 
   */
  public synchronized void enter(Long delay) throws InterruptedException {
    LOG.finer(String.format("Entering crawl lock with delay: %d", delay));
    CrawlFlag flag = flagManager.newFlag();

    if (status) {
      if (throttleDelay!=null) {
        LOG.finer(String.format("Holding on flag"));
        flag.hold(throttleDelay);
      }
    }

    throttleDelay = delay;
    if (throttleDelay != null) {
        status = true;
        Semaphore semaphore = new Semaphore();
        semaphore.start();
    } else {
      flagManager.notifyLast();
    }
    
    LOG.finer(String.format("Exiting crawl lock"));
  }
  
  @Override
  public String toString() {
    return String.format("CrawlLock :: throttle delay: %d, status: %b", throttleDelay, status);
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
      LOG.finer(String.format("Starting lock semaphore with crawl delay: %d", throttleDelay));
      try {
        if (throttleDelay!=null) {
          Thread.sleep(throttleDelay);
        }
      } catch (InterruptedException ex) {
        LOG.log(Level.SEVERE, null, ex);
      } finally {
        status = false;
        LOG.finer(String.format("Lock semaphore ended"));
        flagManager.notifyLast();
      }
    }
  }
}
