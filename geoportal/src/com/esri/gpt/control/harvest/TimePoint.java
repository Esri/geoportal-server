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
package com.esri.gpt.control.harvest;

import com.esri.gpt.framework.adhoc.IAdHocEvent;

/**
 * Time point.
 */
public class TimePoint {
  private IAdHocEvent event;
  private String caption;
  
  /**
   * Creates instance of time point.
   * @param event event
   * @param caption caption
   */
  public TimePoint(IAdHocEvent event, String caption) {
    this.event = event;
    this.caption = caption;
  }
  
  /**
   * Gets event code.
   * @return event code
   */
  public String getCode() {
    return event.toString();
  }
  
  /**
   * Gets caption.
   * @return caption
   */
  public String getCaption() {
    return caption;
  }
  
  /**
   * Gets event.
   * @return event
   */
  public IAdHocEvent getEvent() {
    return event;
  }
}
