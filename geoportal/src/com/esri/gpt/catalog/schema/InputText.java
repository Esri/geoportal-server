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
import com.esri.gpt.framework.xml.DomUtil;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.html.HtmlCommandButton;
import javax.faces.component.html.HtmlInputText;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.component.html.HtmlPanelGroup;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Input text component associated with a metadata parameter.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * <br/>&lt;parameter key="fileIdentifier"&gt;
 * <br/>&lt;input type="text" maxlength="4000" size="50"/&gt;
 * <br/>&lt;/parameter&gt;
 */
public class InputText extends Input {

// class variables =============================================================
  
// instance variables ==========================================================
private int _maxlength;
private int _size;
  
// constructors ================================================================

/** Default constructor. */
public InputText() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public InputText(InputText objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate == null) {
    setMaxlength(getDefaultMaxlength());
    setSize(getDefaultSize());
  } else {
    setMaxlength(objectToDuplicate.getMaxlength());
    setSize(objectToDuplicate.getSize());
  }
}

// properties ==================================================================

/**
 * Gets the default max length (128).
 * @return the default max length
 */
public int getDefaultMaxlength() {
  return 2048;
}

/**
 * Gets the default size (50).
 * @return the default size
 */
public int getDefaultSize() {
  return 50;
}

/**
 * Gets the max length for the input text component.
 * @return the max length
 */
public int getMaxlength() {
  return _maxlength;
}
/**
 * Sets the max length for the input text component.
 * @param size the max length
 */
public void setMaxlength(int size) {
  _maxlength = (size < 1) ? getDefaultMaxlength() : size;
}

/**
 * Gets the size for the input text component.
 * @return the size
 */
public int getSize() {
  return _size;
}
/**
 * Sets the size for the input text component
 * @param size the size
 */
public void setSize(int size) {
  _size = (size < 1) ? getDefaultSize() : size;
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>maxlength size
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setMaxlength(Val.chkInt(DomUtil.getAttributeValue(attributes,"maxlength"),-1));
  setSize(Val.chkInt(DomUtil.getAttributeValue(attributes,"size"),-1));
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new InputText(this);
 */
public InputText duplicate() {
  return new InputText(this);
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
  sb.append(" maxlength=\"").append(getMaxlength()).append("\"");
  sb.append(" size=\"").append(getSize()).append("\"");
}

/**
 * Makes a Faces HtmlInputText component for a parameter.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI component
 */
public UIComponent makeInputComponent(UiContext context,
                                      Section section,
                                      Parameter parameter) {
  
  // make the component
  HtmlInputText component = new HtmlInputText();
  component.setId(getFacesId());
  component.setMaxlength(getMaxlength());
  component.setSize(getSize());
  component.setDisabled(!getEditable());
  component.setOnchange(getOnChange());
  component.setOnclick(getOnClick());
  String sMeaningType = parameter.getMeaningType();
  setComponentValue(context,component,parameter);
  MessageBroker msgBroker = context.extractMessageBroker();
  String testResourceKey = getTestResourceKey();
  if(sMeaningType != null && sMeaningType.equalsIgnoreCase(Meaning.MEANINGTYPE_RESOURCE_URL)
      && testResourceKey.length()>0){
    String testResourceLabel = msgBroker.retrieveMessage(testResourceKey);
    UIComponent inputComponent = applyHint(context,component);      
    HtmlPanelGroup panel = new HtmlPanelGroup();
    panel.getChildren().add(inputComponent);
    HtmlCommandButton testBtn = new HtmlCommandButton();
    String inputComponentId = getFacesId();
    testBtn.setId(inputComponentId +"_testBtn");
    testBtn.setOnclick("window.open(document.getElementById('mdEditor:"+inputComponentId+"').value)");
    testBtn.setValue(testResourceLabel);
    panel.getChildren().add(testBtn);
    return panel;
  }else{
    return applyHint(context,component);
  }
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * On this event, the HtmlInputText input value is propagated to
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
  String sValue = formatValue(parameter,getInputValue(input));
  input.setValue(sValue);
  parameter.getContent().getSingleValue().setValue(sValue);
}

}
