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
package com.esri.gpt.catalog.gxe;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.Schemas;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.util.Val;

import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The Geoportal XML editor servlet.
 */
@SuppressWarnings("serial")
public class GxeServlet extends BaseServlet {
  
  /** class variables ========================================================= */
  
  /** The Logger. */
  private static Logger LOGGER = Logger.getLogger(GxeServlet.class.getName());
  
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
    
    // execute the assertion operation
    try {
      GxeContext gxeContext = new GxeContext();
      FacesContextBroker fcb = new FacesContextBroker(request,response);
      gxeContext.setMessageBroker(fcb.extractMessageBroker());
      
      // handle a request for an editor definition
      boolean isDefinitionRequest = false;
      if (request.getRequestURI() != null) {
        isDefinitionRequest = request.getRequestURI().toLowerCase().endsWith("/definition");
      }
      if (isDefinitionRequest) {
        GxeDefinition definition = null;
        String key = Val.chkStr(request.getParameter("key"));
        String loc = Val.chkStr(request.getParameter("loc"));
        if (key.length() > 0) {
          Schemas schemas = context.getCatalogConfiguration().getConfiguredSchemas();
          Schema schema = schemas.get(key);
          if (schema != null) {
            definition = schema.getGxeEditorDefinition();
          }
        } else if (loc.length() > 0) {
          definition = new GxeDefinition();
          definition.setFileLocation(loc); 
        }
        if (definition != null) {
          if (definition.getRootElement() == null) {
            GxeLoader loader = new GxeLoader();
            loader.loadDefinition(gxeContext,definition);
          }
          GxeJsonSerializer serializer = new GxeJsonSerializer();
          sResponse = serializer.asJson(gxeContext,definition);
        }
      }

    } catch (Exception e) {
      throw e;
    }
    
    // write the response
    LOGGER.finest("gxeResponse:\n"+sResponse);
    if ((sResponse != null) && (sResponse.length() > 0)) {
      writeCharacterResponse(response,sResponse,"UTF-8",mimeType+";charset=UTF-8");
    }
    
  }

}
