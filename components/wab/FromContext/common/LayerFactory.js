define([
  'dojo/Deferred',
  'esri/layers/ArcGISDynamicMapServiceLayer',
  'esri/layers/ArcGISTiledMapServiceLayer',
  "esri/layers/ArcGISImageServiceVectorLayer",
  'esri/layers/ArcGISImageServiceLayer',
  'esri/layers/FeatureLayer',
  'esri/layers/KMLLayer',
  'esri/layers/WFSLayer',
  'esri/layers/WMSLayer',
  "esri/layers/WMTSLayer",
  "esri/layers/WMTSLayerInfo",
  "esri/layers/CSVLayer",
  "esri/layers/GeoRSSLayer",
  "esri/layers/RasterLayer",
  "esri/layers/StreamLayer",
  'esri/InfoTemplate',
  'esri/request',
  'esri/request',
  "esri/layers/WCSLayer",
  "esri/layers/ImageServiceParameters"
],function(Deferred,
          ArcGISDynamicMapServiceLayer,
          ArcGISTiledMapServiceLayer,
          ArcGISImageServiceVectorLayer,
          ArcGISImageServiceLayer,
          FeatureLayer,
          KMLLayer,
		  WFSLayer,
          WMSLayer,
          WMTSLayer,
		  WMTSLayerInfo,
          CSVLayer,
          GeoRSSLayer,
          RasterLayer,
          StreamLayer,
          InfoTemplate, 
          esriRequest,
		  WCSLayer, ImageServiceParameters){
  return {

    _getAgsServiceType: function(url) {
      if ((/\/MapServer$/gi).test(url)) return "mapserver";
      if ((/\/ImageServer/gi).test(url)) return "imageserver";
      if ((/\/MapServer\/\d+$/gi).test(url)) return "featurelayer";

      return "";
    },
    
    createLayer: function(url,type,options) {
       
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
		 
		case "wfs":
			var urlElements = url.split("?");
			var wfsUrl = urlElements[0];
			var wfsLayerInfo = urlElements[1].split("wfsLayerInfo=")[1];
			var wfsLayerName = wfsLayerInfo.split("|")[0];
			wfsLayerName = wfsLayerName.split(":").pop(); // ignore namespace
			
			var template = new InfoTemplate("${type}", "${*}");
						
			// specify url and certain layer
			// "http://localhost:6080/arcgis/services/SampleWorldCities/MapServer/WFSServer&wfsLayerInfo=cities",
			var options = {
				"url" : wfsUrl,
				"name" : wfsLayerName, 
				"wkid" : 3857,
				"maxFeatures" : 100,
				"infoTemplate" : template,
				"showDetails" : true
			};
			
			wfsLayer = new WFSLayer();
			wfsLayer.fromJson(options);
			
			deferred.resolve(wfsLayer);
			break;

        case "wcs":
			var n = url.indexOf('?');
			var theLink = url;
			
			if (n > 0) {
				theLink = url.substring(0,n);
			}
			
			var wcsLayerInfo = url.split("wcsLayerInfo=")[1];
			var coverageId = wcsLayerInfo.split("|")[0];
			var format = wcsLayerInfo.split("|")[1];
			var version = url.split("version=")[1].split("&")[0];
			
			var datastore = url.split("DATASTORE=");
			if (datastore.length > 1) {
				datastore = datastore[1].split("&")[0];
				theLink = theLink + "?DATASTORE=" + datastore;
			}
			
			layer = new WCSLayer(theLink, {
				opacity: 1,
				version: version,
				//pixelFilter: maskPixels,
				coverageId: coverageId,
				format: format
				//imageServiceParameters: isparams
			});
			deferred.resolve(layer);
			break;

        case "wms":
			layer = new WMSLayer(url);
			deferred.resolve(layer);
			break;

        case "wmts":
			var n = url.indexOf('?');
			var theLink = url;
			
			if (n > 0) {
				theLink = url.substring(0,n);
			}
			
			var wmtsLayerInfo = url.split("wmtsLayerInfo=")[1];
			var wmtsIdentifier = wmtsLayerInfo.split("|")[0];
			var wmtsTileMatrixSet = wmtsLayerInfo.split("|")[1];
			var wmtsImageFormat = wmtsLayerInfo.split("|")[2];
			var wmtsCRS = wmtsLayerInfo.split("|")[3];
			
			var layerInfo = new WMTSLayerInfo({
				identifier: wmtsIdentifier,
				tileMatrixSet: wmtsTileMatrixSet,
				format: wmtsImageFormat,
				spatialReference: {	"wkid" : wmtsCRS }
			});
			//	tileMatrixSet: "default028mm",

			var options = {
				serviceMode: "KVP",
				layerInfo: layerInfo
			};
			
			layer = new WMTSLayer(theLink, options);
			deferred.resolve(layer);
         	break;

        case "csv":
         layer = new CSVLayer(url);
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