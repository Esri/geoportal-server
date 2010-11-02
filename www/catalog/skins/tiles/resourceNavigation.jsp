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
<%@taglib prefix="f" uri="http://java.sun.com/jsf/core"%>
<%@taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@taglib prefix="tiles" uri="http://struts.apache.org/tags-tiles"  %>

<%@page import="com.esri.gpt.framework.util.Val" %>
<%@page import="java.io.UnsupportedEncodingException" %>
<%@page import="java.net.URLEncoder" %>
<%@page import="com.esri.gpt.framework.util.LogUtil" %>
<%@page import="com.esri.gpt.framework.context.RequestContext" %>
<%@page import="com.esri.gpt.control.search.browse.TocCollection" %>
<%
	String rnpUuid = Val.chkStr(request.getParameter("uuid"));
	String rnpContextPath = request.getContextPath();
	RequestContext context = RequestContext.extract(request);
	TocCollection tocs = context.getCatalogConfiguration().getConfiguredTocs();
	boolean hasRelationshipsPage = false;
	if(tocs != null && tocs.containsKey("browseResource")){
		hasRelationshipsPage = true;
	}
	String rnpQueryString = "";
	String rnpRestUrl = "";
	if(rnpUuid.length() > 0){
		rnpUuid = java.net.URLEncoder.encode(rnpUuid,"UTF-8");
		rnpRestUrl = rnpContextPath+"/rest/document?id=" + rnpUuid + "&f=json";
		rnpQueryString = "uuid="+ rnpUuid;	
		makeUrls(rnpContextPath,rnpQueryString);	
	}
%>
<%!
	String rnpDetailUrl = "";
	String rnpPreviewUrl = "";
	String rnpReviewUrl = "";
	String rnpRelationshipsUrl = "";

	private void makeUrls(String url, String queryString){
		rnpDetailUrl = com.esri.gpt.framework.util.Val.escapeXmlForBrowser(url + "/catalog/search/resource/details.page?" + queryString);
		rnpReviewUrl = com.esri.gpt.framework.util.Val.escapeXmlForBrowser(url + "/catalog/search/resource/review.page?" + queryString);
		rnpRelationshipsUrl = com.esri.gpt.framework.util.Val.escapeXmlForBrowser(url + "/catalog/search/resource/relationships.page?" + queryString);
	}	
%>
<script>
function rnpInit(){
	var winUrl = window.location.href;
	var element = document.getElementById("rnpDetails");
	if(winUrl != null && winUrl.indexOf("catalog/search/resource/review.page") > 0){		
		element = document.getElementById("rnpReview");
	}else if(winUrl != null && winUrl.indexOf("catalog/search/resource/livedata-preview.page") > 0){
		element = document.getElementById("rnpPreview");
	}else if(winUrl != null && winUrl.indexOf("catalog/search/resource/details.page") > 0){
		element = document.getElementById("rnpDetails");
	}else if(winUrl != null && winUrl.indexOf("catalog/search/resource/relationships.page") > 0){
		element = document.getElementById("rnpRelationships");
	}

	if(element != null) {
		element.className = "current";
	}
 	var hasRelationships = "<%=hasRelationshipsPage%>";
	if(hasRelationships == true || hasRelationships == "true"){
		 var elRelationships = dojo.byId("rnpRelationships");
     if(elRelationships != null){       
    	 elRelationships.style.display = "inline";
     }
	}
	
   var u = "<%=rnpRestUrl%>";
   if(u.length > 0){		     
    dojo.xhrGet({
      handleAs: "json",
      preventCache: true,
      url: u,
      load: function(responseObject,ioArgs) {
        var previewUrl = null;
        var title = null;
        if(responseObject != null && responseObject.records != null && responseObject.records.length ==1){
        	title = responseObject.records[0].title;
			var links = responseObject.records[0].links;
			if(links != null){
				for(var i =0; i< links.length; i++){
					if(links[i].type != null && links[i].type.toLowerCase() == "preview"){
						previewUrl = links[i].href; 
					}
				}
			}  	
        } 
        var elPreview = dojo.byId("rnpPreview");
        if(elPreview != null){
            if(previewUrl == null){  
            	elPreview.style.display = "none";          	
            }else{
            	elPreview.style.display = "inline";
            	elPreview.href = previewUrl;            
            }
        }
        var elTitle = dojo.byId("cmPlPcCaption");
        if(elTitle != null){
            if(title == null){
            	elTitle.style.display = "none";
            }else{
            	//elTitle.appendChild(document.createTextNode(title));
            	elTitle.innerHTML = title;        
            }
        }
      }
    });  
  }
}
if (typeof(dojo) != 'undefined') {
	  dojo.addOnLoad(rnpInit);
}
</script>
<f:verbatim>
	<a id="rnpDetails" href="<%=rnpDetailUrl %>"><%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker().retrieveMessage("catalog.search.resource.details.title")%></a>
	<a id="rnpReview" href="<%=rnpReviewUrl %>"><%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker().retrieveMessage("catalog.search.resource.review.title")%></a>
	<a id="rnpRelationships" style="display:none" href="<%=rnpRelationshipsUrl %>"><%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker().retrieveMessage("catalog.search.resource.relationships.title")%></a>
	<a id="rnpPreview" style="display:none" href="<%=rnpPreviewUrl %>"><%=com.esri.gpt.framework.jsf.PageContext.extractMessageBroker().retrieveMessage("catalog.search.liveData.title")%></a>	
</f:verbatim>