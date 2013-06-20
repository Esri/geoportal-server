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

// these events bubble up from the WidgetItemRenderer and GroupWidgetItemRenderer
[Event(name="widgetItemClick", type="flash.events.Event")]
[Event(name="widgetItemMouseOver", type="flash.events.Event")]
[Event(name="widgetItemMouseOut", type="flash.events.Event")]

public class WidgetItemDataGroup extends DataGroup
{
    public function WidgetItemDataGroup()
    {
        super();

        this.itemRendererFunction = rendererFunction;
    }

    private function rendererFunction(item:Object):ClassFactory
    {
        if (item.isGroup)
        {
            return new ClassFactory(GroupWidgetItemDataGroupRenderer);
        }
        else
        {
            return new ClassFactory(WidgetItemDataGroupRenderer);
        }
    }
}

}
