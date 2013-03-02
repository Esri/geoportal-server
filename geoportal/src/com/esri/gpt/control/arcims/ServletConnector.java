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
package com.esri.gpt.control.arcims;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import com.esri.gpt.catalog.arcims.ImsCatalog;
import com.esri.gpt.catalog.arcims.ImsPermissionDao;
import com.esri.gpt.catalog.context.CatalogConfiguration;
import com.esri.gpt.catalog.discovery.DiscoveryException;
import com.esri.gpt.catalog.management.MmdEnums;
import com.esri.gpt.catalog.publication.ProcessingContext;
import com.esri.gpt.catalog.publication.ProcessorFactory;
import com.esri.gpt.catalog.publication.PublicationRequest;
import com.esri.gpt.catalog.publication.ResourceProcessor;
import com.esri.gpt.catalog.schema.ValidationException;
import com.esri.gpt.catalog.search.SearchException;
import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.http.HttpClientRequest;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.credentials.CredentialsDeniedException;
import com.esri.gpt.framework.security.identity.AuthenticationStatus;
import com.esri.gpt.framework.security.identity.IdentityException;
import com.esri.gpt.framework.security.principal.Publisher;
import com.esri.gpt.framework.util.Val;
import com.esri.gpt.framework.xml.XsltTemplate;
import com.esri.gpt.server.csw.provider.components.OperationResponse;
import com.esri.gpt.server.csw.provider.components.RequestHandler;
import com.esri.gpt.server.csw.provider.local.ProviderFactory;

/**
 * Servlet Connector for an AtcIMS MetadataServer
 */
public class ServletConnector extends BaseServlet {

// class variables =============================================================
private static final Logger LOGGER = Logger.getLogger(ServletConnector.class
                                 .getName());


// instance variables ==========================================================
/** The GPT TO CSW XSLT template *. */
private XsltTemplate axlToCswXsltTemplate;

/** The GPT TO CSW XSLT template *. */
private XsltTemplate cswToAxlXsltTemplate;

// constructors ================================================================

// properties ==================================================================

// methods =====================================================================

/**
 * Handles a GET request.
 * 
 * @param request
 *          the servlet request
 * @param response
 *          the servlet response
 */
@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
  getLogger().finer("Query string=" + request.getQueryString());
  if ("ping".equalsIgnoreCase(request.getParameter("Cmd"))) {
    response.getWriter().write("IMS v9.3.0");
  } else if ("ping".equalsIgnoreCase(request.getParameter("cmd"))) {
    response.getWriter().write("IMS v9.3.0");
  } else if ("getVersion".equalsIgnoreCase(request.getParameter("Cmd"))) {
    response.getWriter().write("Version=9.3.0\nBuild_Number=514.2159");
  } else {
    response.sendError(HttpServletResponse.SC_NOT_FOUND);
  }
  // String axlRequest = readInputCharacters(request);
  // getLogger().finer("axlRequest:\n"+axlRequest);
}

/**
 * Executes a POST request.
 * 
 * @param request
 *          the servlet request
 * @param response
 *          the servlet response
 * @param context
 *          the request context
 */
protected void execute(HttpServletRequest request,
    HttpServletResponse response, RequestContext context) throws Exception {

  // initialize
  CatalogConfiguration catConfig = context.getCatalogConfiguration();
  String sService = getServiceName(request);
  String axlRequest = readInputCharacters(request);
  String axlResponse = "";
  String sSourceUri = "";
  String sServiceNameParam = sService;

  if (sService.indexOf(":EB:") != -1) {
    String s[] = sService.split(":EB:");
    sService = s[0];
    sSourceUri = s[1];
  }

  // ensure that a login request was authenticated
  if (sService.toLowerCase().equals("login")) {
    if (!context.getUser().getAuthenticationStatus().getWasAuthenticated()) {
      throw new CredentialsDeniedException("Invalid credentials.");
    }
  }

  boolean proxyMode = true;
  
  // process the request
  getLogger().finest("axlRequest:\n" + axlRequest);
  if (axlRequest.length() > 0) {
    if ((axlRequest.indexOf("<GETCLIENTSERVICES") != -1)) {
      axlResponse = getClientServices(context);
    } else if ((sService.length() > 0) && !sService.toLowerCase().equals("login")) {

      if (sServiceNameParam.indexOf(":EB:") != -1 && axlRequest.indexOf("<PUT_METADATA") != -1) {
	        String parts[] = axlRequest.split("<!--");
	        String s[] = parts[1].split("-->");
	        String xml = s[0];
	        if (axlRequest.indexOf("<THUMBNAIL>") != -1 && xml.indexOf("<Thumbnail/>") != -1) {
	          xml = setThumbnail(xml, axlRequest);
	          axlResponse = publish(context, xml, sSourceUri);
	        } else if (xml.indexOf("<gptAgsUrl>") != -1) {
	        	axlResponse =  handleAgsUrl(context, xml, sSourceUri);
	       } else {
	    	  axlResponse = publish(context, xml, sSourceUri);
	       }
       }else  if (proxyMode && sServiceNameParam.indexOf(":EB:") == -1){
    	   	axlResponse = makeCswRequestFromAxl(context,request, axlRequest);
      } else {
        throw new Exception("Configuration issue: No ArcXML socket client was created.");
      }
    }
  }

  // write the response
  getLogger().finest("axlResponse:\n" + axlResponse);
  if (axlResponse.length() > 0) {
    writeXmlResponse(response, axlResponse);
  }
}

/**
 * Makes csw requests from axl request and return axl response
 * @param context the RequestContext
 * @param request the HttpServletRequest
 * @param axlRequest the axl request
 * @return axlResponse the axl response
 * @throws SearchException
 * @throws TransformerConfigurationException
 * @throws TransformerException
 * @throws DiscoveryException
 */
public String makeCswRequestFromAxl(RequestContext context,
		HttpServletRequest request, String axlRequest)
		throws SearchException, TransformerConfigurationException,
		TransformerException, DiscoveryException {

	String axlResponse = "";
	if (axlRequest.indexOf("<GET_METADATA><GET_METADATA_DOCUMENT") != -1 
			|| axlRequest.indexOf("<SEARCH_METADATA") != -1) {

		XsltTemplate template = getAxlToCswXsltTemplate();
		Map<String, String> params = new HashMap<String, String>();
		if (axlRequest.contains("{thisHHHH-isHH-aHHH-dumm-ydocidHHHhhh}")) {
			params.put("all", "all");
		}

		int start = axlRequest.indexOf("startresult=\"");
		int end = 0;
		String parts = null;		
		if(start != -1){
		   end = axlRequest.indexOf("\"",start+13);
		   parts = axlRequest.substring(start+13,end);
		}
		
		start = axlRequest.indexOf("maxresults=\"");
		String max = null;
		if(start != -1){
		   end = axlRequest.indexOf("\"",start+12);
		   max =  axlRequest.substring(start+12,end);
		}

		String cswRequest = template.transform(axlRequest, params);
		getLogger().finest(" AXL2CSW transformed request : " + cswRequest);
		String cswResponse = "";
    try {
      RequestHandler handler = ProviderFactory.newHandler(context);
      OperationResponse resp = handler.handleXML(cswRequest);
      cswResponse = resp.getResponseXml();
    } catch (Exception e) {
      throw new SearchException(e);
    }
    
		getLogger().finest(" CSW response : " + cswResponse);
		String metadataUrl = "/csw?service=CSW&request=GetRecordById&version=2.0.2&ElementSetName=full&outputSchema=original&ID=";
		String requestUrl = request.getRequestURL().toString();
		String contextPath = request.getContextPath();
		String baseUrl = requestUrl.substring(0, requestUrl.indexOf(contextPath));
		metadataUrl = baseUrl + contextPath + metadataUrl;
		
		params = new HashMap<String, String>();
		params.put("partialMetadataUrl", metadataUrl);
		params.put("partialThumbnailUrl", baseUrl);
		
		if (axlRequest.indexOf("<GET_METADATA><GET_METADATA_DOCUMENT") != -1) {
			start = axlRequest.indexOf("docid=\"");
			end = axlRequest.indexOf("/></GET_METADATA>");
			String docid = axlRequest.substring(start + 7, end).trim();
			docid = docid.substring(0, docid.length() - 1);
			String url = "/csw?service=CSW&request=GetRecordById&version=2.0.2&ElementSetName=full&outputSchema=original&ID="
					+ docid;
			params.put("metadataUrl", url);
		}
		
		if(parts != null && parts.length() > 0){
			params.put("startResult", parts);
		}		
		if(max != null && max.length() > 0){
			params.put("maxResults", max);
		}

		template = getCswToAxlXsltTemplate();		
		axlResponse = template.transform(cswResponse, params);
		
	} else if (axlRequest.indexOf("<GET_METADATA><GET_ROOT_DATASET") != -1) {
		StringBuffer sb = new StringBuffer();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
		.append(
				"<ARCXML version=\"1.1\"><RESPONSE><METADATA numresults=\"17\" startresult=\"0\" total=\"17\">")
		.append(
				"<METADATA_DATASET name=\"Geoportal\" docid=\"{thisHHHH-isHH-aHHH-dumm-ydocidHHHhhh}\" children=\"true\" siblings=\"false\" folder=\"true\" private=\"false\">")
		.append(
				"<ENVELOPE minx=\"-180\" miny=\"-90\" maxx=\"180\" maxy=\"90.000000083819\" /></METADATA_DATASET></METADATA></RESPONSE></ARCXML>");
		axlResponse = sb.toString();
	}
	return axlResponse;
}



/**
 * Handle Ags url enpoints to publish catalog services
 * @param context the RequestContext
 * @param xml the xml string
 * @param sSourceUri the source Uri
 * @return the axl response
 * @throws Exception
 */
public String handleAgsUrl(RequestContext context, String xml,
		String sSourceUri) throws Exception {
	String axlResponse = "";

	String url = xml.substring(xml.indexOf("<gptAgsUrl>") + 11,
			xml.indexOf("</gptAgsUrl>")).trim();

	if (url != null && url.length() > 0){
		Publisher publisher = new Publisher(context);
        HttpClientRequest httpClient = HttpClientRequest.newRequest();
        ProcessingContext pContext = new ProcessingContext(context,publisher,httpClient,null,false);
        ProcessorFactory factory = new ProcessorFactory();
        ResourceProcessor processor = factory.interrogate(pContext,url);
        if (processor == null) {
        	axlResponse = "Unable to process resource.";
        	return axlResponse;
        }
        processor.setPublicationMethod(MmdEnums.PublicationMethod.batch.toString());
        processor.process();
        
        int numCreated = pContext.getNumberCreated();
		    int numReplaced = pContext.getNumberReplaced();
		    int numFailed = pContext.getNumberFailed();
		    int numDeleted = pContext.getNumberDeleted();
		    int numUnchanged = pContext.getNumberUnchanged();
		
        StringBuffer sb = new StringBuffer();
			sb
					.append(
							"<?xml version=\"1.0\" encoding=\"UTF-8\"?><ARCXML version=\"1.1\">")
					.append("<RESPONSE><METADATA_ACTION>").append(
							"Publication Summary : ").append(numCreated)
					.append(" created, ").append(numReplaced).append(
							" replaced and ").append(numFailed).append(
							" failed").append(
							"</METADATA_ACTION></RESPONSE></ARCXML>");
			axlResponse = sb.toString();
	} else {
		axlResponse = "Error:ArcGIS Server/Service url is invalid.";
	}
	return axlResponse;
}

/**
 * Gets the axl to csw xslt template.
 * 
 * @return the axl to csw xslt template
 * 
 * @throws SearchException
 *             the search exception
 * @throws Searchception
 *             xlst template not initialized by configuration
 */
public XsltTemplate getAxlToCswXsltTemplate() throws SearchException {
  if(this.axlToCswXsltTemplate != null) {
    return this.axlToCswXsltTemplate;
  }   
  String path = "gpt/search/axl2csw.xslt";
  Exception tmpltException = null;
    if(this.axlToCswXsltTemplate != null) {
      return this.axlToCswXsltTemplate;
    }
    try {
      this.axlToCswXsltTemplate = XsltTemplate.makeTemplate(path);
    } catch (TransformerConfigurationException e) {
      tmpltException = e;
    }
  if(tmpltException != null) {
    throw new SearchException("Could not make xslt template from path " +
    		path , tmpltException );
  }
    
  return this.axlToCswXsltTemplate;
}

/**
 * Gets the axl to csw xslt template.
 * 
 * @return the axl to csw xslt template
 * 
 * @throws SearchException the search exception
 * @throws Searchception xlst template not initialized by configuration
 */
public XsltTemplate getCswToAxlXsltTemplate() throws SearchException {
  if(this.cswToAxlXsltTemplate != null) {
    return this.cswToAxlXsltTemplate;
  }
  String path = "gpt/search/csw2axl.xslt";
  Exception tmpltException = null;
    if(this.cswToAxlXsltTemplate != null) {
      return this.cswToAxlXsltTemplate;
    }
    try {
      this.cswToAxlXsltTemplate = XsltTemplate.makeTemplate(path);
    } catch (TransformerConfigurationException e) {
      tmpltException = e;
    }
  if(tmpltException != null) {
    throw new SearchException("Could not make xslt template from path " +
    		path , tmpltException );
  }    
  return this.cswToAxlXsltTemplate;
}

/**
 * Executes a publish request for GPT publish client tool
 * 
 * @param context
 *          the request context
 * @param xml
 *          the metadata xml string
 * @param sourceUri
 *          the source uri for metadata document
 * @return axlResponse the response message for the publication request
 */
private String publish(RequestContext context, String xml, String sourceUri) {
  String axlResponse = "";
  getLogger().finer("Intercepting publication request...");
  try {

    // prepare the publisher
    Publisher publisher = new Publisher(context);

    // publication request
    PublicationRequest publishRequest = new PublicationRequest(context,
        publisher, xml);
    publishRequest.getPublicationRecord().setSourceFileName(sourceUri);
    publishRequest.getPublicationRecord().setPublicationMethod(
        MmdEnums.PublicationMethod.batch.toString());
    publishRequest.publish();

    boolean bReplaced = publishRequest.getPublicationRecord()
        .getWasDocumentReplaced();
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("<ARCXML version=\"1.1\"><RESPONSE><METADATA_ACTION>");
    if (bReplaced) {
      sb.append("REPLACED");
    } else {
      sb.append("OK");
    }
    sb.append("</METADATA_ACTION></RESPONSE></ARCXML>");
    axlResponse = sb.toString();

  } catch (IdentityException e) {
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb
        .append("<ARCXML version=\"1.1\"><RESPONSE><ERROR>Error:Authentication failed.</ERROR></RESPONSE></ARCXML>");
    axlResponse = sb.toString();

  } catch (ValidationException e) {
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("<ARCXML version=\"1.1\"><RESPONSE><ERROR>");

    boolean bProcessMessages = true;
    if (bProcessMessages) {
      MessageBroker messageBroker = new MessageBroker();
      messageBroker.setBundleBaseName("gpt.resources.gpt");
      ArrayList<String> messages = new ArrayList<String>();
      e.getValidationErrors().buildMessages(messageBroker, messages, true);
      sb.append("ValidationError:Metadata publication failed.");
      boolean bFirst = true;
      for (String error : messages) {
        sb.append("<VALIDATIONERROR><![CDATA[");
        if (bFirst)
          sb.append("(").append(e.getKey()).append(") ");
        sb.append(error).append("]]></VALIDATIONERROR>");
        bFirst = false;
      }
    } else {
      String error = Val.chkStr(e.getMessage());
      sb.append("<![CDATA[ValidationError:Metadata publication failed. "
          + error + "]]>");
    }
    sb.append("</ERROR></RESPONSE></ARCXML>");
    axlResponse = sb.toString();

  } catch (Throwable e) {
    String error = Val.chkStr(e.getMessage());
    StringBuffer sb = new StringBuffer();
    sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    sb.append("<ARCXML version=\"1.1\"><RESPONSE><ERROR>");
    sb.append("<![CDATA[Error:Metadata publication failed. " + error + "]]>");
    sb.append("</ERROR></RESPONSE></ARCXML>");
    axlResponse = sb.toString();
  }
  return axlResponse;
}

/**
 * Executes a publish request for GPT publish client tool
 * 
 * @param xml
 *          the metadata xml string
 * @param axlRequest
 *          the ArcXML publish metadata request
 * @return newXml the modified metadata xml string
 */
private String setThumbnail(String xml, String axlRequest) {
  String[] parts = axlRequest.split("<THUMBNAIL>");
  String[] s = parts[1].split("</THUMBNAIL>");
  String newXml = xml.replace("<Thumbnail/>",
      "<Thumbnail><Data EsriPropertyType=\"Picture\">" + s[0]
          + "</Data></Thumbnail>");
  return newXml;
}

/**
 * Returns the GETCLIENTSERVICES axl.
 * 
 * @param context
 *          the request context
 * @return the GETCLIENTSERVICES axl
 */
private String getClientServices(RequestContext context) {
  ImsCatalog catalog = context.getCatalogConfiguration().getArcImsCatalog();
  AuthenticationStatus authStatus = context.getUser().getAuthenticationStatus();
  boolean bIsAdministrator = authStatus.getAuthenticatedRoles().hasRole(
      "gptAdministrator");
  boolean bIsPublisher = authStatus.getAuthenticatedRoles().hasRole(
      "gptPublisher");

  StringBuffer sb = new StringBuffer();
  sb.append("<ARCXML version=\"1.0\">\n");
  sb.append("<RESPONSE><SERVICES>");

  String sService = catalog.getBrowseService().getServiceName();
  if (sService.length() > 0) {
    String sRole = "metadata_browser_all";
    sb.append("<SERVICE ACCESS=\"PUBLIC\" DESC=\"\"");
    sb.append(" NAME=\"").append(sService).append("\"");
    sb.append(" SERVICEGROUP=\"MetadataServer1\" STATUS=\"ENABLED\"");
    sb.append(" roles=\"").append(sRole).append("\"");
    sb.append(" TYPE=\"MetadataServer\" VERSION=\"\" group=\"*\">");
    sb.append("<IMAGE TYPE=\"xml,gnd,jpg\"/>");
    sb
        .append("<ENVIRONMENT><LOCALE country=\"US\" language=\"en\" variant=\"\"/><UIFONT name=\"Arial\"/></ENVIRONMENT>");
    sb.append("<CLEANUP INTERVAL=\"10\"/>");
    sb.append("</SERVICE>");
  }

  sService = catalog.getPublishService().getServiceName();
  if ((sService.length() > 0) && (bIsAdministrator || bIsPublisher)) {
    String sRole = ImsPermissionDao.ROLE_PUBLISHER;
    if (bIsAdministrator)
      sRole = ImsPermissionDao.ROLE_ADMINISTRATOR;
    sb.append("<SERVICE ACCESS=\"PUBLIC\" DESC=\"\"");
    sb.append(" NAME=\"").append(sService).append("\"");
    sb.append(" SERVICEGROUP=\"MetadataServer1\" STATUS=\"ENABLED\"");
    sb.append(" roles=\"").append(sRole).append("\"");
    sb.append(" TYPE=\"MetadataServer\" VERSION=\"\" group=\"*\">");
    sb.append("<IMAGE TYPE=\"xml,gnd,jpg\"/>");
    sb
        .append("<ENVIRONMENT><LOCALE country=\"US\" language=\"en\" variant=\"\"/><UIFONT name=\"Arial\"/></ENVIRONMENT>");
    sb.append("<CLEANUP INTERVAL=\"10\"/>");
    sb.append("</SERVICE>");
  }

  sb.append("</SERVICES></RESPONSE></ARCXML>");
  getLogger().finer("GETCLIENTSERVICES response:\n" + sb.toString());
  return sb.toString();
}

/**
 * Gets the logger.
 * 
 * @return the logger
 */
@Override
protected Logger getLogger() {
  return LOGGER;
}

/**
 * Gets the service name from the servlet request.
 * 
 * @param request
 *          the servlet request
 * @return the service name
 */
protected String getServiceName(HttpServletRequest request) {
  String sServiceName = "";
  String sQuery = Val.chkStr(request.getQueryString());
  int nIdx = sQuery.toLowerCase().indexOf("servicename=");
  if (nIdx != -1) {
    String sTmp = Val.chkStr(sQuery.substring(nIdx + 12));
    nIdx = sTmp.indexOf("&");
    if (nIdx == -1) {
      sServiceName = sTmp;
    } else if (nIdx > 0) {
      sServiceName = Val.chkStr(sTmp.substring(0, nIdx));
    }
  }
  return sServiceName;
}



}
