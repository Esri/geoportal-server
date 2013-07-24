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

import com.esri.gpt.framework.adhoc.IAdHocEvent;
import java.util.Calendar;
import java.util.Date;

/**
 * Abstract ad-hoc event.
 */
public abstract class AdHocEvent implements IAdHocEvent {
  
  /**
   * Gets calendar for a date.
   * @param date date
   * @return calendar or <code>null</code> if date is <code>null</code>
   */
  protected Calendar getCalendar(Date date) {
    if (date!=null) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(date);
      return cal;
    } else {
      return null;
    }
  }
  
}
