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
    import com.esri.ags.SpatialReference;
    import com.esri.ags.events.IdentifyEvent;
    import com.esri.ags.geometry.Extent;
    import com.esri.ags.geometry.Geometry;
    import com.esri.ags.geometry.MapPoint;
    import com.esri.ags.geometry.Polygon;
    import com.esri.ags.layers.GraphicsLayer;
    import com.esri.ags.tasks.IdentifyTask;
    import com.esri.ags.tasks.supportClasses.IdentifyParameters;
    import com.esri.ags.tasks.supportClasses.IdentifyResult;
    
    import flash.events.Event;
    
    import mx.collections.ArrayCollection;
    import mx.logging.ILogger;
    import mx.logging.Log;
    import mx.messaging.messages.IMessage;
    import mx.rpc.AsyncToken;
    import mx.rpc.Fault;
    import mx.rpc.IResponder;
    import mx.rpc.events.FaultEvent;

    /**
    * Identify task for geoprocessing layer
    * */
    public class GeoRssLayerIdentify extends IdentifyTask
    {
        // instance variables  =================================================
        public var _layer:GeoRssLayer = null;
        public var maxFeaturesIntersect:Number = 20;
        private const LOG:ILogger = Log.getLogger(
          "GeoRssLayerIdentify");
          
        // constructor =========================================================
        /**
         * constructor
         * */
        public function GeoRssLayerIdentify(
          layer:GeoRssLayer) 
        {
            _layer = layer;
        }
        
        // methods =============================================================
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
             
             var fault:Fault = new Fault("001", "Layer is set to invisible",
               "Layer is set to invisible");
             var message:IMessage = null;
             var faultEvent:Event = new FaultEvent(FaultEvent.FAULT, false, true, 
               fault, null, message);
             this.dispatchEvent(faultEvent);
             return null;
           }
           var geometry:Geometry    = identifyParameters.geometry;
           var tolerance:Number = identifyParameters.tolerance;
           
           var mousePointExtent:Extent= geometry.extent;
           var mapPoint:MapPoint = geometry as MapPoint;
           
           mousePointExtent = identifyPointToExtent(mapPoint, 
              identifyParameters.width,
              identifyParameters.mapExtent.width, tolerance, 
              identifyParameters.spatialReference);
              
           var graphicsLayer:GraphicsLayer = _layer.graphicsLayer;
           var arrColl:ArrayCollection = new ArrayCollection();
           if(graphicsLayer.graphicProvider is ArrayCollection) {
                arrColl = graphicsLayer.graphicProvider as ArrayCollection;
           }
           var graphic:Graphic = null;
           var arryResults:ArrayCollection = new ArrayCollection();
           var index:Number = 0;
           for(var i:Number = 0; i < arrColl.length; i++) {
                graphic = arrColl[i] as Graphic;
                if(graphic == null) {
                    continue;
                }
                var pass:Boolean = false;
                var geom:Geometry = graphic.geometry;
                if(geom is Polygon) {
                    var poly:Polygon = geom as Polygon;
                    pass = poly.contains(mapPoint);
                } else {
                    pass = mousePointExtent.intersects(graphic.geometry);
                }   
                if(pass) {  
                	index++;
                    var idResult:IdentifyResult = new IdentifyResult();
                    //idResult.layerId = _layer.id;
                    idResult.layerName = _layer.name;
                    idResult.feature = graphic;
                    arryResults.addItem(idResult);
                    if( index >= maxFeaturesIntersect) {
                    	
                        LOG.debug("Max Featurs Intersect Reached" 
                          + maxFeaturesIntersect);
                        break;
                    }
                }
                
            } 
            var event:IdentifyEvent = 
              new IdentifyEvent(IdentifyEvent.EXECUTE_COMPLETE, 
                arryResults.toArray());
            
            this.dispatchEvent(event);
           
           return null;
        }

        
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
    }
    
    
}
