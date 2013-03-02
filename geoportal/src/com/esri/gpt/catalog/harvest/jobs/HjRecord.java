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
package com.esri.gpt.catalog.harvest.jobs;

import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.catalog.harvest.repository.HrRecord.RecentJobStatus;
import com.esri.gpt.control.webharvest.common.CommonCriteria;
import com.esri.gpt.framework.request.Record;
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.util.Date;

/**
 * Harvest job information.
 * @see HjRecords
 */
public class HjRecord extends Record {

// class variables =============================================================
// instance variables ==========================================================
/** Harvest repository. */
private HrRecord _repository;
/** Job unique id. */
private String _uuid = "";
/** Input date. */
private Date _inputDate = new Date(0);
/** Job date. */
private Date _jobDate = new Date(0);
/** Job type. */
private JobType _type = JobType.Full;
/** Job status. */
private JobStatus _status = JobStatus.Submited;
/** Service id. */
private String _serviceId = "";
/** criteria */
private CommonCriteria criteria = new CommonCriteria();

// constructors ================================================================
/**
 * Creates instance of harvest job.
 * @param harvestRepository harvest repository.
 */
public HjRecord(HrRecord harvestRepository) {
  _repository = harvestRepository;
}

// properties ==================================================================
/**
 * Gets job uuid.
 * @return job uuid.
 */
public String getUuid() {
  return _uuid;
}

/**
 * Sets job uuid.
 * @param uuid job uuid.
 */
/*default*/ void setUuid(String uuid) {
  _uuid = UuidUtil.isUuid(uuid) ? uuid : "";
}

/**
 * Gets job date.
 * @return job date.
 */
public Date getInputDate() {
  return _inputDate;
}

/**
 * Sets job date.
 * @param inputDate job date.
 */
/* package */ void setInputDate(Date inputDate) {
  _inputDate = inputDate != null ? inputDate : new Date(0);
}

/**
 * Gets job date.
 * @return job date.
 */
public Date getJobDate() {
  return _jobDate;
}

/**
 * Sets job date.
 * @param inputDate job date.
 */
/* package */ void setJobDate(Date jobDate) {
  _jobDate = jobDate != null ? jobDate : new Date(0);
}

/**
 * Gets harvest site.
 * @return harvest site.
 */
public HrRecord getHarvestSite() {
  return _repository;
}

/**
 * Gets job type.
 * @return job type.
 */
public JobType getType() {
  return _type;
}

/**
 * Sets job type.
 * @param jobType job type.
 */
public void setType(JobType jobType) {
  this._type = jobType;
}

/**
 * Gets job status.
 * @return job status.
 */
public JobStatus getStatus() {
  return _status;
}

/**
 * Sets job status.
 * @param jobStatus job status.
 */
public void setStatus(JobStatus jobStatus) {
  _status = jobStatus;
}

// methods =====================================================================
/**
 * Creates string representation of harvest job.
 * @return string representation of harvest job.
 */
@Override
public String toString() {
  StringBuilder sb = new StringBuilder();

  sb.append("Uuid:" + _uuid);
  sb.append(" protocol:" + _repository.getProtocol().getKind());
  sb.append(" host:" + _repository.getHostUrl());
  sb.append(" date:" + _jobDate);
  sb.append(" type:" + _type.toString());
  sb.append(" status" + _status.toString());

  return sb.toString();
}

/**
 * Gets service id.
 * @return service id
 */
public String getServiceId() {
  return _serviceId;
}

/**
 * Sets service id.
 * @param serviceId service id
 */
public void setServiceId(String serviceId) {
  _serviceId = Val.chkStr(serviceId);
}

/**
 * Gets criteria.
 * @return criteria
 */
public CommonCriteria getCriteria() {
  return criteria;
}

/**
 * Sets criteria.
 * @param criteria criteria
 */
public void setCriteria(CommonCriteria criteria) {
  this.criteria = criteria!=null? criteria: new CommonCriteria();
}

// custom types ================================================================
/**
 * Job type.
 */
public enum JobType {

/** Full harvest. */
Full,
/** Harvest now. */
Now;

/**
 * Checks type.
 * @param name type name.
 * @return type or <code>none</code> if unknown type.
 */
public static JobType checkValueOf(String name) {
  name = Val.chkStr(name);
  for (JobType t : values()) {
    if (t.name().equalsIgnoreCase(name)) {
      return t;
    }
  }
  LogUtil.getLogger().severe("Invalid JobType value: " + name);
  return JobType.Full;
}
}

/**
 * Harvest job status.
 */
public enum JobStatus {

/** Job has just been submited. */
Submited(RecentJobStatus.Submited),
/** Job is running (currently being processed). */
Running(RecentJobStatus.Running),
/** Job has been completed. */
Completed(RecentJobStatus.Completed),
/** Job has been canceled. */
Canceled(RecentJobStatus.Canceled);
/** Recent job status. */
private RecentJobStatus _recentJobStatus;

JobStatus(RecentJobStatus recentJobStatus) {
  _recentJobStatus = recentJobStatus;
}

/**
 * Gets recent job status association.
 * @return recent job status association
 */
public RecentJobStatus getRecentJobStatus() {
  return _recentJobStatus;
}

/**
 * Checks status.
 * @param name status name.
 * @return status or <code>Submited</code> if unknown status.
 */
public static JobStatus checkValueOf(String name) {
  name = Val.chkStr(name);
  for (JobStatus s : values()) {
    if (s.name().equalsIgnoreCase(name)) {
      return s;
    }
  }
  LogUtil.getLogger().severe("Invalid JobStatus value: " + name);
  return JobStatus.Submited;
}

/**
 * Checks value of status.
 * @param recentJobStatus recent job status
 * @return job status
 */
public static JobStatus checkValueOf(RecentJobStatus recentJobStatus) {
  for (JobStatus s : values()) {
    if (s.getRecentJobStatus() == recentJobStatus) {
      return s;
    }
  }
  throw new IllegalArgumentException("Invalid JobStatus: " + recentJobStatus);
}
}
}
