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
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<h:commandLink action="catalog.sdisuite.securityManager" id="sdisuiteSecurityManagerIframe"
               value="#{gptMsg['catalog.sdisuite.securityManager.menuCaption']}"
               styleClass="#{PageContext.menuStyleMap['catalog.sdisuite.securityManager']}"
               rendered="#{PageContext.roleMap['gptAdministrator'] and not empty SdiSuiteIntegrationFactory.securityManagerUrl and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'iframe'}"/>

<h:outputLink   id="sdisuiteSecurityManagerExt" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Manager');"
                rendered="#{PageContext.roleMap['gptAdministrator'] and not empty SdiSuiteIntegrationFactory.securityManagerUrl and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'externalWindow'}">
                <h:outputText value="#{gptMsg['catalog.sdisuite.securityManager.menuCaption']}"/>
</h:outputLink>

<h:outputLink   id="sdisuiteSecurityManagerDialog" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Manager','showDialog');"
                rendered="#{PageContext.roleMap['gptAdministrator'] and not empty SdiSuiteIntegrationFactory.securityManagerUrl and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'dialog'}">
                <h:outputText value="#{gptMsg['catalog.sdisuite.securityManager.menuCaption']}"/>
</h:outputLink>

<h:commandLink action="catalog.sdisuite.serviceMonitor" id="sdisuiteServiceMonitorIframe"
               value="#{gptMsg['catalog.sdisuite.serviceMonitor.menuCaption']}"
               styleClass="#{PageContext.menuStyleMap['catalog.sdisuite.serviceMonitor']}"
               rendered="#{PageContext.roleMap['gptAdministrator'] and not empty SdiSuiteIntegrationFactory.serviceMonitorUrl and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'iframe'}"/>

<h:outputLink   id="sdisuiteServiceMonitorExt" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Monitor');"
                rendered="#{PageContext.roleMap['gptAdministrator'] and not empty SdiSuiteIntegrationFactory.serviceMonitorUrl and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'externalWindow'}">
                <h:outputText value="#{gptMsg['catalog.sdisuite.serviceMonitor.menuCaption']}"/>
</h:outputLink>

<h:outputLink   id="sdisuiteServiceMonitorDialog" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Monitor','showDialog');"
                rendered="#{PageContext.roleMap['gptAdministrator'] and not empty SdiSuiteIntegrationFactory.serviceMonitorUrl and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'dialog'}">
                <h:outputText value="#{gptMsg['catalog.sdisuite.serviceMonitor.menuCaption']}"/>
</h:outputLink>

<h:commandLink action="catalog.sdisuite.smartEditor" id="smartEditorIframe"
               value="#{gptMsg['catalog.sdisuite.smartEditor.menuCaption']}"
               styleClass="#{PageContext.menuStyleMap['catalog.sdisuite.smartEditor']}"
               rendered="#{PageContext.roleMap['gptAdministrator'] and not empty SdiSuiteIntegrationFactory.smartEditorUrl and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'iframe'}"/>

<h:outputLink   id="smartEditorExt" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Editor');"
                rendered="#{PageContext.roleMap['gptPublisher'] and not empty SdiSuiteIntegrationFactory.smartEditorUrl and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'externalWindow'}">
                <h:outputText value="#{gptMsg['catalog.sdisuite.smartEditor.menuCaption']}"/>
</h:outputLink>

<h:outputLink   id="smartEditorDialog" value="javascript:void(0);" styleClass="" onclick="snSdiSuiteLauncher('Editor','showDialog');"
                rendered="#{PageContext.roleMap['gptPublisher'] and not empty SdiSuiteIntegrationFactory.smartEditorUrl and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'dialog'}">
                <h:outputText value="#{gptMsg['catalog.sdisuite.smartEditor.menuCaption']}"/>
</h:outputLink>