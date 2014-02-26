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
package com.esri.viewer.managers
{

import com.esri.ags.events.WebMapEvent;
import com.esri.ags.geometry.Extent;
import com.esri.ags.layers.ArcGISDynamicMapServiceLayer;
import com.esri.ags.layers.ArcGISImageServiceLayer;
import com.esri.ags.layers.ArcGISTiledMapServiceLayer;
import com.esri.ags.layers.CSVLayer;
import com.esri.ags.layers.FeatureLayer;
import com.esri.ags.layers.GeoRSSLayer;
import com.esri.ags.layers.KMLLayer;
import com.esri.ags.layers.Layer;
import com.esri.ags.layers.OpenStreetMapLayer;
import com.esri.ags.layers.WMSLayer;
import com.esri.ags.layers.WebTiledLayer;
import com.esri.ags.layers.supportClasses.Field;
import com.esri.ags.layers.supportClasses.LOD;
import com.esri.ags.portal.WebMapUtil;
import com.esri.ags.tasks.GeometryServiceSingleton;
import com.esri.ags.virtualearth.VETiledLayer;
import com.esri.viewer.AppEvent;
import com.esri.viewer.ConfigData;
import com.esri.viewer.ViewerContainer;
import com.esri.viewer.utils.ErrorMessageUtil;
import com.esri.viewer.utils.LayerObjectUtil;
import com.esri.viewer.utils.LocalizationUtil;
import com.esri.viewer.utils.PortalBasemapAppender;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IOErrorEvent;
import flash.events.SecurityErrorEvent;

import mx.collections.ArrayCollection;
import mx.rpc.Fault;
import mx.rpc.Responder;
import mx.rpc.events.FaultEvent;
import mx.rpc.events.ResultEvent;
import mx.rpc.http.HTTPService;

[Event(name="configLoaded", type="com.esri.viewer.AppEvent")]

/**
 * ConfigManager is used to parse the configuration file and store the information in ConfigData.
 * The default configuration file is config.xml, but you can specify an alternative configuration file,
 * e.g. http://myserver/flexviewer/index.html?config=myconfig.xml
 *
 * The name of the default configuration file is specified in ViewerContainer.mxml.
 */
public class ConfigManager extends EventDispatcher
{
    private const CONFIG_MANAGER:String = "ConfigManager";
    private const DEFAULT_GEOMETRY_SERVICE_URL:String = "http://tasks.arcgisonline.com/ArcGIS/rest/services/Geometry/GeometryServer";

    private var configData:ConfigData;

    public function ConfigManager()
    {
        //make sure the container is properly initialized and then
        //proceed with configuration initialization.
        AppEvent.addListener(ViewerContainer.CONTAINER_INITIALIZED, containerInitializedHandler);
    }

    private function containerInitializedHandler(event:Event):void
    {
        loadConfig();
    }

    private function loadConfig():void
    {
        var configService:HTTPService = new HTTPService();
        configService.url = ViewerContainer.configFile;
        configService.resultFormat = HTTPService.RESULT_FORMAT_TEXT;
        configService.addEventListener(ResultEvent.RESULT, configService_resultHandler);
        configService.addEventListener(FaultEvent.FAULT, configService_faultHandler);
        configService.send();
    }

    private function configService_faultHandler(event:FaultEvent):void
    {
        var sInfo:String = "";

        if (event.fault.rootCause is IOErrorEvent)
        {
            var ioe:IOErrorEvent = event.fault.rootCause as IOErrorEvent;
            // Missing config file
            if (ioe.errorID == 2032)
            {
                sInfo += LocalizationUtil.getDefaultString('missingConfigFileText', ViewerContainer.configFile) + "\n\n";
            }
            else
            {
                // some other IOError
                sInfo += event.fault.rootCause + "\n\n";
            }
        }

        if (event.fault.rootCause is SecurityErrorEvent)
        {
            var sec:SecurityErrorEvent = event.fault.rootCause as SecurityErrorEvent;
            // config file with crossdomain issue
            if (sec.errorID == 2048)
            {
                sInfo += LocalizationUtil.getDefaultString('configFileCrossDomain', "\n", sec.toString()) + "\n\n";
            }
            // some other Security error
            else
            {
                sInfo += event.fault.rootCause + "\n\n";
            }
        }

        if (event.statusCode) // e.g. 404 - Not Found - http://en.wikipedia.org/wiki/List_of_HTTP_status_codes
        {
            sInfo += LocalizationUtil.getDefaultString('httpResponseStatus', event.statusCode) + "\n\n";
        }

        sInfo += LocalizationUtil.getDefaultString('faultCode', event.fault.faultCode) + "\n\n";
        sInfo += LocalizationUtil.getDefaultString('faultInfo', event.fault.faultString) + "\n\n";
        sInfo += LocalizationUtil.getDefaultString('faultDetail', event.fault.faultDetail);

        AppEvent.showError(sInfo, CONFIG_MANAGER);
    }

    private function configService_resultHandler(event:ResultEvent):void
    {
        try
        {
            //parse main configuration file to create config data object
            configData = new ConfigData();
            var configXML:XML = XML(event.result);
            configData.configXML = configXML;

            var i:int;
            var j:int;

            //================================================
            //Proxy configuration
            //================================================
            var proxyUrl:String = configXML.httpproxy;
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

                GeometryServiceSingleton.instance.token = configData.geometryService.token;
                if (configData.geometryService.useproxy)
                {
                    GeometryServiceSingleton.instance.proxyURL = configData.proxyUrl;
                }
                var geometryServiceURL:String = configData.geometryService.url ? configData.geometryService.url : DEFAULT_GEOMETRY_SERVICE_URL;
                GeometryServiceSingleton.instance.url = geometryServiceURL;
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
                };
            configData.font = font;

            var styleTitleFontName:String = configXML.style.titlefont.@name;
            var styleTitleFontSize:String = configXML.style.titlefont.@size;
            var titleFont:Object =
                {
                    id: "titleFont",
                    name: styleTitleFontName,
                    size: int(styleTitleFontSize)
                };
            configData.titleFont = titleFont;

            var styleSubTitleFontName:String = configXML.style.subtitlefont.@name;
            var styleSubTitleFontSize:String = configXML.style.subtitlefont.@size;
            var subTitleFont:Object =
                {
                    id: "subTitleFont",
                    name: styleSubTitleFontName,
                    size: int(styleSubTitleFontSize)
                };
            configData.subTitleFont = subTitleFont;

            //================================================
            //layoutDirection configuration
            //================================================
            var layoutDirection:String = configXML.layoutdirection;
            if (layoutDirection)
            {
                configData.layoutDirection = layoutDirection;
            }

            //================================================
            //user interface
            //================================================
            var configUI:Array = [];
            var value:String = configXML.title[0];
            var title:Object =
                {
                    id: "title",
                    value: value
                };
            configUI.push(title);

            value = configXML.subtitle[0];
            var subtitle:Object =
                {
                    id: "subtitle",
                    value: value
                };
            configUI.push(subtitle);

            value = configXML.logo[0];
            var logo:Object =
                {
                    id: "logo",
                    value: value
                };
            configUI.push(logo);

            value = configXML.splashpage.@url;
            if (value)
            {
                var splashConfig:String = configXML.splashpage.@config;
                var splashConfigXML:XML = null;
                if (splashConfig.charAt(0) === "#")
                {
                    splashConfigXML = configXML.configuration.(@id == splashConfig.substr(1))[0];
                }
                var splashTitle:String = configXML.splashpage.@label;
                var splashPage:Object =
                    {
                        id: "splashpage",
                        value: value,
                        config: splashConfig,
                        configXML: splashConfigXML,
                        title: splashTitle
                    };
                configUI.push(splashPage);
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
                var controlHorizontalCenter:String = controlList[i].@horizontalcenter;
                var controlVerticalCenter:String = controlList[i].@verticalcenter;
                var controlVisible:String = controlList[i].@visible;
                var controlConfig:String = controlList[i].@config;
                var controlUrl:String = controlList[i].@url;

                var controlConfigXML:XML = null;
                if (controlConfig.charAt(0) === "#")
                {
                    controlConfigXML = configXML.configuration.(@id == controlConfig.substr(1))[0];
                }

                var control:Object =
                    {
                        id: controlIdWeight + i,
                        icon: controlIcon,
                        label: controlLabel,
                        left: controlLeft,
                        right: controlRight,
                        top: controlTop,
                        bottom: controlBottom,
                        horizontalCenter: controlHorizontalCenter,
                        verticalCenter: controlVerticalCenter,
                        visible: controlVisible,
                        config: controlConfig,
                        configXML: controlConfigXML,
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
            if (ViewerContainer.urlConfigParams.extent != null)
            {
                var extentParam:String = ViewerContainer.urlConfigParams.extent;
                initialExtent = extentParam.replace(/,/g, " ");
            }
            if (initialExtent)
            {
                var iExt:Object =
                    {
                        id: "initial",
                        extent: initialExtent
                    };
                mapAttrs.push(iExt);
            }
            var fullExtent:String = configXML.map.@fullextent;
            if (fullExtent)
            {
                var fExt:Object =
                    {
                        id: "full",
                        extent: fullExtent
                    };
                mapAttrs.push(fExt);
            }
            var mapCenter:String = configXML.map.@center;
            if (ViewerContainer.urlConfigParams.center != null)
            {
                var centerParam:String = ViewerContainer.urlConfigParams.center;
                mapCenter = centerParam.replace(/,/g, " ");
            }
            if (mapCenter)
            {
                var centerObj:Object =
                    {
                        id: "center",
                        center: mapCenter
                    };
                mapAttrs.push(centerObj);
            }
            var mapLevel:String = configXML.map.@level;
            if (ViewerContainer.urlConfigParams.level != null)
            {
                mapLevel = ViewerContainer.urlConfigParams.level;
            }
            if (mapLevel)
            {
                var levelObj:Object =
                    {
                        id: "level",
                        level: mapLevel
                    };
                mapAttrs.push(levelObj);
            }
            var mapScale:String = configXML.map.@scale;
            if (ViewerContainer.urlConfigParams.scale != null)
            {
                mapScale = ViewerContainer.urlConfigParams.scale;
            }
            if (mapScale)
            {
                var scaleObj:Object =
                    {
                        id: "scale",
                        scale: mapScale
                    };
                mapAttrs.push(scaleObj);
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
            var wkt:String;
            if (configXML.map.@wkid[0])
            {
                wkid = Number(configXML.map.@wkid);
            }
            if (configXML.map.@wkt[0])
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
            var scaleBarStyleName:String = configXML.map.@scalebar[0];
            var scaleBarProperties:Object =
                {
                    id: "scaleBar",
                    scaleBarVisible: scaleBarVisible,
                    scaleBarStyleName: scaleBarStyleName
                };
            mapAttrs.push(scaleBarProperties);

            var esriLogoVisible:Boolean = configXML.map.@esrilogovisible[0] ? configXML.map.@esrilogovisible == "true" : true;
            var esriLogoVisibility:Object =
                {
                    id: "esriLogo",
                    esriLogoVisible: esriLogoVisible
                };
            mapAttrs.push(esriLogoVisibility);

            var openHandCursorVisible:Boolean = configXML.map.@openhandcursorvisible[0] ? configXML.map.@openhandcursorvisible == "true" : false;
            var openHandCursorVisibility:Object =
                {
                    id: "openHandCursor",
                    openHandCursorVisible: openHandCursorVisible
                };
            mapAttrs.push(openHandCursorVisibility);

            var wrapAround180:Boolean = configXML.map.@wraparound180[0] ? configXML.map.@wraparound180 == "true" : false;
            var wrapAround180Attr:Object =
                {
                    id: "wrapAround180",
                    wrapAround180: wrapAround180
                };
            mapAttrs.push(wrapAround180Attr);

            var mapRotation:Number = parseFloat(configXML.map.@rotation[0]);
            if (!isNaN(mapRotation))
            {
                var mapRotationAttr:Object =
                    {
                        id: "mapRotation",
                        mapRotation: mapRotation
                    };
                mapAttrs.push(mapRotationAttr);
            }

            var panEasingFactor:Number = parseFloat(configXML.map.@paneasingfactor[0]);
            if (!isNaN(panEasingFactor))
            {
                var panEasingFactorAttr:Object =
                    {
                        id: "panEasingFactor",
                        panEasingFactor: panEasingFactor
                    };
                mapAttrs.push(panEasingFactorAttr);
            }

            var units:String = configXML.map.@units[0];
            if (units)
            {
                var unitsAttr:Object =
                    {
                        id: "units",
                        units: units
                    };
                mapAttrs.push(unitsAttr);
            }

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

            if (configXML.map.@attributionvisible[0])
            {
                var attributionVisible:Boolean = configXML.map.@attributionvisible[0] != "false";
                var attributionVisibleAttr:Object =
                    {
                        id: "attributionVisible",
                        attributionVisible: attributionVisible
                    };
                mapAttrs.push(attributionVisibleAttr);
            }

            configData.mapAttrs = mapAttrs;

            var configWebMapItemID:String = configXML.map.@webmap[0] || configXML.map.@itemid[0];
            var urlConfigWebMapItemID:String = ViewerContainer.urlConfigParams.webmap || ViewerContainer.urlConfigParams.itemid;
            var webMapItemID:String = urlConfigWebMapItemID ? urlConfigWebMapItemID : configWebMapItemID;

            var portalURL:String = configXML.map.@portalurl[0] || configXML.map.@arcgissharingurl[0];
            var addArcGISBasemaps:Boolean = configXML.map.@addarcgisbasemaps[0] == "true";

            if (webMapItemID)
            {
                var webMapUtil:WebMapUtil = new WebMapUtil();
                webMapUtil.bingMapsKey = configData.bingKey;
                webMapUtil.proxyURL = configData.proxyUrl;
                if (GeometryServiceSingleton.instance.url)
                {
                    webMapUtil.geometryService = GeometryServiceSingleton.instance;
                }
                if (portalURL)
                {
                    webMapUtil.portalURL = portalURL;
                }
                webMapUtil.createMapById(webMapItemID, new Responder(webMapUtil_createMapByIdResultHandler, webMapUtil_createMapByIdFaultHandler));
                function webMapUtil_createMapByIdResultHandler(result:WebMapEvent):void
                {
                    if (!title.value)
                    {
                        title.value = result.item.title;
                    }

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
                        layer.id = label;

                        var lyrXML:XML = createLayerXML(layer, label);
                        if (lyrXML)
                        {
                            if (isOpLayer)
                            {
                                configData.opLayers.push(LayerObjectUtil.getLayerObject(lyrXML, i, true, bingKey, layer, proxyUrl));
                            }
                            else
                            {
                                if (configData.opLayers.length > 0)
                                {
                                    lyrXML.@reference = true;
                                }
                                configData.basemaps.push(LayerObjectUtil.getLayerObject(lyrXML, i, false, bingKey, layer, proxyUrl));
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

                    configData.webMapData = result.itemData;

                    if (addArcGISBasemaps)
                    {
                        appendPortalBasemaps(portalURL);
                    }
                    else
                    {
                        AppEvent.dispatch(AppEvent.CONFIG_LOADED, configData);
                    }
                }

                function webMapUtil_createMapByIdFaultHandler(error:Fault):void
                {
                    AppEvent.showError(
                        ErrorMessageUtil.getKnownErrorCauseMessage(error)
                        + "\n\n" + ErrorMessageUtil.buildFaultMessage(error), CONFIG_MANAGER);
                }
            }
            else
            {
                //================================================
                //map:basemaps
                //================================================
                var configBasemaps:Array = [];
                var maplayerList:XMLList = configXML.map.basemaps.mapservice; // TODO - is this still in use ???

                if (maplayerList.length() < 1)
                {
                    maplayerList = configXML.map.basemaps.layer;
                }

                for (i = 0; i < maplayerList.length(); i++)
                {
                    configBasemaps.push(LayerObjectUtil.getLayerObject(maplayerList[i], i, false, bingKey, null, proxyUrl));
                }
                configData.basemaps = configBasemaps;

                //================================================
                //map:operationalLayers
                //================================================
                var configOpLayers:Array = [];
                var opLayerList:XMLList = configXML.map.operationallayers.layer;
                for (i = 0; i < opLayerList.length(); i++)
                {
                    configOpLayers.push(LayerObjectUtil.getLayerObject(opLayerList[i], i, true, bingKey, null, proxyUrl));
                }
                configData.opLayers = configOpLayers;
            }

            //=================================================
            //widgets
            //================================================
            var configWidgets:Array = [];
            var widgetContainerList:XMLList = configXML.widgetcontainer;
            var widgetId:Number = 0;
            for (var w:int = 0; w < widgetContainerList.length(); w++)
            {
                var wContainer:XML = widgetContainerList[w] as XML;
                var isPartOfPanel:Boolean = wContainer.@paneltype[0] ? true : false;
                for (i = 0; i < wContainer.children().length(); i++)
                {
                    var xmlObject:XML = wContainer.children()[i];
                    if (xmlObject.name() == "widgetgroup")
                    {
                        var widgetGroupList:XMLList = XMLList(xmlObject);
                        createWidgets(widgetGroupList.widget, true, isPartOfPanel, widgetGroupList.widget.length(), widgetGroupList.@label, widgetGroupList.@icon);
                    }
                    else
                    {
                        var widgetList:XMLList = XMLList(xmlObject);
                        createWidgets(widgetList, false, isPartOfPanel);
                    }
                }
            }

            function createWidgets(widgetList:XMLList, grouped:Boolean, isPartOfPanel:Boolean, groupLength:Number = 0, groupLabel:String = null, groupIcon:String = null):void
            {
                var widgetListLength:int = widgetList.length();
                for (var p:int = 0; p < widgetListLength; p++)
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
                    var wWidth:String = widgetList[p].@width;
                    var wHeight:String = widgetList[p].@height;
                    var wUrl:String = widgetList[p].@url;
                    var wx:String = widgetList[p].@x;
                    var wy:String = widgetList[p].@y;
                    var wLeft:String = widgetList[p].@left;
                    var wTop:String = widgetList[p].@top;
                    var wRight:String = widgetList[p].@right;
                    var wBottom:String = widgetList[p].@bottom;

                    // Look for embedded configuration
                    var wConfigXML:XML = null;
                    if (wConfig.charAt(0) === "#")
                    {
                        wConfigXML = configXML.configuration.(@id == wConfig.substr(1))[0];
                    }
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
                            configXML: wConfigXML, // reference to embedded XML configuration (if any)
                            preload: wPreload,
                            width: wWidth,
                            height: wHeight,
                            x: wx,
                            y: wy,
                            left: wLeft,
                            top: wTop,
                            right: wRight,
                            bottom: wBottom,
                            url: wUrl,
                            isPartOfPanel: isPartOfPanel
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
            var wid:uint;
            var n:int;
            var xmlObj:XML;
            for (i = 0; i < wContainers.length(); i++)
            {
                //get container parameters
                var wcPanelType:String = wContainers[i].@paneltype;

                var wgContainer:Object;
                var contWidgets:Array = [];
                if (wcPanelType)
                {
                    var wcSize:String = wContainers[i].@size;
                    var wcInitialState:String = "open";
                    if (wContainers[i].@initialstate[0])
                    {
                        wcInitialState = wContainers[i].@initialstate;
                    }
                    wgContainer =
                        {
                            id: i,
                            panelType: wcPanelType,
                            initialState: wcInitialState,
                            size: wcSize,
                            obj: null
                        };

                    //get widget for this container
                    wid = 0;
                    for (n = 0; n < wContainers[i].children().length(); n++)
                    {
                        xmlObj = wContainers[i].children()[n];

                        var wgLabel:String = xmlObj.@label;
                        var wgConfig:String = xmlObj.@config;
                        var wgWidth:String = xmlObj.@width;
                        var wgHeight:String = xmlObj.@height;
                        var wgUrl:String = xmlObj.@url;

                        var wgConfigXML:XML = null;
                        if (wgConfig.charAt(0) === "#")
                        {
                            wgConfigXML = configXML.configuration.(@id == wgConfig.substr(1))[0];
                        }
                        var wg:Object =
                            {
                                id: wid,
                                label: wgLabel,
                                config: wgConfig,
                                configXML: wgConfigXML, // reference to embedded XML configuration (if any)
                                width: wgWidth,
                                height: wgHeight,
                                url: wgUrl,
                                isPartOfPanel: true
                            };
                        contWidgets.push(wg);

                        //indexing
                        var windex:Object = { container: i, widget: wid };
                        configData.widgetIndex.push(windex);
                        wid++;
                    }
                }
                else
                {
                    var wcUrl:String = wContainers[i].@url;
                    if (!wcUrl)
                    {
                        wcUrl = ViewerContainer.DEFAULT_WIDGET_CONTAINER_WIDGET;
                    }

                    var wcLeft:String = wContainers[i].@left;
                    var wcRight:String = wContainers[i].@right;
                    var wcTop:String = wContainers[i].@top;
                    var wcBottom:String = wContainers[i].@bottom;
                    var wcLayout:String = wContainers[i].@layout;

                    if (!wcLayout)
                    {
                        wcLayout = ViewerContainer.DEFAULT_WIDGET_LAYOUT;
                    }

                    wgContainer =
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
                    wid = 0;
                    for (n = 0; n < wContainers[i].children().length(); n++)
                    {
                        xmlObj = wContainers[i].children()[n];
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
                            var wgWidth:String = wgList[j].@width;
                            var wgHeight:String = wgList[j].@height;
                            var wgUrl:String = wgList[j].@url;
                            var wgx:String = wgList[j].@x;
                            var wgy:String = wgList[j].@y;
                            var wgLeft:String = wgList[j].@left;
                            var wgTop:String = wgList[j].@top;
                            var wgRight:String = wgList[j].@right;
                            var wgBottom:String = wgList[j].@bottom;
                            var wHorizontalCenter:String = wgList[j].@horizontalcenter;
                            var wVerticalCenter:String = wgList[j].@verticalcenter;

                            var wgConfigXML:XML = null;
                            if (wgConfig.charAt(0) === "#")
                            {
                                wgConfigXML = configXML.configuration.(@id == wgConfig.substr(1))[0];
                            }
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
                                    configXML: wgConfigXML, // reference to embedded XML configuration (if any)
                                    preload: wgPreload,
                                    width: wgWidth,
                                    height: wgHeight,
                                    x: wgx,
                                    y: wgy,
                                    left: wgLeft,
                                    right: wgRight,
                                    top: wgTop,
                                    bottom: wgBottom,
                                    horizontalCenter: wHorizontalCenter,
                                    verticalCenter: wVerticalCenter,
                                    url: wgUrl,
                                    isPartOfPanel: false
                                };
                            contWidgets.push(wg);

                            //indexing
                            var windex:Object = { container: i, widget: wid };
                            configData.widgetIndex.push(windex);
                            wid++;
                        }
                    }
                }

                var container:Object = { container: wgContainer, widgets: contWidgets };
                configWContainers.push(container);
            }
            configData.widgetContainers = configWContainers;

            if (!webMapItemID)
            {
                //================================================
                //announce configuration is complete
                //================================================
                if (addArcGISBasemaps)
                {
                    appendPortalBasemaps(portalURL);
                }
                else
                {
                    AppEvent.dispatch(AppEvent.CONFIG_LOADED, configData);
                }
            }
        }
        catch (error:Error)
        {
            AppEvent.showError(LocalizationUtil.getDefaultString("parseConfigErrorText", ViewerContainer.configFile + "\n" + error.message), CONFIG_MANAGER);
        }
    }

    private function createLayerXML(layer:Layer, label:String):XML
    {
        var lyrXML:XML = null;
        if (layer is ArcGISDynamicMapServiceLayer)
        {
            var dynLyr:ArcGISDynamicMapServiceLayer = layer as ArcGISDynamicMapServiceLayer;
            lyrXML = <layer label={label}
                    type="dynamic"
                    visible={dynLyr.visible}
                    alpha={dynLyr.alpha}
                    minscale={dynLyr.minScale}
                    maxscale={dynLyr.maxScale}
                    showinlegend={dynLyr.showInLegend}
                    useproxy={dynLyr.proxyURL != null}
                    url={dynLyr.url}/>;
            if (dynLyr.showInLegend && dynLyr.showInLegendHiddenLayers)
            {
                lyrXML.@showinlegendhiddenlayers = dynLyr.showInLegendHiddenLayers.join();
            }
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
                    minscale={imgLyr.minScale}
                    maxscale={imgLyr.maxScale}
                    showinlegend={imgLyr.showInLegend}
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
                    minscale={tiledLyr.minScale}
                    maxscale={tiledLyr.maxScale}
                    showinlegend={tiledLyr.showInLegend}
                    alpha={tiledLyr.alpha}
                    useproxy={tiledLyr.proxyURL != null}
                    url={tiledLyr.url}/>;
            if (tiledLyr.displayLevels)
            {
                lyrXML.@displaylevels = tiledLyr.displayLevels.join();
            }
            if (tiledLyr.showInLegend && tiledLyr.showInLegendHiddenLayers)
            {
                lyrXML.@showinlegendhiddenlayers = tiledLyr.showInLegendHiddenLayers.join();
            }
            else
            {
                lyrXML.@showinlegend = tiledLyr.showInLegend;
            }
        }
        else if (layer is CSVLayer)
        {
            var csvLyr:CSVLayer = layer as CSVLayer;
            lyrXML = <layer label={label}
                    type="csv"
                    visible={csvLyr.visible}
                    minscale={csvLyr.minScale}
                    maxscale={csvLyr.maxScale}
                    showinlegend={csvLyr.showInLegend}
                    alpha={csvLyr.alpha}
                    url={csvLyr.url}
                    longitudefieldname={csvLyr.longitudeFieldName}
                    latitudefieldname={csvLyr.latitudeFieldName}/>;
            if (csvLyr.columnDelimiter != ",")
            {
                lyrXML.@columndelimiter = csvLyr.columnDelimiter;
            }
            if (csvLyr.sourceFields)
            {
                var fields:Array = [];
                for each (var field:Field in csvLyr.sourceFields)
                {
                    fields.push(field.name + "|" + field.alias + "|" + field.type);
                }
                lyrXML.@sourcefields = fields.join();
            }
        }
        else if (layer is FeatureLayer && !(layer is CSVLayer))
        {
            var feaLyr:FeatureLayer = layer as FeatureLayer;
            if (feaLyr.featureCollection)
            {
                lyrXML = <layer label={label}
                        type="feature"
                        showinlegend={feaLyr.showInLegend}
                        visible={feaLyr.visible}
                        minscale={feaLyr.minScale}
                        maxscale={feaLyr.maxScale}
                        alpha={feaLyr.alpha}
                        iseditable={feaLyr.isEditable}/>
            }
            else
            {
                lyrXML = <layer label={label}
                        type="feature"
                        showinlegend={feaLyr.showInLegend}
                        visible={feaLyr.visible}
                        minscale={feaLyr.minScale}
                        maxscale={feaLyr.maxScale}
                        alpha={feaLyr.alpha}
                        mode={feaLyr.mode}
                        useproxy={feaLyr.proxyURL != null}
                        url={feaLyr.url}
                        iseditable={feaLyr.isEditable}/>;
            }
        }
        else if (layer is GeoRSSLayer)
        {
            var geoRSSLayer:GeoRSSLayer = layer as GeoRSSLayer;
            lyrXML = <layer label={label}
                    type="georss"
                    showinlegend={geoRSSLayer.showInLegend}
                    visible={geoRSSLayer.visible}
                    minscale={geoRSSLayer.minScale}
                    maxscale={geoRSSLayer.maxScale}
                    alpha={geoRSSLayer.alpha}
                    url={geoRSSLayer.url}/>;
        }
        else if (layer is KMLLayer)
        {
            var kmlLayer:KMLLayer = layer as KMLLayer;
            lyrXML = <layer label={label}
                    type="kml"
                    showinlegend={kmlLayer.showInLegend}
                    visible={kmlLayer.visible}
                    minscale={kmlLayer.minScale}
                    maxscale={kmlLayer.maxScale}
                    alpha={kmlLayer.alpha}
                    url={kmlLayer.url}/>;
        }
        else if (layer is OpenStreetMapLayer)
        {
            var osmLyr:OpenStreetMapLayer = layer as OpenStreetMapLayer;
            lyrXML = <layer label={label}
                    type="osm"
                    visible={osmLyr.visible}
                    minscale={osmLyr.minScale}
                    maxscale={osmLyr.maxScale}
                    showinlegend={osmLyr.showInLegend}
                    alpha={osmLyr.alpha}/>;
        }
        else if (layer is VETiledLayer)
        {
            var veLyr:VETiledLayer = layer as VETiledLayer;
            lyrXML = <layer label={label}
                    type="bing"
                    showinlegend={veLyr.showInLegend}
                    visible={veLyr.visible}
                    minscale={veLyr.minScale}
                    maxscale={veLyr.maxScale}
                    alpha={veLyr.alpha}
                    style={veLyr.mapStyle}/>;
        }
        else if (layer is WebTiledLayer)
        {
            var webTiledLayer:WebTiledLayer = layer as WebTiledLayer;
            lyrXML = <layer label={label}
                    type="webtiled"
                    showinlegend={webTiledLayer.showInLegend}
                    visible={webTiledLayer.visible}
                    minscale={webTiledLayer.minScale}
                    maxscale={webTiledLayer.maxScale}
                    alpha={webTiledLayer.alpha}
                    url={webTiledLayer.urlTemplate}/>;
            if (webTiledLayer.copyright)
            {
                lyrXML.@copyright = webTiledLayer.copyright;
            }
            if (webTiledLayer.subDomains)
            {
                lyrXML.@subdomains = webTiledLayer.subDomains.join();
            }
        }
        else if (layer is WMSLayer)
        {
            var wmsLayer:WMSLayer = layer as WMSLayer;
            lyrXML = <layer label={label}
                    type="wms"
                    showinlegend={wmsLayer.showInLegend}
                    visible={wmsLayer.visible}
                    minscale={wmsLayer.minScale}
                    maxscale={wmsLayer.maxScale}
                    alpha={wmsLayer.alpha}
                    version={wmsLayer.version}
                    skipgetcapabilities={wmsLayer.skipGetCapabilities}
                    imageformat={wmsLayer.imageFormat}
                    url={wmsLayer.url}/>;
            if (wmsLayer.copyright)
            {
                lyrXML.@copyright = wmsLayer.copyright;
            }
            if (wmsLayer.maxImageHeight > 0)
            {
                lyrXML.@maximageheight = wmsLayer.maxImageHeight;
            }
            if (wmsLayer.maxImageWidth > 0)
            {
                lyrXML.@maximagewidth = wmsLayer.maxImageWidth;
            }
            if (wmsLayer.visibleLayers)
            {
                lyrXML.@visiblelayers = wmsLayer.visibleLayers.toArray().join();
            }
        }

        return lyrXML;
    }

    private function appendPortalBasemaps(portalURL:String):void
    {
        var basemapAppender:PortalBasemapAppender = new PortalBasemapAppender(portalURL, configData);
        basemapAppender.addEventListener(Event.COMPLETE, basemapAppender_completeHandler);
        basemapAppender.fetchAndAppendPortalBasemaps();
    }

    protected function basemapAppender_completeHandler(event:Event):void
    {
        (event.currentTarget as PortalBasemapAppender).removeEventListener(Event.COMPLETE, basemapAppender_completeHandler);
        AppEvent.dispatch(AppEvent.CONFIG_LOADED, configData);
    }
}
}
