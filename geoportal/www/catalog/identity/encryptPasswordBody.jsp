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
<% // encryptPasswordBody.jsp - Encrypt password page (JSF body) %>
<%@ taglib prefix="f"  uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

<h:outputText 
  value="#{gptMsg['catalog.identity.encryptPassword.notAuthorized']}" 
  rendered="#{not PageContext.roleMap['gptAdministrator']}"/>
<h:form id="frmEncryptPassword" styleClass="fixedWidth"
  rendered="#{PageContext.roleMap['gptAdministrator']}">

<% // input table %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
      
  <% // password to encrypt%>
  <h:outputLabel for="encP1"
    value="#{gptMsg['catalog.identity.encryptPassword.label.input']}"/>
  <h:inputText id="encP1" size="30" maxlength="128" 
    value="#{SelfCareController.changePasswordCriteria.newCredentials.password}"/>

  <% // submit button %>
  <h:outputText value=""/>
  <h:commandButton id="submit" 
    value="#{gptMsg['catalog.identity.encryptPassword.button.submit']}">
  </h:commandButton>  
    
  <% // encrypted value %>
  <h:outputLabel for="encP2" 
    value="#{gptMsg['catalog.identity.encryptPassword.label.output']}"/>
  <h:inputText id="encP2" size="30" maxlength="128" readonly="true"
    value="#{SelfCareController.encryptedPwd}"/>
              
</h:panelGrid>
  
</h:form>
