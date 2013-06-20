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
package com.esri.viewer.components
{

import flash.events.MouseEvent;

import mx.controls.Image;

import spark.components.supportClasses.SkinnableComponent;

[SkinState("normal")]
[SkinState("selected")]

public class TitlebarButton extends SkinnableComponent
{
    [SkinPart(required="false")]
    public var icon:Image;

    [Bindable]
    public var source:Object;

    public var callback:Function;

    public var selectable:Boolean = true;

    //----------------------------------
    //  selected
    //----------------------------------

    private var _selected:Boolean = false;

    public function get selected():Boolean
    {
        return _selected;
    }

    public function set selected(value:Boolean):void
    {
        if (selectable && _selected != value)
        {
            _selected = value;
            invalidateSkinState();
        }
    }

    override protected function getCurrentSkinState():String
    {
        return selected ? "selected" : "normal";
    }

    override protected function partAdded(partName:String, instance:Object):void
    {
        super.partAdded(partName, instance);

        if (instance == icon)
        {
            icon.addEventListener(MouseEvent.CLICK, icon_clickHandler);
        }
    }

    override protected function partRemoved(partName:String, instance:Object):void
    {
        super.partRemoved(partName, instance);

        if (instance == icon)
        {
            icon.removeEventListener(MouseEvent.CLICK, icon_clickHandler);
        }
    }

    private function icon_clickHandler(event:MouseEvent):void
    {
        selected = true;
        callback();
    }
}

}
