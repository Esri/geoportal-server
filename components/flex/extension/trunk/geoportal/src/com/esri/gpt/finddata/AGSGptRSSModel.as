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
  import com.esri.ags.SpatialReference;
  import com.esri.ags.geometry.Extent;
  import com.esri.ags.geometry.Geometry;
  import com.esri.ags.geometry.MapPoint;
  import com.esri.ags.geometry.Polygon;
  import com.esri.ags.geometry.Polyline;
  import com.esri.gpt.utils.Utils;
  
  import flash.external.ExternalInterface;
  import flash.utils.Dictionary;
  
  import mx.collections.ArrayCollection;
  import mx.controls.Alert;
  import mx.logging.ILogger;
  import mx.logging.Log;
  import mx.rpc.AsyncToken;
  import mx.rpc.events.FaultEvent;
  import mx.rpc.events.ResultEvent;
  import mx.rpc.http.HTTPService;
  
  /**
  * Assists the AGS GPT RSS Widget Class. Can be statically held.
  * */  
	public class AGSGptRSSModel {
	 
	  // class variables ========================================================= 
	  private static const LOG:ILogger = 
	    Log.getLogger("com.esri.gpt.flexviewer.widgets.AGSGptRSSModel");
	    
	  // instance variables ======================================================  
	  private var configXml:XML = null;
	 
	  private var bUseProxy:Boolean = true;
	  private var sProxy:String;
	  private var add2MapCallBackfunc:Function;
	  private var bDebug:Boolean = false;
	  private var configService:HTTPService = new HTTPService();
	  private var _dctMapsBeingAdded:Dictionary = new Dictionary();
	 	  
		// constructors ===================================================
		public function AGSGptRSSModel(configXml:XML) {
		  if(configXml == null) {
		    return;
		  }
		     
		  this.configXml = configXml; 
		  
		  this.bUseProxy = Utils.chkBoolean(configXml.useProxy);
		  this.bDebug = Utils.chkBoolean(configXml.debug);
		}
		// properties
		public function get config():XML
		{
			return configXml;
		}
		
		/**
		 * Sets the proxy
		 * */
		public function set proxy(sProxy:String):void {
		  this.sProxy = sProxy;
		}
		
		/**
		 * Gets the proxy
		 * 
		 * @return proxy url string (trimmed, never null)
		 * 
		 * */
		public function get proxy():String {
		  var tmp:String = Utils.chkString(this.sProxy);
		  if(tmp.match(new RegExp("\\?")) == null ||
		    tmp.match(new RegExp("\\?")).length < 1) {
		    tmp = tmp + "?";
		  }
		  return Utils.chkString(tmp);
		}
		
		// methods =======================================================
		
		/**
		 * get the rrs Request timeout
		 * 
		 * @return  the number in the configuration xml or 0
		 * */
		private function get rssRequestTimeout():Number {
		  return Utils.chkNumber(this.configXml.searchTimeout, 0);
		  
		}
		
		/**
		 * Read the httpservice object
		 * 
		 * @return the object
		 * */
		private function readHttpService():HTTPService {
		  var httpService:HTTPService = new HTTPService();
		  return httpService;
		}
		
		
		
		
	
    
        
    /**
    * From a String, it returns the polygon bounding box.  Takes the first
    * 4 points to make a bbox.  If points are less than 4, then it makes a 
    * world extend.
    *  
    * @return Polygon of bounding box
    * */
    public function readBbox(bbox:String, spatialRef:SpatialReference):Polygon {
		  var polyBbox:Polygon= new Polygon();
			var points:Array = null;
			if(bbox != null) {
			  points = bbox.split(" ");
			}
			if(points == null || points.length < 4) {
			  points = new Array("-90.0", "-180.0", "90.0", "180.0");
			  spatialRef = new SpatialReference(4326);
			}
			if(spatialRef == null) {
			  spatialRef = SpatialReference(4326);
			}
				
			var path:Array = [];
			var minY:Number = Number(points[0]);
			var minX:Number = Number(points[1]);
			var maxY:Number = Number(points[2]);
			var maxX:Number = Number(points[3]);
				
			if(minY < -90 || minY > 90) {
			  minY = -90;
			}
			if(maxY < -90 || maxY > 90) {
			  minY = 90;
			}
				
			if(minX < -180 || minX> 180) {
				minX = -180;
			}
			if(maxX > 180 || maxX < -180) {
				maxX = 180;
			}
	           
	    if( minX < maxX) {
	      path.push(new MapPoint(minX, maxY));
	      path.push(new MapPoint(maxX, maxY));
	      path.push(new MapPoint(maxX, minY));
	      path.push(new MapPoint(minX, minY));
	      path.push(new MapPoint(minX, maxY));
	      polyBbox = new Polygon([path], spatialRef);
	    } else {
	      var path1:Array = new Array();
	      path1.push(new MapPoint(-180, maxY));
	      path1.push(new MapPoint(maxX, maxY));
	      path1.push(new MapPoint(maxX, minY));
	      path1.push(new MapPoint(-180, minY));
	      path1.push(new MapPoint(-180, maxY));
	         	              
	      var path2:Array = new Array();	              
	      path2.push(new MapPoint(minX, maxY));
	      path2.push(new MapPoint(180, maxY));
	      path2.push(new MapPoint(180, minY));
	      path2.push(new MapPoint(minX, minY));
	      path2.push(new MapPoint(minX, maxY));
	      polyBbox = new Polygon([path1, path2], spatialRef); 	
	    }
	    return polyBbox;
	  }
	  
	  /**
	  * Expands an extent
	  * 
	  * @extent Extent to be expanded
	  * @factor Number (Should be greater than 0)
	  * 
	  * @return Expanded extent or the same extent if factor <= 0
	  * */
	  public function expandExtent(extent:Extent, factor:Number):Extent {
		
			if(factor > 0) {
			  var ref:SpatialReference = extent.spatialReference;
			  if(ref == null) {
			      new SpatialReference(4326);
			  }
        var DEFAULT_ADJ:Number = 10;
        var OFFSET:Number = 20000;
        var xmin:Number = extent.xmin + OFFSET;
        var ymin:Number = extent.ymin + OFFSET;
        var xmax:Number = extent.xmax + OFFSET;
        var ymax:Number = extent.ymax + OFFSET;
        if(xmin < xmax || ymin < ymax) {
          if(xmin == xmax) {
            xmin = xmin - (DEFAULT_ADJ /2);
            xmax = xmax + (DEFAULT_ADJ /2);
          }
          if(ymin == ymax) {
            ymin = ymin - (DEFAULT_ADJ /2);
            ymax = ymax + (DEFAULT_ADJ /2);
          }
          var oldLength:Number = xmax - xmin;
          var oldWidth:Number = ymax - ymin;
          var newLength:Number = oldLength * factor;
          var newWidth:Number = oldWidth * factor;
          var diffLength:Number = newLength - oldLength;
          var diffWidth:Number = newWidth - oldWidth;
     
          var newXmin:Number = xmin - (diffWidth / 2);
          var newYmin:Number = ymin - (diffLength / 2);
          var newXmax:Number = xmax + (diffWidth / 2);
          var newYmax:Number = ymax + (diffLength / 2);
                    
          extent = new Extent();
          extent.xmin = newXmin - OFFSET;
          extent.ymin = newYmin - OFFSET;
          extent.xmax = newXmax - OFFSET;
          extent.ymax = newYmax - OFFSET;
          extent.spatialReference = ref;
        }
      }
     	return extent;
	  }
	  
	  /**
	  * Gets the Center of the Geometry
	  * 
	  * @param geom The geometry to be extracted the center of
	  * @return a map point (could be null)
	  * */
	  public function getGeomCenter(geom:Geometry):MapPoint {
		  var pt:MapPoint;
	    switch (geom.type) {
	      case Geometry.MAPPOINT: {
	        pt = geom as MapPoint;
	        break;
	      } case Geometry.POLYLINE: {
	        var pl:Polyline = geom as Polyline;
	        var pathCount:Number = pl.paths.length;
	        var pathIndex:int = int(pathCount / 2) - 1;
	        var midPath:Array = pl.paths[pathIndex];
	        var ptCount:Number = midPath.length;
	        var ptIndex:int = int(ptCount / 2) - 1;
	        pt = pl.getPoint(pathIndex, ptIndex);
	        break;
	      } case Geometry.POLYGON: {
	        var poly:Polygon = geom as Polygon;
	        pt = poly.extent.center;
	        break;
	      }
	    }
			return pt;
		}
		
		
		/**
		 * Fill serviceType and service url in the record object
		 * 
		 * @param record The record to manipulate
		 * @param listener Function to call with servicetype
		 * */
		public function readService(record:AGSGptRSSRecord, listener:Function, 
		  endPoint:AGSGptRSSEndPoint):void {
		  if(record == null) {
		    listener(null, null);
		  }
		  
		  var regDetectMapLinkProvider:ArrayCollection = 
		    endPoint.regDetectMapLinkProvider;
		  var regExtractMapservice:ArrayCollection = 
		    endPoint.regExtractMapService; 
		  var regExtractMapLinkType:ArrayCollection = 
		    endPoint.regExtractMapLinkType;
		  
		  for each (var objLink:Object in record.links) {
		    var link:AGSGptRSSEndPoint = objLink as AGSGptRSSEndPoint;
        if(link == null) {
          continue;
        }
        var bIsServiceHolder:Boolean = true;
        var sServiceUrl:String = link.siteUrl;
        var sServiceType:String = link.siteUrl;
        try {
          for each (var ptn:Object in regDetectMapLinkProvider) {
            if((new RegExp(ptn.sRegEx)).test(link.siteUrl) == false) {
              bIsServiceHolder = false;
            }
          }
          if(bIsServiceHolder == false) {
            continue;
          } 
          
          sServiceUrl = AGSGptRSSEndPoint.readString(regExtractMapservice, 
            sServiceUrl);
          sServiceUrl = unescape(sServiceUrl);
         
          if(Utils.chkString(sServiceUrl) != "" ) {
            // service url found
            record.mapServiceUrl = sServiceUrl;
            var tmpServiceType:String = sServiceType;
            sServiceType = AGSGptRSSEndPoint.readString(regExtractMapLinkType, 
              sServiceType);
            
            // Attempting to find service type object
            var enumServiceType:AGSGptEnumServiceTypes = 
              endPoint.extractServiceType(sServiceType);
            if(enumServiceType == null) {
              enumServiceType = endPoint.extractServiceType(sServiceUrl);
            }
            // Attempting to query end point for RSS
            if(enumServiceType == null) {
              add2MapCallBackfunc  = listener;
              configService.addEventListener(FaultEvent.FAULT, ignoreError);
              configService.addEventListener(ResultEvent.RESULT, loadRSSData);
              configService.resultFormat = "e4x";
              configService.requestTimeout = rssRequestTimeout;                    
	            var token:AsyncToken = this.connect(sServiceUrl, configService);
	            if(token != null) {
	              token.recordId = record.id;
	              token.serviceUrl = sServiceUrl;
	              token.endPoint = endPoint;
	            }
	            
            } else {
              listener(sServiceUrl, enumServiceType, record.id);
            }  
          }
        } catch(err:Error) {
          trace(err.message + "\n" + err.getStackTrace());
        }
      }
      listener(record.mapServiceUrl, record.serviceType, record);
		}
	
	 /**
	 * Removes links to be excluded and returns a subset of links to include
	 * 
	 * @param record The GPT RSS record
	 * @return arraycollection of links
	 * 
	 * */	
	 public function removeExcludedLinks(record:AGSGptRSSRecord,
	   endPoint:AGSGptRSSEndPoint):ArrayCollection {
	   var arrTmp:ArrayCollection = new ArrayCollection;
	   var arrLinksTmp:ArrayCollection = new ArrayCollection();
	   var ptn:String = endPoint.regExcludeLinks;
	   var regPtn:RegExp;
	   if(ptn != "") {
	     regPtn = new RegExp(ptn); 
	   } else {
	     return record.links;
	   }
	  
	   for each (var objLink:Object in record.links) {
	     var endPoint:AGSGptRSSEndPoint = objLink as AGSGptRSSEndPoint;
	     if(endPoint == null) {
	       continue;
	     }
	     if(regPtn.test(endPoint.siteUrl) == false) {
	       arrLinksTmp.addItem(objLink);
	     }
	    
	   }
	  
	   return arrLinksTmp;
	   
	 }	
		/**
		 * Load RSS Service
		 * 
		 * 
		 * */
		private function loadRSSData(event:ResultEvent):void {
		  try {
		    if(bDebug) {
		      Alert.show("Loading RSS DATA");
		    }
			  if (event.result is XML) {
				  var x:XML = XML(event.result);
				  var rssObj:AGSGptRSSParser = AGSGptRSSParser.createRssParser(
				    event.result as XML, this, event.token.endPoint );
				  var recAC:ArrayCollection = rssObj.agsGptRssRecords;
				  if(bDebug) {
				    Alert.show("# of RSS Records Found " + recAC.length); 
				  }
				 	if(recAC.length > 0) {
				 	  if(bDebug) {
		          Alert.show("RSS ServiceUrl = " + event.token.serviceUrl + " record id " 
		            +  event.token.recordId);
		        }
				 	  add2MapCallBackfunc(
			    	   event.token.serviceUrl,
			    	   AGSGptEnumServiceTypes.RSS,
			    	   event.token.recordId);
				  }
				 }
			} catch (error:Error) {
				trace(error.getStackTrace());										
			}
  	}
		
			
		/**
		 * Connnect to url
		 * 
		 * @param url Url to endpoint
		 * @param httpService httpservice to be used
		 * 
		 * @return the token (could be null)
		 * */
		public function connect(url:String, 
		  httpService:HTTPService):AsyncToken {
		  var token:AsyncToken = null;  
		  if(url == null || url == "") {
		    return null;
		  }
		  
		  if(!Utils.chkStringMatch(url.match(":\/\/")).length >= 1 && 
		     ExternalInterface.available == true) {
        
         var location:String = Utils.chkString(
           ExternalInterface.call( "location.href.substring")); 
         var regWebContext:String = Utils.chkString(this.configXml.regWebContext);
         var arr:Array = 
           Utils.chkStringMatch(location.match(new RegExp(regWebContext)));
      
         if(arr.length >= 1) {
           if(url.indexOf("/") == 0) {
             url = arr[0] + url;
           } else {
             url = arr[0] + "/" + url;
           }
         }
		  }
		  		  
			if (url) {
					if(bUseProxy) {
					  httpService.url = this.proxy + escape(url);
					  
					} else {
					  httpService.url = url;
					}
					LOG.debug("Connecting to url " + httpService.url);
					token = httpService.send();

			}
			return token;  
		}
		
		/**
	 * Ignores http 
		 * 
	* */
	private function ignoreError(event:FaultEvent):void {
	    if(bDebug) {
	      Alert.show(event.toString());
	    }
  	  trace(event);
    }
    
    /**
    * Creates the query url from the 
    * 
    * @param criteria The criteria object
    * @param url The url prefix
    * */
    public function createUrlCriteria(criteria:AGSGptRSSCriteria, 
      url:String):String {
      if(!url.match(new RegExp(".*\\?.*"))) {
        url = url + "?";
      }
      var params:Dictionary = new Dictionary();
      
      if(criteria.searchText != "") {
        params["searchText"] = criteria.searchText;
      }
      if(criteria.liveData == true) {
        params["contentType"] = "liveData"; 
      }
      var bIncludeBbox:Boolean = false;
      if (criteria.extentOption == AGSGptRSSCriteria.EXTENT_INTERSECT) {
        params["spatialRel"] = "esriSpatialRelOverlaps";
        bIncludeBbox = true;
      } else if (criteria.extentOption == AGSGptRSSCriteria.EXTENT_CONTAINS) {
        params["spatialRel"] = "esriSpatialRelWithin";
        bIncludeBbox = true;
      }
      if(bIncludeBbox == true) {
        var ext:Extent = criteria.extent;
        params["bbox"] = ext.xmin + "," + ext.ymin + "," + ext.xmax  
          + "," + ext.ymax;
      }
      
      if(criteria.startIndex > 0) {
        params["start"] = criteria.startIndex;
      }
      
      if(criteria.numOfResultsPerPage > 0) {
        params["max"] = criteria.numOfResultsPerPage;
      }
    
      params["f"] = "georss";
      
      var firstDone:Boolean = false;
      if(url.indexOf("?") > 1 && url.indexOf("?") < url.length) {
        firstDone = true;
      }
      for ( var key:Object in params) {
        if(firstDone == false) {
          firstDone = true;
        } else {
          url = url + "&";
        }
        url = url + key.toString() + "=" + params[key];
      }
      
      return url;
      
    }
    
  			
}
		
		
}