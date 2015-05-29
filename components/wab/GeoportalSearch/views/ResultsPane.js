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
  'widgets/GeoportalSearch/views/RecordInfo',
  'widgets/GeoportalSearch/common/Util',
  'widgets/GeoportalSearch/common/List',
  'dojo/topic',  
  'dijit/ProgressBar',
    'dijit/form/TextBox',
  'dijit/form/RadioButton',
    'dijit/form/CheckBox',
  'dijit/form/Select',
    'dojo/store/Memory',
    'dojo/data/ObjectStore'
],function(declare,lang,array,domConstruct,domClass,html,_WidgetBase,_TemplatedMixin,_WidgetsInTemplateMixin, template, nls, 
           RecordInfo, util,List,topic){
  return declare([_WidgetBase, _TemplatedMixin, _WidgetsInTemplateMixin],{
    
    baseClass: 'geoportal-results-view',
    templateString: template,
    
    nls: nls,
    list: null,
    
     postCreate: function() {
       this.inherited(arguments);
        this._initList();
        this.attachTopics();
       // this.own(util.subscribeRecords(lang.hitch(this,this.handleRecords)));
     },
     
    /* handleRecords: function(records) {
       console.log("Records received:", records);
       if (records==null) {
         domConstruct.empty(this.domNode);
       } else {
         array.forEach(records,lang.hitch(this,this.appendRecord));
       }
     },*/

     _initList: function() {
      console.log('onSignOut');
      this.list = new List();
      this.list.startup();
  },
     
    /* appendRecord: function(record) {
       var recordInfo = new RecordInfo({record: record});
       recordInfo.startup();
       domConstruct.place(recordInfo.domNode,this.domNode);
       var total = this.domNode.childNodes.length;
       domClass.add(recordInfo.domNode,total%2==0? "even": "odd");
     },*/

     attachTopics: function(){

      this.own(topic.subscribe("/widgets/GeoportalSearch/action/search", lang.hitch(this, function (sender, args) { 
        if(args.success){       
          this._onQueryFinish(args.results, args.io);
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

  _onQueryFinish: function(results, io) {
    var progressBar = this.progressBar;
    var divResult = this.divResult;
    html.setStyle(this.progressBar,'display','none');
    html.setStyle(this.divResult,'display','block');
    if(this.resultLayer){
     this.resultLayer.clear();
    }

    var title = "";
    var titlefield = this.inputQueryCatalog.value; //this.catalogs.value;
    
    var len = results.records.length;
    var divResultMessage = this.divResultMessage;
    if (len === 0) {
      divResultMessage.textContent = "No results"; // this.nls.noResults;  
      return;
    } else {
      divResultMessage.textContent = "Features found: " + results.totalResults; //this.nls.featuresSelected + results.records.length;
    }

    theList = this.list;
    theList.innerHTML = "";
    var suffixes = ["csv", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "gml", "pdf", "zip", "xml", "html", "htm", "aspx", "lyr"];
    var suffixesKML = [".kml","kmz"];
    
    
    for (var i = 0; i < len; i++) {
      var theListContent = "";
      var record = results.records[i];
      var label = "",content = "";
      label = record.title;        
      title = record.title;           
      content = record.summary;
      var metadataLinkSnippet = "";
      var mapserviceLinkSnippet = "";
      var theLinkType = "";
          
      for (var j=0; j < record.links.length; j++) {
        
        var theLink = record.links[j];
        
        if ((theLink.type == "open") || (theLink.type == "customLink") || (theLink.type == "agslyr")  || (theLink.type == "addToMap")) {

          // if a link type has already been established other than www
          if (theLinkType.length > 0 && theLinkType != "www") continue;
                
          var href = theLink.href;
          var hrefLower = href.toLowerCase();
          
          // if the link ends in any of the suffixes, it's not a map service, but general web link
          // if not assigned value yet, check for typical file types
          if ((theLinkType.length == 0) || (theLinkType === "www")){
            for (k=0; k<suffixes.length; k++) {
              var suffix = suffixes[i];
              //if (hrefLower.indexOf(suffix, hrefLower.length - suffix.length) !== -1) {
              if (hrefLower.indexOf(suffix) + suffix.length == hrefLower.length) {
                theLinkType = "www";
                break;
              }
            }
          }

          // if not assigned value yet, check for KML/KMZ
          if ((theLinkType.length == 0) || (theLinkType === "www")) {
            for (k=0; k<suffixesKML.length; k++) {
              var suffix = suffixesKML[k];
              if (hrefLower.indexOf(suffix, hrefLower.length - suffix.length) !== -1) {
                theLinkType = "kml";
                break;
              }
            }
          }
          
          // if not assigned value yet, check for services
          if ((theLinkType.length == 0) || (theLinkType === "www")) {
            if (hrefLower.indexOf("request=getcapabilities") !== -1) {
              if (hrefLower.indexOf("service=wms") !== -1) {
                theLinkType = "wms";
              } else {
                theLinkType = "unsupported";
              }
              
            } else if (hrefLower.indexOf("/rest/services/") !== -1) {
              theLinkType = hrefLower.split("/").pop();
              
              if (hrefLower.indexOf("?f=") > 0) {
                theLinkType = theLinkType.substr(0, theLinkType.indexOf("?f="));
                href = href.substr(0, href.indexOf("?f="));
              }
              
            } else if (hrefLower.indexOf("/services/") !== -1) {
              if (hrefLower.indexOf("/mapserver/wmsserver") !== -1) {
                theLinkType = "wms";
              }
              
            } else if (hrefLower.indexOf("/com.esri.wms.esrimap") !== -1) {
              theLinkType = "wms";
              if (hrefLower.indexOf("?") > 0) {
                href = href.substr(0, href.indexOf("?"));
              }
              
            } else if ((hrefLower.indexOf("viewer.html") !== -1) && (hrefLower.indexOf("url=") !== -1)) {
              // http:\/\/hogeweg.esri.com\/arcgis\/home\/webmap\/viewer.html?&url=https%3A%2F%2Fservices.arcgis.com%2F1Teogo5Do835pBNl%2Farcgis%2Frest%2Fservices%2FWFS_Licenses_TONL_Homepage_ALL_02%2FFeatureServer
              href = href.substr(href.indexOf("url=")+4);
              href = decodeURIComponent(href);
              theLinkType = href.split("/").pop().toLowerCase();
              
            } else if ((hrefLower.indexOf("/sharing/content/items/") !== -1) && (hrefLower.split("/").pop() == "data")) {
              theLinkType = "webmap";
              if (hrefLower.indexOf("?") > 0) {
                href = href.substr(0, href.indexOf("?"));
              }
            }
          }

          // if not assigned value yet, check if the layer ends with f=lyr cause then we can make a rest URL of it
          if ((theLinkType.length == 0) || (theLinkType === "www")) {
            suffix = "?f=lyr";
            if (hrefLower.indexOf(suffix) + suffix.length == hrefLower.length) {
              theLinkType = hrefLower.split("/").pop();
              href = href.replace(suffix, "");
              break;
            }
          }
          
          // if all else fails, just make it a generic web link
          if (theLinkType.length == 0) {
            theLinkType = "www";
          }
          
          mapserviceLinkSnippet = "<input id='" + record.id + "_href' type='hidden' data-linktype='" + theLinkType + "' value='" + href + "'/>";
        } else if (theLink.type == "metadata") {
          metadataLinkSnippet = "<a id='" + record.id + "_metadata' href='" + theLink.href + "' target='_blank'>Metadata</a>";
        }
      }
      
      var imgURL = "";
      switch(theLinkType) {
      case "www":
          imgURL = "widgets/GeoportalSearch/images/ContentType_clearinghouse.png";
          break;
      case "webmap":
          imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
          break;
      case "mapserver":
          imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
          break;
      case "featureserver":
          imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
          break;
      case "imageserver":
          imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
          break;
      case "wms":
          imgURL = "widgets/GeoportalSearch/images/ContentType_liveData.png";
          break;
      case "kml":
          imgURL = "widgets/GeoportalSearch/images/ContentType_geographicActivities.png";
          break;
      default:
          imgURL = "widgets/GeoportalSearch/images/ContentType_unknown.png";
      } 
      var imgSnippet = "<img src='" + imgURL + "'/>";
      
      theListContent += "<div class='snippet'>";
      theListContent += "<div id='" + record.id + "' class='title' onmouseenter='this.list'>" + imgSnippet + record.title + "</div>";
      theListContent += "<div class='abstract'>" + record.summary + "</div>";
      theListContent += "<div class='links'>";
      theListContent += mapserviceLinkSnippet;
      theListContent += metadataLinkSnippet;
      theListContent += "</div>";
      theListContent += "</div>";
      theList.innerHTML += theListContent;
    }

    var features = results.records;
    var symbol = new SimpleFillSymbol();
    symbol.setColor(new esri.Color([0,0,0,0.05]));
    
    var footprints;
    if (this.footprints) {
      this.footprints = this._viewerMap.getLayer("footprints");
      
    } else {
      this.resultLayer = new GraphicsLayer();
      this.resultLayer.id = "footprints";
      this._viewerMap.addLayer(this.resultLayer);
      this.footprints = this._viewerMap.getLayer("footprints");
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
      //this.resultLayer.add(graphic);
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

  _drawResults: function(results) {
    var symbol;
    if(this.config.symbol){
      if(this.config.symbol.url){
        this.config.symbol.url = this.folderUrl + this.config.symbol.url;
      }
      symbol = jsonUtils.fromJson(this.config.symbol);
    }
    var features = results.records;
    for (var i = 0, len = features.length; i < len; i++) {
      var feature = features[i];
      var listItem = this.list.items[i];
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
        if(!symbol){
          symbol = new SimpleFillSymbol();
        }
        centerpoint = geometry.getCenter();
       }
        break;
      default:
        break;
      }

      if(this.resultLayer.renderer){
        symbol = null;
      }
      var title = listItem.title;
      var content = listItem.content;
      var it = new InfoTemplate(title, title + "<br>" + content);
      var graphic = new Graphic(geometry,symbol,feature,it);
      listItem.centerpoint = centerpoint;
      listItem.graphic = graphic;          
      this.resultLayer.add(graphic);
    }
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
        this._viewerMap.addLayer(mapserverLayer);
      
      } else if (linkType == "featureserver") {
        var featureLayer = new FeatureLayer(href, {
          mode: FeatureLayer.MODE_SNAPSHOT,
          outFields: ["*"],
          infoTemplate: infoTemplate
        });

        this._viewerMap.addLayer(featureLayer);
        
      } else if (linkType == "imageserver") {
        var imageServiceLayer = new ArcGISImageServiceLayer(href);
        this._viewerMap.addLayer(imageServiceLayer);
      
      } else if (linkType == "kml") {
        var kmlLayer = new KMLLayer(href);
        this._viewerMap.addLayer(kmlLayer);
      
      } else if (linkType == "wms") {
        var wmsLayer = new WMSLayer(href);
        this._viewerMap.addLayer(wmsLayer);
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
    
    var footprints = this.map.getLayer("footprints");
    footprints.hide();
    
    this.divResultMessage.textContent = this.nls.noResults;
    
    divResultMessage = this.divResultMessage;
    divResultMessage.textContent = "";
    theList = this.list;
    theList.innerHTML = "";
          
    return false;
  }
  });
});