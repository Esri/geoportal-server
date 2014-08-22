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
import com.esri.gpt.catalog.schema.UnrecognizedSchemaException;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.DomUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.fileupload.FileItem;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
        
        if (sLcUri.endsWith("/definition/type")) {
          this.executeDefinitionType(request,response,context);
        } else if (sLcUri.endsWith("/definition/types")) {
          this.executeDefinitionTypes(request,response,context);
        } else if (sLcUri.endsWith("/echo/attachment")) {
          this.executeEchoAttachment(request,response,context);          
        } else if (sLcUri.endsWith("/i18n")) {
          this.executeI18N(request,response,context);
        } else if (sLcUri.endsWith("/interrogate")) {
          this.executeInterrogate(request,response,context);
        } else if (sLcUri.endsWith("/interrogate/details")) {
          this.executeInterrogateDetails(request,response,context,true);
        } else if (sLcUri.endsWith("/interrogate/multipart")) {
          this.executeInterrogate(request,response,context);
          
        // this is for backward compatibility, 
        // should be using /definition/type
        } else if (sLcUri.endsWith("/definition")) {
          this._executeDefinitionRequest(request,response,context);
          
        // this is for backward compatibility, 
        // should be using /interrogate/details
        } else if (sLcUri.endsWith("/details")) {
          this.executeInterrogateDetails(request,response,context,false);
          
        // this is for backward compatibility, 
        // should be using /definition/types
        } else if (sLcUri.endsWith("/types")) {
          this.executeDefinitionTypes(request,response,context);
          
        }
      } 
    } finally {
      context.onExecutionPhaseCompleted();
    }
  }
  
  /**
   * Handles a request to return the editor definition for
   * a supplied schema key or location.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  private void executeDefinitionType(HttpServletRequest request,
                                     HttpServletResponse response, 
                                     RequestContext context) 
    throws Exception {
    
    String sMimeType = "application/json";
    String sResponse = "";
    String sCallback = "";
    MessageBroker msgBroker = null;
    try {
      
      // determine the response format
      String f = Val.chkStr(request.getParameter("f"));
      if (f.equalsIgnoreCase("pjson")) {
        sMimeType = "text/plain";
      }
      sCallback = Val.chkStr(request.getParameter("callback"));
      
      // determine the definition
      GxeDefinition definition = null;
      String key = Val.chkStr(request.getParameter("key"));
      String loc = Val.chkStr(request.getParameter("loc"));
      if (key.length() > 0) {
        Schemas schemas = context.getCatalogConfiguration().getConfiguredSchemas();
        Schema schema = schemas.get(key);
        if (schema == null) {
          throw new SchemaException("Unsupported schema key.");
        } else {
          definition = schema.getGxeEditorDefinition();
        }
      } else if (loc.length() > 0) {
        definition = new GxeDefinition();
        definition.setFileLocation(loc); 
      } 
      String sCfg = "";
      if (definition != null) {
        msgBroker = this.getMessageBroker(request,response,context,msgBroker);
        sCfg = this.generateDefinition(request,response,context,msgBroker,definition);
      }
      
      // set the response
      if ((sCfg != null) && (sCfg.length() > 0)) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\r\n\"cfgDefinition\":").append(sCfg);
        sb.append("\r\n}");
        sResponse = sb.toString();
      } else {
        throw new SchemaException("Unsupported schema type.");
      }

    } catch (Throwable t) {
      if (!(t instanceof SchemaException)) {
        LOGGER.log(Level.WARNING,"Error processing request.",t);
      }
      sResponse = this.generateJsonError(request,response,context,msgBroker,t);
    } 
    
    // write the response
    //LOGGER.finest("gxeResponse:\n"+sResponse);
    if ((sResponse != null) && (sResponse.length() > 0)) {
      if ((sCallback != null) && (sCallback.length() > 0)) {
        sResponse = sCallback+"("+sResponse+")";
      }
      writeCharacterResponse(response,sResponse,"UTF-8",sMimeType+";charset=UTF-8");
    }
  }

  /**
   * Handles a request for the list of defined editor types.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  private void executeDefinitionTypes(HttpServletRequest request,
                                      HttpServletResponse response, 
                                      RequestContext context) 
    throws Exception {
    
    String sMimeType = "application/json";
    String sResponse = "";
    String sCallback = "";
    MessageBroker msgBroker = null;
    try {
      sCallback = Val.chkStr(request.getParameter("callback"));
      
      // make the list of defined editor types
      StringBuilder sb = new StringBuilder();
      sb.append("{\"types\": [");
      Schemas schemas = context.getCatalogConfiguration().getConfiguredSchemas();
      if (schemas != null) {
        int n = 0;
        msgBroker = this.getMessageBroker(request,response,context,msgBroker);
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

    } catch (Throwable t) {
      LOGGER.log(Level.WARNING,"Error processing request.",t);
      sResponse = this.generateJsonError(request,response,context,msgBroker,t);
    } 
    
    // write the response
    //LOGGER.finest("gxeResponse:\n"+sResponse);
    if ((sResponse != null) && (sResponse.length() > 0)) {
      if ((sCallback != null) && (sCallback.length() > 0)) {
        sResponse = sCallback+"("+sResponse+")";
      }
      writeCharacterResponse(response,sResponse,"UTF-8",sMimeType+";charset=UTF-8");
    }
  }   
  
  /**
   * Handles a request to return a posted XML document as
   * a content disposition attachment.
   * <br/>The posted mime-type should be: application/x-www-form-urlencoded
   * <br/>The posted URL parameter name should be: xml
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  private void executeEchoAttachment(HttpServletRequest request,
                                     HttpServletResponse response, 
                                     RequestContext context) 
    throws Exception {
    
    String sResponse = "";
    String sFilename = "";
    try {
      
      // read the posted xml
      String sXml = this.readPostedXml(request,response,context);
      if ((sXml != null) && (sXml.length() > 0)) {
        sResponse = sXml;
        sFilename = Val.chkStr(request.getParameter("filename"));
        if (sFilename.length() == 0) sFilename = "metadata.xml";
      } else {
        sFilename = "error.txt";
        sResponse = "Error: The posted XML was empty.";
      }
    } catch (Throwable t) {
      LOGGER.log(Level.WARNING,"Error processing request.",t);
      String sMsg = Val.chkStr(t.getMessage());
      if (sMsg.length() == 0) sMsg = t.toString();
      sFilename = "error.xml";
      sResponse = "Error: "+sMsg;
    } 
    
    // write the response
    //LOGGER.finest("gxeResponse:\n"+sResponse);
    response.setContentType("APPLICATION/OCTET-STREAM; charset=UTF-8");
    response.setHeader("Content-Disposition","attachment; filename=\""+sFilename+"\"");          
    writeCharacterResponse(response,sResponse,"UTF-8","APPLICATION/OCTET-STREAM; charset=UTF-8");
  }    
  

  
  /**
   * Handles a request to lookup a set of localized strings.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  private void executeI18N(HttpServletRequest request,
                           HttpServletResponse response, 
                           RequestContext context) 
    throws Exception {
    
    String sMimeType = "application/json";
    String sResponse = "";
    String sCallback = "";
    MessageBroker msgBroker = null;
    try {
      
      // determine the response format
      String f = Val.chkStr(request.getParameter("f"));
      if (f.equalsIgnoreCase("pjson")) {
        sMimeType = "text/plain";
      }
      sCallback = Val.chkStr(request.getParameter("callback"));
      msgBroker = this.getMessageBroker(request,response,context,msgBroker);
      
      // determine the property keys
      String sKeys = Val.chkStr(request.getParameter("keys"));
      String[] aKeys = sKeys.split(",");
      
      // lookup the strings
      StringBuilder sb = new StringBuilder();
      sb.append("{\"i18n\": {");
      int n = 0;
      if ((aKeys != null) && (aKeys.length > 0)) {
        for (String sKey: aKeys) {
          String sValue = msgBroker.retrieveMessage(sKey);
          n++;
          if (n > 1) sb.append(",");
          sb.append("\r\n\t");
          sb.append("\"").append(Val.escapeStrForJson(sKey)).append("\": ");
          sb.append("\"").append(Val.escapeStrForJson(sValue)).append("\"");
        }
      }
      sb.append("\r\n}}");
      sResponse = sb.toString();
    
    } catch (Throwable t) {
      LOGGER.log(Level.WARNING,"Error processing request.",t);
      sResponse = this.generateJsonError(request,response,context,msgBroker,t);
    } 
    
    // write the response
    //LOGGER.finest("gxeResponse:\n"+sResponse);
    if ((sResponse != null) && (sResponse.length() > 0)) {
      if ((sCallback != null) && (sCallback.length() > 0)) {
        sMimeType = "text/plain";
        sResponse = sCallback+"("+sResponse+")";
      }
      writeCharacterResponse(response,sResponse,"UTF-8",sMimeType+";charset=UTF-8");
    }
  }

  /**
   * Handles a request to interrogate the editor definition for
   * an uploaded XML document.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  private void executeInterrogate(HttpServletRequest request,
                                  HttpServletResponse response, 
                                  RequestContext context) 
    throws Exception {
    
    String sMimeType = "application/json";
    String sResponse = "";
    boolean isIE = false;
    boolean bWrap = false;
    MessageBroker msgBroker = null;
    try {
       isIE = Val.chkBool(request.getParameter("isIE"),false);
      
      // interrogate the posted XMl, generate the definition
      bWrap = Val.chkBool(request.getParameter("wrap"),false);
      String sXml = this.readPostedXml(request,response,context);
      Schema schema = this.interrogateSchema(context,sXml);
      String sCfg = "";
      GxeDefinition definition = schema.getGxeEditorDefinition();
      if (definition != null) {
        msgBroker = this.getMessageBroker(request,response,context,msgBroker);
        sCfg = this.generateDefinition(request,response,context,msgBroker,definition);
      }
      
      // set the response
      if ((sCfg != null) && (sCfg.length() > 0)) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\r\n\"cfgDefinition\":").append(sCfg);
        sb.append(",\r\n\"xml\":\"").append(
            Val.escapeStrForJson(schema.getActiveDocumentXml())).append("\"");
        sb.append("\r\n}");
        sResponse = sb.toString();
      } else {
        throw new SchemaException("Unsupported XML type.");
      }

    } catch (Throwable t) {
      if (!(t instanceof SchemaException)) {
        LOGGER.log(Level.WARNING,"Error processing request.",t);
      }
      sResponse = this.generateJsonError(request,response,context,msgBroker,t);
    } 
    
    // write the response
    //LOGGER.finest("gxeResponse:\n"+sResponse);
    if ((sResponse != null) && (sResponse.length() > 0)) {
    	if(isIE){
    		sMimeType = "text/html";
    		sResponse = "<script type=\"text/javascript\">var gxeImportFileResponse="+sResponse+";</script>";    		
    	}
      // responseObject= is required for the 
      // HTML5 based dojox.form.Uploader 1.6
    	else if (bWrap) sResponse = "responseObject="+sResponse;
      writeCharacterResponse(response,sResponse,"UTF-8",sMimeType+";charset=UTF-8");
    }
  }
  
  /**
   * Handles a request for a details view of an XML document.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  private void executeInterrogateDetails(HttpServletRequest request,
                                         HttpServletResponse response, 
                                         RequestContext context,
                                         boolean defaultToJson) 
    throws Exception {
    
    String sMimeType = "application/json";
    String sFormat = "json";
    String sResponse = "";
    MessageBroker msgBroker = null;
    try {

      // determine the response format
      if (!defaultToJson) {
        sFormat = "htmlFragment";
      }
      String f = Val.chkStr(request.getParameter("f"));
      if (f.equalsIgnoreCase("htmlFragment")) {
        sFormat = "htmlFragment";
      } else if (f.equalsIgnoreCase("json")) {
        sFormat = "json";
      } else if (f.equalsIgnoreCase("pjson")) {
        sFormat = "json";
      }
      if (!sFormat.equals("json")) {
        sMimeType = "text/plain";
      }
      
      // interrogate the posted XMl, generate the details
      String sXml = this.readPostedXml(request,response,context);
      Schema schema = this.interrogateSchema(context,sXml);
      msgBroker = this.getMessageBroker(request,response,context,msgBroker);
      String sDetails = this.generateDetails(request,response,context,msgBroker,schema);
      sDetails = Val.chkStr(sDetails);
      
      // set the response
      if (sFormat.equals("json")) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"details\": \"").append(Val.escapeStrForJson(sDetails)).append("\"");
        sb.append("}");
        sResponse = sb.toString();
      } else {
        sResponse = sDetails;
      }
      
    } catch (Throwable t) {
      if (!(t instanceof SchemaException)) {
        LOGGER.log(Level.WARNING,"Error processing request.",t);
      }
      if (sFormat.equals("json")) {
        sResponse = this.generateJsonError(request,response,context,msgBroker,t);
      } else {
        String sMsg = Val.chkStr(t.getMessage());
        if (sMsg.length() == 0) sMsg = t.toString();
        sResponse = Val.escapeXmlForBrowser(sMsg);
      }
    }
    
    // write the response
    // LOGGER.finest("gxeResponse:\n"+sResponse);
    if ((sResponse != null) && (sResponse.length() > 0)) {
      writeCharacterResponse(response,sResponse,"UTF-8",sMimeType+";charset=UTF-8");
    }
  }  
  
  /**
   * Generates the JSON editor definition.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @param msgBroker the message broker
   * @param definition the GXE definition
   * @return the JSON definition
   * @throws Exception if a processing exception occurs
   */
  private String generateDefinition(HttpServletRequest request,
                                    HttpServletResponse response,
                                    RequestContext context,
                                    MessageBroker msgBroker,
                                    GxeDefinition definition) 
    throws Exception {
    GxeContext gxeContext = new GxeContext();
    gxeContext.setMessageBroker(msgBroker);
    if (definition.getRootElement() == null) {
      GxeLoader loader = new GxeLoader();
      loader.loadDefinition(gxeContext,definition);
    }
    GxeJsonSerializer serializer = new GxeJsonSerializer();
    return serializer.asJson(gxeContext,definition);
  }
  
  /**
   * Generates a details view for an interrogated schema.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @param msgBroker the message broker
   * @param schema the interrogated schema
   * @return the details
   * @throws Exception if a processing exception occurs
   */
  private String generateDetails(HttpServletRequest request,
                                 HttpServletResponse response,
                                 RequestContext context,
                                 MessageBroker msgBroker,
                                 Schema schema) 
    throws Exception {
    String sXslt = Val.chkStr(schema.getDetailsXslt());
    if (sXslt.length() > 0) {
      MetadataDocument document = new MetadataDocument();
      return document.transformDetails(
          schema.getActiveDocumentXml(),sXslt,msgBroker);
    }
    return null;
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
  private String generateJsonError(HttpServletRequest request,
                                   HttpServletResponse response,
                                   RequestContext context,
                                   MessageBroker msgBroker,
                                   Throwable t) {
    String sMsg = Val.chkStr(t.getMessage());
    if (sMsg.length() == 0) sMsg = t.toString();
    
    // TODO localized messages here
    if (sMsg.contains("Unrecognized metadata schema.")) {
    } else if (sMsg.contains("Unable to parse document.")) {
    } else if (sMsg.contains("Unsupported XML type.")) {  
    } 
    
    StringBuilder sb = new StringBuilder();
    sb.append("{\"error\":{");
    sb.append("\"message\": \"").append(Val.escapeStrForJson(sMsg)).append("\"");
    sb.append("}}");
    return sb.toString();
  }
  
  /**
   * Ensures the existence of a message broker.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @param msgBroker a message broker reference 
   *   (if null then a new message broker will be generated)
   * @return the message broker
   */
  private MessageBroker getMessageBroker(HttpServletRequest request,
                                         HttpServletResponse response,
                                         RequestContext context,
                                         MessageBroker msgBroker) {
    // TODO check for lang parameter
    if (msgBroker != null) {
      return msgBroker;
    } else {
      FacesContextBroker fcb = new FacesContextBroker(request,response);
      return fcb.extractMessageBroker();
    }
  }
  
  /**
   * Interrogates a schema based upon a posted XML document.
   * @param context the request context
   * @param xml the XML string
   * @return the corresponding schema
   * @throws Exception if a processing exception occurs
   */
  private Schema interrogateSchema(RequestContext context, String xml) 
    throws Exception {
    String sXml = Val.chkStr(Val.removeBOM(xml));
    if (sXml.length() == 0) {
      throw new UnrecognizedSchemaException("Unrecognized metadata schema.");
    } else {
      MetadataDocument document = new MetadataDocument();
      Schema schema = document.prepareForView(context,sXml);
      return schema;
    }
  }  
  
  /**
   * Reads a posted XML document.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  private String readPostedXml(HttpServletRequest request,
                               HttpServletResponse response, 
                               RequestContext context) 
    throws Exception {
    String sXml = "";
    String sContentType = Val.chkStr(request.getContentType());
    if (sContentType.toLowerCase().startsWith("multipart/form-data;")) {
      String sMultipartAttribute = "uploadedfiles[]";
      Object oFile = request.getAttribute(sMultipartAttribute);
      if (oFile == null) {
      	sMultipartAttribute = "uploadedfile";
      	oFile = request.getAttribute(sMultipartAttribute);
      }
      if ((oFile != null) && (oFile instanceof FileItem)) {
        FileItem item = (FileItem)oFile;
        sXml = Val.chkStr(Val.removeBOM(item.getString("UTF-8")));
      }
    } else if (sContentType.toLowerCase().startsWith("application/x-www-form-urlencoded")) {
      String sFormUrlParamater = "xml";
      sXml = Val.chkStr(Val.removeBOM(request.getParameter(sFormUrlParamater)));
    } else {
      sXml = Val.chkStr(Val.removeBOM(this.readInputCharacters(request)));
    }
    
    /*
    // check for multipart/form-data
    String sMultipartAttribute = "uploadedfiles[]";
    Object oFile = request.getAttribute(sMultipartAttribute);
    if ((oFile != null) && (oFile instanceof FileItem)) {
      FileItem item = (FileItem)oFile;
      sXml = Val.chkStr(Val.removeBOM(item.getString("UTF-8")));
    } else {
      
      // check for application/x-www-form-urlencoded
      String sFormUrlParamater = "xml";
      sXml = Val.chkStr(request.getParameter(sFormUrlParamater));
      if (sXml == null) {
        
        // check the raw post body
        sXml = this.readInputCharacters(request);
      }
    }
    */
    return sXml;
  }  
  
  
  /**
   * Handles a request for an editor definition.
   * <br/>Original method, should be deprecated.
   * @param request the HTTP servlet request
   * @param response the HTTP servlet response
   * @param context the request context
   * @throws Exception if a processing exception occurs
   */
  private void _executeDefinitionRequest(HttpServletRequest request,
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
        
        String sCallbackParam = Val.chkStr(request.getParameter("callback"));
        if (sCallbackParam.length() > 0) {
          sResponse = sCallbackParam+"("+sResponse+")";
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

}
