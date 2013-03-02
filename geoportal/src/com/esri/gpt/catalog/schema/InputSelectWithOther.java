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
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlPanelGroup;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Select one menu input component with an "Other" option for inputing text.
 * <p/>
 * The drop down choices are based upon the codes defined for the parameter.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 */
public class InputSelectWithOther extends InputSelectOneMenu {

// class variables =============================================================
  
// instance variables ==========================================================
private String    _otherCodeKey = "";
private InputText _otherComponent;
  
// constructors ================================================================

/** Default constructor. */
public InputSelectWithOther() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public InputSelectWithOther(InputSelectWithOther objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate == null) {
    setOtherComponent(new InputText());
  } else {
    setOtherCodeKey(objectToDuplicate.getOtherCodeKey());
    setOtherComponent(objectToDuplicate.getOtherComponent().duplicate());
    setFacesId(objectToDuplicate.getFacesId());
  }
}

// properties ==================================================================

/**
 * Sets the Faces ID for the component.
 * <br/> The '.' character will be replaced with the '.' character.
 * @param id Faces ID
 */
@Override
public void setFacesId(String id) {
  super.setFacesId(id);
  if (getOtherComponent() != null) {
    getOtherComponent().setFacesId(getFacesId()+"_other");
  }
}

/**
 * Gets the code key associated with the "Other" option.
 * @return the key
 */
public String getOtherCodeKey() {
  return _otherCodeKey;
}
/**
 * Sets the code key associated with the "Other" option.
 * @param key the key
 */
public void setOtherCodeKey(String key) {
  _otherCodeKey = Val.chkStr(key);
}

/**
 * Gets the component for inputting "Other" text.
 * @return the InputText component associated with the "Other" option
 */
public InputText getOtherComponent() {
  return _otherComponent;
}
/**
 * Sets the component for inputting "Other" text.
 * @param otherComponent the InputText component associated with the "Other" option
 */
public void setOtherComponent(InputText otherComponent) {
  _otherComponent = otherComponent;
  if (_otherComponent == null) _otherComponent = new InputText();
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>otherCodeKey
 * <p/>
 * The InputText component associated with the "Other" option is also configured
 * from this node.
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setOtherCodeKey(DomUtil.getAttributeValue(attributes,"otherCodeKey"));
  getOtherComponent().configure(context,node,attributes);
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new InputSelectWithOther(this);
 */
@Override
public InputSelectWithOther duplicate() {
  return new InputSelectWithOther(this);
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
  sb.append(" otherCodeKey=\"").append(getOtherCodeKey()).append("\"");
  sb.append("\n").append(getOtherComponent());
}

/**
 * Makes a Faces HtmlSelectOneMenu and an HtmlInputText (for "Other" option)
 * components for a parameter.
 * <p/>
 * The menu items are based upon the defined codes for the parameter.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI component
 */
@Override
public UIComponent makeInputComponent(UiContext context,
                                      Section section,
                                      Parameter parameter) {
  
  // determine values
  String sValue = parameter.getContent().getSingleValue().getValue();
  String sMenuValue = sValue;
  String sTextValue = "";
  boolean bIsOther = false;
  if (!parameter.getContent().getCodes().containsKey(sValue)) {
    bIsOther = true;
  } else if (sValue.equalsIgnoreCase(getOtherCodeKey())) {
    bIsOther = true;
  }
  if (bIsOther) {
    sMenuValue = getOtherCodeKey();
    sTextValue = sValue;
  } 
  
  // make the input text for the "Other" option
  InputText other = getOtherComponent();
  HtmlInputText text = new HtmlInputText();
  text.setId(other.getFacesId());
  text.setMaxlength(other.getMaxlength());
  text.setSize(other.getSize());
  text.setDisabled(!getEditable());
  text.setValue(sTextValue);
  if (!bIsOther) {
    text.setStyle("visibility:hidden;");
  }
  
  // make the script for the onchange event
  StringBuffer sbOnchange = new StringBuffer();
  sbOnchange.append("mdeToggleVisibility(this,");
  sbOnchange.append("'").append(other.getFacesId()).append("',"); 
  sbOnchange.append("this.options[this.selectedIndex].value==");
  sbOnchange.append("'").append(getOtherCodeKey()).append("')");
  
  // make the select one menu
  parameter.getContent().getSingleValue().setValue(sMenuValue);
  UIComponent menu = makeSelectOneMenu(context,section,parameter,sbOnchange.toString());
  parameter.getContent().getSingleValue().setValue(sValue);
  

  
  // group the components
  HtmlPanelGroup panel = new HtmlPanelGroup();
  panel.getChildren().add(menu);
  panel.getChildren().add(makeNBSP());
  panel.getChildren().add(text);
  return panel;
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * On this event, either HtmlSelectOneMenu input value or the HtmlInputText
 * value is propagated to the parameter's singleValue (depending on the 
 * whether or not the user has selected the "Other" option).
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
  UIInput menu = findInputComponent(context,editorForm);
  UIInput text = getOtherComponent().findInputComponent(context,editorForm);
  String sMenuValue = getInputValue(menu);
  String sTextValue = Val.chkStr(getInputValue(text));
  text.setValue(sTextValue);
  if (sMenuValue.equalsIgnoreCase(getOtherCodeKey())) {
    parameter.getContent().getSingleValue().setValue(sTextValue);
    if (text instanceof HtmlInputText) {
      ((HtmlInputText)text).setStyle("visibility:visible;");
    }
  } else {
    parameter.getContent().getSingleValue().setValue(sMenuValue);
    if (text instanceof HtmlInputText) {
      ((HtmlInputText)text).setStyle("visibility:hidden;");
    }
  }
}

}
