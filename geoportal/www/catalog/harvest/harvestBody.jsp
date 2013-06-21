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
<% // createBody.jsp - Manage harvest repositories page (JSF body) %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt"%>
<%@page import="com.esri.gpt.framework.context.RequestContext" %>

<%
String basePath = RequestContext.extract(request).resolveBaseContextPath(request);
%>

<f:verbatim>
<fmt:setBundle basename="gpt.resources.gpt"/>  

<style type="text/css">
.harvestExtra {
  width: 100px;
}
.dijitDialog {
  border-style: solid;
  border-width: 1px;
}
#closeFoldersDialog, #closeOwnersDialog {
  margin-top: 10px;
  margin-left: 0px;
}
#ownersDiv, #foldersDiv {
  min-height: 100px;
  min-width: 350px;
  border-style: solid;
  border-width: 1px;
  padding: 5px;
  max-height: 300px;
  overflow: scroll;
}
</style>
<script type="text/javascript" >

/**
 * Safe function to check if node is checked
 */
function isChecked(node) {
  return node!=null && node.checked;
}

/**
 * Safe function to check/uncheck node
 */
function setChecked(node, checked) {
  if (node!=null) node.checked = checked;
}

/**
 * Checks if 'synchronizable' is selected
 */
function getSynchronizable() {
  return isChecked(dojo.byId("harvestCreate:synchronizable"));
}

/**
 * Checks if 'searchable' is checked.
 */
function getSearchable() {
  return isChecked(dojo.byId("harvestCreate:searchable"));
}

/**
 * Selects/unselects 'searchable'.
 */
function setSearchable(checked) {
  setChecked(dojo.byId("harvestCreate:searchable"),checked);
}

/**
 * Checks if 'update content' is selected
 */
function getUpdateContent() {
  return isChecked(dojo.byId("harvestCreate:updateContent"));
}

/**
 * Selects/unselect 'update content'
 */
function setUpdateContent(checked) {
  setChecked(dojo.byId("harvestCreate:updateContent"),checked);
}

/**
 * Checks if 'update definition' is selected
 */
function getUpdateDefinition() {
  return isChecked(dojo.byId("harvestCreate:updateDefinition"));
}

/**
 * Selects/unselects 'update definition'
 */
function setUpdateDefinition(checked) {
  setChecked(dojo.byId("harvestCreate:updateDefinition"),checked);
}

/**
 * Checks if 'auto approve' is selected
 */
function getAutoApprove() {
  return isChecked(dojo.byId("harvestCreate:autoApprove"));
}

/**
 * Checks/unckecks 'auto approve'
 */
function setAutoApprove(checked) {
  setChecked(dojo.byId("harvestCreate:autoApprove"),checked);
}

/**
 * Gest synchronization frequency
 */
function getFrequency() {
  var value = dojo.query("#harvestCreate\\:harvestFrequency input:checked").attr("value");
  return value!=null && value.length>0? value[0]: "";
}

/**
 * Sets synchronization frequency
 */
function setFrequency(freq) {
  dojo.query("#harvestCreate\\:harvestFrequency input[value=" +freq+ "]").attr("checked",true);
}

/**
 * Checks if send notification
 */
function getSendNotification() {
  var value = dojo.query("#harvestCreate\\:sendNotification input:checked").attr("value");
  return value!=null && value.length>0? eval(value[0]): false;
}

/**
 * Sets if send notification
 */
function setSendNotification(send) {
  dojo.query("#harvestCreate\\:sendNotification input[value=" +send+ "]").attr("checked",true);
}

var lastUpdateContent = false;
var lastUpdateDefinition = false;
var lastAutoApprove = false;
var lastFrequency = "skip";
var lastSendNotification = false;

/**
 * Stores harvesting options
 */
function storeHarvestingOptions() {
  lastUpdateContent = getUpdateContent();
  lastUpdateDefinition = getUpdateDefinition();
  lastAutoApprove = getAutoApprove();
  lastFrequency = getFrequency();
  lastSendNotification = getSendNotification();
}

/**
 * Restores harvesting options
 */
function restoreHarvestingOptions() {
   setUpdateContent(lastUpdateContent);
   setUpdateDefinition(lastUpdateDefinition);
   setAutoApprove(lastAutoApprove);
   setFrequency(lastFrequency);
   setSendNotification(lastSendNotification);
}

/**
 * Clears harvesting options
 */
function clearHarvestingOptions() {
   setUpdateContent(false);
   setUpdateDefinition(false);
   setAutoApprove(false);
   setFrequency("skip");
   setSendNotification(false);
}

/**
 * Enables/disables synchronization options.
 */
function enableSyncOpt(enable) {
  if (enable) {
    restoreHarvestingOptions();
    dojo.query("#harvestCreate .syncOpt").attr("disabled",false);
    dojo.query("#harvestCreate .syncOpt input").attr("disabled",false);
    enable = getUpdateContent() || getUpdateDefinition();
    dojo.query("#harvestCreate .syncOptSpec").attr("disabled",!enable);
    dojo.query("#harvestCreate .syncOptSpec input").attr("disabled",!enable);
    dojo.query("#harvestCreate .autoApprove").attr("disabled",false);
  } else {
    storeHarvestingOptions();
    clearHarvestingOptions();
    dojo.query("#harvestCreate .syncOpt").attr("disabled",true);
    dojo.query("#harvestCreate .syncOpt input").attr("disabled",true);
    dojo.query("#harvestCreate .syncOptSpec").attr("disabled",true);
    dojo.query("#harvestCreate .syncOptSpec input").attr("disabled",true);
    dojo.query("#harvestCreate .autoApprove").attr("disabled",true);
  }
}

var synchronizableClicked = false;

// store original harvesting options just received from the bean
dojo.addOnLoad(function() {
  storeHarvestingOptions();
  dojo.query("#harvestCreate\\:synchronizable").onclick(function(node){
    if (!synchronizableClicked && node.target.checked) {
      lastUpdateContent = true;
      lastUpdateDefinition = true;
      lastAutoApprove = true;
      lastAutoApproveEnabled = true;
    }
    enableSyncOpt(node.target.checked);
    synchronizableClicked = true;
  });
  enableSyncOpt(getSynchronizable());
});

var lastSearchable = false;

/**
 * Enables/diables section.
 */
function enableSection(section,enable) {
  dojo.query("#harvestCreate ."+section).forEach(function(node, index, arr){
    node.parentNode.parentNode.style.display = (enable? "": "none");
  }, null);
}

/**
 * Enables sections.
 */
function selectSection(section) {
  
  if (section=="agp2agp") {
    dojo.query("#harvestCreate\\:repositoryMain > tbody > tr:nth-child(2)").style("display","none");
  } else {
    dojo.query("#harvestCreate\\:repositoryMain > tbody > tr:nth-child(2)").style("display","table-row");
  }

  if (section=="arcgis") {
    dojo.byId("harvestCreate:hostUrlLabel").innerHTML = dojo.byId("restUrlLabelValue").value;
  } else {
    dojo.byId("harvestCreate:hostUrlLabel").innerHTML = dojo.byId("hostUrlLabelValue").value;
  }
  
  enableSection("res", section=="res");
  enableSection("arcgis", section=="arcgis");
  enableSection("arcims", section=="arcims");
  enableSection("oai", section=="oai");
  enableSection("waf", section=="waf");
  enableSection("csw", section=="csw");
  enableSection("agp", section=="agp");
  enableSection("thredds", section=="thredds");
  enableSection("atom", section=="atom");
  
  enableSection("agp2agp", section=="agp2agp");
  
  dojo.query(".onBehalfOf").style("display",section!="agp2agp"? "block": "none");

  adjustSearchable(section);
}

dojo.addOnLoad(function() {
  dojo.query("#harvestCreate\\:protocolType input").onclick(function (v) {
    selectSection(v.target.value);
  });
  var checked = dojo.query("#harvestCreate\\:protocolType input:checked").attr("value");
  if (checked!=null && checked.length>0) {
    selectSection(checked[0]);
  } else {
    dojo.query("#harvestCreate\\:protocolType input")[0].checked = true;
    selectSection(dojo.query("#harvestCreate\\:protocolType input")[0].value);
  }
});

var lastFrequencyEnabled = true;
var lastFrequencyValue   = "skip";
var lastSendNotificationValue = false;

/**
 * Adjusts frequency.
 */
function adjustFrequency() {
  var enabled = getUpdateContent() || getUpdateDefinition();
  dojo.query("#harvestCreate\\:harvestFrequency input").attr("disabled",!enabled);
  dojo.query("#harvestCreate\\:sendNotification input").attr("disabled",!enabled);
  if (!enabled) {
    lastFrequencyValue = getFrequency();
    lastSendNotificationValue = getSendNotification();
    setFrequency("skip");
  } else if (!lastFrequencyEnabled) {
    setFrequency(lastFrequencyValue);
    setSendNotification(lastSendNotificationValue);
  }
  lastFrequencyEnabled = enabled;
}

dojo.addOnLoad(function() {
  dojo.query("#harvestCreate\\:updateContent").onclick(adjustFrequency);
  dojo.query("#harvestCreate\\:updateDefinition").onclick(adjustFrequency);
  adjustFrequency();
});

var lastSearchableValue = false;
var lastSearchableEnabled = true;

/**
 * Adjusts searchable.
 */
function adjustSearchable(section) {
  var enabled = section=="csw";
  dojo.query("#harvestCreate .searchable").attr("disabled",!enabled);
  if (!enabled) {
    if (lastSearchableEnabled) {
      lastSearchableValue = getSearchable();
      setSearchable(false);
    }
  } else if (!lastSearchableEnabled) {
    setSearchable(lastSearchableValue);
  }
  lastSearchableEnabled = enabled;
}

dojo.addOnLoad(function() {
  lastSearchableValue = getSearchable();
  dojo.query("#harvestCreate\\:searchable").onchange(function(node){
    lastSearchableValue = node.target.checked;
  });
  checked = dojo.query("#harvestCreate\\:protocolType input:checked").attr("value");
  if (checked!=null && checked.length>0) {
    adjustSearchable(checked);
  }
});

var lastAutoApproveValue = false;
var lastAutoApproveEnabled = true;

function adjustAutoApprove() {
  var enabled = getUpdateContent();
  dojo.query("#harvestCreate .autoApprove").attr("disabled",!enabled);
  if (!enabled) {
    if (lastAutoApproveEnabled) {
      lastAutoApproveValue = getAutoApprove();
      setAutoApprove(false);
    }
  } else if (!lastAutoApproveEnabled) {
    setAutoApprove(lastAutoApproveValue);
  }
  lastAutoApproveEnabled = enabled;
}

/**
 * Checks if info is enabled
 */
function getInfoEnabled() {
  var enable = false;
  var infoEnabled = dojo.query("#infoEnabled").attr("value");
  if (infoEnabled!=null && infoEnabled.length>0 && infoEnabled[0]=="true") {
    enable = true;
  }
  return enable;
}

function getSynchronizationStatus() {
  var status = "none";
  var synchronizationStatus = dojo.query("#synchronizationStatus").attr("value");
  if (synchronizationStatus!=null && synchronizationStatus.length>0) {
    status = synchronizationStatus[0];
  }
  return status;
}

dojo.addOnLoad(function() {
  lastAutoApproveValue = getAutoApprove();
  dojo.query("#harvestCreate\\:autoApprove").onchange(function(node){
    lastAutoApproveValue = node.target.checked;
  });
  dojo.query("#harvestCreate\\:updateContent").onchange(function(node){
    adjustAutoApprove();
  });
  adjustAutoApprove();
  dojo.query("#harvestCreate\\:syncInfo").forEach(function(node, index, arr){
    node.style.display = (getInfoEnabled()? "": "none");
  }, null);
  var status = getSynchronizationStatus();
  dojo.query("#harvestCreate\\:incSync").forEach(function(node, index, arr){
    node.style.display = (status=="none" || status=="scheduled"? "": "none");
  }, null);
  dojo.query("#harvestCreate\\:fullSync").forEach(function(node, index, arr){
    node.style.display = (status=="none" || status=="scheduled"? "": "none");
  }, null);
  dojo.query("#harvestCreate\\:cancelSync").forEach(function(node, index, arr){
    node.style.display = (status=="running" || status=="submited"? "": "none");
  }, null);
});

dojo.addOnLoad(function() {
  var fetchOwners = dojo.byId("fetchOwners");
  var fetchFolders = dojo.byId("fetchFolders");
  var closeFoldersDialog = dojo.byId("closeFoldersDialog");
  var foldersDiv = dojo.byId("foldersDiv");
  var ownersDiv = dojo.byId("ownersDiv");
  
  
  dojo.connect(fetchOwners,"onclick",function(evt){
    var ownersDialog = dijit.byId("ownersDialog");
    dojo.empty(ownersDiv);
    var h = dojo.trim(dojo.attr("harvestCreate:dest-h","value"));
    var u = dojo.trim(dojo.attr("harvestCreate:dest-u","value"));
    var p = dojo.trim(dojo.attr("harvestCreate:dest-p","value"));
    if (h.length>0 && u.length>0 && p.length>0) {
      dojo.query("[data-type=search]").style("display","none");
      ownersDialog.show();
      getSelf();
    } else {
      alert("<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.ownersDialog.alert"/>");
    }
  });
  
  dojo.connect(fetchFolders,"onclick",function(evt){
    var foldersDialog = dijit.byId("foldersDialog");
    dojo.empty(foldersDiv);
    var h = dojo.trim(dojo.attr("harvestCreate:dest-h","value"));
    var u = dojo.trim(dojo.attr("harvestCreate:dest-u","value"));
    var p = dojo.trim(dojo.attr("harvestCreate:dest-p","value"));
    var o = dojo.trim(dojo.attr("harvestCreate:dest-o","value"));
    if (h.length>0 && u.length>0 && p.length>0 && o.length>0) {
      foldersDialog.show();
      searchFolders();
    } else {
      alert("<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.foldersDialog.alert"/>");
    }
  });

  dojo.connect(closeFoldersDialog,"onclick",function(evt){
    var foldersDialog = dijit.byId("foldersDialog");
    foldersDialog.hide();
  });

  dojo.connect(closeOwnersDialog,"onclick",function(evt){
    var ownersDialog = dijit.byId("ownersDialog");
    ownersDialog.hide();
  });

  dojo.connect(searchOwnersDialog,"onclick",function(evt){
    dojo.empty(ownersDiv);
    searchOwners();
  });
  
  
  dojo.addClass(dijit.byId("foldersDialog").domNode,"tundra");
  dojo.addClass(dijit.byId("ownersDialog").domNode,"tundra");
});

function getSelf() {
  var ownersDiv = dojo.byId("ownersDiv");
  esri.request({
    url: "<%=basePath%>/catalog/harvest/getSelf.jsp",
    content: {
      h: dojo.trim(dojo.attr("harvestCreate:dest-h","value")),
      u: dojo.trim(dojo.attr("harvestCreate:dest-u","value")),
      p: dojo.trim(dojo.attr("harvestCreate:dest-p","value"))
    },
    handleAs: "json",
    callbackParamName: "callback"
  }).then(function(response){
    if (response) {
      if (response.role==="org_admin") {
        dojo.query("[data-type=search]").style("display","inline");
      } else {
        var caption = response.fullName + " (" + response.username + ")";
        var folderDiv = dojo.create("div",null,ownersDiv);
        var folderLink = dojo.create("a",{href:"#", innerHTML: caption},folderDiv);
        dojo.connect(folderLink,"onclick",function(evt){
          dojo.attr("harvestCreate:dest-o","value",response.username);
          var ownersDialog = dijit.byId("ownersDialog");
          ownersDialog.hide();
        });
      }
    }
  });
}

function searchOwners() {
  var ownersDiv = dojo.byId("ownersDiv");
  esri.request({
    url: "<%=basePath%>/catalog/harvest/searchOwners.jsp",
    content: {
      h: dojo.trim(dojo.attr("harvestCreate:dest-h","value")),
      u: dojo.trim(dojo.attr("harvestCreate:dest-u","value")),
      p: dojo.trim(dojo.attr("harvestCreate:dest-p","value")),
      q: dojo.trim(dojo.attr("ownersSearchText","value"))
    },
    handleAs: "json",
    callbackParamName: "callback"
  }).then(function(response){
    if (response==null || response.length==0) {
      var folderDiv = dojo.create("div",{innerHTML: '<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.norecords"/>'},ownersDiv);
    } else {
      for (var i=0; i<response.length; i++) {
        var owner = response[i];
        var caption = owner.fullName + " (" + owner.username + ")";
        var folderDiv = dojo.create("div",null,ownersDiv);
        var folderLink = dojo.create("a",{href:"#", innerHTML: caption},folderDiv);
        dojo.connect(folderLink,"onclick",dojo.hitch({username: owner.username},function(evt){
          dojo.attr("harvestCreate:dest-o","value",this.username);
          var ownersDialog = dijit.byId("ownersDialog");
          ownersDialog.hide();
        }));
      }
    }
  },function(error){
    var folderDiv = dojo.create("div",{innerHTML: '<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.norecords"/>'},ownersDiv);
  });
}

function searchFolders() {
  var foldersDiv = dojo.byId("foldersDiv");
  esri.request({
    url: "<%=basePath%>/catalog/harvest/searchFolders.jsp",
    content: {
      h: dojo.trim(dojo.attr("harvestCreate:dest-h","value")),
      u: dojo.trim(dojo.attr("harvestCreate:dest-u","value")),
      p: dojo.trim(dojo.attr("harvestCreate:dest-p","value")),
      o: dojo.trim(dojo.attr("harvestCreate:dest-o","value"))
    },
    handleAs: "json",
    callbackParamName: "callback"
  }).then(function(response){
    if (response==null || response.length==0) {
      var folderDiv = dojo.create("div",{innerHTML: '<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.norecords"/>'},foldersDiv);
    } else {
      for (var i=0; i<response.length; i++) {
        var folder = response[i];
        var caption = folder.title + " (" + folder.id + ")";
        var folderDiv = dojo.create("div",null,foldersDiv);
        var folderLink = dojo.create("a",{href:"#", innerHTML: caption},folderDiv);
        dojo.connect(folderLink,"onclick",dojo.hitch({folderId: folder.id},function(evt){
          dojo.attr("harvestCreate:dest-f","value",this.folderId);
          var foldersDialog = dijit.byId("foldersDialog");
          foldersDialog.hide();
        }));
      }
    }
  },function(error){
    var folderDiv = dojo.create("div",{innerHTML: '<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.norecords"/>'},foldersDiv);
  });
}
</script>

</f:verbatim>

<h:inputHidden id="hostUrlLabelValue" value="#{gptMsg['catalog.harvest.manage.edit.hostUrl']}"/>
<h:inputHidden id="restUrlLabelValue" value="#{gptMsg['catalog.harvest.manage.edit.restUrl']}"/>

<h:inputHidden id="infoEnabled" value="#{HarvestController.infoEnabled}"/>
<h:inputHidden id="synchronizationStatus" value="#{HarvestController.synchronizationStatus}"/>

<h:form id="harvestCreate">

<h:inputHidden id="selectablePublishers" 
value="#{HarvestController.prepareSelectedPublishers}"/>

<h:inputHidden id="uuid" value="#{HarvestController.editor.repository.uuid}"/>
<h:inputHidden id="localId" value="#{HarvestController.editor.repository.localId}"/>
<h:inputHidden id="inputDate" value="#{HarvestController.editor.repository.inputDate}">
  <f:convertDateTime type="both" dateStyle="full" timeStyle="full"/>
</h:inputHidden>
<h:inputHidden id="updateDate" value="#{HarvestController.editor.repository.updateDate}">
  <f:convertDateTime type="both" dateStyle="full" timeStyle="full"/>
</h:inputHidden>
<h:inputHidden id="lastHarvestDate" value="#{HarvestController.editor.repository.lastHarvestDate}">
  <f:convertDateTime type="both" dateStyle="full" timeStyle="full"/>
</h:inputHidden>
<h:inputHidden id="approvalStatus" value="#{HarvestController.editor.approvalStatus}"/>

<h:inputHidden id="ownerId" value="#{HarvestController.editor.ownerId}"/>

<%-- lock title flag --%>
<h:inputHidden id="lockTitle" value="#{HarvestController.editor.lockTitle}"/>

<%-- Repository id -----------------------------------------------------------%>  
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">

<%-- Repository id --%>
<h:outputLabel for="idDisp" value="#{gptMsg['catalog.harvest.manage.edit.repositoryId']}"/>
<h:outputText id="idDisp" escape="false" 
value="#{not empty HarvestController.editor.repository.uuid? HarvestController.editor.repository.localId: gptMsg['catalog.harvest.manage.message.willBeGenerated'] }" />

<%-- Repository uuid --%>
<h:outputLabel for="uuidDisp" value="#{gptMsg['catalog.harvest.manage.edit.repositoryUuid']}"/>
<h:outputText id="uuidDisp" escape="false" 
value="#{not empty HarvestController.editor.repository.uuid? HarvestController.editor.repository.uuid: gptMsg['catalog.harvest.manage.message.willBeGenerated'] }" />

</h:panelGrid>
<h:outputText escape="false" value="<hr align=\"center\"/>"/>

<%-- General repository info -------------------------------------------------%>  
<h:panelGrid id="repositoryMain"  columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn harvestExtra,formInputColumn">

<%-- Protocol Type --%>
<h:outputLabel for="protocolType" value="#{gptMsg['catalog.harvest.manage.edit.protocolType']}"/>

<h:selectOneRadio value="#{HarvestController.editor.type}" id="protocolType">
  <f:selectItems value="#{HarvestController.protocols}"/>
</h:selectOneRadio>

<%-- Host Url --%>
<h:outputLabel id="hostUrlLabel" for="hostUrl" styleClass="requiredField" value=""/>

<h:panelGroup>
<h:inputText size="50" value="#{HarvestController.editor.hostUrl}" id="hostUrl"/>
<h:outputText value="&nbsp;" escape="false"/>
<h:commandButton 
  id="testConnection"
  value="#{gptMsg['catalog.harvest.manage.edit.testConnection']}" 
  actionListener="#{HarvestController.handleTestConnection}" />
</h:panelGroup>

<%-- ArcGIS specific properties ----------------------------------------------%>

<%-- SOAP URL --%>
<h:outputLabel styleClass="arcgis requiredField" for="soapUrl" value="#{gptMsg['catalog.harvest.manage.edit.soapUrl']}"/>
<h:inputText   styleClass="arcgis" size="50" value="#{HarvestController.editor.soapUrl}" id="soapUrl"/>

<%-- Repository Name --%>
<h:outputLabel for="name" styleClass="" value="#{gptMsg['catalog.harvest.manage.edit.name']}"/>
<h:inputText size="50" value="#{HarvestController.editor.name}" id="name"/>

<%-- ArcIMS specific properties ----------------------------------------------%>


<%-- Port Number --%>
<%--
<h:outputLabel styleClass="arcims" for="portArcImsNumber" value="#{gptMsg['catalog.harvest.manage.edit.portNumber']}"/>
<h:inputText   styleClass="arcims" size="5" maxlength="5" value="#{HarvestController.editor.attrs['port']}" id="portArcImsNumber"/>
--%>

<%-- service name --%>
<h:outputLabel styleClass="arcims requiredField" for="serviceName" value="#{gptMsg['catalog.harvest.manage.edit.serviceName']}"/>
<h:inputText   styleClass="arcims" size="30" value="#{HarvestController.editor.attrs['service']}" id="serviceName"/>

<%-- username name --%>
<h:outputLabel styleClass="arcims" for="arcImsUserName" value="#{gptMsg['catalog.harvest.manage.edit.userName']}"/>
<h:inputText   styleClass="arcims" size="30" value="#{HarvestController.editor.attrs['arcims.username']}" id="arcImsUserName"/>

<%-- username password --%>
<h:outputLabel styleClass="arcims" for="arcImsUserPassword" value="#{gptMsg['catalog.harvest.manage.edit.userPassword']}"/>
<h:inputSecret styleClass="arcims" redisplay="true" size="30" value="#{HarvestController.editor.attrs['arcims.password']}" id="arcImsUserPassword"/>

<%-- OAI specific properties -------------------------------------------------%>

<%-- prefix --%>
<h:outputLabel styleClass="oai requiredField" for="prefix" value="#{gptMsg['catalog.harvest.manage.edit.prefix']}"/>
<h:inputText   styleClass="oai" size="30" value="#{HarvestController.editor.attrs['prefix']}" id="prefix"/>

<%-- Set --%>
<h:outputLabel styleClass="oai" for="set" value="#{gptMsg['catalog.harvest.manage.edit.set']}"/>
<h:inputText   styleClass="oai" size="30" value="#{HarvestController.editor.attrs['set']}" id="set"/>

<%-- WAF specific properties -------------------------------------------------%>

<%-- username name --%>
<h:outputLabel styleClass="waf" for="wafImsUserName" value="#{gptMsg['catalog.harvest.manage.edit.userName']}"/>
<h:inputText   styleClass="waf" size="30" value="#{HarvestController.editor.attrs['waf.username']}" id="wafImsUserName"/>

<%-- username password --%>
<h:outputLabel styleClass="waf" for="wafImsUserPassword" value="#{gptMsg['catalog.harvest.manage.edit.userPassword']}"/>
<h:inputSecret styleClass="waf" redisplay="true" size="30" value="#{HarvestController.editor.attrs['waf.password']}" id="wafImsUserPassword"/>

<%-- CSW specific properties -------------------------------------------------%>

<h:outputLabel styleClass="csw" for="cswProfile" value="#{gptMsg['catalog.harvest.manage.edit.cswProfile']}"/>
<h:selectOneMenu styleClass="csw" value="#{HarvestController.editor.attrs['profile']}" id="cswProfile">
<f:selectItems value="#{HarvestController.editor.allProfiles}"/>
</h:selectOneMenu>

<%-- ArcGIS Portal specific properties ---------------------------------------%>

<%-- THREDDS specific properties ---------------------------------------------%>

<%-- Atom specific properties ------------------------------------------------%>
<h:outputLabel styleClass="atom" for="atomType" value="#{gptMsg['catalog.harvest.manage.edit.atomType']}"/>
<h:selectOneMenu styleClass="atom" value="#{HarvestController.editor.attrs['atomInfoProcessorClassName']}" id="atomType">
  <f:selectItem itemValue="" itemLabel="#{gptMsg['catalog.harvest.manage.edit.atomType.openSearch']}"/>
	<f:selectItem itemValue="com.esri.gpt.control.webharvest.client.atom.AGPAtomInfoProcessor" itemLabel="#{gptMsg['catalog.harvest.manage.edit.atomType.agp']}"/>
	<f:selectItem itemValue="com.esri.gpt.control.webharvest.client.atom.AGPAtomInfoProcessor2" itemLabel="#{gptMsg['catalog.harvest.manage.edit.atomType.agp2']}"/>	
	<f:selectItem itemValue="com.esri.gpt.control.webharvest.client.atom.OpenSearchAtomInfoProcessor" itemLabel="#{gptMsg['catalog.harvest.manage.edit.atomType.file']}"/>
</h:selectOneMenu>

<%-- AGP-TO-AGP specific properties ------------------------------------------%>
<h:outputText styleClass="agp2agp agp2agpCaption" value=""/>
<h:outputText styleClass="agp2agp agp2agpCaption" value="#{gptMsg['catalog.harvest.manage.edit.src.caption']}"/>

<%-- src host --%>
<h:outputLabel styleClass="requiredField agp2agp" for="src-h" value="#{gptMsg['catalog.harvest.manage.edit.src.h']}"/>
<h:inputText   styleClass="agp2agp" size="30" value="#{HarvestController.editor.attrs['src-h']}" id="src-h"/>

<%-- src user name --%>
<h:outputLabel styleClass="requiredField agp2agp" for="src-u" value="#{gptMsg['catalog.harvest.manage.edit.src.u']}"/>
<h:inputText   styleClass="agp2agp" size="30" value="#{HarvestController.editor.attrs['src-u']}" id="src-u"/>

<%-- src password --%>
<h:outputLabel styleClass="requiredField agp2agp" for="src-p" value="#{gptMsg['catalog.harvest.manage.edit.src.p']}"/>
<h:inputSecret redisplay="true" styleClass="agp2agp" size="30" value="#{HarvestController.editor.attrs['src-p']}" id="src-p"/>

<%-- src query --%>
<h:outputLabel styleClass="requiredField agp2agp" for="src-q" value="#{gptMsg['catalog.harvest.manage.edit.src.q']}"/>
<h:panelGroup>
<h:inputText   styleClass="agp2agp" size="30" value="#{HarvestController.editor.attrs['src-q']}" id="src-q"/>
<h:commandButton 
  id="testQuery"
  value="#{gptMsg['catalog.harvest.manage.test.msg.agp2agp.button.testQuery']}" 
  actionListener="#{HarvestController.handleTestAgp2AgpQuery}" />
</h:panelGroup>

<%-- src max items --%>
<h:outputLabel styleClass="requiredField agp2agp" for="src-m" value="#{gptMsg['catalog.harvest.manage.edit.src.m']}"/>
<h:inputText   styleClass="agp2agp" size="10" value="#{HarvestController.editor.attrs['src-m']}" id="src-m"/>

<h:outputText styleClass="agp2agp agp2agpCaption" value=""/>
<h:outputText styleClass="agp2agp agp2agpCaption" value="#{gptMsg['catalog.harvest.manage.edit.dest.caption']}"/>

<%-- dest host --%>
<h:outputLabel styleClass="requiredField agp2agp" for="dest-h" value="#{gptMsg['catalog.harvest.manage.edit.dest.h']}"/>
<h:inputText   styleClass="agp2agp" size="30" value="#{HarvestController.editor.attrs['dest-h']}" id="dest-h"/>

<%-- dest user name --%>
<h:outputLabel styleClass="requiredField agp2agp" for="dest-u" value="#{gptMsg['catalog.harvest.manage.edit.dest.u']}"/>
<h:inputText   styleClass="agp2agp" size="30" value="#{HarvestController.editor.attrs['dest-u']}" id="dest-u"/>

<%-- dest password --%>
<h:outputLabel styleClass="requiredField agp2agp" for="dest-p" value="#{gptMsg['catalog.harvest.manage.edit.dest.p']}"/>
<h:panelGroup>
<h:inputSecret redisplay="true" styleClass="agp2agp" size="30" value="#{HarvestController.editor.attrs['dest-p']}" id="dest-p"/>
<h:commandButton 
  id="testClient"
  value="#{gptMsg['catalog.harvest.manage.test.msg.agp2agp.button.testClient']}" 
  actionListener="#{HarvestController.handleTestAgp2AgpClient}" />
</h:panelGroup>

<%-- dest owner --%>
<h:outputLabel styleClass="requiredField agp2agp" for="dest-o" value="#{gptMsg['catalog.harvest.manage.edit.dest.o']}"/>
<h:panelGroup>
<h:inputText styleClass="agp2agp" size="30" value="#{HarvestController.editor.attrs['dest-o']}" id="dest-o"/>
<f:verbatim>
  <input type="button" style="left: -4px;" value="<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.button.fetchOwners"/>" id="fetchOwners"/>
</f:verbatim>
</h:panelGroup>

<%-- dest folder ID --%>
<h:outputLabel styleClass="requiredField agp2agp" for="dest-f" value="#{gptMsg['catalog.harvest.manage.edit.dest.f']}"/>
<h:panelGroup>
<h:inputText   styleClass="agp2agp" size="40" value="#{HarvestController.editor.attrs['dest-f']}" id="dest-f"/>
<f:verbatim>
  <input type="button" style="left: -4px;" value="<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.button.fetchFolders"/>" id="fetchFolders"/>
</f:verbatim>
</h:panelGroup>

<%-- End of the panel --%>
</h:panelGrid>

<f:verbatim><br/><hr/></f:verbatim>

<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="onBehalfOf formTable" columnClasses="formLabelColumn,formInputColumn">

<% // on behalf of %>
<h:outputLabel for="onBehalfOf" styleClass="requiredField"
  value="#{gptMsg['catalog.harvest.manage.action.owner']}"/>
<h:selectOneMenu id="onBehalfOf"
   value="#{HarvestController.selectablePublishers.selectedKey}">
  <f:selectItems value="#{HarvestController.selectablePublishers.items}"/>
</h:selectOneMenu>

</h:panelGrid>

<f:verbatim><hr class="onBehalfOf"/><br class="onBehalfOf"/></f:verbatim>

<h:outputText value="#{gptMsg['catalog.harvest.manage.edit.purpose.title']}"/>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formInputColumn,">

<%-- Purpose of the registration --%>
<h:selectBooleanCheckbox styleClass="findable" value="#{HarvestController.editor.repository.findable}" id="findable"/>
<h:outputLabel styleClass="findable" for="findable" value="#{gptMsg['catalog.harvest.manage.edit.purpose.findable']}"/>

<h:selectBooleanCheckbox styleClass="searchable" value="#{HarvestController.editor.repository.searchable}" id="searchable"/>
<h:outputLabel styleClass="searchable" for="searchable" value="#{gptMsg['catalog.harvest.manage.edit.purpose.searchable']}"/>

<h:selectBooleanCheckbox styleClass="synchronizable" value="#{HarvestController.editor.repository.synchronizable}" id="synchronizable"/>
<h:outputLabel styleClass="synchronizable" for="synchronizable" value="#{gptMsg['catalog.harvest.manage.edit.purpose.synchronizable']}"/>

</h:panelGrid>

<f:verbatim><hr/><br/></f:verbatim>

<h:outputText styleClass="syncOpt" value="#{gptMsg['catalog.harvest.manage.edit.protocol.syncOptions']}"/>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formInputColumn,">

<%-- Incude in harvesting --%>
<h:selectBooleanCheckbox styleClass="syncOpt" value="#{HarvestController.editor.updateDefinition}" id="updateDefinition"/>
<h:outputLabel styleClass="syncOpt" for="updateDefinition" value="#{gptMsg['catalog.harvest.manage.edit.protocol.updateDefinition']}"/>

<h:selectBooleanCheckbox styleClass="syncOpt" value="#{HarvestController.editor.updateContent}" id="updateContent"/>
<h:outputLabel styleClass="syncOpt" for="updateContent" value="#{gptMsg['catalog.harvest.manage.edit.protocol.updateContent']}"/>

<h:outputText/>
<h:panelGroup>
  <h:selectBooleanCheckbox styleClass="autoApprove syncOpt" value="#{HarvestController.editor.autoApprove}" id="autoApprove"/>
  <h:outputLabel styleClass="autoApprove syncOpt" for="autoApprove" value="#{gptMsg['catalog.harvest.manage.edit.protocol.autoApprove']}"/>
</h:panelGroup>

</h:panelGrid>

<f:verbatim><br/></f:verbatim>

<%-- Harvesting frequency --%>
<h:outputText styleClass="syncOptSpec" value="#{gptMsg['catalog.harvest.manage.edit.frequency.caption']}"/>
<h:selectOneRadio styleClass="syncOptSpec" layout="pageDirection" value="#{HarvestController.editor.harvestFrequency}" id="harvestFrequency">
<f:selectItem itemValue="monthly" itemLabel="#{gptMsg['catalog.harvest.manage.edit.frequency.monthly']}"/>
<f:selectItem itemValue="biweekly" itemLabel="#{gptMsg['catalog.harvest.manage.edit.frequency.biweekly']}"/>
<f:selectItem itemValue="weekly" itemLabel="#{gptMsg['catalog.harvest.manage.edit.frequency.weekly']}"/>
<f:selectItem itemValue="dayly" itemLabel="#{gptMsg['catalog.harvest.manage.edit.frequency.dayly']}"/>
<f:selectItem itemValue="hourly" itemLabel="#{gptMsg['catalog.harvest.manage.edit.frequency.hourly']}"/>
<f:selectItem itemValue="once" itemLabel="#{gptMsg['catalog.harvest.manage.edit.frequency.once']}"/>
<f:selectItem itemValue="skip" itemLabel="#{gptMsg['catalog.harvest.manage.edit.frequency.skip']}"/>
</h:selectOneRadio>

<f:verbatim><br/><hr/><br/></f:verbatim>

<%-- Notifications --%>
<h:outputText styleClass="syncOptSpec" value="#{gptMsg['catalog.harvest.manage.edit.notification.caption']}"/>
<h:selectOneRadio styleClass="syncOptSpec" layout="pageDirection" value="#{HarvestController.editor.sendNotification}" id="sendNotification">
<f:selectItem itemValue="true" itemLabel="#{gptMsg['catalog.harvest.manage.edit.notification.yes']}"/>
<f:selectItem itemValue="false" itemLabel="#{gptMsg['catalog.harvest.manage.edit.notification.no']}"/>
</h:selectOneRadio>

<% // submit button %>
<h:panelGrid columns="3" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="gptRepoLabel,formInputColumn">
<h:outputText value=""/>
<h:commandButton  
  id="hvSubmit"
  value="#{not empty HarvestController.editor.repository.uuid? gptMsg['catalog.harvest.manage.edit.button.submit.update']: gptMsg['catalog.harvest.manage.edit.button.submit.create']}" 
  actionListener="#{HarvestController.handleUpdateRepository}" />
<h:commandButton
  action="catalog.publication.manageMetadata" 
  id="hvSubmitAndClose"
  value="#{not empty HarvestController.editor.repository.uuid? gptMsg['catalog.harvest.manage.edit.button.submit.updateclose']: gptMsg['catalog.harvest.manage.edit.button.submit.createclose']}" 
  actionListener="#{HarvestController.handleUpdateRepositoryAndClose}" />
</h:panelGrid>

<f:verbatim><br/></f:verbatim>

<h:panelGrid id="syncInfo" styleClass="info" columns="1" summary="#{gptMsg['catalog.general.designOnly']}">
  <h:outputText value="#{gptMsg['catalog.harvest.manage.edit.info.caption']}"/>
  <h:panelGroup>
    <h:outputText value="#{gptMsg['catalog.harvest.manage.edit.info.status.caption']}"/>
    <h:outputText value=" "/>
    <h:outputText rendered="#{HarvestController.synchronizationStatus=='none'}"      value="#{gptMsg['catalog.harvest.manage.edit.info.status.none']}"/>
    <h:outputText rendered="#{HarvestController.synchronizationStatus=='submited'}"  value="#{gptMsg['catalog.harvest.manage.edit.info.status.submitted']}"/>
    <h:outputText rendered="#{HarvestController.synchronizationStatus=='running'}"   value="#{gptMsg['catalog.harvest.manage.edit.info.status.running']}"/>
    <h:outputText rendered="#{HarvestController.synchronizationStatus=='canceled'}"  value="#{gptMsg['catalog.harvest.manage.edit.info.status.canceled']}"/>
    <h:outputText rendered="#{HarvestController.synchronizationStatus=='scheduled'}" value="#{gptMsg['catalog.harvest.manage.edit.info.status.scheduled']}"/>
  </h:panelGroup>
  <h:outputText rendered="#{HarvestController.synchronizationStatistics!=''}" value="#{HarvestController.synchronizationStatistics}"/>
  <h:panelGroup>
    <h:commandButton id="incSync"
                     value="#{gptMsg['catalog.harvest.manage.edit.info.choice.incSync']}"
                     actionListener="#{HarvestController.handleIncSynchronization}"/>
    <h:commandButton id="fullSync"
                     value="#{gptMsg['catalog.harvest.manage.edit.info.choice.fullSync']}"
                     actionListener="#{HarvestController.handleFullSynchronization}"/>
    <h:commandButton id="cancelSync"
                     value="#{gptMsg['catalog.harvest.manage.edit.info.choice.cancelSync']}"
                     actionListener="#{HarvestController.handleCancelSynchronization}"/>
  </h:panelGroup>
</h:panelGrid>
<%--
                     rendered="#{HarvestController.synchronizationStatus=='none' || HarvestController.synchronizationStatus=='scheduled'}"/>
                     rendered="#{HarvestController.synchronizationStatus=='none' || HarvestController.synchronizationStatus=='scheduled'}"/>
    <h:commandButton id="cancelSync"
                     value="#{gptMsg['catalog.harvest.manage.edit.info.choice.cancelSync']}"
                     actionListener="#{HarvestController.handleCancelSynchronization}"
                     rendered="#{HarvestController.synchronizationStatus=='running' || HarvestController.synchronizationStatus=='submited'}"/>
--%>

</h:form>

<f:verbatim><br/></f:verbatim>

<h:outputText escape="false" styleClass="requiredFieldNote"
  value="#{gptMsg['catalog.general.requiredFieldNote']}"/>

<f:verbatim>
  <div class="tundra" id="ownersDialog" data-dojo-type="dijit.Dialog" data-dojo-id="ownersDialog" title="<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.ownersDialog.caption"/>" style="min-width: 250px;">
        <input type="text" id="ownersSearchText" data-type="search"/><input id="searchOwnersDialog" type="button" value="<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.ownersDialog.button.search"/>" data-type="search"/>
        <div id="ownersDiv"></div>
        <input id="closeOwnersDialog" type="button" value="<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.ownersDialog.button.close"/>"/>
  </div>
  <div class="tundra" id="foldersDialog" data-dojo-type="dijit.Dialog" data-dojo-id="foldersDialog" title="<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.foldersDialog.caption"/>" style="min-width: 250px;">
        <div id="foldersDiv"></div>
        <input id="closeFoldersDialog" type="button" value="<fmt:message key="catalog.harvest.manage.test.msg.agp2agp.foldersDialog.button.close"/>"/>
  </div>
</f:verbatim>