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

import com.esri.gpt.catalog.harvest.jobs.HjCancelRequest;
import com.esri.gpt.catalog.harvest.jobs.HjCompleteRequest;
import com.esri.gpt.catalog.harvest.jobs.HjCreateRequest;
import com.esri.gpt.catalog.harvest.jobs.HjGetNextRequest;
import com.esri.gpt.catalog.harvest.jobs.HjLoadAllRequest;
import com.esri.gpt.catalog.harvest.jobs.HjRecord;
import com.esri.gpt.catalog.harvest.jobs.HjRecords;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.control.webharvest.common.CommonCriteria;
import com.esri.gpt.framework.context.RequestContext;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Queue of tasks. Allows to add new taskDescriptor to the end of the queue and pick
 * a next taskDescriptor from the begining of the queue. Only one taskDescriptor of associated with
 * given repository can exist in the queue.
 */
class TaskQueue {

/** logger */
private static final Logger LOGGER = Logger.getLogger(TaskQueue.class.getCanonicalName());

/**
 * Adds new harvesting task to the queue.
 * @param context request context
 * @param resource resource to harvest
 * @param criteria criteria
 * @return <code>true</code> if task has been sumbited
 */
public synchronized boolean add(RequestContext context, HrRecord resource, CommonCriteria criteria) {
  HjCreateRequest request = new HjCreateRequest(context, resource, criteria, false);
  boolean result = request.execute();
  notify();
  return result;
}

/**
 * Registers harvesting task. No notification will be sent.
 * @param context request context
 * @param resource resource to harvest
 * @param criteria criteria
 * @return <code>true</code> if task has been registered
 */
public synchronized boolean register(RequestContext context, HrRecord resource, CommonCriteria criteria) {
  HjCreateRequest request = new HjCreateRequest(context, resource, criteria, true);
  return request.execute();
}

/**
 * Notifies change.
 */
public synchronized void notifyChange() {
  notify();
}

/**
 * Cancels harvesting of a specific resource.
 * @param context request context
 * @param uuid resource uuid
 * @return <code>true</code> if matching taskDescriptor has been found and has been withdrawn
 */
public boolean cancel(RequestContext context, String uuid) {
  try {
    HjCancelRequest request = new HjCancelRequest(context, uuid);
    return request.execute();
  } catch (SQLException ex) {
    LOGGER.log(Level.WARNING, "[SYNCHRONIZER] Error canceling task", ex);
    return false;
  }
}

/**
 * Completes the task.
 * @param context request context
 * @param uuid resource UUID
 * @return <code>true</code> if task has been found and completed
 */
public boolean complete(RequestContext context, String uuid) {
  try {
    HjCompleteRequest request = new HjCompleteRequest(context, uuid);
    return request.execute();
  } catch (SQLException ex) {
    LOGGER.log(Level.WARNING, "[SYNCHRONIZER] Error completing task", ex);
    return false;
  }
}

/**
 * Gets next taskDescriptor in the queue.
 * @param context request context
 * @return task descriptor or <code>null</code> if no more tasks
 * @throws SQLException if accessing database fails
 */
public Task next(RequestContext context) throws SQLException {
  HjGetNextRequest request = new HjGetNextRequest(context);
  request.execute();
  HjRecords records = request.getQueryResult().getRecords();
  if (records.size() != 1) {
    return null;
  }
  return new Task(records.get(0));
}

/**
 * Gets all tasks.
 * @param context request context
 * @return all tasks
 */
public Task[] all(RequestContext context) {
  try {
    HjLoadAllRequest request = new HjLoadAllRequest(context);
    request.execute();
    ArrayList<Task> tasks = new ArrayList<Task>();
    for (HjRecord record : request.getQueryResult().getRecords()) {
      tasks.add(new Task(record));
    }
    return tasks.toArray(new Task[tasks.size()]);
  } catch (SQLException ex) {
    LOGGER.log(Level.WARNING, "[SYNCHRONIZER] Error getting all tasks", ex);
    return new Task[]{};
  }
}
}
