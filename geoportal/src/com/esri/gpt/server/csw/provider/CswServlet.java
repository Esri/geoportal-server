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
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.csw.provider.components.IOriginalXmlProvider;
import com.esri.gpt.server.csw.provider.components.IProviderFactory;
import com.esri.gpt.server.csw.provider.components.OperationContext;
import com.esri.gpt.server.csw.provider.components.OperationResponse;
import com.esri.gpt.server.csw.provider.components.OwsException;
import com.esri.gpt.server.csw.provider.components.RequestHandler;
import com.esri.gpt.server.csw.provider.local.ProviderFactory;

import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CSW provider servlet.
 */
@SuppressWarnings("serial")
public class CswServlet extends BaseServlet {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(CswServlet.class.getName());
  
  /** instance variables ====================================================== */
  private boolean allowTransactions = true;
  private String  cswSubContextPath;
  private String  resourceFilePrefix;
      
  /** methods ================================================================= */
  
  /**
   * Initializes the  servlet.
   * <br/>Reads the "cswSubContextPath" and "resourceFilePrefix".
   * init params from the servlet configuration.
   * @param config the servlet configuration
   * @throws ServletException if an initialization exception occurs
   */
  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    this.cswSubContextPath = config.getInitParameter("cswSubContextPath");
    this.resourceFilePrefix = config.getInitParameter("resourceFilePrefix");
    String s = Val.chkStr(config.getInitParameter("allowTransactions"));
    this.allowTransactions = !s.equalsIgnoreCase("false");
  }
     
  /**
   * Executes a request.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  protected void execute(HttpServletRequest request, 
                         HttpServletResponse response,
                         RequestContext context)
    throws Exception {    
        
    // check for a request to return a full xml
    String sGetXmlUuid = "";
    String sParamsLC = Val.chkStr(request.getQueryString());
    if (sParamsLC.indexOf("getxml=") != -1) {
      sGetXmlUuid = Val.chkStr(request.getParameter("getxml"));
      
      // try to decode uuids that have been mistakenly double encoded by an external client
      if (sGetXmlUuid.startsWith("%7B")) {
        try {
          String s = java.net.URLDecoder.decode(sGetXmlUuid,"UTF-8");
          sGetXmlUuid = Val.chkStr(s);
        } catch (UnsupportedEncodingException ue) {}
      }
    }
    
    // return the full xml if requested
    if (sGetXmlUuid.length() > 0) {
      LOGGER.finer("Retrieving document: "+sGetXmlUuid);
      String xml = "";
     try {
        xml = readFullXml(request,response,context,sGetXmlUuid);
      } catch (NotAuthorizedException nae) {
        throw nae;
      } catch (Throwable t) {
        LOGGER.warning("\nError retrieving document: "+sGetXmlUuid+"\n "+t.toString());
      }
      this.writeXmlResponse(response,Val.chkStr(xml));
      
    // execute a normal CSW request
    } else {
      executeCSW(request,response,context);
    }
  }
      
  /**
   * Executes a CSW request.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  protected void executeCSW(HttpServletRequest request, 
                            HttpServletResponse response,
                            RequestContext context)
    throws Exception {
    
    // process the request
    LOGGER.fine("Executing CSW provider request....");
    String cswResponse = "";
    String mimeType = "application/xml";
    RequestHandler handler = null;
    OperationResponse opResponse = null;
    try {
      String cswRequest = readInputCharacters(request);
      LOGGER.finer("cswRequest:\n"+cswRequest);
      handler = this.makeRequestHandler(request,response,context);
      if (cswRequest.length() > 0) {
        opResponse = handler.handleXML(cswRequest);
      } else {
        opResponse = handler.handleGet(request);
      }
      if (opResponse != null) {
        cswResponse = Val.chkStr(opResponse.getResponseXml());
        String fmt = Val.chkStr(opResponse.getOutputFormat());
        if (fmt.equalsIgnoreCase("text/xml")) {
          mimeType = "text/xml";
        }
      }
 
    } catch (Exception e) {
      OperationContext opContext = null;
      if (handler != null) {
        opContext = handler.getOperationContext();
      }
      cswResponse = handleException(opContext,e);
    }
    
    // write the response
    LOGGER.finer("cswResponse:\n"+cswResponse);
    if (cswResponse.length() > 0) {
      writeCharacterResponse(response,cswResponse,"UTF-8",mimeType+"; charset=UTF-8");
    }
  }
  
  /**
   * Creation an ExceptionReport response when an exception is encountered.
   * @param e the exception
   * @return the exception report string
   * @throws throws Exception if an authorization related exception occurs
   * @deprecated replaced by {@link #handleException(OperationContext,Exception)}
   */
  protected String handleException(Exception e) throws Exception {
    return this.handleException(null,e);
  }
  
  /**
   * Creation an ExceptionReport response when an exception is encountered.
   * @param context the operation context
   * @param e the exception
   * @return the exception report string
   * @throws throws Exception if an authorization related exception occurs
   */
  protected String handleException(OperationContext context, Exception e) throws Exception {
    if (e instanceof NotAuthorizedException) {
      throw e;
    } else if (e instanceof OwsException) {
      OwsException ows = (OwsException)e;
      LOGGER.finer("Invalid CSW request: "+e.getMessage());
      return ows.getReport(context);
    } else {
      OwsException ows = new OwsException(e);
      LOGGER.log(Level.WARNING,e.toString(),e);
      return ows.getReport(context);
    }
  }
    
  /**
   * Makes a handler for the CSW request.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @return the request handler
   */
  protected RequestHandler makeRequestHandler(HttpServletRequest request,
                                              HttpServletResponse response,
                                              RequestContext context) {
    IProviderFactory factory = new ProviderFactory();
    RequestHandler handler = factory.makeRequestHandler(
        request,context,this.cswSubContextPath,this.resourceFilePrefix);
    if (handler != null) {
      handler.getOperationContext().getServiceProperties().setAllowTransactions(this.allowTransactions);
    }
    return handler;
  }
  
  /**
   * Reads the full XML associated with a document UUID.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @param id the document id
   * @return the document XML
   * @throws Exception if an exception occurs
   */
  protected String readFullXml(HttpServletRequest request,
                               HttpServletResponse response,
                               RequestContext context,
                               String id) 
    throws Exception {
    RequestHandler handler = this.makeRequestHandler(request,response,context);
    OperationContext opContext = handler.getOperationContext();
    IProviderFactory factory = opContext.getProviderFactory();
    IOriginalXmlProvider oxp = factory.makeOriginalXmlProvider(opContext);
    if (oxp == null) {
      String msg = "The getxml parameter is not supported.";
      String locator = "getxml";
      throw new OwsException(OwsException.OWSCODE_InvalidParameterValue,locator,msg);
    } else {
      String xml = oxp.provideOriginalXml(opContext,id);
      return xml;
    }
  }
}
