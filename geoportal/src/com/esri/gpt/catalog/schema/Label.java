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
import javax.faces.component.html.HtmlOutputLabel;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Label component associated with a metadata schema.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * &lt;label resourceKey="catalog.mdParam.description.abstract"
 * styleClass="sectionCaption"/&gt;
 */
public class Label extends UiComponent {

// class variables =============================================================
  
// instance variables ==========================================================
private String  _resourceKey = "";
  
// constructors ================================================================

/** Default constructor. */
public Label() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Label(Label objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate != null) {
    setResourceKey(objectToDuplicate.getResourceKey());
  }
}

// properties ==================================================================

/**
 * Gets the UI property bundle resource key.
 * @return the resource key
 */
public String getResourceKey() {
  return _resourceKey;
}
/**
 * Sets the UI property bundle resource key.
 * <br/>The key will be trimmed. 
 * <br/>A null key is treated as an empty string.
 * @param key the resource key
 */
public void setResourceKey(String key) {
  _resourceKey = Val.chkStr(key);
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>key resourceKey
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setResourceKey(DomUtil.getAttributeValue(attributes,"resourceKey"));
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new Label(this);
 */
public Label duplicate() {
  return new Label(this);
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
  sb.append(" resourceKey=\"").append(getResourceKey()).append("\"");
}

/**
 * Makes a Faces HtmlOutputLabel for a metadata editor parameter.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI component
 */
public UIComponent makeEditorLabel(UiContext context,
                                   Section section,
                                   Parameter parameter) {
  HtmlOutputLabel outLabel = new HtmlOutputLabel();
  MessageBroker msgBroker = context.extractMessageBroker();
  if (parameter.getInput() != null) {
    // even label has to have unique id (for GlassFish)
    outLabel.setId(parameter.getInput().getFacesId()+"label");
    outLabel.setFor(parameter.getInput().getFacesId());
  }
  outLabel.setValue(msgBroker.retrieveMessage(getResourceKey()));
  if (parameter.getValidation().getRequired()) {
    outLabel.setStyleClass("requiredField");
  }
  return outLabel;
}

}
