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
<% // homeBody.jsp - Home page (JSF body) %>
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<f:verbatim>

<script type="text/javascript">
/**
Submits from when on enter.
@param event The event variable
@param form The form to be submitted.
**/
function hpSubmitForm(event, form) {

  var e = event;
  if (!e) e = window.event;
  var tgt = (e.srcElement) ? e.srcElement : e.target; 
  if ((tgt != null) && tgt.id) {
    if (tgt.id == "frmSearchCriteria:mapInput-locate") return;
  }
  
  if(!GptUtils.exists(event)) {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "fn submitform: could not get event so as to determine if to submit form ");
    return;
  }
  var code;
  
  if(GptUtils.exists(event.which)) {
    code = event.which;
  } else if (GptUtils.exists(event.keyCode)) {
    code = event.keyCode;
  } else {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "fn submitForm: Could not determine key pressed");
    return;
  }
  
  if(code == 13) {
    
    // Getting main search button
    var searchButtonId = "hpFrmSearch:btnDoSearch";
    var searchButton = document.getElementById(searchButtonId);
    if(!GptUtils.exists(searchButton)){
      GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Could not find button id = " + searchButtonId);
    } else if (!GptUtils.exists(searchButton.click)) {
      GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Could not find click action on id = " + searchButtonId);
    } else {
      searchButton.click();
    }
  } else {
    return true;
  }
}
</script>

</f:verbatim>
 


<f:verbatim>
<p>&nbsp;</p>
</f:verbatim>


<h:outputText escape="false" styleClass="prompt" value="#{gptMsg['catalog.main.home.prompt']}"/>

<h:panelGrid columns="4" summary="#{gptMsg['catalog.general.designOnly']}" width="100%" columnClasses="homeTableColLeft,homeTableColRight">

	<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}" width="100%" columnClasses="homeTableLeft" footerClass="homeTableLeftFooter" headerClass="homeTableLeftHeader" cellpadding="0" cellspacing="0">
		<f:facet name="header">
			<h:column>
				<h:graphicImage id="homeTableLeftHeaderImageL" alt="" styleClass="homeTableLeftHeaderImageL" url="/catalog/images/blank.gif" width="15" height="24"></h:graphicImage>
				<h:graphicImage id="homeTableLeftHeaderImageR" alt="" styleClass="homeTableLeftHeaderImageR" url="/catalog/images/blank.gif" width="48" height="24"></h:graphicImage>
				<h:outputText value="#{gptMsg['catalog.main.home.youCanSimply']}"/>
			</h:column>
		</f:facet>
		<h:column>
          <h:outputLabel for="itxFilterKeywordText" value="#{gptMsg['catalog.main.home.topic.findData']}"/>
			<f:verbatim><p>&nbsp;</p></f:verbatim>

			<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}" width="90%" styleClass="homeTableCol">
				<h:panelGrid columns="2" id="_pnlKeyword" cellpadding="0" cellspacing="0">
				
					<h:form id="hpFrmSearch" onkeypress="javascript: hpSubmitForm(event, this);">
					<h:inputText id="itxFilterKeywordText" 
					  onkeypress="if (event.keyCode == 13) return false;"
					  value="#{SearchFilterKeyword.searchText}" maxlength="400" style="width: 240px" />
					
					<h:commandButton id="btnDoSearch"
					  value="#{gptMsg['catalog.search.search.btnSearch']}"
					  action="#{SearchController.getNavigationOutcome}"
					  actionListener="#{SearchController.processAction}"
					  onkeypress="if (event.keyCode == 13) return false;">
					  <f:attribute name="#{SearchController.searchEvent.event}"
					    value="#{SearchController.searchEvent.eventExecuteSearch}" />
					</h:commandButton>
					</h:form>
					
				</h:panelGrid>

				<h:outputText value="#{gptMsg['catalog.main.home.topic.findData.searchData']}"/>
				
				<h:outputText value="#{gptMsg['catalog.main.home.topic.findData.downloadData']}"/>
				
			</h:panelGrid>
		</h:column>
		<f:facet name="footer">
			<h:column>
				<h:graphicImage id="homeTableLeftFooterImageL" alt="" styleClass="homeTableLeftFooterImageL" url="/catalog/images/blank.gif" width="23" height="16"></h:graphicImage>
				<h:graphicImage id="homeTableLeftFooterImageR" alt="" styleClass="homeTableLeftFooterImageR" url="/catalog/images/blank.gif" width="21" height="16"></h:graphicImage>
			</h:column>
		</f:facet>
	</h:panelGrid>
  

	<h:panelGrid columns="2" summary="#{gptMsg['catalog.general.designOnly']}" columnClasses="homeTableRight,homeTableRight" width="100%" footerClass="homeTableRightFooter" headerClass="homeTableRightHeader" cellpadding="0" cellspacing="0">
		<f:facet name="header">
			<h:column>
				<h:graphicImage id="homeTableRightHeaderImageL" alt="" styleClass="homeTableRightHeaderImageL" url="/catalog/images/blank.gif" width="15" height="24"></h:graphicImage>
				<h:graphicImage id="homeTableRightHeaderImageR" alt="" styleClass="homeTableRightHeaderImageR" url="/catalog/images/blank.gif" width="48" height="24"></h:graphicImage>
				<h:outputText value="#{gptMsg['catalog.main.home.youCanDoMore']}"/>
			</h:column>
		</f:facet>
		<h:column>
			<h:outputText value="#{gptMsg['catalog.main.home.topic.beAUser']}"/>
			<f:verbatim><p>&nbsp;</p></f:verbatim>
			<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}" styleClass="homeTableCol" width="80%">
				<h:outputText value="#{gptMsg['catalog.main.home.topic.beAUser.create']}"/>
				<h:outputText value="#{gptMsg['catalog.main.home.topic.beAUser.saveSearch']}"/>
			</h:panelGrid>
		</h:column>
		<h:column>
			<h:outputText value="#{gptMsg['catalog.main.home.topic.shareData']}"/>
			<f:verbatim><p>&nbsp;</p></f:verbatim>
			<h:panelGrid columns="1" summary="#{gptMsg['catalog.general.designOnly']}" styleClass="homeTableCol" width="80%">
				<h:outputText value="#{gptMsg['catalog.main.home.topic.shareData.createMetadata']}"/>
				<h:outputText value="#{gptMsg['catalog.main.home.topic.shareData.uploadData']}"/>
				<h:outputText value="#{gptMsg['catalog.main.home.topic.shareData.publishData']}"/>
			</h:panelGrid>
		</h:column>
		<f:facet name="footer">
			<h:column>
				<h:graphicImage id="homeTableRightFooterImageL" alt="" styleClass="homeTableRightFooterImageL" url="/catalog/images/blank.gif" width="17" height="20"></h:graphicImage>
				<h:graphicImage id="homeTableRightFooterImageR" alt="" styleClass="homeTableRightFooterImageR" url="/catalog/images/blank.gif" width="23" height="20"></h:graphicImage>
			</h:column>
		</f:facet>
	</h:panelGrid>
              
</h:panelGrid>



<f:verbatim>
<br/>

<!-- more content here -->

</f:verbatim>