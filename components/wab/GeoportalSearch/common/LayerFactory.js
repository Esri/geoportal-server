define([
  'dojo/Deferred',
  'esri/layers/ArcGISDynamicMapServiceLayer',
  'esri/layers/ArcGISTiledMapServiceLayer',
  'esri/layers/ArcGISImageServiceLayer',
  'esri/layers/FeatureLayer',
  'esri/request'
],function(Deferred,ArcGISDynamicMapServiceLayer,ArcGISTiledMapServiceLayer,ArcGISImageServiceLayer,FeatureLayer,esriRequest){
  return {
    getAgsServiceType: function(url) {
      if ((/\/MapServer$/gi).test(url)) return "MapServer";
      if ((/\/ImageServer/gi).test(url)) return "ImageServer";
      if ((/\/MapServer\/\d+$/gi).test(url)) return "FeatureLayer";
      return "";
    },
    
    createLayer: function(url) {
       var deferred = new Deferred();
       var layer = null;
       var type = this.getAgsServiceType(url);
       switch (type) {
         case "MapServer":
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
         case "ImageServer":
           layer = new ArcGISImageServiceLayer(url);
           deferred.resolve(layer);
           break;
         case "FeatureLayer":
           layer = new FeatureLayer(url, {
              mode: FeatureLayer.MODE_SNAPSHOT,
              outFields: [ "*" ]
           });
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