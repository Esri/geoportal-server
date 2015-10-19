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
  private Integer crawlDelay;
  private volatile boolean status;

  public CrawlLock(Integer crawlDelay) {
    this.crawlDelay = crawlDelay;
  }

  public void enter(Integer delay) throws InterruptedException {
    if (status) {
      synchronized (CrawlLock.this) {
        LOG.fine("Waiting for notification...");
        wait();
        LOG.fine("Notified");
      }
    }
    if (delay != null) {
      LOG.fine(String.format("Locking server for %d seconds", delay));
      crawlDelay = delay;
      synchronized (CrawlLock.this) {
        Thread thread = new Thread() {
          @Override
          public void run() {
            status = true;
            try {
              LOG.fine(String.format("Extering sleep for %d seconds...", crawlDelay));
              Thread.sleep(crawlDelay * 1000);
              LOG.fine(String.format("Sleep ended after %d seconds...", crawlDelay));
            } catch (InterruptedException ex) {
              LOG.log(Level.SEVERE, "Interrupted.", ex);
            } finally {
              synchronized (CrawlLock.this) {
                status = false;
                LOG.fine("Notifying potential requests...");
                CrawlLock.this.notifyAll();
              }
            }
          }
        };
        thread.setDaemon(true);
        thread.start();
        wait();
        LOG.fine(String.format("Server unlocked after %d seconds", delay));
      }
    }
  }
  
}
