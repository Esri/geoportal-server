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
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.provider.components.CswConstants;
import com.esri.gpt.server.csw.provider.components.IOperationProvider;
import com.esri.gpt.server.csw.provider.components.ISupportedValues;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.ParseHelper;
import com.esri.gpt.server.csw.provider.components.ServiceProperties;
import com.esri.gpt.server.csw.provider.components.SupportedValues;
import com.esri.gpt.server.csw.provider.components.TransactionOptions;
import com.esri.gpt.server.csw.provider.components.ValidationHelper;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Provides the CSW Transaction operation.
 */
public class TransactionProvider implements IOperationProvider {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(TransactionProvider.class.getName());
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public TransactionProvider() {
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
    LOGGER.finer("Handling csw:Transaction request URL...");
    throw new OwsException("HTTP Get is not supported for this operation."); 
    
    /*
    // initialize
    LOGGER.finer("Handling csw:Transaction request URL...");
    TransactionOptions tOptions = context.getRequestOptions().getTransactionOptions();
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    IOperationProvider opProvider = null;
    
    // service and version are parsed by the parent RequestHandler
    
    // output format
    locator = "outputFormat";
    parsed = pHelper.getParameterValues(request,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputFormat);
    context.getOperationResponse().setOutputFormat(
        vHelper.validateValue(supported,locator,parsed,false));
        
    // verbose response
    locator = "verboseResponse";
    parsed = pHelper.getParameterValues(request,locator);
    supported = new SupportedValues("true,false",",");
    String verbose = Val.chkStr(vHelper.validateValue(supported,locator,parsed,false));
    tOptions.setVerboseResponse(verbose.equalsIgnoreCase("true"));
    
    // request ID
    locator = "requestId";
    parsed = pHelper.getParameterValues(request,locator);
    tOptions.setRequestId(vHelper.validateValue(locator,parsed,false));
    
    // determine the sub-operation
    locator = "transactionType";
    parsed = pHelper.getParameterValues(request,locator);
    supported = new SupportedValues("Insert,Update,Delete",",");
    String transactionType = vHelper.validateValue(supported,locator,parsed,true);
    if (transactionType.equalsIgnoreCase("Insert")) {
      opProvider = new InsertProvider();
    } else if (transactionType.equalsIgnoreCase("Update")) {
      opProvider = new UpdateProvider();
    } else if (transactionType.equalsIgnoreCase("Delete")) {
      opProvider = new DeleteProvider();
    }
    
    // handle the sub-operation
    opProvider.handleGet(context,request);
    */
    
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
    LOGGER.finer("Handling csw:Transaction request XML...");
    TransactionOptions tOptions = context.getRequestOptions().getTransactionOptions();
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    NodeList nlActions;
    IOperationProvider opProvider = null;
    
    // service and version are parsed by the parent RequestHandler
    
    // output format
    locator = "@outputFormat";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputFormat);
    context.getOperationResponse().setOutputFormat(
        vHelper.validateValue(supported,locator,parsed,false));
    
    
    // verbose response
    locator = "@verboseResponse";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = new SupportedValues("true,false",",");
    String verbose = Val.chkStr(vHelper.validateValue(supported,locator,parsed,false));
    tOptions.setVerboseResponse(verbose.equalsIgnoreCase("true"));
        
    // request ID
    locator = "@requestId";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    tOptions.setRequestId(vHelper.validateValue(locator,parsed,false));
    
    // determine the sub-operation

    // Insert
    if (opProvider == null) {
      locator = "csw:Insert";
      nlActions = (NodeList)xpath.evaluate(locator,root,XPathConstants.NODESET);
      if ((nlActions != null) && (nlActions.getLength() > 0)) {
        tOptions.setTransactionType(CswConstants.TransactionType_Insert);
        opProvider = new InsertProvider();
      }
    }
    
    // Update
    if (opProvider == null) {
      locator = "csw:Update";
      nlActions = (NodeList)xpath.evaluate(locator,root,XPathConstants.NODESET);
      if ((nlActions != null) && (nlActions.getLength() > 0)) {
        tOptions.setTransactionType(CswConstants.TransactionType_Update);
        opProvider = new UpdateProvider();
      }
    }
    
    // Delete
    if (opProvider == null) {
      locator = "csw:Delete";
      nlActions = (NodeList)xpath.evaluate(locator,root,XPathConstants.NODESET);
      if ((nlActions != null) && (nlActions.getLength() > 0)) {
        tOptions.setTransactionType(CswConstants.TransactionType_Delete);
        opProvider = new DeleteProvider();
      }
    }
    
    // handle the sub-operation
    if (opProvider != null) {
      opProvider.handleXML(context,root,xpath);
    } else {
      locator = "csw:Transaction";
      String msg = "The transaction operation node was missing.";
      throw new OwsException(OwsException.OWSCODE_MissingParameterValue,locator,msg);
    }
  }
  
}
