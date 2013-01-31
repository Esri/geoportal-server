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
import com.esri.gpt.server.csw.provider.components.CswConstants;
import com.esri.gpt.server.csw.provider.components.DescribeRecordOptions;
import com.esri.gpt.server.csw.provider.components.IOperationProvider;
import com.esri.gpt.server.csw.provider.components.IProviderFactory;
import com.esri.gpt.server.csw.provider.components.IResponseGenerator;
import com.esri.gpt.server.csw.provider.components.ISupportedValues;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.ParseHelper;
import com.esri.gpt.server.csw.provider.components.ServiceProperties;
import com.esri.gpt.server.csw.provider.components.ValidationHelper;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import org.w3c.dom.Node;

/**
 * Provides the CSW DescribeRecord operation.
 */
public class DescribeRecordProvider implements IOperationProvider {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(DescribeRecordProvider.class.getName());
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public DescribeRecordProvider() {
    super();
  }
          
  /** methods ================================================================= */
  
  /**
   * Executes a parsed operation request.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void execute(OperationContext context) throws Exception {
    IProviderFactory factory = context.getProviderFactory();
    IResponseGenerator generator = factory.makeResponseGenerator(context);
    if (generator == null) {
      String msg = "IProviderFactory.makeResponseGenerator: instantiation failed.";
      LOGGER.log(Level.SEVERE,msg);
      throw new OwsException(msg);
    } else {
      generator.generateResponse(context);
    } 
  }
  
  /**
   * Handles a URL based request (HTTP GET).
   * @param context the operation context
   * @param request the HTTP request
   * @throws Exception if a processing exception occurs
   */
  public void handleGet(OperationContext context, HttpServletRequest request) 
    throws Exception {
    
    // initialize
    LOGGER.finer("Handling csw:DescribeRecord request URL...");
    DescribeRecordOptions drOptions = context.getRequestOptions().getDescribeRecordOptions();
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    
    // service and version are parsed by the parent RequestHandler
    
    // output format
    locator = "outputFormat";
    parsed = pHelper.getParameterValues(request,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputFormat);
    context.getOperationResponse().setOutputFormat(
        vHelper.validateValue(supported,locator,parsed,false));
    
    // schema language
    locator = "schemaLanguage";
    parsed = pHelper.getParameterValues(request,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_SchemaLanguage);
    drOptions.setSchemaLanguage(vHelper.validateValue(supported,locator,parsed,false));
    
    // type names
    locator = "TypeName";
    parsed = pHelper.getParameterValues(request,locator,",");
    drOptions.setTypeNames(vHelper.validateValues(locator,parsed,false));
    
    // execute
    this.execute(context);
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
    LOGGER.finer("Handling csw:DescribeRecord request XML...");
    DescribeRecordOptions drOptions = context.getRequestOptions().getDescribeRecordOptions();
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    
    // service and version are parsed by the parent RequestHandler
    
    // output format
    locator = "@outputFormat";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputFormat);
    context.getOperationResponse().setOutputFormat(
        vHelper.validateValue(supported,locator,parsed,false));
    
    // schema language
    locator = "@schemaLanguage";  
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_SchemaLanguage);
    drOptions.setSchemaLanguage(vHelper.validateValue(supported,locator,parsed,false));
    
    // type names
    locator = "csw:TypeName";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    drOptions.setTypeNames(vHelper.validateValues(locator,parsed,false)); 
    
    // execute
    this.execute(context);
  }
  
}
