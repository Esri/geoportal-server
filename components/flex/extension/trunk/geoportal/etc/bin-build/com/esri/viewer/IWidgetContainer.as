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

public interface IWidgetContainer
{
    function addWidget(value:IBaseWidget):void;
    function focusWidget(id:Number):void;
    function set configParams(value:Object):void;
}

}
