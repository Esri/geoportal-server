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
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.TimePeriod;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Schedules new harvesting task according to the repository harvest frequency.
 */
abstract class AutoSelector implements Runnable {

/** logger */
private static final Logger LOGGER = Logger.getLogger(AutoSelector.class.getCanonicalName());
/** current thread */
private Thread workerThread;
/** shutdown flag */
private volatile boolean shutdown;
/** autoselect frequency */
private long autoSelectFrequency;

/**
 * Creates instance of auto selector.
 * @param autoSelectFrequency auto select frequency (milliseconds)
 */
public AutoSelector(long autoSelectFrequency) {
  this.autoSelectFrequency = autoSelectFrequency;
}

public void run() {
  workerThread = Thread.currentThread();
  LOGGER.info("[SYNCHRONIZER] AutoSelector activated.");

  do {
    LOGGER.finer("[SYNCHRONIZER] AutoSelector entered run mode.");

    long duration = autoSelectFrequency;

    try {
      HrRecords records = selectRecords();

      // selecting all records with harvest date due now
      HrRecords recordsDueNow = records.findHarvestDue();
      LOGGER.finer("[SYNCHRONIZER] AutoSelector selected " + recordsDueNow.size() + " records with harvest date due now.");

      // process all with harvest date due now
      for (HrRecord r : recordsDueNow) {
        if (shutdown)
          break;
        // this is it; do something in overriden method
        onSelect(r);
      }

      // get the one record with the closes due date but not due yet
      HrRecord nextDue = records.findNextDue();

      // caluclate duration in milliseconds
      duration = nextDue != null ? nextDue.getNextHarvestDate().getTime() - (new Date()).getTime() : autoSelectFrequency;
    } catch (SQLException ex) {
      LOGGER.log(Level.SEVERE, "[SYNCHRONIZER] Error selecting harvesting sites for harvest.", ex);
    }

    if (shutdown)
      break;

    TimePeriod period = new TimePeriod();
    period.setValue(duration);
    
    LOGGER.finer("[SYNCHRONIZER] AutoSelector enters wait mode for " + period);
    // wait for calculated duration or until interrupted
    synchronized (this) {
      try {
        this.wait(duration);
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
}
