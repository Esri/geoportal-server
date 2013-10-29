dojo.require("dijit.Dialog");
dojo.require("dijit.dijit");
dojo.require("dojox.layout.TableContainer");
dojo.require("dijit.form.TextBox");
dojo.require("dojox.data.XmlStore");
dojo.require("dojox.xml.DomParser");

var gxeGemetLangData = [
            
            { name: "Arabic" ,id: "ar"},
            { name: "Bulgarian" ,id: "bg"},
            { name: "Catalan" ,id: "ca"},
            { name: "Czech" ,id: "cs"},
            { name: "Danish" ,id: "da"},
            { name: "German" ,id: "de"},
            { name: "Greek" ,id: "el"},
            { name: "English" ,id: "en"},
            { name: "English (US)" ,id: "en-US"},
            { name: "Spanish" ,id: "es"},
            { name: "Estonian" ,id: "et"},
            { name: "Basque" ,id: "eu"},
            { name: "Finnish" ,id: "fi"},
            { name: "French" ,id: "fr"},
            { name: "Irish" ,id: "ga"},
            { name: "Hungarian" ,id: "hu"},
            { name: "Italian" ,id: "it"},
            { name: "Lithuanian" ,id: "lt"},
            { name: "Latvian" ,id: "lv"},
            { name: "Maltese" ,id: "mt"},
            { name: "Dutch" ,id: "nl"},
            { name: "Norwegian" ,id: "no"},
            { name: "Polish" ,id: "pl"},
            { name: "Portuguese" ,id: "pt"},
            { name: "Romanian" ,id: "ro"},
            { name: "Russian" ,id: "ru"},
            { name: "Slovak" ,id: "sk"},
            { name: "Slovenian" ,id: "sl"},
            { name: "Swedish" ,id: "sv"},
            { name: "Chinese" ,id: "zh-CN"}
              ];

var gemetFocusTextElement;
/**
 * Queries the gemet interface for concepts
 */
dojo.declare(
	"Gemet",
	null,
	{
		// instance variables ==================================================
		djDialogGemet: null,
		
		elDivResults: null,
		lblLoadingMessage: "Fetching concepts from Gemet server : {0}", 
		lblHelp: "Click on a result",
		lblDialogTitle: "Gemet Keyword", 
		lblWordNotFound: "No similar keyword found. ",
		lblCancel: "Cancel",
		lblOk: "Ok",
		lblErrorKeywordEmpty: "Keyword to check gemet against cannot be empty",
		lblSearch: "Search",
		imgLoading: "catalog/images/loading.gif",
		conceptsUrl : "http://www.eionet.europa.eu/gemet/"
			+ "getConceptsMatchingRegexByThesaurus?thesaurus_uri="+
			"http://www.eionet.europa.eu/gemet/concept/&language={0}&regex={1}",
	    /*conceptsThemeUrl: "http://www.eionet.europa.eu/gemet/"
			+ "getTopmostConcepts?thesaurus_uri="+
			"http://inspire.ec.europa.eu/theme/them.{0}.json",*/	
			conceptsThemeUrl: "http://inspire.ec.europa.eu/theme/theme.{0}.xml",/* only change to json format when endpoint has valid json and is jsonp*/	
		conceptsKeywordUrl: "http://www.eionet.europa.eu/gemet/"
			+ "getConceptsMatchingRegexByThesaurus?thesaurus_uri="+
			"http://www.eionet.europa.eu/gemet/concept/&language={0}&regex={1}",
		
		isThemeSearch : false,
		
		proxyUrl: "../../gpt/gxe/gxe-proxy.jsp?url=",
		
		 config: {
		        "ItemMetadata.enabled": false,
		        "ItemMetadata.allowDelete": false,
		        "ItemMetadata.allowSaveDraft": false,
		        "ItemMetadata.documentTypeKeys": null,
		        "ItemMetadata.gxeProxyUrl": null,
		        "ItemMetadata.useGxeProxyUrl": false,
		        "ItemMetadata.gemetThemesProxy": null
		 },
		
		// functions ===========================================================
		 constructor: function() {
			
			 GptCore.loadDeclaredConfig(this.config);
		     if (this.config["ItemMetadata.gemetThemesProxy"]) {
		            var s = this.config["ItemMetadata.gemetThemesProxy"];
		            if ((typeof (s) != "undefined") && (s != null)) {
		                this.proxyUrl = s;
		            }
		     }
			 
		 },
		 initFind : function (
					elHtmlElementWithText	
				  ) {	
			 gemetFocusTextElement = elHtmlElementWithText;
			  var djt =  dijit.byId("djtDialogGemet");
			  if(djt != null) {
				  djt.destroy();
				  dijit.registry.remove("djtDialogGemet");
			  }
			  var name = "gemetControlsTable";
			  djt = dijit.byId(name);
			  if(djt != null) {
				  djt.destroy();
				  dijit.registry.remove(name);
			  }
			 // djt = null;
			 // if(djt == null) {
			    this.createGui();
			    this.initThemeSearch(this.isThemeSearch);
			    if(this.isThemeSearch == true) {
			    	dojo.style(dojo.byId('btnGemetSearch'), {
			    		visibility: 'hidden',
			    		display: 'none'
			    	});
			    	dojo.style(dojo.byId('gemetSearchTerm'), {
			    		visibility: 'hidden',
			    		display: 'none'
			    	});
			    	
			    	
			    	
			    	this.doFind();
			    }
			  /*} else {
				this.initThemeSearch(this.isThemeSearch);
				this.elDivResults =  dojo.byId('gemetDialogResults');
				dojo.empty(this.elDivResults);
				dojo.attr(dojo.byId('gemetSearchTerm'), "value", ""); 
				
				this.djDialogGemet = djt;
				this.djDialogGemet.show();
				if(this.isThemeSearch == true) {
					this.doFind();
				}
			  }*/
			  //this.doFind();
		 },
		 
		 initThemeSearch:function(isThemeSearch) {
			 
			var visibility = "hidden";
			var display = "none";
			if(isThemeSearch == false) {
				var visibility = "visible";
				var display = "inline";
			}
			dojo.style(dojo.byId('btnGemetSearch'), {
		    		visibility: visibility,
		    		display: display
		    	});
		    dojo.style(dojo.byId('gemetSearchTerm'), {
		    		visibility: visibility,
		    		display: display
		    });
		    if(isThemeSearch == true) {
		    	this.conceptsUrl = this.conceptsThemeUrl;
		    	
		    } else {
		    	this.conceptsUrl = this.conceptsKeywordUrl;
		    }
		 },
		 
		 doFind : function (
			searchText	
		  ) {	
			 
			  var e = document.getElementById("slctGemetLanguage");
		      var lang = e.options[e.selectedIndex].value;
		      if(lang == "") {
		    	  lang = null;
		      }
		     
		      var sValue = dojo.attr(dojo.byId('gemetSearchTerm'), "value");
		      if(typeof(searchText) != 'undefined') {
		    	  sValue = searchText;
		      }
		      if (sValue.length > 0 || this.isThemeSearch == true) {
		        this.findConcepts(sValue, lang,dojo.hitch(this,function(sGemetText) {
		          if ((typeof(sGemetText) != "undefined") && (sGemetText != null)) {
		            sGemetText = dojo.trim(sGemetText);
		            if (sGemetText.length > 0) {
		            	gemetFocusTextElement.value = sGemetText;
		              //this.htmlElement.disabled = true;
		            }
		          }
		        })); 
		      }	
		}, 	
		
		createGui:function () {
			
			var elContent = dojo.create("div", {id: "gemetDialogContent"});
			var  elDialogControls = 
				dojo.create("div", {id: "gemetDialogControls"}, elContent);
			
		    dojo.style(elDialogControls, {
		     paddingBottom: "10px"
		    });
			var djTable = new dojox.layout.TableContainer(
					{
			   id: "gemetControlsTable",			
			   cols: 3,
			   customClass:'gemetControlsTable',
			  "labelWidth": "150"
		    }, elDialogControls);
		
	        var defaultLocale = "";
	        
	        var elSearchTerm = dojo.create("input",{
	        	id: "gemetSearchTerm",
	        	type: "text",
	        	size: "30"
	        	
	        });
	        dojo.place(elSearchTerm, elDialogControls);
	        dojo.create("span", {innerHTML: '&nbsp;'}, elDialogControls);
	        var elSelect = new dojo.create("select", {
            	id: "slctGemetLanguage"
	        });
            dojo.place(elSelect, elDialogControls);
	        dojo.create("span", {innerHTML: '&nbsp;'}, elDialogControls);
            var elOptionSelected = null;
            if(typeof(gxeLocale) == 'undefined' || gxeLocale == null) {
            	var gxeLocale = "en";
            }
	        for(var i = 0; i < gxeGemetLangData.length; i++) {
	      	   var elOption = dojo.create('option', 
	      			   {  value: gxeGemetLangData[i].id,
	      		          innerHTML: gxeGemetLangData[i].name
	      		       }, 
	      			   elSelect);
	      	   if( gxeGemetLangData[i].id == gxeLocale) {
	      		   defaultLocale = gxeGemetLangData[i].id;
	      		   elOptionSelected = elOption;
	      	   } else if(gxeLocale.indexOf(gxeGemetLangData[i].id) == 0 && defaultLocale == "") {
	      		   defaultLocale = gxeGemetLangData[i].id;
	      		   elOptionSelected = elOption;
	      	   }
	      	 
	        }
	        
	        if(elOptionSelected != null) {
	            dojo.attr(elOptionSelected,"selected","selected");
	        }
	        
	        var elButton = dojo.create("button", {
            	id: "btnGemetSearch",
            	innerHTML: "Search"
            });
	        
	        dojo.connect(elButton, "click", dojo.hitch(this, function(){
	        	
	        	var searchText = dojo.attr(dojo.byId(elSearchTerm), "value");
	        	this.doFind(searchText);
	        }));
            dojo.place(elButton, elDialogControls);
        	var elLoadingImg = dojo.create("img",{
				src : this.imgLoading,
				id: "gemetImageLoading"
			  });
        	dojo.place(elLoadingImg, elDialogControls);
        	dojo.style(elLoadingImg, {
        		visibility: 'hidden'
        	});
	        
	        var elMsgDiv =dojo.create("div", {
	          		id: "gemetMessages"
	  		  });
	        dojo.place(elMsgDiv, elContent);
	        var elResults = 
	  				dojo.create("div", {id: "gemetDialogResults"}, elContent);
	        dojo.style(elResults, {
	        	width: "400px",
	        	height: "300px"
	        });  
	        this.elDivResults = elResults;
			/*dojo.create("img",
			  {
				src : this.imgLoading
			  },
			  imgDiv);
			dojo.create("div",
			  {
				innerHTML: this.lblLoadingMessage.replace("{0}", urlConcepts)
			  },
			  imgDiv
			);*/
	        //elSelect.onchange = function() {alert('dynamic');}
	        dojo.connect(elSelect, 
	    			"onchange", dojo.hitch(this, function(evt){
	    		
	    				this.doFind();
	    		
	    	}));
	        
	        dojo.connect(elSelect, 
	    			"onclick", dojo.hitch(this, function(evt){
	    		//debugger;
	    		
	    				//this.doFind();
	    		
	    	}));
		
			this.djDialogGemet = new dijit.Dialog({
			      title: this.lblDialogTitle,
			      id: "djtDialogGemet",
			      content: elContent,
			      style: "width: 425px; height: 450px; display: none; border: 1px solid #000000; background: #FFFFFF;"
			});
			dojo.addClass(this.djDialogGemet.domNode, "tundra");
			dojo.addClass(this.djDialogGemet.domNode, "section");
			this.djDialogGemet.show(); 
		},
		
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
			
			
			var elImg = dojo.byId("gemetImageLoading");
			if(elImg != null) {
				dojo.style(elImg, {
					width: "20px",
					height: "20px",
					visibility: "visible"
				});
			}
			
			if(sLanguage == null || sLanguage == "") {
				sLanguage = "en";
			}
			var urlConcepts = this.conceptsUrl.replace("{0}", 
					encodeURIComponent(sLanguage));
			urlConcepts = urlConcepts.replace("{1}", 
					encodeURIComponent(sKeyWord));
			
			
			if(this.isThemeSearch == false
					&& (sKeyWord == null || dojo.trim(sKeyWord) == '')) {
				this.findConceptsErrorHandler( 
						this.lblErrorKeywordEmpty);
				return;
			}
			
			
			// TM.  New url for themes has json errors and no jsonp.  Using their xml endpoint and proxy to circumvent this problem.
			if(urlConcepts.toLowerCase().indexOf('.xml') < 0 /*this.isThemeSearch == false*/) {	
				 var promise = dojo.io.script.get ({
	                 url:  urlConcepts, 
	                 content:{},
	                 callbackParamName:"jsonp",
	                 timeout: 60000,
	                 load:dojo.hitch(this, this.findConceptsHandler, 
	                                 sIdWordInputOrFunction,
	                                 this.isThemeSearch), 
	                 error: dojo.hitch(this, this.findConceptsErrorHandler)
				   });
				 var dfd = new dojo.Deferred(promise);
				 dfd.addErrback(dojo.hitch(this, this.findConceptsErrorHandler));
				 return;  
			} else {
			
				var tmpUrl =  urlConcepts;
				if(this.proxyUrl != null) {
					tmpUrl = this.proxyUrl + ((this.proxyUrl.indexOf("?") > -1 )? "": "?") + urlConcepts;
				} 
			    funcFindConceptsHandler = dojo.hitch(this, this.findConceptsHandlerXml, 
							  sIdWordInputOrFunction,
							  this.isThemeSearch);
			
			    var promise = dojo.xhrGet ({
					  url:  tmpUrl, 
					  content:{},
					  handleAs: 'text',
					  callbackParamName:"jsonp",
					  timeout: 60000,
					  load: funcFindConceptsHandler, 
					  error: dojo.hitch(this, this.findConceptsErrorHandler)
			    });
				var dfd = new dojo.Deferred(promise);
				dfd.addErrback(dojo.hitch(this, this.findConceptsErrorHandler));
			}
			
		},
		
		findConceptsHandlerXml: function(sIdWordInputOrFunction, isThemeSearch, 
				response) {
			
			var jsdom = dojox.xml.DomParser.parse(response);
			var response = dojo.map(jsdom.getElementsByTagName("theme"), function(elTheme) {
				var label = elTheme.getElementsByTagName("label");
				if(label.length < 1 || label[0].childNodes.length < 1) {
					label = "";
				} else {
					label = label[0].childNodes[0].nodeValue;
				}
				var description = elTheme.getElementsByTagName("description");
				if(description.length < 1 || description[0].childNodes.length < 1) {
					description = "";
				} else {
					description = description[0].childNodes[0].nodeValue;
				}
				var definition = elTheme.getElementsByTagName("definition");
				if(definition.length < 1 || definition[0].childNodes.length < 1) {
					definition = "";
				} else {
					definition = definition[0].childNodes[0].nodeValue;
				}
			
				return {
					definition: {
						string: definition
					} ,
					preferredLabel: {
						string: label
					}
				};
			});
            this.findConceptsHandler(sIdWordInputOrFunction, isThemeSearch, response);
			   console.debug(jsdom);
		},
		
		/**
		 * Handler called after a concept is found
		 * 
		 * @param sIdWordInputOrFunction the id of the componenet where the chosen
		 * key word will be input
		 * @param response Response from gemet
		 */
		findConceptsHandler : function(sIdWordInputOrFunction, isThemeSearch, 
				response) {
			
			var elImg = dojo.byId("gemetImageLoading");
			if(elImg != null) {
				dojo.style(elImg, {
					visibility: "hidden"
				});
			}
			dojo.empty(this.elDivResults);
						
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
			var gemetResults = response;
		
			if(gemetResults == null) {
				gemetResults = [];
			}
			//gemetResults.sort(this.sortGemetResults);
			var index = 0;
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
				
				if(isThemeSearch == true) {
				  var elCheckbox = dojo.create (
				    "input",
				    {
				     type:  "checkbox",
					 name:  "gemetThemeCheckbox",
					 value: concept.preferredLabel.string/*,
				     innerHTML:	dojo.string.escape("html",concept.preferredLabel.string) */
				    },
				    elRow
				  );
				  index ++;
				  //dojo.addClass(elCheckbox, "resultsTitle");
				  dojo.addClass(elCheckbox, "gemetCheckBox");
				}
				  var elTitle = dojo.create(
				    "span",
				    {
					  innerHTML: concept.preferredLabel.string
				    },
				    elRow);
				  dojo.addClass(elTitle, "resultsTitle");
				
				var elDefinition = dojo.create(
				  "div",
				  {
					  innerHTML: concept.definition.string
				  },
				  elRow);
				dojo.style(elRow, {
					cursor: "pointer"
					
				});
				
				
				
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
				if(isThemeSearch == false) {
				  dojo.connect(elRow, "onclick", 
				      dojo.hitch(this, "writeChosenEntry", 
						  concept.preferredLabel.string, 
						  sIdWordInputOrFunction,
						  this.djDialogGemet));
				} 
				return true;
				
			}));
			
			dojo.place(elContent, this.elDivResults);
			if(this.isThemeSearch == true) {
			   this.addOkButton(this.elDivResults, 
				  this.lblOk);
					
			}
			if(arrConcepts == null || arrConcepts.length <= 0) {
				throw this.lblWordNotFound;
							
			} else {
				//this.addButton(elContent, this.lblCancel, e);
			}
			
			
		},
		
		/**
		 * Error Handler from gement end point
		 * 
		 * @param djDialogGemet The dialog
		 * @param error
		 */
		findConceptsErrorHandler : function(error, c, d) {
		
			var elImg = dojo.byId("gemetImageLoading");
			if(elImg != null) {
				dojo.style(elImg, {
					visibility: "hidden"
				});
			}
			dojo.empty(this.elDivResults);
			var elContent = dojo.create("div", {}, this.elDivResults);
			
			var elError = dojo.create("div", {innerHTML:error}, elContent);
			djDialogGemet.attr("content", elContent);
			//this.addButton(elContent, this.lblOk, djDialogGemet);
			
			/*djDialogGemet.attr("style", "height: 200px");
			this.addButton(elContent, this.lblOk, djDialogGemet);
			djDialogGemet.resize();*/
			/*djDialogGemet.containerNode.style.width = "";
			djDialogGemet.containerNode.style.height = "";
			djDialogGemet.show();*/
			
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
		
		},
		
		readWriteThemes: function() {
			var delimeter = ", ";
			var sText = "";
			var checkedIndex = 0;
			dojo.query(".gemetCheckBox").forEach(
			  dojo.hitch(this, function(node, index, arr){
				var checked = dojo.attr(node, "checked");
				if(checked != null && 
						(checked == "checked" || checked == true)) {
					if(checkedIndex > 0) {
						sText += delimeter;
					}
					sText += dojo.attr(node, "value");
				    checkedIndex ++;	
				}
			}
            ));
			if(sText.length <= 0) {
				return;
			}
			var val = dojo.attr(gemetFocusTextElement, "value");
			if(val == null) {
				val = "";
			}
			val = dojo.trim(val);
			if(val.length > 0) {
				val += delimeter;
			}
			val += sText;
			dojo.attr(gemetFocusTextElement, "value", val);
			this.closeDialog();
		}, 
		/**
		 * Adds the ok or cancel button (both do the same thing)
		 * 
		 * @param elContent Parent element
		 
		 */
		addOkButton: function(elContent, sLabel) {
			var elButtonDiv = dojo.create("div", {align: "center"}, elContent);
			if(this.isThemeSearch == true) {
			  var elButtonOk = dojo.create("input", {
				value: this.lblOk,
				type: "button"
			  }, elButtonDiv);
			  dojo.connect(elButtonOk,"onclick", 
					dojo.hitch(this, this.readWriteThemes));
			}
			var elButtonCancel = dojo.create("input", {
				value: this.lblCancel,
				type: "button"
			}, elButtonDiv);
			
			dojo.connect(elButtonCancel, "onclick", 
					dojo.hitch(this, this.closeDialog)
			);
			dojo.style(elButtonDiv, {
				margin: "auto",
				padding: "5px"
			});
		},
		
		/**
		 * Called when dialog is closed
		 * 
		 * @param djDialogGemet
		 * 
		 */
		closeDialog :function(djDialogGemet) {
			this.djDialogGemet.hide();
			//djDialogGemet.destroy();
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



