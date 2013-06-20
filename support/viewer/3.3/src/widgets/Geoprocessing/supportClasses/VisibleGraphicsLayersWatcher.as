///////////////////////////////////////////////////////////////////////////
// Copyright (c) 2011 Esri. All Rights Reserved.
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
package widgets.Geoprocessing.supportClasses
{

import com.esri.ags.Map;
import com.esri.ags.events.LayerEvent;
import com.esri.ags.events.MapEvent;
import com.esri.ags.layers.GraphicsLayer;
import com.esri.ags.layers.Layer;

import flash.events.EventDispatcher;

import mx.collections.ArrayCollection;
import mx.collections.ArrayList;
import mx.events.FlexEvent;

public class VisibleGraphicsLayersWatcher extends EventDispatcher
{
    private var isWatching:Boolean;
    private var mapLayers:ArrayCollection;

    [Bindable]
    private var _visibleGraphicsLayers:ArrayList;

    public function get visibleGraphicsLayers():ArrayList
    {
        return _visibleGraphicsLayers ||= new ArrayList();
    }

    private var _map:Map

    public function get map():Map
    {
        return _map;
    }

    public function set map(value:Map):void
    {
        if (value !== _map)
        {
            _map = value;
            mapLayers = _map.layers as ArrayCollection;
        }
    }

    public function startWatching():void
    {
        if (!isWatching && _map)
        {
            isWatching = true;

            addMapEventListeners();
            addMapGraphicsLayersListeners();
            updateVisibleGraphicsLayersFromMap();
        }
    }

    private function addMapEventListeners():void
    {
        map.addEventListener(MapEvent.LAYER_ADD, map_layerAddHandler, false, 0, true);
        map.addEventListener(MapEvent.LAYER_REMOVE, map_layerRemoveHandler, false, 0, true);
        map.addEventListener(MapEvent.LAYER_REMOVE_ALL, map_layerRemoveAllHandler, false, 0, true);
    }

    private function map_layerAddHandler(event:MapEvent):void
    {
        removeMapGraphicsLayersListeners();
        addMapGraphicsLayersListeners();
        updateVisibleGraphicsLayersFromMap();
    }

    private function removeMapGraphicsLayersListeners():void
    {
        var graphicsLayer:GraphicsLayer;

        for each (var layer:Layer in mapLayers)
        {
            graphicsLayer = layer as GraphicsLayer;
            if (graphicsLayer)
            {
                graphicsLayer.removeEventListener(FlexEvent.SHOW, graphicsLayer_showHandler);
                graphicsLayer.removeEventListener(FlexEvent.HIDE, graphicsLayer_hideHandler);
                graphicsLayer.removeEventListener(LayerEvent.IS_IN_SCALE_RANGE_CHANGE, graphicsLayer_isInScaleRangeChangeHandler);
            }
        }
    }

    private function graphicsLayer_showHandler(event:FlexEvent):void
    {
        updateVisibleGraphicsLayersFromMap();
    }

    private function graphicsLayer_hideHandler(event:FlexEvent):void
    {
        updateVisibleGraphicsLayersFromMap();
    }

    private function graphicsLayer_isInScaleRangeChangeHandler(event:LayerEvent):void
    {
        updateVisibleGraphicsLayersFromMap();
    }

    private function updateVisibleGraphicsLayersFromMap():void
    {
        var visibleGraphicsLayersSource:Array = [];
        var graphicsLayer:GraphicsLayer;

        for each (var layer:Layer in mapLayers)
        {
            graphicsLayer = layer as GraphicsLayer;
            if (graphicsLayer && graphicsLayer.visible)
            {
                visibleGraphicsLayersSource.push(graphicsLayer);
            }
        }

        visibleGraphicsLayers.source = visibleGraphicsLayersSource;
    }

    private function map_layerRemoveHandler(event:MapEvent):void
    {
        removeMapGraphicsLayersListeners();
        addMapGraphicsLayersListeners();
        updateVisibleGraphicsLayersFromMap();
    }

    private function map_layerRemoveAllHandler(event:MapEvent):void
    {
        removeMapGraphicsLayersListeners();
        addMapGraphicsLayersListeners();
        updateVisibleGraphicsLayersFromMap();
    }

    private function addMapGraphicsLayersListeners():void
    {
        var graphicsLayer:GraphicsLayer;

        for each (var layer:Layer in mapLayers)
        {
            graphicsLayer = layer as GraphicsLayer;
            if (graphicsLayer)
            {
                graphicsLayer.addEventListener(FlexEvent.SHOW, graphicsLayer_showHandler, false, 0, true);
                graphicsLayer.addEventListener(FlexEvent.HIDE, graphicsLayer_hideHandler, false, 0, true);
                graphicsLayer.addEventListener(LayerEvent.IS_IN_SCALE_RANGE_CHANGE, graphicsLayer_isInScaleRangeChangeHandler, false, 0, true);
            }
        }
    }

    public function stopWatching():void
    {
        if (isWatching && _map)
        {
            isWatching = false;

            removeMapEventListeners();
            removeMapGraphicsLayersListeners();
        }
    }

    private function removeMapEventListeners():void
    {
        map.removeEventListener(MapEvent.LAYER_ADD, map_layerAddHandler);
        map.removeEventListener(MapEvent.LAYER_REMOVE, map_layerRemoveHandler);
        map.removeEventListener(MapEvent.LAYER_REMOVE_ALL, map_layerRemoveAllHandler);
    }
}
}
