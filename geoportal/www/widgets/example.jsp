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
<% // example.jsp - Provides examples for the catalog search widget %>
<%@page language="java" contentType="text/html; charset=UTF-8" session="false"%>
<%
  String basePath = com.esri.gpt.framework.context.RequestContext.resolveBaseContextPath(request);
  String searchjsUrl = basePath+"/widgets/searchjs.jsp";
  String searchFlexUrl = basePath + "/widgets/FlexExample/index.html";
  String searchSilverLightUrl = basePath + "/widgets/SilverlightExample/Index.html";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; UTF-8">
<title>Geoportal Search Widget Examples</title>
</head>
<body>

<h2>Geoportal Search Widget for HTML </h2>

<p>Copy the following lines and paste into an html page at the point within 
the html body where you want the widget to appear.</p>
<p><b>NOTE:</b> If your html page already includes ArcGIS Javascript API, you don't need to copy the javascript api reference part in the below example. <br/>If you have dojo javascript library reference in your html page, please be aware of that dojo libarary referenced in ArcGIS Javascript API may cause a potential conflict with your dojo reference.</p>

<textarea cols="110" rows="4">
<!-- Catalog Search Widget -->
<script type="text/javascript" src="http://serverapi.arcgisonline.com/jsapi/arcgis/?v=2.5"></script>
<script type="text/javascript" src="<%=searchjsUrl%>"></script>
</textarea>

<br/>
<script type="text/javascript" src="http://serverapi.arcgisonline.com/jsapi/arcgis/?v=2.5"></script>
<script type="text/javascript" src="<%=searchjsUrl%>"></script>
<br/>

</body>
</html>
