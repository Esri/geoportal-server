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
<%// mysearches.jsp - Stored searches(JSF include)%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<f:verbatim>
<script type="text/javascript">

// Global variables
if(typeof(contextPath) == 'undefined' || contextPath == "") {
  var contextPath = "<%=request.getContextPath()%>";
}

var nTimeout = 0;
if(typeof(_csDistributedSearchTimeoutMillisecs) != 'undefined') {
  var nTimeout = parseInt(_csDistributedSearchTimeoutMillisecs);
  if(nTimeout == NaN) {
    nTimeout = 0;
  }
}

/**
Writes the saved search into the 
**/
function writeSavedSearch() {
  var elLoadingGif = dojo.byId("frmSearchCriteria:savedSearchLoadingGif");
  dojo.style(elLoadingGif, "visibility", "visible");
  removeError();
  var txtCriteria = dojo.attr(dojo.byId("frmSearchCriteria:scSearchUrl"), 
    "value");
  var txtName = dojo.attr(dojo.byId("frmSearchCriteria:ssName"), "value");
  dojo.xhrPut({
    url: contextPath + "/savedSearch",
    handleAs:"json",
    content: {
      criteria: txtCriteria,
      name: txtName
    },
    preventCache: true,
    timeout: nTimeout,
    load: loadSavedSearches,
    error: handleSavedSearchError
  });
}

/**
Reads the saved searches from server onto the gui
**/
function readSavedSearches() {
  removeError();
  var elLoadingGif = dojo.byId("frmSearchCriteria:savedSearchLoadingGif");
  dojo.style(elLoadingGif, "visibility", "visible");
  dojo.xhrGet({
    url: contextPath + "/savedSearch",
    handleAs:"json",
    load: loadSavedSearches,
    timeout: nTimeout,
    preventCache: true,
    error: handleSavedSearchError
  });

}

/**
Deletes a saved search from the backend and writes new records
**/
function deleteSavedSearch(txtId, event) {
  removeError();
  var elLoadingGif = dojo.byId("frmSearchCriteria:savedSearchLoadingGif");
  dojo.style(elLoadingGif, "visibility", "visible");
  dojo.xhrDelete({
    url: contextPath + "/savedSearch",
    handleAs:"json",
    content: {
      id: txtId
    }, 
    timeout: nTimeout,
    load: loadSavedSearches,
    preventCache: true,
    error: handleSavedSearchError
  });
}

/**
Executes a clicked saved search
**/
function executeSavedSearch(criteria, event) {
  if(typeof(criteria) != 'string') {
    return;
  }
  removeError();
  if(criteria.indexOf("&f=")) {
    criteria = criteria.replace("&f=", "&g=");
  }
 
  if(criteria.indexOf("?") >= 0) {
    criteria = criteria.replace("?", "");
  }
  window.location = contextPath + "/rest/find/document?" + criteria + 
    "&f=searchPage";
}

/**
Handles saved search errors on the 

@param args The error object
**/
function handleSavedSearchError(args) {
  dojo.query("#savedSearcherrorMessage").empty();
  var elLoadingGif = dojo.byId("frmSearchCriteria:savedSearchLoadingGif");
  dojo.style(elLoadingGif, "visibility", "hidden");
  
  if(args == null) {
    return;
  }
  if(typeof(args.status) == "number" && args.status == 404) {
    // Reload the page.  Probably session has expired.
    window.location.reload();
  }
  if(typeof(args.message) == "string") {
    writeSavedSearchError(args.message);
  }

}

/**
Loads saved searches into the guir
@param args The json
**/
function loadSavedSearches(args) {
  var elLoadingGif = dojo.byId("frmSearchCriteria:savedSearchLoadingGif");
  dojo.style(elLoadingGif, "visibility", "hidden");
  
  dojo.query("#savedSearcherrorMessage").empty();
  if(args == null) { 
    return;
  }
  if(typeof(args.error) == 'string'){
    writeSavedSearchError(args.error);
  }
  /*
  var elLoadingGif = dojo.byId("frmSearchCriteria:savedSearchLoadingGif");
  dojo.style(elLoadingGif, "visibility", "visible");*/
  if(typeof(args.resultRecords) == 'undefined'){
    return;
  }
  var elResults = dojo.create("div");
  dojo.style(elResults, "clear", "both");
  dojo.forEach( args.resultRecords, 
    dojo.hitch(this, function(obj, i) {
      var elRow = dojo.create("div", null, elResults);
      dojo.style(elRow, "clear", "both");
      var elName = dojo.create("div", null, elRow);
      dojo.style(elName, "width",  "70%");
      dojo.style(elName, "float",  "left");
      dojo.style(elName, "cursor", "pointer");
      var elNameLink = dojo.create("a",{
          href: "#",
          innerHTML:obj.name
        } , elName );
      dojo.connect(elNameLink,"onclick", 
        dojo.hitch(this, executeSavedSearch, obj.criteria));  
      
      var elActions = dojo.create("div", null, elRow);
      dojo.style(elActions, "width", "30%");
      dojo.style(elActions, "float",  "right");
      dojo.style(elActions, "cursor", "pointer");
      elDeleteLink = dojo.create("a", {
          href: "#",
          innerHTML: "delete"
        }, elActions );
      dojo.connect(elDeleteLink,"onclick", 
        dojo.hitch(this, deleteSavedSearch, obj.id));   
    }
  ));
  dojo.query("#savedSearchList").empty();
  dojo.query("#savedSearchList").addContent(elResults);
  
}


/**
Shows the saved search gui
**/
var showSavedSearch = false;
function toggleSavedSearch() {

  showSavedSearch = !showSavedSearch;
  var elSavedSearches = dojo.byId("frmSearchCriteria:pngSavedSearches");
  if(showSavedSearch == true) {
    readSavedSearches();
    dojo.style(elSavedSearches, "visibility", "visible");
    dojo.style(elSavedSearches, "display", "block");
  } else {
    dojo.style(elSavedSearches, "visibility", "hidden");
    dojo.style(elSavedSearches, "display", "none")
  }
  
  
}

/**
Writes error on the screen
**/
function writeSavedSearchError(error) {
  var elLoadingGif = dojo.byId("frmSearchCriteria:savedSearchLoadingGif");
  dojo.style(elLoadingGif, "visibility", "hidden");
  if(typeof(error) == 'string' ) {
    dojo.query("#savedSearcherrorMessage").empty();
    dojo.query("#savedSearcherrorMessage").addContent(error);
    var elErrorMessage = dojo.byId("savedSearcherrorMessage");
    dojo.style(elErrorMessage, "visibility", "visible");
    dojo.style(elErrorMessage, "display", "block");
    
  }
}

/**
Removes error from the screen
**/
function removeError() {
  dojo.query("#savedSearcherrorMessage").empty();
  var elErrorMessage = dojo.byId("savedSearcherrorMessage");
  dojo.style(elErrorMessage, "visibility", "hidden");
  dojo.style(elErrorMessage, "display", "none");
}

</script>

</f:verbatim>
<h:panelGroup styleClass="mySearchesContainer">

	<% // my searches link %>
	<h:outputLink
	  id="ssLnkSearches"
	  onclick="javascript:toggleSavedSearch(); return false;">
    <h:outputText value="#{gptMsg['catalog.search.savedSearches.caption']}" />
	</h:outputLink>
 
	<% // my searches panel %>
	<h:panelGroup 
	  id="pngSavedSearches" 
	  style="visibility: hidden; display: none;">

		<% // save this search %>
		<h:panelGrid id="ss" columns="1" style="#{SearchController.saveSearchStyle}">
		  <f:verbatim><div class="errorMessage" 
        id="savedSearcherrorMessage"></div>
      </f:verbatim>
		  <h:panelGroup>
		    <h:inputText id="ssName"
		      value="#{SearchController.searchCriteria.savedSearchName}" 
		      maxlength="20" />
		    <f:verbatim>&nbsp;&nbsp;&nbsp;</f:verbatim>  
		    <h:commandLink id="ssBtnSave" 
		      onclick="javascript: writeSavedSearch();return false;">
		      <h:outputText value="#{gptMsg['catalog.search.search.btnSave']}" />
		    </h:commandLink>
		    <h:graphicImage
          id="savedSearchLoadingGif" 
          style="visibility: hidden;"
          url="/catalog/images/loading.gif" alt="" 
          width="30px">
        </h:graphicImage>
		  </h:panelGroup>
		</h:panelGrid>
		
		<% // saved search list%>
		
		<f:verbatim><div id="savedSearchList"></div></f:verbatim>
		<f:verbatim><br/></f:verbatim>
	</h:panelGroup>

</h:panelGroup>