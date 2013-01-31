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
import com.esri.gpt.framework.collection.StringSet;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.provider.components.CswConstants;
import com.esri.gpt.server.csw.provider.components.IOperationProvider;
import com.esri.gpt.server.csw.provider.components.IOriginalXmlProvider;
import com.esri.gpt.server.csw.provider.components.IProviderFactory;
import com.esri.gpt.server.csw.provider.components.IQueryEvaluator;
import com.esri.gpt.server.csw.provider.components.IResponseGenerator;
import com.esri.gpt.server.csw.provider.components.ISupportedValues;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.ParseHelper;
import com.esri.gpt.server.csw.provider.components.QueryOptions;
import com.esri.gpt.server.csw.provider.components.ServiceProperties;
import com.esri.gpt.server.csw.provider.components.ValidationHelper;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import org.w3c.dom.Node;

/**
 * Provides the CSW GetRecordById operation.
 */
public class GetRecordByIdProvider implements IOperationProvider {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(GetRecordByIdProvider.class.getName());
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public GetRecordByIdProvider() {
    super();
  }
          
  /** methods ================================================================= */
  
  /**
   * Executes a parsed operation request.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void execute(OperationContext context) throws Exception {   
    
    // initialize
    LOGGER.finer("Executing csw:GetRecordById request...");
    IProviderFactory factory = context.getProviderFactory();
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    String outputSchema = Val.chkStr(qOptions.getOutputSchema());
 
    if (outputSchema.equalsIgnoreCase("original")) {
      
      // respond with the original XML
      this.validateIfOriginalSchema(context,"csw:Id","@outputSchema");
      
      IOriginalXmlProvider oxp = factory.makeOriginalXmlProvider(context);
      if (oxp == null) {
        String msg = "IProviderFactory.makeOriginalXmlProvider: instantiation failed.";
        LOGGER.log(Level.SEVERE,msg);
        throw new OwsException(msg);         
      } else {
        String id = qOptions.getIDs().iterator().next();
        String xml = oxp.provideOriginalXml(context,id);
        context.getOperationResponse().setResponseXml(xml); 
      }
      
    } else {
      
      // evaluate the query
      IQueryEvaluator evaluator = factory.makeQueryEvaluator(context);
      if (evaluator == null) {
        String msg = "IProviderFactory.makeQueryEvaluator: instantiation failed.";
        LOGGER.log(Level.SEVERE,msg);
        throw new OwsException(msg);
      } else {
        evaluator.evaluateIdQuery(context,qOptions.getIDs().toArray(new String[0]));
      }
            
      // generate the response
      IResponseGenerator generator = factory.makeResponseGenerator(context);
      if (generator == null) {
        String msg = "IProviderFactory.makeResponseGenerator: instantiation failed.";
        LOGGER.log(Level.SEVERE,msg);
        throw new OwsException(msg);
      } else {
        generator.generateResponse(context);
      } 
    
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
    LOGGER.finer("Handling csw:GetRecordById request URL...");
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
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
        
    // output schema
    locator = "outputSchema";
    parsed = pHelper.getParameterValues(request,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputSchema);
    qOptions.setOutputSchema(vHelper.validateValue(supported,locator,parsed,false));
    
    // IDs
    locator = "Id";
    parsed = pHelper.getParameterValues(request,locator,",");
    qOptions.setIDs(vHelper.validateValues(locator,parsed,true)); 
    
    // validate the ID count if an original output schema was requested
    this.validateIfOriginalSchema(context,"Id","outputSchema");
    
    // result type
    qOptions.setResultType(CswConstants.ResultType_Results);
    
    // response element set type
    locator = "ElementSetName";
    parsed = pHelper.getParameterValues(request,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_ElementSetType);
    qOptions.setElementSetType(vHelper.validateValue(supported,locator,parsed,false));
    
    // default element set type for GetRecordById is summary
    if (Val.chkStr(qOptions.getElementSetType()).length() == 0) {
      qOptions.setElementSetType(CswConstants.ElementSetType_Summary);
    }
    
    // execute the request
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
    LOGGER.finer("Handling csw:GetRecordById request XML...");
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
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
    
    // output schema
    locator = "@outputSchema";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputSchema);
    qOptions.setOutputSchema(vHelper.validateValue(supported,locator,parsed,false));
    
    // IDs
    locator = "csw:Id";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    qOptions.setIDs(vHelper.validateValues(locator,parsed,true));
    
    // validate the ID count if an original output schema was requested
    this.validateIfOriginalSchema(context,"csw:Id","@outputSchema");
    
    // result type
    qOptions.setResultType(CswConstants.ResultType_Results);
    
    // response element set type 
    locator = "csw:ElementSetName";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_ElementSetType);
    qOptions.setElementSetType(vHelper.validateValue(supported,locator,parsed,false));
    
    // response element set type names
    String elementSetType = qOptions.getElementSetType();
    if (elementSetType != null) {
      locator = "csw:ElementSetName/@typeNames";
      parsed = pHelper.getParameterValues(root,xpath,locator);
      qOptions.setElementSetTypeNames(vHelper.validateValues(locator,parsed,false)); 
    }
    
    // default element set type for GetRecordById is summary
    if (Val.chkStr(qOptions.getElementSetType()).length() == 0) {
      qOptions.setElementSetType(CswConstants.ElementSetType_Summary);
    }
    
    // execute the request
    this.execute(context);
  }
    
  /**
   * Ensures that the ID count is one if an original schema was requested.
   * @param context the operation context
   * @param idLocator the OwsException ID locator
   * @param schemaLocator OwsException output schema locator
   * @throws OwsException if validation fails
   */
  public void validateIfOriginalSchema(OperationContext context,
                                       String idLocator, 
                                       String schemaLocator) 
    throws OwsException {
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    String outputSchema = qOptions.getOutputSchema();
    if ((outputSchema != null) && outputSchema.equalsIgnoreCase("original")) {
      StringSet ids = qOptions.getIDs();
      if ((ids == null) || (ids.size() != 1)) {
        String msg = "Only one Id can be requested when "+schemaLocator+"=original";
        throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,idLocator,msg);
      }
    }
  }
    
}
