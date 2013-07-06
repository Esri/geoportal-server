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
package com.esri.gpt.wms
{
	import com.esri.ags.Map;
	import com.esri.ags.SpatialReference;
	import com.esri.ags.events.ExtentEvent;
	import com.esri.ags.events.LayerEvent;
	import com.esri.ags.geometry.Extent;
	import com.esri.ags.geometry.MapPoint;
	import com.esri.ags.layers.ArcGISDynamicMapServiceLayer;
	import com.esri.ags.layers.Layer;
	import com.esri.ags.layers.WMSLayer;
	import com.esri.gpt.utils.ProjectionHandler;
	import com.esri.gpt.utils.UrlUtils;
	import com.esri.gpt.utils.Utils;
	
	import flash.display.BitmapData;
	import flash.display.Loader;
	import flash.events.ErrorEvent;
	import flash.events.Event;
	import flash.events.IOErrorEvent;
	import flash.events.ProgressEvent;
	import flash.events.UncaughtErrorEvent;
	import flash.filters.ColorMatrixFilter;
	import flash.geom.Rectangle;
	import flash.net.URLRequest;
	import flash.utils.ByteArray;
	import flash.utils.getQualifiedClassName;
	
	import mx.collections.ArrayCollection;
	import mx.collections.IList;
	import mx.controls.Alert;
	import mx.core.BitmapAsset;
	import mx.core.ByteArrayAsset;
	import mx.events.FlexEvent;
	import mx.graphics.codec.PNGEncoder;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.Fault;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.mxml.HTTPService;
	import mx.utils.ObjectUtil;
	
	//--------------------------------------
	//  Other metadata
	//--------------------------------------
	
	[Bindable]
	
	/**
	 * A map layer that displays images from a WMS service.
	 * Supports WMS versions 1.1.1 and 1.3.0.
	 */
	public class WMSLayer extends ArcGISDynamicMapServiceLayer
	{
	  
	  /***************************************************************************
	  * Class variables
	  * **************************************************************************/
	    
	  /** Class logger **/
	  private static const LOG:ILogger = Log.getLogger("WMS");
    
    [Embed(source="/assets/images/cross_cursor.png")]
    private var pngImage:Class;
	  
    public static var WMSEVENT_GETMAPERROR:String = "WMSEVENT_GETMAPERROR";
		//--------------------------------------------------------------------------
		//
		//  Constructor
		//
		//--------------------------------------------------------------------------
		
		/**
		 * Creates a new WMS Layer object.
		 */
		public function WMSLayer( url:String = null )
		{
			super();
			
			this.url = url;
			this.addEventListener(Event.ADDED, addEvent, false, 0, true);
		}
		
		
		//--------------------------------------------------------------------------
		//
		//  Variables
		//
		//--------------------------------------------------------------------------
		
		private var _url:String;
		private var _urlChanged:Boolean = false;
		
		private var _proxyURL:String;
		
		private var _wmsCapabilities:WMSCapabilities;
		private var _defaultVisibleLayers:String;
		
		// Configurable query parameters
		private var _numLayersShownInitially:uint = new uint(5);
		private var _layers:String;
		private var _styles:String;
    private var _sldUrl:String;
		private var _srs:String;
		private var _imageFormat:String = "image/png";
		private var _imageTransparency:Boolean = true;
		private var _backgroundColor:String = "0xFFFFFF";
		private var _exceptions:String;
		private var _bFlattenned:Boolean = false;		
		private var _visibleLayers:IList = null;
		private var _layerSpatialRefs:ArrayCollection;
		private var _map:Map;
    
		
		
		
		
		// lazy init through logger property
		private var _logger:ILogger;
		
		//--------------------------------------------------------------------------
		//  Property:  url
		//--------------------------------------------------------------------------
		
		[Inspectable(category="Mapping")]
		/**
		 * URL endpoint of a service.
		 */
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
			
			_urlChanged = true;
			invalidateProperties();
			
			_wmsCapabilities = null;
			setLoaded(false);
		}
		
		
		
		/**
		 * accucmulates the spatial references
		 * 
		 * */
		public function get layerSpatialRefs():ArrayCollection {
			
			if(_layerSpatialRefs == null) {
				_layerSpatialRefs = new ArrayCollection();
			}
			var arrLayers:ArrayCollection = this.readLayersAsArray();
			if(arrLayers == null || (_layerSpatialRefs != null 
			  && _layerSpatialRefs.length < 1)) {
			  return _layerSpatialRefs;	
			}
			_layerSpatialRefs = new ArrayCollection();
			for each (var layerInfo:WMSLayerInfo in arrLayers) {
				if(layerInfo == null) {
					continue;
				}
				_layerSpatialRefs.addItem(layerInfo.spatialRef);
			}
			return _layerSpatialRefs;
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
            this.map.addEventListener(ExtentEvent.EXTENT_CHANGE, function(ev:ExtentEvent):void {
				//
				invalidateLayer();
			});
                   
            
        }
		
		//--------------------------------------------------------------------------
		//  Property:  wmsCapabilities
		//--------------------------------------------------------------------------
		
		[Bindable("load")]
		/**
		 * The wmsCapabilities of the WMS service.
		 */
		public function get wmsCapabilities():WMSCapabilities
		{
			return _wmsCapabilities;
		}
		
		//--------------------------------------------------------------------------
		//  Property:  layerInfo
		//--------------------------------------------------------------------------
		
		[Bindable("load")]
		/**
		 * The available layers in the map service.
		 *
		 * @see com.esri.wms.WMSLayerInfo
		 */
		public function get layerInfo():WMSLayerInfo
		{
			return wmsCapabilities ? wmsCapabilities.layerInfo : null;
		}
		
		//--------------------------------------------------------------------------
		//  Property:  proxyURL
		//--------------------------------------------------------------------------
		
		[Inspectable(category="Mapping")]
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
		
		//--------------------------------------------------------------------------
		//  Property:  layers
		//--------------------------------------------------------------------------
		
		[Inspectable(category="Mapping")]
		
		public function get sldUrl():String {
      return this._sldUrl;
    }
    
    public function set sldUrl(sSldUrl:String):void {
      this._sldUrl = sSldUrl;
    }
		
		//--------------------------------------------------------------------------
		//  Property:  styles
		//--------------------------------------------------------------------------
		
		[Inspectable(category="Mapping")]
		/**
		 * The style(s) of the requested map image.
		 */
		public function get styles():String 
		{
			return _styles;
		}
		/**
		 * 
		 * 
		 * @private
		 */
		public function set styles( value:String ):void 
		{
			_styles = value;
			invalidateLayer();
		}
		
		//----------------------------------------------------------------------------
		// property: number of layers shown initially
		//----------------------------------------------------------------------------
		/**
		 * Set number of layers first shown when service is called
		 * 
		 * */
		public function set numLayersShownInitially(num:uint):void {
		  this._numLayersShownInitially = num;
		}
		
		/**
		 * Get the number of layers first shown when service is called
		 * 
		 * @return always 0  or greater
		 * */
		public function get numLayersShownInitially():uint {
		  return this._numLayersShownInitially; 
		}
		//--------------------------------------------------------------------------
		//  Property:  srs
		//--------------------------------------------------------------------------
		
		[Inspectable(category="Mapping")]
		/**
		 * The spatial reference of the requested map image. Setting this property
		 * will override the default behavior of using the same spatial reference as
		 * the Map that contains this layer.
		 */
		public function get srs():String 
		{
			if(!_srs) {
				return this.getSendSpatialReference();
			}
			return _srs;
		}
		/**
		 * @private
		 */
		public function set srs( value:String ):void 
		{
			_srs = value;
			invalidateLayer();
		}
		
		//--------------------------------------------------------------------------
		//  Property:  imageFormat
		//--------------------------------------------------------------------------
		
		[Inspectable(category="Mapping", defaultValue="image/png")]
		/**
		 * The output image type. Valid formats might be: image/png | image/jpeg | image/gif.
		 * The actual supported types are specific to each map service and are available
		 * in the WMS Capabilities.
		 * 
		 * @default image/png
		 */
		override public function get imageFormat():String 
		{
			return _imageFormat;
		}
		/**
		 * @private
		 */
		override public function set imageFormat( value:String ):void 
		{
			_imageFormat = value;
			invalidateLayer();
		}
		
		//--------------------------------------------------------------------------
		//  Property:  imageTransparency
		//--------------------------------------------------------------------------
		
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
		/**
		 * @private
		 */
		override public function set imageTransparency( value:Boolean ):void
		{
			_imageTransparency = value;
			invalidateLayer();
		}
		
		//--------------------------------------------------------------------------
		//  Property:  backgroundColor
		//--------------------------------------------------------------------------
		
		[Inspectable(category="Mapping", defaultValue="0xFFFFFF")]
		/**
		 * A hexadecimal RGB color value (0xRRGGBB) to use for the background color.
		 * 
		 * @default 0xFFFFFF
		 */
		public function get backgroundColor():String
		{
			return _backgroundColor;
		}
		/**
		 * @private
		 */
		public function set backgroundColor( value:String ):void
		{
			_backgroundColor = value;
			invalidateLayer();
		}
		
		//--------------------------------------------------------------------------
		//  Property:  exceptions
		//--------------------------------------------------------------------------
		
		[Inspectable(category="Mapping")]
		/**
		 * The format in which exceptions are to be reported by the map server.
		 */
		public function get exceptions():String
		{
			return _exceptions;
		}
		/**
		 * @private
		 */
		public function set exceptions( value:String ):void
		{
			_exceptions = value;
			invalidateLayer();
		}
		
		//--------------------------------------------------------------------------
		//
		//  Methods
		//
		//--------------------------------------------------------------------------
		
		/**
		 * @private
		 */
		override protected function commitProperties():void 
		{
			super.commitProperties();
			this.invalidateLayer();
			if (_urlChanged)
			{
				_urlChanged = false;
				
				removeAllChildren();
				loadServiceInfo();
			}
		}
		
		
		private function loadServiceInfo():void 
		{
			var capsURL:String = url;
			if (!capsURL)
			{
				LOG.info("Capabilities url is null");
				return;
			}
			LOG.debug("Recieved Capabilities URL" + capsURL);
			
			// Ask for the highest WMS version. The server should send back a 
			// downgraded wmsCapabilities version if it doesn't support this one.
			//capsURL += getURLPrefix(capsURL);		
			if(capsURL.match(new RegExp("\\?")) == null ||
			  capsURL.match(new RegExp("\\?")).length < 1) {
			  capsURL = capsURL + "?";
			}
			//capsURL = capsURL.replace(/&?service=wms/i, "");
			//capsURL = capsURL.replace(/&?request=GetCapabilities/i, "");
			//capsURL = capsURL.replace(/&?version=(\d|\.)*/i, "");
			
			var wmsurl:UrlUtils = new UrlUtils(capsURL);
			/*if (wmsurl.valid){
				capsURL = wmsurl.rootURL() + "?" ;
			}*/
			var value:String = wmsurl.getParameterValue("service");
			capsURL += (value != null)? "": "&service=WMS";
			value = wmsurl.getParameterValue("version");
			capsURL += (value != null)? "": "&version=1.3.0";
			value = wmsurl.getParameterValue("request");
			capsURL += (value != null)? "": "&request=GetCapabilities";
			
			capsURL = capsURL.replace(/&request=GetMap/i, "");
			capsURL = capsURL.replace(/&request=GetFeatureInfo/i, "");
      //Utils.chkStringMatch(
      /*value = wmsurl.getParameterValue("sld");
      this.sldUrl = value;
      capsURL = capsURL.replace(/&sld=.*([^&])/i, "");*/
      
			//if(capsURL.indexOf("?") < capsURL.length - 1) {
			//  capsURL += "&";
			//}
			//capsURL += "service=WMS&version=1.3.0&request=GetCapabilities";
			LOG.debug("constructed capabilites url" + capsURL);
			if (proxyURL)
			{
				if(proxyURL.match(new RegExp("\\?")) == null ||
                  proxyURL.match(new RegExp("\\?")).length < 1) {
                  capsURL = proxyURL + "?" + escape(capsURL);
                } else {
				  capsURL = proxyURL + escape(capsURL);
                }
				//capsURL = proxyURL + capsURL;
			}
			
			if (Log.isDebug())
			{
				logger.debug("{0}::WMS GetCapabilities URL: {1}", id, capsURL)
			}
			
			var httpService:HTTPService = new HTTPService();
			httpService.resultFormat = "e4x";
			httpService.requestTimeout = this.requestTimeout;
			httpService.url = capsURL;
						
			_visibleLayers = null;
			var layer:Layer = this;
			
			LOG.debug("Get Capabilities URL" + capsURL);
			var token:AsyncToken = httpService.send();
			token.serviceUrl = capsURL;
			
			token.addResponder(new AsyncResponder(
				function onResult( event:ResultEvent, token:Object = null ):void
				{
					// Make sure the result is for the current URL
					if (token == url)
					{
					  try{
						_wmsCapabilities = WMSCapabilities.fromXML(event.result as XML);
						if (_wmsCapabilities != null && _wmsCapabilities.layerInfo != null)
						{
							setDefaultVisibleLayers();
							
							invalidateLayer();
							var flattenedLayers:ArrayCollection = new ArrayCollection();
							if(wmsCapabilities.layerInfo == null) {
								dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, layer, 
                              new Fault("WMS", "No Layer information found" + url, 
								  "No Layer information found" + url)));
                              return;
							}
					        flattenedLayers.addItem(wmsCapabilities.layerInfo);
					        var index:Number = 0;
				        	for each (var layerInfo:WMSLayerInfo in flattenedLayers) {
	        	              if(layerInfo != null) {
	        	        //layerInfo.defaultVisibility = index < 
	        	        //this.numLayersShownInitially - 1;
	        	                layerInfo.layerId = index;
	        	 
	        	                   index++;
	        	               }
	        	               if(layerInfo == null || layerInfo.subLayers == null || 
	        	                layerInfo.subLayers.length < 1) {
	        	                  continue;
	        	               }
	        	               for each ( var childLayerInfo:WMSLayerInfo in layerInfo.subLayers) {
	        	                  flattenedLayers.addItem(childLayerInfo);
	        	               }
	        	    
	        	            }
	        	            //_visibleLayers = flattenedLayers;
	        	            setLoaded(true);
						} else {
						  var error:String = WMSError.readError(event.result as XML);
						  if(error == null || error == "") {
						    error = "No Layers Found in WMS url"
						  }
							dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, layer, 
							  new Fault("WMS", error + url, error + url)));
							if (Log.isError()) 
							{
								logger.error("{0}::Invalid WMS capabilities: {1}", id, ObjectUtil.toString(event));
							}
						}
					  } catch(error:Error) {
					  	dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, layer, 
							new Fault("WMS", event.message
                          + ": Error while connecting to url" + capsURL, 
						  event.message
						  + ": Error while connecting to url" + capsURL)));
					  }	 
					} 
				},
				function onFault( event:FaultEvent, token:Object = null ):void
				{
					dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, layer, 
						new Fault("WMS", event.message
					  + ": Error while connecting to url" + capsURL,
					  event.message
					  + ": Error while connecting to url" + capsURL)));
					if (Log.isError())
					{
						logger.error("{0}::{1}", id, ObjectUtil.toString(event));
					}
				},
				url
			));
		}
		
		/**
         * Loads map image
         * @private
         */
		override protected function loadMapImage( loader:Loader ):void  {
			try {
			
			   loadMapImageWork(loader);
			
			} catch(error:Error) {
				dispatchEvent(new LayerEvent(WMSEVENT_GETMAPERROR, 
					this, new Fault("WMS", "Getmap Error", 
						error.toString() )))		
			}
			
		}
		/**
		 * Loads map image
		 * @private
		 */
		protected function loadMapImageWork( loader:Loader ):void 
		{
			var mapURL:String =  wmsCapabilities.urlGetMap;
			if(mapURL == null || mapURL == "") {
			  mapURL = url;
			}
			LOG.debug("Get Map Url " + mapURL);
			if(mapURL.match(new RegExp("\\?")) == null ||
                mapURL.match(new RegExp("\\?")).length < 1) {
                mapURL = mapURL + "?";
            }
			var extent:Extent = map.extent;
			
			if (!wmsCapabilities || !mapURL || !extent)
			{
				return;
			}
			
			var imageRect:Rectangle = buildImageRect();
			if (imageRect.x != 0 || imageRect.y != 0)
			{
				extent = buildImageExtent(imageRect);
			}
			
			mapURL = mapURL.replace(/&?service=wms/i, "");
			mapURL = mapURL.replace(/&?request=GetCapabilities/i, "");
			mapURL = mapURL.replace(/&?version=(\d|\.)*/i, "");
			
			/*
			// TM: Some getmaps need the parameters that come after the question
			// mark yet this part takes it away.  Removing this so that strictly
			// the getmap is used.
			var wmsurl:WMSUrl = new WMSUrl(mapURL);
			if (wmsurl.valid){
				mapURL = wmsurl.rootURL() + "?" ;
			}*/
			
			//mapURL += getURLPrefix(mapURL);
			if(mapURL.indexOf("?") < mapURL.length -1 ) {
			  mapURL += "&";
			}
			mapURL += "service=WMS";
			mapURL += "&version=" + wmsCapabilities.version;
			mapURL += "&request=GetMap";
			mapURL += "&width=" + imageRect.width;
			mapURL += "&height=" + imageRect.height;
			if (imageFormat) mapURL += "&format=" + imageFormat;
			if (imageTransparency) {
			  // TM: Some services are very strict and want the following
			  // to be uppercase	 
			  mapURL += "&transparent=" + 
			    imageTransparency.toString().toUpperCase();
			}
			if (backgroundColor) mapURL += "&bgcolor=" + backgroundColor;
			if (exceptions) mapURL += "&exceptions=" + exceptions;
			var layers:String = this.readLayersAsString();
			if(layers == null || layers == "") {
				this.setVisible(false);
				return;
			} else {
				this.setVisible(true);
			}
			mapURL += "&layers=" + readLayersAsString();
			mapURL += "&styles=" + (styles ? styles : "");
			
			
			var requestSR:String;
		
            if (srs)
            {
                requestSR = srs;
            }
            else if (map.spatialReference)
            {
                // Default to the SRS of the map
                var mapSR:Number = map.spatialReference.wkid;
                if (!isNaN(mapSR) && mapSR != -1)
                {
                    requestSR = "EPSG:" + mapSR;
                }
            }
			
			if (wmsCapabilities.version == "1.3.0")
			{
				if (requestSR)
				{
					mapURL += "&crs=" + requestSR;
				}
        // WMS relies on axis order defined by the projection.  will assume
        // epsg between 4000 & 5000 has lat, lon (not always true)
        var numRequestSr:Number =parseInt(requestSR);
        if(requestSR != null && numRequestSr.toString() == "NaN" ) {
          var numRequestSrArr:Array = Utils.chkStringMatch(
            requestSR.match( new RegExp(/\d+/))
          );
          if(numRequestSrArr.length > 0) {
            numRequestSr = numRequestSrArr[0];
          }
        }
        
				// This WMS version has axis order dependent on CRS authority (e.g. EPSG)
				if (requestSR && requestSR.toUpperCase().indexOf("EPSG:") != -1 &&
          (!isNaN(numRequestSr)  && numRequestSr > 4000 && numRequestSr < 5000))
				{
          // See http://viswaug.wordpress.com/2009/03/15/reversed-co-ordinate-axis-order-for-epsg4326-vs-crs84-when-requesting-wms-130-images/
					// Axis order: (y,x) (lat,lon)
					mapURL += "&bbox=" + extent.ymin+","+extent.xmin+","+extent.ymax+","+extent.xmax;
				}
				else
				{
					// Axis order: (x,y) (lon,lat)
					mapURL += "&bbox=" + extent.xmin+","+extent.ymin+","+extent.xmax+","+extent.ymax;
				}
			}
			else  // 1.1.1 and below
			{
				if (requestSR)
				{
					mapURL += "&srs=" + requestSR;
				}
				// Axis order: (x,y) (lon,lat)
				mapURL += "&bbox=" + extent.xmin+","+extent.ymin+","+extent.xmax+","+extent.ymax;
			}
      
      if(Utils.chkString(this.sldUrl) != "") {
        mapURL += "&sld=" + this.sldUrl;
      }
			
			if (proxyURL)
			{
				if(proxyURL.match(new RegExp("\\?")) == null ||
                  proxyURL.match(new RegExp("\\?")).length < 1) {
                    mapURL = proxyURL + "?" + escape(mapURL);
                } else {
				    mapURL = proxyURL + escape(mapURL);
                }
			}
			
			if (Log.isDebug())
			{
				logger.debug("{0}::WMS GetMap URL: {1}", id, mapURL)
			}
			
			
			var urlReq:URLRequest = new URLRequest(mapURL);
			try {
			  loader.close();
			} catch(err:Error) {
				
			}
			loader.x = imageRect.x;
			loader.y = imageRect.y;
			
			loader.uncaughtErrorEvents.removeEventListener(
				UncaughtErrorEvent.UNCAUGHT_ERROR, 
				imageLoadErrorHandler);
			loader.uncaughtErrorEvents.addEventListener(
				UncaughtErrorEvent.UNCAUGHT_ERROR, 
				imageLoadErrorHandler, 
				false, 0, true);
			loader.removeEventListener(IOErrorEvent.IO_ERROR, 
				imageLoadErrorHandler2);
			loader.addEventListener(IOErrorEvent.IO_ERROR, 
				imageLoadErrorHandler2, false, 0, true);
			loader.load(urlReq);
						
			var httpService:HTTPService = new HTTPService();
			httpService.resultFormat = "e4x";
			httpService.requestTimeout = this.requestTimeout;
			httpService.url = mapURL; 
			var token:AsyncToken = httpService.send();
			token.url = mapURL;
			token.layer = this;
			token.addResponder(new AsyncResponder(
				function onResult( event:ResultEvent, token:Object = null ):void {
				  var error:String = WMSError.readError(event.result as XML);
				  if(error != null && error != "") {
				    dispatchEvent(new LayerEvent(WMSEVENT_GETMAPERROR, 
				      event.token.layer, new Fault("WMS",error + mapURL,error)));
            
                
				  }
				},
				function onFault( event:FaultEvent, token:Object = null ):void {
					
					/*var urlReq:URLRequest = new URLRequest(mapURL);
					loader.close();
					loader.x = imageRect.x;
					loader.y = imageRect.y;
					loader.uncaughtErrorEvents.removeEventListener(
						UncaughtErrorEvent.UNCAUGHT_ERROR, 
						imageLoadErrorHandler);
					loader.uncaughtErrorEvents.addEventListener(
						UncaughtErrorEvent.UNCAUGHT_ERROR, 
						imageLoadErrorHandler, 
						false, 0, true);
					loader.removeEventListener(IOErrorEvent.IO_ERROR, 
						imageLoadErrorHandler2);
					loader.addEventListener(IOErrorEvent.IO_ERROR, 
						imageLoadErrorHandler2, false, 0, true);
						
					try {
						loader.load(urlReq);
					} catch(error:Error) {
						dispatchEvent(new LayerEvent(WMSEVENT_GETMAPERROR, 
							this, new Fault("WMS", "Getmap Error", 
								error.toString() )))		
					}*/
					
					
					/*dispatchEvent(new LayerEvent(LayerEvent.LOAD_ERROR, 
					  event.token.layer, new Fault("WMS", event.message
					  + ": Error while connecting to url" + event.token.url,
					  event.message
					  + ": Error while connecting to url" + event.token.url)));*/
					if (Log.isError()) {
						logger.error("{0}::{1}", id, ObjectUtil.toString(event));
					}
				},
				mapURL
			));
			
			
			
		}
		
		private function imageLoadErrorHandler(event:UncaughtErrorEvent):void {
			
			var strError:String = "Error during getmap: ";
			if (event.error is Error)
			{
				var error:Error = event.error as Error;
				strError += error.toString();
				
			}
			else if (event.error is ErrorEvent)
			{
				var errorEvent:ErrorEvent = event.error as ErrorEvent;
				strError += errorEvent.toString();
				
			}
			dispatchEvent(new LayerEvent(WMSEVENT_GETMAPERROR, 
				this, new Fault("WMS", strError, strError )))

		}
		
		private function imageLoadErrorHandler2(event:Event):void {
			dispatchEvent(new LayerEvent(WMSEVENT_GETMAPERROR, 
				this, new Fault("WMS", "Getmap Error", event.toString() )))
		}
		
		public function set map(map:Map):void {
            this._map = map;
            
        }
         
        override public function get map():Map {
            return this._map;
        }     
		
		/**
		 * Clears the visible layers as defined in layers and resets to the default layers of the map service.
		 
		override public function defaultVisibleLayers():void
		{
      //this.visibleLayers = _defaultVisibleLayers;
      ;
		}*/
		
		private function setDefaultVisibleLayers():void {
      
		  var index:Number = 0;
		  var arr:ArrayCollection = readLayersAsArray();
		  for each (var layerInf:WMSLayerInfo in arr) {
		    
		    if(layerInf != null && (layerInf.subLayers == null ||
		      layerInf.subLayers.length <= 0)) {
		      var show:Boolean = index < this.numLayersShownInitially;
		      layerInf.visible = show;
		      layerInf.defaultVisibility = show;
		      var parentLayerInfo:WMSLayerInfo = layerInf.parentLayer;
		      while(show == true && parentLayerInfo != null && 
		        parentLayerInfo.visible == false) {
		      	parentLayerInfo.visible = show;
		      	parentLayerInfo.defaultVisibility = show;
		      	parentLayerInfo = layerInf.parentLayer;
		      }
		     index++; 	
		    }
		   		    
		  }
		  //this.layerInfo.visible = this.visible;
		  //this.layerInfo.defaultVisibility = this.visible;
	  }
		
		/**
		 * Calculates the pixel size of the image, based on the current map size and
		 * the maximum image size specified in the WMS capabilities.
		 */
		public function buildImageRect():Rectangle
		{
			var w:Number = Math.max(1, map.width);
			var h:Number = Math.max(1, map.height);
			var rect:Rectangle = new Rectangle(0, 0, w, h);
			
			if (!isNaN(wmsCapabilities.maxWidth))
			{
				while (rect.width > wmsCapabilities.maxWidth)
				{
					rect.inflate(-1, 0);
				}
			}
			if (!isNaN(wmsCapabilities.maxHeight))
			{
				while (rect.height > wmsCapabilities.maxHeight)
				{
					rect.inflate(0, -1);
				}
			}
			
			return rect;
		}
		
		/**
		 * Returns the map extent that covers the specified pixel region of the map.
		 */
		public function buildImageExtent( imageRect:Rectangle ):Extent
		{
			var tl:MapPoint = map.toMap(imageRect.topLeft);
			var br:MapPoint = map.toMap(imageRect.bottomRight);
			return new Extent(tl.x, br.y, br.x, tl.y, map.extent.spatialReference);
		}
		
		/**
		 * 
		 * 
		 * @private
		 */
		protected function get logger():ILogger 
		{
			if (!_logger)
			{
				_logger = Log.getLogger(getQualifiedClassName(this).replace(/::/, "."));
			}
			return _logger;
		}
		
		/**
		 * The layer infos
		 * 
		 * @param array with layer information
		**/
		override public function get layerInfos():Array {
		  return this.readLayersAsArray().toArray();
		}
		
		
		/**
		 * Sets the visible layers
		 * 
		 * @arr Array of visible layers, just contains their ids
		 * */
		override public function set visibleLayers(arr:IList):void {
		 
		  this._visibleLayers = arr;
		  for each (var layerInf:WMSLayerInfo in readLayersAsArray()) {
		    layerInf.visible = this._visibleLayers.toArray().indexOf(layerInf.layerId) >= 0;
        var parentLayerInfo:WMSLayerInfo = layerInf.parentLayer;
        while(layerInf.visible == true && parentLayerInfo != null) {
          parentLayerInfo.visible = layerInf.visible;
          if(this._visibleLayers.toArray().indexOf(parentLayerInfo.layerId) < 0) {
            this._visibleLayers.addItem(parentLayerInfo.layerId);
          }
          parentLayerInfo = parentLayerInfo.parentLayer;
          
        }
		  }
		  this.refresh();
      
		}
    
    
    /***
     * Gets the visible layers
     * 
     * @return array of visible layers 
     * */		
    override public function get visibleLayers():IList {
      return this._visibleLayers;
    }
		
		/**
		 * Goes through all the extents and comes up with a union of the
		 * extents which is then returned
		 * 
		 * @returns The extent union of all layers. Could be null
		 * */
		override public function get initialExtent():Extent {
		   var extent:Extent = null;	
		   var thisMapWkid:Number = this.spatialReference.wkid;
		   if(isNaN(thisMapWkid) == true) {
		   	  LOG.warn(
		   	    "wkid of this map is NaN while trying to get intialExtent");
		   	  return extent;
		   }
		   var projectionHandler:ProjectionHandler = 
		      new ProjectionHandler(new SpatialReference(thisMapWkid));
		   
	       for each (var layerInf:WMSLayerInfo in readLayersAsArray()) {
	       	  var ex4326:Extent = null;
	       	  var exSameAsMap:Extent = null;
	       	  for each ( var wmsExtent:WMSExtent in layerInf.bBox   ) {
	       	  	 if(wmsExtent.spatialReference == null) {
	       	  	 	continue;
	       	  	 } 
	       	  	 if(isNaN(wmsExtent.spatialReference.wkid) || 
	       	  	   isNaN(thisMapWkid)) {
	       	  	   	continue;
	       	  	 }
	       	  	 if(wmsExtent.spatialReference.wkid == 4326) {
	       	  	 	ex4326 = wmsExtent;
	       	  	 }
	       	  	 if(ProjectionHandler.isEqualWkids(
                         wmsExtent.spatialReference.wkid, thisMapWkid) == false) {
                    continue;     	
                 }
                 exSameAsMap =  new Extent(wmsExtent.xmin, wmsExtent.ymin, 
	       	  	    wmsExtent.xmax, wmsExtent.ymax, 
	       	  	    new SpatialReference(thisMapWkid));

	       	  	 break;	
	       	  	 
	       	  }
	       	  if(exSameAsMap == null && ex4326 == null) {
	       	  	continue;
	       	  } else if (exSameAsMap == null && ex4326 != null) {
	       	  	exSameAsMap = projectionHandler.reprojectExtent(ex4326);
	       	  }
	       	  if(exSameAsMap == null) {
	       	  	continue;
	       	  }
	       	  if(extent == null) {
                    extent = exSameAsMap;
              } else {
                    extent.union(exSameAsMap);
              }
	       	   
	       }
	     	       
	       return extent;
		}
	  
		
		/****
		 * Reads Layers in Tree into an array
		 * 
		 * @return Read Layers As Array  (never null)
		 * */
		protected function readLayersAsArray():ArrayCollection {
		  var flattenedLayers:ArrayCollection = new ArrayCollection();
		 
		  flattenedLayers.addItem(wmsCapabilities.layerInfo);
		  
		  for each (var layerInfo:WMSLayerInfo in flattenedLayers) {
		    if(layerInfo == null) {
		      continue;
		    }
		   	    	    
		    for each ( var childLayerInfo:WMSLayerInfo in layerInfo.subLayers) {
		      flattenedLayers.addItem(childLayerInfo);
		    }	    
		  }
		  return flattenedLayers;	  
		}
		
		/**
		 * Read the visible layers comma delimited
		 * 
		 * The visible layers of the requested map image.
		 * By default, the visible layers are as defined by the default visibility in 
		 * WMSLayerInfo.
		 * 
		 * @return String (never null)
		 */
		public function readLayersAsString():String {
		  return this.readLayersAsStringWorker();
		}
		
		public function readQueryableLayersAsString():String {
		  return this.readLayersAsStringWorker(true);	
		}
		
		private function readLayersAsStringWorker(onlyQueryable:Boolean = false):String {
		 var arrInfLayers:Array= readLayersAsArray().toArray().reverse();
          // The title and the name have been switched intentionall in 
          // WMSCapabilities class
          var arrLayers:Array = new Array();
          var containsQueryableLayer:Boolean = false;
          for each (var layerInf:WMSLayerInfo in readLayersAsArray()) {
          	if(onlyQueryable == true) {
          		if(!layerInf.queryable) {
          			continue;
          		}
          		var sQqueryable:String = layerInf.queryable.replace(/[^\d]*/g, "");
          		var nQqueryable:Number = parseInt(layerInf.queryable);
          		if(isNaN(nQqueryable) || nQqueryable != 1) {
          			continue;
          		}
          		containsQueryableLayer = true;
          	}
            if(layerInf.visible && layerInf.title != "") {
              if(Utils.chkString(layerInf.title) != "") {
                arrLayers.push(Utils.chkString(layerInf.title));
              }
            }
          }
          
          var layers:String = arrLayers.join(",");
          if(layers == null) {
            layers = "";
          }
          layers = layers.replace(/,,/g, "");
          if(layers.indexOf(",") == 0) {
            layers = layers.substr(1, layers.length - 1);
          }
          
          if(containsQueryableLayer == false && onlyQueryable == true) {
          	throw new Error("No queryable layers founds");
          }
          return layers;
			
		}
		/**
		 * Assumes all the SRS are in epsg
		 * */
		private function hasLayerWithSrs(wkid:int):Boolean {
			var arrRefs:ArrayCollection = this.layerSpatialRefs;
			for each ( var ref:String in arrRefs) {
				if(ref == null) {
					continue;
				}
				var num:Number = parseInt(ref);
				if(num == wkid) {
					return true;
				}
			}
			return false;
		}
		
		
		/**
		 * Gets the spatial reference to be used when getting url from endPoint
		 * 
		 * */
		public function getSendSpatialReference():String {
			var mapWkid:Number = this.map.spatialReference.wkid;
            var arrCrs:ArrayCollection = this.layerInfo.arrCrs;
            var sameAsMap:Boolean = false;
            for( var i:Number = 0; i < arrCrs.length; i++) {
                var srs:String = arrCrs.getItemAt(i) as String;
                if(srs == null || srs.toLowerCase().indexOf("crs:") >= 0) {
                    continue;
                }
                // T.M. Using tmpSrs 
                var tmpSrs:String = srs;
                srs = srs.replace(/[^\d]*/g, "");
                var wkid2:Number = parseInt(srs);
                
                if(ProjectionHandler.isEqualWkids(
                    wkid2, mapWkid) == true || (mapWkid == 4326 && wkid2 == 84)) {
                    return tmpSrs;
                }
            }
            
            return null;
		}
		
		/**
		 * Attempts to return the samep spatialreference as the map
		 * 
		 * return spatial reference of the layer
		 * */
		override public function get spatialReference():SpatialReference {
			var mapWkid:Number = this.map.spatialReference.wkid;
			var arrCrs:ArrayCollection = this.layerInfo.arrCrs;
			var sameAsMap:Boolean = false;
			for( var i:Number = 0; i < arrCrs.length; i++) {
				var srs:String = arrCrs.getItemAt(i) as String;
				if(srs == null || srs.toLowerCase().indexOf("crs:") >= 0 ) {
					continue;
				}
				srs = srs.replace(/[^\d]*/g, "");
				var wkid2:Number = parseInt(srs);
				if(ProjectionHandler.isEqualWkids(
				   wkid2, mapWkid) == true) {
					return new SpatialReference(mapWkid);
				}
			}
			
			return new SpatialReference(4326);
		}
		
		/**
		 * Checks whethe the given wkid is supported by this layer
		 * 
		 * @param wkid The wkid to be checked against
		 * 
		 * @return true/false if wkid is supported or not respectively
		 * */
		public function getSupportsWkid(wkid:Number):Boolean {
			var arrCrs:ArrayCollection = this.layerInfo.arrCrs;
			for( var i:Number = 0; i < arrCrs.length; i++) {
                var srs:String = arrCrs.getItemAt(i) as String;
                if(srs == null || srs.toLowerCase().indexOf("crs:") >= 0 ) {
                    continue;
                }
                srs = srs.replace(/[^\d]*/g, "");
                var wkid2:Number = parseInt(srs);
                if(ProjectionHandler.isEqualWkids(wkid, wkid2) == true) 
                {
                	return true;
                }
            }
            return false;
		}
		
		
		
	}
	
	

}
