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

//[IconFile("toc.gif")]

/**
 * A tree-based Table of Contents component for a Map.
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

    // Label function for TocMapLayerItem
    private var _labelFunction:Function = null;

    private var _labelFunctionChanged:Boolean = false;

    // The effect used on layer show/hide
    private var _fade:Effect;

    private var _fadeDuration:Number = 250; // milliseconds

    private var _useLayerFadeEffect:Boolean = false;

    private var _useLayerFadeEffectChanged:Boolean = false;

    //--------------------------------------------------------------------------
    //  Property:  map
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
    //  Property:  includeLayers
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
    //  Property:  excludeLayers
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
    //  Property:  excludeGraphicsLayers
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
    //  Property:  labelFunction
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
    //  Property:  useLayerFadeEffect
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
        var layer:Layer = event.layer;
        var index:int = event.index;

        var i:int;
        var currentTOCIndex:int;
        var currentItem:Object;
        if (index <= (map.layerIds.length - _tocRoots.length)) // move this item to the bottom of toc
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
        else if ((map.layerIds.length - _tocRoots.length) < index < map.layerIds.length)
        {
            // index of item to move
            currentTOCIndex = getCurrentTOCIndex();
            // item to move
            currentItem = _tocRoots.getItemAt(currentTOCIndex);

            var newTOCIndex:Number = map.layerIds.length - index - 1;
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
                _tocRoots.removeItemAt(i);
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
            //trace("Filtering out TOC layer '" + layer.id + "'");
            return;
        }

        // Init any layer properties, styles, and effects
        if (useLayerFadeEffect)
        {
            setLayerFadeEffect(layer);
        }

        // Add a new top-level TOC item at the beginning of the list (reverse rendering order)
        var tocItem:TocMapLayerItem = new TocMapLayerItem(layer, _labelFunction, _isMapServiceOnly);
        _tocRoots.addItemAt(tocItem, 0);
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

    private function filterOutLayer(layer:Layer):Boolean
    {
        var exclude:Boolean = false;
        if (excludeGraphicsLayers && layer is GraphicsLayer && !(layer is FeatureLayer))
        {
            exclude = true;
        }
        if (!exclude && excludeLayers)
        {
            exclude = false;
            for each (var item:* in excludeLayers)
            {
                if ((item === layer || item == layer.name) || (item == layer.id))
                {
                    exclude = true;
                    break;
                }
            }
        }
        if (includeLayers)
        {
            exclude = true;
            for each (item in includeLayers)
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
