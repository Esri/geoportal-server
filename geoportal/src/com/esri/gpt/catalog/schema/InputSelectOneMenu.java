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
package com.esri.gpt.catalog.schema;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

import java.util.ArrayList;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UISelectItems;
import javax.faces.component.html.HtmlSelectOneMenu;
import javax.faces.model.SelectItem;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Select one menu input component associated with a metadata parameter.
 * <p/>
 * The drop down choices are based upon the codes defined for the parameter.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * <br/>&lt;parameter key="presentationForm"&gt;
 * <br/>&lt;input type="selectOneMenu"/&gt;
 * <br/>&lt;codes&gt;
 * <br/>&lt;code key="documentDigital" 
 * resourceKey="catalog.mdCode.presentationForm.documentDigital"/&gt;
 * <br/>&lt;code key="documentHardcopy" 
 * resourceKey="catalog.mdCode.presentationForm.documentHardcopy"/&gt;
 * <br/>&lt;/codes&gt;
 * <br/>&lt;/parameter&gt;
 */
public class InputSelectOneMenu extends Input {

// class variables =============================================================
  
// instance variables ==========================================================
  
// constructors ================================================================

/** Default constructor. */
public InputSelectOneMenu() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public InputSelectOneMenu(InputSelectOneMenu objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate != null) {
  }
}

// properties ==================================================================

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new InputSelectOneMenu(this);
 */
public InputSelectOneMenu duplicate() {
  return new InputSelectOneMenu(this);
}

/**
 * Appends property information for the component to a StringBuffer.
 * <br/>The method is intended to support "FINEST" logging.
 * <br/>super.echo should be invoked prior appending any local information.
 * @param sb the StringBuffer to use when appending information
 */
@Override
public void echo(StringBuffer sb) {
  super.echo(sb);
}

/**
 * Makes a Faces HtmlSelectOneMenu component for a parameter.
 * <p/>
 * The menu items are based upon the defined codes for the parameter.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI component
 */
public UIComponent makeInputComponent(UiContext context,
                                      Section section,
                                      Parameter parameter) {
  HtmlSelectOneMenu component = makeSelectOneMenu(context,section,parameter,"");
  return applyHint(context,component);
}

/**
 * Makes a Faces HtmlSelectOneMenu component for a parameter.
 * <p/>
 * The menu items are based upon the defined codes for the parameter.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @param onchange Javascript associated with the "onchange" event
 * @return the UI component
 */
protected HtmlSelectOneMenu makeSelectOneMenu(UiContext context,
                                              Section section,
                                              Parameter parameter,
                                              String onchange) {
  
  // initialize the component
  MessageBroker msgBroker = context.extractMessageBroker();
  HtmlSelectOneMenu component = new HtmlSelectOneMenu();
  component.setId(getFacesId());
  component.setDisabled(!getEditable());
  component.setOnchange(getOnChange());
  component.setOnclick(getOnClick());
  setComponentValue(context,component,parameter);
  onchange = Val.chkStr(onchange);
  if (onchange.length() > 0) {
    component.setOnchange(onchange);
  }
  
  // add each code as a SelectItem
  ArrayList<SelectItem> codeItems = new ArrayList<SelectItem>();
  Codes codes = parameter.getContent().getCodes();
  for (Code code: codes.values()) {
    String sResKey = code.getResourceKey();
    String sLabel = code.getKey();
    if (sResKey.length() > 0) {
      sLabel = msgBroker.retrieveMessage(sResKey);
    }
    codeItems.add(new SelectItem(code.getKey(),sLabel));
  } 
  UISelectItems uiItems = new UISelectItems();
  uiItems.setValue(codeItems);
  component.getChildren().add(uiItems);
  return component;
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * On this event, the HtmlSelectOneMenu input value is propagated to
 * the parameter's singleValue.
 * @param context the UI context
 * @param editorForm the Faces HtmlForm for the metadata editor
 * @param parameter the associated parameter
 * @throws SchemaException if an associated Faces UIComponent cannot be located
 */
@Override
public void unBind(UiContext context, 
                   UIComponent editorForm,
                   Parameter parameter) 
  throws SchemaException {
  UIInput input = findInputComponent(context,editorForm);
  String sValue = getInputValue(input);
  parameter.getContent().getSingleValue().setValue(sValue);
}

}
