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
<% // userAttributes.jsp - User profile attributes (JSF include) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

  <% // email address %>
  <h:outputLabel for="email" styleClass="requiredField"
    rendered="#{SelfCareController.hasUserAttribute['email']}"
    value="#{gptMsg['catalog.identity.profile.label.email']}"/>
  <h:inputText id="email" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['email']}"
    value="#{SelfCareController.activeUserAttributes['email'].value}"/>

	<h:outputText value="" rendered="#{not PageContext.roleMap['anonymous'] && PageContext.identitySupport.supportsPasswordChange}"/>    
  <h:commandLink id="cicChangePasswordMenuCaption" 
    action="catalog.identity.changePassword" 
    value="#{gptMsg['catalog.identity.changePassword.menuCaption']}"
    styleClass="#{PageContext.menuStyleMap['catalog.identity.changePassword']}"
    rendered="#{not PageContext.roleMap['anonymous'] && PageContext.identitySupport.supportsPasswordChange}"/> 
    
  <% // separator %>
  <h:outputText value=""/><h:outputText escape="false" value="&nbsp;"/>
  
  <% // first name %>
  <h:outputLabel for="firstName" 
    rendered="#{SelfCareController.hasUserAttribute['firstName']}"
    value="#{gptMsg['catalog.identity.profile.label.firstName']}"/>
  <h:inputText id="firstName" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['firstName']}"
    value="#{SelfCareController.activeUserAttributes['firstName'].value}"/>
    
  <% // last name %>
  <h:outputLabel for="lastName" styleClass="requiredField"
    rendered="#{SelfCareController.hasUserAttribute['lastName']}"
    value="#{gptMsg['catalog.identity.profile.label.lastName']}"/>
  <h:inputText id="lastName" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['lastName']}"
    value="#{SelfCareController.activeUserAttributes['lastName'].value}"/>
    
 <% // display name %>
  <h:outputLabel for="displayName" 
    rendered="#{SelfCareController.hasUserAttribute['displayName']}"
    value="#{gptMsg['catalog.identity.profile.label.displayName']}"/>
  <h:inputText id="displayName" size="30" styleClass="authenticationInput" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['displayName']}"
    value="#{SelfCareController.activeUserAttributes['displayName'].value}"/>  
    
  <% // organization %>
  <h:outputLabel for="organization"
    rendered="#{SelfCareController.hasUserAttribute['organization']}"
    value="#{gptMsg['catalog.identity.profile.label.organization']}"/>
  <h:inputText id="organization" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['organization']}"
    value="#{SelfCareController.activeUserAttributes['organization'].value}"/>
    
  <% // affiliation %>
  <h:outputLabel for="affiliation"
    rendered="#{SelfCareController.hasUserAttribute['affiliation']}"
    value="#{gptMsg['catalog.identity.profile.label.affiliation']}"/>
  <h:inputText id="affiliation" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['affiliation']}"
    value="#{SelfCareController.activeUserAttributes['affiliation'].value}"/>
    
  <% // separator %>
  <h:outputText value=""/><h:outputText escape="false" value="&nbsp;"/>
  
  <% // street %>
  <h:outputLabel for="street"
    rendered="#{SelfCareController.hasUserAttribute['street']}"
    value="#{gptMsg['catalog.identity.profile.label.street']}"/>
  <h:inputText id="street" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['street']}"
    value="#{SelfCareController.activeUserAttributes['street'].value}"/>
    
  <% // city %>
  <h:outputLabel for="city"
    rendered="#{SelfCareController.hasUserAttribute['city']}"
    value="#{gptMsg['catalog.identity.profile.label.city']}"/>
  <h:inputText id="city" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['city']}"
    value="#{SelfCareController.activeUserAttributes['city'].value}"/>
    
  <% // state/province %>
  <h:outputLabel for="stateOrProv"
    rendered="#{SelfCareController.hasUserAttribute['stateOrProv']}"
    value="#{gptMsg['catalog.identity.profile.label.stateOrProv']}"/>
  <h:inputText id="stateOrProv" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['stateOrProv']}"
    value="#{SelfCareController.activeUserAttributes['stateOrProv'].value}"/>
    
  <% // postal code %>
  <h:outputLabel for="postalCode"
    rendered="#{SelfCareController.hasUserAttribute['postalCode']}"
    value="#{gptMsg['catalog.identity.profile.label.postalCode']}"/>
  <h:inputText id="postalCode" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['postalCode']}"
    value="#{SelfCareController.activeUserAttributes['postalCode'].value}"/>
    
  <% // country %>
  <h:outputLabel for="country"
    rendered="#{SelfCareController.hasUserAttribute['country']}"
    value="#{gptMsg['catalog.identity.profile.label.country']}"/>
  <h:inputText id="country" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['country']}"
    value="#{SelfCareController.activeUserAttributes['country'].value}"/>
    
 <% // phone number %>
  <h:outputLabel for="phone"
    rendered="#{SelfCareController.hasUserAttribute['phone']}"
    value="#{gptMsg['catalog.identity.profile.label.phone']}"/>
  <h:inputText id="phone" size="30" maxlength="128" 
    rendered="#{SelfCareController.hasUserAttribute['phone']}"
    value="#{SelfCareController.activeUserAttributes['phone'].value}"/>

