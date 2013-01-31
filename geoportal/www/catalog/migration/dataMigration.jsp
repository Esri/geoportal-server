<%--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%// dataMigration.jsp - migrate data from Geoportal 9.3.x to 10 database.%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="ISO-8859-1"%>
<%@page import="java.io.PrintWriter"%>
<%@page import="com.esri.gpt.framework.context.RequestContext"%>
<%@page import="com.esri.gpt.framework.security.principal.Publisher"%>
<%@page import="com.esri.gpt.framework.security.identity.NotAuthorizedException"%>
<%@page import="com.esri.gpt.migration.to1.DataMigration"%>

	<%
		RequestContext context = null;
		try{
			context = RequestContext.extract(request);
			Publisher p = new Publisher(context);
			if (p.getIsAdministrator()) {
				PrintWriter out2 = response.getWriter();	
				DataMigration dm = new DataMigration();
				dm.migrateData(context,out2);	
			} else {
				response.sendRedirect("../main/home.page");
			}		
		}catch(NotAuthorizedException e){
			response.sendRedirect("../main/home.page");
		}finally{
			if(context != null)
				context.getConnectionBroker().closeAll();
		}
	%>



