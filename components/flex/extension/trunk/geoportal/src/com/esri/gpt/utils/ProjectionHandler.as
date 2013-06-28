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
package com.esri.gpt.utils {

import com.esri.ags.SpatialReference;
import com.esri.ags.events.GeometryServiceEvent;
import com.esri.ags.geometry.Extent;
import com.esri.ags.geometry.Geometry;
import com.esri.ags.geometry.MapPoint;
import com.esri.ags.geometry.Polygon;
import com.esri.ags.tasks.GeometryService;
import com.esri.ags.utils.JSONUtil;
import com.esri.ags.utils.WebMercatorUtil;
import com.esri.gpt.utils.Ajax;
import com.esri.gpt.utils.AjaxRequestType;

import mx.logging.ILogger;
import mx.logging.Log;
import mx.rpc.events.FaultEvent;

import widgets.FindData.FindDataWidget;
    

/**
 * @author TM
 * 
 * Class for reprojecting geometries.  Yet to be complete.  
 * 
 * TODO: Add geometry service to the workflow
 * 
 * */
public class ProjectionHandler
{
    
  // Static variables ========================================================= /
  // TODO: Relocate the ilogger
  private static const LOG:ILogger = 
      Log.getLogger("com.esri.gpt.ProjectionHandler");
  // Field Variables ==========================================================/
  /** Spatial Reference Being Projected To **/  
  private var spatialReferenceTo:SpatialReference;  
  
  private static var geometryService:String;
  
  // constructor ==============================================================/
  /**
  * Constructor
  * 
  * @param spatialReferenceTo spatial reference to be used for reprojection
  * */
  public function ProjectionHandler(spatialReferenceTo:SpatialReference)
  {
    this.spatialReferenceTo = spatialReferenceTo;
    
  }

  // methods ==================================================================/
  [Inspectable(category="Mapping")]
  /**
   * The geometryService request through.
   */
  public function get getGeometryService():String 
  {
	  return geometryService;
  }
  /**
   * @private
   */
  public function set setGeometryService( value:String ):void
  {	  
	  geometryService = value;
  }
  
  /**
  * Reprojects Geometry.  
  * 
  * @param geometry Geometry to be reprojected. If spatial reference missing t
  * then its assumed the spatial reference is 4326.
  * 
  * @return Reprojected geometry or null if not  
  * */      
  public function reprojectGeom(geometry:Geometry):Geometry
  {
    if(geometry.spatialReference == null) {
      geometry.spatialReference = new SpatialReference(4326);
    }
    if(geometry == null) {
      return null;
    } else if(this.spatialReferenceTo.wkid == geometry.spatialReference.wkid) {
      return geometry;
    } else if(geometry is Polygon) {
      return this.reprojectPoly(geometry as Polygon)
    } else if(geometry is MapPoint) {
      return this.reprojectPoint(geometry as MapPoint);
    } else if(geometry is Extent) {
      return this.reprojectExtent(geometry as Extent);
    }
    return null;
  }
  
  /**
  * Reprojects a polygon.
  * 
  * @param polygon Polygon to be reprojected. If spatial reference missing t
  * then its assumed the spatial reference is 4326.
  * 
  * @return Reprojected polygon or null if cannto be reprojected
  * **/
  public function reprojectPoly(polygon:Polygon):Polygon {
    LOG.info("Reproject Polygon");
    if(polygon == null || polygon.rings == null) {
        LOG.info("Recieved Polygon is null.");
        return null;
    }
    var tmpPolygon:Polygon = new Polygon();
    tmpPolygon.rings = new Array();
    for each ( var array:Array in polygon.rings) {
        if(array == null) {
            continue;
        }
            
        var arr:Array = new Array();
        for each (var mapPoint:MapPoint in array) {
            if(mapPoint == null) {
                continue;
            }
            if(mapPoint.spatialReference == null ||
               this.isExtremeNumber(mapPoint.spatialReference.wkid)) {
              mapPoint.spatialReference = new SpatialReference(4326);
            }
            var tmpMapPoint:MapPoint = this.reprojectPoint(mapPoint);
            if(tmpMapPoint == null) {
                
              return null;
            }
            arr.push(tmpMapPoint);
        }
        tmpPolygon.rings.push(arr);
    }
    return tmpPolygon;
  }
  
  /**
  * Reprojects the Extent
  * 
  * @param extent The extent to be reprojected. If spatial reference missing t
  * then its assumed the spatial reference is 4326.
  * 
  * @returns reprojected Extent or null if extent cannot be reprojected
  * @Throws Error if reprojection not possible
  * */
  public function reprojectExtent(extent:Extent):Extent {
    LOG.info("Reprojecting Extent ");
    if(extent == null) {
        LOG.info("");
        return null;
    }
    LOG.info("maxx= " + extent.xmax + ", maxy=" + extent.ymax +
      ", minx=" + extent.xmin + ", miny=" + extent.ymin);
    if(extent.spatialReference == null || 
      this.isExtremeNumber(extent.spatialReference.wkid)) 
    {
        extent.spatialReference = new SpatialReference(4326);
    }
    
    var tmpExtent:Extent = null;
    if(isEqualWkids(spatialReferenceTo.wkid, extent.spatialReference.wkid)) {
        return new Extent(extent.xmin, extent.ymin, extent.xmax, extent.ymax,
		 new SpatialReference(spatialReferenceTo.wkid));
    }
    else if (spatialReferenceTo.wkid == 102113 || 
      spatialReferenceTo.wkid == 102100 ||
       spatialReferenceTo.wkid== 3857) { //TODO: 102100
        tmpExtent = new Extent();
        tmpExtent.xmax = (extent.xmax > 179)? 179 :extent.xmax;
        tmpExtent.xmax = (extent.xmax <-179)? -179 :tmpExtent.xmax;
        tmpExtent.xmin = (extent.xmin > 179)? 179 :extent.xmin;
        tmpExtent.xmin = (extent.xmin <-179)? -179 :tmpExtent.xmin;
        tmpExtent.ymax = (extent.ymax > 85)? 85 :extent.ymax;
        tmpExtent.ymax = (extent.ymax <-85)? -85 :tmpExtent.ymax;
        tmpExtent.ymin = (extent.ymin > 85)? 85 :extent.ymin;
        tmpExtent.ymin = (extent.ymin <-85)? -85 :tmpExtent.ymin;
        tmpExtent = WebMercatorUtil.geographicToWebMercator(tmpExtent) as Extent;
        if(tmpExtent == null || isExtremeNumber(tmpExtent.xmax) || 
          isExtremeNumber(tmpExtent.xmin) || isExtremeNumber(tmpExtent.ymax) || 
          isExtremeNumber(tmpExtent.ymin)) {
          LOG.info("Could not reproject extent to spatialRef=" 
            + spatialReferenceTo.wkid);     
          throw new Error("Could not reproject Extent spatial ref " 
            + extent.spatialReference.wkid + ", maxx= " + extent.xmax + ", maxy=" 
            + extent.ymax + ", minx=" + extent.xmin + ", miny=" + extent.ymin);
        }
    } else if(spatialReferenceTo.wkid == 4326 
      && (extent.spatialReference.wkid == 102113 
        || extent.spatialReference.wkid == 102100 
        || extent.spatialReference.wkid == 3857)) {
        tmpExtent = new Extent();
        tmpExtent.spatialReference = 
          new SpatialReference(extent.spatialReference.wkid);
        tmpExtent.xmax = (extent.xmax > 20037508.34)? 20037508.34 :extent.xmax;
        tmpExtent.xmax = (extent.xmax <-20037508.34)? -20037508.34 :tmpExtent.xmax;
        tmpExtent.xmin = (extent.xmin > 20037508.34)? 20037508.34 :extent.xmin;
        tmpExtent.xmin = (extent.xmin <-20037508.34)? -20037508.34 :tmpExtent.xmin;
        tmpExtent.ymax = (extent.ymax > 20037508.34)? 20037508.34 :extent.ymax;
        tmpExtent.ymax = (extent.ymax <-20037508.34)? -20037508.34 :tmpExtent.ymax;
        tmpExtent.ymin = (extent.ymin > 20037508.34)? 20037508.34 :extent.ymin;
        tmpExtent.ymin = (extent.ymin <-20037508.34)? -20037508.34 :tmpExtent.ymin;
        tmpExtent = WebMercatorUtil.webMercatorToGeographic(tmpExtent) 
          as Extent;
        if(tmpExtent == null || isExtremeNumber(tmpExtent.xmax) || 
          isExtremeNumber(tmpExtent.xmin) || isExtremeNumber(tmpExtent.ymax) || 
          isExtremeNumber(tmpExtent.ymin)) {
          LOG.info("Could not reproject extent to spatialRef=" 
            + spatialReferenceTo.wkid);     
          throw new Error("Could not reproject Extent spatial ref " 
            + extent.spatialReference.wkid + ", maxx= " + extent.xmax + ", maxy=" 
            + extent.ymax + ", minx=" + extent.xmin + ", miny=" + extent.ymin);
        }
        
    } else {
		tmpExtent = callSyncronRequest(extent);
    }
    return tmpExtent;
  }
    
  /**
  * Reprojects a point.  If spatial reference missing then its assumed the 
  * spatial reference is 4326.
  * 
  * @param point The point to be reprojected. If spatial reference missing t
  * then its assumed the spatial reference is 4326.
  * 
  * @return Reprojected Mappoint or null if cannot be reprojected
  * 
  * */
  public function reprojectPoint(point:MapPoint):MapPoint {
    LOG.info("Reprojecting a Map Point");
    if(point == null) {
        LOG.info("Mappoint argument is null");
        return null;
    }
    var rPoint:MapPoint = null;
    if(point.spatialReference == null || 
      this.isExtremeNumber(point.spatialReference.wkid)) {
        point.spatialReference = new SpatialReference(4326);
    }
    if(isEqualWkids(spatialReferenceTo.wkid, point.spatialReference.wkid)) {
        rPoint = point;
		rPoint.spatialReference = new SpatialReference(spatialReferenceTo.wkid);
    } else if (spatialReferenceTo.wkid == 102113 
        || spatialReferenceTo.wkid == 102100
        || spatialReferenceTo.wkid == 3857) {
        if(point.x > 179) {
            point.x = 179;
        }
        if(point.x < -179) {
            point.x = -179;
        }
        if(point.y > 85) {
            point.y = 85;
        }
        if(point.y < -85) {
            point.y = -85;
        }
        
        rPoint = WebMercatorUtil.geographicToWebMercator(point) as MapPoint;
    } else {
		rPoint = callSyncronRequestForPoint(point);		
    }
    if(rPoint == null || isExtremeNumber(rPoint.x) || isExtremeNumber(rPoint.y)) 
    {
        LOG.info("Could not reproject point x = " + point.x + ", y = " 
           + point.y + " to spatial reference " + spatialReferenceTo.wkid);
        return null;
    }
    return rPoint;
  }

  private function callSyncronRequestForPoint(point:MapPoint):MapPoint
  {                                	  	  
	  var params:String = "/project?f=json&inSR="+this.spatialReferenceTo.wkid+"&outSR="+point.spatialReference.wkid + "&geometries=" +escape("{\"geometryType\":\"esriGeometryPoint\", \"geometries\":[{\"x\":"+ point.x +",\"y\":"+ point.y +"}]}");
	  var url:String = geometryService + params;
	  if(FindDataWidget.URL_TO_PROXY != "") {
		  url =  FindDataWidget.URL_TO_PROXY + '?' + escape(url);
		  //url = url.replace("??", "?");	  
	  }
	  var ajax:Ajax = new Ajax(url);
	  ajax.requestType = AjaxRequestType.GET;
	  ajax.async = false;               
	  
	  var response:String = ajax.send();
	  var ro:Object = JSONUtil.decode(response);
	  var pt:MapPoint = new MapPoint(ro.geometries[0].x,ro.geometries[0].y);
	  return pt;
  }
  
  private function callSyncronRequest(extent:Extent):Extent
  {                                	  	  
	  var params:String = "/project?f=json&inSR="+this.spatialReferenceTo.wkid+"&outSR="+extent.spatialReference.wkid+"&geometries=" + escape("{\"geometryType\":\"esriGeometryEnvelope\", \"geometries\":[{\"xmin\":"+ extent.xmin +",\"ymin\":"+ extent.ymin + ",\"xmax\":"+ extent.xmax +",\"ymax\":"+ extent.ymax +"}]}");
	  var url:String = geometryService + params;
	  if(FindDataWidget.URL_TO_PROXY != "") {
	    url =  FindDataWidget.URL_TO_PROXY + '?' + escape(url);
		//url = url.replace("??", "?");
	  }
	  var ajax:Ajax = new Ajax(url);
	  ajax.requestType = AjaxRequestType.GET;
	  ajax.async = false;               
	  
	  var response:String = ajax.send();
	  
	  var ro:Object = JSONUtil.decode(response);
	  var ext:Extent = new Extent(ro.geometries[0].xmin,ro.geometries[0].ymin,ro.geometries[0].xmax,ro.geometries[0].ymax);
	  return ext;
  }
  
  /**
  * Checks if number is a border number 
  * (MAX_VALUE, MIN_VALUE, INFINITY, NAN)
  * 
  * **/
  private function isExtremeNumber(param:Number):Boolean {
    return param == Number.MAX_VALUE || param == Number.MIN_VALUE || 
      param == Number.NEGATIVE_INFINITY || param == Number.POSITIVE_INFINITY;
  }
  
    /**
    *  Checks for equality of ids.  Accounts for web mercator problems
    *
    *@param wkid1 Well known id of first geom
    *@param wkid2 Well known id of second geom
    *
    *@return if they are equal           
    */
    public static function isEqualWkids(wkid1:Number, wkid2:Number):Boolean {
        
        if(isNaN(wkid1) || isNaN(wkid2)) {
            return false;
        }
        if(wkid1 == wkid2) {
            return true;
        } else if(
             (wkid1 == 102100 || wkid1 == 3857 || wkid1 == 102113) &&
             (wkid2 == 102100 || wkid2 == 3857 || wkid2 == 102113)
          ) {
            return true
        } else if(
           (wkid1 == 84 || wkid2 == 84) && 
           (wkid1 == 4326 || wkid2 == 4326)
        ) {
            return true;
        }
        return false;
    }
} 
}