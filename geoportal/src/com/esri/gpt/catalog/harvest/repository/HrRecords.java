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

import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.framework.request.Records;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.util.Date;

/**
 * Collection of harvest repository data.
 * @see HrRecord
 */
public class HrRecords extends Records<HrRecord> {

// class variables =============================================================
  
// instance variables ==========================================================
  
// constructors ================================================================
  
// properties ==================================================================

// methods =====================================================================

/**
 * Gets all UUID's.
 * @return array of UUID's
 */  
public String [] getUuids() {
  String [] uuids = new String[size()];
  for (int i=0; i<size(); i++) {
    uuids[i] = get(i).getUuid();
  }
  return uuids;
}

/**
 * Looks for harvest repository.
 * @param uuid repository id
 * @return harvest repository or <code>null</code> if repository not found
 */  
public HrRecord findByUuid(String uuid) {
  for (HrRecord hr : this) {
    if (hr.getUuid().equals(Val.chkStr(uuid))) {
      return hr;
    }
  }
  return null;
}

/**
 * Finds selected harvest repositories.
 * @return collection of selected harvest repositories
 */
public HrRecords findSelected() {
  return findByCriteria(new ISearchCriteria(){
    @Override
    public boolean qualified(HrRecord record) {
      return record.getIsSelected();
    }
  });
}

/**
 * Finds saved/non-saved harvest repositories.
 * @param saved <code>true</code> to search for saved harvest repositories
 * @return collection of saved/non-saved harvest repositories
 */
public HrRecords findSaved(final boolean saved) {
  return findByCriteria(new ISearchCriteria(){
    @Override
    public boolean qualified(HrRecord record) {
      return UuidUtil.isUuid(record.getUuid())==saved;
    }
  });
}

/**
 * Finds all records for which harvest is due now.
 * @return collection of records for which harvest is due now
 */
public HrRecords findHarvestDue() {
  return findByCriteria(new ISearchCriteria(){
    @Override
    public boolean qualified(HrRecord record) {
      return record.getIsHarvestDue() && record.getApprovalStatus()==ApprovalStatus.approved && record.getSynchronizable();
    }
  });
}

/**
 * Finds the closest next record to harvest in the near future.
 * @return record to harvest in the near future or <code>null</code> if no such a record
 */
public HrRecord findNextDue() {
  Date now = new Date();
  HrRecord nextDue = null;
  for (HrRecord record : this) {
    if ((record.getApprovalStatus()== ApprovalStatus.approved || record.getApprovalStatus()== ApprovalStatus.reviewed) && record.getSynchronizable()) {
      Date nextHarvestDate = record.getNextHarvestDate();
      if (nextHarvestDate!=null && nextHarvestDate.after(now)) {
        if (nextDue==null || nextDue.getNextHarvestDate().after(nextHarvestDate)) {
          nextDue = record;
        }
      }
    }
  }
  return nextDue;
}

/**
 * Sets <i>selected</i> flag on all harvest repositories.
 * @param selected <i>selected</i> flag value
 */
public void setAllSelected(final boolean selected) {
  for (HrRecord record : this) {
    record.setIsSelected(selected);
  }
}

/**
 * Find criteria.
 */
private interface ISearchCriteria  {
  boolean qualified(HrRecord record);
}

/**
 * Finds harvest repositories by criteria.
 * @param criteria search criteria
 * @return collection of harvest repositories matching criteria
 */
private HrRecords findByCriteria(ISearchCriteria criteria) {
  HrRecords records = new HrRecords();
  
  for (HrRecord record : this) {
    if (criteria.qualified(record)) {
      records.add(record);
    }
  }
  
  return records;
}

}
