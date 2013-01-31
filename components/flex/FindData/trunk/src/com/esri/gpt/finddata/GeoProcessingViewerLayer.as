/*See the NOTICE file distributed with
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
*/ 
package com.esri.gpt.finddata
{
	import com.esri.ags.Graphic;
	import com.esri.ags.Map;
	import com.esri.ags.SpatialReference;
	import com.esri.ags.events.GeoprocessorEvent;
	import com.esri.ags.events.LayerEvent;
	import com.esri.ags.geometry.Extent;
	import com.esri.ags.geometry.MapPoint;
	import com.esri.ags.geometry.Multipoint;
	import com.esri.ags.geometry.Polyline;
	import com.esri.ags.layers.ArcGISDynamicMapServiceLayer;
	import com.esri.ags.layers.GraphicsLayer;
	import com.esri.ags.symbols.SimpleFillSymbol;
	import com.esri.ags.symbols.SimpleLineSymbol;
	import com.esri.ags.symbols.SimpleMarkerSymbol;
	import com.esri.ags.symbols.Symbol;
	import com.esri.ags.tasks.Geoprocessor;
	import com.esri.ags.tasks.supportClasses.ParameterValue;
	
	import flash.display.DisplayObject;
	import flash.display.Loader;
	import flash.events.Event;
	import flash.events.MouseEvent;
	import flash.utils.Dictionary;
	
	import mx.collections.ArrayCollection;
	import mx.containers.HBox;
	import mx.containers.TitleWindow;
	import mx.containers.VBox;
	import mx.controls.Button;
	import mx.controls.ColorPicker;
	import mx.controls.Label;
	import mx.controls.Text;
	import mx.controls.TextArea;
	import mx.events.ColorPickerEvent;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.managers.PopUpManager;
	import mx.rpc.Fault;
	import mx.rpc.events.FaultEvent;
	
	/**
	 * Layer that understands how to connecting to the geoprocessing event that
	 * emits native geometries from zip and kml files
    **/
	public class GeoProcessingViewerLayer extends ArcGISDynamicMapServiceLayer
	{
		// class variables =====================================================
		private static const LOG:ILogger =
           Log.getLogger("com.esri.gpt.flexviewer.widgets.AGSGptRSSWidget.mxml");
           
		// instance variables ==================================================
		private var _imageTransparency:Boolean;
		private var _visibleLayers:ArrayCollection; 
		private var _defaultColor:uint;
		private var _attribs:Dictionary = new Dictionary();
		
		private var graphicLineSym:SimpleLineSymbol = 
          new SimpleLineSymbol("solid", 0x0099ff, 0.8, 2);
        private var graphicPolySym:SimpleFillSymbol = 
          new SimpleFillSymbol("solid", 0x7375D8, 0.8, 
            new SimpleLineSymbol("solid", 0x0099ff, 0.8, 2));
        private var graphicMarkerSymbol:SimpleMarkerSymbol =
          new SimpleMarkerSymbol(
            SimpleMarkerSymbol.STYLE_CIRCLE, 15,0x6FED94, 0.8, 0, 0, 0, 
              new SimpleLineSymbol("solid", 0x0099ff, 0.8, 2));  
        private var windowColorPick:TitleWindow = new TitleWindow(); 
        private var _initialExtent:Extent = null;	
        private var _mask:DisplayObject = null;
        private var _visible:Boolean = true;
        	
		public var title:String;
		public var graphicSymbol:Symbol
		public var graphicsLayer:GraphicsLayer;
		private var _map:Map;
		public var urlGpService:String;
        private var _url:String;
        private var _proxyURL:String;
        private var _geomTypes:ArrayCollection = new ArrayCollection();
        
        private var _spatialReference:SpatialReference;
        
        public var lblPoints:String = "Points";
        public var lblLines:String = "Lines";
        public var lblPolygons:String = "Polygons";
        	
		// constructors ======================================================== 
		/*
		* Geoprocessing Viewer layer
		*
		*@parm url The url to the zip or kml/kmz file
		*/
		public function GeoProcessingViewerLayer(url:String = null)
        {
            this.url = url;
            this.addEventListener(Event.ADDED, addEvent, false, 0, true);
            
        }
        
        /**
        * map has been changed. notify the program
        **/
        private function addEvent(event:Event):void {
        	
        	// Below parent lines are undocumented but work
        	
        	if(this.parent as Map) {
        	  this.map = this.parent as Map;
        	} else if (this.parent.parent as Map) {
        	   this.map = this.parent.parent as Map;
        	} else if (this.parent.parent.parent as Map) {
        	   this.map = this.parent.parent.parent as Map;
        	}
        	
        	       
        	
        }
        
       
        
		
		[Inspectable(category="Mapping", defaultValue="true")]
        /**
         * Whether or not the background of the image is transparent.
         * Only valid for image formats that support transparency.
         * 
         * @default true
         */
        override public function get imageTransparency():Boolean
        {
            return _imageTransparency;
        }
        
        override public function set alpha(alpha:Number):void {
        	if(this.graphicsLayer) {
        	  this.graphicsLayer.alpha = alpha;
        	}
        }
        
        
        
       override public function set mask(obj:DisplayObject):void {
       	  super.mask = obj;
          if(this.graphicsLayer != null) {
          	this.graphicsLayer.mask = obj;
          	//this.graphicsLayer.
          	
          	/*var arrColl:ArrayCollection = new ArrayCollection();
          	if(graphicsLayer.graphicProvider is ArrayCollection) {
                arrColl = graphicsLayer.graphicProvider as ArrayCollection;
            }
            var graphic:Graphic = null;
          	for(var i:Number = 0; i < arrColl.length; i++) {
                graphic = arrColl[i] as Graphic;
                
                if(graphic == null) {
                    continue;
                }
                graphic.mask = mask;
                if(i == 1) {
                	break;
                }
                
          	}*/
          	
          }	
        }
        
        /**
        * Returns the initial extent
        * */
        override public function get initialExtent():Extent {
        	return _initialExtent;
        }
        /**
         * @private
         */
        override public function set imageTransparency( value:Boolean ):void
        {
            _imageTransparency = value;
            invalidateLayer();
        }
        
        /**
         * The URL to proxy the request through.
         */
        override public function get proxyURL():String 
        {
            return _proxyURL;
        }
        /**
         * @private
         */
        override public function set proxyURL( value:String ):void
        {
            _proxyURL = value;
            //_proxyURL = "http://imeda08/FlexProxy/proxy.ashx"
            invalidateLayer();
        }
        
        private function set spatialReference(
          spatialReference:SpatialReference):void {
            this._spatialReference = spatialReference;	
        }
        
        override public function get spatialReference():SpatialReference {
        	return this._spatialReference;
        }
        
        public function set map(map:Map):void {
        	if(map != this._map) {
        		setLoaded(false);
        	}
        	this._map = map;
        	if(graphicsLayer != null) {
        		this._map.addChild(graphicsLayer);
        	}
        	
        }
         
        override public function get map():Map {
        	return this._map;
        }     
        override public function get url():String 
        {
            return _url;
        }
        /**
         * @private
         */
        override public function set url( value:String ):void 
        {
            _url = value;
            
            //_urlChanged = true;
            invalidateProperties();
            
            //_capabilities = null;
            setLoaded(false);
        }
        
        override public function set visible(visible:Boolean):void {
        	super.visible = visible;
            this._visible = visible;
            if(this.graphicsLayer == null) {
            	return;
            }
           	try {
           		graphicsLayer.visible = visible;
                  //this._map.removeLayer(this.graphicsLayer);
            } catch(err:Error) {
                	
            }
            if(visible == true && map.getLayer(this.graphicsLayer.id) == null) {
              this.map.addLayer(this.graphicsLayer);	
            }
            
        }
        
        
        /**
         * Ignored by this class loading an image does not emit a byte image
         * 
         * @private
         */
        override protected function loadMapImage( loader:Loader ):void 
        {
        	if(this.loaded == false) {
        		this.getGeometries();
        	}
        	// No image needed to be emitted.
        }
        
        /**
        *  Gets the geometries from the geometry service
        * 
        * */
        private function getGeometries():void {
        	if(this.graphicsLayer != null) {
        	   graphicsLayer.clear();
        	}
        	
        	
        	var gp:Geoprocessor = new Geoprocessor();
        	gp.url = this.urlGpService;
        	gp.proxyURL = this.proxyURL;
            var params:Object = {
              input_URL: this.url
              //env: {outSR: {wkid: _map.spatialReference.wkid}}
            };
            gp.outSpatialReference = _map.spatialReference;
            //gp.processSpatialReference = _map.spatialReference;
            
            gp.addEventListener(GeoprocessorEvent.EXECUTE_COMPLETE, handlerGpResult,
              false, 0, true);
            gp.addEventListener(FaultEvent.FAULT, handlerGpFault,
              false, 0, true);
            if(this.requestTimeout > 0) {  
              gp.requestTimeout = this.requestTimeout; 
            }
            gp.execute(params); 
            
            
           
        }
        
        /**
        * Handles Geoprocessor result
        * 
        * @param gpEvent The event fault
        * */
        private function handlerGpResult(gpEvent:GeoprocessorEvent):void {
        	try {
        	  this.handlerGpResultWork(gpEvent);
        	  dispatchEvent(new LayerEvent(LayerEvent.LOAD, this));
        	  this.setLoaded(true);
        	} catch(e:Error) {
        		var fault:Fault = new Fault("10000", e.message, e.toString());
        		var faultEvent:FaultEvent = 
        		  new FaultEvent(FaultEvent.FAULT, false, true, fault);
        		return handlerGpFault(faultEvent);
        	} 
        		
        	
        }
        
        /**
        * Helps the function handlerGpResult
        * 
        * @param gpEvent The geprocessing event
        * */
        private function handlerGpResultWork(gpEvent:GeoprocessorEvent, 
            token:Object = null):void {
            	
            if(!graphicsLayer) {
                graphicsLayer = new GraphicsLayer();
                graphicsLayer.visible = this.visible;
                graphicsLayer.alpha = this.alpha;
                graphicsLayer.mask = this.mask;
                graphicsLayer.id = this.id + " [GraphicsLayer]"; 
            }
           	
           	_geomTypes = new ArrayCollection();
           	
            var arrFeatures:ArrayCollection = new ArrayCollection();
            for(var i:Number = 0; i < gpEvent.executeResult.results.length; i++) {
            	var pv:ParameterValue = gpEvent.executeResult.results[i] as ParameterValue;
				if(pv == null) {
					continue;
				}
            	if(pv.paramName == "error_message" && pv.value != null  &&
            	  pv.value != "") {
            	  	dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, 
                        this, new Fault("-1", pv.value as String)));  
            		return;
            	} else if(pv.dataType == "GPFeatureRecordSetLayer" && pv.value && pv.value.features) {
            		arrFeatures.addItem(pv.value.features);
            	}
            }
          
            var extent:Extent = null;
            var index:Number = 0;
            for(var j:Number = 0; j < arrFeatures.length; j++) { 
            	
	            for each (var graphic:Graphic in arrFeatures[j]) {
	            	LOG.debug("Processing graphic #" + index);
	            	if(!_geomTypes.contains(graphic.geometry.type)) {
	            	  _geomTypes.addItem(graphic.geometry.type);	
	            	}
	            	           	
	            	if(pv.value.features.length > index) {
	             	  _attribs[graphic.id] = pv.value.features[index].attributes;
	            	}
	            	if(graphic.geometry is Polyline) {
	            	  graphic.symbol = this.graphicLineSym;
	            	   _defaultColor = this.graphicLineSym.color;
	            	} else if(graphic.geometry is MapPoint || 
	            	  graphic.geometry is Multipoint) {
	            	  graphic.symbol = this.graphicMarkerSymbol;
	            	  _defaultColor = this.graphicMarkerSymbol.color;	
	            	} else {
	                  graphic.symbol = this.graphicPolySym;
	                  _defaultColor = this.graphicPolySym.color;
	            	}
	            	var tmpExtent:Extent = null;
	            	if(graphic.geometry is MapPoint) {
	            		var point:MapPoint = graphic.geometry as MapPoint;
	            		var spatialRef:SpatialReference = point.spatialReference;
	        
	            		if(spatialRef == null || isNaN(spatialRef.wkid)) {
	            			spatialRef = new SpatialReference(4326);
	            			graphic.geometry.spatialReference = spatialRef;
	            		}
	                    tmpExtent = new Extent(point.x-1, point.y-1, 
	                      point.x+1, point.y+1, 
	            		  spatialRef);
	            	} else {
	            		tmpExtent = graphic.geometry.extent;
	            	}
	            	
	            	if(extent == null) {
	            	 extent = tmpExtent;
	            	} else {
	            	 extent = extent.union(tmpExtent);
	            	}
	            	graphicsLayer.add(graphic);  
	            	if(index == 0) {
	                  if(graphic.geometry.spatialReference == null || 
	                    isNaN(graphic.geometry.spatialReference.wkid)) {
	                  	// Some geometries do not have a spatial reference, give
	                    // them 4326
	                  	_spatialReference = new SpatialReference(4326);
	                  	graphicsLayer.spatialReference = new SpatialReference(4326);
	                  	if(ProjectionHandler.isEqualWkids(map.spatialReference.wkid,
	                  	  4326) == false) {
	                  		index++;
	                  		_initialExtent = null;
	                  		extent = null;
	                  		break;
	                  	}
	                  } else {
	                  	graphicsLayer.spatialReference = new SpatialReference(
	                  	     graphic.geometry.spatialReference.wkid);
	                  	_spatialReference = new SpatialReference( 
	                  	     graphic.geometry.spatialReference.wkid);
	                  } 
	                  
	                }
	            	index ++;
	            }
            }
            if(this.map.getLayer(graphicsLayer.id) ==  null) {
                this.map.addLayer(graphicsLayer);
            }
            this._initialExtent == null;
            if(extent != null) {
            	_map.extent = extent.expand(1.5);
            	this._initialExtent = extent;
            }
            
            if(index > 0 ) { 
               if(map.spatialReference && _spatialReference && 
                    ProjectionHandler.isEqualWkids(map.spatialReference.wkid,
                       _spatialReference.wkid)) {
                    // It will be taken care of further down the road by the
                    // map manager if this does not pass
                    promptColorChoose();
               }
            } else { 
                dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, 
                   this, new Fault("-1", "File has too many features", "")));
            }
            
        }
        
        /**
        * When there is a fault contacting the gp service then function
        * is called
        * 
        * @param info FaultEven tobject
        * @param token unused 
        * */
        private function handlerGpFault(info:FaultEvent, token:Object = null):void {
        	this.setLoaded(false);
            dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, 
              this, 
			  info.fault));       
        } 
         
        /**
         * Sets the visible layers
         * 
         * @arr Array of visible layers, just contains their ids
         * */
        override public function set visibleLayers(arr:ArrayCollection):void {
          
          this._visibleLayers = arr;
          if(arr && arr.length > 0) {
          	var layerInfos:Array = layerInfos;
          	var showPoints:Boolean = false;
          	var showLines:Boolean = false;
          	var showPolygons:Boolean = false;
          	for(var i:Number = 0; i < arr.length; i++) {
          		var index:Number = arr[i];
          	    var layerInfo:GeoProcessingViewerLayerInfo = layerInfos[index] ;
          	    if(layerInfo.title == lblPoints) {
          	    	showPoints = true;
          	    } else if (layerInfo.title == lblPolygons) {
          	    	showPolygons = true;
          	    } else if (layerInfo.title == lblLines) {
                    showLines = true;
                }
          	}
          	geometryVisibility(showLines, showPoints, showPolygons);
          } else {
          	this._map.removeLayer(this.graphicsLayer);
          }
          /*for each (var layerInf:WMSLayerInfo in readLayersAsArray()) {
            layerInf.visible = this._visibleLayers.contains(layerInf.id);
            layerInf.defaultVisibility = this.visible;
          }*/
          this.refresh();
        }
        
         /**
        * Switches geometry visibilities on or off
        * 
        * @param showLines Lines shown or not shown (true\false)
        * @param showPoints Points shown or not shown (true\false)
        * @param showPolgyons Polygons shown or not shown (true\false)
        * */
        private function geometryVisibility(showLines:Boolean = true, 
          showPoints:Boolean = true, showPolygons:Boolean = true):void {
            var arrColl:ArrayCollection = new ArrayCollection();
            if(graphicsLayer.graphicProvider is ArrayCollection) {
                arrColl = graphicsLayer.graphicProvider as ArrayCollection;
            }
            var graphic:Graphic = null;
            for(var i:Number = 0; i < arrColl.length; i++) {
                graphic = arrColl[i] as Graphic;
                if(graphic == null) {
                    continue;
                }
                var type:String = graphic.geometry.type;
                if(type.toLowerCase().indexOf("point") > 0) {
                    graphic.visible = showPoints;  
              
                } else if(type.toLowerCase().indexOf("line") > 0) {
                    graphic.visible = showLines;
                   
                } else { 
                    graphic.visible = showPolygons;
                       
                }
                
            }
            
        }
      
        override public function get visibleLayers():ArrayCollection{
          return this._visibleLayers;
          
        }
                
        /**
        *  Gets layer information.  We will just add one for now
        * */
        override public function get layerInfos():Array {
          var arr:Array = new Array();
          for(var i:Number = 0; i < _geomTypes.length; i++) {
              var type:String = _geomTypes.getItemAt(i) as String;
              var layerInfo:GeoProcessingViewerLayerInfo = 
                  new GeoProcessingViewerLayerInfo(); 
              layerInfo.visible = true;
              if(type.toLowerCase().indexOf("point") >= 0) {
                layerInfo.title = lblPoints;
              } else if(type.toLowerCase().indexOf("line") >= 0) {
                layerInfo.title = lblLines;
              } else {
              	layerInfo.title = lblPolygons;
              }
              layerInfo.name = layerInfo.title;
              layerInfo.id = i;
              layerInfo.defaultVisibility = true;
             
              arr.push(layerInfo)    
          }
  
          return arr;
        }
        
		/**
         * @private
         */
        override protected function commitProperties():void 
        {
            super.commitProperties();
            
            //if (_urlChanged)
            //{
            //    _urlChanged = false;
                
                removeAllChildren();
                var arr:ArrayCollection = new ArrayCollection();
                              
                this._visibleLayers = arr;
                
                
                invalidateLayer();
                
                //invalidateProperties();
                //invalidateLayer();
                if(this.loaded == false) {
                  getGeometries();
                }
                
                //loadServiceInfo();
            //}
        }
        
        /**
        * Prompts user to choose color
        * */
        public function promptColorChoose():void {
        	if(_geomTypes == null) {
        		return;
        	}
        	var tmpTitle:String = this.title;
        	if(tmpTitle && tmpTitle.length > 50) {
        		tmpTitle = tmpTitle.substring(0, 50);
        		tmpTitle + "...";
        	}
        	windowColorPick = new TitleWindow();
        	windowColorPick.title = tmpTitle;
        	windowColorPick.width = 300;
        	windowColorPick.height = 230;
        	//windowColorPick.showCloseButton = true;
        	windowColorPick.styleName="WidgetCanvas" 
        
        	        	
        	var cont:VBox = new VBox();
            windowColorPick.addChild(cont);
        	
        	var textArea:TextArea = new TextArea();
        	textArea.width = 270;
        	textArea.height = 100;
        	textArea.wordWrap = true;
        	var label:Text = new Text();
        	label.text = "Choose a color to be used for the incoming " +
        	  "map information";   
        	label.selectable = false;
        	//textArea.addChild(label);
        	textArea.editable = false;
        	textArea.text = label.text; 
        	     	
        	cont.addChild(textArea);
        	
        	var hBox:HBox = new HBox();
        	for(var i:Number = 0; i < _geomTypes.length; i++) {
        		
        		var type:String = _geomTypes.getItemAt(i) as String;
        		var vBox:VBox = new VBox();
	        	var colorPicker:ColorPicker = new ColorPicker();
	        	colorPicker.showTextField = true;
	        	if(type.toLowerCase().indexOf("point") > 0) {
		        	colorPicker.selectedColor = graphicMarkerSymbol.color; 
		       	   	colorPicker.addEventListener(ColorPickerEvent.CHANGE, colorPickedPoint, 
		        	   false, 0, true);
		        	type = lblPoints;   
		      
		        } else if(type.toLowerCase().indexOf("line") > 0) {
	        		colorPicker.selectedColor = graphicLineSym.color; 
                    colorPicker.addEventListener(ColorPickerEvent.CHANGE, colorPickedLine, 
                       false, 0, true);
                    type = lblLines;
                   
                } else { 
	        		colorPicker.selectedColor = graphicPolySym.color; 
                    colorPicker.addEventListener(ColorPickerEvent.CHANGE, colorPickedPolygon, 
                       false, 0, true);
                    type = lblPolygons;
                       
               	}
	        	vBox.addChild(colorPicker);
	        	var geomLabel:Label = new Label();
	        	geomLabel.text = type;
	        	vBox.addChild(geomLabel);
	        	hBox.addChild(vBox);
        	}
        	cont.addChild(hBox);
        	var button:Button = new Button();
	        button.label = "Done";
	        button.addEventListener(MouseEvent.CLICK, closeColorPick, false, 0, 
	        	   true);  
	        cont.addChild(button);
        	
        	
        	PopUpManager.addPopUp(windowColorPick, this._map, true);
            PopUpManager.centerPopUp(windowColorPick);
 	
        }
        
        
        /**
        * Closes the color picker
        * */
        private function closeColorPick(event:MouseEvent):void {
        	PopUpManager.removePopUp(windowColorPick);

        }
        
        /**
        * Choose color pick for the points
        * */
        private function colorPickedPoint(event:ColorPickerEvent):void {
        	graphicMarkerSymbol.color = event.color;
        }
        
        /**
        * Choose color picks for the line
        * */
        private function colorPickedLine(event:ColorPickerEvent):void {
        	graphicLineSym.color = event.color;
        }
        
        /**
        * Choose color picsk for the polygons
        * 
        * */
        private function colorPickedPolygon(event:ColorPickerEvent):void {
            graphicPolySym.color = event.color
        }
        
       
         		
	}
	
	
}