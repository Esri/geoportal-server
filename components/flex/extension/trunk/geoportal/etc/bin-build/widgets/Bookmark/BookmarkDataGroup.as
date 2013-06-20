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
package widgets.Bookmark
{

import mx.core.ClassFactory;

import spark.components.DataGroup;

// these events bubble up from the BookmarkItemRenderer
[Event(name="bookmarkClick", type="flash.events.Event")]
[Event(name="bookmarkDelete", type="flash.events.Event")]

public class BookmarkDataGroup extends DataGroup
{
    public function BookmarkDataGroup()
    {
        super();

        this.itemRenderer = new ClassFactory(BookmarkItemRenderer);
    }
}

}
