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

import javax.faces.component.UIData;
import javax.faces.component.UIPanel;


/**
 * The Class dataTableBackingBean.
 */
public class DataTableBackingBean {

// instance variable ==========================================================
/** The data table. */
UIData dataTable;

/** The info panel. */
UIPanel infoPanel;

// properties =================================================================
/**
 * Gets the data table.
 * 
 * @return the data table
 */
public UIData getDataTable() {
  return dataTable;
}

/**
 * Sets the data table.
 * 
 * @param dataTable the new data table
 */
public void setDataTable(UIData dataTable) {
  this.dataTable = dataTable;
}

/**
 * Gets the row index as string.
 * 
 * @return the row index as string
 */
public String getRowIndexAsString() {
  return String.valueOf(this.getDataTable().getRowIndex());
}

/**
 * Gets the info panel.
 * 
 * @return the info panel
 */
public UIPanel getInfoPanel() {
  if(infoPanel == null) {
    infoPanel = new UIPanel();
    infoPanel.setId("_metadataPanel" + this.getRowIndexAsString());
  }
  return infoPanel;
}

/**
 * Sets the info panel.
 * 
 * @param infoPanel the new info panel
 */
public void setInfoPanel(UIPanel infoPanel) {
  this.infoPanel = infoPanel;
  infoPanel.setId("_metadataPanel" + this.getRowIndexAsString());
}
}
