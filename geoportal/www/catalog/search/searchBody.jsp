<%--
 See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 Esri Inc. licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>
<%// searchBody.jsp - Create search criteria (JSF body)%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="gpt" uri="http://www.esri.com/tags-gpt"%>

<f:verbatim>
  <style type="text/css">
  .columnsTable td {vertical-align: top;}
</style>
</f:verbatim>

<%
  // interactive map configuration , 
  // the ArcGIS Server Javascript API is explicitly loaded for this page within centeredLayout.jsp,
  // loading the jsapi within the <head> tag reduces flicker on the search page
  com.esri.gpt.framework.ArcGIS.InteractiveMap imConfig = com.esri.gpt.framework.context.RequestContext
    .extract(request).getApplicationConfiguration().getInteractiveMap();
%>
<script type="text/javascript">
  dojo.require("dojo.cookie");
  dojo.require("dijit.form.Form");
  
  var _cookieLabel = "searchPageCookie";
  
  var _frmDjSearchCriteria = null;
  
   	// Set the extent based on the bookmark selected. WARNING: the WKID is hardcoded to 4326.
    function setMapExtent()	{	
      var scEl = document.getElementById("frmSearchCriteria:scSel");
      var extent= scEl!=null? scEl.value: null;
      if((extent!=null)&&(extent!=""))	{
        var selectedExtent = eval("new esri.geometry.Extent({"+gptMapConfig.mapInitialExtent+"})");
        if(extent!=="default") {
          selectedExtent = new esri.geometry.Extent(Number(extent.split(";")[0]),Number(extent.split(";")[1]),Number(extent.split(";")[2]),Number(extent.split(";")[3]),(new esri.SpatialReference({wkid:4326})));
        } 
        scMap._gptMap.zoom(selectedExtent);
      }
    }
	
  function serilizeFormToCookie() {
    //dojo.cookie(_cookieLabel, dojo.formToJson("frmSearchCriteria"));
  }
  
  function deserializeFormFromCookie() {
   /*var func = dojo.hitch(this, function() {
    if(_frmDjSearchCriteria == null) {
      _frmDjSearchCriteria = new dijit.form.Form({ id:"frmSearchCriteria" });
    }
    debugger;
    var formValues = dojo.cookie(_cookieLabel);
    if(formValues == null || formValues == "") {
      return;
    }
    formValues = eval( "(" + formValues + ")");
    var key;
    for(key in formValues) {
      
      var node = dojo.byId(key);
      if(node != null && node.value) {
        var value = formValues[key];
        alert(value);
        node.setAttribute("value", value);
      }
    }
    _frmDjSearchCriteria.setValues(formValues);
   }); 
   func();
   */ 
    //
  } 

  var gptMapConfig = new GptMapConfig();
  gptMapConfig.mapServiceURL = "<%=imConfig.getMapServiceUrl()%>";
  gptMapConfig.mapServiceType = "<%=imConfig.getMapServiceType()%>";
  gptMapConfig.geometryServiceURL = "<%=imConfig.getGeometryServiceUrl()%>";
  gptMapConfig.locatorURL = "<%=imConfig.getLocatorUrl()%>";
  gptMapConfig.locatorSingleFieldParameter = "<%=imConfig.getLocatorSingleFieldParameter()%>";
  gptMapConfig.locatorGraphicURL = "<%=request.getContextPath()%>/catalog/images/pushpin_red.gif";
  gptMapConfig.mapVisibleLayers = "<%=imConfig.getMapVisibleLayers()%>";
  gptMapConfig.mapInitialExtent = "<%=imConfig.getMapInitialExtent()%>";
</script>

<f:verbatim>
    
<script type="text/javascript" >

var _initialMinX = null;
var _initialMinY = null;
var _initialMaxX = null;
var _initialMaxY = null;
dojo.declare("SearchMap", null, {
  _gptMap: null,
  _gptMapToolbar: null,
  _gptLocator: null,
  _gptInpEnv: null,


  constructor: function() {
    this.initialize = dojo.hitch(this,this.initialize);
    this.drawFootPrints = dojo.hitch(this,this.drawFootPrints);
    this.clearFootPrints = dojo.hitch(this,this.clearFootPrints);
    this.highlightFootPrint = dojo.hitch(this,this.highlightFootPrint);
    this.getProcessedExtent = dojo.hitch(this,this.getProcessedExtent);
    this.zoomToAOI = dojo.hitch(this,this.zoomToAOI);
    this.zoomAnywhere = dojo.hitch(this,this.zoomAnywhere);
    this.zoomToThese = dojo.hitch(this,this.zoomToThese);
    this.zoomToFootPrint = dojo.hitch(this,this.zoomToFootPrint);
    this.zoomToInitExtent = dojo.hitch(this, this.zoomToInitExtent);
    this.reposition = dojo.hitch(this,this.reposition);
    this.clearGraphics = dojo.hitch(this, this.clearGraphics);
    
  },

	setMapExtent:function() // Set map extent based on bookmark
	{	var extent= document.getElementById("frmSearchCriteria:scSel").value;
	    if((extent!=null)&&(extent!=""))
		{	if(extent=="default")
				selectedExtent= eval("new esri.geometry.Extent({"+gptMapConfig.mapInitialExtent+"})");
			else
				var selectedExtent = new esri.geometry.Extent(Number(extent.split(";")[0]),Number(extent.split(";")[1]),Number(extent.split(";")[2]),Number(extent.split(";")[3]),(new esri.SpatialReference({wkid:4326})));
			
			scMap._gptMap.zoom(selectedExtent);
		}
	},

  initialize: function() {
    var config = gptMapConfig;

    config.mapElementId = "interactiveMap";
    config.mapToolbarId = "frmSearchCriteria:mapToolbar";
    config.inputEnvelopeXMinId = "frmSearchCriteria:sfsMinX";
    config.inputEnvelopeYMinId = "frmSearchCriteria:sfsMinY";
    config.inputEnvelopeXMaxId = "frmSearchCriteria:sfsMaxX";
    config.inputEnvelopeYMaxId = "frmSearchCriteria:sfsMaxY";
    config.locatorInputId = "frmSearchCriteria:mapInput-locate";
    config.locatorCandidatesId = "locatorCandidates";
    esriConfig.defaults.io.proxyUrl = "<%=request.getContextPath()%>/catalog/download/proxy.jsp";
    esri.config.defaults.io.proxyUrl = "<%=request.getContextPath()%>/catalog/download/proxy.jsp";

    this._gptMap = new GptMap();
    dojo.connect(this._gptMap,"onMapLoaded",this,"onMapLoaded");
    dojo.connect(this._gptMap,"onExtentChange",this,"onExtentChange");
    this._gptMap.initialize(config);

    this._gptInpEnv = new GptInputEnvelope();
    this._gptInpEnv.initialize(config,this._gptMap,true);

    this._gptMapToolbar = new GptMapToolbar();
    dojo.connect(this._gptMapToolbar,"onMapButtonClicked",this,"onMapButtonClicked");
    this._gptMapToolbar.initialize(config,this._gptMap);
    
    this._gptLocator = new GptLocator();
    this._gptLocator.initialize(config,this._gptMap);
    
     _initialMinX = parseInt(dojo.byId("frmSearchCriteria:sfsMinX").value);
     _initialMinY = parseInt(dojo.byId("frmSearchCriteria:sfsMinY").value);
     _initialMaxX = parseInt(dojo.byId("frmSearchCriteria:sfsMaxX").value);
     _initialMaxY = parseInt(dojo.byId("frmSearchCriteria:sfsMaxY").value);
  },

  onMapLoaded: function() {
    if (this._gptMap!=null) {
      // Set the map extent based on the bookmark
      var scSel = document.getElementById("frmSearchCriteria:scSel");
      var extent= scSel!=null? scSel.value: null;
      if((extent!=null)&&(extent!=""))
        setTimeout(dojo.hitch(this,"setMapExtent",true),1000);
      else
        setTimeout(dojo.hitch(this,"zoomToInitExtent",true),1000);

      this.drawFootPrints();
      var agsMap = this._gptMap.getAgsMap();
      if (agsMap != null) {
        dojo.connect(agsMap,"onMouseMove",this, "onMouseMove");
        dojo.connect(agsMap,"onMouseOut",this, "onMouseOut");
      }
      if (gptMapConfig.mapServiceType=="openstreet" || gptMapConfig.mapServiceType=="wmts") {
        var slider = dojo.byId("interactiveMap_zoom_slider");
        if (slider!=null) {
          if (dojo.isIE) {
            slider.style.height="120px";
          } else {
            slider.style.height="170px";
          }
        }
      }
    }
  },
  
  clearFootPrints: function() {
    this._gptMap.clearGraphics();
  },
  
  drawFootPrints:  function() {

    var records = jsMetadata.records;
    var iRecord, record, iBBox, oBBox;
    var aExtents = new Array();
    var aRecordIDs = new Array();

    for (iRecord=0;iRecord<records.length;iRecord++) {
      record = records[iRecord];
      if ((record != null) && (record.bboxes != null)) {
        for (iBBox=0;iBBox<record.bboxes.length;iBBox++) {
          oBBox = record.bboxes[iBBox];
          if (oBBox.isDefaultGeometry == false) {
            var tmp = this.getProcessedExtent(oBBox);
            tmp.gptMViewIndex = iRecord;
            aExtents[aExtents.length] = tmp;
            aRecordIDs[aRecordIDs.length] = iRecord;
          }
        }
      }
    }

    this._gptMap.projectGCSExtents(aExtents, dojo.hitch(this,function(gfx) {
      var aGfx = [];
	    var i, iRecord = -1, invalidRing = false, poly = null, pt, pts = new Array(), graphic, symbol;
	    if (gfx != null) {
	      for (i=0;i<gfx.length;i++) {
	        pt = gfx[i].geometry;
	        pts[pts.length] = pt;
	        if ((pt == null) || isNaN(pt.x) || isNaN(pt.y)) invalidRing = true;
	        if (pts.length == 4) {
	          iRecord++;
            graphic = new esri.Graphic();
            symbol = new esri.symbol.SimpleFillSymbol(esri.symbol.SimpleFillSymbol.STYLE_NULL,
                     new esri.symbol.SimpleLineSymbol(esri.symbol.SimpleLineSymbol.STYLE_SOLID,
                     new dojo.Color([255,0,0]), 2), new dojo.Color([255,255,0,0.1]));
            
            graphic.gptSRTag = "frmSearchCriteria:mdRecords:"+aRecordIDs[iRecord]+":_metadataMainRecordTable";
            
             if(pts[3].x == pts[2].x  && pts[3].y == pts[2].y) {
              symbol = new esri.symbol.SimpleMarkerSymbol(
                  esri.symbol.SimpleMarkerSymbol.STYLE_CIRCLE, 8, 
                  new esri.symbol.SimpleLineSymbol(
                      esri.symbol.SimpleLineSymbol.STYLE_SOLID, 
                      new dojo.Color([255,0,0]), 1), 
                      new dojo.Color([255,255,0,0.1]));
              graphic.setGeometry(pts[3]);
              graphic.gptArea = 0;
              graphic.setSymbol(symbol);
            } else if (invalidRing) {
		          graphic.setGeometry(null);
		          graphic.gptArea = 0;
		          graphic.setSymbol(symbol);
		        } else {
		        	graphic.setSymbol(symbol);
		          poly = new esri.geometry.Polygon(pts[0].spatialReference);
              // this has been added to adjust to the JS API 1.5 requirement to
              // have all rings as closed polygons (last point is equal to the first)
              pts[pts.length] = pts[0];
	            poly.addRing(pts);
	            graphic.setGeometry(poly);
              var extent = poly.getExtent();
              if (extent!=null) {
                graphic.gptArea = extent.getWidth() * extent.getHeight();
              } else {
                graphic.gptArea = 0;
              }
	          }

	          aGfx[aGfx.length] = graphic;
            invalidRing = false;
	          pts = new Array();
	        }
	      }
	    }

      aGfx.sort(function(a,b) {return a.gptArea - b.gptArea;});
      aGfx.reverse();
      for (iRecord=0;iRecord<aGfx.length;iRecord++) {
        this._gptMap.addGraphic(aGfx[iRecord]);
      }

    }));

  },
 
  highlightFootPrint: function(sTag, bHighlight) {
    var agsMap = null, aGfx, graphic, i, n, symbol;
    if (this._gptMap != null) agsMap = this._gptMap.getAgsMap();
    if ((agsMap != null) && (agsMap.graphics != null) && (agsMap.graphics.graphics != null)) {
      aGfx = agsMap.graphics.graphics;
      for (i=0, n=aGfx.length; i<n; i++) {
        graphic = aGfx[i];
        if ( graphic.geometry!=null && graphic.gptSRTag != null && graphic.gptSRTag == sTag) {
        	if(graphic.symbol instanceof esri.symbol.MarkerSymbol)
          {
        		symbol = new esri.symbol.SimpleMarkerSymbol(
                        esri.symbol.SimpleMarkerSymbol.STYLE_CIRCLE, 8, 
                        new esri.symbol.SimpleLineSymbol(
                            esri.symbol.SimpleLineSymbol.STYLE_SOLID, 
                            new dojo.Color([255,0,0]), 1), 
                            new dojo.Color([255,255,0,0.3]));
        		if (bHighlight) 
        			symbol.color = new dojo.Color([255,255,0,0.9]);
        	} else {
            symbol = new esri.symbol.SimpleFillSymbol(esri.symbol.SimpleFillSymbol.STYLE_NULL,
                   new esri.symbol.SimpleLineSymbol(esri.symbol.SimpleLineSymbol.STYLE_SOLID,
                   new dojo.Color([255,0,0]), 2), new dojo.Color([255,255,0,0.3]));
            if (bHighlight) symbol.style = 
            	esri.symbol.SimpleFillSymbol.STYLE_SOLID;
        	}
          graphic.setSymbol(symbol);
        }
      }
    }
  },

	getProcessedExtent: function(bbox) {
	  var tmp
	  var lMinX = GptUtils.valChkDouble(bbox.minX,-180.0);
	  var lMinY = GptUtils.valChkDouble(bbox.minY,-90.0);
	  var lMaxX = GptUtils.valChkDouble(bbox.maxX,180.0);
	  var lMaxY = GptUtils.valChkDouble(bbox.maxY,90.0);
	  if (lMaxY > 90)    {lMaxY = 90;}
	  if (lMinY < -90)   {lMinY = -90;}
	  if (lMaxY < lMinY) {tmp = lMaxY; lMaxY = lMinY; lMinY = tmp;}
	  if (lMaxX < lMinX) {lMaxX = 180; lMinX = -180;}
	  if (lMaxX > 179.99 )  {lMaxX = 179.99;}
	  if (lMinX < -179.99 )  {lMinX = -179.99;}
	  return new esri.geometry.Extent(lMinX,lMinY,lMaxX,lMaxY,new esri.SpatialReference({wkid:4326}));
	},
	
	clearGraphics: function() {
    this._gptMap.clearGraphics();
  },

  onExtentChange: function(extent) {
    if (this._gptMap == null) return;
    var wkid = "";
    var elXMin = document.getElementById("frmSearchCriteria:sfsVMinX");
    var elYMin = document.getElementById("frmSearchCriteria:sfsVMinY");
    var elXMax = document.getElementById("frmSearchCriteria:sfsVMaxX");
    var elYMax = document.getElementById("frmSearchCriteria:sfsVMaxY");
    var elWkid = document.getElementById("frmSearchCriteria:sfsVWkid");
    if ((elXMin != null) && (elYMin != null) && (elXMax != null) && (elYMax != null) && (elWkid != null)) {
      //if (this._gptMap.isMapProjected()) 
      wkid = this._gptMap.getAgsMap().spatialReference.wkid;
      elXMin.value = extent.xmin;
      elYMin.value = extent.ymin;
      elXMax.value = extent.xmax;
      elYMax.value = extent.ymax;
      elWkid.value = wkid;
    }
    if (this._gptInpEnv != null) {
	    this._gptMap.projectMapExtentToGSC(extent, dojo.hitch(this,function(gfx) {
	      var ext, poly = this._gptMap.projectedExtentAsPolygon(gfx);
	      if (poly != null) ext = poly.getExtent();
	      if (ext != null) {
	        this._gptInpEnv.setInputEnvelope(ext);
	      }
	    }));
	  }
  },

  onLocatorKeyPress: function(e) {
    if (!e) e = window.event;
    if (e) {
      var nKey = (e.keyCode) ? e.keyCode : e.which;
      if (nKey == 13) {
        if (this._gptLocator != null) this._gptLocator.locate();
        return false;
      }
    }
    return true;
  },

  onMapButtonClicked: function(sButtonName) {
    if (sButtonName == "zoomToWorld") {
      if (this._gptMap != null) this._gptMap.zoomToWorld();
    } else if (sButtonName == "zoomToInputEnvelope") {
      if (this._gptInpEnv != null) this._gptInpEnv.zoomToInputEnvelope();
    } else if (sButtonName == "locate") {
      if (this._gptLocator != null) this._gptLocator.locate();
    }
  },

  pointToExtent: function(
	      /*esri.Map*/ map, 
	      /*esri.geometry.Point (in map coords)*/ point) {
	    toleranceInPixel = 10;
	    //calculate map coords represented per pixel
	    var pixelWidth = map.extent.getWidth() / map.width;
	    //calculate map coords for tolerance in pixel
	    var toleraceInMapCoords = toleranceInPixel * pixelWidth;
	    //calculate & return computed extent
	    return new esri.geometry.Extent( point.x - toleraceInMapCoords,
	                                     point.y - toleraceInMapCoords,
	                                     point.x + toleraceInMapCoords,
	                                     point.y + toleraceInMapCoords,
	                                     map.spatialReference ); 
	  }, onMouseMove: function(event) {
	    var agsMap = null, bFound = false;
	    var aGfx, i, n, graphic, geometry, symbol, rec, recClass, fillStyle, fillColor;
	    if (this._gptMap != null) agsMap = this._gptMap.getAgsMap();
	    if ((agsMap != null) && (agsMap.graphics != null) && (agsMap.graphics.graphics != null)) {
	      aGfx = agsMap.graphics.graphics;
	      var validEnvs = new Array();
	      var validEnvsIndx = new Array();
	      for (i=aGfx.length-1;i>=0; i--) {
	        graphic = aGfx[i];
	        geometry = graphic.geometry;
	        var tmpGraphic = {};
	        if(graphic.geometry instanceof esri.geometry.Point) {
	          tmpGraphic.geometry = this.pointToExtent(agsMap, graphic.geometry)
	          tmpGraphic.gptSRTag = graphic.gptSRTag;
	        }
	        if(graphic.geometry!=null && graphic.gptSRTag != null && event && 
	          event.mapPoint && geometry!=null) {
	        	 var geomInScope = false;
	        	 if(geometry instanceof esri.geometry.Point) {
	        		 geomInScope = (this.pointToExtent(agsMap, event.mapPoint))
	        		   .contains(geometry);
	        	 } else {
	        		 geomInScope = geometry.contains(event.mapPoint);
	        	 }
	        	 if(geomInScope == true) {
	             validEnvs[graphic.gptSRTag] = "done";
	             validEnvsIndx[validEnvsIndx.length] = graphic.gptSRTag;
	        	 }
	        }
	      }
      for (i=aGfx.length-1;i>=0; i--) {
        graphic = aGfx[i];
        if (graphic.geometry!=null && graphic.gptSRTag != null) {
          recClass= "noneSelectedResultRow";
          geometry = graphic.geometry;
          fillStyle = esri.symbol.SimpleFillSymbol.STYLE_NULL;
          fillColor = new dojo.Color([255,255,0,0.3]);
          var highlight = false;
          if (validEnvs[graphic.gptSRTag] != null) {
            
            if (!bFound || graphic.gptSRTag == validEnvsIndx[0]  ) {
              fillStyle = esri.symbol.SimpleFillSymbol.STYLE_SOLID;
              recClass= "selectedResultRow";
            } else {
              recClass= "selectedResultRowLight";
              highlight = true;
            }
            bFound = true;
          }
          if(graphic.geometry instanceof esri.geometry.Point) {
        	  if(  recClass.indexOf("selected") == 0) {
        		  symbol = new esri.symbol.SimpleMarkerSymbol(
                          esri.symbol.SimpleMarkerSymbol.STYLE_CIRCLE, 8, 
                          new esri.symbol.SimpleLineSymbol(
                              esri.symbol.SimpleLineSymbol.STYLE_SOLID, 
                              new dojo.Color([255,0,0]), 1), 
                              new dojo.Color([255,255,0,0.9]));
        	  } else {
        	    symbol = new esri.symbol.SimpleMarkerSymbol(
                      esri.symbol.SimpleMarkerSymbol.STYLE_CIRCLE, 8, 
                      new esri.symbol.SimpleLineSymbol(
                          esri.symbol.SimpleLineSymbol.STYLE_SOLID, 
                          new dojo.Color([255,0,0]), 1), 
                          new dojo.Color([255,255,0,0.1]));
        	  }
        	  graphic.setSymbol(symbol);
          } else {
            symbol = new esri.symbol.SimpleFillSymbol(fillStyle,
                   new esri.symbol.SimpleLineSymbol(esri.symbol.SimpleLineSymbol.STYLE_SOLID,
                   new dojo.Color([255,0,0]), 2), fillColor);
            graphic.setSymbol(symbol);
          }
          rec = document.getElementById(graphic.gptSRTag);
          if (rec != null) rec.className = recClass;
        }
      }
    }
  },

  onMouseOut: function(event) {
    this.onMouseMove({"event":"noMapPoint"});
  },

  reposition: function() {
    if (this._gptMap != null) this._gptMap.reposition();
  },

  zoomToAOI: function(bIsInitializing) {
    var extent, wkid = 4326, mwkid, requiresProjection = false, srefFrom = null, srefTo = null;
    if ((this._gptMap != null) && (typeof(aoiOperator) != 'undefined') && (typeof(aoiMinX) != 'undefined')) {
      if ((aoiOperator == "anywhere") || (aoiMinX == -9999.0)) {
        this.zoomAnywhere();
      } else {
        extent = new esri.geometry.Extent(aoiMinX,aoiMinY,aoiMaxX,aoiMaxY,null);
        if ((aoiWkid != null) && (aoiWkid.length > 0) && !isNaN(aoiWkid)) {
          iwkid = parseInt(aoiWkid);
          if(iwkid != NaN) {
            var spatialReference = 
              new esri.SpatialReference({wkid : iwkid  }); 
            extent = new esri.geometry.Extent(aoiMinX,aoiMinY,aoiMaxX,aoiMaxY,
              spatialReference);
          }
        }
        if (this._gptMap.getAgsMap() != null) {
          srefTo = this._gptMap.getAgsMap().spatialReference;
          if (srefTo != null) {
            mwkid = srefTo.wkid;
            if ((mwkid != null) && (mwkid != wkid)) {
              srefFrom = new esri.SpatialReference({wkid:eval(wkid)});
              requiresProjection = true;
              if (this._gptMap.isGCSWkid(wkid) && this._gptMap.isGCSWkid(wkid)) {
                requiresProjection = false;
              }
            }
          }
        }

        if (!requiresProjection) {
          this._gptMap.zoom(extent);
        } else {
			    this._gptMap.projectExtent(extent, srefFrom, srefTo, dojo.hitch(this,function(gfx) {
			      var poly = this._gptMap.projectedExtentAsPolygon(gfx);
			      if (poly != null) this._gptMap.zoom(poly.getExtent());
			    }));
        }
      }
    }
  },


  zoomAnywhere: function zoomAnywhere() {
    if (this._gptMap != null) {
      if (this._gptMap._initialExtent!=null) {
        this._gptMap.zoomToInitial();
      } else {
        this._gptMap.zoomToDefault();
      }
    }
  },

  zoomToFootPrint: function(rowIndex) {
    var oBBox,extent;
    if ((this._gptMap != null) && (typeof(jsMetadata) != 'undefined') 
    		&& (typeof(jsMetadata.records[rowIndex]) != 'undefined')) {
      oBBox = jsMetadata.records[rowIndex].enclosingEnvelope;
      extent = this.getProcessedExtent(oBBox);
      if(extent.xmin == extent.xmax && extent.ymin == extent.ymax) {
    	  // This is a point
    	  extent.xmin = extent.xmin - 0.005;
    	  extent.xmax = extent.xmax + 0.005;
    	  extent.ymin = extent.ymin - 0.005;
        extent.ymax = extent.ymax + 0.005;
    	  //extent = extent.expand(2);
      }
      this._gptMap.zoomToGCSExtent(extent,true);
    }
  },

  zoomToThese: function zoomToThese() {
    var oBBox = new Array(), extent;
    if ((this._gptMap != null) && (typeof(resultsMapMinX) != 'undefined')) {
      oBBox.minX = resultsMapMinX;
      oBBox.maxX = resultsMapMaxX;
      oBBox.minY = resultsMapMinY;
      oBBox.maxY = resultsMapMaxY;
      extent = this.getProcessedExtent(oBBox);
      
      this._gptMap.zoomToGCSExtent(extent,true);
    }
  },
  
  zoomToInitExtent:function() {
   
    if ((aoiOperator == "anywhere") || (_initialMinX == -9999.0)) {
        this.zoomAnywhere();
        return;
    }
    
    var extent = new esri.geometry.Extent(
      _initialMinX,_initialMinY,_initialMaxX,_initialMaxY,
        new esri.SpatialReference({wkid:4326}));
    //extent = this.getProcessedExtent(extent);
    this._gptMap.zoomToGCSExtent(extent,true);
  }

});

var scMap = new SearchMap();

dojo.addOnLoad(scInit);
dojo.addOnLoad(deserializeFormFromCookie);

function scInit(){
  var elFocus = document.getElementById("frmSearchCriteria:scText");
  if (elFocus != null) elFocus.focus();

  scMap.initialize();
  dojo.connect(window,"onresize",scMap,"reposition");
  scMap.reposition();
} 
  
  
// Checks whether map is loaded and ready for actions
function scIsSearchMapReady() {
  var scSearchMap;
  return (GptUtils.exists(scSearchMap) && GptUtils.exists(scSearchMap.getMap) && GptUtils.exists(scSearchMap.getMap()) && GptUtils.exists(scSearchMap.getMap().loaded) && GptUtils.exists(scSearchMap.getMap().loaded == true));
}
    
      
 
   

/**
Submits from when on enter.
@param event The event variable
@param form The form to be submitted.
**/
function scSubmitForm(event, form) {

  var e = event;
  if (!e) e = window.event;
  var tgt = (e.srcElement) ? e.srcElement : e.target; 
  if ((tgt != null) && tgt.id) {
    if (tgt.id == "frmSearchCriteria:mapInput-locate") return;
  }
  
  if(!GptUtils.exists(event)) {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "fn submitform: could not get event so as to determine if to submit form ");
    return;
  }
  var code;
  
  if(GptUtils.exists(event.which)) {
    code = event.which;
  } else if (GptUtils.exists(event.keyCode)) {
    code = event.keyCode;
  } else {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "fn submitForm: Could not determine key pressed");
    return;
  }
  
  if(code == 13) {
    
    // Getting main search button
    var searchButtonId = "frmSearchCriteria:btnDoSearch";
    var searchButton = document.getElementById(searchButtonId);
    if(!GptUtils.exists(searchButton)){
      GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Could not find button id = " + searchButtonId);
    } else if (!GptUtils.exists(searchButton.click)) {
      GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Could not find click action on id = " + searchButtonId);
    } else {
      searchButton.click();
    }
  } else {
    return true;
  }
}


/*
* Prevents double submission of the form
* @param form The form to check
*/

function checkFormSubmitted(form) {
  // TODO: how did urban do this?
  if(!GptUtils.exists(form)) {
    GptUtils.logl(GptUtils.log.Level.WARNING, 
         "Form given not valid while checking  ");
    return false;
  }
  if(!GptUtils.exists(form.submitted) || form.submitted == false){
    form.submitted = true;
    setTimeout ( "uncheckForm()", 5000 );
    return true;
    
  } 
  
  return false;
}

/**
Aids in timed form submission to stop the events overflow
in JSF
**/
function uncheckForm() {
  var eForm = document.getElementById("frmSearchCriteria");
  if(eForm != null && GptUtils.exists(eForm.submitted)) {
    eForm.submitted = false;
  }
}

</script>
</f:verbatim>

<% // layout the page %>
<h:form id="frmSearchCriteria" 
  
  onkeypress="javascript:return scSubmitForm(event, this);" 
  onsubmit="return checkFormSubmitted(this);" >
 	<% // two columns containing criteria/mysearches and results %>
	<h:panelGrid id="spMain" columns="2" styleClass="columnsTable">
		<h:panelGroup>
			<jsp:include page="/catalog/search/criteria.jsp" />
		</h:panelGroup>
		<h:panelGroup>
			<h:panelGroup rendered="#{not PageContext.roleMap['anonymous']}">
				<jsp:include page="/catalog/search/mysearches.jsp" />
			</h:panelGroup>
			<h:panelGroup id="pngResults">
			  <jsp:include page="/catalog/search/results.jsp" />
			</h:panelGroup>
		</h:panelGroup>
	</h:panelGrid>

</h:form>
<f:verbatim>
  <iframe id="frmDistSearch" width="0px"  
    onload="javascript:distributedSearchDone()"
    style="border:0px; visibility:hidden; display: none;">
  </iframe>
</f:verbatim>


