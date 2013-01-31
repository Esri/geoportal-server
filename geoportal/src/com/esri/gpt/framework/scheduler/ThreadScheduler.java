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
package com.esri.gpt.framework.scheduler;

import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

/**
 * Thread scheduler.
 */
public class ThreadScheduler {

// class variables =============================================================

// instance variables ==========================================================
/** executor service */  
private ScheduledExecutorService _service = null;
// constructors ================================================================

// properties ==================================================================

// methods =====================================================================
/**
 * Schedules thread definitions.
 * @param schedulerCfg thread definitions to schedule
 */
public void schedule(ThreadSchedulerConfiguration schedulerCfg) {
  Logger logger = LogUtil.getLogger();

  logger.info("Scheduling background threads...");
  
  int total = schedulerCfg!=null? schedulerCfg.getThreadsDefinitions().size(): 0;
  int scheduled = 0;

  if (schedulerCfg==null) {
    logger.severe("Scheduling background threads without thread definition.");
  } else if (!schedulerCfg.getActive()) {
    logger.info("Scheduling background threads is not enabled ('active' attribute is set to false).");
  } else {
    _service = schedulerCfg.getService();
    for (ThreadDefinition td : schedulerCfg.getThreadsDefinitions()) {
      ScheduledFuture scheduledFuture = td.schedule(_service);
      if (scheduledFuture != null) {
        logger.info("Scheduled: " + Val.chkStr(td.getClassName()));
        scheduled++;
      } else {
        logger.info("Not scheduled: " + Val.chkStr(td.getClassName()));
      }
    }

    logger.info("Scheduling background threads completed. " +
      "Scheduled "+scheduled+" out of "+total+" defined threads.");
  }
}

/**
 * Shuts down scheduler.
 * It sends {@link java.lang.Thread#interrupt} to each running thread.
 */
public void shutdown() {
  if (_service!=null) {
    Logger logger = LogUtil.getLogger();

    logger.info("Shutting down background threads...");
    
    try {
      _service.shutdownNow();
    } catch (SecurityException ex) {
      // this is just info, not severe
      LogUtil.getLogger().info(
        "Error shutting down background threads: "+ex.getMessage());
    } finally {
      _service = null;
      logger.info("Background threads shutted down.");
    }
  }
}
}
