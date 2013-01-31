/* See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * Esri Inc. licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * gpt.js
 * GPT JavaScript library.
 */

dojo.require("esri.map");
dojo.require("esri.layers.graphics");
dojo.require("esri.layers.osm");
dojo.require("esri.layers.wms");
if (esri.version>=2.5) {
  dojo.require("esri.layers.wmts");
}

function GptUtil() {}

GptUtil.getTarget = function(e) {
  if (!e) e = window.event;
  return (e.srcElement) ? e.srcElement : e.target;
}

dojo.declare("GptMapConfig", null, {

  mapServiceURL: "",
  mapServiceType: "dynamic",
  mapVisibleLayers: "[]",
  mapElementId: "interactiveMap",
  mapToolName: "deactivate",
  mapToolbarId: "mapToolbar",
  mapInitialExtent: null,
  
  geometryServiceURL: null,
  
  inputEnvelopeXMinId: null,
  inputEnvelopeYMinId: null,
  inputEnvelopeXMaxId: null,
  inputEnvelopeYMaxId: null,
  
  locatorURL: "",
  locatorSingleFieldParameter: "street",
  locatorInputId: "mapInput-locate",
  locatorCandidatesId: "locatorCandidates",
  locatorGraphicURL: null,
  locatorGraphicWidth: 28,
  locatorGraphicHeight: 28,
  locatorNomatch: "No matching place was located.",
  locatorInfoTitle: "Location",
  locatorInfoContent: "${address}<br/>Score: ${score}"
  
});

dojo.declare("GptMap", null, {
  _agsMap: null,
  _agsGSVC: null,
  _defaultExtent: null,
  _this: this,
  _initialExtent: null,

  constructor: function() {
    this.initialize = dojo.hitch(this,this.initialize);
    this.addGraphic = dojo.hitch(this,this.addGraphic);
    this.centerAt = dojo.hitch(this,this.centerAt);
    this.clearGraphics = dojo.hitch(this,this.clearGraphics);
    this.clearGraphicsByTag = dojo.hitch(this,this.clearGraphicsByTag);
    this.getAgsMap = dojo.hitch(this,this.getAgsMap);
    this.getDefaultExtent = dojo.hitch(this,this.getDefaultExtent);
    this.getGeometrySvc = dojo.hitch(this,this.getGeometrySvc);
    this.getWorldExtent = dojo.hitch(this,this.getWorldExtent);
    this.isGCSWkid = dojo.hitch(this,this.isGCSWkid);
    this.isMapProjected = dojo.hitch(this,this.isMapProjected);
    this.onExtentChange = dojo.hitch(this,this.onExtentChange);
    this.onMapServiceLayerLoaded = dojo.hitch(this,this.onMapServiceLayerLoaded);
    this.projectExtent = dojo.hitch(this,this.projectExtent);
    this.projectExtents = dojo.hitch(this,this.projectExtents);
    this.projectGCSExtent = dojo.hitch(this,this.projectGCSExtent);
    this.projectGCSExtents = dojo.hitch(this,this.projectGCSExtents);
    this.projectMapExtentToGSC = dojo.hitch(this,this.projectMapExtentToGSC);
    this.projectedExtentAsPolygon = dojo.hitch(this,this.projectedExtentAsPolygon);
    this.reposition = dojo.hitch(this,this.reposition);
    this.zoom = dojo.hitch(this,this.zoom);
    this.zoomToDefault = dojo.hitch(this,this.zoomToDefault);
    this.zoomToGCSExtent = dojo.hitch(this,this.zoomToGCSExtent);
    this.zoomToWorld = dojo.hitch(this,this.zoomToWorld);
    this.zoomToInitial = dojo.hitch(this,this.zoomToInitial);
    this.onMapLoaded = dojo.hitch(this,this.onMapLoaded);
  },
  
  initialize: function(config) {
    if ((config.geometryServiceURL != null) && (config.geometryServiceURL.length > 0)) {
     this._agsGSVC = new esri.tasks.GeometryService(config.geometryServiceURL);
    }
    
    var service = null;
     
    if (config.mapServiceType == "openstreet") {
      service = new esri.layers.OpenStreetMapLayer();
    } else if ((config.mapServiceURL != null) && (config.mapServiceType != null)) {
      if (config.mapServiceType == "dynamic") {
        service = new esri.layers.ArcGISDynamicMapServiceLayer(config.mapServiceURL);
        // changed to JPG, because IE can not handle PNG24 properly (problems with paning)
        service.setImageFormat("jpg");
      } else if (config.mapServiceType == "tiled") {
        service = new esri.layers.ArcGISTiledMapServiceLayer(config.mapServiceURL);
      } else if (config.mapServiceType == "wms") {
        service = new esri.layers.WMSLayer(config.mapServiceURL);
        if (esri.version < 2.4) {
          service.extent = new esri.geometry.Extent({"xmin":-180,"ymin":-90,"xmax":180,"ymax":90,"spatialReference":{"wkid":4326}});
        }
        service.setVisibleLayers(eval(config.mapVisibleLayers));  
        service.setImageFormat("png");
      } else if (config.mapServiceType == "wmts") {
        var layerInfo = new esri.layers.WMTSLayerInfo({
          identifier: "world",
          tileMatrixSet: "EPSG:4326",
          format: "png"
        });
        var options = {
          serviceMode: "KVP",
          layerInfo: layerInfo
        };
        service = new esri.layers.WMTSLayer(config.mapServiceURL,options);
      }
    }

    if ((service != null) && (config.mapElementId != null)) {
      this._agsMap = new esri.Map(config.mapElementId);
      this._agsMap.graphics = new esri.layers.GraphicsLayer();
      dojo.connect(service,"onLoad",this,"onMapServiceLayerLoaded");
      dojo.connect(this._agsMap,"onLoad",this,"onMapLoaded");
      dojo.connect(this._agsMap,"onExtentChange",this,"onExtentChange");
      this._agsMap.addLayer(service);
      if (config.mapServiceType == "openstreet") {
        dojo.create("div",{"class": "openstreetmap", innerHTML: "Map data © OpenStreetMap contributors, CC-BY-SA"},config.mapElementId+"_layers","last");
      }
    }
    
    if (config.mapInitialExtent!=null && config.mapInitialExtent.length>0) {
      try {
        this._initialExtent = eval("new esri.geometry.Extent({"+config.mapInitialExtent+"})");
      } catch (err) {
        this._initialExtent = null;
      }
    }
  },

  onMapLoaded: function() {
    if (this._agsMap != null && this._defaultExtent == null) {
      this._defaultExtent = this._agsMap.extent;
    }
  },
  
  addGraphic: function(graphic) {
    if ((this._agsMap != null) && (graphic != null)) {
      this._agsMap.graphics.add(graphic);
    }
  },
  
  centerAt: function(geometry) {
    if (this._agsMap != null)
      this._agsMap.centerAt(geometry);
  },
  
  clearGraphics: function() {
    if (this._agsMap != null){// && this._agsMap.constructor.prototype["graphics"])
      this._agsMap.graphics.clear();
    }
  },
  
  clearGraphicsByTag: function(sTag) {
    if (this._agsMap != null) {
      var aGfx = this._agsMap.graphics.graphics;
      var aRmGfx = new Array();
      for (var i=0, n=aGfx.length; i<n; i++) {
        var graphic = aGfx[i];
        if ((graphic.gptTag != null) && (graphic.gptTag == sTag)) {
          aRmGfx[aRmGfx.length] = graphic;
        }
      }
      for (i=0, n=aRmGfx.length; i<n; i++) {
        this._agsMap.graphics.remove(aRmGfx[i]);
      }
    }
  },
  
  getAgsMap: function() {
    return this._agsMap;
  },
  
  getDefaultExtent: function() {
    return this._defaultExtent;
  },
  
  getGeometrySvc: function() {
    return this._agsGSVC;
  },
    
  getWorldExtent: function getWorldExtent() {
    var ref = new esri.SpatialReference({wkid:4326});
    return new esri.geometry.Extent(-179.9999,-90,179.9999,90,ref);
  },
  
  isGCSWkid: function(wkid) {
    if (wkid == null) return false;
    if ((wkid >=   4000) && (wkid <   5000)) return true;
    if ((wkid >=  37000) && (wkid <  38000)) return true;
    if ((wkid >= 104000) && (wkid < 105000)) return true;
    return false;
  },
  
  isMapProjected: function() {
    var mwkid;
    if ((this._agsMap != null) && (this._agsMap.spatialReference != null)) {
      return !this.isGCSWkid(this._agsMap.spatialReference.wkid);
    }
    return false;
  },
  
  onExtentChange: function(extent) {
	  
  },
  
  onMapServiceLayerLoaded: function(serviceLayer) {},
  
  projectExtent: function(extent,srefFrom,srefTo,callback) {
    if (extent == null) this.projectExtents(null,srefFrom,srefTo,callback);
    else this.projectExtents([extent],srefFrom,srefTo,callback);
  },
  
  projectExtents: function(extents,srefFrom,srefTo,callback) {
    var i, n, ext, gfx = null, xmin, xmax, ymin, ymax;
    var toWkid;
    var iToWkid = 0;
    var bIsWebMercator = false;
    
    if(srefTo != 'undefined' 
    	&& srefTo != null && typeof(srefTo.wkid) == 'number'){
    	iToWkid = srefTo.wkid; 
    }
    bIsWebMercator = (iToWkid == 102100 || iToWkid == 3857 || iToWkid == 102113); 
    if (extents != null) {
      gfx = new Array();
      for (i=0;i<extents.length;i++) {
        n = gfx.length; ext = extents[i];
        var currSrefFrom = srefFrom!=null? srefFrom: ext.spatialReference;
        xmin = ext.xmin; xmax = ext.xmax;
        ymin = ext.ymin; ymax = ext.ymax;
        if (xmin == -180) xmin = -179.9999;
        if (xmax == 180) xmax = 179.9999;
        if(bIsWebMercator == true) {
        	if(ymax > 85) {
        		ymax = 85;
        	}
        	if(ymin < -85) {
        		ymin = -85;
        	}
        	if(ymin > 85) {
        		ymin = 85;
        	}
        	if(ymax < -85) {
        		ymax = -85;
        	}
        }
        if(esri.version < 2 ) {
          gfx[n] = new esri.Graphic();  
          gfx[n+1] = new esri.Graphic();
          gfx[n+2] = new esri.Graphic();
          gfx[n+3] = new esri.Graphic();
          gfx[n].setGeometry(new esri.geometry.Point(xmin,ymin,currSrefFrom));
          gfx[n+1].setGeometry(new esri.geometry.Point(xmin,ymax,currSrefFrom));
          gfx[n+2].setGeometry(new esri.geometry.Point(xmax,ymax,currSrefFrom));
          gfx[n+3].setGeometry(new esri.geometry.Point(xmax,ymin,currSrefFrom));
        } else {
          gfx[n] = new esri.geometry.Point(xmin,ymin,currSrefFrom);
          gfx[n+1] = new esri.geometry.Point(xmin,ymax,currSrefFrom);
          gfx[n+2] = new esri.geometry.Point(xmax,ymax,currSrefFrom);
          gfx[n+3] = new esri.geometry.Point(xmax,ymin,currSrefFrom);	
        }
        if(ext.gptMViewIndex) {
          gfx[n].gptMViewIndex = ext.gptMViewIndex; 
          gfx[n+1].gptMViewIndex = ext.gptMViewIndex;
          gfx[n+2].gptMViewIndex = ext.gptMViewIndex;
          gfx[n+3].gptMViewIndex = ext.gptMViewIndex;
        }
      }
      //for (i=0;i<gfx.length;i++) alert(gfx[i].geometry.x+" gfx "+gfx[i].geometry.y);
    }
    if ((this._agsGSVC == null) || (gfx == null) || (gfx.length == 0) 
    		|| (srefFrom == null) || (srefTo == null)) {
      if(esri.version >= 2) {
    	  for(var i = 0; i < gfx.length; i++ ) {
    		  var point = gfx[i];	
              gfx[i] = new esri.Graphic();
    	      gfx[i].setGeometry(point);
    	  }
      }
      callback(gfx);
    } else {
      //alert("projecting...");
      //this._agsGSVC.project(gfx,srefTo,callback);
    	this._agsGSVC.project(gfx,srefTo,dojo.hitch(this, 
    	 function(arrPoints){
    		if(typeof(arrPoints) == 'undefined' && arrPoints != null) {
    			return;
    		}
    		var gfx = new Array();
    		for(var i = 0; i < arrPoints.length; i++ ) {
    		  var index = gfx.length;	
              gfx[index] = new esri.Graphic();
    	      gfx[index].setGeometry(arrPoints[i]);
    		}
    		callback(gfx);
    	 })
    	);
    }
  },


  projectGCSExtent: function(extent,callback) {
    var srefFrom = new esri.SpatialReference({wkid:4326}), srefTo = null;
    if (this.isMapProjected()) srefTo = this._agsMap.spatialReference;
    this.projectExtent(extent,srefFrom,srefTo,callback);
  },

  projectGCSExtents: function(extents,callback) {
    var srefFrom = new esri.SpatialReference({wkid:4326}), srefTo = null;
    if (this.isMapProjected()) srefTo = this._agsMap.spatialReference;
    this.projectExtents(extents,srefFrom,srefTo,callback);
  },
  
  projectMapExtentToGSC: function(extent,callback) {
    var srefFrom = null, srefTo = new esri.SpatialReference({wkid:4326});
    if (this.isMapProjected()) srefFrom = this._agsMap.spatialReference;
    this.projectExtent(extent,srefFrom,srefTo,callback);
  },
  
  projectedExtentAsPolygon: function(gfx) {
    var i, poly = null, pts = new Array(), sref = null;
    if (gfx != null) {
      for (i=0;i<gfx.length;i++) {
        pts[i] = gfx[i].geometry;
        if ((pts[i] == null) || isNaN(pts[i].x) || isNaN(pts[i].y)) {
          pts = null;
          break;
        }
        if (sref == null)
          sref = pts[i].spatialReference;
      }
      if ((pts != null) && (pts.length > 0)) {
        poly = new esri.geometry.Polygon(sref);
        // this has been added to adjust to the JS API 1.5 requirement to
        // have all rings as closed polygons (last point is equal to the first)
        pts[pts.length] = pts[0];
        poly.addRing(pts);
      }
    }
    return poly;
  },
  
  reposition: function() {
    if (this._agsMap != null) {
      this._agsMap.reposition();
      
      // reposition function for jsapi 1.3/1.4 has an issue with scrollbars,
      // override
      var bOverrideReposition = true;
      if (bOverrideReposition) {
        var pos = dojo.coords(this._agsMap.container,true);
        var brdr = dojo._getBorderExtents(this._agsMap.container);
        this._agsMap.position.setX(pos.x + brdr.l);
        this._agsMap.position.setY(pos.y + brdr.t);
        this._agsMap.onReposition(this._agsMap.position.x, this._agsMap.position.y);
      }
    
    }
  },
    
  zoom: function(extent) {
    if ((this._agsMap != null) && (extent != null)) this._agsMap.setExtent(extent);
  },
  
  zoomToDefault: function() {
    if (this._defaultExtent == null) this._defaultExtent = this.getWorldExtent();
    this.zoom(this._defaultExtent);
  },
  
  zoomToGCSExtent: function(extent,bExpand) {
    if (this._agsMap == null) return;
    var agsMap = this._agsMap;
    var env = extent;
    if (env != null) {
      // handle extents wrapped around the dateline if required
      if (env.xmin > env.xmax) {
        var ref = new esri.SpatialReference({wkid:4326});
        env = new esri.geometry.Extent(-179.9999,env.ymin,179.9999,env.ymax,ref);
      }
    }
    this.projectGCSExtent(env, dojo.hitch(this, function(gfx) {
      var ext;
      var poly = this.projectedExtentAsPolygon(gfx);
      if (poly != null) ext = poly.getExtent();
      if (ext != null) {
        if (bExpand) ext = ext.expand(1.1);
        agsMap.setExtent(ext);
      }
    }));
  },
  
  zoomToWorld: function() {
    this.zoomToGCSExtent(this.getWorldExtent(),false);
  },
  
  zoomToInitial: function() {
    if (this._initialExtent!=null) {
      this.zoomToGCSExtent(this._initialExtent,true);
    }
  }
   
});

dojo.declare("GptMapToolbar", null, {
  _agsToolbar: null,
  _gptMap: null,
  _activeToolName: "deactivate",
  _toolImages: new Array(),

  constructor: function() {
    this.initialize = dojo.hitch(this,this.initialize);
    this.onButtonAccessed = dojo.hitch(this,this.onButtonAccessed);
    this.onButtonClicked = dojo.hitch(this,this.onButtonClicked);
    this.onButtonDown = dojo.hitch(this,this.onButtonDown);
    this.onButtonOut = dojo.hitch(this,this.onButtonOut);
    this.onButtonUp = dojo.hitch(this,this.onButtonUp);
    this.onDrawEnd = dojo.hitch(this,this.onDrawEnd);
    this.onDrawInputEnvelope = dojo.hitch(this,this.onDrawInputEnvelope);
    this.onMapButtonClicked = dojo.hitch(this,this.onMapButtonClicked);
    this.onMapLoaded = dojo.hitch(this,this.onMapLoaded);
    this.onToolClicked = dojo.hitch(this,this.onToolClicked);
    this.setActiveTool = dojo.hitch(this,this.setActiveTool);
  },

  initialize: function(config, gptMap) {
    this._gptMap = gptMap;
    this._activeToolName = config.mapToolName;
    this._agsToolbar = new esri.toolbars.Draw(gptMap.getAgsMap(),{showTooltips: false});
    this._toolImages = new Array();
    dojo.connect(this._gptMap,"onMapLoaded",this,"onMapLoaded");
    dojo.connect(this._agsToolbar,"onDrawEnd",this,"onDrawEnd");
    
    var elToolbar = dojo.byId(config.mapToolbarId); 
    if (elToolbar != null) {
      var aImgs = elToolbar.getElementsByTagName("img");
      if (aImgs != null) {
        for (var i=0;i<aImgs.length;i++) {
          var el = aImgs[i];
          if (el && el.id) {
            if (el.id.indexOf("mapButton-") != -1) {
              dojo.connect(el,"onmousedown",this,"onButtonDown");
              dojo.connect(el,"onmouseout",this,"onButtonOut");
              dojo.connect(el,"onmouseup",this,"onButtonUp");
              dojo.connect(el,"onclick",this,"onButtonClicked");
            } else if (el.id.indexOf("mapTool-") != -1) {
              this._toolImages[this._toolImages.length] = el;
              dojo.connect(el,"onclick",this,"onToolClicked");
            }
          }
        }
      }
    }
    
    this.setActiveTool(this._activeToolName);
  },
  
  onButtonAccessed: function(element,bShowOn) {
    if ((element != null) && (element.src != null)) {
      var n = null;
      if (bShowOn) {
        n = element.src.indexOf("-off.gif");
        if (n != -1) element.src = element.src.substring(0,n)+"-on.gif";
      } else {
        n = element.src.indexOf("-on.gif");
        if (n != -1) element.src = element.src.substring(0,n)+"-off.gif";
      }
    }
  },
  
  onButtonClicked: function onButtonClicked(e) {
    if (!e) e = window.event;
    var el = (window.event) ? e.srcElement : e.target; 
    if (el && el.id) {
      var n =  el.id.indexOf("mapButton-");
      if (n != -1) {
        var sButtonName = el.id.substr(n+10);
        this.onMapButtonClicked(sButtonName);
      }
    }
  },
  
  onButtonDown: function(e) {
    this.onButtonAccessed(GptUtil.getTarget(e),true);
  },
  
  onButtonOut: function(e) {
    this.onButtonAccessed(GptUtil.getTarget(e),false);
  },
  
  onButtonUp: function(e) {
    this.onButtonAccessed(GptUtil.getTarget(e),false);
  },
  
  onDrawEnd: function(geometry) {
    if (this._activeToolName != null) {
      switch (geometry.type) {
        case "extent":
          if (this._activeToolName == "drawInputEnvelope") this.onDrawInputEnvelope(geometry);
          break;
      }
    }
  },
  
  onDrawInputEnvelope: function(geometry) {},
  
  onMapButtonClicked: function(sButtonName) {},
  
  onMapLoaded: function() {
    this.setActiveTool(this._activeToolName);
  },
  
  onToolClicked: function(e) {
    if (!e) e = window.event;
    var el = (window.event) ? e.srcElement : e.target; 
    if (el && el.id) {
      var n =  el.id.indexOf("mapTool-");
      if (n != -1) {
        var sToolName = el.id.substr(n+8);
        this.setActiveTool(sToolName);
      }
    }
  },
  
  setActiveTool: function(sToolName) {
    this._activeToolName = sToolName;
    if (this._activeToolName == null) this._activeToolName = "deactivate";
    if (this._agsToolbar != null) {
      switch (this._activeToolName) {
        case "drawInputEnvelope":
          this._agsToolbar.activate(esri.toolbars.Draw.EXTENT);
          break;
        case "deactivate":
          this._agsToolbar.deactivate();
          break;
      }
      for (var i=0;i<this._toolImages.length;i++) {
        var el = this._toolImages[i];
        var n =  el.id.indexOf("mapTool-");
        if (n != -1) {
          var s = el.id.substr(n+8);
          this.onButtonAccessed(el,(s == this._activeToolName));
        }
      }
    }
  }
  
});

dojo.declare("GptInputEnvelope", null, {
  _gptMap: null,
  _inputElements: null,
  _isHidden: true,
  _this: this,

  constructor: function() {
    this.initialize = dojo.hitch(this,this.initialize);
    this.connect = dojo.hitch(this,this.connect);
    this.getInputEnvelope = dojo.hitch(this,this.getInputEnvelope);
    this.highlightInputEnvelope = dojo.hitch(this,this.highlightInputEnvelope);
    this.onDrawInputEnvelope = dojo.hitch(this,this.onDrawInputEnvelope);
    this.setInputEnvelope = dojo.hitch(this,this.setInputEnvelope);
    this.zoomToInputEnvelope = dojo.hitch(this,this.zoomToInputEnvelope);
  },

  initialize: function(config, gptMap, isHidden) {
    this._gptMap = gptMap;
    this.connect(isHidden,config.inputEnvelopeXMinId,config.inputEnvelopeYMinId, 
         config.inputEnvelopeXMaxId,config.inputEnvelopeYMaxId);
  },
  
  connect: function(isHidden, inputXMinId, inputYMinId, inputXMaxId, inputYMaxId) {
    var i, el, elXMin, elYMin, elXMax, elYMax;
    this._isHidden = isHidden;
    this._inputElements = null;
    if ((inputXMinId != null) && (inputYMinId != null) && (inputXMaxId != null) && (inputYMaxId != null)) {
      elXMin = document.getElementById(inputXMinId);
      elYMin = document.getElementById(inputYMinId);
      elXMax = document.getElementById(inputXMaxId);
      elYMax = document.getElementById(inputYMaxId);
      if ((elXMin != null) && (elYMin != null) && (elXMax != null) && (elYMax != null)) {
        this._inputElements = new Array(elXMin,elYMin,elXMax,elYMax);
        if (!this._isHidden) {
          if (this._gptMap != null) dojo.connect(this._gptMap,"onMapLoaded",this,"highlightInputEnvelope");
          for (i=0;i<this._inputElements.length;i++) {
            el = this._inputElements[i];
            dojo.connect(el,"onchange",this,"highlightInputEnvelope");
            dojo.connect(el,"onkeyup",this,"highlightInputEnvelope");
          }
        }
      }
    }
  },
  
  getInputEnvelope: function() {
    var env = null;
    if (this._inputElements != null) {
      var nXMin = parseFloat(this._inputElements[0].value);
      var nYMin = parseFloat(this._inputElements[1].value);
      var nXMax = parseFloat(this._inputElements[2].value);
      var nYMax = parseFloat(this._inputElements[3].value);
      if (!isNaN(nXMin) && !isNaN(nYMin) && !isNaN(nXMax) && !isNaN(nYMax)) {
        //if ((nXMax > nXMin) && (nYMax > nYMin)) {
        if (nYMax > nYMin) {
          var ref = new esri.SpatialReference({wkid:4326});
          env = new esri.geometry.Extent(nXMin,nYMin,nXMax,nYMax,ref);
        }
      }
    }
    return env;
  },
  
  highlightInputEnvelope: function() {
    if (!this._isHidden && (this._gptMap != null)) {
      var env = this.getInputEnvelope();
      this._gptMap.clearGraphicsByTag("inputEnvelope");
      if (env != null) { 
        var envs = new Array(env);
        
        // wrap around the dateline if required
        if (env.xmin > env.xmax) {
          var ref = new esri.SpatialReference({wkid:4326});
          var envLeft = new esri.geometry.Extent(-179.9,env.ymin,env.xmax,env.ymax,ref);
          var envRight = new esri.geometry.Extent(env.xmin,env.ymin,179.9,env.ymax,ref);
          envs = new Array(envLeft,envRight);
        }
        
        for (var i=0;i<envs.length;i++) {
	        this._gptMap.projectGCSExtent(envs[i], dojo.hitch(this, function(gfx) {
	          var graphic, symbol;
	          var poly = this._gptMap.projectedExtentAsPolygon(gfx);
	          if (poly != null) {
	            symbol = new esri.symbol.SimpleFillSymbol(esri.symbol.SimpleFillSymbol.STYLE_SOLID, 
	                     new esri.symbol.SimpleLineSymbol(esri.symbol.SimpleLineSymbol.STYLE_SOLID, 
	                     new dojo.Color([255,0,0]), 2), new dojo.Color([255,255,0,0.3]));
	            graphic =  new esri.Graphic();
	            graphic.setGeometry(poly);
	            graphic.setSymbol(symbol);
	            graphic.gptTag = "inputEnvelope";
	            this._gptMap.addGraphic(graphic);
	          }
	        }));
	      }
	       
      }
    }
  },
  
  onDrawInputEnvelope: function(geometry) {
    if (!this._isHidden && (this._gptMap != null)) {
     this._gptMap.projectMapExtentToGSC(geometry, dojo.hitch(this, function(gfx) {
        var ext, xmin, ymin, xmax, ymax;
        var poly = this._gptMap.projectedExtentAsPolygon(gfx);
        if (poly != null) {
          //ext = GptUtil.calcExtent(poly);
          ext = poly.getExtent();
          if (ext != null) {
            xmin = ext.xmin; ymin = ext.ymin; xmax = ext.xmax; ymax = ext.ymax;
            if (xmin < -180) xmin = -180;
            if (xmax >  180) xmax = 180;
            if (ymin < -90)  ymin = -90;
            if (ymax >  90)  ymax = 90;
            ext = new esri.geometry.Extent(xmin,ymin,xmax,ymax,new esri.SpatialReference({wkid:4326}));
            this.setInputEnvelope(ext);
            this.highlightInputEnvelope();
          }
        } 
      }));
     
    }
  },
    
  setInputEnvelope: function(extent) {
    if ((this._inputElements != null) && (extent != null)) {
      var rnd = Math.pow(10,4);
      this._inputElements[0].value = Math.round(extent.xmin * rnd) / rnd;
      this._inputElements[1].value = Math.round(extent.ymin * rnd) / rnd;
      this._inputElements[2].value = Math.round(extent.xmax * rnd) / rnd;
      this._inputElements[3].value = Math.round(extent.ymax * rnd) / rnd;
      this.onSetInputEnvelope(this.getInputEnvelope());
    }
  },
  
  onSetInputEnvelope: function(extent) {},
  
  zoomToInputEnvelope: function() {
    if (this._gptMap != null) this._gptMap.zoomToGCSExtent(this.getInputEnvelope(),true);
  }   
   
});

dojo.declare("GptLocator", null, {
  _config: null,
  _gptMap: null,
  _agsLocator: null,
  _agsInfoTemplate: null,
  _candidatesElement: null,
  _inputElement: null,
  _singleFieldParameter: "",

  constructor: function() {
    this.initialize = dojo.hitch(this,this.initialize);
    this.checkForCoordinates = dojo.hitch(this,this.checkForCoordinates);
    this.clearCandidates = dojo.hitch(this,this.clearCandidates);
    this.locate = dojo.hitch(this,this.locate);
    this.locateCandidate = dojo.hitch(this,this.locateCandidate);
    this.onAddressToLocationsComplete = dojo.hitch(this,this.onAddressToLocationsComplete);
    this.onCandidateClicked = dojo.hitch(this,this.onCandidateClicked);
    this.onKeyPress = dojo.hitch(this,this.onKeyPress);
    this.toggleCandidates = dojo.hitch(this,this.toggleCandidates);
    this.reposition = dojo.hitch(this,this.reposition);
  },

  initialize: function(config, gptMap) {
    this._config = config;
    this._gptMap = gptMap;
    this._inputElement = null;
    this._candidatesElement = null;
    
    if (config.locatorInputId != null) {
      this._inputElement = document.getElementById(config.locatorInputId);
      if (this._inputElement != null) {
        //dojo.connect(_inputElement,"onkeypress",this,"onKeyPress");
      }
    }
    if (config.locatorCandidatesId != null) {
      this._candidatesElement = document.getElementById(config.locatorCandidatesId);
    }
    if (config.locatorURL != null) {
      this._agsLocator = new esri.tasks.Locator(config.locatorURL);
      // the template is causing issues and in this case not providing much value, it's been disabled
      //this._agsInfoTemplate = new esri.InfoTemplate(config.locatorInfoTitle,config.locatorInfoContent);
      dojo.connect(this._agsLocator,"onAddressToLocationsComplete",this,"onAddressToLocationsComplete");
    }
    if ((config.locatorSingleFieldParameter == null) || (config.locatorSingleFieldParameter.length == 0)) {
      this._singleFieldParameter = "place";
    } else {
      this._singleFieldParameter = config.locatorSingleFieldParameter;
    }
  },
  
  checkForCoordinates: function(sTxt) {
    var aTxt = sTxt.split(" ");
    //if (aTxt.length != 2) aTxt = sTxt.split(",");
    if (aTxt.length == 2) {
      var s1 = dojo.trim(aTxt[0]);
      var s2 = dojo.trim(aTxt[1]);
      if ((s1.length > 0) && (s2.length > 0)) {
        var dX = parseFloat(s1);
        var dY = parseFloat(s2);
        if (!isNaN(dX) && !isNaN(dY)) {
          if ((dX >= -180) && (dX <= 180) && (dY >= -90) && (dY <= 90)) {
             var ref = new esri.SpatialReference({wkid:4326});
             var pt = new esri.geometry.Point(dX,dY,ref);
             return pt;
          }
        }
      }
    }
    return null;
  },
  
  clearCandidates: function() {
    if (this._candidatesElement != null) {
      if (this._candidatesElement.hasChildNodes()) {
        while (this._candidatesElement.childNodes.length >= 1) {
          this._candidatesElement.removeChild(this._candidatesElement.firstChild);
        }
        this.reposition();
      }
    }
  },
  
  locate: function() {
    if ((this._gptMap != null) && (this._agsLocator != null) && (this._inputElement != null)) {
      this._gptMap.clearGraphicsByTag("locator");
      this.toggleCandidates(false);
      this.clearCandidates();
      var sTxt = dojo.trim(this._inputElement.value);
      var pt = this.checkForCoordinates(sTxt);
      if (pt != null) {
       var candidate = {address: pt.x+" "+pt.y, location: pt, score: 100};
       this.locateCandidate(candidate,false);
      } else {
        var params = {};
        eval("params."+this._singleFieldParameter+" = sTxt");
        this._agsLocator.addressToLocations(params);
      }
      this.reposition();
    }
  },
  
  locateCandidate: function(candidate, bSetTextValue) {
    if ((this._gptMap == null) || (candidate == null)) return;

    this._gptMap.clearGraphicsByTag("locator");
 
    var point = {};
    dojo.mixin(point, candidate.location);

    var srefFrom = null, srefTo = null;
    if (point.constructor.prototype["spatialReference"]) {
      srefFrom = point.spatialReference;
    }
    if (srefFrom == null) {
      srefFrom = new esri.SpatialReference({wkid:4326})
    }
    if (this._gptMap.isMapProjected()) {
      srefTo = this._gptMap.getAgsMap().spatialReference;
    }
    
    var ext = new esri.geometry.Extent(point.x,point.y,point.x,point.y,srefFrom);

    this._gptMap.projectExtent(ext, srefFrom, srefTo, dojo.hitch(this, function(gfx) {
      var ext, point, graphic, symbol;
      var poly = this._gptMap.projectedExtentAsPolygon(gfx);
      if (poly != null) {
        ext = poly.getExtent();
      }
      if (ext != null) {
        point = new esri.geometry.Point(ext.xmin,ext.ymin,this._gptMap.getAgsMap().spatialReference);
        symbol = new esri.symbol.PictureMarkerSymbol(this._config.locatorGraphicURL,
                 this._config.locatorGraphicWidth,this._config.locatorGraphicHeight);
        graphic = new esri.Graphic(point,symbol,null,this._agsInfoTemplate);
        graphic.gptTag = "locator";
        this._gptMap.addGraphic(graphic);
        this._gptMap.centerAt(point);
      }
    }));
  },
  
  onAddressToLocationsComplete: function(candidates) {
    if ((this._gptMap != null) && (candidates != null)) {
      var aCandidates = new Array();
      var a100 = new Array();
      var a80 = new Array();
      for (var i=0, n=candidates.length; i<n; i++) {
        var candidate = candidates[i]; 
        if (candidate.score == 100) {
          a100[a100.length] = candidate; 
          //break;
        }
        if (candidate.score > 80) {
          a80[a80.length] = candidate;
        }
      }   
      if (a100.length > 0) aCandidates = a100;
      else if (a80.length > 0) aCandidates = a80;
      else aCandidates = candidates;
      
      if (aCandidates.length == 0) {
        var msg = document.createElement("h4");
        msg.appendChild(document.createTextNode(this._config.locatorNomatch));
        this._candidatesElement.appendChild(msg);
      } else if (aCandidates.length == 1) {
        this.locateCandidate(aCandidates[0],false);
      } else {
        var div = document.createElement("div");
        div.setAttribute("id","divCanditateCollection");
        this._candidatesElement.appendChild(div);
        for (i=0, n=aCandidates.length; i<n; i++) {
          if (i > 10) break;
          candidate = aCandidates[i];
          if (this._candidatesElement != null) {
            var link = document.createElement("a");
            link.setAttribute("href","javascript:void(0);");
            link.gptCandidate = candidate;
            link.appendChild(document.createTextNode(candidate.address));
            dojo.connect(link,"onclick",this,"onCandidateClicked");
            div.appendChild(link);
          }
        }
      }
    }
    
    if (this._candidatesElement != null) {
      this.toggleCandidates(this._candidatesElement.childNodes.length > 0);
    }
  },
  
  onCandidateClicked: function(e) {
    if (!e)
      e = window.event;
    var el = (window.event) ? e.srcElement : e.target;
    if (el && el.gptCandidate)
      this.locateCandidate(el.gptCandidate,true);
    this.clearCandidates();
    this.toggleCandidates(false);
  },
        
  onKeyPress: function(e) {
    if (!e)
      e = window.event;
    var nKey = (e.keyCode) ? e.keyCode : e.which; 
    if ((nKey != null) && (nKey == 13))
      this.locate();
    return false;
  },
  
  toggleCandidates: function(bDisplay) {
    if (this._candidatesElement != null) {
      if (bDisplay) this._candidatesElement.style.display = "block";
      else this._candidatesElement.style.display = "none";
    }
    this.reposition();
  },

  reposition: function() {
    if (this._gptMap != null) {
      this._gptMap.reposition();
    }
  }

});


