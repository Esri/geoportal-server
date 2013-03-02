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
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Thread definition.
 */
public class ThreadDefinition implements Serializable {

// class variables =============================================================

// instance variables ==========================================================
/** thread class */
private String _className = "";
/** delay */
private TimePeriod _delay = new TimePeriod();
/** period */
private TimePeriod _period = new TimePeriod();
/** date of the execution */
private Date _at = extractTime(new Date());
/** has delay indicator */
private boolean _hasDelay;
/** has period indicator */
private boolean _hasPeriod;
/** has date indicator */
private boolean _hasAt;
/** additional parameters */
private StringAttributeMap _parameters = new StringAttributeMap();
// constructors ================================================================

// properties ==================================================================
/**
 * Checks if thread definition has delay.
 * @return <code>true</code> if has delay
 */
public boolean hasDelay() {
  return _hasDelay;
}

/**
 * Checks if thread definition has period.
 * @return <code>true</code> if has period
 */
public boolean hasPeriod() {
  return _hasPeriod;
}

/**
 * Checks if thread definition has date.
 * @return <code>true</code> if has date
 */
public boolean hasAt() {
  return _hasAt;
}

/**
 * Gets thread class name.
 * @return thread class name
 */
public String getClassName() {
  return _className;
}

/**
 * Sets thread class name.
 * @param className thread class name
 */
public void setClassName(String className) {
  _className = Val.chkStr(className);
}

/**
 * Gets delay.
 * @return return delay
 */
public TimePeriod getDelay() {
  return _delay;
}

/**
 * Sets delay.
 * <i>hasDelay</i> flag wil be set to <code>true</code>. <i>hasAt</i> flag will
 * be set to <code>false</code>.
 * @param delay delay (<code>null</code> to clear delay)
 */
public void setDelay(TimePeriod delay) {
  _delay = delay != null ? delay : new TimePeriod();
  _hasDelay = delay != null;
  if (_hasDelay) {
    _hasAt = false;
  }
}

/**
 * Gets period.
 * @return period
 */
public TimePeriod getPeriod() {
  return _period;
}

/**
 * Sets period.
 * <i>hasPeriod</i> flag wil be set to <code>true</code>. <i>hasAt</i> flag will
 * be set to <code>false</code>.
 * @param period period
 */
public void setPeriod(TimePeriod period) {
  _period = period != null ? period : new TimePeriod();
  _hasPeriod = period != null;
  if (_hasPeriod) {
    _hasAt = false;
  }
}

/**
 * Gets time.
 * @return time of the daily execution
 */
public Date getAt() {
  return _at;
}

/**
 * Sets time.
 * @param time time of the daily execution
 */
public void setAt(Date time) {
  _at = time != null ? extractTime(time) : extractTime(new Date());
  _hasAt = time != null;
  if (_hasAt) {
    _hasDelay = false;
    _hasPeriod = false;
  }
}

/**
 * Sets additional parameters.
 * @param parameters parameters
 */
public void setParameters(StringAttributeMap parameters) {
  _parameters = parameters!=null? parameters: new StringAttributeMap();
}

// methods =====================================================================
/**
 * Schedules thread.
 * @param service service used to schedule
 * @return scheduled future
 */
public ScheduledFuture schedule(ScheduledExecutorService service) {

  if (service != null) {
    Runnable runnable = createRunnable();
    if (runnable != null) {
      
      if (runnable instanceof IScheduledTask) {
        IScheduledTask scheduledTask = (IScheduledTask)runnable;
        scheduledTask.setParameters(_parameters);
      }
      
      if (hasAt()) {

        return scheduleAt(runnable, service);

      } else if (hasPeriod()) {

        return schedulePeriodical(runnable, service);

      } else {

        return scheduleOnce(runnable, service);
        
      }
    }
  }

  return null;
}

/**
 * Schedule thread at fixed rate.
 * @param runnable runnable to schedule
 * @param service service used to schedule
 * @return scheduled future
 */
private ScheduledFuture scheduleAt(Runnable runnable,
                                    ScheduledExecutorService service) {
  Date now = extractTime(new Date());
  long delay = getAt().getTime() - now.getTime();
  if (delay < 0) {
    delay += 86400000L;
  }
  return service.scheduleAtFixedRate(
    runnable, delay, 86400000L, TimeUnit.MILLISECONDS);
}

/**
 * Schedules periodical task.
 * @param runnable runnable to schedule
 * @param service service used to schedule
 * @return scheduled future
 */
private ScheduledFuture schedulePeriodical(Runnable runnable,
                                            ScheduledExecutorService service) {
  return service.scheduleWithFixedDelay(
    runnable, getDelay().getValue(), getPeriod().getValue(),
    TimeUnit.MILLISECONDS);
}

/**
 * Schedules one shot task.
 * @param runnable runnable to schedule
 * @param service service used to schedule
 * @return scheduled future
 */
private ScheduledFuture scheduleOnce(Runnable runnable,
                                      ScheduledExecutorService service) {
return service.schedule(runnable, getDelay().getValue(), TimeUnit.MILLISECONDS);                                      
}

/**
 * Creates instance of the runnable class.
 * @return  instance of the runnable class
 */
private Runnable createRunnable() {
  Runnable runnable = null;

  if (getClassName().length() > 0) {
    try {
      Class classObj = Class.forName(getClassName());
      runnable = (Runnable) classObj.newInstance();
    } catch (ClassNotFoundException ex) {
      LogUtil.getLogger().severe("Error creating runnable: " + ex.getMessage());
    } catch (InstantiationException ex) {
      LogUtil.getLogger().severe("Error creating runnable: " + ex.getMessage());
    } catch (IllegalAccessException ex) {
      LogUtil.getLogger().severe("Error creating runnable: " + ex.getMessage());
    } catch (ClassCastException ex) {
      LogUtil.getLogger().severe("Error creating runnable: " + ex.getMessage());
    }
  }

  return runnable;
}

/**
 * Extract date (hour, minue) from given date.
 * @param date given date
 * @return date
 */
private Date extractTime(Date date) {
  if (date != null) {
    Calendar initCal = Calendar.getInstance();
    initCal.setTime(date);

    Calendar timeCal = Calendar.getInstance();
    timeCal.clear();

    timeCal.set(Calendar.HOUR_OF_DAY, initCal.get(Calendar.HOUR_OF_DAY));
    timeCal.set(Calendar.MINUTE, initCal.get(Calendar.MINUTE));

    return timeCal.getTime();
  }
  return null;
}
}
