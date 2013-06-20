////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2010 ESRI
//
// All rights reserved under the copyright laws of the United States.
// You may freely redistribute and use this software, with or
// without modification, provided you include the original copyright
// and use restrictions.  See use restrictions in the file:
// <install location>/License.txt
//
////////////////////////////////////////////////////////////////////////////////
package com.esri.viewer.utils
{

import com.esri.ags.geometry.Geometry;
import com.esri.ags.geometry.MapPoint;
import com.esri.ags.geometry.Polygon;
import com.esri.ags.geometry.Polyline;

import mx.utils.StringUtil;

public class GeoRSSUtil
{
    private static const GEORSS:Namespace = Namespaces.GEORSS_NS;
    private static const GEO:Namespace = Namespaces.GEO_NS;
    private static const GML:Namespace = Namespaces.GML_NS;
    private static const GEOLL:Namespace = Namespaces.GEO_LL;

    public function GeoRSSUtil(singletonEnforcer:SingletonEnforcer)
    {
    }

    public static function toGeometry(x:XML):Geometry
    {
        var georssPoint:String = String(x.GEORSS::point);
        if (georssPoint)
        {
            return parseGeoRSSPoint(georssPoint);
        }
        var georssLine:String = String(x.GEORSS::line);
        if (georssLine)
        {
            return parseGeoRSSLine(georssLine);
        }
        var georssPolygon:String = String(x.GEORSS::polygon);
        if (georssPolygon)
        {
            return parseGeoRSSPolygon(georssPolygon);
        }
        var pointList:XMLList = x.GEO::point;
        if (pointList && pointList.length() > 0)
        {
            var geoPoint:XML = pointList[0];
            var getLat:Number = Number(geoPoint.GEO::lat);
            var geoLon:Number = Number(geoPoint.GEO::long);
            return new MapPoint(geoLon, getLat);
        }
        var whereList:XMLList = x.GEORSS::where;
        if (whereList && whereList.length() > 0)
        {
            var pos:String = whereList[0].GML::Point[0].GML::pos[0];
            var arr:Array = pos.split(" ");
            var gmlLat:Number = Number(arr[0]);
            var gmlLon:Number = Number(arr[1]);
            return new MapPoint(gmlLon, gmlLat);
        }
        var georssLat:String = String(x.GEOLL::lat);
        var georssLong:Number = Number(x.GEOLL::long);
        if ((georssLong) && (georssLat))
        {
            return new MapPoint(Number(georssLong), Number(georssLat));
        }
        return null;
    }

    private static function parseGeoRSSWhere(x:XML):Geometry
    {
        return null;
    }

    private static function parseGeoRSSPoint(text:String):Geometry
    {
        var tokens:Array = StringUtil.trim(text).split(" ");
        var lat:Number = Number(tokens[0]);
        var lon:Number = Number(tokens[1]);
        return new MapPoint(lon, lat);
    }

    private static function parseGeoRSSLine(text:String):Geometry
    {
        var path:Array = [];
        var tokens:Array = StringUtil.trim(text).split(" ");
        if (tokens.length > 3)
        {
            for (var i:int = 0, j:int = 1; j < tokens.length; i += 2, j += 2)
            {
                var lat:Number = Number(tokens[i]);
                var lon:Number = Number(tokens[j]);
                path.push(new MapPoint(lon, lat));
            }
        }
        return new Polyline([ path ]);
    }

    private static function parseGeoRSSPolygon(text:String):Geometry
    {
        var path:Array = [];
        var tokens:Array = StringUtil.trim(text).split(" ");
        for (var i:int = 0, j:int = 1; j < tokens.length; i += 2, j += 2)
        {
            var lat:Number = Number(tokens[i]);
            var lon:Number = Number(tokens[j]);
            path.push(new MapPoint(lon, lat));
        }
        return new Polygon([ path ]);
    }
}

}

class SingletonEnforcer
{
}
