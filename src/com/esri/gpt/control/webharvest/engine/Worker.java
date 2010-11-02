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

import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.framework.context.RequestContext;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker. A harvesting thread body. Waits for any new harvesting task to appear
 * in the queue and performs harvesting.
 */
class Worker extends WorkerBase {
/** task queue */
protected final TaskQueue taskQueue;
/** dropped flag */
protected volatile boolean dropped;
/** ended flag */
protected volatile boolean ended;

/**
 * Creates instance of the worker.
 * @param dataProcessor data processor
 * @param taskQueue task queue
 */
public Worker(DataProcessor dataProcessor, TaskQueue taskQueue) {
  super(dataProcessor);
  if (taskQueue == null)
    throw new IllegalArgumentException("No task queue provided.");
  this.taskQueue = taskQueue;
}

/**
 * Drops harvesting current repository and move on to harvesting next one if
 * available, or wait for the next available.
 */
public synchronized void drop() {
  dropped = true;
  workerThread.interrupt();
}

/**
 * Let's finish currently working thread, and then shut it down.
 * Allows to complete pending task execution, then shuts down.
 */
public synchronized void end() {
  ended = true;
  workerThread.interrupt();
}

/**
 * Gets dropped flag.
 * @return dropped flag
 */
public boolean isDropped() {
  return dropped;
}

/**
 * Gets ended flag.
 * @return ended flag
 */
public boolean isEnded() {
  return ended;
}

protected void execute() {
  do {
    dropped = false;

    try {
      // get next available task
      ExecutionUnit nextUnit = next();
      if (nextUnit != null) {
        if (ApprovalStatus.isPubliclyVisible(nextUnit.getRepository().getApprovalStatus().name()) && nextUnit.getRepository().getSynchronizable()) {
          // create executor and start harvesting
          Executor exe = newExecutor(nextUnit);
          setExecutor(exe);
          exe.execute();
        } else {
          complete(nextUnit.getRepository().getUuid());
        }
      } else {
        // wait for another task
        synchronized (taskQueue) {
          try {
            taskQueue.wait(60000); // wait a minute and try again
          } catch (InterruptedException ex) {
          }
        }
      }
    } catch (Exception ex) {
      Logger.getLogger(Worker.class.getCanonicalName()).log(Level.SEVERE, "[SYNCHRONIZER] Internal worker error.", ex);
    } finally {
      if (executor!=null && !shutdown) {
        ExecutionUnit unit = executor.getExecutionUnit();
        if (unit!=null) {
          complete(unit.getRepository().getUuid());
        }
      }
      setExecutor(null);
    }

  } while (!shutdown && !ended);
}

/**
 * Creates new executor.
 * @param unit execution unit
 * @return executor
 */
private Executor newExecutor(ExecutionUnit unit) {
  return new Executor(dataProcessor, unit) {
    @Override
    protected boolean isActive() {
      return !(isShutdown() || isDropped());
    }

    @Override
    protected boolean isShutdown() {
      return Worker.this.isShutdown();
    }
  };
}

/**
 * Completes task.
 * @param uuid repository uuid
 */
private void complete(String uuid) {
  RequestContext context = RequestContext.extract(null);
  try {
    taskQueue.complete(context, uuid);
  } finally {
    context.onExecutionPhaseCompleted();
  }
}

/**
 * Gets execution nextUnit for the next task.
 * @return execution nextUnit
 */
private ExecutionUnit next() {
  RequestContext context = RequestContext.extract(null);
  try {
    final Task task = taskQueue.next(context);
    if (task == null)
      return null;
    return newExecutionUnit(task);
  } finally {
    context.onExecutionPhaseCompleted();
  }
}

}
