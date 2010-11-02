	// flag used to differentiate between user changes and programmatic changes
	var ignoreHashChange = true;
	// stores the JSON map data loaded via AJAX
	var tocMap = "";
	// stores a request page anchor while the page and toc are loaded
	var pageAnchor = {anchr: "", token: ""};
	// a boolean flag, whether to send error reports home
	var reportErrors = true;
	// contstant values
	var UNDEFINED = "undefined";

	// executes when DOM is ready for use
	$(document).ready(function(){	
		// create tabs for left panel
		if($.ui && $.ui.tabs) {
			$('#leftPanel').tabs();
		}
		// initialize jQuery splitter
		Splitter.init();
		//initialize jQuery lazyTree
		TOC.init();	
		// initialize page
		HelpDoc.init();	

		if (isWebHelp) {
			modSearchForm();
		}

		$(HelpDoc).bind("newDocumentLoaded", function(){
			if(typeof initWS != UNDEFINED) {
				initWS();
			}
			
			if(typeof initNavBar != UNDEFINED) {
				initNavBar();
			}

			//video
			$('#docWrapper .colorbox-iframe').colorbox({iframe:true, scrolling:false, innerWidth:"716", innerHeight:"420", maxWidth:"90%", maxHeight:"90%"});
			
			if (isWebHelp) {
				try{
					var pageTracker = _gat._getTracker("UA-215788-43");
					pageTracker._setDomainName(".arcgis.com");
					pageTracker._trackPageview(getCurPageUrl());
				} catch(err) {}
			}
		});

	});

	function getRsrcSearchUrl () {
		var surl = "http://resources.arcgis.com/content/search-result";
		var hostname = window.location.hostname;
		
		if (hostname == "helpdev.arcgis.com") {
			surl = "http://resourcesdev.arcgis.com/content/search-result";

		} else if (hostname == "helpstg.arcgis.com")  {
			surl = "http://resourcesstg.arcgis.com/content/search-result";

		}
		
		var lang="en";

		if (typeof getLang != UNDEFINED) {
			lang = getLang();
		}

		if (lang != "en") {
			if (lang == "zh") {
				lang = "zh-hans";
			}			
			var i = surl.indexOf (".com/");
			surl = surl.substring (0, i) + ".com/" + lang + "/"+ surl.substring (i+5);						
		}
		
		return surl;
	}
	
	function modSearchForm() {
		try {
			var surl = "";
			if(typeof getSearchSourceURL != UNDEFINED) {
				//get value from config file first
				surl = getSearchSourceURL ();
			} else {
				var href = window.location.href; 
				surl = href.slice (0, href.indexOf ("index."));

			}
		
			var prodName = "";
			if (typeof getSearchSourceProdName != UNDEFINED) {
				//get value from config file first
				prodName = getSearchSourceProdName ();
			} else {
				prodName = $('title').text();
				prodName = prodName.replace (/help/i, "");
			}
					
			var buf = [];
			buf.push ('<input type="hidden" name="sourceURL" value="'+surl +'" />');
			buf.push ('<input type="hidden" name="sourceProductName" value="'+prodName +'" />');

			$("#search").attr ("action", getRsrcSearchUrl());
			$("#search fieldset").append (buf.join (""));
		
		} catch (err) {
			//noop
		}
	}
	

	function getCurPageUrl() {
		try {
			var curID = HashManager.getHash().token;
			var loc = window.location; 
			var url = loc.pathname.replace ("index.html", "");

			if (curID == "" || typeof curID == UNDEFINED) {
				curID = tocMap.startNode;
			}

			url =  url + curID.substr (0,4) + "/" + curID + ".htm";
			return url;

		} catch (err) {
		
			return window.location;
		}
		
	}
	
	
	var Splitter = {
		init : function(){
			$('#contentWrapper').splitter({
				sizeLeft : 300,
				anchorToWindow : true,
				cookie : 'esri-helpDoc-splitter'
			});
		}
	};// end Splitter

	
	var TOC = {
		
		// internal constants
		BRANCH_LOAD_EVENT : "branchLoaded_",
		OPENED_TO_EVENT : "openedTo_",
		OPENED_NEW_EVENT : "openedNew_",
		
		// counter used to prevent possible infinite recursion
		recursionCount : 0,
		
		init : function(){
			lazyTree = $('#tocWrapper').lazyTree({
				ajaxURL : 'ajaxtoc.js',
				rootData : {},
				persistTree : false,
				idProperty : 't',
				parentProperty : 'p',
				titleProperty : 'l',
				slugProperty : 's',
				mapProperty : 'm',
				childrenProperty : 'c',
				dataType : 'json'
			});
			
			
			lazyTree.successHandler = function(data, textStatus, treeNode){
				// get the tree data from the return
				var treeData = data.tree;
				// if this is the initial request, find the root node
				var isRootRequest = (tocMap.length == 0);
				if(isRootRequest){
					//var rootElement = TOC.getRootElement(treeData);
					// store the tree data for later reference
					tocMap = data;
					tocMap.loadedMaps = [];
					// remove the 'loading...' message
					lazyTree.getTreeRoot().find('li.treeLoader').remove();
					// create the new branches
					lazyTree.createBranch(treeData[data.rootElement].c, treeNode);
					lazyTree.getTreeRoot().trigger('treeLoaded', treeNode);
				}else{
					// add properties to tocMap
					TOC.populateTocMap(treeData);
					//lazyTree.createBranch(treeData, treeNode);
					lazyTree.createBranch(treeData[data.rootElement].c, treeNode);
				}
			};
			
			lazyTree.createBranch = function(children, treeNode){
				// determine if there are inline maps
				var inlineMaps = [];
				if(typeof children != UNDEFINED) {
					inlineMaps = $.grep(children, function(elem, index){ return (elem.substr(0,4) == "map_"); });
				}
				if(inlineMaps.length > 0){	
					TOC.loadInlineMaps(inlineMaps, children, treeNode);	
					return;
				}
				// remove any previous error messages
				treeNode.children('ul.' + lazyTree.getTreeOption('errorClass')).remove();
				// create new branch
				var newBranch = $('<ul class="lazyTreeBranch"></ul>');
				$(children).each(function(){
					if(typeof tocMap.tree[this] != UNDEFINED){
						newBranch.append(lazyTree.createTreeNode(tocMap.tree[this]));	
					}
				});
				treeNode.append(newBranch);
				// bind click handler and other events to the new nodes
				lazyTree.bindEvents(treeNode);
				// remove the loading class from the branch that made the request
				treeNode.removeClass(lazyTree.getTreeOption('loadingClass'));
				// open the requesting branch to show all it's new children
				lazyTree.toggleBranch(newBranch);
				// dispatch the 'branchLoad' event
				var nodeID =  treeNode.data('nodeData').t;
				lazyTree.getTreeRoot().trigger(TOC.BRANCH_LOAD_EVENT+nodeID, treeNode);
				//lazyTree.getTreeRoot().trigger('branchLoad', treeNode);
			};
			
			lazyTree.nodeClickHandler = function(evt){
				// grab the list item for the link that was clicked
				var treeNode = $(evt.target).parent('li');
				//$(evt.target).focus();
				// determine if the link was a 'branch' or a 'leaf'
				if(lazyTree.isBranch(treeNode)){
					if(lazyTree.branchHasChildren(treeNode)){
						// if branch has children, then open/close that branch
						lazyTree.toggleBranch(treeNode.children('ul:first'));	
					}else{
						// if branch does not have children, load them now
						if(typeof treeNode.data('nodeData').m != UNDEFINED){	// is map
							lazyTree.loadBranches(treeNode);
						}else{
							var nodeID = treeNode.data('nodeData').t;
							var nodeChildren = tocMap.tree[nodeID].c;
							lazyTree.createBranch(nodeChildren, treeNode);	
						}
					}
				}else{
					// if a leaf has been clicked, call the leafClickHandler
					lazyTree.leafClickHandler(treeNode);	
				}
				// return false to cancel the click event
				return false;
			};
			
			lazyTree.getAjaxURL = function(treeNode){
				if(typeof treeNode.data('nodeData').m != UNDEFINED){
					return treeNode.data('nodeData').m + '/' + lazyTree.getTreeOption('ajaxURL');
				}else{
					return lazyTree.getTreeOption('ajaxURL');
				}
			};
			
			lazyTree.createTreeNode = function(data){
				// check to see if node is a branch by checking whether it has children or a related map
				var nodeType = (typeof data.c != UNDEFINED ||
								typeof data.m != UNDEFINED) 
								? lazyTree.getTreeOption('branchType') 
								: lazyTree.getTreeOption('leafType'); 
				// build list item
				// we are adding rel as well as href because IE has a hard time evaluating the href value
				var newNode = $('<li class="' + nodeType + '" rel="">' + '<a href="' + 
								HelpDoc.getDocPath( data.t ) + '" rel="' +
								data.t + '" title="' + 
								data.l + '">' +
								data.l + '</a></li>');
				// attach data to element
				newNode.data('nodeData', data);
				return newNode;
			};
			
			lazyTree.loadLeaf = function(leaf){
				var leafValue = leaf.data('nodeData').t;
				if(leafValue.match(/\.(pdf|htm|html)/)) {
					window.open(leafValue);
				} else {
					var parts = leafValue.split("#", 2);
					if (parts.length == 1) {
						HelpDoc.getDoc(leafValue);
					} else {
						setPageAnchor (parts[1], parts[0]);
						HelpDoc.getDoc(parts[0]);
					}
				}
			};
			
			lazyTree.navigateByKeystroke = function(evt){
				var keyProcessed = true;
				switch(evt.which){
					// down arrow key
					case 40:
							// focus on next node
							lazyTree.focusNextNode($(this), true);
							break;
					// right arrow key
					case 39:
					case 107: // plus '+' key
					case 187: // plus key (for webkit browsers)
							// get the parent list item
							var treeNode = $(this).parent('li');
							// determine if the link is a 'branch' or a 'leaf'
							if(lazyTree.isBranch(treeNode)){
								if(lazyTree.branchHasChildren(treeNode)){
									if(lazyTree.isBranchOpen(treeNode)){
										// if branch has children and is open, focus on first child node
										lazyTree.focusNextNode($(this), true);		 
									}else{
										// if branch has children but is not open, open the branch
										lazyTree.toggleBranch(treeNode.children('ul:first'));		
									}
								}else{
									// if branch does not have children, load them now by firing a click event
									$(this).trigger('click');
								}
							}else{
								// if this is a leaf then focus on the next node
								lazyTree.focusNextNode($(this), true);
							}
							break;
					// up arrow key
					case 38:
							// focus on the previous node
							lazyTree.focusNextNode($(this), false);
							break;
					// left arrow key
					case 37:
					case 109: // minus '-' key
					case 189: // minus key (for webkit browsers)
							// get the parent list item
							var treeNode = $(this).parent('li');
							// determine if the link is a 'branch' or a 'leaf'
							if(lazyTree.isBranch(treeNode) && lazyTree.isBranchOpen(treeNode)){
								// if it is an open branch, close it
								lazyTree.toggleBranch(treeNode.children('ul:first'));
							}else{
								// otherwise, focus on the previous node
								lazyTree.focusNextNode($(this), false);
							}
							break;
					default:
							// this keystroke was not processed so let it continue to propagate
							keyProcessed = false;
				}
				return !keyProcessed;
			};
			
			
			lazyTree.init();
		},
		
		getPathFromRoot : function(id){
			// create array to hold all parent IDs
			var parentArray = [];
			// create a counter to prevent infinite loops
			var loopCount = 0;
			// while elements have parents keep looping, limit to 100 to prevent infinite loops
			while(id.length && loopCount < 100){
				loopCount++;
				// if item is valid and has a valid parent
				if(tocMap.tree.hasOwnProperty(id) && tocMap.tree[id].hasOwnProperty('p')){
					// get the parent id
					var parentID = tocMap.tree[id].p;
					if(parentID.length == 0){
						// sometimes a node has no parents because it's part of a sub-map
						// if that's the case get the maps' parent in the root map
						var parentMap = TOC.getMapParent(id.substr(0, HelpDoc.mapLength));
						if(parentMap.length > 0) {
							parentID = parentMap;
						} else {
							parentMap = TOC.getInlineMapParent(id.substr(0, HelpDoc.mapLength));
							if (parentMap.length > 0) {
								parentID = parentMap;
							} else {
								parentMap = TOC.fixInlineMapParent(tocMap.tree[id].c[0]);
								if (parentMap.length > 0) {
									parentID = parentMap;
								}
							}
						}
					}
					// add parent to the array, unles the parent is a root node
					if(parentID.length && tocMap.tree[parentID].p){
						parentArray.unshift(parentID);
					}
					// set the parentID as id for the next iteration
					id = parentID;
				}
			}
			return parentArray;
		},
		
		loadInlineMaps : function(inlineMaps, children, treeNode){
			if(inlineMaps.length > 0){
				// get the next element in the inlineMaps array
				var mapValue = inlineMaps[0];
				var mapIndex = $.inArray(mapValue, children);
				if(mapIndex > -1){
					// make request to fetch the map
					$.get(	mapValue.substr(4) + '/' + lazyTree.getTreeOption('ajaxURL'),
							"",
							function(data,textStatus){
								if(textStatus == 'success'){
									var newNodes = "";
									var rootChildren = data.tree[data.rootElement].c;
									$.each(rootChildren, function(index, node){
										newNodes += "'" + node + "',";
									});
									// must eval because params are dynamic
									eval('children.splice(mapIndex, 1,' + newNodes.substr(0,newNodes.length-1) + ')');
									// add new tree nodes to the tocMap
									TOC.populateTocMap(data.tree, mapValue.substr(4));
								}
								// remove processed map from array
								inlineMaps.splice(inlineMaps.length-1, 1);
								// repeat the process
								TOC.loadInlineMaps(inlineMaps, children, treeNode);	
							},
							'json'
					);
				}
			}else{
				// all inline maps have been loaded, so it's safe to create the branch now
				lazyTree.createBranch(children, treeNode);
			}
		},

		
		getMapParent : function(id){
			var parentID = "";
			var currentItem;
			var currentKey;
			// loop through each item in the tocMap to find the element that matches
			for(currentKey in tocMap.tree){
				currentItem = tocMap.tree[currentKey];
				if(currentItem.hasOwnProperty('m') && currentItem.m == id){
					parentID = currentItem.t;
					break;
				}
			}
			return parentID;
		},
		
		fixInlineMapParent : function(id){
			var parentID = "";
			var currentItem;
			var currentKey;
			// loop through each item in the tocMap to find the element that matches
			for(currentKey in tocMap.tree){
				currentItem = tocMap.tree[currentKey];
				
				//OPT
				/*
				if(currentItem.hasOwnProperty('c') && $.inArray(id, currentItem.c) > -1){
					parentID = currentItem.t;
					break;
				}
				*/
				
				
				if(currentItem.hasOwnProperty('c')){
					var cL = currentItem.c;
					var idx = -1;
					for ( var i = 0, length = cL.length; i < length; i++ ) {
						// Use === because on IE, window == document
						if (cL[i] === id)
							idx = i;
							break;
					}
					if (idx > -1) {
						parentID =  currentItem.t;
						break;
					}	
				}
			}
			return parentID;
		},

		getInlineMapParent : function(id){
			var parentID = "";
			var currentItem;
			var currentKey;
			// loop through each item in the tocMap to find the element that matches
			for(currentKey in tocMap.tree){
				currentItem = tocMap.tree[currentKey];
				//OPT
				/*
				if(currentItem.hasOwnProperty('c') && $.inArray('map_'+id, currentItem.c) > -1){
					parentID = currentItem.t;
					break;
				}
				*/
				if(currentItem.hasOwnProperty('c')){
					var cL = currentItem.c;
					var idx = -1;
					var val = 'map_' + id
					for ( var i = 0, length = cL.length; i < length; i++ ) {
						// Use === because on IE, window == document
						if ( cL[i] === val )
							idx = i;
							break;
					}
					if (idx > -1) {
						parentID = currentItem.t;
						break;
					}						
				}				
			}
			return parentID;
		},
				

		openToNode : function(nodeID, endNode){
			// track recursion to prevent infinite loops
			TOC.recursionCount++;
			if(TOC.recursionCount > 50) {
				return false;
			}
			
			// param the endNode argument
			if(typeof endNode == UNDEFINED){
				endNode = nodeID;
				// new request, reset recursion count
				TOC.recursionCount = 0;
			}
			
			// determine if the node already exists in the tree or if it still needs to be loaded
			if(tocMap.tree.hasOwnProperty(nodeID)){
				// determine the path from the root to the node
				var pathArray = TOC.getPathFromRoot(nodeID);
				// flag for asynchronous operations
				var pendingRequest = false;
				// loop through each step in the path to the node
				$(pathArray).each( function(){
					// get current nodes data from tocMap
					var tocItem = tocMap.tree[this];
					// try and get the node from the tree
					var treeNode = TOC.getNodeById(this);
					// has the load been loaded yet?
					if(treeNode.length){
						// has the nodes children been loaded yet?
						if(lazyTree.branchHasChildren(treeNode)){
							// if branch has children but isn't open, open it now
							if(!lazyTree.isBranchOpen(treeNode)) {
								lazyTree.toggleBranch(treeNode.children('ul:first'));
							}
							
							// now that this branch is open, dispatch the event to all listeners
							lazyTree.getTreeRoot().trigger(TOC.OPENED_TO_EVENT + this, treeNode);
						}else{
							// setup a listener so we know when this branch is done
							TOC.setListener(TOC.OPENED_NEW_EVENT+this, TOC.openToNode, [nodeID, endNode]);
							// now open the new branch
							TOC.openNewBranch(treeNode);
							// set pending flag
							pendingRequest = true;
							// stop the loop until loading is complete
							return false;
						}						
					}
				});	
				
				if(!pendingRequest){
					if(nodeID == endNode){
						// we're done, so scroll to end node
						TOC.focusOnNode(nodeID);
					}else{
						TOC.openToNode(endNode, endNode);
					}	
				}else{
					// just waiting for other nodes to open
				}

				return;
			}else{
				// it must belong to an unloaded map, so get the map ID from the node's value
				var mapID = nodeID.substr(0, HelpDoc.mapLength);
				// find the map's owner ID
				var parentID = TOC.getMapParent(mapID);
				if(parentID == "") {
					parentID = TOC.getInlineMapParent(mapID);				
				}
				if(parentID != ""){
					// find the parent node in the tree	
					var parentNode = TOC.getNodeById(parentID);
					// if the parent has been loaded either, then load it first
					if(parentNode.length == 0){
						// setup a listener to know when parent node is done opening
						TOC.setListener(TOC.OPENED_TO_EVENT + parentID, TOC.openToNode, [nodeID, endNode]);
						// now open parent node
						TOC.openToNode(parentID, endNode);
						
						return;
					}else{
						// get all the children (siblings) from the parent node
						var nodeChildren = tocMap.tree[parentID].c;
						// determine if the node parent is an inline map, or a node with a map property
						if(typeof nodeChildren != UNDEFINED){ // inline map
							// of the siblings, find any other maps that will need to be loaded
							var inlineMaps = $.grep(nodeChildren, function(elem, index){ return (elem.substr(0,4) == "map_"); });		
	
							// make sure we found at least one map (if not, we have a data problem)
							if(inlineMaps.length > 0){
								// set the listener to continue opening nodes once the maps have been loaded
								TOC.setListener(TOC.BRANCH_LOAD_EVENT + parentID, TOC.openToNode, [nodeID, endNode]);
								// load all inline maps and sibling branches
								TOC.loadInlineMaps(inlineMaps, nodeChildren, parentNode);	
							}	
						}else{ // node with solo map property
							// load the map
							if(tocMap.loadedMaps.find(mapID) == -1){
								// add the loading to the node's parent
								parentNode.addClass(lazyTree.getTreeOption('loadingClass'));
								// get the new map
								$.get(	mapID + '/' + lazyTree.getTreeOption('ajaxURL'),
										"",
										function(data, textStatus){
											if(textStatus == 'success'){
												// populate the TocMap with the new data
												TOC.populateTocMap(data.tree, mapID);
												// open to the new node once the new branch has been created
												TOC.setListener(TOC.BRANCH_LOAD_EVENT + parentID, TOC.openToNode, [nodeID, endNode]);
												// load new branch
												lazyTree.successHandler(data, textStatus, parentNode);
											}
										},
										'json'
								);// end get AJAX call
							}
						}
						// end process and wait for maps/nodes to load
						return;
					}
				}	
			}
		},

	
		populateTocMap : function(treeData, mapID){
			// add newly loaded nodes to the TocMap
			$.each(treeData, function(node){
				tocMap.tree[node] = treeData[node];
			});
			
			// add mapID to list of loaded maps
			tocMap.loadedMaps.push(mapID);
		},
		
		getNodeById : function(id){
			// this compares rel instead of href because IE6 has a hard time comparing href values
			return lazyTree.getTreeRoot().find('a[rel=' + id + ']').parents('li:first');
		},
		
		openNewBranch : function(treeNode){
			// get the branches ID
			var nodeID = treeNode.data('nodeData').t;
			// get the branch's children from its data
			var children = treeNode.data('nodeData').c;

			// create a listener so when know when the branch is opened
			lazyTree.getTreeRoot().one(TOC.BRANCH_LOAD_EVENT+nodeID, {nodeID: nodeID, treeNode: treeNode}, function(evt){
				lazyTree.getTreeRoot().trigger(TOC.OPENED_NEW_EVENT+evt.data.nodeID, evt.data.treeNode );
			});
			
			// were children found?  If not, it must have an inline map
			if(children){
				// create branch from it's children
				lazyTree.createBranch(children, treeNode);
			}else if(typeof treeNode.data('nodeData').m != UNDEFINED){
				// create branch from map
				lazyTree.loadBranches(treeNode);
			}
		},
		
		focusOnNode : function(nodeID){
			// highlight and scroll to the final node
			var selectedNode = TOC.getNodeById(nodeID);
			lazyTree.setSelectedNode(selectedNode);
			// put in slight delay to let IE catch its breath :-P
			setTimeout(function(){ HelpDoc.scrollToNode(selectedNode); }, 100, selectedNode);
		},
		
		setListener : function(evtType, evtHandler, handlerArguments){
			// create event data to pass to the handler
			eventData = {};
			eventData.handler = evtHandler;
			eventData.arguments = handlerArguments;
			
			// create the handler for whatever event was passed
			lazyTree.getTreeRoot().one(evtType, eventData, function(evt){
				// call the handler function, passing in the handler arguments
				evt.data.handler.apply( this, evt.data.arguments );
			});
		}
		
	};// end TOC
	
	
	/*
		HelpDoc is responsible for manipulting the page:
		- loading new pages via AJAX
		- scrolling to anchors
		- setting page title
	*/
	var HelpDoc = {
		mapLength: 4,
		tokenLength: 18,
		
		// initializes page on load
		init : function(){
			// intercept all link clicks so we don't redirect
			ClickHijacker.attachInterceptions();
			// listen for URL changes
			$.address.change(HashManager.addressChangeHandler);
			// listen for when the TOC gets loaded and load the initial document
			lazyTree.getTreeRoot().bind('treeLoaded', function(evt, rootNode){
				// if a page was request in the URL
				var urlHash = HashManager.getHash();
				var urlToken = urlHash.token;
				// set the page anchor 
				setPageAnchor(urlHash.anchr, urlToken);
				// if specific page was requested, load it... otherwise load default
				if(urlToken.length){
					HelpDoc.getDoc(urlToken);
					TOC.openToNode(urlToken);
				}else if(tocMap.hasOwnProperty('startNode')){
					// otherwise, load the default page
					HelpDoc.getDoc(tocMap.startNode);
					// highlight the default node
					TOC.openToNode(tocMap.startNode);
				}
			});
		},
		
		// make AJAX call to get helpDoc content
		getDoc : function(path){
			// fade out the old doc and load the new
			$("#docWrapper").fadeOut("slow", function(){
				// make AJAX call to get the data
				$.ajax({
					type 	: 	'GET',
					url 	: 	HelpDoc.getDocPath(path),
					dataType :	'html',
					success : 	HelpDoc.getDocHandler,
					error 	: 	HelpDoc.errorHandler
				});									  
			});
		},
		
		// handle AJAX return and dump HTML into content area
		getDocHandler : function(result){
			//grab the title of the page
			var startTitle = result.lastIndexOf ('<title>') + 7;
			var endTitle = result.indexOf ('</title>', startTitle);
			var title = "";
			if (startTitle > -1 && endTitle > -1) {
				title = result.substring (startTitle, endTitle);
			}


			// parse out just the content of the page
			var startIndex = result.indexOf('<div');
			var endIndex = result.lastIndexOf('</div>') + 6;
			if(startIndex > -1){
				result = result.substring(startIndex, endIndex);
			}

			// load HTML content into the page
			$("#docWrapper").html(result).fadeIn("normal");
			// set the new hash value in the URL
			if(lazyTree.getSelectedNode().length && lazyTree.getSelectedNode().data('nodeData')){
				HelpDoc.setMetaData(lazyTree.getSelectedNode().data('nodeData'), title);
			}else{
				var pageID = HashManager.getHash().token;
				if(tocMap.tree.hasOwnProperty(pageID)){
					HelpDoc.setMetaData(tocMap.tree[pageID], title);
				}
			}
			// if there's a page anchor, scroll to it
			if(pageAnchor.anchr.length > 0) {
				HelpDoc.scrollToAnchor(pageAnchor.anchr);
			}
			// reset page anchor
			setPageAnchor("", "");
			$(HelpDoc).trigger("newDocumentLoaded");

		},
		
		errorHandler : function(XMLHttpRequest, textStatus, errorThrown){
			if(XMLHttpRequest.status == 404 || errorThrown.name == 'NS_ERROR_DOM_BAD_URI'){
				$("#docWrapper").html("<h1>"+txtDocNotFound_main+"</h1>").fadeIn("normal");
				if(typeof initNavBar != UNDEFINED) {
					initNavBar();
				}
			}else{
				$("#docWrapper").html("<h1>"+txtUnknownErr_main+"</h1>").fadeIn("normal");
				if(typeof initNavBar != UNDEFINED) {
					initNavBar();
				}
			}
		},
		
		// set the metadata for a node, includes the page hash and page title
		setMetaData : function(nodeData, title){
			var id = nodeData.t;
			var slug = nodeData.l;
			var anchr = (pageAnchor.anchr.length > 0 && pageAnchor.token == id) ? pageAnchor.anchr : "";
			
			var lang="en";

			if (typeof getLang != UNDEFINED) {
				lang = getLang();
			}

			if (lang == "en") {
				slug = slug.replace(/\W/g, "_").replace (/_{2,}/g, "_").replace (/^_|_$/g,"");
				slug = slug.slice (0, 256); 
			} else {
				slug = "na";
			}

			if (slug == "") {
				slug = "na";
			}
			
			if (title == "") {
				title = nodeData.l;
			}

			if (title == "") {
				title = txtNoTitle_main;
			}
			
			// if this is the home page, don't set the hash because this can cause a 'back' loop
			if(HashManager.getHash().token != "" || id != tocMap.startNode){
				HashManager.setHash(id, slug, anchr);	
			}
			HelpDoc.setPageTitle(title);
			
		},
		
		// set the page title for usability
		setPageTitle : function(title){
			$.address.title(title);
		},
		
		// scroll to an anchor point within the doc
		scrollToAnchor : function(anchorName){
			// find anchor element
			var $anchorElem = $("a[name='"+anchorName+"']");
			// if no anchor was found, try uppercase anchor name
			if ($anchorElem.length == 0) {
				var anchorName1 = anchorName.substr (0, 1).toUpperCase() + anchorName.substr (1);
				$anchorElem = $("a[name='"+anchorName1+"']");
			}
			// if no anchor was found, try looking for a matching id
			if($anchorElem.length == 0) {
				$anchorElem = $('#'+anchorName);
			}
			// scroll to anchor using the jQuery animate method
			if($anchorElem.length > 0){
				$('#rightPanel').animate({scrollTop: ($anchorElem.offset().top-$('#rightPanel').offset().top)}, 800);
			}
		},
		
		// scrolls the TOC panel to a newly selected node
		scrollToNode : function(node){
			var tocWindow = $('#leftPanel');
			var nodePos = Math.round(node.offset().top + tocWindow.scrollTop());
			var windowMin = tocWindow.offset().top;
			var windowMax = windowMin + tocWindow.height();
			var midPoint = Math.round(windowMax*0.45);
			var scrollPos = nodePos-midPoint;
			$('#leftPanel').animate({scrollTop: scrollPos}, 800);
		},
		
		getDocPath : function(path, ext){
			if(typeof ext == UNDEFINED) {
				ext = '.htm';
			}
			return path.substr(0, HelpDoc.mapLength) + '/' + path + ext;
		}
	
	};// end HelpDoc
	
	
	/*
		HashManager is responsible for handling hash changes and manipulations:
		- handle address changes
		- setting hash values in the URL
	*/
	var HashManager = {
		
		// handle URL address changes
		addressChangeHandler : function(event){
			// ignore programmatic changes
			if(ignoreHashChange){
				ignoreHashChange = false;
				return;
			}
			// get hash value from URL
			var requestedHash = HashManager.getHash();
			var requestedID = requestedHash.token;
			// if hash is invalid or not provided, load default
			if(!requestedID.length) {
				requestedID = tocMap.startNode;
			}
			// if a anchor is present, store it
			setPageAnchor(requestedHash.anchr, requestedID);
			// reset the selected item
			lazyTree.setSelectedNode("");
			// load the requested help document
			HelpDoc.getDoc(requestedID);
			// open the tree to the requested node
			TOC.openToNode(requestedID);
			
		},
		
		getHash : function(){
			// use a regular expression to parse the hashString
			var expression = /([\/|#]*)(.[^\/]*)\/(.{18})\/?(.[^\/]*)?/;
			var hashString = $.address.value();
			var hashObj = {slug : "", token : "", anchr : ""};
			if(hashString.match(expression)){
				hashObj.slug = 	RegExp.$2;
				hashObj.token = RegExp.$3;
				hashObj.anchr = RegExp.$4;
			}
			return hashObj;
		},
		
		// set the hash value in the URL
		setHash : function(page, slug, anchr){
			// if the token hasn't changed, don't change the hash because that'll cause a redirect loop
			if(HashManager.getHash().token == page) {
				return;
			}
			var hashValue = "/";
			if(slug.length > 0) {
				hashValue += slug + '/';
			}
			hashValue += page + '/';
			if(anchr.length > 0) {
				hashValue += anchr + '/';
			}
			if($.address.value() != hashValue){
				// ignore the "change" event since we are making the change programmatically
				ignoreHashChange = true;
				// set the hash in the URL
				$.address.value(hashValue);
			}
		}
		
		
	};// end HashManager
	
	
	/*
		ClickHijacker is responsible for intercepting page clicks
		- if anchor link, scroll to anchor
		- if helpdoc link, request new helpdoc from HelpDoc obj
		- if link is anything, let it proceed
	*/
	var ClickHijacker = {
		// listen for any <a> tags being clicked
		attachInterceptions : function(){
			// intercept all mouse clicks inside the content area
			$("#docWrapper a:not(.colorbox-iframe)").live("click", ClickHijacker.interceptClick);
			$("#docWrapper area").live("click", ClickHijacker.interceptClick);
		},
		
		// intercept all links
		interceptClick : function(event){
			// if this was a right-click then ignore
			if(event.button && event.button == 2) {
				return;
			}
			else if($(event.target).attr('esrisubtype') == 'extrel') {
				return;
			}
			
			var cancelClick = false;
			// grab href value from the link clicked
			var linkRef = ($.support.hrefNormalized) ? $(event.target).attr('href') : $(event.target).attr('rel');
			// determine if this link is internal or external
			try{
				var urlProperties = ClickHijacker.parseURL(linkRef);
				// check to make sure filename is the right length
				if(urlProperties.filename.length == HelpDoc.tokenLength){
					cancelClick = true;
					// reset the selected item
					lazyTree.setSelectedNode("");
					// open to the node
					TOC.openToNode(urlProperties.filename);
					// set the page anchor, if it exists
					setPageAnchor(urlProperties.hash, urlProperties.filename);
					// load the request document
					HelpDoc.getDoc(urlProperties.filename);
				}else if(urlProperties.hash.length > 0){
					HelpDoc.scrollToAnchor(urlProperties.hash);
					cancelClick = true;
				}
			}catch(error){
				reportError(error);
			}
			return !cancelClick;
		},
		
		parseURL : function(url){
			// create empty urlObj Object
			var urlObj = {	filename : "",
							fileext : "",
							queryString : "",
							hash : ""	};
			// according to the publishing system if it'a a full url
			// then it's an extenal link, so let it go
			if(url.match(/^((((http|https|ftp):\/\/)+)|(www))/)) {
				return true;
			}
			// otherwise, test to see if it's a valid internal link
			var expression = /([^\/]+)\.(html|htm|shtml|cfm)(.[^#]*)?(#.+)?/i;
			// if RegExp matches, populate it the urlObj
			if(url.match(expression)){
				urlObj.filename = 	RegExp.$1;
				urlObj.fileExt = 	RegExp.$2;
				urlObj.queryString = RegExp.$3;
				urlObj.hash = 		RegExp.$4;
				// if no queryString is found the hash value can move from $.4 to $.3
				if(urlObj.hash.length == 0 && 
				   	urlObj.queryString.length > 1 && 
				   	urlObj.queryString.substr(0,1) == '#'){
					urlObj.hash = urlObj.queryString;
					urlObj.queryString = "";
				}
			}else if(url.match(/^#+(.*)/)){ // look for hash values
				urlObj.hash = RegExp.$1;
			}
			return urlObj;
		}
	};// end ClickHijacker

	/*reportError = function(){
		if(reportErrors){
			$.post('http://webhelp.esri.com/Shared/Narm/narm.cfc?method=submitNotification', ""
				   {serverName : 'none',
				   	tier : 'client',
					criticality : 5,
					productId : 68,
					appArea : 'JavaScript',
					title : 'Help Error - JavaScript',
					ip : '',
					info : errorObj,
					infoType : 'html'}
			);
		}
	}*/	
	
	setPageAnchor = function(_anchr, _token){
		// remove hashes or slashes for the start of the anchor
		_anchr = _anchr.replace(/^(#|\/)*/, "");
		// if both values are valid set them, otherwise reset pageAnchor
		if(_anchr.length > 0 && _token.length > 0) {
			pageAnchor = {anchr:  _anchr, token : _token};
		} else {
			pageAnchor = {anchr:  "", token : ""};
		}
	};
	
	$(window).error(function(msg, url, line){
		return true;				 
	});
	
	Array.prototype.find = function(elem){
		for(var i=0; i < this.length; i++){
			if(this[i] == elem){ return i; }
		}
		return -1;
	};
	
	
	
	
	
	
	
	
	
	
	
	
	
	