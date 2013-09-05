var custom = {
	config: {
		catalogs: [
			// TODO: create as many entries as needed
			// {rest: "http://<host name>/geoportal/rest/find/document", caption: "<caption>"}
		]
	},
	
	resources: {
		title: "Search additional catalogues",
		wait: "One moment please...",
		searching: "Searching '@{name}' for '@{keywords}'.",
		showing: "Showing @{total} results for '@{keywords}' from @{name}.",
		invalid: "Illegal query '@{keywords}', catalogue cannot process request.",
		
		sort: {
			relevance: "Relevance",
			title: "Title",
			contentType: "Content Type",
			date: "Date",
			area: "Area"
		}
	},
	
	flipDateSort: false,
	flipAreaSort: false,

	setup: function() {
		console.log("Setting up additional federated searches.");
		custom._createPlaceholder(dojo.byId("relatedSearchesSection"));
		// TODO: localize resources (optional)
	},
	
	_createPlaceholder: function(root) {
		if (!root) {
			console.error("Error setting up federated searches: to root element.");
		}
		var searchText = custom._getSearchText();
		var sKeywords = results._cleanQuery(searchText);
		dojo.create("span",{"class": "section", innerHTML: custom.resources.title}, root);
		var federatedSearchContainer = dojo.create("div",{"id": "federatedSearchContainer", "style": "margin-left: 0.5em;overflow:auto;"}, root);
		var federatedSearchList = dojo.create("span", {"class": "esriItemLinks"}, federatedSearchContainer);
		dojo.forEach(custom.config.catalogs, function(catalog,index){
			var rest = catalog.rest;
			var caption = catalog.caption;
			var listItem = dojo.create("li", {"class": "federatedSearchItem"}, federatedSearchList);
			var listLink = dojo.create("a", {"class": "item", innerHTML: caption}, listItem);
			dojo.connect(listLink, "onclick", dojo.hitch({rest: rest, caption: caption, keywords: sKeywords},function(){
				if (searchText.indexOf(":")>=0) {
					custom._dontDoFederatedSearch(searchText);
				} else {
					custom._doFederatedSearch(this.rest,this.caption,"",encodeURIComponent(this.keywords),"",1,10);
				}
			}));
		});
		dojo.create("div",{"id": "federatedSearchResultContainer", "style": "height:200px;overflow:auto;"}, root);
	},
	
	_getSearchText: function() {
		return dojo.query("#search-box").attr("value").join(" ");
	},
	
	_dontDoFederatedSearch: function(searchText) {
		var targetNode = dojo.byId("esri_arcgisonline_sharing_dijit_SearchResultsSimple_0");
		var targetHeaderNode = dojo.byId("resultsLabel");
		var targetSortersNode = dojo.byId("esri_arcgisonline_sharing_dijit_Sorter_0");
		
		// destroy unwanted original paginator
		dojo.query("#esri_arcgisonline_sharing_dijit_SearchResultsPaging_0 span").forEach(function(n){
			dojo.destroy(n);
		});
		// destroy federated paginator; it will be recreated if needed
		dojo.query("#paginator").forEach(function(n){
			dojo.destroy(n);
		});
		
		// display message
		targetHeaderNode.innerHTML =  custom.resources.invalid.replace(/@{keywords}/gi,searchText);
		targetNode.innerHTML = "";            	
	    targetSortersNode.innerHTML = "";
	},
	
	_doFederatedSearch: function(REST, sName, sRid, sKeywords, sSortOrder, sStart, sMax) {
		console.log("_doFederatedSearch",this._doFederatedSearch.arguments);
        sKeywords = decodeURIComponent(sKeywords);
	    //Look up the node we'll stick the search results under.
	    var targetNode = dojo.byId("esri_arcgisonline_sharing_dijit_SearchResultsSimple_0");
		var targetHeaderNode = dojo.byId("resultsLabel");
		var targetSortersNode = dojo.byId("esri_arcgisonline_sharing_dijit_Sorter_0");
		
		// destroy unwanted original paginator
		dojo.query("#esri_arcgisonline_sharing_dijit_SearchResultsPaging_0 span").forEach(function(n){
			dojo.destroy(n);
		});
		// destroy federated paginator; it will be recreated if needed
		dojo.query("#paginator").forEach(function(n){
			dojo.destroy(n);
		});
		
		var sUrl = REST + "?rid=" + sRid + "&start=" + sStart + "&max=" + sMax + "&searchText=" + encodeURIComponent(sKeywords) + "&orderBy=" + sSortOrder + "&f=xjson";
		
	    targetSortersNode.innerHTML = "";
		
		targetHeaderNode.innerHTML =  custom.resources.searching.replace(/@{name}/gi,sName).replace(/@{keywords}/gi,sKeywords);
	    targetNode.innerHTML = custom.resources.wait;            	
	    
		var funLoad = function(data) {
			var nNumberOfResults = data.totalResults;
			var sNumberOfResultsText = ""+nNumberOfResults;
			targetHeaderNode.innerHTML = custom.resources.showing.replace(/@{name}/gi,sName).replace(/@{keywords}/gi,sKeywords).replace(/@{total}/gi,sNumberOfResultsText); 
			dojo.empty(targetNode);
			
			dojo.query(".mainResults .esriSorter").style("display","none");
			if (data.features && data.features.length>1) {
				custom._renderSorter(REST, sName, sRid, sKeywords, sSortOrder, sStart, sMax, nNumberOfResults, targetNode);
			}
			
			dojo.forEach(data.features,function(feature){
				var attrs = feature.properties;
				var links = feature.links;
				var resources = feature.resources;
				var divSnippet = dojo.create("div",{"class": "snippet"},targetNode);
				var divTitle = dojo.create("div",{"class": "title", "id":attrs.id},divSnippet);
				dojo.forEach(resources,function(resource){
					if (resource.type=="icon") {
						dojo.create("img",{"title":resource.label,"alt":resource.label,"src":resource.href},divTitle);
					}
				});
				dojo.create("span",{"innerHTML": attrs.title},divTitle);
				
				var divAbstract = dojo.create("div",{"class":"abstract"},divSnippet);
				
				var divTable = dojo.create("table",{"width": "100%"},divAbstract);
				var divRow = dojo.create("tr",null,divTable);
				
				var divDescriptionData = dojo.create("td",null,divRow);
				var divDescription = dojo.create("div",{"innerHTML": attrs.summary, "class": "description"},divDescriptionData);
				
				dojo.forEach(resources,function(resource){
					if (resource.type=="thumbnail") {
						var divThumbnailData = dojo.create("td",{"width": "140"},divRow);
						var divThumbnail = dojo.create("div",{"class": "thumbnail"},divThumbnailData);
						dojo.create("img",{"title":resource.label,"alt":resource.label,"src":resource.href},divThumbnail);
					}
				});
				
				var divLinks = dojo.create("div",{"class":"links"},divSnippet);
				dojo.forEach(links,function(link){
					var divA = dojo.create("a",{"target":"_blank", "href": link.href, "innerHTML": link.label, "style": "padding-right: 2px;"},divLinks);
				});
			});
			
			
			if (nNumberOfResults>sMax) {
				// find root and create pagination
				var paging = dojo.byId("esri_arcgisonline_sharing_dijit_SearchResultsPaging_0");
				if (paging!=null) {
					custom._renderPaginator(REST, sName, sRid, sKeywords, sSortOrder, sStart, sMax, nNumberOfResults, paging, "");
				}
			}
		};
		
		var funError = function(error) {
			targetHeaderNode.innerHTML = "There was a problem searching '" + sName + "'";
			targetNode.innerHTML = "An unexpected error occurred: " + error;
		};
		
		var layersRequest = esri.request({
		  url: sUrl,
		  content: { f: "xson" },
		  handleAs: "json",
		  callbackParamName: "callback"
		});
		layersRequest.then(funLoad,funError);
	},
	
	_renderSorter: function(REST, sName, sRid, sKeywords, sSortOrder, sStart, sMax, nNumberOfResults, root) {
		var divSorter = dojo.create("div", {"class": "esriSorter"}, root);
		var divLinks = dojo.create("span", {"class": "esriItemLinks"}, divSorter);
		
		var linkRelevance = dojo.create("a",{"innerHTML": custom.resources.sort.relevance, "href": "#", "class": "itemField " + (sSortOrder==""? " selected": "")}, divLinks);
		dojo.connect(linkRelevance,"onclick",null,dojo.hitch({rest: REST, caption: sName, keywords: sKeywords},function(evt){
			custom.flipDateSort = false;
			custom.flipAreaSort = false;
			custom._doFederatedSearch(this.rest,this.caption,"",encodeURIComponent(this.keywords),"",1,10);
		}));
		
		var linkTitle = dojo.create("a",{"innerHTML": custom.resources.sort.title, "href": "#", "class": "itemField " + (sSortOrder=="title"? " selected": "")}, divLinks);
		dojo.connect(linkTitle,"onclick",null,dojo.hitch({rest: REST, caption: sName, keywords: sKeywords},function(evt){
			custom.flipDateSort = false;
			custom.flipAreaSort = false;
			custom._doFederatedSearch(this.rest,this.caption,"",encodeURIComponent(this.keywords),"title",1,10);
		}));
		
		var linkContentType = dojo.create("a",{"innerHTML": custom.resources.sort.contentType, "href": "#", "class": "itemField " + (sSortOrder=="format"? " selected": "")}, divLinks);
		dojo.connect(linkContentType,"onclick",null,dojo.hitch({rest: REST, caption: sName, keywords: sKeywords},function(evt){
			custom.flipDateSort = false;
			custom.flipAreaSort = false;
			custom._doFederatedSearch(this.rest,this.caption,"",encodeURIComponent(this.keywords),"format",1,10);
		}));
		
		var linkDate = dojo.create("a",{"innerHTML": custom.resources.sort.date, "href": "#", "class": "itemField " + (sSortOrder=="dateDescending" || sSortOrder=="dateAscending"? " selected": "")}, divLinks);
		if (sSortOrder=="dateAscending") {
			dojo.create("span",{"class":"dijitInline esriArrows arrowUp"},linkDate);
		}else if (sSortOrder=="dateDescending") {
			dojo.create("span",{"class":"dijitInline esriArrows arrowDown"},linkDate);
		}
		dojo.connect(linkDate,"onclick",null,dojo.hitch({rest: REST, caption: sName, keywords: sKeywords, sort: sSortOrder},function(evt){
			var sortOrder = !custom.flipDateSort? "dateDescending": (this.sort=="dateDescending"? "dateAscending": "dateDescending");
			custom.flipDateSort = true;
			custom.flipAreaSort = false;
			custom._doFederatedSearch(this.rest,this.caption,"",encodeURIComponent(this.keywords),sortOrder,1,10);
		}));
		
		var linkArea = dojo.create("a",{"innerHTML": custom.resources.sort.area, "href": "#", "class": "itemField " + (sSortOrder=="areaDescending" || sSortOrder=="areaAscending"? " selected": "")}, divLinks);
		if (sSortOrder=="areaAscending") {
			dojo.create("span",{"class":"dijitInline esriArrows arrowUp"},linkArea);
		}else if (sSortOrder=="areaDescending") {
			dojo.create("span",{"class":"dijitInline esriArrows arrowDown"},linkArea);
		}
		dojo.connect(linkArea,"onclick",null,dojo.hitch({rest: REST, caption: sName, keywords: sKeywords, sort: sSortOrder},function(evt){
			var sortOrder = !custom.flipAreaSort? "areaDescending": (this.sort=="areaDescending"? "areaAscending": "areaDescending");
			custom.flipDateSort = false;
			custom.flipAreaSort = true;
			custom._doFederatedSearch(this.rest,this.caption,"",encodeURIComponent(this.keywords),sortOrder,1,10);
		}));
	},
	
	_renderPaginator: function(REST, sName, sRid, sKeywords, sSortOrder, sStart, sMax, nNumberOfResults, root, position ) {
		var pagination = dojo.create("div", {'id':'paginator', 'style':'clear: both; width: 100%; text-align: center; '}, root, position);
		
		// create 'previous' button
		var buttonPrev         = dojo.create("span", {'class':'dijit dijitReset dijitInline dijitButton'}, pagination);
		var buttonPrevNode     = dojo.create("span", {'class':'dijitReset dijitInline dijitButtonNode'}, buttonPrev);
		var buttonPrevContents = dojo.create("span", {'class':'dijitReset dijitStretch dijitButtonContents'}, buttonPrevNode);
		var buttonPrevText     = dojo.create("span", {'class':'dijitReset dijitInline dijitButtonText','innerHTML':'Previous'}, buttonPrevContents);
		if (sStart>1) {
			buttonPrevNode.style.cursor='pointer';
			dojo.connect(buttonPrevNode,"onclick",null,function(){
				custom._doFederatedSearch(REST, sName, sRid, sKeywords, sSortOrder, sStart-sMax, sMax);
			});
		} else {
			dojo.addClass(buttonPrev,"dijitButtonDisabled dijitDisabled");
		}

		// create links
		var links = dojo.create("span", {'class':'esriItemLinks esriPagingLinks paginatorLinks'}, pagination);
		
		var totalPages = Math.ceil(nNumberOfResults/sMax);
		var currentPage = Math.ceil(sStart/sMax);
		
		var page = 1;
		var counter = 1;
		for (page=1; (page-1)*sMax<nNumberOfResults; page++) {
			// generate leading '...' if needed
			if (counter==2 && totalPages>10) {
				if (currentPage-3>2) {
					page = Math.max(3,currentPage-3+Math.min(0,totalPages-currentPage-4));
					dojo.create("span",{'innerHTML':'...','class':'dim'},links);
				}
			}
			// generate page link
			var cl           = currentPage==page? "currentPage": "";
			var link         = dojo.create("a", null, links);
			var sp           = dojo.create("span",{'innerHTML':page,'class':cl},link);
			link.style.cursor='pointer';
			dojo.connect(link,"onclick",page,function(){
				custom._doFederatedSearch(REST, sName, sRid, sKeywords, sSortOrder, (this-1)*sMax+1, sMax);
			});
			// generate trailing '...' if needed
			if (counter==8 && totalPages>10) {
				if (currentPage+3<totalPages-1) {
					page = totalPages-1;
					dojo.create("span",{'innerHTML':'...','class':'dim'},links);
				}
			}
			counter++;
		}
		
		// create 'next' button
		var buttonNext         = dojo.create("span", {'class':'dijit dijitReset dijitInline dijitButton'}, pagination);
		var buttonNextNode     = dojo.create("span", {'class':'dijitReset dijitInline dijitButtonNode'}, buttonNext);
		var buttonNextContents = dojo.create("span", {'class':'dijitReset dijitStretch dijitButtonContents'}, buttonNextNode);
		var buttonNextText     = dojo.create("span", {'class':'dijitReset dijitInline dijitButtonText','innerHTML':'Next'}, buttonNextContents);
		if (sStart+sMax<nNumberOfResults) {
			buttonNextNode.style.cursor='pointer';
			dojo.connect(buttonNextNode,"onclick",null,function(){
				custom._doFederatedSearch(REST, sName, sRid, sKeywords, sSortOrder, sStart+sMax, sMax);
			});
		} else {
			dojo.addClass(buttonNext,"dijitButtonDisabled dijitDisabled");
		}
	}
}

dojo.addOnLoad(custom.setup);
