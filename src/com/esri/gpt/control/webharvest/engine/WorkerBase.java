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

import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.UuidUtil;

/**
 * Worker base.
 */
abstract class WorkerBase implements Runnable {
/** data processor */
protected final DataProcessor dataProcessor;
/** executor */
protected Executor executor;
/** shutdown flag */
protected volatile boolean shutdown;
/** worker thread */
protected Thread workerThread;

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
public synchronized boolean isExecuting(String uuid) {
  if (executor == null)
    return false;
  return UuidUtil.removeCurlies(executor.getExecutionUnit().getRepository().getUuid()).equalsIgnoreCase(UuidUtil.removeCurlies(uuid));
}

/**
 * Gets shutdown flag.
 * @return shutdown flag
 */
public boolean isShutdown() {
  return shutdown;
}

/**
 * Completly shuts down the worker.
 * Does not allow to complete pending task execution.
 */
public synchronized void shutdown() {
  shutdown = true;
  workerThread.interrupt();
}

/**
 * Creates new execution unit.
 * @param task task upon the execution unit shall be created
 * @return execution unit
 */
protected ExecutionUnit newExecutionUnit(final Task task) {
  if (task==null)
    throw new IllegalArgumentException("No task provided.");
  IterationContext ic = new IterationContext() {
    public void onIterationException(Exception ex) {
      dataProcessor.onIterationException(task, ex);
    }
  };
  QueryBuilder queryBuilder = task.getResource().newQueryBuilder(ic);
  if (queryBuilder==null) return null;
  ExecutionUnit unit = new ExecutionUnit(task.getResource(), task.getCriteria(), queryBuilder);
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
}
