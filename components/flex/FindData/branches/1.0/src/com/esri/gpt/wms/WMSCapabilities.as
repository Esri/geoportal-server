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
	import com.esri.ags.SpatialReference;
	
	import mx.collections.ArrayCollection;
	import mx.logging.ILogger;
	import mx.logging.Log;
	import mx.utils.ObjectUtil;
	
	//--------------------------------------
	//  Other metadata
	//--------------------------------------
	
	[Bindable]
	
	/**
	 * WMS service capabilities.
	 */
	public class WMSCapabilities
	{
		//--------------------------------------------------------------------------
		//
		//  Constants
		//
		//--------------------------------------------------------------------------
		
		private static const WMS:Namespace = 
		  new Namespace("http://www.opengis.net/wms");
		private static const XLINK:Namespace = 
		  new Namespace("http://www.w3.org/1999/xlink");
		private static const LOG:ILogger = 
		  Log.getLogger("com.esri.gpt.wms.WMSCapabilities");
		
		//--------------------------------------------------------------------------
		//
		//  Variables
		//
		//--------------------------------------------------------------------------
		
		// WMS version ("1.1.1", "1.3.0")
		public var version:String;
		
		// Service info
		public var serviceName:String;
		public var serviceTitle:String;
		public var urlGetMap:String;
		public var urlGetFeatureInfo:String;
		
		// Max image size, in pixels
		public var maxWidth:Number;
		public var maxHeight:Number
		
		// Capability info
		public var layerInfo:WMSLayerInfo;
		
		//--------------------------------------------------------------------------
		//
		//   Methods
		//
		//--------------------------------------------------------------------------
		
		/**
		 * @private
		 */
		public function toString():String
		{
			return ObjectUtil.toString(this);
		}
		
		/**
		 * Factory method for creating a WMSCapabilities instance from the XML
		 * response of a GetCapabilities URL request. // TODO Make it catch exception
		 */
		public static function fromXML( xml:XML ):WMSCapabilities
		{
			var result:WMSCapabilities;
			
			if (xml) {
				//trace(xml);
				
				result = new WMSCapabilities();
				
				// A version is required so we know how to ask the server for a map
				if (xml.@version[0]) {
					result.version = xml.@version;
				} else {
					return null;
				}
				
				var ns:Namespace = xml.namespace();
				if (ns && ns.uri == WMS.uri) {
					parseXmlNs(xml, result);
				} else {
					parseXml(xml, result);
				}
			}
			
			if(result.layerInfo != null) {
			  setIds(result.layerInfo);
			}
			
			return result;
		}
		
		private static function parseXmlNs( xml:XML, result:WMSCapabilities ):void
		{
			if (xml.WMS::Service.WMS::Name[0]) {
				result.serviceName = xml.WMS::Service.WMS::Name;
			}
			if (xml.WMS::Service.WMS::Title[0]) {
				result.serviceTitle = xml.WMS::Service.WMS::Title;
			}
			if (xml.WMS::Service.WMS::MaxWidth[0]) {
				result.maxWidth = xml.WMS::Service.WMS::MaxWidth;
			}
			if (xml.WMS::Service.WMS::MaxHeight[0]) {
				result.maxHeight = xml.WMS::Service.WMS::MaxHeight;
			}
			
			var getMapUrl:String = 
			  xml.WMS::Capability.WMS::Request.WMS::GetMap.WMS::DCPType.WMS::HTTP.WMS::Get.WMS::OnlineResource.@XLINK::href;
			if(getMapUrl != null && getMapUrl != "") {
			   result.urlGetMap = getMapUrl;
			}
			
			try {
				var getFeatureInfoUrl:String = 
				  xml.WMS::Capability.WMS::Request.WMS::GetFeatureInfo.WMS::DCPType.WMS::HTTP.WMS::Get.WMS::OnlineResource.@XLINK::href;
				if(getFeatureInfoUrl != null && getFeatureInfoUrl != "") {
					result.urlGetFeatureInfo = getFeatureInfoUrl;
				} 
			} catch(error:Error) {
				LOG.debug("Capabilitles does not have get feature info");
			}
			
			var layerXML:XML = xml.WMS::Capability.WMS::Layer[0];
			if (layerXML) {
				result.layerInfo = parseLayerInfoNs(layerXML,null);
			}
		}
		
		private static function parseXml( xml:XML, result:WMSCapabilities ):void
		{
			if (xml.Service.Name[0]) {
				result.serviceName = xml.Service.Name;
			}
			if (xml.Service.Title[0]) {
				result.serviceTitle = xml.Service.Title;
			}
			if (xml.Service.MaxWidth[0]) {
				result.maxWidth = xml.Service.MaxWidth;
			}
			if (xml.Service.MaxHeight[0]) {
				result.maxHeight = xml.Service.MaxHeight;
			}
			var getMapUrl:String = 
			  xml.Capability.Request.GetMap.DCPType.HTTP.Get.OnlineResource.@XLINK::href;
			if(getMapUrl != null && getMapUrl != "") {
			   result.urlGetMap = getMapUrl;
			}
			
			try {
				var getFeatureInfoUrl:String = 
	              xml.Capability.Request.GetFeatureInfo.DCPType.HTTP.Get.OnlineResource.@XLINK::href;
	            if(getFeatureInfoUrl != null && getFeatureInfoUrl != "") {
	                result.urlGetFeatureInfo = getFeatureInfoUrl;
	            } 
            } catch(error:Error) {
            	LOG.debug("Capabilitles does not have get feature info");
            }
            
              
			
			var layerXML:XML = xml.Capability.Layer[0];
			if (layerXML) {
				result.layerInfo = parseLayerInfo(layerXML, null);
			}
		}
		
		private static function parseLayerInfoNs( layerXML:XML, 
		  parentInfo:WMSLayerInfo = null):WMSLayerInfo
		{
			var layerInfo:WMSLayerInfo = new WMSLayerInfo();
			layerInfo.parentLayer = parentInfo;
			layerInfo.queryable = layerXML.@queryable
			// exchanging name and title.  title will be used for layer ids,
			// name is used in the widget as the label of the WMS drop down menu
			if (layerXML.WMS::Name[0]) {
				layerInfo.title = layerXML.WMS::Name;
			}
			if (layerXML.WMS::Title[0]) {
				layerInfo.name = layerXML.WMS::Title;
			}
			if (layerXML.WMS::SRS[0]) {
                layerInfo.spatialRef = layerXML.WMS::SRS; 
            }
            try {
              if (layerXML.WMS::Style.WMS::LegendURL.WMS::OnlineResource.@XLINK::href) {
            	layerInfo.legendUrl = layerXML.WMS::Style.WMS::LegendURL.WMS::OnlineResource.@XLINK::href;
              }
            } catch(err:Error) {
            	//No style found
            }
            
            var tmp:String = "";
            var xSrs:XML;
            if(layerInfo.arrCrs.length < 1) {
	            for each (xSrs in layerXML.WMS::SRS ) {
	                tmp = xSrs.text();
	                layerInfo.arrCrs.addItem(tmp);
	            }
	            for each (xSrs in layerXML.WMS::CRS ) {
	                tmp = xSrs.text();
	                layerInfo.arrCrs.addItem(tmp);
	            }
            }
            var crs:String;
            var west:Number;
            var east:Number;
            var south:Number;
            var north:Number
            
            if(layerXML.EX_GeographicBoundingBox.length > 0) {
	            west = 
	              parseInt(layerXML.WMS::EX_GeographicBoundingBox[0].WMS::westBoundLongitude);
	            east = 
	              parseInt(layerXML.WMS::EX_GeographicBoundingBox[0].WMS::eastBoundLongitude);
	            south = 
	              parseInt(layerXML.WMS::EX_GeographicBoundingBox[0].WMS::southBoundLatitude);
	            north =
	              parseInt(layerXML.WMS::EX_GeographicBoundingBox[0].WMS::northBoundLatitude);
	            var extent:WMSExtent;  
	            if(!(isNaN(west) || isNaN(east) || isNaN(north) || isNaN(south))) {
	                extent = new WMSExtent(west, south, east, north, 
	                    new SpatialReference(4326));
	                layerInfo.bBox.addItem(extent);    	
	            }  
            }
                        
            for each (var xLatLon:XML in layerXML.WMS::BoundingBox) {
            	west = parseInt(xLatLon.@minx);
            	east = parseInt(xLatLon.@maxx);
            	south = parseInt(xLatLon.@miny);
            	north = parseInt(xLatLon.@maxy);
            	crs = xLatLon.@CRS || xLatLon.@CRS;
            	if(!(isNaN(west) || isNaN(east) || isNaN(north) || isNaN(south))) {
                    extent = new WMSExtent(west, south, east, north);
                    extent.rawSpatial = crs;
                    layerInfo.bBox.addItem(extent);     
                }  
            } 
            
			
			var subLayers:Array = null;
			var children:XMLList = layerXML.WMS::Layer;
			for each (var child:XML in children) {
				var childInfo:WMSLayerInfo = parseLayerInfoNs(child, layerInfo);
				if (!subLayers) {
					subLayers = [];
				}
				subLayers.push(childInfo);
			}
			
			// Reverse the layer order so that the topmost layer is first (top -> bottom).
			// This matches the layer order of the ArcGIS and ArcIMS layer infos in the Flex API.
			if (subLayers) {
				subLayers.reverse();
			}
			
			layerInfo.subLayers = subLayers;
			
			return layerInfo;
		}
		
		private static function parseLayerInfo( layerXML:XML, 
		  parentInfo:WMSLayerInfo = null ):WMSLayerInfo
		{
			var layerInfo:WMSLayerInfo = new WMSLayerInfo();
			layerInfo.parentLayer = parentInfo;
			layerInfo.queryable = layerXML.@queryable;
							
			// exchanging name and title.  title will be used for layer ids,
			// name is used in the widget as the label of the WMS drop down menu
			if (layerXML.Name[0]) {
				layerInfo.title = layerXML.Name;
			}
			if (layerXML.Title[0]) {
				layerInfo.name = layerXML.Title;
			}
			if (layerXML.SRS[0]) {
				layerInfo.spatialRef = layerXML.SRS;
			}
			try {
				if (layerXML.Style.LegendURL.OnlineResource.@XLINK::href) {
	                layerInfo.legendUrl = layerXML.Style.LegendURL.OnlineResource.@XLINK::href;
	            } 
            } catch(err:Error) {
                //No style found
            }
			
			var tmp:String = "";
            var xSrs:XML;
            if(layerInfo.arrCrs.length < 1) {
	            for each (xSrs in layerXML.WMS::SRS ) {
	                tmp = xSrs.text();
	                layerInfo.arrCrs.addItem(tmp);
	            }
	            for each (xSrs in layerXML.WMS::CRS ) {
	                tmp = xSrs.text();
	                layerInfo.arrCrs.addItem(tmp);
	            }
            }
            var crs:String;
            var west:Number;
            var east:Number;
            var south:Number;
            var north:Number;
            var extent:WMSExtent; 
            if(layerXML.EX_GeographicBoundingBox.length > 0) {
	            west = 
	              parseInt(layerXML.EX_GeographicBoundingBox[0].westBoundLongitude);
	            east = 
	              parseInt(layerXML.EX_GeographicBoundingBox[0].eastBoundLongitude);
	            south = 
	              parseInt(layerXML.EX_GeographicBoundingBox[0].southBoundLatitude);
	            north =
	              parseInt(layerXML.EX_GeographicBoundingBox[0].northBoundLatitude);
	             
	            if(!(isNaN(west) || isNaN(east) || isNaN(north) || isNaN(south))) {
	                extent = new WMSExtent(west, south, east, north, 
	                    new SpatialReference(4326));
	                layerInfo.bBox.addItem(extent);     
	            }
            }
            
                            
            for each (var xLatLon:XML in layerXML.BoundingBox) {
                west = parseInt(xLatLon.@minx);
                east = parseInt(xLatLon.@maxx);
                south = parseInt(xLatLon.@miny);
                north = parseInt(xLatLon.@maxy);
                crs = xLatLon.@CRS || xLatLon.@SRS;
                if(!(isNaN(west) || isNaN(east) || isNaN(north) || isNaN(south))) {
                    extent = new WMSExtent(west, south, east, north);
                    extent.rawSpatial = crs;
                    layerInfo.bBox.addItem(extent);     
                }  
            } 
            
			var subLayers:Array = null;
			var children:XMLList = layerXML.Layer;
			for each (var child:XML in children) {
				var childInfo:WMSLayerInfo = parseLayerInfo(child, layerInfo);
				if (!subLayers) {
					subLayers = [];
				}
				subLayers.push(childInfo);
			}
			
			// Reverse the layer order so that the topmost layer is first (top -> bottom).
			// This matches the layer order of the ArcGIS and ArcIMS layer infos in the Flex API.
			if (subLayers) {
				subLayers.reverse();
			}
			
			layerInfo.subLayers = subLayers;
			
			return layerInfo;
		}
		
		private static function setIds(layerInfo:WMSLayerInfo) : void {
		  var flattenedLayers:ArrayCollection = new ArrayCollection();
		  flattenedLayers.addItem(layerInfo);
		  
		  layerInfo.id = 0;
		  layerInfo.parentLayerId = -1;
		  var idIndex:Number = 0;
		  
		  for each (var layerInfo:WMSLayerInfo in flattenedLayers) {
		    if(layerInfo == null) {
		      continue;
		    }
		    	    
		    for each ( var childLayerInfo:WMSLayerInfo in layerInfo.subLayers) {
		      idIndex++;
		      childLayerInfo.id = idIndex;
		      childLayerInfo.parentLayerId = layerInfo.id;
		      if(layerInfo.subLayerIds == null) {
		        layerInfo.subLayerIds = new Array();
		      }
		      layerInfo.subLayerIds.push(childLayerInfo.id);
		      flattenedLayers.addItem(childLayerInfo);
		    }
		    
		  }
		  
			
		}
	}

}
