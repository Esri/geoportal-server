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
<% // error.jsp - sdi.suite error page %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%   
  com.esri.gpt.framework.jsf.MessageBroker tcMsgBroker = 
    com.esri.gpt.framework.jsf.PageContext.extractMessageBroker();
  String tcTitle = tcMsgBroker.retrieveMessage("catalog.site.title");
  String tcError = com.esri.gpt.framework.util.Val.chkStr(request.getParameter("error"));
%>
<html>
<head>
<jsp:include page="/catalog/skins/lookAndFeel.jsp"/>
</head>
<body>
  <div id="gptMainWrap">
    <div id="gptBanner">
      <div id="gptTitle"><%=tcTitle%></div>
    </div>
    <% if (tcError.length() > 0) { %>
    <p class="errorMessage"><%=tcError%></p>
    <% }%>
  </div>
</body>