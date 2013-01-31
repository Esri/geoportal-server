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
package com.esri.gpt.catalog.gxe;
import javax.faces.context.FacesContext;

import com.esri.gpt.framework.jsf.MessageBroker;

/**
 * Represents the context associated with execution of a GXE request.
 */
public class GxeContext {
  
  /** class variables ========================================================= */
  
  /** URI GXE - "http://www.esri.com/geoportal/gxe" */
  public static final String URI_GXE = "http://www.esri.com/geoportal/gxe";
  
  /** URI GXEHTML - "http://www.esri.com/geoportal/gxe/html" */
  public static final String URI_GXEHTML = "http://www.esri.com/geoportal/gxe/html"; 
  
  /** instance variables ====================================================== */
  private GxeDefinition activeDefinition;
  private MessageBroker messageBroker;
  
  /** constructors ============================================================ */
  
  /** Default constructor. */
  public GxeContext() {}
  
  /** properties ============================================================== */
  
  
  /**
   * Gets the Geoportal XML definition that is currently active.
   * @return the definition (can be null)
   */
  public GxeDefinition getActiveDefinition() {
    return this.activeDefinition;
  }
  /**
   * Sets the Geoportal XML definition that is currently active.
   * @param activeDefinition the definition
   */
  public void setActiveDefinition(GxeDefinition activeDefinition) {
    this.activeDefinition = activeDefinition;
  }
  
  /**
   * Gets the current FacesContext instance.
   * @return the FacesContext
   */
  public FacesContext getFacesContext() {
    return FacesContext.getCurrentInstance();
  }
  
  /**
   * Gets the resource bundle message broker.
   * @return the message broker (can be null)
   */
  public MessageBroker getMessageBroker() {
    return this.messageBroker;
  }
  /**
   * Sets the resource bundle message broker.
   * @param messageBroker the message broker
   */
  public void setMessageBroker(MessageBroker messageBroker) {
    this.messageBroker = messageBroker;
  }

}
