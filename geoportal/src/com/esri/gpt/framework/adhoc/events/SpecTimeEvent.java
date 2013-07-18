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
 * Specific time ad-hoc event.
 */
public class SpecTimeEvent extends AdHocEvent {
  private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
  private Date specTime;
  
  /**
   * Creates instance of the event.
   * @param specTime specific time
   */
  public SpecTimeEvent(Date specTime) {
    this.specTime = specTime;
  }

  @Override
  public Date getNextEventDate(Date lastHarvestDate) {
    Date now = new Date();
    Calendar lhc = getCalendar(lastHarvestDate);
    Calendar stc = Calendar.getInstance();
    stc.setTime(specTime);
    if (now.after(specTime) && (lhc==null || stc.after(lhc))) {
      return specTime;
    }
    if (now.before(specTime)) {
      return specTime;
    }
    return null;
  }

  @Override
  public String getLocalizedCaption(MessageBroker broker) {
    return broker.retrieveMessage(getClass().getCanonicalName(), new Object[]{specTime.toString()});
  }

  @Override
  public String getCode() {
    return SDF.format(specTime);
  }
  
  @Override
  public String toString() {
    return SDF.format(specTime);
  }
}
