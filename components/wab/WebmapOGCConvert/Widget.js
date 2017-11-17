define(['dojo/_base/declare', 'jimu/BaseWidget',
  'dojo/_base/lang',
  'dojo/promise/all',
  'esri/request'],
function(declare, BaseWidget,
  lang,
  all,
  esriRequest
  ) {
  //To create a widget, you need to derive from BaseWidget.
  return declare([BaseWidget], {
    // DemoWidget code goes here

    //please note that this property is be set by the framework when widget is loaded.
    //templateString: template,

    baseClass: 'jimu-widget-demo',

    postCreate: function() {
      this.inherited(arguments);
      console.log('postCreate');
    },

    startup: function() {
      this.inherited(arguments);
      console.log('startup');
    },

    onOpen: function(){
      console.log('onOpen');
    },

    onClose: function(){
      console.log('onClose');
    },

    onMinimize: function(){
      console.log('onMinimize');
    },

    onMaximize: function(){
      console.log('onMaximize');
    },

    onSignIn: function(credential){
      /* jshint unused:false*/
      console.log('onSignIn');
    },

    onSignOut: function(){
      console.log('onSignOut');
    },

    _onConvertToOGCClick: function(evt) {
      
      var text = "";

      var outputType = dojo.byId("outputType").value;
      var outputMediaType = "";
      var outputExtension = "";
      var jsonContext = this.toGeoJSON();

      if (outputType == "outputContextTypeGeoJSON") {
      outputMediaType = "application/vnd.geo+json";
      outputExtension = "json";

      console.log('jsonContext: ', jsonContext);

      text = this.onSaveAsGeoJSON(jsonContext);

      } else if (outputType == "outputContextTypeATOM") {
      outputMediaType = "application/atom+xml";
      outputExtension = "xml";
      text = this.onSaveAsATOM(jsonContext);

      } else if (outputType == "outputContextTypeContext") {
      outputMediaType = "application/xml";
      outputExtension = "xml";
      text = this.onSaveAsContext(jsonContext);
      }

      var a = document.getElementById("a");

      var file = new Blob([text], {type: outputMediaType});
      a.href = URL.createObjectURL(file);
      a.download = "context." + outputExtension;
      a.click();

    },

    onSaveAsGeoJSON: function(jsonContext) {
      return JSON.stringify(jsonContext);       
      
    },

    // onClick which will trigger request to get general webmap information, can be adjusted to
    // handle different item types as well
    // based on item type
    toGeoJSON: function() {

      // first, see if I can make a JSON object of the web app web map
      console.log('main map object: ', this.map);

      console.log('web map item info: ', this.map.itemInfo);

      console.log('web map item itemData: ', this.map.itemInfo.itemData);

      console.log('getting general "webmap" info: ', this.map.itemInfo.item);

      console.log('checking to add additional layers: ', this.addedLayersNode.checked);

      // Next see if I can get the featureLayers and map services.
      // Feature Collection is required for OGC standards, additional info will
      // be added to this
      /*var owsMap = {
        "type": "FeatureCollection",
        "id": "http://www.opengis.net/owc/1.0/examples/geojson/1/",
        "properties": {
          "title" : "OWS Context GeoJSON Example", // Should be a variable?
          "subtitle" : "This Context document uses WMS and GML",
          "date": "2012-11-04T00:01:23Z/2012-12-05T17:28:56Z",
          "updated" : "2012-11-04T17:26:23Z",
          "authors" : [{
           "name" : "Joe Doe",
           "email" : "jdoe@some.com",
           "uri" : "http://some.com/jdoe"
           }],
          "publisher": "Esri Inc.", // Should be okay, but should use a variable
          "creator" : "ACME CSW Server",
          "rights" : "ACME Srl, licensed under a Creative Commons Attribution 3.0 License",
          "links" : { 
            "profiles" : [
              "http://www.opengis.net/spec/owc-geojson/1.0/req/core"
            ],
            "via" : [
              { // needs to be a variable
                "href" : "http://www.acme.com/collections/algal.xml", 
                "type" : "application/xml ", 
                "title" : "Algal XML metadata", 
                "lang" : "en" 
              }
            ]
          },
          "categories" : [{
            "scheme" : "http://www.acme.com/category",
            "term" : "oceansec",
            "label" : "Ocean Integrated Maritime Security"
          }],
          "lang" : "en"
        },
        "bbox": [100.0, 0.0, 105.0, 1.0],
        "features": [] // will be made to contain "feature" objects inside array
      }*/

      var generalWebmapInfo = this.map.itemInfo.item;

      var webmapInfo = this.map.itemInfo.itemData;

      var currentDate = new Date().toISOString();

      var createdDate = new Date(generalWebmapInfo.created).toISOString();
      var updatedDate = new Date(generalWebmapInfo.modified).toISOString();

      // trimmed down, just need to think of this as a general container object
      // I'm assuming based on the feature template that all the features should
      // be able to stand on their own hopefully since they should have all relevant information
      // Also, the map will only have the data available based on the snapshot of the map
      var owsMap = {
        "type":"FeatureCollection",
        "id": generalWebmapInfo.title + ' - ' + generalWebmapInfo.id,
        "properties":{
          "title": generalWebmapInfo.title,
          "subtitle":"GeoJSON Testing",
          "date": createdDate,
          "updated": updatedDate,
          "publisher":"Esri Inc.",
          "authors" : [{
           "name" : generalWebmapInfo.owner
           }],
          "links":{
            "profiles":[
            {
              "href":"http://www.opengis.net/spec/owc-geojson/1.0/req/core",
              "title":"This file is compliant with version 1.0 of OGC Context"
            }]
          },
          "lang":"en"
        },
        "features":[]
      }

      console.log('owsMap object: ', owsMap);

      // just testing featurelayers for now
      for (i in webmapInfo.operationalLayers) {

        var operationalLayer = webmapInfo.operationalLayers[i]

        var serviceType = this.getServiceType(operationalLayer);
        console.log('serviceType: ', serviceType);
        var theOGCType = this.getOGCType(serviceType);
        console.log('theOGCType: ', theOGCType);


        // get the extent from the layer
        var minX, minY, maxX, maxY;
        if (webmapInfo.operationalLayers[i].layerObject.fullExtent) {
          minX = operationalLayer.layerObject.fullExtent.xmin;
          minY = operationalLayer.layerObject.fullExtent.ymin;
          maxX = operationalLayer.layerObject.fullExtent.xmax;
          maxY = operationalLayer.layerObject.fullExtent.ymax;
        } else {
          minX = -180.0;
          maxX = 180.0;
          minY = -90.0;
          maxY = 90.0;
        }
          
        var feature = {
        "type": "Feature",
        "id": operationalLayer.id,
        "geometry": {
          "type": "Polygon",
          "coordinates": [
            [
              [minX,minY],
              [maxX,minY],
              [maxX,maxY],
              [minX,maxY],
              [minX,minY]
            ]
          ],
          "crs": {
            "type": "name",
            "properties": {
              "name": "urn:ogc:def:crs:EPSG:8.8:4326"
            }
          }
          },
          "properties": {
            "title": operationalLayer.title,
            "abstract": operationalLayer.title,
            "updated": currentDate,
            "content": operationalLayer.title,
            "offerings": [{
              "code": theOGCType,
              "operations": [{
                "method": "GET",
                "type": "",
                "href": operationalLayer.url
              }]
            }]
          }
        };

        console.log('feature check: ', feature);
        
        owsMap["features"].push(feature);
      }

      // if the user would like to include layers from the "Add Data" widget 
      if(this.addedLayersNode.checked == true) {
        // create an array of active layers in the web map
        // this.activeLayers = this.updateActiveLayers();

        // console.log('activeLayers: ', this.activeLayers);

        console.log('including added features');

        // Getting list of feature services

        var graphicsIdLayerArray = this.map.graphicsLayerIds;
        
        //filter out those graphicsLayers that already exist as part of the webmap
        for (var d in webmapInfo.operationalLayers) {
          // remove webmap operational layers from consideration
          if (graphicsIdLayerArray.indexOf(webmapInfo.operationalLayers[d].id) > -1) {
            graphicsIdLayerArray.splice(graphicsIdLayerArray.indexOf(webmapInfo.operationalLayers[d].id), 1);
          }
          
        }
        //webmapInfo.operationalLayers

        console.log(graphicsIdLayerArray);

        var graphicsLayerArray = [];

        console.log('graphicsIdLayerArray: ', graphicsIdLayerArray);

        for (var glayer in graphicsIdLayerArray) {

          var glayerInfo = this.map.getLayer(graphicsIdLayerArray[glayer]);
          console.log('glayerInfo', glayerInfo);

          if (graphicsIdLayerArray[glayer] == 'graphicsLayer1' || glayerInfo.url == null) {
            console.log('skipping this');
          } else {
            console.log(graphicsIdLayerArray[glayer]);
            console.log(this.map.getLayer(graphicsIdLayerArray[glayer]));
            //var glayerInfo = this.map.getLayer(graphicsIdLayerArray[glayer]);

            // should only have to include the same level of info as oplayers
            var operationalLayer = glayerInfo;

            var serviceType = this.getServiceType(operationalLayer);
            console.log('serviceType: ', serviceType);
            var theOGCType = this.getOGCType(serviceType);
            console.log('theOGCType: ', theOGCType);


            // get the extent from the layer
            var minX, minY, maxX, maxY;
            if (operationalLayer.fullExtent) {
              minX = operationalLayer.fullExtent.xmin;
              minY = operationalLayer.fullExtent.ymin;
              maxX = operationalLayer.fullExtent.xmax;
              maxY = operationalLayer.fullExtent.ymax;
            } else {
              minX = -180.0;
              maxX = 180.0;
              minY = -90.0;
              maxY = 90.0;
            }
              
            var feature = {
            "type": "Feature",
            "id": operationalLayer.id,
            "geometry": {
              "type": "Polygon",
              "coordinates": [
                [
                  [minX,minY],
                  [maxX,minY],
                  [maxX,maxY],
                  [minX,maxY],
                  [minX,minY]
                ]
              ],
              "crs": {
                "type": "name",
                "properties": {
                  "name": "urn:ogc:def:crs:EPSG:8.8:4326"
                }
              }
              },
              "properties": {
                "title": operationalLayer.title,
                "abstract": operationalLayer.title,
                "updated": currentDate,
                "content": operationalLayer.title,
                "offerings": [{
                  "code": theOGCType,
                  "operations": [{
                    "method": "GET",
                    "type": "",
                    "href": operationalLayer.url
                  }]
                }]
              }
            }
            
            console.log('graphics Layer feature check: ', feature);

            owsMap["features"].push(feature);

          } // end else

        } // end for loop

        return owsMap;

      } else {

        return owsMap;
      }
      
    },

    getServiceType: function(theLayer) {

      console.log('theLayer: ', theLayer);

      var href = theLayer.url;
      var theLinkType = "";
  
    if (href) {
      // if this has an href it is a remote web service. figure out the type
      var hrefLower = href.toLowerCase();
      
      if (theLayer.type) {
        theLinkType = theLayer.type.toLowerCase();
      } else if (theLayer.layerType) {
        theLinkType = theLayer.layerType.toLowerCase();
      } else {
        
        if (hrefLower.indexOf("request=getcapabilities") !== -1) {
          if (hrefLower.indexOf("service=wms") !== -1) {
          theLinkType = "wms";
          } else if (hrefLower.indexOf("service=wmts") !== -1) {
          theLinkType = "wmts";
          } else if (hrefLower.indexOf("service=wcs") !== -1) {
          theLinkType = "wcs";
          } else if (hrefLower.indexOf("service=wfs") !== -1) {
          theLinkType = "wfs";
          } else {
          theLinkType = "unsupported"; 
          }
          
        } else if (hrefLower.indexOf("/rest/services/") !== -1) {
          theLinkType = hrefLower.split("/").pop();
          
          if (hrefLower.indexOf("?f=") > 0) {
          theLinkType = theLinkType.substr(0, theLinkType.indexOf("?f="));
          href = href.substr(0, href.indexOf("?f="));
          } else if (!isNaN(theLinkType)) {
            // this refers to a layer in the service. the service type is the previous part of the path.
            theLinkType = "featureserver";      
          }
          
        } else if (hrefLower.indexOf("/featureserver/") !== -1) {
          if (hrefLower.indexOf("koop") !== -1) {
          theLinkType = "featureserver";
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
          href = href.substr(href.indexOf("url=")+4);
          href = decodeURIComponent(href);
          theLinkType = href.split("/").pop().toLowerCase();
          
        } else if ((hrefLower.indexOf("index.jsp") !== -1) && (hrefLower.indexOf("resource=") !== -1)) {
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

    } else {
      // this is likely a featureCollection. not supported at this time
      // TO-DO: add featureCollection support.
    }

      return theLinkType;
    },

    getOGCType: function(theLinkType) {
      var theOGCType = "";

      console.log('theLinkType: ', theLinkType);
    
      switch(theLinkType) {
        case "mapserver":
      theOGCType = "http://www.esri.com/rest/services/mapserver";
      break;
        case "featureserver":
      theOGCType = "http://www.esri.com/rest/services/featureserver";
      break;
        case "arcgisfeaturelayer":
      theOGCType = "http://www.esri.com/rest/services/featureserver";
      break;
        case "feature layer":
      theOGCType = "http://www.esri.com/rest/services/featureserver";
      break;
        case "imageserver":
      theOGCType = "http://www.esri.com/rest/services/imageserver";
      break;
        case "kml":
      theOGCType = "http://www.opengis.net/spec/owc-geojson/1.0/req/kml";
      break;
        case "wcs":
      theOGCType = "http://www.opengis.net/spec/owc-geojson/1.0/req/wcs";
      break;
        case "wfs":
      theOGCType = "http://www.opengis.net/spec/owc-geojson/1.0/req/wfs";
      break;
       case "wms":
      theOGCType = "http://www.opengis.net/spec/owc-geojson/1.0/req/wms";
      break;
        case "wmts":
      theOGCType = "http://www.opengis.net/spec/owc-geojson/1.0/req/wmts";
      break;
        default:
      theOGCType = "";
      break;
      }
    
      return theOGCType
    
    },

    // will update global array of present layers in the web map.
    updateActiveLayers: function () {
      console.log('Getting all active layers from map', this.map);

      var mapLayerIdArray = this.map.layerIds;
      var mapLayerArray = []; // contain deferred array

      console.log('mapLayerIdArray: ', mapLayerIdArray);

      for (var layer in mapLayerIdArray) {
        var layerInfo = this.map.getLayer(mapLayerIdArray[layer]);
        //mapLayerArray.push(layerInfo);

        /*var mapServiceElement = esriRequest({
            url: layerInfo.url + "?f=json",
            method: 'post',
            content: {
              f: "json"
            },
            callbackParamName:'callback'
          }, {usePost: true});*/

        mapLayerArray.push(layerInfo);

      }

      console.log('Available Map Services: ', mapLayerArray);

      // Getting list of feature services
      var graphicsIdLayerArray = this.map.graphicsLayerIds;
      var graphicsLayerArray = [];

      console.log('graphicsIdLayerArray: ', graphicsIdLayerArray);

      for (var glayer in graphicsIdLayerArray) {

        if (graphicsIdLayerArray[glayer] == 'graphicsLayer1') {
          console.log('skipping this');
        } else {
          console.log(graphicsIdLayerArray[glayer]);
          console.log(this.map.getLayer(graphicsIdLayerArray[glayer]));
          var glayerInfo = this.map.getLayer(graphicsIdLayerArray[glayer]);

          /*var featureServiceElement = esriRequest({
            url: glayerInfo.url + "?f=json",
            method: 'post',
            content: {
              f: "json"
            },
            callbackParamName:'callback'
          }, {usePost: true});*/

          graphicsLayerArray.push(glayerInfo);
        }

      }

      // return layers found in the map
      var layerArray = [mapLayerArray, graphicsLayerArray];


      return layerArray;

      // use a promise to wait for JSON return

      /*var jsonPromise = all({
        mapServiceResults: mapLayerArray,
        featureServiceResults: graphicsLayerArray
      });

      jsonPromise.then(lang.hitch(this, function (results) {
        console.log("JSON Results: ", results);

        var mapServiceJSON = [];

        for (var mapResult in results.mapServiceResults) {
          console.log('mapResult: ', mapResult);
          mapServiceJSON.push(results.mapServiceResults[mapResult].results[0]);
        }

        var featureServiceJSON = [];

        for (var featureResult in results.featureServiceResults) {
          console.log('mapResult: ', featureResult);
          featureServiceJSON.push(results.featureServiceResults[featureResult].results[0]);
        }

        console.log('mapServiceJSON: ', mapServiceJSON);

        console.log('featureServiceJSON: ', featureServiceJSON);

        // Process the array of returned featureServiceJSON
        this.processFeature(featureServiceJSON);

      }));*/

    },

    // when a new feature needs to be added to the OGC webmap array, this function will process and return
    // a properly formatted feature array element
    processFeature: function (featureInfo) {

      console.log('featureInfo: ', featureInfo);
      
      // creating a feature that will return a proper OWC feature element entry
      // will be returned to a main feature array

      var owcFeatureElement = {
        "id": "http://www.acme.eu/geoserver/wms/#world_countries",
        "geometry": {
          "type" : "Polygon",
          "coordinates" : [[[-2,45],[8,45],[8,55],[-2,55],[-2,45]]]
        },
        "properties" : {
          "title": " Base World Map",
          "abstract": "ENVISAT MERIS Level 1 Reduced Resolution",
          "active": true, // make var
          "updated": "2012-05-10T14:35:00.400Z",
          "authors" : [{
            "name" : "Joe Doe",
             "email" : "jdoe@some.com",
             "uri" : "http://some.com/jdoe"
          }],
          "publisher": "ACME Project",
          "rights": "ACME Srl, licensed under a Creative Commons Attribution 3.0 License",
          "links": {
            "previews" : [{
              "href" : "http://www.acme.com/products/algal20090123090856.png",
              "type" : "image/png",
              "length" : "12321",
              "title" : " Quicklook for the entry 2009-01-23 09:08:56"
            }],
            "alternates" : [{
              "href" : "http://www.acme.com/products/algal20090123090856.html",
              "type" : "text/html",
              "title" : "Information for the entry 2009-01-23 09:08:56"
            }],
            "data" : [{
              "href" : "http://www.acme.com/products/algal20090123090856.hdf",
              "type" : "application/x-hdf5",
              "length" : "453123432",
              "title" : "HDF file for the entry 2009-01-23 09:08:56"
            }],
            "via" : {
              "href" : "http://www.acme.com/products/algal20090123090856.xml",
              "type" : "application/xml",
              "length" : "435",
              "title" : "XML metadata file for the entry 2009-01-23 09:08:56"
            }
          }, // end links
          "offerings" : [
          {
          "code" : "http://www.opengis.net/spec/owc-geojson/1.0/req/wms"
          },
          {
          "code" : "http://www.opengis.net/spec/owc-geojson/1.0/req/gml"
          }
          ],
          "categories" : [
          {
          "scheme" : "http://www.acme.com/category",
          "term" : "high_concentration",
          "label" : "High Concentration was detected on this entry"
          },{
          "scheme" : "http://www.earthobservations.org/category",
          "term" : "GEOSSDataCore",
          "label" : "GEOSS Data Collection of Open Resources for Everyone"
          }
          ],
          "minscaledenonimator" : 100.0,  // find this in data
          "maxscaledenominator" : 1000000.0, // find this in data
          // "folder" : "/root/algal", // this might be skippable
        
        } // end properties

      } // end feature element

      /*var owcFeatureElement = {
        "id": "http://www.acme.eu/geoserver/wms/#world_countries",
        "geometry": {
          "type" : "Polygon",
          "coordinates" : [[[-2,45],[8,45],[8,55],[-2,55],[-2,45]]]
        },
        "properties" : {
          "title": " Base World Map",
          "abstract": "ENVISAT MERIS Level 1 Reduced Resolution",
          "active": true, // make var
          "updated": "2012-05-10T14:35:00.400Z",
          "authors" : [{
            "name" : "Joe Doe",
             "email" : "jdoe@some.com",
             "uri" : "http://some.com/jdoe"
          }],
          "publisher": "ACME Project",
          "rights": "ACME Srl, licensed under a Creative Commons Attribution 3.0 License",
          "links": {
            "previews" : [{
              "href" : "http://www.acme.com/products/algal20090123090856.png",
              "type" : "image/png",
              "length" : "12321",
              "title" : " Quicklook for the entry 2009-01-23 09:08:56"
            }],
            "alternates" : [{
              "href" : "http://www.acme.com/products/algal20090123090856.html",
              "type" : "text/html",
              "title" : "Information for the entry 2009-01-23 09:08:56"
            }],
            "data" : [{
              "href" : "http://www.acme.com/products/algal20090123090856.hdf",
              "type" : "application/x-hdf5",
              "length" : "453123432",
              "title" : "HDF file for the entry 2009-01-23 09:08:56"
            }],
            "via" : {
              "href" : "http://www.acme.com/products/algal20090123090856.xml",
              "type" : "application/xml",
              "length" : "435",
              "title" : "XML metadata file for the entry 2009-01-23 09:08:56"
            }
          }, // end links
          "offerings" : [
          {
          "code" : "http://www.opengis.net/spec/owc-geojson/1.0/req/wms"
          },
          {
          "code" : "http://www.opengis.net/spec/owc-geojson/1.0/req/gml"
          }
          ],
          "categories" : [
          {
          "scheme" : "http://www.acme.com/category",
          "term" : "high_concentration",
          "label" : "High Concentration was detected on this entry"
          },{
          "scheme" : "http://www.earthobservations.org/category",
          "term" : "GEOSSDataCore",
          "label" : "GEOSS Data Collection of Open Resources for Everyone"
          }
          ],
          "minscaledenonimator" : 100.0,  // find this in data
          "maxscaledenominator" : 1000000.0, // find this in data
          // "folder" : "/root/algal", // this might be skippable
        
        } // end properties

      } // end feature element
      */

      return owcFeatureElement;
    
    },

    _processWebMap: function(){
      console.log('Trigger conversion of Web Map to OGC', this.map);

      // Need to determine what service we are interacting with
      // for now assume that it is a featurecoollection

      // Step 1: Get webmap id / service info for current web map (added layers, extent, etc.)
      console.log(this.map.graphicsLayerIds);

      var graphicsLayerArray = this.map.graphicsLayerIds;

      for (var layer in graphicsLayerArray ) {
        if (graphicsLayerArray[layer] == 'graphicsLayer1') {
          console.log('skipping this');
        } else {

          var graphicsLayer = this.map.getLayer(graphicsLayerArray[layer]);
          console.log(graphicsLayer);

          console.log('item ID:', graphicsLayer.xtnItemId);

          var id = graphicsLayer.xtnItemId;

          var webmapUrl = 'http://www.arcgis.com/sharing/rest/content/items/' + id + "/data";

          esriRequest({
            url: webmapUrl,
            method: 'post',
            content: {
              f: "pjson"
            },
            callbackParamName:'callback'
          }, {usePost: true}).then(lang.hitch(this, function(results) {
            console.log('testing results: ', results);

            // this.getOwsFromWebmap(results);

          }));

        }

      }

      // Step 2: Format the data into OGC format

        //It would make sense for it to be able to also output GeoJSON etc. 

      // Step 3: Provide an output for the user in OGC format so as WMS or WFS for instance

    }

  });
});