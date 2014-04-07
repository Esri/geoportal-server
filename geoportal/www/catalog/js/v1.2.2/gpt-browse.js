/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * gpt-browse.js (v1.2.2)
 * Browse support
 */

dojo.declare("BrowseMain", null, {  
  contextPath: "",
  filterText: "",
  nItemsPerPage: 10,
  persist: true,
  resultsBodyId: "browse-results-body",
  resultsHeaderId: "browse-results-header",
  query: "",
  resetFilterOnTreeItemClicked: false,
  treeId: "browse-toc",
  resources: {
    noItemsSelected: "Select one of the items in the tree to view a specific list of resources.",
    noResults: "Search returned no results",
    filter: "Filter",
    clear: "Clear",
    first: "First",
    last: "Last",
    filterLabel : 'Text: ',
    summaryPattern: "{0} results",
    summaryPatternFiltered: "{0} filtered results",
    pageSummaryPattern: "Showing {0}-{1}"
  },
  
  buildHeader: function(nStartIndex,nTotalResults,wasFiltered) {
    var elHdr = dojo.byId(this.resultsHeaderId);
    
    var elFilterControl = document.createElement("div");
    elFilterControl.className = "nav";
    
    var label = this.resources.filterLabel;
    
    var elSearchLabelText = dojo.create("label", {
    	id: this.resultsHeaderId+"-FilterLabel",
    	"for": this.resultsHeaderId+"-filterText",
    	innerHTML: label
    });
    elFilterControl.appendChild(elSearchLabelText);
    
    
    var elFilterText = document.createElement("input");
    elFilterText.id = this.resultsHeaderId+"-filterText";
    elFilterText.type = "text";
    elFilterText.size = 20;
    elFilterText.maxlength = 1024;
    elFilterText.value = this.filterText;
    elFilterControl.appendChild(elFilterText);
    //dojo.connect(elText,"onchange",this,"onFilterTextChange");
    dojo.connect(elFilterText,"onkeypress",this,"onFilterTextKeyPress");
   
    var elFilterBtn = document.createElement("input");
    elFilterBtn.id = this.resultsHeaderId+"-filter";
    elFilterBtn.type = "submit";
    elFilterBtn.value = this.resources.filter;
    elFilterControl.appendChild(elFilterBtn);
    dojo.connect(elFilterBtn,"onclick",this,"onFilterClicked");
    
    var elClearBtn = document.createElement("input");
    elClearBtn.id = this.resultsHeaderId+"-clearFilter";
    elClearBtn.type = "submit";
    elClearBtn.value = this.resources.clear;
    elFilterControl.appendChild(elClearBtn);
    dojo.connect(elClearBtn,"onclick",this,"onClearFilterClicked");
    
    var sSummaryText = this.resources.summaryPattern;
    if (wasFiltered) sSummaryText = this.resources.summaryPatternFiltered;
    sSummaryText = sSummaryText.replace("{0}",nTotalResults);
    var elSummaryText = document.createElement("span");
    elSummaryText.id = this.resultsHeaderId+"-summaryText";
    elSummaryText.className = "result";
    elSummaryText.appendChild(document.createTextNode(sSummaryText));
    elFilterControl.appendChild(elSummaryText);
    
    
    elHdr.innerHTML = "";
    elHdr.appendChild(elFilterControl);
    
    if (nTotalResults == 0) {
      return;
    }
    
    var currentPageNumber = Math.ceil(nStartIndex / this.nItemsPerPage);
    var nOfPages = Math.ceil(nTotalResults / this.nItemsPerPage);
    var iFrom = (currentPageNumber - 2) > 1 ? (currentPageNumber - 2) : 1;
    var iTo = (currentPageNumber + 2) > nOfPages ? nOfPages : (currentPageNumber + 2);
    if (iTo < 6) {
      iTo = nOfPages >= 5 ? 5 : nOfPages;
      iFrom = 1;
    }else if (iTo == nOfPages){
    	iFrom = nOfPages - 4;
    }
    var nEndIndex = nStartIndex + this.nItemsPerPage - 1;
    if (nEndIndex > nTotalResults) nEndIndex = nTotalResults;

    var elPageControl = document.createElement("div");
    elPageControl.id = this.resultsHeaderId+"-pageControl";
    elPageControl.className = "nav";
    
    var sPageSummary = this.resources.pageSummaryPattern;
    sPageSummary = sPageSummary.replace("{0}",nStartIndex);
    sPageSummary = sPageSummary.replace("{1}",nEndIndex);
    var elPageSummary = document.createElement("span");
    elPageSummary.className = "result";
    elPageSummary.appendChild(document.createTextNode(sPageSummary));
    elPageControl.appendChild(elPageSummary);
    
    var elPageNumbers = document.createElement("span");
    elPageControl.appendChild(elPageNumbers);
    if (iFrom > 1) {
      var elPage = document.createElement("a");
      elPage.setAttribute("href","javascript:void(0);");
      elPage.pageNumber = 1;
      elPage.appendChild(document.createTextNode(this.resources.first));
      elPageNumbers.appendChild(elPage);
      dojo.connect(elPage,"onclick",this,"onPageClicked");

    	 var elPage = document.createElement("a");
       elPage.setAttribute("href","javascript:void(0);");
       elPage.pageNumber = iFrom;
       elPage.appendChild(document.createTextNode("<"));
       elPageNumbers.appendChild(elPage);
       dojo.connect(elPage,"onclick",this,"onPageClicked");
    }
    if (iTo > 1) {
      for (var i=iFrom; i<=iTo; i++) {          
      	var elPage = document.createElement("a");
        elPage.setAttribute("href","javascript:void(0);");
        elPage.pageNumber = i;       
        elPage.appendChild(document.createTextNode(""+i));        
        if (i == currentPageNumber) {
          elPage.className = "current";
        }
        elPageNumbers.appendChild(elPage);
        dojo.connect(elPage,"onclick",this,"onPageClicked");
    
      }      
    }

    if (iTo < nOfPages) {  
  		var elPage = document.createElement("a");
      elPage.setAttribute("href","javascript:void(0);");
      elPage.pageNumber = iTo;
      elPage.appendChild(document.createTextNode(">"));
      elPageNumbers.appendChild(elPage);
      dojo.connect(elPage,"onclick",this,"onPageClicked");
      var elPage = document.createElement("a");
      elPage.setAttribute("href","javascript:void(0);");
      elPage.pageNumber = nOfPages;
      elPage.appendChild(document.createTextNode(this.resources.last));
      elPageNumbers.appendChild(elPage);
      dojo.connect(elPage,"onclick",this,"onPageClicked");
    }
    
    elHdr.appendChild(elPageControl);
    
  },
   
  executeSearch: function(e,nStartIndex) {
    var wasFiltered = false;
    if (nStartIndex == null) nStartIndex = 1;
    if ((this.query == undefined) || (this.query == null)) {
      dojo.byId(this.resultsHeaderId).innerHTML = "";
      this.showMessage(this.resources.noItemsSelected,"prompt");
    } else {
      var sQueryString = ""+this.query;
      var sRequestBase = this.contextPath+"/rest/find/document";
      if (sQueryString.length > 0) {
        if (sQueryString.charAt(0) == "?") {
          sQueryString = dojo.trim(sQueryString.substring(1));
        }
        if (sQueryString.charAt(0) == "&") {
          sQueryString = dojo.trim(sQueryString.substring(1));
        }
      }

      if (this.filterText != null) {
        var sFilter = dojo.trim(this.filterText);
        if (sFilter.length > 0) {
          wasFiltered = true;
          if (sQueryString.indexOf("searchText=") != -1) {          	
            var iIndex = sQueryString.indexOf("searchText=");
            var sLeft = sQueryString.substring(0,iIndex);
            var sRight = sQueryString.substring(iIndex+11);
            var sMiddle = "searchText=" + encodeURI(sFilter + " AND ");
            sQueryString = sLeft + sMiddle + sRight;
          } else {
            if (sQueryString.length > 0) sQueryString += "&";
            sQueryString += "searchText=" + encodeURI(sFilter);
          }
        }
      }   
      if (sQueryString.length > 0) {
        sRequestBase += "?"+sQueryString;
      }
    	if (sQueryString.toLowerCase().indexOf("http://") > -1 || sQueryString.toLowerCase().indexOf("https://") > -1) {
  			sRequestBase = sQueryString;
  		} 
    	var sUrl = sRequestBase;
      if (sUrl.indexOf("?") == -1) sUrl += "?";
      else sUrl += "&";
      sUrl += "f=htmlfragment&start="+nStartIndex+"&max="+this.nItemsPerPage+"&showRelativeUrl=true";
      	
      dojo.xhrGet({
        url: sUrl,
        handleAs: "text",
        load: dojo.hitch(this,function(response, ioArgs) {
        	
          var nTotalResults = this.parseHiddenInt(response,"<input type=\"hidden\" id=\"totalResults\" value=\"", 0);
          
          if (nTotalResults > 0) {      
          	nStartIndex = this.parseHiddenInt(response,"<input type=\"hidden\" id=\"startIndex\" value=\"", nStartIndex);
  	        this.buildHeader(nStartIndex,nTotalResults,wasFiltered);
  	        dojo.byId(this.resultsBodyId).innerHTML = response;
            dojo.query("div.title",this.resultsBodyId).forEach(dojo.hitch(this, function(item) {
              dojo.connect(item,"onclick",this,"onTitleClicked"); 
            }));
            
            if ((typeof(itemCart) != "undefined") && (itemCart != null)) {
              itemCart.initializeKeys({
                error: dojo.hitch(this,function(responseObject,ioArgs) {
                  console.log(responseObject);
                }), 
                load: dojo.hitch(this,function(oCart,ioArgs) {
                  dojo.query("div.title",this.resultsBodyId).forEach(dojo.hitch(this,function(item) {
                    try {
                      var oChildNodes = item.childNodes;
                      for (var iChild=0;iChild<oChildNodes.length;iChild++) {
                        var oChild = oChildNodes[iChild];
                        if (oChild.nodeName.toLowerCase() == "img") {
                          var elCartCtl = itemCart.makeCheckControl(oChild.parentNode,oCart.keys,item.id);
                          if (elCartCtl != null) {
                            oChild.parentNode.insertBefore(elCartCtl,oChild);
                          }
                          break;
                        }
                      }
                    } catch (err) {
                      console.log(err);
                    }   
                  }));
                })
              });
            }
  	        
        	} else {
  	        this.buildHeader(nStartIndex,nTotalResults,wasFiltered);
  	        dojo.byId(this.resultsBodyId).innerHTML = "";
        	}
          return response;
        }),
    
        error: dojo.hitch(this,function(response, ioArgs) {
          this.showMessage(sRequest+" "+response,"errorMessage");
          return response;
        })
      });
      
    }
  },
  
  init: function() {
    var tree = dijit.byId(this.treeId);
    if (tree != null) dojo.connect(tree,"onLoad",this,"onDijitTreeLoaded");
  },
  
  onDijitTreeLoaded: function() {
    if (this.persist) {
      var tree = dijit.byId(this.treeId);
      if (tree != null) {
        var lastClickedId = dojo.cookie(tree.cookieName+"_lastClickedId");
        if (lastClickedId != null) {
          var aryNodes = tree.getNodesByItem(lastClickedId);
          if ((aryNodes != null) && (aryNodes.length > 0)) {
            var treeNode = aryNodes[0];
            if ((treeNode != null) && (treeNode.item != null)) {
              tree.focusNode(treeNode);
              this.onTreeItemClicked(treeNode.item);
            }
          }
        }
      }
    }
  },
  
  onClearFilterClicked: function() {
    var elText = dojo.byId(this.resultsHeaderId+"-filterText");
    if (elText != null) {
      elText.value = "";
    }
    this.filterText = "";
    this.executeSearch();
  },
  
  onFilterClicked: function(e) {
    var elText = dojo.byId(this.resultsHeaderId+"-filterText");
    if (elText != null) {
      this.filterText = dojo.trim(elText.value);
      this.executeSearch(e,1);
    }
  },
  
  onFilterTextChange: function(e) {
    var el = this;
    if ((el != null) && (el.value != null)) {
      this.filterText = dojo.trim(dojo.byId(el.value));
    }
  },
  
  onFilterTextKeyPress: function(e) {
    if (!e) e = window.event;
    var target = (window.event) ? e.srcElement : e.target; 
    if (e) {
      var nKey = (e.keyCode) ? e.keyCode : e.which;
      if ((target != null) && (nKey == 13)) {
        this.filterText = dojo.trim(target.value);
        this.executeSearch();
      }
    } 
  },
  
  onPageClicked: function(e) {
    if (!e) e = window.event;
    var el = (window.event) ? e.srcElement : e.target;
    if ((el != null) && (el.pageNumber != null)) {
      var nStartIndex = ((el.pageNumber - 1) * this.nItemsPerPage) + 1;
      this.executeSearch(e,nStartIndex);
    }
  },
    
  onTitleClicked: function(e) {
    if (!e) e = window.event;
    var target = (window.event) ? e.srcElement : e.target; 
    if (target != null) {
      var qScope = target.parentNode;
      if (target.tagName.toLowerCase() == "img") qScope = qScope.parentNode;
      dojo.query("div.abstract",qScope).forEach(function(item) {
        if ((typeof(item.style.display) == 'undefined') ||
            (item.style.display == null) || (item.style.display == "")) {
          item.style.display = "none";
        } else if (item.style.display == "block") {
          item.style.display = "none";
        } else {
          item.style.display = "block";
        }
      });
      dojo.query("div.links",qScope).forEach(function(item) {
        if ((typeof(item.style.display) == 'undefined') ||
            (item.style.display == null) || (item.style.display == "")) {
          item.style.display = "none";
        } else if (item.style.display == "block") {
          item.style.display = "none";
        } else {
          item.style.display = "block";
        }
      });
    }
  },
  
  onTreeItemClicked: function(item) {
    this.query = item.query;
    if (this.resetFilterOnTreeItemClicked) {
      var elText = dojo.byId(this.resultsHeaderId+"-filterText");
      if (elText != null) {
        elText.value = "";
      }
      this.filterText = "";
    }
    if (this.persist) {
      var tree = dijit.byId(this.treeId);
      var lastClickedId = tree.model.getIdentity(item);
      dojo.cookie(tree.cookieName+"_lastClickedId",lastClickedId,{expires:365});
    }
    this.executeSearch();
  },
  
  parseHiddenInt: function(sResponse,sPrefix,nDefault) {
    var sTemp = sPrefix;
    var iIndex = sResponse.indexOf(sTemp);
    if(iIndex != -1){
    	sTemp = sResponse.substring(iIndex+sTemp.length);
    	iIndex = sTemp.indexOf("\"");
    	if(iIndex != -1){
    		var nTemp = parseInt(sTemp.substring(0, iIndex));
    		if(!isNaN(nTemp)){
    			return nTemp;
    		}
    	}
    }
    return nDefault;
  },
    
  showMessage: function(msg,className) {    
    var elMsg = document.createElement("div");
    elMsg.id = this.resultsHeaderId+"-messageText";
    if (className != null) {
      elMsg.className = className;
    }
    elMsg.appendChild(document.createTextNode(msg));
    dojo.byId(this.resultsBodyId).innerHTML = "";
    dojo.byId(this.resultsBodyId).appendChild(elMsg);
  }
  
});
	  