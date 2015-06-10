define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/topic',
  'esri/request',
  'widgets/GeoportalSearch/common/LayerFactory'
],function(declare,lang,
           topic,esriRequest,
           LayerFactory){
    return declare(null, {    

  map: null,

  process: function(url,map){
    if(!map || !url) {
      return;
    }
    this.map = map;
    var requestHandle = esriRequest({
          "url": url,
          handleAs:'json'
        },{
          useProxy:false
        });
    requestHandle.then(this._onFetchWebMapFinish, this._onFetchWebMapError);
  },

  _onFetchWebMapFinish: function(response) {
    console.log('_onFetchWebMapFinish');
    
    var numLayers = response.operationalLayers.length;
    var opLayers = response.operationalLayers;
    array.forEach(opLayers, function(theLayer, ii){
      var href = theLayer.url;
      var hrefLower = href.toLowerCase();
      var linkType = hrefLower.split("/").pop();
  
      if (linkType == "mapserver" || linkType == "featureserver" || linkType == "imageserver" 
        || linkType == "kml" || linkType == "wms") {        
        LayerFactory.createLayer(href,linkType).then(lang.hitch(this,function(layer){
            this.map.addLayer(layer);
        }));               
      }      
     });
    },
    
    _onFetchWebMapError: function(error) {
      console.log('_onFetchWebMapError');
    }

   });
});