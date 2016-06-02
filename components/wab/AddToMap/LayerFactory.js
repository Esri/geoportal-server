define([
  'dojo/Deferred',
  'esri/layers/ArcGISDynamicMapServiceLayer',
  'esri/layers/ArcGISTiledMapServiceLayer',
  "esri/layers/ArcGISImageServiceVectorLayer",
  'esri/layers/ArcGISImageServiceLayer',
  'esri/layers/FeatureLayer',
  'esri/layers/KMLLayer',
  'esri/layers/WMSLayer',
  "esri/layers/WMTSLayer",
  "esri/layers/CSVLayer",
  "esri/layers/GeoRSSLayer",
  "esri/layers/RasterLayer",
  "esri/layers/StreamLayer",
  'esri/InfoTemplate',
  'esri/request'
],function(Deferred,
          ArcGISDynamicMapServiceLayer,
          ArcGISTiledMapServiceLayer,
          ArcGISImageServiceVectorLayer,
          ArcGISImageServiceLayer,
          FeatureLayer,
          KMLLayer,
          WMSLayer,
          WMTSLayer, 
          CSVLayer,
          GeoRSSLayer,
          RasterLayer,
          StreamLayer,
          InfoTemplate, 
          esriRequest){
  return {

    _getAgsServiceType: function(url) {
      if ((/\/MapServer$/gi).test(url)) return "mapserver";
      if ((/\/ImageServer/gi).test(url)) return "imageserver";
      if ((/\/MapServer\/\d+$/gi).test(url)) return "featurelayer";

      return "";
    },
    
    createLayer: function(url,type) {
       
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
    }
    
  }
});