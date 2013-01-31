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

<%@taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<f:verbatim rendered="#{SdiSuiteIntegrationFactory.integrationEnabled}">
    <script type="text/javascript">
        dojo.require("dijit.Dialog");
        function showDialog(title) {
            var oldOverflow = dojo.style(dojo.body(), "overflow");
            if (oldOverflow) {
                dojo.style(dojo.body(), "overflow", "hidden");
            }
            var dialog = dijit.byId("sdiSuiteDialog");
            dojo.connect(dialog, "onHide", function() {
                var contentDocBody = dojo.byId("ifrm-sdisuite-launch").contentDocument.body;
                if (contentDocBody) {
                    dojo.empty(contentDocBody);
                }
                if (oldOverflow) {
                    dojo.style(dojo.body(), "overflow", oldOverflow);
                }
            });
            dialog.setAttribute("title", title);
            dojo.style(dialog.domNode, "background", "white");
            dialog.show();
        }
    </script>
</f:verbatim>
<f:verbatim rendered="#{SdiSuiteIntegrationFactory.integrationEnabled}">
    <script type="text/javascript">
    var sdisuite =  new Object();
	sdisuite.tkn='';
    <% if (com.esri.gpt.framework.context.RequestContext.extract(request).getUser().getAuthenticationStatus().getWasAuthenticated()) {
    	  com.esri.gpt.sdisuite.IntegrationContextFactory _tcICF  = new com.esri.gpt.sdisuite.IntegrationContextFactory();
    	  if(_tcICF.isIntegrationEnabled()){
	         out.println("   sdisuite.tkn='"+_tcICF.newIntegrationContext().getBase64EncodedToken(
	        		 com.esri.gpt.framework.context.RequestContext.extract(request).getUser())+"';");
	    	}
    	 } 
    %>
	
	sdisuite.secMan = "</f:verbatim><h:outputText value="#{SdiSuiteIntegrationFactory.securityManagerUrl}"/><f:verbatim
        rendered="#{SdiSuiteIntegrationFactory.integrationEnabled}">";
	sdisuite.servMon ="</f:verbatim><h:outputText value="#{SdiSuiteIntegrationFactory.serviceMonitorUrl}"/><f:verbatim
        rendered="#{SdiSuiteIntegrationFactory.integrationEnabled}">";
	sdisuite.smartEditor ="</f:verbatim><h:outputText value="#{SdiSuiteIntegrationFactory.smartEditorUrl}"/><f:verbatim
        rendered="#{SdiSuiteIntegrationFactory.integrationEnabled}">";
	sdisuite.smartEditorUpdate ="</f:verbatim><h:outputText value="#{SdiSuiteIntegrationFactory.smartEditorStartWithUrl}"/><f:verbatim
        rendered="#{SdiSuiteIntegrationFactory.integrationEnabled}">";
	sdisuite.integrationType ="</f:verbatim><h:outputText value="#{SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value}"/><f:verbatim
        rendered="#{SdiSuiteIntegrationFactory.integrationEnabled}">"; 

    function snSdiSuiteLauncher(sType, sDialog) {
        var elForm = document.getElementById("frm-sdisuite-launch" + sType);
        if (elForm != null) {
            if (sType == "Manager" || sType == "Licenses") {
                elForm.ticket.value = sdisuite.tkn;
                if (sDialog) {
                    elForm.action = sdisuite.secMan + "?embedded=true";
                } else {
                    elForm.action = sdisuite.secMan + "?embedded=false";
                }
                elForm.submit();
                if (sDialog) {
                    showDialog((sType == "Manager") ? "securityManager / licenseManager" : "Licenses");
                }
            } else if (sType == "Monitor") {
                elForm.ticket.value = sdisuite.tkn;
                if (sDialog) {
                    elForm.action = sdisuite.servMon + "?embedded=true";
                } else {
                    elForm.action = sdisuite.servMon + "?embedded=false";
                }
                elForm.submit();
                if (sDialog) {
                    showDialog("serviceMonitor");
                }
            } else if (sType == "Editor") {
                elForm.ticket.value = sdisuite.tkn;
                elForm.action = sdisuite.smartEditor;
                elForm.submit();
               if (sDialog) {
                   showDialog("smartEditor");
               }
           }
       }
    }
    </script>
</f:verbatim>
