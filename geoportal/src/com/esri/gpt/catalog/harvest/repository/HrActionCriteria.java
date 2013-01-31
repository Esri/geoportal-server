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
package com.esri.gpt.catalog.harvest.repository;

import com.esri.gpt.framework.request.ActionCriteria;
import com.esri.gpt.framework.util.DateProxy;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.sql.Timestamp;
import java.util.Date;

/**
 * Harvest repository action criteria.
 */
public class HrActionCriteria extends ActionCriteria {

// class variables =============================================================
// instance variables ==========================================================
/** Uuid of repository to edit. */
private String _uuid = "";
/** Harvest repository action. */
private RepositoryAction _action = RepositoryAction.Unknown;
/** harvest type */
private String _harvestType = "full";
/** from date */
private String _fromDate = "";
/** max recs */
private String _maxRecs = "";

// constructors ================================================================
// properties ==================================================================
/**
 * Gets uuid of repository to edit.
 * @return uuid of repository to edit
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets uuid of repository to edit.
 * @param uuid uuid of repository to edit
 */
public void setUuid(String uuid) {
  _uuid = UuidUtil.isUuid(uuid) ? uuid : "";
}

/**
 * Gets repository action.
 * @return repository action
 */
public RepositoryAction getAction() {
  return _action;
}

/**
 * Sets repository action.
 * @param action repository action
 */
public void setAction(RepositoryAction action) {
  _action = action;
}

/**
 * Gets action as string.
 * @return action name
 */
public String getActionAsString() {
  return getAction().name().toLowerCase();
}

/**
 * Sets action as string.
 * @param action action name
 */
public void setActionAsString(String action) {
  setAction(RepositoryAction.checkValueOf(action));
}

/**
 * Gets harvest type.
 * @return harvest type
 */
public String getHarvestType() {
  return _harvestType;
}

/**
 * Set sharvest type.
 * @param harvestType harvest type
 */
public void setHarvestType(String harvestType) {
  this._harvestType = Val.chkStr(harvestType);
}

/**
 * Gets from date as date object.
 * @return from date or <code>null</code> if date invalid
 */
public Date getFromDateAsDate() {
  Timestamp timestamp = null;
  DateProxy dp = new DateProxy();
  dp.setDate(getFromDate());
  if (dp.getIsValid()) {
    timestamp = dp.asFromTimestamp();
  }
  return timestamp;
}

/**
 * Gets from date.
 * @return from date
 */
public String getFromDate() {
  return _fromDate;
}

/**
 * Sets from date.
 * @param fromDate from date
 */
public void setFromDate(String fromDate) {
  this._fromDate = Val.chkStr(fromDate);
}

/**
 * Gets max recs.
 * @return max recs
 */
public String getMaxRecs() {
  return _maxRecs;
}

/**
 * Sets max recs.
 * @param maxRecs max recs
 */
public void setMaxRecs(String maxRecs) {
  this._maxRecs = Val.chkStr(maxRecs);
}

// methods =====================================================================
// types =======================================================================
/**
 * Harvest repository action.
 */
public enum RepositoryAction {

/** Create new repository. */
Create,
/** Edit selected repository. */
Edit,
/** Delete selected repositories. */
Delete,
/** View harvest history. */
History,
/**
 * Synchronize catalog.
 */
Synchronize,
/**
 * Cancel synchronization.
 */
Cancel,
/** Unknown action. */
Unknown;

/**
 * Checks repository action.
 * @param name repository actio name
 * @return repository action
 */
public static RepositoryAction checkValueOf(String name) {
  name = Val.chkStr(name);
  for (RepositoryAction ra : values()) {
    if (ra.name().equalsIgnoreCase(name)) {
      return ra;
    }
  }
  return Create;
}
}
}
