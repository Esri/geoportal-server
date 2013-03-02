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

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.framework.request.Record;
import com.esri.gpt.framework.util.UuidUtil;
import java.util.Date;

/**
 * Historical information about harvesting.
 * @see HeRecords
 */
public class HeRecord extends Record {

// class variables =============================================================
  
// instance variables ==========================================================

/** Harvest repository. */
private HrRecord _repository;
/** Uuid. */
private String _uuid = "";
/** This harvest date. */
private Date _harvestDate = new Date(0);
/** Harvested documents number. */
private int _harvestedCount;
/** Validated documents number. */
private int _validatedCount;
/** Published documents number. */
private int _publishedCount;
/** Deleted documents number.*/
private long _deletedCount;

// constructors ================================================================

/**
 * Creates instance of harvest event.
 * @param harvestRepository harvest repository
 */
public HeRecord(HrRecord harvestRepository) {
  _repository = harvestRepository;
}

// properties ==================================================================

/**
 * Gets harvest event record uuid.
 * @return uuid
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets harvest event record uuid.
 * @param uuid uuid
 */
/* default */ void setUuid(String uuid) {
  _uuid = UuidUtil.isUuid(uuid)? uuid: "";
}

/**
 * Gets harvest site.
 * @return harvest site
 */
public HrRecord getRepository() {
  return _repository;
}

/**
 * Gets harvest date.
 * @return harvest date
 */
public Date getHarvestDate() {
  return _harvestDate;
}

/**
 * Sets harvest date.
 * <p/>
 * If the harvest date passed as an argument is <code>null</code>, then harvest 
 * date will be set to current date.
 * @param harvestDate harvest date
 */
/*default*/ void setHarvestDate(Date harvestDate) {
  _harvestDate = harvestDate!=null ? harvestDate : new Date(0);
}

// methods =====================================================================

/**
 * Creates string representation of harvest event.
 * @return string representation of harvest event
 */
@Override
public String toString() {
  StringBuilder sb = new StringBuilder();

  sb.append("Uuid:").append(_uuid);
  sb.append(" harvest date:").append(_harvestDate);
  sb.append(" protocol:").append(_repository.getProtocol().getKind());
  sb.append(" url:").append(_repository.getHostUrl());
  sb.append(" #harvested:").append(_harvestedCount);
  sb.append(" #validated:").append(_validatedCount);
  sb.append(" #published:").append(_publishedCount);
  sb.append(" #deleted:").append(_deletedCount);

  return sb.toString();
}

/**
 * Gets harvested documents count.
 * @return  harvested documents count
 */
public int getHarvestedCount() {
  return _harvestedCount;
}

/**
 * Sets harvested documents count.
 * @param harvested harvested documents count
 */
public void setHarvestedCount(int harvested) {
  _harvestedCount = harvested;
}

/**
 * Gets validated documents count.
 * @return validated documents count
 */
public int getValidatedCount() {
  return _validatedCount;
}

/**
 * Sets validated documents count.
 * @param validated validated documents count
 */
public void setValidatedCount(int validated) {
  _validatedCount = validated;
}

/**
 * Gets published documents count.
 * @return published documents count
 */
public int getPublishedCount() {
  return _publishedCount;
}

/**
 * Sets published documents count.
 * @param published published documents count
 */
public void setPublishedCount(int published) {
  _publishedCount = published;
}

/**
 * Gets deleted documents count.
 * @return deleted documents count
 */
public long getDeletedCount() {
  return _deletedCount;
}

/**
 * Sets deleted documents count.
 * @param deleted deleted documents count
 */
public void setDeletedCount(long deleted) {
  this._deletedCount = deleted;
}

}