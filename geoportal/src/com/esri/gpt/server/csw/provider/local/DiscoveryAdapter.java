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
package com.esri.gpt.server.csw.provider.local;
import com.esri.gpt.catalog.discovery.Discoverable;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Node;

/**
 * Super-class for adapting CSW operation components to the local discovery model.
 */
public class DiscoveryAdapter {
    
  /** instance variables ====================================================== */
  private DiscoveryContext discoveryContext;
  
  /** constructors ============================================================ */
  
  /** 
   * Constructs with a supplied operation context
   * @param context the operation context
   */
  public DiscoveryAdapter(OperationContext context) {
    String key = "DiscoveryAdapter.DiscoveryContext";
    DiscoveryContext dCtx = (DiscoveryContext)context.getAdditionalProperties().get(key);
    if (dCtx == null) {
      dCtx = new DiscoveryContext(context);
      context.getAdditionalProperties().put(key,dCtx);
    }
    this.discoveryContext = dCtx;
  }
  
  /** properties ============================================================== */
  
  /**
   * Gets the active discovery context.
   * @return the active discovery context
   */
  public DiscoveryContext getDiscoveryContext() {
    return this.discoveryContext;
  }
  
  /** methods ================================================================= */
  
  /**
   * Parses a parent node for an associated property name (ogc:PropertyName).
   * @param parent the parent of the property to locate
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @return a discoverable associated with the property name
   * @throws OwsException if validation fails
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  public Discoverable parsePropertyName(Node parent, XPath xpath) 
    throws OwsException, XPathExpressionException  {
    
    // parse the property name
    String locator = "PropertyName";
    Node ndPropName = (Node)xpath.evaluate("ogc:PropertyName",parent,XPathConstants.NODE);
    if (ndPropName == null) {
      String msg = "The parameter was not found";
      throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
    } 
    String sPropName = Val.chkStr(ndPropName.getTextContent());
    if (sPropName.length() == 0) {
      String msg = "The parameter value was empty.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    }
    
    // find the discoverable
    Discoverable discoverable = this.getDiscoveryContext().findDiscoverable(sPropName);
    if (discoverable == null) {
      String msg = sPropName+" is not a supported queryable.";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    }
    return discoverable;
  }
  
}
