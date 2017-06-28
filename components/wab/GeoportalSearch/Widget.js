define([	
    'dojo/_base/declare',
	  'dijit/_WidgetsInTemplateMixin',
    'jimu/BaseWidget',
    'jimu/dijit/TabContainer',    
    'jimu/dijit/Message',
    'jimu/utils',
    'jimu/dijit/LoadingShelter',
		  
    'dojo/json',
    'dojo/_base/lang',
    'dojo/_base/html',
    'dojo/_base/array',
    'dojo/query',
    'dojo/mouse',
    'dojo/on',
    'dojo/aspect',
    'dojo/dom-construct',
	  'dojo/dom',
	  'dojo/topic',
	  'widgets/GeoportalSearch/views/SearchPane',
    'widgets/GeoportalSearch/views/ResultsPane'    
  ],

function(declare, _WidgetsInTemplateMixin, BaseWidget, 
	TabContainer, Message, utils, LoadingShelter, 	
	JSON,lang, html, array,
  query, mouse, on, aspect, domConstruct, dom,topic) {	
  return declare([BaseWidget, _WidgetsInTemplateMixin], {

  	name: 'GeoportalSearch',
    baseClass: 'geoportal-search-widget',

  	tabContainer: null,
    resultLayer: null,
  	progressBar: null,
    tabContainer: null,
    onClickEvent: null,
    isValidConfig:false,
	
    postCreate: function() {
  	  console.log('postCreate');
	    this.inherited(arguments);      
      this._initTabContainer();	 	  
      this._attachTopics();      
	  },

    onOpen: function(){
      console.log('onOpen');
    },

	 onClose:function(){
    console.log('onClose');
		this._hideInfoWindow();
		this.inherited(arguments);
	 },


	destroy:function(){
    console.log('destroy');
		this._hideInfoWindow();
		if(this.resultLayer){
			this.map.removeLayer(this.resultLayer);
		}
		this.resultLayer = null;
		this.inherited(arguments);
	},

    onMinimize: function(){
      console.log('onMinimize');
    },

    onMaximize: function(){
      console.log('onMaximize');
    },

    onSignIn: function(credential){
      /* jshint unused:false*/
      console.log('onSignIn');
    },

    onSignOut: function(){
      console.log('onSignOut');
    },

	onShowAll: function() {
		this.search(null, true);
	},
		
	_startup:function(){
        if (!this._startedNow) {
        }
        this._startedNow = true;
    },

  _attachTopics: function(){
      this.own(topic.subscribe("/widgets/GeoportalSearch/action/switchTab", lang.hitch(this, function (sender, args) { 
      	if(args.tab == this.nls.results){       
       		this.tabContainer.selectTab(this.nls.results); 
          this.shelter.show();         
        }else {
        	this.tabContainer.selectTab(this.nls.selectByAttribute);
        }
    		if(this.resultLayer){
    			this.resultLayer.clear();
    		}
      })));

      this.own(topic.subscribe("/widgets/GeoportalSearch/action/search", lang.hitch(this, function (sender, args) { 
        this.shelter.hide();   
      })));
    },		
	
	_initTabContainer:function(){
      console.log('_initTabContainer');
      this.tabContainer = new TabContainer({
        tabs: [{
          title: this.nls.selectByAttribute,
          content: this.queryNode1
        }, {
          title: this.nls.results,
          content: this.queryNode2
        }],
        selected: this.nls.selectByAttribute
      }, this.tabQuery);
      this.tabContainer.startup();
      utils.setVerticalCenter(this.tabContainer.domNode);
    },

    _hideInfoWindow:function(){
        if(this.map && this.map.infoWindow){
          this.map.infoWindow.hide();
          if(typeof this.map.infoWindow.setFeatures === 'function'){
            this.map.infoWindow.setFeatures([]);
          }
          this.map.infoWindow.setTitle('');
          this.map.infoWindow.setContent('');
        }
      }

	});
});