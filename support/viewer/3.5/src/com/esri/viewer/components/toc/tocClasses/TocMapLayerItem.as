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
package com.esri.viewer.components.toc.tocClasses
{

import com.esri.ags.Map;
import com.esri.ags.events.ExtentEvent;
import com.esri.ags.events.LayerEvent;
import com.esri.ags.layers.ArcGISDynamicMapServiceLayer;
import com.esri.ags.layers.ArcGISTiledMapServiceLayer;
import com.esri.ags.layers.ArcIMSMapServiceLayer;
import com.esri.ags.layers.ILegendSupport;
import com.esri.ags.layers.KMLLayer;
import com.esri.ags.layers.Layer;
import com.esri.ags.layers.WMSLayer;
import com.esri.ags.layers.supportClasses.KMLFeatureInfo;
import com.esri.ags.layers.supportClasses.KMLFolder;
import com.esri.ags.layers.supportClasses.LayerInfo;
import com.esri.ags.layers.supportClasses.LayerLegendInfo;
import com.esri.ags.layers.supportClasses.LegendItemInfo;
import com.esri.ags.layers.supportClasses.WMSLayerInfo;
import com.esri.viewer.ViewerContainer;
import com.esri.viewer.components.toc.utils.MapUtil;
import com.esri.viewer.utils.MapServiceUtil;

import flash.events.Event;

import mx.binding.utils.ChangeWatcher;
import mx.collections.ArrayCollection;
import mx.collections.ArrayList;
import mx.events.CollectionEvent;
import mx.events.FlexEvent;
import mx.rpc.AsyncResponder;
import mx.rpc.Fault;

/**
 * A TOC item representing a map service or graphics layer.
 *
 * @private
 */
public class TocMapLayerItem extends TocItem
{
    //--------------------------------------------------------------------------
    //
    //  Variables
    //
    //--------------------------------------------------------------------------

    private var _isMSOnly:Boolean = false;
    private var _isVisibleLayersSet:Boolean = false;
    private var _layer:Layer;
    private var _includeLegendItems:Boolean = false;
    private var _layerLegendInfos:Array;
    private var _labelFunction:Function;
    private var _visibleLayersChangeWatcher:ChangeWatcher;
    private var _dynamicMapServiceLayerInfos:Array;

    //--------------------------------------------------------------------------
    //
    //  Constructor
    //
    //--------------------------------------------------------------------------

    /**
     * Creates a new TocMapLayerItem
     */
    public function TocMapLayerItem(layer:Layer, labelFunction:Function = null, isMapServiceOnly:Boolean = false, includeLegendItems:Boolean = false)
    {
        super();

        _layer = layer;
        _isMSOnly = isMapServiceOnly;
        _includeLegendItems = includeLegendItems;
        // Set the initial visibility without causing a layer refresh
        setVisible(layer.visible, false);
        // Indicate whether the item is in scale range without causing a layer refresh
        setIsInScaleRange(layer.isInScaleRange, false);

        // check if the visiblelayers was set on the dynamic map servicelayer
        var opLayers:Array = ViewerContainer.getInstance().configData.opLayers;
        for (var i:int = 0; i < opLayers.length; )
        {
            if (layer is ArcGISDynamicMapServiceLayer && (layer.id == opLayers[i].label) && opLayers[i].visibleLayers)
            {
                _isVisibleLayersSet = true;
                break;
            }
            else
            {
                i++;
            }
        }

        if (labelFunction == null)
        {
            // Default label function
            labelFunction = MapUtil.labelLayer;
        }
        _labelFunction = labelFunction;
        label = labelFunction(layer);

        if (!isMapServiceOnly)
        {
            if (layer.loaded)
            {
                // Process the layer info immediately
                layer.map.addEventListener(ExtentEvent.EXTENT_CHANGE, onExtentChange, false, 0, true);
                if (layer is ILegendSupport && _includeLegendItems)
                {
                    ILegendSupport(layer).getLegendInfos(new AsyncResponder(getLegendResult, getLegendFault));
                }
                else
                {
                    createChildren();
                }
            }
        }

        // Listen for future layer load events
        layer.addEventListener(LayerEvent.LOAD, onLayerLoad, false, 0, true);

        // Listen for changes in layer visibility
        layer.addEventListener(FlexEvent.SHOW, onLayerShow, false, 0, true);
        layer.addEventListener(FlexEvent.HIDE, onLayerHide, false, 0, true);
        layer.addEventListener(LayerEvent.IS_IN_SCALE_RANGE_CHANGE, onScaleChange, false, 0, true);
    }

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //  layer
    //--------------------------------------------------------------------------

    /**
     * The map layer to which this TOC item is attached.
     */
    public function get layer():Layer
    {
        return _layer;
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden Methods
    //
    //--------------------------------------------------------------------------

    /**
     * @private
     */
    override internal function updateIndeterminateState(calledFromChild:Boolean = false):void
    {
        indeterminate = DEFAULT_INDETERMINATE;

        // Recurse up the tree
        if (parent)
        {
            parent.updateIndeterminateState(true);
        }
    }

    /**
     * @private
     */
    override internal function refreshLayer():void
    {
        layer.visible = visible;

        // ArcIMS requires layer names, whereas ArcGIS Server requires layer IDs
        var useLayerInfoName:Boolean = (layer is ArcIMSMapServiceLayer);

        var visLayers:Array = [];
        for each (var child:TocItem in children)
        {
            accumVisibleLayers(child, visLayers, useLayerInfoName);
        }

        if (layer is ArcGISDynamicMapServiceLayer)
        {
            if (ArcGISDynamicMapServiceLayer(layer).visibleLayers)
            {
                ArcGISDynamicMapServiceLayer(layer).visibleLayers.removeEventListener(CollectionEvent.COLLECTION_CHANGE, visibleLayersChangeHandler);
            }
            ArcGISDynamicMapServiceLayer(layer).visibleLayers = new ArrayCollection(visLayers);
            ArcGISDynamicMapServiceLayer(layer).visibleLayers.addEventListener(CollectionEvent.COLLECTION_CHANGE, visibleLayersChangeHandler);
        }
        else if (layer is ArcIMSMapServiceLayer)
        {
            ArcIMSMapServiceLayer(layer).visibleLayers = new ArrayCollection(visLayers);
        }
    }

    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------

    private function visibleLayersChange(event:Event = null):void
    {
        var dynamicMapServiceLayer:ArcGISDynamicMapServiceLayer = ArcGISDynamicMapServiceLayer(event.target);
        var actualVisibleLayers:Array;
        if (dynamicMapServiceLayer.visibleLayers)
        {
            dynamicMapServiceLayer.visibleLayers.addEventListener(CollectionEvent.COLLECTION_CHANGE, visibleLayersChangeHandler);
            actualVisibleLayers = getActualVisibleLayers(dynamicMapServiceLayer.visibleLayers.toArray(), _dynamicMapServiceLayerInfos);
        }
        else
        {
            actualVisibleLayers = getActualVisibleLayers(MapServiceUtil.getVisibleSubLayers(_dynamicMapServiceLayerInfos), _dynamicMapServiceLayerInfos);
        }
        for each (var child:TocLayerInfoItem in children)
        {
            updateTOCItemVisibility(child, actualVisibleLayers);
        }
    }

    private function visibleLayersChangeHandler(event:CollectionEvent):void
    {
        var actualVisibleLayers:Array = getActualVisibleLayers(ArcGISDynamicMapServiceLayer(layer).visibleLayers.toArray(), _dynamicMapServiceLayerInfos);
        for each (var child:TocLayerInfoItem in children)
        {
            updateTOCItemVisibility(child, actualVisibleLayers);
        }
    }

    private function updateTOCItemVisibility(item:TocLayerInfoItem, actualVisibleLayers:Array):void
    {
        if (item.isGroupLayer())
        {
            if (item.visible)
            {
                for each (var child:TocLayerInfoItem in item.children)
                {
                    updateTOCItemVisibility(child, actualVisibleLayers);
                }
            }
        }
        else
        {
            item.visible = actualVisibleLayers.indexOf(item.layerInfo.layerId) != -1;
        }
    }

    private function accumVisibleLayers(item:TocItem, accum:Array, useLayerInfoName:Boolean = false):void
    {
        if (item.isGroupLayer())
        {
            // Don't include group layer IDs/names in the visible layer list, since the ArcGIS REST API
            // implicitly turns on all child layers when the group layer is visible. This goes
            // counter to what most users have come to expect from apps, e.g. ArcMap.

            if (item.visible) // only accumulate for a visible group layers
            {
                for each (var child:TocItem in item.children)
                {
                    accumVisibleLayers(child, accum, useLayerInfoName);
                }
            }
        }
        else
        {
            // Leaf layer
            if (item.visible)
            {
                if (item is TocLayerInfoItem)
                {
                    var layer:TocLayerInfoItem = TocLayerInfoItem(item);
                    accum.push(useLayerInfoName ? layer.layerInfo.name : layer.layerInfo.layerId);
                }
            }
        }
    }

    private function getLegendResult(layerLegendInfos:Array, token:Object = null):void
    {
        _layerLegendInfos = [];
        getLayerLegendInfos(layerLegendInfos); // get all layerLegendInfos as these can be nested

        createChildren();
    }

    private function getLayerLegendInfos(arr:Array):void
    {
        for each (var layerLegendInfo:LayerLegendInfo in arr)
        {
            if (layerLegendInfo) // check if layerLegendInfo exists
            {
                _layerLegendInfos.push(layerLegendInfo);
                if (layerLegendInfo.layerLegendInfos)
                {
                    getLayerLegendInfos(layerLegendInfo.layerLegendInfos);
                }
            }
        }
    }

    private function getLegendFault(fault:Fault, token:Object = null):void
    {
        createChildren();
    }

    private function onLayerLoad(event:LayerEvent):void
    {
        // Relabel this item, since map layer URL or service name might have changed.
        label = _labelFunction(layer);
        if (!_isMSOnly)
        {
            layer.map.addEventListener(ExtentEvent.EXTENT_CHANGE, onExtentChange, false, 0, true);
            if (layer is ILegendSupport && _includeLegendItems)
            {
                ILegendSupport(layer).getLegendInfos(new AsyncResponder(getLegendResult, getLegendFault));
            }
            else
            {
                createChildren();
            }
        }
    }

    private function onLayerShow(event:FlexEvent):void
    {
        setVisible(layer.visible, true);
    }

    private function onLayerHide(event:FlexEvent):void
    {
        setVisible(layer.visible, true);
    }

    private function onScaleChange(event:LayerEvent):void
    {
        setIsInScaleRange(layer.isInScaleRange, true);
    }

    private function onExtentChange(event:ExtentEvent):void
    {
        if (layer is ArcGISDynamicMapServiceLayer || layer is ArcGISTiledMapServiceLayer)
        {
            for each (var tocItem:TocItem in children)
            {
                if (tocItem is TocLayerInfoItem)
                {
                    var tocLayerInfoItem:TocLayerInfoItem = tocItem as TocLayerInfoItem;
                    updateEnabledBasedOnScale(tocLayerInfoItem, isTocLayerInfoItemInScale(tocLayerInfoItem.layerInfo));
                }
            }
        }
    }

    private function updateEnabledBasedOnScale(tocLayerInfoItem:TocLayerInfoItem, isInScaleRange:Boolean):void
    {
        if (tocLayerInfoItem.children && tocLayerInfoItem.children.length)
        {
            tocLayerInfoItem.isInScaleRange = isInScaleRange;
            for each (var childTocItem:TocItem in tocLayerInfoItem.children.toArray())
            {
                if (childTocItem is TocLayerInfoItem)
                {
                    var childTocLayerInfoItem:TocLayerInfoItem = childTocItem as TocLayerInfoItem;
                    updateEnabledBasedOnScale(childTocLayerInfoItem, tocLayerInfoItem.isInScaleRange && isTocLayerInfoItemInScale(childTocLayerInfoItem.layerInfo));
                }
            }
        }
        else
        {
            tocLayerInfoItem.isInScaleRange = isInScaleRange;
        }
    }

    private function isTocLayerInfoItemInScale(layerInfo:LayerInfo):Boolean
    {
        var result:Boolean = true;
        var map:Map = layer.map;

        if (map && (layerInfo.maxScale > 0 || layerInfo.minScale > 0))
        {
            var scale:Number = map.scale;
            if (layerInfo.maxScale > 0 && layerInfo.minScale > 0)
            {
                result = layerInfo.maxScale <= Math.ceil(scale) && Math.floor(scale) <= layerInfo.minScale;
            }
            else if (layerInfo.maxScale > 0)
            {
                result = layerInfo.maxScale <= Math.ceil(scale);
            }
            else if (layerInfo.minScale > 0)
            {
                result = Math.floor(scale) <= layerInfo.minScale;
            }
        }
        return result;
    }

    /**
     * Populates this item's children collection based on any layer info
     * of the map service.
     */
    private function createChildren():void
    {
        children = null;
        var layerInfos:Array; // of LayerInfo
        var wmsLayerInfos:Array; // of WMSLayerInfo

        if (layer is ArcGISTiledMapServiceLayer)
        {
            layerInfos = ArcGISTiledMapServiceLayer(layer).layerInfos;
        }
        else if (layer is ArcGISDynamicMapServiceLayer)
        {
            var arcGISDynamicMapServiceLayer:ArcGISDynamicMapServiceLayer = ArcGISDynamicMapServiceLayer(layer);
            _dynamicMapServiceLayerInfos = arcGISDynamicMapServiceLayer.dynamicLayerInfos ? arcGISDynamicMapServiceLayer.dynamicLayerInfos : arcGISDynamicMapServiceLayer.layerInfos;
            if (!arcGISDynamicMapServiceLayer.visibleLayers)
            {
                var visLayers:Array = getActualVisibleLayers(MapServiceUtil.getVisibleSubLayers(_dynamicMapServiceLayerInfos), _dynamicMapServiceLayerInfos);
                arcGISDynamicMapServiceLayer.visibleLayers = new ArrayCollection(visLayers);
            }
            arcGISDynamicMapServiceLayer.visibleLayers.addEventListener(CollectionEvent.COLLECTION_CHANGE, visibleLayersChangeHandler);
            _visibleLayersChangeWatcher = ChangeWatcher.watch(arcGISDynamicMapServiceLayer, "visibleLayers", visibleLayersChange);
            if (_isVisibleLayersSet)
            {
                layerInfos = [];
                // get the actual visible layers
                var actualVisibleLayers:Array = getActualVisibleLayers(arcGISDynamicMapServiceLayer.visibleLayers.toArray(), _dynamicMapServiceLayerInfos);
                for each (var layerInfo:LayerInfo in _dynamicMapServiceLayerInfos)
                {
                    if (actualVisibleLayers.indexOf(layerInfo.layerId) != -1)
                    {
                        layerInfo.defaultVisibility = true;
                    }
                    else
                    {
                        layerInfo.defaultVisibility = false;
                    }
                    layerInfos.push(layerInfo);
                }
            }
            else
            {
                layerInfos = _dynamicMapServiceLayerInfos;
            }
        }
        else if (layer is ArcIMSMapServiceLayer)
        {
            layerInfos = ArcIMSMapServiceLayer(layer).layerInfos;
        }
        else if (layer is WMSLayer)
        {
            wmsLayerInfos = WMSLayer(layer).layerInfos;
        }
        else if (layer is KMLLayer)
        {
            createKMLLayerTocItems(this, KMLLayer(layer));
        }

        if (layerInfos)
        {
            var rootLayers:Array = findRootLayers(layerInfos);
            for each (var layerInfo1:LayerInfo in rootLayers)
            {
                addChild(createTocLayer(this, layerInfo1, layerInfos, layerInfo1.defaultVisibility, isTocLayerInfoItemInScale(layerInfo1)));
            }
        }
        else if (wmsLayerInfos)
        {   
            var wmsVisibleLayers:ArrayList = WMSLayer(layer).visibleLayers as ArrayList;             
            for each (var wmsLayerInfo:WMSLayerInfo in wmsLayerInfos)
            {   
                if (wmsVisibleLayers.source.indexOf(wmsLayerInfo.name) !== -1)
                {    
                    addChild(createWMSTocLayer(this, wmsLayerInfo));
                }
            }
        }
        else if (_layerLegendInfos && _layerLegendInfos.length)
        {
            for each (var layerLegendInfo:LayerLegendInfo in _layerLegendInfos)
            {
                for each (var legendItemInfo:LegendItemInfo in layerLegendInfo.legendItemInfos)
                {
                    addChild(new TocLegendItem(this, legendItemInfo));
                }
            }            
        }
    }

    private function getActualVisibleLayers(layerIds:Array, layerInfos:Array):Array
    {
        var result:Array = [];

        layerIds = layerIds ? layerIds.concat() : null;
        var layerInfo:LayerInfo;
        var layerIdIndex:int;

        if (layerIds)
        {
            // replace group layers with their sub layers
            for each (layerInfo in layerInfos)
            {
                layerIdIndex = layerIds.indexOf(layerInfo.layerId);
                if (layerInfo.subLayerIds && layerIdIndex != -1)
                {
                    layerIds.splice(layerIdIndex, 1); // remove the group layer id
                    for each (var subLayerId:Number in layerInfo.subLayerIds)
                    {
                        layerIds.push(subLayerId); // add subLayerId
                    }
                }
            }

            //copying layerInfos as Array#reverse() is destructive.
            var reversedLayerInfos:Array = layerInfos.concat();
            reversedLayerInfos.reverse();
            for each (layerInfo in reversedLayerInfos)
            {
                if (layerIds.indexOf(layerInfo.layerId) != -1 && layerIds.indexOf(layerInfo.parentLayerId) == -1 && layerInfo.parentLayerId != -1)
                {
                    layerIds.push(layerInfo.parentLayerId);
                }
            }

            result = layerIds;
        }

        return result;
    }

    private function createKMLLayerTocItems(parentItem:TocItem, layer:KMLLayer, isNetworkLink:Boolean = false):void
    {
        for each (var folder:KMLFolder in layer.folders)
        {
            if (folder.parentFolderId == -1)
            {
                if (isNetworkLink)
                {
                    if (folder.subFolderIds && folder.subFolderIds.length)
                    {
                        parentItem.addChild(createKmlFolderTocItem(parentItem, folder, layer.folders, layer));
                    }
                }
                else
                {
                    parentItem.addChild(createKmlFolderTocItem(parentItem, folder, layer.folders, layer));
                }
            }
        }

        for each (var networkLink:KMLLayer in layer.networkLinks)
        {
            // If the parent folder exists , do not create NetworkLinkItem as it is already created
            if (!(hasParentFolder(Number(networkLink.id), layer.folders)))
            {
                // add network link
                parentItem.addChild(createKmlNetworkLinkTocItem(parentItem, networkLink, layer));
            }
        }
    }

    private function hasParentFolder(id:Number, folders:Array):Boolean
    {
        // find the immediate parent folder
        var parentFolderFound:Boolean;

        for (var i:int = 0; i < folders.length; )
        {
            for (var j:int = 0; j < KMLFolder(folders[i]).featureInfos.length; )
            {
                if (id == KMLFolder(folders[i]).featureInfos[j].id)
                {
                    parentFolderFound = true;
                    break;
                }
                else
                {
                    j++
                }
            }
            if (parentFolderFound)
            {
                break;
            }
            else
            {
                i++;
            }
        }

        return parentFolderFound;
    }

    private function findRootLayers(layerInfos:Array):Array // of LayerInfo
    {
        var roots:Array = [];
        for each (var layerInfo:LayerInfo in layerInfos)
        {
            // ArcGIS: parentLayerId is -1
            // ArcIMS: parentLayerId is NaN
            if (isNaN(layerInfo.parentLayerId) || layerInfo.parentLayerId == -1)
            {
                roots.push(layerInfo);
            }
        }
        return roots;
    }

    private function findLayerById(id:Number, layerInfos:Array):LayerInfo
    {
        for each (var layerInfo:LayerInfo in layerInfos)
        {
            if (id == layerInfo.layerId)
            {
                return layerInfo;
            }
        }
        return null;
    }

    private function createTocLayer(parentItem:TocItem, layerInfo:LayerInfo, layerInfos:Array, isVisible:Boolean, isInScaleRange:Boolean):TocLayerInfoItem
    {
        var item:TocLayerInfoItem = new TocLayerInfoItem(parentItem, layerInfo, isVisible, isInScaleRange);

        // Handle any sublayers of a group layer
        if (layerInfo.subLayerIds)
        {
            for each (var childId:Number in layerInfo.subLayerIds)
            {
                var childLayer:LayerInfo = findLayerById(childId, layerInfos);
                if (childLayer)
                {
                    item.addChild(createTocLayer(item, childLayer, layerInfos, childLayer.defaultVisibility, item.isInScaleRange && isTocLayerInfoItemInScale(childLayer)));
                }
            }
        }
        else
        {
            var layerLegendInfo:LayerLegendInfo = getLayerLegendInfo(layerInfo.layerId);
            if (layerLegendInfo)
            {
                for each (var legendItemInfo:LegendItemInfo in layerLegendInfo.legendItemInfos)
                {
                    item.addChild(new TocLegendItem(item, legendItemInfo));
                }
            }
        }

        return item;
    }
    
    private function createWMSTocLayer(parentItem:TocItem, wmsLayerInfo:WMSLayerInfo):TocWMSLayerInfoItem
    {
        var item:TocWMSLayerInfoItem = new TocWMSLayerInfoItem(parentItem, wmsLayerInfo);
        
        var layerLegendInfo:LayerLegendInfo = getWMSLayerLegendInfo(wmsLayerInfo.name);
        if (layerLegendInfo)
        {
            for each (var legendItemInfo:LegendItemInfo in layerLegendInfo.legendItemInfos)
            {
                item.addChild(new TocLegendItem(item, legendItemInfo));
            }
        }
        
        return item;
    }
    
    private function getLayerLegendInfo(layerId:Number):LayerLegendInfo
    {
        var result:LayerLegendInfo;

        for each (var layerLegendInfo:LayerLegendInfo in _layerLegendInfos)
        {
            if (Number(layerLegendInfo.layerId) == layerId)
            {
                result = layerLegendInfo;
                break;
            }
        }

        return result;
    }
    
    private function getWMSLayerLegendInfo(wmsLayerId:String):LayerLegendInfo
    {
        var result:LayerLegendInfo;
        
        for each (var layerLegendInfo:LayerLegendInfo in _layerLegendInfos)
        {
            if (layerLegendInfo.layerId == wmsLayerId)
            {
                result = layerLegendInfo;
                break;
            }
        }
        
        return result;
    }

    private function createKmlFolderTocItem(parentItem:TocItem, folder:KMLFolder, folders:Array, layer:KMLLayer):TocKmlFolderItem
    {
        var item:TocKmlFolderItem = new TocKmlFolderItem(parentItem, folder, layer);

        // Handle any sublayers of a group layer
        if (folder.subFolderIds && folder.subFolderIds.length > 0)
        {
            var lookInFeatureInfos:Boolean = true;
            for each (var childId:Number in folder.subFolderIds)
            {
                var childFolder:KMLFolder = findFolderById(childId, folders);
                if (childFolder)
                {
                    item.addChild(createKmlFolderTocItem(item, childFolder, folders, layer));
                }
            }
        }
        if (folder.featureInfos && folder.featureInfos.length > 0)
        {
            for each (var featureInfo:KMLFeatureInfo in folder.featureInfos)
            {
                if (featureInfo.type == KMLFeatureInfo.NETWORK_LINK)
                {
                    var networkLink:KMLLayer = layer.getFeature(featureInfo) as KMLLayer;
                    item.addChild(createKmlNetworkLinkTocItem(item, networkLink, layer));
                }
            }
        }
        return item;
    }

    private function createKmlNetworkLinkTocItem(item:TocItem, networkLink:KMLLayer, layer:KMLLayer):TocKmlNetworkLinkItem
    {
        var tocKmlNetworkLinkItem:TocKmlNetworkLinkItem = new TocKmlNetworkLinkItem(item, networkLink, layer);
        if (networkLink.loaded)
        {
            createKMLLayerTocItems(tocKmlNetworkLinkItem, networkLink, true); // as network link is also a type of KMLLayer
        }
        else
        {
            networkLink.addEventListener(LayerEvent.LOAD, layerLoadHandler);
        }

        function layerLoadHandler(event:LayerEvent):void
        {
            createKMLLayerTocItems(tocKmlNetworkLinkItem, networkLink, true);
        }

        return tocKmlNetworkLinkItem;
    }

    private function findFolderById(id:Number, allFolders:Array):KMLFolder
    {
        var match:KMLFolder;

        for (var i:int = 0; i < allFolders.length; )
        {
            if (allFolders[i].id == id)
            {
                match = allFolders[i] as KMLFolder;
                break;
            }
            else
            {
                i++;
            }
        }

        return match;
    }
}

}
