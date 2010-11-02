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
<% // securityManagerBody.jsp - sdi.suite security manager page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%
  String sdisuiteUrl = "";
  String samlToken = "";
  com.esri.gpt.sdisuite.IntegrationContextFactory icf  = new com.esri.gpt.sdisuite.IntegrationContextFactory();
  if (icf.isIntegrationEnabled()) {
    com.esri.gpt.sdisuite.IntegrationContext ic = icf.newIntegrationContext();
    if (ic != null) {
      samlToken = ic.getBase64EncodedToken(
          com.esri.gpt.framework.context.RequestContext.extract(request).getUser());
      if (samlToken != null) {
        sdisuiteUrl = com.esri.gpt.framework.util.Val.chkStr(icf.getSecurityManagerUrl());
      }
    }
  }
%>
<h:form id="frmSdisuiteSecurityManager">

<f:verbatim>
<script type="text/javascript" >
function launchSdiSuiteComponent() {
  var sdisuiteUrl = "<%=sdisuiteUrl%>";
  if ((sdisuiteUrl != null) && (sdisuiteUrl.length > 0)) {
    var elForm = document.getElementById("frm-sdisuite-launch");
    var elIFrame = document.getElementById("ifrm-sdisuite-launch");
    if ((elForm != null) && (elIFrame != null)) {
      <% if ((samlToken != null) && (samlToken.length() > 0)) { %>
      elForm.ticket.value = "<%=samlToken%>";
      <% } %>
      elForm.action = sdisuiteUrl;
      elForm.submit();
    }
  }
}
dojo.addOnLoad(launchSdiSuiteComponent);
</script>
<iframe id="ifrm-sdisuite-launch" name="ifrm-sdisuite-launch" 
  width="100%" height="600px" src=""></iframe>
<form id="frm-sdisuite-launch-tmp"></form>
<form id="frm-sdisuite-launch" name="frm-sdisuite-launch" 
  action="none.html" method="post" target="ifrm-sdisuite-launch">
  <input type="hidden" id="ticket" name="ticket" value=""/>
</form>
</f:verbatim>
  
</h:form>
