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
<% // loginParameters.jsp - Login parameters page (JSF include) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<% // prompt %>
<h:outputText escape="false" styleClass="prompt"
  value="#{gptMsg['catalog.identity.login.prompt']}"/>
  
<% // input table %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
  
  <% // username %>
  <h:outputLabel for="userN" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.profile.label.username']}"/>
  <h:inputText id="userN" size="30" maxlength="128" 
    value="#{LoginController.credentials.username}"/>
    
  <% // password %>
  <h:outputLabel for="userP" styleClass="requiredField"
     value="#{gptMsg['catalog.identity.profile.label.password']}"/>
  <h:inputSecret id="userP" size="30" maxlength="64" 
    value="#{LoginController.credentials.password}"/>
    
  <% // submit button %>
  <h:outputText value=""/>
  <h:panelGroup>
  <h:commandButton id="submit"
    value="#{gptMsg['catalog.identity.login.button.submit']}" 
    action="#{LoginController.getNavigationOutcome}"
    actionListener="#{LoginController.processAction}" />
  <f:verbatim>&nbsp;&nbsp;</f:verbatim>
  <h:commandLink id="cifForgotPasswordMenuCaption" 
    action="catalog.identity.forgotPassword" 
    value="#{gptMsg['catalog.identity.forgotPassword.menuCaption']}"
    styleClass="#{PageContext.menuStyleMap['catalog.identity.forgotPassword']}"
    rendered="#{PageContext.roleMap['anonymous'] && PageContext.identitySupport.supportsPasswordRecovery}"/> 
  </h:panelGroup>
    
</h:panelGrid>

<% // required fields note %>
<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>



