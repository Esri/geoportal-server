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
 * An array of input text components associated with a metadata parameter.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * <br/>&lt;parameter key="theme"&gt;
 * <br/>&lt;input type="textArray" arraySize="10"/&gt;
 * <br/>&lt;/parameter&gt;
 */
public class InputTextArray extends InputText {

// class variables =============================================================
  
// instance variables ==========================================================
private int _arraySize = 1;
  
// constructors ================================================================

/** Default constructor. */
public InputTextArray() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public InputTextArray(InputTextArray objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate != null) {
    setArraySize(objectToDuplicate.getArraySize());  
  }
}

// properties ==================================================================

/**
 * Gets the number of input text components in the array.
 * @return the number of input text components
 */
public int getArraySize() {
  return _arraySize;
}
/**
 * Sets the number of input text components in the array.
 * @param size the number of input text components
 */
public void setArraySize(int size) {
  _arraySize = size;
}


// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>arraySize
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setArraySize(Val.chkInt(DomUtil.getAttributeValue(attributes,"arraySize"),-1));
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new InputTextArray(this);
 */
@Override
public InputTextArray duplicate() {
  return new InputTextArray(this);
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
  sb.append(" arraySize=\"").append(getArraySize()).append("\"");
}

/**
 * Makes a Faces HtmlPanelGroup containing a collection of
 * HtmlInputTextarea components for a parameter.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI component
 */
@Override
public UIComponent makeInputComponent(UiContext context,
                                      Section section,
                                      Parameter parameter) {
 
  // initialize the panel
  HtmlPanelGroup panel = new HtmlPanelGroup();
  String sIdPfx = getFacesId();
  panel.setId(sIdPfx);
  
  // add input text components based upon the array size
  ContentValues values = parameter.getContent().getMultipleValues();
  for (int i=0;i<getArraySize();i++) {
    HtmlInputText component = new HtmlInputText();
    String sId = sIdPfx+"-v"+i;
    component.setId(sId);
    component.setMaxlength(getMaxlength());
    component.setSize(getSize());
    component.setDisabled(!getEditable());
    component.setOnchange(getOnChange());
    component.setOnclick(getOnClick());
    component.setValue("");
    if (i < values.size()) {
      component.setValue(Val.chkStr(values.get(i).getValue()));
    }
    panel.getChildren().add(applyHint(context,component));
    panel.getChildren().add(makeBR());
  }
  return panel;
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * On this event, the array of input values are used to generate
 * the parameter's multiple values (parameter.getMultipleValues()).
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
  
  // clear current values
  parameter.getContent().clearAllValues();
  ContentValues values = parameter.getContent().getMultipleValues();
  
  // find input text component
  String sIdPfx = getFacesId();
  for (int i=0;i<getArraySize();i++) {
    String sId = sIdPfx+"-v"+i;
    UIComponent component = editorForm.findComponent(sId);
    if ((component != null) && (component instanceof UIInput)) {
      UIInput input = (UIInput)component;
      String sValue = formatValue(parameter,getInputValue(input));
      input.setValue(sValue);
      
      // add the input value to the multiple values list
      if (sValue.length() > 0) {
        values.add(new ContentValue(sValue));
      }
    }
  }
}

}
