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
package com.esri.gpt.framework.adhoc;

import java.util.ArrayList;
import java.util.Date;

/**
 * Ad-hoc event list.
 */
public class AdHocEventList extends ArrayList<IAdHocEvent> {
  /**
   * Gets next harvest date.
   * @param lastHarvestDate last harvest date
   * @return next harvest date or <code>null</code> if no next harvest date
   */
  public Date getNextHarvestDate(Date lastHarvestDate) {
    Date nextHarvestDate = null;
    for (IAdHocEvent evt: this) {
      Date date = evt.getNextEventDate(lastHarvestDate);
      if (date!=null) {
        if (nextHarvestDate==null || date.before(nextHarvestDate)) {
          nextHarvestDate = date;
        }
      }
    }
    return nextHarvestDate;
  }
  
  /**
   * Gets events codes.
   * @return event codes
   */
  public String getCodes() {
    StringBuilder sb = new StringBuilder();
    for (IAdHocEvent evt: this) {
      if (sb.length()>0) {
        sb.append("|");
      }
      sb.append(evt.getCode());
    }
    return sb.toString();
  }
  
  @Override
  public String toString() {
    return getCodes();
  }
}
