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

import com.esri.ags.Map;

public interface IInfowindowTemplate
{
    function set data(value:Object):void;
    function get data():Object;
    function set infoClass(value:Class):void;
    function get infoClass():Class;
    function set infoConfig(value:XML):void;
}

}
