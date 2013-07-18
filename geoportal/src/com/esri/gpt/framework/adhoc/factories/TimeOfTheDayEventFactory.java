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
package com.esri.gpt.framework.adhoc.factories;

import com.esri.gpt.framework.adhoc.IAdHocEvent;
import com.esri.gpt.framework.adhoc.IAdHocEventFactory;
import com.esri.gpt.framework.adhoc.events.TimeOfTheDayEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Time of the day event factory.
 */
public class TimeOfTheDayEventFactory implements IAdHocEventFactory {
  private static final SimpleDateFormat SDF = new SimpleDateFormat("HH:mm");

  @Override
  public IAdHocEvent parse(String definition) {
    try {
      return new TimeOfTheDayEvent(SDF.parse(definition));
    } catch (ParseException ex) {
      return null;
    }
  }
  
}
