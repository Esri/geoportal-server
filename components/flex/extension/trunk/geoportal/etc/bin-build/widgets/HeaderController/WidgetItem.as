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

import flash.events.EventDispatcher;

[Bindable]
[RemoteClass(alias="widgets.HeaderController.WidgetItem")]

public class WidgetItem extends EventDispatcher
{
    public var id:Number; // id of the associated widget
    public var isGroup:Boolean;
    public var icon:Object;
    public var url:String; // url 
    public var label:String;
    public var open:Boolean; // indicates whether the associated widget is open or closed
    public var widgets:Array;
}

}
