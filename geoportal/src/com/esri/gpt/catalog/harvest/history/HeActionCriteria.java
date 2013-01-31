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
package com.esri.gpt.catalog.harvest.history;

import com.esri.gpt.framework.request.ActionCriteria;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Harvest repository history action criteria.
 */
public class HeActionCriteria extends ActionCriteria {

// class variables =============================================================

// instance variables ==========================================================
/** Uuid of the selected harvest repository. */
private String _uuid = "";
/** uuid of the selected event with report to view. */
private String _eventUuid = "";
/** Harvest history action. */
private HistoryAction _action = HistoryAction.Unknown;

// constructors ================================================================

// properties ==================================================================
/**
 * Gets selected repository uuid.
 * @return selected repository uuid
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets selected repository uuid.
 * @param uuid selected repository uuid
 */
public void setUuid(String uuid) {
  _uuid = UuidUtil.isUuid(uuid) ? uuid : "";
}

/**
 * Gets history action.
 * @return the history action
 */
public HistoryAction getAction() {
  return _action;
}

/**
 * Sets history action.
 * @param action the history action
 */
public void setAction(HistoryAction action) {
  _action = action;
}

/**
 * Gets history action as string.
 * @return the history action name
 */
public String getActionAsString() {
  return getAction().name().toLowerCase();
}

/**
 * Sets history action from string.
 * @param name the name of history action
 */
public void setActionAsString(String name) {
  setAction(HistoryAction.checkValueOf(name));
}

/**
 * Gets uuid of the selected report to view.
 * @return the uuid of the selected report to view
 */
public String getEventUuid() {
  return _eventUuid;
}

/**
 * Sets uuid of the selected report to view.
 * @param eventUuid the uuid of the selected report to view
 */
public void setEventUuid(String eventUuid) {
  _eventUuid = UuidUtil.isUuid(eventUuid)? eventUuid: "";
}

// methods =====================================================================

// types =======================================================================
/**
 * Harvest history actions.
 */
public enum HistoryAction {

/** View report. */
ViewReport,
/** Delete report. */
Delete,
/** Unknown action. */
Unknown;

/**
 * Checks history action name.
 * @param name the history action name
 * @return the history action
 */
public static HistoryAction checkValueOf(String name) {
  name = Val.chkStr(name);
  for (HistoryAction ha: values()) {
    if (ha.name().equalsIgnoreCase(name)) {
      return ha;
    }
  }
  return HistoryAction.Unknown;
}
}
}
