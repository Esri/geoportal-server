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

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.catalog.harvest.repository.HrRecords;
import com.esri.gpt.catalog.harvest.repository.HrSelectRequest;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.TimePeriod;
import com.esri.gpt.framework.util.Val;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Schedules new harvesting task according to the repository harvest frequency.
 */
abstract class AutoSelector implements Runnable {
private static final int DEFAULT_MAX_ATTEMPTS = 1;  

/** logger */
private static final Logger LOGGER = Logger.getLogger(AutoSelector.class.getCanonicalName());
/** current thread */
private Thread workerThread;
/** shutdown flag */
private volatile boolean shutdown;
/** autoselect frequency */
private long autoSelectFrequency;
/** suspended */
private volatile boolean suspended;

/**
 * Creates instance of auto selector.
 * @param autoSelectFrequency auto select frequency (milliseconds)
 */
public AutoSelector(long autoSelectFrequency) {
  this.autoSelectFrequency = autoSelectFrequency;
}

@Override
public void run() {
  workerThread = Thread.currentThread();
  LOGGER.info("[SYNCHRONIZER] AutoSelector activated.");
  int attempt = 0;
  
  do {
    long duration = autoSelectFrequency;
    if (!suspended) {
      LOGGER.finer("[SYNCHRONIZER] AutoSelector entered run mode.");

      try {
        HrRecords records = selectRecords();

        // selecting all records with harvest date due now
        HrRecords recordsDueNow = records.findHarvestDue();
        LOGGER.log(Level.FINER, "[SYNCHRONIZER] AutoSelector selected {0} records with harvest date due now.", recordsDueNow.size());

        // process all with harvest date due now
        for (HrRecord r : recordsDueNow) {
          if (shutdown || suspended) break;
          // this is it; do something in overriden method
          onSelect(r);
        }

        // get the one record with the closes due date but not due yet
        HrRecord nextDue = records.findNextDue();
        
        // caluclate duration in milliseconds
        if (nextDue!=null) {
          Date nextHarvestDate = nextDue.getNextHarvestDate();
          duration = nextHarvestDate.getTime() - (new Date()).getTime() ;
          LOGGER.log(Level.INFO,"[SYNCHRONIZER] Next synchronization time : "+nextHarvestDate.toString()+" has been determined based on scheduling of "+nextDue.getUuid()+"/\""+nextDue.getName()+"\" harvesting site.");
        } else {
          duration = autoSelectFrequency;
          LOGGER.log(Level.INFO,"[SYNCHRONIZER] Next synchronization time couldn't been determined at this time.");
        }
        
        // clear attempt counter
        attempt = 0;
      } catch (SQLException ex) {
        LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Error selecting harvesting sites for harvest.", ex);
        attempt++;
        if (attempt<=getMaxAttempts()) {
          LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Error selecting harvesting sites for harvest.", ex);
        }
      }
    }

    if (shutdown) break;
    
    // wait for calculated duration or until interrupted
    synchronized (this) {
      try {
        if (isSuspendedWithAck()) {
          LOGGER.finer("[SYNCHRONIZER] AutoSelector suspended mode");
          wait();
        } else {
          TimePeriod period = new TimePeriod();
          period.setValue(duration);
          LOGGER.log(Level.FINER, "[SYNCHRONIZER] AutoSelector enters wait mode for {0}", period);
          wait(duration);
        }
      } catch (InterruptedException ex) {
        if (shutdown)
          break;
      }
    }
  } while (true);
}

/**
 * Shuts down selector.
 */
public synchronized void shutdown() {
  LOGGER.info("[SYNCHRONIZER] Shutting down AutoSelector.");
  this.shutdown = true;
  if (workerThread != null) {
    workerThread.interrupt();
  }
}

/**
 * Forces to reselect harvesting sites.
 */
public void reselect() {
  if (workerThread != null) {
    workerThread.interrupt();
  }
}

/**
 * Called on every repository selected for harvest.
 * @param context request context
 * @param repository harvest repository
 */
protected abstract void onSelect(HrRecord repository);

/**
 * Selects records.
 * @return records
 * @throws SQLException if accessing database failed
 */
private HrRecords selectRecords() throws SQLException {
  RequestContext context = RequestContext.extract(null);
  try {
    // get all harveting records
    HrSelectRequest selectRequest = new HrSelectRequest(context);
    selectRequest.execute();
    return selectRequest.getQueryResult().getRecords();
  } finally {
    context.onExecutionPhaseCompleted();
  }
}

public synchronized void safeSuspend() {
  if (!suspended) {
    LOGGER.info("[SYNCHRONIZER] Suspending AutoSelector");
    suspended = true;
    notify();
  } else {
    LOGGER.info("[SYNCHRONIZER] AutoSelector already suspended");
  }
}

public synchronized void safeResume() {
  if (suspended) {
    LOGGER.info("[SYNCHRONIZER] Resuming AutoSelector");
    suspended = false;
    notify();
  } else {
    LOGGER.info("[SYNCHRONIZER] AutoSelector already resumed");
  }
}

private boolean isSuspendedWithAck() {
  if (suspended) {
    LOGGER.info("[SYNCHRONIZER] AutoSelector acknowledged suspension");
  }
  return suspended;
}
  
private int getMaxAttempts() {
  ApplicationContext appCtx = ApplicationContext.getInstance();
  ApplicationConfiguration appCfg = appCtx.getConfiguration();
  StringAttributeMap parameters = appCfg.getCatalogConfiguration().getParameters();
  return Val.chkInt(parameters.getValue("webharvester.maxAttempts"),DEFAULT_MAX_ATTEMPTS);
}
}
