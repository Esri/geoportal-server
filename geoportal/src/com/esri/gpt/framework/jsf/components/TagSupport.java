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

import javax.faces.component.UIComponent;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;


/**
 * The Class TagSupport.  Supports JSF Tags.
 */
public class TagSupport {

//instance variables ==========================================================
/** JSF component support. */
private UISupport support = new UISupport();
//methods =====================================================================
/**
 * Sets the properties.  If value is valueReference then a Value Binding
 * is generated.
 * 
 * @param component the component
 * @param key the key
 * @param value the value (if null, no action taken)
 */
@SuppressWarnings("unchecked")
public void setPropValueBind(UIComponent component, String key, String value){

  if(value == null) {
    return;
  }
  value = value.trim();
  if(UIComponentTag.isValueReference(value)){
    ValueBinding vb = support.createValueBinding(value);
    component.getAttributes().put(key, vb);

  }
  else {
    component.getAttributes().put(key, value);
  }

}

/**
 * Sets the properties.  If value is method reference then a Method Binding
 * is generated.
 * If setting an action see setActionBind
 * @param component the component
 * @param key the key
 * @param value the value (if null, no action taken)
 */
@SuppressWarnings("unchecked")
public void setPropMethodBind(UIComponent component, String key, String value) {
  if(value == null) {
    return;
  }
  value = value.trim();
  if(UIComponentTag.isValueReference(value)){
    MethodBinding mb = support.createActLstnrMethodBinding(value);
    component.getAttributes().put(key, mb);
  }
  else {
    component.getAttributes().put(key, value);
  }

}

/**
 * Sets the action of a component.  The value can be a reference
 * or a return string.
 * 
 * @param component the component
 * @param key the key
 * @param value the value
 */
@SuppressWarnings("unchecked")
public void setActionBind(UIComponent component, String key, String value) {
  if(value == null) {
    return;
  }
  value = value.trim();
  if(UIComponentTag.isValueReference(value)) {
    MethodBinding mb = support.createActionBinding(value);
    component.getAttributes().put(key, mb);
  } else {
    SimpleMethodBinding bind = new SimpleMethodBinding(value);
    component.getAttributes().put(key, bind);
  }
}

/**
 * Gets the value binding or string.
 * 
 * @param value the value (accepts null or empty string)
 * 
 * @return the value binding or string (null if value == null)
 */
public Object getValueBindingOrString(String value) {
  if(value == null) {
    return null;
  }
  value = value.trim();
  if(UIComponentTag.isValueReference(value)){
    ValueBinding vb = support.createValueBinding(value);
    return vb;
  }
  else {
    return value;
  }
}

}
