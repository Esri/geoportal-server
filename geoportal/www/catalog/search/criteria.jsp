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
<%// criteria.jsp - Search criteria (JSF include)%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<%
  com.esri.gpt.framework.jsf.MessageBroker schMsgBroker = com.esri.gpt.framework.jsf.PageContext.extractMessageBroker();
	String schContextPath = request.getContextPath();	
	com.esri.gpt.framework.context.RequestContext schContext = com.esri.gpt.framework.context.RequestContext.extract(request);
	com.esri.gpt.catalog.context.CatalogConfiguration schCatalogCfg = schContext.getCatalogConfiguration();
	com.esri.gpt.framework.collection.StringAttributeMap schParameters = schCatalogCfg.getParameters();
	boolean hasSearchHint = false;
	if(schParameters.containsKey("catalog.searchCriteria.hasSearchHint")){	
		String schHasSearchHint = com.esri.gpt.framework.util.Val.chkStr(schParameters.getValue("catalog.searchCriteria.hasSearchHint"));
		hasSearchHint = Boolean.valueOf(schHasSearchHint);
	}
	String schHintPrompt = schMsgBroker.retrieveMessage("catalog.searchCriteria.hintSearch.prompt");
	String VER121 = "v1.2.1";
%>

<% if(hasSearchHint){ %>
  <input type="hidden" id="schContextPath" value="<%=schContextPath %>"/>
  <input type="hidden" id="schHintPrompt" value="<%=schHintPrompt %>"/>
	<script type="text/javascript" src="<%=schContextPath+"/catalog/js/" +VER121+ "/gpt-search-hint.js"%>"></script>	
<% } %>

<% // date picker support %>
<gpt:DatePickerConfig/>

<% // scripting functions %>
<f:verbatim>

  <style type="text/css">

    .valignUp {
      vertical-align: top;
    }

  </style>

 <script type="text/javascript">
    // &filter parameter based on window.location.href
    function scAppendExtendedFilter(sUrlParams,bIsRemoteCatalog) {
      if (bIsRemoteCatalog == false) {
        var f = scGetExtendedFilter();
        if ((typeof(f) != "undefined") && (f != null) && (f.length > 0)) {
          if (sUrlParams.length > 0) sUrlParams += "&";
          sUrlParams += "filter="+ encodeURIComponent(f);
          console.debug("scAppendExtendedFilter="+sUrlParams);
        }
      }
      return sUrlParams;
    }
    function scGetExtendedFilter() {
      var q = dojo.queryToObject(window.location.search.slice(1));
      var f = q.filter;
      if ((typeof(f) != "undefined") && (f != null)) {
        f = dojo.trim(f);
        if (f.length > 0) {
          console.debug("scGetExtendedFilter="+f);
          return f;
        }
      }     
      return null;
    }
  </script>

  <script type="text/javascript">
    // Results
    var srMapViewer = null;

    function srAddToMap(args) {
      if (srMapViewer == null) srMapViewer = new GptMapViewer(srMvsUrl);
      srMapViewer.addToMap(args);
    }

    function srHighlightRecord(table) {
  
      if (typeof(scMap) != 'undefined') scMap.highlightFootPrint(table.id,true);
    }

    function srUnhighlightRecord(table) {
  
      if (typeof(scMap) != 'undefined') scMap.highlightFootPrint(table.id,false);
    }

    function srZoomTo(rowIndex) {
      if (typeof(scMap) != 'undefined') scMap.zoomToFootPrint(rowIndex);
    }

    function srZoomToAOI() {
      if (typeof(scMap) != 'undefined') scMap.zoomToAOI();
    }

    function srZoomToThese() {
      if (typeof(scMap) != 'undefined') scMap.zoomToThese();
    }

    function srCorrectThumbs() {

      var els = document.getElementsByTagName("IMG");
      if(els == null) {
        return;
      }
  
      for(var i = 0; i < els.length; i = i + 1) {
    
        if(typeof(els[i].id) != 'undefined' &&
          els[i].id.toLowerCase().match("thumbnail")  == "thumbnail") {
          els[i].onerror = "GptUtils.checkImgError(this)";
          els[i].setAttribute("onerror", "GptUtils.checkImgError(this)");
        }
      }
    }
    dojo.addOnLoad(srCorrectThumbs);

    // Replaces all links that are target = _top (Currently just the view Details
    // link)

    dojo.addOnLoad(function(){
      dojo.query("a.resultsLink").forEach(
      function(item) {
        if(item != null && typeof(item.target) == 'string' &&
          item.target.toLowerCase() == "_top") {
          item.target = "_blank";
        }
      }
    );
      // Preloading the search gif
      var img = new Image();
      img.src = dojo.byId("/catalog/images/loading.gif");
      //scInitDistrPane();
    }
  );



  </script>

  <script type="text/javascript">

    dojo.require("dojo._base.NodeList");
    dojo.require("dijit.Dialog");
    dojo.require("dojo._base.html");
    
    //dojo.require("dojo.lfx.*");

    $(document).ready(function(){
      var dpCfg = new DatePickerConfig();
      dpCfg.initialize();
      dpCfg.attach("frmSearchCriteria:scDateFrom");
      dpCfg.attach("frmSearchCriteria:scDateTo");
       
    });

    /**
    Makes a valid html id out of the a string
    **/
    function srNormalizeId(id) {
      var strId = id.replace(/[^A-Za-z0-9_]/g, "");
      strId = "sc" + strId;
      return strId;
    }

    function scOnSpatialOptionClicked(options) {
      if ((typeof(scMap) != 'undefined') && (options.value == "anywhere")) {
        scMap.zoomAnywhere();
      }
    }


    var _scRdbIndex = null;
    var _scRpsIndex = null;
    var contextPath = "<%=request.getContextPath()%>";

    /*
  Updating of sites once the ok button is clicked
     */
    function scUpdateHsites(isCancel) {
    
      var rdbIndex = _scRdbIndex;
      var rpsIndex = _scRpsIndex;
      isCancel = GptUtils.valChkBool(isCancel);
  
      /*if(rdbIndex == null || rpsIndex == null) {
      return;
    }*/
  
      var siteName = "";
      var siteProfile = "";
      var siteUrl = "";
      var shsIndex = "";
  
      /*if(rdbIndex < 0) {
      siteName = csDefaultSiteLabel;
    } else {
      siteName = _scSearchSites.rows[rdbIndex].name;
      //siteProfile = dsHarvestSites[rdbIndex].profile;
      siteUrl = _scSearchSites.rows[rdbIndex].url;
    }*/
  
      if(isCancel == false){
    
        var elSiteName = document.getElementById("frmSearchCriteria:_harvestSiteName");
        var elSiteProfile = document.getElementById("frmSearchCriteria:_harvestSiteProfile");
        var elSiteUrl = document.getElementById("frmSearchCriteria:_harvestSiteUrl");
        var elHarvestId = document.getElementById("frmSearchCriteria:_harvestSiteId");
    
        if(elSiteName.value != siteName) {
          var elResults = document.getElementById("frmSearchCriteria:srResultsPanel");
          if(elResults != null) {
            elResults.style.display = "none";
            elResults.style.visibility = "hidden";
          }
        }
   
    
        elSiteProfile.value = siteProfile;
        elSiteUrl.value = siteUrl;
        scWriteToDistrEndPointComponents();
        scInitComponents();
      }
    }

    /**
  Initializes visual text fields to show user which site is being searched
     **/
    function scInitTextFields() {
      var elSiteName = document.getElementById("frmSearchCriteria:_harvestSiteName");
      
      var name = "";
  
      if(elSiteName != null && GptUtils.exists(elSiteName.value)) {
        name = elSiteName.value;
      }
      var elPrntTxt = document.getElementById("frmSearchCriteria:txtSiteName");
      if( elPrntTxt != null && GptUtils.exists(elPrntTxt.firstChild)) {
        elPrntTxt.removeChild(elPrntTxt.firstChild);
      }
      if(elPrntTxt != null) {
        elPrntTxt.appendChild(document.createTextNode(name));
      }
      var elPrntTxt = document.getElementById("frmSearchCriteria:txtSiteName2");
      if(elPrntTxt != null && GptUtils.exists(elPrntTxt.firstChild)) {
        elPrntTxt.removeChild(elPrntTxt.firstChild);
      }
      if(elPrntTxt != null) {
        elPrntTxt.appendChild(document.createTextNode(name));
      }
      var elPrntTxt = document.getElementById("frmSearchCriteria:txtSiteName3");
      if(elPrntTxt != null && GptUtils.exists(elPrntTxt.firstChild)) {
        elPrntTxt.removeChild(elPrntTxt.firstChild);
      }
      if(elPrntTxt != null) {
        elPrntTxt.appendChild(document.createTextNode(name));
      }

      var msg = _csTitleDistributedSearch.replace("{0}", name);
      dojo.query("#djtCntDistributedSearchesTitle").empty().addContent(msg);
      
      var rid = dojo.byId("frmSearchCriteria:_harvestSiteId").value;
      dojo.query(".distrSiteTable").forEach(
        function(node, index, arr) {
          if(node.id == rid) {
            dojo.addClass(node,"selectedResultRow" );
          } else {
            dojo.removeClass(node,"selectedResultRow" );
          }
        }      
      );
  
    }

    /**
  Initialization of page components
     **/
    function scInitComponents() {
  
      scInitTextFields();
      scPopulateHarvestSites();
      scWriteToDistrEndPointComponents();
      scInitDistrPane();
      scReconfigureCriteria();
      if(typeof(rsInsertReviews) != 'undefined') {
          rsInsertReviews();
      }
      if ((typeof(itemCart) != "undefined") && (itemCart != null)) {
        itemCart.connectToSearchResults();
      }
      
	  if(typeof(rsGetQualityOfService) != 'undefined') {
  	      try{
  	  		rsGetQualityOfService();
	  	  	} catch(error) {
	  	  	    console.log("unable to fetch quality of service info : ", error);
	  	  	}
      }
	  
	    // hide locator input if locator service is not configured
	    if(typeof(gptMapConfig) != 'undefined' && typeof(gptMapConfig.locatorURL) != 'undefined' 
	    		&& (gptMapConfig.locatorURL == null || dojo.trim(gptMapConfig.locatorURL).length == 0)){	    
	  		dojo.byId("frmSearchCriteria:mapToolbar").style.display = "none";
	    }
    }
    dojo.addOnLoad(scInitComponents);
    
    function scIsRemoteCatalog() {
      var elSiteId = document.getElementById("frmSearchCriteria:_harvestSiteId");
      if(elSiteId != null && GptUtils.valChkStr(elSiteId.value, "") == "local") {
        return false;
      }
      return true;
    }
    
    /**
    Reconfigure the GUI critieria
    **/
    function scReconfigureCriteria() {
      var elSiteId = document.getElementById("frmSearchCriteria:_harvestSiteId");
      var elSiteUrl = document.getElementById("frmSearchCriteria:_harvestSiteUrl");
      var blnRemoteCatalog = scIsRemoteCatalog();
      var blnSameCatalog = false;
        
      var el = document.getElementById("frmSearchCriteria:_authUserNamePassword");
      //scShow(blnRemoteCatalog, el, false);

      el = document.getElementById("frmSearchCriteria:_pngDataThemes");
      scShow(!blnRemoteCatalog, el);
  
      el = document.getElementById("frmSearchCriteria:_pngModDateSection");
      scShow(!blnRemoteCatalog, el);
  
      el = document.getElementById("frmSearchCriteria:_pngSortSection");
      scShow(!blnRemoteCatalog, el);
    
      el = document.getElementById("frmSearchCriteria:_pngCtypeRemote");
      scShow(blnRemoteCatalog, el);
  
      el = document.getElementById("frmSearchCriteria:_pngCtypeLocal");
      scShow(!blnRemoteCatalog, el);
    
      el = document.getElementById("frmSearchCriteria:_pngModDateSection");
      scShow(!blnRemoteCatalog, el);
    }
    
    
    function scShow(boolVal, el, bCanCollapseBlock) {
      if(GptUtils.exists(el) != true) {
        return;
      }
      if(typeof(bCanCollapseBlock) != 'boolean') {
        bCanCollapseBlock = true;
      }
  
      if(boolVal == true) {
        el.style.display = "";
        el.style.visibility = "visible";
      } else {
        if(bCanCollapseBlock == true) {
          el.style.display = "none";
        }
        el.style.visibility = "hidden";
      }
    }

    var _sHarvestSites;
    var _scSearchSites;

   


    // Adds the 'this site" and the arcgis website and any other
    // end points in gpt.xml
    function scAddLocalSites() {
      var rows = _scSearchSites.rows;
  
      if(typeof(csExteriorRepositories) == 'undefined'
        || csExteriorRepositories == null
        || typeof(csExteriorRepositories.length) != 'number') {
        return;
      }
      for(var i = csExteriorRepositories.length-1;  i >= 0 ; i--) {
        var obj = new Object();
        if(typeof(csExteriorRepositories[i].name) == 'undefined'
          || typeof(csExteriorRepositories[i].uuid) == 'undefined') {
          continue;
        }
        obj.name = csExteriorRepositories[i].name;
        obj.uuid = csExteriorRepositories[i].uuid;
        rows.unshift(obj);
      }
    }
    
    function scGetHarvesterSitesHandler(data) {
    	 if(typeof(data) == 'undefined' || data == null) {
    		 data = "";
    	 }
    	 _scSearchSites = dojo.eval("[{" + data + "}]");
        if(typeof(_scSearchSites.length) != 'undefined'
          && _scSearchSites.length == 1) {
          _scSearchSites = _scSearchSites[0];
          
        }
        if(typeof(_scSearchSites.rows) == 'undefined') {
          _scSearchSites.rows = new Array();
        }
        scAddLocalSites();
        
    }
    
    // Gets the harvest sites via ajax
    var triedAddSitesFromError  = false; 
    function scGetHarvestSites() {
      var url = contextPath + '/rest/repositories?protocol=csw';
      var nTimeout = parseInt(_csDistributedSearchTimeoutMillisecs);
      if(nTimeout == NaN) {
        nTimeout = -1;
      }
      if(GptUtils.valChkBool(_csAllowDistributedSearch) == false) {
    	  scGetHarvesterSitesHandler("");
    	  return;
      }
      dojo.xhrGet ({
      
        url: url,
        load: scGetHarvesterSitesHandler,
        timeout: nTimeout,
        preventCache: true,
        error: function (data) {
         
          if(triedAddSitesFromError == true) {
            return;
          }
          GptUtils.logl(GptUtils.log.Level.WARNING,
          "scGetHarvestSites could not get harvesting sites body" +
            "Error: " +data);
          dojo.query("#cmPlPgpGptMessages").addContent(
            "<div class=\"errorMessage searchInjectedError\">" + data.message 
              + " : " + data.description + " : " + url +
            "</div>"
          );
          if (typeof(scMap) != 'undefined') scMap.reposition();
          if(triedAddSitesFromError == false) {
            triedAddSitesFromError = true;
            scAddLocalSites();
            
          }  
         
        },
      
        sync: true
      });
    }



    //Injects harvest sites the user can choose from
    function scPopulateHarvestSites() {
  
  
      scGetHarvestSites();
      if(_scSearchSites == null || _scSearchSites == ""
        || !GptUtils.exists(_scSearchSites.rows)) {
        GptUtils.logl(GptUtils.log.Level.WARNING,
        "scPopulateHarvestSites could not get harvesting sites");
        return;
      }
      if(_scSearchSites.rows && _scSearchSites.rows.length 
        && _scSearchSites.rows.length <= 1) {
        
        // Do not draw anything.  There are no harvest rows
        return;
      }
      var elOption;
      var elOptionText;
      var elTable = document.createElement("table");
      elTable.id = 'tblHarvestSites';
      var elTbody = document.createElement("tbody");
      var elTr = document.createElement("tr");
      var elTdInp = document.createElement("td");
      var elTdName = document.createElement("td");
      var elName = "";
      var bDefaultInj = false;
      var arrDistributedIds = (dojo.byId(
         "frmSearchCriteria:scSelectedDistributedIds").value).toLowerCase().split(",");
      
      for(var i = 0; i < _scSearchSites.rows.length; i++) {
        var elSiteName = document.getElementById("frmSearchCriteria:_harvestSiteName");
        var elSiteUrl = document.getElementById("frmSearchCriteria:_harvestSiteUrl");
        var elSiteId =  document.getElementById("frmSearchCriteria:_harvestSiteId");
        var bCheck = false;
        var siteName = _scSearchSites.rows[i].name;
        var siteId = null;
        if(GptUtils.exists(_scSearchSites.rows[i].uuid)) {
          siteId = _scSearchSites.rows[i].uuid;
        } else {
          continue;
        }
   
        var aSiteIds = GptUtils.valChkStr(elSiteId.value).toLowerCase().split(",");
        var sUuid = GptUtils.valChkStr(_scSearchSites.rows[i].uuid).toLowerCase();
        if(dojo.indexOf(aSiteIds, sUuid) >= 0) {
          bCheck = true;
         
          
        } else if( dojo.indexOf(arrDistributedIds, encodeURIComponent(sUuid).toLowerCase()) >= 0) { 
          bCheck = true;
        } else {
          bCheck = false;
        }
        var disabled = "";
        /*if(siteId == 'local') {
          //disabled = "  disabled ";
          bCheck = true;
        }*/
   
        try {
          // I.E. needs this grrr
          if(bCheck == true) {
            el = document.createElement(
            "<input id=\""+ siteId+"\" "+ disabled + " type=\"checkbox\" name=\"remoteCatalog\" onclick=\"return scWriteToDistrEndPointComponents(this);\" checked=\"true\" >");
          } else {
            el = document.createElement(
            "<input id=\""+ siteId+"\" " + disabled + " type=\"checkbox\" name=\"remoteCatalog\" onclick=\"return scWriteToDistrEndPointComponents(this);\" >");
          }
        } catch(err)  {
          el = document.createElement("input");
          el.type = "checkbox";
          el.id = siteId;
          el.name = "remoteCatalog";
          el.value = elSiteUrl;
          el.checked = bCheck
          if(disabled.length > 1) {
            el.disabled = 1;
          }
      
        }
     
        if(disabled.length > 1) {
          el.disabled = 1;
        }
        el.onclick = "return scWriteToDistrEndPointComponents(this);";
        el.setAttribute("onclick", "return scWriteToDistrEndPointComponents(this);" );
        
        elTdInp.appendChild(el);
        elTr.appendChild(elTdInp);
        elTdName.appendChild(
        		dojo.create('label', { "for": siteId, innerHTML: siteName}))
        		//document.createTextNode(siteName));
        elTr.appendChild(elTdName);
        elTbody.appendChild(elTr);
        elTr = document.createElement("tr");
        elTdInp = document.createElement("td");
        elTdName = document.createElement("td");
      }
      elTable.appendChild(elTbody);
      var elFormerTable = document.getElementById(elTable.id);
      if(GptUtils.exists(elFormerTable) == true){
        elFormerTable.parentNode.removeChild(elFormerTable);
      }
      // TM: Changing to populate other div
      //var elDiv = document.getElementById('divHarvestingSitesBodyList');
      var elDiv = document.getElementById('cntDistributedSearchesConfig');
      if(typeof(elDiv) != 'undefined' && elDiv != null) {
        elTable.style.verticalAlign = "top";
	      elTable.style.overflow = "auto";
	//      elTable.className = "noneSelectedResultRow ";
	      elDiv.appendChild(elTable);
      }
   
    }

    /*
  Update Additional options after clicking on ok or cancel.  Invisible
  elements are transfered to their hidden counter parts or vice versa
     */
    var _scAddOptions = new Array();
    function scUpdateAdditionalOptions(isCancel) {
      for(var ent in _scAddOptions) {
    
        var elInvisible = _scAddOptions[ent].comp;
        var hiddenId = _scAddOptions[ent].id;
    
        if(!GptUtils.exists(elInvisible) || !GptUtils.exists(elInvisible.id)) {
          GptUtils.logl(GptUtils.log.Level.WARNING,
          "updateHiddenValue recieved invalid input");
          continue;
        }
        var id = elInvisible.id + "Hidden";
        if(GptUtils.valChkStr(hiddenId) != "") {
          id = hiddenId;
        }
        var el = document.getElementById(id);
        if(el == null || typeof(el) == 'undefined') {
          GptUtils.logl(GptUtils.log.Level.WARNING,
          "updateHiddenValue missing: id = " + id);
          return;
        }
        if(GptUtils.valChkBool(isCancel) == true) {
          elInvisible.value = el.value;
        } else {
          el.value = elInvisible.value;
        }
      }
    }

    //Some inputs are in a hidden div so this method makes helps in exposing
    //the values to their input hidden counterparts
    function updateHiddenValue(elInvisible, hiddenId) {
      _scAddOptions[elInvisible.id + ''] = { "comp" : elInvisible, "id" : GptUtils.valChkStr(hiddenId) };
    }

    //Updates hidden values associated with values that are now
    //not displayed by the dialog.  These values will not be
    //sent to the server because they are not displayed.  So hidden
    //values take these values so that the values can be posted to the server
    function updateHiddenValuesMultiple(elInvisible, hiddenId, isCancel) {

      var delimeter = "|";
  
      if(!GptUtils.exists(elInvisible)) {
        GptUtils.logl(GptUtils.log.Level.WARNING,
        "updateHiddenValueMultiple recieved invalid input");
        return;
      }
      var name = '';
      if(typeof(elInvisible) == 'string') {
        name = elInvisible;
      } else if (GptUtils.exists(elInvisible.name)) {
        name = elInvisible.name;
      }
      var els = document.getElementsByName(name);
      if(els == null || !GptUtils.exists(els.length) || els.length <= 0) {
        GptUtils.logl(GptUtils.log.Level.WARNING,
        "Could not find elements with name = " + elInvisible.name);
        return;
      }
      var id = hiddenId;
      var el = document.getElementById(id);
      if(el == null || typeof(el) == 'undefined') {
        GptUtils.logl(GptUtils.log.Level.WARNING,
        "updateHiddenValue missing: id = " + id);
        return;
      }
      if(GptUtils.valChkBool(isCancel) == false) {
    
        el.value = "";
        for(var i = 0; i < els.length; i++) {
          if(GptUtils.valChkStr(els[i].value) != "" && els[i].checked == true) {
            el.value = el.value + delimeter + els[i].value;
          }
        }
      } else {
   
        for( var j = 0; j < els.length; j++ ) {
          els[j].checked = false;
        }
        var tokenized = GptUtils.valChkStr(el.value).split(delimeter);
        for(var i = 0; i < tokenized.length; i++) {
          for( var j = 0; j < els.length; j++ ) {
            if(els[j].value == tokenized[i]) {
              els[j].checked = true;
            }
          }
        }
      }
    }

    /**
  When advanced options is closed by the buttons, this action takes place.
     **/
    function scAdvOptDialog(isCancel) {
      scUpdateAdditionalOptions(isCancel);
      updateHiddenValuesMultiple('frmSearchCriteria:scSelTheme',
      'frmSearchCriteria:scSelThemeHidden', isCancel);
  
    }
    /**
  Shows a dialog (the advanced search dialog)
  @sId is the string id
     **/

    function scShowDialog(sId, bShow ) {
 
      var dj = dijit.byId(sId);
      dj.refreshOnShow = true;
      if(GptUtils.valChkBool(bShow) == true) {
        if(dj.closeText)
        {
          dj.closeText.setAttribute("title","");
          dj.closeButtonNode.setAttribute("title", "");
        }
        dj.startup();
    
        // need to do this to reset the width and height
        dj.containerNode.style.width = "";
        dj.containerNode.style.height = "";
        dj.show();
    
        var iPWidth = parseInt(dj.domNode.style.width);
        var iCWidth = parseInt(dj.containerNode.style.width);
        var maxWidth = parseInt(dj.domNode.style.maxWidth);
        dj.containerNode.style.width = "100%";
        if(maxWidth != NaN && iCWidth != NaN && iCWidth > maxWidth ) {
       
          dj.containerNode.style.width = maxWidth - 10 + "px";
          //dj.containerNode.style.overflowX = "auto";
          //dj.containerNode.style.overflowY = "auto";
       
        }
    
      } else {
        dj.hide();
      }
      var func = dj.onCancel;
  
      if(typeof(dj.attachedCancel) == 'undefined') {
  
        if(dj.open == true)
     
        dj.onCancel = function(){
          if(dj.id == "crtAdvOptnsContent") {
            scAdvOptDialog(false);
          }
       
      
        if(typeof(func) != 'undefined') {
          func();
        }
    
      }
      dj.attachedCancel = true;
    }

  }

  /**
  Does the distributed search.  Calls the distributed search page.
   **/
  var formerDistributedSearchUrl = "";
  function scExecuteDistributedSearch() {
    //scDoDistrSearch();
    
    var restUrl = contextPath + "/rest/distributed?" + scReadRestUrlParams();
    //if(formerDistributedSearchUrl == restUrl) {
      //return;
    //}
    formerDistributedSearchUrl = restUrl;
    if(_csDistributedSearchTimeoutMillisecs) {
      restUrl += "&maxSearchTimeMilliSec=" + encodeURIComponent(_csDistributedSearchTimeoutMillisecs);
    }
    var elIframe = dojo.byId("frmDistSearch");
    // Do the search only when panel is open and when more than one
    // result is their
    var scRids = GptUtils.valChkStr(
       dojo.byId("frmSearchCriteria:scSelectedDistributedIds").value);
    scRids = scRids.split(",");
    if(_showingDistrSearchSites == true && scRids.length > 1) {
      elIframe.src = restUrl + "&f=searchpage&=preventCache"+ 
        (new Date()).getTime();
    }
    dojo.query(".loadingImages").style("visibility", "hidden");
    //dojo.query(".loadingImages").style("display", "block");
    dojo.query(".distrHitCountTxt").empty();
  }


  var tmpAoiMinX;
  var tmpAoiMinY;
  var tmpAoiMaxX;
  var tmpAoiMaxY; 
  var tmpAoiWkid;
  /**
  Gets the Rest Url params in a string

  Should be modified if new search filter has been input.
   **/
  function scReadRestUrlParams() {
    var restParams = "";
    var bIsRemoteCatalog = scIsRemoteCatalog();
      
     
    var scRid = GptUtils.valChkStr(
    dojo.byId("frmSearchCriteria:_harvestSiteId").value);
    if(scRid != "") {
      restParams += "rid=" +  encodeURIComponent(scRid);
    }
    var scName = GptUtils.valChkStr(
    dojo.byId("frmSearchCriteria:_harvestSiteName").value);
    if(scName != "") {
      restParams += "&ridName=" +  encodeURIComponent(scName);
    }
    var scRids = GptUtils.valChkStr(
    dojo.byId("frmSearchCriteria:scSelectedDistributedIds").value);
    if(scRids != "") {
      restParams += "&rids=" +  encodeURIComponent(scRids);
    }
   
    var scText = GptUtils.valChkStr(
    dojo.byId('frmSearchCriteria:scText').value);
    if(scText != "") {
      restParams += "&searchText=" +  encodeURIComponent(scText);
    }

    // &filter parameter based on window.location.href
    restParams = scAppendExtendedFilter(restParams,bIsRemoteCatalog);
   
    var scPage = dojo.byId("frmSearchCriteria:scCurrentPage").value;
    var scMaxResultsPerPage = dojo.byId("frmSearchCriteria:scRecordsPerPage").value;
    var intPage = parseInt(scPage);
    var intMaxResultsPerPage = parseInt(scMaxResultsPerPage);
    if(intPage != NaN && intMaxResultsPerPage != NaN) {
      var startPosition = ((intPage - 1) * intMaxResultsPerPage) + 1;
      restParams += "&start=" + encodeURIComponent(startPosition);
      restParams += "&max=" + intMaxResultsPerPage;
    }
   
    var scSort = GptUtils.valChkStr(
    dojo.byId("frmSearchCriteria:scSelSortHidden").value);
    if(scSort != "" && bIsRemoteCatalog == false) {
      restParams += "&orderBy=" + encodeURIComponent(scSort);
    }
   
    var scDateFrom = GptUtils.valChkStr(
    dojo.byId("frmSearchCriteria:scDateFromHidden").value);
    if(scDateFrom != ""  && bIsRemoteCatalog == false) {
      restParams += "&after=" + encodeURIComponent(scDateFrom);
    }
   
    var scDateTo = GptUtils.valChkStr(
    dojo.byId("frmSearchCriteria:scDateToHidden").value);
    if(scDateTo != ""  && bIsRemoteCatalog == false) {
      restParams += "&before=" + encodeURIComponent(scDateTo);
    }
   
    var scContentType = GptUtils.valChkStr(
    dojo.byId("frmSearchCriteria:scSelContentHidden").value);
    if(scContentType != "") {
      restParams += "&contentType=" + encodeURIComponent(scContentType);
    }
   
    var scThemes = GptUtils.valChkStr(
    dojo.byId("frmSearchCriteria:scSelThemeHidden").value);
    if(scThemes != ""  && bIsRemoteCatalog == false) {
      scThemes = scThemes.replace(/^\|/g, ""); 
      scThemes = scThemes.replace(/\|/g, ","); 
      restParams += "&dataCategory=" + scThemes; 
    }
   
    var outputBbox = false;
    dojo.query("[name=frmSearchCriteria:scSelSpatial]").forEach(
	    function(node, index, arr) {
	      if(node.checked == "checked" || node.checked == true) {
	        if(node.value == "useGeogExtent") {
	          restParams += "&spatialRel=" + "esriSpatialRelOverlaps";
	          outputBbox = true;
	        } else if (node.value == "dataWithinExtent") {
	          restParams += "&spatialRel=" + "esriSpatialRelWithin";
	          outputBbox = true;
	        }
	      }
	    }
    );
    if(outputBbox) {
      restParams += "&bbox=" + dojo.byId("frmSearchCriteria:sfsMinX").value;
      restParams += "," + dojo.byId("frmSearchCriteria:sfsMinY").value;
      restParams += "," + dojo.byId("frmSearchCriteria:sfsMaxX").value;
      restParams += "," + dojo.byId("frmSearchCriteria:sfsMaxY").value;
      tmpAoiMinX = dojo.byId("frmSearchCriteria:sfsVMinX").value;
      tmpAoiMinY = dojo.byId("frmSearchCriteria:sfsVMinY").value;
      tmpAoiMaxX = dojo.byId("frmSearchCriteria:sfsVMaxX").value;
      tmpAoiMaxY = dojo.byId("frmSearchCriteria:sfsVMaxY").value;
      tmpAoiWkid = dojo.byId("frmSearchCriteria:sfsVWkid").value;
      
    }
    
    var node = dojo.byId("frmSearchCriteria:srExpandResults");
    if(typeof(node) != 'undefined' && 
    node != null && (node.checked == "checked" || node.checked == true)) {
      restParams += "&expandResults=true";
    }
    
    return restParams;
  }

  /**
   *Does a search on the specified page number
   *
   *@param page = The Search page
   **/
  function scSetPageTo(page) {
    dojo.byId("frmSearchCriteria:scCurrentPage").setAttribute("value", page + '');
    scDoAjaxSearch();
    return false;
  }

  /* Does an ajax search and injects the results into the page
   */
  var _xhrSearch;
  var _lastSearch = "";
  function scDoAjaxSearch(clear, searchUrl) {
  
    if(_xhrSearch) {
      try {
        _xhrSearch.cancel();
      } catch(err) {
        //GptUtils.logl(GptUtils.log.Level.WARNING,
        //"Error while cancelling search post" + err);
      }
    }
  
    // Add loading gif in results container
    var elLoadingGif = dojo.byId("frmSearchCriteria:loadingGif");
    dojo.query("#cmPlPgpGptMessages").empty();
    if (typeof(scMap) != 'undefined') scMap.reposition();
    dojo.style(elLoadingGif, "visibility", "visible");
    
    var restUrlParams = scReadRestUrlParams();
    if(typeof(clear) == 'boolean' && clear == true) {
      restUrlParams = '';
    }
    if(typeof(searchUrl) == 'string') {
      restUrlParams = searchUrl;
    }
    var urlToSearch = contextPath + "/rest/find/document?" +
        restUrlParams;
    
    if ( _csSearchTimeOut > 0) {
      urlToSearch += "&maxSearchTimeMilliSec=" + _csSearchTimeOut;
    }   
    urlToSearch +=  "&f=searchpageresults";  
    
    var el = dojo.byId("frmSearchCriteria:scSearchUrl");
    el.setAttribute("value", urlToSearch);
    //serilizeFormToCookie();
    
      
    _xhrSearch = dojo.xhrGet({
   
      url: urlToSearch,
  
      load: dojo.hitch(this, function (data) {
        if(typeof(clear) == 'boolean' && clear == true) {
          window.location = contextPath + "/catalog/search/search.page";
          return;
        }
        if(typeof(clear) == 'boolean' && clear == true) {
          restUrlParams = '';
        }
        if(typeof(data) != 'string' || data == null || data.length < 1 ||
          data.toLowerCase().indexOf("text/javascript") < 0) {
          throw new Error("No data recieved from server");
        } 
        scInitTextFields();
        scReconfigureCriteria();
        var elLoadingGif = dojo.byId("frmSearchCriteria:loadingGif");
        dojo.style(elLoadingGif, "visibility", "hidden");
        data = data.replace(/\r\n|\r/g, '');
        data = data.replace(/<\/form>/i, "");
        data = data.replace(/<form.*>/gi, "");
        data = data.replace(/<\/html>/i, "");
        data = data.replace(/<html.*>/gi, "");
        data = data.replace(/<\/body>/i, "");
        data = data.replace(/<body.*>/gi, "");
        
        // I.E. and Chrome cannot execute javascript when we add the html
        // via dojo.addContent. Below extracts javascript and executes it.
        // Fragile.  Dependent on our jscript compnent having a certain 
        // comment in front of it and being a one liner jscript.
        var jScript = ""; 
        var jScripts = data.match(/\/\* Component .*;/gi);
        if(jScripts == null) {
          throw new Error("No data recieved from server");
        }
        
        for( var i = 0; i < jScripts.length; i++) {
          jScript = jScripts[i];
          jScript = jScript.replace(/\/\* Comp.*var/gi, "");
          eval(jScript);
        }
        
        data = data.replace(/\/\* Component .*;/gi, "");
        data = data.replace(/document.forms\['frmSearchCriteria'].submit/mgi,
        "scDoAjaxSearch");
        data = data.replace(/name="frmSearchCriteria"/mgi,
        "name=\"suppressed_frmSearchCriteria");
        data = data.replace(/name="frmSearchCriteria:_idcl"/mgi,
        "name=\"suppressed_frmSearchCriteria");
        dojo.query("#frmSearchCriteria\\:srResultsPanel").empty();
        dojo.query("#frmSearchCriteria\\:srResultsPanel").addContent(data);
        var node = dojo.byId("frmSearchCriteria:srResultsPanel");
        dojo.style(node, "visibility", "visible");
        dojo.style(node, "display", "block");
        if(typeof(rsInsertReviews) != 'undefined') {
          rsInsertReviews();
        }
        if ((typeof(itemCart) != "undefined") && (itemCart != null)) {
          itemCart.connectToSearchResults();
        }

		if(typeof(rsGetQualityOfService) != 'undefined') {
  	      try{
  	  		rsGetQualityOfService();
	  	  	} catch(error) {
	  	  	    console.log("unable to fetch quality of service info : ", error);
	  	  	}
        }

        //scShowDistrSearchSites(false);
        try {
          scMap.clearFootPrints();
          scMap.drawFootPrints();
        } catch(error) {
          GptUtils.logl(GptUtils.log.Level.WARNING,
          "Error on save criteria" + error);
        }
        aoiMinX = parseInt(tmpAoiMinX);
        aoiMinY = parseInt(tmpAoiMinY);
        aoiMaxX = parseInt(tmpAoiMaxX);
        aoiMaxY = parseInt(tmpAoiMaxY);
        aoiWkid = parseInt(tmpAoiWkid);
       
      }),
      preventCache: true,
      error: function(args) {
        scInitTextFields();
        scReconfigureCriteria();
        var elLoadingGif = dojo.byId("frmSearchCriteria:loadingGif");
        dojo.style(elLoadingGif, "visibility", "hidden");
        if (args.dojoType =='cancel') {
          return;
        } else {
          dojo.query("#frmSearchCriteria\\:srResultsPanel").empty();
          dojo.query("#cmPlPgpGptMessages").addContent(
          "<div style=\"width: 800px;\" class=\"errorMessage searchInjectedError\">" + args.message +
            "<a href=\""+ this.url + "\" target=\"_blank\">" + this.url + "</a>"   +
            "</div>" + "<br/>" + 
          "<div style=\"width: 800px;\" class=\"errorMessage searchInjectedError\">" + 
              csErrorLabel +
          "</div>"  
            
          );
          
          if (typeof(scMap) != 'undefined') scMap.reposition();
          /*GptUtils.logl(GptUtils.log.Level.WARNING,
            "Error while performing search" + args);*/
        }
      }
  
    });
  

    return false;
  }

  /**
  Updated by distributed search iframe
   **/
  function updateDistributedSearch(rid, message, status) {
   
  
    var fullMessage = message;
   
    // Status comes from com.esri.gpt.control.rest.search.SearchStatus
    dojo.query("#distrHitCountTxt" + srNormalizeId(rid)).empty();
    if(status == "failed") {
      if(message.length > 15) {
        message = message.substring(0, 14) + "..."
      }
      // TODO: encodeURIComponent the message
      message = 
          "<div class=\"errorMessage\">" 
          + "<a target=\"_blank\" href=\""+ formerDistributedSearchUrl + "&f=atom"
          +"\" title =\"" + fullMessage + "\">" 
          + message 
          + "</a>"; 
        + "</div>";
      dojo.query("#distrLoadImg" + srNormalizeId(rid)).style("visibility", "hidden");  
    }
    if(status == "working" ) {
      dojo.query("#distrLoadImg" + srNormalizeId(rid)).style("visibility", "visible");
      dojo.query("#distrHitCountTxt" + srNormalizeId(rid)).empty();
      return;
    } else if (status == "completed") {
      dojo.query("#distrLoadImg" + srNormalizeId(rid)).style("visibility", "hidden");
    }
    dojo.query("#distrHitCountTxt" + srNormalizeId(rid)).addContent(message + '');
  }


  /**
  Distributed search is now done
   **/
  function distributedSearchDone() {
    dojo.query(".loadingImages").style("visibility", "hidden");
    //dojo.query(".loadingImages").style("display", "block");
  }


  /**
  Distributed search row
   **/
  function scCreateDistrSearchRow(uuid, name, isCurrentSearchSite) {
    
    if(name == null) {
      name = "";
    }
    
    var highlightClass = "distributed";
    if(typeof(isCurrentSearchSite) == 'boolean' && isCurrentSearchSite == true) {
      highlightClass = "selectedResultRow";
    }
    
    var htmlCatalogs = "<tr><td>";
    htmlCatalogs += "<table ";
    htmlCatalogs += 'id="'+ uuid + '" ';
    htmlCatalogs += 'class="distrSiteTable '+ highlightClass +'" ';
    htmlCatalogs += 'width="100%">';
      
    htmlCatalogs += "<tr><td><div style=\"float:left; width: 65%;\">";

    htmlCatalogs += "<a href=\"#\" onclick=\"javascript: srChangeDistrSearch('"
      + uuid + "','" + name.replace("'", "\\'") + "'); return false;\">" + name + "</a>";
    htmlCatalogs += "&nbsp;&nbsp;&nbsp;&nbsp;";
    
    htmlCatalogs += "</div><div style=\"float:right;\"><span class=\"distrHitCountTxt\" id=\"distrHitCountTxt"
      + srNormalizeId(uuid)+ "\"" +
      " style=\"text-align: right;\" " +
      " name=\"distrHitCountTxt\" " +
      ">";
    htmlCatalogs += "</span>";
    htmlCatalogs += "<img height=\"20\" style=\"visibility:hidden; \"  "
      + "name=\"distrLoadingImg\" alt=\"\" src=\""
      + contextPath + "/catalog/images/loading.gif\" class=\"loadingImages\" "
      + "id=\"distrLoadImg" + srNormalizeId(uuid) + "\"/>";    
    htmlCatalogs += "</div></td></tr>";
    htmlCatalogs += "</table>";
    htmlCatalogs += "</td></tr>";
  
    return htmlCatalogs;
  }

  /*
  Adds the distributed search points to the dom
   */
  function scAddDistrSearchPoints(htmlCatalogs) {
    htmlCatalogs = "<table width=\"100%\" >"
      + htmlCatalogs + "</table>";
    /* dojo.query("#frmSearchCriteria\\:srDistributedPanel").empty();
    dojo.query(
      "#frmSearchCriteria\\:srDistributedPanel").addContent(htmlCatalogs);*/
    dojo.query("#cntDistributedSearches").empty();
    dojo.query("#cntDistributedSearches").addContent(htmlCatalogs);
    dojo.query(
    "#frmSearchCriteria\\:srDistributedPanel").style("display", "block");
    dojo.query(
    "#frmSearchCriteria\\:srDistributedPanel").style("visibility", "visible");
    if (typeof(scMap) != 'undefined') scMap.reposition();
  }

  /*
  Positions the distributed search pane
   **/
  function scInitDistrPane() {
  
    if(GptUtils.valChkBool(_csAllowDistributedSearch) == false) {
    	if(dojo.byId("djtCntDistributedSearches") != null) {
        dojo.style("djtCntDistributedSearches",
           {visibility:"hidden", display:"none"});
      }
    	if(dojo.byId("djtCntDistributedSearchesImg") != null) {
        dojo.style("djtCntDistributedSearchesImg",
          {visibility:"hidden", display:"none"});
    	}
    	if(dojo.byId("frmSearchCriteria:_pngCtypeRemote") != null) {
        dojo.style("frmSearchCriteria:_pngCtypeRemote",
              {visibility:"hidden", display:"none"});
    	}
      return;
    }
    if(typeof(_scSearchSites) == 'undefined'|| typeof(_scSearchSites) == 'string'
     || _scSearchSites == null
     || typeof(_scSearchSites.rows) == 'undefined' || _scSearchSites.rows == null
     || typeof(_scSearchSites.rows.length) == 'undefined' || _scSearchSites.rows == null
     || _scSearchSites.rows.length <= 1) {
      // Do not draw anything.  There are no harvest rows
      dojo.style("djtCntDistributedSearchesImg",
          {visibility:"hidden", display:"none"});
      return;
    }

    dojo.query("#distrSearchTitle").onclick(function(evt) {
      scShowDistrSearchSites();
    });
   
    dojo.style("djtCntDistributedSearchesTitle", "cursor", "pointer");
    dojo.query("#djtCntDistributedSearchesTitleRemark").empty().addContent(_csTitleDistributedSearchRemark);

    dojo.query("#cntDistributedSearchesConfigCaption").empty().addContent(_csConfigDistributedSearchCaption);
    var remark  = _csConfigDistributedSearchCaptionRemark.replace("{0}", _csDistributedSearchMaxSelectedSites);
    dojo.query("#cntDistributedSearchesConfigCaptionRemark").empty().addContent(remark);

    var showDistrPanel = GptUtils.valChkBool(dojo.byId("frmSearchCriteria:scDistributedSitesPanelShow").value);
    scShowDistrSearchSites(showDistrPanel);
   
    if (typeof(scMap) != 'undefined') scMap.reposition();
  }

  /**
  Shows distributed search box or not.
  @param show is boolean.  Can be undefined and box will just be toggled.
   **/
  var _showingDistrSearchSites = false;
  function scShowDistrSearchSites(show) {

    var elDvContent = dojo.byId("dvDistributedSearchesContent");
    var elRemark    = dojo.byId("djtCntDistributedSearchesTitleRemark");

    dojo.style("djtCntDistributedSearches", {visibility: "visible"});
    var lShow = true;
    if(typeof(show) == 'boolean') {
      lShow = show;
     
    } else {
      lShow = elDvContent.style.display=="none";
    }
    
    if(lShow == true) {
      
      _showingDistrSearchSites = true;
      dojo.style(elDvContent, {visibility:"visible", display:"block"} );
//      dojo.style(elRemark, {visibility:"visible", display:"none"} );
    } else {
      _showingDistrSearchSites = false;
      dojo.style(elDvContent, {visibility:"visible", display:"none"} );
//      dojo.style(elRemark, {visibility:"visible", display:"block"} );
    }

    var elImg = dojo.byId("djtCntDistributedSearchesImg");
    if (elImg!=null) {
      elImg.src = lShow? contextPath + "/catalog/images/section_open.gif": contextPath + "/catalog/images/section_closed.gif";
    }
    if (typeof(scMap) != 'undefined') scMap.reposition();
  }

  /**
   * Reads selected searches and fills the distributed search components.
   * Also selects a selected site as the default search site so works even
   * on none distributed searches
   *
   */
  function scWriteToDistrEndPointComponents(option) {
    
    if(_csDistributedSearchMaxSelectedSites > 1
      && typeof(option) == 'object') {
      var numberOfChecked = 
        dojo.query("input[name=remoteCatalog]:checked").length;
      if(option.checked == true && 
          numberOfChecked > _csDistributedSearchMaxSelectedSites) {
        option.checked = false;
        return false;
      } else if(option.checked == false && 
          numberOfChecked < 1) {
        option.checked = true;
        return false;
      }
     }
    if(_scSearchSites.rows && _scSearchSites.rows.length 
        && _scSearchSites.rows.length <= 1) {
        
       // Do not draw anything.  There are no other rows
       return;
    } 
    var tmpIds = "";
    var tmpNames = "";
    var tmpSelected = new Array();
    var isDistributedSearch = false;
    var siteNames = "";
    var htmlCatalogs = "";
    var selectedRid = dojo.byId("frmSearchCriteria:_harvestSiteId").value;
 
    // Elements containing remote catalogs
    var catalogs = document.getElementsByName("remoteCatalog");
    if(catalogs != null) {
    
      for(var i = 0; i < catalogs.length; i++) {
        if(typeof (catalogs[i].checked) != undefined
          && (catalogs[i].checked == true ||  catalogs[i].checked == 1)) {
                
          tmpSelected[tmpSelected.length] = i;
          if(tmpSelected.length >= 2) {
            tmpIds += ",";
            siteNames += ", ";
          }
          // Hold values in variables to be written to components later
          // Note that they are encodeURIComponentd!
          tmpIds = tmpIds + encodeURIComponent(_scSearchSites.rows[i].uuid);
          siteNames = siteNames + encodeURIComponent(_scSearchSites.rows[i].name);
          htmlCatalogs +=  scCreateDistrSearchRow(_scSearchSites.rows[i].uuid,
          _scSearchSites.rows[i].name, selectedRid == _scSearchSites.rows[i].uuid );
        }
      }
    }
  
    // This will do the actual set of the site id to search and the name
    // of the site to search.  This is done even when only one endpoint is selected
    var elSiteName = document.getElementById("frmSearchCriteria:_harvestSiteName");
    var elHarvestId = document.getElementById("frmSearchCriteria:_harvestSiteId");
    var arrIds = tmpIds.split(",");
    if(tmpSelected.length < 1) {
      // Turn this to the last site that was selected
      tmpIds = encodeURIComponent(elHarvestId.value);
      siteNames = encodeURIComponent(elSiteName.value);
      htmlCatalogs =  scCreateDistrSearchRow(elHarvestId.value, elSiteName.value);
      tmpSelected = 1;
    } else if(dojo.indexOf(arrIds, encodeURIComponent(elHarvestId.value)) < 0) {
      elHarvestId.value = decodeURIComponent(arrIds[0]);
      elSiteName.value = decodeURIComponent((siteNames.split(","))[0]);
    }
    
  
    if(tmpSelected.length < 1) {
      // Nothing selected so return
      return true;
    }
  
    // Gui addition
    scAddDistrSearchPoints(htmlCatalogs);
 
    // Write distribute search info to these components
    var elDistributedIds =
      document.getElementById("frmSearchCriteria:scSelectedDistributedIds");
    //var elDistributedNames =
      //document.getElementById("frmSearchCriteria:scSelectedDistributedNames");
    elDistributedIds.value = tmpIds;
    //elDistributedNames.value = siteNames;
    return true;
  
  }



  /**
  Changes the distributed search end poi
  @param uuid to change to
  @param name the name of the endpoint
   **/
  function srChangeDistrSearch(uuid, name) {
    var doDistributed = false;
  
    dojo.query(".distrHitCountTxt").forEach(
    function(node, index, arr) {
      if((typeof(node.innerText) != 'undefined'
        && GptUtils.valChkStr(node.innerText).length < 1) ||
        typeof(node.textContent) != 'undefined'
        && GptUtils.valChkStr(node.textContent).length < 1) {
        doDistributed = true;
      }
    }
    );
     
    scExecuteDistributedSearch();
    
    var el = dojo.byId("frmSearchCriteria:_harvestSiteId");
    el.setAttribute("value", uuid);
    el = dojo.byId("frmSearchCriteria:_harvestSiteName");
    el.setAttribute("value", name);
    scInitTextFields();
    scReconfigureCriteria();
    scSetPageTo(1);
  }
 


  </script>
</f:verbatim> 

<gpt:jscriptVariable 
  id="_csExteriorRepositories"
  quoted="false"
  value="#{SearchFilterHarvestSites.jscriptForeignSites}"
  variableName="csExteriorRepositories"/>

<gpt:jscriptVariable 
  id="_csDefaultSiteLabel"
  quoted="true"
  value="#{gptMsg['catalog.search.searchSite.defaultsite']}"
  variableName="csDefaultSiteLabel"/>

<gpt:jscriptVariable 
  id="_csErrorLabel"
  quoted="true"
  value="#{gptMsg['com.esri.gpt.catalog.search.SearchException']}"
  variableName="csErrorLabel"/>

<gpt:jscriptVariable 
  id="_csTitleDistributedSearch"
  quoted="true"
  value="#{gptMsg['catalog.search.distributedSearch.WindowTitle']}"
  variableName="_csTitleDistributedSearch"/>

<gpt:jscriptVariable
  id="_csTitleDistributedSearchRemark"
  quoted="true"
  value="#{gptMsg['catalog.search.distributedSearch.WindowTitleRemark']}"
  variableName="_csTitleDistributedSearchRemark"/>


<gpt:jscriptVariable
  id="_csConfigDistributedSearchCaption"
  quoted="true"
  value="#{gptMsg['catalog.search.distributedSearch.ConfigTitle']}"
  variableName="_csConfigDistributedSearchCaption"/>

<gpt:jscriptVariable
  id="_csConfigDistributedSearchCaptionRemark"
  quoted="true"
  value="#{gptMsg['catalog.search.distributedSearch.ConfigTitleRemark']}"
  variableName="_csConfigDistributedSearchCaptionRemark"/>

<gpt:jscriptVariable 
  id="_csAllowDistributedSearch"
  quoted="true"
  value="#{SearchController.searchConfig.allowExternalSearch}"
  variableName="_csAllowDistributedSearch"/>

<gpt:jscriptVariable 
  id="_csDistributedSearchMaxSelectedSites"
  quoted="true"
  value="#{SearchController.searchConfig.distributedSearchMaxSelectedSites}"
  variableName="_csDistributedSearchMaxSelectedSites"/>

<gpt:jscriptVariable 
  id="_csDistributedSearchTimeoutMillisecs"
  quoted="true"
  value="#{SearchController.searchConfig.distributedSearchTimeoutMillisecs}"
  variableName="_csDistributedSearchTimeoutMillisecs"/>

<gpt:jscriptVariable 
  id="_csSearchTimeOut"
  quoted="true"
  value="#{SearchController.searchConfig.timeOut}"
  variableName="_csSearchTimeOut"/>

<gpt:jscriptVariable 
  id="_csDefaultSearchSite"
  quoted="true"
  value="#{SearchController.searchConfig.timeOut}"
  variableName="_csDefaultSearchSite"/>

<% // search text and submit button %>
<h:panelGrid columns="4">
  <h:outputLabel for="scText" value="#{gptMsg['catalog.search.search.lblSearch']}"/>
  <h:inputText id="scText"
               value="#{SearchController.searchCriteria.searchFilterKeyword.searchText}"
               maxlength="4000" styleClass="searchBox" />
  <h:commandButton id="btnDoSearch" rendered="true"
                   onclick="javascript:scSetPageTo(1); scExecuteDistributedSearch(); return false;"
                   value="#{gptMsg['catalog.search.search.btnSearch']}"
                   action="#{SearchController.getNavigationOutcome}"
                   actionListener="#{SearchController.processAction}">
    <f:attribute name="#{SearchController.searchEvent.event}"
                 value="#{SearchController.searchEvent.eventExecuteSearch}" />
    <f:attribute name="onSearchPage" value="true"/>
  </h:commandButton>
  <h:graphicImage
    id="loadingGif" 
    style="visibility: hidden;"
    url="/catalog/images/loading.gif" alt="" 
    width="30px">
  </h:graphicImage>
   <f:verbatim>
   	<div id="hints"></div>
   </f:verbatim>
</h:panelGrid> 

<h:panelGroup id="dockDistributedSearch" rendered="#{SearchController.searchConfig.allowExternalSearch == true}">
  <f:verbatim>
    <div id="djtCntDistributedSearches" class="section" style="width:400px">
      <div id="distrSearchTitle">
        <table width="100%">
          <tr>
            <td/>
            <td valign="top"><img id="djtCntDistributedSearchesImg" alt="" src="/geoportal/catalog/images/section_closed.gif"/></td>
            <td>
              <table width="100%" cellpadding="0" cellspacing="0">
                <tr><td id="djtCntDistributedSearchesTitle" class="sectionCaption"/></tr>
                <tr><td id="djtCntDistributedSearchesTitleRemark"  class="sectionCaptionRemark"/></tr>
              </table>
            </td>
          </tr>
        </table>
      </div>
      <div id="dvDistributedSearchesContent">
        <div id="cntDistributedSearches" class="sectionBody">
        </div>
        <div id="cntDistributedSearchesConfigHeader">
          <table width="100%" cellpadding="0" cellspacing="0">
            <tr><td id="cntDistributedSearchesConfigCaption" class="sectionCaption"/></tr>
            <tr><td id="cntDistributedSearchesConfigCaptionRemark"  class="sectionCaptionRemark"/></tr>
          </table>
        </div>
        <%--
        <div id="cntDistributedSearchesConfigCaption" class="sectionCaption">
          <div id="cntDistributedSearchesConfigCaptionRemark" class="sectionCaptionRemark">
          </div>
        </div>
        --%>
        <div id="cntDistributedSearchesConfig" 
           class="sectionBody" style="max-height:200px;overflow:auto;">
        </div>
      </div>
    </div>
  </f:verbatim>
</h:panelGroup>

<h:outputText id="brkscLnkAdditionals" escape="false" value="<br/>"/>
<h:outputLink id="scLnkAdditionals" 
              onclick="javascript:scShowDialog('crtAdvOptnsContent', true)" value="javascript:void(0)">
  <h:outputText escape="false" value="#{gptMsg['catalog.search.additionalOptions']}" />
</h:outputLink>

<h:outputText id="txtClearHtml" escape="false" value="<br/>"/>
<h:outputLink
  value="#"
  onclick="javascript:scDoAjaxSearch(true); return false;">
  <h:outputText escape="false" 
    value="#{gptMsg['catalog.search.search.btnReset']}" />
</h:outputLink>

<h:inputHidden id="_harvestSiteName"
               value="#{SearchFilterHarvestSites.selectedHarvestSiteName}" />
<h:inputHidden id="_harvestSiteProfile"
               value="#{SearchFilterHarvestSites.selectedHarvestSiteProfile}" />
<h:inputHidden id="_harvestSiteUrl"
               value="#{SearchFilterHarvestSites.selectedHarvestSiteUrl}" />
<h:inputHidden id="_harvestSiteId"
               value="#{SearchFilterHarvestSites.selectedHarvestSiteId}" />
<h:inputHidden id="_harvestSiteSupportsCtype"
               value="#{SearchFilterHarvestSites.selectedHarvestSiteSupoortsCtpQury}" />
<h:inputHidden id="_harvestSiteSupportsSptlQury"
               value="#{SearchFilterHarvestSites.selectedHarvestSiteSupoortsSptlQury}" />
<h:inputHidden id="_havestSiteUsernameHidden"
               value="#{SearchFilterHarvestSites.selectedHarvestSiteUsername}" />
<h:inputHidden id="_havestSitePasswordHidden"
               value="#{SearchFilterHarvestSites.selectedHarvestSitePassword}" />


<% // spatial filter %>
<h:outputText escape="false" value="<h3>"/>
<h:outputText id="scLblSptial" value="#{gptMsg['catalog.search.filterSpatial.title']}" />
<h:outputText escape="false" value="</h3>"/>
<h:panelGrid columns="1" id="scPnlSpatial">
  <h:selectOneRadio id="scSelSpatial"
                    value="#{SearchController.searchCriteria.searchFilterSpatial.selectedBounds}"
                    onselect="javascript:scOnSpatialOptionClicked(this);"
                    onclick="javascript:scOnSpatialOptionClicked(this);">
    <f:selectItem itemValue="anywhere"
                  itemLabel="#{gptMsg['catalog.search.filterSpatial.anywhere']}" />
    <f:selectItem itemValue="useGeogExtent"
                  itemLabel="#{gptMsg['catalog.search.filterSpatial.useGeogExtent']}" />
    <f:selectItem itemValue="dataWithinExtent"
                  itemLabel="#{gptMsg['catalog.search.filterSpatial.dataWithinExtent']}" />
  </h:selectOneRadio>

  <% // map %>
  <h:panelGrid id="pnlMap">
    <h:panelGroup id="mapToolbar" styleClass="mapToolbar">
      <h:outputLabel for="mapInput-locate" value="#{gptMsg['catalog.search.search.lblLocator']}"/>
      <h:inputText id="mapInput-locate" styleClass="locatorInput"
                   maxlength="1024" onkeypress="return scMap.onLocatorKeyPress(event);"/>
      <h:graphicImage id="mapButton-locate" url="/catalog/images/btn-locate-off.gif"
                      alt="#{gptMsg['catalog.general.map.locate']}"
                      title="#{gptMsg['catalog.general.map.locate']}"/>
    </h:panelGroup>
    <f:verbatim>
      <div id="locatorCandidates" class="locatorCandidates"></div>
         <div id="interactiveMap" 
             dojotype="dijit.layout.ContentPane"
             style="width:360px; height:220px; cursor:pointer; border: 1px solid #000000;">
        </div>
    </f:verbatim>

    <h:inputHidden id="sfsMinX" 
                   value="#{SearchController.searchCriteria.searchFilterSpatial.envelope.minX}" />
    <h:inputHidden id="sfsMinY" 
                   value="#{SearchController.searchCriteria.searchFilterSpatial.envelope.minY}" />
    <h:inputHidden id="sfsMaxX" 
                   value="#{SearchController.searchCriteria.searchFilterSpatial.envelope.maxX}" />
    <h:inputHidden id="sfsMaxY" 
                   value="#{SearchController.searchCriteria.searchFilterSpatial.envelope.maxY}" />
    <h:inputHidden id="sfsVMinX" 
                   value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.minX}" />
    <h:inputHidden id="sfsVMinY" 
                   value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.minY}" />
    <h:inputHidden id="sfsVMaxX" 
                   value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.maxX}" />
    <h:inputHidden id="sfsVMaxY" 
                   value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.maxY}" />
    <h:inputHidden id="sfsVWkid" 
                   value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.wkid}" />

  </h:panelGrid>
</h:panelGrid>


<h:outputText escape="false" 
              value='<div id="crtAdvOptnsContent" dojoType="dijit.Dialog"
              class="tundra" style="width: 400px; display: none; border: 1px solid #000000; background: #FFFFFF;" title="#{gptMsg["catalog.search.additionalOptions"]}">'/>

<h:outputText escape="false" value='<div style="padding:0px 10px 10px 10px;">'/>
<% // content type %>
<h:panelGroup id="_pngCtypeLocal">
  <h:outputText escape="false" value="<h3>"/>
  <h:outputLabel for="scSelContent" id="scLblContent" value="#{gptMsg['catalog.search.filterContentTypes.title']}" />
  <h:outputText escape="false" value="</h3>"/>
  <h:panelGrid id="scPnlContent">
    <h:selectOneMenu id="scSelContent"
                     value="#{SearchController.searchCriteria.searchFilterContentTypes.selectedContentType}"
                     onchange="javascript:updateHiddenValue(this)"
                     >
      <f:selectItem itemValue=""
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.default']}" />
      <f:selectItem itemValue="liveData"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.liveData']}" />
      <f:selectItem itemValue="downloadableData"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.downloadableData']}" />
      <f:selectItem itemValue="offlineData"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.offlineData']}" />
      <f:selectItem itemValue="staticMapImage"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.staticMapImage']}" />
      <f:selectItem itemValue="document"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.document']}" />
      <f:selectItem itemValue="application"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.application']}" />
      <f:selectItem itemValue="geographicService"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.geographicService']}" />
      <f:selectItem itemValue="clearinghouse"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.clearinghouse']}" />
      <f:selectItem itemValue="mapFiles"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.mapFiles']}" />
      <f:selectItem itemValue="geographicActivities"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.geographicActivities']}" />
      <f:selectItem itemValue="unknown"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.unknown']}" />
    </h:selectOneMenu>
  </h:panelGrid>
</h:panelGroup>

<h:panelGroup id="_pngCtypeRemote">
  <h:outputText escape="false" value="<h3>"/>
  <h:outputLabel for="scSelContentR" id="scLblContentR" value="#{gptMsg['catalog.search.filterContentTypes.title']}" />
  <h:outputText escape="false" value="</h3>"/>
  <h:panelGrid id="scPnlContentR">
    <h:selectOneMenu id="scSelContentR"
                     onchange="javascript:updateHiddenValue(this, 'frmSearchCriteria:scSelContentHidden')"
                     value="#{SearchController.searchCriteria.searchFilterContentTypes.selectedContentType}">
      <f:selectItem itemValue=""
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.default']}" />
      <f:selectItem itemValue="liveData"
                    itemLabel="#{gptMsg['catalog.search.filterContentTypes.liveData']}" />
    </h:selectOneMenu>
  </h:panelGrid>
</h:panelGroup>


<% // data theme %>
<h:panelGroup id="_pngDataThemes">
  <h:outputText escape="false" value="<h3>"/>
  <h:outputText id="scLblTheme" value="#{gptMsg['catalog.search.filterThemeTypes.title']}" />
  <h:outputText escape="false" value="</h3>"/>
  <h:outputText escape="false" value='<div style="overflow:auto; height:200px;">'/>
  <h:panelGrid id="scPnlTheme">
    <h:selectManyCheckbox id="scSelTheme" layout="pageDirection"
                          value="#{SearchController.searchCriteria.searchFilterThemes.selectedThemes}">
      
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.boundaries']}"
        itemValue="boundaries" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.farming']}"
        itemValue="farming" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.climatologyMeteorologyAtmosphere']}"
        itemValue="climatologyMeteorologyAtmosphere" />  
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.biota']}"
        itemValue="biota" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.economy']}"
        itemValue="economy" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.planningCadastre']}"
        itemValue="planningCadastre" /> 
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.society']}"
        itemValue="society" /> 
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.elevation']}"
        itemValue="elevation" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.environment']}"
        itemValue="environment" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.structure']}"
        itemValue="structure" />  
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.geoscientificInformation']}"
        itemValue="geoscientificInformation" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.health']}"
        itemValue="health" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.imageryBaseMapsEarthCover']}"
        itemValue="imageryBaseMapsEarthCover" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.inlandWaters']}"
        itemValue="inlandWaters" /> 
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.location']}"
        itemValue="location" />   
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.intelligenceMilitary']}"
        itemValue="intelligenceMilitary" />
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.oceans']}"
        itemValue="oceans" />  
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.transportation']}"
        itemValue="transportation" />  
      <f:selectItem
        itemLabel="#{gptMsg['catalog.mdCode.topic.utilitiesCommunication']}"
        itemValue="utilitiesCommunication" />
    </h:selectManyCheckbox>
  </h:panelGrid>
  <h:outputText escape="false" value="</div>"/>
</h:panelGroup>

<% // modification date %>
<h:panelGroup id="_pngModDateSection">
  <h:outputText escape="false" value="<h3>"/>
  <h:outputText id="scLblDate" value="#{gptMsg['catalog.search.filterTemporal.title']}" />
  <h:outputText escape="false" value="</h3>"/>
  <h:panelGrid columns="3" id="scPnlDate">
    <h:outputLabel id="scLblDateFrom" for="scDateFrom"
                   value="#{gptMsg['catalog.search.filterTemporal.dateFrom']}" />
    <h:inputText id="scDateFrom" maxlength="10"
                 value="#{SearchController.searchCriteria.searchFilterTemporal.dateModifiedFrom}"
                 onchange="javascript:updateHiddenValue(this)"/>
    <h:outputText value="#{gptMsg['catalog.general.inputDateFormat']}" />
    <h:outputLabel id="scLblDateTo" for="scDateTo"
                   value="#{gptMsg['catalog.search.filterTemporal.dateTo']}" />
    <h:inputText id="scDateTo" maxlength="10"
                 onchange="javascript:updateHiddenValue(this)"
                 value="#{SearchController.searchCriteria.searchFilterTemporal.dateModifiedTo}" />
    <h:outputText value="#{gptMsg['catalog.general.inputDateFormat']}" />
  </h:panelGrid>
</h:panelGroup>


<% // sort option %>
<h:panelGroup id="_pngSortSection">
  <h:outputText escape="false" value="<h3>"/>
  <h:outputLabel for="scSelSort" id="scLblSort" value="#{gptMsg['catalog.search.filterSort.labelSort']}" />
  <h:outputText escape="false" value="</h3>"/>
  <h:panelGrid id="scPnlSort">
    <h:selectOneMenu id="scSelSort"
                     value="#{SearchController.searchCriteria.searchFilterSort.selectedSort}"
                     onchange="javascript:updateHiddenValue(this)">
      <f:selectItem itemValue="relevance"
                    itemLabel="#{gptMsg['catalog.search.filterSort.Relevance']}" />
      <f:selectItem itemValue="title"
                    itemLabel="#{gptMsg['catalog.search.filterSort.Title']}" />
      <f:selectItem itemValue="format"
                    itemLabel="#{gptMsg['catalog.search.filterSort.Format']}" />
      <f:selectItem itemValue="dateAscending"
                    itemLabel="#{gptMsg['catalog.search.filterSort.dateAscending']}" />
      <f:selectItem itemValue="dateDescending"
                    itemLabel="#{gptMsg['catalog.search.filterSort.dateDescending']}" />
      <f:selectItem itemValue="areaAscending"
                    itemLabel="#{gptMsg['catalog.search.filterSort.areaAscending']}" />
      <f:selectItem itemValue="areaDescending"
                    itemLabel="#{gptMsg['catalog.search.filterSort.areaDescending']}" />
    </h:selectOneMenu>

  </h:panelGrid>
</h:panelGroup>

<h:panelGrid columns="2" style="margin-left: auto; margin-right: auto">
  <h:commandButton id="btnOkAdv" style="text-align:center;"
                   onclick="javascript: scShowDialog('crtAdvOptnsContent', false); scAdvOptDialog(false); return false;"
                   value="#{gptMsg['catalog.general.dialog.ok']}">
  </h:commandButton>
  <h:commandButton id="btnCancelAdv" style="text-align:center;"
                   onclick="javascript:scShowDialog('crtAdvOptnsContent', false); scAdvOptDialog(true); return false;"
                   value="#{gptMsg['catalog.general.dialog.cancel']}">
  </h:commandButton>
</h:panelGrid>

<h:outputText escape="false" value="</div>"/>

<h:outputText escape="false" value="</div>"/>
<h:inputHidden id="scSelSortHidden" 
               value="#{SearchController.searchCriteria.searchFilterSort.selectedSort}"/>
<h:inputHidden id="scDateToHidden" 
               value="#{SearchController.searchCriteria.searchFilterTemporal.dateModifiedTo}"/>
<h:inputHidden id="scDateFromHidden" 
               value="#{SearchController.searchCriteria.searchFilterTemporal.dateModifiedFrom}"/>
<h:inputHidden id="scSelContentHidden" 
               value="#{SearchController.searchCriteria.searchFilterContentTypes.selectedContentType}"/>
<h:inputHidden id="scSelThemeHidden" 
               value="#{SearchController.searchCriteria.searchFilterThemes.selectedThemes}">
  <f:converter  converterId="gpt.ListToString" />
</h:inputHidden>
<h:inputHidden id="scCurrentPage"
               value="#{SearchController.searchCriteria.searchFilterPageCursor.currentPage}"/>
<h:inputHidden id="scRecordsPerPage"
               value="#{SearchController.searchCriteria.searchFilterPageCursor.recordsPerPage}"/>
<h:inputHidden id="scSelectedDistributedIds" 
               value="#{SearchController.searchFilterHarvestSites.selectedDistributedIds}">
</h:inputHidden>
<h:inputHidden id="scDistributedSitesPanelShow" 
               value="#{SearchController.searchFilterHarvestSites.distributedPanelOpen}"/>
<h:inputHidden id="scSearchUrl" 
               value="#{SearchController.searchFilterHarvestSites.searchUrl}"/>
              

