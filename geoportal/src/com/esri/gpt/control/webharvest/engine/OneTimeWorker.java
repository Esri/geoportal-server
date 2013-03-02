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
import com.esri.gpt.framework.resource.query.Criteria;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * One time worker.
 */
abstract class OneTimeWorker extends WorkerBase {

/** resource */
protected HrRecord resource;
/** criteria */
protected Criteria criteria;

/**
 * Creates instance of the worker.
 * @param dataProcessor data processor
 * @param resource resource
 * @param criteria criteria
 */
public OneTimeWorker(DataProcessor dataProcessor, HrRecord resource, Criteria criteria) {
  super(dataProcessor);
  if (resource == null)
    throw new IllegalArgumentException("No resource provided.");
  this.resource = resource;
  this.criteria = criteria;
}

@Override
protected void execute() {
  try {
    Task task = new Task(resource, criteria);
    ExecutionUnit unit = newExecutionUnit(task);
    Executor exe = newExecutor(unit);
    setExecutor(exe);
    exe.execute();
  } catch (Exception ex) {
    Logger.getLogger(Worker.class.getCanonicalName()).log(Level.SEVERE, "[SYNCHRONIZER] Internal worker error.", ex);
  } finally {
    onComplete();
    setExecutor(null);
  }
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
      return !(isShutdown());
    }
    @Override
    protected boolean isShutdown() {
      return OneTimeWorker.this.isShutdown();
    }

    @Override
    protected boolean isSuspended() {
      return OneTimeWorker.this.isSuspended();
    }
  };
}

/**
 * Called upon completion.
 */
protected abstract void onComplete();
}
