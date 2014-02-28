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

import com.esri.gpt.catalog.harvest.repository.HrUpdateLastSyncDate;
import com.esri.gpt.control.webharvest.common.CommonResult;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.resource.adapters.FlatResourcesAdapter;
import com.esri.gpt.framework.resource.adapters.PublishablesAdapter;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Query;
import com.esri.gpt.framework.resource.query.Result;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Executes query.
 */
public abstract class Executor {
/** logger */
private static final Logger LOGGER = Logger.getLogger(Executor.class.getCanonicalName());

/** data processor */
private DataProcessor dataProcessor;
/** query used to obtain metadata */
private ExecutionUnit unit;

/**
 * Creates instance of the executor.
 * @param dataProcessor data processor
 * @param unit execution unit
 */
public Executor(DataProcessor dataProcessor, ExecutionUnit unit) {
  if (dataProcessor == null)
    throw new IllegalArgumentException("No execution context provided.");
  if (unit == null)
    throw new IllegalArgumentException("No execution unit provided.");
  this.dataProcessor = dataProcessor;
  this.unit = unit;
}

/**
 * Gets execution unit.
 * @return execution unit
 */
public ExecutionUnit getExecutionUnit() {
  return unit;
}

/**
 * Gets data processor.
 * @return data processor
 */
protected DataProcessor getProcessor() {
  return dataProcessor;
}

/**
 * Executes query.
 */
public void execute() {
  RequestContext context = RequestContext.extract(null);
  
  boolean success = false;
  long count = 0;
  Result result = null;
  LOGGER.log(Level.FINEST, "[SYNCHRONIZER] Starting harvesting through unit: {0}", unit);
  if (isActive()) {
    dataProcessor.onStart(unit);
  }

  ExecutionUnitHelper helper = new ExecutionUnitHelper(unit);
  // get report builder
  ReportBuilder rp = helper.getReportBuilder();

  try {
    result = executeQuery();
    Iterable<Publishable> records = new PublishablesAdapter(new FlatResourcesAdapter(result.getResources()));
    for (Publishable r : records) {
      if (!isActive()){
        unit.setCleanupFlag(false);
        break;
      }
      count++;
      LOGGER.log(Level.FINEST, "[SYNCHRONIZER] Harvested metadata #{0} of source URI: \"{1}\" through unit: {2}", new Object[]{rp.getHarvestedCount()+1, r.getSourceUri(), unit});
      if (isSuspendedWithAck()) {
        while (isSuspended()) {
          try {
            synchronized (this) {
              wait();
            }
          } catch (InterruptedException ex) {

          }
          if (!isActive()) {
            break;
          }
        }
      }
      if (isActive()) {
        dataProcessor.onMetadata(unit, r);
      }
    }
    
    success = true;
    
    if (isActive()) {
      // save last sync date
      unit.getRepository().setLastSyncDate(rp.getStartTime());
      HrUpdateLastSyncDate updLastSyncDate = new HrUpdateLastSyncDate(context, unit.getRepository());
      updLastSyncDate.execute();
    }
  } catch (Exception ex) {
    rp.setException(ex);
    unit.setCleanupFlag(false);
    LOGGER.log(Level.FINEST, "[SYNCHRONIZER] Failed harvesting through unit: {0}. Cause: {1}", new Object[]{unit, ex.getMessage()});
    dataProcessor.onIterationException(unit, ex);
  } finally {
    try {
      if (!isShutdown()) {
        dataProcessor.onEnd(unit, success);
        context.onExecutionPhaseCompleted();
      }
    } finally {
      if (result!=null) {
        result.destroy();
      }
    }
    LOGGER.log(Level.FINEST, "[SYNCHRONIZER] Completed harvesting through unit: {0}. Obtained {1} records.", new Object[]{unit, count});
  }
}

/**
 * Executes query.
 * @return result
 */
private Result executeQuery() {
  if (ProtocolInvoker.getUpdateContent(unit.getRepository().getProtocol())) {
    Query query = unit.getQuery();
    return query!=null?
      query.execute():
      new CommonResult(new ArrayList<Resource>());
  } else if (ProtocolInvoker.getUpdateDefinition(unit.getRepository().getProtocol())) {
    return new CommonResult(Arrays.asList(new Resource[]{unit.getNative()}));
  } else {
    return new CommonResult(new ArrayList<Resource>());
  }
}

/**
 * Cleans up.
 */
public void shutdown() {
  if (unit!=null) {
    ExecutionUnitHelper helper = new ExecutionUnitHelper(unit);
    ReportBuilder rp = helper.getReportBuilder();
    if (rp!=null) {
      rp.cleanup();
    }
    SourceUriArray sourceUris = helper.getSourceUris();
    if (sourceUris!=null) {
      try {
        sourceUris.close();
      } catch (IOException ex) {}
    }
  }
}

/**
 * Indicates if thread is active.
 * @return <code>true</code> if thread is active
 */
protected abstract boolean isActive();

/**
 * Indicates if thread is in shutdown process
 * @return <code>true</code> if thread is in shutdown process
 */
protected abstract boolean isShutdown();

/**
 * Indicates if thread is in suspended mode
 * @return <code>true</code> if thread is in suspended mode
 */
protected abstract boolean isSuspended();

@Override
public String toString() {
  return "Executor: {unit: "+unit+"}";
}

/**
 * Checks if thread is suspended (with printed acknowledge to the log) 
 * @return <code>true</code> if thread is in suspended mode
 */
private boolean isSuspendedWithAck() {
  boolean val = isSuspended();
  if (val) {
    LOGGER.log(Level.INFO, "[SYNCHRONIZER] Executor on thread: {0} acknowledged suspension.", Thread.currentThread().getId());
  }
  return val;
}
}
