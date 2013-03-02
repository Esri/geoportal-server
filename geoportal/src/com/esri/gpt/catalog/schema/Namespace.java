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
 * Namespace component associated with a metadata schema.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * &lt;namespace prefix="gmd" uri="http://www.isotc211.org/2005/gmd"/&gt;
 */
public class Namespace extends Component {

// class variables =============================================================
  
// instance variables ==========================================================
private String _prefix = "";
private String _uri = "";
  
// constructors ================================================================

/** Default constructor. */
public Namespace() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Namespace(Namespace objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate != null) {
    setPrefix(objectToDuplicate.getPrefix());
    setUri(objectToDuplicate.getUri());
  }
}

// properties ==================================================================

/**
 * Gets the prefix.
 * @return the prefix
 */
public String getPrefix() {
  return _prefix;
}
/**
 * Sets the prefix.
 * @param prefix the prefix
 */
public void setPrefix(String prefix) {
  _prefix = Val.chkStr(prefix);
}

/**
 * Gets the uri.
 * @return the uri
 */
public String getUri() {
  return _uri;
}
/**
 * Sets the uri.
 * @param uri the uri
 */
public void setUri(String uri) {
  _uri = Val.chkStr(uri);
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>prefix uri
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setPrefix(DomUtil.getAttributeValue(attributes,"prefix"));
  setUri(DomUtil.getAttributeValue(attributes,"uri"));
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new Namespace(this);
 */
public Namespace duplicate() {
  return new Namespace(this);
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
  sb.append(" prefix=\"").append(getPrefix()).append("\"");
  sb.append(" uri=\"").append(getUri()).append("\"");
}


}
