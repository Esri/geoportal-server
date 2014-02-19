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
<% // historyBody.jsp - Manage harvest repositories page (JSF body) %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<gpt:DatePickerConfig/>

<f:verbatim>

<script type="text/javascript">
$(document).ready(function(){
    var dpCfg = new DatePickerConfig();
    dpCfg.initialize();
    
    dpCfg.attach("hhForm:heFromDate");
    dpCfg.attach("hhForm:heToDate");
});
  
// class CommonTableHandler.
//
// CommonTableHandler provides common functionality used in forms with grid
// table, and action button.
//
// @param sFormId         - id of the form where all the controls are places
// @param sUuidsId        - id of hiden input field where uuid's of the selected 
//                          items will be placed
// @param sActionId       - id of the control having available actions
// @param sActionButtonId - id of the button on the form used to execute action

function CommonTableHandler(sFormId,sUuidsId,sActionId,sActionButtonId) {

// Gets DOM node of the form.
this.getFormNode = function() {
  return document.getElementById(sFormId);
}

// Gets DOM node of the hiden input field used to store selected records uuid's
this.getUuidsNode = function() {
  return document.getElementById(sFormId + ":" + sUuidsId);
}

// Gets DOM node of the available actions control
this.getActionNode = function() {
  return document.getElementById(sFormId + ":" + sActionId);
}

// Gets DOM node of the action button
this.getActionButtonNode = function() {
  return document.getElementById(sFormId + ":" + sActionButtonId);
}

// Gets currently selected action from the action control
this.getSelectedAction = function() {
  var sSelectedAction = "";
  var actionNode = this.getActionNode();
  if (actionNode!=null) {
    for (i=0; i<actionNode.options.length; i++) {
      var opt = actionNode.options[i];
      if (opt.selected) {
        sSelectedAction = opt.value;
        break;
      }
    }  
  }
  return sSelectedAction;
};

// Sets currently selecte action in the action control
this.setSelectedAction = function(sSelectedAction) {
  var actionNode = this.getActionNode();
  if (actionNode!=null) {
    for (i=0; i<actionNode.options.length; i++) {
      var opt = actionNode.options[i];
      if (opt.value==sSelectedAction) {
        opt.selected = true;
      }
    }  
  }
}

// Finds all the check boxes associated with the individual records
this.findCheckBoxes = function() {
  var aCheckBoxes = new Array();
  var aInputs = document.getElementsByTagName("input");
  if (aInputs != null) {
    for (var i=0;i<aInputs.length;i++) {
      var el = aInputs[i];
      if (el && el.name) {
        var sUuid = null;
        var fOnClick = el.onclick;
        if (fOnClick != null) {
          var sTmp = fOnClick.toString();
          var nIdx = sTmp.indexOf("onCheckRecordClicked");
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
          if ((sUuid != null) && (sUuid.length > 0)) {
            var aCheckBox = new Array(el,sUuid);
            aCheckBoxes[aCheckBoxes.length] = aCheckBox;
          }
        }
      }
    }
  }
  return aCheckBoxes;
}

// Collects all the uuid's of the selected records and stores it in the hidden
// input fiels.
this.collectUuids = function() {
  var sUuids = "";
  var uuids = this.getUuidsNode();
  if (uuids!=null) {
    var aCheckBoxes = this.findCheckBoxes();
    for (var i=0;i<aCheckBoxes.length;i++) {
      if (aCheckBoxes[i][0].checked) {
        var sUuid = aCheckBoxes[i][1];
        if ((sUuid != null) && (sUuid.length > 0)) {
          if (sUuids.length > 0) sUuids += ",";
          sUuids += sUuid;
        }
      }
    }
    uuids.value = sUuids;
  }
}

// Invokes action after the main check box has been clicked
this.onCheckAllClicked = function(checked) {
  var aCheckBoxes = this.findCheckBoxes();
  for (var i=0;i<aCheckBoxes.length;i++) {
    aCheckBoxes[i][0].checked = checked;
  }
  this.selectBestAction(checked? aCheckBoxes.length: 0);
}

// Invokes action after any check box associated with the individual record
// has been clicked.
this.onCheckRecordClicked = function(sUuid) {
  var aCheckBoxes = this.findCheckBoxes();
  var checkedNumber = 0;
  for (var i=0;i<aCheckBoxes.length;i++) {
    if (aCheckBoxes[i][0].checked) {
      checkedNumber++;
    }
  }
  this.selectBestAction(checkedNumber);
}

// Invokes action after icon representing action has been clicked.
this.onIconClicked = function(sAction,sUuid) {
  var aCheckBoxes = this.findCheckBoxes();
  for (var i=0;i<aCheckBoxes.length;i++) {
    if (aCheckBoxes[i][1] == sUuid) 
      aCheckBoxes[i][0].checked = true;
    else 
      aCheckBoxes[i][0].checked = false;
  }
  var elActionButton = this.getActionButtonNode();
  if (elActionButton!=null) {
    this.setSelectedAction(sAction);
    elActionButton.click();
  }
}

// Select most appropriate action to the number of the selected records
this.selectBestAction = function(checkedNumber) {
}

// count checked check boxes
this.countChecked = function() {
  var aCheckBoxes = this.findCheckBoxes();
  var count = 0;
  for (var i=0;i<aCheckBoxes.length;i++) {
    if (aCheckBoxes[i][0].checked) {
      count++;
    }
  }
  return count;
}

}
  
function ConcreteTableHandler() {
  this.selectBestAction = function(checkedNumber) {
    if (checkedNumber<=0) {
      this.setSelectedAction("viewreport");
    } else {
      this.setSelectedAction("delete");
    }
  }
}

ConcreteTableHandler.prototype = 
  new CommonTableHandler("hhForm", "hhUuids", "hhAction", "executeAction");
var actionHanlder = new ConcreteTableHandler();

var searchRequested = false;
  
<% // called at form submission  %>
function hhOnSubmit(hhForm,sConfirmationMessage) {
  if (!searchRequested && actionHanlder.getSelectedAction()=="delete") {
    if (actionHanlder.countChecked()>0 && !confirm(sConfirmationMessage)) {
      return false;
    }
  }
  searchRequested = false;
  actionHanlder.collectUuids();
  return true;
}

function hhSearchRequest() {
  searchRequested = true;
}

</script>

</f:verbatim>

<h:inputHidden value="#{HarvestHistoryController.listEventsPostView}"/>

<h:form id="hhForm" onsubmit="return hhOnSubmit(this,'#{gptMsg['catalog.harvest.manage.history.actions.delete.confirm']}');">
  
<h:inputHidden id="hhUuids" 
value="#{HarvestHistoryController.criteria.actionCriteria.selectedRecordIds}"/>

<% // identification %>
<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">

<h:outputText
value="#{gptMsg['catalog.harvest.manage.history.repositoryName']}"/>
<h:outputText value="#{HarvestHistoryController.result.queryResult.name}"/>

<h:outputText
value="#{gptMsg['catalog.harvest.manage.history.repositoryUuid']}"/>
<h:outputText value="#{HarvestHistoryController.result.queryResult.uuid}"/>
    
<h:outputText
value="#{gptMsg['catalog.harvest.manage.history.repositoryUrl']}"/>
<h:outputText value="#{HarvestHistoryController.result.queryResult.url}"/>

<h:outputText
value="#{gptMsg['catalog.harvest.manage.history.repositoryProtocol']}"/>
<h:outputText 
value="#{HarvestHistoryController.result.queryResult.protocolTypeAsString}"/>
 

</h:panelGrid>

<f:verbatim><hr/></f:verbatim>

<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}"
  styleClass="formTable" columnClasses="formLabelColumn,formInputColumn">


<h:outputLabel for="heUuid" 
  value="#{gptMsg['catalog.harvest.manage.history.search.uuid']}"/>
<h:inputText id="heUuid" size="50" maxlength="128" 
  value="#{HarvestHistoryController.criteria.queryCriteria.eventUuid}"/>

<h:outputLabel for="heFromDate" 
  value="#{gptMsg['catalog.harvest.manage.history.search.fromDate']}"/>
<h:panelGroup>
  <h:inputText id="heFromDate" size="10" maxlength="10" 
    value="#{HarvestHistoryController.criteria.queryCriteria.dateRange.fromDate.date}"/>
  <h:outputLabel for="heToDate" 
    value="#{gptMsg['catalog.harvest.manage.history.search.toDate']}"/>
  <h:outputText escape="false" value="&nbsp;"/>
  <h:inputText id="heToDate" size="10" maxlength="10" 
    value="#{HarvestHistoryController.criteria.queryCriteria.dateRange.toDate.date}"/>
  <h:outputText escape="false" value="&nbsp;"/>
  <h:outputText value="#{gptMsg['catalog.general.inputDateFormat']}"/>
</h:panelGroup>


<% // search button %>
<h:outputText value=""/>
<h:commandButton  
  value="#{gptMsg['catalog.harvest.manage.history.search.button']}"
  onclick="hhSearchRequest();"> 
</h:commandButton>  

</h:panelGrid>

<f:verbatim><hr/></f:verbatim>

<h:panelGrid>
<h:panelGroup>
  
<h:outputLabel for="hhAction" 
  value="#{gptMsg['catalog.harvest.manage.history.actions']}"/>

<h:selectOneMenu id="hhAction"
  value="#{HarvestHistoryController.criteria.actionCriteria.actionAsString}">
  <f:selectItem 
    itemValue="viewreport" 
    itemLabel="#{gptMsg['catalog.harvest.manage.history.actions.viewreport']}"/>
  <f:selectItem 
    itemValue="delete" 
    itemLabel="#{gptMsg['catalog.harvest.manage.history.actions.delete']}"/>
</h:selectOneMenu>

<h:commandButton id="executeAction" 
action="#{HarvestHistoryController.onClickButtonHistoryExecuteAction}"
value="#{gptMsg['catalog.harvest.manage.history.actions.execute']}"/>
  
</h:panelGroup>
</h:panelGrid>

<f:verbatim><hr/></f:verbatim>

<% // page cursor - top %>
<h:panelGroup id="hhPageCursorTop" styleClass="pageCursor"
  binding="#{HarvestHistoryController.pageCursorPanel.topHtmlPanelGroup}"/>

<h:dataTable 
id="history"
value="#{HarvestHistoryController.result.queryResult.records}" 
var="hist"
styleClass="grid" 
rowClasses="rowOdd,rowEven"
cellspacing="0" 
cellpadding="2">

<h:column>
  <f:facet name="header">
    <h:selectBooleanCheckbox 
      id="hhCheckAll"
      title=" "
      value="#{HarvestHistoryController.alwaysFalse}" 
      onclick="actionHanlder.onCheckAllClicked(this.checked);"/>
  </f:facet>
  <h:selectBooleanCheckbox 
    id="hhCheckRecord"
    value="#{hist.isSelected}"
    onclick="actionHanlder.onCheckRecordClicked('!#{hist.uuid}!');"/>
</h:column>  

<h:column>
  <f:facet name="header">
    <h:outputText 
    value="#{gptMsg['catalog.harvest.manage.history.headeraction']}"/>
  </f:facet>
  <h:graphicImage 
    alt="#{gptMsg['catalog.harvest.manage.history.actions.viewreport.tip']}"
    title="#{gptMsg['catalog.harvest.manage.history.actions.viewreport.tip']}"
    url="/catalog/images/mr_report.gif"
    onclick="actionHanlder.onIconClicked('viewreport','#{hist.uuid}');"/>
  <h:graphicImage 
    alt="#{gptMsg['catalog.harvest.manage.history.actions.delete.tip']}"
    title="#{gptMsg['catalog.harvest.manage.history.actions.delete.tip']}"
    url="/catalog/images/mr_delete.gif"
    onclick="actionHanlder.onIconClicked('delete','#{hist.uuid}');"/>
</h:column>  

<h:column>
 <f:facet name="header">
    <h:commandLink 
      value="#{gptMsg['catalog.harvest.manage.history.date']}"
      styleClass="#{HarvestHistoryController.sortDirectionStyleMap['harvest_date']}"
      actionListener="#{HarvestHistoryController.handleListOfEvents}">
      <f:attribute name="command" value="sort"/>
      <f:attribute name="column" value="harvest_date"/>
      <f:attribute name="defaultDirection" value="asc"/>
    </h:commandLink>
  </f:facet>
  <h:outputLabel for="hhCheckRecord" value="#{hist.harvestDate}">
    <f:convertDateTime type="date" pattern="yyyy-MM-dd"/>
  </h:outputLabel>
</h:column>  

<h:column>
  <f:facet name="header">
    <h:commandLink 
      value="#{gptMsg['catalog.harvest.manage.history.harvested']}"
      styleClass="#{HarvestHistoryController.sortDirectionStyleMap['harvested_count']}"
      actionListener="#{HarvestHistoryController.handleListOfEvents}">
      <f:attribute name="command" value="sort"/>
      <f:attribute name="column" value="harvested_count"/>
      <f:attribute name="defaultDirection" value="asc"/>
    </h:commandLink>
  </f:facet>
  <h:outputText value="#{hist.harvestedCount}"/>
</h:column>  

<h:column>
  <f:facet name="header">
    <h:commandLink 
      value="#{gptMsg['catalog.harvest.manage.history.validated']}"
      styleClass="#{HarvestHistoryController.sortDirectionStyleMap['validated_count']}"
      actionListener="#{HarvestHistoryController.handleListOfEvents}">
      <f:attribute name="command" value="sort"/>
      <f:attribute name="column" value="validated_count"/>
      <f:attribute name="defaultDirection" value="asc"/>
    </h:commandLink>
  </f:facet>
  <h:outputText value="#{hist.validatedCount}"/>
</h:column>  

<h:column>
  <f:facet name="header">
    <h:commandLink 
      value="#{gptMsg['catalog.harvest.manage.history.published']}"
      styleClass="#{HarvestHistoryController.sortDirectionStyleMap['published_count']}"
      actionListener="#{HarvestHistoryController.handleListOfEvents}">
      <f:attribute name="command" value="sort"/>
      <f:attribute name="column" value="published_count"/>
      <f:attribute name="defaultDirection" value="asc"/>
    </h:commandLink>
  </f:facet>
  <h:outputText value="#{hist.publishedCount}"/>
</h:column>  

</h:dataTable>

<% // page cursor - top %>
<h:panelGroup id="hhPageCursorBottom" styleClass="pageCursor"
  binding="#{HarvestHistoryController.pageCursorPanel.bottomHtmlPanelGroup}"/>
  
</h:form>

<h:form id="hrBackButtonForm">

<h:panelGrid width="100%"
columns="1" 
summary="#{gptMsg['catalog.general.designOnly']}"
styleClass="formTable" 
columnClasses="formBackButtonColumn">

<h:commandButton 
value="#{gptMsg['catalog.harvest.manage.back']}" 
action="catalog.publication.manageMetadata"
actionListener="#{ManageMetadataController.processAction}">
</h:commandButton>

</h:panelGrid>

</h:form>
