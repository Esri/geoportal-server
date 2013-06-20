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
package com.esri.viewer
{

/**
 * ConfigData class is used to store configuration information from the config.xml file.
 */
public class ConfigData
{
    public var configXML:XML; // Reference to the main config.xml
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
    public var subTitleFont:Object;
    public var geometryService:Object;
    public var bingKey:String;
    public var proxyUrl:String;
    public var layoutDirection:String;
    public var webMapData:Object;

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
