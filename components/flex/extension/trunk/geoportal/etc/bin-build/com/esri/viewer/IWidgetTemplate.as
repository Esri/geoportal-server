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
package com.esri.viewer
{

public interface IWidgetTemplate
{
    function set baseWidget(value:IBaseWidget):void;

    /**
     * Set the widget state in the template implementation.
     *
     * @param value the state string
     */
    function set widgetState(value:String):void;
    function set draggable(value:Boolean):void;
    function set resizable(value:Boolean):void;
}

}
