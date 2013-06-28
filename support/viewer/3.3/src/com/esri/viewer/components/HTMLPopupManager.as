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

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;

import mx.core.IFlexDisplayObject;
import mx.managers.PopUpManager;

/**
 * Manager class used to control HTML pop-up component location and content.
 */
public class HTMLPopupManager
{
    public static function addPopUp(window:IFlexDisplayObject,
                                    parent:DisplayObject,
                                    modal:Boolean = false,
                                    childList:String = null):void
    {
        PopUpManager.addPopUp(window, parent, modal, childList);
        showSplash(window, parent as DisplayObjectContainer);
    }

    private static function showSplash(window:IFlexDisplayObject, parent:DisplayObjectContainer):void
    {
        var x:Number = (parent.width - window.width) / 2;
        var y:Number = (parent.height - window.height) / 2;

        window.move(Math.ceil(x), Math.ceil(y));
    }

    public static function removePopUp(window:IFlexDisplayObject):void
    {
        PopUpManager.removePopUp(window);
    }
}

}
