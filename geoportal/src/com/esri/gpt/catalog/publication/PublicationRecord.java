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
package com.esri.gpt.catalog.publication;
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;

/**
 * Describes the basic properties for a metadata document that is about to be
 * published.
 */
public class PublicationRecord {

// class variables =============================================================

// instance variables ==========================================================
private boolean _autoApprove = false;
private String  _approvalStatus = "";
private String  _fileIdentifier = "";
private String  _publicationMethod;
private String  _sourceFileName = "";
private String  _siteUuid = "";
private String  _sourceUri = "";
private String  _sourceXml = "";
private boolean _updateOnlyIfXmlHasChanged = true;
private String  _uuid = "";
private boolean _wasDocumentReplaced = false;
private boolean _wasDocumentUnchanged = false;
private String  _alternativeTitle = "";
private boolean _lockTitle;
private boolean _indexEnabled = true;

// constructors ================================================================
  
/** Default constructor. */
public PublicationRecord() {}

// properties ==================================================================

/**
 * Gets the status indicating if a new document should be automatically approved.
 * @return true if a new document should be automatically approved
 */
public boolean getAutoApprove() {
  return _autoApprove;
}
/**
 * Sets the status indicating if a new document should be automatically approved.
 * @param autoApprove true if a new document should be automatically approved
 */
public void setAutoApprove(boolean autoApprove) {
  _autoApprove = autoApprove;
}

/**
 * Gets the approval status.
 * @return the approval status
 */
public String getApprovalStatus() {
  return _approvalStatus;
}

/**
 * Sets the approval status.
 * @param status the approval status
 */
public void setApprovalStatus(String status) {
  status = Val.chkStr(status);
  _approvalStatus = MmdEnums.ApprovalStatus.valueOf(status).toString();
}

/**
 * Gets the file identifier.
 * <br/>The file identifier is typically associated with ISO-19139 documents.
 * @return the file identifier
 */
public String getFileIdentifier() {
  return _fileIdentifier;
}
/**
 * Sets the file identifier.
 * <br/>The file identifier is typically associated with ISO-19139 documents.
 * @param id the file identifier
 */
public void setFileIdentifier(String id) {
  _fileIdentifier = Val.chkStr(id);
}

/**
 * Gets the publication method.
 * @return the publication method
 */
public String getPublicationMethod() {
  return _publicationMethod;
}
/**
 * Sets the publication method.
 * @param method the publication method
 */
public void setPublicationMethod(String method) {
  _publicationMethod = method;
}

/**
 * Gets the source file name (applicable for uploaded files).
 * @return the source file name
 */
public String getSourceFileName() {
  return _sourceFileName;
}
/**
 * Sets the source file name (applicable for uploaded files).
 * @param fileName the source file name
 */
public void setSourceFileName(String fileName) {
  _sourceFileName = Val.chkStr(fileName);
}

/**
 * Gets harvest site UUID.
 * @return the harvest site UUID
 */
public String getSiteUuid() {
  return _siteUuid;
}

/**
 * Sets harvest site UUID.
 * @param siteUuid harvest site UUID
 */
public void setSiteUuid(String siteUuid) {
  _siteUuid = UuidUtil.isUuid(siteUuid)? siteUuid: "";
}

/**
 * Gets the source URI for the document.
 * @return the source URI
 */
public String getSourceUri() {
  return _sourceUri;
}
/**
 * Sets the source URI for the document.
 * @param uri the source URI
 */
public void setSourceUri(String uri) {
  _sourceUri = Val.chkStr(uri,4000);
}

/**
 * Gets the source XML string.
 * @return the source XML string
 */
public String getSourceXml() {
  return _sourceXml;
}
/**
 * Sets the source XML string.
 * @param xml the source XML string
 */
public void setSourceXml(String xml) {
  _sourceXml = Val.chkStr(Val.removeBOM(xml));
}

/**
 * Gets the status indicating if a document should be updated only if the XML has changed.
 * @return true if a document should be updated only if the XML has changed
 */
public boolean getUpdateOnlyIfXmlHasChanged() {
  return _updateOnlyIfXmlHasChanged;
}
/**
 * Sets the status indicating if a document should be updated only if the XML has changed.
 * @param onlyIfChanged true if a document should be updated only if the XML has changed
 */
public void setUpdateOnlyIfXmlHasChanged(boolean onlyIfChanged) {
  _updateOnlyIfXmlHasChanged = onlyIfChanged;
}

/**
 * Gets the UUID for the document.
 * @return the UUID
 */
public String getUuid() {
  return _uuid;
}
/**
 * Sets the UUID for the document.
 * @param uuid the UUID
 */
public void setUuid(String uuid) {
  _uuid = UuidUtil.addCurlies(uuid);
}

/**
 * Gets the status indicating if a document was replaced within the catalog.
 * @return true if the document was replaced
 */
public boolean getWasDocumentReplaced() {
  return _wasDocumentReplaced;
}
/**
 * Sets the status indicating if a document was replaced within the catalog.
 * @param wasReplaced true if the document was replaced
 */
protected void setWasDocumentReplaced(boolean wasReplaced) {
  _wasDocumentReplaced = wasReplaced;
}

/**
 * Gets the status indicating if a document was unchanged 
 * (no XML change, no database update tool place).
 * @return true if the document was unchanged
 */
public boolean getWasDocumentUnchanged() {
  return _wasDocumentUnchanged;
}
/**
 * Sets the status indicating if a document was unchanged 
 * (no XML change, no database update tool place).
 * @param wasUnchanged true if the document was unchanged
 */
protected void setWasDocumentUnchanged(boolean wasUnchanged) {
  _wasDocumentUnchanged = wasUnchanged;
}

/**
 * Gets alternative title.
 * This is a title provided by the user (not extracted from the resource).
 * Used only when dealing with registered resource. If set it overrides possible
 * title from the resource.
 * @return alternative title
 */
public String getAlternativeTitle() {
  return _alternativeTitle;
}

/**
 * Sets alternative title.
 * This is a title provided by the user (not extracted from the resource).
 * Used only when dealing with registered resource. If set it overrides possible
 * title from the resource.
 * @param title alternative title
 */
public void setAlternativeTitle(String title) {
  this._alternativeTitle = Val.chkStr(title);
}

/**
 * Checks if updating title is enabled.
 * Used only when dealing with registered resources.
 * @return <code>true</code> if updating title is enabled
 */
public boolean getLockTitle() {
  return _lockTitle;
}

/**
 * Enables updating title.
 * Used only when dealing with registered resources.
 * @param lockTitle <code>true</code> to enable updating title
 */
public void setLockTitle(boolean lockTitle) {
  this._lockTitle = lockTitle;
}

/**
 * Checks if index creation is enabled. Default: <code>true</code>.
 * @return <code>true</code> if index creation is enabled
 */
public boolean getIndexEnabled() {
  return _indexEnabled;
}

/**
 * Enables index creation. Default: <code>true</code>.
 * @param indexEnabled <code>true</code> to enable index creation
 */
public void setIndexEnabled(boolean indexEnabled) {
  this._indexEnabled = indexEnabled;
}

// methods =====================================================================

}