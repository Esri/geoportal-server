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
<% // verifyMetadataBody.jsp - Verify metadata page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@taglib uri="http://www.esri.com/tags-gpt" prefix="gpt" %>

<h:form id="validate" enctype="multipart/form-data" styleClass="fixedWidth">
<h:inputHidden value="#{ValidateMetadataController.prepareView}"/>

<% // prompt %>
<h:outputText escape="false" styleClass="prompt"
  value="#{gptMsg['catalog.publication.validateMetadata.prompt']}"/>

<% // input table %>
<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formInputColumn">

  <% // file %>
  <f:verbatim> 
    <label class="requiredField" id="validate:lblvalidateXml" for="validate:validateXml"><%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker().retrieveMessage("catalog.publication.validateMetadata.label.file")%></label>
    <input type="file" id="validate:validateXml" name="validate:validateXml"
      size="50" accept="application/xml,text/xml"/>
  </f:verbatim>

  <% // submit button %>
  <f:verbatim><br/></f:verbatim>
  <h:commandButton id="submit"
    value="#{gptMsg['catalog.publication.validateMetadata.button.submit']}"
    action="#{ValidateMetadataController.getNavigationOutcome}"
    actionListener="#{ValidateMetadataController.processAction}" />

</h:panelGrid>

<% // required fields note %>
<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>

</h:form>
