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

import com.esri.ags.events.WebMapEvent;
import com.esri.ags.geometry.Extent;
import com.esri.ags.layers.ArcGISDynamicMapServiceLayer;
import com.esri.ags.layers.ArcGISImageServiceLayer;
import com.esri.ags.layers.ArcGISTiledMapServiceLayer;
import com.esri.ags.layers.FeatureLayer;
import com.esri.ags.layers.Layer;
import com.esri.ags.layers.supportClasses.LOD;
import com.esri.ags.tasks.GeometryService;
import com.esri.ags.virtualearth.VETiledLayer;
import com.esri.ags.webmap.WebMapUtil;
import com.esri.viewer.AppEvent;
import com.esri.viewer.ConfigData;
import com.esri.viewer.ViewerContainer;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;

import mx.collections.ArrayCollection;
import mx.resources.ResourceManager;
import mx.rpc.Fault;
import mx.rpc.Responder;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.http.HTTPService;
import mx.utils.StringUtil;

[Event(name="configLoaded", type="com.esri.viewer.AppEvent")]

/**
 * ConfigManager is used to parse the configuration file and store the information in ConfigData.
 * The default configuration file is config.xml, but you can specify an alternative configuration file,
 * e.g. http://myserver/flexwebmaps/index.html?config=config2.xml
 *
 * The name of the default configuration file is specified in ViewerContainer.mxml.
 */
public class ConfigManager extends EventDispatcher
{
    public function ConfigManager()
    {
        super();

        //make sure the container is properly initialized and then
        //proceed with configuration initialization.
        ViewerContainer.addEventListener(ViewerContainer.CONTAINER_INITIALIZED, init);
    }

    //init - start loading the configuration file and parse.
    public function init(event:Event):void
    {
        configLoad();
    }

    //config load
    private function configLoad():void
    {
        var configService:HTTPService = new HTTPService();
        configService.url = ViewerContainer.configFile;
        configService.resultFormat = HTTPService.RESULT_FORMAT_E4X;

        configService.addEventListener(ResultEvent.RESULT, configResult);
        configService.addEventListener(FaultEvent.FAULT, configFault);
        configService.send();
    }

    //config fault
    private function configFault(event:mx.rpc.events.FaultEvent):void
    {
        // happens if for example the main config file is missing or have crossdomain problem

        var sInfo:String = "";

        // Missing config file
        if (event.fault.rootCause is IOErrorEvent)
        {
            var ioe:IOErrorEvent = event.fault.rootCause as IOErrorEvent;
            if (ioe.text.indexOf("2032: Stream Error. URL:") > -1)
            {
                sInfo += StringUtil.substitute(getDefaultString('missingConfigFile'), ioe.text.substring(32)) + "\n\n";
            }
            else
            {
                // some other IOError
                sInfo += event.fault.rootCause + "\n\n";
            }
        }

        // config file with crossdomain issue
        if (event.fault.rootCause is SecurityErrorEvent)
        {
            var sec:SecurityErrorEvent = event.fault.rootCause as SecurityErrorEvent;
            if (sec.text.indexOf("Error #2048: ") > -1)
            {
                sInfo += StringUtil.substitute(getDefaultString('configFileCrossDomain'), "\n", sec.text) + "\n\n";
            }
            else
            {
                // some other Security error
                sInfo += event.fault.rootCause + "\n\n";
            }
        }

        if (event.statusCode) // e.g. 404 - Not Found - http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
        {
            sInfo += StringUtil.substitute(getDefaultString('httpResponseStatus'), event.statusCode) + "\n\n";
        }

        sInfo += StringUtil.substitute(getDefaultString('faultCode'), event.fault.faultCode) + "\n\n";
        sInfo += StringUtil.substitute(getDefaultString('faultInfo'), event.fault.faultString) + "\n\n";
        sInfo += StringUtil.substitute(getDefaultString('faultDetail'), event.fault.faultDetail);

        ViewerContainer.showError(sInfo);
    }

    //config result
    private function configResult(event:ResultEvent):void
    {
        try
        {
            //parse main configuration file to create config data object
            var configData:ConfigData = new ConfigData();
            var configXML:XML = event.result as XML;
            var i:int;
            var j:int;

            //================================================
            //Proxy configuration
            //================================================
            var proxyUrl:String = configXML..httpproxy;
            if (proxyUrl)
            {
                configData.proxyUrl = proxyUrl;
            }

            //================================================
            //BingKey configuration
            //================================================
            var bingKey:String = configXML.bing.@key;
            if (bingKey)
            {
                configData.bingKey = bingKey;
            }

            //================================================
            //GeometryService configuration
            //================================================
            var geometryService:XML = configXML.geometryservice[0];
            if (geometryService)
            {
                configData.geometryService.url = geometryService.@url[0] ? String(geometryService.@url[0]) : "";
                configData.geometryService.token = geometryService.@token[0] ? String(geometryService.@token[0]) : "";
                configData.geometryService.useproxy = geometryService.@useproxy[0] == "true";
            }

            //================================================
            //Style configuration
            //================================================
            var styleAlpha:String = (XMLList(configXML.style.alpha).length() > 0) ? configXML.style.alpha : configXML.stylealpha;
            if (styleAlpha)
            {
                configData.styleAlpha = Number(styleAlpha);
            }

            var styleColors:Array = String(configXML.style.colors).split(",");
            if (styleColors.length == 1) // if style.colors are not specified, then check for stylecolors for backwards compatibility with version 2.1
            {
                styleColors = String(configXML.stylecolors).split(",");
            }

            var colorStr:String = "";
            for each (colorStr in styleColors)
            {
                configData.styleColors.push(uint(colorStr));
            }

            var styleFontName:String = configXML.style.font.@name;
            var styleFontSize:String = configXML.style.font.@size;
            var font:Object =
                {
                    id: "font",
                    name: styleFontName,
                    size: int(styleFontSize)
                }
            configData.font = font;

            var styleTitleFontName:String = configXML.style.titlefont.@name;
            var styleTitleFontSize:String = configXML.style.titlefont.@size;
            var titleFont:Object =
                {
                    id: "titleFont",
                    name: styleTitleFontName,
                    size: int(styleTitleFontSize)
                }
            configData.titleFont = titleFont;

            //================================================
            //user interface
            //================================================
            var configUI:Array = [];
            var value:String = configXML..title;
            var title:Object =
                {
                    id: "title",
                    value: value
                };
            configUI.push(title);

            value = configXML..subtitle;
            var subtitle:Object =
                {
                    id: "subtitle",
                    value: value
                };
            configUI.push(subtitle);

            value = configXML..logo;
            var logo:Object =
                {
                    id: "logo",
                    value: value
                }
            configUI.push(logo);

            value = configXML..widgetlayout;
            var widgetlayout:Object =
                {
                    id: "widgetlayout",
                    value: value
                };
            configUI.push(widgetlayout);

            value = configXML..splashpage.@url;
            if (value)
            {
                var splashconfig:String = configXML..splashpage.@config;
                var splashtitle:String = configXML..splashpage.@label;
                var splashpage:Object =
                    {
                        id: "splashpage",
                        value: value,
                        config: splashconfig,
                        title: splashtitle
                    };
                configUI.push(splashpage);
            }

            var wleft:String = configXML.widgetcontainer.@left;
            var wright:String = configXML.widgetcontainer.@right;
            var wtop:String = configXML.widgetcontainer.@top;
            var wbottom:String = configXML.widgetcontainer.@bottom;
            var wlayout:String = configXML.widgetcontainer.@layout;
            if (!wlayout)
            {
                wlayout = "horizontal";
            }

            if (wleft || wright || wtop || wbottom || wlayout)
            {
                var widgetContainer:Object =
                    {
                        id: "widgetcontainer",
                        left: wleft,
                        right: wright,
                        top: wtop,
                        bottom: wbottom,
                        layout: wlayout
                    };
                configUI.push(widgetContainer);
            }

            configData.viewerUI = configUI;


            //================================================
            //controls
            //================================================
            var configControls:Array = [];
            var controlList:XMLList = configXML.widget;
            var controlIdWeight:Number = 1000;
            for (i = 0; i < controlList.length(); i++)
            {
                var controlIcon:String = controlList[i].@icon;
                var controlLabel:String = controlList[i].@label;
                var controlLeft:String = controlList[i].@left;
                var controlRight:String = controlList[i].@right;
                var controlTop:String = controlList[i].@top;
                var controlBottom:String = controlList[i].@bottom;
                var controlVisible:String = controlList[i].@visible;
                var controlConfig:String = controlList[i].@config;
                var controlUrl:String = controlList[i].@url;

                var control:Object =
                    {
                        id: controlIdWeight + i,
                        icon: controlIcon,
                        label: controlLabel,
                        left: controlLeft,
                        right: controlRight,
                        top: controlTop,
                        bottom: controlBottom,
                        visible: controlVisible,
                        config: controlConfig,
                        url: controlUrl
                    };
                configControls.push(control);
            }
            configData.controls = configControls;


            //=================================================
            //map
            //================================================
            var mapAttrs:Array = [];
            var initialExtent:String = configXML.map.@initialextent;
            var fullExtent:String = configXML.map.@fullextent;
            if (initialExtent)
            {
                var iExt:Object =
                    {
                        id: "initial",
                        extent: initialExtent
                    };
                mapAttrs.push(iExt);
            }
            if (fullExtent)
            {
                var fExt:Object =
                    {
                        id: "full",
                        extent: fullExtent
                    };
                mapAttrs.push(fExt);
            }

            var mapLeft:Number;
            var mapRight:Number;
            var mapTop:Number;
            var mapBottom:Number;
            if (configXML.map.@left)
            {
                mapLeft = Number(configXML.map.@left);
            }
            if (configXML.map.@right)
            {
                mapRight = Number(configXML.map.@right);
            }
            if (configXML.map.@top)
            {
                mapTop = Number(configXML.map.@top);
            }
            if (configXML.map.@bottom)
            {
                mapBottom = Number(configXML.map.@bottom);
            }

            var size:Object =
                {
                    id: "map",
                    left: mapLeft,
                    right: mapRight,
                    top: mapTop,
                    bottom: mapBottom
                };
            mapAttrs.push(size);

            var wkid:Number;
            var wkt:String
            if (configXML.map.@wkid)
            {
                wkid = Number(configXML.map.@wkid);
            }
            if (configXML.map.@wkt)
            {
                wkt = configXML.map.@wkt;
            }
            var ref:Object =
                {
                    id: "spatialref",
                    wkid: wkid,
                    wkt: wkt
                };
            mapAttrs.push(ref);

            var zoomSliderVisible:Boolean = configXML.map.@zoomslidervisible == "true";
            var zoomSliderVisibility:Object =
                {
                    id: "zoomSlider",
                    zoomSliderVisible: zoomSliderVisible
                };
            mapAttrs.push(zoomSliderVisibility);

            var scaleBarVisible:Boolean = configXML.map.@scalebarvisible[0] ? configXML.map.@scalebarvisible == "true" : true;
            var scaleBarVisibility:Object =
                {
                    id: "scaleBar",
                    scaleBarVisible: scaleBarVisible
                };
            mapAttrs.push(scaleBarVisibility);

            var esriLogoVisible:Boolean = configXML.map.@esrilogovisible[0] ? configXML.map.@esrilogovisible == "true" : true;
            var esriLogoVisibility:Object =
                {
                    id: "esriLogo",
                    esriLogoVisible: esriLogoVisible
                };
            mapAttrs.push(esriLogoVisibility);

            var openHandCursorVisible:Boolean = configXML.map.@openhandcursorvisible[0] ? configXML.map.@openhandcursorvisible == "true" : true;
            var openHandCursorVisiblility:Object =
                {
                    id: "openHandCursor",
                    openHandCursorVisible: openHandCursorVisible
                };
            mapAttrs.push(openHandCursorVisiblility);

            var lodsList:XMLList = configXML.map.lods.lod;
            if (lodsList.length() > 0)
            {
                var lods:Array = [];
                for each (var lod:XML in lodsList)
                {
                    var resolution:Number = lod.@resolution;
                    var scale:Number = lod.@scale;
                    lods.push(new LOD(NaN, resolution, scale));
                }
                mapAttrs.push({ id: "lods", lods: lods });
            }

            configData.mapAttrs = mapAttrs;

            var arcGISWebMapItemID:String = configXML.map.@itemid[0];

            if (arcGISWebMapItemID)
            {
                var webMapUtil:WebMapUtil = new WebMapUtil();
                webMapUtil.bingMapsKey = configData.bingKey;
                webMapUtil.proxyURL = configData.proxyUrl;
                if (configData.geometryService.url)
                {
                    webMapUtil.geometryService = new GeometryService();
                    webMapUtil.geometryService.url = configData.geometryService.url;
                    webMapUtil.geometryService.token = configData.geometryService.token;
                    if (configData.geometryService.useproxy)
                    {
                        webMapUtil.geometryService.proxyURL = configData.proxyUrl;
                    }
                }
                webMapUtil.createMapById(arcGISWebMapItemID, new Responder(webMapUtil_createMapByIdResultHandler, webMapUtil_createMapByIdFaultHandler));
                function webMapUtil_createMapByIdResultHandler(result:WebMapEvent):void
                {
                    //trace(ObjectUtil.toString(result.itemData, null, [ "MapItems", "Slides" ]));
                    var baseMapTitle:String;
                    if (result.itemData.baseMap)
                    {
                        baseMapTitle = result.itemData.baseMap.title;
                    }

                    var layers:ArrayCollection = result.map.layers as ArrayCollection;
                    for (i = 0; i < layers.length; i++)
                    {
                        var layer:Layer = layers[i];
                        var isOpLayer:Boolean = layer.id.indexOf("base") != 0;

                        if (!isOpLayer && !baseMapTitle && i == 0)
                        {
                            baseMapTitle = layer.name;
                        }

                        var label:String = baseMapTitle;
                        if (isOpLayer)
                        {
                            label = layer.name;
                        }

                        var lyrXML:XML;
                        if (layer is ArcGISDynamicMapServiceLayer)
                        {
                            var dynLyr:ArcGISDynamicMapServiceLayer = layer as ArcGISDynamicMapServiceLayer;
                            lyrXML = <layer label={label}
                                    type="dynamic"
                                    visible={dynLyr.visible}
                                    alpha={dynLyr.alpha}
                                    useproxy={dynLyr.proxyURL != null}
                                    url={dynLyr.url}/>;
                            if (dynLyr.visibleLayers)
                            {
                                lyrXML.@visiblelayers = dynLyr.visibleLayers.toArray().join();
                            }
                        }
                        else if (layer is ArcGISImageServiceLayer)
                        {
                            var imgLyr:ArcGISImageServiceLayer = layer as ArcGISImageServiceLayer;
                            lyrXML = <layer label={label}
                                    type="image"
                                    visible={imgLyr.visible}
                                    alpha={imgLyr.alpha}
                                    useproxy={imgLyr.proxyURL != null}
                                    url={imgLyr.url}/>;
                            if (imgLyr.bandIds)
                            {
                                lyrXML.@bandids = imgLyr.bandIds.join();
                            }
                        }
                        else if (layer is ArcGISTiledMapServiceLayer)
                        {
                            var tiledLyr:ArcGISTiledMapServiceLayer = layer as ArcGISTiledMapServiceLayer;
                            lyrXML = <layer label={label}
                                    type="tiled"
                                    visible={tiledLyr.visible}
                                    alpha={tiledLyr.alpha}
                                    useproxy={tiledLyr.proxyURL != null}
                                    url={tiledLyr.url}/>;
                            if (tiledLyr.displayLevels)
                            {
                                lyrXML.@displaylevels = tiledLyr.displayLevels.join();
                            }
                        }
                        else if (layer is FeatureLayer)
                        {
                            var feaLyr:FeatureLayer = layer as FeatureLayer;
                            lyrXML = <layer label={label}
                                    type="feature"
                                    visible={feaLyr.visible}
                                    alpha={feaLyr.alpha}
                                    mode={feaLyr.mode}
                                    useproxy={feaLyr.proxyURL != null}
                                    url={feaLyr.url}/>;
                        }
                        else if (layer is VETiledLayer)
                        {
                            var veLyr:VETiledLayer = layer as VETiledLayer;
                            lyrXML = <layer label={label}
                                    type="bing"
                                    visible={veLyr.visible}
                                    alpha={veLyr.alpha}
                                    style={veLyr.mapStyle}/>;
                            if (veLyr.displayLevels)
                            {
                                lyrXML.@displaylevels = veLyr.displayLevels.join();
                            }
                        }
                        if (lyrXML)
                        {
                            if (isOpLayer)
                            {
                                configData.opLayers.push(getLayerObject(lyrXML, i, true, bingKey));
                            }
                            else
                            {
                                if (configData.opLayers.length > 0)
                                {
                                    lyrXML.@reference = true;
                                }
                                configData.basemaps.push(getLayerObject(lyrXML, i, false, bingKey));
                            }
                        }
                    }
                    if (!initialExtent)
                    {
                        var extent:Extent = result.map.extent;
                        if (extent)
                        {
                            var extentArr:Array = [ extent.xmin, extent.ymin, extent.xmax, extent.ymax ];
                            var iExt:Object =
                                {
                                    id: "initial",
                                    extent: extentArr.join(" ")
                                };
                            mapAttrs.push(iExt);
                        }
                    }
                    ViewerContainer.dispatchEvent(new AppEvent(AppEvent.CONFIG_LOADED, configData));
                }
                function webMapUtil_createMapByIdFaultHandler(error:Fault):void
                {
                    ViewerContainer.showError(error.faultString);
                }
            }
            else
            {
                //================================================
                //map:basemaps
                //================================================
                var configMap:Array = [];
                var maplayerList:XMLList = configXML.map.basemaps.mapservice;

                if (maplayerList.length() < 1)
                {
                    maplayerList = configXML.map.basemaps.layer;
                }

                for (i = 0; i < maplayerList.length(); i++)
                {
                    configMap.push(getLayerObject(maplayerList[i], i, false, bingKey));
                }
                configData.basemaps = configMap;


                //================================================
                //map:operationalLayers
                //================================================
                var configOpLayers:Array = [];
                var opLayerList:XMLList = configXML.map.operationallayers.layer;
                for (i = 0; i < opLayerList.length(); i++)
                {
                    configOpLayers.push(getLayerObject(opLayerList[i], i, true, bingKey));
                }
                configData.opLayers = configOpLayers;
            }


            //=================================================
            //widgets
            //================================================
            var configWidgets:Array = [];
            var widgetContainerList:XMLList = configXML.widgetcontainer;
            var widgetId:Number = 0;
            for (i = 0; i < widgetContainerList.children().length(); i++)
            {
                var xmlObject:XML = widgetContainerList.children()[i];
                if (xmlObject.name() == "widgetgroup")
                {
                    var widgetGroupList:XMLList = XMLList(xmlObject);
                    createWidgets(widgetGroupList.widget, true, widgetGroupList.widget.length(), widgetGroupList.@label, widgetGroupList.@icon);
                }
                else
                {
                    var widgetList:XMLList = XMLList(xmlObject);
                    createWidgets(widgetList, false);
                }
            }

            function createWidgets(widgetList:XMLList, grouped:Boolean, groupLength:Number = 0, groupLabel:String = null, groupIcon:String = null):void
            {
                for (var p:int = 0; p < widgetList.length(); p++)
                {
                    // if grouped
                    var wGrouped:Boolean = grouped;
                    var wGroupLength:Number = groupLength;
                    var wGroupIcon:String = groupIcon;
                    var wGroupLabel:String = groupLabel;

                    var wLabel:String = widgetList[p].@label;
                    var wIcon:String = widgetList[p].@icon;
                    var wConfig:String = widgetList[p].@config;
                    var wPreload:String = widgetList[p].@preload;
                    var wUrl:String = widgetList[p].@url;
                    var wx:String = widgetList[p].@x;
                    var wy:String = widgetList[p].@y;
                    var wLeft:String = widgetList[p].@left;
                    var wTop:String = widgetList[p].@top;
                    var wRight:String = widgetList[p].@right;
                    var wBottom:String = widgetList[p].@bottom;

                    if (!wGroupIcon)
                    {
                        wGroupIcon = ViewerContainer.DEFAULT_WIDGET_GROUP_ICON;
                    }
                    if (!wIcon)
                    {
                        wIcon = ViewerContainer.DEFAULT_WIDGET_ICON;
                    }

                    var widget:Object =
                        {
                            id: widgetId,
                            grouped: wGrouped,
                            groupLength: wGroupLength,
                            groupIcon: wGroupIcon,
                            groupLabel: wGroupLabel,
                            label: wLabel,
                            icon: wIcon,
                            config: wConfig,
                            preload: wPreload,
                            x: wx,
                            y: wy,
                            left: wLeft,
                            top: wTop,
                            right: wRight,
                            bottom: wBottom,
                            url: wUrl
                        };
                    configWidgets.push(widget);
                    widgetId++;
                }
            }
            configData.widgets = configWidgets;


            //=================================================
            //widgetContainers
            //   [] ={container, widgets}
            //================================================

            var wContainers:XMLList = configXML.widgetcontainer;
            var configWContainers:Array = [];
            for (i = 0; i < wContainers.length(); i++)
            {
                //get container parameters
                var wcLeft:String = wContainers[i].@left;
                var wcRight:String = wContainers[i].@right;
                var wcTop:String = wContainers[i].@top;
                var wcBottom:String = wContainers[i].@bottom;
                var wcLayout:String = wContainers[i].@layout;
                var wcUrl:String = wContainers[i].@url;

                if (!wcLayout)
                {
                    wcLayout = ViewerContainer.DEFAULT_WIDGET_LAYOUT;
                }

                if (!wcUrl)
                {
                    wcUrl = ViewerContainer.DEFAULT_WIDGET_CONTAINER_WIDGET;
                }

                var wgContainer:Object =
                    {
                        id: i,
                        left: wcLeft,
                        right: wcRight,
                        top: wcTop,
                        bottom: wcBottom,
                        layout: wcLayout,
                        url: wcUrl,
                        obj: null
                    };


                //get widgets for this container
                var contWidgets:Array = [];
                var wid:uint = 0;
                for (var n:int = 0; n < wContainers[i].children().length(); n++)
                {
                    var xmlObj:XML = wContainers[i].children()[n];
                    if (xmlObj.name() == "widgetgroup")
                    {
                        var widgetGrpList:XMLList = XMLList(xmlObj);
                        getWidgetList(widgetGrpList.widget, true, widgetGrpList.widget.length(), widgetGrpList.@label, widgetGrpList.@icon);
                    }
                    else
                    {
                        var wdgtList:XMLList = XMLList(xmlObj);
                        getWidgetList(wdgtList, false);
                    }
                }

                function getWidgetList(wgList:XMLList, grouped:Boolean, groupLength:Number = 0, groupLabel:String = null, groupIcon:String = null):void
                {
                    for (j = 0; j < wgList.length(); j++)
                    {
                        // if grouped
                        var wgGrouped:Boolean = grouped;
                        var wgGroupLength:Number = groupLength;
                        var wgGroupIcon:String = groupIcon;
                        var wgGroupLabel:String = groupLabel;

                        var wgLabel:String = wgList[j].@label;
                        var wgIcon:String = wgList[j].@icon;
                        var wgConfig:String = wgList[j].@config;
                        var wgPreload:String = wgList[j].@preload;
                        var wgUrl:String = wgList[j].@url;
                        var wgx:String = wgList[j].@x;
                        var wgy:String = wgList[j].@y;
                        var wgLeft:String = wgList[j].@left;
                        var wgTop:String = wgList[j].@top;
                        var wgRight:String = wgList[j].@right;
                        var wgBottom:String = wgList[j].@bottom;

                        if (!wgGroupIcon)
                        {
                            wgGroupIcon = ViewerContainer.DEFAULT_WIDGET_GROUP_ICON;
                        }
                        if (!wgIcon)
                        {
                            wgIcon = ViewerContainer.DEFAULT_WIDGET_ICON;
                        }

                        var wg:Object =
                            {
                                id: wid,
                                grouped: wgGrouped,
                                groupLength: wgGroupLength,
                                groupIcon: wgGroupIcon,
                                groupLabel: wgGroupLabel,
                                label: wgLabel,
                                icon: wgIcon,
                                config: wgConfig,
                                preload: wgPreload,
                                x: wgx,
                                y: wgy,
                                left: wgLeft,
                                right: wgRight,
                                top: wgTop,
                                bottom: wgBottom,
                                url: wgUrl
                            };
                        contWidgets.push(wg);

                        //indexing
                        var windex:Object = { container: i, widget: wid };
                        configData.widgetIndex.push(windex);
                        wid++;
                    }
                }

                var container:Object = { container: wgContainer, widgets: contWidgets };
                configWContainers.push(container);
            }
            configData.widgetContainers = configWContainers;


            if (!arcGISWebMapItemID)
            {
                //================================================
                //announce configuration is complete
                //================================================
                ViewerContainer.dispatchEvent(new AppEvent(AppEvent.CONFIG_LOADED, configData));
            }
        }
        catch (error:Error)
        {
            ViewerContainer.showError(StringUtil.substitute(getDefaultString("parseConfigError"), error.message));
        }
    }

    private function getLayerObject(obj:XML, num:Number, isOpLayer:Boolean, bingkey:String):Object
    {
        var label:String = isOpLayer ? 'OpLayer ' + num : 'Map ' + num; // default label
        if (obj.@label[0]) // check that label attribute exist
        {
            label = obj.@label; // set basemap label if specified in configuration file
        }

        var type:String;
        if (!isOpLayer)
        {
            type = "tiled"; // default basemap type
        }
        if (obj.@type[0]) // check that type attribute exist
        {
            type = obj.@type; // set basemap type if specified in configuration file
        }

        var visible:Boolean = obj.@visible == "true";

        var alpha:Number = 1.0;
        if (obj.@alpha[0])
        {
            if (!isNaN(parseFloat(obj.@alpha)))
            {
                alpha = parseFloat(obj.@alpha);
            }
        }

        var noData:Number;
        if (obj.@nodata[0])
        {
            if (!isNaN(parseFloat(obj.@nodata)))
            {
                noData = parseFloat(obj.@nodata);
            }
        }

        var autoRefresh:Number = 0;
        if (obj.@autorefresh[0])
        {
            if (!isNaN(parseInt(obj.@autorefresh)))
            {
                autoRefresh = parseInt(obj.@autorefresh);
            }
        }

        var useProxy:Boolean = obj.@useproxy[0] && obj.@useproxy == "true"; // default false
        var useMapTime:Boolean = obj.@usemaptime[0] ? obj.@usemaptime == "true" : true; // default true
        var useAMF:String = obj.@useamf[0] ? obj.@useamf : "";
        var token:String = obj.@token[0] ? obj.@token : "";
        var mode:String = obj.@mode[0] ? obj.@mode : "";
        var icon:String = obj.@icon[0] ? obj.@icon : "";
        var imageFormat:String = obj.@imageformat;
        var visibleLayers:String = obj.@visiblelayers;
        var displayLevels:String = obj.@displaylevels;
        var bandIds:String = obj.@bandids;
        var skipGetCapabilities:String = obj.@skipgetcapabilities[0];
        var version:String = obj.@version[0];
        var url:String = obj.@url;
        var username:String = obj.@username;
        var password:String = obj.@password;

        // ve tiled layer
        var style:String = obj.@style[0] ? obj.@style : "";
        var key:String;
        if (bingkey && bingkey != "")
        {
            key = bingkey;
        }
        else
        {
            key = obj.@key[0] ? obj.@key : "";
        }
        var culture:String = obj.@culture[0] ? obj.@culture : "";

        // arcims layer
        var serviceHost:String = obj.@servicehost[0] ? obj.@servicehost : "";
        var serviceName:String = obj.@servicename[0] ? obj.@servicename : "";

        // definitionExpression for featurelayer
        var definitionExpression:String = obj.@definitionexpression[0] ? obj.@definitionexpression : "";

        var resultObject:Object =
            {
                id: String(num),
                alpha: alpha,
                bandIds: bandIds,
                autoRefresh: autoRefresh,
                culture: culture,
                definitionExpression: definitionExpression,
                displayLevels: displayLevels,
                icon: icon,
                imageFormat: imageFormat,
                key: key,
                label: label,
                mode: mode,
                noData: noData,
                password: password,
                serviceHost: serviceHost,
                serviceName: serviceName,
                skipGetCapabilities: skipGetCapabilities,
                style: style,
                token: token,
                type: type,
                url: url,
                useAMF: useAMF,
                useMapTime: useMapTime,
                useProxy: useProxy,
                username: username,
                version: version,
                visible: visible,
                visibleLayers: visibleLayers
            };

        if (isOpLayer)
        {
            var opLayerInfo:String = obj.@info;
            var opLayerInfoConfig:String = obj.@infoconfig;
            resultObject.infoConfig = opLayerInfoConfig;
            resultObject.infoUrl = opLayerInfo;
            resultObject.layer = null;
        }
        else
        {
            var reference:Boolean = obj.@reference[0] && obj.@reference == "true";
            resultObject.reference = reference;
        }

        return resultObject;
    }

    private function getDefaultString(token:String):String
    {
        return ResourceManager.getInstance().getString("ViewerStrings", token);
    }
}

}
