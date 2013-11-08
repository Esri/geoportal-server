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
<% // downloadBody.jsp - Download extract page (JSF body) %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>

<%
      com.esri.gpt.framework.context.RequestContext context =
          com.esri.gpt.framework.context.RequestContext.extract(request);
      com.esri.gpt.framework.ArcGIS.InteractiveMap imConfig =
          context.getApplicationConfiguration().getInteractiveMap();
      com.esri.gpt.control.download.DownloadConfiguration dloadConfig =
          context.getApplicationConfiguration().getDownloadDataConfiguration();
      String mapServiceUrl =
          com.esri.gpt.framework.util.Val.chkStr(request.getParameter("mapServiceUrl"));
      String mapServiceType =
          com.esri.gpt.framework.util.Val.chkStr(request.getParameter("mapServiceType"));
      if (mapServiceUrl.length() == 0) {
        mapServiceUrl = dloadConfig.getMapServiceUrl();
      }
      if (mapServiceType.length() == 0) {
        mapServiceType = dloadConfig.getMapServiceType();
      }
      if (mapServiceType.length() == 0) {
        mapServiceType = "dynamic";
      }
%>

<f:verbatim>

  <style type="text/css">
    .pgdDownloadColumn {
      vertical-align: top;
    }
  </style>

  <script type="text/javascript">
    var gptMapConfig = new GptMapConfig();
    gptMapConfig.mapServiceURL = "<%=mapServiceUrl%>"
    gptMapConfig.mapServiceType = "<%=mapServiceType%>";
    gptMapConfig.geometryServiceURL = "<%=imConfig.getGeometryServiceUrl()%>";
    gptMapConfig.locatorURL = "<%=imConfig.getLocatorUrl()%>";
    gptMapConfig.locatorSingleFieldParameter = "<%=imConfig.getLocatorSingleFieldParameter()%>";
    gptMapConfig.locatorGraphicURL = "<%=request.getContextPath()%>/catalog/images/pushpin_red.gif";
    gptMapConfig.mapInitialExtent = "<%=dloadConfig.getMapInitialExtent()%>";
  </script>

  <script type="text/javascript">
    dojo.require("esri.map");
    dojo.require("esri.tasks.gp");
    dojo.require("esri.toolbars.draw");

    function MdeMap() {
      var _gptMap = null;
      var _gptMapToolbar = null;
      var _gptLocator = null;
      var _gptTask = null;
      var _gptDraw = null;
      var _layersCount = 0;
      var _featureSet = new esri.tasks.FeatureSet();
  
      this.initialize = function initialize() {
        esriConfig.defaults.io.proxyUrl = "proxy.jsp";
        var config = gptMapConfig;
    
        config.mapElementId = "interactiveMap";
        config.mapToolbarId = "mapToolbar";
        config.locatorInputId = "mapInput-locate";
        config.locatorCandidatesId = "locatorCandidates";
      
        var gptTaskUrl = document.getElementById("extractDataTaskUrl").value;
      
        _gptMap = new GptMap();
        dojo.connect(_gptMap,"onMapLoaded",this,"onMapServiceLayerLoaded");
        dojo.connect(_gptMap,"onMapLoaded",this,"reposition");
        _gptMap.initialize(config);
    
        _gptMapToolbar = new GptMapToolbar();
        _gptMapToolbar.initialize(config,_gptMap);
    
        _gptLocator = new GptLocator();
        _gptLocator.initialize(config,_gptMap);

        _gptTask = new esri.tasks.Geoprocessor(gptTaskUrl);
        _gptDraw = new esri.toolbars.Draw(_gptMap.getAgsMap());

        dojo.connect(_gptMapToolbar,"onMapButtonClicked",this,"onMapButtonClicked");
        dojo.connect(_gptTask, "onComplete", this, "onConfirmDownload");
        dojo.connect(_gptDraw, "onDrawEnd", this, "onSaveClipArea");
      }

      this.zoomToInitial = function zoomToInitial() {
        if (_gptMap != null) {
          if (_gptMap._initialExtent!=null) {
            _gptMap.zoomToInitial();
          } else {
            _gptMap.zoomToDefault();
          }
        }
      }

      this.onLocatorKeyPress = function onLocatorKeyPress(e) {
        if (!e) e = window.event;
        if (e) {
          var nKey = (e.keyCode) ? e.keyCode : e.which;
          if (nKey == 13) {
            if (_gptLocator != null) _gptLocator.locate();
            return false;
          }
        }
        return true;
      }
  
      this.onMapButtonClicked = function onMapButtonClicked(sButtonName) {
        if (sButtonName == "locate") {
          if (_gptLocator != null) _gptLocator.locate();
        } else if (sButtonName == "draw") {
          if (_gptDraw!=null) {
            _gptDraw.activate(esri.toolbars.Draw.POLYGON);
          }
        } else if (sButtonName == "deactivate") {
          if (_gptDraw!=null) {
            _gptDraw.deactivate();
          }
        } else if (sButtonName == "clear") {
          if (_gptMap !=null ) {
            _gptMap.getAgsMap().graphics.clear();
          }
        }
      }
  
      this.reposition = function reposition() {
        if (_gptMap != null) _gptMap.reposition();
      }

      this.onMapServiceLayerLoaded = function onMapServiceLayerLoaded() {
        this.zoomToInitial();
        
        var layers = _gptMap.getAgsMap().getLayer(_gptMap.getAgsMap().layerIds).layerInfos;
        var s = [];
        var preSelLayers = document.getElementById("preSelLayers").value;
        var indentSize = 20;
      
        _layersCount = 0;
    
        for (var i=0; i<layers.length; i++) {
          var layer = layers[i];
		  if (!layer.defaultVisibility) continue;
		  
          var subLayersCount = layer.subLayerIds!=null? layer.subLayerIds.length: 0;
          var currId = "layer_" + _layersCount;
          var parentsCount = 0;
          var parentId = null;
        
          if(subLayersCount==0) {
            var layername = layer.name;
            parentId = layer.parentLayerId;
            while (parentId>=0) {
              var parentLayer = layers[parentId];
              layername = parentLayer.name + "\\" + layername;
              parentId = parentLayer.parentLayerId;
              parentsCount++;
            }
            s.push(
            "<div style=\"text-indent: " +(indentSize*parentsCount)+ "px\"><input type=\"checkbox\" " +
              "id=\"" + currId + "\" "+
              "name=\"" + layername + "\" " +
              (preSelLayers.search("(^|,)" + _layersCount + "(,|$)")>=0? "checked": "") +
              "/>" +
              "<label for=\"" + currId + "\">" + layer.name + "</label></div>");
            _layersCount++;
          }
          else {
            parentId = layer.parentLayerId;
            while (parentId>=0) {
              var parentLayer = layers[parentId];
              parentId = parentLayer.parentLayerId;
              parentsCount++;
            }
            s.push("<div style=\"text-indent: " +(indentSize*parentsCount)+ "px\"><b>" + layer.name + "</b></div>")
          }
        }
      
        dojo.byId("toc").innerHTML = s.join("");
      }

      this.onConfirmDownload = function(results, messages) {
        alert(document.getElementById("msgDownloadComplete").value);
      }

      this.onSaveClipArea = function(geometry) {
        _gptMap.clearGraphics();
        var symbol = null;
        switch (geometry.type) {
          case "point":
            symbol = new esri.symbol.SimpleMarkerSymbol(
            esri.symbol.SimpleMarkerSymbol.STYLE_SQUARE,
            10,
            new esri.symbol.SimpleLineSymbol(
            esri.symbol.SimpleLineSymbol.STYLE_SOLID,
            new dojo.Color([255,0,0]),
            1),
            new dojo.Color([0,255,0,0.25]));
            break;
          case "polyline":
            symbol = new esri.symbol.SimpleLineSymbol(
            esri.symbol.SimpleLineSymbol.STYLE_DASH,
            new dojo.Color([255,0,0]),
            1);
            break;
          case "polygon":
            symbol = new esri.symbol.SimpleFillSymbol(
            esri.symbol.SimpleFillSymbol.STYLE_NONE,
            new esri.symbol.SimpleLineSymbol(
            esri.symbol.SimpleLineSymbol.STYLE_DASHDOT,
            new dojo.Color([255,0,0]),
            2),
            new dojo.Color([255,255,0,0.25]));
            break;
          case "multipoint":
            symbol = new esri.symbol.SimpleMarkerSymbol(
            esri.symbol.SimpleMarkerSymbol.STYLE_DIAMOND,
            20,
            new esri.symbol.SimpleLineSymbol(
            esri.symbol.SimpleLineSymbol.STYLE_SOLID,
            new dojo.Color([0,0,0]),
            1),
            new dojo.Color([255,255,0,0.5]));
            break;
        }
        var graphic = new esri.Graphic(geometry, symbol);
        _gptMap.addGraphic(graphic);
      
        var features= new Array();
        features.push(graphic);
        _featureSet.features = features;
      }
    
      this.onExtractData = function onExtractData() {
        var i = 0;
        var checkedLayers = [];

        for(i=0 ; i < _layersCount ; i++) {
          var currentLayer = document.getElementById("layer_"+i);
          if (currentLayer!=null && currentLayer.checked) {
            checkedLayers.push(currentLayer.name);
          }
        }

        // Get output format
        var selObj = document.getElementById("selFeature");
        var selIndex = selObj.selectedIndex;
        var outFeature = selObj.options[selIndex].value;

        // Get projection
        var selObj2 = document.getElementById("selRaster");
        var selIndex2= selObj2.selectedIndex;
        var outRaster = selObj2.options[selIndex2].value;
      
        // get user email
        var userEmail = document.getElementById("userEmail").value;
      
        if (userEmail.length == 0) {
          alert(document.getElementById("msgEnterEmail").value);
        } else if (_featureSet==null || _featureSet.features == null || _featureSet.features.length == 0) {
          alert(document.getElementById("msgSelectRegion").value);
        } else {
          alert(document.getElementById("msgConfirmation").value + " " + userEmail);
          var params = {"Raster_Format": outRaster, "Feature_Format": outFeature, "Area_of_Interest": _featureSet, "Layers_to_Clip": checkedLayers , "To": userEmail  };
          _gptTask.submitJob(params);
        }
      }
    }

    function mdeMapInit() {
      var elMap = document.getElementById("interactiveMap");
      if (elMap != null) {
        mdeMap.initialize();
      }
      dojo.connect(window,"onresize",mdeMap,"reposition");
    }

    var mdeMap = new MdeMap();
    dojo.addOnLoad(mdeMapInit);

    <% // find the form for this page %>
      function mdeFindForm(sBaseName) {
        var elForm = null;
        var aForms = document.getElementsByTagName("form");
        if (aForms != null) {
          if (sBaseName == null) sBaseName = "mdEditor";
          for (var i=0;i<aForms.length;i++) {
            var el = aForms[i];
            if (el && el.id && (el.id.indexOf(sBaseName) != -1)) {
              elForm = el;
              break;
            }
          }
        }
        return elForm;
      }
  </script>

</f:verbatim>

<%-- Localized messages to use in JavaScript --%>
<h:inputHidden id="msgEnterEmail"
               value="#{gptMsg['catalog.download.msg.enterEmail']}" />
<h:inputHidden id="msgSelectRegion"
               value="#{gptMsg['catalog.download.msg.selectRegion']}" />
<h:inputHidden id="msgConfirmation"
               value="#{gptMsg['catalog.download.msg.confirmation']}" />
<h:inputHidden id="msgDownloadComplete"
               value="#{gptMsg['catalog.download.msg.downloadComplete']}" />

<%-- Information from controller to use in JavaScript --%>
<h:inputHidden id="mapServiceUrl"
               value="#{DownloadController.mapServiceUrl}" />
<h:inputHidden id="extractDataTaskUrl"
               value="#{DownloadController.extractDataTaskUrl}" />
<h:inputHidden id="preSelLayers" value="#{DownloadController.layers}" />

<h:panelGrid columns="2"
             summary="#{gptMsg['catalog.general.designOnly']}" width="100%"
             columnClasses="homeTableColLeft,homeTableColRight">

    <h:panelGrid columns="1"
                 summary="#{gptMsg['catalog.general.designOnly']}"
                 columnClasses="homeTableRight" width="100%"
                 footerClass="homeTableRightFooter" headerClass="homeTableRightHeader"
                 cellpadding="0" cellspacing="0">
      <f:facet name="header">
        <h:column>
          <h:graphicImage styleClass="homeTableRightHeaderImageL"
                          url="/catalog/images/blank.gif" width="15" height="24"></h:graphicImage>
          <h:graphicImage styleClass="homeTableRightHeaderImageR"
                          url="/catalog/images/blank.gif" width="48" height="24"></h:graphicImage>
          <h:outputText value="#{gptMsg['catalog.download.msg.stepOne']}" />
        </h:column>
      </f:facet>
      <h:column>
        <h:panelGroup id="mapToolbar" styleClass="mapToolbar">
          <h:outputText value="#{gptMsg['catalog.download.msg.stepOneMsg']}" />
          <f:verbatim>&nbsp;</f:verbatim>
          <h:graphicImage id="mapButton-deactivate"
                          url="/catalog/images/btn-deactivate-off.gif"
                          alt="#{gptMsg['catalog.download.buton.deactivate.caption']}"
                          title="#{gptMsg['catalog.download.buton.deactivate.caption']}" />
          <f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>
          <h:outputText value="#{gptMsg['catalog.download.msg.stepTwoMsg']}" />
          <f:verbatim>&nbsp;</f:verbatim>
          <h:graphicImage id="mapButton-draw"
                          url="/catalog/images/btn-drawInputPolygon-off.gif"
                          alt="#{gptMsg['catalog.download.buton.drawPolygon.caption']}"
                          title="#{gptMsg['catalog.download.buton.drawPolygon.caption']}" />
          <h:graphicImage id="mapButton-clear"
                          url="/catalog/images/btn-clearSelection-off.gif"
                          alt="#{gptMsg['catalog.download.buton.clearSelection.caption']}"
                          title="#{gptMsg['catalog.download.buton.clearSelection.caption']}" />
        </h:panelGroup>
        <h:panelGrid id="pgdMap" columns="1"> <%-- Map --%>
          <h:panelGroup>
            <f:verbatim>
              <table border="0" cellpadding="0" cellspacing="0">
                <tr>
                  <td style="text-align: left">
                    <div
                      style="width: 400px; height: 325px; margin-top: 1px; border: 1px solid #000000; background:white">
                      <div id="interactiveMap"
                           style="width: 400px; height: 325px; cursor: pointer;"></div>
                    </div>
                  </td>
                </tr>
              </table>
            </f:verbatim>
          </h:panelGroup>
        </h:panelGrid>
      </h:column>
      <f:facet name="footer">
        <h:column>
          <h:graphicImage styleClass="homeTableRightFooterImageL"
                          url="/catalog/images/blank.gif" width="17" height="20"></h:graphicImage>
          <h:graphicImage styleClass="homeTableRightFooterImageR"
                          url="/catalog/images/blank.gif" width="23" height="20"></h:graphicImage>
        </h:column>
      </f:facet>
    </h:panelGrid>

    <h:panelGrid columns="1"
                 summary="#{gptMsg['catalog.general.designOnly']}"
                 columnClasses="homeTableRight" width="100%"
                 footerClass="homeTableRightFooter" headerClass="homeTableRightHeader"
                 cellpadding="0" cellspacing="0">
      <f:facet name="header">
        <h:column>
          <h:graphicImage styleClass="homeTableRightHeaderImageL"
                          url="/catalog/images/blank.gif" width="15" height="24"></h:graphicImage>
          <h:graphicImage styleClass="homeTableRightHeaderImageR"
                          url="/catalog/images/blank.gif" width="48" height="24"></h:graphicImage>
          <h:outputText value="#{gptMsg['catalog.download.msg.stepTwo']}" />
        </h:column>
      </f:facet>
      <h:column>
        <h:panelGroup>
          <f:verbatim>
            <table border="0" cellpadding="0" cellspacing="0">
              <tr>
                <td align="left">
                  <div id="toc"
                       style="overflow: auto; width: 390px; height: 353px;
                       border-style: solid; border-color: gray; border-width: 1px;
                       padding: 2px; background:white" />
                </td>
              </tr>
            </table>
          </f:verbatim>
        </h:panelGroup>
      </h:column>
      <f:facet name="footer">
        <h:column>
          <h:graphicImage styleClass="homeTableRightFooterImageL"
                          url="/catalog/images/blank.gif" width="17" height="20"></h:graphicImage>
          <h:graphicImage styleClass="homeTableRightFooterImageR"
                          url="/catalog/images/blank.gif" width="23" height="20"></h:graphicImage>
        </h:column>
      </f:facet>
    </h:panelGrid>

</h:panelGrid> 

<h:panelGrid columns="1"
             summary="#{gptMsg['catalog.general.designOnly']}"
             columnClasses="homeTableRight" width="100%"
             footerClass="homeTableRightFooter" headerClass="homeTableRightHeader"
             cellpadding="0" cellspacing="0">
  <f:facet name="header">
    <h:column>
      <h:graphicImage styleClass="homeTableRightHeaderImageL"
                      url="/catalog/images/blank.gif" width="15" height="24"></h:graphicImage>
      <h:graphicImage styleClass="homeTableRightHeaderImageR"
                      url="/catalog/images/blank.gif" width="48" height="24"></h:graphicImage>
      <h:outputText value="#{gptMsg['catalog.download.msg.stepThree']}" />
    </h:column>
  </f:facet>
  <h:column>

    <h:panelGrid id="pgdCriteria" columns="2" columnClasses="formLabelColumn,formInputColumn">

      <%-- Data generation criteria --%>
      <h:outputLabel for="userEmail"
                     value="#{gptMsg['catalog.download.email']}" />
      <h:inputText id="userEmail" value="#{DownloadController.userEmail}"
                   size="50" />

      <h:outputLabel for="selFeature"
                     value="#{gptMsg['catalog.download.featureFormat']}" />
      <h:selectOneMenu id="selFeature"
                       value="#{DownloadController.selectedFeature}">
        <f:selectItems value="#{DownloadController.allFeatures}" />
      </h:selectOneMenu>

      <h:outputLabel for="selRaster"
                     value="#{gptMsg['catalog.download.rasterFormat']}" />
      <h:selectOneMenu id="selRaster"
                       value="#{DownloadController.selectedRaster}">
        <f:selectItems value="#{DownloadController.allRasters}" />
      </h:selectOneMenu>

      <h:outputText value="" />
      <h:commandButton onclick="return mdeMap.onExtractData();"
                       value="#{gptMsg['catalog.download.buton.action.caption']}" />

    </h:panelGrid>
  </h:column>
  <f:facet name="footer">
    <h:column>
      <h:graphicImage styleClass="homeTableRightFooterImageL"
                      url="/catalog/images/blank.gif" width="17" height="20"></h:graphicImage>
      <h:graphicImage styleClass="homeTableRightFooterImageR"
                      url="/catalog/images/blank.gif" width="23" height="20"></h:graphicImage>
    </h:column>
  </f:facet>
</h:panelGrid>