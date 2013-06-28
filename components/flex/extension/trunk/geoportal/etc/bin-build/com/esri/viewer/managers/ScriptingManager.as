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
package com.esri.viewer.managers
{

import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;

public class ScriptingManager extends EventDispatcher
{
    public function ScriptingManager(target:IEventDispatcher = null)
    {
        super(target);
    }

    public function setWidgetState(id:Number, state:String):void
    {

    }
}

}
