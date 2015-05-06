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
package com.esri.gpt.server.assertion.handler;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.AsnFactory;
import com.esri.gpt.server.assertion.components.AsnConstants;
import com.esri.gpt.server.assertion.components.AsnContext;
import com.esri.gpt.server.assertion.components.AsnRequestOptions;
import com.esri.gpt.server.assertion.components.AsnResponse;
import com.esri.gpt.server.assertion.components.AsnSupportedValues;
import com.esri.gpt.server.assertion.components.AsnValue;
import com.esri.gpt.server.assertion.components.AsnValueFilter;
import com.esri.gpt.server.assertion.components.AsnValueType;
import com.esri.gpt.server.assertion.exception.AsnInsufficientPrivilegeException;
import com.esri.gpt.server.assertion.exception.AsnInvalidOperationException;
import com.esri.gpt.server.csw.components.ParseHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Handles inbound assertion requests.
 */
public class AsnRequestHandler {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(AsnRequestHandler.class.getName());
  
  /** instance variables ====================================================== */
  private AsnContext assertionContext;
  
  /** constructors ============================================================ */
  
  /** Default constructor */
  public AsnRequestHandler() {}
    
  /** properties ============================================================== */
  
  /**
   * Gets the operation context.
   * @return the operation context
   */
  public AsnContext getAssertionContext() {
    return this.assertionContext;
  }
  /**
   * Sets the operation context.
   * @param context the operation context
   */
  public void setAssertionContext(AsnContext context) {
    this.assertionContext = context;
  }
  
  /** methods ================================================================= */
  
  /**
   * Handles a URL based request (HTTP GET).
   * @param request the HTTP request
   * @param response the HTTP response
   * @return the operation response
   * @throws Exception if a processing exception occurs
   */
  public AsnResponse handleRequest(HttpServletRequest request, HttpServletResponse response) 
    throws Exception {
    
    // initialize
    AsnContext context = this.getAssertionContext();
    AsnFactory factory = context.getAssertionFactory();
    AsnRequestOptions rOptions = context.getRequestOptions();
    AsnResponse opResponse = context.getOperationResponse();
    ParseHelper pHelper = new ParseHelper();
    String[] parsed;
    
    // parse the HTTP request
    if (request != null) {
    
      // set the IP address
      rOptions.setIPAddress(request.getRemoteAddr());
      
      // parse an operations request
      boolean isOperationsRequest = false;
      if (request.getRequestURI() != null) {
        isOperationsRequest = request.getRequestURI().toLowerCase().endsWith("/operations");
      }
      if (isOperationsRequest) {
        rOptions.setSubject(AsnConstants.APP_URN_PREFIX);
        rOptions.setPredicate(AsnConstants.APP_URN_PREFIX+":assertion:operations");
        
      // parse a non-operations request
      } else {
  
        // subject
        parsed = pHelper.getParameterValues(request,"s");
        if ((parsed == null) || (parsed.length) == 0) {
          parsed = pHelper.getParameterValues(request,"subject");
        }
        if ((parsed != null) && (parsed.length) > 0) {
          rOptions.setSubject(parsed[0]);
        }
        
        // predicate
        parsed = pHelper.getParameterValues(request,"p");
        if ((parsed == null) || (parsed.length) == 0) {
          parsed = pHelper.getParameterValues(request,"predicate");
        }
        if ((parsed != null) && (parsed.length) > 0) {
          rOptions.setPredicate(parsed[0]);
        }
        
        // value
        parsed = pHelper.getParameterValues(request,"v");
        if ((parsed == null) || (parsed.length) == 0) {
          parsed = pHelper.getParameterValues(request,"value");
        }
        if ((parsed != null) && (parsed.length) > 0) {
          rOptions.setValue(parsed[0]);
        }
        
        // output format
        parsed = pHelper.getParameterValues(request,"f");
        if ((parsed == null) || (parsed.length) == 0) {
          parsed = pHelper.getParameterValues(request,"format");
        }
        if ((parsed != null) && (parsed.length) > 0) {
          opResponse.setOutputFormat(parsed[0]);
        }
        
        // start and max records start=&max=
        parsed = pHelper.getParameterValues(request,"start");
        if ((parsed == null) || (parsed.length) == 0) {
          parsed = pHelper.getParameterValues(request,"startRecord");
          if ((parsed == null) || (parsed.length) == 0) {
            parsed = pHelper.getParameterValues(request,"startPosition");
          }
        }
        if ((parsed != null) && (parsed.length) > 0) {
          rOptions.setStartRecord(Math.max(Val.chkInt(parsed[0],1),1));
        }
        parsed = pHelper.getParameterValues(request,"max");
        if ((parsed == null) || (parsed.length) == 0) {
          parsed = pHelper.getParameterValues(request,"maxRecords");
        }
        if ((parsed != null) && (parsed.length) > 0) {
          rOptions.setMaxRecords(Val.chkInt(parsed[0],10));
        }
      }
    }
    
    try {
      
      // determine the handler
      AsnOperationHandler handler = factory.makeOperationHandler(context);
      
      // ensure a valid value if required
      AsnValue asnValue = context.getOperation().getValue();
      if (asnValue != null) {
        AsnValueType asnValueType = asnValue.getValueType();
        if (asnValueType != null) {
          String val = Val.chkStr(rOptions.getValue());
          int maxChars = asnValueType.getMaxCharacters();
          if (asnValueType.getRequired() && (val.length() == 0) && (request != null)) {
            val = Val.chkStr(this.readInputCharacters(request,maxChars));
            if (val.length() == 0) val = null;
            rOptions.setValue(val);
          }
          if ((maxChars >= 0) && (val != null) && (val.length() > maxChars)) {
            val = Val.chkStr(val.substring(0,maxChars));
          }
          if ((val != null) && (val.length() > 0)) {
            AsnValueFilter vFilter = asnValueType.makeValueFilter(context);
            if (vFilter != null) {
              val = Val.chkStr(vFilter.filter(val));
            }
          }
          AsnSupportedValues supported = asnValueType.getSupportedValues();
          if (supported != null) {
            val = supported.getSupportedValue(val);
            if (val != null) {
              asnValue.setTextValue(val);
            } else {
              String msg = "The supplied value is not supported - "+val;
              throw new AsnInvalidOperationException(msg);
            }
          } else {
            if ((val != null) && (val.length() == 0)) val = null;
            asnValue.setTextValue(val);
            if (asnValueType.getRequired() && (val == null)) {
              String msg = "A value is required.";
              throw new AsnInvalidOperationException(msg);
            } 
          }
        }
      }
      
      // ensure a message broker if required
      if (context.getOperation().getUIResources() != null) {
        if ((request != null) && (response != null)) {
          FacesContextBroker fcb = new FacesContextBroker(request,response);
          context.setMessageBroker(fcb.extractMessageBroker());
        }
      }
      
      // handle the operation
      handler.handle(context);
      
    } catch (NotAuthorizedException e) {
      throw e;
    } catch (AsnInsufficientPrivilegeException e) {
      context.getOperationResponse().exceptionToResponse(context,e);
      LOGGER.log(Level.FINER,e.toString(),e);
    } catch (AsnInvalidOperationException e) {
      context.getOperationResponse().exceptionToResponse(context,e);
      LOGGER.log(Level.FINER,e.toString(),e);
    } catch (Exception e) {
      context.getOperationResponse().exceptionToResponse(context,e);
      LOGGER.log(Level.SEVERE,e.toString(),e);
    }
        
    // return the response
    return context.getOperationResponse();
    
  }
  
  /**
   * Fully reads the characters from the request input stream.
   * @param request the HTTP servlet request
   * @return the characters read
   * @throws IOException if an exception occurs
   */
  protected String readInputCharacters(HttpServletRequest request, int maxChars) 
    throws IOException {
    StringBuffer sb = new StringBuffer();
    InputStream is = null;
    InputStreamReader ir = null;
    BufferedReader br = null;
    try {
      char cbuf[] = new char[4096];
      int n = 0;
      int nLen = cbuf.length;
      String encoding = request.getCharacterEncoding();
      if ((encoding == null) || (encoding.trim().length() == 0)) {
        encoding = "UTF-8";
      }
      is = request.getInputStream();
      ir = new InputStreamReader(is,encoding);
      br = new BufferedReader(ir);
      while ((n = br.read(cbuf,0,nLen)) >= 0) {
        sb.append(cbuf,0,n);
        if ((maxChars >= 0) && (sb.length() > maxChars)) {
          break;
        }
      }
    } finally {
      try {if (br != null) br.close();} catch (Exception ef) {}
      try {if (ir != null) ir.close();} catch (Exception ef) {}
      try {if (is != null) is.close();} catch (Exception ef) {}
    }
    if ((maxChars >= 0) && (sb.length() > maxChars)) {
      return sb.substring(0,maxChars);
    } else {
      return sb.toString();
    }
  }

}
