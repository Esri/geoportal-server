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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Input delimited text area component associated with a metadata parameter.
 * <p/>
 * The component provides a text area for inputing a delimited list of 
 * values (such as keywords) associated with multiple nodes within a 
 * metadata document.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * <br/>&lt;parameter key="abstract"&gt;
 * <br/>&lt;input type="delimitedTextArea" 
 * cols="60" rows="8" delimiter=","/&gt;
 * <br/>&lt;/parameter&gt;
 */
public class InputDelimitedTextArea extends InputTextArea {

// class variables =============================================================
  
// instance variables ==========================================================
private String _delimiter = "";
  
// constructors ================================================================

/** Default constructor. */
public InputDelimitedTextArea() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public InputDelimitedTextArea(InputDelimitedTextArea objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate == null) {
    setDelimiter(getDefaultDelimeter());
  } else {   
    setDelimiter(objectToDuplicate.getDelimiter());
  }
}

// properties ==================================================================

/**
 * Gets the delimiter.
 * @return the delimiter
 */
public String getDelimiter() {
  return _delimiter;
}
/**
 * Sets the delimiter.
 * <br/>The delimiter will be trimmed. 
 * <br/>If a null or empty string is supplied, the
 * default delimiter will be used (",").
 * @param delimiter the delimiter
 */
public void setDelimiter(String delimiter) {
  _delimiter = Val.chkStr(delimiter);
  if (_delimiter.length() == 0) {
    _delimiter = getDefaultDelimeter();
  }
  if (_delimiter.length() == 0) {
    _delimiter = ",";
  }
}

/**
 * Gets the default delimiter (",").
 * @return the default max length
 */
public String getDefaultDelimeter() {
  return ",";
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>key maxlength cols rows delimiter
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setDelimiter(DomUtil.getAttributeValue(attributes,"delimiter"));
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new InputDelimitedTextArea(this);
 */
@Override
public InputDelimitedTextArea duplicate() {
  return new InputDelimitedTextArea(this);
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
  sb.append(" delimiter=\"").append(getDelimiter()).append("\"");
}

/**
 * Makes a delimited string multiple values associated with the parameter.
 * @param parameter the associated parameter
 * @return the delimited string
 */
protected String makeDelimitedValue(Parameter parameter) {
  StringBuffer sb = new StringBuffer();
  for (ContentValue value: parameter.getContent().getMultipleValues()) {
    String sValue = Val.chkStr(value.getValue());
    if (sValue.length() > 0) {
      if (sb.length() > 0) {
        sb.append(getDelimiter()).append(" ");
      }
      sb.append(sValue);
    }
  }
  return sb.toString();
}

/**
 * Makes a Faces HtmlInputTextarea component for a parameter.
 * <p/>
 * The multiple values associated with the parameter 
 * (parameter.getMultipleValues()) are used to generate
 * a delimited single value. The super class method 
 * (InputTextArea.makeInputComponent) is then invoked to create
 * component.
 * <p/>
 * The reverse operation is executed on the unBind() method.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI component
 */
@Override
public UIComponent makeInputComponent(UiContext context,
                                      Section section,
                                      Parameter parameter) {
  parameter.getContent().getSingleValue().setValue(makeDelimitedValue(parameter));
  return super.makeInputComponent(context,section,parameter);
}

/**
 * Triggered on the save event from the metadata editor.
 * <p/>
 * On this event, the multiple values associated with the parameter
 * (parameter.getMultipleValues()) are generated from the delimited 
 * input string.
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
  
  // tokenize the delimited input string
  UIInput input = findInputComponent(context,editorForm);
  String sDelimited = Val.chkStr(getInputValue(input));
  sDelimited = sDelimited.replaceAll("(\r\n|\r|\n|\n\r)",getDelimiter());
  String[] tokens = Val.tokenize(sDelimited,getDelimiter());
  
  // clear current values, append each token to the values collection
  parameter.getContent().clearAllValues();
  ContentValues values = parameter.getContent().getMultipleValues();
  for (String sValue: tokens) {
    if (sValue.length() > 0) {
      values.add(new ContentValue(sValue));
    }
  }
  input.setValue(makeDelimitedValue(parameter));
}

}
