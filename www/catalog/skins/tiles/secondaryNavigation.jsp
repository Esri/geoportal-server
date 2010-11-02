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
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"  %>

<%
  boolean snSdisuiteActive = false;
  String snSdisuiteManagerUrl = "";
  String snSdisuiteMonitorUrl = "";
  String snSdisuiteEditorUrl = "";
  String snSdisuiteLicensesUrl = "";
  String snSdisuiteSamlToken = "";
  if (com.esri.gpt.framework.context.RequestContext.extract(request).getUser().getAuthenticationStatus().getWasAuthenticated()) {
    com.esri.gpt.framework.jsf.TabStyleMap tsm = com.esri.gpt.framework.jsf.PageContext.extract().getTabStyleMap();
    if ((tsm.get("catalog.publication") != "") || 
        (tsm.get("catalog.harvest") != "") || 
        (tsm.get("catalog.sdisuite") != "")) {
	    com.esri.gpt.sdisuite.IntegrationContextFactory snSdisuiteICF  = new com.esri.gpt.sdisuite.IntegrationContextFactory();
		  if (snSdisuiteICF.isIntegrationEnabled()) {
		    com.esri.gpt.sdisuite.IntegrationContext snSdisuiteIC = snSdisuiteICF.newIntegrationContext();
		    if (snSdisuiteIC != null) {
		      snSdisuiteSamlToken = snSdisuiteIC.getBase64EncodedToken(
		          com.esri.gpt.framework.context.RequestContext.extract(request).getUser());
		      if (snSdisuiteSamlToken != null) {
		        snSdisuiteActive = true;
		        snSdisuiteManagerUrl = com.esri.gpt.framework.util.Val.chkStr(snSdisuiteICF.getSecurityManagerUrl());
		        snSdisuiteMonitorUrl = com.esri.gpt.framework.util.Val.chkStr(snSdisuiteICF.getServiceMonitorUrl());
		        snSdisuiteEditorUrl = com.esri.gpt.framework.util.Val.chkStr(snSdisuiteICF.getSmartEditorUrl());
		        snSdisuiteLicensesUrl = com.esri.gpt.framework.util.Val.chkStr(snSdisuiteICF.getLicensesUrl());
		      }
		    }
		  }
    }
	}
%>

<f:subview id="svSecondaryNavigation">
<% if (snSdisuiteActive) { %>
<f:verbatim>
<script type="text/javascript">
function snSdiSuiteLauncher(sType) {
  var elForm = document.getElementById("frm-sdisuite-launch"+sType);
  if (elForm != null) {
    var tkn = "<%=snSdisuiteSamlToken%>";
    if (sType == "Manager") {
      elForm.ticket.value = tkn;
      elForm.action = "<%=snSdisuiteManagerUrl%>";
      elForm.submit();
    } else if (sType == "Monitor") {
      elForm.ticket.value = tkn;
      elForm.action = "<%=snSdisuiteMonitorUrl%>";
      elForm.submit();
    } else if (sType == "Editor") {
      elForm.ticket.value = tkn;
      elForm.action = "<%=snSdisuiteEditorUrl%>";
      elForm.submit();
    } else if (sType == "Licenses") {
      elForm.ticket.value = tkn;
      elForm.action = "<%=snSdisuiteLicensesUrl%>";
      elForm.submit();
    }
  }
}
</script>
</f:verbatim>
<% } %>

<h:form id="frmSecondaryNavigation" 
  rendered="#{(PageContext.tabStyleMap['catalog.publication']!='' or 
               PageContext.tabStyleMap['catalog.harvest']!='' or 
               PageContext.tabStyleMap['catalog.sdisuite']!='') and
               PageContext.roleMap['gptPublisher']}">

  <h:commandLink action="catalog.publication.manageMetadata" 
    id="publicationManageMetadata" 
    value="#{gptMsg['catalog.publication.manageMetadata.subMenuCaption']}" 
    styleClass="#{PageContext.menuStyleMap['catalog.publication.manageMetadata']}"
    actionListener="#{ManageMetadataController.processAction}"/>

  <h:commandLink action="catalog.publication.addMetadata"
    id="publicationAddMetadata"
    value="#{gptMsg['catalog.publication.addMetadata.subMenuCaption']}"
    styleClass="#{PageContext.menuStyleMap['catalog.publication.addMetadata']}"/>
    
  <h:outputLink
    id="sdisuiteManager" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Manager');"
    rendered="#{PageContext.roleMap['gptAdministrator'] and not empty SdiSuiteIntegrationFactory.securityManagerUrl}">
    <h:outputText value="#{gptMsg['catalog.sdisuite.securityManager.menuCaption']}" />
  </h:outputLink>
  <h:outputLink
    id="sdisuiteMonitor" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Monitor');"
    rendered="#{PageContext.roleMap['gptAdministrator'] and not empty SdiSuiteIntegrationFactory.serviceMonitorUrl}">
    <h:outputText value="#{gptMsg['catalog.sdisuite.serviceMonitor.menuCaption']}" />
  </h:outputLink>
  <h:outputLink 
    id="sdisuiteEditor" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Editor');"
    rendered="#{PageContext.roleMap['gptPublisher'] and not empty SdiSuiteIntegrationFactory.smartEditorUrl}">
    <h:outputText value="#{gptMsg['catalog.sdisuite.smartEditor.menuCaption']}" />
  </h:outputLink>
  <h:outputLink
    id="sdisuiteLicenses" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Licenses');"
    rendered="#{not PageContext.roleMap['anonymous'] and not empty SdiSuiteIntegrationFactory.licensesUrl}">
    <h:outputText value="#{gptMsg['catalog.sdisuite.licenses.menuCaption']}" />
  </h:outputLink>
  
</h:form>
</f:subview>

<% if (snSdisuiteActive) { %>
<form id="frm-sdisuite-launchManager" name="frm-sdisuite-launch-launchManager" style="display:none"
  action="none.html" method="post" target="_blank">
  <input type="hidden" id="ticket" name="ticket" value=""/>
</form>
<form id="frm-sdisuite-launchMonitor" name="frm-sdisuite-launchMonitor" style="display:none"
  action="none.html" method="post" target="_blank">
  <input type="hidden" id="ticket" name="ticket" value=""/>
</form>
<form id="frm-sdisuite-launchEditor" name="frm-sdisuite-launchEditor" style="display:none"
  action="none.html" method="post" target="_blank">
  <input type="hidden" id="ticket" name="ticket" value=""/>
  <input type="hidden" id="base64" name="base64" value="true"/>
  <input type="hidden" id="request" name="request" value="insert"/>
</form>
<form id="frm-sdisuite-launchLicenses" name="frm-sdisuite-launchLicenses" style="display:none"
  action="none.html" method="post" target="_blank">
  <input type="hidden" id="ticket" name="ticket" value=""/>
</form>
<% } %>
