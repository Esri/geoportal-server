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
<% // login.jsp - Login page (tiles definition) %>
<%@taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"%>
<%@taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<%
  String oauthUrl = "";
  boolean _chkForOA = true;
  if (_chkForOA) {
		com.esri.gpt.framework.context.RequestContext ctx = com.esri.gpt.framework.context.RequestContext.extract(request);
		com.esri.gpt.framework.security.identity.IdentityAdapter ia = ctx.newIdentityAdapter();
		if (ia instanceof com.esri.gpt.framework.security.identity.agp.PortalIdentityAdapter) {
			com.esri.gpt.framework.security.identity.agp.PortalIdentityAdapter pa = (com.esri.gpt.framework.security.identity.agp.PortalIdentityAdapter)ia;
		  oauthUrl = pa.getAuthorizeUrl();
		  try {
		  	oauthUrl += "?client_id="+java.net.URLEncoder.encode(pa.getAppId(),"UTF-8");
		  	oauthUrl += "&response_type=token";
		  	oauthUrl += "&expiration="+pa.getExpirationMinutes(); 
		  } catch (java.io.UnsupportedEncodingException e) {
		    // should never occur
		    e.printStackTrace();
		  }
		}
  }
%>

<script type="text/javascript">
function _chkoa() {
	var href, oauthUrl = "<%=oauthUrl%>";
	if (oauthUrl.length > 0) {
		href = window.location.href;
		href = href.substring(0,href.indexOf("/catalog/identity/"))+"/catalog/identity/oauthResponse.jsp";
		oauthUrl += "&redirect_uri="+encodeURIComponent(href);
		window.location = oauthUrl;
	}
}
_chkoa();
</script>

<% // initialize the page %>
<gpt:page id="catalog.identity.login"/>
<tiles:insert definition=".gptLayout" flush="false" >
  <tiles:put name="body" value="/catalog/identity/loginBody.jsp"/>
</tiles:insert>