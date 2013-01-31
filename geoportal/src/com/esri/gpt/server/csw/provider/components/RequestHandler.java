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
package com.esri.gpt.server.csw.provider.components;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.credentials.UsernameCredential;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;
import com.esri.gpt.framework.xml.XmlIoUtil;
import com.esri.gpt.sdisuite.IntegrationContext;
import com.esri.gpt.sdisuite.IntegrationContextFactory;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Request handler.
 */
public class RequestHandler {

  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(RequestHandler.class.getName());
  
  /** instance variables ====================================================== */
  private OperationContext operationContext;
  private boolean          wasSoap = false;
   
  /** constructors ============================================================ */
  
  /** Default constructor */
  public RequestHandler() {}
  
  /** properties ============================================================== */
  
  /**
   * Gets the operation context.
   * @return the operation context
   */
  public OperationContext getOperationContext() {
    return this.operationContext;
  }
  /**
   * Sets the operation context.
   * @param context the operation context
   */
  public void setOperationContext(OperationContext context) {
    this.operationContext = context;
  }
  
  /**
   * Gets the flag indicating a SOAP based request.
   * @return <code>true</code> is this was a SOAP based request
   */
  public boolean getWasSoap() {
    return this.wasSoap;
  }
  /**
   * Sets the flag indicating a SOAP based request.
   * @param wasSoap <code>true</code> is this was a SOAP based request
   */
  public void setWasSoap(boolean wasSoap) {
    this.wasSoap = wasSoap;
  }
            
  /** methods ================================================================= */
  
  /**
   * Handles a URL based request (HTTP GET).
   * @param request the HTTP request
   * @throws Exception if a processing exception occurs
   */
  public OperationResponse handleGet(HttpServletRequest request) 
    throws Exception {
        
    // initialize
    LOGGER.finer("Handling CSW request URL...");
    OperationContext context = this.getOperationContext();
    IProviderFactory factory = context.getProviderFactory();
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    
    // determine the operation name and provider
    String opName = null;
    IOperationProvider opProvider = null;
    locator = "request";
    parsed = pHelper.getParameterValues(request,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_OperationName);
    opName = vHelper.validateValue(supported,locator,parsed,true);
    context.setOperationName(opName);
    if (opName != null) {
      opProvider = factory.makeOperationProvider(context,opName);
    }
    
    // ensure a valid operation provider
    if (opProvider == null) {
      if ((opName == null) || (opName.length() == 0)) {
        throw new OwsException(OwsException.OWSCODE_MissingParameterValue,
            "request","The request parameter was missing.");
      } else {
        throw new OwsException(OwsException.OWSCODE_OperationNotSupported,
            "request","Unsupported operation: "+opName);
      }
    }
    
    // parse the service and version
    this.parseServiceAndVersion(context,request);
    
    // parse and execute the operation
    opProvider.handleGet(context,request);
    return context.getOperationResponse();
   
  }
  
  /**
   * Handles an XML based request (normally HTTP POST).
   * @param xml the XML
   * @throws Exception if a processing exception occurs
   */
  public OperationResponse handleXML(String xml) throws Exception {
    
    // initialize
    LOGGER.finer("Handling CSW request XML...");
    OperationContext context = this.getOperationContext();
    IProviderFactory factory = context.getProviderFactory();
    Node root = null;
    String nsSoapUri = null;
    String nsSoapPfx = null;
    
    try {
      
      // load the dom
      context.getRequestOptions().setRequestXml(xml);
      Document dom = this.loadDom(xml);
      context.getRequestOptions().setRequestDom(dom);
      
      // make an XPath for the CSW name space context
      CswNamespaces ns = new CswNamespaces();
      XPath xpath = XPathFactory.newInstance().newXPath();
      xpath.setNamespaceContext(ns.makeNamespaceContext());
      
      // check for a soap request, if so determine the root CSW operation node
      /*
        <?xml version="1.0"?>
        <soap:Envelope xmlns:soap="http://www.w3.org/2001/12/soap-envelope"
              soap:encodingStyle="http://www.w3.org/2001/12/soap-encoding">
          <soap:Body xmlns:m="http://www.example.org/stock">
         
        <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
          <soap:Body>
       */
      String expr = "/SOAP-ENV:Envelope | /soap:Envelope | /soap_2003_05:Envelope";
      Node ndSoapEnv = (Node)xpath.evaluate(expr,dom,XPathConstants.NODE);
      if (ndSoapEnv != null) {
        this.setWasSoap(true);
        nsSoapUri = ndSoapEnv.getNamespaceURI();
        nsSoapPfx = ndSoapEnv.lookupPrefix(nsSoapUri);
        expr = "//SOAP-ENV:Body | //soap:Body | //soap_2003_05:Body";
        Node ndSoapBody = (Node)xpath.evaluate(expr,ndSoapEnv,XPathConstants.NODE);
        if (ndSoapBody == null) {
          throw new OwsException(OwsException.OWSCODE_MissingParameterValue,
              "Envelope","The SOAP body was missing.");
        } else {
          NodeList nl = ndSoapBody.getChildNodes(); 
          for (int i=0; i<nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.ELEMENT_NODE){ 
              root = nl.item(i);
              break;
            }
          }
          if (root == null) {
            throw new OwsException(OwsException.OWSCODE_MissingParameterValue,
                "Body","No CSW operation was located within the soap body.");
          }
        }
        
        // check for an sdi-suite SAML token
        expr = "//tcExt:samlTicket";
        Node ndSaml = (Node)xpath.evaluate(expr,ndSoapEnv,XPathConstants.NODE);
        if (ndSaml != null) {
          String samlToken = ndSaml.getTextContent();
          if (samlToken != null) {
            IntegrationContextFactory icf = new IntegrationContextFactory();
            if (icf.isIntegrationEnabled()) {
              IntegrationContext ic = icf.newIntegrationContext();
              if (ic != null) {
                String samlUsername = ic.getUsernameFromSAMLToken(samlToken);
                RequestContext rc = context.getRequestContext();
                if ((rc != null) && (samlUsername != null)) {
                  User user = rc.getUser();
                  user.reset();
                  user.setCredentials(new UsernameCredential(samlUsername));
                  rc.newIdentityAdapter().authenticate(user);
                  context.getRequestOptions().getTransactionOptions().setPublicationMethod("seditor");

                  expr = "//tcExt:tcSecurity/tcExt:tcSecuredAction[tcExt:status='draft']";
                  Node ndTmp = (Node)xpath.evaluate(expr,ndSoapEnv,XPathConstants.NODE);
                  if (ndTmp != null) {
                    context.getRequestOptions().getTransactionOptions().setApprovalStatus("draft");
                  }
                }
              }
            }
          }
        }
      }
      
      // if this wasn't a SOAP request, determine the root node
      if (!this.getWasSoap()) {
        NodeList nl = dom.getChildNodes(); 
        for (int i=0; i<nl.getLength(); i++) {
          if (nl.item(i).getNodeType() == Node.ELEMENT_NODE){ 
            root = nl.item(i);
            break;
          }
        }
      }
          
      // determine the operation name and provider
      String opName = null;
      IOperationProvider opProvider = null;
      String namespace = root.getNamespaceURI();
      opName = root.getLocalName();
      if (CswNamespaces.URI_CSW.equals(namespace)) {
        context.setOperationName(opName);
        if (opName != null) {
          opProvider = factory.makeOperationProvider(context,opName);
        }
      }
      
      // ensure a valid operation provider
      if (opProvider == null) {
        throw new OwsException(OwsException.OWSCODE_OperationNotSupported,
              "root-node","Unsupported operation: "+opName);
      }
      
      // parse the service and version
      this.parseServiceAndVersion(context,root,xpath);
      
      // parse and execute the operation
      opProvider.handleXML(context,root,xpath);
      
    } catch (NotAuthorizedException e) {
      throw e;
    } catch (Exception e) {
      if (this.getWasSoap()) {
        this.generateSoapResponse(context,nsSoapUri,nsSoapPfx,e);
        return context.getOperationResponse();
      } else {
        throw e;
      }
    }
    
    // wrap the SOAP response if required
    if (this.getWasSoap()) {
      this.generateSoapResponse(context,nsSoapUri,nsSoapPfx,null);
    }
    return context.getOperationResponse();
  }
  
  /**
   * Wraps the response within a SOAp envelope.
   * <br/>If the exception argument is null, the operation response is wrapped.
   * <br/>If the exception argument is not null, an OWS exception report is wrapped.
   * <br/>The OperationResponse.responseXml is reset to the SOAP response string.
   * @param context the operation context
   * @param nsSoapUri the SOAP namespace URI
   * @param nsSoapPfx the SOAP namespace prefix
   * @param exception an exception condition that should be wrapped as an OWS exception report
   * @throws Exception Exception if a processing exception occurs
   */
  protected void generateSoapResponse(OperationContext context, 
                                      String nsSoapUri, 
                                      String nsSoapPfx,
                                      Exception exception) 
    throws Exception {
    
    // make the SOAP document
    if (Val.chkStr(nsSoapPfx).length() == 0) {
      nsSoapPfx = "soap";
    }
    Document domSoap = DomUtil.newDocument();
    Node ndSoapEnv = domSoap.createElementNS(nsSoapUri,nsSoapPfx+":Envelope");
    Node ndSoapBody = domSoap.createElementNS(nsSoapUri,nsSoapPfx+":Body");
    domSoap.appendChild(ndSoapEnv);
    ndSoapEnv.appendChild(ndSoapBody);
    
    // check for an exception condition
    OperationResponse opResponse = context.getOperationResponse();
    if (exception != null) {
      if (exception instanceof OwsException) {
        OwsException ows = (OwsException)exception;
        LOGGER.finer("Invalid CSW request: "+exception.getMessage());
        opResponse.setResponseXml(ows.getReport(context));
      } else {
        OwsException ows = new OwsException(exception);
        LOGGER.log(Level.WARNING,exception.toString(),exception);
        opResponse.setResponseXml(ows.getReport(context));
      }
    }
    
    try {

      // wrap the response within the SOAP body
      Document domCsw = DomUtil.makeDomFromString(opResponse.getResponseXml(),true);
      NodeList nl = domCsw.getChildNodes(); 
      for (int i=0; i<nl.getLength(); i++) {
        if (nl.item(i).getNodeType() == Node.ELEMENT_NODE){ 
          Node cswRoot = nl.item(i);
          Node ndImported = domSoap.importNode(cswRoot,true);
          ndSoapBody.appendChild(ndImported);
          opResponse.setResponseXml(XmlIoUtil.domToString(domSoap));
          break;
        }
      }
    
    // throw a SOAP fault
    } catch (Exception e) {
      Node ndFault = domSoap.createElementNS(nsSoapUri,nsSoapPfx+":Fault"); 
      Node ndCode = domSoap.createElement("faultcode");
      Node ndReason = domSoap.createElement("faultstring");
      Node ndDetail = domSoap.createElementNS(nsSoapUri,nsSoapPfx+":Detail");
      Node ndMessage = domSoap.createElement("message");
      ndFault.appendChild(ndCode);
      ndCode.setTextContent("Server");
      ndFault.appendChild(ndReason);
      ndReason.setTextContent("An error occurred while generating the SOAP body.");
      ndFault.appendChild(ndDetail);
      ndDetail.appendChild(ndMessage);
      ndMessage.setTextContent(e.toString());
      ndSoapBody.appendChild(ndFault);
      opResponse.setResponseXml(XmlIoUtil.domToString(domSoap));
    }
  }
  
  /**
   * Loads an XML string into an XML Document.
   * @param xml the document XML string
   * @return the document
   * @throws OwsException if the document fails to load
   */
  protected Document loadDom(String xml) throws OwsException {
    String sErrorMsg = "Unable to parse incoming XML document.";
    try {
      xml = Val.chkStr(xml);
      if (xml.length() == 0) {
        sErrorMsg = "The incoming XML document was empty."; 
      }
      return DomUtil.makeDomFromString(xml,true);
    } catch (ParserConfigurationException e) {
      throw new OwsException(sErrorMsg,e);
    } catch (SAXException e) {
      throw new OwsException(sErrorMsg,e);
    } catch (IOException e) {
      throw new OwsException(sErrorMsg,e);
    }
  }
  
  /**
   * Parses the service name and version for a URL based request (HTTP GET).
   * @param context the operation context
   * @param request the HTTP request
   * @throws OwsException if validation fails
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  public void parseServiceAndVersion(OperationContext context, HttpServletRequest request) 
    throws OwsException {
    
    // initialize
    LOGGER.finer("Parsing request URL for service and version...");
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    
    // service name
    locator = "service";
    parsed = pHelper.getParameterValues(request,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_Service);
    String service = vHelper.validateValue(supported,locator,parsed,true);
    svcProps.setServiceName(service);
    
    // service version
    locator = "acceptVersions";
    parsed = pHelper.getParameterValues(request,locator,",");
    supported = svcProps.getSupportedValues(CswConstants.Parameter_Version);
    String version = Val.chkStr(vHelper.negotiateValue(supported,locator,parsed,false));
    if (version.length() > 0) {
      svcProps.setServiceVersion(version);
    } else {
      locator = "version";
      parsed = pHelper.getParameterValues(request,locator);
      supported = svcProps.getSupportedValues(CswConstants.Parameter_Version);
      version = vHelper.validateValue(supported,locator,parsed,false);
      svcProps.setServiceVersion(version);
    }
  }
  
  /**
   * Parses the service name and version an XML based request (normally HTTP POST).
   * @param context the operation context
   * @param root the root node
   * @param xpath an XPath to enable queries (properly configured with name spaces)
   * @throws OwsException if validation fails
   * @throws XPathExpressionException if an XPath related exception occurs
   */
  public void parseServiceAndVersion(OperationContext context, Node root, XPath xpath) 
    throws OwsException, XPathExpressionException {
    
    // initialize
    LOGGER.finer("Parsing request XML for service and version...");
    ServiceProperties svcProps = context.getServiceProperties();
    ParseHelper pHelper = new ParseHelper();
    ValidationHelper vHelper = new ValidationHelper();
    String locator;
    String[] parsed;
    ISupportedValues supported;
    
    // service name
    locator = "@service";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_Service);
    String service = vHelper.validateValue(supported,locator,parsed,true);
    svcProps.setServiceName(service);
    
    // service version
    locator = "ows:AcceptVersions/ows:Version";
    parsed = pHelper.getParameterValues(root,xpath,locator);
    supported = svcProps.getSupportedValues(CswConstants.Parameter_Version);
    String version = Val.chkStr(vHelper.negotiateValue(supported,locator,parsed,false));
    if (version.length() > 0) {
      svcProps.setServiceVersion(version);
    } else {
      locator = "@version";
      parsed = pHelper.getParameterValues(root,xpath,locator);
      supported = svcProps.getSupportedValues(CswConstants.Parameter_Version);
      version = vHelper.validateValue(supported,locator,parsed,false);
      svcProps.setServiceVersion(version);
    }
  } 
  
}
