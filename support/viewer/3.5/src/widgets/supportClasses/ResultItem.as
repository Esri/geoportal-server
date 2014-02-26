///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2011 Esri. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////////
package widgets.supportClasses
{

import com.esri.ags.Graphic;
import com.esri.ags.geometry.Geometry;
import com.esri.ags.geometry.MapPoint;
import com.esri.ags.geometry.Multipoint;
import com.esri.ags.geometry.Polygon;
import com.esri.ags.geometry.Polyline;
import com.esri.ags.symbols.Symbol;

public class ResultItem
{
    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    public function ResultItem(graphic:Graphic, attributes:ResultAttributes)
    {
        _graphic = graphic;
        _attributes = attributes;
        _center = getGeomCenter(graphic);
    }

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //  graphic
    //--------------------------------------------------------------------------

    private var _graphic:Graphic;

    public function get graphic():Graphic
    {
        return _graphic;
    }

    //--------------------------------------------------------------------------
    //  attributes
    //--------------------------------------------------------------------------

    private var _attributes:ResultAttributes;

    public function get attributes():ResultAttributes
    {
        return _attributes;
    }

    //--------------------------------------------------------------------------
    //  center
    //--------------------------------------------------------------------------

    private var _center:MapPoint;

    public function get center():MapPoint
    {
        return _center;
    }

    //--------------------------------------------------------------------------
    //  geometry
    //--------------------------------------------------------------------------

    public function get geometry():Geometry
    {
        return _graphic.geometry;
    }

    //--------------------------------------------------------------------------
    //  symbol
    //--------------------------------------------------------------------------

    public function get symbol():Symbol
    {
        return _graphic.symbol;
    }

    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------

    private function getGeomCenter(graphic:Graphic):MapPoint
    {
        var point:MapPoint;
        var geometry:Geometry = graphic.geometry;

        if (geometry)
        {
            switch (geometry.type)
            {
                case Geometry.MAPPOINT:
                {
                    point = geometry as MapPoint;
                    break;
                }
                case Geometry.MULTIPOINT:
                {
                    const multipoint:Multipoint = geometry as Multipoint;
                    point = multipoint.points && multipoint.points.length > 0 ? multipoint.points[0] as MapPoint : null;
                    break;
                }
                case Geometry.POLYLINE:
                {
                    var pl:Polyline = geometry as Polyline;
                    var pathCount:Number = pl.paths.length;
                    var pathIndex:int = int((pathCount / 2) - 1);
                    var midPath:Array = pl.paths[pathIndex];
                    var ptCount:Number = midPath.length;
                    var ptIndex:int = int((ptCount / 2) - 1);
                    point = pl.getPoint(pathIndex, ptIndex);
                    break;
                }
                case Geometry.POLYGON:
                {
                    const poly:Polygon = geometry as Polygon;
                    point = poly.extent.center;
                    break;
                }
            }
        }

        return point;
    }
}

}
