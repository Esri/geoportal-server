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
package com.esri.gpt.control.harvest;

import com.esri.gpt.catalog.arcgis.metadata.AGSProcessorConfig;
import com.esri.gpt.framework.adhoc.AdHocEventList;
import com.esri.gpt.framework.adhoc.IAdHocEvent;
import com.esri.gpt.catalog.harvest.history.HeCriteria;
import com.esri.gpt.catalog.harvest.jobs.HjRecord;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolArcIms;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocolResource;
import com.esri.gpt.catalog.harvest.repository.HrActionCriteria;
import com.esri.gpt.catalog.harvest.repository.HrAssertUrlException;
import com.esri.gpt.catalog.harvest.repository.HrCompleteUpdateRequest;
import com.esri.gpt.catalog.harvest.repository.HrCriteria;
import com.esri.gpt.catalog.harvest.repository.HrDeleteRequest;
import com.esri.gpt.catalog.harvest.repository.HrHarvestRequest;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.catalog.harvest.repository.HrRecord.HarvestFrequency;
import com.esri.gpt.catalog.harvest.repository.HrRecord.RecentJobStatus;
import com.esri.gpt.catalog.harvest.repository.HrRecords;
import com.esri.gpt.catalog.harvest.repository.HrResult;
import com.esri.gpt.catalog.harvest.repository.HrSelectRequest;
import com.esri.gpt.catalog.management.MmdEnums.ApprovalStatus;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.ValidationException;
import com.esri.gpt.control.publication.ManageMetadataController;
import com.esri.gpt.control.view.BaseSortDirectionStyleMap;
import com.esri.gpt.control.view.SelectablePublishers;
import com.esri.gpt.control.view.SortDirectionStyle;
import com.esri.gpt.control.webharvest.engine.Statistics;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactories;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.control.webharvest.protocol.factories.AgpProtocolFactory;
import com.esri.gpt.control.webharvest.validator.IConnectionChecker;
import com.esri.gpt.control.webharvest.validator.IValidator;
import com.esri.gpt.control.webharvest.validator.MessageCollectorAdaptor;
import com.esri.gpt.control.webharvest.validator.ValidatorFactory;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.request.SortOption;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.identity.local.LocalDao;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.security.principal.Users;
import com.esri.gpt.framework.util.TimePeriod;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 * Harvest controller. Provides functionality to support *.jsp pages to list,
 * and edit harvest repository data.
 */
public class HarvestController extends BaseHarvestController {

// class variables =============================================================
  /**
   * action expression
   */
  private static final String ACTION_EXPRESSION =
          "#{HarvestController.handleListRepositories}";
  /**
   * change expression
   */
  private static final String CHANGE_EXPRESSION =
          "#{HarvestController.pageCursorPanel.onChange}";
  
  private static String _timeCodes = "";
// instance variables ==========================================================
  /**
   * Harvest result.
   */
  private HrResult _result = new HrResult();
  /**
   * Repository editor.
   */
  private HarvestEditor _editor = new HarvestEditor(new HrRecord());
  /**
   * Sort direction style map.
   */
  private BaseSortDirectionStyleMap _sortDirectionStyleMap =
          new SortDirectionStyleMapImpl();
  /**
   * Selectable publishers
   */
  private SelectablePublishers _selectablePublishers = new SelectablePublishers();
  /**
   * Selectable publishers build
   */
  private boolean _selectablePublishersBuild;
  /**
   * synchronization status
   */
  private String synchronizationStatus = "none";
  /**
   * info enabled
   */
  private boolean infoEnabled = false;
  private ManageMetadataController mmController;

// constructors ================================================================
  /**
   * Creates instance of the controller.
   */
  public HarvestController() {
    super(ACTION_EXPRESSION, CHANGE_EXPRESSION);
    _pageCursorPanel.setPageCursor(getResult().getQueryResult().getPageCursor());
  }
// properties ==================================================================

  public ManageMetadataController getMmController() {
    return mmController;
  }

  public void setMmController(ManageMetadataController mmController) {
    this.mmController = mmController;
  }

  /**
   * Gets harvest criteria.
   *
   * @return harvest criteria
   */
  public HrCriteria getCriteria() {
    return getHarvestContext().getHarvestCriteria();
  }

  /**
   * Sets harvest criteria.
   *
   * @param criteria harvest criteria
   */
  public void setCriteria(HrCriteria criteria) {
    getHarvestContext().setHarvestCriteria(criteria);
  }

  /**
   * Gets harvest result.
   *
   * @return harvest result
   */
  public HrResult getResult() {
    return _result;
  }

  /**
   * Sets harvest result.
   *
   * @param result harvest result
   */
  public void setResult(HrResult result) {
    _result = result != null ? result : new HrResult();
  }

  /**
   * Gets harvest history criteria.
   *
   * @return harvest history criteria
   */
  public HeCriteria getHistoryCriteria() {
    return getHarvestContext().getHistoryCriteria();
  }

  /**
   * Sets harvest history criteria.
   *
   * @param historyCriteria harvest history criteria
   */
  public void setHistoryCriteria(HeCriteria historyCriteria) {
    getHarvestContext().setHistoryCriteria(historyCriteria);
  }

  /**
   * Gets harvest repository editor.
   *
   * @return harvest repository editor
   */
  public HarvestEditor getEditor() {
    return _editor;
  }

  /**
   * Sets harvest repository editor.
   *
   * @param editor harvest repository editor
   */
  public void setEditor(HarvestEditor editor) {
    _editor = editor != null ? editor : new HarvestEditor(new HrRecord());
  }

  /**
   * Gets sort direction style map.
   *
   * @return sort direction style map
   */
  public BaseSortDirectionStyleMap getSortDirectionStyleMap() {
    return _sortDirectionStyleMap;
  }

  /**
   * Gets sort direction style.
   *
   * @return sort direction style
   */
  public SortDirectionStyle getSortDirectionStyle() {
    return getSortDirectionStyleMap().getStyle();
  }

  /**
   * Sets sort direction style.
   *
   * @param style sort direction style
   */
  public void setSortDirectionStyle(SortDirectionStyle style) {
    getSortDirectionStyleMap().setStyle(style);
  }

  /**
   * Gets always false.
   *
   * @return <code>false</code>
   */
  public boolean getAlwaysFalse() {
    return false;
  }

  /**
   * Sets always false.
   *
   * @param ignored ignored parameter
   */
  public void setAlwaysFalse(boolean ignored) {
  }

  /**
   * Gets selectable publishers.
   *
   * @return selectable publishers
   */
  public SelectablePublishers getSelectablePublishers() {
    return _selectablePublishers;
  }
// methods =====================================================================

  /**
   * Called to handle list of repositories event.
   * <p/>
   * Reads sort columna and sort direction and stores within query criteria
   * object.
   *
   * @param event the associated JSF action event
   * @throws AbortProcessingException if processing should be aborted
   * @see HarvestContext
   */
  public void handleListRepositories(ActionEvent event)
          throws AbortProcessingException {

    try {
      // start execution phase
      RequestContext context = onExecutionPhaseStarted();

      // check authorization
      authorizeAction(context);

      // check for a page cursor navigation event
      getPageCursorPanel().checkActionEvent(event, true);

      // retrieve 'navMenuGlobal' flag from the request; only call from main menu
      // has this flag set

      UIComponent component = event.getComponent();
      String sCommand =
              Val.chkStr((String) component.getAttributes().get("command"));
      if (sCommand.equalsIgnoreCase("sort")) {
        String sCol = (String) component.getAttributes().get("column");
        String sDir = (String) component.getAttributes().get("defaultDirection");
        String sCurrCol =
                getCriteria().getQueryCriteria().getSortOption().getColumnKey();
        if (sCol.equalsIgnoreCase(sCurrCol)) {
          switch (SortOption.SortDirection.checkValue(sDir)) {
            case asc:
              sDir = SortOption.SortDirection.desc.name().toLowerCase();
              break;
            case desc:
              sDir = SortOption.SortDirection.asc.name().toLowerCase();
              break;
          }
        }
        getCriteria().getQueryCriteria().
                getSortOption().setColumnKey(sCol, true, sDir);
      }

      // evaluate local id
      String sLocalId = getCriteria().getQueryCriteria().getLocalId();
      if (sLocalId.length() > 0) {
        if (Val.chkInt(sLocalId, 0) <= 0) {
          if (isAdministrator(context)) {
            getCriteria().getQueryCriteria().setLocalId("");
          } else {
            extractMessageBroker().addErrorMessage(
                    "catalog.harvest.manage.message.err.idInv");
          }
        }
      }
    } catch (AbortProcessingException e) {
      throw (e);
    } catch (Throwable t) {
      handleException(t);
    } finally {
      onExecutionPhaseCompleted();
    }
  }

  /**
   * Handles repository creation.
   *
   * @param event the associated JSF action event
   * @throws AbortProcessingException if processing should be aborted
   */
  public void handleCreateRepository(ActionEvent event)
          throws AbortProcessingException {
    try {
      // start execution phase
      RequestContext context = onExecutionPhaseStarted();

      // check authorization
      authorizeAction(context);

      HrCriteria hc = getHarvestContext().getHarvestCriteria();
      hc.getActionCriteria().setUuid("");

    } catch (AbortProcessingException e) {
      throw (e);
    } catch (Throwable t) {
      handleException(t);
    } finally {
      onExecutionPhaseCompleted();
    }
  }

  /**
   * Handles initiating incremental synchronization.
   *
   * @param event action event
   * @throws AbortProcessingException if processing has been aborted
   */
  public void handleIncSynchronization(ActionEvent event)
          throws AbortProcessingException {

    // start execution phase
    RequestContext context = onExecutionPhaseStarted();

    try {
      submitIncSynchronization(context);
      createEditor(context, true);
    } catch (Throwable t) {
      handleException(t);
    } finally {
      onExecutionPhaseCompleted();
    }
  }

  /**
   * Handles initiating full synchronization.
   *
   * @param event action event
   * @throws AbortProcessingException if processing has been aborted
   */
  public void handleFullSynchronization(ActionEvent event)
          throws AbortProcessingException {

    // start execution phase
    RequestContext context = onExecutionPhaseStarted();

    try {
      submitFullSynchronization(context);
      createEditor(context, true);
    } catch (Throwable t) {
      handleException(t);
    } finally {
      onExecutionPhaseCompleted();
    }

  }

  /**
   * Handles canceling synchronization.
   *
   * @param event action event
   * @throws AbortProcessingException if processing has been aborted
   */
  public void handleCancelSynchronization(ActionEvent event)
          throws AbortProcessingException {

    // start execution phase
    RequestContext context = onExecutionPhaseStarted();

    try {
      submitCancelSycnhronization(context);
      createEditor(context, true);
    } catch (Throwable t) {
      handleException(t);
    } finally {
      onExecutionPhaseCompleted();
    }

  }

  public void handleUpdateRepositoryAndClose(ActionEvent event) throws AbortProcessingException {
    handleUpdateRepository(event);
    getMmController().processAction(event);
  }

  /**
   * Updates repository repository.
   *
   * @param event the associated JSF action event
   * @throws AbortProcessingException if processing should be aborted
   */
  public void handleUpdateRepository(ActionEvent event)
          throws AbortProcessingException {

    // start execution phase
    RequestContext context = onExecutionPhaseStarted();

    try {
      getEditor().setTimeCodes(_timeCodes);
      _timeCodes = "";
      
      // copy ownership
      Publisher owner = getSelectablePublishers().selectedAsPublisher(context, true);
      if (owner != null) {
        getEditor().getRepository().setOwnerId(owner.getLocalID());
      }
      getEditor().prepareForUpdate();
      if (getEditor().validate(extractMessageBroker())) {
        HrCompleteUpdateRequest req = new HrCompleteUpdateRequest(context, getEditor().getRepository());
        boolean creating = req.execute();

        extractMessageBroker().addSuccessMessage(
                creating ? "catalog.harvest.manage.message.create.2" : "catalog.harvest.manage.message.update.2");
      }

    } catch (ValidationException e) {
      String sKey = e.getKey();
      if (sKey.length() > 0) {
        String sMsg = sKey;
        Schema schema = context.getCatalogConfiguration().getConfiguredSchemas().get(sKey);
        if (schema != null) {
          if (schema.getLabel() != null) {
            String sResKey = schema.getLabel().getResourceKey();
            if (sResKey.length() > 0) {
              sMsg = extractMessageBroker().retrieveMessage(sResKey) + " (" + sKey + ")";
            }
          }
        }
        FacesMessage fm = new FacesMessage(FacesMessage.SEVERITY_WARN, " - " + sMsg, null);
        extractMessageBroker().addMessage(fm);
      }
      e.getValidationErrors().buildMessages(extractMessageBroker(), true);
    } catch (HrAssertUrlException e) {
      extractMessageBroker().addErrorMessage("catalog.harvest.manage.message.err.duplicatedUrl");
    } catch (TransformerException ex) {
      extractMessageBroker().addErrorMessage("catalog.harvest.manage.edit.err.notXml");
    } catch (SAXException ex) {
      extractMessageBroker().addErrorMessage("catalog.harvest.manage.edit.err.invXml");
    } catch (AbortProcessingException e) {
      throw (e);
    } catch (Throwable t) {
      handleException(t);
    } finally {
      onExecutionPhaseCompleted();
    }
  }

  /**
   * Checks connection to the remote server.
   *
   * @param event the associated JSF action event
   * @throws AbortProcessingException if processing should be aborted
   */
  public void handleTestConnection(ActionEvent event)
          throws AbortProcessingException {
    try {
      // start execution phase
      RequestContext context = onExecutionPhaseStarted();

      // check authorization
      authorizeAction(context);
      
      // perform check through the validator
      HrRecord repository = getEditor().getRepository();
      ValidatorFactory validatorFactory = ValidatorFactory.getInstance();
      IValidator validator = validatorFactory.getValidator(repository);
      if (validator!=null && validator.checkConnection(new MessageCollectorAdaptor(extractMessageBroker()))) {
        extractMessageBroker().addSuccessMessage("catalog.harvest.manage.test.success");
      }
      
    } catch (Throwable t) {
      handleException(t);
    } finally {
      onExecutionPhaseCompleted();
    }
  }

  /**
   * Checks connection to the remote server.
   *
   * @param event the associated JSF action event
   * @throws AbortProcessingException if processing should be aborted
   * @deprecated identical to {@link #handleTestConnection} 
   */
  @Deprecated
  public void handleTestAgs2AgpConnection(ActionEvent event)
          throws AbortProcessingException {
    handleTestConnection(event);
  }

  /**
   * Tests agp-2-agp query.
   * @param event action event
   * @throws AbortProcessingException  if processing should be aborted
   * @deprecated identical to {@link #handleTestConnection} 
   */
  @Deprecated
  public void handleTestAgp2AgpQuery(ActionEvent event)
          throws AbortProcessingException {
    handleTestConnection(event);
  }

  /**
   * Tests agp destination client.
   * @param event action event
   * @throws AbortProcessingException  if processing should be aborted
   */
  public void handleTestAgpDestination(ActionEvent event)
          throws AbortProcessingException {
    try {
      // start execution phase
      RequestContext context = onExecutionPhaseStarted();

      // check authorization
      authorizeAction(context);
      
      // perform check through the validator
      HrRecord repository = getEditor().getRepository();
      ValidatorFactory validatorFactory = ValidatorFactory.getInstance();
      IValidator validator = validatorFactory.getValidator(repository);
      IConnectionChecker destinationChecker = validator.listConnectionCheckers().get("destination");
      
      if (destinationChecker!=null && destinationChecker.checkConnection(new MessageCollectorAdaptor(extractMessageBroker()))) {
        extractMessageBroker().addSuccessMessage("catalog.harvest.manage.test.success");
      }
      
    } catch (Throwable t) {
      handleException(t);
    } finally {
      onExecutionPhaseCompleted();
    }
  }
  
  /**
   * Tests agp-2-agp destination client.
   * @param event action event
   * @throws AbortProcessingException  if processing should be aborted
   * @deprecated replaced by {@link #handleTestAgpDestination}
   */
  @Deprecated
  public void handleTestAgp2AgpClient(ActionEvent event)
          throws AbortProcessingException {
    handleTestAgpDestination(event);
  }

  /**
   * Tests agp-2-agp destination client.
   * @param event action event
   * @throws AbortProcessingException  if processing should be aborted
   * @deprecated replaced by {@link #handleTestAgpDestination}
   */
  @Deprecated
  public void handleTestAgs2AgpClient(ActionEvent event)
          throws AbortProcessingException {
    handleTestAgpDestination(event);
  }

  /**
   * <i>Execute</i> button action handler.
   *
   * @return navigation outcome
   */
  public String onClickButtonExecuteAction() {
    try {
      // start execution phase
      RequestContext context = onExecutionPhaseStarted();

      // check authorization
      authorizeAction(context);

      // get action
      HrActionCriteria.RepositoryAction action =
              getCriteria().getActionCriteria().getAction();

      StringSet uuids =
              getCriteria().getActionCriteria().getSelectedRecordIdSet();

      String[] aUuids = uuids.toArray(new String[uuids.size()]);

      switch (action) {
        case Create:
          getCriteria().getActionCriteria().setUuid("");
          return "catalog.harvest.manage.create";
        case Edit:
          if (aUuids.length == 1) {
            getCriteria().getActionCriteria().setUuid(aUuids[0]);
            return "catalog.harvest.manage.edit";
          } else {
            extractMessageBroker().addErrorMessage(
                    "catalog.harvest.manage.message.err.selection");
          }
          break;
        case Delete:
          HrDeleteRequest request = new HrDeleteRequest(context, aUuids);
          request.execute();
          int nRecordsDeleted =
                  request.getActionResult().getNumberOfRecordsModified();
          if (aUuids.length > 0) {
            extractMessageBroker().addSuccessMessage(
                    "catalog.harvest.manage.message.deleted",
                    new Object[]{
              Integer.toString(nRecordsDeleted),
              Integer.toString(aUuids.length)
            });
          } else {
            extractMessageBroker().addErrorMessage(
                    "catalog.harvest.manage.message.err.atLeast");
          }
          break;
        case History:
          if (aUuids.length == 1) {
            getHistoryCriteria().getActionCriteria().setUuid(aUuids[0]);
            return "catalog.harvest.manage.history";
          } else {
            extractMessageBroker().addErrorMessage(
                    "catalog.harvest.manage.message.err.selection");
          }
          break;
        case Synchronize:
          submitIncSynchronization(context);
          break;
        case Cancel:
          submitCancelSycnhronization(context);
          break;
      }

    } catch (Throwable t) {
      handleException(t);
    } finally {
      onExecutionPhaseCompleted();
    }

    return "";
  }

  /**
   * Prepares page to display list of harvest repositories.
   *
   * @return empty string
   */
  public String getListRepositoriesView() {

    try {
      // start view preparation phase
      RequestContext context = onPrepareViewStarted();

      // check authorization
      authorizeAction(context);

      getResult().getQueryResult().getRecords().clear();
      if (getCriteria().getActionCriteria().getAction()
              == HrActionCriteria.RepositoryAction.Unknown) {
        getCriteria().getActionCriteria().
                setAction(HrActionCriteria.RepositoryAction.Create);
      }
      HrSelectRequest request =
              new HrSelectRequest(context, getCriteria(), getResult(), isAdministrator(context));
      request.execute();

    } catch (NotAuthorizedException e) {
      try {
        ExternalContext ec = getContextBroker().getExternalContext();
        ec.redirect(Val.chkStr(ec.getRequestContextPath()) + "/catalog/main/home.page");
      } catch (Throwable t) {
        getLogger().log(Level.SEVERE, "Exception raised.", t);
      }
    } catch (Throwable t) {
      handleException(t);
    } finally {
      onPrepareViewCompleted();
    }

    return "";
  }

  /**
   * Does post-preparation of the list of harvest repositories.
   *
   * @return empty string
   */
  public String getListRepositoriesPostView() {
    // build the UI components associated with the PageCursorPanel
    getPageCursorPanel().setPageCursor(
            getResult().getQueryResult().getPageCursor());
    return "";
  }

  /**
   * Prepares page to display edited repository.
   *
   * @return empty string
   */
  public String getEditRepositoryView() {

    try {
      // start view preparation phase
      RequestContext context = onPrepareViewStarted();

      // check authorization
      authorizeAction(context);

      // create editor
      createEditor(context, false);

    } catch (NotAuthorizedException e) {
      try {
        ExternalContext ec = getContextBroker().getExternalContext();
        ec.redirect(Val.chkStr(ec.getRequestContextPath()) + "/catalog/main/home.page");
      } catch (Throwable t) {
        getLogger().log(Level.SEVERE, "Exception raised.", t);
      }
    } catch (Throwable t) {
      handleException(t);
      HrRecord record = new HrRecord();
      record.setProtocol(new HarvestProtocolArcIms());
      setEditor(new HarvestEditor(record));
    } finally {
      onPrepareViewCompleted();
    }

    return "";
  }

  /**
   * Sets synchronization status.
   *
   * @param status synchronization status
   */
  public void setSynchronizationStatus(String status) {
    this.synchronizationStatus = Val.chkStr(status, "none");
  }

  /**
   * Gets synchronization status.
   *
   * @return synchronization status.
   */
  public String getSynchronizationStatus() {
    return synchronizationStatus;
  }

  /**
   * Gets synchronization statistics.
   *
   * @return synchronization statistics
   */
  public String getSynchronizationStatistics() {
    RequestContext context = onPrepareViewStarted();
    try {
      HrRecord record = getEditor().getRepository();
      if (record.getRecentJobStatus() == RecentJobStatus.Running) {
        Statistics stats = context.getApplicationContext().getHarvestingEngine().getStatistics(record.getUuid());
        if (stats != null) {
          String[] params = new String[]{
            Long.toString(stats.getHarvestedCount()),
            Long.toString(stats.getValidatedCount()),
            Long.toString(stats.getPublishedCount()),
            new TimePeriod(stats.getDuration()).toLocalizedString(extractMessageBroker()),
            Double.toString(stats.getPerformance())
          };
          return extractMessageBroker().retrieveMessage("catalog.harvest.manage.edit.syncRunningStats", params);
        }
      }
      return "";
    } finally {
      context.onExecutionPhaseCompleted();
    }
  }

  /**
   * Prepares selected publishers.
   *
   * @return empty string
   */
  public String getPrepareSelectedPublishers() {
    try {
      // start view preparation phase
      RequestContext context = onPrepareViewStarted();
      if (!_selectablePublishersBuild) {
        _selectablePublishers.build(context, isAdministrator(context));
        // read owner distinguished name and select record owner
        if (getEditor() != null && getEditor().getRepository() != null) {
          // read owner distinguished name
          HrRecord record = getEditor().getRepository();
          LocalDao localDao = new LocalDao(context);
          String uDN = localDao.readDN(record.getOwnerId());
          if (uDN.length() == 0) {
            User u = context.getUser();
            if (u != null) {
              uDN = u.getDistinguishedName();
            }
          }
          // check if this user already exist on the list of selectable publishers
          if (uDN.length() > 0) {
            boolean bExist = false;
            for (SelectItem si : getSelectablePublishers().getItems()) {
              if (si.getValue().equals(uDN)) {
                bExist = true;
                break;
              }
            }
            // if does not exist, try to finf him and add to this list
            if (!bExist) {
              Users allSelectablePublishers =
                      Publisher.buildSelectablePublishers(context, true);
              User owner = allSelectablePublishers.get(uDN);
              if (owner != null) {
                getSelectablePublishers().getItems().add(
                        new SelectItem(owner.getKey(), owner.getName()));
              }
            }
            // at least, select it
            getSelectablePublishers().setSelectedKey(uDN);
          }
        }
        _selectablePublishersBuild = true;
      }

    } catch (Throwable t) {
      handleException(t);
    } finally {
      onPrepareViewCompleted();
    }

    return encode(getSelectablePublishers());
  }

  /**
   * Companion to {@link HarvestController#getPrepareSelectedPublishers}
   *
   * @param ignore ignored argument
   */
  public void setPrepareSelectedPublishers(String ignore) {
    _selectablePublishers = decodeSelectablePublishers(ignore);
    _selectablePublishersBuild = true;
  }

  /**
   * Sets info enabled.
   *
   * @param enabled <code>true</code> to enabled info.
   */
  public void setInfoEnabled(boolean enabled) {
    this.infoEnabled = enabled;
  }

  /**
   * Checks if info enabled.
   *
   * @return <code>true</code> if info enabled.
   */
  public boolean getInfoEnabled() {
    return infoEnabled;
  }

  /**
   * Gets protocols eligible to choose.
   *
   * @return collection of protocols eligible to choose
   */
  public ArrayList<SelectItem> getProtocols() {
    ArrayList<SelectItem> protocols = new ArrayList<SelectItem>();
    MessageBroker msgBroker = getContextBroker().extractMessageBroker();
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();
    ProtocolFactories protocolFactories = appCfg.getProtocolFactories();
    for (String key : protocolFactories.getKeys()) {
      ProtocolFactory pf = protocolFactories.get(key);
      if (pf instanceof AgpProtocolFactory && !AGSProcessorConfig.isAvailable()) {
        continue;
      }
      String resourceKey = protocolFactories.getResourceKey(key);
      SelectItem item = new SelectItem(key.toLowerCase(), msgBroker.retrieveMessage(resourceKey));
      protocols.add(item);
    }
    return protocols;
  }

  /**
   * Gets time points.
   *
   * @return list of time points
   */
  public ArrayList<TimePoint> getTimePoints() {
    MessageBroker broker = extractMessageBroker();
    ArrayList<TimePoint> timePoints = new ArrayList<TimePoint>();
    try {
      AdHocEventList adHocEventList = getEditor().getRepository().getAdHocEventList();
      for (IAdHocEvent evt : adHocEventList) {
        String localizedCaption = evt.getLocalizedCaption(broker);
        TimePoint tp = new TimePoint(evt, localizedCaption);
        timePoints.add(tp);
      }
    } catch (ParseException ex) {
    }
    return timePoints;
  }

  public void setTimeMessages(String timeMessages) {
    // intentionally left empty
  }
  
  public String getTimeMessages() {
    MessageBroker broker = extractMessageBroker();

    try {
      StringBuilder sb = new StringBuilder();
      AdHocEventList adHocEventList = getEditor().getRepository().getAdHocEventList();
      for (IAdHocEvent evt : adHocEventList) {
        String localizedCaption = evt.getLocalizedCaption(broker);
        if (sb.length() > 0) {
          sb.append("|");
        }
        sb.append(localizedCaption);
      }

      return sb.toString();
    } catch (ParseException ex) {
      return "";
    }
  }

/**
 * Gets time codes.
 *
 * @return time codes.
 */
public String getTimeCodes() {
  return getEditor().getTimeCodes();
}

/**
 * Sets time codes.
 *
 * @param timeCodes time codes
 */
public void setTimeCodes(String timeCodes) {
  this._timeCodes = timeCodes;
  getEditor().setTimeCodes(timeCodes);
}

  /**
   * Creates editor.
   *
   * @param context request context
   * @param reload <code>true</code> to reload repository
   * @throws SQLException if accessing database fails
   */
  private void createEditor(RequestContext context, boolean reload) throws SQLException {
    HrCriteria hc = getHarvestContext().getHarvestCriteria();
    String uuid = hc.getActionCriteria().getUuid();

    HrRecord record = null;

    boolean doInit = false;
    boolean doClear = false;

    if (UuidUtil.isUuid(uuid)) {
      if (!getEditor().getRepository().getUuid().equals(uuid) || reload) {
        RequestContext rc = new FacesContextBroker().extractRequestContext();
        HrSelectRequest request = new HrSelectRequest(rc, uuid);
        request.execute();
        HrRecords records = request.getQueryResult().getRecords();
        if (records.size() == 1) {
          record = records.get(0);
          doInit = true;
        } else {
          doClear = true;
          extractMessageBroker().addErrorMessage(
                  "catalog.harvest.manage.message.err.missing");
        }
      }
    } else {
      doClear = true;
    }

    if (record == null) {
      record = getEditor().getRepository();

      if (record.getProtocol() == null || context.getApplicationConfiguration().getProtocolFactories().get(record.getProtocol().getKind()) == null) {
        record = new HrRecord();
        record.setProtocol(new HarvestProtocolResource());
        ProtocolInvoker.setUpdateContent(record.getProtocol(), true);
        ProtocolInvoker.setUpdateDefinition(record.getProtocol(), true);
        ProtocolInvoker.setAutoApprove(record.getProtocol(), true);
        record.setFindable(true);
        record.setSearchable(true);
        record.setSynchronizable(true);
      }
    }

    if (record.getRecentJobStatus() == RecentJobStatus.Submited) {
      setSynchronizationStatus("submited");
    } else if (record.getRecentJobStatus() == RecentJobStatus.Running) {
      setSynchronizationStatus("running");
    } else if (record.getRecentJobStatus() == RecentJobStatus.Canceled) {
      setSynchronizationStatus("canceled");
    } else if (record.getHarvestFrequency() != HarvestFrequency.Skip
            && record.getHarvestFrequency() != HarvestFrequency.Once
            && ApprovalStatus.isPubliclyVisible(record.getApprovalStatus().name())
            && record.getSynchronizable()) {
      setSynchronizationStatus("scheduled");
    } else {
      setSynchronizationStatus("none");
    }

    setInfoEnabled(UuidUtil.isUuid(record.getUuid()) && ApprovalStatus.isPubliclyVisible(record.getApprovalStatus().name()) && record.getSynchronizable());

    HarvestEditor harvestEditor = new HarvestEditor(record);
    harvestEditor.prepareForEdit();

    setEditor(harvestEditor);
  }

  /**
   * Submits incremental synchronization request.
   *
   * @param context request context
   * @throws Exception if performing operation fails
   */
  private void submitIncSynchronization(RequestContext context) throws Exception {

    ArrayList<String> uuids = new ArrayList<String>();

    String uuid = getEditor().getRepository().getUuid();
    HrSelectRequest select = new HrSelectRequest(context, uuid);
    select.execute();

    for (HrRecord r : select.getQueryResult().getRecords()) {
      if (UuidUtil.isUuid(r.getUuid())) {
        uuids.add(r.getUuid());
      }
    }

    String[] aUuids = uuids.toArray(new String[uuids.size()]);

    HrHarvestRequest hrvNowRequest =
            new HrHarvestRequest(context,
            aUuids,
            HjRecord.JobType.Now,
            getCriteria(),
            getResult());
    hrvNowRequest.execute();

    if (hrvNowRequest.getActionResult().getNumberOfRecordsModified() > 0) {
      extractMessageBroker().addSuccessMessage(
              "catalog.harvest.manage.message.synchronized",
              new Object[]{Integer.toString(hrvNowRequest.getActionResult().
        getNumberOfRecordsModified())
      });
    } else {
      extractMessageBroker().addSuccessMessage(
              "catalog.harvest.manage.message.synchronized.none");
    }

  }

  /**
   * Submits full synchronization request.
   *
   * @param context request context
   * @throws Exception if performing operation fails
   */
  private void submitFullSynchronization(RequestContext context) throws Exception {
    ArrayList<String> uuids = new ArrayList<String>();

    String uuid = getEditor().getRepository().getUuid();
    HrSelectRequest select = new HrSelectRequest(context, uuid);
    select.execute();

    for (HrRecord r : select.getQueryResult().getRecords()) {
      if (UuidUtil.isUuid(r.getUuid())) {
        uuids.add(r.getUuid());
      }
    }

    String[] aUuids = uuids.toArray(new String[uuids.size()]);

    HrHarvestRequest hrvFullRequest =
            new HrHarvestRequest(context,
            aUuids,
            HjRecord.JobType.Full,
            getCriteria(),
            getResult());
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
   * Submits cancel synchronization request.
   *
   * @param context request context
   * @throws Exception if performing operation fails
   */
  private void submitCancelSycnhronization(RequestContext context) throws Exception {
    ArrayList<String> uuids = new ArrayList<String>();

    String uuid = getEditor().getRepository().getUuid();
    HrSelectRequest select = new HrSelectRequest(context, uuid);
    select.execute();

    for (HrRecord r : select.getQueryResult().getRecords()) {
      if (UuidUtil.isUuid(r.getUuid())) {
        uuids.add(r.getUuid());
      }
    }

    String[] aUuids = uuids.toArray(new String[uuids.size()]);

    int canceledCount = 0;
    for (String u : aUuids) {
      if (context.getApplicationContext().getHarvestingEngine().cancel(context, u)) {
        canceledCount++;
      }
    }

    extractMessageBroker().addSuccessMessage(
            "catalog.harvest.manage.message.canceled",
            new Object[]{Integer.toString(canceledCount)});
  }

  /**
   * Checks if user is administrator.
   *
   * @param context request context
   * @return <code>true</code> if user is administrator
   */
  private boolean isAdministrator(RequestContext context) {
    return context.getUser() != null
            && context.getUser().getAuthenticationStatus().getWasAuthenticated()
            && context.getUser().getAuthenticationStatus().
            getAuthenticatedRoles().hasRole("gptAdministrator");
  }

  /**
   * Encodes selected item.
   *
   * @param si selected item to encode
   * @return encoded selected item
   */
  private String encode(SelectItem si) {
    return si.getLabel() + "\t" + si.getValue().toString();
  }

  /**
   * Decodes selected item.
   *
   * @param siData selected item to decode
   * @return decoded selected item
   */
  private SelectItem decodeSelectItem(String siData) {
    String[] elements = Val.chkStr(siData).split("\t");
    return new SelectItem(elements.length > 1 ? elements[1] : "",
            elements.length > 0 ? elements[0] : "");
  }

  /**
   * Encodes selectable publishers.
   *
   * @param sp selectable publishers to encode
   * @return encoded selected publishers
   */
  private String encode(SelectablePublishers sp) {
    StringBuilder sb = new StringBuilder();
    for (SelectItem si : sp.getItems()) {
      if (sb.length() > 0) {
        sb.append("\r\n");
      }
      sb.append(encode(si));
    }
    return sb.toString();
  }

  /**
   * Decodes selectable publishers.
   *
   * @param spData selectable publishers to decode
   * @return decoded selectable publishers
   */
  private SelectablePublishers decodeSelectablePublishers(String spData) {
    String[] elements = Val.chkStr(spData).split("\r\n");
    SelectablePublishers sp = new SelectablePublishers();
    for (String siData : elements) {
      sp.getItems().add(decodeSelectItem(siData));
    }
    return sp;
  }
// types =======================================================================

  /**
   * Custom implementation of SortDirectionStyleMap.
   */
  private class SortDirectionStyleMapImpl extends BaseSortDirectionStyleMap {

    /**
     * Gets sort option.
     *
     * @return sort option
     */
    @Override
    public SortOption getSortOption() {
      return getCriteria().getQueryCriteria().getSortOption();
    }
  }
}
