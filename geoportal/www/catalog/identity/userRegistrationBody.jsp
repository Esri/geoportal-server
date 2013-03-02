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
<% // userRegistrationBody.jsp - User registration page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<h:form id="frmUserRegistration" styleClass="fixedWidth">

<% // prompt and notes %>
<h:outputText escape="false" styleClass="prompt"
  value="#{gptMsg['catalog.identity.userRegistration.prompt']}"/>

<% // input table %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
  
  <% // username %>
  <h:outputLabel for="newUserN" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.profile.label.username']}"/>
    <h:column>
  <h:inputText id="newUserN" size="30" maxlength="128" 
    value="#{SelfCareController.newUserCredentials.username}"/>
    <h:outputText escape="false" styleClass="hint"
  value="#{gptMsg['catalog.identity.general.usernamePolicy']}"/>
    </h:column>
    
  <% // password %>
  <h:outputLabel for="newUserP1" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.profile.label.password']}"/>
    <h:column>
  <h:inputSecret id="newUserP1" size="30" maxlength="128" 
    value="#{SelfCareController.newUserCredentials.password}"/>
    <h:outputText escape="false" styleClass="hint"
  value="#{gptMsg['catalog.identity.general.passwordPolicy']}"/>
    </h:column>

  <% // confirmation password %>
  <h:outputLabel for="newUserP2" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.profile.label.confirm']}"/>
  <h:inputSecret id="newUserP2" size="30" maxlength="128" 
    value="#{SelfCareController.newUserCredentials.confirmationPassword}"/>
    
  <% // include the remaining user profile attributes %>
  <jsp:include page="/catalog/identity/userAttributes.jsp" />

  <% // submit button %>
  <h:outputText value=""/>
  <h:commandButton id="submit"
    value="#{gptMsg['catalog.identity.userRegistration.button.submit']}" 
    action="#{SelfCareController.getNavigationOutcome}"
    actionListener="#{SelfCareController.processAction}">
    <f:attribute name="command" value="registerUser" />
  </h:commandButton>  
              
</h:panelGrid>

<% // required fields note %>
<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>
  
</h:form>
