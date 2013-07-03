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
<% // manageUserRoleBody.jsp - manage user roles (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>


<%
    com.esri.gpt.framework.jsf.MessageBroker umMsgBroker = com.esri.gpt.framework.jsf.PageContext.extractMessageBroker();
	String umContextPath = request.getContextPath();	
	com.esri.gpt.framework.context.RequestContext umContext = com.esri.gpt.framework.context.RequestContext.extract(request);
	com.esri.gpt.catalog.context.CatalogConfiguration umCatalogCfg = umContext.getCatalogConfiguration();
	com.esri.gpt.framework.collection.StringAttributeMap umParameters = umCatalogCfg.getParameters();
	boolean umHasDeleteUserButton = false;	
	boolean umHasManageUserLink = false;
	if(umParameters.containsKey("ldap.identity.manage.userRoleEnabled")){	
		String umHasManageUserLinkEnabled = com.esri.gpt.framework.util.Val.chkStr(umParameters.getValue("ldap.identity.manage.userRoleEnabled"));
		umHasManageUserLink = Boolean.valueOf(umHasManageUserLinkEnabled);
	}
	
	if(umHasManageUserLink && umParameters.containsKey("ldap.identity.user.deleteEnabled")){	
		String umDeleteUserButtonEnabled = com.esri.gpt.framework.util.Val.chkStr(umParameters.getValue("ldap.identity.user.deleteEnabled"));
		umHasDeleteUserButton = Boolean.valueOf(umDeleteUserButtonEnabled);
	}
	
	String umSearchResults = umMsgBroker.retrieveMessage("catalog.manage.user.role.searchResults");
	String umSearchUser = umMsgBroker.retrieveMessage("catalog.manage.user.role.searchUser");
	String umSearch = umMsgBroker.retrieveMessage("catalog.manage.user.search");
	String umListAGroup = umMsgBroker.retrieveMessage("catalog.manage.user.listAGroup");
	String VER124 = "v1.2.4";
%>

<script type="text/javascript" src="<%=request.getContextPath()+"/catalog/js/" +VER124+ "/gpt-identity-users.js"%>"></script>
<script type="text/javascript">
var umMain = null;
function umInit() {
	umMain = new gpt.identity.Users();        
	umMain.contextPath = "<%=umContextPath %>";
	umMain.resources.addRole = "<%=umMsgBroker.retrieveMessage("catalog.manage.user.role.add")%>";
	umMain.resources.removeRole = "<%=umMsgBroker.retrieveMessage("catalog.manage.user.role.remove")%>";
	umMain.resources.memberOf = "<%=umMsgBroker.retrieveMessage("catalog.manage.user.role.memberOf")%>";
	umMain.resources.configurableRoles = "<%=umMsgBroker.retrieveMessage("catalog.manage.user.role.configurableRoles")%>";
	umMain.resources.userAttributes = "<%=umMsgBroker.retrieveMessage("catalog.manage.user.role.userAttributes")%>";
	umMain.resources.deleteUser = "<%=umMsgBroker.retrieveMessage("catalog.manage.user.role.deleteUser")%>";
	umMain.resources.deleteUserConfirmation = "<%=umMsgBroker.retrieveMessage("catalog.manage.user.role.deleteUserConfirmation")%>";
	umMain.resources.addConfirmation = "<%=umMsgBroker.retrieveMessage("catalog.manage.user.role.addConfirmation")%>";
	umMain.resources.removeConfirmation = "<%=umMsgBroker.retrieveMessage("catalog.manage.user.role.removeConfirmation")%>";
	umMain.resources.selfDeleteUserConfirmation = "<%=umMsgBroker.retrieveMessage("catalog.identity.deleteUser.self")%>";
	umMain.resources.selfAddConfirmation = "<%=umMsgBroker.retrieveMessage("catalog.identity.addRole.self")%>";
	umMain.resources.selfRemoveConfirmation = "<%=umMsgBroker.retrieveMessage("catalog.identity.removeRole.self")%>";
	umMain.resources.searchResultsSummary = "<%=umSearchResults %>";
	umMain.resources.searchUser = "<%=umSearchUser %>";
	umMain.resources.searchResults = "<%=umSearchResults %>";
	umMain.resources.search = "<%=umSearch %>";
	umMain.resources.listAGroup = "<%=umListAGroup%>";
	umMain.hasDeleteUser = <%=umHasDeleteUserButton%>;
	umMain.init();
}

if (typeof(dojo) != 'undefined') {
    dojo.addOnLoad(umInit);
}  

</script>


<f:verbatim>
   <div id="users-div">      	  
   </div>
</f:verbatim>






