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

import com.esri.gpt.catalog.harvest.history.HeActionCriteria;
import com.esri.gpt.catalog.harvest.history.HeCriteria;
import com.esri.gpt.catalog.harvest.history.HeDeleteRequest;
import com.esri.gpt.catalog.harvest.history.HeRecord;
import com.esri.gpt.catalog.harvest.history.HeRecords;
import com.esri.gpt.catalog.harvest.history.HeResult;
import com.esri.gpt.catalog.harvest.history.HeSelectOneRequest;
import com.esri.gpt.catalog.harvest.history.HeSelectRequest;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.catalog.harvest.repository.HrRecords;
import com.esri.gpt.catalog.harvest.repository.HrSelectRequest;
import com.esri.gpt.control.view.BaseSortDirectionStyleMap;
import com.esri.gpt.control.view.SortDirectionStyle;
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.context.ApplicationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.request.SortOption;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;
import java.util.logging.Level;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;

/**
 * Harvest history controller.
 * Provides functionality to support *.jsp pages to display harvest history and
 * view harvest reports.
 */
public class HarvestHistoryController extends BaseHarvestController {

// class variables =============================================================
/** action expression */
private static final String ACTION_EXPRESSION =
  "#{HarvestHistoryController.handleListOfEvents}";
/** change expression */
private static final String CHANGE_EXPRESSION =
  "#{HarvestHistoryController.pageCursorPanel.onChange}";

// instance variables ==========================================================
/** History result. */
private HeResult _result = new HeResult();
/** Harvest viewer viewer. */
private ReportViewer _reportViewer = new ReportViewer();
/** Sort direction style map. */
private BaseSortDirectionStyleMap _sortDirectionStyleMap =
  new SortDirectionStyleMapImpl();
/** flag to indicate if is this external call request */
private boolean external = false;

// constructors ================================================================
/**
 * Creates instance of the controller.
 */
public HarvestHistoryController() {
  super(ACTION_EXPRESSION, CHANGE_EXPRESSION);
  _pageCursorPanel.setPageCursor(getResult().getQueryResult().getPageCursor());
}
// properties ==================================================================
/**
 * Gets harvest history criteria.
 * @return harvest history criteria
 */
public HeCriteria getCriteria() {
  return getHarvestContext().getHistoryCriteria();
}

/**
 * Sets harvest history criteria.
 * @param historyCriteria harvest history criteria
 */
public void setCriteria(HeCriteria historyCriteria) {
  getHarvestContext().setHistoryCriteria(historyCriteria);
}

/**
 * Gets harvest history result.
 * @return harvest history result
 */
public HeResult getResult() {
  return _result;
}

/**
 * Sets harvest history result.
 * @param historyResult harvest history result
 */
public void setResult(HeResult historyResult) {
  _result = historyResult != null ? historyResult : new HeResult();
}

/**
 * Gets viewer viewer.
 * @return viewer viewer
 */
public ReportViewer getReportViewer() {
  return _reportViewer;
}

/**
 * Sets viewer viewer.
 * @param viewer viewer viewer
 */
/* default */ void setReportViewer(ReportViewer viewer) {
  _reportViewer = viewer;
}

/**
 * Gets sort direction style map.
 * @return sort direction style map
 */
public BaseSortDirectionStyleMap getSortDirectionStyleMap() {
  return _sortDirectionStyleMap;
}

/**
 * Gets sort direction style.
 * @return sort direction style
 */
public SortDirectionStyle getSortDirectionStyle() {
  return getSortDirectionStyleMap().getStyle();
}

/**
 * Sets sort direction style.
 * @param style sort direction style
 */
public void setSortDirectionStyle(SortDirectionStyle style) {
  getSortDirectionStyleMap().setStyle(style);
}

/**
 * Gets always false.
 * @return <code>false</code>
 */
public boolean getAlwaysFalse() {
  return false;
}

/**
 * Sets always false.
 * @param ignored ignored parameter
 */
public void setAlwaysFalse(boolean ignored) {

}

/**
 * Gets information if it's external call.
 * @return <code>true</code> if it's external call
 */
public boolean getExternal() {
  return external;
}

/**
 * Sets information if it's external call.
 * @param external <code>true</code> if it's external call
 */
public void setExternal(boolean external) {
  this.external = external;
}
// methods =====================================================================
/**
 * Called to handle history event.
 * <p/>
 * Reads sort columna and sort direction and stores within query criteria 
 * object.
 * @param event the associated JSF action event
 * @throws AbortProcessingException if processing should be aborted
 * @see HarvestContext
 */
public void handleListOfEvents(ActionEvent event) {
  try {
    // start execution phase
    RequestContext context = onExecutionPhaseStarted();

    // check authorization
    authorizeAction(context);

    // check for a page cursor navigation event
    getPageCursorPanel().checkActionEvent(event, true);

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
  } catch (AbortProcessingException e) {
    throw (e);
  } catch (Throwable t) {
    handleException(t);
  } finally {
    onExecutionPhaseCompleted();
  }
}

/**
 * Prepares page to display harvest history.
 * @return empty string
 */
public String getListEventsView() {

  try {
    // start view preparation phase
    RequestContext context = onPrepareViewStarted();

    // check authorization
    authorizeAction(context);

    // prepare action criteria
    if (getCriteria().getActionCriteria().getAction() ==
      HeActionCriteria.HistoryAction.Unknown) {
      getCriteria().getActionCriteria().setAction(
        HeActionCriteria.HistoryAction.ViewReport);
    }

    // get uuid of repository record
    String uuid = getCriteria().getActionCriteria().getUuid();
    if (!UuidUtil.isUuid(uuid)) {
      throw new ApplicationException();
    }

    // select and read repository record
    HrSelectRequest selectRepositoryRequest =
      new HrSelectRequest(context, uuid);
    selectRepositoryRequest.execute();
    HrRecords records = selectRepositoryRequest.getQueryResult().getRecords();

    // get harvest repository record
    HrRecord record = records.size() == 1 ? records.get(0) : null;
    if (record == null) {
      throw new ApplicationException();
    }

    // select and read harvest event record
    HeSelectRequest selectRequest =
      new HeSelectRequest(context, record, getCriteria(), getResult());
    selectRequest.execute();

    getResult().getQueryResult().setUuid(record.getUuid());
    getResult().getQueryResult().setProtocolTypeAsString(record.getProtocol().getKind());
    getResult().getQueryResult().setName(record.getName());
    getResult().getQueryResult().setUrl(record.getHostUrl());
    getResult().getQueryResult().setReportUuid("");

  } catch (NotAuthorizedException e) {
    try {
      ExternalContext ec = getContextBroker().getExternalContext();
      ec.redirect(Val.chkStr(ec.getRequestContextPath())+"/catalog/main/home.page");
    } catch (Throwable t) {
      getLogger().log(Level.SEVERE,"Exception raised.",t);
    }
  } catch (Throwable t) {
    handleException(t);
  } finally {
    onPrepareViewCompleted();
  }

  return "";
}

/**
 * Does post preparation of the list of events.
 * @return empty string
 */
public String getListEventsPostView() {
  // build the UI components associated with the PageCursorPanel
  getPageCursorPanel().setPageCursor(
    getResult().getQueryResult().getPageCursor());
  return "";
}

/**
 * Prepares harvest report page.
 * @return empty string
 */
public String getViewReportView() {

  try {
    // start view preparation phase
    RequestContext context = onPrepareViewStarted();
    getReportViewer().setRequestContext(context);
    getReportViewer().setMsgBroker(extractMessageBroker());
    getReportViewer().setRecord(new HeRecord(new HrRecord()));

    String uuid = context.getServletRequest().getParameter("uuid");
    if (UuidUtil.isUuid(uuid)) {
      getCriteria().getActionCriteria().setEventUuid(uuid);
      setExternal(true);
    } else {
      // check authorization
      authorizeAction(context);
    }

    String sEventUuid = getCriteria().getActionCriteria().getEventUuid();

    // get uuid of harvest event uuid
    if (!UuidUtil.isUuid(sEventUuid)) {
      throw new ApplicationException();
    }

    // select and read harvest event record
    HeSelectOneRequest selectEventReq =
      new HeSelectOneRequest(context, sEventUuid);
    selectEventReq.execute();
    HeRecords events = selectEventReq.getQueryResult().getRecords();

    // get harvest event record
    HeRecord event = events.size() == 1 ? events.get(0) : null;
    if (event == null) {
      throw new ApplicationException("No requested event found.");
    }

    HrRecord repository = event.getRepository();

    getResult().getQueryResult().setUuid(repository.getUuid());
    getResult().getQueryResult().setProtocolTypeAsString(repository.getProtocol().getKind());
    getResult().getQueryResult().setName(repository.getName());
    getResult().getQueryResult().setUrl(repository.getHostUrl());
    getResult().getQueryResult().setReportUuid(event.getUuid());

    getReportViewer().setRecord(event);

  } catch (NotAuthorizedException e) {
    try {
      ExternalContext ec = getContextBroker().getExternalContext();
      ec.redirect(Val.chkStr(ec.getRequestContextPath())+"/catalog/main/home.page");
    } catch (Throwable t) {
      getLogger().log(Level.SEVERE,"Exception raised.",t);
    }
  } catch (Throwable t) {
    handleException(t);
  } finally {
    onPrepareViewCompleted();
  }

  return "";
}

/**
 * History report execute button action.
 * @return navigation outcome
 */
public String onClickButtonHistoryExecuteAction() {
  try {
    // start execution phase
    RequestContext context = onExecutionPhaseStarted();

    // check authorization
    authorizeAction(context);

    // get action
    HeActionCriteria.HistoryAction action =
      getCriteria().getActionCriteria().getAction();

    StringSet uuids =
      getCriteria().getActionCriteria().getSelectedRecordIdSet();

    String[] aUuids = uuids.toArray(new String[uuids.size()]);

    switch (action) {
      case ViewReport:
        if (aUuids.length == 1) {
          getCriteria().getActionCriteria().setEventUuid(aUuids[0]);
          return "catalog.harvest.manage.report";
        } else {
          extractMessageBroker().addErrorMessage(
            "catalog.harvest.history.manage.message.err.selection");
        }
        break;
      case Delete:
        HeDeleteRequest request = new HeDeleteRequest(context, aUuids);
        request.execute();
        int nRecordsDeleted =
          request.getActionResult().getNumberOfRecordsModified();
        if (aUuids.length > 0) {
          extractMessageBroker().addSuccessMessage(
            "catalog.harvest.manage.history.message.deleted",
            new Object[]{
            Integer.toString(nRecordsDeleted),
            Integer.toString(aUuids.length)
          });
        } else {
          extractMessageBroker().addErrorMessage(
            "catalog.harvest.history.manage.message.err.atLeast");
        }
        break;
    }

  } catch (Throwable t) {
    handleException(t);
  } finally {
    onExecutionPhaseCompleted();
  }

  return "";
}

// types =======================================================================
/**
 * Custom implementation of SortDirectionStyleMap.
 */
private class SortDirectionStyleMapImpl extends BaseSortDirectionStyleMap {

/**
 * Gets sort option.
 * @return sort option
 */
@Override
public SortOption getSortOption() {
  return getCriteria().getQueryCriteria().getSortOption();
}
}
}
