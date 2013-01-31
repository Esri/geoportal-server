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
<% // viewMetadataDetails.jsp - view metadata details(JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<% // interactive map configuration %>
<%
  com.esri.gpt.framework.ArcGIS.InteractiveMap imConfig = 
     com.esri.gpt.framework.context.RequestContext.extract(request).getApplicationConfiguration().getInteractiveMap();
  String vmdUuid = request.getParameter("uuid");
  String sRestUrl = request.getContextPath()+"/rest/document?f=html&id="+java.net.URLEncoder.encode(vmdUuid,"UTF-8");
  
%>

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

<h:form id="mdDetails" styleClass="mdDetails">

<script type="text/javascript">

function MddMap() {
  var _gptMap = null;
  var _gptInpEnv = null;
  
  this.initialize = function initialize() {
    var config = gptMapConfig;
    config.mapElementId = "interactiveMap";
    config.mapToolbarId = "mdDetails:mapToolbar";
        
    var elForm = mddFindForm();
    if ((elForm != null) && (typeof(mdeEnvelopeIds) != 'undefined') && (mdeEnvelopeIds != null)) {
      var sPfx = elForm.id+":";
      config.inputEnvelopeXMinId = sPfx+mdeEnvelopeIds[0];
      config.inputEnvelopeYMinId = sPfx+mdeEnvelopeIds[1];
      config.inputEnvelopeXMaxId = sPfx+mdeEnvelopeIds[2];
      config.inputEnvelopeYMaxId = sPfx+mdeEnvelopeIds[3];
      var el = document.getElementById(config.inputEnvelopeXMinId);
      if (el != null) el.value = el.innerHTML;
      el = document.getElementById(config.inputEnvelopeYMinId);
      if (el != null) el.value = el.innerHTML;
      el = document.getElementById(config.inputEnvelopeXMaxId);
      if (el != null) el.value = el.innerHTML;
      el = document.getElementById(config.inputEnvelopeYMaxId);
      if (el != null) el.value = el.innerHTML;
    }
      
    _gptMap = new GptMap();
    dojo.connect(_gptMap,"onMapLoaded",this,"onMapLoaded");

    _gptInpEnv = new GptInputEnvelope();
    _gptInpEnv.initialize(config,_gptMap);

    _gptMap.initialize(config);
  }

  this.onMapLoaded = function() {
      if (_gptInpEnv != null) {
        _gptInpEnv.highlightInputEnvelope();
        setTimeout(dojo.hitch(_gptInpEnv,"zoomToInputEnvelope"),1000);
        _gptMap.getAgsMap().disableMapNavigation();
        _gptMap.getAgsMap().disablePan();
        _gptMap.getAgsMap().disableClickRecenter();
        _gptMap.getAgsMap().disableDoubleClickZoom();
        _gptMap.getAgsMap().disableRubberBandZoom();
        _gptMap.getAgsMap().disableScrollWheelZoom();
        _gptMap.getAgsMap().disableShiftDoubleClickZoom();
        _gptMap.getAgsMap().hidePanArrows();
        _gptMap.getAgsMap().hideZoomSlider();
      }
  }
  
  this.reposition = function reposition() {
    if (_gptMap != null) _gptMap.reposition();
  }
};

<% // find the form for this page %>
function mddFindForm(sBaseName) {
  var elForm = null;
  var aForms = document.getElementsByTagName("form");
  if (aForms != null) {
    if (sBaseName == null) sBaseName = "mdDetails";
    for (var i=0;i<aForms.length;i++) {
      var el = aForms[i];
      if (el && el.id && (el.id.indexOf(sBaseName) != -1)) {
        elForm = el;
        break;
      }
    }
  }
  return elForm;
};

<% // initialize the map %>
var mddMap = new MddMap();;
if (typeof(dojo) != 'undefined') {
  dojo.addOnLoad(mddMapInit);
}

function mddMapInit() {
  var toolbar = dojo.byId("mdDetails:mapToolbar");
  if (toolbar!=null) toolbar.style.display="none";
  var elMap = document.getElementById("interactiveMap");
  if (elMap != null) mddMap.initialize();
  dojo.connect(window,"onresize",mddMap,"reposition");
};

<% // toggle a section when the open/close image is clicked %>
function mddOnSectionClicked(sSectionId) {
  var elForm = mddFindForm();
  if (elForm != null) {
    var bDisplay = true;
    var sFolder = "<%=request.getContextPath()%>/catalog/images/";
    var sSrc = sFolder+"section_open.gif";
    var elImg = document.getElementById(elForm.id+":"+sSectionId+"-img");
    if (elImg != null) {
      if (elImg.src.indexOf("section_open.gif") != -1) {
        sSrc = sFolder+"section_closed.gif";
        bDisplay = false;
      }
      elImg.src = sSrc;
      var elChk = document.getElementById(elForm.id+":"+sSectionId+"-chk");
      if (elChk != null) elChk.checked = bDisplay;
    }
    var el = document.getElementById(elForm.id+":"+sSectionId+"-body");

	if (el != null) {
	  if (bDisplay) el.style.display = "block"; else el.style.display = "none";
	  mddMap.reposition();
	}      
  }
}

</script>

<%//TODO: Replace back to search with property %>
<h:commandButton
  value="#{gptMsg['catalog.search.viewMetadataDetails.backToSrch']}"
  actionListener="#{SearchController.processAction}"
  action="#{SearchController.getNavigationOutcome}"
  rendered="false">
  <f:attribute 
    name="#{SearchController.searchEvent.event}" 
    value="#{SearchController.searchEvent.eventRedoSearch}"/>
</h:commandButton>

<% // bind detail sections %>
<h:panelGroup binding="#{SearchController.detailsPanelGroup}"/>

</h:form>

<% // button section %>
<f:verbatim>
  <iframe class="section" src="<%=sRestUrl%>" width="100%" scrolling="no" frameborder="0"></iframe>
  <span class="note"><%=sRestUrl%></span>
</f:verbatim>







