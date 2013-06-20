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

import mx.collections.ArrayCollection;

public class LayerOrderer
{
    private var _map:Map;

    public function get map():Map
    {
        return _map;
    }

    public function set map(value:Map):void
    {
        _map = value;
    }

    private var _layerOrder:Array;

    public function get layerOrder():Array
    {
        return _layerOrder;
    }

    public function set layerOrder(value:Array):void
    {
        _layerOrder = value;
    }

    //NOTE: reorders layers from topmost layer
    public function orderLayers():void
    {
        if (layerOrder && map)
        {
            var totalLayersToOrder:int = layerOrder.length;
            var totalLayersOnMap:int = (map.layers as ArrayCollection).length - 1;
            for (var i:int = 0; i < totalLayersToOrder; i++)
            {
                map.reorderLayer(layerOrder[i], totalLayersOnMap - i);
            }
        }
    }
}

}
