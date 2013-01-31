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
<% // forgotPasswordBody.jsp - Forgot password page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<h:form id="frmForgotPassword" styleClass="fixedWidth">

<% // prompt %>
<h:outputText escape="false" styleClass="prompt"
  value="#{gptMsg['catalog.identity.forgotPassword.prompt']}"/>
  
<% // input table %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
  
  <% // username %>
  <h:outputLabel for="forgotUserN" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.profile.label.username']}"/>
  <h:inputText id="forgotUserN" size="30" maxlength="128" 
    value="#{SelfCareController.recoverPasswordCriteria.username}"/>
    
  <% // email address %>
  <h:outputLabel for="email" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.profile.label.email']}"/>
  <h:inputText id="email" size="30" maxlength="128" 
    value="#{SelfCareController.recoverPasswordCriteria.emailAddress}"/>
    
  <% // submit button %>
  <h:outputText value=""/>
  <h:panelGroup>
    <h:commandButton id="submit" 
      value="#{gptMsg['catalog.identity.forgotPassword.button.submit']}" 
      action="#{SelfCareController.getNavigationOutcome}"
      actionListener="#{SelfCareController.processAction}">
      <f:attribute name="command" value="recoverPassword" />
    </h:commandButton>  
    <f:verbatim>&nbsp;&nbsp;</f:verbatim>
    <h:commandLink id="forgotUsername"
      action="catalog.identity.feedback"
      value="#{gptMsg['catalog.identity.forgotPassword.button.forgotUsername']}" />
  </h:panelGroup>
            
</h:panelGrid>

<% // required fields note %>
<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>
  
</h:form>
