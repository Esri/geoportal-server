define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/_base/array',
  'dojo/dom-construct',
  'dojo/dom-class',
  'dojo/_base/html',
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

  'esri/SpatialReference',
  'esri/layers/ArcGISDynamicMapServiceLayer',
  'esri/layers/ArcGISTiledMapServiceLayer',
  'esri/layers/ArcGISImageServiceLayer',
  'esri/layers/KMLLayer',
  'esri/layers/GraphicsLayer',
  'esri/layers/FeatureLayer',
  'esri/layers/WMSLayer',
  'esri/graphic',
  'esri/geometry/Point',
  'esri/geometry/Extent',
  'esri/symbols/SimpleMarkerSymbol',
  'esri/symbols/PictureMarkerSymbol',
  'esri/geometry/Polyline',
  'esri/symbols/SimpleLineSymbol',
  'esri/geometry/Polygon',
  
  'esri/symbols/SimpleFillSymbol',
    'esri/InfoTemplate',
    'esri/symbols/jsonUtils',
    'esri/geometry/webMercatorUtils',
  'dojo/topic',    
  'dijit/ProgressBar',
    'dijit/form/TextBox',
  'dijit/form/RadioButton',
    'dijit/form/CheckBox',
  'dijit/form/Select',
    'dojo/store/Memory',
    'dojo/data/ObjectStore'
],function(declare,lang,array,domConstruct,domClass,html,
           _WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin, template, nls, 
           Record, util,List, Query, QueryTask, 
           SpatialReference, ArcGISDynamicMapServiceLayer, 
           ArcGISTiledMapServiceLayer, ArcGISImageServiceLayer, 
           KMLLayer, GraphicsLayer, FeatureLayer, WMSLayer,
           Graphic, Point, Extent, SimpleMarkerSymbol, PictureMarkerSymbol, 
           Polyline, SimpleLineSymbol, Polygon, SimpleFillSymbol, InfoTemplate, 
           jsonUtils, webMercatorUtils,
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

  _paginateResults: function(){

    var elHdr = this.pagination;
    var nStartIndex = this.query.start;
    var nTotalResults = this.results.totalResults;

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

    if(this.elPageControl){
      elHdr.removeChild(this.elPageControl);
    }
    this.elPageControl = document.createElement("div");
    //this.elPageControl.id = this.resultsHeaderId+"-pageControl";
    this.elPageControl.className = "nav";
    
    var sPageSummary = this.nls.pageSummaryPattern;
    sPageSummary = sPageSummary.replace("{0}",nStartIndex);
    sPageSummary = sPageSummary.replace("{1}",nEndIndex);
    var elPageSummary = document.createElement("span");
    elPageSummary.className = "result";
    elPageSummary.appendChild(document.createTextNode(sPageSummary));
    this.elPageControl.appendChild(elPageSummary);
    
    var elPageNumbers = document.createElement("span");
    this.elPageControl.appendChild(elPageNumbers);
    if (iFrom > 1) {
      var elPage = document.createElement("a");
      elPage.setAttribute("href","javascript:void(0);");
      elPage.pageNumber = 1;
      elPage.appendChild(document.createTextNode(this.nls.first));
      elPageNumbers.appendChild(elPage);
      dojo.connect(elPage,"onclick",this,"_onPageClicked");

       var elPage = document.createElement("a");
       elPage.setAttribute("href","javascript:void(0);");
       elPage.pageNumber = iFrom;
       elPage.appendChild(document.createTextNode("<"));
       elPageNumbers.appendChild(elPage);
       dojo.connect(elPage,"onclick",this,"_onPageClicked");
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
        dojo.connect(elPage,"onclick",this,"_onPageClicked");
    
      }      
    }

    if (iTo < nOfPages) {  
      var elPage = document.createElement("a");
      elPage.setAttribute("href","javascript:void(0);");
      elPage.pageNumber = iTo;
      elPage.appendChild(document.createTextNode(">"));
      elPageNumbers.appendChild(elPage);
      dojo.connect(elPage,"onclick",this,"_onPageClicked");
      var elPage = document.createElement("a");
      elPage.setAttribute("href","javascript:void(0);");
      elPage.pageNumber = nOfPages;
      elPage.appendChild(document.createTextNode(this.nls.last));
      elPageNumbers.appendChild(elPage);
      dojo.connect(elPage,"onclick",this,"_onPageClicked");
    }

     elHdr.appendChild(this.elPageControl);

  },

  _onPageClicked: function(e){
    if (!e) e = window.event;
    var el = (window.event) ? e.srcElement : e.target;
    if ((el != null) && (el.pageNumber != null)) {
      this.query.start = ((el.pageNumber - 1) * this.nItemsPerPage) + 1;
      var queryTask = new QueryTask();
      queryTask.execute(this.query);
    }
  },

  _processRecords: function(){

    var len = this.results.records.length;
    if(len > 0){
      this._paginateResults();
    }

    theList = dojo.byId("list"); // dnw this.list.domNode;
    theList.innerHTML = "";   
     
    for (var i = 0; i < len; i++) {          
      var record = this.results.records[i];
      var recordInfo = new Record({record: record, index:i});
      recordInfo.startup();
      domConstruct.place(recordInfo.domNode,theList);
    }  

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
      divResultMessage.textContent = "No results"; // this.nls.noResults;  
      this.clear();
      return;
    } else {
      this.clear();
      divResultMessage.textContent = "Results found: " + results.totalResults; //this.nls.featuresSelected + results.records.length;
    }

   this._processRecords();
   this._addFootprints();
  },

  _addFootprints:function(results){
    var features = this.results.records;
    var symbol = new SimpleFillSymbol();
    symbol.setColor(new esri.Color([0,0,0,0.05]));
    
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
    
    for (var i = 0, len = features.length; i < len; i++) {
      var feature = features[i];
      var type = feature.geometry.type;
      var json = {};
      var geometry, centerpoint;
      if(feature.geometry.spatialReference){
       json.spatialReference = feature.geometry.spatialReference;
      }else{
        json.spatialReference = 4326;
      }
      switch (type) {
        case "multipoint":
        case "point":
          break;
        case "polyline":
          break;
        case "extent":
        case "Polygon":
        case "polygon":
         if(feature.bbox){
          var bbox = feature.bbox;
          geometry = Extent(bbox[0],bbox[1],bbox[2],bbox[3], new SpatialReference(4326)); 
          centerpoint = geometry.getCenter();
         }
          break;
        default:
          break;
      }

      var title = feature.title;
      var content = feature.summary;
      var it = new InfoTemplate(title, title + "<br>" + content);
      var graphic = new Graphic(geometry,symbol,feature,it);
      this.footprints.add(graphic);
    } 
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
    var link = dojo.byId(element.id + "_href");
    var href = "";
    
    var linkType = "unknown";
    if (link) {
      linkType = link.dataset.linktype;
      href = link.value;
    }
    
    console.log('_selectResultItem=' + element.id + ", linktype=" + linkType);
    
    var infoTemplate = new InfoTemplate("Attributes", "${*}");
  
    if (linkType == "mapserver") {
      var mapserverLayer = null;
      if (href.indexOf("tiles.arcgis.com/tiles") > 0) {
        mapserverLayer = new ArcGISTiledMapServiceLayer(href);
      } else {
        mapserverLayer = new ArcGISDynamicMapServiceLayer(href);
      }
      this.map.addLayer(mapserverLayer);
      
    } else if (linkType == "featureserver") {
      var featureLayer = new FeatureLayer(href, {
      mode: FeatureLayer.MODE_SNAPSHOT,
      outFields: ["*"],
      infoTemplate: infoTemplate
    });

      this.map.addLayer(featureLayer);
      
    } else if (linkType == "imageserver") {
      var imageServiceLayer = new ArcGISImageServiceLayer(href);
      this.map.addLayer(imageServiceLayer);
    
    } else if (linkType == "kml") {
      var kmlLayer = new KMLLayer(href);
      this.map.addLayer(kmlLayer);
    
    } else if (linkType == "wms") {
      var wmsLayer = new WMSLayer(href);
      this.map.addLayer(wmsLayer);
      
    } else if (linkType == "webmap") {
      //this.map.addLayer(imageServiceLayer);
      //http://www.arcgis.com/sharing/content/items/57c2df89f4064d748e9b84a690d7865a/data
      /*
      arcgisUtils.arcgisUrl = href.substr(0, href.indexOf("/sharing/content/items")) + "/sharing/content/items";
      var webmap = href.substr(href.indexOf("/sharing/content/items/") + 23, href.length-1);
      arcgisUtils.createMap(webmap);
      arcgisUtils.createMap(webmap,"map").then(function(response){
        this.map = response.map;
      });
      */
      var requestHandle = esriRequest({
        "url": href,
        handleAs:'json'
      },{
        useProxy:false
      });
      requestHandle.then(this._onFetchWebMapFinish, this._onFetchWebMapError);
    
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


  
  _onFetchWebMapFinish: function(response) {
    console.log('_onFetchWebMapFinish');
    
    var numLayers = response.operationalLayers.length;
    for (var ii=0; ii < numLayers; ii++) {
      var theLayer = response.operationalLayers[ii];
      var href = theLayer.url;
      var hrefLower = href.toLowerCase();
      var linkType = hrefLower.split("/").pop();
  
      if (linkType == "mapserver") {
        var mapserverLayer = null;
        if (href.indexOf("tiles.arcgis.com/tiles") > 0) {
          mapserverLayer = new ArcGISTiledMapServiceLayer(href);
        } else {
          mapserverLayer = new ArcGISDynamicMapServiceLayer(href);
        }
        this.map.addLayer(mapserverLayer);
      
      } else if (linkType == "featureserver") {
        var featureLayer = new FeatureLayer(href, {
          mode: FeatureLayer.MODE_SNAPSHOT,
          outFields: ["*"],
          infoTemplate: infoTemplate
        });

        this.map.addLayer(featureLayer);
        
      } else if (linkType == "imageserver") {
        var imageServiceLayer = new ArcGISImageServiceLayer(href);
        this.map.addLayer(imageServiceLayer);
      
      } else if (linkType == "kml") {
        var kmlLayer = new KMLLayer(href);
        this.map.addLayer(kmlLayer);
      
      } else if (linkType == "wms") {
        var wmsLayer = new WMSLayer(href);
        this.map.addLayer(wmsLayer);
      }
      
    }
    },
    
    _onFetchWebMapError: function(error) {
      console.log('_onFetchWebMapError');
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

    if(this.elPageControl){
      this.pagination.removeChild(this.elPageControl);
      this.elPageControl = null;
    }
    
    var footprints = this.map.getLayer("footprints");
    if(footprints){
     footprints.hide();
    }
    
    this.divResultMessage.textContent = this.nls.noResults;
    
    divResultMessage = this.divResultMessage;
    divResultMessage.textContent = "";
    
    theList = dojo.byId("list");
    theList.innerHTML = "";
          
    return false;
  }
  });
});