/*
 * Copyright 2013 Esri.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.esri.gpt.framework.adhoc.events;

import com.esri.gpt.framework.jsf.MessageBroker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Day of the month ad-hoc event.
 */
public class DayOfTheMonthEvent extends AdHocEvent {
  private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");
  private int dayOfTheMonth;
  private Date timeOfTheDay;

  /**
   * Creates instance of the event.
   * @param dayOfTheMonth day of the month
   * @param timeOfTheDay  time of the day
   */
  public DayOfTheMonthEvent(int dayOfTheMonth, Date timeOfTheDay) {
    this.dayOfTheMonth = dayOfTheMonth;
    this.timeOfTheDay = timeOfTheDay;
  }

  @Override
  public Date getNextEventDate(Date lastHarvestDate) {
    Calendar req = Calendar.getInstance();
    req.set(Calendar.DAY_OF_MONTH, dayOfTheMonth);
    req.set(Calendar.HOUR_OF_DAY, timeOfTheDay.getHours());
    req.set(Calendar.MINUTE, timeOfTheDay.getMinutes());
    req.set(Calendar.SECOND, 0);
    req.set(Calendar.MILLISECOND, 0);
    
    Calendar now = Calendar.getInstance();
    Calendar lhc = getCalendar(lastHarvestDate);
    if (now.after(req) && (lhc==null || req.after(lhc))) {
      return req.getTime();
    }
    while (now.after(req)) {
      req.add(Calendar.MONTH, 1);
    }
    return req.getTime();
  }

  @Override
  public String getLocalizedCaption(MessageBroker broker) {
    return broker.retrieveMessage(getClass().getCanonicalName(), new Object[]{Integer.toString(dayOfTheMonth), SDF.format(timeOfTheDay)});
  }

  @Override
  public String getCode() {
    return ""+dayOfTheMonth+","+SDF.format(timeOfTheDay);
  }
  
  @Override
  public String toString() {
    return ""+dayOfTheMonth+","+SDF.format(timeOfTheDay);
  }
}
