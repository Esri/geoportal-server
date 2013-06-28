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
package widgets.Samples.ThematicQuery
{

import com.esri.ags.geometry.Geometry;
import com.esri.ags.geometry.MapPoint;

import flash.events.EventDispatcher;

public class ThematicQueryResult extends EventDispatcher
{
    public var title:String;

    public var icon:String;

    public var content:String;

    public var attributes:Object;

    public var listFields:Array;

    public var columnChartFields:Array;

    public var point:MapPoint;
}

}
