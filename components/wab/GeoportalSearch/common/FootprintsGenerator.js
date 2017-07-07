define([
  'dojo/_base/declare',
  'dojo/_base/lang',
  'dojo/_base/array',
  'dojo/topic',
  'esri/SpatialReference', 
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
],function(declare,lang,array,
           topic, SpatialReference, 
           Graphic, Point, Extent, SimpleMarkerSymbol, PictureMarkerSymbol, 
           Polyline, SimpleLineSymbol, Polygon, SimpleFillSymbol, InfoTemplate, 
           jsonUtils, webMercatorUtils){
    return declare(null, {    

    addFootprints:function(footprintsLayer, features){
              
      var symbol = new SimpleFillSymbol();
      symbol.setColor(new esri.Color([0,0,0,0.05]));

      array.forEach(features, function(feature, i){
        if (!feature.geometry) return;
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
            geometry = new Polygon([[bbox[0],bbox[1]],[bbox[2],bbox[1]],[bbox[2],bbox[3]],[bbox[0],bbox[3]],[bbox[0],bbox[1]]]);
            geometry.setSpatialReference(new SpatialReference(4326));            
            centerpoint = geometry.getCentroid();
           }
            break;
          default:
            break;
        }

        var title = feature.title;
        var content = feature.summary;
        var it = new InfoTemplate(title, title + "<br>" + content);
        var graphic = new Graphic(geometry,symbol,feature,it);
        footprintsLayer.add(graphic);
      });
    }

   });
});