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
import com.esri.gpt.framework.util.Val;

/**
 * Transfers metadata ownership by executing requests against an ArcIMS
 * metadata publish service.
 * <p>
 * Ownership is transfered, and the document is moved to the folder 
 * of the new owner.
 */
public class TransferOwnershipRequest extends PublishServiceRequest {

// class variables =============================================================

// instance variables ==========================================================

// constructors ================================================================

/** Default constructor. */
public TransferOwnershipRequest() {}

/**
 * Constructs with an associated request context and publisher.
 * @param requestContext the request context
 * @param publisher the publisher
 */
public TransferOwnershipRequest(RequestContext requestContext,
                                Publisher publisher) {
  super(requestContext,publisher);
}

// properties ==================================================================

// methods =====================================================================

/**
 * Adds a relationship by executing a PUT_METADATA_RELATIONSHIP request
 * against an ArcIMS metadata publish service.
 * @param docUuid the metatata document uuid
 * @param folderUuid the containing folder id
 * @return true if the action status was ok
 * @throws PublishServiceException is an exception occurs
 */
private boolean executeAddRelationship(String docUuid, String folderUuid)
  throws ImsServiceException {

  // make the axl request
  folderUuid = UuidUtil.addCurlies(folderUuid);
  docUuid    = UuidUtil.addCurlies(docUuid);
  StringBuffer sbAxl =  new StringBuffer();
  sbAxl = new StringBuffer();
  sbAxl.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  sbAxl.append("\r\n<ARCXML version=\"1.1\">");
  sbAxl.append("\r\n<REQUEST>");
  sbAxl.append("\r\n<PUBLISH_METADATA>");
  sbAxl.append("\r\n<PUT_METADATA_RELATIONSHIP>");
  sbAxl.append("\r\n<METADATA_SOURCE docid=\"").append(folderUuid).append("\"/>");
  sbAxl.append("\r\n<METADATA_CHILD docid=\"").append(docUuid).append("\"/>");
  sbAxl.append("\r\n</PUT_METADATA_RELATIONSHIP>");
  sbAxl.append("\r\n</PUBLISH_METADATA>");
  sbAxl.append("\r\n</REQUEST>");
  sbAxl.append("\r\n</ARCXML>");
  setAxlRequest(sbAxl.toString());

  // execute the request, return the status
  executeRequest();
  return wasActionOK();
}

/**
 * Executes a CHANGE_OWNER request against an ArcIMS metadata publish service.
 * @param docUuid the metatata document uuid of the record for which
 *        ownsership will be changed
 * @param newOwner the new owner for the record
 * @return true if the action status was ok
 * @throws PublishServiceException  if an exception occurs
 */
private boolean executeChangeOwner(String docUuid, String newOwner)
  throws ImsServiceException {

  // make the axl request
  docUuid  = UuidUtil.addCurlies(docUuid);
  newOwner = Val.escapeXml(Val.chkStr(newOwner));
  StringBuffer sbAxl =  new StringBuffer();
  sbAxl = new StringBuffer();
  sbAxl.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  sbAxl.append("\r\n<ARCXML version=\"1.1\">");
  sbAxl.append("\r\n<REQUEST>");
  sbAxl.append("\r\n<PUBLISH_METADATA>");
  sbAxl.append("\r\n<CHANGE_OWNER docid=\"").append(docUuid).append("\"");
  sbAxl.append(" newowner=\"").append(newOwner).append("\"/>");
  sbAxl.append("\r\n</PUBLISH_METADATA>");
  sbAxl.append("\r\n</REQUEST>");
  sbAxl.append("\r\n</ARCXML>");
  setAxlRequest(sbAxl.toString());

  // execute the request, return the status
  executeRequest();
  return wasActionOK();
}

/**
 * Removes parant relationships by executing a DELETE_METADATA_RELATIONSHIP request
 * against an ArcIMS metadata publish service.
 * @param docUuid the metatata document uuid
 * @param folderUuid the containing folder id
 * @return true if the action status was ok
 * @throws PublishServiceException is an exception occurs
 */
private boolean executeDeleteParent(String docUuid, String folderUuid)
  throws ImsServiceException {

  // make the axl request
  folderUuid = UuidUtil.addCurlies(folderUuid);
  docUuid = UuidUtil.addCurlies(docUuid);
  StringBuffer sbAxl =  new StringBuffer();
  sbAxl = new StringBuffer();
  sbAxl.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
  sbAxl.append("\r\n<ARCXML version=\"1.1\">");
  sbAxl.append("\r\n<REQUEST>");
  sbAxl.append("\r\n<PUBLISH_METADATA>");
  sbAxl.append("\r\n<DELETE_METADATA_RELATIONSHIP>");
  sbAxl.append("\r\n<METADATA_SOURCE docid=\"").append(folderUuid).append("\"/>");
  sbAxl.append("\r\n<METADATA_CHILD docid=\"").append(docUuid).append("\"/>");
  sbAxl.append("\r\n</DELETE_METADATA_RELATIONSHIP>");
  sbAxl.append("\r\n</PUBLISH_METADATA>");
  sbAxl.append("\r\n</REQUEST>");
  sbAxl.append("\r\n</ARCXML>");
  setAxlRequest(sbAxl.toString());

  // execute the request, return the status
  executeRequest();
  return wasActionOK();
}

/**
 * Transfers ownsership for a metadata document.
 * @param docUuid the metatata document uuid of the record for which ownsership will be changed
 * @param newOwner the new owner for the record
 * @param newFolderUuid the folder id for the new owner
 * @return true if the action status was ok
 * @throws PublishServiceException  if an exception occurs
 */
public boolean executeTransfer(String docUuid,
                               String newOwner,
                               String newFolderUuid)
  throws ImsServiceException {
  
  // check for the metadata server data access proxy, use if active
  ImsMetadataProxyDao proxy = new ImsMetadataProxyDao(this.getRequestContext(),this.getPublisher());
  try {
    proxy.transferOwnership(this,docUuid,newOwner);
    return wasActionOK();
  } catch (SQLException e) {
    throw new ImsServiceException(e.toString(),e);
  }
}

}
