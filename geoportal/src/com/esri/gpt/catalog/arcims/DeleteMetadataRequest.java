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
package com.esri.gpt.catalog.arcims;
import java.sql.SQLException;

import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.UuidUtil;

/**
 * Deletes a record by executing a request against an ArcIMS metadata publish service.
 */
public class DeleteMetadataRequest extends PublishServiceRequest {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public DeleteMetadataRequest() {}

/**
 * Constructs with an associated request context and publisher.
 * @param requestContext the request context
 * @param publisher the publisher
 */
public DeleteMetadataRequest(RequestContext requestContext,
                             Publisher publisher) {
  super(requestContext,publisher);
}

// properties ==================================================================

// methods =====================================================================
  
/**
 * Executes a DELETE_METADATA request against an ArcIMS metadata publish service.
 * @param docUuid the metatata document uuid of the record to delete
 * @return true if the action status was ok
 * @throws PublishServiceException  if an exception occurs
 */
public boolean executeDelete(String docUuid) throws ImsServiceException {
  docUuid = UuidUtil.addCurlies(docUuid);
  
  // check for the metadata server data access proxy, use if active
  ImsMetadataProxyDao proxy = new ImsMetadataProxyDao(this.getRequestContext(),this.getPublisher());
  try {
    int nDeleted = proxy.deleteRecord(this,docUuid);
    if (nDeleted>0) {
      setActionStatus(ACTION_STATUS_OK);
    }
    return wasActionOK();
  } catch (SQLException e) {
    throw new ImsServiceException(e.toString(),e);
  }
}

}

