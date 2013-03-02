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
<% // previewBody.jsp - preview page (JSF body) %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@page import="java.net.URL"%>
<%@page import="com.esri.gpt.framework.util.Val" %>
<%@page import="com.esri.gpt.control.livedata.LiveDataController" %>

<%
  String prvContextPath = com.esri.gpt.framework.util.Val.chkStr(request.getContextPath());
%>
<%!
private String makeSafeUrl(String contextPath, String infoUrl) {
  try {
    new URL(infoUrl);
    return infoUrl;
  } catch (Exception ex) {
    try {
      if (infoUrl.startsWith(contextPath)) {
        return infoUrl;
      }
      return "";
    } catch (Exception ex2) {
      return "";
    }
  }
}
%>

<%-- Url's section --%>
<h:panelGrid styleClass="section previewUrlsSection" columns="2">
  <h:outputLabel for="urlInput" styleClass="labelStyle" value="#{gptMsg['catalog.search.liveData.url']}"/>
  <h:inputText id="urlInput" readonly="true" styleClass="inputStyle" value="#{LiveDataController.serviceUrl}"/>
  <h:outputLabel for="urlEmbed" styleClass="labelStyle" value="#{gptMsg['catalog.search.liveData.embed']}"/>
  <h:inputText id="urlEmbed" readonly="true" styleClass="inputStyle" value="#{LiveDataController.embededSnippet}"/>
</h:panelGrid>

<%-- Widget section --%>
<f:verbatim>
<% if (LiveDataController.getCurrentInstance().getInfoUrl().length() > 0) {%>
<div class="section previewMapSection">
  <iframe src="<%=makeSafeUrl(prvContextPath,Val.escapeXmlForBrowser(LiveDataController.getCurrentInstance().getEmbededUrl()))%>"
          style="<%=LiveDataController.getCurrentInstance().getFrameStyle()%>"
          frameborder="0" scrolling="no"
          id="mapFrame"></iframe>
</div>
<%} else {%>
<div class="section previewMapSectionWide">
  <iframe src="<%=makeSafeUrl(prvContextPath,Val.escapeXmlForBrowser(LiveDataController.getCurrentInstance().getEmbededUrl()))%>"
          style="<%=LiveDataController.getCurrentInstance().getFrameStyleWide()%>"
          frameborder="0" scrolling="no"
          id="mapFrame"></iframe>
</div>
<%}%>
</f:verbatim>

<%-- Info section --%>
<f:verbatim>
<% if (LiveDataController.getCurrentInstance().getInfoUrl().length() > 0) {%>
<div class="section previewInfoSection">
  <iframe src="<%=makeSafeUrl(prvContextPath,Val.escapeXmlForBrowser(LiveDataController.getCurrentInstance().getInfoUrl()))%>" 
          class="previewInfoFrame"
          frameborder="0" 
          id="infoFrame"></iframe>
</div>
<%}%>
</f:verbatim>


