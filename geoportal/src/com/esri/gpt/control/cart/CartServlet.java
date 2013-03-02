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
package com.esri.gpt.control.cart;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet end-point for a simple implementation of an item cart based 
 * upon an in-memory (i.e. session) collection of ids.
 */
@SuppressWarnings("serial")
public class CartServlet extends BaseServlet {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(CartServlet.class.getName());

  /** methods ================================================================= */

  /**
   * Processes the HTTP request.
   * @param request the HTTP request
   * @param response HTTP response
   * @param context request context
   * @throws Exception if an exception occurs
   */
  @Override
  protected void execute(HttpServletRequest request,
      HttpServletResponse response, RequestContext context) throws Exception {
        
    String sMimeType = "application/json";
    String sResponse = "";
    String sCallback = "";
    String sWarning  = "";
    boolean bGenerateInfo = true;
    boolean bIncludeKeys = false;
    MessageBroker msgBroker = null;
    
    // determine the request type and execute
    try {
      String sLcUri = request.getRequestURI().toLowerCase();
      StringAttributeMap cfg = context.getCatalogConfiguration().getParameters();
      int maxItems = Val.chkInt(cfg.getValue("catalog.cart.maxItems"),10);
        
      // determine the response format
      String f = Val.chkStr(request.getParameter("f"));
      if (f.equalsIgnoreCase("pjson")) {
        sMimeType = "text/plain";
      }
      sCallback = Val.chkStr(request.getParameter("callback"));
      
      // get the cart from the session
      HttpSession session = request.getSession();
      if (session == null) {
        session = request.getSession(true);
      }
      String sSessionKey = Cart.class.getCanonicalName();
      Cart cart = (Cart)session.getAttribute(sSessionKey);
      if (cart == null) {
        cart = new Cart();
        session.setAttribute(sSessionKey,cart);
      }
      
      // add a key
      if (sLcUri.endsWith("/add")) {
        String sKey = Val.chkStr(request.getParameter("key"));
        if ((sKey.length() > 0) && !cart.containsKey(sKey)) {
          if (cart.size() < maxItems) {
            cart.add(sKey);
          } else {
            sWarning = "cartWasFull";
          }
        }
        
      // clear the cart
      } else if (sLcUri.endsWith("/clear")) {
        if (cart.size() > 0) {
          cart.clear();
        }
        
      // include the keys within the response
      } else if (sLcUri.endsWith("/keys")) {
        bIncludeKeys = true;
        
      // process the response
      } else if (sLcUri.endsWith("/process")) {
        
        /*
        some examples of a processor for gpt/form/Cart.js
        
        processor: "?processor=com.esri.gpt.control.cart.ZipXmls",
        
        processor: "?processor=com.esri.gpt.control.cart.XslBundler"+
                   "&xslt="+encodeURIComponent("gpt/metadata/some.xslt")+
                   "&mimeType="+encodeURIComponent("text/html"),
                   "&contentDisposition="+encodeURIComponent("attachment; filename=tmp.xml"),
        */
        
        bGenerateInfo = false;
        KeysetProcessor processor = KeysetProcessor.newProcessor(request,context);
        if (processor != null) {
          processor.execute(request,response,context);
        }
        
      // remove a key
      } else if (sLcUri.endsWith("/remove")) {
        String sKey = Val.chkStr(request.getParameter("key"));
        if (sKey.length() > 0) {
          cart.remove(sKey);
        }
      } 
      
      // generate the response
      if (bGenerateInfo) {
        sResponse = this.generateJsonInfo(request,response,context,
            msgBroker,cart,maxItems,sWarning,bIncludeKeys);
      }
      
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE,"Exception:",t);
      if (sMimeType == null) {
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      } else {
        sResponse = this.generateJsonError(request,response,context,
            msgBroker,t);
      }
    } finally {
      context.onExecutionPhaseCompleted();
    }
    
    // write the response
    if ((sResponse != null) && (sResponse.length() > 0)) {
      LOGGER.finest("cartResponse:\n"+sResponse);
      if ((sCallback != null) && (sCallback.length() > 0)) {
        sResponse = sCallback+"("+sResponse+")";
      }
      writeCharacterResponse(response,sResponse,"UTF-8",sMimeType+";charset=UTF-8");
    }
    
  }
  
  /**
   * Generates a JSON based error object.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @param msgBroker the message broker
   * @param t the exception
   * @return the JSON string
   */
  protected String generateJsonError(HttpServletRequest request,
                                     HttpServletResponse response,
                                     RequestContext context,
                                     MessageBroker msgBroker,
                                     Throwable t) {
    String sMsg = Val.chkStr(t.getMessage());
    if (sMsg.length() == 0) sMsg = t.toString();    
    StringBuilder sb = new StringBuilder();
    sb.append("{\"error\":{");
    sb.append("\"message\":\" ").append(Val.escapeStrForJson(sMsg)).append("\"");
    sb.append("}}");
    return sb.toString();
  }
  
  /**
   * Generates a JSON based summary object for the cart.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @param msgBroker the message broker
   * @param cart the cart
   * @param maxItems the maximum number of items
   * @param warning optional warning message
   * @param includeKeys optionally include the keys
   * @return the JSON string
   */
  protected String generateJsonInfo(HttpServletRequest request,
                                    HttpServletResponse response,
                                    RequestContext context,
                                    MessageBroker msgBroker,
                                    Cart cart,
                                    int maxItems,
                                    String warning,
                                    boolean includeKeys) {
    warning = Val.chkStr(warning);
    String pfx = "\r\n  ";
    StringBuilder sb = new StringBuilder();
    sb.append("{\"cart\":{");
    sb.append(pfx+"\"size\": ").append(cart.size()).append(",");
    sb.append(pfx+"\"maxItems\": ").append(maxItems);
    if (warning.length() > 0) {
      sb.append(",");
      sb.append(pfx+"\"warning\":\" ").append(Val.escapeStrForJson(warning)).append("\"");
    }
    if (includeKeys) {
      Set<String> keys = cart.keySet();
      StringBuilder sbK = new StringBuilder();
      for (String sKey: keys) {
        if (sbK.length() > 0) {
          sbK.append(",");
        }
        String sEsc = Val.escapeStrForJson(sKey);
        sbK.append("\"").append(sEsc).append("\"");
      }
      sb.append(",");
      sb.append(pfx+"\"keys\": [").append(sbK.toString()).append("]");
    }
    sb.append("\r\n}}");
    return sb.toString();
  }

}
