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
<% // reviewBody.jsp - review resource(JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<%
  String vmdUuid = request.getParameter("uuid");
  String asnErr = com.esri.gpt.framework.util.Val.chkStr(request.getParameter("err"));
  asnErr = com.esri.gpt.framework.util.Val.escapeSingleQuotes(asnErr);
  com.esri.gpt.server.assertion.AsnFactory asnFactory = 
    com.esri.gpt.server.assertion.AsnFactory.newFactory(null);
  com.esri.gpt.server.assertion.AsnConfig asnConfig = asnFactory.getConfiguration();
  com.esri.gpt.framework.security.principal.User asnUser = 
    com.esri.gpt.framework.context.RequestContext.extract(request).getUser();
  com.esri.gpt.framework.security.identity.open.OpenProviders asnOpenProviders = 
    com.esri.gpt.framework.context.RequestContext.extract(request).getIdentityConfiguration().getOpenProviders();
  com.esri.gpt.framework.jsf.MessageBroker asnMsgBroker = 
    com.esri.gpt.framework.jsf.PageContext.extractMessageBroker();
  boolean asnUserWasAuthenticated = asnUser.getAuthenticationStatus().getWasAuthenticated();
  boolean asnHasErr = (asnErr.length() > 0);
%>


<%@page import="com.esri.gpt.framework.util.Val"%><h:form id="mdReview" styleClass="mdReview">

<script type="text/javascript">
  dojo.require("dojox.html.entities");
  function asnAddErr(sErr) {
    var el = dojo.byId("asn-err");
    el.appendChild(document.createTextNode(sErr));
    el.style.display = "block";
  }
  
  function asnAddOp(sName,sIcon,sHRef,sLabel) {
    var elDiv = dojo.byId("asn-openid");
    if ((sIcon != null) && (sIcon.length > 0)) {
      var elImg = document.createElement("img");
      elImg.src = sIcon;
      elImg.title = sLabel;
      elImg.alt = sLabel;
      elDiv.appendChild(elImg);
    } 
    var el = document.createElement("a");
    var txt = sName;
    var op = sName;
    if (sHRef.indexOf("?") == -1) sHRef += "?"; else sHRef += "&";
    sHRef += "fwd="+encodeURIComponent(window.location);
    el.setAttribute("href",sHRef);
    el.appendChild(document.createTextNode(sLabel));
    elDiv.appendChild(el);
  }
  
  function asnAddOpPrompt(sPrompt) {
    var elDiv = dojo.byId("asn-openid");
    var el = document.createElement("span");
    el.appendChild(document.createTextNode(sPrompt));
    elDiv.appendChild(el);
  }
   
  function asnInit() {
    var asnMain = new AsnMain();
    asnMain.enabled = <%=asnConfig.getAreAssertionsEnabled()%>;
    asnMain.baseContextPath = "<%=request.getContextPath()%>/assertion";
    asnMain.imagesPath = "<%=request.getContextPath()%>/catalog/images";
    try{
    	asnMain.resourceId = dojox.html.entities.decode("<%=vmdUuid%>");
    }catch(error){
    	console.log("Invalid uuid parameter value.")
    }
    <% if (asnHasErr) { 
      out.println("    asnAddErr('"+asnErr+"');"); 
    } %>
    if (asnMain.enabled && (asnMain.resourceId != null) && (asnMain.resourceId != "null")) {
      <% 
      if (!asnUserWasAuthenticated && (asnOpenProviders != null) && (asnOpenProviders.size() > 0)) { 
        String sPrompt = asnMsgBroker.retrieveMessage("catalog.openProvider.prompt");
        sPrompt = com.esri.gpt.framework.util.Val.escapeSingleQuotes(sPrompt);
        out.println("      asnAddOpPrompt('"+sPrompt+"');");
        for (com.esri.gpt.framework.security.identity.open.OpenProvider asnOp: asnOpenProviders.values()) {
          String sName = com.esri.gpt.framework.util.Val.escapeSingleQuotes(asnOp.getName());
          String sLabel = com.esri.gpt.framework.util.Val.chkStr(asnOp.getName());
          String sResKey = com.esri.gpt.framework.util.Val.chkStr(asnOp.getResourceKey());
          if (sResKey.length() > 0) {
            sLabel = asnMsgBroker.retrieveMessage(sResKey);
          }
          sLabel = com.esri.gpt.framework.util.Val.escapeSingleQuotes(sLabel);
          String sIcon = com.esri.gpt.framework.util.Val.chkStr(asnOp.getIconUrl());
          if (sIcon.length() > 0) {
            if (!sIcon.startsWith("http") && !sIcon.startsWith("/")) {
              sIcon = request.getContextPath()+"/catalog/images/"+sIcon;
            }
          }
          sIcon = com.esri.gpt.framework.util.Val.escapeSingleQuotes(sIcon);
          String sHRef = request.getContextPath()+"/openid";
          sHRef += "?op="+java.net.URLEncoder.encode(asnOp.getName(),"UTF-8");
          sHRef = com.esri.gpt.framework.util.Val.escapeSingleQuotes(sHRef);
          
          out.println("      asnAddOp('"+sName+"','"+sIcon+"','"+sHRef+"','"+sLabel+"');");  
        }
      } 
      %>
      asnMain.loadResources();
    }
  }
  if (typeof(dojo) != 'undefined') {
    dojo.addOnLoad(asnInit);
  }
</script>

<% // error section %>
<f:verbatim>
  <div id="asn-err" class="errorMessage" style="display:none;"></div>
</f:verbatim>

<% // openid login section %>
<f:verbatim>
  <div id="asn-openid" class="asn-openid"></div>
</f:verbatim>

<% // up/down rating section %>
<f:verbatim>
  <div id="asn-rating" class="section asn-rating"></div>
</f:verbatim>

<% // comments section %>
<f:verbatim>
  <div id="asn-comments" class="section asn-comments"></div>
</f:verbatim>

</h:form>





