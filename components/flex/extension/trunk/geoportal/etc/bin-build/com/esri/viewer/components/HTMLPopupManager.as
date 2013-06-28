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

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;

import mx.core.IFlexDisplayObject;
import mx.managers.PopUpManager;

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
        var x:Number;
        var y:Number;
        x = (parent.width - window.width) / 2;
        y = (parent.height - window.height) / 2;

        window.move(Math.ceil(x), Math.ceil(y));
        //tweenPosition(window, window.height);
    }

    public static function removePopUp(window:IFlexDisplayObject):void
    {
        PopUpManager.removePopUp(window);
    }
}

}
