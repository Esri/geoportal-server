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
package com.esri.gpt.catalog.management;

import com.esri.gpt.catalog.arcims.DeleteMetadataRequest;
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.arcims.ImsServiceException;
import com.esri.gpt.catalog.arcims.TransferOwnershipRequest;
import com.esri.gpt.catalog.context.CatalogIndexException;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.request.ActionResult;
import com.esri.gpt.framework.security.metadata.MetadataAcl;
import com.esri.gpt.framework.security.principal.Groups;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Executes metadata management actions.
 * <p>
 * Actions keys:
 * <li>setPosted = Set as Posted</li>
 * <li>setApproved = Set as Approved</li>
 * <li>setIncomplete = Set as Incomplete</li>
 * <li>setDisapproved = Set as Disapproved</li>
 * <li>setReviewed = Set as Reviewed</li>
 * <li>transfer = Transfer Ownership</li>
 * <li>delete = Delete</li>
 * <li>shareWith = Share with a collection</li>
 * <li>dontShareWith = Don't share with a collection</li>
 */
public class MmdActionRequest extends MmdRequest {

// class variables =============================================================

// instance variables ==========================================================
private boolean hadUnalteredDraftDocuments = false;
private boolean _applyToAll = false;

// constructors ================================================================
/**
 * Construct a metadata management query request.
 * @param requestContext the request context
 * @param publisher the publisher
 * @param criteria the request criteria
 * @param result the request result
 */
public MmdActionRequest(RequestContext requestContext, Publisher publisher,
    MmdCriteria criteria, MmdResult result) {
  super(requestContext, publisher, criteria, result);
}

// properties ==================================================================

/**
 * Gets the status indicating whether or not documents in draft mode were unaltered by an update.
 * <br/>The approval status of a document in draft mode can only be altered by publishing
 * the document from the online editor.
 * @return true if draft documents were unaltered
 */
public boolean hadUnalteredDraftDocuments() {
  return this.hadUnalteredDraftDocuments;
}

// methods =====================================================================

/**
 * Gets flag if apply action to all records.
 * @return flag if apply action to all records
 */
public boolean getApplyToAll() {
  return _applyToAll;
}

/**
 * Sets flag if apply action to all records.
 * @param applyToAll flag if apply action to all records
 */
public void setApplyToAll(boolean applyToAll) {
  this._applyToAll = applyToAll;
}

/**
 * Executes the action request.
 * @throws ImsServiceException in an exception occurs while communication
 *         with the ArcIMS metadata publishing service
 * @throws Exception if an exception occurs
 */
public void execute() throws Exception {
  int nRows = 0;
  String sAction = getActionCriteria().getActionKey();
  StringSet uuids = getActionCriteria().getSelectedRecordIdSet();
  ImsMetadataAdminDao adminDao = new ImsMetadataAdminDao(getRequestContext());

  // check for approval status updates
  if (sAction.equalsIgnoreCase("setPosted")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),uuids,MmdEnums.ApprovalStatus.posted);
  } else if (sAction.equalsIgnoreCase("setApproved")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),uuids,MmdEnums.ApprovalStatus.approved);
  } else if (sAction.equalsIgnoreCase("setIncomplete")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),uuids,MmdEnums.ApprovalStatus.incomplete);
  } else if (sAction.equalsIgnoreCase("setDisapproved")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),uuids,MmdEnums.ApprovalStatus.disapproved);
  } else if (sAction.equalsIgnoreCase("setReviewed")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),uuids,MmdEnums.ApprovalStatus.reviewed);
  }
  getActionResult().setNumberOfRecordsModified(nRows);
  this.hadUnalteredDraftDocuments = adminDao.hadUnalteredDraftDocuments();

  // check for access policy updates
  if (sAction.equalsIgnoreCase("setPolicyPrivate")) {
  } else if (sAction.equalsIgnoreCase("setPolicyPublic")) {
  }
  getActionResult().setNumberOfRecordsModified(nRows);

  // check for a delete request
  if (sAction.equalsIgnoreCase("delete")) {
    executeDelete(adminDao, uuids);
  }

  // check for an ownership transfer request
  if (sAction.equalsIgnoreCase("transfer")) {
    String sNewOwner = getActionCriteria().getTransferToOwner();
    if (sNewOwner.length() > 0) {
      Publisher newOwner = new Publisher(getRequestContext(), sNewOwner);
      String sNewOwnerName = newOwner.getName();
      String sNewFolderUuid = newOwner.getFolderUuid();
      executeTransfer(adminDao, uuids, sNewOwnerName, sNewFolderUuid);
    }
  }

  // check for an assign Acl request
  if (sAction.equalsIgnoreCase("assignAcl")) {
    ArrayList<String> acl = getActionCriteria().getMetadataAccessPolicy();
    Groups groups = Publisher.buildSelectableGroups(getRequestContext());
    // if (acl != null && acl.size() > 0) {
    executeAssignAcl(adminDao, uuids, groups, acl);
    // }
  }
  
  // check collection sharing (isPartOf)
  if (sAction.equalsIgnoreCase("shareWith") || sAction.equalsIgnoreCase("dontShareWith") ) {
    String colUuid = Val.chkStr(getActionCriteria().getSharingCollectionUuid());
    this.executeAssignCollection(sAction,colUuid,uuids);
  }
  
}

/**
 * Executes request for all records matching given query criteria.
 * @param queryCriteria query criteria
 * @throws Exception if executing request fails
 */
public void execute(MmdQueryCriteria queryCriteria) throws Exception {
  int nRows = 0;
  String sAction = getActionCriteria().getActionKey();
  ImsMetadataAdminDao adminDao = new ImsMetadataAdminDao(getRequestContext());

  // check for approval status updates
  if (sAction.equalsIgnoreCase("setPosted")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),queryCriteria,MmdEnums.ApprovalStatus.posted);
  } else if (sAction.equalsIgnoreCase("setApproved")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),queryCriteria,MmdEnums.ApprovalStatus.approved);
  } else if (sAction.equalsIgnoreCase("setIncomplete")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),queryCriteria,MmdEnums.ApprovalStatus.incomplete);
  } else if (sAction.equalsIgnoreCase("setDisapproved")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),queryCriteria,MmdEnums.ApprovalStatus.disapproved);
  } else if (sAction.equalsIgnoreCase("setReviewed")) {
    nRows = adminDao.updateApprovalStatus(getPublisher(),queryCriteria,MmdEnums.ApprovalStatus.reviewed);
  }
  this.hadUnalteredDraftDocuments = adminDao.hadUnalteredDraftDocuments();

  // check for a delete request
  if (sAction.equalsIgnoreCase("delete")) {
    nRows = adminDao.deleteRecord(getPublisher(), queryCriteria);
  }

  // check for an ownership transfer request
  if (sAction.equalsIgnoreCase("transfer")) {
    String sNewOwner = getActionCriteria().getTransferToOwner();
    if (sNewOwner.length() > 0) {
      Publisher newOwner = new Publisher(getRequestContext(), sNewOwner);
      nRows = adminDao.transferOwnership(getPublisher(), queryCriteria, newOwner.getLocalID());
    }
  }

  // check for an assign Acl request
  if (sAction.equalsIgnoreCase("assignAcl")) {
    ArrayList<String> selectedGroups = getActionCriteria().getMetadataAccessPolicy();
    Groups groups = Publisher.buildSelectableGroups(getRequestContext());
    if (selectedGroups != null && selectedGroups.size() > 0) {
      MetadataAcl acl = new MetadataAcl(getRequestContext());
      nRows = adminDao.updateAcl(getPublisher(), queryCriteria, acl.buildAclGroups(groups, selectedGroups));
    } else {
      nRows = adminDao.updateAcl(getPublisher(), queryCriteria, null);
    }
  }

  getActionResult().setNumberOfRecordsModified(nRows);
}

/**
 * Share or unshare with a collection.
 * @param option the option ("shareWith" or "dontShareWith")
 * @param colUuid the collection UUID
 * @param uuids the set of document UUID's
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
private void executeAssignCollection(String option, String colUuid, StringSet uuids) 
  throws SQLException, CatalogIndexException {
  ActionResult result = getActionResult();
  if (colUuid.length() > 0) {
    CollectionDao colDao = new CollectionDao(this.getRequestContext());
    if (option.equalsIgnoreCase("shareWith")) {
      int nMod = colDao.addMembers(getPublisher(),uuids,colUuid);
      result.setNumberOfRecordsModified(nMod);
    } else if (option.equalsIgnoreCase("dontShareWith")) {
      int nMod = colDao.removeMembers(getPublisher(),uuids,colUuid);
      result.setNumberOfRecordsModified(nMod);
    }
  }
}

/**
 * Deletes a selected set of metadata records.
 * @param adminDao the administrative table DAO
 * @param uuids the set of UUID's to delete
 * @throws ImsServiceException in an exception occurs while communication
 *         with the ArcIMS metadata publishing service
 * @throws SQLException if a database exception occurs
 * @throws CatalogIndexException if a document indexing exception occurs
 */
private void executeDelete(ImsMetadataAdminDao adminDao, StringSet uuids)
    throws ImsServiceException, SQLException, CatalogIndexException {
  ActionResult result = getActionResult();
  DeleteMetadataRequest imsRequest;
  imsRequest = new DeleteMetadataRequest(getRequestContext(), getPublisher());
  for (String sUuid : uuids) {
    boolean bOk = imsRequest.executeDelete(sUuid);
    if (bOk) {
      int nMod = (result.getNumberOfRecordsModified() + 1);
      result.setNumberOfRecordsModified(nMod);
    }
  }
}

/**
 * Transfers a selected set of metadata records to a new owner.
 * @param adminDao the administrative table dao
 * @param uuids the set of uuids to transfer
 * @param newOwnerName the new owner name
 * @param newFolderUuid the new folder UUID
 * @throws ImsServiceException in an exception occurs while communication
 *         with the ArcIMS metadata publishing service
 * @throws SQLException if a database exception occurs
 */
private void executeTransfer(ImsMetadataAdminDao adminDao, StringSet uuids,
    String newOwnerName, String newFolderUuid) 
  throws ImsServiceException, SQLException {
  ActionResult result = getActionResult();
  TransferOwnershipRequest imsRequest;
  if ((newOwnerName.length() > 0) && (newFolderUuid.length() > 0)) {
    imsRequest = new TransferOwnershipRequest(getRequestContext(),getPublisher());
    for (String sUuid : uuids) {
      boolean bOk = imsRequest.executeTransfer(sUuid,newOwnerName,newFolderUuid);
      if (bOk) {
        int nMod = (result.getNumberOfRecordsModified() + 1);
        result.setNumberOfRecordsModified(nMod);
      }
    }
  }
}

/**
 * Assign acl to a selected set of metadata records
 * @param adminDao the administrative table dao
 * @param ArrayList<String> acl
 * @throws ImsServiceException in an exception occurs while communication
 *         with the ArcIMS metadata publishing service
 * @throws SQLException if a database exception occurs
 */
private void executeAssignAcl(ImsMetadataAdminDao adminDao, StringSet uuids,
    Groups groups, ArrayList<String> selectedGroups)
  throws ImsServiceException, SQLException, CatalogIndexException {
  ActionResult result = getActionResult();
  MetadataAcl acl = null;
  if (selectedGroups != null && selectedGroups.size() > 0) {
    if ((uuids.size() > 0) && (groups.size() > 0)) {
      acl = new MetadataAcl(getRequestContext());
      result.setNumberOfRecordsModified(adminDao.updateAcl(getPublisher(),
          uuids, acl.buildAclGroups(groups, selectedGroups)));
    } else if ((uuids.size() > 0) && (selectedGroups.size() > 0)) {
      if (selectedGroups.get(0).equalsIgnoreCase("unrestricted")) {
        result.setNumberOfRecordsModified(adminDao.updateAcl(getPublisher(),uuids, null));
      } else {
        StringBuilder sbAclXml = new StringBuilder();
        sbAclXml.append("<acl>");
        sbAclXml.append("<principal type=\"groupDn\">");
        sbAclXml.append(selectedGroups.get(0));
        sbAclXml.append("</principal>");
        sbAclXml.append("</acl>");
        result.setNumberOfRecordsModified(adminDao.updateAcl(getPublisher(),uuids, sbAclXml.toString()));
      }
    }
  } else {
    result.setNumberOfRecordsModified(adminDao.updateAcl(getPublisher(),uuids,null));
  }
}
}
