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

import com.esri.gpt.catalog.harvest.clients.exceptions.HRConnectionException;
import com.esri.gpt.catalog.harvest.clients.exceptions.HRInvalidProtocolException;
import com.esri.gpt.framework.context.RequestContext;

/**
 * Provides connection test request functionality.
 * @deprecated replaced with {@link com.esri.gpt.control.webharvest.validator.ValidatorFactory}
 */
@Deprecated
public class HrTestRequest extends HrRequest {

// class variables =============================================================

// instance variables ==========================================================
/** Repository to update. */
private HrRecord _repository = new HrRecord();

// constructors ================================================================

/**
 * Create instance of the request.
 * @param requestContext request context
 * @param record record to test
 */
public HrTestRequest(RequestContext requestContext, HrRecord record) {
  super(requestContext, new HrCriteria(), new HrResult());
  setRepository(record);
}

// properties ==================================================================

/**
 * Gets repository to update.
 * @return repository to update
 */
public HrRecord getRepository() {
  return _repository;
}

/**
 * Sets repository to update.
 * @param repository repository to update
 */
public void setRepository(HrRecord repository) {
  _repository = repository!=null? repository: new HrRecord();
}

// methods =====================================================================

/**
 * Executes request.
 * @throws HRInvalidProtocolException when protocol attributes are invalid
 * @throws HRConnectionException if connecting remote repository failed
 */
public void execute() 
  throws HRInvalidProtocolException, HRConnectionException {
  getRepository().checkConnection();
}

}
