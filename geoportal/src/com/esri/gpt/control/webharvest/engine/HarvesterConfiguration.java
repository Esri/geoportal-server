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

import com.esri.gpt.framework.context.Configuration;
import com.esri.gpt.framework.util.TimePeriod;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import java.util.List;

/**
 * Harvester configuration.
 */
public class HarvesterConfiguration extends Configuration {

/** default pool of worker threads getPoolSize */
public static final int DEFAULT_POOL_SIZE = 4;
/** default autoselect wait/sleep duration (1 hour) */
public static final long AUTOSELECT_FREQUENCY = 3600000;
/** default watch-dog wait/sleep duration (1 minute) */
public static final long WATCHDOG_FREQUENCY = 60000;
/** default maximum reported records */
public static final long MAX_REP_RECORDS = 10000;
/** default maximum reported errors */
public static final long MAX_REP_ERRORS = 5000;
/** default resource auto approve */
public static final boolean RESOURCE_AUTOAPPROVE = false;
/** active/inactive flag */
private boolean active;
/** suspended flag */
private boolean suspended;
/** queue enabled */
private boolean queueEnabled;
/** initial worker threads pool size */
private int poolSize;
/** auto-selection frequency (milliseconds) */
private TimePeriod autoSelectFrequency;
/** watch-dog frequency */
private TimePeriod watchDogFrequency;
/** base context path */
private String baseContextPath = "";
/** maximum reported records */
private long maxRepRecords = MAX_REP_RECORDS;
/** maximum reported errors */
private long maxRepErrors = MAX_REP_ERRORS;
/** resource auto approve */
private boolean resourceAutoApprove = RESOURCE_AUTOAPPROVE;
/** list of data processor factories */
private List<DataProcessorFactory> dataProcessorFactories = new ArrayList<DataProcessorFactory>();

/**
 * Gets 'active' flag
 * @return <code>true</code> if harvester should be activated at startup
 */
public boolean getActive() {
  return active;
}

/**
 * Sets 'active' flag.
 * @param active <code>true</code> if harvester should be activated at startup
 */
public void setActive(boolean active) {
  this.active = active;
}

/**
 * Gets 'suspended' flag.
 * @return <code>true</code> if suspended
 */
public boolean getSuspended() {
  return suspended;
}

/**
 * Sets 'suspended' flag.
 * @param suspended <code>true</code> to mark as suspended
 */
public void setSuspended(boolean suspended) {
  this.suspended = suspended;
}

/**
 * Checks if queue enabled.
 * @return <code>true</code> if queue enabled
 */
public boolean getQueueEnabled() {
  return queueEnabled;
}

/**
 * Enables/disables queue.
 * @param queueEnabled <code>true</code> to enable queue
 */
public void setQueueEnabled(boolean queueEnabled) {
  this.queueEnabled = queueEnabled;
}

/**
 * Gets initial worker threads pool size.
 * @return the poolSize
 */
public int getPoolSize() {
  return poolSize;
}

/**
 * Sets initial worker threads pool size.
 * @param poolSize the poolSize to set
 */
public void setPoolSize(int poolSize) {
  this.poolSize = poolSize;
}

/**
 * Gets auto-selection frequency (milliseconds).
 * @return the autoSelectFrequency
 */
public long getAutoSelectFrequency() {
  return autoSelectFrequency != null ? autoSelectFrequency.getValue() : 0;
}

/**
 * Gets auto-selection frequency (String).
 * @return the autoSelectFrequency
 */
private String getAutoSelectFrequencyAsString() {
  return autoSelectFrequency != null ? autoSelectFrequency.toString() : "0";
}

/**
 * Sets auto-selection frequency (milliseconds).
 * @param autoSelectFrequency the autoSelectFrequency to set
 */
public void setAutoSelectFrequency(TimePeriod autoSelectFrequency) {
  this.autoSelectFrequency = autoSelectFrequency;
}

/**
 * Gets base context path
 * @return the baseContextPath
 */
public String getBaseContextPath() {
  return baseContextPath;
}

/**
 * Sets base context path
 * @param baseContextPath the baseContextPath to set
 */
public void setBaseContextPath(String baseContextPath) {
  this.baseContextPath = Val.chkStr(baseContextPath);
}

/**
 * Gets watch-dog frequency (milliseconds).
 * @return watch-dog frequency
 */
public long getWatchDogFrequency() {
  return watchDogFrequency != null ? watchDogFrequency.getValue() : 0;
}

/**
 * Gets watch-dog frequency (String).
 * @return watch-dog frequency
 */
private String getWatchDogFrequencyAsString() {
  return watchDogFrequency != null ? watchDogFrequency.toString() : "0";
}

/**
 * Sets watch-dog frequency (milliseconds).
 * @param watchDogFrequency watch-dog frequency
 */
public void setWatchDogFrequency(TimePeriod watchDogFrequency) {
  this.watchDogFrequency = watchDogFrequency;
}

/**
 * Gets maximum number of errors to report.
 * @return maximum number of errors to report or <code>-1</code> for no limit
 */
public long getMaxRepErrors() {
  return maxRepErrors;
}

/**
 * Sets maximum number of errors to report.
 * @param maxRepErrors maximum number of errors to report or <code>-1</code> for no limit
 */
public void setMaxRepErrors(long maxRepErrors) {
  this.maxRepErrors = maxRepErrors;
}

/**
 * Gets maximum number of records to report.
 * @return maximum number of records to report or <code>-1</code> for no limit
 */
public long getMaxRepRecords() {
  return maxRepRecords;
}

/**
 * Sets maximum number of records to report.
 * @param maxRepRecords maximum number of records to report or <code>-1</code> for no limit
 */
public void setMaxRepRecords(long maxRepRecords) {
  this.maxRepRecords = maxRepRecords;
}

/**
 * Checks if auto approve resource.
 * @return <code>true</code> if auto approve resource
 */
public boolean getResourceAutoApprove() {
  return resourceAutoApprove;
}

/**
 * Sets if auto approve resource.
 * @param resourceAutoApprove <code>true</code> to auto approve resource
 */
public void setResourceAutoApprove(boolean resourceAutoApprove) {
  this.resourceAutoApprove = resourceAutoApprove;
}

/**
 * Gets data processor factories.
 * @return data processor factories
 */
public List<DataProcessorFactory> getDataProcessorFactories() {
  return dataProcessorFactories;
}

/**
 * Sets data processor factories.
 * @param dataProcessorFactories data processor factories
 */
public void setDataProcessorFactories(List<DataProcessorFactory> dataProcessorFactories) {
  this.dataProcessorFactories = dataProcessorFactories!=null? dataProcessorFactories: new ArrayList<DataProcessorFactory>();
}

/**
 * Creates string representation of the object.
 * @return string representation of the object
 */
@Override
public String toString() {
  StringBuilder sb = new StringBuilder(getClass().getName()).append(" (\r\n");

  sb.append(" active=").append(active).append("\r\n");
  sb.append(" queueEnabled=").append(queueEnabled).append("\r\n");
  sb.append(" poolSize=").append(poolSize).append("\r\n");
  sb.append(" autoSelectFrequency=").append(getAutoSelectFrequencyAsString()).append("\r\n");
  sb.append(" watchDogFrequency=").append(getWatchDogFrequencyAsString()).append("\r\n");
  sb.append(" baseContextPath=").append(baseContextPath).append("\r\n");
  sb.append(" maxRepRecords=").append(maxRepRecords).append("\r\n");
  sb.append(" maxRepErrors=").append(maxRepErrors).append("\r\n");
  sb.append(" resource.autoApprove=").append(resourceAutoApprove).append("\r\n");
  sb.append(") ===== end ").append(getClass().getName());

  return sb.toString();
}
}
