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
<% // popupLayout.jsp - Primary layout for popup windows. %>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"  %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">

<f:view>
<f:loadBundle basename="gpt.resources.gpt" var="gptMsg"/>

<html lang="<%=request.getLocale().getLanguage()%>">
  
<head>
  <title><%=com.esri.gpt.framework.jsf.PageContext.extract().getSiteTitle()%></title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <meta http-equiv="X-UA-Compatible" content="IE=EmulateIE7" />
  <jsp:include page="/catalog/skins/lookAndFeel.jsp"/>
  <script type="text/javascript" src="<%=request.getContextPath()+"/catalog/js/Utils.js"%>"></script>
</head>

<body>
<% // page container %>
<h:panelGrid columns="1" styleClass="gptPage" 
  cellpadding="0" cellspacing="0" summary="#{gptMsg['catalog.general.designOnly']}">

  <% // page content - caption, and body  %>
  <h:panelGrid columns="1" styleClass="pageContentRight"
    cellpadding="0" cellspacing="0" summary="#{gptMsg['catalog.general.designOnly']}">

    <% // caption %>
    <h:panelGroup id="gptPopupCaption" styleClass="pageCaption">
      <h:outputText value="#{PageContext.caption}"/>
    </h:panelGroup>

    <% // body %>
    <h:panelGrid columns="2" styleClass="pageBody"
      cellpadding="0" cellspacing="0" summary="#{gptMsg['catalog.general.designOnly']}">
      <h:panelGroup>
        <tiles:insert attribute="body" flush="false"/>
      </h:panelGroup>
    </h:panelGrid>

  </h:panelGrid>
      
</h:panelGrid>
</body>

</html>

</f:view>
