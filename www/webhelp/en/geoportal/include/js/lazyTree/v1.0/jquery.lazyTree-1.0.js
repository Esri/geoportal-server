
// create closure
if(typeof jQuery != 'undefined') (function($){
	
	// plugin definition
	$.fn.lazyTree = function(_options){
		var lazyTree = $.fn.lazyTree;
		// store a reference to the root object
		var treeRoot = $(this);
		// extend jQuery options for this object
		var treeOptions = $.extend({}, lazyTree.defaultOptions, _options);
		// stores an array of nodes that needs to be opened (if using persistence)
		var nodesToOpen = [];
		// stores a reference to the currently selected (leaf) node
		var selectedNode = treeRoot;
		
		// declare public methods - all these methods may be overridden
		var publicMethods = {
			
			/*
				init : creates the root elements of the tree and starts all necessary operations
			*/
			init : function(){
				// create the root element for the tree
				lazyTree.createRoot();
				// load the first branch of the tree
				lazyTree.loadBranches( treeRoot.find('li.root') );
				// establish event listeners required for operation
				lazyTree.setEventListeners();
				// if persistence is enabled, try to load previously stored elements
				if(lazyTree.isPersistenceEnabled()){
					lazyTree.loadTree();	
				}
			},
			
			/*
				bindEvents : binds events to items in the tree
			*/
			bindEvents : function(treeNode){
				treeNode.find("a").each( function(){
					// listen for the click event on all nodes
					$(this).bind('click', lazyTree.nodeClickHandler);
					// if keystrokes are enabled, listen for related events
					if(treeOptions.enableKeystrokes){
						// add class on focus
						$(this).bind('focus', function(){
							$(this).addClass(treeOptions.focusedClass);
						});
						// remove class on focus
						$(this).bind('blur', function(){
							$(this).removeClass(treeOptions.focusedClass);
						});	
						// listen for keystrokes that are used to navigate the tree
						$(this).bind('keydown', lazyTree.navigateByKeystroke);	
					}
				});
			},
			
			/*
				branchHasChildren : determines if a branch currently has children loaded
			*/
			branchHasChildren : function(treeNode){
				// check to see if a branch has child lists
				return (treeNode.children('ul:not(.' + treeOptions.errorClass + ')').length > 0);
			},
			
			/*
				createRoot : creates the root elements in the DOM which all branches will expand upon
			*/
			createRoot : function() {
				// create root ul with two li items, one for loading, one as the first branch
				var root = $('<ul class="jQueryLazyTree"><li class="treeLoader">' + treeOptions.loadingMessage + '</li>' +
							 '<li class="root" rel="' + treeOptions.rootData[treeOptions.idProperty] + '"></li></ul>');
				// set the root data on the first branch in the tree
				root.find('li.root').data('nodeData', treeOptions.rootData);
				// add root element to the DOM
				treeRoot.html(root);
			},
			
			/*
				createTreeNode : creates a list item for all tree nodes and attaches appropriate data
			*/
			createTreeNode : function(data, parent){
				// build list item
				var newNode = 	$('<li class="' + data[treeOptions.typeProperty] + '" rel="' + data[treeOptions.idProperty] + '">' + 
								'<a href="' + lazyTree.getNodeHref(data) + '">' + data[treeOptions.titleProperty] + '</a></li>');
				// attach data to element
				newNode.data('nodeData', data);
				return newNode;
			},
			
			/*
				debug : for development only, writes to the FireBug console
			*/
			debug : function(value){
				// make sure the enableDebug flag is true and the console is available
				if(treeOptions.enableDebug && window.console && window.console.log){
					window.console.log(value);
				}
			},
			
			/*
				errorHandler : handles all errors returned from the AJAX request
			*/
			errorHandler : function(XMLHttpRequest, textStatus, errorThrown, treeNode){
				// log the error
				lazyTree.debug(textStatus + ' : ' + errorThrown);
				// remove the loading class from the branch that made the request
				treeNode.removeClass(lazyTree.getTreeOption('loadingClass'));
				var nodeChildren =  treeNode.children('ul.error');
				if(nodeChildren.length == 0){
					// create new branch to display warning to user
					var newBranch = $('<ul class="lazyTreeBranch ' + treeOptions.errorClass + '"><li>Sorry, load failed.</li></ul>');
					// add the branch to the tree
					treeNode.append(newBranch);
					// open the requesting branch to show all the warning
					lazyTree.toggleBranch(newBranch);
				}else{
					nodeChildren.fadeOut().fadeIn();
				}
			},
			
			/*
				findInArray : finds an element in an array that has a property value that matches the value provided
			*/
			findInArray : function(array, propertyName, value){
				// loop through the array
				for(var i=0; i<array.length; i++){
					// check the object property to see if it matches the value given
					if(array[i].hasOwnProperty(propertyName) && array[i][propertyName] == value){
						return i;	
					}
				}
				// if no matches were found, return -1
				return -1;
			},
			
			/*
				findNodeInBranch : finds a node within a branch based on it's id
			*/
			findNodeInBranch : function(branch, nodeID){
				// the node is located by the value of it's "rel" attribute
				// if "rel" attributes are not used, you could simply loop
				// through the nodes and compare their "nodeData"
				return $(branch).find("li[rel='" + nodeID + "']");
			},
			
			/*
				focusNextNode : finds the next or previous node in the tree and gives it focus
			*/
			focusNextNode : function(currentNode, isNext){
				// remove focus from current node
				currentNode.blur();
				// get the index in the tree of the current node
				var nodeIndex = treeRoot.find('a:visible').index(currentNode);
				// determine if we want the previous or next node
				var indexModifier = (isNext) ? 1 : -1;
				// get the previous or next node and give it focus
				var nextNode = treeRoot.find('a:visible').get(nodeIndex+indexModifier);
				if(typeof nextNode == 'object') nextNode.focus();
			},
			
			/*
				getAjaxURL : get the URL to make the AJAX request
			*/
			getAjaxURL : function(treeNode){
				// simply return what's in the options
				// this method will often be overridden for more granular control
				return treeOptions.ajaxURL;
			},
			
			/*
				getNodeHref : gets the href value for a tree leaf, this method would typically by override for custom URLs
			*/
			getNodeHref : function(nodeData){
				return "#";
			},
			
			/*
				getSelectedNode : convenience method for external references to get the currently selected Node
			*/
			getSelectedNode : function(){
				return selectedNode;
			},
			
			/*
				getTreeOption : convenience method for external references to find an option value
			*/
			getTreeOption : function(optionName){
				// if the option value exists then return it, otherwise return null
				if(typeof treeOptions[optionName] != "undefined"){
					return treeOptions[optionName];
				}else{
					return null;	
				}	
			},
			
			/*
				getTreeRoot : convenience method for external references to get the root element of the tree
			*/
			getTreeRoot : function(){
				return treeRoot;
			},
			
			/*
				isBranch : determines if a tree node is a branch element or not
			*/
			isBranch : function(treeNode){
				// detect the 'branchType' class
				return treeNode.hasClass(treeOptions.branchType);
			},
			
			/*
				isBranchOpen : determines if a branch is currently open/expanded
			*/
			isBranchOpen : function(branch){
				return branch.hasClass(treeOptions.expandedClass);
			},
			
			/*
				isPersistenceEnabled : detected whether the persistence flag has been set and all dependencies are met
			*/
			isPersistenceEnabled : function(){
				// checks the persistTree boolean flag in the options
				if(treeOptions.persistTree){
					// makes sure that required cookie plugin is present
					if(typeof jQuery.cookie != 'undefined'){
						return true;	
					}else{
						lazyTree.debug('lazyTree cannot persist, jQuery.cookie plugin not found');
					}
				}else{
					lazyTree.debug('lazyTree cannot persist, persistTree property is set to false');
				}
				return false;
			},
			
			/*
				leafClickHandler : sets the selected node properties and call the loadLeaf method
			*/
			leafClickHandler : function(leaf){
				// set this leaf as the selected node and set the selected style
				lazyTree.setSelectedNode(leaf);
				// load the document (or whatever) requested
				lazyTree.loadLeaf(leaf);
			},
			
			/*
				loadLeaf : gets the request document, this method is typically overridden
			*/
			loadLeaf : function(leaf){
				alert('is leaf: ' + leaf.data('nodeData')[treeOptions.titleProperty]);
			},
			
			/*
				loadBranches : makes the AJAX call to request branch information from the server
			*/
			loadBranches : function(treeNode){
				// add the loading class to the branch being queried
				treeNode.addClass(treeOptions.loadingClass);
				// make the AJAX call
				$.ajax({
					type : 		'GET',
					url : 		lazyTree.getAjaxURL(treeNode),
					dataType : 	treeOptions.dataType,
					// data gets set by the prepDataForRequest method
					data : 		lazyTree.prepDataForRequest(treeNode.data("nodeData"), treeNode),
					// before the success event is triggered, the data is filtered in the preprocessDataFilter method
					dataFilter : function(data, type){
									return lazyTree.preprocessDataFilter(data, type, treeNode);
								},
					// the request has been completed and the data has been filtered
					success : 	function(data, textStatus){
									lazyTree.successHandler(data, textStatus, treeNode);
								},
					// there was an error in the request
					error : 	function(XMLHttpRequest, textStatus, errorThrown){
									lazyTree.errorHandler(XMLHttpRequest, textStatus, errorThrown, treeNode);
								}

				});
			},
			
			/*
				loadTree : starts the process of reloading the tree to it's previous 'state' by reading the cookie
			*/
			loadTree : function(){
				// get the cookie
				var treeCookie = $.cookie('lazyTree-' + treeOptions.treeName);
				// validate the cookie, the cookie should be in the form of an object - stored as JSON
				if(treeCookie && typeof eval('('+$.trim(treeCookie)+')') == 'object'){
					var cookieObj = eval('('+$.trim(treeCookie)+')');
					// store the openNodes array globally
					nodesToOpen = cookieObj.openNodes;
					// the selected node object will be passed the persistBranchLoad function
					var selectedLeaf = cookieObj.selectedNode;
					// create an event listener for the 'branchLoad' event
					// the persistBranchLoad function will load branches stored in the openNodes array
					treeRoot.bind('branchLoad', function(evt, newNode){ lazyTree.persistentBranchLoad(evt, newNode, selectedLeaf)});
				}
			},
			
			/*
				navigateByKeystroke : handles the key clicks that can be used to navigate the tree
			*/
			navigateByKeystroke : function(evt){
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
									// if branch does not have children, load them now
									lazyTree.loadBranches(treeNode);	
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
				};
				return !keyProcessed;
			},
			
			/*
				nodeClickHandler : handles the click event on a tree node
			*/
			nodeClickHandler : function(evt){
				// grab the list item for the link that was clicked
				var treeNode = $(evt.target).parent('li');
				$(evt.target).focus();
				// determine if the link was a 'branch' or a 'leaf'
				if(lazyTree.isBranch(treeNode)){
					if(lazyTree.branchHasChildren(treeNode)){
						// if branch has children, then open/close that branch
						lazyTree.toggleBranch(treeNode.children('ul:first'));	
					}else{
						// if branch does not have children, load them now
						lazyTree.loadBranches(treeNode);	
					}
				}else{
					// if a leaf has been clicked, call the leafClickHandler
					lazyTree.leafClickHandler(treeNode);	
				}
				// return false to cancel the click event
				return false;
			},
			
			/*
				persistendBranchLoad :
			*/
			persistentBranchLoad : function(evt, newBranch, selectedLeaf){
				// store the data of the new branch that just loaded
				var parentNodeData = $(newBranch).data('nodeData');
				// verify that it contains the id property necessary
				if(parentNodeData.hasOwnProperty(treeOptions.idProperty)){
					// loop through each 'open node' stored in the cookie
					$.each(nodesToOpen, function(){
						// determine if the newly loaded branch is a parent to any of the 'open nodes'
						if(this.parent == parentNodeData[treeOptions.idProperty]){
							// find that node within the branch
							lazyTree.findNodeInBranch(newBranch, this.node).each( function(){
								var foundNode = $(this);
								// because of the synchronous nature of JavaScript method calls,
								// each branch must be stored in a queue to be executed later
								treeRoot.queue('openBranchQueue', function(){
									// load the node in the tree
									lazyTree.loadBranches(foundNode);
									// remove the node from the cookie array
									var nodeIndex = lazyTree.findInArray(nodesToOpen, 'node', foundNode.data('nodeData')[treeOptions.idProperty]);
									if(nodeIndex > -1){
										nodesToOpen.splice(nodeIndex,1);	
									}
								});		
																				
							});
						}
					});	
					
					// now load all the functions that have been stored in the queue
					treeRoot.dequeue('openBranchQueue');
					
					lazyTree.debug('number of items stored: ' + nodesToOpen.length);
					// determine if the new branch is the parent of the stored selected node
					if(selectedLeaf.parent == parentNodeData[treeOptions.idProperty]){
						// get the selected node from the DOM
						var selectedNodeFound = lazyTree.findNodeInBranch(newBranch, selectedLeaf.node);
						lazyTree.setSelectedNode(selectedNodeFound);
					}					
				}
				
				// check to see if there are any 'open nodes' still left to open
				if(nodesToOpen.length < 1){
					lazyTree.debug('all nodes loaded: ' + parentNodeData[treeOptions.idProperty]);
					treeRoot.unbind('branchLoad');
				}
			},
			
			/*
				preprocessDataFilter : filters data before is returned to the 'success' method
			*/
			preprocessDataFilter : function(data, type, treeNode){
				// simply return the raw data since it's already in JavaScript Object Notation
				// this method would typically be overriden to accomodate XML or other data types returned
				return data;
			},
			
			/*
				prepDataForRequest : returns whatever data needs to be sent to the server in the AJAX request
			*/
			prepDataForRequest : function(data, treeNode){
				// simply send the node ID, if it's null then send a blank ID
				var nodeID = (typeof data[treeOptions.idProperty] != 'undefined') ? data[treeOptions.idProperty] : '';
				return {id: nodeID};	
			},
			
			/*
				saveTree : stores the current 'state' of the tree in a cookie
			*/
			saveTree : function(){
				// make sure persistence is enabled
				if(lazyTree.isPersistenceEnabled()){
					// open nodes will store the object string for all open nodes
					var openNodes = '';
					// find all open folders by checking for the 'expanded' class
					treeRoot.find('li.expanded').each( function(){
						// get the node's parent and grab it's id
						var parentNode = $(this).parents('li:first');
						if(parentNode.length == 0) return;
						var parentID = parentNode.data('nodeData')[treeOptions.idProperty];
						// get the node's id 
						var nodeID = $(this).data('nodeData')[treeOptions.idProperty];
						// append this node to the object string
						openNodes += '{node: "' + nodeID + '", parent: "' + parentID + '"},';
					});
					
					// remove the trailing comma from the object string
					openNodes = openNodes.substr(0, openNodes.length-1);
					
					// get the id and the parent id of the currently selected node
					var selectedLeaf = "{id: '', parent: ''}";
					if(typeof selectedNode.data('nodeData') == 'object'){
						var selectedNodeID = selectedNode.data('nodeData')[treeOptions.idProperty];
						var selectedNodeParentID = $(selectedNode).parents('li:first').data('nodeData')[treeOptions.idProperty];
						selectedLeaf = "{node: '" + selectedNodeID + "', parent: '" + selectedNodeParentID + "'}";	
					}
					
					// save the cookie to the user's machine
					$.cookie('lazyTree-'+treeOptions.treeName,
							 '{treeName: "' + treeOptions.treeName + '", openNodes: [' + openNodes + '], selectedNode: ' + selectedLeaf + '}',
							 {expires: 45, path: '/'});	
				}
			},
			
			/*
				setEventListeners : creates event listeners to respond to tree events
			*/
			setEventListeners : function(){
				// if persistence is enabled, detect the 'unload' event so we can store the 'state' of the tree
				if(lazyTree.isPersistenceEnabled()){
					$(window).bind('unload', lazyTree.saveTree);	
				}
			},
			
			/*
				setSelectedNode : set the selected node in the tree and adds the selected style to it
			*/
			setSelectedNode : function(node){
				// remove selection class from current item
				if(typeof selectedNode == 'object')
					selectedNode.removeClass(treeOptions.selectedClass);
				// add the selection class to the new item
				if(typeof node == 'object')
					node.addClass(treeOptions.selectedClass);
				// store a reference to the new selection
				selectedNode = node;
			},
			
			/*
				successHandler : handles the data returned from a successful AJAX request
			*/
			successHandler : function(data, textStatus, treeNode){
				//lazyTree.debug('return data type: ' + typeof data);
				// if this was the first branch loaded, remove the 'loading...' message
				treeRoot.find('li.treeLoader').remove();
				// remove any previous error messages
				treeNode.children('ul.' + treeOptions.errorClass).remove();

				//create a new list and append all the new children to the requesting branch
				var newBranch = $('<ul class="lazyTreeBranch"></ul>');
				$.each(data, function(index, obj){
					// add one li for each data item returned from the server
					newBranch.append(lazyTree.createTreeNode(obj, treeNode));
				});
				treeNode.append(newBranch);
				
				// bind click handler and other events to the new nodes
				lazyTree.bindEvents(treeNode);
				// remove the loading class from the branch that made the request
				treeNode.removeClass(treeOptions.loadingClass);
				// open the requesting branch to show all it's new children
				lazyTree.toggleBranch(newBranch);
				// dispatch the 'branchLoad' event
				treeRoot.trigger('branchLoad', treeNode);
			},
			
			/*
				toggleBranch : opens or closed a branch
			*/
			toggleBranch : function(branch){
				// add or remove the 'expanded' class
				branch.parent('li').toggleClass(treeOptions.expandedClass);
				// hide or show the branch's children
				branch.slideToggle(600);
			}
			
		};// end public methods
		
		// extend jQuery methods to add the Tree's public methods
		$.each(publicMethods, function(method){
			lazyTree[method] = this;
		});
		
		return lazyTree;
	};// end lazyTree function

	// establish default tree options
	$.fn.lazyTree.defaultOptions = {
		treeName : 'lazyTree',		// the name of the tree, will be stored in the the persistence cookie (if applicable)
		persistTree : false,		// a boolean flag, whether to store the 'state' of the tree between visists
		titleProperty : 'title',	// the property in the data that contains the 'title' of each tree item
		typeProperty : 'type',		// the property in the data that differentiates branches from leaves
		idProperty : 'id',			// the property in the data that uniquely identifies each element in the tree
		loadingMessage : '&nbsp;',	// the loading text that displays while the tree is initializing
		rootData : {id: ''},		// the data to send to the server for the initial AJAX call to the the root elements
		ajaxURL : 'proxy.cfm',		// the URL to use for the AJAX requests - this can become dynamic by overriding the getAjaxURL method
		dataType : 'json',			// the return type of the AJAX requests
		branchType : 'branch',		// the value of the 'typeProperty' for branch elements
		leafType : 'leaf',			// the value of the 'typeProperty' for leaf elements
		enableKeystrokes : true,	// a boolean flag specifying whether to allow the user to navigate the tree with the keypad
		enableDebug : false,		// a boolean flag specifying whether to allow logs in the Firebug console
		expandedClass : 'expanded',	// the CSS class used to style branch nodes
		loadingClass : 'loading',	// the CSS class used to style elements during the AJAX load process
		selectedClass : 'selected',	// the CSS class used to style the currently selected element in the tree
		focusedClass : 'focused',	// the CSS class used for elements in the tree that currently have focus
		errorClass : 'error'		// the CSS class used when an error occurrs loading a node
	};
	
// end closure
})(jQuery);
