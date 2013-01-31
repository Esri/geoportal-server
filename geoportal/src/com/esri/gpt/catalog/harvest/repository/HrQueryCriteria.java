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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocol;
import com.esri.gpt.framework.request.QueryCriteria;
import com.esri.gpt.framework.util.DateRange;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Harvest repository search criteria.
 */
public class HrQueryCriteria extends QueryCriteria {

// class variables =============================================================

// instance variables ==========================================================
/** Harvest protocol type. */
private HarvestProtocol.ProtocolType _protocolType =
  HarvestProtocol.ProtocolType.None;
/** Repository name. */
private String _name = "";
/** Host name. */
private String _hostUrl = "";
/** Update date range. */
private DateRange _dateRange = new DateRange();
/** Repository uuid. */
private String _uuid = "";
/** Repository local id. */
private String _localId = "";
/** Last harvest date range. */
private DateRange _lastHarvestDateRange = new DateRange();
/** Due only. */
private boolean _dueOnly;

// constructors ================================================================

// properties ==================================================================
/**
 * Gets protocol type.
 * @return protocol type
 */
public HarvestProtocol.ProtocolType getProtocolType() {
  return _protocolType;
}

/**
 * Sets protocol type.
 * @param protocolType protocol type.
 */
public void setProtocolType(HarvestProtocol.ProtocolType protocolType) {
  _protocolType = protocolType;
}

/**
 * Gets protocol type as string.
 * @return protocol type as string
 */
public String getProtocolTypeAsString() {
  return getProtocolType().name().toLowerCase();
}

/**
 * Sets protocol type as string.
 * @param name protocol type as string
 */
public void setProtocolTypeAsString(String name) {
  setProtocolType(HarvestProtocol.ProtocolType.checkValueOf(name));
}

/**
 * Gets repository name.
 * @return repository name
 */
public String getName() {
  return _name;
}

/**
 * Sets repository name.
 * @param name repository name
 */
public void setName(String name) {
  _name = Val.chkStr(name);
}

/**
 * Gets host name.
 * @return hostUrl name
 */
public String getHost() {
  return _hostUrl;
}

/**
 * Sets host url.
 * @param hostUrl hostUrl url
 */
public void setHost(String hostUrl) {
  _hostUrl = Val.chkStr(hostUrl);
}

/**
 * Gets date range.
 * @return date range
 */
public DateRange getDateRange() {
  return _dateRange;
}

/**
 * Sets update date range.
 * @param dateRange update date range
 */
public void setDateRange(DateRange dateRange) {
  _dateRange = dateRange != null ? dateRange : new DateRange();
}

/**
 * Gets date range.
 * @return date range
 */
public DateRange getLastHarvestDateRange() {
  return _lastHarvestDateRange;
}

/**
 * Sets update date range.
 * @param dateRange update date range
 */
public void setLastHarvestDateRange(DateRange dateRange) {
  _lastHarvestDateRange = dateRange != null ? dateRange : new DateRange();
}

/**
 * Gets uuid.
 * @return uuid
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets uuid.
 * @param uuid uuid
 */
public void setUuid(String uuid) {
  _uuid = UuidUtil.isUuid(uuid) ? uuid : "";
}

/**
 * Gets local id.
 * @return local id
 */
public String getLocalId() {
  return _localId;
}

/**
 * Sets local id.
 * @param localId local id
 */
public void setLocalId(String localId) {
  _localId = Val.chkStr(localId);
}

/**
 * Get due only flag.
 * @return <code>true</code> to search records due now only
 */
public boolean getDueOnly() {
  return _dueOnly;
}

/**
 * Set due only flag.
 * @param dueOnly <code>true</code> to search records due now only
 */
public void setDueOnly(boolean dueOnly) {
  _dueOnly = dueOnly;
}

// methods =====================================================================
}
