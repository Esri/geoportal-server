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
import com.esri.gpt.server.csw.provider.components.CapabilityOptions;
import com.esri.gpt.server.csw.provider.components.CswConstants;
import com.esri.gpt.server.csw.provider.components.IOperationProvider;
import com.esri.gpt.server.csw.provider.components.IProviderFactory;
import com.esri.gpt.server.csw.provider.components.IResponseGenerator;
import com.esri.gpt.server.csw.provider.components.ISupportedValues;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.ParseHelper;
import com.esri.gpt.server.csw.provider.components.ServiceProperties;
import com.esri.gpt.server.csw.provider.components.SupportedValues;
import com.esri.gpt.server.csw.provider.components.ValidationHelper;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import org.w3c.dom.Node;

/**
 * Provides the CSW GetCapabilities operation.
 */
public class GetCapabilitiesProvider implements IOperationProvider {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(GetCapabilitiesProvider.class.getName());
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public GetCapabilitiesProvider() {
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
    LOGGER.finer("Handling csw:GetCapabilities request URL...");
    CapabilityOptions cOptions = context.getRequestOptions().getCapabilityOptions();
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    
    // service and version and language code are parsed by the parent RequestHandler,
    // the language code is initialized within the associated IProviderFactory
                
    // output format
    locator = "acceptFormats";
    parsed = pHelper.getParameterValues(request,locator,",");
    supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputFormat);
    String outputFormat = Val.chkStr(vHelper.negotiateValue(supported,locator,parsed,false));
    if (outputFormat.length() > 0) {
      context.getOperationResponse().setOutputFormat(outputFormat);
    } else {
      locator = "outputFormat";
      parsed = pHelper.getParameterValues(request,locator);
      supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputFormat);
      outputFormat = vHelper.validateValue(supported,locator,parsed,false);
      context.getOperationResponse().setOutputFormat(outputFormat);
    }
    
    // sections (an empty sections parameter should return all non-optional sections)
    locator = "sections";
    parsed = pHelper.getParameterValues(request,locator,",");
    if ((parsed != null) && (parsed.length == 0)) {
      cOptions.setSections(new StringSet());
      cOptions.getSections().add("Filter_Capabilities");
    } else {
      supported = new SupportedValues(
          "ServiceIdentification,ServiceProvider,OperationsMetadata,Filter_Capabilities",",");
      cOptions.setSections(vHelper.validateValues(supported,locator,parsed,false));
    }
    
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
    LOGGER.finer("Handling csw:GetCapabilities request XML...");
    CapabilityOptions cOptions = context.getRequestOptions().getCapabilityOptions();
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;

    // service and version are parsed by the parent RequestHandler
        
    // output format
    locator = "ows:AcceptFormats/ows:OutputFormat";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputFormat);
    String outputFormat = Val.chkStr(vHelper.negotiateValue(supported,locator,parsed,false));
    if (outputFormat.length() > 0) {
      context.getOperationResponse().setOutputFormat(outputFormat);
    } else {
      locator = "@outputFormat";
      parsed = pHelper.getParameterValues(root,xpath,locator);
      supported = svcProps.getSupportedValues(CswConstants.Parameter_OutputFormat);
      outputFormat = vHelper.validateValue(supported,locator,parsed,false);
      context.getOperationResponse().setOutputFormat(outputFormat);
    }
    
    // sections (an empty sections parameter should return all non-optional sections)
    locator = "ows:Sections/ows:Section";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = new SupportedValues(
        "ServiceIdentification,ServiceProvider,OperationsMetadata,Filter_Capabilities",",");
    if (parsed == null) {
      parsed = pHelper.getParameterValues(root,xpath,"ows:Sections");
      if (parsed != null) {
        cOptions.setSections(new StringSet());
        cOptions.getSections().add("Filter_Capabilities");
      } else {
        cOptions.setSections(vHelper.validateValues(supported,locator,parsed,false));
      }
    } else {
      cOptions.setSections(vHelper.validateValues(supported,locator,parsed,false));
    }
    
    // execute
    this.execute(context);
  }
  
}
