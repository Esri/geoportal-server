dojo.require("dijit.Dialog");


/**
 * Queries the gemet interface for concepts
 */
dojo.declare(
	"Gemet",
	null,
	{
		// instance variables ==================================================
		lblLoadingMessage: "Fetching concepts from Gemet server : {0}", 
		lblHelp: "Click on a result",
		lblDialogTitle: "Gemet Keyword", 
		lblWordNotFound: "No similar keyword found. ",
		lblCancel: "Cancel",
		lblOk: "Ok",
		lblErrorKeywordEmpty: "Keyword to check gemet against cannot be empty",
		imgLoading: "catalog/images/loading.gif",
		proxyUrl: "catalog/download/proxy.jsp?{0}",
		conceptsUrl : "http://www.eionet.europa.eu/gemet/"
			+ "getConceptsMatchingRegexByThesaurus?thesaurus_uri="+
			"http://www.eionet.europa.eu/gemet/concept/&language={0}&regex={1}",
		
		// functions ===========================================================	
		/**
		 * Entry point function
		 * 
		 * @param sKeyWord Keyword to search for
		 * @param sLanguage 2 word language cord
		 * @param sIdWordInputOrFunction Id to HTML component where chosen word should 
		 * will be input to or the function to call with the string chosen
		 */
		findConcepts : function(sKeyWord/*String*/, 
				                sLanguage/*String*/,
				                sIdWordInputOrFunction/*String or function*/) {
			
			
			if(sLanguage == null || sLanguage == "") {
				sLanguage = "en";
			}
			var urlConcepts = this.conceptsUrl.replace("{0}", 
					encodeURIComponent(sLanguage));
			urlConcepts = urlConcepts.replace("{1}", 
					encodeURIComponent(sKeyWord));
			
			var imgDiv = dojo.create("div");
			dojo.create("img",
			  {
				src : this.imgLoading
			  },
			  imgDiv);
			dojo.create("div",
			  {
				innerHTML: this.lblLoadingMessage.replace("{0}", urlConcepts)
			  },
			  imgDiv
			);
			/*dojo.style(imgDiv, {
			  position: "relative",
  	          left: "50%",
  	          top: "50%"
			});*/
			var djDialogGemet = new dijit.Dialog({
			      title: this.lblDialogTitle,
			      content: imgDiv,
			      style: "width: 600px; display: none; border: 1px solid #000000; background: #FFFFFF;"
			});
			dojo.addClass(djDialogGemet.domNode, "tundra");
			dojo.addClass(djDialogGemet.domNode, "section");
			djDialogGemet.show();
			if(sKeyWord == null || dojo.trim(sKeyWord) == '') {
				this.findConceptsErrorHandler(djDialogGemet, 
						this.lblErrorKeywordEmpty);
				return;
			}
			var promise = dojo.xhrGet ({
				  url:   this.proxyUrl.replace("{0}", urlConcepts), 
				  content:{},
				  callbackParamName:"callback",
				  timeout: 60000,
				  load:dojo.hitch(this, this.findConceptsHandler, sIdWordInputOrFunction, djDialogGemet), 
				  error: dojo.hitch(this, this.findConceptsErrorHandler, djDialogGemet)
		    });
			var dfd = new dojo.Deferred(promise);
			dfd.addErrback(dojo.hitch(this, this.findConceptsErrorHandler, djDialogGemet));
			
		},
		/**
		 * Handler called after a concept is found
		 * 
		 * @param sIdWordInputOrFunction the id of the componenet where the chosen
		 * key word will be input
		 * @param response Response from gemet
		 */
		findConceptsHandler : function(sIdWordInputOrFunction, djDialogGemet, response) {
			
			var elContent = dojo.create("div",
			  {
				id: "gemetWizardContent",
				innerHTML: this.lblHelp
			  });
			var elTable = dojo.create("div",
			  {	
				id: "gemetWordTable",
				overflow: "auto",
				width: "100%",
				align: "center"
		
			  }, elContent);
			dojo.style(elContent,
			  {
				overflow: "auto",
				width: "100%",
				border: "1px solid gray",
				padding: "5px"
			  })	;
			dojo.style(elTable,
			  {
				overflow: "auto",
				width: "96%",
				maxHeight: "300px",
				border: "1px solid gray",
				margin: "auto"
			  }	
			
			);
			try {
			  var gemetResults = dojo.fromJson(response);
			}catch(err) {
				throw this.lblWordNotFound;
			}
			if(gemetResults == null) {
				gemetResults = [];
			}
			//gemetResults.sort(this.sortGemetResults);
			
			var arrConcepts = 
				dojo.filter(gemetResults, dojo.hitch(this, function(concept) {
				if(typeof(concept.preferredLabel) == 'undefined' || 
				   concept.preferredLabel == null ||
				   typeof(concept.preferredLabel.string) == 'undefined'	||
				   concept.preferredLabel.string == null
						) {
					return false;
				}
				if(typeof(concept.definition) == 'undefined' || 
						concept.definition == null) {
					concept.definition = {};
					concept.definition.string = "";
				}
				var elRow = dojo.create(
				  "div",
			      {
					  align: "left"
			      },
				  elTable);
				var elTitle = dojo.create(
				  "div",
				  {
					  innerHTML: concept.preferredLabel.string
				  },
				  elRow);
				var elDefinition = dojo.create(
				  "div",
				  {
					  innerHTML: concept.definition.string
				  },
				  elRow);
				dojo.style(elRow, {
					cursor: "pointer"
					
				});
				dojo.addClass(elTitle, "resultsTitle");
				dojo.addClass(elDefinition, "resultsContent");
				dojo.addClass(elRow, "noneSelectedResultRow");
				dojo.connect(elRow, "onmouseenter", 
				  dojo.hitch(this, function(evt) {
					dojo.removeClass(elRow, "noneSelectedResultRow");
					dojo.addClass(elRow, "selectedResultRow");
				}));
				dojo.connect(elRow, "onmouseleave", 
				  dojo.hitch(this, function(evt) {		
					dojo.addClass(elRow, "noneSelectedResultRow");
					dojo.removeClass(elRow, "selectedResultRow");
				}));
				dojo.connect(elRow, "onclick", 
				  dojo.hitch(this, "writeChosenEntry", 
						  concept.preferredLabel.string, 
						  sIdWordInputOrFunction,
						  djDialogGemet));
				return true;
				
			}));
			
			
			if(arrConcepts == null || arrConcepts.length <= 0) {
				throw this.lblWordNotFound;
							
			} else {
				this.addButton(elContent, this.lblCancel, djDialogGemet);
			}
			djDialogGemet.attr("content", elContent);
			djDialogGemet.containerNode.style.width = "";
			djDialogGemet.containerNode.style.height = "";
			djDialogGemet.startup();
			djDialogGemet.show();
		},
		
		/**
		 * Error Handler from gement end point
		 * 
		 * @param djDialogGemet The dialog
		 * @param error
		 */
		findConceptsErrorHandler : function(djDialogGemet, error, c, d) {
			var elContent = dojo.create("div");
			
			var elError = dojo.create("div", {innerHTML:error}, elContent);
			djDialogGemet.attr("content", elContent);
			this.addButton(elContent, this.lblOk, djDialogGemet);
			/*djDialogGemet.attr("style", "height: 200px");
			this.addButton(elContent, this.lblOk, djDialogGemet);
			djDialogGemet.resize();*/
			djDialogGemet.containerNode.style.width = "";
			djDialogGemet.containerNode.style.height = "";
			djDialogGemet.show();
			
		},
		
		
		
		/**
		 * Chosen entry written to the component 
		 * 
		 * @param title Chosen entry title
		 * @param sIdWordInputOrFunction Componenet to write the title into or function to call with title
		 * @param djDialogGemet The dialog component
		 * @param evt Click event
		 */
		writeChosenEntry :  function(title, sIdWordInputOrFunction, 
				djDialogGemet, evt) {
			
			if(typeof(sIdWordInputOrFunction) == 'function') {
				sIdWordInputOrFunction(title);
			} else {
			  dojo.attr(dojo.byId(sIdWordInputOrFunction), "value", title);
			}
			djDialogGemet.hide();
			djDialogGemet.destroy();
		},
		
		/**
		 * Adds the ok or cancel button (both do the same thing)
		 * 
		 * @param elContent Parent element
		 * @param sLabel Button label
		 * @param djDialogGemet Dialog to act on when action is pressed
		 */
		addButton: function(elContent, sLabel, djDialogGemet) {
			var elButtonDiv = dojo.create("div", {align: "center"}, elContent);
			var elButton = dojo.create("input", {
				value: sLabel,
				//value: sLabel,
				type: "button"
			}, elButtonDiv);
			dojo.connect(elButton,"onclick", dojo.hitch(this, this.closeDialog, 
					djDialogGemet));
			dojo.style(elButtonDiv, {
				margin: "auto",
				padding: "5px"
			});
		},
		
		/**
		 * Called when dialog is closed
		 * 
		 * @param djDialogGemet
		 * @param evt
		 */
		closeDialog :function(djDialogGemet, evt) {
			djDialogGemet.hide();
			djDialogGemet.destroy();
		},
		
		/**
		 * Sorts the results
		 * 
		 * @param x left value
		 * @param y right value
		 * @returns
		 */
		sortGemetResults : function(x, y) {
			
			if (x == null || y == null ||
			 typeof(x.preferredLabel) == 'undefined' ||
			 x.preferredLabel == null ||
			 x.preferredLabel.string ==  null ||
			 typeof(y.preferredLabel) == 'undefined' ||
			 y.preferredLabel == null ||
			 y.preferredLabel == null) {
			 return 1000000000000;
			}
			
			return x.preferredLabel.string.toLowerCase().localeCompare(
					y.preferredLabel.string.toLowerCase());
			
		} 
	}
	
);



