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
import java.io.Serializable;
import java.util.logging.Level;

import javax.faces.component.UIComponentBase;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;

import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.jsf.PageContext;
import com.esri.gpt.framework.util.LogUtil;

/**
 * Initializes view of the page.
 * Calls actionListener to initialize page and updates error/success messages.
 * @see PrepareViewTag
 */
public class UIPrepareView extends UIComponentBase implements Serializable {

// class variables =============================================================
/** The JSF Component type. */
public static final String COMPONENT_TYPE = "com.esri.gpt.faces.PrepareView";
/** The JSF Component family. */
public static final String COMPONENT_FAMILY = COMPONENT_TYPE;

// instance variables ==========================================================

// constructors ================================================================

// properties ==================================================================

// methods =====================================================================
/**
 * Gets component family.
 * @return component family ({@link UIPrepareView#COMPONENT_FAMILY})
 */
@Override
public String getFamily() {
  return COMPONENT_FAMILY;
}

/**
 * Completes begin phase of component encoding.
 * @param context faces context
 * @throws IOException if encoding fails
 */
@Override
public void encodeBegin(FacesContext context) throws IOException {
  super.encodeBegin(context);

  try {
    PageContext pageContext = PageContext.extract();
    
    if (pageContext!=null && pageContext.getPrepareView().length()>0) {
      // infoke action listener
      MethodBinding binding = context.getApplication().createMethodBinding(
        pageContext.getPrepareView(), null);
      binding.invoke(context, null);
    }
    
  } catch (Throwable t) {
    MessageBroker messageBroker = 
      new FacesContextBroker().extractMessageBroker();
    messageBroker.addErrorMessage(t);
    LogUtil.getLogger().log(Level.SEVERE,"Exception raised.",t);
  }
}
}
