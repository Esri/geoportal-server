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
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.ConfigurationException;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.provider.DescribeRecordProvider;
import com.esri.gpt.server.csw.provider.GetCapabilitiesProvider;
import com.esri.gpt.server.csw.provider.GetRecordByIdProvider;
import com.esri.gpt.server.csw.provider.GetRecordsProvider;
import com.esri.gpt.server.csw.provider.TransactionProvider;
import com.esri.gpt.server.csw.provider.components.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Instantiates components associated with the execution of a 
 * requested CSW operation against the local catalog.
 */
public class ProviderFactory implements IProviderFactory {
     
  /** constructors ============================================================ */
  
  /** Default constructor */
  public ProviderFactory() {}
  
  /** main ==================================================================== */
  
  /**
   * Main unit test method.
   * @param args startup arguments
   */
  public static void main(String[] args) {
    RequestContext rc = null;
    try {
      rc = RequestContext.extract(null);
      
      String cswCtx = "/csw";
      String resPfx = "gpt/metadata/";
      
      String testFolder = "C:/Projects/GPT10/Portal/src/testdata/cswxmls";
      String path = testFolder+"/GetCapabilities_1.xml";
      
      path = testFolder+"/DescribeRecord_1.xml";
      //path = testFolder+"/GetCapabilities_1.xml";
      //path = testFolder+"/GetRecordById_1.xml";
      //path = testFolder+"/GetRecords_1.xml";
      
      String xml = com.esri.gpt.framework.xml.XmlIoUtil.readXml(path);
      System.err.println(xml);
      
      IProviderFactory self = new ProviderFactory();
      RequestHandler handler = self.makeRequestHandler(null,rc,cswCtx,resPfx);
      
      OperationResponse opResponse = handler.handleXML(xml);
      System.err.println(opResponse.getResponseXml());
      
    } catch (Throwable t) {
      t.printStackTrace(System.err);
    } finally {
      if (rc != null) rc.onExecutionPhaseCompleted();
    }
  }
    
  /** methods ================================================================= */
    
  /**
   * Makes an adapter for a csw:CqlText expression.
   * @param context the operation context
   * @param version the CSW constraint version
   * @return the CQL adapter
   * @throws OwsException if the method is unsupported
   */
  public ICqlParser makeCqlParser(OperationContext context, String version)
    throws OwsException {
    String locator = "csw:CqlText";
    String msg = locator+" is not supported.";
    if (context.getRequestOptions().getRequestXml() == null) {
      locator = "constraintLanguage";
      msg = "CQL_TEXT is not supported.";
    }
    throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
  }
  
  /**
   * Makes an adapter for an ogc:Filter.
   * @param context the operation context
   * @param version the CSW constraint version
   * @return the filter adapter
   * @throws OwsException if the method is unsupported
   */
  public IFilterParser makeFilterParser(OperationContext context, String version)
    throws OwsException {
    version = Val.chkStr(version);
    if (version.equals("") || version.equals("1.0.0") || version.equals("1.1.0")) {
      return new QueryFilterParser(context);
    } else {
      String locator = "csw:Constraint/@version";
      String msg = version+" is not supported.";
      if (context.getRequestOptions().getRequestXml() == null) {
        locator = "constraint_language_version";
      }
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    }
  }
  
  /**
   * Makes an operation provider for a given operation name.
   * @param context the operation context
   * @param operationName the operation name
   * @return the operation provider
   * @throws OwsException if the method is unsupported
   */
  public IOperationProvider makeOperationProvider(OperationContext context, 
                                                  String operationName) 
    throws OwsException {
    
    ServiceProperties svcProps = context.getServiceProperties();
    SupportedParameters parameters = svcProps.getSupportedParameters();
    SupportedValues values;
    String resPfx = svcProps.getResourceFilePrefix();
    IOperationProvider opProvider = null;
    RequestOptions reqOptions = context.getRequestOptions();
    
    // output format
    values = new SupportedValues("application/xml,text/xml",",");
    parameters.add(new SupportedParameter(CswConstants.Parameter_OutputFormat,values));
    
    // GetCapabilities
    if (operationName.equalsIgnoreCase("GetCapabilities")) {
      GetCapabilitiesProvider gcp = new GetCapabilitiesProvider();
      opProvider = gcp;
      String loc = resPfx+"Capabilities.xml";
      reqOptions.getCapabilityOptions().setCapabilitiesLocation(loc);
      
    // DescribeRecord
    } else if (operationName.equals("DescribeRecord")) {
      DescribeRecordProvider drp = new DescribeRecordProvider();
      opProvider = drp;
      String loc = resPfx+"DescribeRecord.xml";
      reqOptions.getDescribeRecordOptions().setDescribeRecordLocation(loc); 
      
      values = new SupportedValues("XMLSCHEMA,http://www.w3.org/XML/Schema",",");
      parameters.add(new SupportedParameter(CswConstants.Parameter_SchemaLanguage,values));
      
    // GetRecordById
    } else if (operationName.equals("GetRecordById")) {
      opProvider = new GetRecordByIdProvider();
      
      values = new SupportedValues(
          "csw:Record,http://www.opengis.net/cat/csw/2.0.2,original,http://www.isotc211.org/2005/gmd",",");
      parameters.add(new SupportedParameter(CswConstants.Parameter_OutputSchema,values));
      values = new SupportedValues("brief,summary,full",",");
      parameters.add(new SupportedParameter(CswConstants.Parameter_ElementSetType,values));
      
    // GetRecords
    } else if (operationName.equals("GetRecords")) { 
      GetRecordsProvider grp = new GetRecordsProvider();
      opProvider = grp;
      
      values = new SupportedValues(
          "csw:Record,http://www.opengis.net/cat/csw/2.0.2,http://www.isotc211.org/2005/gmd",",");
      parameters.add(new SupportedParameter(CswConstants.Parameter_OutputSchema,values));
      
      // query type names
      
      values = new SupportedValues("brief,summary,full",",");
      parameters.add(new SupportedParameter(CswConstants.Parameter_ElementSetType,values));
      
      parameters.add(new SupportedParameter(CswConstants.Parameter_ElementName,new AnySupportedValues()));
      
      values = new SupportedValues("hits,results,validate",",");
      parameters.add(new SupportedParameter(CswConstants.Parameter_ResultType,values));
      
      values = new SupportedValues("1.0.0,1.1.0",",");
      parameters.add(new SupportedParameter(CswConstants.Parameter_ConstraintVersion,values));
      
      parameters.add(new SupportedParameter(CswConstants.Parameter_ConstraintCql,new NoSupportedValues()));
      
      // Transaction
    } else if (operationName.equals("Transaction")) { 
      if (!svcProps.getAllowTransactions()) {
        throw new OwsException("transactions are not allowed at this end-point.");
      }
      TransactionProvider tp = new TransactionProvider();
      opProvider = tp;
      
      values = new SupportedValues("1.0.0,1.1.0",",");
      parameters.add(new SupportedParameter(CswConstants.Parameter_ConstraintVersion,values));
    }
        
    return opProvider;
  }
  
  /**
   * Makes a provider for documents in their original XML schema.
   * @param context the operation context
   * @return the original XML provider
   * @throws OwsException if the method is unsupported
   */
  public IOriginalXmlProvider makeOriginalXmlProvider(OperationContext context) 
    throws OwsException {
    return new OriginalXmlProvider();
  }
  
  /**
   * Makes an evaluator for a CSW query.
   * @param context the operation context
   * @return the query evaluator
   * @throws OwsException if the method is unsupported
   */
  public IQueryEvaluator makeQueryEvaluator(OperationContext context)
    throws OwsException {
    return new QueryEvaluator(context);
  }
    
  /**
   * Makes a CSW request handler.
   * @param request the HTTP servlet request
   * @param requestContext the active request context
   * @param cswSubContextPath the HTTP sub-context path associated with the CSW service
   * @param resourceFilePrefix the path prefix for XML/XSLT resource files
   * @return the request handler
   */
  public RequestHandler makeRequestHandler(HttpServletRequest request,
                                           RequestContext requestContext,
                                           String cswSubContextPath,
                                           String resourceFilePrefix) {
    
    // make the operation context
    OperationContext context = new OperationContext();
    context.setProviderFactory(this);
    context.setRequestContext(requestContext);
        
    // set the service properties
    ServiceProperties svcProps = new ServiceProperties();
    context.setServiceProperties(svcProps);
    if (request != null) {
      svcProps.setHttpContextPath(RequestContext.resolveBaseContextPath(request));
    } else if (requestContext!= null) {
      if (requestContext.getServletRequest() instanceof HttpServletRequest) {
        HttpServletRequest hr = (HttpServletRequest)requestContext.getServletRequest();
        svcProps.setHttpContextPath(RequestContext.resolveBaseContextPath(hr));
      }
    }
    svcProps.setCswSubContextPath(cswSubContextPath);
    svcProps.setResourceFilePrefix(resourceFilePrefix);
        
    // supported parameters
    SupportedParameters parameters = context.getServiceProperties().getSupportedParameters();
    SupportedValues values;
    
    // supported service name and versions
    values = new SupportedValues("CSW",",");
    parameters.add(new SupportedParameter(CswConstants.Parameter_Service,values));
    values = new SupportedValues("2.0.2",",");
    parameters.add(new SupportedParameter(CswConstants.Parameter_Version,values));
    
    // supported operations
    values = new SupportedValues(
        "GetCapabilities,DescribeRecord,GetRecordById,GetRecords,Transaction",",");
    parameters.add(new SupportedParameter(CswConstants.Parameter_OperationName,values));
    
    // make and return the request handler
    RequestHandler handler = new RequestHandler();
    handler.setOperationContext(context);
    
    // initialize the language code (INSPIRE requirement but generally applicable)
    if (request != null) {
      ParseHelper pHelper = new ParseHelper();
      String[] parsed = pHelper.getParameterValues(request,"language");
      if ((parsed != null) && (parsed.length) > 0) {
        String tmp = Val.chkStr(parsed[0]);
        if (tmp.length() > 0) {
          CapabilityOptions cOptions = context.getRequestOptions().getCapabilityOptions();
          cOptions.setLanguageCode(tmp);
        }
      }
    }
    
    return handler;
  }
     
  /**
   * Makes an appropriate CSW operation response generator.
   * @param context the operation context
   * @return the response generator
   * @throws OwsException if the method is unsupported
   */
  public IResponseGenerator makeResponseGenerator(OperationContext context)
    throws OwsException {
    String opName = Val.chkStr(context.getOperationName());
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    
    if (opName.equalsIgnoreCase(CswConstants.Operation_GetCapabilities)) {
      return new GetCapabilitiesResponse();
      
    } else if (opName.equalsIgnoreCase(CswConstants.Operation_DescribeRecord)) {
      return new DescribeRecordResponse();
      
    } else if (opName.equalsIgnoreCase(CswConstants.Operation_GetRecordById)) {
      return new QueryResponse(context);
      
    } else if (opName.equalsIgnoreCase(CswConstants.Operation_GetRecords)) {
      String resultType = Val.chkStr(qOptions.getResultType());
      boolean isValidate = resultType.equalsIgnoreCase(CswConstants.ResultType_Validate);
      if (isValidate) {
        return new AcknowlegementResponse();
      } else {
        return new QueryResponse(context);
      }
      
    } else if (opName.equalsIgnoreCase(CswConstants.Operation_Transaction)) {
      return new TransactionResponse();
      
    }
    return null;
  }
  
  /**
   * Makes an adapter for an ogc:SortyBy clause.
   * @param context the operation context
   * @return the sortBy adapter
   * @throws OwsException if the method is unsupported
   */
  public ISortByParser makeSortByParser(OperationContext context)
    throws OwsException {
    return new SortByParser(context);
  }
  
  /**
   * Instantiates a CSW request handler the local catalog.
   * @param requestContext the active request context
   * @return the request handler
   */
  public static RequestHandler newHandler(RequestContext requestContext) {
    IProviderFactory factory = ProviderFactory.newFactory(requestContext);
    return factory.makeRequestHandler(null,requestContext,"/csw","gpt/metadata/csw");
  }
  
  /**
   * Instantiates a CSW provider factory for the local catalog.
   * <p/>
   * By default, a new instance of 
   * com.esri.gpt.server.csw.provider.local.ProviderFactory is returned.
   * <p/>
   * This can be overridden by the configuration parameter:
   * /gptConfig/catalog/parameter@key="csw.provider.local.IProviderFactory"
   * @param requestContext the active request context
   * @return the provider factory
   */
  public static IProviderFactory newFactory(RequestContext requestContext) {
    CatalogConfiguration catCfg = null;
    if (requestContext != null) {
      catCfg = requestContext.getCatalogConfiguration();
    } else {
      catCfg = ApplicationContext.getInstance().getConfiguration().getCatalogConfiguration();
    }
    
    String key = "csw.provider.local.IProviderFactory";
    String className = Val.chkStr(catCfg.getParameters().getValue(key));
    if (className.length() == 0) {
      return new ProviderFactory();
    } else {

      try {
        Class<?> cls = Class.forName(className);
        Object obj = cls.newInstance();
        if (obj instanceof IProviderFactory) {
          return (IProviderFactory)obj;
        } else {
          String sMsg = "The configured "+key+" parameter is invalid: "+ className;
          throw new ConfigurationException(sMsg);
        }
      } catch (ConfigurationException t) {
        throw t;
      } catch (Throwable t) {
        String sMsg = "Error instantiating provider factory: " + className;
        throw new ConfigurationException(sMsg, t);
      }
    }
  }
  
}
