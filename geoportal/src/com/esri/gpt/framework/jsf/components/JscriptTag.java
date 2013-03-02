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

import java.util.logging.Logger;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;


/**
 * The Class JscriptTag. Makes it easier to write
 * javascript variable in jsf
 */
public class JscriptTag extends UIComponentTag {

// class variables ==============================================================
/** The LOG. */
private static final Logger LOG =
  Logger.getLogger(JscriptTag.class.getCanonicalName());

// instance variables ==========================================================
/** The value. */
private String     value;

/** The quoted. */
private String     quoted;

/** The variable name. */
private String     variableName;

/** The tag support. */
private final TagSupport tagSupport = new TagSupport();

// properties ==================================================================
/**
 * Gets the value.
 * 
 * @return the value
 */
public String getValue() {
  return value;
}

/**
 * Sets the value.
 * 
 * @param value the new value
 */
public void setValue(String value) {
  this.value = value;
}

/**
 * Gets the quoted.
 * 
 * @return the quoted
 */
public String getQuoted() {
  return quoted;
}

/**
 * Sets the quoted.
 * 
 * @param quoted the new quoted
 */
public void setQuoted(String quoted) {
  this.quoted = quoted;
}

/**
 * Gets the variable name.
 * 
 * @return the variable name
 */
public String getVariableName() {
  return variableName;
}

/**
 * Sets the variable name.
 * 
 * @param variableName the new variable name
 */
public void setVariableName(String variableName) {
  this.variableName = variableName;
}

// methods =====================================================================
/**
 * Sets properties.
 * @param component belonging to this tag
 * 
 */

@Override
public void setProperties(UIComponent component) {
  
  super.setProperties(component);
  tagSupport.setPropValueBind(component, UIJscript.Options.value.name(), 
  this.getValue());
  
  tagSupport.setPropValueBind(component, UIJscript.Options.variableName.name(),
      this.getVariableName());
  tagSupport.setPropValueBind(component, UIJscript.Options.quoted.name(), this
      .getQuoted());
  
      
  LOG.finer("Finished Setting properties");
}

/**
 * 
 * Clears all instance variables.
 */
@Override
public void release() {
  variableName = null;
  quoted = null;
  value = null;
}

/**
 * Returns component type string representation
 * @return Component type
 * 
 */
@Override
public String getComponentType() {
  return UIJscript.COMPONENT_TYPE;
}

/**
 * Gets the JSF render Type
 * @return JSF render Type
 * 
 */
@Override
public String getRendererType() {

  return null;
}

}

