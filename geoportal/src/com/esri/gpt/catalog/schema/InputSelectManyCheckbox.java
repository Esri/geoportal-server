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
import java.util.HashMap;
import javax.faces.component.UIComponent;
import javax.faces.component.UISelectBoolean;
import javax.faces.component.html.HtmlOutputLabel;
import javax.faces.component.html.HtmlPanelGroup;
import javax.faces.component.html.HtmlSelectBooleanCheckbox;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Select many checkbox input component associated with a metadata parameter.
 * <p/>
 * The checkbox choices are based upon the codes defined for the parameter.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * <br/>&lt;parameter key="topics"&gt;
 * <br/>&lt;input type="selectManyCheckbox"/&gt;
 * <br/>&lt;codes&gt;
 * <br/>&lt;code key="boundaries" 
 * resourceKey="catalog.mdCode.topic.boundaries"/&gt;
 * <br/>&lt;code key="farming" 
 * resourceKey="catalog.mdCode.topic.farming"/&gt;
 * <br/>&lt;/codes&gt;
 * <br/>&lt;/parameter&gt;
 */
public class InputSelectManyCheckbox extends Input {

// class variables =============================================================
  
// instance variables ==========================================================
  
// constructors ================================================================

/** Default constructor. */
public InputSelectManyCheckbox() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public InputSelectManyCheckbox(InputSelectManyCheckbox objectToDuplicate) {
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
 * <br/>return new InputSelectManyCheckbox(this);
 */
public InputSelectManyCheckbox duplicate() {
  return new InputSelectManyCheckbox(this);
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
 * Makes a Faces HtmlPanelGroup of HtmlSelectBooleanCheckbox components
 * for a parameter.
 * <p/>
 * The check boxes are based upon the defined codes for the parameter.
 * <p/>
 * The multiple values associated with the parameter 
 * (parameter.getMultipleValues()) are used to establish the 
 * selected/unselected status for each check box.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI component
 */
public UIComponent makeInputComponent(UiContext context,
                                      Section section,
                                      Parameter parameter) {
  
  // initialize the panel
  MessageBroker msgBroker = context.extractMessageBroker();
  HtmlPanelGroup panel = new HtmlPanelGroup();
  String sIdPfx = getFacesId();
  panel.setId(sIdPfx);
  
  // build a map of values
  HashMap<String,String> valuesMap = new HashMap<String,String>();
  for (ContentValue value: parameter.getContent().getMultipleValues()) {
    valuesMap.put(value.getValue(),"");
  }
  
  // add a checkbox for each code
  Codes codes = parameter.getContent().getCodes();
  for (Code code: codes.values()) {
    
    // make the checkbox
    String sKey = code.getKey();
    String sFKey = sKey.replace('.','_');
    sFKey = sKey.replace(':','_');
    String sId  = sIdPfx+"-"+sFKey;
    HtmlSelectBooleanCheckbox checkBox = new HtmlSelectBooleanCheckbox();
    checkBox.setId(sId);
    checkBox.setDisabled(!getEditable());
    checkBox.setSelected(valuesMap.containsKey(sKey));
    checkBox.setOnchange(getOnChange());
    checkBox.setOnclick(getOnClick());
    panel.getChildren().add(checkBox);
    
    // make the label
    String sLabel = sKey;
    String sResKey = code.getResourceKey();
    if (sResKey.length() > 0) {
      sLabel = msgBroker.retrieveMessage(sResKey);
    }
    HtmlOutputLabel outLabel = new HtmlOutputLabel();
    // even label has to have unique id (for GlassFish)
    outLabel.setId(sId+"label");
    outLabel.setFor(sId);
    outLabel.setValue(sLabel);
    panel.getChildren().add(outLabel);
    panel.getChildren().add(makeBR());
  } 

  return panel;
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * On this event, the selected parameter codes are used to generate
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
  
  // find each check box
  String sIdPfx = getFacesId();
  Codes codes = parameter.getContent().getCodes();
  for (Code code: codes.values()) {
    String sKey = code.getKey();    
    String sFKey = sKey.replace('.','_');
    sFKey = sKey.replace(':','_');
    String sId  = sIdPfx+"-"+sFKey;
    UIComponent component = editorForm.findComponent(sId);
    if ((component != null) && (component instanceof UISelectBoolean)) {
      UISelectBoolean checkBox = (UISelectBoolean)component;
      
      // add the code key to the parameter's multiple values if selected
      if (checkBox.isSelected() && (sKey.length() > 0)) {
        values.add(new ContentValue(sKey));
      }
    }
  }
}

}
