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
import javax.faces.component.html.HtmlOutputText;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Super-class for a configurable UI component associated with a metadata schema.
 */
public abstract class UiComponent extends Component {

// class variables =============================================================
  
// instance variables ==========================================================
private String _styleClass = "";
  
// constructors ================================================================

/** Default constructor. */
public UiComponent() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public UiComponent(UiComponent objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate != null) {
    setStyleClass(objectToDuplicate.getStyleClass());
  }
}

// properties ==================================================================

/**
 * Gets the CSS style class.
 * @return the style class
 */
public String getStyleClass() {
  return _styleClass;
}
/**
 * Sets the CSS style class.
 * @param name the style class
 */
public void setStyleClass(String name) {
  _styleClass = Val.chkStr(name);
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>key styleClass
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setStyleClass(DomUtil.getAttributeValue(attributes,"styleClass"));
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
  if (getStyleClass().length() > 0) {
    sb.append(" styleClass=\"").append(getStyleClass()).append("\"");
  }
}

/**
 * Makes a Faces HtmlOutputText containing a &lt;br/&gt;
 * @return the UI component
 */
public UIComponent makeBR() {
  HtmlOutputText outText = new HtmlOutputText();
  outText.setEscape(false);
  outText.setValue("<br/>");
  return outText;
}

/**
 * Makes a Faces HtmlOutputText containing &nbsp;
 * @return the UI component
 */
public UIComponent makeNBSP() {
  HtmlOutputText outText = new HtmlOutputText();
  outText.setEscape(false);
  outText.setValue("&nbsp;");
  return outText;
}

}

