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
<% // feedbackBody.jsp - Feedback page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<h:form id="frmFeedback" styleClass="fixedWidth">

<% // prompt %>
<h:outputText escape="false" styleClass="prompt"
  value="#{gptMsg['catalog.identity.feedback.prompt']}"/>
  
<% // input table %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
  
  <% // name %>
  <h:outputLabel for="feedbackFromName" 
    value="#{gptMsg['catalog.identity.feedback.label.name']}"/>
  <h:inputText id="feedbackFromName" size="30" maxlength="128" 
    value="#{SelfCareController.feedbackMessage.fromName}"/>
    
  <% // email address %>
  <h:outputLabel for="email" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.feedback.label.email']}"/>
  <h:inputText id="email" size="30" maxlength="128"
    value="#{SelfCareController.feedbackMessage.fromAddress}"/>
    
  <% // message body %>
  <h:outputLabel for="feedbackBody" styleClass="requiredField"
    value="#{gptMsg['catalog.identity.feedback.label.body']}"/>
   <h:inputTextarea id="feedbackBody" rows="12" cols="50" 
    value="#{SelfCareController.feedbackMessage.body}"/>
    
  <% // submit button %>
  <h:outputText value=""/>
  <h:commandButton id="submit" 
    value="#{gptMsg['catalog.identity.feedback.button.submit']}" 
    action="#{SelfCareController.getNavigationOutcome}"
    actionListener="#{SelfCareController.processAction}">
    <f:attribute name="command" value="sendFeedback" />
  </h:commandButton> 
   
</h:panelGrid>

<% // required fields note %>
<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>
  
</h:form>
