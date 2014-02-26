///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2010-2012 Esri. All Rights Reserved.
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

import mx.managers.IFocusManagerComponent;

import spark.components.Image;

public class FocusableImage extends Image implements IFocusManagerComponent
{
    public function FocusableImage()
    {
        super();
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
            dispatchEvent(new MouseEvent(MouseEvent.CLICK));
        }
        event.updateAfterEvent();
    }
}
}
