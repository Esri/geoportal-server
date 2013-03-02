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
package com.esri.gpt.server.csw.provider;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides the CSW Transaction/Update operation.
 */
public class UpdateProvider extends InsertProvider {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(UpdateProvider.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public UpdateProvider() {
    super();
  }
          
  /** methods ================================================================= */      
  
  /**
   * Handles a URL based request (HTTP GET).
   * @param context the operation context
   * @param request the HTTP request
   * @throws Exception if a processing exception occurs
   */
  public void handleGet(OperationContext context, HttpServletRequest request) 
    throws Exception {
    
    // initialize
    LOGGER.finer("Handling csw:Transaction/csw:Update request URL...");
    throw new OwsException("HTTP Get is not supported for this operation."); 
  }
  
  /**
   * Handles an XML based request (normally HTTP POST).
   * @param context the operation context
   * @param root the root node
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @throws Exception if a processing exception occurs
   */
  public void handleXML(OperationContext context, Node root, XPath xpath)
    throws Exception {
    
    // initialize
    LOGGER.finer("Handling csw:Transaction/csw:Update request XML...");
    Publisher publisher = new Publisher(context.getRequestContext());;
    
    // find all the Update nodes
    String locator = "csw:Update";
    NodeList nlActions = (NodeList)xpath.evaluate(locator,root,XPathConstants.NODESET);
    if (nlActions != null) {
      for (int i=0;i<nlActions.getLength();i++) {
        Node ndAction = nlActions.item(i);
        String typeName = xpath.evaluate("@typeName",ndAction);
        String handle = xpath.evaluate("@handle",ndAction);
        
        // updates based upon constraints and record properties are not supported
        locator = "csw:Constraint";
        Node nd = (Node)xpath.evaluate(locator,ndAction,XPathConstants.NODE);
        if (nd != null) {
          String msg = "Update transactions based upon "+locator+" are not supported.";
          throw new OwsException(OwsException.OWSCODE_OperationNotSupported,locator,msg);
        }
        locator = "csw:RecordProperty";
        nd = (Node)xpath.evaluate("csw:RecordProperty",ndAction,XPathConstants.NODE);
        if (nd != null) {
          String msg = "Update transactions based upon "+locator+" are not supported.";
          throw new OwsException(OwsException.OWSCODE_OperationNotSupported,locator,msg);
        }
        
        // publish all XML documents contained
        NodeList nlXmls = ndAction.getChildNodes();
        this.processDocumentNodes(context,publisher,typeName,handle,nlXmls);
      }
    }
    
    // generate the response
    this.generateResponse(context);
  }
  
}
