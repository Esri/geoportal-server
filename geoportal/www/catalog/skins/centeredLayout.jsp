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
<% // centeredLayout.jsp - Primary layout for a page within the site. %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"  %>
<%@taglib uri="http://www.esri.com/tags-gpt" prefix="gpt" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<f:view>
<f:loadBundle basename="gpt.resources.gpt" var="gptMsg"/>
<gpt:prepareView/>
<html lang="<%=request.getLocale().getLanguage()%>">
<head>
	<title><%=com.esri.gpt.framework.jsf.PageContext.extract().getSiteTitle()%></title>
    <jsp:include page="/catalog/skins/lookAndFeel.jsp"/>
	<tiles:insert attribute="head" flush="false"/>
</head>
<body>
	<div id="gptMainWrap" style="position: static;">
	   
		<div id="gptTopBar">
			<div id="gptSkip">
				<ul>
					<li><a href="#gptBody"><h:outputText id="txtSkipToContent" value="#{gptMsg['catalog.site.skipToContent']}"/></a></li>
				</ul>
			</div>
			<div id="gptTertiaryNavigation">
				<tiles:insert attribute="tertiaryNavigation" flush="false"/>
			</div>
		</div>
		
		<div id="gptBanner">
			<tiles:insert attribute="banner" flush="false"/>
		</div>
		<div style="clear:both"></div>
		<div id="gptPrimaryNavigation">
			<tiles:insert attribute="primaryNavigation" flush="false"/>
		</div>
		<div style="clear:both"></div>
		<div id="gptBody">
		
			<div id="gptSecondaryNavigation">
				<tiles:insert attribute="secondaryNavigation" flush="false"/>
			</div>
  
			<h1><h:outputText id="cmPlPcCaption" value="#{PageContext.caption}"/></h1>

  <% // page content - navigation menu and main body %>
  <h:panelGrid id="cmPlPgdNavMenuAndMainBody" styleClass="pageContent"
    columns="1" cellpadding="0" cellspacing="0" 
    summary="#{gptMsg['catalog.general.designOnly']}">


    
    <% // page content right - page caption, messages and page body  %>
    <h:panelGrid id="cmPlPgdPageContentBody" styleClass="pageContentRight"
      columns="1" cellpadding="0" cellspacing="0" 
      summary="#{gptMsg['catalog.general.designOnly']}">
      
      
      <% // messages %>
      <h:panelGroup id="cmPlPgpGptMessages">
        <h:messages id="cmPlMsgsPageMessages" layout="list" 
          infoClass="successMessage" 
          errorClass="errorMessage"/>
      </h:panelGroup>
        
      <% // page body %>
      <h:panelGrid id="cmPlPgdPageBody" styleClass="pageBody" 
        columns="1" cellpadding="0" cellspacing="0" 
        summary="#{gptMsg['catalog.general.designOnly']}">
        <h:panelGroup id="cmPlPgpPageBody" >
          <tiles:insert attribute="body" flush="false"/>
        </h:panelGroup>
      </h:panelGrid>
    </h:panelGrid>
    
  </h:panelGrid>
      
		</div>
		<div style="clear:both"></div>
		<div id="gptFooter">
			<tiles:insert attribute="footer" flush="false"/>
		</div>
		
	</div> <!-- gptMainWrap -->
 
</body>
</html>
</f:view>
