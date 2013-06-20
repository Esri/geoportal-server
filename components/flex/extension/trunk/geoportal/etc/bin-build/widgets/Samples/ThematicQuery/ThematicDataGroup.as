////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2010 ESRI
//
// All rights reserved under the copyright laws of the United States.
// You may freely redistribute and use this software, with or
// without modification, provided you include the original copyright
// and use restrictions.  See use restrictions in the file:
// <install location>/FlexViewer/License.txt
//
////////////////////////////////////////////////////////////////////////////////
package widgets.Samples.ThematicQuery
{

import mx.core.ClassFactory;

import spark.components.DataGroup;

// This event bubble up from the ThematicItemRenderer
[Event(name="themeClick", type="flash.events.Event")]

public class ThematicDataGroup extends DataGroup
{
    public function ThematicDataGroup()
    {
        super();

        this.itemRenderer = new ClassFactory(ThematicItemRenderer);
    }
}

}
