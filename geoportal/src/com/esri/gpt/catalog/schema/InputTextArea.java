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
import javax.faces.component.html.HtmlInputTextarea;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Input text area component associated with a metadata parameter.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * <br/>&lt;parameter key="abstract"&gt;
 * <br/>&lt;input type="textArea" cols="60" rows="8"/&gt;
 * <br/>&lt;/parameter&gt;
 */
public class InputTextArea extends InputText {

// class variables =============================================================
  
// instance variables ==========================================================
private int _cols;
private int _rows;
  
// constructors ================================================================

/** Default constructor. */
public InputTextArea() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public InputTextArea(InputTextArea objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate == null) {
    setCols(getDefaultCols());
    setRows(getDefaultRows()); 
  } else {
    setCols(objectToDuplicate.getCols());
    setRows(objectToDuplicate.getRows());   
  }
}

// properties ==================================================================

/**
 * Gets the default number of columns (60).
 * @return the default number of columns
 */
public int getDefaultCols() {
  return 60;
}

/**
 * Gets the default max length (4000).
 * @return the default max length
 */
@Override
public int getDefaultMaxlength() {
  return 4000;
}

/**
 * Gets the default number of rows (8).
 * @return the number of rows
 */
public int getDefaultRows() {
  return 8;
}

/**
 * Gets the number of columns in the text area component.
 * @return the number of columns
 */
public int getCols() {
  return _cols;
}
/**
 * Sets the number of columns in the text area component.
 * @param size the number of columns
 */
public void setCols(int size) {
  _cols = (size < 1) ? getDefaultCols() : size;
}

/**
 * Gets the number of rows in the text area component.
 * @return the number of rows
 */
public int getRows() {
  return _rows;
}
/**
 * Sets the number of rows in the text area component.
 * @param size the number of rows
 */
public void setRows(int size) {
  _rows = (size < 1) ? getDefaultRows() : size;
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>cols rows
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setCols(Val.chkInt(DomUtil.getAttributeValue(attributes,"cols"),-1));
  setRows(Val.chkInt(DomUtil.getAttributeValue(attributes,"rows"),-1));
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new InputTextArea(this);
 */
@Override
public InputTextArea duplicate() {
  return new InputTextArea(this);
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
  sb.append(" cols=\"").append(getCols()).append("\"");
  sb.append(" rows=\"").append(getRows()).append("\"");
}

/**
 * Makes a Faces HtmlInputTextarea component for a parameter.
 * @param context the UI context
 * @param section the parent section
 * @param parameter the associated parameter
 * @return the UI component
 */
@Override
public UIComponent makeInputComponent(UiContext context,
                                      Section section,
                                      Parameter parameter) {  
  
  // make the component
  HtmlInputTextarea component = new HtmlInputTextarea();
  component.setId(getFacesId());
  component.setCols(getCols());
  component.setRows(getRows());
  component.setDisabled(!getEditable());
  component.setOnchange(getOnChange());
  component.setOnclick(getOnClick());
  setComponentValue(context,component,parameter);
  return applyHint(context,component);
}

}
