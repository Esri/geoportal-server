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
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Worker. A harvesting thread body. Waits for any new harvesting task to appear
 * in the queue and performs harvesting.
 */
class Worker extends WorkerBase {
  private static final int DEFAULT_MAX_ATTEMPTS = 1;

  /**
   * task queue
   */
  protected final TaskQueue taskQueue;
  /**
   * dropped flag
   */
  protected volatile boolean dropped;
  /**
   * ended flag
   */
  protected volatile boolean ended;
  /**
   * to skip
   */
  protected Set<String> toSkip;

  /**
   * Creates instance of the worker.
   *
   * @param dataProcessor data processor
   * @param taskQueue task queue
   */
  public Worker(DataProcessor dataProcessor, TaskQueue taskQueue) {
    super(dataProcessor);
    if (taskQueue == null) {
      throw new IllegalArgumentException("No task queue provided.");
    }
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
   * Let's finish currently working thread, and then shut it down. Allows to
   * complete pending task execution, then shuts down.
   */
  public synchronized void end() {
    ended = true;
    workerThread.interrupt();
  }

  /**
   * Gets dropped flag.
   *
   * @return dropped flag
   */
  public boolean isDropped() {
    return dropped;
  }

  /**
   * Gets ended flag.
   *
   * @return ended flag
   */
  public boolean isEnded() {
    return ended;
  }

  @Override
  protected void execute() {
    int attempt = 0;
    do {
      dropped = false;

      try {
        // get next available task
        ExecutionUnit nextUnit = !suspended ? next() : null;
        // clear atempts counter
        attempt = 0;
        if (nextUnit != null) {
          if (ApprovalStatus.isPubliclyVisible(nextUnit.getRepository().getApprovalStatus().name())
                  && nextUnit.getRepository().getSynchronizable()
                  && !isToSkip(nextUnit.getRepository().getUuid())) {
            // create executor and start harvesting
            Executor exe = newExecutor(nextUnit);
            setExecutor(exe);
            exe.execute();
          } else {
            complete(nextUnit.getRepository().getUuid());
          }
        } else {
          if (isSuspendedWithAck()) {
            synchronized (this) {
              try {
                wait();
              } catch (InterruptedException ex) {
              }
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
        }
      } catch (SQLException ex) {
        attempt++;
        if (attempt<=getMaxAttempts()) {
          Logger.getLogger(Worker.class.getCanonicalName()).log(Level.SEVERE, "[SYNCHRONIZER] Internal worker error.", ex);
        } else {
          // wait for another task
          synchronized (taskQueue) {
            try {
              taskQueue.wait(60000); // wait a minute and try again
            } catch (InterruptedException ex1) {
            }
          }
        }
      } finally {
        if (executor != null && !shutdown) {
          ExecutionUnit unit = executor.getExecutionUnit();
          if (unit != null) {
            complete(unit.getRepository().getUuid());
          }
        }
        setExecutor(null);
      }

    } while (!shutdown && !ended);
  }

  /**
   * Creates new executor.
   *
   * @param unit execution unit
   * @return executor
   */
  private Executor newExecutor(ExecutionUnit unit) {
    return unit.getRepository().getProtocol().newExecutor(dataProcessor, unit, this);
  }

  /**
   * Completes task.
   *
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
   *
   * @return execution nextUnit
   * @throws SQLException if accessing database fails
   */
  private ExecutionUnit next() throws SQLException {
    RequestContext context = RequestContext.extract(null);
    try {
      final Task task = taskQueue.next(context);
      if (task == null) {
        return null;
      }
      return newExecutionUnit(task);
    } finally {
      context.onExecutionPhaseCompleted();
    }
  }

  protected boolean isToSkip(String uuid) {
    if (toSkip == null) {
      toSkip = getSitesToSkip();
    }
    return toSkip != null ? toSkip.contains(uuid) : false;
  }

  protected Set<String> getSitesToSkip() {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    StringAttributeMap parameters = appCfg.getCatalogConfiguration().getParameters();
    String skip = Val.chkStr(parameters.getValue("webharvester.skip"));

    TreeSet<String> set = new TreeSet<String>();
    set.addAll(Arrays.asList(skip.split(",")));
    return set;
  }

  @Override
  public void safeResume() {
    synchronized (taskQueue) {
      super.safeResume();
      taskQueue.notifyAll();
    }
  }

  @Override
  public void safeSuspend() {
    synchronized (taskQueue) {
      super.safeSuspend();
      taskQueue.notifyAll();
    }
  }

  @Override
  public boolean isActive() {
    return super.isActive() && !isDropped();
  }

  private boolean isSuspendedWithAck() {
    if (isSuspended()) {
      Logger.getLogger(Worker.class.getCanonicalName()).log(Level.INFO, "[SYNCHRONIZER] Worker {0} acknowledged suspension", workerThread.getId());
    }
    return isSuspended();
  }
  
  private int getMaxAttempts() {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    StringAttributeMap parameters = appCfg.getCatalogConfiguration().getParameters();
    return Val.chkInt(parameters.getValue("webharvester.maxAttempts"),DEFAULT_MAX_ATTEMPTS);
  }
}
