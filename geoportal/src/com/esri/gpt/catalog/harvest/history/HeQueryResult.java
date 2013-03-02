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

import com.esri.gpt.catalog.harvest.protocols.HarvestProtocol;
import com.esri.gpt.framework.request.QueryResult;
import com.esri.gpt.framework.util.Val;

/**
 * Harvest repository history query result.
 */
public class HeQueryResult extends QueryResult<HeRecords> {

// class variables =============================================================

// instance variables ==========================================================
/** Harvest repository protocol type. */
private String _protocolType = HarvestProtocol.ProtocolType.None.name();
/** Harvest repository name. */
private String _name = "";
/** Harvest repository url. */
private String _url = "";
/** Harvest repository uuid. */
private String _uuid = "";
/** Harvest report uuid. */
private String _reportUuid = "";

// constructors ================================================================
/** Non-argument constructor. */
public HeQueryResult() {
  super(new HeRecords());
}

// properties ==================================================================
/**
 * Gets protocol type.
 * @return the protocol type
 */
public HarvestProtocol.ProtocolType getProtocolType() {
  return HarvestProtocol.ProtocolType.checkValueOf(_protocolType);
}

/**
 * Sets protocol type.
 * @param protocolType the protocol type
 */
public void setProtocolType(HarvestProtocol.ProtocolType protocolType) {
  _protocolType = protocolType.name();
}

/**
 * Gets protocol type as string.
 * @return the protocol type name
 */
public String getProtocolTypeAsString() {
  return _protocolType;
}

/**
 * Sets protocol type from string.
 * @param name protocol type name
 */
public void setProtocolTypeAsString(String name) {
  _protocolType = Val.chkStr(name);
}

/**
 * Gets protocol type as string.
 * @return the protocol type name
 */
public String getProtocolIdAsString() {
  return getProtocolType().getId();
}

/**
 * Sets protocol type from string.
 * @param name protocol type name
 */
public void setProtocolIdAsString(String name) {
  _protocolType = Val.chkStr(_protocolType);
}

/**
 * Gets harvest repository name.
 * @return harvest repository name
 */
public String getName() {
  return _name;
}

/**
 * Sets harvest repository url.
 * @param name repository name
 */
public void setName(String name) {
  _name = Val.chkStr(name);
}

/**
 * Gets harvest repository url.
 * @return harvest repository url
 */
public String getUrl() {
  return _url;
}

/**
 * Sets harvest repository url.
 * @param url repository url
 */
public void setUrl(String url) {
  _url = Val.chkStr(url);
}

/**
 * Gets repository uuid.
 * @return repository uuid
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets repository uuid.
 * @param uuid repository uuid
 */
public void setUuid(String uuid) {
  _uuid = Val.chkStr(uuid);
}

/**
 * Gets report uuid.
 * @return report uuid
 */
public String getReportUuid() {
  return _reportUuid;
}

/**
 * Sets report uuid.
 * @param reportUuid report uuid
 */
public void setReportUuid(String reportUuid) {
  _reportUuid = Val.chkStr(reportUuid);
}

// methods =====================================================================
}
