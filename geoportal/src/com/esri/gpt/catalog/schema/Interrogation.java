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
import com.esri.gpt.framework.util.LogUtil;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Interrogation component associated with a metadata schema.
 * <p/>
 * The component is configured from a node with a schema configuration
 * XML document.
 * <p/>
 * Example:<br/> 
 * &lt;interrogation count="count(/metadata/dataIdInfo/*[node()])"/&gt;
 */
public class Interrogation extends Component {

// class variables =============================================================
  
// instance variables ==========================================================
private String _countExpression = "";
private int    _matchedNodeCount = 0;
private String _toKnownSchemaXslt = "";
  
// constructors ================================================================

/** Default constructor. */
public Interrogation() {
  this(null);
}

/**
 * Construct by duplicating an existing object.
 * @param objectToDuplicate the object to duplicate
 */
public Interrogation(Interrogation objectToDuplicate) {
  super(objectToDuplicate);
  if (objectToDuplicate != null) {
    setCountExpression(objectToDuplicate.getCountExpression());
    setMatchedNodeCount(objectToDuplicate.getMatchedNodeCount());
    setToKnownSchemaXslt(objectToDuplicate.getToKnownSchemaXslt());
  }
}

// properties ==================================================================

/**
 * Gets the selection expression.
 * @return the selection expression
 */
public String getCountExpression() {
  return _countExpression;
}
/**
 * Sets the selection expression.
 * @param expression the selection expression
 */
public void setCountExpression(String expression) {
  _countExpression = Val.chkStr(expression);
}

/**
 * Gets the number of nodes matching the count expression.
 * @return the number of matching nodes
 */
public int getMatchedNodeCount() {
  return _matchedNodeCount;
}
/**
 * Sets the number of nodes matching the count expression.
 * @param count the number of matching nodes
 */
public void setMatchedNodeCount(int count) {
  _matchedNodeCount = count;
}

/**
 * Gets the XSLT (file path) for translating to a known schema.
 * <p/>
 * Certain XML structures require translation to a known schema, for
 * example, the translation of an OGC-WMS GetCapabilities response to 
 * a Dublin Core document.
 * @return the file path to the XSLT
 */
public String getToKnownSchemaXslt() {
  return _toKnownSchemaXslt;
}
/**
 * Sets the XSLT (file path) for translating to a known schema.
 * <p/>
 * Certain XML structures require translation to a known schema, for
 * example, the translation of an OGC-WMS GetCapabilities response to 
 * a Dublin Core document.
 * @param xslt the file path to the XSLT
 */
public void setToKnownSchemaXslt(String xslt) {
  _toKnownSchemaXslt = Val.chkStr(xslt);
}

// methods =====================================================================

/**
 * Configures the object based upon a node loaded from a 
 * schema configuration XML.
 * <br/>The super.configure method should be invoked prior to any
 * sub-class configuration.
 * <p/>
 * The following attributes are configured:
 * <br/>count toKnownSchemaXslt
 * @param context the configuration context
 * @param node the configuration node
 * @param attributes the attributes of the configuration node
 */
@Override
public void configure(CfgContext context, Node node, NamedNodeMap attributes) {
  super.configure(context,node,attributes);
  setCountExpression(DomUtil.getAttributeValue(attributes,"count"));
  setToKnownSchemaXslt(DomUtil.getAttributeValue(attributes,"toKnownSchemaXslt"));
}

/**
 * Produces a deep clone of the object.
 * <br/>The duplication constructor is invoked.
 * <br/>return new Interrogation(this);
 */
public Interrogation duplicate() {
  return new Interrogation(this);
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
  sb.append(" count=\"").append(getCountExpression()).append("\"");
  sb.append(" toKnownSchemaXslt=\"").append(getToKnownSchemaXslt()).append("\"");
}

/**
 * Interrogates a metadata document by executing the configured
 * XPath count expression for this interrogation object.
 * @param schema the parent schema
 * @param dom the metadata document to interrogate
 * @param xpath an XPath object configured with an appropriate 
 *        Namespace context for the schema
 * @throws XPathExpressionException if an evaluation expression fails 
 */
public void interrogate(Schema schema, Document dom, XPath xpath) 
  throws XPathExpressionException {
  setMatchedNodeCount(0);
  String sCountExpr = getCountExpression();
  if (sCountExpr.length() > 0) {
    LogUtil.getLogger().finer("Interogating "+schema.getKey()+", expr="+sCountExpr);
    setMatchedNodeCount(Val.chkInt(xpath.evaluate(sCountExpr,dom),0));
  }
}

}
