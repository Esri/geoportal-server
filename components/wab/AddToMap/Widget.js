define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/_base/array',
  'dojo/on',
  'dijit/_WidgetsInTemplateMixin',
  'jimu/BaseWidget',
  'dojo/topic',
  "dojo/Deferred",
  "dojo/promise/all",
  "esri/layers/ArcGISDynamicMapServiceLayer",
  "esri/layers/ArcGISImageServiceLayer",
  "esri/layers/ArcGISTiledMapServiceLayer",
  "esri/layers/CSVLayer",
  "esri/layers/FeatureLayer",
  "esri/layers/GeoRSSLayer",
  "esri/layers/KMLLayer",
  "esri/layers/StreamLayer",
  "esri/layers/VectorTileLayer",
  "esri/layers/WFSLayer",
  "esri/layers/WMSLayer",
  "esri/layers/WMTSLayer",
  "esri/InfoTemplate",
  'jimu/dijit/Message',
  './LayerLoader'],
function(declare, lang, array, on, _WidgetsInTemplateMixin, BaseWidget, topic,Deferred, all,
  ArcGISDynamicMapServiceLayer, ArcGISImageServiceLayer, ArcGISTiledMapServiceLayer, CSVLayer,
  FeatureLayer, GeoRSSLayer, KMLLayer, StreamLayer, VectorTileLayer, WFSLayer, WMSLayer, WMTSLayer,
  InfoTemplate, Message, LayerLoader) {

  return declare([BaseWidget, _WidgetsInTemplateMixin], {

    name: 'AddToMap',
    baseClass: 'geoportal-addToMap',

    postCreate: function(){
      this.inherited(arguments);
      topic.subscribe("mapLoaded", lang.hitch(this, this._checkWindowUrl));
      this._checkWindowUrl();
    },

	 _handleAdd: function(dfd,type,url) {
      var map = this.map;
      url = this._checkMixedContent(url);
      var lc = url.toLowerCase();
      var loader = new LayerLoader();
      var id = loader._generateLayerId();
      var self = this, layer = null;

      if (type === "ArcGIS" || type === "ags") {
        if (lc.indexOf("/featureserver") > 0 || lc.indexOf("/mapserver") > 0) {
          loader._readRestInfo(url).then(function(info){
            //console.warn("restInfo",info);
            if (info && typeof info.type === "string" && info.type === "Feature Layer") {
              layer = new FeatureLayer(url,{
                id: id,
                outFields: ["*"],
                infoTemplate: new InfoTemplate()
              });
              self._waitThenAdd(dfd,map,type,loader,layer);
            } else {

              if (lc.indexOf("/featureserver") > 0) {
                var dfds = [];
                array.forEach(info.layers,function(li){
                  var lyr = new FeatureLayer(url+"/"+li.id,{
                    id: loader._generateLayerId(),
                    outFields: ["*"],
                    infoTemplate: new InfoTemplate()
                  });
                  dfds.push(loader._waitForLayer(lyr));
                });
                all(dfds).then(function(results){
                  var lyrs = [];
                  array.forEach(results,function(lyr){lyrs.push(lyr);});
                  lyrs.reverse();
                  array.forEach(lyrs,function(lyr){map.addLayer(lyr);});
                  dfd.resolve(lyrs);
                }).otherwise(function(error){
                  dfd.reject(error);
                });

              } else if (lc.indexOf("/mapserver") > 0) {
                if (info.tileInfo) {
                  layer = new ArcGISTiledMapServiceLayer(url,{id:id});
                } else {
                  layer = new ArcGISDynamicMapServiceLayer(url,{id:id});
                }
                self._waitThenAdd(dfd,map,type,loader,layer);
              }
            }
          }).otherwise(function(error){
            dfd.reject(error);
          });

        } else if (lc.indexOf("/imageserver") > 0) {
          layer = new ArcGISImageServiceLayer(url,{id:id});
          this._waitThenAdd(dfd,map,type,loader,layer);

        } else if (lc.indexOf("/vectortileserver") > 0 || lc.indexOf("/resources/styles/root.json") > 0) {
          if (!VectorTileLayer || !VectorTileLayer.supported()) {
            dfd.reject("Unsupported");
          } else {
            loader._checkVectorTileUrl(url,{}).then(function(vturl){
              //console.warn("vectorTileUrl",vturl);
              layer = new VectorTileLayer(vturl,{id:id});
              self._waitThenAdd(dfd,map,type,loader,layer);
            }).otherwise(function(error){
              dfd.reject(error);
            });
          }
        } else if (lc.indexOf("/streamserver") > 0) {
          layer = new StreamLayer(url, {
            id: id,
            purgeOptions: {displayCount: 10000},
            infoTemplate: new InfoTemplate()
          });
          this._waitThenAdd(dfd,map,type,loader,layer);

        } else {
          dfd.reject("Unsupported");
        }
      } else if (type.toUpperCase() === "WMS") {
        layer = new WMSLayer(url,{id:id});
        this._waitThenAdd(dfd,map,type,loader,layer);
      } else if (type.toUpperCase() === "WMTS") {
        layer = new WMTSLayer(url,{id:id});
      } else if (type.toUpperCase() === "WFS") {
        layer = new WFSLayer({id:id,url:url,infoTemplate:new InfoTemplate()});
        this._waitThenAdd(dfd,map,type,loader,layer);
        console.warn("WFSLayer",layer);
      } else if (type.toUpperCase() === "KML") {
        layer = new KMLLayer(url,{id:id});
        this._waitThenAdd(dfd,map,type,loader,layer);
      } else if (type === "GeoRSS") {
        layer = new GeoRSSLayer(url,{id:id});
        this._waitThenAdd(dfd,map,type,loader,layer);
      } else if (type.toUpperCase() === "CSV") {
        layer = new CSVLayer(url,{id:id});
        layer.setInfoTemplate(loader._newInfoTemplate());
        this._waitThenAdd(dfd,map,type,loader,layer);
      }
    },

    _waitThenAdd: function(dfd,map,type,loader,layer) {
      //console.warn("_waitThenAdd",type,layer);
      loader._waitForLayer(layer).then(function(lyr){
        //console.warn("_waitThenAdd.ok",lyr);
        var templates = null;
        if (type.toUpperCase() === "WMS") {
          loader._setWMSVisibleLayers(lyr);
        } else if (lyr &&
          (lyr.declaredClass === "esri.layers.ArcGISDynamicMapServiceLayer" ||
           lyr.declaredClass === "esri.layers.ArcGISTiledMapServiceLayer")) {
          if (lyr.infoTemplates === null) {
            array.forEach(lyr.layerInfos,function(lInfo){
              if (templates === null) templates = {};
              templates[lInfo.id] = {infoTemplate: new InfoTemplate()};
            });
            if (templates) lyr.infoTemplates = templates;
          }
        }
        map.addLayer(lyr);
        dfd.resolve(lyr);
      }).otherwise(function(error){
        //console.warn("_waitThenAdd.error",error);
        dfd.reject(error);
      });
    },

    _checkMixedContent: function(uri) {
      if ((typeof window.location.href === "string") && (window.location.href.indexOf("https://") === 0)) {
        if ((typeof uri === "string") && (uri.indexOf("http://") === 0)) {
          uri = "https:"+uri.substring("5");
        }
      }
      return uri;
    },


    _addLayer: function(linkType,href){
      //console.warn("AddToMap._addLayer...",linkType,href);
      linkType = linkType.toLowerCase();
      if (linkType == "mapserver" || linkType == "featureserver" || linkType == "imageserver" ||
          linkType == "kml" || linkType == "wms") {

        LayerFactory.createLayer(href,linkType).then(lang.hitch(this,function(layer){
          layer.on("error",lang.hitch(this,function(error){
            //new Message({message: "Unable to load: "+href});
            console.warn(error);
          }));
          layer.on("load",lang.hitch(this,function(){
            //console.warn("onLoad",layer);
            /*if(title.length > 0){
            layer.attr("id",title);
            layer.attr("name",title);
            layer.attr("title",title);
            }*/
            //console.warn("AddToMap._addLayer",layer);
            if (layer && layer.declaredClass && layer.declaredClass === "esri.layers.WMSLayer") {
              //console.warn(layer.declaredClass);
              //console.warn(layer.layerInfos);
              var maxLayers = 10, lyrNames = [];
              array.forEach(layer.layerInfos,function(lyrInfo){
                //console.warn("lyrInfo",lyrInfo);
                if (typeof lyrInfo.name === "string" && lyrInfo.name.length > 0) {
                  if (lyrNames.length < maxLayers) lyrNames.push(lyrInfo.name);
                }
              });
              //console.warn("lyrNames",lyrNames);
              if (lyrNames.length <= maxLayers) {
                layer.setVisibleLayers(lyrNames);
              }
            }
            console.warn("AddToMap._addLayer",layer);
            this.map.addLayer(layer);
            //console.log("layer added to map.");
          }));
        }));

      } else if (linkType == "agsrest" || linkType == "ags") {

        var linksProcessor = new LinksProcessor();
        linkType = linksProcessor.getServiceType(href);

        LayerFactory.createLayer(href,linkType).then(lang.hitch(this,function(layer){
          this.map.addLayer(layer);
          //console.log("layer added to map.");
        }));

      } else if (linkType == "webmap") {
        //console.log("webmap processing...");
        var wmProcessor = new WebMapProcessor();
        wmProcessor.process(href,this.map);
        //console.log("webmap operational layers added to map.");
      }
      //console.groupEnd();
    },

    _checkWindowUrl: function(){
      //console.warn("AddToMap._checkWindowUrl...");
      var queryObject = this._parseParameters();  // window.queryObject; env.js // <-- did not work well, so using above function
      if(queryObject.resource){
        var resource = queryObject.resource;
        //console.warn("Add to map parameters => " + resource);
        var title = "";
        if(queryObject.title){
          title = decodeURIComponent(queryObject.title);
        }
        var parts = resource.split(":");
        if(!parts && parts.length<2){
          return;
        }

        var linkType = parts[0]; // .toLowerCase();
        var href = "";
        // loop parameter values in array elements since value may contain ':'
        for(var i=1; i<parts.length; i++){
          if(href.length > 0){
            href += ":";
          }
          href += parts[i];
        }
        if (href.length === 0)return;

        var dfd = new Deferred();
        this._handleAdd(dfd,linkType,href);
        dfd.then(function(result){
         if (result) {

          } else {

          }
        }).otherwise(function(error){

        });
      }
    },

    _parseParameters: function(){
      var query = window.location.search;
      if (query.indexOf('?') > -1) {
        query = query.substr(1);
      }
      var pairs = query.split('&');
      var queryObject = {};
      for(var i = 0; i < pairs.length; i++){
        var splits = decodeURIComponent(pairs[i]).split('=');
        var parameterValue = "";
        // loop parameter values in array elements since value may contain '='
        for(j=1; j<splits.length; j++){
          if(parameterValue.length > 0){
            parameterValue += "=";
          }
          parameterValue += splits[j];
        }
        queryObject[splits[0]] = parameterValue;
      }
      return queryObject;
    }

  });
});
