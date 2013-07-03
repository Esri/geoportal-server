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
package com.esri.gpt.control.view;

import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.util.Val;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import javax.faces.component.UIComponent;
import javax.faces.component.UIForm;
import javax.faces.component.UISelectItem;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectOneListbox;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;

/**
 * Provides support for binding a page cursor associated with a query result to
 * an HtmlPanelGroup.
 * <p>
 * The cursor requires message resource keys. All resource keys used are
 * prefixed with the supplied resourceKeyPrefix property (the default
 * value is "general.pageCursor.").
 * <p>
 * The following resource keys are used by the cursor:
 * <br>nomatch results first previous next last
 * <p>
 * Examples:
 * <br/>catalog.general.pageCursor.nomatch  = No matching records were located.
 * <br/>catalog.general.pageCursor.results  = Results {0}-{1} of {2} record(s)
 * <br/>catalog.general.pageCursor.first    = First
 * <br/>catalog.general.pageCursor.previous = <
 * <br/>catalog.general.pageCursor.next     = >
 * <br/>catalog.general.pageCursor.last     = Last
 * <p>
 * The "results" resource key is record (not page based) and
 * takes the following 3 arguments:
 * <br/>The starting record for the current page, the ending record for the
 * current page and the total number of records.
 * <p>
 */
public class PageCursorPanel {

// class variables =============================================================
/** Default list of results per page. */
private static final Integer[] _defaultResultsPerPage =
  new Integer[]{5, 10, 20, 50};
// instance variables ==========================================================
private String _actionListenerExpression = "";
private String _changeListenerExpression = "";
private HtmlPanelGroup _bottomHtmlPanelGroup;
private String _commandAttributeName;
private String _commandAttributeValue;
private String _currentStyleClass;
private String _cursorPageAttributeName;
private PageCursor _pageCursor;
private String _resourceKeyPrefix;
private String _resultStyleClass;
private HtmlPanelGroup _topHtmlPanelGroup;

// constructors ================================================================
/** Default constructor. */
public PageCursorPanel() {
  this.setCommandAttributeName("command");
  this.setCommandAttributeValue("setCursorPage");
  this.setCurrenStyleClass("current");
  this.setCursorPageAttributeName("cursorPage");
  this.setResourceKeyPrefix("general.pageCursor.");
  this.setResultStyleClass("result");
}

// properties ==================================================================
/**
 * Gets the expression used to bind generated command links to the controller.
 * <br/>Typically the expression will have the following form:
 * <br/>#{SomeController.processAction}
 * @return the action listener binding expression
 */
public String getActionListenerExpression() {
  return _actionListenerExpression;
}

/**
 * Sets the expression used to bind generated command links to the controller.
 * <br/>Typically the expression will have the following form:
 * <br/>#{SomeController.processAction}
 * @param expression the action listener binding expression
 */
public void setActionListenerExpression(String expression) {
  _actionListenerExpression = Val.chkStr(expression);
}

public String getChangeListenerExpression() {
  return _changeListenerExpression;
}

public void setChangeListenerExpression(String expression) {
  _changeListenerExpression = Val.chkStr(expression);
}
/**
 * Gets the bound HtmlPanelGroup for the bottom portion of the page.
 * <br/>This object is used during the Faces component binding process.
 * @return the bound HtmlPanelGroup
 */
public HtmlPanelGroup getBottomHtmlPanelGroup() {
  return _bottomHtmlPanelGroup;
}

/**
 * Sets the bound HtmlPanelGroup for the bottom portion of the page.
 * <br/>This object is used during the Faces component binding process.
 * @param htmlPanelGroup the bound HtmlPanelGroup
 */
public void setBottomHtmlPanelGroup(HtmlPanelGroup htmlPanelGroup) {
  _bottomHtmlPanelGroup = htmlPanelGroup;
}

/**
 * Gets the name of the attribute used to indicate the command that
 * should be executed.
 * <br/>The default value is "command".
 * @return the command attribute name
 */
public String getCommandAttributeName() {
  return _commandAttributeName;
}

/**
 * Sets the name of the attribute used to indicate the command that
 * should be executed.
 * <br/>The default value is "command".
 * @param name the command attribute name
 */
public void setCommandAttributeName(String name) {
  _commandAttributeName = Val.chkStr(name);
}

/**
 * Gets value of the command attribute indicating that a page cursor
 * navigation event has occurred.
 * <br/>The default value is "setCursorPage".
 * @return the command attribute value for a page cursor navigation event
 */
public String getCommandAttributeValue() {
  return _commandAttributeValue;
}

/**
 * Sets value of the command attribute indicating that a page cursor
 * navigation event has occurred.
 * <br/>The default value is "setCursorPage".
 * @param value the command attribute value for a page cursor navigation event
 */
public void setCommandAttributeValue(String value) {
  _commandAttributeValue = Val.chkStr(value);
}

/**
 * Gets the style class associated with the current page.
 * <br/>The default value is "current".
 * @return the current page style class
 */
public String getCurrentStyleClass() {
  return _currentStyleClass;
}

/**
 * Sets the style class associated with the current page.
 * <br/>The default value is "current".
 * @param styleClass the current page style class
 */
public void setCurrenStyleClass(String styleClass) {
  _currentStyleClass = Val.chkStr(styleClass);
}

/**
 * Gets the name of the attribute used to indicate the cursor page.
 * <br/>The default value is "cursorPage".
 * @return the cursor page attribute name
 */
public String getCursorPageAttributeName() {
  return _cursorPageAttributeName;
}

/**
 * Sets the name of the attribute used to indicate the cursor page.
 * <br/>The default value is "cursorPage".
 * @param name the cursor page attribute name
 */
public void setCursorPageAttributeName(String name) {
  _cursorPageAttributeName = Val.chkStr(name);
}

/**
 * Gets the underlying page cursor.
 * <br/>The page cursor UI will be generated from the content of this cursor.
 * @return the page cursor
 */
public PageCursor getPageCursor() {
  return _pageCursor;
}

/**
 * Sets the underlying page cursor.
 * <br/>The page cursor UI will be generated from the content of this cursor.
 * <br/>Setting the page cursor triggers the building of the HtmlPanelGroup
 * UI components.
 * <br/>This method should be invoked immediately following the generation
 * of query results.
 * @param cursor the page cursor
 */
public void setPageCursor(PageCursor cursor) {
  _pageCursor = cursor;
  build();
}

/**
 * Gets the resource key prefix to be used when generating UI components.
 * <br/>The default value is "general.pageCursor.".
 * @return the resource key prefix
 */
public String getResourceKeyPrefix() {
  return _resourceKeyPrefix;
}

/**
 * Sets the resource key prefix to be used when generating UI components.
 * <br/>The default value is "general.pageCursor.".
 * @param resourceKeyPrefix the resource key prefix
 */
public void setResourceKeyPrefix(String resourceKeyPrefix) {
  _resourceKeyPrefix = Val.chkStr(resourceKeyPrefix);
}

/**
 * Gets the style class associated with the result text.
 * <br/>The result text takes the form:
 * <br/>Results 1-10 of 61 record(s)
 * <br/>The default value is "result".
 * @return the result text style class
 */
public String getResultStyleClass() {
  return _resultStyleClass;
}

/**
 * Sets the style class associated with the result text.
 * <br/>The result text takes the form:
 * <br/>Results 1-10 of 61 record(s)
 * <br/>The default value is "result".
 * @param styleClass the result text style class
 */
public void setResultStyleClass(String styleClass) {
  _resultStyleClass = Val.chkStr(styleClass);
}

/**
 * Gets the bound HtmlPanelGroup for the top portion of the page.
 * <br/>This object is used during the Faces component binding process.
 * @return the bound HtmlPanelGroup
 */
public HtmlPanelGroup getTopHtmlPanelGroup() {
  return _topHtmlPanelGroup;
}

/**
 * Sets the bound HtmlPanelGroup for the top portion of the page.
 * <br/>This object is used during the Faces component binding process.
 * @param htmlPanelGroup the bound HtmlPanelGroup
 */
public void setTopHtmlPanelGroup(HtmlPanelGroup htmlPanelGroup) {
  _topHtmlPanelGroup = htmlPanelGroup;
}

// methods =====================================================================
/**
 * Builds the components of the top and bottom HtmlPanelGroup based upon the content
 * of the PageCursor.
 */
public void build() {
  build(getTopHtmlPanelGroup(), getPageCursor(), false);
  build(getBottomHtmlPanelGroup(), getPageCursor(), true);
}

/**
 * Builds the components of an HtmlPanelGroup based upon the content
 * of the PageCursor.
 * @param navigationPanelGroup the panel group to build
 * @param cursor the underlying page cursor
 * @param isBottom true if we are building the bottom panel
 */
private void build(HtmlPanelGroup masterPanelGroup,
                    PageCursor cursor,
                    boolean isBottom) {

  // clear existing children, return if there is nothing to build
  if (masterPanelGroup != null) {
    masterPanelGroup.getChildren().clear();
  }
  if ((masterPanelGroup == null) || (cursor == null)) {
    return;
  }

  // don't build the bottom panel if it's not really necessary
  if (isBottom && ((cursor.getEndRecord() - cursor.getStartRecord()) < 4)) {
    return;
  }

  // initialize parameters
  int nPage;
  String sMsg;
  String sRecordsPerPageMsg;
  HtmlCommandLink cmdLink;
  HtmlOutputText outText;
  FacesContextBroker ctxBroker = new FacesContextBroker();
  FacesContext facesContext = ctxBroker.getFacesContext();
  MessageBroker msgBroker = ctxBroker.extractMessageBroker();
  String sKeyPfx = getResourceKeyPrefix();

  // check the cursor
  cursor.checkCurrentPage();
  int nStartPage = cursor.getStartPage();
  int nEndPage = cursor.getEndPage();
  int nTotalPageCount = cursor.getTotalPageCount();

  // add the result text
  Integer[] args = new Integer[3];
  if (nTotalPageCount == 0) {
    sMsg = msgBroker.retrieveMessage(sKeyPfx + "nomatch", args);
  } else {
    args[0] = cursor.getStartRecord();
    args[1] = cursor.getEndRecord();
    args[2] = cursor.getTotalRecordCount();
    sMsg = msgBroker.retrieveMessage(sKeyPfx + "results", args);
  }

  sRecordsPerPageMsg = msgBroker.retrieveMessage(sKeyPfx + "resultsPerPage");

  // create grid to separate navigoation panel from records per page panel
  HtmlPanelGrid panelGrid = new HtmlPanelGrid();
  masterPanelGroup.getChildren().add(panelGrid);
  panelGrid.setBorder(0);
  panelGrid.setColumns(2);
  panelGrid.setWidth("100%");
  panelGrid.setColumnClasses("nav,count");

  // create navigation panel
  HtmlPanelGroup navigationPanelGroup = new HtmlPanelGroup();
  panelGrid.getChildren().add(navigationPanelGroup);

  outText = makeResultText(facesContext, sMsg);
  navigationPanelGroup.getChildren().add(outText);

  int linkCount = 0;

  // add page navigation links
  if (nTotalPageCount > 1) {

    // first and previous pages
    if (cursor.getHasPreviousPage()) {
      if (nStartPage != 1) {
        sMsg = msgBroker.retrieveMessage(sKeyPfx + "first");
        cmdLink = makePageLink(facesContext, 1, sMsg, isBottom, ++linkCount);
        navigationPanelGroup.getChildren().add(cmdLink);
      }
      nPage = cursor.getPreviousPage();
      sMsg = msgBroker.retrieveMessage(sKeyPfx + "previous");
      cmdLink = makePageLink(facesContext, nPage, sMsg, isBottom, ++linkCount);
      navigationPanelGroup.getChildren().add(cmdLink);
    }

    // pages
    for (int i = nStartPage; i <= nEndPage; i++) {
      cmdLink = makePageLink(facesContext, i, "" + i, isBottom, ++linkCount);
      navigationPanelGroup.getChildren().add(cmdLink);
    }

    // next and last pages
    if (cursor.getHasNextPage()) {
      nPage = cursor.getNextPage();
      sMsg = msgBroker.retrieveMessage(sKeyPfx + "next");
      cmdLink = makePageLink(facesContext, nPage, sMsg, isBottom, ++linkCount);
      navigationPanelGroup.getChildren().add(cmdLink);
      if (nEndPage != nTotalPageCount) {
        sMsg = msgBroker.retrieveMessage(sKeyPfx + "last");
        cmdLink = makePageLink(facesContext, nTotalPageCount, sMsg, isBottom, ++linkCount);
        navigationPanelGroup.getChildren().add(cmdLink);
      }
    }
  }

  // create records per page panel
  if (!isBottom && getChangeListenerExpression().length()>0) {
    HtmlPanelGroup resultsPerPagePanelGroup = new HtmlPanelGroup();
    panelGrid.getChildren().add(resultsPerPagePanelGroup);

    // listbox id
    String listBoxId = "recsPerPage";
    
    // Display label
    HtmlOutputLabel resultsPerPageLabel =
      makeResultsLabel(facesContext, sRecordsPerPageMsg);
    resultsPerPagePanelGroup.getChildren().add(resultsPerPageLabel);
    resultsPerPageLabel.setFor(listBoxId);

    // Create lisbox
    HtmlSelectOneListbox listBox = new HtmlSelectOneListbox();
    resultsPerPagePanelGroup.getChildren().add(listBox);
    listBox.setId(listBoxId);

    listBox.setSize(1);
    listBox.setValue(Integer.toString(cursor.getRecordsPerPage()));
    UIComponent form = findForm(masterPanelGroup);
    if (form!=null) {
      String inChangeExpression =
        "document.forms['"+form.getId()+"'].submit(); return false;";
      listBox.setOnchange(inChangeExpression);
    }

    // create listener
    Class[] parms = new Class[]{ValueChangeEvent.class};
    MethodBinding mb =
      FacesContext.getCurrentInstance().getApplication().createMethodBinding(
      getChangeListenerExpression(), parms);
    listBox.setValueChangeListener(mb);

    // create list content
    Set<Integer> rpp =
      new TreeSet<Integer>(Arrays.asList(_defaultResultsPerPage));
    rpp.add(new Integer(cursor.getRecordsPerPage()));

    for (Integer p : rpp) {
      int resultPerPage = p.intValue();
      UISelectItem selectItem = new UISelectItem();
      listBox.getChildren().add(selectItem);
      selectItem.setItemLabel(Integer.toString(resultPerPage));
      selectItem.setItemValue(Integer.toString(resultPerPage));
    }

  }
}

/**
 * Calles when record per page has changed.
 * @param event event
 */
public void onChange(ValueChangeEvent event) {
  _pageCursor.setRecordsPerPage(
    Val.chkInt(event.getNewValue().toString(),
               _pageCursor.getRecordsPerPage()));
}

/**
 * Checks an action event to determine if this is a page cursor navigation event.
 * <br/>If so, the current page of the associated page cursor is set.
 * <br/>This method will not re-execute a query, it will simply reset the
 * current page.
 * <br/>To rebuild the underlying UI components, user the setPageCursor() or
 * build() methods.
 * @param event the associated JSF action event
 * @param resetPageIfNot if true, reset the current page to 1 if this was not
 *        a page cursor navigation event
 * @return <code>true</code> page cursor event detected
 */
public boolean checkActionEvent(ActionEvent event, boolean resetPageIfNot) {

  // determine if the command is a page cursor navigation event
  boolean bWasPageCursorEvent = false;
  if (event != null) {
    UIComponent component = event.getComponent();
    String sName = getCommandAttributeName();
    String sCmd = (String) component.getAttributes().get(sName);
    if (Val.chkStr(sCmd).equalsIgnoreCase(getCommandAttributeValue())) {
      bWasPageCursorEvent = true;

      // determine the page to navigate to
      sName = getCursorPageAttributeName();
      String sPage = (String) component.getAttributes().get(sName);
      int nPage = Val.chkInt(sPage, -1);
      if (nPage < 1) {
        nPage = 1;
      }
      if (getPageCursor() != null) {
        getPageCursor().setCurrentPage(nPage);
      }
    }
  }

  // reset the current page if this was not a page cursor navigation event
  if (!bWasPageCursorEvent && resetPageIfNot) {
    if (getPageCursor() != null) {
      getPageCursor().setCurrentPage(1);
    }
  }
  return bWasPageCursorEvent;
}

/**
 * Makes an HtmlCommandLink component for UI page cursor navigation.
 * @param facesContext the active Faces context
 * @param page the subject page
 * @param pageText the text for the subject page
 * @return the new HtmlCommandLink component
 */
private HtmlCommandLink makePageLink(FacesContext facesContext,
                                      int page,
                                      String pageText, boolean isBottom, int linkCount) {
  HtmlCommandLink cmd = new HtmlCommandLink();
  String sExpr = getActionListenerExpression();
  Class a[] = {ActionEvent.class};
  MethodBinding mb = facesContext.getApplication().createMethodBinding(sExpr, a);
  cmd.setValue(pageText);
  cmd.setId((isBottom? "bottomLink_":"topLink_")+linkCount);
  cmd.setActionListener(mb);
  cmd.getAttributes().put(getCommandAttributeName(), getCommandAttributeValue());
  cmd.getAttributes().put(getCursorPageAttributeName(), "" + page);
  if (page == getPageCursor().getCurrentPage()) {
    if (getCurrentStyleClass().length() > 0) {
      cmd.setStyleClass(getCurrentStyleClass());
    }
  }
  return cmd;
}

/**
 * Makes an HtmlOutputText component for result text display.
 * @param facesContext the active Faces context
 * @param text the text to display
 * @return the new HtmlOutputText component
 */
private HtmlOutputText makeResultText(FacesContext facesContext, String text) {
  HtmlOutputText outText = new HtmlOutputText();
  outText.setEscape(false);
  outText.setValue(text);
  if (getResultStyleClass().length() > 0) {
    outText.setStyleClass(getResultStyleClass());
  }
  return outText;
}


/**
 * Makes an HtmlOutputLabel component for result text display.
 * @param facesContext the active Faces context
 * @param text the text to display
 * @return the new HtmlOutputLabel component
 */
private HtmlOutputLabel makeResultsLabel(FacesContext facesContext, String text) {
  HtmlOutputLabel outLabel = new HtmlOutputLabel();
  outLabel.setEscape(false);
  outLabel.setValue(text);
  if (getResultStyleClass().length() > 0) {
    outLabel.setStyleClass(getResultStyleClass());
  }
  return outLabel;
}

/**
 * Looks for the parent form.
 * @param component child component
 * @return form or <code>null</code> if form doesn't exist
 */
private UIComponent findForm(UIComponent component) {
  if (component==null) {
    return null;
  }
  if (component.getFamily().equals(UIForm.COMPONENT_FAMILY)) {
    return component;
  }
  return findForm(component.getParent());
}
}
