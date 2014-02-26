///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2012 Esri. All Rights Reserved.
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
package com.esri.viewer.utils
{

import com.esri.ags.clusterers.ESRIClusterer;
import com.esri.ags.layers.Layer;
import com.esri.ags.renderers.IRenderer;
import com.esri.ags.symbols.Symbol;

public class LayerObjectUtil
{
    public static function getLayerObject(obj:XML, num:Number, isOpLayer:Boolean, bingKey:String, layer:Layer = null, proxyUrl:String = null):Object
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

        // wms
        var wkid:String;
        if (obj.@wkid[0])
        {
            wkid = obj.@wkid;
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

        var maxAllowableOffset:Number;
        if (obj.@maxallowableoffset[0])
        {
            if (!isNaN(parseFloat(obj.@maxallowableoffset)))
            {
                maxAllowableOffset = parseFloat(obj.@maxallowableoffset);
            }
        }

        var minScale:Number;
        if (obj.@minscale[0])
        {
            if (!isNaN(parseFloat(obj.@minscale)))
            {
                minScale = parseFloat(obj.@minscale);
            }
        }

        var maxScale:Number;
        if (obj.@maxscale[0])
        {
            if (!isNaN(parseFloat(obj.@maxscale)))
            {
                maxScale = parseFloat(obj.@maxscale);
            }
        }

        var maxImageWidth:Number;
        if (obj.@maximagewidth[0])
        {
            if (!isNaN(parseInt(obj.@maximagewidth)))
            {
                maxImageWidth = parseInt(obj.@maximagewidth);
            }
        }

        var maxImageHeight:Number;
        if (obj.@maximageheight[0])
        {
            if (!isNaN(parseInt(obj.@maximageheight)))
            {
                maxImageHeight = parseInt(obj.@maximageheight);
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

        var copyright:String = obj.@copyright[0];
        var clustererParser:ClustererParser = new ClustererParser();
        var clusterer:ESRIClusterer = clustererParser.parseClusterer(obj.clustering[0]);
        var rendererParser:RendererParser = new RendererParser();
        var renderer:IRenderer = rendererParser.parseRenderer(obj);
        var useProxy:Boolean = obj.@useproxy[0] && obj.@useproxy == "true"; // default false
        var useMapTime:Boolean = obj.@usemaptime[0] ? obj.@usemaptime == "true" : true; // default true
        var useAMF:String = obj.@useamf[0] ? obj.@useamf : "";
        var token:String = obj.@token[0] ? obj.@token : "";
        var mode:String = obj.@mode[0] ? obj.@mode : "";
        var icon:String = isSupportedImageType(obj.@icon[0]) ? obj.@icon : 'assets/images/defaultBasemapIcon.png';
        var layerId:String = obj.@layerid[0];
        var imageFormat:String = obj.@imageformat;
        var noData:String = obj.@nodata[0];
        var visibleLayers:String = obj.@visiblelayers;
        var displayLevels:String = obj.@displaylevels;
        var bandIds:String = obj.@bandids;
        var skipGetCapabilities:String = obj.@skipgetcapabilities[0];
        var version:String = obj.@version[0];
        var url:String = obj.@url;
        var portalURL:String = obj.@portalurl[0];
        var serviceURL:String = obj.@serviceurl[0];
        var serviceMode:String = obj.@servicemode[0];
        var tileMatrixSetId:String = obj.@tilematrixsetid[0];
        var username:String = obj.@username;
        var password:String = obj.@password;
        var disableClientCaching:Boolean = obj.@disableclientcaching[0] && obj.@disableclientcaching == "true"; // default false

        // ve tiled layer or wmts layer
        var style:String = obj.@style[0] ? obj.@style : "";
        var key:String;
        if (bingKey)
        {
            key = bingKey;
        }
        else
        {
            key = obj.@key[0] ? obj.@key : "";
        }
        var culture:String = obj.@culture[0] ? obj.@culture : "";

        var showInLegend:Boolean = obj.@showinlegend[0] != "false";

        var showInLegendHiddenLayers:String = obj.@showinlegendhiddenlayers;

        // arcims layer
        var serviceHost:String = obj.@servicehost[0] ? obj.@servicehost : "";
        var serviceName:String = obj.@servicename[0] ? obj.@servicename : "";

        // definitionExpression for featurelayer
        var definitionExpression:String = obj.@definitionexpression[0] ? obj.@definitionexpression : "";
        var gdbVersion:String = obj.@gdbversion[0];
        // isEditable for feature layer
        var isEditable:Boolean = true;
        if (obj.@iseditable[0])
        {
            isEditable = obj.iseditable == "true";
        }

        //sublayers
        var subLayers:Array = [];
        if (type == "tiled" || type == "dynamic")
        {
            var subLayersList:XMLList = obj.sublayer;
            for (var i:int = 0; i < subLayersList.length(); i++)
            {
                subLayers.push({ id: String(subLayersList[i].@id), info: subLayersList[i].@info, infoConfig: subLayersList[i].@infoconfig, popUpConfig: subLayersList[i].@popupconfig, definitionExpression: String(subLayersList[i].@definitionexpression)});
            }
        }

        //csv layer
        var latitudeFieldName:String = obj.@latitudefieldname;
        var longitudeFieldName:String = obj.@longitudefieldname;
        var sourceFields:String = obj.@sourcefields;
        var columnDelimiter:String = obj.@columndelimiter;

        //web tiled layer
        var subDomains:String = obj.@subdomains[0];

        var symbolParser:SymbolParser = new SymbolParser();

        var markerSymbol:Symbol;
        if (obj.simplemarkersymbol[0])
        {
            markerSymbol = symbolParser.parseSimpleMarkerSymbol(obj.simplemarkersymbol[0]);
        }
        else if (obj.picturemarkersymbol[0])
        {
            markerSymbol = symbolParser.parsePictureMarkerSymbol(obj.picturemarkersymbol[0]);
        }

        var lineSymbol:Symbol;
        if (obj.simplelinesymbol[0])
        {
            lineSymbol = symbolParser.parseSimpleLineSymbol(obj.simplelinesymbol[0]);
        }

        var fillSymbol:Symbol;
        if (obj.simplefillsymbol[0])
        {
            fillSymbol = symbolParser.parseSimpleFillSymbol(obj.simplefillsymbol[0]);
        }

        var resultObject:Object =
            {
                id: String(num),
                alpha: alpha,
                bandIds: bandIds,
                autoRefresh: autoRefresh,
                columnDelimiter: columnDelimiter,
                copyright: copyright,
                culture: culture,
                clusterer: clusterer,
                definitionExpression: definitionExpression,
                disableClientCaching: disableClientCaching,
                displayLevels: displayLevels,
                fillSymbol: fillSymbol,
                gdbVersion: gdbVersion,
                icon: icon,
                imageFormat: imageFormat,
                isEditable: isEditable,
                key: key,
                label: label,
                layerId: layerId,
                latitudeFieldName: latitudeFieldName,
                lineSymbol: lineSymbol,
                longitudeFieldName: longitudeFieldName,
                markerSymbol: markerSymbol,
                maxAllowableOffset: maxAllowableOffset,
                maxImageHeight: maxImageHeight,
                maxImageWidth: maxImageWidth,
                minScale: minScale,
                maxScale: maxScale,
                mode: mode,
                noData: noData,
                password: password,
                portalURL: portalURL,
                proxyUrl: proxyUrl,
                renderer: renderer,
                serviceHost: serviceHost,
                serviceName: serviceName,
                serviceMode: serviceMode,
                serviceURL: serviceURL,
                showInLegend: showInLegend,
                showInLegendHiddenLayers: showInLegendHiddenLayers,
                skipGetCapabilities: skipGetCapabilities,
                sourceFields: sourceFields,
                style: style,
                subDomains: subDomains,
                subLayers: subLayers,
                tileMatrixSetId: tileMatrixSetId,
                token: token,
                type: type,
                url: url,
                useAMF: useAMF,
                useMapTime: useMapTime,
                useProxy: useProxy,
                username: username,
                version: version,
                visible: visible,
                visibleLayers: visibleLayers,
                wkid: wkid
            };

        // look for info, infoconfig and popupconfig on basemaps and operational layers
        var opLayerInfo:String = obj.@info;
        var opLayerInfoConfig:String = obj.@infoconfig;
        var opLayerPopUpConfig:String = obj.@popupconfig;
        resultObject.popUpConfig = opLayerPopUpConfig;
        resultObject.infoConfig = opLayerInfoConfig;
        resultObject.infoUrl = opLayerInfo;
        resultObject.layer = layer;
        if (!isOpLayer)
        {
            var reference:Boolean = obj.@reference[0] && obj.@reference == "true";
            resultObject.reference = reference;
        }

        return resultObject;
    }

    private static function isSupportedImageType(filePath:String):Boolean
    {
        var fp:String = filePath;
        if (!fp)
        {
            return false;
        }
        var tokenIndex:int = fp.indexOf("?");
        var hasToken:Boolean = (tokenIndex > -1);
        fp = hasToken ? fp.substr(0, tokenIndex) : fp;
        var endsWithSupportedImageFileType:RegExp = /\.(png|gif|jpg)$/i;
        return endsWithSupportedImageFileType.test(fp);
    }
}
}
