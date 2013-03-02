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
<f:verbatim rendered="#{SdiSuiteIntegrationFactory.integrationEnabled and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'dialog'}">
    <form id="frm-sdisuite-launchManager" name="frm-sdisuite-launchManager" style="display:none"
          action="none.html" method="post" target="ifrm-sdisuite-launch">
        <input type="hidden" id="ticket" name="ticket" value=""/>
    </form>
    <form id="frm-sdisuite-launchMonitor" name="frm-sdisuite-launchMonitor" style="display:none"
          action="none.html" method="post" target="ifrm-sdisuite-launch">
        <input type="hidden" id="ticket" name="ticket" value=""/>
    </form>
    <form id="frm-sdisuite-launchEditor" name="frm-sdisuite-launchEditor" style="display:none"
          action="none.html" method="post" target="ifrm-sdisuite-launch">
        <input type="hidden" id="ticket" name="ticket" value=""/>
        <input type="hidden" id="base64" name="base64" value="true"/>
        <input type="hidden" id="request" name="request" value="insert"/>
    </form>
    <form id="frm-sdisuite-launchLicenses" name="frm-sdisuite-launchLicenses" style="display:none"
          action="none.html" method="post" target="ifrm-sdisuite-launch">
        <input type="hidden" id="ticket" name="ticket" value=""/>
    </form>

    <div dojoType="dijit.Dialog" class="tundra" id="sdiSuiteDialog" draggable="false" style="display:none">
        <iframe id="ifrm-sdisuite-launch" name="ifrm-sdisuite-launch" src="../../tc" width="1200px"
                height="600px"></iframe>
    </div>
</f:verbatim>
<f:verbatim rendered="#{SdiSuiteIntegrationFactory.integrationEnabled and SdiSuiteIntegrationFactory.configuration['sdisuite.integrationType'].value == 'externalWindow'}">
    <form id="frm-sdisuite-launchManager" name="frm-sdisuite-launchManager" style="display:none"
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
</f:verbatim>
