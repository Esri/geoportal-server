define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/_base/array',
  'dojo/dom-construct',
  'dojo/dom-class',
  'dojo/_base/html',
  'dojo/Deferred',
  'dijit/_WidgetBase',
  'dijit/_TemplatedMixin',
  'dijit/_WidgetsInTemplateMixin',
  'dojo/text!./ResultsPane.html',
  'dojo/i18n!widgets/GeoportalSearch/nls/strings',
  'widgets/GeoportalSearch/views/Record',
  'widgets/GeoportalSearch/common/Util',
  'widgets/GeoportalSearch/common/List',  
  'widgets/GeoportalSearch/common/Query',
  'widgets/GeoportalSearch/common/QueryTask',
  'widgets/GeoportalSearch/common/Paginator',
  'widgets/GeoportalSearch/common/LayerFactory',
  'widgets/GeoportalSearch/common/WebmapProcessor',
  'widgets/GeoportalSearch/common/FootprintsGenerator',
  'jimu/dijit/Message',
  "esri/layers/GraphicsLayer",

  'dojo/topic',    
  'dijit/ProgressBar',
  'dijit/form/TextBox',
  'dijit/form/RadioButton',
  'dijit/form/CheckBox',
  'dijit/form/Select',  
  'dojo/store/Memory',
  'dojo/data/ObjectStore'
],function(declare,lang,array,domConstruct,domClass,html,Deferred,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin, template, nls, 
           Record, util, List, Query, QueryTask,Paginator, LayerFactory,
           WebmapProcessor,FootprintsGenerator,
           Message,GraphicsLayer,
           topic){
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
    
    baseClass: 'geoportal-results-view',
    templateString: template,
    
    nls: nls,
    list: null,
    results: null,
    
     postCreate: function() {
       this.inherited(arguments);
       this._initList();
       this.attachTopics();
       this.nItemsPerPage = this.config.recordsPerPage;
       this.showAllAddedLayers = this.config.showAllAddedLayers;
     },


     _initList: function() {
        console.log('onSignOut');
        this.list = new List();
        this.list.startup();
     },

     attachTopics: function(){

      this.own(topic.subscribe("/widgets/GeoportalSearch/action/search", lang.hitch(this, function (sender, args) { 
        if(args.success){       
          this._onQueryFinish(args.results,args.query, args.io);
        }else {
          this._onQueryError(args.error, args.io);
        }
      })));

      this.own(topic.subscribe("/widgets/GeoportalSearch/action/switchTab", lang.hitch(this, function (sender, args) {      
        if(this.resultLayer){
          this.resultLayer.clear();
        }
      })));


    },    

  _onQueryError: function(error, io) {
      var progressBar = this.progressBar;
      var divResult = this.divResult;
      html.setStyle(this.progressBar,'display','none');
      html.setStyle(this.divResult,'display','block');
      if(this.resultLayer){
       this.resultLayer.clear();
      }
      new Message({
        message: this.nls._onQueryError
      });
      console.debug(error);
  },

  _processRecords: function(){

    var len = this.results.records.length;
    if(len > 0){
      domConstruct.empty(this.pagination);
      var paginator = new Paginator();
      paginator.paginateResults({
                                  container:this.pagination,
                                  results:this.results,
                                  query:this.query,
                                  nItemsPerPage: Number(this.config.recordsPerPage),
                                  nls:this.nls
                                });
    }

    theList = dojo.byId("list"); // dnw this.list.domNode;
    theList.innerHTML = "";   
     
    var records = this.results.records;         
    array.forEach(records, lang.hitch(this, function(record, index){
      var recordInfo = new Record({record: record, index:index});
      recordInfo.startup();
      domConstruct.place(recordInfo.domNode,theList);
    }));  

  },

  _onQueryFinish: function(results, query, io) {

    var progressBar = this.progressBar;
    var divResult = this.divResult;
    html.setStyle(this.progressBar,'display','none');
    html.setStyle(this.divResult,'display','block');
    if(this.resultLayer){
     this.resultLayer.clear();
    }

    this.results = results;
    this.query = query;

    var title = "";
    var titlefield = query.queryUrl; //this.catalogs.value;
    
    var len = results.records.length;
    var divResultMessage = this.divResultMessage;
    if (len === 0) {      
      this.clear();
      divResultMessage.textContent =  this.nls.noResults;  
      return;
    } else {
      this.clear();
      divResultMessage.textContent = this.nls.featuresSelected + results.totalResults;
    }

   this._processRecords();
   this._addFootprints();
  },

  _addFootprints:function(){    
    var footprints;
    if (this.footprints) {
      this.footprints = this.map.getLayer("footprints");
      
    } else {
      this.resultLayer = new GraphicsLayer();
      this.resultLayer.id = "footprints";
      this.map.addLayer(this.resultLayer);
      this.footprints = this.map.getLayer("footprints");
      this.footprints.clear();
    }
    
    var fpGenerator = new FootprintsGenerator();
    fpGenerator.addFootprints(this.footprints,this.results.records);
  },

  _getAlias: function(att) {
    var field = this.config.layer.fields.field;
    var item;
    for (var i in field) {
      item = field[i];
      if (item.name.toLowerCase() === att.toLowerCase() && item.alias) {
        return item.alias;
      }
    }
    return att;
  },

  _selectResultItem: function(index, item) {
    var x = index.clientX;
    var y = index.clientY;
    var element = document.elementFromPoint(x, y);
   // var record = dojo.byId(element.id);

    var elementId = element.id;
    var elParts = elementId.split("_addToMap");
    var recordId = "";
    var isAddToMapLink = false;
    if(elParts && elParts.length==2){
      recordId = elParts[0];
      isAddToMapLink = true;
    }

    var link = dojo.byId(recordId + "_open");
    if(!link) {
      link = dojo.byId(elementId);
      if(!link || !link.dataset.href) {
        link = dojo.byId(elementId + "_open");
      }
    }

  //  var addToMapLink = dojo.byId(element.id);// + "_addToMap");

    if(!link) return;

    var href = link.dataset.href;    
    var linkType = "unknown";
         
    console.log('_selectResultItem=' + element.id + ", linktype=" + linkType);
  
    if (link) {

      linkType = link.dataset.linktype;

      if (isAddToMapLink && (linkType == "mapserver" || linkType == "featureserver" || linkType == "imageserver" 
        || linkType == "kml" || linkType == "wms")) {
        
        if(this.showAllAddedLayers){
          var def = new Deferred();
		  if(linkType == "mapserver" || linkType == "featureserver" || linkType == "imageserver"){
			linkType = "ags";
		  }
          LayerFactory.addLayer(this.map,def,linkType,href);
        }else{
           LayerFactory.createLayer(linkType,href).then(lang.hitch(this,function(layer){
                this.map.addLayer(layer);
            }));
        }
               
      } else if (linkType == "webmap") {        
          var wmProcessor = new WebMapProcessor();
          wmProcessor.process(href,this.map);
      } else {
        var win = window.open(href, '_blank');
        win.focus();
      }  

    } else {
      var win = window.open(href, '_blank');
      win.focus();
    }

    if (false) {
      var point = this.list.items[this.list.selectedIndex].centerpoint;
      this.map.centerAt(point).then(lang.hitch(this, function(){
        this.map.infoWindow.setFeatures([item.graphic]);
        this.map.infoWindow.setTitle(item.title);
        if(item.content){
          this.map.infoWindow.setContent(item.content);
        }else{
          this.map.infoWindow.setContent(item.title);
        }
        this.map.infoWindow.reposition();
        this.map.infoWindow.show(item.centerpoint);
      }));
    }

  }, 
  
    _hideInfoWindow:function(){
      if(this.map &&　this.map.infoWindow){
        this.map.infoWindow.hide();
      }
    },

    _isConfigValid:function(){
      return this.config.layer && this.config.layer.url && this.config.layer.fields && (this.config.layer.fields.field.length > 0);
    },
    

    addToMap:function (service){
      console.log('_addToMap: ' + service);
    },

    toggleFootprints: function() {
      var footprints = this.map.getLayer("footprints");
      if (footprints.visible) {
        footprints.hide();
        this.btnToggleFootprints.textContent = this.nls.show;
      } else {
        footprints.show();
        this.btnToggleFootprints.textContent = this.nls.hide;
      }
    
  },


  clear: function() {
    this._hideInfoWindow();

    if(this.pagination){
      domConstruct.empty(this.pagination);
    }
    
    var footprints = this.map.getLayer("footprints");
    if(footprints){
     // footprints.hide();
     footprints.clear();
    }
    
    this.divResultMessage.textContent = this.nls.noResults;
    
    divResultMessage = this.divResultMessage;
    divResultMessage.textContent = "";
    
    theList = dojo.byId("list");
    theList.innerHTML = "";
          
    return false;
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