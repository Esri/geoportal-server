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
package com.esri.gpt.control.rest;
import com.esri.gpt.catalog.arcims.ImsMetadataAdminDao;
import com.esri.gpt.catalog.harvest.protocols.HarvestProtocol.ProtocolType;
import com.esri.gpt.catalog.harvest.repository.HrAssertUrlException;
import com.esri.gpt.catalog.harvest.repository.HrCompleteUpdateRequest;
import com.esri.gpt.catalog.harvest.repository.HrRecord;
import com.esri.gpt.catalog.harvest.repository.HrRecord.HarvestFrequency;
import com.esri.gpt.catalog.management.MmdActionCriteria;
import com.esri.gpt.catalog.management.MmdActionRequest;
import com.esri.gpt.catalog.management.MmdCriteria;
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.catalog.management.MmdResult;
import com.esri.gpt.catalog.publication.PublicationRecord;
import com.esri.gpt.catalog.publication.PublicationRequest;
import com.esri.gpt.catalog.schema.MetadataDocument;
import com.esri.gpt.catalog.schema.SchemaException;
import com.esri.gpt.catalog.schema.ValidationException;
import com.esri.gpt.control.webharvest.client.arcgis.ArcGISProtocol;
import com.esri.gpt.control.webharvest.protocol.ProtocolFactory;
import com.esri.gpt.control.webharvest.protocol.ProtocolInvoker;
import com.esri.gpt.framework.robots.BotsMode;
import com.esri.gpt.framework.collection.StringAttribute;
import com.esri.gpt.framework.collection.StringAttributeMap;
import com.esri.gpt.framework.context.ApplicationConfiguration;
import com.esri.gpt.framework.context.ApplicationContext;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.credentials.Credentials;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.EnumerationAdapter;
import com.esri.gpt.framework.util.UuidUtil;
import com.esri.gpt.framework.util.Val;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Provides an HTTP REST interface for the management of an XML based metadata document.
 * <p/>
 * The URL pattern for this end-point is:<br/>
 * <i>http://host:port/application/</i><b>rest&#47;manage&#47;document</b> 
 * <p/>
 * Four HTTP methods are supported:
 * <ul>
 *   <li>GET - gets an XML document from the catalog
 *     <ul>
 *       <li>the subject document must be specified within the URL</li>
 *       <li>the XML is returned within the body of the HTTP response</li>
 *     </ul>
 *   <li>PUT - publishes an XML document to the catalog
 *     <ul>
 *       <li>the XML must be supplied within the body of the HTTP request.</li>
 *       <li>URL parameter publicationMethod is accepted, values are upload,editor,other</li>
 *       <li>URL parameter asDraft=true is accepted</li>
 *       <li>URL parameter approve=true is accepted</li>
 *       <li>if no document provided, that means this is resource registration request.</li>
 *       <li>returns HTTP 200 when a document is replaced</li>
 *       <li>returns HTTP 201 when a document is created</li>
 *     </ul>
 *   <li>POST - same as PUT</li>
 *   <li>DELETE - deletes a document from the catalog
 *     <ul>
 *       <li>the subject document must be specified within the URL</li>
 *       <li>returns HTTP 200 when a document is deleted</li>
 *     </ul>
 * </ul>
 * The subject document can be specified within the URL as follows:
 * <ul>
 *   <li>.../rest/manage/document/<b>identifier</b></li>
 *   <li>.../rest/manage/document<b>?id=identifier</b></li>
 *   <li><b>identifier</b> can be the catalog's UUID for the document, 
 *         or the file identifier internal to the XML document</li>
 *   <li><b>identifier</b> must be properly URL encoded</li>
 * </ul>
 * Attributes applicable for resource registration:
 * <ul>
 *    <li>url - resource URL (required)</li>
 *    <li>uuid - UUID of the resource (optional). If provided this is an update of the already existing resource</li>
 *    <li>name - resource name/title (optional)</li>
 *    <li>protocol - protocol type (optional: res,arcgis,arcims,csw,oai,waf). Default: res</li>
 *    <li>frequency - frequency (optional: Monthly, BiWeekly, Weekly, Dayly, Hourly, Once, Skip). Default: Skip</li>
 *    <li>sendNotification - to send notification (optional: true,false). Default: false</li>
 *    <li>updateContent - to harvest content during synchronization (optional: true,false). Default: true</li>
 *    <li>updateDefinition - to update definition during synchronization (optional: true,false). Default: true</li>
 *    <li>autoApprove - to automatically approve newly acquired metadata (optional: true,false). Default: true</li>
 *    <li>findable - to make this resource findable when searching for metadata (optional: true, false), Default: true</li>
 *    <li>searchable - to make this resource to act as distributed search endpoint (only if csw) (optional: true, false), Default: true</li>
 *    <li>synchronizable - to make this resource synchronizable (optional: true, false), Default: true</li>
 *    <li>username - user name required to access protected resource (optional)</li>
 *    <li>password - password required to access protected resource (optional)</li>
 *    <li>soapurl - (ARCGIS only) resource SOAP URL</li>
 *    <li>profile - (CSW only) profile</li>
 *    <li>set - (OAI only) set</li>
 *    <li>prefix - (OAI only) prefix</li>
 *    <li>service - (ARCIMS only) service</li>
 *    <li>port - (ARCIMS only) port</li>
 *    <li>rootFolder - (ARCIMS only) root folder</li>
 * </ul>
 * Exceptions applicable to all methods:
 * <ul>
 *   <li>HTTP 401 Unauthorized 
 *       - indicates that valid credentials must be supplied to access this URL</li>
 *   <li>HTTP 403 The document is owned by another user. 
 *       - when an attempt is made to access a document owned by another user</li>
 * </ul>
 * Exceptions applicable to GET/DETETE:
 * <ul>
 *   <li>HTTP 404 Document not found.. 
 *       - when the document specified within the request URL could not be located 
 *         within the catalog</li>
 * </ul>
 * Exceptions applicable to PUT/POST:
 * <ul>
 *   <li>HTTP 400 IOException while reading request body. 
 *       - when characters could not be read from the request body</li>
 *   <li>HTTP 409 Document was empty. 
 *       - when the request body was empty</li>   
 *   <li>HTTP 409 Unable to parse document as XML. 
 *       - when the content of the request body cannot be parsed as XML</li> 
 *   <li>HTTP 409 Unrecognized metadata schema. 
 *       - when the supplied XML does not match a configured XML standard for the catalog</li>
 *   <li>HTTP 409 Document failed to validate.. 
 *       - when the supplied XML is invalid with respect to the configured validation rules 
 *         for the catalog</li> 
 *   <li>HTTP 409 XSD violation. 
 *       - when the supplied XML is invalid with respect to it's XSD</li> 
 *   <li>HTTP 409 Duplicated resource URL.
 *       - when registering new resource with URL as another already registered resource</li>
 * </ul>
 * 
 */
public class ManageDocumentServlet extends BaseServlet {
  
  /** class variables ========================================================= */
  
  /** The logger.*/
  private static final Logger LOGGER = Logger.getLogger(ManageDocumentServlet.class.getName());
    
  /** methods ================================================================= */
  
  /**
   * Determines the source uri for a publication request.
   * @param request the servlet request
   * @param context the request context
   * @param publisher the publisher
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws ServletException 
   */
  private void determineSourceUri(HttpServletRequest request, RequestContext context, 
      PublicationRequest publicationRequest) throws SQLException, ServletException {
    String uuid = this.determineUuid(request, context, false);
    if ((uuid != null) && (uuid.length() > 0)) {
      publicationRequest.getPublicationRecord().setSourceFileName(uuid+".xml");
      //sourceUri = uuid;
    } else {
      String pathInfo = Val.chkStr(request.getPathInfo());
      String queryString = Val.chkStr(request.getQueryString());
      if (pathInfo.startsWith("/")) pathInfo = pathInfo.substring(1);
      if (queryString.length() > 0) {
        pathInfo = pathInfo+"?"+queryString;
      }
      if (pathInfo.length() > 0) {
        //sourceUri = "userid:"+pubRequest.getPublisher().getLocalID()+"/"+pathInfo;
      }
    }
  }
  
  /**
   * Determines the specified document UIID from the HTTP request URL.
   * @param request the servlet request
   * @param context the request context
   * @param force if true, throw a ServletException if the document UUID is undetermined
   * @return the document UUID
   * @throws SQLException if an exception occurs while communicating with the database
   * @throws ServletException if the document UUID is undetermined (force=true only)
   */
  private String determineUuid(HttpServletRequest request, RequestContext context, boolean force) 
    throws SQLException, ServletException {
    String uuid = "";
    String id = Val.chkStr(request.getParameter("id"));
    if (id.length() == 0) {
      String tmp = Val.chkStr(request.getPathInfo()).replaceAll("^/", "");
      try {
        id = Val.chkStr(URLDecoder.decode(tmp,"UTF-8"));
      } catch (UnsupportedEncodingException e) {
        // will never happen
      }
    }
    
    // determine the document uuid (the supplied id could be a gpt uuid or file identifier)
    if (id.length() > 0) {
      ImsMetadataAdminDao dao = new ImsMetadataAdminDao(context);
      uuid = dao.findUuid(id);
      if (!force && ((uuid == null) || (uuid.length() == 0))) {
        String tmpId = UuidUtil.addCurlies(id);
        if (tmpId.length() == 38) {
          uuid = tmpId;
        }
      }
    }
    
    // throw an exception if the document uuid was not located
    if (force && ((uuid == null) || (uuid.length() == 0))) {
      throw new ServletException("404: Document not found.");
    }
    return uuid;
  }

  /**
   * Deletes a document from the catalog.
   * <br/>An HTTP 200 response code is set when a document is successfully deleted.
   * @param request the servlet request
   * @param response the servlet response
   * @throws ServletException if the request cannot be handled
   * @throws IOException if an I/O error occurs while handling the request 
   */
  @Override
  protected void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    this.execute(request,response,"DELETE");
  }

  /**
   * Gets an XML document from the catalog.
   * <br/>The XML is returned within the body of the HTTP response.
   * @param request the servlet request
   * @param response the servlet response
   * @throws ServletException if the request cannot be handled
   * @throws IOException if an I/O error occurs while handling the request 
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    this.execute(request,response,"GET");
  }
  
  /**
   * Handles a POST request.
   * <br/>Same as doPut().
   * @param request the servlet request
   * @param response the servlet response
   * @throws ServletException if the request cannot be handled
   * @throws IOException if an I/O error occurs while handling the request 
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    this.execute(request,response,"POST");
  }

  /**
   * Publishes an XML document to the catalog.
   * <br/>The XML must be supplied within the body of the HTTP request.
   * <br/>An HTTP 200 response code is set when a document is successfully replaced.
   * <br/>An HTTP 201 response code is set when a document is successfully created.
   * @param request the servlet request
   * @param response the servlet response
   * @throws ServletException if the request cannot be handled
   * @throws IOException if an I/O error occurs while handling the request 
   */
  @Override
  protected void doPut(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    this.execute(request,response,"PUT");
  }

  /**
   * Provides a concrete executeUpdate() method to fulfill the sub-class requirement of the parent
   * BaseServlet class. 
   * This method is never invoked, all work is handled through 
   * doGet(), doPut(), doPost() and doDelete().
   */
  @Override
  protected void execute(HttpServletRequest request, HttpServletResponse response, RequestContext context) 
    throws Exception {
  }
  
  /**
   * Processes the HTTP request.
   * @param request the HTTP request
   * @param response HTTP response
   * @param context request context
   * @param method the method to executeUpdate GET|PUT|POST|DELETE
   * @throws ServletException if the request cannot be handled
   * @throws IOException if an I/O error occurs while handling the request 
   */
  private void execute(HttpServletRequest request, HttpServletResponse response, String method) 
    throws ServletException, IOException {
    
    RequestContext context = null;
    try {
      String msg = "HTTP "+method+", "+request.getRequestURL().toString();
      if ((request.getQueryString() != null) && (request.getQueryString().length() > 0)) {
        msg += "?"+request.getQueryString();
      }
      getLogger().fine(msg);
      
      String sEncoding = request.getCharacterEncoding();
      if ((sEncoding == null) || (sEncoding.trim().length() == 0)) {
        request.setCharacterEncoding("UTF-8");
      }
      context = RequestContext.extract(request);
      
      /// estabish the publisher
      StringAttributeMap params = context.getCatalogConfiguration().getParameters();
      String autoAuthenticate = Val.chkStr(params.getValue("BaseServlet.autoAuthenticate"));
      if (!autoAuthenticate.equalsIgnoreCase("false")) {
        Credentials credentials = getCredentials(request);
        if (credentials != null) {
          this.authenticate(context,credentials);
        }
      }
      Publisher publisher = new Publisher(context);

      // executeUpdate the appropriate action
      if (method.equals("GET")) {
        this.executeGet(request,response,context,publisher);
      } else if (method.equals("POST")) {
        this.executePost(request,response,context,publisher);
      } else if (method.equals("PUT")) {
        this.executePut(request,response,context,publisher);
      } else if (method.equals("DELETE")) {
        this.executeDelete(request,response,context,publisher);
      }
      
    } catch (CredentialsDeniedException e) {
      String sRealm = this.getRealm(context);
      response.setHeader("WWW-Authenticate","Basic realm=\""+sRealm+"\"");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    } catch (NotAuthorizedException e) {
      String sRealm = this.getRealm(context);
      response.setHeader("WWW-Authenticate","Basic realm=\""+sRealm+"\"");
      response.sendError(HttpServletResponse.SC_UNAUTHORIZED); 
    } catch (ValidationException e) {
      String sMsg = e.toString();
      if (sMsg.contains("XSD violation.")) {
        sMsg = "XSD violation.";
      } else if (sMsg.contains("Invalid metadata document.")) {
        sMsg = "Invalid metadata document.";
      } else {
        sMsg = "Invalid metadata document.";
      }
      String json = Val.chkStr(request.getParameter("errorsAsJson"));
      if (json.length()>0) {
      	json = Val.escapeXmlForBrowser(json);
        FacesContextBroker fcb = new FacesContextBroker(request, response);
        MessageBroker msgBroker = fcb.extractMessageBroker();
        
        ArrayList<String> validationMessages = new ArrayList<String>();
        e.getValidationErrors().buildMessages(msgBroker, validationMessages, true);
        
        StringBuilder sb = new StringBuilder();
        sb.append(json).append(" = {\r\n");
        sb.append("message: \"").append(Val.escapeStrForJson(sMsg)).append("\",\r\n");
        sb.append("code: 409,\r\n");
        sb.append("errors: [\r\n");
        for (int i=0; i<validationMessages.size(); i++) {
          if (i>0) {
            sb.append(",\r\n");
          }
          sb.append("\"").append(Val.escapeStrForJson(validationMessages.get(i))).append("\"");
        }
        if (validationMessages.size()>0) {
          sb.append("\r\n");
        }
        sb.append("]}");
        
        LOGGER.log(Level.SEVERE, sb.toString());
        response.getWriter().print(sb.toString());
      } else {
        response.sendError(409,sMsg);
      }
    } catch (ServletException e) {
      String sMsg = e.getMessage();
      int nCode = Val.chkInt(sMsg.substring(0,3),500);
      sMsg = Val.chkStr(sMsg.substring(4));
      String json = Val.chkStr(request.getParameter("errorsAsJson"));
      if (json.length()>0) {
      	json = Val.escapeXmlForBrowser(json);
        StringBuilder sb = new StringBuilder();
        sb.append(json).append(" = {\r\n");
        sb.append("message: \"").append(Val.escapeStrForJson(sMsg)).append("\",\r\n");
        sb.append("code: ").append(nCode).append(",\r\n");
        sb.append("errors: [\r\n");
        sb.append("\"").append(Val.escapeStrForJson(sMsg)).append("\"");
        sb.append("]}");
        
        LOGGER.log(Level.SEVERE, sb.toString());
        response.getWriter().print(sb.toString());
      } else {
        response.sendError(nCode,sMsg);
      }
    } catch (Throwable t) {
      String sMsg = t.toString();
      String json = Val.chkStr(request.getParameter("errorsAsJson"));
      if (json.length()>0) {
      	json = Val.escapeXmlForBrowser(json);
        StringBuilder sb = new StringBuilder();
        sb.append(json).append(" = {\r\n");
        sb.append("message: \"").append(Val.escapeStrForJson(sMsg)).append("\",\r\n");
        sb.append("code: ").append(500).append(",\r\n");
        sb.append("errors: [\r\n");
        sb.append("\"").append(Val.escapeStrForJson(sMsg)).append("\"");
        sb.append("]}");
        
        LOGGER.log(Level.SEVERE, sb.toString());
        response.getWriter().print(sb.toString());
      } else if (sMsg.contains("The document is owned by another user:")) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN,"The document is owned by another user.");
      } else {
        String sErr = "Exception occured while processing servlet request.";
        getLogger().log(Level.SEVERE,sErr,t);
        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            sMsg + sErr);
      }
    } finally {
      if (context != null) context.onExecutionPhaseCompleted();
    }
  }  
  
  /**
   * Deletes the metadata document specified within the URL.
   * @param request the servlet request
   * @param response the servlet response
   * @param context the request context
   * @param publisher the publisher
   * @throws Exception if an exception occurs
   */
  private void executeDelete(HttpServletRequest request, HttpServletResponse response,
      RequestContext context, Publisher publisher) throws Exception {
    String uuid = this.determineUuid(request,context,true);
    MmdActionCriteria actionCriteria = new MmdActionCriteria();
    actionCriteria.setActionKey("delete");
    actionCriteria.getSelectedRecordIdSet().add(uuid);
    MmdResult result = new MmdResult();
    MmdCriteria criteria = new MmdCriteria();
    criteria.setActionCriteria(actionCriteria);
    MmdActionRequest actionRequest = new MmdActionRequest(context,publisher,criteria,result);
    actionRequest.execute();
    LOGGER.finer(result.getActionResult().getNumberOfRecordsModified()+" document(s) deleted.");
  }
  
  /**
   * Gets the XML string associated with the metadata document specified within the URL.
   * @param request the servlet request
   * @param response the servlet response
   * @param context the request context
   * @param publisher the publisher
    * @throws Exception if an exception occurs
   */
  private void executeGet(HttpServletRequest request, HttpServletResponse response,
      RequestContext context, Publisher publisher) throws Exception {
    String uuid = this.determineUuid(request,context,true);
    MetadataDocument mdDoc = new MetadataDocument();
    String xml = Val.chkStr(mdDoc.prepareForDownload(context,publisher,uuid));
    if (xml.length() > 0) {
      this.writeXmlResponse(response,xml);
    } 
  }
  
  /**
   * Publishes the XML metadata document supplied within the request body.
   * <br/>this method is a pass-turu to executePut().
   * @param request the servlet request
   * @param response the servlet response
   * @param context the request context
   * @param publisher the publisher
   * @throws Exception if an exception occurs
   */
  private void executePost(HttpServletRequest request, HttpServletResponse response,
      RequestContext context, Publisher publisher) throws Exception {
    this.executePut(request,response,context,publisher);
  }
  
  /**
   * Publishes the XML metadata document supplied within the request body.
   * @param request the servlet request
   * @param response the servlet response
   * @param context the request context
   * @param publisher the publisher
   * @throws Exception if an exception occurs
   */
  private void executePut(HttpServletRequest request, HttpServletResponse response,
      RequestContext context, Publisher publisher) throws Exception {
    String xml = null;
    HrRecord record = extractRegistrationInfo(request);

    if (record==null) {
      try {
        xml = this.readInputCharacters(request);
      } catch (IOException e) {
        throw new ServletException("400: IOException while reading request body.");
      }
      xml = Val.chkStr(Val.removeBOM(xml));
      if (xml.length() > 0) {
        PublicationRequest pubRequest = new PublicationRequest(context,publisher,xml);
        PublicationRecord pubRecord = pubRequest.getPublicationRecord();
        
        pubRecord.setPublicationMethod(MmdEnums.PublicationMethod.upload.toString());
        String pubMethod = Val.chkStr(request.getParameter("publicationMethod"));
        if (pubMethod.length() > 0) {
          try {
            pubMethod = MmdEnums.PublicationMethod.valueOf(Val.chkStr(pubMethod)).toString();
            pubRecord.setPublicationMethod(pubMethod);
          } catch (IllegalArgumentException ex) {
          }
        }
        
        String asDraft = Val.chkStr(request.getParameter("asDraft"));
        String approve = Val.chkStr(request.getParameter("approve"));
        LOGGER.fine("Approving of uploaded documents through the REST with 'approve' flag: "+approve);
        if (asDraft.equals("true")) {
          pubRecord.setApprovalStatus(MmdEnums.ApprovalStatus.draft.toString());
        } else if (approve.equals("true")) {
          pubRecord.setApprovalStatus(MmdEnums.ApprovalStatus.approved.toString());
          if (!publisher.getIsAdministrator()) {
            throw new NotAuthorizedException("Not authorized.");
          }
        }
        
        this.determineSourceUri(request,context,pubRequest);
        try {
          pubRequest.publish();
          if (!pubRecord.getWasDocumentReplaced()) {
            response.setStatus(HttpServletResponse.SC_CREATED);
          }
//        } catch (ValidationException e) {
//          String sMsg = e.toString();
//          if (sMsg.contains("XSD violation.")) {
//            throw new ServletException("409: XSD violation.");
//          } else if (sMsg.contains("Invalid metadata document.")) {
//            throw new ServletException("409: Document failed to validate.");
//          } else {
//            throw new ServletException("409: Document failed to validate.");
//          }
        } catch (SchemaException e) {
          String sMsg = e.toString();
          if (sMsg.contains("Unrecognized metadata schema.")) {
            throw new ServletException("409: Unrecognized metadata schema.");
          } else if (sMsg.contains("Unable to parse document.")) {
            throw new ServletException("409: Unable to parse document as XML.");
          } else {
            throw e;
          }
        }
      } else {
        throw new ServletException("409: Document was empty.");
      }
    } else {
      try {
        HrCompleteUpdateRequest req = new HrCompleteUpdateRequest(context, record);
        req.execute();
        response.setStatus(HttpServletResponse.SC_CREATED);
      } catch (HrAssertUrlException e) {
        throw new ServletException("409: Duplicated resource URL.");
      } catch (ValidationException e) {
        String sMsg = e.toString();
        if (sMsg.contains("XSD violation.")) {
          throw new ServletException("409: XSD violation.");
        } else if (sMsg.contains("Invalid metadata document.")) {
          throw new ServletException("409: Document failed to validate.");
        } else {
          throw new ServletException("409: Document failed to validate.");
        }
      } catch (SchemaException e) {
        String sMsg = e.toString();
        if (sMsg.contains("Unrecognized metadata schema.")) {
          throw new ServletException("409: Unrecognized metadata schema.");
        } else if (sMsg.contains("Unable to parse document.")) {
          throw new ServletException("409: Unable to parse document as XML.");
        } else {
          throw e;
        }
      } catch (Exception ex) {
        throw new ServletException("409: Unable to register resource.");
      }
    }
  }

  /**
   * Extracts registration info.
   * @param request HTTP request
   * @return registration info or <code>null</code> if unable to extract registration info
   */
  private HrRecord extractRegistrationInfo(HttpServletRequest request) {
    ApplicationContext appCtx = ApplicationContext.getInstance();
    ApplicationConfiguration appCfg = appCtx.getConfiguration();

    HrRecord record = new HrRecord();
    StringAttributeMap attributes = new StringAttributeMap();
    boolean updateContent = true;
    boolean updateDefinition = true;
    boolean autoApprove = true;
    BotsMode robotsTxtMode = BotsMode.getDefault();

    for (String paramName : new EnumerationAdapter<String>(request.getParameterNames())) {
      String paramValue = request.getParameter(paramName);
      if (paramName.equalsIgnoreCase("uuid")) {
        record.setUuid(paramValue);
      }
      if (paramName.equalsIgnoreCase("name")) {
        record.setName(paramValue);
      }
      else if (paramName.equalsIgnoreCase("url")) {
        record.setHostUrl(paramValue);
      }
      else if (paramName.equalsIgnoreCase("soapurl")) {
        attributes.add(new StringAttribute(ArcGISProtocol.SOAP_URL,paramValue));
      }
      else if (paramName.equalsIgnoreCase("protocol")) {
        ProtocolFactory factory = appCfg.getProtocolFactories().get(paramValue);
        if (factory!=null) {
          record.setProtocol(factory.newProtocol());
        }
      }
      else if (paramName.equalsIgnoreCase("frequency")) {
        record.setHarvestFrequency(HarvestFrequency.checkValueOf(paramValue));
      }
      else if (paramName.equalsIgnoreCase("sendNotification")) {
        record.setSendNotification(Val.chkBool(paramValue, false));
      }
      else if (paramName.equalsIgnoreCase("updateContent")) {
        updateContent = Val.chkBool(paramValue, true);
      }
      else if (paramName.equalsIgnoreCase("updateDefinition")) {
        updateDefinition = Val.chkBool(paramValue, true);
      }
      else if (paramName.equalsIgnoreCase("autoApprove")) {
        autoApprove = Val.chkBool(paramValue, true);
      }
      else if (paramName.equalsIgnoreCase("findable")) {
        record.setFindable(Val.chkBool(paramValue, true));
      }
      else if (paramName.equalsIgnoreCase("searchable")) {
        record.setSearchable(Val.chkBool(paramValue, true));
      }
      else if (paramName.equalsIgnoreCase("synchronizable")) {
        record.setSynchronizable(Val.chkBool(paramValue, true));
      }
      else if (paramName.equalsIgnoreCase("robotstxtmode")) {
        robotsTxtMode = BotsMode.parseMode(paramValue);
      }
      else {
        attributes.add(new StringAttribute(paramName,paramValue));
      }
    }

    if (record.getProtocol()==null || record.getProtocol().getKind().equalsIgnoreCase(ProtocolType.None.name())) {
      ProtocolFactory factory = appCfg.getProtocolFactories().get(ProtocolType.RES.name());
      if (factory!=null) {
        record.setProtocol(factory.newProtocol());
      }
    }
    
    if (record.getProtocol()!=null) {
      record.getProtocol().setAttributeMap(attributes);
      ProtocolInvoker.setUpdateDefinition(record.getProtocol(), updateDefinition);
      ProtocolInvoker.setUpdateContent(record.getProtocol(), updateContent);
      ProtocolInvoker.setAutoApprove(record.getProtocol(), autoApprove);
      ProtocolInvoker.setRobotsTxtMode(record.getProtocol(), robotsTxtMode);
    }

    record = record.getName().length()>0 && record.getHostUrl().length()>0 && record.getProtocol()!=null? record: null;

    return record;
  }
}
