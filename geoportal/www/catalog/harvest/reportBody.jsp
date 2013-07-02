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
<% // reportBody.jsp - report popup page (JSF body) %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<f:verbatim>

<script type="text/javascript">
<%-- toggles item --%>
function toggleItem(id) {
 var anItem = document.getElementById(id);
 if (anItem.style.display != 'none') {
   anItem.style.display = 'none';
 } else {
   anItem.style.display = '';
 }
}

<%-- changes dots --%>
function changeDots(id) {
 var srcImage = document.getElementById(id);
 if (srcImage.src.substr(srcImage.src.length-8,8) == "open.gif") {
   srcImage.src = "../images/bul_dot.gif";
 } else {
   srcImage.src = "../images/bul_dot_open.gif";
 }
}
</script>

</f:verbatim>
  
<% // identification %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">

<h:outputText
value="#{gptMsg['catalog.harvest.manage.history.repositoryName']}"/>
<h:outputText value="#{HarvestHistoryController.result.queryResult.name}"/>
    
<h:outputText
value="#{gptMsg['catalog.harvest.manage.history.repositoryUuid']}"/>
<h:outputText value="#{HarvestHistoryController.result.queryResult.uuid}"/>
    
<h:outputText
value="#{gptMsg['catalog.harvest.manage.history.repositoryUrl']}"/>
<h:outputText value="#{HarvestHistoryController.result.queryResult.url}"/>

<h:outputText
value="#{gptMsg['catalog.harvest.manage.history.repositoryProtocol']}"/>
<h:outputText 
value="#{HarvestHistoryController.result.queryResult.protocolTypeAsString}"/>

<h:outputText
value="#{gptMsg['catalog.harvest.manage.history.reportUuid']}"/>
<h:outputText 
value="#{HarvestHistoryController.result.queryResult.reportUuid}"/>

</h:panelGrid>

<f:verbatim><hr/></f:verbatim>

<%-- summary --%>
<h:panelGrid summary="#{gptMsg['catalog.general.designOnly']}" 
styleClass="report">
  
<h:panelGroup>
<h:outputLink value="#" onclick="changeDots('dot_summary'); toggleItem('panel_summary'); return false;">
  <h:graphicImage url="../images/bul_dot_open.gif" id="dot_summary" styleClass="dot" title="#{gptMsg['catalog.harvest.manage.report.summary']}" alt="#{gptMsg['catalog.harvest.manage.report.summary']}"/>
</h:outputLink>

<h:outputLink value="#" onclick="changeDots('dot_summary'); toggleItem('panel_summary'); return false;">
<h:outputText value="#{gptMsg['catalog.harvest.manage.report.summary']}"/>
</h:outputLink>
</h:panelGroup> 

<h:panelGrid id="panel_summary">
  <h:outputText escape="false" binding="#{HarvestHistoryController.reportViewer.summary}"/>
</h:panelGrid>

</h:panelGrid>

<f:verbatim><hr/></f:verbatim>

<%-- details --%>
<h:panelGrid summary="#{gptMsg['catalog.general.designOnly']}"
styleClass="report">

<h:panelGroup>
<h:outputLink value="#" onclick="changeDots('dot_details'); toggleItem('panel_details'); return false;">
  <h:graphicImage url="../images/bul_dot.gif" id="dot_details" styleClass="dot" title="#{gptMsg['catalog.harvest.manage.report.details']}" alt="#{gptMsg['catalog.harvest.manage.report.details']}"/>
</h:outputLink>

<h:outputLink value="#" onclick="changeDots('dot_details'); toggleItem('panel_details'); return false;">
<h:outputText value="#{gptMsg['catalog.harvest.manage.report.details']}"/>
</h:outputLink>
</h:panelGroup> 

<h:panelGrid id="panel_details" style="display: none;">
<h:outputText escape="false" binding="#{HarvestHistoryController.reportViewer.details}"/>
</h:panelGrid>

</h:panelGrid>


<h:form id="hrBackButtonForm" rendered="#{not HarvestHistoryController.external}">

<h:panelGrid width="100%"
columns="1" 
summary="#{gptMsg['catalog.general.designOnly']}"
styleClass="formTable" 
columnClasses="formBackButtonColumn">

<h:commandButton 
value="#{gptMsg['catalog.harvest.manage.report.back']}" 
action="catalog.harvest.manage.history">
</h:commandButton>

</h:panelGrid>

</h:form>

