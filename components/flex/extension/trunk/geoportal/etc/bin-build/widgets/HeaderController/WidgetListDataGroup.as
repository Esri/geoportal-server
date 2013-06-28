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
package widgets.HeaderController
{

import mx.core.ClassFactory;

import spark.components.DataGroup;

// these events bubble up from the WidgetListItemRenderer
[Event(name="widgetListItemClick", type="flash.events.Event")]

public class WidgetListDataGroup extends DataGroup
{
    public function WidgetListDataGroup()
    {
        super();

        this.itemRenderer = new ClassFactory(WidgetListItemRenderer);
    }
}

}
