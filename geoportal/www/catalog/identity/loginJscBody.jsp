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
<% // loginJscBody.jsp - j_secutity_check based login page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<% // check for a single sign on login error %>
<% boolean jscErr = com.esri.gpt.framework.util.Val.chkStr(
   request.getParameter("error")).equalsIgnoreCase("true");
   if (jscErr) {
     String key = "catalog.identity.login.err.denied";
     String msg = com.esri.gpt.framework.jsf.PageContext.extract().getResourceMessage(key);
     msg = "<p class=\"errorMessage\">"+msg+"</p>";
     out.println(msg);
   }
%>

<h:form id="frmLogin" styleClass="fixedWidth" onsubmit="return loginOnSubmit()" >
<h:inputHidden value="#{LoginController.prepareView}"/>

<% // submit to the j_security_check proxy form %>
<f:verbatim>
<script>
function loginOnSubmit() {
  var elForm = document.getElementById("frmLogin");
  var elProxy = document.getElementById("jscProxy");
  if ((elForm != null) && (elProxy != null)) {
    document.getElementById("j_username").value = document.getElementById("frmLogin:userN").value;
    document.getElementById("j_password").value = document.getElementById("frmLogin:userP").value;
    elProxy.submit();
  }
  return false;
}
</script>
</f:verbatim>

<% // include the login parameters (username / password / submit button) %>
<jsp:include page="/catalog/identity/loginParameters.jsp" />
  
</h:form>

<% // j_security_check proxy form %>
<f:verbatim>
<form id="jscProxy" method="post" action="j_security_check">
  <input type="hidden" id="j_username" name="j_username"/>
  <input type="hidden" id="j_password" name="j_password"/>
</form>
</f:verbatim>

