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
package widgets.GeoRSS
{

import mx.core.ClassFactory;

import spark.components.DataGroup;

// these events bubble up from the GeoRSSFeedItemRenderer
[Event(name="geoRSSFeedClick", type="flash.events.Event")]
[Event(name="geoRSSFeedMouseOver", type="flash.events.Event")]
[Event(name="geoRSSFeedMouseOut", type="flash.events.Event")]

public class GeoRSSFeedDataGroup extends DataGroup
{
    public function GeoRSSFeedDataGroup()
    {
        super();

        this.itemRenderer = new ClassFactory(GeoRSSFeedItemRenderer);
    }
}

}
