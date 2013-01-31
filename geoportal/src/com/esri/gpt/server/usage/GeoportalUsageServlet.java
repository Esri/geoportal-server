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
package com.esri.gpt.server.usage;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esri.gpt.framework.context.BaseServlet;
import com.esri.gpt.framework.context.RequestContext;
import com.esri.gpt.framework.jsf.FacesContextBroker;
import com.esri.gpt.framework.jsf.MessageBroker;
import com.esri.gpt.framework.security.credentials.UsernamePasswordCredentials;
import com.esri.gpt.framework.security.identity.NotAuthorizedException;
import com.esri.gpt.framework.security.principal.RoleSet;
import com.esri.gpt.framework.security.principal.User;
import com.esri.gpt.framework.util.HttpRequestUtil;
import com.esri.gpt.server.usage.api.GeoportalUsageInformation;
import com.esri.gpt.server.usage.api.StatisticsRequestContext;
import com.esri.gpt.server.usage.api.StatisticsTypes;
import com.esri.gpt.server.usage.factories.GeoportalStatisticsProviderFactory;
import com.esri.gpt.server.usage.factories.StatisticsWriterFactory;

/**
 * Geoportal Usage servlet.
 * Provides Geoportal usage information. 
 */
public class GeoportalUsageServlet extends BaseServlet {

// class variables =============================================================
private MessageBroker msgBroker = null;
	
/** Serialization key */
private static final long serialVersionUID = 1L;

// constructors ================================================================

/**
 * Creates instance of the servlet.
 */
public GeoportalUsageServlet() {}

// properties ==================================================================

// methods =====================================================================
/**
 * Initializes servlet.
 * @param config servlet configuration
 * @throws ServletException if error initializing servlet
 */
@Override
public void init(ServletConfig config) throws ServletException {
  super.init(config);  
}

/**
 * Process the HTTP request.
 * @param request HTTP request.
 * @param response HTTP response.
 * @param context request context
 * @throws ServletException if error invoking command.
 * @throws IOException if error writing to the buffer.
 */
@SuppressWarnings("unused")
@Override
protected void execute(HttpServletRequest request,
                     HttpServletResponse response,
                     RequestContext context)
  throws Exception {
    msgBroker = new FacesContextBroker(request,response).extractMessageBroker();    
    StatisticsRequestContext statRequestCtx = null;
    String homePage = "/catalog/main/home.page";
	String contextPath = request.getContextPath();   
	checkRole(context);
	statRequestCtx = new StatisticsRequestContext();
	initStatisticsRequestContext(request,response,context, statRequestCtx);		
	GeoportalStatisticsProviderFactory geoportalStatProviderFactory = new GeoportalStatisticsProviderFactory();
	StatisticsWriterFactory statisticsWriterFactory = new StatisticsWriterFactory();		
	statisticsWriterFactory.makeStatisticsWriter(statRequestCtx);
	GeoportalUsageInformation usageInfo = geoportalStatProviderFactory.buildUsageReport(statRequestCtx);		
 }

/**
 * Collects the statistics request parameters
 * @param request the http servlet request
 * @param response the http servlet response
 * @param context the request context
 * @param statRequestCtx the statistics request context
 * @throws Exception if exception occurs
 */
private void initStatisticsRequestContext(HttpServletRequest request,
	HttpServletResponse response, RequestContext context,StatisticsRequestContext statRequestCtx)
	throws Exception {	
    statRequestCtx.setRequestContext(context);
    statRequestCtx.setRequest(request);
    statRequestCtx.setResponse(response);
    statRequestCtx.setStatHeaderParams(HttpRequestUtil.collectHeader(request));
	statRequestCtx.setStatQueryParams(HttpRequestUtil.collectQuery(request));
	String[] parts = request.getRequestURI().toString().split("/");
	statRequestCtx.setRestUriParts(parts);
	if(parts.length >= 5 && parts[4].equalsIgnoreCase(StatisticsTypes.HARVESTER.toString())){
		statRequestCtx.setRequestType(StatisticsTypes.HARVESTER.toString());
	}else if(parts.length >= 3){
		statRequestCtx.setRequestType(StatisticsTypes.CATALOG.toString());
	}else{		
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "{ \"error\":\"Invalid request.\"}");
		return;
	}	
}

/** 
 * Constructs a administrator based upon the user associated with the 
 * current request context.
 * @param context the current request context (contains the active user)
 * @throws NotAuthorizedException if the user does not have publishing rights
 */
private void checkRole(RequestContext context) 
  throws NotAuthorizedException {
  
  // initialize
  User user = context.getUser();
  user.setKey(user.getKey());
  user.setLocalID(user.getLocalID());
  user.setDistinguishedName(user.getDistinguishedName());
  user.setName(user.getName());
  
  // establish credentials
  UsernamePasswordCredentials creds = new UsernamePasswordCredentials();
  creds.setUsername(user.getName());
  user.setCredentials(creds);
  
  user.setAuthenticationStatus(user.getAuthenticationStatus());  
  assertAdministratorRole(user);
}

/**
 * Asserts the administrator role.
 * @throws NotAuthorizedException if the administrator role has not been granted
 */
private void assertAdministratorRole(User user) throws NotAuthorizedException {
  RoleSet roles = user.getAuthenticationStatus().getAuthenticatedRoles();
  roles.assertRole("gptAdministrator");
}

}
