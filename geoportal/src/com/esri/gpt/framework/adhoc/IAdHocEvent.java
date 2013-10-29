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

import com.esri.gpt.framework.jsf.MessageBroker;
import java.util.Date;

/**
 * Ad-hoc event.
 */
public interface IAdHocEvent {
  /**
   * Gets event code.
   * @return event code
   */
  String getCode();
  /**
   * Gets next event date.
   * @param lastEventDate last event date
   * @return next harvest date or <code>null</code> if no next event date
   */
  Date getNextEventDate(Date lastEventDate);
  /**
   * Gets localized caption.
   * @param broker message broker
   * @return caption
   */
  String getLocalizedCaption(MessageBroker broker);
}
