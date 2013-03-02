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
<% // editMetadataBody.jsp - Edit metadata page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>

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

<h:form id="mdEditor" styleClass="mdEditor">
<h:inputHidden value="#{EditMetadataController.openSchemaKey}"/>
<h:inputHidden value="#{EditMetadataController.openDocumentUuid}"/>
<h:inputHidden value="#{EditMetadataController.onBehalfOf}"/>

<script type="text/javascript">

function MdeMap() {
  var _gptMap = null;
  var _gptMapToolbar = null;
  var _gptLocator = null;
  var _gptInpEnv = null;
  
  this.initialize = function initialize() {
    var config = gptMapConfig;
    
    config.mapElementId = "interactiveMap";
    config.mapToolName = "drawInputEnvelope";
    config.mapToolbarId = "mdEditor:mapToolbar";
    config.locatorInputId = "mdEditor:mapInput-locate";
    config.locatorCandidatesId = "locatorCandidates";
        
    var elForm = mdeFindForm();
    if ((elForm != null) && (typeof(mdeEnvelopeIds) != 'undefined') && (mdeEnvelopeIds != null)) {
      var sPfx = elForm.id+":";
      config.inputEnvelopeXMinId = sPfx+mdeEnvelopeIds[0];
      config.inputEnvelopeYMinId = sPfx+mdeEnvelopeIds[1];
      config.inputEnvelopeXMaxId = sPfx+mdeEnvelopeIds[2];
      config.inputEnvelopeYMaxId = sPfx+mdeEnvelopeIds[3];
    }
      
    _gptMap = new GptMap();
    dojo.connect(_gptMap,"onMapLoaded",this,"onMapLoaded");

    _gptInpEnv = new GptInputEnvelope();
    _gptInpEnv.initialize(config,_gptMap);
    
    _gptMap.initialize(config);
    
    _gptMapToolbar = new GptMapToolbar();
    dojo.connect(_gptMapToolbar,"onMapButtonClicked",this,"onMapButtonClicked");
    dojo.connect(_gptMapToolbar,"onDrawInputEnvelope",_gptInpEnv,"onDrawInputEnvelope");
    _gptMapToolbar.initialize(config,_gptMap);
    
    _gptLocator = new GptLocator();
    _gptLocator.initialize(config,_gptMap);
  }

  this.onMapLoaded = function() {
      if (_gptInpEnv != null) {
        _gptInpEnv.highlightInputEnvelope();
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
    if (sButtonName == "zoomToWorld") {
      if (_gptMap != null) _gptMap.zoomToWorld();
    } else if (sButtonName == "zoomToInputEnvelope") {
      if (_gptInpEnv != null) _gptInpEnv.zoomToInputEnvelope();
    } else if (sButtonName == "locate") {
      if (_gptLocator != null) _gptLocator.locate();
    }
  }
  
  this.reposition = function reposition() {
    if (_gptMap != null) _gptMap.reposition();
  }
   
}

var mdeMap = new MdeMap();;
if (typeof(dojo) != 'undefined') dojo.addOnLoad(mdeMapInit);

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

<% // initialize the map %>
function mdeMapInit() {
  var elMap = document.getElementById("interactiveMap");
  if (elMap != null) mdeMap.initialize();
  dojo.connect(window,"onresize",mdeMap,"reposition");
}

<% // toggle a section when the open/close image is clicked %>
function mdeOnSectionClicked(elClicked,bOptional,bExclusive,sSectionId) {
  var elForm = mdeFindForm();
  if (elForm != null) {
    var elBody, bDisplay = true;
    
	  if (!bOptional && !bExclusive) {
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
		  elBody = document.getElementById(elForm.id+":"+sSectionId+"-body");
		  mdeToggleDisplay(elBody,bDisplay)
		  
	  } else if (bOptional) {
	    bDisplay = false;
	    var elChk = document.getElementById(elForm.id+":"+sSectionId+"-chk");
	    if (elChk != null) bDisplay = elChk.checked;
	    elBody = document.getElementById(elForm.id+":"+sSectionId+"-body");
	    mdeToggleDisplay(elBody,bDisplay)
	    
	  } else if (bExclusive && (elClicked != null)) {
	    var elRadios = document.getElementsByName(elClicked.name);
	    if (elRadios != null) {
		    for (var i=0;i<elRadios.length;i++) {
		      var sId = elRadios[i].value;
		      bDisplay = elRadios[i].checked;
		      if ((sId != null) && (bDisplay != null)) {
		        elBody = document.getElementById(elForm.id+":"+sId+"-body");
		        mdeToggleDisplay(elBody,bDisplay);
		      }
		    }
		  }
	  }
	}

}

<% // toggle the visibility of an element %>
function mdeToggleDisplay(el,bDisplay) {
  if (el != null) {
    if (bDisplay) el.style.display = "block"; else el.style.display = "none";
    mdeMap.reposition();
  }
}

<% // toggle a hint section %>
function mdeToggleHint(elClicked) {
  if ((elClicked != null) && (elClicked.parentNode != null)) {
    dojo.query(".hintSection").forEach(function(item) {
      if (item.parentNode == elClicked.parentNode) {
        if ((item.style.display == null) || (item.style.display == "none")) {
          item.style.display = "block";
        } else {
          item.style.display = "none";
        }
        mdeMap.reposition();
      }
    });
  }
}

<% // toggle the visibility of an element %>
function mdeToggleVisibility(sourceElement,sLocalTargetId,bVisible) {
  var el = document.getElementById(sourceElement.form.id+":"+sLocalTargetId);
  if (el != null) {
    if (bVisible) el.style.visibility = "visible"; else el.style.visibility = "hidden";
    mdeMap.reposition();
  }
}

</script>

<% // prompt %>
<h:outputText escape="false" styleClass="prompt"
  value="#{gptMsg['catalog.publication.editMetadata.prompt']}"/>

<% // required fields note %>
<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>
 
<% // bind sections %>
<h:panelGroup binding="#{EditMetadataController.sectionsPanelGroup}"/>

<% // submit %>
<f:verbatim><hr/></f:verbatim>
<h:panelGrid columns="4" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">
  
  <% // submit buttons %>
  <h:outputText value=""/>
    <h:commandButton id="validate"
    value="#{gptMsg['catalog.publication.editMetadata.button.validate']}" 
    action="#{EditMetadataController.getNavigationOutcome}"
    actionListener="#{EditMetadataController.processAction}">
    <f:attribute name="command" value="validate"/>
  </h:commandButton>
  <h:commandButton id="saveAsDraft"
    value="#{gptMsg['catalog.publication.editMetadata.button.saveAsDraft']}" 
    action="#{EditMetadataController.getNavigationOutcome}"
    actionListener="#{EditMetadataController.processAction}">
    <f:attribute name="command" value="saveAsDraft"/>
  </h:commandButton>
  <h:commandButton id="submit"
    value="#{gptMsg['catalog.publication.editMetadata.button.submit']}" 
    action="#{EditMetadataController.getNavigationOutcome}"
    actionListener="#{EditMetadataController.processAction}">
    <f:attribute name="command" value="save"/>
  </h:commandButton>

</h:panelGrid>

<% // required fields note %>
<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>
  
</h:form>
