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
	import com.esri.ags.events.LayerEvent;
	import com.esri.ags.geometry.Geometry;
	import com.esri.ags.geometry.MapPoint;
	import com.esri.ags.geometry.Polygon;
	import com.esri.ags.geometry.Polyline;
	import com.esri.ags.layers.ArcGISDynamicMapServiceLayer;
	import com.esri.ags.layers.GraphicsLayer;
	import com.esri.ags.symbols.PictureMarkerSymbol;
	import com.esri.ags.symbols.SimpleFillSymbol;
	import com.esri.ags.symbols.SimpleLineSymbol;
	import com.esri.gpt.utils.Namespaces;
	import com.esri.viewer.components.InfoPopup;
	
	import flash.display.DisplayObject;
	import flash.display.Loader;
	import flash.events.MouseEvent;
	import flash.events.TimerEvent;
	import flash.utils.Timer;
	
	import mx.collections.*;
	import mx.rpc.Fault;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;

	public class GeoRssLayer extends ArcGISDynamicMapServiceLayer
	{
	   
        //labels
        private var loadingLabel:String;
        
        private var _url:String;
        
        private var querySource:String;
        
        private var queryExpr:String;
        
        private var queryFields:String;
        
        private var queryTitleField:String;
        
        private var queryLinkField:String;
        
        private var queryRefreshRate:String;
        
        private var zoomScale:Number = 5000;
        
        public var graphicsLayer:GraphicsLayer;
        
        private var graphicPointSym:PictureMarkerSymbol;
        
        private var graphicLineSym:SimpleLineSymbol = new SimpleLineSymbol("solid", 0x00FF00, 0.8, 2);
        
        private var graphicPolySym:SimpleFillSymbol = new SimpleFillSymbol("solid", 0xFF0000, 0.001, graphicLineSym);
        
        private var icon:String = "com/esri/solutions/flexviewer/assets/images/icons/i_globe.png";
        
        private var timer:Timer;
        
        public var title:String;
        
        private var _map:Map;
                       
        private var agsPopupInfo:InfoPopup = new InfoPopup();
                            
        [Bindable]
        private var msgVisible:Boolean = false;
        
        private var rsstype:String = "";
        
        private var hitimer:uint;
                    
        private static const ATOM:Namespace = Namespaces.ATOM_NS;
        
        private static const RE:RegExp = /M[ ](?P<mag>[^,]+),\.*/;
        
        public function GeoRssLayer(url:String = null) {
        	super(url);
        	this.addEventListener(Event.ADDED, addEvent, false,
                0, true);
        }
        
        public function init1(url:String, useProxy:Boolean, 
          queryTitleField:String, queryLinkField:String, map:Map, label:String):void {
          
          this.querySource = url;
            if(useProxy) {
              this.querySource = this.proxyURL
              if(this.querySource.indexOf("?") < 0) {
                this.querySource += "?";
              }
              this.querySource += url;  
            }
            this.queryTitleField = queryTitleField;
            this.queryLinkField = queryLinkField;
            this.map = map;
            //this.label = label;
            
            
        }
           
         public function init():void
        {
          //this.addChild(agsPopupInfo);
          //agsPopupInfo.visible = true;
          
            //this.widgetTitle = "GeoRSS";
            graphicPointSym = new PictureMarkerSymbol(icon, 30, 30)
            if(graphicsLayer == null) {
              graphicsLayer = new GraphicsLayer();
              graphicsLayer.symbol = graphicPointSym;
              map.addLayer(graphicsLayer);
            } else {
              graphicsLayer.clear();    
            }
            //if (configXML)
            //{
                //var tmp:String = this.configXML.georssW.label;
              //  super.widgetTitle = "GeoRSS";
                //labels
                //loadingLabel = configXML.labels.loadinglabel || "Loading feed...";
                
                //if (configXML.proxy == "true") {
                    //querySource = this.configData.proxy + configXML.source;
                //}
                //else {
                    //querySource = configXML.source;                       
                //}
                
                //queryExpr = configXML.query;
                //queryFields = configXML.fields;
                
                //queryLinkField = configXML.linkfield;
                //queryRefreshRate = configXML.refreshrate;
                //if(Number(configXML.zoomscale) > 0)
                    //zoomScale = Number(configXML.zoomscale);
                //queryRSSData();
                //startWidgetTimer();
           // }
            //loadingLabel = "Loading feed...";
            
           
            queryTitleField = "title";
            queryLinkField = "link";
            queryFields = "description";
            startWidgetTimer();
            
            
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
        
        public function set map(map:Map):void {
        	invalidateProperties();
            
            //_capabilities = null;
            setLoaded(false);
        	_map = map;
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
        override public function get map():Map {
        	return _map;
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
        private function get rssService():HTTPService {
        	var httpService:HTTPService = new HTTPService();
        	if(this.requestTimeout) {
        	  httpService.requestTimeout = this.requestTimeout;
        	}
        	httpService.addEventListener(ResultEvent.RESULT, loadRSSData, 
        	   false, 0, true);
        	httpService.addEventListener(FaultEvent.FAULT, faultHandler, 
               false, 0, true);
            httpService.resultFormat = "e4x";
         
        	return httpService;
        }
        
          /**
         * Ignored by this class loading an image does not emit a byte image
         * 
         * @private
         */
        override protected function loadMapImage( loader:Loader ):void 
        {
            if(this.loaded == false) {
            	this.init();
                this.queryRSSData();
            }
            
            // No image needed to be emitted.
        }
        
        /**
         * @private
         */
        override protected function commitProperties():void 
        {
            super.commitProperties();
            if(this.loaded == false) {
                this.init();
                this.queryRSSData();
            }
        }
        
        //start widget timer
        private function startWidgetTimer():void
        {
            if (queryRefreshRate)
            {
                var seconds:Number = Number(queryRefreshRate);
                var rate:Number = seconds * 1000;
                stopWidgetTimer();
                timer = new Timer(rate);
                timer.addEventListener(TimerEvent.TIMER,timerTick);
                timer.start();
            }
        }           
        
        //stop widget timer
        private function stopWidgetTimer():void
        {
            if (timer != null)
            {
                if (timer.running)
                    timer.stop();
            }
        }           
        
        //timer tick
        private function timerTick(evt:TimerEvent):void
        {
        	
            queryRSSData();
        }           
        
        //query rss data
        private function queryRSSData():void
        {
        	try {
	        	if(url) {
	        		var querySource:String = url;
	        		var lProxyUrl:String = this.proxyURL;
	        		if(lProxyUrl) {
	        	       if(lProxyUrl.indexOf("?") < 0) {
	                    lProxyUrl += "?";
	                   }
	                   querySource = lProxyUrl + querySource;
	        		}
	        		var httpService:HTTPService = rssService;
	        		httpService.url = querySource;
	                httpService.send();
	        		
	        	} else {
	        	   throw new Error("URL to rss not configured");
	        	}        	
            } catch(error:Error) {
				
            	dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR,
                          this, 
						  new Fault("-1","A problem occured while connecting to rss." +
							  error.message,"")));
            }
        }           
        
        //fault handler
        private function faultHandler(event:mx.rpc.events.FaultEvent):void
        {
        	
        	var layerEvent:LayerEvent = new LayerEvent(LayerEvent.LOAD_ERROR,
        	   this, new Fault("-1", event.message + "\n" + event.toString(), ""));
        	this.dispatchEvent(layerEvent);
            
        }           
        
        //load RSS data
        private function loadRSSData(event:ResultEvent):void
        {
            var ac:ArrayCollection;
            try
            {
                if (event.result is XML)
                {
                    var x:XML = XML(event.result);
                    if (x.name() == "rss" && Number(x.@version) <= 2)
                    {
                        rsstype = "RSS";
                        ac = parseRSS(x);
                        setLoaded(true);
                    }
                    else if (x.namespace().uri.toLowerCase() == "http://www.w3.org/2005/atom")
                    {
                        rsstype = "ATOM";
                        ac = parseAtom(x);
                        setLoaded(true);                
                    }
                    else
                    {
						dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR,
							this, new Fault("-1", "Unable to determine RSS feed type.", "")));
                    	                
                    }  
                    createRecordData(ac);
                }          
                else
                {
                	dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR,
                          this, new Fault("-1", "HTTP Response is not XML", ""))); 
                    
                }
                
            }
            catch (error:Error)
            {
            	dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR,
                          this, 
						  new Fault("-1","A problem occured while parsing the RSS feed.","")
                          + error.message)); 
               
            }
        }           
        
        //create record data
        private function createRecordData(ac:ArrayCollection):ArrayCollection
        {
            graphicsLayer.clear();
            
            var projectionHandler:ProjectionHandler = new ProjectionHandler(
                new SpatialReference(map.spatialReference.wkid));
            var recAC:ArrayCollection = new ArrayCollection();
            var flds:Array;
            if (queryFields)
                flds = queryFields.split(",");
                    
            for (var i:Number = 0; i < ac.length; i++)                    
            {   
                var recxml:XML = XML(ac.getItemAt(i));
//                var icon:String = widgetIcon;
                var title:String = "";
                if (queryTitleField)
                    title = recxml[queryTitleField]; 
                //if (!title)
//                    title = widgetTitle; 
                var content:String = "";
                if (flds)
                {
                    for (var j:Number = 0; j < flds.length; j++)
                    {
                        var fld:String = flds[j];
                        var val:String = recxml[fld];
                        if (val)
                            content += fld + ": " + val + "\n"; 
                    }
                }
                var link:String;
                    if (queryLinkField)
                        link = recxml[queryLinkField];
                var geometry:Geometry = GeoRSSUtil.toGeometry(recxml);
                if(!geometry.spatialReference) {
                	geometry.spatialReference = new SpatialReference(4326);
                }
                geometry = projectionHandler.reprojectGeom(geometry);
                var point:MapPoint = getGeomCenter(geometry);
                                
                var infoData:Object = 
                {
                    icon: icon, 
                    title: title, 
                    content: content, 
                    link: link, 
                    point: point,
                    geometry: geometry
                };                 
                recAC.addItem(infoData);
                
                //***
                var gra:Graphic = new Graphic(geometry);
                gra.addEventListener(MouseEvent.ROLL_OVER, mouseOverGraphic)
                gra.attributes = infoData;
                switch (gra.geometry.type)
                {
                    case Geometry.POLYLINE:
                    {
                        gra.symbol = graphicLineSym;
                        break;
                    }
                    
                    case Geometry.POLYGON:
                    {
                        gra.symbol = graphicPolySym;
                        break;
                    }
                }
                graphicsLayer.add(gra);
                if(i == 0) {
                	graphicsLayer.spatialReference = geometry.spatialReference;
                	if(!map.contains(graphicsLayer)) {
                        map.addLayer(graphicsLayer);
                    }
                }
                //***
                
            } 
            return recAC;
        }           
    
         /**
        *  Gets layer information.  We will just add one for now
        * 
        override public function get layerInfos():Array {
          var layerInfo:GeoProcessingViewerLayerInfo = 
            new GeoProcessingViewerLayerInfo(); 
          layerInfo.visible = true;
          layerInfo.title = this.title + "[rss]";
          layerInfo.name = layerInfo.title;
          layerInfo.defaultVisibility = true;
          var arr:Array = new Array(1);
          arr.push(layerInfo)
          
          return arr;
        }*/
        
        //parse atom
         private function parseAtom(x:XML):ArrayCollection
         {
            var ac:ArrayCollection = new ArrayCollection();
            for each (var entryXML:XML in x.ATOM::entry)
            {
                ac.addItem(entryXML );                                                                        
            }        
            return ac;
        }       
    
        //parse rss
        private function parseRSS(x:XML):ArrayCollection
        {
            var ac:ArrayCollection = new ArrayCollection();
            for each (var itemXML:XML in x.channel.item)
            {
                ac.addItem(itemXML);            
            }
            return ac;
        }                   
        
        //get geom center
        private function getGeomCenter(geom:Geometry):MapPoint
        {
            var pt:MapPoint;
            switch (geom.type)
            {
               case Geometry.MAPPOINT:
               {
                    pt = geom as MapPoint;
                    break;
               }
               
               case Geometry.POLYLINE:
               {
                    var pl:Polyline = geom as Polyline;
                    var pathCount:Number = pl.paths.length;
                    var pathIndex:int = int(pathCount / 2) - 1;
                    var midPath:Array = pl.paths[pathIndex];
                    var ptCount:Number = midPath.length;
                    var ptIndex:int = int(ptCount / 2) - 1;
                    pt = pl.getPoint(pathIndex, ptIndex);
                        
                    //pt = pl.extent.center;
                    break;
               }
               
               case Geometry.POLYGON:
               {
                    var poly:Polygon = geom as Polygon;
                    pt = poly.extent.center;
                    break;
               }
            }
            return pt;
        }           
                    
        //mouse over graphic
        private function mouseOverGraphic(event:MouseEvent):void
        {
            var gra:Graphic = event.currentTarget as Graphic;
            var infoData:Object = gra.attributes;
            
            showInfoWindow(infoData);
        }
        
        
        public function showInfoWindow(infoData:Object):void {
          
          if(infoData != null) {
            map.addChild(agsPopupInfo);
            agsPopupInfo.visible = true;
            
          } else { 
            agsPopupInfo.visible = false;
            map.removeChild(agsPopupInfo);
          }
          agsPopupInfo.map = map;
          agsPopupInfo.infoData = infoData;
         
        }
                    
       
                    
        private function showHighlight(params:Array):void
        {
            var infoData:Object = params[0];
            var pt:MapPoint = infoData.point;
            //if (!map.extent.contains(pt))
            //  map.centerAt(pt);
            //this.showInfoWindow(params[0]);
        }           
        
                   
      
            

	}
}