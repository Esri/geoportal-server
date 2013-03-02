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
package com.esri.gpt.framework.jsf.components;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.component.UIComponentBase;
import javax.faces.component.UIOutput;
import javax.faces.component.html.HtmlCommandLink;
import javax.faces.component.html.HtmlForm;
import javax.faces.component.html.HtmlInputHidden;
import javax.faces.component.html.HtmlOutputLink;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGrid;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.servlet.http.HttpServletRequest;

import com.esri.gpt.control.search.SearchController;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.jsf.PageContext;
import com.esri.gpt.framework.request.PageCursor;
import com.esri.gpt.framework.util.Val;


/**
 * The Class UIPagination.  JSF Component class that outputs a pagination
 * control
 */
public class UIPagination extends UIComponentBase {

//class variables =============================================================
/**
 * The Enum ComponentMapKeys.
 */
public static enum ComponentMapKeys {

/** The action listener. */
actionListener,

/** The action. */
action,

/** The page cursor. */
pageCursor,

/** The criteria page cursor. */
criteriaCursor,

/** The max enumerated pages. */
maxEnumeratedPages,

/** The page cursor total pages. */
pageCursorTotalPages,

/** The page cursor current page. */
pageCursorCurrentPage,

/** The css prefix. */
cssPrefix,

/** The id prefix. */
idPrefix,

/** The property prefix. */
propertyPrefix,

/** The render first page link. */
renderFirstPageLink,

/** The render last page link. */
renderLastPageLink,

/** The css of first page link. */
cssFirstPageLink,

/** The css of last page link. */
cssLastPageLink,

/** The css of next page link. */
cssNextPageLink,

/** The css of prev page link. */
cssPrevPageLink,

/** The css of other enumerated page links. */
cssOtherEnumdPageLinks,

/** The css of the main panel. */
cssMainPanel,

/** The css current page enum link. */
cssCurrentPageEnumLink,



}

/**
 * The Label.
 */
public static enum Label {

/** The label. */
label,

/** The label values. */
labelValueList,

/** The label position. */
labelPosition,

/** The label CSS. */
labelCSS,

/** The label when there are no results */
labelNoResults,

/** Position string for label above. */
above,

/** Position string for label below. */
below,

/** Position string for label on left side. */
leftSide,

/** Position string for label on right side. */
rightSide,

}

/**
 * The Page Events.
 */
public static enum PageEvents {

goToPage
}

/**
 * The Enum SaveStateKeys.
 */
private static enum SaveStateKeys {

/** The super object. */
superObject,

/** The added components. */
addedComponents,

/** The id prefix. */
idPrefix
}

/** The JSF Component type. */
public static final String COMPONENT_TYPE   = "com.esri.gpt.faces.Pagination"; //$NON-NLS-1$

/** The JSF Component family. */
public static final String COMPONENT_FAMILY = COMPONENT_TYPE;

/** The variable to store in the request so as to identify
 *  how many UIPagination components are in the JSF page. */
private static String      REQUEST_VARNAME  = "PAGINATION_OBJ_"; //$NON-NLS-1$

/** The class logger *. */
private static Logger             LOG       = Logger
.getLogger(UIPagination.class
    .getCanonicalName());

/** The message broker. */
private MessageBroker msgBroker = null;

/** The Constant JSFBEAN_SEARCH_CONTROLLER. */
private static final String JSFBEAN_SEARCH_CONTROLLER = "SearchController";



//instance variables ==========================================================
/** The id prefix. */
private String             idPrefix;

/** The added components. */
private boolean            addedComponents  = false;

/** The ui support. */
private final UISupport          uiSupport        = new UISupport();

/** The page cursor for the component. */
private PageCursor         pageCursor       = null;

/** The actionlistener method. */
private final MethodBinding      actionListenerMethod = null;

/** The action method. */
private MethodBinding      actionMethod = null;

/** The propertyPrefix */
private final String propertyPrefix = null;

/** The css prefix. */
private String cssPrefix = null;

private boolean useAjax = true;

//properties ==================================================================

/**
 * Sets the id prefix.
 * 
 * @param idPrefix the new id prefix
 */
public void setIdPrefix(String idPrefix) {
  this.idPrefix = idPrefix;
}

/**
 * Gets the id prefix.
 * 
 * @return the id prefix
 */
public String getIdPrefix() {
  if (idPrefix != null && !"".equals(idPrefix)) { //$NON-NLS-1$
    return idPrefix;
  }
  /*idPrefix = (String) this.getAttributes()
      .get(ComponentMapKeys.idPrefix.name());
  if (idPrefix != null && !"".equals(idPrefix)) {
    return idPrefix;
  }*/
  FacesContextBroker broker = new FacesContextBroker();
  HttpServletRequest req = broker.extractHttpServletRequest();
  Integer index = (Integer) req.getAttribute(REQUEST_VARNAME);
  if (index == null) {
    index = 0;
  } else {
    index = index + 1;
  }
  req.setAttribute(REQUEST_VARNAME, index);

  idPrefix = this.getId() + "_UIPagination" + index + "_"; //$NON-NLS-1$ //$NON-NLS-2$
  return idPrefix;
}

private PageCursor getCriteriaPageCursor() {
  PageCursor pageCursor = null;
  try {
    
  Object pageCursorObj = this.getAttributes()
  .get(ComponentMapKeys.criteriaCursor.name());
   pageCursorObj = ((ValueBinding) pageCursorObj)
  .getValue(this.getFacesContext());
   if(pageCursorObj instanceof PageCursor) {

    pageCursor = (PageCursor) pageCursorObj;

   } else {
    throw new ConfigurationException("Search Criteria PageCursor in JSF " +
    		"Paginator should be an"  //$NON-NLS-1$
      + "instance of " + PageCursor.class.getCanonicalName()); //$NON-NLS-1$
   }
  } catch (Throwable t) {
    // for backward compatibility
    return this.getPageCursor();
  }

   return pageCursor;
}
/**
 * Gets the page cursor associated with this Component.
 * 
 *  
 * @return the page cursor
 */
private PageCursor getPageCursor() {
  /*if(this.pageCursor != null) {
    return pageCursor;
  }*/
  
  //this.pageCursor = this.readController().getSearchResult().getPageCursor();
 
  Object pageCursorObj = this.getAttributes()
    .get(ComponentMapKeys.pageCursor.name());
  pageCursorObj = ((ValueBinding) pageCursorObj)
    .getValue(this.getFacesContext());
  if(pageCursorObj instanceof PageCursor) {

    pageCursor = (PageCursor) pageCursorObj;

  } else {
    throw new ConfigurationException("PageCursor in JSF Paginator should be an"  //$NON-NLS-1$
        + "instance of " + PageCursor.class.getCanonicalName()); //$NON-NLS-1$
  }

  return this.pageCursor;

}

/**
 * Gets the actionListener method object. 
 * 
 * @return the actionListener method object
 */
private MethodBinding getActionListenerMethod() {
  if(actionListenerMethod != null) {
    return actionListenerMethod;
  }
  if(!this.getAttributes().containsKey
      (ComponentMapKeys.actionListener.name())) {
    return null;
  }
  Object actionListener = this.getAttributes()
  .get(ComponentMapKeys.actionListener.name());

  if(!(actionListener instanceof MethodBinding)) {
    throw new ConfigurationException("ActionListener for Pagintor should " + //$NON-NLS-1$
        " be a valid method binding. Object derived =  " + actionListener) ; //$NON-NLS-1$
  }

  return (MethodBinding) actionListener;


}

/**
 * Gets the css class prefix.
 * 
 * @return the css prefix
 */
private String getCssClassPrefix() {
  if(this.cssPrefix != null ) {
    return cssPrefix;
  }
  if(!this.getAttributes().containsKey(ComponentMapKeys.cssPrefix.name())) {
    cssPrefix = ""; //$NON-NLS-1$
  } else {
    cssPrefix = (String)
     this.getAttributes().get(ComponentMapKeys.cssPrefix.name());
  }
  return cssPrefix; 
}

/**
 * Gets the label.
 * 
 * @return the label (never null)
 */
@SuppressWarnings("unchecked") //$NON-NLS-1$
private String getLabel() {
  
  String propName = Label.label.name();
  if(this.getPageCursor().getTotalRecordCount() <= 0) {
    propName = Label.labelNoResults.name();
  }
  
  String label = null;
  Object tmp = null;
  if(this.getAttributes().containsKey(propName)) {
    tmp =  this.getAttributes().get(propName);
    if(tmp instanceof ValueBinding) {
      tmp = ((ValueBinding) tmp).getValue(this.getFacesContext());
    }
    if(tmp != null) {
      label = tmp.toString();           
    }
  } 
  
  if (label == null || "".equals(label)) { //$NON-NLS-1$
    return ""; //$NON-NLS-1$
  }
  if(this.getPageCursor().getTotalRecordCount() <= 0) {
    return label;
  }
  List labelVals = null;
  if(this.getAttributes().containsKey(Label.labelValueList.name())) {
    labelVals = (List)
       this.getAttributes().get(Label.labelValueList.name());
  }
  if(labelVals == null || labelVals.size() <= 0) {
    return label;
  }
  int i = 0;
  for(Object tknValue : labelVals) {
    if(tknValue != null && tknValue instanceof String) {
      label = label.replaceAll("\\{"+ i + "\\}",  //$NON-NLS-1$ //$NON-NLS-2$
        tknValue.toString());
    } else if (tknValue != null && tknValue instanceof ValueBinding) {
      label = label.replaceAll("\\{"+ i + "\\}",  //$NON-NLS-1$ //$NON-NLS-2$
        ((ValueBinding) tknValue).getValue(this.getFacesContext()).toString());
    }
    i++;
  }
  
  return label;
}

/**
 * Gets the label position.
 * 
 * @return the label position
 */
private String getLabelPosition() {
  if(!this.getAttributes().containsKey(Label.labelPosition.name())) {
    return Label.rightSide.name();
  }
  String strPos = null;
  Object obj = this.getAttributes().get(Label.labelPosition.name());
  if(obj instanceof String) {
    strPos = obj.toString();
  }
  if(obj instanceof ValueBinding) {
    strPos = ((ValueBinding) obj).getValue(this.getFacesContext()).toString();  
  }
  if(strPos == null) {
    return Label.rightSide.name();
  }
  strPos = strPos.trim();
  if(strPos.equalsIgnoreCase(Label.above.name()) ||
      strPos.equalsIgnoreCase(Label.below.toString()) ||
      strPos.equalsIgnoreCase(Label.leftSide.name()) ||
      strPos.equalsIgnoreCase(Label.rightSide.name())) {
    return strPos;
  }
  return Label.rightSide.name();
}

/**
 * Gets the action biding.
 *  
 * @return the action biding (possibly null)
 */
@SuppressWarnings("unused") //$NON-NLS-1$
private MethodBinding getActionBinding() {
  if(this.actionMethod == null) {
    if(!this.getAttributes().containsKey(ComponentMapKeys.action.name())) {
      return null;
    }
    this.actionMethod = (MethodBinding)
    this.getAttributes().get(ComponentMapKeys.action.name());
  }
  return this.actionMethod;
}

//methods =====================================================================
/**
 * Inits the components.
 */
@SuppressWarnings("unchecked")  //$NON-NLS-1$
private void initComponents() {

  if (addedComponents) {
    return;
  }
  MethodBinding actionListenerMethod = this.getActionListenerMethod();
  MethodBinding actionMethod = this.getActionBinding();
  
  // Statistics tag
  HtmlOutputText txtLabel = new HtmlOutputText();
  txtLabel.setId(this.getIdTxtStats());
  txtLabel.setValue(this.getLabel());
  txtLabel.setStyleClass(Messages.getString("gpt.jsfcomponent.pageCursorStatisticsLabel")); //$NON-NLS-1$
  txtLabel.setEscape(false);
  String lblPosition = this.getLabelPosition();
  
  // rz
  HtmlPanelGrid mainPanel = new HtmlPanelGrid();
  mainPanel.setColumns(100);
  mainPanel.setId(this.getIdMainPanel());
  mainPanel.setStyleClass(Messages.getString("gpt.jsfcomponent.pageCursorMainPanel")); //$NON-NLS-1$
  mainPanel.setCellpadding("0"); //$NON-NLS-1$
  mainPanel.setCellspacing("0"); //$NON-NLS-1$
 
  // Add label as header if applicable
  if(Label.above.name().equalsIgnoreCase(lblPosition)) {
    mainPanel.getFacets().put("header", txtLabel); //$NON-NLS-1$
  }
  
  // Add label as footer if applicable
  if(Label.below.name().equalsIgnoreCase(lblPosition)) {
    mainPanel.getFacets().put("footer", txtLabel); //$NON-NLS-1$
  }
 
  // Add label to left side if applicable
  if(Label.leftSide.name().equalsIgnoreCase(lblPosition)) {
    mainPanel.getChildren().add(txtLabel);
  }

  // firstPage link
  UIComponent link = null;
  if (!useAjax) {
    HtmlCommandLink commandLink = new HtmlCommandLink();
    if (actionListenerMethod != null) {
      commandLink.setActionListener(actionListenerMethod);
    }
    if (actionMethod != null) {
      commandLink.setAction(actionMethod);
    }
    commandLink.setStyleClass(this.getCssClassFirstPage());
    commandLink.setTitle(this
        .getProperty("catalog.general.pageCursor.first.alt"));
    link = commandLink;
  } else {
    HtmlOutputLink outputLink = new HtmlOutputLink();
    outputLink.setStyleClass(this.getCssClassFirstPage());
    outputLink.setValue("#");
    outputLink.setTitle(this
        .getProperty("catalog.general.pageCursor.first.alt"));
    link = outputLink;
  }
  link.getAttributes().put(PageEvents.goToPage.name(), 1);
  link.setId(this.getIdFirstPage());
  HtmlOutputText linkText = new HtmlOutputText();
  linkText.setId(this.getIdPrefix() + "txtFirstPage"); //$NON-NLS-1$
  linkText.setValue(this.getProperty("catalog.general.pageCursor.first")); //$NON-NLS-1$
  link.getChildren().add(linkText);
  mainPanel.getChildren().add(link);

  // previousPage link
  link = null;
  if (!useAjax) {
    HtmlCommandLink commandLink = new HtmlCommandLink();
    if (actionListenerMethod != null) {
      commandLink.setActionListener(actionListenerMethod);
    }
    if (actionMethod != null) {
      commandLink.setAction(actionMethod);
    }
    commandLink.setStyleClass(this.getCssClassPreviousPage());
    commandLink.setTitle(this
        .getProperty("catalog.general.pageCursor.previous.alt"));
    link = commandLink;
  } else {
    HtmlOutputLink outputLink = new HtmlOutputLink();
    outputLink.setValue("#");
    outputLink.setStyleClass(this.getCssClassPreviousPage());
    outputLink.setTitle(this
        .getProperty("catalog.general.pageCursor.previous.alt"));
    link = outputLink;
    
  }
  link.setId(this.getIdPreviousPage());
  linkText = new HtmlOutputText();
  linkText.setId(this.getIdPrefix() + "txtPreviousPage"); //$NON-NLS-1$
  linkText.setValue(this.getProperty("catalog.general.pageCursor.previous")); //$NON-NLS-1$
  link.getChildren().add(linkText);
  mainPanel.getChildren().add(link);

  // Enumerated Pages link
  HtmlPanelGrid grid = new HtmlPanelGrid();
  grid.setId(this.getIdEnumPagesPanel());
  mainPanel.getChildren().add(grid);
  grid.setCellpadding("0"); //$NON-NLS-1$
  grid.setCellspacing("0"); //$NON-NLS-1$

  // nextPage link
  link = null;
  if (!useAjax) {
    HtmlCommandLink commandLink = new HtmlCommandLink();
    if (actionListenerMethod != null) {
      commandLink.setActionListener(actionListenerMethod);
    }
    if (actionMethod != null) {
      commandLink.setAction(actionMethod);
    }
    commandLink.setStyleClass(this.getCssClassNextPage());
    link = commandLink;
  } else {
    HtmlOutputLink outputLink = new HtmlOutputLink();
    outputLink.setValue("#");
    outputLink.setStyleClass(this.getCssClassNextPage());
    outputLink.setTitle(this.getProperty("catalog.general.pageCursor.next.alt")); 
    link = outputLink;
  }
  link.setId(this.getIdNextPage());
  linkText = new HtmlOutputText();
  linkText.setId(this.getIdPrefix() + "txtNextPage"); //$NON-NLS-1$
  linkText.setValue(this.getProperty("catalog.general.pageCursor.next")); //$NON-NLS-1$
  link.getChildren().add(linkText);
  mainPanel.getChildren().add(link);

  // lastPage link
  link = null;
  if (!useAjax) {
    HtmlCommandLink commandLink = new HtmlCommandLink();
    if (actionListenerMethod != null) {
      commandLink.setActionListener(actionListenerMethod);
    }
    if (actionMethod != null) {
      commandLink.setAction(actionMethod);
    }
    
    commandLink.setStyleClass(this.getCssClassLastPage());
    commandLink.setTitle(this
        .getProperty("catalog.general.pageCursor.last.alt"));
    link = commandLink;
  } else {
    HtmlOutputLink outputLink = new HtmlOutputLink();
    outputLink.setValue("#");
    outputLink.setStyleClass(this.getCssClassLastPage());
    outputLink.setTitle(this
        .getProperty("catalog.general.pageCursor.last.alt"));
    link = outputLink;
  }
  link.setId(this.getIdLastPage());
  linkText = new HtmlOutputText();
  linkText.setId(this.getIdPrefix() + "txtLastPage"); //$NON-NLS-1$
  linkText.setValue(this.getProperty("catalog.general.pageCursor.last")); //$NON-NLS-1$
  link.getChildren().add(linkText);
  mainPanel.getChildren().add(link);

  HtmlInputHidden inpHidden = new HtmlInputHidden();
  inpHidden.setId(this.getIdPrefix() + "pageCursorTotalRecords");
  Object obj = 
    this.getAttributes().get(ComponentMapKeys.pageCursorTotalPages.name());
  if(obj instanceof ValueBinding) {
    inpHidden.setValueBinding("value", (ValueBinding)obj);
    mainPanel.getChildren().add(inpHidden);
  }
  inpHidden = new HtmlInputHidden();
  inpHidden.setId(this.getIdPrefix() + "pageCurrentPage");
  obj = 
    this.getAttributes().get(ComponentMapKeys.pageCursorCurrentPage.name());
  if(obj instanceof ValueBinding) {
    inpHidden.setValueBinding("value", (ValueBinding)obj);
    mainPanel.getChildren().add(inpHidden);
  }


  // Add label to right side if applicable
  if(Label.rightSide.name().equalsIgnoreCase(lblPosition)) {
    mainPanel.getChildren().add(txtLabel);
  }


  // enclose in form if necessary
  if (uiSupport.enclosedInForm(this)) {
    this.getChildren().add(mainPanel);

  } else {

    HtmlForm form = new HtmlForm();
    form.setId(this.getIdPrefix() + "frmPagination"); //$NON-NLS-1$
    form.getChildren().add(mainPanel);
    this.getChildren().add(form);
  }

  addedComponents = true;

}

/**
 * Decides which components should be rendered for the current view.
 * 
 * @param context the context
 */
@SuppressWarnings("unchecked")  
private void setRenderComponents() {

  PageCursor pageCursor = this.getPageCursor();


  UIComponent component = null;

  // Next component
  component = uiSupport.findComponent(this, this.getIdNextPage());
  component.setRendered(pageCursor.getHasNextPage());
  if(component.isRendered()) {
   component.getAttributes().put
    (PageEvents.goToPage.name(), pageCursor.getNextPage());
   if(component instanceof HtmlOutputLink) {
     ((HtmlOutputLink)component).setOnclick(this.readJscriptOnclickPage(
         pageCursor.getCurrentPage() + 1));
   }
  }
  

  // Previous component
  component = uiSupport.findComponent(this, this.getIdPreviousPage());
  component.setRendered(pageCursor.getHasPreviousPage());
  if(component.isRendered()) {
    component.getAttributes().put
    (PageEvents.goToPage.name(), pageCursor.getPreviousPage());
    if(component instanceof HtmlOutputLink) {
      ((HtmlOutputLink)component).setOnclick(this.readJscriptOnclickPage(
          pageCursor.getCurrentPage() - 1));
    }
  }

  // First page component
  component = uiSupport.findComponent(this, this.getIdFirstPage());
  component.setRendered((pageCursor.getCurrentPage() > 1 )
      && this.getRenderFirstPageLink());
  if(component instanceof HtmlOutputLink) {
    ((HtmlOutputLink)component).setOnclick(this.readJscriptOnclickPage(1));
  }
  // this.getRenderFirstPageLink();


  // Last page component
  component = uiSupport.findComponent(this, this.getIdLastPage());
  component.setRendered(
      (pageCursor.getCurrentPage() != pageCursor.getTotalPageCount())
      && (pageCursor.getTotalPageCount() > 0)
      && this.getRenderFirstPageLink());
  if(component.isRendered()) {
    component.getAttributes().put
    (PageEvents.goToPage.name(), this.getPageCursor().getTotalPageCount());
    if(component instanceof HtmlOutputLink) {
      ((HtmlOutputLink)component).setOnclick(this.readJscriptOnclickPage(
          pageCursor.getTotalPageCount()));
    }
  }
  
  // Label component
  component = uiSupport.findComponent(this, this.getIdTxtStats());
  ((UIOutput) component).setValue(getLabel());
  


}

/**
 * Render numbers that represent pages.
 */
@SuppressWarnings("unchecked")  //$NON-NLS-1$
public void renderPageNumbers() {

  UIComponent pnlEnumPages = uiSupport.findComponent(this, 
      this.getIdEnumPagesPanel());
  if(!pnlEnumPages.isRendered()) {
    return;
  }
  Object obj = 
    this.getAttributes().get(ComponentMapKeys.maxEnumeratedPages.name());
  if(obj == null) {
    return;
  }
  double numEnumPages = Val.chkInt(obj.toString(), -1);
  if(numEnumPages < 2) {
    LOG.warning(this.getClass().getCanonicalName() + " recieved attribute " + //$NON-NLS-1$
        " maxEnumeratedPages not null but less than 2. " + //$NON-NLS-1$
        " if this number is defined in your JSP page it should be" + //$NON-NLS-1$
    " >= 2."); //$NON-NLS-1$
    return;
  }
  PageCursor pageCursor = this.getPageCursor();
  if(pageCursor.getTotalPageCount() <= 1) {
    pnlEnumPages.getChildren().clear();
    return;
  }

  MethodBinding pActionMethodBind = this.getActionListenerMethod();
  MethodBinding actionMethod = this.getActionBinding();

  int currentPage = pageCursor.getCurrentPage();
  int lowNum = currentPage - 
  (int) Math.floor((numEnumPages / 2.0));
  int highNum = currentPage 
  + (int) Math.floor(numEnumPages / 2.0);
  if(lowNum <= 0) {
    highNum = highNum + Math.abs(lowNum) + 1;
  }
  if(highNum > pageCursor.getTotalPageCount() && lowNum > 1) {
    lowNum = lowNum - (highNum - pageCursor.getTotalPageCount());
  }

  pnlEnumPages.getChildren().clear();
  if(pnlEnumPages instanceof HtmlPanelGrid) {
    ((HtmlPanelGrid) pnlEnumPages).setColumns((int)numEnumPages);
  }

  UIComponent link = null;
  HtmlOutputText outputText = null;
  String propertyPrefix = 
    this.getProperty("catalog.general.pageCursor.pagesPrefix.alt"); //$NON-NLS-1$
  for(int i = lowNum; i <= highNum && i <= pageCursor.getTotalPageCount(); i++) {
    if(i <= 0) {
      continue;
    }
    if (useAjax) {
      link = new HtmlOutputLink();
    } else {
      link = new HtmlCommandLink();
    }
    link.setId(this.getIdEnumPageLink(i));
    // commandLink.setValue(i);
    if (!useAjax) {
      HtmlCommandLink commandLink = (HtmlCommandLink) link;
      if (pActionMethodBind != null) {
        commandLink.setActionListener(pActionMethodBind);
      }
      if (actionMethod != null) {
        commandLink.setAction(actionMethod);
      }
      if (i != currentPage) {
        commandLink.setStyleClass(this.getCssClassOtherEnumPageLinks());
      } else {
        commandLink.setStyleClass(this.getCssClassCurrentEnumPageLink());

        // If you change this then change decode
        link.getAttributes().put(PageEvents.goToPage.name(), i);
        commandLink.setTitle(propertyPrefix + " " + i);
      }
    } else {
      HtmlOutputLink outputLink = (HtmlOutputLink) link;
      if (i != currentPage) {
        outputLink.setStyleClass(this.getCssClassOtherEnumPageLinks());
      } else {
        outputLink.setStyleClass(this.getCssClassCurrentEnumPageLink());
        outputLink.setValue("#");
        link.getAttributes().put(PageEvents.goToPage.name(), i);
        outputLink.setTitle(propertyPrefix + " " + i);
      }
      outputLink.setOnclick(this.readJscriptOnclickPage(i));
    }
    outputText = new HtmlOutputText();
    outputText.setValue(i);
    outputText.setId(this.getIdPrefix() + "txtPageNum" + i); //$NON-NLS-1$
    link.getChildren().add(outputText);
    pnlEnumPages.getChildren().add(link);
    
  }


}

/**
 * Encode begin.
 * 
 * @param context the context
 * 
 * @throws IOException Signals that an I/O exception has occurred.
 * 
 * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
 */

@Override
public void encodeBegin(FacesContext context) throws IOException {
  super.encodeBegin(context);
  if (!this.isRendered()) {
    return;
  }
  if (LOG.isLoggable(Level.FINE)) {
    LOG.fine("Beginning encoding of " + this.getClass().getCanonicalName()); //$NON-NLS-1$
  }

  this.initComponents();
  this.setRenderComponents();
  this.renderPageNumbers();



}




/**
 * Encode end.
 * 
 * @param context the context
 * 
 * @throws IOException Signals that an I/O exception has occurred.
 * 
 * @see javax.faces.component.UIComponentBase#encodeEnd(javax.faces.context.FacesContext)
 */

@Override
public void encodeEnd(FacesContext context) throws IOException {

  if (!this.isRendered()) {
    return;
  }
  if (LOG.isLoggable(Level.FINE)) {
    LOG.fine("Ending encoding of " + this.getClass().getCanonicalName()); //$NON-NLS-1$
  }
  super.encodeEnd(context);
}






/**
 * Restore state.
 * 
 * @param context the context
 * @param state the state
 * 
 * @see javax.faces.component.UIComponentBase#restoreState(javax.faces.context.FacesContext,
 * java.lang.Object)
 */

@SuppressWarnings("unchecked")  //$NON-NLS-1$
@Override
public void restoreState(FacesContext context, Object state) {

  Map map = (Map) state;
  Object superObject = map.get(SaveStateKeys.superObject.name());
  this.setIdPrefix((String) map.get(SaveStateKeys.idPrefix.name()));
  this.addedComponents = (Boolean) map
  .get(SaveStateKeys.addedComponents.name());
  super.restoreState(context, superObject);
}

/**
 * Save state.
 * 
 * @param context the context
 * 
 * @return the object
 * 
 * @see javax.faces.component.UIComponentBase#saveState(javax.faces.context.FacesContext)
 */

@SuppressWarnings("unchecked")  //$NON-NLS-1$
@Override
public Object saveState(FacesContext context) {
  Object superObject = super.saveState(context);
  Map map = new HashMap();
  map.put(SaveStateKeys.superObject.name(), superObject);
  map.put(SaveStateKeys.idPrefix.name(), this.getIdPrefix());
  map.put(SaveStateKeys.addedComponents.name(), this.addedComponents);

  return map;
}


/**
 * Gets the family.
 * 
 * @return The family the object belongs to.
 */
@Override
public String getFamily() {

  return COMPONENT_FAMILY;
}


/**
 * Gets the id main panel.
 * 
 * @return the id main panel
 */
private String getIdMainPanel() {
  return getIdPrefix() + "pnlMain"; //$NON-NLS-1$
}

/**
 * Gets the id next.
 * 
 * @return the id next
 */
private String getIdNextPage() {
  return this.getIdPrefix() + "lnkNext"; //$NON-NLS-1$
}

/**
 * Gets the id previous.
 * 
 * @return the id previous
 */
private String getIdPreviousPage() {
  return this.getIdPrefix() + "lnkPrevious"; //$NON-NLS-1$
}

/**
 * Gets the id number panel.
 * 
 * @return the id number panel
 */
private String getIdNumberPanel() {
  return this.getIdPrefix() + "pnlNumpanel"; //$NON-NLS-1$
}

/**
 * Gets the id first page.
 * 
 * @return the id first page
 */
private String getIdFirstPage() {
  return this.getIdPrefix() + "lnkFirstPage"; //$NON-NLS-1$
}

/**
 * Gets the id last page.
 * 
 * @return the id last page
 */
private String getIdLastPage() {
  return this.getIdPrefix() + "lnkLastPage"; //$NON-NLS-1$
}

/**
 * Gets the id of the enumerated pages panel.
 * 
 * @return the id enumerated pages panel
 */
private String getIdEnumPagesPanel() {
  return this.getIdPrefix() + "pnlEnumedPages"; //$NON-NLS-1$

}

/**
 * Gets the id of the page link.
 * 
 * @param i the page index number
 * 
 * @return the id of the page (never null)
 */
private String getIdEnumPageLink(int i) {
  return this.getIdPrefix() + "lnkEnumedPage_" + i; //$NON-NLS-1$
}

/**
 * Gets the id txt stats.
 * 
 * @return the id txt stats
 */
private String getIdTxtStats() {
  return this.getIdPrefix() + "txtStats"; //$NON-NLS-1$
}

/**
 * Gets the CSS of the main panel.
 * 
 * @return the CSS class name
 */
private String getCssClassMainPanel() {
  return getCssClassPrefix() + "pnlMain"; //$NON-NLS-1$
}

/**
 * Gets the CSS class of the next page component.
 * 
 * @return the CSS class name
 */
private String getCssClassNextPage() {
  return this.getCssClassPrefix() + 
  Messages.getString("gpt.jsfcomponent.cssnextPage");  //$NON-NLS-1$
}

/**
 * Gets the CSS class name of the previous component.
 * 
 * @return the CSS class name
 */
private String getCssClassPreviousPage() {
  return this.getCssClassPrefix() + 
  Messages.getString("gpt.jsfcomponen.cssprevPage"); //$NON-NLS-1$
}


/**
 * Gets the CSS class of first page.
 * 
 * @return the CSS class of the first page
 */
private String getCssClassFirstPage() {


  return this.getCssClassPrefix() 
  + Messages.getString("gpt.jsfcomponent.cssfirstPage"); //$NON-NLS-1$
}

/**
 * Gets the CSS class of the last page.
 * 
 * @return the CSS class of the last page
 */
private String getCssClassLastPage() {
  return this.getCssClassPrefix() + 
  Messages.getString("gpt.jsfcomponent.csslastPage"); //$NON-NLS-1$
}


/**
 * Gets the Css class other page links.
 * 
 *  * 
 * @return the CSS class of a page link
 */
private String getCssClassOtherEnumPageLinks() {

  return Messages.getString("gpt.jsfcomponent.pageCursorNonCurrentPage"); //$NON-NLS-1$
}

/**
 * Gets the CSS of the current page link.
 * 
 * 
 * @return the CSS class of a page link
 */
private String getCssClassCurrentEnumPageLink() {

  return Messages.getString("gpt.jsfcomponent.pageCursorCurrentPage"); //$NON-NLS-1$
}

/**
 * Gets the render first page link.
 * 
 * @return the render first page link (default true)
 */
@SuppressWarnings("unchecked") //$NON-NLS-1$
public boolean getRenderFirstPageLink() {
  String key = ComponentMapKeys.renderFirstPageLink.name();
  Map map = this.getAttributes();
  if(!map.containsKey(key)) {
    return true;
  }
  Object obj = map.get(key);
  if(obj == null) {
    return true;
  }
  return Val.chkBool(obj.toString(), true);
}

/**
 * Gets the render last page link.
 * 
 * @return the render last page link
 */
@SuppressWarnings("unchecked") //$NON-NLS-1$
public boolean getRenderLastPageLink() {
  String key = ComponentMapKeys.renderLastPageLink.name();
  Map map = this.getAttributes();
  if(!map.containsKey(key)) {
    return true;
  }
  Object obj = map.get(key);
  if(obj == null) {
    return true;
  }
  return Val.chkBool(obj.toString(), true);

}

/**
 * Gets the property prefix.
 * 
 * @return the property prefix (trimmed, never null)
 */
public String getPropertyPrefix() {

  if(this.propertyPrefix != null) {
    return this.propertyPrefix;
  }
  /*

  Object obj = this.getAttributes().get(ComponentMapKeys.propertyPrefix);

  if(obj == null) {
    this.propertyPrefix = ""; //$NON-NLS-1$
  }
  else if(obj instanceof String) {
    this.propertyPrefix = obj.toString().trim();
  }
  else if(obj instanceof ValueBinding) {
    this.propertyPrefix = 
      ((ValueBinding)obj).getValue(this.getFacesContext()).toString().trim();
  } */
  //return this.propertyPrefix;
  return ""; //$NON-NLS-1$
}


/**
 * Gets the property from the property file.
 * 
 * @param property the property key
 * 
 * @return the property

 */
private String getProperty(String property) {
  String propPrefix = this.getPropertyPrefix();

  if(this.msgBroker == null) {
    this.msgBroker = PageContext.extractMessageBroker();
  }
  return this.msgBroker.getMessage(propPrefix + property).getSummary();

}

/**
 * Deals with interpretating the incoming prameters to fill in 
 * pagination details
 * 
 * @param context
 */
@Override
public void decode(FacesContext context) {
  
  PageCursor cursor = this.getCriteriaPageCursor();
  if(cursor == null) {
    return;
  }
  UISupport support = new UISupport();
  Map requestMap = context.getExternalContext().getRequestParameterMap();
  String clientId = getClientId(context);
  String prefix = this.getIdPrefix();
  Iterator iter = requestMap.values().iterator();
  while(iter.hasNext()) {
    try {
      Object obj = iter.next();
      if (obj == null) {
        continue;
      }
      String value = null;
      UIComponent component = null;
      HtmlCommandLink link = null;
      HtmlOutputText txt = null;
      value = obj.toString();
      if (value.contains(clientId) && value.contains(prefix)) {
        component = support.findComponent(this, value.substring(value
            .indexOf(":") + 1));
      } 
      
      if (!(component instanceof HtmlCommandLink )) {
        continue;
      }

      link = (HtmlCommandLink) component;
      if (link.getId().contains(this.getIdFirstPage())) {
        cursor.setCurrentPage(1);
      } else if (link.getId().contains(this.getIdLastPage())) {
        cursor.setCurrentPage(cursor.getTotalPageCount());
      } else if (link.getId().contains(this.getIdNextPage())) {
        cursor.setCurrentPage(cursor.getCurrentPage() + 1);
      } else if (link.getId().contains(this.getIdPreviousPage())) {
        cursor.setCurrentPage(cursor.getCurrentPage() - 1);
      } else {
        txt = (HtmlOutputText) component.getChildren().get(0);
        int page = Val.chkInt(txt.getValue().toString(), Integer.MIN_VALUE);
        if (page >= 1) {
          cursor.setCurrentPage(page);
        }
      }
 
    } catch (Throwable e) {
      LOG.log(Level.FINER, "", e);
    }
    
  }
      
}

private String readJscriptOnclickPage(int page) {
  return "javascript:return scSetPageTo("+ page +");";
}



}
