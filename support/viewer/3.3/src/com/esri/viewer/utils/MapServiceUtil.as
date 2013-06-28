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

import com.esri.ags.layers.supportClasses.LayerInfo;

public class MapServiceUtil
{
    public static function getVisibleSubLayers(layerInfos:Array, layerIds:Array = null):Array
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
            result = layerIds;
        }
        else
        {
            result = getDefaultVisibleLayers(layerInfos);
        }

        // remove group layers
        for each (layerInfo in layerInfos)
        {
            if (layerInfo.subLayerIds)
            {
                layerIdIndex = result.indexOf(layerInfo.layerId);
                if (layerIdIndex != -1)
                {
                    result.splice(layerIdIndex, 1);
                }
            }
        }

        return result;
    }

    private static function getDefaultVisibleLayers(layerInfos:Array):Array
    {
        var result:Array = [];

        for each (var layerInfo:LayerInfo in layerInfos)
        {
            if (layerInfo.parentLayerId >= 0 && result.indexOf(layerInfo.parentLayerId) == -1)
            {
                // layer is not visible if it's parent is not visible
                continue;
            }
            if (layerInfo.defaultVisibility)
            {
                result.push(layerInfo.layerId);
            }
        }

        return result;
    }
}

}
