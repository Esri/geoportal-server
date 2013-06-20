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
package com.esri.viewer.utils
{

import com.esri.ags.components.IdentityManager;
import com.esri.ags.events.PortalEvent;
import com.esri.ags.portal.Portal;
import com.esri.ags.portal.supportClasses.PortalGroup;
import com.esri.ags.portal.supportClasses.PortalItem;
import com.esri.ags.portal.supportClasses.PortalQueryParameters;
import com.esri.ags.portal.supportClasses.PortalQueryResult;
import com.esri.ags.tasks.JSONTask;
import com.esri.viewer.AppEvent;
import com.esri.viewer.ConfigData;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.net.URLVariables;
import flash.utils.Dictionary;

import mx.resources.ResourceManager;
import mx.rpc.AsyncResponder;
import mx.rpc.Fault;
import mx.rpc.events.FaultEvent;
import mx.utils.ObjectUtil;

public class PortalBasemapAppender extends EventDispatcher
{
    private const PORTAL_BASEMAP_APPENDER:String = "PortalBasemapAppender";

    private var configData:ConfigData;
    private var portalURL:String;

    private var portalItemOrder:Array;
    private var portalItemToLabel:Dictionary;
    private var processedArcGISBasemaps:Array;
    private var totalBasemaps:int;
    private var totalPossibleArcGISBasemaps:int;

    private var comparableDefaultBasemapObjects:Array;
    private var defaultBasemapTitle:String;

    private var cultureCode:String;

    public function PortalBasemapAppender(portalURL:String, configData:ConfigData)
    {
        this.portalURL = portalURL;
        this.configData = configData;
    }

    public function fetchAndAppendPortalBasemaps():void
    {
        const idMgrEnabled:Boolean = IdentityManager.instance.enabled;
        var portal:Portal = new Portal();
        // the Portal constructor enables the IdentityManager so restore it back to what it was
        IdentityManager.instance.enabled = idMgrEnabled;

        portal.addEventListener(PortalEvent.LOAD, portal_loadHandler);
        portal.addEventListener(FaultEvent.FAULT, portal_faultHandler);

        cultureCode = toCultureCode(ResourceManager.getInstance().localeChain[0]);
        portal.load(portalURL, cultureCode);
    }

    protected function portal_loadHandler(event:PortalEvent):void
    {
        var portal:Portal = event.target as Portal;
        portal.removeEventListener(PortalEvent.LOAD, portal_loadHandler);
        portal.removeEventListener(FaultEvent.FAULT, portal_faultHandler);

        comparableDefaultBasemapObjects = getComparableBasemapObjects(portal.info.defaultBasemap);
        var queryParams:PortalQueryParameters = PortalQueryParameters.forQuery(portal.info.basemapGalleryGroupQuery);
        portal.queryGroups(queryParams, new AsyncResponder(portal_queryGroupsResultHandler, portal_queryGroupsFaultHandler, portal));
    }

    protected function portal_queryGroupsResultHandler(queryResult:PortalQueryResult, portal:Portal):void
    {
        if (queryResult.results.length > 0)
        {
            var portalGroup:PortalGroup = queryResult.results[0];
            var queryParams:PortalQueryParameters = PortalQueryParameters.forItemsInGroup(portalGroup.id).withLimit(50).withSortField("name");
            portal.queryItems(queryParams, new AsyncResponder(portal_queryItemsResultHandler, portal_queryItemsFaultHandler));
        }
        else
        {
            dispatchComplete();
        }
    }

    private function portal_queryItemsResultHandler(queryResult:PortalQueryResult, token:Object = null):void
    {
        const resultItems:Array = queryResult.results;
        totalPossibleArcGISBasemaps = resultItems.length;
        portalItemOrder = [];
        portalItemToLabel = new Dictionary(true);
        processedArcGISBasemaps = [];
        totalBasemaps = configData.basemaps.length;
        for each (var item:PortalItem in resultItems)
        {
            processPortalItem(item);
        }
    }

    private function processPortalItem(item:PortalItem):void
    {
        if (item.type == PortalItem.TYPE_WEB_MAP)
        {
            portalItemOrder.push(item);
            processWebMapPortalItem(item);
        }
        else if (item.type == PortalItem.TYPE_MAP_SERVICE)
        {
            portalItemOrder.push(item);
            processMapServicePortalItem(item);
        }
        else
        {
            updateTotalArcGISBasemaps();
        }
    }

    private function processWebMapPortalItem(item:PortalItem):void
    {
        item.getJSONData(new AsyncResponder(item_getJSONDataResultHandler,
                                            item_getJSONDataFaultHandler,
                                            item));
    }

    private function item_getJSONDataResultHandler(itemData:Object, item:PortalItem):void
    {
        createBasemapLayerObjectFromWebMapItemAndData(item, itemData);
        if (isDefaultBasemap(itemData.baseMap))
        {
            defaultBasemapTitle = itemData.baseMap.title;
        }
        updateTotalArcGISBasemaps();
    }

    private function createBasemapLayerObjectFromWebMapItemAndData(item:PortalItem, itemData:Object):void
    {
        if (!itemData)
        {
            return;
        }

        var basemapObject:Object = itemData.baseMap;
        var basemapLayerObjects:Array = basemapObject.baseMapLayers;
        if (!(basemapObject && basemapLayerObjects))
        {
            return;
        }

        var title:String = basemapObject.title;
        var iconURL:String = item.thumbnailURL;
        var existingBasemapLayerObject:Object = findBasemapLayerObjectById(title);
        if (existingBasemapLayerObject)
        {
            existingBasemapLayerObject.icon = iconURL;
            return;
        }

        portalItemToLabel[item] = title;
        var basemapLayerObject:Object = basemapLayerObjects[0];
        addBasemapLayerObject(baseMapLayerObjectToLayerXML(title,
                                                           basemapLayerObject,
                                                           iconURL));

        var totalBaseMapLayers:int = basemapLayerObjects.length;
        if (totalBaseMapLayers > 1)
        {
            basemapLayerObject = basemapLayerObjects[1];
            addBasemapLayerObject(baseMapLayerObjectToLayerXML(title,
                                                               basemapLayerObject,
                                                               iconURL));
        }
    }

    private function isDefaultBasemap(basemapObject:Object):Boolean
    {
        var comparableBasemapObjects:Array = getComparableBasemapObjects(basemapObject);

        return (ObjectUtil.compare(comparableBasemapObjects, comparableDefaultBasemapObjects) == 0);
    }

    private function getComparableBasemapObjects(basemapObject:Object):Array
    {
        var basemapLayerObjects:Array = basemapObject.baseMapLayers;
        var comparableBasemapObjects:Array = [];
        var comparableBasemapLayerObject:Object;

        for each (var basemapLayerObject:Object in basemapLayerObjects)
        {
            comparableBasemapLayerObject = {};

            if (basemapLayerObject.url)
            {
                comparableBasemapLayerObject.url = basemapLayerObject.url;
            }
            if (basemapLayerObject.type)
            {
                comparableBasemapLayerObject.type = basemapLayerObject.type;
            }

            comparableBasemapObjects.push(comparableBasemapLayerObject);
        }

        return comparableBasemapObjects;
    }

    private function findBasemapLayerObjectById(id:String):Object
    {
        var layerObjectResult:Object;

        var basemapLayerObjects:Array = configData.basemaps;
        for each (var layerObject:Object in basemapLayerObjects)
        {
            if (layerObject.layer && (layerObject.layer.id == id))
            {
                layerObjectResult = layerObject;
                break;
            }
        }

        return layerObjectResult;
    }

    private function updateTotalArcGISBasemaps():void
    {
        totalPossibleArcGISBasemaps--;
        if (totalPossibleArcGISBasemaps == 0)
        {
            addArcGISBasemapsToConfig();
            dispatchComplete();
        }
    }

    private function dispatchComplete():void
    {
        dispatchEvent(new Event(Event.COMPLETE));
    }

    private function addArcGISBasemapsToConfig():void
    {
        var hasBasemaps:Boolean = (configData.basemaps.length > 0);

        if (!hasBasemaps)
        {
            if (defaultBasemapTitle)
            {
                setDefaultBasemapVisible();
            }
            else
            {
                setFirstBasemapVisible();
            }
        }

        addBasemapsInOrder();
    }

    private function setDefaultBasemapVisible():void
    {
        for each (var layerObject:Object in processedArcGISBasemaps)
        {
            if (defaultBasemapTitle == layerObject.label)
            {
                layerObject.visible = true;
            }
        }
    }

    private function setFirstBasemapVisible():void
    {
        if (!portalItemOrder || portalItemOrder.length == 0)
        {
            return;
        }

        var firstBasemapLabel:String = portalItemToLabel[portalItemOrder[0]];
        for each (var layerObject:Object in processedArcGISBasemaps)
        {
            if (layerObject.label == firstBasemapLabel)
            {
                layerObject.visible = true;
            }
        }
    }

    private function addBasemapsInOrder():void
    {
        for each (var portalItem:PortalItem in portalItemOrder)
        {
            for each (var layerObject:Object in processedArcGISBasemaps)
            {
                if (layerObject.label == portalItemToLabel[portalItem])
                {
                    configData.basemaps.push(layerObject);
                }
            }
        }
    }

    private function addBasemapLayerObject(layerXML:XML):void
    {
        if (layerXML)
        {
            processedArcGISBasemaps.push(LayerObjectUtil.getLayerObject(layerXML,
                                                                        totalBasemaps++,
                                                                        false,
                                                                        configData.bingKey));
        }
    }

    private function baseMapLayerObjectToLayerXML(title:String, basemapLayerObject:Object, iconURL:String = null):XML
    {
        var layerXML:XML;
        const url:String = basemapLayerObject.url;
        const type:String = basemapLayerObject.type;

        if (url)
        {
            layerXML = createLayerXML(title, "tiled", iconURL, url, basemapLayerObject.opacity, false);
        }
        else if (isNonEsriType(type))
        {
            layerXML = createNonEsriLayerXML(title, iconURL, basemapLayerObject, false, type);
        }

        return layerXML;
    }

    private function createLayerXML(title:String, type:String, iconURL:String, url:String, alpha:Number, visible:Boolean):XML
    {
        return <layer label={title}
                type={type}
                icon={iconURL}
                url={url}
                alpha={alpha}
                visible={visible}/>;
    }

    private function isNonEsriType(type:String):Boolean
    {
        return type == "OpenStreetMap" ||
            (isBingBasemap(type) && hasBingKey());
    }

    private function createNonEsriLayerXML(title:String, iconURL:String, basemapLayerObject:Object, visible:Boolean, type:String):XML
    {
        var layerXML:XML = <layer label={title}
                icon={iconURL}
                type={toViewerNonEsriLayerType(basemapLayerObject.type)}
                alpha={basemapLayerObject.opacity}
                visible={visible}/>;

        if (isBingBasemap(type))
        {
            layerXML.@style = mapBingStyleFromBasemapType(type);
            layerXML.@culture = cultureCode;
        }

        return layerXML;
    }

    private function toViewerNonEsriLayerType(type:String):String
    {
        var viewerType:String;
        if (type == "OpenStreetMap")
        {
            viewerType = "osm";
        }
        else if (isBingBasemap(type))
        {
            viewerType = "bing";
        }

        return viewerType;
    }

    private function isBingBasemap(type:String):Boolean
    {
        return type && type.indexOf('BingMaps') > -1;
    }

    private function hasBingKey():Boolean
    {
        var bingKey:String = configData.bingKey;
        return (bingKey != null && bingKey.length > 0);
    }

    private function mapBingStyleFromBasemapType(type:String):String
    {
        if (type == 'BingMapsAerial')
        {
            return 'aerial';
        }
        else if (type == 'BingMapsHybrid')
        {
            return 'aerialWithLabels';
        }
        else
        {
            //default - BingMapsRoad
            return 'road';
        }
    }

    private function item_getJSONDataFaultHandler(fault:Fault, token:Object = null):void
    {
        AppEvent.dispatch(AppEvent.APP_ERROR,
                          LocalizationUtil.getDefaultString("couldNotFetchBasemapData",
                                                            fault.faultString));
        updateTotalArcGISBasemaps();
    }

    private function processMapServicePortalItem(item:PortalItem):void
    {
        const urlVars:URLVariables = new URLVariables();
        urlVars.f = "json";
        var mapServiceMetadataRequest:JSONTask = new JSONTask(item.url);
        mapServiceMetadataRequest.execute(
            urlVars, new AsyncResponder(mapServiceRequest_resultHandler,
                                        mapServiceRequest_faultHandler,
                                        item));
    }

    private function mapServiceRequest_resultHandler(serviceMetadata:Object, item:PortalItem):void
    {
        createBasemapLayerObjectFromMapServiceItemAndData(item, serviceMetadata);
        updateTotalArcGISBasemaps();
    }

    private function createBasemapLayerObjectFromMapServiceItemAndData(item:PortalItem, serviceMetadata:Object):void
    {
        if (!serviceMetadata)
        {
            return;
        }

        var layerType:String = getLayerType(serviceMetadata, item);
        if (!layerType)
        {
            return;
        }

        var title:String = item.title;
        var iconURL:String = item.thumbnailURL;
        var existingBasemapLayerObject:Object = findBasemapLayerObjectById(title);
        if (existingBasemapLayerObject)
        {
            existingBasemapLayerObject.icon = iconURL;
            return;
        }

        portalItemToLabel[item] = title;
        addBasemapLayerObject(mapServicePortalItemToLayerXML(item, layerType));
    }

    private function getLayerType(serviceMetadata:Object, item:PortalItem):String
    {
        var layerType:String;

        if (serviceMetadata.singleFusedMapCache)
        {
            layerType = "tiled";
        }
        else if (serviceMetadata.bandCount)
        {
            layerType = "image";
        }
        else if (isNaN(Number(item.url.charAt(item.url.length - 1))))
        {
            layerType = "dynamic";
        }
        else
        {
            layerType = "feature";
        }

        return layerType;
    }

    private function mapServicePortalItemToLayerXML(item:PortalItem, type:String):XML
    {
        const title:String = item.title;
        const iconURL:String = item.thumbnailURL;
        const url:String = item.url;
        return createLayerXML(title, type, iconURL, url, 1, false);
    }

    private function mapServiceRequest_faultHandler(fault:Fault, token:Object = null):void
    {
        if (fault.faultString != "Sign in aborted")
        {
            AppEvent.dispatch(AppEvent.APP_ERROR,
                              LocalizationUtil.getDefaultString("couldNotFetchBasemapData",
                                                                fault.faultString));
        }

        updateTotalArcGISBasemaps();
    }

    private function portal_queryGroupsFaultHandler(fault:Fault, token:Object = null):void
    {
        AppEvent.showError(LocalizationUtil.getDefaultString("couldNotQueryPortal"), PORTAL_BASEMAP_APPENDER);
        dispatchComplete();
    }

    private function portal_queryItemsFaultHandler(fault:Fault, token:Object = null):void
    {
        AppEvent.showError(LocalizationUtil.getDefaultString("couldNotQueryPortalItems"), PORTAL_BASEMAP_APPENDER);
        dispatchComplete();
    }

    private function portal_faultHandler(event:FaultEvent):void
    {
        var portal:Portal = event.target as Portal;
        portal.removeEventListener(PortalEvent.LOAD, portal_loadHandler);
        portal.removeEventListener(FaultEvent.FAULT, portal_faultHandler);

        AppEvent.showError(LocalizationUtil.getDefaultString("couldNotConnectToPortal"), PORTAL_BASEMAP_APPENDER);
        dispatchComplete();
    }

    private function toCultureCode(locale:String):String
    {
        return locale ? locale.replace('_', '-') : locale;
    }
}

}
