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
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.framework.util.TimePeriod;
import com.esri.gpt.framework.util.UuidUtil;
import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Harvesting site suspender.
 */
public class Suspender implements Serializable {
  private static final Logger LOG = Logger.getLogger(Suspender.class.getCanonicalName());
  private final TreeMap<String,Expiration> registry = new TreeMap<String, Expiration>(String.CASE_INSENSITIVE_ORDER);
  
  public synchronized boolean suspend(String uuid, Expiration expiration) {
    boolean suspended = false;
    uuid = UuidUtil.addCurlies(uuid);
    if (expiration!=null && !expiration.expired()) {
      registry.put(uuid, expiration);
      suspended = true;
    }
    LOG.log(Level.FINE,String.format("Suspending repo: %s (suspended: %b)", uuid, suspended));
    return suspended;
  }
  
  public synchronized boolean isSuspended(String uuid) {
    boolean suspended = false;
    uuid = UuidUtil.addCurlies(uuid);
    Expiration expiration = registry.get(uuid);
    if (expiration!=null) {
      if (expiration.expired()) {
        expire(uuid);
      } else {
        suspended = true;
      }
    }
    LOG.log(Level.FINE,String.format("Checking repo: %s (suspended: %b)", uuid, suspended));
    return suspended;
  }
  
  public synchronized void expire(String uuid) {
    LOG.log(Level.FINE,String.format("Expiring repo: %s", uuid));
    registry.remove(uuid);
  }
  
  public static final Expiration DAY = new PeriodicExpiration(1000*60*60*24);
  
  public static class PeriodicExpiration implements Expiration {
    private final long end;
    private final TimePeriod duration;
    
    public PeriodicExpiration(TimePeriod duration) {
      this.duration = duration;
      end = (new Date()).getTime() + duration.getValue();
    }
    
    public PeriodicExpiration(long duration) {
      this.duration = new TimePeriod(duration);
      end = (new Date()).getTime() + duration;
    }

    @Override
    public boolean expired() {
      return (new Date()).getTime() >= end;
    }
    
    @Override
    public String toString() {
      return duration.toString();
    }
  }
  
  /**
   * Expiration predicated
   */
  public static interface Expiration {
    boolean expired();
  }
}
