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

import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.resource.adapters.FlatResourcesAdapter;
import com.esri.gpt.framework.resource.adapters.PublishablesAdapter;
import com.esri.gpt.framework.resource.api.Publishable;
import com.esri.gpt.framework.resource.api.Resource;
import com.esri.gpt.framework.resource.query.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Executes query.
 */
abstract class Executor {
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
 * Executes query.
 */
public void execute() {
  long count = 0;
  LOGGER.finest("[SYNCHRONIZER] Starting harvesting through unit: "+unit);
  if (isActive()) {
    dataProcessor.onStart(unit);
  }

  // get report builder
  ReportBuilder rp = unit.getReportBuilder();

  try {
    Iterable<Publishable> records = new PublishablesAdapter(getResources());
    for (Publishable r : records) {
      if (!isActive()){
        unit.setCleanupFlag(false);
        break;
      }
      count++;
      try {
        LOGGER.finest("[SYNCHRONIZER] Harvested metadata #"+(rp.getHarvestedCount()+1)+" of source URI: \"" +r.getSourceUri()+ "\" through unit: "+unit);
        if (isActive()) {
          dataProcessor.onMetadata(unit, r);
        }
      } catch (IOException ex) {
        LOGGER.finest("[SYNCHRONIZER] Failed harvesting metadata #"+(rp.getHarvestedCount()+1)+" of source URI: \"" +r.getSourceUri()+ "\" through unit: "+unit+". Cause: "+ex.getMessage());
        if (isActive()) {
          dataProcessor.onIterationException(unit, r.getSourceUri(), ex);
        }
      }
    }
    if (isActive()) {
      dataProcessor.onCompleted(unit);
    }
  } catch (Exception ex) {
    unit.setCleanupFlag(false);
    LOGGER.finest("[SYNCHRONIZER] Failed harvesting through unit: "+unit+". Cause: "+ex.getMessage());
    dataProcessor.onIterationException(unit, ex);
  } finally {
    if (!isShutdown()) {
      dataProcessor.onEnd(unit);
    }
    LOGGER.finest("[SYNCHRONIZER] Completed harvesting through unit: "+unit+". Obtained "+count+" records.");
  }
}

/**
 * Gets resources.
 * @return resources
 * @throws QueryException if getting resources fails
 */
private Iterable<Resource> getResources() {
  if (ProtocolInvoker.getUpdateContent(unit.getRepository().getProtocol())) {
    Query query = unit.getQuery();
    return query!=null?
      new FlatResourcesAdapter(query.execute().getResources()):
      new ArrayList<Resource>();
  } else if (ProtocolInvoker.getUpdateDefinition(unit.getRepository().getProtocol())) {
    return Arrays.asList(new Resource[]{unit.getNative()});
  } else {
    return new ArrayList<Resource>();
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

@Override
public String toString() {
  return "Executor: {unit: "+unit+"}";
}
}
