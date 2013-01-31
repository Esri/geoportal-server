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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Super-class for a configurable component associated with a metadata schema.
 */
public abstract class Component {

// class variables =============================================================
  
// instance variables ==========================================================
private String _key = "";
  
// constructors ================================================================

/** Default constructor. */
public Component() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Component(Component objectToDuplicate) {
  if (objectToDuplicate != null) {
    setKey(objectToDuplicate.getKey());
  }
}

// properties ==================================================================

/**
 * Gets the key.
 * @return the key
 */
public String getKey() {
  return _key;
}
/**
 * Sets the key.
 * <br/>The key will be trimmed. 
 * <br/>A null key is treated as an empty string.
 * @param key the key
 */
public void setKey(String key) {
  _key = Val.chkStr(key);
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>key
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  setKey(DomUtil.getAttributeValue(attributes,"key"));
}

/**
 * Produces a deep clone of the object.
 * <p/>
 * The typical approach is to invoke a duplication constructor.
 * <br/>Example: return new Parameter(this);
 */
public abstract Component duplicate();

/**
 * Appends property information for the component to a StringBuffer.
 * <br/>The method is intended to support "FINEST" logging.
 * <br/>super.echo should be invoked prior appending any local information.
 * @param sb the StringBuffer to use when appending information
 */
public void echo(StringBuffer sb) {
  if (getKey().length() > 0) {
    sb.append(" key=\"").append(getKey()).append("\"");
  }
}

/**
 * Returns the string representation of the object.
 * @return the string
 */
@Override
public String toString() {
  StringBuffer sb = new StringBuffer(getClass().getName()).append(" (\n");
  echo(sb);
  sb.append("\n) ===== end ").append(getClass().getName());
  return sb.toString();
}

}

