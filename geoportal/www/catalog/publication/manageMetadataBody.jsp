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
<% // manageMetadataBody.jsp - Manage metadata page (JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@taglib uri="http://www.esri.com/tags-gpt" prefix="gpt" %>

<f:verbatim>

<style type="text/css">
.actionColumnStyle {
  min-width: 10em;
}
</style>
<script type="text/javascript" language="Javascript">

<% // collect selected uuids %>
function mmdCollectUuids(elForm) {
  if (elForm == null) elForm = mmdFindForm();
  var sUuids = "";
  var elHidden = document.getElementById(elForm.id+":mmdUuids");
  var aCheckBoxes = mmdFindCheckBoxes();
  for (var i=0;i<aCheckBoxes.length;i++) {
    if (aCheckBoxes[i][0].checked) {
      if (sUuids.length > 0) sUuids += ",";
      sUuids += aCheckBoxes[i][1];
    }
  }
  elHidden.value = sUuids;
  return sUuids;
}

<% // find the selected action dropdown %>
function mmdFindActionDropdown() {
  var elAction = null;
  var elForm = mmdFindForm();
  if (elForm != null) {
    elAction = document.getElementById(elForm.id+":mmdAction");
    if (elAction == null) elAction = document.getElementById(elForm.id+":mmdActionAdmin");
  }
  return elAction;
}

<% // find record checkboxes %>
function mmdFindCheckBoxes() {
  var aCheckBoxes = new Array();
  var aInputs = document.getElementsByTagName("input");
  if (aInputs != null) {
    for (var i=0;i<aInputs.length;i++) {
      var el = aInputs[i];
      if (el && el.name && (el.name.indexOf("mmdCheckRecord") != -1)) {
	      var sUuid = null;
	      var fOnClick = el.onclick;
	      if (fOnClick != null) {
	        var sTmp = fOnClick.toString();
	        var nIdx = sTmp.indexOf("mmdOnUuidChecked");
	        if (nIdx != -1) {
	          sTmp = sTmp.substring(nIdx);
	          nIdx = sTmp.indexOf("!");
	          if (nIdx != -1) {
	            sTmp = sTmp.substring(nIdx+1);
	            nIdx = sTmp.indexOf("!");
	            if (nIdx != -1) {
	              sUuid = sTmp.substring(0,nIdx);
	            }
	          }
	        }
	      }
	      if ((sUuid != null) && (sUuid.length > 0)) {
          var aCheckBox = new Array(el,sUuid);
          aCheckBoxes[aCheckBoxes.length] = aCheckBox;
	      }
      }
    }
  }
  return aCheckBoxes;
}

<% // find the form for this page %>
function mmdFindForm(sBaseName) {
  var elForm = null;
  var aForms = document.getElementsByTagName("form");
  if (aForms != null) {
    if (sBaseName == null) sBaseName = "mmdForm";
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

<% // action button clicked %>
function mmdOnActionButtonClicked() {
  var sMsgNone = "<%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker(
                     ).retrieveMessage("catalog.publication.manageMetadata.action.err.noneSelected")%>";
  var sMsgDel  = "<%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker(
                     ).retrieveMessage("catalog.publication.manageMetadata.action.delete.confirm")%>";
  var sMsgApplyToAll = "<%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker(
                     ).retrieveMessage("catalog.publication.manageMetadata.action.applyToAll.confirm")%>";

  var bContinue = false;
  var elForm = mmdFindForm();
  if (elForm != null) {
    var mmdApplyToAll = dojo.byId("mmdForm:mmdApplyToAll");
    if (mmdApplyToAll!=null && mmdApplyToAll.checked) {
      if (confirm(sMsgApplyToAll)) {
        var elExecuteActionToAll = mmdFindForm("mmdExecuteActionToAll");
        if (elExecuteActionToAll != null) {
          var elLaunch = document.getElementById(elExecuteActionToAll.id+":mmdLaunch");
          var elAction = document.getElementById(elExecuteActionToAll.id+":mmdAction");
          var elTransfer = document.getElementById(elExecuteActionToAll.id+":mmdTransfer");
          var elAcl = document.getElementById(elExecuteActionToAll.id+":mmdAcl");
          var elAclToggle = document.getElementById(elExecuteActionToAll.id+":mmdAclToggle");
          if ((elLaunch != null) && (elTransfer!=null) && (elAcl!=null) && (elAclToggle!=null)) {
            var action = dojo.byId("mmdForm:mmdAction");
            if (action==null) {
              action = dojo.byId("mmdForm:mmdActionAdmin");
            }
            if (action!=null) {
              elAction.value     = action.value;
              elTransfer.value   = dojo.query("#mmdForm\\:mmdTransfer").attr("value").join(",");
              elAcl.value        = dojo.query("#mmdForm\\:mmdAcl input:checked").attr("value").join("0x1E");
              elAclToggle.value  = dojo.query("#mmdForm\\:mmdAcclToggle").attr("value").join(",");
              elLaunch.click();
            }
          }
        }
      }
      bContinue = false;
    } else {
        var sUuids = mmdCollectUuids(elForm);
        if (sUuids.length == 0) {
          bContinue = false;
          alert(sMsgNone);
        } else {
          bContinue = true;
          var elActionDropdown = mmdFindActionDropdown();
          if (elActionDropdown != null) {
            var sAction = elActionDropdown.options[elActionDropdown.selectedIndex].value;
            if (sAction == "delete") {
              bContinue = confirm(sMsgDel);
            }
          }
        }
    }
  }
  return bContinue;
}

<% // on action changed %>
function mmdOnActionChanged(elSelect) {
  if (elSelect != null) {

    var sOpt = elSelect.options[elSelect.selectedIndex].value;
    var sId = elSelect.form.id+":mmdTransfer";
    var sDisplay = "none";
    if (sOpt == "transfer") sDisplay = "inline";
    var elTransfer = document.getElementById(sId);
    var elTransferLabel = document.getElementById(sId+"Label");
    if (elTransfer) elTransfer.style.display = sDisplay;
    if (elTransferLabel) elTransferLabel.style.display = sDisplay;

    sId = elSelect.form.id+":mmdSharingPanel";
    sDisplay = "none";
    if ((sOpt == "shareWith") || (sOpt == "dontShareWith")) {
      sDisplay = "inline";
    }
    var elSharingPanel = document.getElementById(sId);
    if (elSharingPanel) elSharingPanel.style.display = sDisplay;

    var sFormId = elSelect.form.id+":mmdAcl";
    var sFormIdToggle = elSelect.form.id+":mmdAclToggle";

    var elAclScrollPanel = document.getElementById(elSelect.form.id+":groupsScrollPanel");
    if(elAclScrollPanel == null) elAclScrollPanel = document.getElementById(elSelect.form.id+":toggleScrollPanel");
    sDisplay = "none";
    if (sOpt == "assignAcl"){
    		sDisplay = "inline";
        if (elAclScrollPanel!=null) {
          if(document.getElementById(elSelect.form.id+":mmdAclLabel")){
            elAclScrollPanel.style.display = "block";
            elAclScrollPanel.className = 'scrollPanel';
          }else if(document.getElementById(elSelect.form.id+":mmdAclToggleLabel")){
            elAclScrollPanel.style.display = sDisplay;
          }
        }
    }else{
        if (elAclScrollPanel!=null) {
          elAclScrollPanel.style.display = sDisplay;
        }
    }

    var elAcl = document.getElementById(sFormId);
    var elAclLabel = document.getElementById(sFormId+"Label");

    var elAclToggle = document.getElementById(sFormIdToggle);
    var elAclToggleLabel = document.getElementById(sFormIdToggle+"Label");

    if (elAcl) elAcl.style.display = sDisplay;
    if (elAclLabel) elAclLabel.style.display = sDisplay;


    if (elAclToggle) elAclToggle.style.display = sDisplay;
    if (elAclToggleLabel) elAclToggleLabel.style.display = sDisplay;
  }
}

<% // action clicked from one of the record icons %>
function mmdOnActionIconClicked(sAction,sUuid,sPublicationMethod) {
  var aCheckBoxes = mmdFindCheckBoxes();
  for (var i=0;i<aCheckBoxes.length;i++) {
    if (aCheckBoxes[i][1] == sUuid) aCheckBoxes[i][0].checked = true;
    else aCheckBoxes[i][0].checked = false;
  }
  var elForm = mmdFindForm();
  if (elForm != null) {
    mmdCollectUuids(elForm);
    var elActionDropdown = mmdFindActionDropdown();
    var elActionButton = document.getElementById(elForm.id+":mmdExecuteAction");
    if ((elActionDropdown != null) && (elActionButton != null)) {
      if (sAction == "delete") {
        for (var i=0;i<elActionDropdown.options.length;i++) {
          if (elActionDropdown.options[i].value == "delete") {
            if (elActionDropdown.selectedIndex != i) {
              elActionDropdown.selectedIndex = i;
              mmdOnActionChanged(elActionDropdown);
            }
            break;
          }
        }
        elActionButton.click();
      } else if (sAction == "view") {
        var sUrl = "<%=request.getContextPath()+"/catalog/publication/downloadMetadata.jsp"%>";
        sUrl += "?uuid="+sUuid+"&option=view";
        window.open(sUrl);
      } else if (sAction == "download") {
        var elFrame = document.getElementById("mmdDownloadFrame");
        if (elFrame != null) {
          var sUrl = "<%=request.getContextPath()+"/catalog/publication/downloadMetadata.jsp"%>";
          sUrl += "?uuid="+sUuid;
          elFrame.src = sUrl;
        }

      } else if (sAction == "edit") {
        var elEditForm = mmdFindForm("mmdLaunchEditorForm");
        
        try {
	        if (sdisuite.smartEditorUpdate.length > 0) { 
		        if ((sPublicationMethod != null) && (sPublicationMethod == "seditor")) {
		          elEditForm = null;
		          var elSdiLaunch = document.getElementById("frm-sdisuite-launch-editor");
		          if (elSdiLaunch != null) {
		            elSdiLaunch.identifier.value = sUuid;
								elSdiLaunch.ticket.value = sdisuite.tkn;
								elSdiLaunch.action = sdisuite.smartEditorUpdate;
		            elSdiLaunch.submit();
		          }
		        }
	        }
        } catch(e){
        	elEditForm = mmdFindForm("mmdLaunchEditorForm");
        }
        
        if (elEditForm != null) {
          var elLaunch = document.getElementById(elEditForm.id+":mmdLaunch");
          var elUuid = document.getElementById(elEditForm.id+":mmdUuid");
          if ((elLaunch != null) && (elUuid != null)) {
            elUuid.value = sUuid;
            elLaunch.click();
          }
        }

      } else if (sAction == "register") {
        var elEditForm = mmdFindForm("mmdLaunchRegistrationForm");
        if (elEditForm != null) {
          var elLaunch = document.getElementById(elEditForm.id+":mmdLaunch");
          var hrUuid = document.getElementById(elEditForm.id+":hrUuid");
          if ((elLaunch != null) && (hrUuid != null)) {
            hrUuid.value = sUuid;
            elLaunch.click();
          }
        }
      } else if (sAction == "history") {
        var elEditForm = mmdFindForm("mmdLaunchHistoryForm");
        if (elEditForm != null) {
          var elLaunch = document.getElementById(elEditForm.id+":mmdLaunch");
          var hrUuid = document.getElementById(elEditForm.id+":hrUuid");
          if ((elLaunch != null) && (hrUuid != null)) {
            hrUuid.value = sUuid;
            elLaunch.click();
          }
        }
      } else if (sAction == "synchronize") {
        var elEditForm = mmdFindForm("mmdLaunchSynchronizeForm");
        if (elEditForm != null) {
          var elLaunch = document.getElementById(elEditForm.id+":mmdLaunch");
          var hrUuid = document.getElementById(elEditForm.id+":mmdUuids");
          if ((elLaunch != null) && (hrUuid != null)) {
            hrUuid.value = sUuid;
            elLaunch.click();
          }
        }
      } else if (sAction == "cancel") {
        var elEditForm = mmdFindForm("mmdLaunchCancelForm");
        if (elEditForm != null) {
          var elLaunch = document.getElementById(elEditForm.id+":mmdLaunch");
          var hrUuid = document.getElementById(elEditForm.id+":mmdUuids");
          if ((elLaunch != null) && (hrUuid != null)) {
            hrUuid.value = sUuid;
            elLaunch.click();
          }
        }
      } else if (sAction == "showharvested") {
        var elEditForm = mmdFindForm("mmdShowHarvestedForm");
        if (elEditForm != null) {
          var elLaunch = document.getElementById(elEditForm.id+":mmdLaunch");
          var hrUuid = document.getElementById(elEditForm.id+":mmdUuids");
          if ((elLaunch != null) && (hrUuid != null)) {
            hrUuid.value = sUuid;
            elLaunch.click();
          }
        }
      } else if (sAction == "find") {
        var elEditForm = mmdFindForm("mmdFindParentForm");
        if (elEditForm != null) {
          var elLaunch = document.getElementById(elEditForm.id+":mmdLaunch");
          var hrUuid = document.getElementById(elEditForm.id+":mmdUuids");
          if ((elLaunch != null) && (hrUuid != null)) {
            hrUuid.value = sUuid;
            elLaunch.click();
          }
        }
      }
    }
  }
}

<% // on load %>
function mmdOnLoad() {
  var elForm = mmdFindForm();
  if (elForm != null) {
    mmdCollectUuids(elForm);

    var elScrollPanel = document.getElementById(elForm.id+":groupsScrollPanel");
    if(elScrollPanel == null) elScrollPanel = document.getElementById(elForm.id+":toggleScrollPanel");
    if(elScrollPanel != null) elScrollPanel.style.display = "none";
    var elSelect = document.getElementById(elForm.id+":mmdActionAdmin");
    if(elSelect == null) elSelect = document.getElementById(elForm.id+":mmdAction");
    mmdOnActionChanged(elSelect);
  }
}

<% // on submit %>
function mmdOnSubmit(elForm) {
  mmdCollectUuids(elForm);
}

<% // select/unselect a uuid %>
function mmdOnUuidChecked(elCheckBox) {
  if (elCheckBox != null) {
    mmdCollectUuids(elCheckBox.form);
  }
}

<% // select/unselect all %>
function mmdToggleCheckBoxes(elCheckBox) {
  if (elCheckBox != null) {
    var bChecked = elCheckBox.checked;
    var aCheckBoxes = mmdFindCheckBoxes();
    for (var i=0;i<aCheckBoxes.length;i++) {
      aCheckBoxes[i][0].checked = bChecked;
    }
    mmdCollectUuids(elCheckBox.form);
  }
}

<% // onclick of metadata access policy %>
function mmdOnSetAccessPolicy(action, currentPolicy){
	var elForm = mmdFindForm();
	var elSelect = document.getElementById(elForm.id+":mmdActionAdmin");
	if(elSelect == null) elSelect = document.getElementById(elForm.id+":mmdAction");
	for (var idx=0;idx<elSelect.options.length;idx++) {
            if (action == elSelect.options[idx].value) {
                  elSelect.selectedIndex=idx;
                  elSelect.options[idx].selected=true;
            }
  }
	mmdOnActionChanged(elSelect);
	var elAclGroups = elSelect.form.id+":mmdAcl";
	setCurrentMetadataAccessPolicy(elAclGroups, currentPolicy);
}

<% // sets current policies check boxes %>
function setCurrentMetadataAccessPolicy(elAclGroups, currentPolicy){
	var groups = document.getElementsByName(elAclGroups);
	var policies = currentPolicy.split("\u2715");

	// clear check boxes
	for (var idx=0;idx<groups.length;idx++) {
			groups[idx].checked = false;
	}

	for (var idx=0;idx<groups.length;idx++) {
			for(var i=0;i<policies.length;i++){
            if (policies[i] == groups[idx].value) {
            		groups[idx].checked = true;
            }
      }
  }
}
<% // clears current policies check boxes %>
function mmdClearAclSelection(){
  var elForm = mmdFindForm();
	var elSelect = document.getElementById(elForm.id+":mmdActionAdmin");
	if(elSelect == null) elSelect = document.getElementById(elForm.id+":mmdAction");
	var elAclGroups = elSelect.form.id+":mmdAcl";
	var groups = document.getElementsByName(elAclGroups);
 	// clear check boxes
	for (var idx=0;idx<groups.length;idx++) {
			groups[idx].checked = false;
	}
}
</script>

</f:verbatim>

<h:form id="mmdForm" onsubmit="return mmdOnSubmit(this);">
<h:inputHidden value="#{ManageMetadataController.prepareView}"/>
<h:inputHidden id="mmdUuids" value="#{ManageMetadataController.actionCriteria.selectedRecordIds}"/>

<% // query criteria section %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">

  <% // document title %>
  <h:outputLabel for="mmdTitle"
    value="#{gptMsg['catalog.publication.manageMetadata.label.title']}"/>
  <h:inputText id="mmdTitle" size="50" maxlength="128"
    value="#{ManageMetadataController.queryCriteria.title}"/>

  <% // document UUID %>
  <h:outputLabel for="mmdUuid"
    value="#{gptMsg['catalog.publication.manageMetadata.label.uuid']}"/>
  <h:inputText id="mmdUuid" size="50" maxlength="128"
    value="#{ManageMetadataController.queryCriteria.uuid}"/>

  <% // site UUID %>
  <h:outputLabel for="mmdSiteUuid"
    value="#{gptMsg['catalog.publication.manageMetadata.label.siteuuid']}"/>
  <h:inputText id="mmdSiteUuid" size="50" maxlength="128"
    value="#{ManageMetadataController.queryCriteria.siteUuid}"/>

  <% // document owner %>
  <h:outputLabel for="mmdOwner"
    rendered="#{not PageContext.roleMap['gptAdministrator']}"
    value="#{gptMsg['catalog.publication.manageMetadata.label.owner']}"/>
  <h:selectOneMenu id="mmdOwner"
    rendered="#{not PageContext.roleMap['gptAdministrator']}"
    value="#{ManageMetadataController.queryCriteria.owner}">
    <f:selectItems value="#{ManageMetadataController.selectablePublishers.items}"/>
  </h:selectOneMenu>
  <h:outputLabel for="mmdOwnerAdmin"
    rendered="#{PageContext.roleMap['gptAdministrator']}"
    value="#{gptMsg['catalog.publication.manageMetadata.label.owner']}"/>
  <h:selectOneMenu id="mmdOwnerAdmin"
    rendered="#{PageContext.roleMap['gptAdministrator']}"
    value="#{ManageMetadataController.queryCriteria.owner}">
    <f:selectItem
      itemValue=""
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.owner.any']}"/>
    <f:selectItems value="#{ManageMetadataController.selectablePublishers.items}"/>
  </h:selectOneMenu>
  
  <% // collection %>
  <h:outputLabel for="mmdCollection"
    rendered="#{ManageMetadataController.useCollections}"
    value="#{gptMsg['catalog.publication.manageMetadata.label.collection']}"/>
  <h:selectOneMenu id="mmdCollection"
    rendered="#{ManageMetadataController.useCollections}"
    value="#{ManageMetadataController.queryCriteria.collectionUuid}">
    <f:selectItem
      itemValue=""
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.collection.any']}"/>
    <f:selectItems value="#{ManageMetadataController.selectableCollections.items}"/>
  </h:selectOneMenu>

  <% // approval status  and publication method%>
  <h:outputLabel for="mmdStatus"
    value="#{gptMsg['catalog.publication.manageMetadata.label.status']}"/>
  <h:panelGroup>
	  <h:selectOneMenu id="mmdStatus"
	    value="#{ManageMetadataController.queryCriteria.approvalStatus}">
	    <f:selectItem
	      itemValue="any"
	      itemLabel="#{gptMsg['catalog.publication.manageMetadata.status.any']}"/>
	    <f:selectItem
	      itemValue="posted"
	      itemLabel="#{gptMsg['catalog.publication.manageMetadata.status.posted']}"/>
	    <f:selectItem
	      itemValue="approved"
	      itemLabel="#{gptMsg['catalog.publication.manageMetadata.status.approved']}"/>
	    <f:selectItem
	      itemValue="incomplete"
	      itemLabel="#{gptMsg['catalog.publication.manageMetadata.status.incomplete']}"/>
	    <f:selectItem
	      itemValue="disapproved"
	      itemLabel="#{gptMsg['catalog.publication.manageMetadata.status.disapproved']}"/>
	    <f:selectItem
	      itemValue="reviewed"
	      itemLabel="#{gptMsg['catalog.publication.manageMetadata.status.reviewed']}"/>
      <f:selectItem
        itemValue="draft"
        itemLabel="#{gptMsg['catalog.publication.manageMetadata.status.draft']}"/>
	  </h:selectOneMenu>
	  <h:outputText escape="false" value="&nbsp;"/>
    <h:outputLabel for="mmdMethod"
      value="#{gptMsg['catalog.publication.manageMetadata.label.method']}"/>
    <h:outputText escape="false" value="&nbsp;"/>
    <h:selectOneMenu id="mmdMethod"
      value="#{ManageMetadataController.queryCriteria.publicationMethod}">
      <f:selectItem
        itemValue="any"
        itemLabel="#{gptMsg['catalog.publication.manageMetadata.method.any']}"/>
      <f:selectItem
        itemValue="registration"
        itemLabel="#{gptMsg['catalog.publication.manageMetadata.method.registration']}"/>
      <f:selectItem
        itemValue="upload"
        itemLabel="#{gptMsg['catalog.publication.manageMetadata.method.upload']}"/>
      <f:selectItem
        itemValue="editor"
        itemLabel="#{gptMsg['catalog.publication.manageMetadata.method.editor']}"/>
      <f:selectItem
        itemValue="harvester"
        itemLabel="#{gptMsg['catalog.publication.manageMetadata.method.harvester']}"/>
      <f:selectItem
        itemValue="batch"
        itemLabel="#{gptMsg['catalog.publication.manageMetadata.method.batch']}"/>
      <f:selectItem
        itemValue="other"
        itemLabel="#{gptMsg['catalog.publication.manageMetadata.method.other']}"/>
    </h:selectOneMenu>
    <h:outputText escape="false" value="&nbsp;"/>
    <h:outputLabel for="hrType"
      value="#{gptMsg['catalog.harvest.manage.search.type']}"/>
    <h:outputText escape="false" value="&nbsp;"/>
    <h:selectOneMenu id="hrType"
      value="#{ManageMetadataController.queryCriteria.protocolType}">
      <f:selectItems value="#{ManageMetadataController.protocols}"/>
    </h:selectOneMenu>
  </h:panelGroup>

  <% // update date range %>
  <h:outputLabel for="mmdFromDate"
    value="#{gptMsg['catalog.publication.manageMetadata.label.fromDate']}"/>
  <h:panelGroup>
    <h:inputText id="mmdFromDate" size="10" maxlength="10"
      value="#{ManageMetadataController.queryCriteria.dateRange.fromDate.date}"/>
    <h:outputLabel for="mmdToDate"
      value="#{gptMsg['catalog.publication.manageMetadata.label.toDate']}"/>
    <h:outputText escape="false" value="&nbsp;"/>
    <h:inputText id="mmdToDate" size="10" maxlength="10"
      value="#{ManageMetadataController.queryCriteria.dateRange.toDate.date}"/>
    <h:outputText escape="false" value="&nbsp;"/>
    <h:outputText value="#{gptMsg['catalog.general.inputDateFormat']}"/>
  </h:panelGroup>

  <% // search button %>
  <h:outputText value=""/>
  <h:commandButton
    id="mmdSearch"
    value="#{gptMsg['catalog.publication.manageMetadata.button.search']}"
    actionListener="#{ManageMetadataController.processAction}">
    <f:attribute name="command" value="search" />
  </h:commandButton>
</h:panelGrid>

<% // separator %>
<f:verbatim><hr/></f:verbatim>

<% // action criteria section %>
<h:panelGrid
  columns="8"
  summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable"
  columnClasses="formLabelColumn,formInputColumn">

  <% // action to perform - publisher %>
  <h:outputLabel for="mmdAction"
    rendered="#{not PageContext.roleMap['gptAdministrator']}"
    value="#{gptMsg['catalog.publication.manageMetadata.label.action']}"/>
  <h:selectOneMenu id="mmdAction" onchange="mmdOnActionChanged(this);"
    rendered="#{not PageContext.roleMap['gptAdministrator']}"
    value="#{ManageMetadataController.actionCriteria.actionKey}">
    <f:selectItem
      itemValue="setPosted"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.setPosted']}"/>
     <f:selectItem
      itemValue="setIncomplete"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.setIncomplete']}"/>
    <f:selectItem
      itemValue="delete"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.delete']}"/>
    <f:selectItem
       itemValue="assignAcl"
       itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.acl']}" 
       itemDisabled="#{ManageMetadataController.metadataAccessPolicyConfig.policyUnrestricted}"/>
  </h:selectOneMenu>

  <% // action to perform - administrator %>
  <h:outputLabel for="mmdActionAdmin"
    rendered="#{PageContext.roleMap['gptAdministrator']}"
    value="#{gptMsg['catalog.publication.manageMetadata.label.action']}"/>
  <h:selectOneMenu id="mmdActionAdmin" onchange="mmdOnActionChanged(this);"
    rendered="#{PageContext.roleMap['gptAdministrator']}"
    value="#{ManageMetadataController.actionCriteria.actionKey}">
    <f:selectItem
      itemValue="setPosted"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.setPosted']}"/>
    <f:selectItem
      itemValue="setApproved"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.setApproved']}"/>
    <f:selectItem
      itemValue="setIncomplete"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.setIncomplete']}"/>
    <f:selectItem
      itemValue="setDisapproved"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.setDisapproved']}"/>
    <f:selectItem
      itemValue="setReviewed"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.setReviewed']}"/>
    <f:selectItem
      itemValue="transfer"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.transfer']}"/>
    <f:selectItem
      itemValue="delete"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.delete']}"/>
    <f:selectItem
      itemValue="shareWith"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.shareWith']}" 
      itemDisabled="#{not ManageMetadataController.useCollections}"/>
    <f:selectItem
      itemValue="dontShareWith"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.dontShareWith']}" 
      itemDisabled="#{not ManageMetadataController.useCollections}"/>
    <f:selectItem
      itemValue="assignAcl"
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.action.acl']}" 
      itemDisabled="#{ManageMetadataController.metadataAccessPolicyConfig.policyUnrestricted}"/>
  </h:selectOneMenu>

  <% // transfer to owner %>
  <h:outputLabel id="mmdTransferLabel" for="mmdTransfer"
    rendered="#{PageContext.roleMap['gptAdministrator']}"
    value="#{gptMsg['catalog.publication.manageMetadata.label.transfer']}"/>
  <h:selectOneMenu id="mmdTransfer"
    rendered="#{PageContext.roleMap['gptAdministrator']}"
    value="#{ManageMetadataController.actionCriteria.transferToOwner}">
    <f:selectItem itemValue=""
      itemLabel="#{gptMsg['catalog.publication.manageMetadata.prompt.transfer']}"/>
    <f:selectItems value="#{ManageMetadataController.selectablePublishers.items}"/>
  </h:selectOneMenu>
    
  <% // collection sharing (isPartOf)  %>
  <h:panelGroup id="mmdSharingPanel"
    rendered="#{ManageMetadataController.useCollections}">
	  <h:outputLabel id="mmdSharingLabel" for="mmdSharingCollectionUuid"
	    value="#{gptMsg['catalog.publication.manageMetadata.sharing.collection.label']}"/>
	  <h:selectOneMenu id="mmdSharingCollectionUuid"
	    value="#{ManageMetadataController.actionCriteria.sharingCollectionUuid}">
	    <f:selectItem 
	      itemValue=""
	      itemLabel="#{gptMsg['catalog.publication.manageMetadata.sharing.collection.prompt']}"/>
	    <f:selectItems value="#{ManageMetadataController.selectableCollections.items}"/>
	  </h:selectOneMenu>
  </h:panelGroup>

  <% // assign access level  %>
  <h:panelGroup>

    <h:panelGrid columns="2"  summary="#{gptMsg['catalog.general.designOnly']}"
      columnClasses="formLabelColumn,formInputColumn" cellspacing="0" cellpadding="0"
      rendered="#{(PageContext.roleMap['gptAdministrator'] || PageContext.roleMap['gptPublisher']) && !ManageMetadataController.metadataAccessPolicyConfig.policyUnrestricted && !ManageMetadataController.metadataAccessPolicyConfig.protectedGroupDN}">
      <h:outputLabel id="mmdAclLabel" for="mmdAcl"
        value="#{gptMsg['catalog.publication.manageMetadata.label.acl']}"/>

      <h:panelGroup id="groupsScrollPanel" styleClass="scrollPanel">
        <h:outputLabel styleClass="aclLink" onclick="mmdClearAclSelection();"
          title="#{gptMsg['catalog.publication.manageMetadata.acl.clear.tip']}"
          value="#{gptMsg['catalog.publication.manageMetadata.acl.clear']}" />
        <h:outputText escape="false" value="<br/>"/>
        <h:selectManyCheckbox id="mmdAcl" value="#{ManageMetadataController.actionCriteria.metadataAccessPolicyArray}"  layout="pageDirection">
           <f:selectItems value="#{ManageMetadataController.candidateGroups.items}"/>
        </h:selectManyCheckbox>
      </h:panelGroup>
    </h:panelGrid>

    <h:panelGrid  columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
    columnClasses="formLabelColumn,formInputColumn" cellspacing="0" cellpadding="0"
    rendered="#{(PageContext.roleMap['gptAdministrator'] || PageContext.roleMap['gptPublisher']) && !ManageMetadataController.metadataAccessPolicyConfig.policyUnrestricted && ManageMetadataController.actionCriteria.accessToGroupDN != null}">
         <h:outputLabel id="mmdAclToggleLabel" for="mmdAclToggle"
           value="#{gptMsg['catalog.publication.manageMetadata.label.acl']}"/>
         <h:panelGroup id="toggleScrollPanel" styleClass="toggleScrollPanel">
             <h:selectOneRadio id="mmdAclToggle"
               value="#{ManageMetadataController.actionCriteria.toggleMetadataAccessPolicy}">
               <f:selectItem
                itemValue="Unrestricted"
                itemLabel="#{gptMsg['catalog.publication.manageMetadata.label.unrestricted']}"/>
                <f:selectItem
                  itemValue="#{ManageMetadataController.actionCriteria.accessToGroupDN}"
                  itemLabel="#{gptMsg['catalog.publication.manageMetadata.label.restricted']}"/>
             </h:selectOneRadio>
         </h:panelGroup>
    </h:panelGrid>

  </h:panelGroup>

  <% // execute action button %>
  <h:outputText escape="false" value="&nbsp;&nbsp;&nbsp;&nbsp;"/>
  <h:commandButton
    id="mmdExecuteAction"
    onclick="return mmdOnActionButtonClicked()"
    value="#{gptMsg['catalog.publication.manageMetadata.button.executeAction']}"
    actionListener="#{ManageMetadataController.processAction}">
    <f:attribute name="command" value="executeAction" />
  </h:commandButton>

</h:panelGrid>

<% // apply to all %>
<h:panelGrid
  columns="2"
  summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable"
  columnClasses="formLabelColumn,formInputColumn"
  rendered="#{PageContext.roleMap['gptAdministrator'] and ManageMetadataController.allowApplyToAll and not ManageMetadataController.queryCriteria.isEmpty and ManageMetadataController.queryResult.hasRecords}">
    <h:outputLabel for="mmdApplyToAll" value="#{gptMsg['catalog.publication.manageMetadata.button.applyToAll']}"/>
    <h:selectBooleanCheckbox id="mmdApplyToAll" value="false"/>
</h:panelGrid>

<% // separator %>
<f:verbatim><hr/></f:verbatim>

<% // page cursor - top %>
<h:panelGroup id="mmdPageCursorTop" styleClass="pageCursor"
  binding="#{ManageMetadataController.pageCursorPanel.topHtmlPanelGroup}"/>

<% // records section %>
<h:dataTable
  id="mmdRecords"
  value="#{ManageMetadataController.queryResult.records}"
  var="record"
  styleClass="grid"
  rowClasses="rowOdd,rowEven"
  columnClasses=",actionColumnStyle,,,,,,,"
  cellspacing="0"
  cellpadding="2">

  <% // selection check boxes %>
  <h:column>
    <f:facet name="header">
      <h:selectBooleanCheckbox
        id="mmdCheckAll"
        title="#{gptMsg['catalog.publication.manageMetadata.header.check.tip']}"
        value="#{ManageMetadataController.alwaysFalse}"
        onclick="mmdToggleCheckBoxes(this);"/>
    </f:facet>
    <h:selectBooleanCheckbox
      id="mmdCheckRecord"
      value="false"
      onclick="mmdOnUuidChecked(this,'!#{record.uuid}!');">
      <f:attribute name="uuid" value="1"/>
    </h:selectBooleanCheckbox>
  </h:column>

  <% // action icons %>
  <h:column>
    <f:facet name="header">
      <h:outputText
        value="#{gptMsg['catalog.publication.manageMetadata.header.action']}" />
    </f:facet>
    <h:graphicImage
      alt="#{gptMsg['catalog.publication.manageMetadata.action.view.tip']}"
      title="#{gptMsg['catalog.publication.manageMetadata.action.view.tip']}"
      url="/catalog/images/mmd_view.gif"
      onclick="mmdOnActionIconClicked('view','#{record.uuid}');"/>
    <h:graphicImage rendered="#{record.protocol ne null}"
      alt="#{gptMsg['catalog.harvest.manage.action.history.tip']}"
      title="#{gptMsg['catalog.harvest.manage.action.history.tip']}"
      url="/catalog/images/mr_history.gif"
      onclick="mmdOnActionIconClicked('history','#{record.uuid}');"/>
    <h:graphicImage rendered="#{record.protocol eq null}"
      alt="#{gptMsg['catalog.publication.manageMetadata.action.download.tip']}"
      title="#{gptMsg['catalog.publication.manageMetadata.action.download.tip']}"
      url="/catalog/images/mmd_download.gif"
      onclick="mmdOnActionIconClicked('download','#{record.uuid}');"/>
    <h:graphicImage rendered="#{record.canEdit and record.protocol eq null}"
	    alt="#{gptMsg['catalog.publication.manageMetadata.action.edit.tip']}"
	    title="#{gptMsg['catalog.publication.manageMetadata.action.edit.tip']}"
	    url="/catalog/images/mmd_edit.gif"
	    onclick="mmdOnActionIconClicked('edit','#{record.uuid}','#{record.publicationMethod}');"/>
    <h:graphicImage rendered="#{record.canEdit and record.protocol ne null}"
	    alt="#{gptMsg['catalog.publication.manageMetadata.action.register.tip']}"
	    title="#{gptMsg['catalog.publication.manageMetadata.action.register.tip']}"
	    url="/catalog/images/mr_edit.gif"
	    onclick="mmdOnActionIconClicked('register','#{record.uuid}');"/>
    <h:graphicImage rendered="#{not record.canEdit and record.protocol eq null}"
      alt="#{gptMsg['catalog.publication.manageMetadata.action.edit.tip']}"
      title="#{gptMsg['catalog.publication.manageMetadata.action.edit.tip']}"
      url="/catalog/images/mmd_edit_off.gif"/>
    <h:graphicImage rendered="#{not record.canEdit and record.protocol ne null}"
      alt="#{gptMsg['catalog.publication.manageMetadata.action.register.tip']}"
      title="#{gptMsg['catalog.publication.manageMetadata.action.register.tip']}"
      url="/catalog/images/mmd_edit_off.gif"/>
    <h:graphicImage
      alt="#{gptMsg['catalog.publication.manageMetadata.action.delete.tip']}"
      title="#{gptMsg['catalog.publication.manageMetadata.action.delete.tip']}"
      url="/catalog/images/mmd_delete.gif"
      onclick="mmdOnActionIconClicked('delete','#{record.uuid}');"/>
    <h:graphicImage rendered="#{record.protocol ne null and ((record.approvalStatus=='approved' or record.approvalStatus=='reviewed') and record.synchronizable) and (record.recentJobStatus eq null or record.recentJobStatus=='Unavailable' or record.recentJobStatus=='Completed') and not record.executingLocally}"
      alt="#{gptMsg['catalog.harvest.manage.action.harvest.sync.tip']}"
      title="#{gptMsg['catalog.harvest.manage.action.harvest.sync.tip']}"
      url="/catalog/images/mr_fullharvest.gif"
      onclick="mmdOnActionIconClicked('synchronize','#{record.uuid}');"/>
    <h:graphicImage rendered="#{record.protocol ne null and not ((record.approvalStatus=='approved' or record.approvalStatus=='reviewed') and record.synchronizable) and (record.recentJobStatus eq null or record.recentJobStatus=='Unavailable' or record.recentJobStatus=='Completed') and not record.executingLocally}"
      alt="#{gptMsg['catalog.harvest.manage.action.harvest.sync.tip']}"
      title="#{gptMsg['catalog.harvest.manage.action.harvest.sync.tip']}"
      url="/catalog/images/mr_fullharvest_inact.gif"/>
    <h:graphicImage rendered="#{record.protocol ne null and (not (record.recentJobStatus eq null or record.recentJobStatus=='Unavailable' or record.recentJobStatus=='Completed') or record.executingLocally)}"
      alt="#{gptMsg['catalog.harvest.manage.action.harvest.cancel.tip']}"
      title="#{gptMsg['catalog.harvest.manage.action.harvest.cancel.tip']}"
      url="/catalog/images/mr_fullharvest_cancel.gif"
      onclick="mmdOnActionIconClicked('cancel','#{record.uuid}');"/>
    <h:graphicImage rendered="#{record.protocol ne null and record.protocol.kind!='AGP2AGP' and record.protocol.kind!='AGS2AGP'}"
      alt="#{gptMsg['catalog.publication.manageMetadata.action.showharvested.tip']}"
      title="#{gptMsg['catalog.publication.manageMetadata.action.showharvested.tip']}"
      url="/catalog/images/mmd_showharvested.gif"
      onclick="mmdOnActionIconClicked('showharvested','#{record.uuid}');"/>
    <h:graphicImage rendered="#{record.protocol eq null and record.siteUuid!=''}"
      alt="#{gptMsg['catalog.publication.manageMetadata.action.findparent.tip']}"
      title="#{gptMsg['catalog.publication.manageMetadata.action.findparent.tip']}"
      url="/catalog/images/mmd_findparent.gif"
      onclick="mmdOnActionIconClicked('find','#{record.siteUuid}');"/>
  </h:column>

  <% // document title %>
  <h:column>
    <f:facet name="header">
      <h:commandLink
        styleClass="#{ManageMetadataController.queryCriteria.sortOption.styleMap['title']}"
        value="#{gptMsg['catalog.publication.manageMetadata.header.title']}"
        actionListener="#{ManageMetadataController.processAction}">
        <f:attribute name="command" value="sort"/>
        <f:attribute name="column" value="title"/>
        <f:attribute name="defaultDirection" value="asc"/>
      </h:commandLink>
    </f:facet>
    <h:outputLabel for="mmdCheckRecord" value="#{record.title}"/>
  </h:column>

  <% // document owner %>
  <h:column>
    <f:facet name="header">
      <h:outputText
        value="#{gptMsg['catalog.publication.manageMetadata.header.owner']}" />
    </f:facet>
    <h:outputText value="#{record.ownerName}"/>
  </h:column>

  <% // approval status %>
  <h:column>
    <f:facet name="header">
      <h:commandLink
        styleClass="#{ManageMetadataController.queryCriteria.sortOption.styleMap['status']}"
        value="#{gptMsg['catalog.publication.manageMetadata.header.status']}"
        actionListener="#{ManageMetadataController.processAction}">
        <f:attribute name="command" value="sort"/>
        <f:attribute name="column" value="status"/>
        <f:attribute name="defaultDirection" value="asc"/>
      </h:commandLink>
    </f:facet>
    <h:outputText value="#{record.approvalStatusMsg}"
      rendered="#{empty record.collectionMembership}"/>
    <h:outputText value="#{record.approvalStatusMsg}"
      title="#{record.collectionMembership}"
      style="cursor:help;"
      rendered="#{not empty record.collectionMembership}"/>
  </h:column>

  <% // publication method %>
  <h:column>
    <f:facet name="header">
      <h:commandLink
        styleClass="#{ManageMetadataController.queryCriteria.sortOption.styleMap['method']}"
        value="#{gptMsg['catalog.publication.manageMetadata.header.method']}"
        actionListener="#{ManageMetadataController.processAction}">
        <f:attribute name="command" value="sort"/>
        <f:attribute name="column" value="method"/>
        <f:attribute name="defaultDirection" value="asc"/>
      </h:commandLink>
    </f:facet>
    <h:outputText value="#{record.publicationMethodMsg}"/>
  </h:column>

  <% // update date %>
  <h:column>
    <f:facet name="header">
      <h:commandLink
        styleClass="#{ManageMetadataController.queryCriteria.sortOption.styleMap['updateDate']}"
        value="#{gptMsg['catalog.publication.manageMetadata.header.updateDate']}"
        actionListener="#{ManageMetadataController.processAction}">
        <f:attribute name="command" value="sort"/>
        <f:attribute name="column" value="updatedate"/>
        <f:attribute name="defaultDirection" value="desc"/>
      </h:commandLink>
    </f:facet>
    <h:outputText value="#{record.formattedUpdateDate}"/>
  </h:column>

  <% // metadata access policy %>
  <h:column rendered="#{!ManageMetadataController.policyUnrestricted}">
    <f:facet name="header">
      <h:commandLink
        styleClass="#{ManageMetadataController.queryCriteria.sortOption.styleMap['acl']}"
        value="#{gptMsg['catalog.publication.manageMetadata.header.access']}"
        actionListener="#{ManageMetadataController.processAction}">
        <f:attribute name="command" value="sort"/>
        <f:attribute name="column" value="acl"/>
        <f:attribute name="defaultDirection" value="asc"/>
      </h:commandLink>
    </f:facet>
   <h:outputLabel styleClass="aclLink" onclick="mmdOnSetAccessPolicy('assignAcl','#{record.currentMetadataAccessPolicyKeys}');" title="#{record.currentMetadataAccessPolicy}" value="#{record.metadataAccessPolicyType}"/>
   
  </h:column>

</h:dataTable>

<% // page cursor - bottom %>
<h:panelGroup id="mmdPageCursorBottom" styleClass="pageCursor"
  binding="#{ManageMetadataController.pageCursorPanel.bottomHtmlPanelGroup}"/>

<% // trigger the action changed event %>
<f:verbatim>
  <script type="text/javascript" language="Javascript">mmdOnLoad();</script>
</f:verbatim>

<gpt:DatePickerConfig/>
<script type="text/javascript">
$(document).ready(function(){
    var dpCfg = new DatePickerConfig();
    dpCfg.initialize();
    dpCfg.attach("mmdForm:mmdFromDate");
    dpCfg.attach("mmdForm:mmdToDate");
});
</script>

</h:form>

<h:form id="mmdLaunchEditorForm" style="display:none">
	<h:commandButton id="mmdLaunch"
      value="launch"
	  action="#{EditMetadataController.getNavigationOutcome}"
	  actionListener="#{EditMetadataController.processAction}">
	  <f:attribute name="command" value="open"/>
	</h:commandButton>
	<h:inputHidden id="mmdUuid" value="#{EditMetadataController.openDocumentUuid}"/>
</h:form>

<h:form id="mmdLaunchRegistrationForm" style="display:none">
	<h:commandButton id="mmdLaunch"
      value="launch"
	  action="#{EditMetadataController.getNavigationOutcome}"
	  actionListener="#{EditMetadataController.processAction}">
	  <f:attribute name="command" value="register"/>
	</h:commandButton>
    <h:inputHidden id="hrUuid" value="#{HarvestController.criteria.actionCriteria.uuid}"/>
</h:form>

<h:form id="mmdLaunchHistoryForm" style="display:none">
	<h:commandButton id="mmdLaunch"
      value="launch"
	  action="#{EditMetadataController.getNavigationOutcome}"
	  actionListener="#{EditMetadataController.processAction}">
	  <f:attribute name="command" value="history"/>
	</h:commandButton>
    <h:inputHidden id="hrUuid" value="#{EditMetadataController.openDocumentUuid}"/>
</h:form>

<h:form id="mmdLaunchSynchronizeForm" style="display:none" onsubmit="return confirm('#{gptMsg['catalog.harvest.manage.action.harvest.sync.confirm']}')">
	<h:commandButton id="mmdLaunch"
      value="launch"
	  action="#{ManageMetadataController.getNavigationOutcome}"
	  actionListener="#{ManageMetadataController.processAction}">
	  <f:attribute name="command" value="synchronize"/>
	</h:commandButton>
    <h:inputHidden id="mmdUuids" value="#{ManageMetadataController.actionCriteria.selectedRecordIds}"/>
</h:form>

<h:form id="mmdLaunchCancelForm" style="display:none" onsubmit="return confirm('#{gptMsg['catalog.harvest.manage.action.harvest.cancel.confirm']}')">
	<h:commandButton id="mmdLaunch"
      value="launch"
	  action="#{ManageMetadataController.getNavigationOutcome}"
	  actionListener="#{ManageMetadataController.processAction}">
	  <f:attribute name="command" value="cancel"/>
	</h:commandButton>
    <h:inputHidden id="mmdUuids" value="#{ManageMetadataController.actionCriteria.selectedRecordIds}"/>
</h:form>

<h:form id="mmdShowHarvestedForm" style="display:none">
	<h:commandButton id="mmdLaunch"
      value="launch"
	  action="#{ManageMetadataController.getNavigationOutcome}"
	  actionListener="#{ManageMetadataController.processAction}">
	  <f:attribute name="command" value="showharvested"/>
	</h:commandButton>
    <h:inputHidden id="mmdUuids" value="#{ManageMetadataController.actionCriteria.selectedRecordIds}"/>
</h:form>

<h:form id="mmdFindParentForm" style="display:none">
	<h:commandButton id="mmdLaunch"
      value="launch"
	  action="#{ManageMetadataController.getNavigationOutcome}"
	  actionListener="#{ManageMetadataController.processAction}">
	  <f:attribute name="command" value="find"/>
	</h:commandButton>
    <h:inputHidden id="mmdUuids" value="#{ManageMetadataController.actionCriteria.selectedRecordIds}"/>
</h:form>


  <h:form id="mmdExecuteActionToAll" style="display:none" rendered="#{PageContext.roleMap['gptAdministrator'] and not ManageMetadataController.queryCriteria.isEmpty and ManageMetadataController.queryResult.hasRecords}">
	<h:commandButton id="mmdLaunch"
      value="launch"
	  action="#{ManageMetadataController.getNavigationOutcome}"
	  actionListener="#{ManageMetadataController.processAction}">
	  <f:attribute name="command"    value="executeAction"/>
	  <f:attribute name="applyToAll" value="true"/>
	</h:commandButton>
    <h:inputHidden id="mmdAction"    value="#{ManageMetadataController.actionCriteria.actionKey}"/>
    <h:inputHidden id="mmdTransfer"  value="#{ManageMetadataController.actionCriteria.transferToOwner}"/>
    <h:inputHidden id="mmdAcl"       value="#{ManageMetadataController.actionCriteria.metadataAccessPolicyString}"/>
    <h:inputHidden id="mmdAclToggle" value="#{ManageMetadataController.actionCriteria.toggleMetadataAccessPolicy}"/>
    <h:inputHidden id="criteria"     value="#{ManageMetadataController.queryCriteriaAsEncrypedString}"/>
</h:form>

<% // metadata download frame %>
<f:verbatim>
  <iframe id="mmdDownloadFrame" src="" width="1" height="1" style="display:none"
    title="<%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker(
          ).retrieveMessage("catalog.publication.manageMetadata.action.download.frame")%>"></iframe>
</f:verbatim>

<% // sdi.suite editor %>
<f:verbatim>
	<form id="frm-sdisuite-launch-editor" name="frm-sdisuite-launch-editor" style="display:none"
	  action="none.html" method="post" target="_blank">
	  <input type="hidden" id="ticket" name="ticket" value=""/>
	  <input type="hidden" id="identifier" name="identifier" value=""/>
	  <input type="hidden" id="base64" name="base64" value="true"/>
	  <input type="hidden" id="request" name="request" value="update"/>
	</form>
</f:verbatim>

