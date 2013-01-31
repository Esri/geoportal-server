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
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Parameter component associated with a metadata schema.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example: 
 * <br/>&lt;parameter key="abstract"&gt;
 * <br/>&lt;/parameter&gt;
 */
public class Parameter extends Component {

// class variables ============================================================= 
  
// instance variables ==========================================================
private Content    _content;
private Input      _input;
private Label      _label;
private String     _meaningType = "";
private Validation _validation;
private boolean    _visible = true;
private boolean    _visibleOnDetails = true;

  
// constructors ================================================================

/** Default constructor. */
public Parameter() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Parameter(Parameter objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate == null) {
    setValidation(new Validation());
    setContent(new Content());
  } else {
    setMeaningType(objectToDuplicate.getMeaningType());
    setVisible(objectToDuplicate.getVisible());
    setVisibleOnDetails(objectToDuplicate.getVisibleOnDetails());
    setValidation(objectToDuplicate.getValidation().duplicate());
    setContent(objectToDuplicate.getContent().duplicate());
    if (objectToDuplicate.getLabel() != null) {
      setLabel(objectToDuplicate.getLabel().duplicate());
    }
    if (objectToDuplicate.getInput() != null) {
      setInput(objectToDuplicate.getInput().duplicate());
    }
  } 
}

// properties ==================================================================

/**
 * Gets the Content component.
 * @return the Content component
 */
public Content getContent() {
  return _content;
}
/**
 * Sets the Content component.
 * @param content the Content component
 */
public void setContent(Content content) {
  _content = content;
  if (_content == null) _content = new Content();
}

/**
 * Gets the input component.
 * @return the input component
 */
public Input getInput() {
  return _input;
}
/**
 * Sets the input component.
 * @param input the input component
 */
public void setInput(Input input) {
  _input = input;
}

/**
 * Gets the label component.
 * @return the label component
 */
public Label getLabel() {
  return _label;
}
/**
 * Sets the label component.
 * @param label the label component
 */
public void setLabel(Label label) {
  _label = label;
}

/**
 * Gets the meaning type.
 * @return the meaning type
 */
public String getMeaningType() {
  return _meaningType;
}
/**
 * Sets the meaning type.
 * @param type the meaning type
 */
public void setMeaningType(String type) {
  _meaningType = Val.chkStr(type);
}

/**
 * Gets the validation component.
 * @return the validation component
 */
public Validation getValidation() {
  return _validation;
}
/**
 * Sets the validation component.
 * @param validation the validation component
 */
public void setValidation(Validation validation) {
  _validation = validation;
  if (_validation == null) _validation = new Validation();
}

/**
 * Gets the visible status.
 * @return true if the parameter is visible
 */
public boolean getVisible() {
  return _visible;
}
/**
 * Sets the visible status.
 * @param visible true if the parameter is visible
 */
public void setVisible(boolean visible) {
  _visible = visible;
}

/**
 * Gets the status indicating if the parameter should be displayed on the 
 * view details page.
 * @return true if the parameter should be displayed on the view details page
 */
public boolean getVisibleOnDetails() {
  return _visibleOnDetails;
}
/**
 * Sets the status indicating if the parameter should be displayed on the 
 * view details page.
 * @param visible  true if the parameter should be displayed on the view details page
 */
public void setVisibleOnDetails(boolean visible) {
  _visibleOnDetails = visible;
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>meaning visible visibleOnDetails
 * <p/>
 * The following child nodes are configured:
 * <br/>label input validation content
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  
  // configure attributes
  String value = DomUtil.getAttributeValue(attributes,"meaning");
  if (Val.chkStr(value).equalsIgnoreCase("anytext")) {
    value = "body";
  }
  setMeaningType(value);
  setVisible(Val.chkBool(DomUtil.getAttributeValue(attributes,"visible"),true));
  setVisibleOnDetails(Val.chkBool(DomUtil.getAttributeValue(attributes,"visibleOnDetails"),true));
  
  // configure child components
  setLabel(context.getFactory().newLabel(
      context,DomUtil.findFirst(node,"label")));
  setInput(context.getFactory().newInput(
      context,DomUtil.findFirst(node,"input")));
  setValidation(context.getFactory().newValidation(
      context,DomUtil.findFirst(node,"validation")));
  setContent(context.getFactory().newContent(
      context,DomUtil.findFirst(node,"content")));
  
  // set the faces ID for the input component
  if (getInput() != null) {
    getInput().setFacesId(getKey());
  }
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new Parameter(this);
 */
public Parameter duplicate() {
  return new Parameter(this);
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
  if (getMeaningType().length() > 0) {
    sb.append(" meaning=\"").append(getMeaningType()).append("\"");
  }
  sb.append(" visible=\"").append(getVisible()).append("\"");
  sb.append(" visibleOnDetails=\"").append(getVisibleOnDetails()).append("\"");
  if (getLabel() != null) sb.append("\n").append(getLabel());
  if (getInput() != null) sb.append("\n").append(getInput());
  sb.append("\n").append(getValidation());
  sb.append("\n").append(getContent());
}

/**
 * Evaluates a parameter based upon the supplied metadata document.
 * <p/>
 * The default behavior is to invoke the "evaluate" method for the 
 * Content component associated with the parameter, and then invoke 
 * the "evaluate" method on the Meaning component of the parent schema.
 * @param schema the schema being evaluated
 * @param dom the metadata document
 * @param xpath an XPath object configured with an appropriate 
 *        Namespace context for the schema
 * @throws XPathExpressionException if an evaluation expression fails 
 */
public void evaluate(Schema schema, Document dom, XPath xpath) 
  throws XPathExpressionException {
  getContent().evaluate(dom,xpath,this);
  schema.getMeaning().evaluate(this);
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * The default behavior is to invoke the "unBind" method for
 * the Input object, as well as each child parameter. 
 * @param context the UI context
 * @param editorForm the Faces HtmlForm for the metadata editor
 * @throws SchemaException if an associated Faces UIComponent cannot be located
 */
public void unBind(UiContext context, UIComponent editorForm) 
  throws SchemaException {
  if (getInput() != null) {
    getInput().unBind(context,editorForm,this);
  }  
}

/**
 * Updates the metadata document template based upon entered parameter value(s).
 * <p/>
 * The default behavior is to invoke the "update" method
 * for the Content component associated with the parameter.
 * @param dom the metadata document template for the schema
 * @param xpath an XPath object configured with an appropriate 
 *        Namespace context for the schema
 * @throws XPathExpressionException if an expression fails 
 * @throws SchemaException if the update fails
 */
public void update(Document dom, XPath xpath) 
  throws XPathExpressionException, SchemaException {
  getContent().update(dom,xpath,this);
}

/**
 * Validates a parameter.
 * <p/>
 * The default behavior is to invoke the "validate" method on the 
 * Validation component associated with the parameter, and then invoke 
 * the "evaluate" method on the Meaning component of the parent schema.
 * <p/>
 * Encountered errors should be appended to the 
 * schema.getValidationErrors() collection.
 * <p/>
 * Parameter values associated with meaning should be used to populate
 * schema.getMeaning() values.
 * @param schema the schema being validated
 * @param section the section being validated
 */
public void validate(Schema schema, Section section) {
  getValidation().validate(schema,section,this);
  schema.getMeaning().evaluate(this);
}

}

