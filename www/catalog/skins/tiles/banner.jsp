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
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>


<div id="gptTitle">
	<%=com.esri.gpt.framework.jsf.PageContext.extract().getSiteTitle()%>
</div>

<h:form id="frmTertiaryNavigation">

	<h:commandLink 
        id="identityFeedback"
        action="catalog.identity.feedback" 
        styleClass="#{PageContext.tabStyleMap['catalog.identity.feedback']}"
        value="#{gptMsg['catalog.identity.feedback.menuCaption']}" />
        
	<h:commandLink 
        id="contentAbout"
        action="catalog.content.about" 
        styleClass="#{PageContext.tabStyleMap['catalog.content.about']}"
        value="#{gptMsg['catalog.content.about.menuCaption']}"/>

	<h:outputLink value="#"
		id="openHelp" 
		onclick="javascript:mainOpenPageHelp()">
		<h:outputText value="#{gptMsg['catalog.help.menuCaption']}" />
	</h:outputLink>

	<h:commandLink id="identityMyProfile" 
		action="catalog.identity.myProfile" 
		value="#{gptMsg['catalog.identity.myProfile.menuCaption']}"
		styleClass="#{PageContext.menuStyleMap['catalog.identity.myProfile']}"
		rendered="#{not PageContext.roleMap['anonymous'] && not PageContext.roleMap['openid'] && PageContext.identitySupport.supportsUserProfileManagement}"/>
		
	<h:commandLink id="identityUserRegistration" 
		action="catalog.identity.userRegistration" 
		value="#{gptMsg['catalog.identity.userRegistration.menuCaption']}"
		styleClass="#{PageContext.menuStyleMap['catalog.identity.userRegistration']}"
		rendered="#{PageContext.roleMap['anonymous'] && PageContext.identitySupport.supportsUserRegistration}"/> 

	<h:commandLink id="identityLogin" action="catalog.identity.login" 
		value="#{gptMsg['catalog.identity.login.menuCaption']}"
		styleClass="#{PageContext.menuStyleMap['catalog.identity.login']}"
		rendered="#{PageContext.roleMap['anonymous'] && PageContext.identitySupport.supportsLogin}"/> 
	
	<h:commandLink id="identityLogout" action="catalog.main.home" 
		value="#{gptMsg['catalog.identity.logout.menuCaption']}" 
		rendered="#{not PageContext.roleMap['anonymous'] && PageContext.identitySupport.supportsLogout}"
		actionListener="#{LoginController.processLogout}"/>

  <h:outputText 
    id="msgAuthenticatedUser"
    rendered="#{not PageContext.roleMap['anonymous']}"
    value="#{PageContext.welcomeMessage}"/>
  <h:outputText 
    id="msgNonAuthenticatedUser"
    rendered="#{PageContext.roleMap['anonymous']}"
    value="#{gptMsg['catalog.site.anonymous']}"/>

</h:form>