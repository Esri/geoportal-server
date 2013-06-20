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
package widgets.Bookmark
{

import flash.events.EventDispatcher;

[Bindable]
[RemoteClass(alias="widgets.Bookmark.Bookmark")]

public class Bookmark extends EventDispatcher
{
    public var name:String;

    public var icon:String;

    public var userCreated:Boolean;

    public var xmin:Number;
    public var ymin:Number;
    public var xmax:Number;
    public var ymax:Number;
}

}
