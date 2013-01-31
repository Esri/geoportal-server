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

import com.esri.gpt.catalog.harvest.repository.HrRecord.HarvestFrequency;
import com.esri.gpt.catalog.harvest.repository.HrRecord.RecentJobStatus;
import com.esri.gpt.control.webharvest.IterationContext;
import com.esri.gpt.control.webharvest.protocol.Protocol;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.request.Record;
import com.esri.gpt.framework.resource.query.QueryBuilder;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a metadata record associated with a manage metadata request.
 */
public class MmdRecord extends Record {

// class variables  =============================================================
// instance variables ==========================================================
  private String _approvalStatus = "";
  private String _approvalStatusMsg = "";
  private boolean _canEdit = false;
  private String _collectionMembership = "";
  private String _formattedUpdateDate = "";
  private String _ownerName = "";
  private String _publicationMethod;
  private String _publicationMethodMsg = "";
  private Timestamp _systemUpdateDate = null;
  private String _title = "";
  private String _uuid = "";
  private String _siteUuid = "";
  private String _metadataAccessPolicyType = "";
  private String _currentMetadataAccessPolicy = "";
  private String _currentMetadataAccessPolicyKeys = "";

  // resource specific attributes
  private int              _localId;
  private String           _hostUrl = "";
  private Protocol         _protocol;
  private HarvestFrequency _harvestFrequency;
  private boolean          _sendNotification;
  private Timestamp        _lastHarvestDate;
  private RecentJobStatus  _recentJobStatus;

  /** findable */
  private boolean findable;
  /** searchable */
  private boolean searchable;
  /** synchronizable */
  private boolean synchronizable;
  
  private Map<String, Object> _objectMap = new LinkedHashMap<String, Object>();

// constructors ================================================================
  /** Default constructor. */
  public MmdRecord() {
    super();
  }

// properties ==================================================================
 
  /**
  * Gets the object map.
  *
  * @return the object map (never null)
  */
  public Map<String, Object> getObjectMap() {
    if(_objectMap == null) {
      _objectMap = new LinkedHashMap<String, Object>();
    }
    return _objectMap;
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
    _approvalStatus = MmdEnums.ApprovalStatus.checkValue(status).toString();
  }

  /**
   * Gets the approval status resource bundle message.
   * @return the approval status message
   */
  public String getApprovalStatusMsg() {
    return _approvalStatusMsg;
  }

  /**
   * Sets the approval status resource bundle message.
   * @param msg the approval status message
   */
  public void setApprovalStatusMsg(String msg) {
    _approvalStatusMsg = Val.chkStr(msg);
  }

  /**
   * Gets the editable status for the document.
   * @return true if the document can be edited
   */
  public boolean getCanEdit() {
    return _canEdit;

  }

  /**
   * Sets the editable status for the document.
   * @param canEdit true if the document can be edited
   */
  protected void setCanEdit(boolean canEdit) {
    _canEdit = canEdit;
  }
  
  /**
   * Gets the collection membership string.
   * @return the collection membership 
   */
  public String getCollectionMembership() {
    return _collectionMembership;
  }
  /**
   * Sets the collection membership string.
   * @param membership the collection membership
   */
  public void setCollectionMembership(String membership) {
    _collectionMembership = Val.chkStr(membership);
  }

  /**
   * Gets the formatted update date.
   * @return the formatted update date
   */
  public String getFormattedUpdateDate() {
    return _formattedUpdateDate;
  }

  /**
   * Sets the formatted update date.
   * @param date the formatted update date
   */
  public void setFormattedUpdateDate(String date) {
    _formattedUpdateDate = Val.chkStr(date);
  }

  /**
   * Gets the document owner name (username).
   * @return the document owner name
   */
  public String getOwnerName() {
    return _ownerName;
  }

  /**
   * Sets the document owner name (username).
   * @param name the document owner name
   */
  public void setOwnerName(String name) {
    _ownerName = Val.chkStr(name);
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
    _publicationMethod = MmdEnums.PublicationMethod.checkValue(method).toString();
  }

  /**
   * Gets the publication method resource bundle message.
   * @return the publication method message
   */
  public String getPublicationMethodMsg() {
    return _publicationMethodMsg;
  }

  /**
   * Sets the publication method resource bundle message.
   * @param msg the publication method message
   */
  public void setPublicationMethodMsg(String msg) {
    _publicationMethodMsg = Val.chkStr(msg);
  }

  /**
   * Gets the system update date.
   * @return the system update date
   */
  public Timestamp getSystemUpdateDate() {
    return _systemUpdateDate;
  }

  /**
   * Sets the system update date.
   * @param date the system update date
   */
  public void setSystemUpdateDate(Timestamp date) {
    _systemUpdateDate = date;
  }

  /**
   * Gets the title.
   * @return the title
   */
  public String getTitle() {
    return _title;
  }

  /**
   * Sets the title.
   * @param title the title
   */
  public void setTitle(String title) {
    _title = Val.chkStr(title);
  }

  /**
   * Gets the document UUID.
   * @return the UUID
   */
  public String getUuid() {
    return _uuid;
  }

  /**
   * Sets the document UUID.
   * @param uuid the UUID
   */
  public void setUuid(String uuid) {
    _uuid = UuidUtil.addCurlies(uuid);
  }

  /**
   * Gets the site UUID.
   * @return the site UUID
   */
  public String getSiteUuid() {
    return _siteUuid;
  }

  /**
   * Sets the site UUID.
   * @param uuid site UUID
   */
  public void setSiteUuid(String uuid) {
    _siteUuid = UuidUtil.addCurlies(uuid);
  }

  /**
   * Gets the document Metadata Access policy type.
   * @return access policy type type
   */
  public String getMetadataAccessPolicyType() {
    return _metadataAccessPolicyType;
  }

  /**
   * Sets the document Metadata Access policy type.
   * @param metadataAccessPolicyType access policy type
   */
  public void setMetadataAccessPolicyType(String metadataAccessPolicyType) {
    this._metadataAccessPolicyType = metadataAccessPolicyType;
  }

  /**
   * Gets the document current access policy.
   * @return access policy
   */
  public String getCurrentMetadataAccessPolicy() {
    return _currentMetadataAccessPolicy;
  }

  /**
   * Sets the document current access policy.
   * @param currentMetadataAccessPolicy access policy
   */
  public void setCurrentMetadataAccessPolicy(String currentMetadataAccessPolicy) {
    this._currentMetadataAccessPolicy = currentMetadataAccessPolicy;
  }
  
  /**
   * Gets the document current access policy.
   * @return access policy keys
   */
  public String getCurrentMetadataAccessPolicyKeys() {
    return _currentMetadataAccessPolicyKeys;
  }

  /**
   * Sets the document current access policy.
   * @param currentMetadataAccessPolicyKeys access policy keys
   */
  public void setCurrentMetadataAccessPolicyKeys(String currentMetadataAccessPolicyKeys) {
    this._currentMetadataAccessPolicyKeys = currentMetadataAccessPolicyKeys;
  }

  /**
   * Gets local id.
   * @return local id
   */
  public int getLocalId() {
    return _localId;
  }

  /**
   * Sets local id.
   * @param localId local id
   */
  public void setLocalId(int localId) {
    this._localId = localId;
  }

  /**
   * Gets host URL.
   * @return host URL
   */
  public String getHostUrl() {
    return _hostUrl;
  }

  /**
   * Sets host URL.
   * @param hostUrl host URL
   */
  public void setHostUrl(String hostUrl) {
    this._hostUrl = Val.chkStr(hostUrl);
  }

  /**
   * Gets protocol.
   * @return protocol
   */
  public Protocol getProtocol() {
    return _protocol;
  }

  /**
   * Sets protocol.
   * @param harvestProtocol protocol
   */
  public void setProtocol(Protocol harvestProtocol) {
    this._protocol = harvestProtocol;
  }

  /**
   * Gets harvest frequency.
   * @return harvest frequency
   */
  public HarvestFrequency getHarvestFrequency() {
    return _harvestFrequency;
  }

  /**
   * Sets harvest frequency.
   * @param harvestFrequency harvest frequency
   */
  public void setHarvestFrequency(HarvestFrequency harvestFrequency) {
    this._harvestFrequency = harvestFrequency;
  }


  /**
   * Gets flag to check if send harvest notification.
   * @return flag to check if send harvest notification
   */
  public boolean getSendNotification() {
    return _sendNotification;
  }

  /**
   * Sets flag to check if send harvest notification.
   * @param sendNotification flag to check if send harvest notification
   */
  public void setSendNotification(boolean sendNotification) {
    this._sendNotification = sendNotification;
  }

  /**
   * Gets last harvest date.
   * @return last harvest date
   */
  public Timestamp getLastHarvestDate() {
    return _lastHarvestDate;
  }

  /**
   * Sets last harvest date.
   * @param lastHarvestDate last harvest date
   */
  public void setLastHarvestDate(Timestamp lastHarvestDate) {
    this._lastHarvestDate = lastHarvestDate;
  }

  /**
   * Gets recent job status.
   * @return recent job status
   */
  public RecentJobStatus getRecentJobStatus() {
    return _recentJobStatus;
  }

  /**
   * Checks if synchronization of this resource is being executed locally.
   * @return <code>true</code> if synchronization of this resource is being executed locally
   */
  public boolean isExecutingLocally() {
    return ApplicationContext.getInstance().getHarvestingEngine().isExecutingLocally(getUuid());
  }

  /**
   * Sets recent job status.
   * @param _recentJobStatus recent job status
   */
  public void setRecentJobStatus(RecentJobStatus _recentJobStatus) {
    this._recentJobStatus = _recentJobStatus;
  }

  public String getName() {
    return getTitle();
  }

  /**
   * Checks if record is findable.
   * @return <code>true</code> if record is findable
   */
  public boolean getFindable() {
    return findable;
  }

  /**
   * Sets record is findable.
   * @param findable <code>true</code> to make record is findable
   */
  public void setFindable(boolean findable) {
    this.findable = findable;
  }

  /**
   * Checks if records is synchronizable.
   * @return <code>true</code> if records is synchronizable
   */
  public boolean getSynchronizable() {
    return synchronizable;
  }

  /**
   * Sets records is synchronizable.
   * @param synchronizable <code>true</code> to make records is synchronizable
   */
  public void setSynchronizable(boolean synchronizable) {
    this.synchronizable = synchronizable;
  }

  /**
   * Checks if records is searchable.
   * @return <code>true</code> if records is searchable
   */
  public boolean getSearchable() {
    return searchable;
  }

  /**
   * Sets records is searchable.
   * @param searchable <ocde>true</code> to make records is searchable
   */
  public void setSearchable(boolean searchable) {
    this.searchable = searchable;
  }

  /**
   * Creates new query builder.
   * @param iterationContext iteration context (can be <code>null</code>)
   * @return query builder or <code>null</code> if no protocol
   */
  public QueryBuilder newQueryBuilder(IterationContext iterationContext) {
    if (iterationContext==null) {
      iterationContext = new IterationContext() {
          public void onIterationException(Exception ex) {
          }
      };
    }
    return getProtocol()!=null? getProtocol().newQueryBuilder(iterationContext, getHostUrl()): null;
  }
// methods =====================================================================
}
