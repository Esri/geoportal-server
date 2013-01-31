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
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="com.esri.gpt.framework.util.Val" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">
<%@taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt"%>
<%
  com.esri.gpt.framework.ArcGIS.InteractiveMap imConfig = com.esri.gpt.framework.context.RequestContext
    .extract(request).getApplicationConfiguration().getInteractiveMap();
%>
<%
String url = Val.chkStr(request.getParameter("url")!=null? request.getParameter("url"): request.getQueryString());
String width = Val.chkStr(request.getParameter("width")!=null? request.getParameter("width"): "600px");
String height = Val.chkStr(request.getParameter("height")!=null? request.getParameter("height"): "400px");
%>
<%!
/**
 * Escapes string.
 */
private String esc(String str) {
  return Val.escapeXmlForBrowser(str);
}
%>
<html>
  <head>
    <fmt:setBundle basename="gpt.resources.gpt"/>

    <title><fmt:message key="catalog.search.liveData.title"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <jsp:include page="/catalog/skins/lookAndFeel.jsp"/>
  </head>
  <body style="background: #FFFFFF" class="tundra">
    <div dojoType="gpt.LiveData"
      url="<%=esc(url)%>"
      mapStyle="width: <%=esc(width)%>; height: <%=esc(height)%>; border: 1px solid #000;"
      mapService="<%=imConfig.getMapServiceUrl()%>"
      mapType="<%=imConfig.getMapServiceType()%>"
      mapVisibleLayers="<%=imConfig.getMapVisibleLayers()%>"
      mapInitialExtent="<%=imConfig.getMapInitialExtent()%>"
      geometryService="<%=imConfig.getGeometryServiceUrl()%>"
      proxy="<%=request.getContextPath()%>/catalog/livedata/liveDataProxy.page"
      verbose="true"
      errorMessage="<fmt:message key="catalog.search.liveData.errorMessage"/>"
      WMSErrorMessage="<fmt:message key="catalog.search.liveData.WMSErrorMessage"/>"
      basemapLabel="<fmt:message key="catalog.search.liveData.basemapLabel"/>"
      tooltips="<fmt:message key="catalog.search.liveData.tooltips"/>"></div>
  </body>
</html>
