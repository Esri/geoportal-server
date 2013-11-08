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
package com.esri.viewer.components
{

import flash.events.KeyboardEvent;
import flash.events.MouseEvent;
import flash.ui.Keyboard;

import mx.controls.Image;
import mx.managers.IFocusManagerComponent;

import spark.components.supportClasses.SkinnableComponent;

[SkinState("normal")]
[SkinState("selected")]

/**
 * TitlebarButton...
 *
 * @private
 */
public class TitlebarButton extends SkinnableComponent implements IFocusManagerComponent
{

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //----------------------------------
    //  icon
    //----------------------------------

    [SkinPart(required="false")]
    public var icon:Image;

    //----------------------------------
    //  source
    //----------------------------------

    [Bindable]
    public var source:Object;

    //----------------------------------
    //  callback
    //----------------------------------

    public var callback:Function;

    //----------------------------------
    //  selectable
    //----------------------------------

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


    //--------------------------------------------------------------------------
    //
    //  Skin Management
    //
    //--------------------------------------------------------------------------

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

    //--------------------------------------------------------------------------
    //
    //  Focus Management
    //
    //--------------------------------------------------------------------------

    private var keyboardPressed:Boolean = false;

    override protected function keyDownHandler(event:KeyboardEvent):void
    {
        if (event.keyCode != Keyboard.SPACE)
        {
            return;
        }

        keyboardPressed = true;
        event.updateAfterEvent();
    }

    override protected function keyUpHandler(event:KeyboardEvent):void
    {
        if (event.keyCode != Keyboard.SPACE)
        {
            return;
        }

        if (enabled && keyboardPressed)
        {
            // Mimic mouse click on the button.
            keyboardPressed = false;
            if (icon)
            {
                icon.dispatchEvent(new MouseEvent(MouseEvent.CLICK, true));
            }
        }
        event.updateAfterEvent();
    }

    //--------------------------------------------------------------------------
    //
    //  Event listeners
    //
    //--------------------------------------------------------------------------

    private function icon_clickHandler(event:MouseEvent):void
    {
        selected = true;
        callback();
    }
}

}
