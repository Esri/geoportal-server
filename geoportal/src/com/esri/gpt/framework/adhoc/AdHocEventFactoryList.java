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

import com.esri.gpt.framework.adhoc.factories.DayOfTheMonthEventFactory;
import com.esri.gpt.framework.adhoc.factories.DayOfTheWeekEventFactory;
import com.esri.gpt.framework.adhoc.factories.DayOfTheWeekInTheMonthEventFactory;
import com.esri.gpt.framework.adhoc.factories.SpecTimeEventFactory;
import com.esri.gpt.framework.adhoc.factories.TimeOfTheDayEventFactory;
import com.esri.gpt.framework.util.Val;
import java.text.ParseException;
import java.util.ArrayList;

/**
 * Ad-hoc event factory list.
 */
public class AdHocEventFactoryList extends ArrayList<IAdHocEventFactory>  {
  {
    add(new SpecTimeEventFactory());
    add(new DayOfTheWeekInTheMonthEventFactory());
    add(new DayOfTheWeekEventFactory());
    add(new DayOfTheMonthEventFactory());
    add(new TimeOfTheDayEventFactory());
  }
  
  private static final AdHocEventFactoryList instance = new AdHocEventFactoryList();
  
  /**
   * Gets singleton instance of the factory list.
   * @return factory list
   */
  public static AdHocEventFactoryList getInstance() {
    return instance;
  }
  
  /**
   * Parses collection of ad-hoc event definitions.
   * @param definition ad-hoc event definition.
   * @return list of ad-hoc events
   * @throws ParseException if parsing failed
   */
  public AdHocEventList parse(String definition) throws ParseException {
    AdHocEventList list = new AdHocEventList();
    definition = Val.chkStr(definition);
    if (!definition.isEmpty()) {
      String[] parts = definition.split("\\|");
      for (String part: parts) {
        list.add(parseSingle(part));
      }
    }
    return list;
  }
  
  /**
   * Parses single as-hoc event definition.
   * @param definition definition
   * @return event
   * @throws ParseException if parsing failed
   */
  public IAdHocEvent parseSingle(String definition) throws ParseException {
    for (IAdHocEventFactory factory: this) {
      IAdHocEvent event = factory.parse(definition);
      if (event!=null) {
        return event;
      }
    }
    throw new ParseException("Invalid event definition: "+definition, 0);
  }
}
