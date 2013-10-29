var custom = {
	config: {
		title: "Search additional catalogs",
		catalogs: [
			// {rest: "http://<host name>/geoportal/rest/find/document", caption: "<caption>"}
		]
	},

	setup: function() {
		console.log("Setting up additional federated searches.");
		custom._createPlaceholder(dojo.byId("relatedSearchesSection"));
	},
	
	_createPlaceholder: function(root) {
		if (!root) {
			console.error("Error setting up federated searches: to root element.");
		}
		var searchText = custom._getSearchText();
		var sKeywords = results._cleanQuery(searchText);
		dojo.create("span",{"class": "section", innerHTML: custom.config.title}, root);
		var federatedSearchContainer = dojo.create("div",{"id": "federatedSearchContainer", "style": "margin-left: 0.5em;overflow:auto;"}, root);
		var federatedSearchList = dojo.create("span", {"class": "esriItemLinks"}, federatedSearchContainer);
		dojo.forEach(custom.config.catalogs, function(catalog,index){
			var rest = catalog.rest;
			var caption = catalog.caption;
			var listItem = dojo.create("li", {"class": "federatedSearchItem"}, federatedSearchList);
			var listLink = dojo.create("a", {"class": "item", innerHTML: caption}, listItem);
			dojo.connect(listLink, "onclick", dojo.hitch({rest: rest, caption: caption, keywords: sKeywords},function(){
				custom._doFederatedSearch(this.rest,this.caption,"",encodeURIComponent(this.keywords),"",1,10);
			}));
		});
		dojo.create("div",{"id": "federatedSearchResultContainer", "style": "height:200px;overflow:auto;"}, root);
	},
	
	_getSearchText: function() {
		return dojo.query("#search-box").attr("value").join(" ");
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
		
		var sUrl = "../sharing/proxy?"+REST + "?rid=" + sRid + "&start=" + sStart + "&max=" + sMax + "&searchText=" + encodeURIComponent(sKeywords) + "&orderBy=" + sSortOrder + "&f=html";
		
		targetHeaderNode.innerHTML =  "Searching '" + sName + "' for '" + sKeywords + "'"; 
	    targetNode.innerHTML = "One moment please...";            	
	    targetSortersNode.innerHTML = "";
	    
	    //The parameters to pass to xhrGet, the url, how to handle it, and the callbacks.
	    var xhrArgs = {
	        url: sUrl,
	        handleAs: "text",
	        load: function(data) {
	        	var sNumberOfResults = "NaN";
	        	var sNumberOfResultsText = "";
	        	var nNumberOfResults = -1;
	        	
	        	var sLookUp = "<meta name=\"totalResults\" content=\"";
	        	var i = data.indexOf(sLookUp) + 35;
	        	var sSnippet = data.substring(i,i+20);
	        	sNumberOfResults = sSnippet.substring(0,sSnippet.indexOf("\""));
	        	
	        	if (sNumberOfResults != "NaN") {
	        	  	sNumberOfResultsText = sNumberOfResults;
	        	  	nNumberOfResults = Number(sNumberOfResultsText);
	        	}
				targetHeaderNode.innerHTML = "Showing " +sNumberOfResultsText + " results for '" + sKeywords + "' from " + sName + ".";
	    	    targetNode.innerHTML = data;
				
				
				if (nNumberOfResults>sMax) {
					// find root and create pagination
					var paging = dojo.byId("esri_arcgisonline_sharing_dijit_SearchResultsPaging_0");
					if (paging!=null) {
						custom._renderPaginator(REST, sName, sRid, sKeywords, sSortOrder, sStart, sMax, nNumberOfResults, paging, "");
					}
					var sorters = dojo.byId("sorters");
					if (sorters!=null) {
						custom._renderPaginator(REST, sName, sRid, sKeywords, sSortOrder, sStart, sMax, nNumberOfResults, sorters, "after");
					}
				}
	        },
	        error: function(error) {
				targetHeaderNode.innerHTML = "There was a problem searching '" + sName + "'";
	            targetNode.innerHTML = "An unexpected error occurred: " + error;
	        }
	    }
	
	    //Call the asynchronous xhrGet
	    var deferred = dojo.xhrGet(xhrArgs);
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
