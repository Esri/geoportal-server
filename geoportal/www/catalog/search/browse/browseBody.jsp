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
<% // browseBody.jsp - view metadata details(JSF body) %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<%	
  com.esri.gpt.framework.jsf.MessageBroker brwMsgBroker = com.esri.gpt.framework.jsf.PageContext.extractMessageBroker();
	String brwContextPath = request.getContextPath();
	String brwKey = request.getParameter("key");
	String brwUuid = com.esri.gpt.framework.util.Val.chkStr(request.getParameter("uuid"));
	if(brwKey == null){
		brwKey = "browseCatalog";
	}
	String brwTocUrl = brwContextPath + "/browse/toc?key=" + java.net.URLEncoder.encode(brwKey,"UTF-8");
	if(brwUuid.length() > 0){
		brwUuid = java.net.URLEncoder.encode(brwUuid,"UTF-8");
		brwTocUrl += "&uuid=" + brwUuid;
	}
	brwTocUrl = com.esri.gpt.framework.util.Val.escapeXmlForBrowser(brwTocUrl);
	String brwPrompt = "";
	 if(brwKey.equals("browseCatalog")){
		 brwPrompt = brwMsgBroker.retrieveMessage("catalog.browse.prompt");
	 } else {
		 brwPrompt = brwMsgBroker.retrieveMessage("catalog.search.resource.relationships.prompt");
	 } 
%>

<script type="text/javascript">      
	var brwMain = null;
	function brwInit() {
	  brwMain = new BrowseMain();        
	  brwMain.contextPath = "<%=brwContextPath %>";
	  brwMain.resources.filter = "<%=brwMsgBroker.retrieveMessage("catalog.browse.filter")%>";  
	  brwMain.resources.clear = "<%=brwMsgBroker.retrieveMessage("catalog.browse.filter.clear")%>"; 
	  brwMain.resources.summaryPattern = "<%=brwMsgBroker.retrieveMessage("catalog.browse.summaryPattern")%>";
	  brwMain.resources.summaryPatternFiltered = "<%=brwMsgBroker.retrieveMessage("catalog.browse.summaryPattern.filtered")%>";
	  brwMain.resources.pageSummaryPattern = "<%=brwMsgBroker.retrieveMessage("catalog.browse.pageSummaryPattern")%>";
    brwMain.resources.first = "<%=brwMsgBroker.retrieveMessage("catalog.browse.page.first")%>";
    brwMain.resources.last = "<%=brwMsgBroker.retrieveMessage("catalog.browse.page.last")%>";	  
    brwMain.resources.noItemsSelected = "<%=brwMsgBroker.retrieveMessage("catalog.browse.noItemSelected")%>";
    brwMain.resources.noResults = "<%=brwMsgBroker.retrieveMessage("catalog.browse.noResults")%>";
    brwMain.init();
  }
  	
	function treeItemClicked(item){
	  if (brwMain != null) {	  
	    brwMain.onTreeItemClicked(item);                  
	  }
	} 

    
  if (typeof(dojo) != 'undefined') {
    dojo.require("dojo.data.ItemFileReadStore");
    dojo.require("dijit.Tree");
    dojo.require("dijit.layout.SplitContainer");
    dojo.require("dijit.layout.ContentPane");    
    dojo.addOnLoad(brwInit);
  }   
</script>
   
<f:verbatim>
  <span id="browse-prompt" class="prompt"><%=brwPrompt %></span> 
  <div dojoType="dojo.data.ItemFileReadStore" url="<%=brwTocUrl %>" jsid="popStore" requestMethod="get" urlPreventCache="true"/>
  <div class="section" id="browse-splitter" dojoType="dijit.layout.SplitContainer"
    orientation="horizontal" sizerWidth="7" activeSizing="true" style="width: 100%; height: 500px;">	               	
    <div id="browse-toc" class="tundra" dojotype="dijit.Tree" store="popStore" labelattr="name" 
      sizeMin="30" sizeShare="30">			        			        	
      <script type="dojo/method" event="onClick" args="item">treeItemClicked(item);</script>			
    </div>
    <div id="browse-results" class="browse-results" dojoType="dijit.layout.ContentPane" sizeMin="30" sizeShare="70">
      <div id="browse-results-header">
        <%=brwMsgBroker.retrieveMessage("catalog.browse.noItemSelected")%>
      </div>
      <div id="browse-results-body"></div>
    </div>
  </div>
</f:verbatim>
