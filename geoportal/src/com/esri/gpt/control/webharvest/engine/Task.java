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
package com.esri.gpt.control.webharvest.engine;

import com.esri.gpt.catalog.harvest.jobs.HjRecord;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.control.webharvest.common.CommonCriteria;
import com.esri.gpt.framework.resource.query.Criteria;

/**
 * Task.
 */
public class Task {

/** resource to harvest */
private HrRecord resource;
/** criteria */
private Criteria criteria = new CommonCriteria();

/**
 * Creates instance of the task.
 * @param resource resource
 * @param criteria criteria
 */
public Task(HrRecord resource, Criteria criteria) {
  if (resource==null) {
    throw new IllegalArgumentException("No resource provided.");
  }
  this.resource = resource;
  this.criteria = criteria!=null? criteria: new CommonCriteria();
}

/**
 * Creates instance of the task.
 * @param record job record
 */
public Task(HjRecord record) {
  this(record.getHarvestSite(), record.getCriteria());
}

/**
 * Gets resource to harvest.
 * @return resource
 */
public HrRecord getResource() {
  return resource;
}

/**
 * Gets criteria.
 * @return criteria
 */
public Criteria getCriteria() {
  return criteria;
}
}
