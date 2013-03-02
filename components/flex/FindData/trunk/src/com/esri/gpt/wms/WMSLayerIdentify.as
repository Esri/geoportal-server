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
	import com.esri.ags.Graphic;
	import com.esri.ags.SpatialReference;
	import com.esri.ags.events.IdentifyEvent;
	import com.esri.ags.geometry.Extent;
	import com.esri.ags.geometry.Geometry;
	import com.esri.ags.geometry.MapPoint;
	import com.esri.ags.tasks.IdentifyTask;
	import com.esri.ags.tasks.supportClasses.IdentifyParameters;
	import com.esri.ags.tasks.supportClasses.IdentifyResult;

	
	import flash.events.Event;
	import flash.geom.Rectangle;
	
	import mx.collections.ArrayCollection;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.messaging.management.Attribute;
	import mx.messaging.messages.IMessage;
	import mx.rpc.AsyncResponder;
	import mx.rpc.AsyncToken;
	import mx.rpc.Fault;
	import mx.rpc.IResponder;
	import mx.rpc.events.FaultEvent;
	import mx.rpc.events.ResultEvent;
	import mx.rpc.http.HTTPService;
	import mx.utils.ObjectUtil;

	public class WMSLayerIdentify extends IdentifyTask
	{
	
		
				
		  // instance variables  =================================================
        public var _layer:WMSLayer = null;
        public var maxFeaturesIntersect:Number = 20;
        private const LOG:ILogger = Log.getLogger(
          "WMSLayerIdentify");
          
        // constructor =========================================================
        /**
         * constructor
         * */
        public function WMSLayerIdentify(
          layer:WMSLayer) 
        {
            _layer = layer;
        }
        
        // methods =============================================================
        
         /**
         * Identify point buffered to extent
         * 
         * @param point The point in the map
         * @mapWidth Pixel width of the map
         * @extentWidth Width of maxX - maxY
         * @toleranceInPixels The tolerance
         * @spatialReference Spatial reference of the map
         * 
         * Idea from http://blogs.esri.com/Dev/blogs/arcgisserver/archive/
         * 2010/02/08/Find-graphics-under-a-mouse-click-with-the-ArcGIS-API-for-JavaScript.aspx
         * **/
        private function identifyPointToExtent(point:MapPoint, mapWidth:Number,
           extentWidth:Number, toleranceInPixels:Number, 
           spatiaReference:SpatialReference):Extent {
            
           
           var pixelWidth:Number = extentWidth / mapWidth;
           var toleratedBuffer:Number = toleranceInPixels * pixelWidth;
           
           return new Extent(
               point.x - toleratedBuffer, point.y - toleratedBuffer,
               point.x + toleratedBuffer, point.y + toleratedBuffer,
               spatiaReference);
    
        }
        
         /**
         * Execute function to get the results
         * 
         * @identifyPrameters Parameters for getting 
         * @responder not used
         * @returns null ( we dont go online to check for the params)
         * */
        override public function execute(identifyParameters:IdentifyParameters, 
            responder:IResponder = null):AsyncToken {
           if(_layer.visible == false) {
             this.notifyFault(this._layer.id + "001",
               "Layer is set to invisible");
             return null;
           }
           var getFeatureInfoUrl:String =  _layer.capabilities.urlGetFeatureInfo;
           if(getFeatureInfoUrl == null || getFeatureInfoUrl == "") {
           	 this.notifyFault(this._layer.id + "002","Does not support Get Feature info");
           	 return null;
           }
            
           LOG.debug("Get Feature Url " + getFeatureInfoUrl);
           if(getFeatureInfoUrl.match(new RegExp("\\?")) == null ||
               getFeatureInfoUrl.match(new RegExp("\\?")).length < 1) {
               getFeatureInfoUrl = getFeatureInfoUrl + "?";
           }
                     
           getFeatureInfoUrl = getFeatureInfoUrl.replace(/&?service=wms/i, "");
           getFeatureInfoUrl = getFeatureInfoUrl.replace(/&?request=GetCapabilities/i, "");
           getFeatureInfoUrl = getFeatureInfoUrl.replace(/&?request=GetFeatureInfo/i, "");
           getFeatureInfoUrl = getFeatureInfoUrl.replace(/&?request=GetMap/i, "");
           //getFeatureInfoUrl = getFeatureInfoUrl.replace(/&?version=(\d|\.)*/i, "");
            
           var mapURL:String = getFeatureInfoUrl;
            if(mapURL.indexOf("?") < mapURL.length -1 ) {
              mapURL += "&";
            }
            mapURL += "service=WMS";
            mapURL += "&version=" + _layer.capabilities.version;
            mapURL += "&request=GetFeatureInfo";
            var mapPoint:MapPoint = identifyParameters.geometry as MapPoint;
            var requestSR:String;
            var extent:Extent = identifyPointToExtent(mapPoint, 
              identifyParameters.width,
              identifyParameters.mapExtent.width, identifyParameters.tolerance, 
              identifyParameters.spatialReference);
            try  {
              mapURL += "&query_layers="+ _layer.readQueryableLayersAsString();
            } catch(error:Error) {
              this.notifyFault(this._layer.id + "004", error.message);
              return null;	
            }
            var imageRect:Rectangle = _layer.buildImageRect();
            mapURL += "&width=1";
            mapURL += "&height=1";
            mapURL += "&info_format=application/vnd.ogc.wms_xml" 
            if (_layer.srs)
            {
                requestSR = _layer.srs;
            }
                        
            if (_layer.capabilities.version == "1.3.0")
            {
                if (requestSR)
                {
                    mapURL += "&crs=" + requestSR;
                }
                // This WMS version has axis order dependent on CRS authority (e.g. EPSG)
                if (requestSR && requestSR.indexOf("EPSG:") != -1)
                {
                    // Axis order: (y,x) (lat,lon)
                    mapURL += "&bbox=" + 
                      extent.ymin+","+extent.xmin+","+extent.ymax+","+extent.xmax;
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
            
           
            if (  proxyURL)
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
                logger.debug("{0}::WMS GetFeatureInfo URL: {1}", id, mapURL)
            }
            
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
                  	this.notifyFault(this._layer.id + "004", error);
                  } else {
                  	var arryResults:ArrayCollection = new ArrayCollection();
                    var table:XML = event.result as XML;
                    
                    for each (var field:XML in table.FIELDS) {
                    	
                    	var objAttr:Object = new Object();      
                    	
                    	var atts:XMLList = field.attributes(); 
                        for (var i:int = 0; i < atts.length(); i++)
                        {
                          var name:String  = String(atts[i].name());
                          var value:String =  atts[i].toXMLString();
                          objAttr[name] = value;
                        }
                    	
                    	var geometry:Geometry = mapPoint;
                        var gra:Graphic = new Graphic(geometry);
                    	gra.attributes = objAttr;
                    	var idResult:IdentifyResult = new IdentifyResult();
                    //idResult.layerId = _layer.id;
                        idResult.layerName = _layer.name;
                        idResult.feature = gra;
                        arryResults.addItem(idResult);
                    	
                    	
                    }
                    var identifyEvent:IdentifyEvent = 
                     new IdentifyEvent(IdentifyEvent.EXECUTE_COMPLETE, 
                      arryResults.toArray());
                    dispatchEvent(identifyEvent);
                   
                  }
                },
                function onFault( event:FaultEvent, token:Object = null ):void {
                    this.notifyFault( 
                       this._layer.id + "004",
                       ": Error while connecting to url" + event.token.url);
                    if (Log.isError()) {
                        logger.error("{0}::{1}", id, ObjectUtil.toString(event));
                    }
                },
                mapURL
            ));
            
       
            /*var idResult:IdentifyResulr = new IdentifyResult();
                    //idResult.layerId = _layer.id;
                    idResult.layerName = _layer.name;
                    idResult.feature = graphic;
                    arryResults.addItem(idResult);
                  
                    
            var event:IdentifyEvent = 
              new IdentifyEvent(IdentifyEvent.IDENTIFY_COMPLETE, 
                arryResults.toArray());
            
            this.dispatchEvent(event);*/
           
           return null;
        }
        
        /**
        * Dispatches a fault eveent
        * */
        private function notifyFault(faultCode:String, message:String):void {
        	var fault:Fault = new Fault(faultCode, message);
        	
        	var iMessage:IMessage;
            var faultEvent:Event = new FaultEvent(FaultEvent.FAULT, false, true, 
               fault, null, iMessage);
             this.dispatchEvent(faultEvent);
        }
       
        

        
   }
		
	
}