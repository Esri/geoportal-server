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
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.Schema;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.catalog.schema.Schemas;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
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
            
    // determine the request type and execute
    try {
      if (request.getRequestURI() != null) {
        String sLcUri = request.getRequestURI().toLowerCase();
        if (sLcUri.endsWith("/definition")) {
          this.executeDefinitionRequest(request,response,context);
        } else if (sLcUri.endsWith("/details")) {
          this.executeDetailsRequest(request,response,context);
        } else if (sLcUri.endsWith("/types")) {
          this.executeTypesRequest(request,response,context);
        }
      } 
    } finally {
      context.onExecutionPhaseCompleted();
    }
  }
  
  
  /**
   * Handles a request for an editor definition.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  protected void executeDefinitionRequest(HttpServletRequest request,
                                          HttpServletResponse response, 
                                          RequestContext context) 
    throws Exception {
            
    // initialize
    String mimeType = "text/plain";
    String sResponse = "";
    
    // handle a request for an editor definition
    try {
      GxeContext gxeContext = new GxeContext();
      FacesContextBroker fcb = new FacesContextBroker(request,response);
      gxeContext.setMessageBroker(fcb.extractMessageBroker());
      
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
      } else {
        
        String xml = null;
        try {
          xml = this.readInputCharacters(request);
        } catch (IOException e) {
          LOGGER.log(Level.WARNING,"Error processing request.",e);
          throw new ServletException("400: IOException while reading request body.");
        }
        xml = Val.chkStr(Val.removeBOM(xml));
        if (xml.length() > 0) {
          MetadataDocument document = new MetadataDocument();
          Schema schema = document.prepareForView(context,xml);
          if (schema != null) {
            definition = schema.getGxeEditorDefinition();
          }
        }
        
      }
      if (definition != null) {
        if (definition.getRootElement() == null) {
          GxeLoader loader = new GxeLoader();
          loader.loadDefinition(gxeContext,definition);
        }
        GxeJsonSerializer serializer = new GxeJsonSerializer();
        sResponse = serializer.asJson(gxeContext,definition);
      }
      
    } catch (SchemaException e) {
      LOGGER.log(Level.WARNING,"Error processing request.",e);
      String sMsg = e.toString();
      if (sMsg.contains("Unrecognized metadata schema.")) {
        throw new ServletException("409: Unrecognized metadata schema.");
      } else if (sMsg.contains("Unable to parse document.")) {
        throw new ServletException("409: Unable to parse document as XML.");
      } else {
        throw new ServletException("409: Unable process request.");
      } 

    } catch (Exception e) {
      throw e;
    } finally {
      context.onExecutionPhaseCompleted();
    }
    
    // write the response
    LOGGER.finest("gxeResponse:\n"+sResponse);
    if ((sResponse != null) && (sResponse.length() > 0)) {
      writeCharacterResponse(response,sResponse,"UTF-8",mimeType+";charset=UTF-8");
    }
    
  }
  
  /**
   * Handles a request to view the details for a document.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  protected void executeDetailsRequest(HttpServletRequest request,
                                       HttpServletResponse response, 
                                       RequestContext context) 
    throws Exception {
         
    try {
      // initialize
      String mimeType = "text/plain";
      String sResponse = "";
          
      String xml = null;
      try {
        try {
          xml = this.readInputCharacters(request);
        } catch (IOException e) {
          LOGGER.log(Level.WARNING,"Error processing request.",e);
          throw new ServletException("400: IOException while reading request body.");
        }
        xml = Val.chkStr(Val.removeBOM(xml));
        if (xml.length() > 0) {
          MetadataDocument document = new MetadataDocument();
          Schema schema = document.prepareForView(context,xml); 
          String sDetailsXslt = Val.chkStr(schema.getDetailsXslt());
          if (sDetailsXslt.length() > 0) {
        	FacesContextBroker fcb = new FacesContextBroker(request,response);        	
            sResponse = Val.chkStr(document.transformDetails(xml,sDetailsXslt,fcb.extractMessageBroker()));
          }
        }
        
      } catch (SchemaException e) {
        LOGGER.log(Level.WARNING,"Error processing request.",e);
        String sMsg = e.toString();
        if (sMsg.contains("Unrecognized metadata schema.")) {
          throw new ServletException("409: Unrecognized metadata schema.");
        } else if (sMsg.contains("Unable to parse document.")) {
          throw new ServletException("409: Unable to parse document as XML.");
        } else {
          throw new ServletException("409: Unable process request.");
        }      
        
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE,"Error processing request.",e);
        throw new ServletException("409: Unable process request.");
      } 
      
      // write the response
      LOGGER.finest("gxeResponse:\n"+sResponse);
      if ((sResponse != null) && (sResponse.length() > 0)) {
        writeCharacterResponse(response,sResponse,"UTF-8",mimeType+";charset=UTF-8");
      }
      
    } catch (ServletException e) {
      String sMsg = e.getMessage();
      int nCode = Val.chkInt(sMsg.substring(0,3),500);
      sMsg = Val.chkStr(sMsg.substring(4)); 
      response.sendError(nCode,sMsg);
      
    } catch (Throwable t) {
      LOGGER.log(Level.SEVERE,"Error processing request.",t);
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      
    } finally {
      context.onExecutionPhaseCompleted();
    }
  }
  
  /**
   * Handles a request for the list of defined editor types.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  protected void executeTypesRequest(HttpServletRequest request,
                                     HttpServletResponse response, 
                                     RequestContext context) 
    throws Exception {
            
    // initialize
    String mimeType = "text/plain";
    String sResponse = "";
    
    // handle a request for the list of editor definitions
    try {
      FacesContextBroker fcb = new FacesContextBroker(request,response);
      MessageBroker msgBroker = fcb.extractMessageBroker();  

      StringBuilder sb = new StringBuilder();
      sb.append("{\"types\": [");
      
      Schemas schemas = context.getCatalogConfiguration().getConfiguredSchemas();
      if (schemas != null) {
        int n = 0;
        for (Schema schema: schemas.values()) {
          if (schema.getEditable()) {
            GxeDefinition definition = schema.getGxeEditorDefinition();
            if (definition != null) {
              String key = schema.getKey();
              String label = null;
              if (schema.getLabel() != null) {
                String resKey = schema.getLabel().getResourceKey();
                if ((resKey != null) && (resKey.length() > 0)) {
                  label = msgBroker.retrieveMessage(resKey);
                }
              }
              if ((label == null) || (label.length() == 0)) {
                label = key;
              }
              n++;
              if (n > 1) sb.append(",");
              sb.append("\r\n\t");
              sb.append("{\"key\": \"").append(Val.escapeStrForJson(key)).append("\"");
              sb.append(", \"label\": \"").append(Val.escapeStrForJson(label)).append("\"}");
            }
          }
        }
      }  
      sb.append("\r\n]}");
      sResponse = sb.toString();

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
