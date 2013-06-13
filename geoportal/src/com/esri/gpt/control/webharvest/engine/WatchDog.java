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

import com.esri.gpt.catalog.harvest.jobs.HjLoadAllRequest;
import com.esri.gpt.catalog.harvest.jobs.HjRecord;
import com.esri.gpt.catalog.harvest.jobs.HjRecord.JobStatus;
import com.esri.gpt.catalog.harvest.jobs.HjRecords;
import com.esri.gpt.catalog.harvest.jobs.HjWithdrawRequest;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.TimePeriod;
import com.esri.gpt.framework.util.Val;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Watch-dog.
 * Checks every specified period of time for any panding jobs which has been
 * canceled by another instance of the application. If any of such a jobs are
 * found, a real drop of worker is performed on that machine, which is actually
 * executing a task.
 */
abstract class WatchDog implements Runnable {
private static final int DEFAULT_MAX_ATTEMPTS = 1;

/** logger */
private static final Logger LOGGER = Logger.getLogger(WatchDog.class.getCanonicalName());
/** current thread */
private Thread workerThread;
/** shutdown flag */
private volatile boolean shutdown;
/** autoselect frequency */
private long watchDogFrequency;
/** suspended */
private volatile boolean suspended;

/**
 * Creates instance of the watch-dog.
 * @param watchDogFrequency watch-dog frequency (milliseconds)
 */
public WatchDog(long watchDogFrequency) {
  this.watchDogFrequency = watchDogFrequency;
}

@Override
public void run() {
  workerThread = Thread.currentThread();
  LOGGER.info("[SYNCHRONIZER] Watch-dog activated.");
  int attempt = 0;
  do {
    if (!suspended) {
      LOGGER.finer("[SYNCHRONIZER] Watch-dog entered run mode.");

      String[] uuids = getCurrentlyHarvesterResourceUuids();
      String[] canceledUuids = new String[]{};

      try {
        ArrayList<String> uuidsToCancel = new ArrayList<String>();
        HjRecords records = selectAll(uuids);

        for (HjRecord r : records) {
          if (r.getStatus() == JobStatus.Canceled) {
            uuidsToCancel.add(r.getHarvestSite().getUuid());
          }
        }

        canceledUuids = uuidsToCancel.toArray(new String[uuidsToCancel.size()]);

        if (uuidsToCancel.size() > 0) {
          LOGGER.finer("[SYNCHRONIZER] Watch-dog loaded tasks to drop for resources: " + uuidsToCancel.toString());
        } else {
          LOGGER.finer("[SYNCHRONIZER] Watch-dog loaded no tasks to drop.");
        }

        cancelByResourceUuids(canceledUuids);

        if (canceledUuids.length > 0) {
          withdrawAll(canceledUuids);
        }
        
        // clear attempt counter
        attempt = 0;
      } catch (SQLException ex) {
        attempt++;
        if (attempt<=getMaxAttempts()) {
          LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Error loading tasks for Watch-dog.", ex);
        }
      }
    }

    if (shutdown) break;

    // wait for calculated duration or until interrupted
    synchronized (this) {
      try {
        if (isSuspendedWithAck()) {
          LOGGER.finer("[SYNCHRONIZER] Watch-dog suspended mode");
          wait();
        } else {
          TimePeriod period = new TimePeriod();
          period.setValue(watchDogFrequency);
          LOGGER.finer("[SYNCHRONIZER] Watch-dog enters wait mode for " + period);
          wait(watchDogFrequency);
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
  LOGGER.info("[SYNCHRONIZER] Shutting down Watch-dog.");
  this.shutdown = true;
  if (workerThread != null) {
    workerThread.interrupt();
  }
}

/**
 * Gets array of currently beiing harvested resources uuids.
 * @return array of currently beiing harvested resources uuids
 */
protected abstract String[] getCurrentlyHarvesterResourceUuids();

/**
 * Cancels all tasks specified by resource uuid
 * @param uuids array of resource uuids
 */
protected abstract void cancelByResourceUuids(String[] uuids);

/**
 * Selects all records
 * @param uuids uuids of the records
 * @return collection of records
 * @throws SQLException if accessing database fails
 */
private HjRecords selectAll(String[] uuids) throws SQLException {
  RequestContext context = RequestContext.extract(null);
  try {
    HjLoadAllRequest loadAllRequest = new HjLoadAllRequest(context, uuids);
    loadAllRequest.execute();
    return loadAllRequest.getQueryResult().getRecords();
  } finally {
    context.onExecutionPhaseCompleted();
  }
}

/**
 * Withdraws all records.
 * @param uuids uuids of the records to withdraw
 * @throws SQLException if accessing database fails
 */
private void withdrawAll(String[] uuids) throws SQLException {
  RequestContext context = RequestContext.extract(null);
  try {
    HjWithdrawRequest withdrawRequest = new HjWithdrawRequest(context, uuids);
    withdrawRequest.execute();
  } finally {
    context.onExecutionPhaseCompleted();
  }
}

public synchronized void safeSuspend() {
  if (!suspended) {
    LOGGER.info("[SYNCHRONIZER] Suspending Watch-Dog");
    suspended = true;
    notify();
  } else {
    LOGGER.info("[SYNCHRONIZER] Watch-Dog already suspended");
  }
}

public synchronized void safeResume() {
  if (suspended) {
    LOGGER.info("[SYNCHRONIZER] Resuming Watch-Dog");
    suspended = false;
    notify();
  } else {
    LOGGER.info("[SYNCHRONIZER] Watch-Dog already resumed");
  }
}

private boolean isSuspendedWithAck() {
  if (suspended) {
    LOGGER.info("[SYNCHRONIZER] Watch-Dog acknowledged suspension");
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
