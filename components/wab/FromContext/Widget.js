define(['dojo/_base/declare', 'jimu/BaseWidget',
	'jimu/dijit/TabContainer',
    'jimu/utils',
	"dojo/dom", "dojo/on", "dojo/dom-class", "dojo/_base/json",
    'dojo/dom-construct',
	'dojo/_base/lang',
	'dojo/_base/window',
	'dojo/sniff',
	'dojox/xml/parser',
	'dojox/xml/DomParser',
	'esri/request',
	'dijit/form/Select',
    'widgets/FromContext/common/LayerFactory',
	'widgets/FromContext/common/LinksProcessor',
	'widgets/FromContext/common/List',
	'widgets/FromContext/views/OpenPane',
	'widgets/FromContext/views/SaveAsPane',
	'dijit/form/RadioButton'
],
	function(declare, BaseWidget,
		TabContainer, utils, dom, on, domClass, dojoJson, domConstruct, lang, win, sniff, parser, domParser,
		esriRequest,
		Select, LayerFactory, LinksProcessor, List) {

		return declare([BaseWidget], {

			baseClass: 'ogc-context-widget',
			name: 'OGC Context',
			tabContainer: null,
			activeLayers: [],

			//methods to communication with app container:

			postCreate: function() {
				this.inherited(arguments);
			},

			startup: function() {
				this.inherited(arguments);

				this.features = [];
   			},

			onSaveAs: function(evt) {
				var text = "";

				var outputType = dojo.byId("outputType").value;
				var outputMediaType = "";
				var outputExtension = "";
				var jsonContext = this.toGeoJSON();

				if (outputType == "outputContextTypeGeoJSON") {
					outputMediaType = "application/vnd.geo+json";
					outputExtension = "json";
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

			toGeoJSON: function() {
				var dDate = new Date();
				var sDate = dDate.toISOString();

				var jsonContext = {
					"type": "FeatureCollection",
					"id": "http://www.opengis.net/owc/1.0/examples/geojson/1",
					"properties": {
						"title": "Sample GeoJSON",
						"subtitle": "UC 2016",
						"updated": sDate,
						"publisher": "Esri Inc.",
						"links": {
							"profiles": [{
								"href": "http://www.opengis.net/spec/owc-geojson/1.0/req/core",
								"title": "This file is compliant with version 1.0 of OGC Context"
							}]
						},
						"lang": "en"
					},
					"features": []
				};

				var coordinates = [[
					[this.map.extent.xmin,this.map.extent.ymin],
					[this.map.extent.xmax,this.map.extent.ymin],
					[this.map.extent.xmax,this.map.extent.ymax],
					[this.map.extent.xmin,this.map.extent.ymax],
					[this.map.extent.xmin,this.map.extent.ymin]
				]];

				var featureTemplate = {
					"type": "Feature",
					"id": "",
					"geometry": {
						"type": "Polygon",
						"coordinates": coordinates,
						"crs": {
							"type": "name",
							"properties": {
								"name": "urn:ogc:def:crs:EPSG:8.8:4326"
							}
						}
					},
					"properties": {
						"title": "",
						"abstract": "",
						"updated": "",
						"content": "",
						"offerings": [{
							"code": "",
							"operations": [{
								"method": "GET",
								"type": "",
								"href": ""
							}]
						}]
					}
				};

				// loop over layers in the map
				var thething = this.activeLayers;

				for (i=0;i<thething.length;i++) {
					var layer = this.activeLayers[i];
					var feature = featureTemplate;

					feature.properties.title = layer.name;
					feature.properties.abstract = layer.description;
					feature.properties.updated = sDate;
					feature.properties.content = "";
					var theType = this.getLinkType(layer.url);
					var theCode = this.getOGCOfferingCode(theType);
					feature.properties.offerings[0].code = this.getOGCOfferingCode(this.getLinkType(layer.url));
					feature.properties.offerings[0].operations[0].href = layer.url;


					jsonContext["features"].push(feature);
				}

				return jsonContext;
			},

			onSaveAsGeoJSON: function(jsonContext) {
				return JSON.stringify(jsonContext);

			},

			onSaveAsATOM: function(jsonContext) {
				var header = '<?xml version="1.0" encoding="UTF-8"?>';
				header += '<feed xmlns="http://www.w3.org/2005/Atom" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:georss="http://www.georss.org/georss" xmlns:gml="http://www.opengis.net/gml" xml:lang="en">';
				header += '<id>https://portal.opengeospatial.org/twiki/bin/view/OWSContextswg/SpecAtomEncoding#1</id>'
				header += '<title>OGC Context Document</title>';
				header += '<subtitle>Saved from ArcGIS Web AppBuilder</subtitle>';
				header += '<link rel="profile" href="http://www.opengis.net/spec/owc-atom/1.0/req/core" title="This file is compliant with version 1.0 of OGC Context"/>';
				header += '<updated>2012-02-21T11:58:23Z</updated>';
				header += '<author>';
				header += '<name>John Doe</name>';
				header += '<email>JohnDoe@example.com</email>';
				header += '<uri>http://example.com/~johndoe</uri>';
				header += '</author>';
				header += '<dc:publisher>ACME Project</dc:publisher>';
				header += '<rights>Copyright (c) 2012. Some rights reserved. This feed is licensed under a Creative Commons Attribution 3.0 License.</rights>';
				header += '<georss:where>';
				header += '<gml:Polygon>';
				header += '<gml:exterior>';
				header += '<gml:LinearRing>';
				header += '<gml:posList srsDimension="2">-90 -180 90 -180 90 180 -90 180 -90 -180</gml:posList>';
				header += '</gml:LinearRing>';
				header += '</gml:exterior>';
				header += '</gml:Polygon>';
				header += '</georss:where>';
				header += '<dc:date>2009-01-23T09:08:56.000Z/2009-01-23T09:14:08.000Z</dc:date>';

				var entries = '';

				var footer = '</feed>';

				for (i=0;i<jsonContext.features.length;i++) {
					var entry = '<entry>';
					entry += '<id>http://www.acme.eu/geoserver/wms/#world_countries</id>';
					entry += '<title>Base World Map</title>';
					entry += '<content type="html">ENVISAT MERIS Level 1 Reduced Resolution </content>';
					entry += '<updated>2012-05-10T14:35:00.400Z</updated>';
					entry += '<author>';
					entry += '  <name></name>';
					entry += '  <email></email>';
					entry += '  <uri></uri>';
					entry += '</author>';
					entry += '<dc:publisher></dc:publisher>';
					entry += '<rights>Copyright (c) 2012. Some rights reserved. This feed is licensed under a Creative Commons Attribution 3.0 License.</rights>';
					entry += '<georss:where>';
					entry += '  <gml:Polygon>';
					entry += '    <gml:exterior>';
					entry += '      <gml:LinearRing>';
					entry += '        <gml:posList>38.4921 44.2699 38.6058 43.4414 37.5318 43.2089 37.4215 44.0128 38.4921 44.2699</gml:posList>';
					entry += '      </gml:LinearRing>';
					entry += '    </gml:exterior>';
					entry += '  </gml:Polygon>';
					entry += '</georss:where>';
					entry += '<dc:date>2009-01-23T09:08:56.000Z/2009-01-23T09:14:08.000Z</dc:date>';
					entry += '<link rel="icon" type="image/png" href="http://www.acme.com/collections/products/algal20090123090856.png" title="Quicklook for the entry 2009-01-23 09:08:56" length="123432"/>';
					entry += '<owc:offering code="http://www.opengis.net/spec/owc-atom/1.0/req/wms">';
					entry += '<owc:operation method="GET" code="GetCapabilities" href="http://www.someserver.com/wrs.cgi?REQUEST=GetCapabilities&amp;SERVICE=WMS&amp;VERSION=1.1.1"/>';
					entry += '</owc:offering>';
					entry += '</entry>';
				}


				var text = header + entries + footer;

				return text;
			},

			onSaveAsContext: function(jsonContext) {
				var text = '<?xml version="1.0" encoding="UTF-8"?><ViewContext xmlns="http://www.opengis.net/context" version="1.1.0" id="OpenLayers_Context_117" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/context http://schemas.opengis.net/context/1.1.0/context.xsd"><General><Window width="768" height="384"/><BoundingBox minx="-180.000000000000000" miny="-90.0000000000000000" maxx="180.000000000000000" maxy="90.0000000000000000" SRS="EPSG:4326"/><Title/><Extension><ol:maxExtent xmlns:ol="http://openlayers.org/context" minx="-180.000000000000000" miny="-90.0000000000000000" maxx="180.000000000000000" maxy="90.0000000000000000"/></Extension></General><LayerList><Layer queryable="0" hidden="0"><Server service="OGC:WMS" version="1.1.1"><OnlineResource xmlns:xlink="http://www.w3.org/1999/xlink" xlink:type="simple" xlink:href="http://vmap0.tiles.osgeo.org/wms/vmap0"/></Server><Name>ground_01,ground_02,population,river,country_01,country_02,coastline_01,coastline_02,inwater</Name><Title>basic</Title><FormatList><Format current="1">image/png</Format></FormatList><StyleList><Style current="1"><Name/><Title>Default</Title></Style></StyleList><Extension><ol:maxExtent xmlns:ol="http://openlayers.org/context" minx="-180.000000000000000" miny="-90.0000000000000000" maxx="180.000000000000000" maxy="90.0000000000000000"/><ol:tileSize xmlns:ol="http://openlayers.org/context" width="256" height="256"/><ol:transparent xmlns:ol="http://openlayers.org/context">true</ol:transparent><ol:numZoomLevels xmlns:ol="http://openlayers.org/context">16</ol:numZoomLevels><ol:units xmlns:ol="http://openlayers.org/context">degrees</ol:units><ol:isBaseLayer xmlns:ol="http://openlayers.org/context">false</ol:isBaseLayer><ol:displayInLayerSwitcher xmlns:ol="http://openlayers.org/context">true</ol:displayInLayerSwitcher><ol:singleTile xmlns:ol="http://openlayers.org/context">false</ol:singleTile></Extension></Layer></LayerList></ViewContext>';

				return text;
			},

			getOGCOfferingCode: function(serviceType) {
				var offeringCode = "";

				if (serviceType == "wms" || serviceType == "wmts" || serviceType == "wfs" || serviceType == "wcs") {
					offeringCode = "http://www.opengis.net/spec/owc-geojson/1.0/req/" + serviceType;
				} else if (serviceType == "featureserver") {
					// TO-DO add ArcGIS service types
					offeringCode = "http://www.esri.com/rest/services/featureserver";
				} else if (serviceType == "mapserver") {
					// TO-DO add ArcGIS service types
					offeringCode = "http://www.esri.com/rest/services/mapserver";
				} else {
					offeringCode = "unsupported";
				}

				return offeringCode;

			},

			onUpload: function(evt) {
				console.log("onUpload");

				var fileName = evt.target.value.toLowerCase();

				if (sniff("ie")) { //filename is full path in IE so extract the file name
				  var arr = fileName.split("\\");
				  fileName = arr[arr.length - 1];
				}

				if (fileName.indexOf(".json") !== -1) {//is file a json - this is a new GeoJSON OGC Context doc
				  this.loadContext(evt.target.files[0], "geojson", this);

				} else if (fileName.indexOf(".xml") !== -1) {//is file a xml - this is an old Web Map Context XML doc
				  this.loadContext(evt.target.files[0], "xml", this);

				} else{
				  this.uploadStatus.innerHTML = '<p style="color:red">Load OGC Context document as .json or .xml file</p>';
				}
			},

			loadContext: function(file, contextType, that){
				var reader = new FileReader();

				// process OGC Context into list
				reader.onload = (function(theFile) {
					return function(e) {
						//dojo.byId("uploadStatus").innerHTML = ['<textarea>', e.target.result,'</textarea>'].join('');

						theList = dojo.byId("contextList"); // dnw this.list.domNode;
						theList.innerHTML = "";

						this.list = new List();
						this.list.startup();

						var ogcContext = {};

						// parse file contents as object assuming proper context type
						if (contextType == "geojson") {
							ogcContext = JSON.parse(e.target.result);
						} else {

							ogcContext.features = [];

							var jsdom = dojox.xml.parser.parse(e.target.result);
							var jsdomjson = domParser.parse(e.target.result);

							var viewContext = jsdomjson.childrenByName("ViewContext")[0];
							var layerList = viewContext.childrenByName("LayerList")[0];
							for (i=0; i<layerList.childNodes.length;i++) {
								var layer = layerList.childNodes[i];
								var server = layer.childrenByName("Server")[0];

								// get the link to the actual OGC service
								var offeringOperationHref = "";
								var onlineResource = server.childrenByName("OnlineResource")[0];
								for (j=0;j<onlineResource.attributes.length;j++) {
									if (onlineResource.attributes[j].nodeName == "xlink:href") {
										offeringOperationHref = onlineResource.attributes[j].nodeValue;
										break;
									}
								}

								// turn service attribute into context document offering code
								// and fix the link if not complete GetCapabilities request
								var wmcServiceType = "";
								var offeringCode = "";
								for (j=0;j<server.attributes.length;j++) {
									if (server.attributes[j].nodeName == "service") {
										wmcServiceType = server.attributes[j].nodeValue;
										break;
									}
								}
								if (wmcServiceType == "OGC:WMS") {
									offeringCode = "http://www.opengis.net/spec/owc-geojson/1.0/req/wms";
									if (offeringOperationHref.indexOf('?') < 0) {
										offeringOperationHref += "?request=GetCapabilities&service=WMS";
									}
								} else if (wmcServiceType == "OGC:WCS") {
									offeringCode = "http://www.opengis.net/spec/owc-geojson/1.0/req/wcs";
									if (offeringOperationHref.indexOf('?') < 0) {
										offeringOperationHref += "?request=GetCapabilities&service=WCS";
									}
								} else if (wmcServiceType == "OGC:WFS") {
									offeringCode = "http://www.opengis.net/spec/owc-geojson/1.0/req/wfs";
									if (offeringOperationHref.indexOf('?') < 0) {
										offeringOperationHref += "?request=GetCapabilities&service=WFS";
									}

								}



								var jsonLayer = {};
								jsonLayer.type = "Feature";
								jsonLayer.id = i;
								jsonLayer.geometry = {
									"type":"Polygon",
									"coordinates":[[[-180,-90],[180,-90],[180,90],[-180,90],[-180,-90]]],
									"crs":{
										"type":"name",
										"properties":{
											"name":"urn:ogc:def:crs:EPSG:8.8:4326"
										}
									}
								};
								jsonLayer.properties = {};
								jsonLayer.properties.title = layer.childrenByName("Title")[0].childNodes[0].nodeValue;
								jsonLayer.properties.abstract = layer.childrenByName("Name")[0].childNodes[0].nodeValue;
								jsonLayer.properties.updated = "2016-06-09T00:00:00Z";
								jsonLayer.properties.content = layer.childrenByName("Name")[0].childNodes[0].nodeValue;
								jsonLayer.properties.offerings = [{
										"code": offeringCode,
										"operations":[{
												"method":"GET",
												"type":"application/jpeg",
												"href": offeringOperationHref
										}]
								}];

								ogcContext.features.push(jsonLayer);
							}
						}


						// loop over 'features' and find ones with links
						this.features = ogcContext.features;
						for (i=0; i<this.features.length;i++) {
							// create list item for each
							feature = this.features[i];

							var item={};
							item.id = feature.id;
							item.label = feature.properties.title;
							item.link = feature.id;
							item.href = feature.properties.offerings[0].operations[0].href; // assumes the first offering and first operation are the full URL to the service.


							// if this is WMTS, expand the individual layers as entries in the list
							var linkType = that.getLinkType(item.href);

							var wmtsLayerList = [];
							var self = this;

							if (linkType == "wmts") {
								var wmtsLayersRequest = esriRequest({
									url: item.href,
									handleAs: "text",
									callbackParamName: "callback"
								});
								wmtsLayersRequest.then(
									function(response) {
										//console.log("Success: ", response);

										var xmlDoc;

										if (window.DOMParser) {
											parser = new DOMParser();
											xmlDoc = parser.parseFromString(response, "text/xml");
										} else {
											// Internet Explorer
											xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
											xmlDoc.async = false;
											xmlDoc.loadXML(response);
										}

										var wmtsLayers = xmlDoc.getElementsByTagNameNS("http://www.opengis.net/wmts/1.0","Layer");
										var wmtsOperations = xmlDoc.getElementsByTagName("Operation");
										var wmtsTileMatrixSets = xmlDoc.getElementsByTagName("TileMatrixSet");
										var wmtsUrl = "";

										for (i=0;i<wmtsOperations.length;i++) {
											var getCapabilities = wmtsOperations[i].attributes["name"];
											if (getCapabilities && (getCapabilities.value.toLowerCase() === "getcapabilities")) {
												wmtsUrl = wmtsOperations[i].getElementsByTagName("Get")[0].attributes["xlink:href"].value;
												if(!wmtsUrl.endsWith("?")) wmtsUrl += "?";
												wmtsUrl += "service=WMTS&version=1.0.0&request=GetCapabilities";
												break;
											}
										}

										for (i=0; i<wmtsLayers.length;i++) {
											// create list item for each
											wmtsLayer = wmtsLayers[i];

											var wmtsItem={};
											wmtsItem.link = wmtsUrl;
											wmtsItem.label = wmtsLayer.getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Title")[0].innerHTML;
											wmtsItem.id = wmtsLayer.getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Identifier")[0].innerHTML;

											var imageFormat = wmtsLayer.getElementsByTagName("Format")[0].innerHTML;

											// simplify format
											if (imageFormat.toLowerCase() === "image/x-jpegorpng") {
												imageFormat = "jpeg";
											} else {
												imageFormat = imageFormat.replace("image/","");
											}

											// get CRS for the layer (we're using the first tile matrix set)
											var wmtsCRS = "";
											var layerTileMatrixSet = wmtsLayer.getElementsByTagName("TileMatrixSet")[0].innerHTML;

											// TileMatrixSet occurs within layer and as a main level element.
											// differentiate as the layer entry doesn't have an identifier child element
											for (j=0; j<wmtsTileMatrixSets.length; j++) {
												var tileMatrixSetIdentifier;
												var tileMatrixSetIdentifiers = wmtsTileMatrixSets[j].getElementsByTagName("Identifier");

												if (tileMatrixSetIdentifiers.length > 0) {
													tileMatrixSetIdentifier = tileMatrixSetIdentifiers[0].innerHTML;
													if (layerTileMatrixSet == tileMatrixSetIdentifier) {
														wmtsCRS = wmtsTileMatrixSets[j].getElementsByTagName("SupportedCRS")[0].innerHTML;
														wmtsCRS = wmtsCRS.split(":").pop();

														break;
													}
												}
											}

											// assumes the first offering and first operation are the full URL to the service.
											// append the selected WMTS layer, tileMatrixSet, and imageFormat as additional attribute in the URL.
											// this will be picked up by the layer factory when the user selects the WMTS layer.
											console.log("WMTS Layer = " + wmtsItem.id + "|" + layerTileMatrixSet + "|" + imageFormat + "|" + wmtsCRS);
											wmtsItem.href = wmtsUrl + "&wmtsLayerInfo=" + wmtsItem.id + "|" + layerTileMatrixSet + "|" + imageFormat + "|" + wmtsCRS;

											self.list.add(wmtsItem);
										}


									}, function(error) {
										console.log("Error: ", error.message);
									}
								);

								//wmtsLayerList = dojo.byId("po_box").innerHTML;

								//this.list.add(wmtsLayerList);

							} else if (linkType == "wcs") {
								var wcsLayersRequest = esriRequest({
									url: item.href,
									handleAs: "text",
									callbackParamName: "callback"
								});
								wcsLayersRequest.then(
									function(response) {
										//console.log("Success: ", response);

										var xmlDoc;

										if (window.DOMParser) {
											parser = new DOMParser();
											xmlDoc = parser.parseFromString(response, "text/xml");
										} else {
											// Internet Explorer
											xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
											xmlDoc.async = false;
											xmlDoc.loadXML(response);
										}

										var wcsCapabilities = xmlDoc.getElementsByTagName("Capabilities");
										if (wcsCapabilities && wcsCapabilities.length > 0) {
											wcsCapabilities = xmlDoc.getElementsByTagName("Capabilities")[0];
										} else {
											wcsCapabilities = xmlDoc.getElementsByTagName("WCS_Capabilities")[0];
										}
										//var wcsVersion = xmlDoc.getElementsByTagName("Capabilities")[0].getAttribute("version");
										var wcsVersion = wcsCapabilities.getAttribute("version");
										var wcsNamespace = "";
										var wcsFormatElement = "";
										var wcsCoverageId = "";

										if (wcsVersion.startsWith("1.")) {
											wcsNamespace = "http://www.opengis.net/wcs/1.1";
											wcsFormatElement = "SupportedFormat";
											wcsCoverageId = "Identifier";
										} else {
											wcsNamespace = "http://www.opengis.net/wcs/2.0";
											wcsFormatElement = "formatSupported";
											wcsCoverageId = "CoverageId";
										}

										var wcsLayers = xmlDoc.getElementsByTagNameNS(wcsNamespace,"CoverageSummary");
										var wcsFormats = xmlDoc.getElementsByTagNameNS(wcsNamespace, wcsFormatElement);
										var wcsOperations = xmlDoc.getElementsByTagName("Operation");
										var wcsUrl = "";

										for (j=0;j<wcsOperations.length;j++) {
											var getCapabilities = wcsOperations[j].attributes["name"];
											if (getCapabilities && (getCapabilities.value.toLowerCase() === "getcapabilities")) {
												wcsUrl = wcsOperations[j].getElementsByTagName("Get")[0].attributes["xlink:href"].value;
												if(!wcsUrl.endsWith("&")) {
													if(!wcsUrl.endsWith("?")) wcsUrl += "?";
												}
												wcsUrl += "service=WCS&version=" + wcsVersion + "&request=GetCapabilities";
												break;
											}
										}

										for (j=0; j<wcsLayers.length;j++) {
											// create list item for each
											wcsLayer = wcsLayers[j];

											var wcsItem={};
											wcsItem.link = wcsUrl;
											wcsItem.id = wcsLayer.getElementsByTagNameNS(wcsNamespace, wcsCoverageId)[0].innerHTML;

											var wcsLayerLabelNode = wcsLayer.getElementsByTagNameNS("http://www.opengis.net/ows/1.1", "Title")[0];
											if (wcsLayerLabelNode) {
												wcsLayerLabel = wcsLayerLabelNode.innerHTML;
											} else {
												wcsLayerLabel = wcsItem.id;
											}
											wcsItem.label = wcsLayerLabel;

											var imageFormat = "";  // the format used in the JS API WCSLayer
											var wcsFormat = "";  // the format as advertized in GetCapabilities
											for (k=0; k<wcsFormats.length;k++) {
												wcsFormat = wcsFormats[k].innerHTML.toLowerCase();

												if (wcsFormat === "image/png") break;
												if (wcsFormat === "image/tiff") break;
												if (wcsFormat === "image/jpg") break;
											}

											// simplify format
											if (wcsFormat.toLowerCase() === "image/x-jpegorpng") {
												imageFormat = "jpeg";
											} else if (wcsFormat.toLowerCase() === "image/jp2") {
												imageFormat = "jpeg";
											} else if (wcsFormat.toLowerCase() === "application/netcdf") {
												imageFormat = "jpeg";
											} else {
												imageFormat = wcsFormat.replace("image/","");
											}

											// assumes the first offering and first operation are the full URL to the service.
											// append the selected WMTS layer, tileMatrixSet, and imageFormat as additional attribute in the URL.
											// this will be picked up by the layer factory when the user selects the WMTS layer.
											console.log("WCS Layer = " + wcsItem.id + "|" + imageFormat);
											wcsItem.href = wcsUrl + "&wcsLayerInfo=" + wcsItem.id + "|" + imageFormat;

											self.list.add(wcsItem);
										}


									}, function(error) {
										console.log("Error: ", error.message);
									}
								);

							} else if (linkType == "wfs") {
								var wfsLayersRequest = esriRequest({
									url: item.href,
									handleAs: "text",
									callbackParamName: "callback"
								});
								wfsLayersRequest.then(
									function(response) {
										//console.log("Success: ", response);

										var xmlDoc;

										if (window.DOMParser) {
											parser = new DOMParser();
											xmlDoc = parser.parseFromString(response, "text/xml");
										} else {
											// Internet Explorer
											xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
											xmlDoc.async = false;
											xmlDoc.loadXML(response);
										}

										var wfsLayers = xmlDoc.getElementsByTagNameNS("http://www.opengis.net/wfs/2.0","FeatureType");
										//var wfsOperations = xmlDoc.getElementsByTagName("Operation");
										var wfsOperations = xmlDoc.getElementsByTagNameNS("http://www.opengis.net/ows/1.1","Operation");
										var wfsUrl = "";

										for (i=0;i<wfsOperations.length;i++) {
											var getCapabilities = wfsOperations[i].attributes["name"];
											if (getCapabilities && (getCapabilities.value.toLowerCase() === "getcapabilities")) {
												wfsUrl = wfsOperations[i].getElementsByTagNameNS("http://www.opengis.net/ows/1.1","Get")[0].attributes["xlink:href"].value;
												if(!wfsUrl.endsWith("?")) wfsUrl += "?";
												wfsUrl += "service=WFS&version=2.0.0&request=GetCapabilities";
												break;
											}
										}

										for (j=0; j<wfsLayers.length;j++) {
											// create list item for each
											wfsLayer = wfsLayers[j];

											var wfsItem={};
											wfsItem.link = wfsUrl;
											wfsItem.label = wfsLayer.getElementsByTagNameNS("http://www.opengis.net/wfs/2.0", "Title")[0].innerHTML;
											wfsItem.id = wfsLayer.getElementsByTagNameNS("http://www.opengis.net/wfs/2.0", "Name")[0].innerHTML;

											console.log("WFS Layer = " + wfsItem.id);
											wfsItem.href = wfsUrl + "&wfsLayerInfo=" + wfsItem.id;

											self.list.add(wfsItem);
										}


									}, function(error) {
										console.log("Error: ", error.message);
									}
								);

							} else {
								this.list.add(item);
							}
						}

						domConstruct.place(this.list.domNode,theList);

					}
				})(file);

				reader.readAsText(file);

				//this.uploadStatus.innerHTML = "<textarea>" + reader.result + "</textarea>";
				console.log("loadContext");
			},

			_selectResultItem: function(index, item) {
				var x = index.clientX;
				var y = index.clientY;
				var element = document.elementFromPoint(x, y).parentElement;


				var elementId = element.id;
				//var elParts = elementId.split("http");
				//var recordId = "";
				//if(elParts && elParts.length==2){
				//  recordId = elParts[1];
				//}

				//var href = "http" + recordId;
				var href = element.dataset.href;
				var linkType = "unknown";

				linkType = this.getLinkType(href);
				console.log('href=' + href + "\n linktype=" + linkType);


				if (linkType == "mapserver" || linkType == "featureserver" || linkType == "imageserver"	|| linkType == "kml" || linkType == "wcs" || linkType == "wfs" || linkType == "wms" || linkType == "wmts") {

					LayerFactory.createLayer(href, linkType, element.options).then(lang.hitch(this,function(layer){
						this.map.addLayer(layer);
						this.activeLayers.push(layer);
					}));

				} else if (linkType == "webmap") {
					var wmProcessor = new WebMapProcessor();
					wmProcessor.process(href,this.map);
				}
			},

			getLinkType: function(link) {
				var linksProcessor = new LinksProcessor();
				var linkType = linksProcessor.getServiceType(link);

				return linkType;
			}
		});
});
