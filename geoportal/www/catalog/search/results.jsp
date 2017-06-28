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
<%// results.jsp - Search results (JSF include)%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<gpt:jscriptVariable 
   quoted="false" value="#{SearchController.serviceCheckerEnabled}" 
   variableName="serviceCheckerEnabled" 
   id="serviceCheckerEnabled">
</gpt:jscriptVariable>

<gpt:jscriptVariable 
   quoted="" value="#{SearchController.serviceCheckerCheckUrl}" 
   variableName="serviceCheckerCheckUrl" 
   id="serviceCheckerCheckUrl">
</gpt:jscriptVariable>

<gpt:jscriptVariable 
   quoted="" value="#{SearchController.serviceCheckerInfoUrl}" 
   variableName="serviceCheckerInfoUrl" 
   id="serviceCheckerInfoUrl">
</gpt:jscriptVariable>

<gpt:jscriptVariable 
   quoted="" value="#{SearchController.serviceCheckerToken}" 
   variableName="serviceCheckerToken" 
   id="serviceCheckerToken">
</gpt:jscriptVariable>

<f:verbatim>

<script type="text/javascript">


/**
Expands all the records

@param oCheckbox True or false if records have been clicked.
**/
function rsExpandAllRecords(oCheckbox) {
  rsInsertReviews();
  var bChecked = oCheckbox.checked, aElements = document.getElementsByTagName("span"), el, i;
  if (aElements != null) {
    for (i=0;i<aElements.length;i++) {
      el = aElements[i];
      if (el && el.id && (el.id.indexOf(":recContent") != -1)) {
        if (bChecked) el.style.display = "block"; else  el.style.display = "none";
      }
    }
    if (typeof(scMap) != 'undefined') scMap.reposition();
  }
}

/**
On Record clicked, abstract and links are exposed
@param rowIndex The row index (From 0) that should be exposed
**/
function rsExpandRecord(rowIndex) {

  var requestIcon = false;;
  var el = document.getElementById("frmSearchCriteria:mdRecords:"+rowIndex+":recContent");
  if(el == null) {
    el = document.getElementById("mdRecords:"+rowIndex+":recContent");
  }
  if (el != null) {
    if (el.style.display == "none" ) {
      el.style.display = "block";
      requestIcon = true;
    } else {
      el.style.display = "none";
    }
    if (typeof(scMap) != 'undefined') scMap.reposition(); 
  }
  if( requestIcon == true && 
    !(typeof(jsMetadata) == 'undefined' || jsMetadata == null || 
     typeof(jsMetadata.records) == 'undefined' || jsMetadata.records == null)) {
     if(jsMetadata.records.length > rowIndex) {
        rsInsertRecordReview(jsMetadata.records[rowIndex], rowIndex);
     }
  }
  return false;
}

/**
HighLighting of record
@param The record that should be highLighted
**/
function rsHighlightRecord(uiComponent) {
  uiComponent.className = "selectedResultRow";
  if(typeof(srHighlightRecord) == "function") {
    srHighlightRecord(uiComponent);
  }
}

/**
Unhighlights the record
@param The record that should be unhighlighted
**/
function rsUnhighlightRecord(uiComponent) {
  uiComponent.className = "noneSelectedResultRow";
  if(typeof(srUnhighlightRecord) == "function") {
    srUnhighlightRecord(uiComponent);
  }
}


// Dictionary storing uuid to rownum to be used between rsInsertReviews and
// rsInsertReviewsHandler
var dictUuidToRowNum = new Array(); 
if(typeof(contextPath) == 'undefined' || contextPath == "") {
  var contextPath = "<%=request.getContextPath()%>";
}


function getCsv(url) {
	url = url.replace("f=pjson", "f=csv");
	window.open(url);

}

/**
Connects to the reviews endpoint and 
**/
function rsInsertReviews() {
	
  if(typeof(jsMetadata) == 'undefined' || jsMetadata == null || 
     typeof(jsMetadata.records) == 'undefined' || jsMetadata.records == null) {
     return;
  }
  var value = dojo.attr(dojo.byId("frmSearchCriteria:srExpandResults"), 
    "checked");
  if(value != "checked" && value != true && value != "true") {
    // expand all elements not checked
    return;
  }
  dictUuidToRowNum = new Array(); 
  dojo.forEach(jsMetadata.records,
    function(record, index, arr) {
      rsInsertRecordReview(record, index);
    }
  );

}


/**
Gets one record and inserts the review on the particular record
@ record The record object from jsMetadata.records object
@ index the index of the record
**/
function rsInsertRecordReview(record, index) {
  if(rsShowReviews == "none") {
    return;
  }
  if(typeof(scIsRemoteCatalog) != 'undefined') {
    if(scIsRemoteCatalog() == true) {
      return;
    }
  }
  
  if(typeof(record) == 'undefined' || record == null 
     || typeof(record.uuid) == 'undefined' || record.uuid == null 
     || record.uuid == "") {
    return;
  }
  
  var endPoint = contextPath +
       "/assertion?s=urn:esri:geoportal:resourceid:" + 
       "{0}&p=urn:esri:geoportal:rating:query&f=json";
  var uuid = record.uuid;
  dictUuidToRowNum[uuid] = index;
  var uuidReviewUrl = endPoint.replace("{0}", encodeURIComponent(uuid));
  dojo.xhrGet({ 
  
    url: uuidReviewUrl,
    preventCache: true,
    handleAs: "json",
    load: rsInsertReviewsHandler,
    error: function(err) {
          
    }
  });     
}


/**
Takes one record and updates the page
@param data, Json object with the review info
**/
function rsInsertReviewsHandler(data) {
  if(typeof(data) == 'undefined' || data == null ||
     typeof(data.subject) == 'undefined' || data.subject == null ||
     typeof(data.properties) == 'undefined' || data.properties == null ||
     typeof(data.properties.length)  != 'number' ||
     typeof(data.predicate) == 'undefined' || data.predicate == null ||
     data.predicate == "urn:esri:geoportal:operation:status:failed") {
     return;
  }
  var upCount = 0;
  var downCount = 0;
  var rating = 0;
  var uuid = data.subject.replace("urn:esri:geoportal:resourceid:", "");
  var recordPosition = dictUuidToRowNum[uuid];  
  var canCreate = false;
  var urlReview = contextPath + "/catalog/search/resource/review.page?uuid=" + 
    encodeURIComponent(uuid);
  for(var i = 0;  i < data.properties.length; i++) {
    if(typeof(data.properties[i].predicate) == 'undefined') {
      continue;
    }
    if(data.properties[i].predicate == 
      "urn:esri:geoportal:rating:value:up:count") {
      upCount = parseInt(data.properties[i].value);
    } else if(data.properties[i].predicate == 
      "urn:esri:geoportal:rating:value:down:count") {
      downCount = parseInt(data.properties[i].value);
    } else if(data.properties[i].predicate == 
      "urn:esri:geoportal:rating:count") {
      rating = parseInt(data.properties[i].value);
    } else if(data.properties[i].predicate == 
      "urn:esri:geoportal:rating:activeUser:canCreate") {
      canCreate = data.properties[i].value;
    }
  }
  
  if(upCount == NaN || downCount == NaN) {
    return;
  } 
  
  if(canCreate == "true" || canCreate == "True" || canCreate == "1" ||
    canCreate == 1 || canCreate == true) {
    canCreate = true;
  } else {
    canCreate == false;
  }
  
  if((canCreate == false || canCreate == "false") && upCount == 0 && downCount == 0 
      && rsShowReviews == "only-reviewed") { 
      return;
  }
 
  
  var elIcon = null;
  var elIconHide = null;
  var elIconHide1 = null;
  if(upCount == 0 && downCount == 0) {
     elIconHide1 = dojo.byId("frmSearchCriteria:mdRecords:" + recordPosition 
       + ":imgDown");
     elIconHide = dojo.byId("frmSearchCriteria:mdRecords:" + recordPosition 
       + ":imgUp");
     elIcon = dojo.byId("frmSearchCriteria:mdRecords:" + recordPosition 
       + ":imgNoReview"); 
  } else if((Math.abs(upCount) - Math.abs(downCount)) < 0) {
     elIcon = dojo.byId("frmSearchCriteria:mdRecords:" + recordPosition 
       + ":imgDown");
     elIconHide = dojo.byId("frmSearchCriteria:mdRecords:" + recordPosition 
       + ":imgUp");
     elIconHide1 = dojo.byId("frmSearchCriteria:mdRecords:" + recordPosition 
       + ":imgNoReview");  
  } else {
     elIcon = dojo.byId("frmSearchCriteria:mdRecords:" + recordPosition 
       + ":imgUp");
     elIconHide = dojo.byId("frmSearchCriteria:mdRecords:" + recordPosition 
       + ":imgDown"); 
     elIconHide1 = dojo.byId("frmSearchCriteria:mdRecords:" + recordPosition 
       + ":imgNoReview");   
  }
  
  dojo.style(elIcon, {
      "visibility" : "visible",
      "margin-right" : "0.1em",
      "display"  : ""
    });
  dojo.style(elIconHide, {
      "visibility" : "hidden",
      "display" : "none"
    }
  );
  dojo.style(elIconHide1, {
      "visibility" : "hidden",
      "display" : "none"
    }
  );
 
  var reviewAlt = rsReviewAlt.replace("{0}", upCount);
  reviewAlt = reviewAlt.replace("{1}", downCount);
  dojo.attr(elIcon, "alt", reviewAlt);
  dojo.attr(elIcon, "border", "0");
  dojo.attr(elIcon.parentNode, "title", reviewAlt);
  dojo.attr(elIcon.parentNode, "href", urlReview);
  
  var reviewCountLabel= rsReviewCountLabel.replace("{0}", 
    Math.abs(upCount) - Math.abs(downCount));
  if(upCount == 0 && downCount == 0) {
    reviewCountLabel = rsReview0CoutLabel;
  }
    
  var elLnkReviewIcon = dojo.byId("frmSearchCriteria:mdRecords:"+ recordPosition 
    + ":lnkReviewIcon");
  dojo.query(".reviewCountLabel", elLnkReviewIcon).orphan();
  var nodeList = new dojo.NodeList(elLnkReviewIcon);
  nodeList.addContent("<span class=\"reviewCountLabel\">" + reviewCountLabel + "</span>", 'first');
  dojo.style(elLnkReviewIcon, {
      "visibility" : "visible",
      "display" : ""
    }
  );
   
}

/*
Goes through the resource urls and types and forwards the information to
the service checker url via an xml to json jsp
*/
function rsGetQualityOfService() {
  if (!serviceCheckerEnabled) {
    // return immediatelly if service checker not enabled
    return;
  }
  dojo.query(".recordInfo").forEach(
    function(node, index) {
      var uuid = dojo.query(".recordUuid", node)[0].value;
      var url  = dojo.query(".recordUrl", node)[0].value;
      var type = dojo.query(".recordType", node)[0].value;
      
      
      if(type == null || type == "") {
        type = null;
      } else if(type.toLowerCase() == "aims") {
        type = "image";
      } else if(type.toLowerCase() == "wms" || type.toLowerCase() == "wfs" || type.toLowerCase() == "csw" || type.toLowerCase() == "kml" || type.toLowerCase() == "sos" || type.toLowerCase() == "ags") {
      } else {
        type = null;
      }

      //alert("Candid: "+uuid+", type: "+type);

      if (type!=null) {
        var checkUrl = 
          serviceCheckerCheckUrl + "?auth=" + serviceCheckerToken
          + "&type=" + encodeURIComponent(type)
          + "&id=" + encodeURIComponent(uuid)
          ;
        var infoUrl = 
          serviceCheckerInfoUrl + "?auth=" + serviceCheckerToken
          + "&serviceType=" + encodeURIComponent(type)
          + "&uId=" + encodeURIComponent(uuid)
        var context = {
          node: node,
          index: index,
          uuid: uuid,
          url: checkUrl,
          info: infoUrl,
          type: type
        };
        //alert("Calling: "+checkUrl);
        dojo.xhrGet({
          url: contextPath + "/catalog/download/proxy.jsp?"+checkUrl,
          handleAs: "json",
          load: dojo.hitch(context, function(data) {
            console.log("Received data: "+data);
            try {
              if (data.error!=null) {
                console.log(data.error.message);
              } else if (data.data!=null && data.data.constructor==Array && data.data.length>0) {
                var score = data.data[0].summary.scoredTest.currentScore;
                var info = null;
                var imgSrc = "<%=request.getContextPath()%>/catalog/images/serviceChecker"; 
                if(score == NaN){
                  imgSrc = imgSrc + "Unknown16.png";
                  info = "Unknown";
                } else if(score < 0) {
                  imgSrc = imgSrc + "Unknown16.png";
                  info = "Unknown";
                } else if(score <= 25) {
                  imgSrc = imgSrc + "VeryBad16.png";
                } else if(score <= 50 ) {
                  imgSrc = imgSrc + "Bad16.png";
                } else if(score <= 75 ) {
                  imgSrc = imgSrc + "Good16.png";
                } else if(score > 75 && score <= 100) {
                  imgSrc = imgSrc + "Excellent16.png";
                } else {
                  imgSrc = imgSrc + "Unknown16.png";
                  info = "Unknown";
                }
                if(info == null) {
                  info = "Service Availability = " + score + "%";
                }
                var elRecordImg = dojo.byId("frmSearchCriteria:mdRecords:"+ this.index +":smallImgContentType");
                var serviceUrl = this.info;

                var elLink = dojo.create("a",
                  {target: "_blank", href:serviceUrl, alt:info, title:info},
                  elRecordImg,
                  'after');
                dojo.style(elLink, 
                  {"border":"0px", "text-decoration" : "none", "border-bottom" : "0px", "margin-right": "0.5em"});
                var elImg = dojo.create("img", {src:imgSrc, alt:info, title:info}, elLink);

                dojo.style(elImg, {"border":"0px"});
              }
            } catch (error) {
              console.log(error.message);
            }
          }),
          error: function(error) {
            console.log(error.message);
          }
        });
      }
    }
  );
}

</script>

</f:verbatim>

<gpt:jscriptVariable 
  id="_rsReviewAlt"
  quoted="true"
  value="#{gptMsg['catalog.search.searchResult.altReview']}"
  variableName="rsReviewAlt"/>
<gpt:jscriptVariable 
  id="_rsReviewCountLabel"
  quoted="true"
  value="#{gptMsg['catalog.search.searchResult.reviewCountLabel']}"
  variableName="rsReviewCountLabel"/> 
<gpt:jscriptVariable 
  id="_rsReview0CoutLabel"
  quoted="true"
  value="#{gptMsg['catalog.search.searchResult.review0CoutLabel']}"
  variableName="rsReview0CoutLabel"/>     
  
  
<gpt:jscriptVariable 
  id="_rsShowReviews"
  quoted="true"
  value="#{SearchController.searchConfig.resultsReviewsShown}"
  variableName="rsShowReviews"/> 
  
<gpt:jscriptVariable id="_jvaoiMinX" quoted="false" 
  value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.minX}" 
  variableName="aoiMinX"/>
<gpt:jscriptVariable id="_jvaoiMinY" quoted="false" 
  value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.minY}" 
  variableName="aoiMinY"/>
<gpt:jscriptVariable id="_jvaoiMaxX" quoted="false" 
  value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.maxX}" 
  variableName="aoiMaxX"/>
<gpt:jscriptVariable id="_jvaoiMaxY" quoted="false" 
  value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.maxY}" 
  variableName="aoiMaxY"/>
<gpt:jscriptVariable id="_jvaoiWkid" quoted="true" 
  value="#{SearchController.searchCriteria.searchFilterSpatial.visibleEnvelope.wkid}" 
  variableName="aoiWkid"/>        
<gpt:jscriptVariable id="_jvaoiOperator" quoted="true" 
  value="#{SearchController.searchCriteria.searchFilterSpatial.selectedBounds}" 
  variableName="aoiOperator"/>
  
  
<gpt:jscriptVariable
  id="_jvResultsMapMaxX" 
  quoted="false" 
  value="#{SearchController.searchResult.enclosingEnvelope.maxX}" 
  variableName="resultsMapMaxX"/>
<gpt:jscriptVariable 
  id="_jvResultsMapMaxY" 
  quoted="false" 
  value="#{SearchController.searchResult.enclosingEnvelope.maxY}" 
  variableName="resultsMapMaxY"/>
<gpt:jscriptVariable 
  id="_jvResultsMapMinX" 
  quoted="false" 
  value="#{SearchController.searchResult.enclosingEnvelope.minX}" 
  variableName="resultsMapMinX"/>
<gpt:jscriptVariable 
  id="_jvResultsMapMinY" 
  quoted="false" 
  value="#{SearchController.searchResult.enclosingEnvelope.minY}" 
  variableName="resultsMapMinY"/>
    
    

<% // map viewer url %>
<gpt:jscriptVariable id="_jvMvsUrl" quoted="true"  
  value="#{SearchController.searchCriteria.searchFilterSpatial.mvsUrl}" 
  variableName="srMvsUrl" />

<gpt:jscriptVariable id="_jvJsMetadata" quoted="false"
   value="#{SearchController.searchResult.recordsAsJSON}" variableName="jsMetadata"/>
   
<% // scripting functions %>


<h:panelGroup id="srResultsPanel" 
  style="#{SearchController.displayResultsStyle}">

<% // page cursor %>
<gpt:pageCursor id="srTopCursor"
   pageCursor="#{SearchController.searchResult.pageCursor}"
   action="#{SearchController.getNavigationOutcome}"
   criteriaPageCursor="#{SearchController.searchCriteria.searchFilterPageCursor}"
   actionListener="#{SearchController.processAction}"
   maxEnumeratedPages="5"
   label="#{gptMsg['catalog.general.pageCursor.results']}"
   labelValues="#{SearchController.searchResult.pageCursor.startRecord}
    |#{SearchController.searchResult.pageCursor.startRecord - 1 
    + SearchController.searchResult.recordSize}
    |#{SearchController.searchResult.pageCursor.totalRecordCount}
    |#{SearchController.searchResult.searchTimeInSeconds}"
   labelPosition="leftSide"
   labelNoResults="#{gptMsg['catalog.search.searchResult.labelNoResults']}" />
   

<% // expand/zoom to results %>
<h:panelGroup>

  <div>
    <span>
      <h:selectBooleanCheckbox id="srToggle"
        rendered="#{not empty PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'] and PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'].value == 'true'}"
        style="#{SearchController.expandResultCheckboxStyle}"
        onclick="void(this);"/>
      <h:outputLabel for="srToggle" id="srToggle_label"
        rendered="#{not empty PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'] and PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'].value == 'true'}"
        value="#{gptMsg['catalog.search.searchResult.lblToggle']}"
        style="vertical-align: top; #{SearchController.expandResultCheckboxStyle}"/>
      <h:selectBooleanCheckbox id="srExpandResults"
        value="#{SearchController.searchCriteria.expandResultContent}"
        style="#{SearchController.expandResultCheckboxStyle}"
        onclick="rsExpandAllRecords(this);"/>
      <h:outputLabel for="srExpandResults" 
        value="#{gptMsg['catalog.search.searchResult.lblExpand']}"
        style="vertical-align: top; #{SearchController.expandResultCheckboxStyle}"/>
    </span>
    <span style="float: right; margin-right: 4px;">
      <h:outputLink id="srLnkZoomToThese2" value="javascript:void(0)" 
        rendered="#{empty PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'] or not PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'].value == 'true'}"
        onclick="javascript:return srZoomToThese();"
        style="#{SearchController.expandResultCheckboxStyle}">
        <h:outputText id="srTxtZoomToThese2" value="#{gptMsg['catalog.search.searchResult.zoomToThese']}" />
      </h:outputLink>
      <h:outputText escape="false" value="&nbsp;&nbsp;&nbsp;" 
         rendered="#{empty PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'] or not PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'].value == 'true'}"
                    />
      <h:outputLink id="srLnkZoomToAOI2" value="javascript:void(0)" 
        rendered="#{empty PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'] or not PageContext.applicationConfiguration.catalogConfiguration.parameters['catalog.cart.enabled'].value == 'true'}"
        onclick="javascript:return srZoomToAOI();"
        style="#{SearchController.expandResultCheckboxStyle}">
        <h:outputText id="srTxtZoomToAOI2" value="#{gptMsg['catalog.search.searchResult.zoomToAOI']}" />
      </h:outputLink>
    </span>
  </div>
</h:panelGroup>
   
<% // results %>
<h:panelGroup styleClass="resultsContainer">

	<h:dataTable id="mdRecords"
		value="#{SearchController.searchResult.records}" var="record"
	  binding="#{dTable.dataTable}" width="100%">
		<h:column id="metadataColumn">
			<h:panelGrid id="_metadataMainRecordTable" columns="1"
				onmouseover="javascript:rsHighlightRecord(this);"
				onmouseout="javascript:rsUnhighlightRecord(this);"
				styleClass="noneSelectedResultRow" width="100%"
				columnClasses="metadataCntTypIcon,metadataInfoSection,metadataExpansionGifSection">
	
				<% // Icon and title %>
				<h:panelGroup>
				  <h:graphicImage id="smallImgContentType" 
				    height="16px" width="16px" 
				    alt="#{record.contentTypeLink.label}"
				    value="#{record.contentTypeLink.url}" 
				    title="#{record.contentTypeLink.label}"
				    onmouseover="javascript:this.style.cursor='pointer';"
				    onmouseout="javascript:this.style.cursor='default'"
				    styleClass="resultsIcon" />
				  <h:outputLink id="recLnkTitle"
				    value="javascript:void(0)"
            onclick="javascript:return rsExpandRecord(#{dTable.dataTable.rowIndex});">
					  <h:outputText styleClass="resultsTitle" id="recTxtTitle" value="#{record.title}" />
					</h:outputLink>
					
				</h:panelGroup>
				
				<% // record content %>
				<h:panelGroup id="recContent" style="#{SearchController.expandResultContentStyle}">
				
					<% // Abstract and thumbnail %>
					<h:panelGroup>
                      <h:outputLink title="" target="_blank" value="#{record.thumbnailLink.url}" styleClass="reviewResult">
                        <h:graphicImage id="_imgRecordThumbnail" 
                          alt="#{gptMsg['catalog.rest.thumbNail']}"
                          rendered="#{not empty record.thumbnailUrl}"
                          value="#{record.thumbnailUrl}" width="64" height="64" styleClass="resultsThumbnail" />
                      </h:outputLink>
                      <h:outputText id="_txtAbstract" styleClass="resultsContent" value="#{record['abstract']}" />
				  </h:panelGroup>
				  
				  <% // resource info %>
                  <f:verbatim>
                    <div class="recordInfo" style="display: none; visibility: hidden;">
                      </f:verbatim>
                        <h:inputText styleClass="recordUuid" value="#{record.uuid}"/>
                        <h:inputText styleClass="recordUrl" value="#{record.resourceUrl}"/>
                        <h:inputText styleClass="recordType" value="#{record.serviceType}"/>
                      <f:verbatim>
                    </div>
                  </f:verbatim>
				    
				  <% // Resource links %>
					<h:panelGrid id="pgdResourceLinks">
					  <h:panelGroup id="pgpResourceLinks">
					    <h:outputText escape="false" value="#{record.resourceLinks}">
					      <f:converter  converterId="gpt.ResourceLinksToHtml" />
					    </h:outputText>
					    <% // Zoom map to this metadata extent %>
              <h:outputLink id="_lnkZoomTo" value="javascript:void(0)" 
                rendered="#{record.defaultGeometry == false && record.objectMap['linkInfo'] != null && record.objectMap['linkInfo'].showZoomTo}"
                styleClass="resultsLink" onclick="javascript:return srZoomTo(#{dTable.dataTable.rowIndex});">
                <h:outputText id="_txtZoomTo" value="#{gptMsg['catalog.search.searchResult.zoomTo']}" />
              </h:outputLink>
          
              <f:verbatim><span style="padding-left: 15px;"></span></f:verbatim>
              <h:outputLink id="lnkReviewIcon" title="" target="_blank" 
                style="visibility: hidden; display: none;" 
                styleClass="reviewResult">
		              <h:graphicImage
		                style="visibility: hidden; display: none;"  
				            id="imgUp" 
				            height="16px" width="16px" 
				            value="/catalog/images/asn-vote-up.png">
				          </h:graphicImage>
		              <h:graphicImage
		                style="visibility: hidden; display: none;" 
				            height="16px" width="16px" 
				            id="imgDown"
				            value="/catalog/images/asn-vote-down.png">
				          </h:graphicImage>
				          <h:graphicImage
                    style="visibility: hidden; display: none;"  
                    id="imgNoReview" 
                    height="16px" width="16px" 
                    value="/catalog/images/asn-vote-up.png">
                  </h:graphicImage>
				      </h:outputLink>
		        </h:panelGroup>
				  </h:panelGrid>	
			   </h:panelGroup>
			</h:panelGrid>
		</h:column>
	</h:dataTable>
	
</h:panelGroup>

<% // rest bindings for this result %>
<h:panelGroup rendered="#{SearchController.searchResult.recordSize > 0 && SearchController.restSearchRequestUrlGeorss != null}">
  <h:outputText value="#{gptMsg['catalog.search.searchResult.restLabel']}"/>
  <h:outputLink id="srRestGEORSS" target="_blank" value="#{SearchController.restSearchRequestUrlGeorss}" styleClass="resultsLinkRestApi">
    <h:outputText value="GEORSS"/>
  </h:outputLink>
   <h:outputLink id="srRestATOM" target="_blank" value="#{SearchController.restSearchRequestUrlAtom}" styleClass="resultsLinkRestApi">
    <h:outputText value="ATOM"/>
  </h:outputLink>
  <h:outputLink id="srRestHTML" target="_blank" value="#{SearchController.restSearchRequestUrlHtml}" styleClass="resultsLinkRestApi">
    <h:outputText value="HTML"/>
  </h:outputLink>
  <h:outputLink id="srRestFRAGMENT" target="_blank" value="#{SearchController.restSearchRequestUrlHtmlFragment}" styleClass="resultsLinkRestApi">
    <h:outputText value="FRAGMENT"/>
  </h:outputLink>
  <h:outputLink id="srRestKML" target="_blank" value="#{SearchController.restSearchRequestUrlKml}" styleClass="resultsLinkRestApi">
    <h:outputText value="KML"/>
  </h:outputLink>
  <h:outputLink id="srRestJSON" target="_blank" value="#{SearchController.restSearchRequestUrlJson}" styleClass="resultsLinkRestApi">
    <h:outputText value="JSON"/>
  </h:outputLink>
  <h:outputLink id="srRestDCAT" target="_blank" value="#{SearchController.restSearchRequestUrlDcat}" styleClass="resultsLinkRestApi">
    <h:outputText value="DCAT"/>
  </h:outputLink>
  <h:outputLink id="srRestCSV" target="_blank" value="javascript:void(0);" onclick="javascript:getCsv('#{SearchController.restSearchRequestUrlJson}'); return false;" styleClass="resultsLinkRestApi">
    <h:outputText value="CSV"/>
  </h:outputLink>
</h:panelGroup>


</h:panelGroup>

