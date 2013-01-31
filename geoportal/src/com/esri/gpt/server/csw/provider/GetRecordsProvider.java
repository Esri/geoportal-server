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
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.server.csw.provider.components.CswConstants;
import com.esri.gpt.server.csw.provider.components.CswNamespaces;
import com.esri.gpt.server.csw.provider.components.ICqlParser;
import com.esri.gpt.server.csw.provider.components.IFilterParser;
import com.esri.gpt.server.csw.provider.components.IOperationProvider;
import com.esri.gpt.server.csw.provider.components.IProviderFactory;
import com.esri.gpt.server.csw.provider.components.IQueryEvaluator;
import com.esri.gpt.server.csw.provider.components.IResponseGenerator;
import com.esri.gpt.server.csw.provider.components.ISortByParser;
import com.esri.gpt.server.csw.provider.components.ISupportedValues;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.ParseHelper;
import com.esri.gpt.server.csw.provider.components.QueryOptions;
import com.esri.gpt.server.csw.provider.components.ServiceProperties;
import com.esri.gpt.server.csw.provider.components.SupportedValues;
import com.esri.gpt.server.csw.provider.components.ValidationHelper;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Provides the CSW GetRecords operation.
 */
public class GetRecordsProvider implements IOperationProvider {
    
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(GetRecordsProvider.class.getName());
    
  /** constructors ============================================================ */
  
  /** Default constructor */
  public GetRecordsProvider() {
    super();
  }

  /** methods ================================================================= */
  
  /**
   * Builds an ogc:Filter node from HTTP GET parameters.
   * @param namespace the namespace parameter values
   * @param constraintFilter the constraint parameter value
   * @throws Exception if a processing exception occurs
   */
  protected Node buildFilterNode(String[] namespace, String constraintFilter) throws Exception {
    
    // TODO GetRecordsDomBuilder had a different pattern??
    
    // parse namespaces
    // pattern: namespace=xmlns(ogc=http://www.opengis.net/ogc),xmlns(gml=http://www.opengis.net/gml)...
    StringBuilder nsBuffer = new StringBuilder();
    boolean hasCswUri = false;
    boolean hasCswPfx = false;
    String cswPfx = "";
    if (namespace != null) {
      for (String ns: namespace) {
        ns = Val.chkStr(ns);
        String nsPfx = null;
        String nsUri = null;
        
        if (ns.toLowerCase().startsWith("xmlns(")) {
          ns = ns.substring(6);
          if (ns.toLowerCase().endsWith(")")) {
            ns = ns.substring(0,ns.length() - 1);
          }
          ns = Val.chkStr(ns);
          if (ns.length() > 0) {
            String[] pair = ns.split("=");
            if (pair.length == 1) {
              nsUri = Val.chkStr(pair[0]);
            } else if (pair.length == 2) {
              nsPfx = Val.chkStr(pair[0]);
              nsUri = Val.chkStr(pair[1]);
            }
          }
        }
        if ((nsUri == null) || (nsUri.length() == 0)) {
          String msg = "The namespace must follow the following pattern:";
          msg += " xmlns(pfx1=uri1),xmlns(pfx2=uri2),...";
          throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,"namespace",msg);
        } else {
          if (nsUri.equals("http://www.opengis.net/cat/csw/")) {
            hasCswUri = true;
            if ((nsPfx != null) && (nsPfx.length() > 0)) {
              hasCswPfx = true;
              cswPfx = nsPfx;
            }
          }
          nsUri = Val.escapeXml(nsUri);
          if ((nsPfx == null) || (nsPfx.length() == 0)) {
            nsBuffer.append(" xmlns=\"").append(nsUri).append("\"");
          } else {
            nsBuffer.append(" xmlns:").append(nsPfx).append("=\"").append(nsUri).append("\"");
          }
        }
      }
    }   
    
    // use ogc as the default namespace if no namespace parameter was supplied
    if (nsBuffer.length() == 0) {
      nsBuffer.append(" xmlns=\"http://www.opengis.net/ogc\"");
    }
    
    // build the constraint XML
    StringBuilder sbXml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    if (hasCswUri && hasCswPfx) {
      cswPfx = cswPfx+":";
    } else if (hasCswUri) {
      cswPfx = "";
    } else {
      cswPfx = "csw:";
      nsBuffer.append(" xmlns:csw=\"http://www.opengis.net/cat/csw/2.0.2\"");
    }
    sbXml.append("\r\n<").append(cswPfx).append("Constraint");
    if (nsBuffer.length() > 0) {
      sbXml.append(" ").append(nsBuffer);
    }
    sbXml.append(">");
    sbXml.append("\r\n").append(constraintFilter);
    sbXml.append("\r\n</").append(cswPfx).append("Constraint>");
    
    // make the dom, find the ogc:Filter node
    try {
      Document dom = DomUtil.makeDomFromString(sbXml.toString(),true);
      CswNamespaces ns = new CswNamespaces();
      XPath xpath = XPathFactory.newInstance().newXPath();
      xpath.setNamespaceContext(ns.makeNamespaceContext());
      
      Node ndFilter = null;
      Node ndConstraint = (Node)xpath.evaluate("csw:Constraint",dom,XPathConstants.NODE);
      if (ndConstraint != null) {
        ndFilter = (Node)xpath.evaluate("ogc:Filter",ndConstraint,XPathConstants.NODE);;
      }
      if (ndFilter == null) {
        String msg = "The supplied constraint was not a valid ogc:Filter.";
        throw new OwsException(OwsException.OWSCODE_NoApplicableCode,"constraint",msg);
      } else {
        return ndFilter;
      }
      
    } catch (SAXException e) {
      String msg = "The supplied namespace/constraint pairs were not well-formed xml: ";
      msg += " "+e.toString();
      throw new OwsException(OwsException.OWSCODE_NoApplicableCode,"constraint",msg);
    }
    
  }
  
  /**
   * Builds an ogc:SortBy node from HTTP GET parameters.
   * @param sortBy the sortBy parameter values
   * @throws Exception if a processing exception occurs
   */
  protected Node buildSortByNode(String[] sortBy) throws Exception {
    
    // parse sort by parameters
    // pattern: sortby=property1:A,property2:D...
    
    if (sortBy != null) {
      StringBuilder sbXml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      sbXml.append("\r\n<ogc:SortBy xmlns:ogc=\"http://www.opengis.net/ogc\">");
      boolean hadProperty = false;
      for (String param: sortBy) {
        param = Val.chkStr(param);
        String name = null;
        String dir = null;
        if (param.toLowerCase().endsWith(":a")) {
          name = Val.chkStr(param.substring(0,param.length() - 2));
          dir= "ASC";
        } else if (param.toLowerCase().endsWith(":d")) {
          name = Val.chkStr(param.substring(0,param.length() - 2));
          dir = "DESC";
        } else {
          name = Val.chkStr(param);
        }
        if ((name == null) || (name.length() == 0)) {
          // we'll ignore this condition without an exception
        } else {
          hadProperty = true;
          sbXml.append("\r\n<ogc:SortProperty>");
          sbXml.append("\r\n<ogc:PropertyName>").append(Val.escapeXml(name)).append("</ogc:PropertyName>");
          if (dir != null) {
            sbXml.append("\r\n<ogc:SortOrder>").append(Val.escapeXml(dir)).append("</ogc:SortOrder>");
          }
          sbXml.append("\r\n</ogc:SortProperty>");
        }
      }
      sbXml.append("\r\n</ogc:SortBy>");
      if (hadProperty) {
        Document dom = DomUtil.makeDomFromString(sbXml.toString(),true);
        NodeList nl = dom.getChildNodes(); 
        for (int i=0; i<nl.getLength(); i++) {
          if (nl.item(i).getNodeType() == Node.ELEMENT_NODE){ 
            return nl.item(i);
          }
        }
      }
    }
    return null;
  }
  
  /**
   * Executes a parsed operation request.
   * @param context the operation context
   * @throws Exception if a processing exception occurs
   */
  public void execute(OperationContext context) throws Exception {
    
    // initialize
    LOGGER.finer("Executing csw:GetRecords request...");
    IProviderFactory factory = context.getProviderFactory();
    
    // evaluate the query
    IQueryEvaluator evaluator = factory.makeQueryEvaluator(context);
    if (evaluator == null) {
      String msg = "IProviderFactory.makeQueryEvaluator: instantiation failed.";
      LOGGER.log(Level.SEVERE,msg);
      throw new OwsException(msg);
    } else {
      evaluator.evaluateQuery(context);
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
  
  /**
   * Handles a URL based request (HTTP GET).
   * @param context the operation context
   * @param request the HTTP request
   * @throws Exception if a processing exception occurs
   */
  public void handleGet(OperationContext context, HttpServletRequest request) 
    throws Exception {
    
    // initialize
    LOGGER.finer("Handling csw:GetRecords request URL...");
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    IProviderFactory factory = context.getProviderFactory();
    
    CswNamespaces ns = new CswNamespaces();
    XPath xpath = XPathFactory.newInstance().newXPath();
    xpath.setNamespaceContext(ns.makeNamespaceContext());
    
    // service and version are parsed by the parent RequestHandler
    
    // TODO typeNames requestId distributedSearch hopCount responseHandler
    // TODO resultype validate is not applicable for a GET request?
    
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
    
    // start and max records
    parsed = pHelper.getParameterValues(request,"startPosition");
    if ((parsed != null) && (parsed.length) > 0) {
      qOptions.setStartRecord(Math.max(Val.chkInt(parsed[0],1),1));
    }
    parsed = pHelper.getParameterValues(request,"maxRecords");
    if ((parsed != null) && (parsed.length) > 0) {
      qOptions.setMaxRecords(Val.chkInt(parsed[0],10));
    }
    
    // result type
    locator = "resultType";
    parsed = pHelper.getParameterValues(request,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_ResultType);
    qOptions.setResultType(vHelper.validateValue(supported,locator,parsed,false));
    if (qOptions.getResultType() == null) {
      qOptions.setResultType(CswConstants.ResultType_Hits);
    }
    
    // query type names
    locator = "typeNames";
    parsed = pHelper.getParameterValues(request,locator);
    qOptions.setQueryTypeNames(vHelper.validateValues(locator,parsed,false));
    
    // response element set type
    locator = "ElementSetName";
    parsed = pHelper.getParameterValues(request,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_ElementSetType);
    qOptions.setElementSetType(vHelper.validateValue(supported,locator,parsed,false));
    
    // response element names
    if (qOptions.getElementSetType() == null) {
      // TODO supported ElementNames this for GetRecordById as well?
      locator = "ElementName";
      parsed = pHelper.getParameterValues(request,locator,",");
      supported = svcProps.getSupportedValues(CswConstants.Parameter_ElementName);
      qOptions.setElementNames(vHelper.validateValues(supported,locator,parsed,false));
    }
    
    // constraint language
    locator = "constraintLanguage";
    parsed = pHelper.getParameterValues(request,locator);
    supported = new SupportedValues("CQL_TEXT,FILTER",",");
    String constraintLanguage = vHelper.validateValue(supported,locator,parsed,false);
    
    // constraint version
    locator = "constraint_language_version";
    parsed = pHelper.getParameterValues(request,locator);
    String constraintVersion = vHelper.validateValue(locator,parsed,false);
    qOptions.setQueryConstraintVersion(constraintVersion);
    
    // constraint text
    locator = "constraint";
    parsed = pHelper.getParameterValues(request,locator);
    String constraint = vHelper.validateValue(locator,parsed,false);
    
    // csw:CqlText
    if ((constraintLanguage != null) && constraintLanguage.equalsIgnoreCase("CQL_TEXT")) {
      String cql = Val.chkStr(constraint);
      qOptions.setQueryConstraintCql(cql);
      ICqlParser parser = factory.makeCqlParser(context,constraintVersion);
      if (parser == null) {
        String msg = "IProviderFactory.makeCqlParser: instantiation failed.";
        throw new OwsException(OwsException.OWSCODE_NoApplicableCode,locator,msg);
      } else {
        parser.parseCql(context,cql);
      }
    } 
    
    // ogc:Filter
    if ((constraintLanguage == null) || constraintLanguage.equalsIgnoreCase("FILTER")) {
      Node ndFilter = null;
      IFilterParser parser = factory.makeFilterParser(context,constraintVersion);
      if (parser == null) {
        String msg = "IProviderFactory.makeFilterParser: instantiation failed.";
        throw new OwsException(OwsException.OWSCODE_NoApplicableCode,locator,msg);
      }
      String constraintFilter = Val.chkStr(constraint);
      if (constraintFilter.length() > 0) {
        String[] namespace = pHelper.getParameterValues(request,"namespace",",");
        ndFilter = this.buildFilterNode(namespace,constraintFilter);
        parser.parseFilter(context,ndFilter,xpath);
      }
    }
    
    // ogc:SortBy
    locator = "sortBy";
    String[] sortBy = pHelper.getParameterValues(request,"sortBy",",");
    if (sortBy != null) {
      Node ndSortBy = this.buildSortByNode(sortBy);
      if (ndSortBy != null) {
        ISortByParser parser = factory.makeSortByParser(context);
        if (parser == null) {
          String msg = "IProviderFactory.makeSortByParser: instantiation failed.";
          throw new OwsException(OwsException.OWSCODE_NoApplicableCode,locator,msg);
        } else {
          parser.parseSortBy(context,ndSortBy,xpath);
        }
      }
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
    LOGGER.finer("Handling csw:GetRecords request XML...");
    QueryOptions qOptions = context.getRequestOptions().getQueryOptions();
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    IProviderFactory factory = context.getProviderFactory();
    
    // service and version are parsed by the parent RequestHandler
    
    // TODO requestId
        
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
    
    // start and max records
    qOptions.setStartRecord(Math.max(Val.chkInt(xpath.evaluate("@startPosition",root),1),1));
    qOptions.setMaxRecords(Val.chkInt(xpath.evaluate("@maxRecords",root),10));  
    
    // result type
    locator = "@resultType";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_ResultType);
    qOptions.setResultType(vHelper.validateValue(supported,locator,parsed,false));
    if (qOptions.getResultType() == null) {
      qOptions.setResultType(CswConstants.ResultType_Hits);
    }
    
    // find the query node
    locator = "csw:Query";
    Node ndQuery = (Node)xpath.evaluate(locator,root,XPathConstants.NODE);
    if (ndQuery != null) {
    
      // query type names
      locator = "csw:Query/@typeNames";
      parsed = pHelper.getParameterValues(root,xpath,"@typeNames");
      qOptions.setQueryTypeNames(vHelper.validateValues(locator,parsed,false));
              
      // response element set type
      locator = "csw:ElementSetName";
      parsed = pHelper.getParameterValues(ndQuery,xpath,locator);
      supported = svcProps.getSupportedValues(CswConstants.Parameter_ElementSetType);
      qOptions.setElementSetType(vHelper.validateValue(supported,locator,parsed,false));
      
      // response element set type names
      String elementSetType = qOptions.getElementSetType();
      if (elementSetType != null) {
        locator = "csw:ElementSetName/@typeNames";
        parsed = pHelper.getParameterValues(ndQuery,xpath,locator);
        qOptions.setElementSetTypeNames(vHelper.validateValues(locator,parsed,false)); 
      }
      
      // response element names
      if (elementSetType == null) {
        // TODO supported ElementNames
        locator = "csw:ElementName";
        parsed = pHelper.getParameterValues(ndQuery,xpath,locator);
        supported = svcProps.getSupportedValues(CswConstants.Parameter_ElementName);
        qOptions.setElementNames(vHelper.validateValues(supported,locator,parsed,false));
      }
      
      // find the constraint node
      Node ndConstraint = (Node)xpath.evaluate("csw:Constraint",ndQuery,XPathConstants.NODE);
      if (ndConstraint != null) {
        
        // constraint version
        String constraintVersion = xpath.evaluate("@version",ndConstraint);
        qOptions.setQueryConstraintVersion(constraintVersion);
    
        // csw:CqlText
        locator = "csw:CqlText";
        Node ndCql = (Node)xpath.evaluate(locator,ndConstraint,XPathConstants.NODE);
        if (ndCql != null) {
          String cql = Val.chkStr(ndCql.getTextContent());
          qOptions.setQueryConstraintCql(cql);
          ICqlParser parser = factory.makeCqlParser(context,constraintVersion);
          if (parser == null) {
            String msg = "IProviderFactory.makeCqlParser: instantiation failed.";
            throw new OwsException(OwsException.OWSCODE_NoApplicableCode,locator,msg);
          } else {
            parser.parseCql(context,cql);
          }
        } else {
        
          // ogc:Filter
          locator = "ogc:Filter";
          Node ndFilter = (Node)xpath.evaluate(locator,ndConstraint,XPathConstants.NODE);
          if (ndFilter != null) {
            IFilterParser parser = factory.makeFilterParser(context,constraintVersion);
            if (parser == null) {
              String msg = "IProviderFactory.makeFilterParser: instantiation failed.";
              throw new OwsException(OwsException.OWSCODE_NoApplicableCode,locator,msg);
            } else {
              parser.parseFilter(context,ndFilter,xpath);
            }
          } else {
            String msg = "An OGC filter for the CSW constraint is required.";
            throw new OwsException(OwsException.OWSCODE_NoApplicableCode,locator,msg);
          }
        }
      }
      
      // ogc:SortBy
      locator = "ogc:SortBy";
      Node ndSortBy = (Node)xpath.evaluate(locator,ndQuery,XPathConstants.NODE);
      if (ndSortBy != null) {
        ISortByParser parser = factory.makeSortByParser(context);
        if (parser == null) {
          String msg = "IProviderFactory.makeSortByParser: instantiation failed.";
          throw new OwsException(OwsException.OWSCODE_NoApplicableCode,locator,msg);
        } else {
          parser.parseSortBy(context,ndSortBy,xpath);
        }
      }
    }
        
    // execute the request
    this.execute(context);
  }
    
}
