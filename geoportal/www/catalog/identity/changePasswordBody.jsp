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
<% // changePasswordBody.jsp - Change password page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<h:form id="frmChangePassword" styleClass="fixedWidth">
  
<% // prompt and notes %>
<h:outputText escape="false" styleClass="prompt"
  value="#{gptMsg['catalog.identity.changePassword.prompt']}"/>


<% // input table %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
  
  <% // current user %>
  <h:outputLabel for="changeUserN" 
    value="#{gptMsg['catalog.identity.changePassword.label.username']}"/>
  <h:inputText id="changeUserN" size="30" maxlength="128" readonly="true" 
    value="#{SelfCareController.activeUsername}"/>
    
  <% // original password %>
  <h:outputLabel for="changeUserP1" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.changePassword.label.old']}"/>
  <h:inputSecret id="changeUserP1" size="30" maxlength="128" 
    value="#{SelfCareController.changePasswordCriteria.originalCredentials.password}"/>
    
  <% // new password %>
  <h:outputLabel for="changeUserP2" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.changePassword.label.new']}"/>
    <h:column>
  <h:inputSecret id="changeUserP2" size="30" maxlength="128" 
    value="#{SelfCareController.changePasswordCriteria.newCredentials.password}"/>
    <h:outputText escape="false" styleClass="hint"
  value="#{gptMsg['catalog.identity.general.passwordPolicy']}"/>
</h:column>

  <% // confirmation password %>
  <h:outputLabel for="changeUserP3" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.changePassword.label.confirm']}"/>
  <h:inputSecret id="changeUserP3" size="30" maxlength="128" 
    value="#{SelfCareController.changePasswordCriteria.newCredentials.confirmationPassword}"/>
    
  <% // submit button %>
  <h:outputText value=""/>
  <h:commandButton id="submit"
    value="#{gptMsg['catalog.identity.changePassword.button.submit']}" 
    action="#{SelfCareController.getNavigationOutcome}"
    actionListener="#{SelfCareController.processAction}">
    <f:attribute name="command" value="changePassword" />
  </h:commandButton>  
              
</h:panelGrid>

<% // required fields note %>
<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>
  
</h:form>
