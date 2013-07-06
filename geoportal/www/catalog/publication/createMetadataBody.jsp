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
<% // createMetadataBody.jsp - Create metadata page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<h:form id="frmCreateMetadata" styleClass="fixedWidth">
<h:inputHidden value="#{EditMetadataController.prepareView}"/>

<% // prompt %>
<h:outputText escape="false" styleClass="prompt"
  value="#{gptMsg['catalog.publication.createMetadata.prompt']}"/>
  
<% // input table %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
  
  <% // on behalf of %>
  <h:outputLabel for="onBehalfOf" styleClass="requiredField"
    value="#{gptMsg['catalog.publication.uploadMetadata.label.onBehalfOf']}"/>
  <h:selectOneMenu id="onBehalfOf"
    value="#{EditMetadataController.selectablePublishers.selectedKey}">
    <f:selectItems value="#{EditMetadataController.selectablePublishers.items}"/>
  </h:selectOneMenu>
  
  <% // schema %>
  <h:outputText styleClass="requiredField"
    value="#{gptMsg['catalog.publication.createMetadata.label.schema']}"/>
  <h:selectOneRadio id="schema" layout="pageDirection"
    value="#{EditMetadataController.createSchemaKey}">
    <f:selectItems value="#{EditMetadataController.createSchemaItems}"/>
  </h:selectOneRadio>

  <% // submit button %>
  <h:outputText value=""/>
  <h:commandButton 
    id="submit"  
    value="#{gptMsg['catalog.publication.createMetadata.button.submit']}" 
    action="#{EditMetadataController.getNavigationOutcome}"
    actionListener="#{EditMetadataController.processAction}">
    <f:attribute name="command" value="create"/>
  </h:commandButton>
    
</h:panelGrid>
  
</h:form>
