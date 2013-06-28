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
package widgets.Navigation
{

[Bindable]
public class NavToolItem
{
    public function NavToolItem(
        name:String = "",
        icon:String = "",
        visible:Boolean = false
        )
    {
        this.toolName = name;
        this.toolIcon = icon;
        this.toolVisible = visible;
    }

    public var toolName:String;
    public var toolIcon:String;
    public var toolVisible:Boolean;
}

}
