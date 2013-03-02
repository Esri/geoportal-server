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
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.resource.query.Criteria;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Pool of working threads.
 */
final class Pool {

/** logger */
private static final Logger LOGGER = Logger.getLogger(Pool.class.getCanonicalName());
/** data processor */
private final DataProcessor dataProcessor;
/** task queue */
private final TaskQueue taskQueue;
/** collection of workers*/
private ArrayList<Worker> workers = new ArrayList<Worker>();
/** collection of ad-hoc workers */
private ArrayList<OneTimeWorker> adHoc = new ArrayList<OneTimeWorker>();

/**
 * Creates instance of the pool.
 * @param dataProcessor data processor
 * @param taskQueue task queue to pull tasks
 * @param initialSize initial size of the pool
 */
public Pool(DataProcessor dataProcessor, TaskQueue taskQueue, int initialSize) {
  if (dataProcessor==null) {
    throw new IllegalArgumentException("No data processor provided.");
  }
  if (taskQueue==null) {
    throw new IllegalArgumentException("No task queue provided.");
  }
  this.dataProcessor = dataProcessor;
  this.taskQueue = taskQueue;
  this.resize(initialSize);
}

/**
 * Resizes the pool. 
 * If shrinking, worker-thread pairs to be removed are allowed to finish their jobs.
 * @param size desired size of the pool
 */
public synchronized void resize(int size) {
  if (size == workers.size() || size<0)
    return;
  if (size > workers.size()) {
    int missing = size - workers.size();
    for (int i = 0; i < missing; i++) {
      Worker worker = new Worker(dataProcessor, taskQueue);
      Thread thread = new Thread(worker, "harvester");
      workers.add(worker);
      thread.start();
    }
  } else {
    while (workers.size()>size) {
      Worker worker = workers.remove(workers.size() - 1);
      worker.end();
    }
  }
  LOGGER.info("[SYNCHRONIZER] Pool size resized to: "+size());
}

/**
 * Spans separate thread for ad hoc synchronization request.
 * @param resource resource to synchronize
 * @param criteria criteria
 */
public synchronized void span(HrRecord resource, Criteria criteria) {
  OneTimeWorker worker = new OneTimeWorker(dataProcessor, resource, criteria){
      @Override
      protected void onComplete() {
        RequestContext context = RequestContext.extract(null);
        try {
          taskQueue.complete(context, resource.getUuid());
        } finally {
          adHoc.remove(this);
          context.onExecutionPhaseCompleted();
        }
      }
  };
  adHoc.add(worker);
  Thread thread = new Thread(worker, "harvester ad hoc");
  thread.start();
}

/**
 * Gets pool size.
 * @return pool size
 */
public int size() {
  return workers.size();
}

/**
 * Drops executing any worker thread harvesting given repository.
 * @param uuid repository uuid
 */
public boolean drop(String uuid) {
  boolean anythingFound = false;
  for (Worker worker : workers) {
    if (worker.isExecuting(uuid)) {
      worker.drop();
      anythingFound = true;
    }
  }
  return anythingFound;
}

/**
 * Shuts down all the worker threads.
 */
public synchronized void shutdown() {
  for (Worker worker : workers) {
    worker.shutdown();
  }
  for (OneTimeWorker worker : adHoc) {
    worker.shutdown();
  }
}

/**
 * Checks if any worker is currently executing harvesting given repository.
 * @param record repository
 * @return <code>true</code> if there is a worker executing harvesting given repository
 */
public boolean isExecuting(String uuid) {
  return getExecutionUnitFor(uuid)!=null;
}

/**
 * Gets execution unit for given repository id.
 * @param uuid repository id
 * @return execution unit or <code>null</code> if execution unit not available
 */
public ExecutionUnit getExecutionUnitFor(String uuid) {
  for (Worker w : workers) {
    if (w.isExecuting(uuid)) {
      return w.getExecutionUnit();
    }
  }
  for (OneTimeWorker w : adHoc) {
    if (w.isExecuting(uuid)) {
      return w.getExecutionUnit();
    }
  }
  return null;
}

/**
 * Gets array of currently executed execution units.
 * @return array of currently executed execution units
 */
public ExecutionUnit [] getAllExecutedUnits() {
  ArrayList<ExecutionUnit> executedUnits = new ArrayList<ExecutionUnit>();
  for (Worker w : workers) {
    ExecutionUnit unit = w.getExecutionUnit();
    if (unit!=null) executedUnits.add(unit);
  }
  for (OneTimeWorker w : adHoc) {
    ExecutionUnit unit = w.getExecutionUnit();
    if (unit!=null) executedUnits.add(unit);
  }
  return executedUnits.toArray(new ExecutionUnit[executedUnits.size()]);
}

public synchronized void safeSuspend() {
  for (WorkerBase w: workers) {
    w.safeSuspend();
  }
  for (WorkerBase w: adHoc) {
    w.safeSuspend();
  }
}

public synchronized void safeResume() {
  for (WorkerBase w: workers) {
    w.safeResume();
  }
  for (WorkerBase w: adHoc) {
    w.safeResume();
  }
}
}
