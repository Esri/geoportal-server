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

import com.esri.gpt.control.view.PageCursorPanel;
import com.esri.gpt.framework.jsf.BaseActionListener;

/**
 * Base harvest controller.
 */
public abstract class BaseHarvestController extends BaseActionListener {

// class variables =============================================================

// instance variables ==========================================================
/** Harvest context. */
protected HarvestContext _harvestContext = new HarvestContext();
/** Page cursor for harvest history list. */
protected PageCursorPanel _pageCursorPanel;

// constructors ================================================================
/**
 * Creates instance of the object.
 * @param sActionExpression action expression
 * @param sChangeExpression change expression
 */
public BaseHarvestController(String sActionExpression, String sChangeExpression) {
  _pageCursorPanel = new PageCursorPanel();
  _pageCursorPanel.setActionListenerExpression(sActionExpression);
  _pageCursorPanel.setChangeListenerExpression(sChangeExpression);
}
// properties ==================================================================

/**
 * Sets harvest context.
 * @param harvestContext harvest context
 */
public final void setHarvestContext(HarvestContext harvestContext) {
  _harvestContext =
    harvestContext != null ? harvestContext : new HarvestContext();
  _pageCursorPanel.getPageCursor().setRecordsPerPageProvider(
    getHarvestContext());
}

/**
 * Gets harvest context.
 * @return harvest context
 */
public final HarvestContext getHarvestContext() {
  return _harvestContext;
}

/**
 * Gets harvest history page cursor panel.
 * @return harvest history page cursor panel
 */
public PageCursorPanel getPageCursorPanel() {
  return _pageCursorPanel;
}

// methods =====================================================================


}
