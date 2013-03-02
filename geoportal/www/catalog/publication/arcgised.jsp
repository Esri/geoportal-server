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
<% // arcgised.jsp - Support for editor invocation from an external ArcGIS destop client. %>
<%
  String gxeNewDocumentId = com.esri.gpt.framework.util.UuidUtil.makeUuid();
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
<title>Geoportal Server XML Editor</title>
<link rel="stylesheet" type="text/css" href="http://serverapi.arcgisonline.com/jsapi/arcgis/2.5/js/dojo/dijit/themes/tundra/tundra.css" />
<link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/catalog/skins/themes/red/main.css"  />
<style type="text/css">
	.gxeMapEnvelopeTable {font-size: 1em;}
</style>

<script type="text/javascript" src="<%=com.esri.gpt.framework.context.RequestContext.extract(request).getApplicationConfiguration().getInteractiveMap().getJsapiUrl()%>"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/catalog/js/v1.2/gpt.js""></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/catalog/js/v1.1/gemet.js"></script>
<script type="text/javascript" src="<%=request.getContextPath()%>/catalog/js/v1.2/gxe.js"></script>

<% // interactive map configuration %>
<% com.esri.gpt.framework.ArcGIS.InteractiveMap imConfig = 
   com.esri.gpt.framework.context.RequestContext.extract(request).getApplicationConfiguration().getInteractiveMap();%>
<script type="text/javascript">
	var gptMapConfig = new GptMapConfig();
	gptMapConfig.mapServiceURL = "<%=imConfig.getMapServiceUrl()%>";
	gptMapConfig.mapServiceType = "<%=imConfig.getMapServiceType()%>";
	gptMapConfig.geometryServiceURL = "<%=imConfig.getGeometryServiceUrl()%>";
	gptMapConfig.locatorURL = "<%=imConfig.getLocatorUrl()%>";
	gptMapConfig.locatorSingleFieldParameter = "<%=imConfig.getLocatorSingleFieldParameter()%>";
	gptMapConfig.locatorGraphicURL = "<%=request.getContextPath()%>/catalog/images/pushpin_red.gif";
    gptMapConfig.mapVisibleLayers = "<%=imConfig.getMapVisibleLayers()%>";
</script>

<script type="text/javascript">
	var gxeContext = new gxe.Context();
  gxeContext.contextPath = "<%=request.getContextPath()%>";
  gxeContext.gptMapConfig = gptMapConfig;
  gxeContext.newDocumentId = "<%=gxeNewDocumentId%>";

  function gxecomGenerateXml(asDraft,sCallbackOption) {
    if (gxeContext.cfgDefinition != null) {
	    var xmlGenerator = new gxe.xml.Generator();
	    var sXml = xmlGenerator.generate(gxeContext,asDraft); 
	    if (!xmlGenerator.hadValidationErrors) {
	      return sXml;
	    }
    }
    return null;
  }

  function gxecomNewDocument(sCaption,sDocumentTypeKey) {
    var sParam = "key";
    var sParamValue = sDocumentTypeKey;
    gxelocalPrepForLoad();
    var elLoading = dojo.byId("gxeLoading");
    var elHtmlParent = dojo.byId("gxeDocument");
    var gxeClient = new gxe.Client();
    gxeClient.queryDefinition(gxeContext,sParam,sParamValue,function(responseObject,ioArgs) {
      if (responseObject == null) {
      } else {
	      var cfgDefinition = responseObject;
	      gxe.cfg.initialize(cfgDefinition);
	      gxeContext.buildUI(cfgDefinition,elHtmlParent,null);
	      gxecomSetCaption(sCaption);
      }
      elLoading.style.visibility = "hidden";
      elHtmlParent.style.display = "block";
    });
  }

  function gxecomOpenXml(sCaption,sXml) {
    gxelocalPrepForLoad();
    var elLoading = dojo.byId("gxeLoading");
    var elHtmlParent = dojo.byId("gxeDocument");
    var gxeClient = new gxe.Client();
    gxeClient.interrogateDefinition(gxeContext,sXml,function(responseObject,ioArgs) {
      if (responseObject == null) {
        
      } else {
	      var cfgDefinition = responseObject;
	      gxe.cfg.initialize(cfgDefinition);
	      var dom = null;
	      try {
	        if (window.DOMParser) {
	          var parser = new DOMParser();
	          dom = parser.parseFromString(sXml,"text/xml");
	        } else if (window.ActiveXObject) {
	          dom = new ActiveXObject("MSXML2.DOMDocument");
	          dom.async = "false";
	          dom.loadXML(sXml);
	        }
	      } catch (xmlParseErr) {
	        dom = null;
	        console.error("Unable to dynamically parse existing metadata XML.");
	        console.error(xmlParseErr);
	      }
	      if (dom != null) {
	        //gxeContext.openDocumentId = ???;
	        gxeContext.buildUI(cfgDefinition,elHtmlParent,dom);
	        gxecomSetCaption(sCaption);
	      }
      }
      elLoading.style.visibility = "hidden";
      elHtmlParent.style.display = "block";
    });
  }

  function gxecomSetCaption(sCaption) {
    dojo.byId("gxeEditorCaption").innerHTML = sCaption;
  }

  function gxelocalPrepForLoad() {
    gxecomSetCaption("");
    var elLoading = dojo.byId("gxeLoading");
    elLoading.style.visibility = "visible";

    var ctlMessageArea = gxeContext.messageArea;
    if ((typeof(ctlMessageArea) != "undefined") && (ctlMessageArea != null)) {
      ctlMessageArea.clearAll();
    }  
    
    var elHtmlParent = dojo.byId("gxeDocument");
    elHtmlParent.style.display = "none";
    while (elHtmlParent.childNodes.length >= 1) elHtmlParent.removeChild(elHtmlParent.firstChild);

  	var gxeContext2 = new gxe.Context();
    gxeContext2.contextPath = gxeContext.contextPath;
    gxeContext2.gptMapConfig = gxeContext.gptMapConfig;
    gxeContext2.newDocumentId = gxeContext.newDocumentId;
    gxeContext = gxeContext2;
  }

	if (typeof(dojo) != 'undefined') {
	  dojo.addOnLoad(function() {
	    window.external.gxecomOnHtmlLoaded();
	  });
	}  
</script>

</head>

<body class="tundra">
  <div style="padding-left:10px; background-color:#fff;">
	  <span id="gxeEditorCaption" style="font-size:x-small"></span>
	  <img id="gxeLoading" src="<%=request.getContextPath()%>/catalog/images/loading.gif" 
	       style="margin-left:2px; width:20px; height:20px; visibility:hidden;"/>
		<div id="gxeMessageArea" class="gxeMessageArea"></div>
		<div id="gxeDocument" class="gxeDocument"></div>
	</div>
</body>

</html>
