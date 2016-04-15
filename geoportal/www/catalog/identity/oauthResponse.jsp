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
<%
  boolean oauthCheckResponse = true;
  String oauthToken = request.getParameter("t");
	if ((oauthToken != null) && (oauthToken.length() > 0)) {
		oauthCheckResponse = false;
		String oauthUsername = request.getParameter("u");
		com.esri.gpt.framework.context.RequestContext ctx = com.esri.gpt.framework.context.RequestContext.extract(request);
		com.esri.gpt.framework.security.identity.IdentityAdapter ia = ctx.newIdentityAdapter();
		if (ia instanceof com.esri.gpt.framework.security.identity.agp.PortalIdentityAdapter) {
			com.esri.gpt.framework.security.identity.agp.PortalIdentityAdapter pa = (com.esri.gpt.framework.security.identity.agp.PortalIdentityAdapter)ia;
			pa.validateOAuthResponseToken(oauthToken,oauthUsername);
		}
		response.sendRedirect(request.getContextPath());
	}
%>

<script type="text/javascript">
 
 function _chkoa() {
	 
	 var getParam = function(name,q) {
		 var r = new RegExp('[\\?&]'+name+'=([^&#]*)').exec(q);
		 if (!r) return null;
		 return r[1];
	 };
	 
	 var href = window.location.href;
	 href = href.substring(0,href.indexOf("/catalog/identity/"))+"/";
	 
	 var params = {access_token:null, expires_in:null, username:null, ssl: null, error:null, error_description:null};
	 var q = window.location.hash;
	 if ((q != null) && (q.length > 1)) {
		 q = "?"+q.substring(1);
		 params.access_token = getParam("access_token",q);
		 params.expires_in = getParam("expires_in",q);
		 params.username = getParam("username",q);
		 params.ssl = getParam("ssl",q);
		 params.error = getParam("error",q);
		 params.error_description = getParam("error_description",q);
	 }
	 //console.log(params);
	 
	 if (params.error !== null) {
		 if (console && console.error) console.error("[OAuth Error]:",params.error," - ",params.error_description);
		 window.location = href+"catalog/main/home.page";
	 } else if ((params.access_token !== null) && (params.username != null)) {
	   // no need ro re-encode
		 //q = "?t="+encodeURIComponent(params.access_token);
		 //q += "&u="+encodeURIComponent(params.username);
		 q = "?t="+params.access_token;
     q += "&u="+params.username;
		 window.location = href+"catalog/identity/oauthResponse.jsp"+q;
	 } else {
		 if (console && console.error) console.error("[OAuth Error]:", "Invalid Response");
		 window.location = href+"catalog/main/home.page";
	 }
 }
 
 <% if (oauthCheckResponse) { %>
 _chkoa();
 <% } %>
 
</script>
