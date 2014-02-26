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
package com.esri.viewer.components.toc
{

import com.esri.ags.Map;
import com.esri.ags.events.MapEvent;
import com.esri.ags.layers.FeatureLayer;
import com.esri.ags.layers.GraphicsLayer;
import com.esri.ags.layers.Layer;
import com.esri.viewer.components.toc.tocClasses.TocItem;
import com.esri.viewer.components.toc.tocClasses.TocItemRenderer;
import com.esri.viewer.components.toc.tocClasses.TocMapLayerItem;
import com.esri.viewer.components.toc.utils.MapUtil;

import flash.events.Event;

import mx.collections.ArrayCollection;
import mx.controls.Tree;
import mx.core.ClassFactory;
import mx.effects.Effect;
import mx.effects.Fade;
import mx.events.CollectionEvent;
import mx.events.CollectionEventKind;
import mx.events.ListEvent;

//--------------------------------------
//  Other metadata
//--------------------------------------

/**
 * A tree-based Table of Contents component for a Map.
 *
 * @private
 */
public class TOC extends Tree
{
    /**
     * Creates a new TOC object.
     *
     * @param map The map that is linked to this TOC.
     */
    public function TOC(map:Map = null)
    {
        super();

        dataProvider = _tocRoots;
        itemRenderer = new ClassFactory(TocItemRenderer);
        iconFunction = tocItemIcon;
        dataTipField = "label";
        showDataTips = true;

        this.map = map;

        // Double click support for expanding/collapsing tree branches
        doubleClickEnabled = true;
        addEventListener(ListEvent.ITEM_DOUBLE_CLICK, onItemDoubleClick);

        // Set default styles
        setStyle("borderStyle", "none");
    }

    //--------------------------------------------------------------------------
    //
    //  Variables
    //
    //--------------------------------------------------------------------------

    // The tree data provider
    private var _tocRoots:ArrayCollection = new ArrayCollection(); // of TocItem

    private var _map:Map;
    private var _mapChanged:Boolean = false;

    //toc style
    private var _isMapServiceOnly:Boolean = false;

    // Layer list filters
    private var _includeLayers:ArrayCollection;
    private var _excludeLayers:ArrayCollection;
    private var _excludeGraphicsLayers:Boolean = false;

    private var _layerFiltersChanged:Boolean = false;

    private var _basemapLayers:ArrayCollection;

    // Label function for TocMapLayerItem
    private var _labelFunction:Function = null;
    private var _labelFunctionChanged:Boolean = false;

    // The effect used on layer show/hide
    private var _fade:Effect;
    private var _fadeDuration:Number = 250; // milliseconds
    private var _useLayerFadeEffect:Boolean = false;
    private var _useLayerFadeEffectChanged:Boolean = false;

    // include legend items
    private var _includeLegendItems:Boolean;
    // show layer menu
    private var _showLayerMenu:Boolean;

    private var _expandLayerItemsPending:Boolean;
    private var _pendingItemsToExpand:Array = [];

    //--------------------------------------------------------------------------
    //
    //  Properties
    //
    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------
    //  expandLayerItems
    //--------------------------------------------------------------------------

    [Bindable("expandLayerItemsChanged")]
    private var _expandLayerItems:Boolean;

    public function get expandLayerItems():Boolean
    {
        return _expandLayerItems;
    }

    public function set expandLayerItems(value:Boolean):void
    {
        if (value != _expandLayerItems)
        {
            _expandLayerItems = value;
            _expandLayerItemsPending = true;

            if (_expandLayerItems)
            {
                //need to expand all current TOC items
                _pendingItemsToExpand = _pendingItemsToExpand.concat(_tocRoots.source);
            }
            else
            {
                _pendingItemsToExpand.length = 0;
            }

            invalidateProperties();
            dispatchEvent(new Event("expandLayerItemsChanged"));
        }
    }

    //--------------------------------------------------------------------------
    //  map
    //--------------------------------------------------------------------------

    [Bindable("mapChanged")]
    /**
     * The Map to which this TOC is attached.
     */
    public function get map():Map
    {
        return _map;
    }

    /**
     * @private
     */
    public function set map(value:Map):void
    {
        if (value != _map)
        {
            removeMapListeners();
            _map = value;
            addMapListeners();

            _mapChanged = true;
            invalidateProperties();

            dispatchEvent(new Event("mapChanged"));
        }
    }

    [Bindable("mapServiceOnlyChanged")]
    public function get isMapServiceOnly():Boolean
    {
        return _isMapServiceOnly;
    }

    public function set isMapServiceOnly(value:Boolean):void
    {
        _isMapServiceOnly = value;
        dispatchEvent(new Event("mapServiceOnlyChanged"));
    }

    //--------------------------------------------------------------------------
    //  includeLayers
    //--------------------------------------------------------------------------

    [Bindable("includeLayersChanged")]
    /**
     * A list of layer objects and/or layer IDs to include in the TOC.
     */
    public function get includeLayers():Object
    {
        return _includeLayers;
    }

    /**
     * @private
     */
    public function set includeLayers(value:Object):void
    {
        removeLayerFilterListeners(_includeLayers);
        _includeLayers = normalizeLayerFilter(value);
        addLayerFilterListeners(_includeLayers);
        onFilterChange();
        dispatchEvent(new Event("includeLayersChanged"));
    }

    //--------------------------------------------------------------------------
    //  excludeLayers
    //--------------------------------------------------------------------------

    [Bindable("excludeLayersChanged")]
    /**
     * A list of layer objects and/or layer IDs to exclude from the TOC.
     */
    public function get excludeLayers():Object
    {
        return _excludeLayers;
    }

    /**
     * @private
     */
    public function set excludeLayers(value:Object):void
    {
        removeLayerFilterListeners(_excludeLayers);
        _excludeLayers = normalizeLayerFilter(value);
        addLayerFilterListeners(_excludeLayers);

        onFilterChange();
        dispatchEvent(new Event("excludeLayersChanged"));
    }

    //--------------------------------------------------------------------------
    //  excludeGraphicsLayers
    //--------------------------------------------------------------------------

    [Bindable]
    [Inspectable(category="Mapping", defaultValue="false")]
    /**
     * Whether to exclude all GraphicsLayer map layers from the TOC.
     *
     * @default false
     */
    public function get excludeGraphicsLayers():Boolean
    {
        return _excludeGraphicsLayers;
    }

    /**
     * @private
     */
    public function set excludeGraphicsLayers(value:Boolean):void
    {
        _excludeGraphicsLayers = value;

        onFilterChange();
    }

    //--------------------------------------------------------------------------
    //  basemapLayers
    //--------------------------------------------------------------------------

    /**
     * A list of basemap layer objects and/or layer IDs.
     */
    public function get basemapLayers():Object
    {
        return _basemapLayers;
    }

    /**
     * @private
     */
    public function set basemapLayers(value:Object):void
    {
        _basemapLayers = normalizeLayerFilter(value);
    }

    //--------------------------------------------------------------------------
    //  labelFunction
    //--------------------------------------------------------------------------

    /**
     * A label function for map layers.
     *
     * The function signature must be: <code>labelFunc( layer : Layer ) : String</code>
     */
    override public function set labelFunction(value:Function):void
    {
        // CAUTION: We are overriding the semantics and method signature of the
        //   super Tree's labelFunction, so do not set the super.labelFunction property.
        //
        //   Also, we must reference the function using "_labelFunction" instead of
        //   "labelFunction" since the latter will call the getter method on Tree,
        //   rather than grabbing this TOC's instance variable.

        _labelFunction = value;

        _labelFunctionChanged = true;
        invalidateProperties();
    }

    //--------------------------------------------------------------------------
    //  useLayerFadeEffect
    //--------------------------------------------------------------------------

    [Bindable("useLayerFadeEffectChanged")]
    [Inspectable(category="Mapping", defaultValue="false")]
    /**
     * Whether to use a Fade effect when the map layers are shown or hidden.
     *
     * @default false
     */
    public function get useLayerFadeEffect():Boolean
    {
        return _useLayerFadeEffect;
    }

    /**
     * @private
     */
    public function set useLayerFadeEffect(value:Boolean):void
    {
        if (value != _useLayerFadeEffect)
        {
            _useLayerFadeEffect = value;

            _useLayerFadeEffectChanged = true;
            invalidateProperties();

            dispatchEvent(new Event("useLayerFadeEffectChanged"));
        }
    }

    //--------------------------------------------------------------------------
    //  includeLegendItems
    //--------------------------------------------------------------------------

    [Bindable("includeLegendItemsChanged")]
    /**
     * Whether to include legend items.
     */
    public function get includeLegendItems():Boolean
    {
        return _includeLegendItems;
    }

    /**
     * @private
     */
    public function set includeLegendItems(value:Boolean):void
    {
        _includeLegendItems = value;

        onFilterChange();
        dispatchEvent(new Event("includeLegendItemsChanged"));
    }
    
    //--------------------------------------------------------------------------
    //  showLayerMenu
    //--------------------------------------------------------------------------
    
    [Bindable("showLayerMenuChanged")]
    /**
     * Whether to show layer menu.
     */
    public function get showLayerMenu():Boolean
    {
        return _showLayerMenu;
    }
    
    /**
     * @private
     */
    public function set showLayerMenu(value:Boolean):void
    {
        _showLayerMenu = value;        
        dispatchEvent(new Event("showLayerMenuChanged"));
    }

    //--------------------------------------------------------------------------
    //
    //  Overridden Methods
    //
    //--------------------------------------------------------------------------

    /**
     * @private
     */
    override protected function commitProperties():void
    {
        super.commitProperties();

        if (_mapChanged || _layerFiltersChanged || _labelFunctionChanged)
        {
            _mapChanged = false;
            _layerFiltersChanged = false;
            _labelFunctionChanged = false;

            // Repopulate the TOC data provider
            registerAllMapLayers();
        }

        if (_useLayerFadeEffectChanged)
        {
            _useLayerFadeEffectChanged = false;

            MapUtil.forEachMapLayer(map, function(layer:Layer):void
            {
                setLayerFadeEffect(layer);
            });
        }

        if (_expandLayerItemsPending)
        {
            _expandLayerItemsPending = false;
            if (expandLayerItems)
            {
                expandPendingItems();
            }
        }
    }

    //--------------------------------------------------------------------------
    //
    //  Methods
    //
    //--------------------------------------------------------------------------

    private function expandPendingItems():void
    {
        if (_pendingItemsToExpand.length > 0)
        {
            for each (var tocItem:TocItem in _pendingItemsToExpand)
            {
                expandChildrenOf(tocItem, true);
            }
            _pendingItemsToExpand.length = 0;
        }
    }

    private function addMapListeners():void
    {
        if (map)
        {
            map.addEventListener(MapEvent.LAYER_ADD, onLayerAdd, false, 0, true);
            map.addEventListener(MapEvent.LAYER_REMOVE, onLayerRemove, false, 0, true);
            map.addEventListener(MapEvent.LAYER_REMOVE_ALL, onLayerRemoveAll, false, 0, true);
            map.addEventListener(MapEvent.LAYER_REORDER, onLayerReorder, false, 0, true);
        }
    }

    private function removeMapListeners():void
    {
        if (map)
        {
            map.removeEventListener(MapEvent.LAYER_ADD, onLayerAdd);
            map.removeEventListener(MapEvent.LAYER_REMOVE, onLayerRemove);
            map.removeEventListener(MapEvent.LAYER_REMOVE_ALL, onLayerRemoveAll);
            map.removeEventListener(MapEvent.LAYER_REORDER, onLayerReorder);
        }
    }

    /**
     * Registers the new map layer in the TOC tree.
     */
    private function onLayerAdd(event:MapEvent):void
    {
        registerMapLayer(event.layer);
    }

    private function onLayerRemove(event:MapEvent):void
    {
        unregisterMapLayer(event.layer);
    }

    private function onLayerRemoveAll(event:MapEvent):void
    {
        unregisterAllMapLayers();
    }

    private function onLayerReorder(event:MapEvent):void
    {
        for each (var item:Object in this.dataProvider)
        {
            this.expandItem(item, false);
        }

        var layer:Layer = event.layer;
        var index:int = getEffectiveTOCIndex(layer, event.index);

        if (isGraphicsLayer(layer) || isHiddenLayer(layer) || isLayerExcluded(layer))
        {
            return;
        }

        var i:int;
        var currentTOCIndex:int;
        var currentItem:Object;
        // remove hidden layers, to get the correct layerIds count
        var newLayerIds:Array = getNewLayerIds(map.layerIds);
        if (index <= (newLayerIds.length - _tocRoots.length)) // move this item to the bottom of toc
        {
            // index of item to move
            currentTOCIndex = getCurrentTOCIndex();
            // item to move
            currentItem = _tocRoots.getItemAt(currentTOCIndex);

            for (i = currentTOCIndex; i < _tocRoots.length; i++)
            {
                if (i == _tocRoots.length - 1)
                {
                    _tocRoots.setItemAt(currentItem, _tocRoots.length - 1);
                }
                else
                {
                    _tocRoots.setItemAt(_tocRoots.getItemAt(i + 1), i);
                }
            }
        }
        else if ((newLayerIds.length - _tocRoots.length < index) && (index < newLayerIds.length))
        {
            // index of item to move
            currentTOCIndex = getCurrentTOCIndex();
            // item to move
            currentItem = _tocRoots.getItemAt(currentTOCIndex);

            var newTOCIndex:Number = newLayerIds.length - index - 1;
            if (newTOCIndex < currentTOCIndex)
            {
                for (i = currentTOCIndex; newTOCIndex <= i; i--)
                {
                    if (i == newTOCIndex)
                    {
                        _tocRoots.setItemAt(currentItem, newTOCIndex);
                    }
                    else
                    {
                        _tocRoots.setItemAt(_tocRoots.getItemAt(i - 1), i);
                    }
                }
            }
            else
            {

                for (i = currentTOCIndex; i <= newTOCIndex; i++)
                {
                    if (i == newTOCIndex)
                    {
                        _tocRoots.setItemAt(currentItem, newTOCIndex);
                    }
                    else
                    {
                        _tocRoots.setItemAt(_tocRoots.getItemAt(i + 1), i);
                    }
                }
            }
        }

        function getCurrentTOCIndex():int
        {
            var result:int;
            for (i = 0; i < _tocRoots.length; i++)
            {
                if (_tocRoots.getItemAt(i) is TocMapLayerItem && TocMapLayerItem(_tocRoots.getItemAt(i)).layer === layer)
                {
                    result = i;
                    break;
                }
            }
            return result;
        }
    }

    private function getEffectiveTOCIndex(layer:Layer, index:Number):int
    {
        var result:int = 0;
        for each (var layerId:String in getNewLayerIds(map.layerIds))
        {
            if (layer.id == layerId)
            {
                break;
            }
            result++;
        }
        return result;
    }

    private function getNewLayerIds(layerIds:Array):Array
    {
        var result:Array = [];
        var mapLayers:ArrayCollection = ArrayCollection(map.layers);
        for (var i:int = 0; i < layerIds.length; i++)
        {
            var layer:Layer = mapLayers.getItemAt(i) as Layer
            if (isHiddenLayer(layer) || isGraphicsLayer(layer) || islayerExcludedAndNotBaseMap(layer))
            {
                continue;
            }
            result.push(layerIds[i]);
        }
        return result;
    }

    private function isGraphicsLayer(layer:Layer):Boolean
    {
        return (layer is GraphicsLayer) && !(layer is FeatureLayer);
    }

    private function isHiddenLayer(layer:Layer):Boolean
    {
        return layer.name.indexOf("hiddenLayer_") > -1;
    }

    private function isLayerExcluded(layer:Layer):Boolean
    {
        var exclude:Boolean;
        for each (var item:* in excludeLayers)
        {
            if ((item === layer || item == layer.name) || (item == layer.id))
            {
                exclude = true;
                break;
            }
        }
        return exclude;
    }

    private function islayerExcludedAndNotBaseMap(layer:Layer):Boolean
    {
        var exclude:Boolean;
        for each (var item:* in excludeLayers)
        {
            if ((item === layer || item == layer.name) || (item == layer.id))
            {
                exclude = true;
                for each (var item1:* in basemapLayers)
                {
                    if (item1 === item)
                    {
                        exclude = false;
                        break;
                    }
                }
                if (!exclude)
                {
                    break;
                }
            }
        }
        return exclude;
    }

    private function unregisterAllMapLayers():void
    {
        _tocRoots.removeAll();
    }

    private function unregisterMapLayer(layer:Layer):void
    {
        for (var i:int = 0; i < _tocRoots.length; i++)
        {
            var item:Object = _tocRoots[i];
            if (item is TocMapLayerItem && TocMapLayerItem(item).layer === layer)
            {
                var tocItem:TocItem = _tocRoots.removeItemAt(i) as TocItem;
                tocItem.removeEventListener(Event.CHANGE, tocItem_childAddedHandler);
                break;
            }
        }
    }

    /**
     * Registers all existing map layers in the TOC tree.
     */
    private function registerAllMapLayers():void
    {
        unregisterAllMapLayers();
        MapUtil.forEachMapLayer(map, function(layer:Layer):void
        {
            registerMapLayer(layer);
        });
    }

    /**
     * Creates a new top-level TOC item for the specified map layer.
     */
    private function registerMapLayer(layer:Layer):void
    {
        if (filterOutLayer(layer))
        {
            return;
        }

        // Init any layer properties, styles, and effects
        if (useLayerFadeEffect)
        {
            setLayerFadeEffect(layer);
        }

        var tocItem:TocMapLayerItem = new TocMapLayerItem(layer, _labelFunction, _isMapServiceOnly, _includeLegendItems);
        tocItem.addEventListener(Event.CHANGE, tocItem_childAddedHandler, false, 0, true);
        // need to get the true index of this layer in the map, removing any graphics layers from the equation if necessary as well as any exclude layers
        var trueMapLayerIndex:Number = 0;
        for each (var mapLayer:Layer in this.map.layers)
        {
            if (mapLayer == layer)
            {
                break;
            }

            if (!filterOutLayer(mapLayer)) // only increase the index if this layer is in the TOC
            {
                trueMapLayerIndex++;
            }
        }
        // now add at the correct index
        _tocRoots.addItemAt(tocItem, _tocRoots.length - trueMapLayerIndex);
    }

    private function tocItem_childAddedHandler(event:Event):void
    {
        if (expandLayerItems)
        {
            _expandLayerItemsPending = true;
            _pendingItemsToExpand.push(event.currentTarget as TocItem);
            invalidateProperties();
        }
    }

    private function setLayerFadeEffect(layer:Layer):void
    {
        if (useLayerFadeEffect)
        {
            // Lazy load the effect
            if (!_fade)
            {
                _fade = new Fade();
                _fade.duration = _fadeDuration;
            }
            layer.setStyle("showEffect", _fade);
            layer.setStyle("hideEffect", _fade);
        }
        else
        {
            layer.clearStyle("showEffect");
            layer.clearStyle("hideEffect");
        }
    }

    private function addLayerFilterListeners(filter:ArrayCollection):void
    {
        if (filter)
        {
            filter.addEventListener(CollectionEvent.COLLECTION_CHANGE, onFilterChange, false, 0, true);
        }
    }

    private function removeLayerFilterListeners(filter:ArrayCollection):void
    {
        if (filter)
        {
            filter.removeEventListener(CollectionEvent.COLLECTION_CHANGE, onFilterChange);
        }
    }

    private function onFilterChange(event:CollectionEvent = null):void
    {
        var isValidChange:Boolean = false;

        if (event == null)
        {
            // Called directly from the setters
            isValidChange = true;
        }
        else
        {
            // Only act on certain kinds of collection changes.
            // Specifically, avoid acting on the UPDATE kind.
            // It causes unwanted refresh of the TOC model.
            switch (event.kind)
            {
                case CollectionEventKind.ADD:
                case CollectionEventKind.REMOVE:
                case CollectionEventKind.REPLACE:
                case CollectionEventKind.REFRESH:
                case CollectionEventKind.RESET:
                {
                    isValidChange = true;
                }
            }
        }

        if (isValidChange)
        {
            _layerFiltersChanged = true;
            invalidateProperties();
        }
    }

    public function filterOutLayer(layer:Layer):Boolean
    {
        var exclude:Boolean = false;
        if (excludeGraphicsLayers && isGraphicsLayer(layer))
        {
            exclude = true;
        }
        if (isHiddenLayer(layer))
        {
            exclude = true;
        }
        if (!exclude && excludeLayers)
        {
            exclude = isLayerExcluded(layer);
        }
        if (includeLayers)
        {
            exclude = true;
            for each (var item:* in includeLayers)
            {
                if (item === layer || item == layer.id)
                {
                    exclude = false;
                    break;
                }
            }
        }
        return exclude;
    }

    private function normalizeLayerFilter(value:Object):ArrayCollection
    {
        var filter:ArrayCollection;
        if (value is ArrayCollection)
        {
            filter = value as ArrayCollection;
        }
        else if (value is Array)
        {
            filter = new ArrayCollection(value as Array);
        }
        else if (value is String || value is Layer)
        {
            // Possibly a String (layer id) or Layer object
            filter = new ArrayCollection([ value ]);
        }
        else
        {
            // Unsupported value type
            filter = null;
        }
        return filter;
    }

    /**
     * Double click handler for expanding or collapsing a tree branch.
     */
    private function onItemDoubleClick(event:ListEvent):void
    {
        if (event.itemRenderer && event.itemRenderer.data)
        {
            var item:Object = event.itemRenderer.data;
            expandItem(item, !isItemOpen(item), true, true, event);
        }
    }

    private function tocItemIcon(item:Object):Class
    {
        if (item is TocMapLayerItem)
        {
            return getStyle("mapServiceIcon");
        }
        if (item is TocItem)
        {
            return TocItem(item).isGroupLayer()
                ? getStyle("groupLayerIcon")
                : getStyle("layerIcon");
        }
        return getStyle("layerIcon");
    }
}

}
