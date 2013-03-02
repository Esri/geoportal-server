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
package com.esri.gpt.framework.scheduler;

import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.util.TimePeriod;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * Collection of thread definitions.
 */
public class ThreadSchedulerConfiguration implements Serializable {

// class variables =============================================================
/** date format ("HH:mm")*/  
private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");

// instance variables ==========================================================
  
/** activity flag. */
private boolean _active;
/** core pool size. 0 to not create pool */
private int _corePoolSize;
/** thread definitions */
private List<ThreadDefinition> _threadsDefinitions = 
  new ArrayList<ThreadDefinition>();
// constructors ================================================================

// properties ==================================================================

/**
 * Gets <i>active</i> flag value.
 * @return <i>active</i> flag value
 */
public boolean getActive() {
  return _active;
}

/**
 * Sets <i>active</i> flag value.
 * @param active <i>active</i> flag value
 */
public void setActive(boolean active) {
  _active = active;
}

/**
 * Gets core pool size.
 * @return core pool size
 */
public int getCorePoolSize() {
  return _corePoolSize;
}

/**
 * Sets core pool size.
 * @param corePoolSize core pool size
 */
public void setCorePoolSize(int corePoolSize) {
  _corePoolSize = Math.max(corePoolSize, 0);
}

/**
 * Gets threads definitions.
 * @return threads definitions
 */
public List<ThreadDefinition> getThreadsDefinitions() {
  return _threadsDefinitions;
}

/**
 * Sets threads definitions.
 * @param threadsDefinitions threads definitions
 */
public void setThreadsDefinitions(List<ThreadDefinition> threadsDefinitions) {
  _threadsDefinitions = threadsDefinitions!=null? threadsDefinitions: 
    new ArrayList<ThreadDefinition>();
}

// methods =====================================================================

/**
 * Adds a thread definition.
 * @param className the class to be scheduled
 * @param delay to execution since registering thread in units
 * @param period frequency of execution in units
 * @param at time of execution in the format "HH:MM" 
 * @param parameters map of additional optional parameters
 */
public void addDefinition(String className, String delay, String period, 
  String at, StringAttributeMap parameters) {

  ThreadDefinition threadDefinition = new ThreadDefinition();
  className = Val.chkStr(className);
  delay = Val.chkStr(delay);
  period = Val.chkStr(period);
  at = Val.chkStr(at);

  if (className.length() > 0) {
    threadDefinition.setClassName(className);
  }
  
  if (delay.length() > 0) {
    try {
      threadDefinition.setDelay(TimePeriod.parseValue(delay));
    } catch (IllegalArgumentException ex) {
      LogUtil.getLogger().log(Level.SEVERE, "Illegal delay definition: {0}", delay);
    }
  }
  
  if (period.length() > 0) {
    try {
      threadDefinition.setPeriod(TimePeriod.parseValue(period));
    } catch (IllegalArgumentException ex) {
      LogUtil.getLogger().log(Level.SEVERE, "Illegal period definition: {0}", period);
    }
  }
  
  if (at.length() > 0) {
    try {
      threadDefinition.setAt(SDF.parse(at));
    } catch (ParseException ex) {
      LogUtil.getLogger().log(Level.SEVERE, "Illegal time definition: {0}", at);
    }
  }
  
  threadDefinition.setParameters(parameters);
  
  getThreadsDefinitions().add(threadDefinition);
  
}

/**
 * Creates service.
 * @return service or <code>null</code> if service creation not allowed
 */
public ScheduledExecutorService getService() {
  ScheduledExecutorService service = null;
  
  if (getActive()) {
    if (getCorePoolSize()==0) {
      service = Executors.newSingleThreadScheduledExecutor();
    } else {
      service = Executors.newScheduledThreadPool(getCorePoolSize());
    }
  }
  
  return service;
}

/**
 * Creates string representation of the object.
 * @return string representation of the object
 */
@Override
public String toString() {
  StringBuilder sb = new StringBuilder(getClass().getName()).append(" (\r\n");
  
  sb.append(" active=").append(_active).append("\r\n");
  sb.append(" corePoolSize=").append(_corePoolSize).append("\r\n");
  sb.append(") ===== end ").append(getClass().getName());
  
  return sb.toString();
}
}
