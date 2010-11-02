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
<%// mysearches.jsp - Stored searches(JSF include)%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<h:panelGroup styleClass="mySearchesContainer">

	<% // my searches link %>
	<h:commandLink 
	  id="ssLnkSearches"
	  action="#{SearchController.getNavigationOutcome}"
	  actionListener="#{SearchController.processAction}"  >
    <h:outputText value="#{gptMsg['catalog.search.savedSearches.caption']}" escape="false"/>
	  <f:attribute 
	    name="#{SearchController.searchEvent.event}" 
	    value="#{SearchController.searchEvent.eventMySearches}"/>
	</h:commandLink>

	<% // my searches panel %>
	<h:panelGroup id="pngSavedSearches" style="#{SearchController.savedSearchesPanelStyle}">

		<% // save this search %>
		<h:panelGrid id="ss" columns="1" style="#{SearchController.saveSearchStyle}">
		  <h:panelGroup>
		    <h:inputText id="ssName"
		      value="#{SearchController.searchCriteria.savedSearchName}" maxlength="20" />
		    <h:commandButton id="ssBtnSave"
		      value="#{gptMsg['catalog.search.search.btnSave']}"
		      action="#{SearchController.getNavigationOutcome}"
		      actionListener="#{SearchController.processAction}">
		      <f:attribute name="#{SearchController.searchEvent.event}"
		        value="#{SearchController.searchEvent.eventSaveSearch}" />
		      <f:attribute name="#{SearchController.searchEvent.paramExecuteSearch}" value="true" />
		    </h:commandButton>
		  </h:panelGroup>
		</h:panelGrid>
		
		<% // saved search list%>
		<h:dataTable id="ssRecords" var="record" width="100%"
		  value="#{SearchController.savedSearches}">
			<h:column id="ssColName">
			  <gpt:tableCommandLink id="ssLnkName"
			    actionListener="#{SearchController.processAction}"
			    action="#{SearchController.getNavigationOutcome}">
			    <f:param name="#{SearchController.searchEvent.event}"
			      value="#{SearchController.searchEvent.eventLoadSavedSearch}" />
			    <f:param name="#{SearchController.searchEvent.paramUuid}" value="#{record.value}" />
			    <f:param name="#{SearchController.searchEvent.paramExecuteSearch}" value="true" />
			    <h:outputText id="ssTxtName" value="#{record.label}" />
			  </gpt:tableCommandLink>
			</h:column>
			<h:column id="ssColDel">
			  <gpt:tableCommandLink id="ssLnkDel"
			    actionListener="#{SearchController.processAction}"
			    action="#{SearchController.getNavigationOutcome}">
			    <f:param name="#{SearchController.searchEvent.event}"
			      value="#{SearchController.searchEvent.eventDeleteSavedSearch}" />
			    <f:param name="#{SearchController.searchEvent.paramUuid}" value="#{record.value}" />
			    <f:param name="#{SearchController.searchEvent.paramExecuteSearch}" value="true" />
			    <h:outputText  id="ssTxtDel" 
			      value="#{gptMsg['catalog.search.savedSearches.delete']}" />
			  </gpt:tableCommandLink>
			</h:column>
		</h:dataTable>

	</h:panelGroup>

</h:panelGroup>