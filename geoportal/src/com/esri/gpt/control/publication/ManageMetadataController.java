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
package com.esri.gpt.control.publication;

import com.esri.gpt.catalog.arcgis.metadata.AGSProcessorConfig;
import com.esri.gpt.catalog.harvest.jobs.HjRecord;
import com.esri.gpt.catalog.harvest.repository.HrCriteria;
import com.esri.gpt.catalog.harvest.repository.HrHarvestRequest;
import com.esri.gpt.catalog.harvest.repository.HrResult;
import com.esri.gpt.catalog.management.CollectionDao;
import com.esri.gpt.catalog.management.MmdActionCriteria;
import com.esri.gpt.catalog.management.MmdActionRequest;
import com.esri.gpt.catalog.management.MmdActionResult;
import com.esri.gpt.catalog.management.MmdCriteria;
import com.esri.gpt.catalog.management.MmdQueryCriteria;
import com.esri.gpt.catalog.management.MmdQueryRequest;
import com.esri.gpt.catalog.management.MmdQueryResult;
import com.esri.gpt.catalog.management.MmdRecord;
import com.esri.gpt.catalog.management.MmdResult;
import com.esri.gpt.control.view.PageCursorPanel;
import com.esri.gpt.control.view.SelectablePublishers;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactories;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.factories.AgpProtocolFactory;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.BaseActionListener;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.identity.local.SimpleIdentityAdapter;
import com.esri.gpt.framework.security.metadata.MetadataAccessPolicy;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;
import java.util.ArrayList;
import javax.faces.component.UIComponent;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;

/**
 * Handles actions related to metadata management.
 */
public class ManageMetadataController extends BaseActionListener {

// class variables =============================================================

// instance variables ==========================================================
private MmdCriteria           _criteria;
private PageCursorPanel       _pageCursorPanel;
private MmdResult             _result;
private SelectableCollections _selectableCollections;
private SelectablePublishers  _selectablePublishers;
private SelectableGroups      _candidateGroups;
private MetadataAccessPolicy  _metadataAccessPolicyConfig;
private MmdQueryCriteria      _queryCriteriaForAction = new MmdQueryCriteria();
private boolean               _useCollections = false;

// constructors ================================================================
/** Default constructor. */
public ManageMetadataController() {
  super();
  setResult(new MmdResult());


  // initialize the selectablables
  _selectablePublishers = new SelectablePublishers();
  _candidateGroups = new SelectableGroups();
  _selectableCollections = new SelectableCollections();

  // initialize the page cursor panel
  String sExpression = "#{ManageMetadataController.processAction}";
  _pageCursorPanel = new PageCursorPanel();
  _pageCursorPanel.setActionListenerExpression(sExpression);
  _pageCursorPanel.setPageCursor(getQueryResult().getPageCursor());
}

// properties ==================================================================
/**
 * Gets list of candidate groups
 * @return the list of candidate groups
 */
public SelectableGroups getCandidateGroups() {
  return _candidateGroups;
}

/**
 * Sets list of candidate groups
 * @param candidateGroups the list of candidate groups
 */
public void setCandidateGroups(SelectableGroups candidateGroups) {
  this._candidateGroups = candidateGroups;
}

/**
 * Gets access policy configuration
 * @return access policy configuration
 */
public MetadataAccessPolicy getMetadataAccessPolicyConfig() {
  return _metadataAccessPolicyConfig;
}

/**
 * Sets access policy configuration.
 * @param metadataAccessPolicyConfig access policy configuration
 */
public void setAccessPolicyConfig(
    MetadataAccessPolicy metadataAccessPolicyConfig) {
  this._metadataAccessPolicyConfig = metadataAccessPolicyConfig;
}

/**
 * Gets the action criteria.
 * @return the action criteria
 */
public MmdActionCriteria getActionCriteria() {
  return getCriteria().getActionCriteria();
}

/**
 * Gets the action result.
 * @return the action result
 */
private MmdActionResult getActionResult() {
  return getResult().getActionResult();
}

/**
 * Determine if apply to all is allowable.
 * @return <code>true</code> if apply to all is allowable
 */
public boolean getAllowApplyToAll() {
  RequestContext context = this.getContextBroker().extractRequestContext();
  StringAttributeMap params = context.getCatalogConfiguration().getParameters();
  String s = Val.chkStr(params.getValue("catalog.admin.allowApplyToAll"));
  return !s.equalsIgnoreCase("false");
}

/**
 * Returns a false condition in support of the check-all control on the manage metadata page.
 * @return false
 */
public boolean getAlwaysFalse() {
  return false;
}

/**
 * Accepts a boolean in support of the check-all control on the manage metadata page.
 * @param ignored this value is ignored (the state of this control is not preserved)
 */
public void setAlwaysFalse(boolean ignored) {
}

/**
 * Gets the criteria for a manage metadata request.
 * @return the criteria
 */
public MmdCriteria getCriteria() {
  return _criteria;
}

/**
 * Sets the criteria for a manage metadata request.
 * @param criteria the criteria
 */
public void setCriteria(MmdCriteria criteria) {
  _criteria = criteria;
}

/**
 * Gets the panel for displaying the page cursor.
 * @return the page cursor panel
 */
public PageCursorPanel getPageCursorPanel() {
  return _pageCursorPanel;
}

/**
 * Gets the query criteria.
 * @return the query criteria
 */
public MmdQueryCriteria getQueryCriteria() {
  return getCriteria().getQueryCriteria();
}

/**
 * Gets the query result.
 * @return the query result
 */
public MmdQueryResult getQueryResult() {
  return getResult().getQueryResult();
}

/**
 * Gets the maximum number of records to be displayed per page.
 * @return the maximum number of records per page
 */
public int getRecordsPerPage() {
  return getQueryResult().getPageCursor().getRecordsPerPage();
}

/**
 * Sets the maximum number of records to be displayed per page.
 * @param recordsPerPage the maximum number of records per page
 */
public void setRecordsPerPage(int recordsPerPage) {
  getQueryResult().getPageCursor().setRecordsPerPage(recordsPerPage);
}

/**
 * Gets the result of a manage metadata request.
 * @return the result
 */
private MmdResult getResult() {
  return _result;
}

/**
 * Sets the result of a manage metadata request.
 * @param result the result
 */
private void setResult(MmdResult result) {
  _result = result;
}

/**
 * Gets list of selectable collections.
 * @return the list of selectable collections
 */
public SelectableCollections getSelectableCollections() {
  return _selectableCollections;
}

/**
 * Gets list of selectable publishers.
 * @return the list of selectable publishers
 */
public SelectablePublishers getSelectablePublishers() {
  return _selectablePublishers;
}

/**
 * Determine if collections are in use.
 * @return <code>true</code> if collections are in use
 */
public boolean getUseCollections() {
  RequestContext context = this.getContextBroker().extractRequestContext();
  CollectionDao colDao = new CollectionDao(context);
  return colDao.getUseCollections();
}

/**
 * Check if document access policy type is unrestricted
 * @return <code>true</code> if policy is unrestricted
 */
public boolean isPolicyUnrestricted() {
	if(getCriteria().getActionCriteria().getMetadataAccessPolicyType() != null){
		return getCriteria().getActionCriteria().getMetadataAccessPolicyType()
      .equalsIgnoreCase(MetadataAccessPolicy.TYPE_UNRESTRICTED);
	}
	return false;
}

// methods =====================================================================
/**
 * Executes an action against a set of metadata records.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @param actionCriteria the criteria for the action
 * @param publisher the publisher
 * @param applyToAll <code>true</code> to apply action to current set
 * @throws Exception if an exception occurs
 */
private void executeAction(ActionEvent event, final RequestContext context,
    MmdActionCriteria actionCriteria, Publisher publisher, boolean applyToAll) throws Exception {
  MessageBroker msgBroker = extractMessageBroker();

  // check to ensure that records were selected
  if (actionCriteria.getSelectedRecordIdSet().size() == 0 && applyToAll==false) {
    msgBroker.addErrorMessage("catalog.publication.manageMetadata.action.err.noneSelected");
  } else {

    // check the publisher
    Publisher publisherForAction = publisher;
    if (!publisher.getIsAdministrator()) {
      String sOwner = getQueryCriteria().getOwner();
      if (sOwner.length() == 0) {
        sOwner = publisher.getKey();
      }
      if (!sOwner.equalsIgnoreCase(publisher.getKey())) {
        if (!Publisher.buildSelectablePublishers(context, true).containsKey(sOwner)) {
          throw new NotAuthorizedException("Not authorized.");
        } else {
          publisherForAction = new Publisher(context, sOwner);
        }
      }
    }

    // execute the request
    MmdCriteria criteria = new MmdCriteria();
    criteria.setActionCriteria(actionCriteria);

    if (_metadataAccessPolicyConfig==null) {
      getSelectablePublishers().build(context, true);
      prepareAccessPolicyConfig(context);
      prepareGroups(context);
      prepareActionCriteria(context);
    }
    
    String defaultGroup = _metadataAccessPolicyConfig.getAccessToGroupDN();
    if (defaultGroup != null && defaultGroup.trim().length() > 0) {
      ArrayList<String> defaultAcl = new ArrayList<String>();
      defaultAcl.add(getActionCriteria().getToggleMetadataAccessPolicy());
      criteria.getActionCriteria().setMetadataAccessPolicy(defaultAcl);
    }

    MmdActionRequest request = new MmdActionRequest(context,publisherForAction,criteria,getResult());
    if (!applyToAll) {
      request.execute();
    } else {
      if (!_queryCriteriaForAction.getIsEmpty()) {
        request.execute(_queryCriteriaForAction);
      }
    }

    // set the success message
    int nModified = getActionResult().getNumberOfRecordsModified();
    if (nModified > 0) {
      String sKey = "catalog.publication.manageMetadata.action.success";
      if (actionCriteria.getActionKey().equalsIgnoreCase("delete")) {
        sKey += ".delete";
      }
      String[] args = new String[1];
      args[0] = "" + nModified;
      msgBroker.addSuccessMessage(sKey, args);
    }
    if (request.hadUnalteredDraftDocuments()) {
      msgBroker.addErrorMessage("catalog.publication.manageMetadata.action.err.draftUnaltered");
    }
  }
}

/**
 * Executes a search for metadata records.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @param publisher the publisher
 * @throws Exception if an exception occurs
 */
private void executeSearch(ActionEvent event, RequestContext context,
    Publisher publisher) throws Exception {
  MessageBroker msgBroker = extractMessageBroker();

  // check the publisher
  if (!publisher.getIsAdministrator()) {
    if (getQueryCriteria().getOwner().length() == 0) {
      getQueryCriteria().setOwner(publisher.getKey());
    }
    String sOwner = getQueryCriteria().getOwner();
    if (!sOwner.equalsIgnoreCase(publisher.getKey())) {
      if (!Publisher.buildSelectablePublishers(context, true).containsKey(sOwner)) {
        getQueryCriteria().setOwner(publisher.getKey());
      }
    }
  }

  // execute the request
  MmdQueryRequest request;
  request = new MmdQueryRequest(context, publisher, getCriteria(), getResult());
  request.execute();
  
  // determine if collections are in use
  CollectionDao colDao = new CollectionDao(context);
  boolean useCollections = colDao.getUseCollections();

  // set the resource messages for the results
  String sMsg;
  String sValue;
  for (MmdRecord record : request.getQueryResult().getRecords()) {

    // lookup approval status
    sValue = record.getApprovalStatus();
    sMsg = msgBroker.retrieveMessage("catalog.publication.manageMetadata.status."+sValue);
    record.setApprovalStatusMsg(sMsg);

    // lookup publication method
    sValue = record.getPublicationMethod();
    sMsg = msgBroker.retrieveMessage("catalog.publication.manageMetadata.method."+sValue);
    record.setPublicationMethodMsg(sMsg);
    
    // collection membership
    if (useCollections) {
      sValue = Val.chkStr(record.getCollectionMembership());
      if (sValue.length() > 0) {
        Object[] p = new String[]{sValue};
        sMsg = msgBroker.retrieveMessage("catalog.publication.manageMetadata.sharing.collection.popup",p);
        record.setCollectionMembership(sMsg);
      }
    }
  }
}

/**
 * Executes synchronization.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @param actionCriteria the criteria for the action
 * @throws Exception if an exception occurs
 */
private void executeSynchronization(ActionEvent event, RequestContext context, MmdActionCriteria actionCriteria) throws Exception {
  StringSet uuids = actionCriteria.getSelectedRecordIdSet();
  String[] aUuids = uuids.toArray(new String[uuids.size()]);
  HrHarvestRequest hrvFullRequest =
    new HrHarvestRequest(context,aUuids,HjRecord.JobType.Now,new HrCriteria(),new HrResult());
  hrvFullRequest.execute();
  if (hrvFullRequest.getActionResult().getNumberOfRecordsModified() > 0) {
    extractMessageBroker().addSuccessMessage(
      "catalog.harvest.manage.message.synchronized",
      new Object[]{Integer.toString(hrvFullRequest.getActionResult().
                   getNumberOfRecordsModified())
      });
  } else {
    extractMessageBroker().addSuccessMessage(
      "catalog.harvest.manage.message.synchronized.none");
  }
}

/**
 * Executes canceling of the synchronization.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @param actionCriteria the criteria for the action
 * @throws Exception if an exception occurs
 */
private void executeCancelSynchronization(ActionEvent event, RequestContext context, MmdActionCriteria actionCriteria) throws Exception {
  StringSet uuids = actionCriteria.getSelectedRecordIdSet();
  String[] aUuids = uuids.toArray(new String[uuids.size()]);
  int canceledCount = 0;
  for (String uuid : aUuids) {
    if (context.getApplicationContext().getHarvestingEngine().cancel(context, uuid)) {
      canceledCount++;
    }
  }
  extractMessageBroker().addSuccessMessage(
    "catalog.harvest.manage.message.canceled",
    new Object[]{Integer.toString(canceledCount)});
}

/**
 * Executes canceling of the synchronization.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @param actionCriteria the criteria for the action
 * @throws Exception if an exception occurs
 */
private void executeShowHarvested(ActionEvent event, RequestContext context, MmdActionCriteria actionCriteria) throws Exception {
  StringSet uuids = actionCriteria.getSelectedRecordIdSet();
  String[] aUuids = uuids.toArray(new String[uuids.size()]);
  if (aUuids.length>0) {
      getCriteria().getQueryCriteria().reset();
      getCriteria().getQueryCriteria().setSiteUuid(aUuids[0]);
  }
}

/**
 * Executes canceling of the synchronization.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @param actionCriteria the criteria for the action
 * @throws Exception if an exception occurs
 */
private void executeFind(ActionEvent event, RequestContext context, MmdActionCriteria actionCriteria) throws Exception {
  StringSet uuids = actionCriteria.getSelectedRecordIdSet();
  String[] aUuids = uuids.toArray(new String[uuids.size()]);
  if (aUuids.length>0) {
      getCriteria().getQueryCriteria().reset();
      getCriteria().getQueryCriteria().setUuid(aUuids[0]);
  }
}

/**
 * Fired when the getPrepareView() property is accessed.
 * <br/>This event is triggered from the page during the
 * render response phase of the JSF cycle.
 * <p>
 * The UI components associated with the PageCursorPanel are
 * build on the firing of this event.
 * @param context the context associated with the active request
 * @throws Exception if an exception occurs
 */
@Override
protected void onPrepareView(final RequestContext context) throws Exception {

  CollectionDao colDao = new CollectionDao(context);
  this._useCollections = colDao.getUseCollections();
  if (this._useCollections) {
    this.prepareCollections(context);
  }
  
  // build the selectable list of publishers
  getSelectablePublishers().build(context,true);
  prepareAccessPolicyConfig(context);
  prepareGroups(context);
  prepareActionCriteria(context);

  // build the UI components associated with the PageCursorPanel
  getPageCursorPanel().setPageCursor(getQueryResult().getPageCursor());
}

private void prepareAccessPolicyConfig(RequestContext context) throws Exception {
  _metadataAccessPolicyConfig = context.getApplicationConfiguration().getMetadataAccessPolicy();
  if(context.newIdentityAdapter() instanceof SimpleIdentityAdapter){
    if(_metadataAccessPolicyConfig.getAccessPolicyType().equals(MetadataAccessPolicy.TYPE_RESTRICTED)){
      _metadataAccessPolicyConfig.setAccessPolicyType(MetadataAccessPolicy.TYPE_PUBLIC_PROTECTED);
      _metadataAccessPolicyConfig.setAccessToGroupDN("protected");
    }
  }
}

private void prepareCollections(RequestContext context) throws Exception {
  if (false) return;
  this.getSelectableCollections().buildAll(context);
}

private void prepareGroups(RequestContext context) throws Exception {
  if (_metadataAccessPolicyConfig==null) {
    prepareAccessPolicyConfig(context);
  }
  String defaultGroup = _metadataAccessPolicyConfig.getAccessToGroupDN();
  if (defaultGroup == null || defaultGroup.trim().length() == 0) {
    // build the candidate list of access groups
    getCandidateGroups().buildAllGroups(context);
  }
}

private void prepareActionCriteria(RequestContext context) throws Exception {
  if (_metadataAccessPolicyConfig==null) {
    prepareAccessPolicyConfig(context);
  }
  getActionCriteria().setMetadataAccessPolicyType(_metadataAccessPolicyConfig.getAccessPolicyType());
  getActionCriteria().setAccessToGroupDN(_metadataAccessPolicyConfig.getAccessToGroupDN());
}

/**
 * Checks if is there any selectable group
 * @return <code>true</code> if is there at leas one selectable group present
 */
public boolean isSelectableGroups(){
  return _candidateGroups.getSize()>0?true:false;
}

/**
 * Gets query criteria as encrypted string.
 * Gets data from the last search criteria invoked.
 * @return query criteria as encrypted string
 */
public String getQueryCriteriaAsEncrypedString() {
  // get directly from the current query criteria
  return getQueryCriteria().getContentAsEncryptedString();
}

/**
 * Sets query criteria as encrypted string.
 * Instead overwriting query criteria for search, stores data in a separate
 * object.
 * @param content query criteria as encrypted string
 */
public void setQueryCriteriaAsEncrypedString(String content) {
  // don't update current criteria; instead store in a designated field
  _queryCriteriaForAction.setContentAsEncryptedString(content);
}

/**
 * Gets protocols eligible to choose.
 * @return collection of protocols eligible to choose
 */
public ArrayList<SelectItem> getProtocols() {
  ArrayList<SelectItem> protocols = new ArrayList<SelectItem>();
  MessageBroker msgBroker = getContextBroker().extractMessageBroker();
  ApplicationContext appCtx = ApplicationContext.getInstance();
  ApplicationConfiguration appCfg = appCtx.getConfiguration();
  ProtocolFactories protocolFactories = appCfg.getProtocolFactories();
  protocols.add(new SelectItem("", msgBroker.retrieveMessage("catalog.harvest.manage.edit.protocol.any")));
  for (String key: protocolFactories.getKeys()) {
    ProtocolFactory pf = protocolFactories.get(key);
    if (pf instanceof AgpProtocolFactory && !AGSProcessorConfig.isAvailable()) continue;
    String resourceKey = protocolFactories.getResourceKey(key);
    SelectItem item = new SelectItem(key.toLowerCase(), msgBroker.retrieveMessage(resourceKey));
    protocols.add(item);
  }
  return protocols;
}

/**
 * Handles a metadata management action.
 * <br/>This is the default entry point for a sub-class of BaseActionListener.
 * <br/>This BaseActionListener handles the JSF processAction method and
 * invokes the processSubAction method of the sub-class.
 * @param event the associated JSF action event
 * @param context the context associated with the active request
 * @throws AbortProcessingException if processing should be aborted
 * @throws Exception if an exception occurs
 */
@Override
protected void processSubAction(ActionEvent event, RequestContext context)
    throws AbortProcessingException, Exception {

  // duplicate the action criteria for the request to limit
  // thread safety issues
  MmdActionCriteria actionCriteria;
  actionCriteria = new MmdActionCriteria(getActionCriteria());

  // check for a page cursor navigation event
  getPageCursorPanel().checkActionEvent(event, true);

  // prepare the publisher
  Publisher publisher = new Publisher(context);

  // determine and execute the command
  UIComponent component = event.getComponent();
  String sCommand = Val.chkStr((String) component.getAttributes().get("command"));
  if (sCommand.equals("")) {
    
  // set the sort option
  } else if (sCommand.equals("sort")) {
    String sCol = (String) component.getAttributes().get("column");
    String sDir = (String) component.getAttributes().get("defaultDirection");
    getQueryCriteria().getSortOption().setColumnKey(sCol, true, sDir);

    // execute an action
  } else if (sCommand.equals("executeAction")) {
    String sAction = actionCriteria.getActionKey();
    boolean applyToAll = Val.chkBool((String) component.getAttributes().get("applyToAll"), false);
    boolean bRequiresAdmin = sAction.equalsIgnoreCase("transfer")
        || sAction.equalsIgnoreCase("setApproved")
        || sAction.equalsIgnoreCase("setDisapproved")
        || sAction.equalsIgnoreCase("setReviewed")
        || applyToAll;
    if (bRequiresAdmin && !publisher.getIsAdministrator()) {
      throw new NotAuthorizedException("Not authorized.");
    }
    executeAction(event, context, actionCriteria, publisher, applyToAll);
  } else if (sCommand.equals("synchronize")) {
    executeSynchronization(event, context, actionCriteria);
  } else if (sCommand.equals("cancel")) {
    executeCancelSynchronization(event, context, actionCriteria);
  } else if (sCommand.equals("showharvested")) {
    executeShowHarvested(event, context, actionCriteria);
  } else if (sCommand.equals("find")) {
    executeFind(event, context, actionCriteria);
  }

  // always execute the search for metadata records
  executeSearch(event, context, publisher);
}
}
