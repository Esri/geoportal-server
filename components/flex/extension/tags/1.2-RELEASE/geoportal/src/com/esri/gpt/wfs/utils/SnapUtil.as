/* Copyright 2008 ESRI
 *
 * All rights reserved under the copyright laws of the United States
 * and applicable international laws, treaties, and conventions.
 *
 * You may freely redistribute and use this sample code, with or
 * without modification, provided you include the original copyright
 * notice and use restrictions.
 * See use restrictions at http://resources.esri.com/help/9.3/usagerestrictions.htm.
 */
package com.esri.gpt.wfs.utils
{
	import com.esri.ags.Graphic;
	import com.esri.ags.geometry.Extent;
	import com.esri.ags.geometry.Geometry;
	import com.esri.ags.geometry.MapPoint;
	import com.esri.ags.geometry.Multipoint;
	import com.esri.ags.geometry.Polygon;
	import com.esri.ags.geometry.Polyline;
	
	import flash.geom.Point;

	/**
	 * This component provides snapping support for feature editing via a graphics layer.
	 * @see GraphicsLayer
	 */
	public class SnapUtil
	{		
		/** Constructor */
		public function SnapUtil()
		{
		}
		
		/**
		 * Determines if snapping is enabled.
		 * @param snapTo The snapTo mode.
		 * @return True if snapping is enabled, false otherwise.
		 */
		public static function snappingEnabled( snapTo :String ) :Boolean
		{
			switch( snapTo )
			{
				case SnapMode.END:
				case SnapMode.EDGE:
				case SnapMode.VERTEX:
					return true;
				case SnapMode.NONE:
				default:
					return false;
			}
		}
		
		/**
		 * Determines if the mouse is down and moving while editing and then attempts
		 * to determine if there is a snappable target within the tolerance.
		 * @param context The snapping context.
		 * @return If a snap point is found then it is returned, otherwise returns the input point.
		 */
		public static function findSnapPoint( context :SnapContext ) :MapPoint
		{	
			// perform a hitTestObject on each feature in the graphics layer
			// (if we can, limit the search for graphics within the envelope of the tolerance).
			var lastClosePoint :MapPoint = null;
			var closePoint :MapPoint = null;
			for each( var graphic :Graphic in context.graphicProvider )
			{
				if( graphic != context.selectedGraphic && graphic.hitTestObject( context.snapShape ) )
				{
					switch( graphic.geometry.type )
					{
						case Geometry.EXTENT:
							closePoint = findNearestInExtent( context.snapMode, context.origin, graphic.geometry as Extent );
							break;
						case Geometry.MULTIPOINT:
							closePoint = findNearestInMultipoint( context.origin, graphic.geometry as Multipoint );
							break;
						case Geometry.MAPPOINT:
							closePoint = MapPoint( graphic.geometry );
							break;
						case Geometry.POLYGON:
							closePoint = findNearestInPolygon( context.snapMode, context.origin, graphic.geometry as Polygon );
							break;
						case Geometry.POLYLINE:
							closePoint = findNearestInPolyline( context.snapMode, context.origin, graphic.geometry as Polyline );
							break;
					}
				}
				
				// save the last closest point
				if( lastClosePoint == null )
				{
					lastClosePoint = closePoint;
				}
				else
				{
					lastClosePoint = findCloserPoint( context.origin, lastClosePoint, closePoint );
				}
			}
			
			// if a close point was found return it, otherwise return the input point
			if( lastClosePoint )
			{
				// calculate the final distance to ensure it is within the snap radius
				var originScreen :Point = context.map.toScreen( context.origin );
				var currentScreen :Point = context.map.toScreen( lastClosePoint );
				var dx :Number = Math.max( originScreen.x, currentScreen.x ) - Math.min( originScreen.x, currentScreen.x );
				var dy :Number = Math.max( originScreen.y, currentScreen.y ) - Math.min( originScreen.y, currentScreen.y );
				var distance :Number = Math.pow( dx, 2 ) + Math.pow( dy, 2 );
				if( distance <= Math.pow( context.snapTolerance, 2 ) )
				{
					return lastClosePoint;
				}
				else
				{
					return context.origin;
				}
			}
			return context.origin;
		}
		
		/**
		 * @private
		 * Finds the closest point in an extent to the origin.
		 * @param origin The origin point.
		 * @param extent The extent.
		 * @return The closest point from the extent to the origin.
		 */ 
		private static function findNearestInExtent( snapTo :String, origin :MapPoint, extent :Extent ) :MapPoint
		{
			switch( snapTo )
			{
				case SnapMode.END:
				case SnapMode.VERTEX:
					return findNearestVertexInExtent( origin, extent );
				case SnapMode.EDGE:
					return findNearestEdgeInExtent( origin, extent );
				default:
					return origin;
			}
		}
		
		/**
		 * @private
		 * Finds the closest edge point in the extent with respect to the origin.
		 * @param origin The origin point.
		 * @param extent The extent.
		 * @return The closest edge point from the extent to the origin.
		 */
		private static function findNearestEdgeInExtent( origin :MapPoint, extent :Extent ) :MapPoint
		{
			// create the vertexes for each line segment in the extent
			var lowerLeft :MapPoint = new MapPoint( extent.xmin, extent.ymin, origin.spatialReference );
			var upperLeft :MapPoint = new MapPoint( extent.xmin, extent.ymax, origin.spatialReference );
			var upperRight :MapPoint = new MapPoint( extent.xmax, extent.ymax, origin.spatialReference );
			var lowerRight :MapPoint = new MapPoint( extent.xmax, extent.ymin, origin.spatialReference );
			
			var closestPoint :MapPoint = findClosestEdgePoint( origin, lowerLeft, upperLeft ); // left side
			var closePoint :MapPoint = findClosestEdgePoint( origin, upperLeft, upperRight );  // top side
			closestPoint = findCloserPoint( origin, closestPoint, closePoint );
			closePoint = findClosestEdgePoint( origin, upperRight, lowerRight );               // right side
			closestPoint = findCloserPoint( origin, closestPoint, closePoint );
			closePoint = findClosestEdgePoint( origin, lowerRight, lowerLeft );                // bottom side
			closestPoint = findCloserPoint( origin, closestPoint, closePoint );
			return closestPoint;
		}
		
		/**
		 * @private
		 * Finds the closest vertex in an extent to the origin.
		 * @param origin The origin point.
		 * @param extent The extent.
		 * @return The closest point from the extent to the origin.
		 */
		private static function findNearestVertexInExtent( origin :MapPoint, extent :Extent ) :MapPoint
		{
			var closestPoint :MapPoint = new MapPoint( extent.xmin, extent.ymin, extent.spatialReference );
			closestPoint = findCloserPoint( origin, closestPoint, new MapPoint( extent.xmin, extent.ymax, extent.spatialReference ) );
			closestPoint = findCloserPoint( origin, closestPoint, new MapPoint( extent.xmax, extent.ymax, extent.spatialReference ) );
			closestPoint = findCloserPoint( origin, closestPoint, new MapPoint( extent.xmax, extent.ymin, extent.spatialReference ) );
			return closestPoint;
		}
		
		/**
		 * @private
		 * Finds the closest point in a multipoint to the origin.
		 * @param origin The origin point.
		 * @param multipoint The multipoint geometry.
		 * @return The closest point from the multipoint to the origin.
		 */
		private static function findNearestInMultipoint( origin :MapPoint, multipoint :Multipoint ) :MapPoint
		{
			return findClosestPoint( origin, multipoint.points );
		}
		
		/**
		 * @private
		 * Finds the closest point in a polygon to the origin.
		 * @param snapTo The snap to directive.
		 * @param origin The origin point.
		 * @param polygon The polygon.
		 * @return The closest point from the polygon to the origin.
		 */
		private static function findNearestInPolygon( snapTo :String, origin :MapPoint, polygon :Polygon ) :MapPoint
		{
			switch( snapTo )
			{
				case SnapMode.END:
				case SnapMode.VERTEX:
					return findNearestVertexInPolygon( origin, polygon );
				case SnapMode.EDGE:
					return findNearestEdgeInPolygon( origin, polygon );
				default:
					return origin;
			}
		}
		
		/**
		 * @private
		 * Finds the closest edge point in a polygon with respect to the origin.
		 * @param origin The origin point.
		 * @param polygon The polygon.
		 * @return The closest edge point from the polygon to the origin.
		 */
		private static function findNearestEdgeInPolygon( origin :MapPoint, polygon :Polygon ) :MapPoint
		{
			var closestPoint :MapPoint;
			var closePoint :MapPoint;
			for each( var ring :Array in polygon.rings )
			{
				for( var i :int = 0; i < ring.length; ++i )
				{
					if( i == ring.length - 1 )
					{
						closePoint = findClosestEdgePoint(origin, ring[i], ring[0] );
					}
					else
					{
						closePoint = findClosestEdgePoint( origin, ring[i], ring[i + 1] );
					}
					
					// determine if this is a closer point than the current
					if( closestPoint == null )
					{
						closestPoint = closePoint;
					}
					else
					{
						closestPoint = findCloserPoint( origin, closestPoint, closePoint );
					}
				}
			}
			
			if( closestPoint )
			{
				return closestPoint;
			}
			return origin;
		}
		
		/**
		 * @private
		 * Finds the closest point in a polygon to the origin.
		 * @param origin The origin point.
		 * @param polygon The polygon.
		 * @return The closest point from the polygon to the origin.
		 */
		private static function findNearestVertexInPolygon( origin :MapPoint, polygon :Polygon ) :MapPoint
		{
			var closestPoint :MapPoint;
			for each( var ring :Array in polygon.rings )
			{
				closestPoint = findClosestPoint( origin, ring );
			}
			return closestPoint;
		}
		
		/**
		 * @private
		 * Finds the closest point in a polyline to the origin.
		 * @param origin The origin point.
		 * @param polyline The polyline.
		 * @return The closest point from the polyline to the origin.
		 */
		private static function findNearestInPolyline( snapTo :String, origin :MapPoint, polyline :Polyline ) :MapPoint
		{
			switch( snapTo )
			{
				case SnapMode.END:
					return findNearestEndpointInPolyline( origin, polyline );
				case SnapMode.VERTEX:
					return findNearestVertexInPolyline( origin, polyline );
				case SnapMode.EDGE:
					return findNearestEdgeInPolyline( origin, polyline );
				default:
					return origin;
			}
		}
		
		/**
		 * @private
		 * Finds the closest end point in a polyline with respect to the origin.
		 * @param origin The origin point.
		 * @param polyline The polyline.
		 * @return The closest end point from the polyline to the origin.
		 */
		private static function findNearestEndpointInPolyline( origin :MapPoint, polyline :Polyline ) :MapPoint
		{
			var paths :int = polyline.paths.length;
			var startPoint :MapPoint = polyline.paths[0][0];
			var endPoint :MapPoint = polyline.paths[paths - 1][polyline.paths[paths - 1].length - 1];
			return findCloserPoint( origin, startPoint, endPoint );
		}
		
		/**
		 * @private
		 * Finds the closest edge point in a polyline with respect to the origin.
		 * @param origin The origin point.
		 * @param polyline The polyline.
		 * @return The closest edge point from the polyline to the origin.
		 */
		private static function findNearestEdgeInPolyline( origin :MapPoint, polyline :Polyline ) :MapPoint
		{
			var closestPoint :MapPoint;
			var closePoint :MapPoint;
			for each( var path :Array in polyline.paths )
			{
				for( var i :int = 0; i < path.length - 1; ++i )
				{
					closePoint = findClosestEdgePoint( origin, path[i], path[i + 1] );
					
					// determine if this is a closer point than the current
					if( closestPoint == null )
					{
						closestPoint = closePoint;
					}
					else
					{
						closestPoint = findCloserPoint( origin, closestPoint, closePoint );
					}
				}
			}
			
			if( closestPoint )
			{
				return closestPoint;
			}
			return origin;
		}
		
		/**
		 * @private
		 * Finds the closest point in a polyline to the origin.
		 * @param origin The origin point.
		 * @param polyline The polyline.
		 * @return The closest point from the polyline to the origin.
		 */
		private static function findNearestVertexInPolyline( origin :MapPoint, polyline :Polyline ) :MapPoint
		{
			var closestPoint :MapPoint;
			for each( var path :Array in polyline.paths )
			{
				closestPoint = findClosestPoint( origin, path );
			}
			return closestPoint;
		}
		
		/**
		 * @private
		 * Find the point along the line between the two points that is nearest the origin.
		 * @param origin The origin point.
		 * @param point1 The first line point.
		 * @param point2 The second line point.
		 * @return The closest point to the origin along the line between the two given points.
		 */
		 private static function findClosestEdgePoint( origin :MapPoint, point1 :MapPoint, point2 :MapPoint ) :MapPoint
		 {
			var closePoint :MapPoint;
			var dx :Number = point2.x - point1.x;
			var dy :Number = point2.y - point1.y;
					
			// vertical
			if( dx == 0 )
			{
				closePoint = new MapPoint( point1.x, origin.y, origin.spatialReference );
			}
			// horizontal
			else if( dy == 0 )
			{
				closePoint = new MapPoint( origin.x, point1.y, origin.spatialReference );
			}
			else
			{
				if( dx == 0 || dy == 0 )
				{
					closePoint = findCloserPoint( origin, point1, point2 );
				}
				else
				{
					// slope: m = dy / dx
					// y-intercept: b = y - mx
					// solve for x: x = (b2 - b1) / (m2 - m1)
					
					// calculate slope and y-intercept of first line
					var slope1 :Number = dy / dx;					
					var intercept1 :Number = point1.y - ( slope1 * point1.x );
					
					// calculate slope (inverse slope of the first line) and y-intercept of the second line
					var slope2 :Number = -1 / slope1;
					var intercept2 :Number = origin.y - ( slope2 * origin.x );
					
					// check the denominator before solving for x
					var denominator :Number = slope1 - slope2;

					var x :Number = ( intercept2 - intercept1 ) / denominator;
					var y :Number = slope2 * x + intercept2;
					closePoint = new MapPoint( x, y, origin.spatialReference );  
				}
			}
			
			// check to make sure the point exists in the line segment
			if( ( closePoint.x < point1.x && closePoint.x < point2.x ) ||
				( closePoint.x > point1.x && closePoint.x > point2.x ) ||
				( closePoint.y < point1.y && closePoint.y < point2.y ) ||
				( closePoint.y > point1.y && closePoint.y > point2.y ) )
			{
				closePoint = findCloserPoint( origin, point1, point2 );
		    }
		    return closePoint;
		 }  
		
		/**
		 * @private
		 * Find the closest point to the origin from the given array of points.
		 * @param origin The origin point.
		 * @param points The array of points.
		 * @return The closest point to the origin from the given array of points.
		 */
		private static function findClosestPoint( origin :MapPoint, points :Array ) :MapPoint
		{
			var closestPoint :MapPoint = null;
			for each( var point :MapPoint in points )
			{
				if( closestPoint == null )
				{
					closestPoint = point;
				}
				else
				{
					closestPoint = findCloserPoint( origin, closestPoint, point );
				}
			}
			return closestPoint;
		}
		
		/**
		 * @protected
		 * Finds the closer of the two points to the origin point.
		 * @param origin The origin point.
		 * @param point1 The first comparison point.
		 * @param point2 The second comparison point.
		 * @return The closest point to the origin.
		 */
		public static function findCloserPoint( origin :MapPoint, point1 :MapPoint, point2 :MapPoint ) :MapPoint
		{
			var dx1 :Number = origin.x - point1.x;
			var dy1 :Number = origin.y - point1.y;
			var dx2 :Number = origin.x - point2.x;
			var dy2 :Number = origin.y - point2.y;
			
			// closer point found
			if( ( dx2 * dx2 + dy2 * dy2 ) < ( dx1 * dx1 + dy1 * dy1 ) )
			{
				return point2;
			}
			return point1;
		}
	}
}