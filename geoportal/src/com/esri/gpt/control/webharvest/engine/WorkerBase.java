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

import com.esri.gpt.framework.util.UuidUtil;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker base.
 */
abstract class WorkerBase implements Runnable, IWorker {
/** logger */
private static final Logger LOGGER = Logger.getLogger(WorkerBase.class.getCanonicalName());
/** data processor */
protected final DataProcessor dataProcessor;
/** executor */
protected Executor executor;
/** shutdown flag */
protected volatile boolean shutdown;
/** worker thread */
protected Thread workerThread;
/** suspended */
protected volatile boolean suspended;

/**
 * Creates instance of the worker.
 * @param dataProcessor data processor
 */
public WorkerBase(DataProcessor dataProcessor) {
  if (dataProcessor == null)
    throw new IllegalArgumentException("No execution context provided.");
  this.dataProcessor = dataProcessor;
}

public void run() {
  workerThread = Thread.currentThread();
  execute();
}

/**
 * Gets execution unit.
 * @return execution unit or <code>null</code> if no execution unit
 */
public synchronized ExecutionUnit getExecutionUnit() {
  if (executor == null)
    return null;
  return executor.getExecutionUnit();
}

/**
 * Checks if is executing synchronization of the specific repository.
 * @param uuid repository UUID
 * @return <code>true</code> if is executing synchronization of the specific repository
 */
  @Override
  public synchronized boolean isExecuting(String uuid) {
  if (executor == null)
    return false;
  return UuidUtil.removeCurlies(executor.getExecutionUnit().getRepository().getUuid()).equalsIgnoreCase(UuidUtil.removeCurlies(uuid));
}

/**
 * Gets shutdown flag.
 * @return shutdown flag
 */
  @Override
  public boolean isShutdown() {
  return shutdown;
}

/**
 * Checks if worker is active.
 * @return <code>true</code> if worker is active
 */
  @Override
  public boolean isActive() {
  return !isShutdown();
}

/**
 * Completly shuts down the worker.
 * Does not allow to complete pending task execution.
 */
public synchronized void shutdown() {
  if (executor!=null) {
    executor.shutdown();
  }
  shutdown = true;
  workerThread.interrupt();
}

/**
 * Creates new execution unit.
 * @param task task upon the execution unit shall be created
 * @return execution unit
 */
protected ExecutionUnit newExecutionUnit(final Task task) {
  ExecutionUnit unit = new ExecutionUnit(task) {
    @Override
    protected void onIteratonException(Exception ex) {
      dataProcessor.onIterationException(this, ex);
    }
  };
  return unit;
}

/**
 * Sets executor.
 * @param executor executor or <code>null</code>
 */
protected synchronized void setExecutor(Executor executor) {
  this.executor = executor;
}

/**
 * Executes worker tasks.
 */
protected abstract void execute();

public synchronized void safeSuspend() {
  if (workerThread!=null) {
    if (!suspended) {
      LOGGER.log(Level.INFO, "[SYNCHRONIZER] Suspending Worker thread: {0}", workerThread.getId());
      suspended = true;
      notify();
      workerThread.interrupt();
    } else {
      LOGGER.log(Level.INFO, "[SYNCHRONIZER] Worker thread already suspended: {0}", workerThread.getId());
    }
  }
}

public synchronized void safeResume() {
  if (workerThread!=null) {
    if (suspended) {
      LOGGER.log(Level.INFO, "[SYNCHRONIZER] Resuming Worker thread: {0}", workerThread.getId());
      suspended = false;
      workerThread.interrupt();
    } else {
      LOGGER.log(Level.INFO, "[SYNCHRONIZER] Worker thread already resumed: {0}", workerThread.getId());
    }
  }
}

/**
 * Gets suspended flag.
 * @return suspended flag
 */
  @Override
  public boolean isSuspended() {
  return suspended;
}
}
