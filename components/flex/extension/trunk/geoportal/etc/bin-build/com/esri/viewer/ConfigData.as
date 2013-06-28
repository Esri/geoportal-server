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

/**
 * ConfigData class is used to store configuration information from the config.xml file.
 */
public class ConfigData
{
    public var viewerUI:Array;
    public var controls:Array;
    public var mapAttrs:Array;
    public var basemaps:Array;
    public var opLayers:Array;
    public var widgetContainers:Array;
    public var widgets:Array;
    public var widgetIndex:Array;
    public var styleAlpha:Number;
    public var styleColors:Array;
    public var font:Object;
    public var titleFont:Object;
    public var geometryService:Object;
    public var bingKey:String;
    public var proxyUrl:String;

    public function ConfigData()
    {
        viewerUI = [];
        controls = [];
        mapAttrs = [];
        basemaps = [];
        opLayers = [];
        widgets = [];
        widgetContainers = []; //[i]={container, widgets]
        widgetIndex = []; //[i]={container, inx}
        styleAlpha = 0.8;
        styleColors = [];
        geometryService = {}; // { url: "foo", token: "123", useproxy: false }
    }
}

}
