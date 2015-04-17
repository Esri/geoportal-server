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
package com.esri.gpt.server.assertion;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.server.assertion.components.AsnResponse;
import com.esri.gpt.server.assertion.handler.AsnRequestHandler;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Assertion servlet.
 */
@SuppressWarnings("serial")
public class AsnServlet extends BaseServlet {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(AsnServlet.class.getName());
  
  /** methods ================================================================= */

  /**
   * Executes a request.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  @Override
  protected void execute(HttpServletRequest request,
                         HttpServletResponse response, 
                         RequestContext context) 
    throws Exception {
            
    // initialize
    String mimeType = "text/plain";
    String sResponse = "";
    AsnFactory asnFactory = null;
    AsnRequestHandler asnHandler = null;
    AsnResponse asnResponse = null;
    
    // execute the assertion operation
    try {
      asnFactory = AsnFactory.newFactory(context);
      asnHandler = asnFactory.makeRequestHandler(request,context);
      asnResponse = asnHandler.getAssertionContext().getOperationResponse();
      asnHandler.handleRequest(request,response);
      sResponse = Val.chkStr(asnResponse.getResponseString());
      mimeType = asnResponse.getMimeType();
    } catch (NotAuthorizedException e) {
      throw e;
    } catch (Exception e) {
      if (asnResponse != null) {
        asnResponse.exceptionToResponse(asnHandler.getAssertionContext(),e);
        sResponse = Val.chkStr(asnResponse.getResponseString());
      } else {
        throw e;
      }
    }
    
    // write the response
    LOGGER.finer("assertionResponse:\n"+Val.stripControls(sResponse));
    if ((sResponse != null) && (sResponse.length() > 0)) {
      writeCharacterResponse(response,sResponse,"UTF-8",mimeType+"; charset=UTF-8");
    }
    
  }

}
