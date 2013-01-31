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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

import com.esri.gpt.framework.util.Val;

/**
 * The Class PaginationTag.  Tag class associated with UIPagination.
 */
public class PaginationTag extends UIComponentTag {


// class variables =============================================================
/** The LOG. */
private static Logger LOG = 
  Logger.getLogger(PaginationTag.class.getCanonicalName());

// instance variables ==========================================================
/** The action listener. */
private String actionListener;

/** The action. */
private String action;

/** The page cursor. */
private String pageCursor;

private String criteriaPageCursor;

/** The max enumerated pages. */
private String maxEnumeratedPages;

/** The css prefix. */
private String cssPrefix;

/** The id prefix. */
private String idPrefix;

/** The property prefix in the property file. */
private String propertyPrefix;

/** The render first page. */
private String renderFirstPage;

/** The render last page. */
private String renderLastPage;

/** The tag support. */
private final TagSupport tagSupport = new TagSupport();

/** The label. */
private String label;

/** The label values. */
private String labelValues;

/** The label position. */
private String labelPosition;

/** The label no results. */
private String labelNoResults;

// properties ==================================================================

/**
 * Gets the label when there are no results.
 * 
 * @return the label for no results
 */
public String getLabelNoResults() {
  return labelNoResults;
}

/**
 * Sets the label when there are no results.
 * 
 * @param labelNoResults the new label no results
 */
public void setLabelNoResults(String labelNoResults) {
  this.labelNoResults = labelNoResults;
}

/**
 * Gets the action listener.
 * 
 * @return the action listener
 */
public String getActionListener() {
  return actionListener;
}

/**
 * Sets the action listener.
 * 
 * @param actionListener the new action listener
 */
public void setActionListener(String actionListener) {
  this.actionListener = actionListener;
}

/**
 * Gets the action.
 * 
 * @return the action
 */
public String getAction() {
  return action;
}

/**
 * Sets the action.
 * 
 * @param action the new action
 */
public void setAction(String action) {
  this.action = action;
}

/**
 * Gets the page cursor.
 * 
 * @return the page cursor
 */
public String getPageCursor() {
  return pageCursor;
}

/**
 * Sets the page cursor.
 * 
 * @param pageCursor the new page cursor
 */
public void setPageCursor(String pageCursor) {
  this.pageCursor = pageCursor;
}

/**
 * Gets the max enumerated pages.
 * 
 * @return the max enumerated pages
 */
public String getMaxEnumeratedPages() {
  return maxEnumeratedPages;
}

/**
 * Sets the max enumerated pages.
 * 
 * @param maxEnumeratedPages the new max enumerated pages
 */
public void setMaxEnumeratedPages(String maxEnumeratedPages) {
  this.maxEnumeratedPages = maxEnumeratedPages;
}

/**
 * Gets the css prefix.
 * 
 * @return the css prefix
 */
public String getCssPrefix() {
  return cssPrefix;
}

/**
 * Sets the css prefix.
 * 
 * @param cssPrefix the new css prefix
 */
public void setCssPrefix(String cssPrefix) {
  this.cssPrefix = cssPrefix;
}

/**
 * Gets the id prefix.
 * 
 * @return the id prefix
 */
public String getIdPrefix() {
  return idPrefix;
}

/**
 * Sets the id prefix.
 * 
 * @param idPrefix the new id prefix
 */
public void setIdPrefix(String idPrefix) {
  this.idPrefix = idPrefix;
}

/**
 * Gets the property prefix.
 * 
 * @return the property prefix
 */
public String getPropertyPrefix() {
  return propertyPrefix;
}

/**
 * Sets the property prefix.
 * 
 * @param propertyPrefix the new property prefix
 */
public void setPropertyPrefix(String propertyPrefix) {
  this.propertyPrefix = propertyPrefix;
}

/**
 * Gets the component type.
 * 
 * @return COMPONENT_TYPE
 */
@Override
public String getComponentType() {
  return UIPagination.COMPONENT_TYPE;
}

/**
 * Gets the render first page.
 * 
 * @return the render first page
 */
public String getRenderFirstPageLink() {
  return renderFirstPage;
}

/**
 * Sets the render first page.
 * 
 * @param renderFirstPage the new render first page
 */
public void setRenderFirstPageLink(String renderFirstPage) {
  this.renderFirstPage = renderFirstPage;
}

/**
 * Gets the render last page.
 * 
 * @return the render last page
 */
public String getRenderLastPageLink() {
  return renderLastPage;
}

/**
 * Sets the render last page.
 * 
 * @param renderLastPage the new render last page
 */
public void setRenderLastPageLink(String renderLastPage) {
  this.renderLastPage = renderLastPage;
}

/**
 * Gets the label.
 * 
 * @return the label
 */
public String getLabel() {
  return label;
}

/**
 * Sets the label.
 * 
 * @param label the new label
 */
public void setLabel(String label) {
  this.label = label;
}

/**
 * Gets the label values.
 * 
 * @return the label values (possibly null)
 */
public String getLabelValues() {
  return labelValues;
}

/**
 * Sets the label values.
 * 
 * @param labelValues the new label values
 */
public void setLabelValues(String labelValues) {
  this.labelValues = labelValues;
}

/**
 * Gets the label position.
 * 
 * @return the label position
 */
public String getLabelPosition() {
  return labelPosition;
}

/**
 * Sets the label position.
 * 
 * @param labelPostion the new label position
 */
public void setLabelPosition(String labelPostion) {
  this.labelPosition = labelPostion;
}




// methods =====================================================================

/**
 * Gets the renderer type.
 * 
 * @return null currently
 * 
 * @see javax.faces.webapp.UIComponentTag#getRendererType()
 */
@Override
public String getRendererType() {
  
  return null;
}

/**
 * Nulling instance variables.
 */

@Override
public void release() {
  LOG.finer("Releasing variables");
  this.action = null;
  this.actionListener = null;
  this.cssPrefix = null;
  this.idPrefix = null;
  this.maxEnumeratedPages = null;
  this.pageCursor = null;
  this.propertyPrefix = null;
  this.renderFirstPage = null;
  this.renderLastPage = null;
  this.label = null;
  this.labelPosition = null;
  this.labelValues = null;
  this.criteriaPageCursor = null;
  super.release();
}

/**
 * Sets the properties.
 * 
 * @param component the component
 * 
 * @see javax.faces.webapp.UIComponentTag#setProperties(javax.faces.component.UIComponent)
 */

@SuppressWarnings("unchecked")
@Override
protected void setProperties(UIComponent component) {
  super.setProperties(component);
  LOG.finer("Setting UIComponent properties");
      
  tagSupport.setActionBind(
      component, UIPagination.ComponentMapKeys.action.name(),
      this.getAction());
  tagSupport.setPropMethodBind(
      component, UIPagination.ComponentMapKeys.actionListener.name(), 
      this.getActionListener());
  tagSupport.setPropValueBind(component, 
      UIPagination.ComponentMapKeys.idPrefix.name(), 
      this.getIdPrefix());
  tagSupport.setPropValueBind(component, 
      UIPagination.ComponentMapKeys.pageCursor.name(), 
      this.getPageCursor());
  tagSupport.setPropValueBind(component, 
      UIPagination.ComponentMapKeys.criteriaCursor.name(), 
      this.getCriteriaPageCursor());
  tagSupport.setPropValueBind(
      component, UIPagination.ComponentMapKeys.pageCursorTotalPages.name(), 
      this.getPageCursorTotalPages());
  tagSupport.setPropValueBind(
      component, UIPagination.ComponentMapKeys.pageCursorCurrentPage.name(), 
      this.getPageCursorCurrentPage());
  tagSupport.setPropValueBind(component, 
      UIPagination.ComponentMapKeys.cssPrefix.name(), this.getCssPrefix());
  tagSupport.setPropValueBind(component, 
      UIPagination.ComponentMapKeys.maxEnumeratedPages.name(), 
      this.getMaxEnumeratedPages());
  tagSupport.setPropValueBind(component, 
      UIPagination.ComponentMapKeys.propertyPrefix.name(), 
      this.getPropertyPrefix());
  tagSupport.setPropValueBind(component, 
      UIPagination.ComponentMapKeys.renderFirstPageLink.name(), 
      this.getRenderFirstPageLink());
  tagSupport.setPropValueBind(component, 
      UIPagination.ComponentMapKeys.renderLastPageLink.name(), 
      this.getRenderLastPageLink());
  tagSupport.setPropValueBind(component,
      UIPagination.Label.label.name(), this.getLabel());
  tagSupport.setPropValueBind(component,
      UIPagination.Label.labelPosition.name(), 
      this.getLabelPosition());
  tagSupport.setPropValueBind(component,
      UIPagination.Label.labelNoResults.name(), 
      this.getLabelNoResults());
  List list = this.getLabelValueList();
  if(list != null) {
    component.getAttributes().put(
        UIPagination.Label.labelValueList.name(), 
        this.getLabelValueList());
  }
 
}

/**
 * Gets the label values.
 * 
 * @return the label values (possibly null)
 */
private List<Object> getLabelValueList() {
  String lblValues = this.getLabelValues();
  if(lblValues == null) {
    return null;
  }
  String values[] = this.getLabelValues().split("\\|");
  List<Object> lstVals = new LinkedList<Object>();
  if(values.length <= 0) {
    return null;
  }
  for(String value: values) {
    if(value == null) {
      continue;
    }
    Object obj = tagSupport.getValueBindingOrString(value);
    if(obj == null) {
      continue;
    }
    lstVals.add(obj);
    
  }
  return lstVals;
}

/**
 * Gets the page cursor total pages.
 * 
 * @return the page cursor total pages
 */
private String getPageCursorTotalPages() {
  String pageCursor = Val.chkStr(this.getPageCursor());
  if (!"".equals(pageCursor) && pageCursor.endsWith("}")) {
    pageCursor = pageCursor.replaceFirst("}$", ".totalRecordCount}");
    return pageCursor;
  }
 
  return null;

}

/**
 * Gets the page cursor current page
 * 
 * @return the page cursor current page
 */
private String getPageCursorCurrentPage() {
  String pageCursor = Val.chkStr(this.getPageCursor());
  if (!"".equals(pageCursor) && pageCursor.endsWith("}")) {
    pageCursor = pageCursor.replaceFirst("}$", ".totalRecordCount}");
    return pageCursor;
  }
 
  return null;

}

public String getCriteriaPageCursor() {
  return criteriaPageCursor;
}

public void setCriteriaPageCursor(String criteriaPageCursor) {
  this.criteriaPageCursor = criteriaPageCursor;
}


  
}


