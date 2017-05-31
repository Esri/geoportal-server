define([
  'dojo/Deferred',
  'dojo/topic',
  'dojo/_base/array',
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
  'esri/InfoTemplate',
  'esri/request',
  './LayerLoader'
],function(Deferred,
          topic,array, all,
  ArcGISDynamicMapServiceLayer, ArcGISImageServiceLayer, ArcGISTiledMapServiceLayer, CSVLayer,
  FeatureLayer, GeoRSSLayer, KMLLayer, StreamLayer, VectorTileLayer, WFSLayer, WMSLayer, WMTSLayer,
  InfoTemplate,
          InfoTemplate,
          esriRequest,LayerLoader){
  return {

    _getAgsServiceType: function(url) {
      if ((/\/MapServer$/gi).test(url)) return "mapserver";
      if ((/\/ImageServer/gi).test(url)) return "imageserver";
      if ((/\/MapServer\/\d+$/gi).test(url)) return "featurelayer";

      return "";
    },

    createLayer: function(type,url) {

       var deferred = new Deferred();
       var layer = null;

       if(!type){
         type = this.getAgsServiceType(url);
       }

       switch (type) {

         case "mapserver":
            esriRequest({
              url: url,
              content: { f: "json" },
              handleAs: "json",
              callbackParamName: "callback"
           }).then(function(response){
             if (response) {
               if (response.singleFusedMapCache) {
                 layer = new ArcGISTiledMapServiceLayer(url);
               } else {
                 layer = new ArcGISDynamicMapServiceLayer(url);
               }
             }
             deferred.resolve(layer);
           },function(){
             deferred.resolve(null);
           });
           break;

         case "imageserver":
           layer = new ArcGISImageServiceLayer(url);
           deferred.resolve(layer);
           break;

         case "imageservervectorlayer":
           layer = new ArcGISImageServiceVectorLayer(url);
           deferred.resolve(layer);
           break;

         case "featureserver":
         case "featurelayer":
           var infoTemplate = new InfoTemplate("Attributes", "${*}");
           layer = new FeatureLayer(url, {
              mode: FeatureLayer.MODE_SNAPSHOT,
              outFields: [ "*" ],
              infoTemplate: infoTemplate
           });
           deferred.resolve(layer);
           break;

        case "kml":
         layer = new KMLLayer(url);
         deferred.resolve(layer);
         break;

        case "wms":
         layer = new WMSLayer(url);
         deferred.resolve(layer);
         break;

        case "wms":
         layer = new WMSLayer(url);
         deferred.resolve(layer);
         break;

        case "wmts":
         layer = new WMTSLayer(url);
         break;

        case "cvs":
         layer = new CVSLayer(url);
         deferred.resolve(layer);
         break;

        case "georss":
         layer = new GeoRSSLayer(url);
         deferred.resolve(layer);
         break;

        case "stream":
         layer = new StreamLayer(url);
         deferred.resolve(layer);
         break;

        case "raster":
         layer = new RasterLayer(url);
         deferred.resolve(layer);
         break;

         default:
           deferred.resolve(layer);
           break;
       }

       return deferred;
    },

    addLayer: function(map,dfd,type,url) {
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
    }

  }
});
