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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Stores a string value associated with a parameter's content.
 */
public class ContentValue extends Component {

// class variables =============================================================
        
// instance variables ==========================================================
private String _value = "";

// constructors ================================================================

/** Default constructor. */
public ContentValue() {
  super();
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public ContentValue(ContentValue objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate != null) {
    setValue(objectToDuplicate.getValue());
  }
}

/**
 * Construct with a supplied value.
 * @param value the value
 */
public ContentValue(String value) {
  super();
  setValue(value);
}

// properties ==================================================================

/**
 * Gets the associated value.
 * @return the value
 */
public String getValue() {
  return _value;
}
/**
 * Sets the associated value.
 * <br/>The value is trimmed.
 * <br/>Null values are treated a empty strings.
 * @param value the value
 */
public void setValue(String value) {
  _value = Val.chkStr(value);
}

// methods =====================================================================

/**
 * Clears the value.
 */
public void clear() {
  setValue("");
}

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
 * <br/>return new ContentValue(this);
 */
public ContentValue duplicate() {
  return new ContentValue(this);
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
  sb.append(" value=").append(getValue());
}

}
